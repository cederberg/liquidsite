/*
 * TemplateManager.java
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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite;

import java.io.File;
import java.io.IOException;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * A simple FreeMarker template manager. This class provides static
 * methods that simplify the creation of templates.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class TemplateManager {

    /**
     * Initializes the template manager.
     * 
     * @param dir            the template file base directory
     * 
     * @throws IOException if the template base directory couldn't be
     *             read properly
     */
    public static void initialize(File dir) throws IOException {
        Configuration  config;
        
        config = Configuration.getDefaultConfiguration();
        config.clearTemplateCache();
        config.setStrictSyntaxMode(true);
        // TODO: config.setTemplateUpdateDelay(600);
        config.setDirectoryForTemplateLoading(dir);
    }

    /**
     * Returns a template from the file system. The file name must be
     * specified relative to the base directory used in the 
     * initialize() call.
     * 
     * @param path           the relative file name
     * 
     * @return the template found
     * 
     * @throws IOException if the template couldn't be read correctly
     */
    public static Template getFileTemplate(String path) 
        throws IOException {

        Configuration  config;

        config = Configuration.getDefaultConfiguration();
        return config.getTemplate(path);
    }
}
