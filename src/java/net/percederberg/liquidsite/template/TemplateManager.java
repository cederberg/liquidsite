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

package net.percederberg.liquidsite.template;

import java.io.IOException;

import net.percederberg.liquidsite.Application;
import net.percederberg.liquidsite.Log;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;

/**
 * A simple FreeMarker template manager. This class provides static
 * methods that simplify the creation of templates.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class TemplateManager {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(TemplateManager.class);

    /**
     * The application object. 
     */
    private static Application application = null;

    /**
     * The file template configuration.
     */
    private static Configuration fileConfig = null;

    /**
     * Initializes the template manager.
     * 
     * @param app            the application object
     * 
     * @throws TemplateException if the template base directory 
     *             couldn't be read properly
     */
    public static void initialize(Application app) 
        throws TemplateException {

        application = app;
        fileConfig = Configuration.getDefaultConfiguration();
        fileConfig.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
        fileConfig.clearTemplateCache();
        fileConfig.setStrictSyntaxMode(true);
        // TODO: fileConfig.setTemplateUpdateDelay(600);
        try {
            fileConfig.setDirectoryForTemplateLoading(app.getBaseDir());
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new TemplateException("couldn't read " + 
                                        app.getBaseDir());
        }
    }
    
    /**
     * Returns the current template manager application object.
     * 
     * @return the current template manager application object
     */
    public static Application getApplication() {
        return application;
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
     * @throws TemplateException if the template couldn't be read correctly
     */
    public static Template getFileTemplate(String path) 
        throws TemplateException {

        try {
            return new Template(fileConfig.getTemplate(path));
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new TemplateException("couldn't read " + path, e);
        }
    }
}
