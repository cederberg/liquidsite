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
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.util.ExtendedProperties;

import org.liquidsite.app.template.PluginBean;
import org.liquidsite.app.template.TemplateException;
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
     * The core plugin identifier.
     */
    private static final String CORE_PLUGIN_ID = "core";

    /**
     * The core plugin manifest location.
     */
    private static final String CORE_PLUGIN_MANIFEST = "core-plugin.xml";

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
        ExtensionPoint    point;
        String            msg;

        // Initialize plugin manager
        if (manager == null) {
            initialize(new File(dir, "tmp"));
        }
        try {
            manager.publishPlugins(getLocations(dir));
        } catch (MalformedURLException e) {
            msg = "invalid plugin URL";
            LOG.error(msg, e);
            throw new PluginException(msg, e);
        } catch (Exception e) {
            msg = "plugin startup error";
            LOG.error(msg + ": " + e.toString());
            throw new PluginException(msg, e);
        }

        // Load extension points
        desc = manager.getRegistry().getPluginDescriptor(CORE_PLUGIN_ID);
        point = desc.getExtensionPoint("TemplateBean");
        iter = point.getConnectedExtensions().iterator();
        while (iter.hasNext()) {
            loadTemplateBean((Extension) iter.next());
        }
    }

    /**
     * Shuts down the plugin loader and deinitializes all loaded
     * plugins.
     */
    public void shutdown() {
        PluginBean.removeAll();
        try {
            manager.shutdown();
        } catch (Exception e) {
            LOG.error("plugin shutdown error", e);
        }
        manager = null;
    }

    /**
     * Finds all the plugin locations from the specified directory.
     * The core plugin location will be added to the array returned. 
     *
     * @param dir            the plugin directory
     *
     * @return the array of plugin locations
     *
     * @throws MalformedURLException if some plugin URL couldn't be
     *             created
     */
    private PluginManager.PluginLocation[] getLocations(File dir)
        throws MalformedURLException {

        PluginManager.PluginLocation[] locations;
        File[]                         files;

        files = dir.listFiles(new PluginFilter());
        locations = new PluginManager.PluginLocation[1 + files.length];
        locations[0] = getCoreLocation();
        for (int i = 0; i < files.length; i++) {
            LOG.info("found plugin " + files[i].getName());
            locations[i + 1] = new StandardZipPluginLocation(files[i]);
        }
        return locations;
    }

    /**
     * Returns the location of the core plugin. The core plugin is used to
     * define the extension points available for normal plugins.
     *
     * @return the location of the core plugin
     */
    private StandardPluginLocation getCoreLocation() {
        URL  url;

        url = getClass().getResource(CORE_PLUGIN_MANIFEST);
        return new StandardPluginLocation(url, url);
    }

    /**
     * Loads a template bean extension.
     *
     * @param ext            the template bean extension
     *
     * @throws PluginException if the extension couldn't be loaded
     */
    private void loadTemplateBean(Extension ext) throws PluginException {
        PluginDescriptor  desc;
        String            name;
        String            className;
        Class             cls;
        String            msg;

        desc = ext.getDeclaringPluginDescriptor();
        name = ext.getParameter("name").valueAsString();
        className = ext.getParameter("class").valueAsString();
        LOG.info("loading plugin template bean '" + name + "' as " +
                 className + " from plugin " + desc.getId() +
                 ", version " + desc.getVersion());
        if (!manager.isPluginActivated(desc)) {
            try {
                manager.activatePlugin(desc.getId());
            } catch (PluginLifecycleException e) {
                msg = "failed to activate plugin " + desc.getId();
                LOG.error(msg, e);
                throw new PluginException(msg, e);
            }
        }
        try {
            cls = manager.getPluginClassLoader(desc).loadClass(className);
        } catch (ClassNotFoundException e) {
            msg = "failed to load class '" + className + "' in plugin " +
                  desc.getId();
            LOG.error(msg, e);
            throw new PluginException(msg, e);
        }
        try {
            PluginBean.add(name, cls);
        } catch (TemplateException e) {
            msg = "failed to plugin mapping '" + name + "' in plugin " +
                  desc.getId();
            LOG.error(msg, e);
            throw new PluginException(msg, e);
        }
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
}
