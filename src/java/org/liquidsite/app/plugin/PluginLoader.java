/*
 * PluginLoader.java
 *
 * This work is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * Copyright (c) 2006 Per Cederberg. All rights reserved.
 */

package org.liquidsite.app.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.util.ExtendedProperties;

import org.liquidsite.util.log.Log;

/**
 * A plugin loader and manager. This class handles the initialization
 * and loading of the application plugins.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PluginLoader {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(PluginLoader.class);

    /**
     * The plugin manager used.
     */
    private static PluginManager manager = null;

    /**
     * Creates a new plugin manager.
     *
     * @param dir            the plugin work directory
     */
    private static void initialize(File dir) {
        ExtendedProperties properties = new ExtendedProperties();
        ObjectFactory      factory;

        LOG.info("initializing plugin loader for " + dir);
        properties.setProperty("org.java.plugin.PathResolver",
                               "org.java.plugin.standard.ShadingPathResolver");
        properties.setProperty("shadowFolder", dir.getAbsolutePath());
        properties.setProperty("unpackMode", "always");
        factory = ObjectFactory.newInstance(properties);
        manager = factory.createManager();
        try {
            manager.getPathResolver().configure(properties);
        } catch (Exception e) {
            LOG.error("plugin configuration error", e);
        }
    }

    /**
     * Initializes the plugin loader. All the plugins found in the
     * specified directory will automatically be loaded and
     * initialized.
     *
     * @param dir            the plugin directory
     *
     * @throws PluginException if some plugin failed to load or
     *             initialize correctly
     */
    public void startup(File dir) throws PluginException {
        Iterator          iter;
        PluginDescriptor  desc;
        String            msg;

        if (manager == null) {
            initialize(new File(dir, "tmp"));
        }
        try {
            manager.publishPlugins(getLocations(dir));
            iter = manager.getRegistry().getPluginDescriptors().iterator();
            while (iter.hasNext()) {
                desc = (PluginDescriptor) iter.next();
                LOG.info("activating plugin " + desc.getId() +
                         ", version " + desc.getVersion());
                if (!manager.isPluginActivated(desc)) {
                    manager.activatePlugin(desc.getId());
                }
            }
        } catch (MalformedURLException e) {
            msg = "invalid plugin URL";
            LOG.error(msg, e);
            throw new PluginException(msg, e);
        } catch (Exception e) {
            msg = "plugin startup error";
            LOG.error(msg, e);
            throw new PluginException(msg, e);
        }
    }

    /**
     * Shuts down the plugin loader and deinitializes all loaded
     * plugins.
     */
    public void shutdown() {
        Iterator          iter;
        PluginDescriptor  desc;

        try {
            iter = manager.getRegistry().getPluginDescriptors().iterator();
            while (iter.hasNext()) {
                desc = (PluginDescriptor) iter.next();
                LOG.info("deactivating plugin " + desc.getId() +
                         ", version " + desc.getVersion());
                if (manager.isPluginActivated(desc)) {
                    manager.deactivatePlugin(desc.getId());
                }
            }
        } catch (Exception e) {
            LOG.error("plugin shutdown error", e);
        }
    }

    /**
     * Finds all the plugin locations from the specified directory.
     *
     * @param dir            the plugin directory
     *
     * @return the array of plugin locations
     *
     * @throws MalformedURLException if some plugin URL couldn't be
     *             created
     */
    private StandardZipPluginLocation[] getLocations(File dir)
        throws MalformedURLException {

        StandardZipPluginLocation[] locations;
        File[]                      files;

        files = dir.listFiles(new PluginFilter());
        locations = new StandardZipPluginLocation[files.length];
        for (int i = 0; i < files.length; i++) {
            LOG.info("found plugin " + files[i].getName());
            locations[i] = new StandardZipPluginLocation(files[i]);
        }
        return locations;
    }

    /**
     * A simple plugin filename filter. This filter accepts all zip
     * files.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    private class PluginFilter implements FilenameFilter {

        /**
         * Checks if a file is accepted by the filter.
         *
         * @param dir            the file directory
         * @param name           the file name
         *
         * @return true if the file is accepted, or
         *         false otherwise
         */
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar") || name.endsWith(".zip");
        }
    }

    /**
     * A standard jar or zip file plugin location.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    private class StandardZipPluginLocation
        implements PluginManager.PluginLocation {

        /**
         * The context URL.
         */
        private URL context;

        /**
         * The plugin manifest URL.
         */
        private URL manifest;

        /**
         * Creates a new plugin location from a jar or a zip file.
         * This constructor assumes that the plugin manifest file is
         * named "plugin.xml" and is present in the root directory of
         * the jar file.
         *
         * @param file           the zip file
         *
         * @throws MalformedURLException if the plugin URL:s couldn't
         *             be created 
         */
        public StandardZipPluginLocation(File file)
            throws MalformedURLException {

            this(file, "plugin.xml");
        }

        /**
         * Creates a new plugin location from a jar or a zip file.
         * This plugin manifest file path specified is relative to
         * the root directory of the jar or zip file.
         *
         * @param file           the zip file
         * @param path           the relative manifest path
         *
         * @throws MalformedURLException if the plugin URL:s couldn't
         *             be created 
         */
        public StandardZipPluginLocation(File file, String path)
            throws MalformedURLException {

            if (path != null && path.startsWith("/")) {
                path = path.substring(1);
            }
            context = new URL("jar:file:" + file.getAbsolutePath() + "!/");
            manifest = new URL(context, path);
        }

        /**
         * Returns a string representation of this object.
         *
         * @return a string representation of this object
         */
        public String toString() {
            return context.toString();
        }

        /**
         * Returns the location of the plugin context directory.
         *
         * @return the location of the plugin context directory
         */
        public URL getContextLocation() {
            return context;
        }

        /**
         * Returns the location of the plugin manifest file.
         *
         * @return the location of the plugin manifest file
         */
        public URL getManifestLocation() {
            return manifest;
        }
    }
}
