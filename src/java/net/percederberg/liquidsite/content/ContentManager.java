/*
 * ContentManager.java
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

package net.percederberg.liquidsite.content;

import net.percederberg.liquidsite.Application;

/**
 * The content manager. This class manages the content objects in use
 * by the application.  
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentManager {

    /**
     * The content manager instance currently in use.
     */
    private static ContentManager instance = null;

    /**
     * The application context.
     */
    private Application app;

    /**
     * Returns the content manager currently in use.
     * 
     * @return the content manager currently in use
     * 
     * @throws ContentException if no content manager is available
     */
    public static ContentManager getInstance() throws ContentException {
        if (instance == null) {
            throw new ContentException("content manager not initialized");
        }
        return instance;
    }

    /**
     * Creates a new content manager. All new content handling 
     * requests will pass through the newly created content manager.
     * 
     * @param app            the application context
     */
    public ContentManager(Application app) {
        this.app = app;
        instance = this;
    }

    /**
     * Returns the application context for this content manager.
     * 
     * @return the application context for this content manager
     */
    public Application getApplication() {
        return app; 
    }

    /**
     * Closes this content manager and frees all resources. This 
     * method should be called in order to garbage collect the 
     * resources used by this manager.
     */
    public void close() {
        if (instance == this) {
            instance = null;
        }
    }
}
