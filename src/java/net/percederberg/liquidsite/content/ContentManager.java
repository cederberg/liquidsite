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
import java.util.HashMap;

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
     * The domain cache. This is a map of all domains known to the 
     * content manager. The domains are indexed by their names.
     */
    private HashMap domains = new HashMap();

    /**
     * The host cache. This is a map of all hosts known to the 
     * content manager. The hosts are indexed by their names.
     */
    private HashMap hosts = new HashMap();

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
     * If the application is installed, the domain and content caches
     * will also be initialized.
     * 
     * @param app            the application context
     * 
     * @throws ContentException if the content manager couldn't be 
     *             initialized properly
     */
    public ContentManager(Application app) throws ContentException {
        ArrayList  list;

        instance = this;
        this.app = app;
        if (app.isInstalled()) {
            LOG.trace("initializing content manager cache...");
            list = DomainPeer.doSelectAll();
            for (int i = 0; i < list.size(); i++) {
                addDomain((Domain) list.get(i));
            }
            list = HostPeer.doSelectAll();
            for (int i = 0; i < list.size(); i++) {
                addHost((Host) list.get(i));
            }
            LOG.trace("done initializing content manager cache");
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
     * Returns a domain with the specified name. This method will 
     * only consult the internal cache, and not the database. 
     * 
     * @param name           the domain name
     * 
     * @return the domain found, or
     *         null if no such domain exists
     */
    public Domain getDomain(String name) {
        return (Domain) domains.get(name);
    }

    /**
     * Adds a domain to the domain cache. This method also updates 
     * existing entries for the same domain.
     * 
     * @param domain         the domain to add or update
     */
    void addDomain(Domain domain) {
        removeDomain(domain);
        domains.put(domain.getName(), domain);
    }
    
    /**
     * Removes a domain from the domain cache.
     * 
     * @param domain         the domain to remove
     */
    void removeDomain(Domain domain) {
        domains.remove(domain.getName());
    }

    /**
     * Returns a host with the specified name. This method will only 
     * consult the internal cache, and not the database. 
     * 
     * @param name           the host name
     * 
     * @return the host found, or
     *         null if no such host exists
     */
    public Host getHost(String name) {
        return (Host) hosts.get(name);
    }

    /**
     * Adds a host to the host cache. This method also updates 
     * existing entries for the same host.
     * 
     * @param host           the host to add or update
     */
    void addHost(Host host) {
        removeHost(host);
        hosts.put(host.getName(), host);
    }
    
    /**
     * Removes a host from the host cache.
     * 
     * @param host           the host to remove
     */
    void removeHost(Host host) {
        hosts.remove(host.getName());
    }

    /**
     * Closes this content manager and frees all resources. This 
     * method should be called in order to garbage collect the 
     * resources used by this manager.
     */
    public void close() {
        domains.clear();
        domains = null;
        hosts.clear();
        hosts = null;
        if (instance == this) {
            instance = null;
        }
        LOG.trace("closed content manager");
    }
}
