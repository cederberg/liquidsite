/*
 * StandardZipPluginLocation.java
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
import java.net.MalformedURLException;
import java.net.URL;

import org.java.plugin.PluginManager;

/**
 * A standard jar or zip file plugin location.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class StandardZipPluginLocation implements PluginManager.PluginLocation {

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
