/*
 * RequestProcessor.java
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
 * A request processor.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public abstract class RequestProcessor {

    /**
     * The content manager to use.
     */
    private ContentManager manager;

    /**
     * The base directory for application files.
     */
    private File baseDir;

    /**
     * Creates a new request processor.
     * 
     * @param manager        the content manager to use
     * @param baseDir        the base directory for application files
     */
    public RequestProcessor(ContentManager manager, File baseDir) {
        this.manager = manager;
        this.baseDir = baseDir;
    }
    
    /**
     * Returns the content manager used by this processor.
     * 
     * @return the content manager used by this processor
     */
    protected ContentManager getContentManager() {
        return manager;
    }

    /**
     * Returns an application file. The file path should be specified
     * relative to the base application directory.
     * 
     * @param path           the relative file path
     * 
     * @return the absolute file path
     */
    protected File getFile(String path) {
        return new File(baseDir, path);
    }

    /**
     * Processes a request.
     * 
     * @param request        the request to process
     * 
     * @throws RequestException if the request couldn't be processed
     */
    public abstract void process(Request request) throws RequestException;

    /**
     * Destroys this request processor. This method frees all
     * internal resources used by this processor.
     */
    public abstract void destroy();
}
