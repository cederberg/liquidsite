/*
 * StandardPluginLocation.java
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

import java.net.URL;

import org.java.plugin.PluginManager;

/**
 * A standard plugin location.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class StandardPluginLocation implements PluginManager.PluginLocation {

    /**
     * The context URL.
     */
    private URL context;

    /**
     * The plugin manifest URL.
     */
    private URL manifest;

    /**
     * Creates a new standard plugin location.
     *
     * @param context        the location of the plugin context directory
     * @param manifest       the location of the plugin manifest file
     */
    public StandardPluginLocation(URL context, URL manifest) {
        this.context = context;
        this.manifest = manifest;
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
