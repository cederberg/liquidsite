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

import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.User;

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

    /**
     * Finds the page content corresponding to a request path. This 
     * method does NOT control access permissions and should thus 
     * ONLY be used internally in the request processing. Also note
     * that any content category matching the request path may be 
     * returned including the parent content object, if the path was
     * empty. 
     *
     * @param user           the user requesting the page
     * @param parent         the content parent
     * @param path           the request path after the parent
     * 
     * @return the content object corresponding to the path, or
     *         null if no matching content was found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly 
     * @throws ContentSecurityException if the specified content 
     *             object wasn't readable by the user
     */
    protected Content findPage(User user, Content parent, String path) 
        throws ContentException, ContentSecurityException {

        ContentManager  manager = getContentManager();
        Content         content = parent;
        String          name;
        int             pos;

        while (content != null && path.length() > 0) {
            pos = path.indexOf('/');
            if (pos <= 0) {
                name = path;
            } else {
                name = path.substring(0, pos);
            }
            path = path.substring(name.length());
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            content = manager.getContentChild(user, parent, name);
            parent = content;
        }
        return content;
    }

    /**
     * Finds the index page for a content parent. This method does 
     * NOT control access permissions and should thus ONLY be used 
     * internally in the request processing.
     * 
     * @param user           the user requesting the page
     * @param parent         the content parent
     * 
     * @return the index content object, or
     *         null if no matching content was found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly 
     * @throws ContentSecurityException if the specified content 
     *             object wasn't readable by the user
     */
    public Content findIndexPage(User user, Content parent) 
        throws ContentException, ContentSecurityException {
            
        String[]  index = { "index.html", "index.htm" };
        Content   page;

        for (int i = 0; i < index.length; i++) {
            page = findPage(user, parent, index[i]);
            if (page != null) {
                return page;
            }
        }
        return null;
    }
}
