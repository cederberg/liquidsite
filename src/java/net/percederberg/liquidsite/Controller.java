/*
 * Controller.java
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

import net.percederberg.liquidsite.content.ContentManager;

/**
 * A request controller.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public abstract class Controller {

    /**
     * The application context.
     */
    private Application application;

    /**
     * Creates a new request controller.
     * 
     * @param app            the application context
     */
    public Controller(Application app) {
        this.application = app;
    }
    
    /**
     * Destroys this request controller. This method frees all
     * internal resources used by this controller.
     */
    public abstract void destroy();

    /**
     * Processes a request.
     * 
     * @param request        the request to process
     * 
     * @throws RequestException if the request couldn't be processed
     */
    public abstract void process(Request request) throws RequestException;

    /**
     * Returns the application context.
     * 
     * @return the application context
     */
    protected Application getApplication() {
        return application;
    }
    
    /**
     * Returns the application content manager.
     * 
     * @return the application content manager
     */
    protected ContentManager getContentManager() {
        return getApplication().getContentManager();
    }

    /**
     * Returns a file in the application context. The file path is
     * specified relative to the application base directory.
     * 
     * @param path           the relative file path
     * 
     * @return the file in the application context
     */
    protected File getFile(String path) {
        return new File(application.getBaseDir(), path);
    }
}
