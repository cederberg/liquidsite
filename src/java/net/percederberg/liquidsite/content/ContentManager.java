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

import java.util.HashMap;
import java.util.Iterator;

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
     * The site cache. This is a map of site arrays, recently queried
     * though this interface. The site arrays are indexed by their
     * domain name.
     */
    private HashMap sites = new HashMap();

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
        Domain[]  domains;
        Host[]    hosts;

        instance = this;
        this.app = app;
        if (app.getConfig().isInitialized()) {
            LOG.trace("initializing content manager cache...");
            domains = Domain.findAll();
            for (int i = 0; i < domains.length; i++) {
                cacheAdd(domains[i]);
            }
            hosts = Host.findAll();
            for (int i = 0; i < hosts.length; i++) {
                cacheAdd(hosts[i]);
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
     * Returns an array of all domains. This method will only consult 
     * the internal cache, and not the database.
     * 
     * @return an array of all domains
     */
    public Domain[] getDomains() {
        Domain[]  res = new Domain[domains.size()];
        Iterator  iter = domains.values().iterator();
        
        for (int i = 0; iter.hasNext(); i++) {
            res[i] = (Domain) iter.next();
        }
        return res;
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
     * Returns the root domain. This method will only consult the 
     * internal cache, and not the database. 
     * 
     * @return the root domain, or
     *         null if no such domain exists
     */
    public Domain getRootDomain() {
        return getDomain("ROOT");
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
     * Returns all sites in a domain. This method will retrieve the
     * sites from the cache if possible, otherwise the sites will be
     * read from the database and added to the cache.
     * 
     * @param domain         the domain
     * 
     * @return the array of sites in the domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Site[] getSites(Domain domain) throws ContentException {
        Site[]  res;
        
        if (sites.containsKey(domain.getName())) {
            return (Site[]) sites.get(domain.getName());
        } else {
            res = Site.findByDomain(domain);
            sites.put(domain.getName(), res);
            return res;
        }
    }

    /**
     * Returns the content object with the specified identifier and 
     * highest revision. This method may return a content object from
     * the cache.
     * 
     * @param id             the content identifier
     * 
     * @return the content object found, or
     *         null if no matching content existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Content getContent(int id) throws ContentException {
        return Content.findById(id);
    }
    
    /**
     * Returns the content object with the specified identifier and 
     * revision. This method may return a content object from the 
     * cache.
     * 
     * @param id             the content identifier
     * @param revision       the content revision
     * 
     * @return the content object found, or
     *         null if no matching content existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Content getContent(int id, int revision) 
        throws ContentException {

        return Content.findByRevision(id, revision);
    }

    /**
     * Returns a user with a specified name. If the user couldn't be
     * found in the specified domain, this method also checks for 
     * superusers with the specified name.
     * 
     * @param domain         the domain
     * @param name           the user name
     * 
     * @return the user found, or
     *         null if no matching user existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public User getUser(Domain domain, String name) 
        throws ContentException {

        User  user;
        
        user = User.findByName(domain, name);
        if (user == null) {
            user = User.findByName(null, name);
        }
        return user;
    }

    /**
     * Returns a group with a specified name.
     * 
     * @param domain         the domain
     * @param name           the group name
     * 
     * @return the group found, or
     *         null if no matching group existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Group getGroup(Domain domain, String name) 
        throws ContentException {

        return Group.findByName(domain, name);
    }

    /**
     * Adds a persistent object to the cache.
     * 
     * @param obj            the object to add
     */
    void cacheAdd(PersistentObject obj) { 
        cacheRemove(obj);
        if (obj instanceof Domain) {
            domains.put(((Domain) obj).getName(), obj);
        } else if (obj instanceof Host) {
            hosts.put(((Host) obj).getName(), obj);
        } else if (obj instanceof Site) {
            sites.remove(((Site) obj).getDomainName());
        }
    }
    
    /**
     * Removes a persistent object from the cache.
     * 
     * @param obj            the object to remove
     */
    void cacheRemove(PersistentObject obj) {
        if (obj instanceof Domain) {
            domains.remove(((Domain) obj).getName());
        } else if (obj instanceof Host) {
            hosts.remove(((Host) obj).getName());
        } else if (obj instanceof Site) {
            sites.remove(((Site) obj).getDomainName());
        }
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
        sites.clear();
        sites = null;
        if (instance == this) {
            instance = null;
        }
        LOG.trace("closed content manager");
    }
}
