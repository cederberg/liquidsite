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

import java.util.ArrayList;

import net.percederberg.liquidsite.Application;
import net.percederberg.liquidsite.Log;

/**
 * The content manager. This class manages the content objects in use
 * by the application. In particular these objects include the site
 * and content caches.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentManager {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(ContentManager.class);

    /**
     * The content manager instance currently in use.
     */
    private static ContentManager instance = null;

    /**
     * The application context.
     */
    private Application app;

    /**
     * The web site cache. This is a list of all web sites known to 
     * the content manager.
     */
    private ArrayList sites = new ArrayList();

    /**
     * Returns the content manager currently in use.
     * 
     * @return the content manager currently in use
     * 
     * @throws ContentException if no content manager is available
     */
    public static ContentManager getInstance() throws ContentException {
        if (instance == null) {
            LOG.debug("content manager not initialized");
            throw new ContentException("content manager not initialized");
        }
        return instance;
    }

    /**
     * Creates a new content manager. All new content handling 
     * requests will pass through the newly created content manager.
     * If the application is installed, the site and content caches
     * will also be initialized.
     * 
     * @param app            the application context
     * 
     * @throws ContentException if the content manager couldn't be 
     *             initialized properly
     */
    public ContentManager(Application app) throws ContentException {
        instance = this;
        this.app = app;
        if (app.isInstalled()) {
            LOG.trace("reading content manager site cache...");
            sites = SitePeer.doSelectAll();
            LOG.trace("done reading content manager site cache");
        }
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
     * Searches for the best matching site in the site cache. Under
     * normal operations, the site cache should contain all sites in
     * the database. Exact matches for host and port are always 
     * preferred over defaults (matching all values). Longer URI 
     * matches are preferred over shorter.
     * 
     * @param host           the request host name (server name)
     * @param port           the request (server) port
     * @param uri            the request uri
     * 
     * @return the best matching site
     */
    public Site findSite(String host, int port, String uri) {
        Site  found = null;
        int   max = 0;
        Site  site;
        int   value;
        
        for (int i = 0; i < sites.size(); i++) {
            site = (Site) sites.get(i);
            value = site.match(host, port, uri);
            if (value > max) {
                found = site;
                max = value;
            }
        }
        return found;
    }

    /**
     * Adds a site to the site cache. This method also updates an 
     * existing entry for the same site.
     * 
     * @param site           the site to add
     */
    public void addSite(Site site) {
        if (sites.contains(site)) {
            removeSite(site);
        }
        sites.add(site);
    }
    
    /**
     * Removes a site from the site cache.
     * 
     * @param site           the site to remove
     */
    public void removeSite(Site site) {
        int  index = sites.indexOf(site);
        
        if (index >= 0) {
            sites.remove(index);
        }
    }

    /**
     * Closes this content manager and frees all resources. This 
     * method should be called in order to garbage collect the 
     * resources used by this manager.
     */
    public void close() {
        sites.clear();
        sites = null;
        if (instance == this) {
            instance = null;
        }
        LOG.trace("closed content manager");
    }
}
