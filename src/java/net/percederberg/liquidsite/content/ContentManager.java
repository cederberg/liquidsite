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
import java.util.Collections;
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
     * The content security manager.
     */
    private ContentSecurityManager securityManager = 
        new ContentSecurityManager();

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
     * Returns the content security manager.
     * 
     * @return the content security manager
     */
    public ContentSecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Returns an array of all domains readable by a user.
     * 
     * @param user           the user requesting the list
     * 
     * @return an array of all user readable domains
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Domain[] getDomains(User user) throws ContentException {
        Iterator   iter = domains.values().iterator();
        ArrayList  list = new ArrayList();
        Domain[]   res;
        Domain     domain;
        
        // Find all readable domains
        for (int i = 0; iter.hasNext(); i++) {
            domain = (Domain) iter.next();
            if (domain.hasReadAccess(user)) {
                list.add(domain);
            }
        }

        // Create domain array
        Collections.sort(list);
        res = new Domain[list.size()];
        for (int i = 0; i < list.size(); i++) {
            res[i] = (Domain) list.get(i);
        }

        return res;
    }

    /**
     * Returns a domain with the specified name. 
     * 
     * @param name           the domain name
     * 
     * @return the domain found, or
     *         null if no such domain exists
     */
    Domain getDomain(String name) {
        return (Domain) domains.get(name);
    }

    /**
     * Returns a domain with the specified name readable by a user.
     * 
     * @param user           the user requesting the domain
     * @param name           the domain name
     * 
     * @return the domain found, or
     *         null if no such domain exists
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     * @throws ContentSecurityException if the specified domain 
     *             wasn't readable by the user
     */
    public Domain getDomain(User user, String name) 
        throws ContentException, ContentSecurityException {

        Domain  domain = getDomain(name);
        
        if (domain != null && !domain.hasReadAccess(user)) {
            throw new ContentSecurityException(user, "read", domain);
        }
        return domain;
    }

    /**
     * Returns a host with the specified name.
     *
     * @param name           the host name
     *
     * @return the host found, or
     *         null if no such host exists
     */
    Host getHost(String name) {
        return (Host) hosts.get(name);
    }

    /**
     * Returns all sites in a domain.
     * 
     * @param domain         the domain
     * 
     * @return the array of sites in the domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    Site[] getSites(Domain domain) throws ContentException {
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
     * Returns all user readable sites in a domain.
     * 
     * @param user           the user requesting the sites
     * @param domain         the domain
     * 
     * @return the array of user readable sites in the domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Site[] getSites(User user, Domain domain) 
        throws ContentException {

        Site[]     sites = getSites(domain);
        ArrayList  list = new ArrayList(sites.length);
        Site[]     res;
        
        for (int i = 0; i < sites.length; i++) {
            if (sites[i].hasReadAccess(user)) {
                list.add(sites[i]);
            }
        }
        res = new Site[list.size()];
        list.toArray(res);
        return res;
    }

    /**
     * Returns the content object with the specified identifier and 
     * highest revision.
     * 
     * @param id             the content identifier
     * 
     * @return the content object found, or
     *         null if no matching content existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    Content getContent(int id) throws ContentException {
        return Content.findByMaxRevision(id);
    }
    
    /**
     * Returns the content object with the specified identifier and 
     * highest revision readable by the user.
     * 
     * @param user           the user requesting the content
     * @param id             the content identifier
     * 
     * @return the content object found, or
     *         null if no matching content existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     * @throws ContentSecurityException if the specified content 
     *             object wasn't readable by the user
     */
    public Content getContent(User user, int id) 
        throws ContentException, ContentSecurityException {

        Content  content = getContent(id);

        if (content != null && !content.hasReadAccess(user)) {
            throw new ContentSecurityException(user, "read", content);
        }
        return content;
    }
    
    /**
     * Returns the content object with the specified identifier and 
     * revision.
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
    Content getContent(int id, int revision) 
        throws ContentException {

        return Content.findByRevision(id, revision);
    }

    /**
     * Returns the user readable child content objects. Only the 
     * highest revision of each object will be returned.
     * 
     * @param user           the user requesting the content
     * @param parent         the content parent
     * 
     * @return the user readable child content objects
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public Content[] getContentChildren(User user, Content parent) 
        throws ContentException {

        Content[]  children = Content.findByParent(parent);
        ArrayList  list = new ArrayList(children.length);
        Content[]  res;

        for (int i = 0; i < children.length; i++) {
            if (children[i].hasReadAccess(user)) {
                list.add(children[i]);
            }
        } 
        res = new Content[list.size()];
        list.toArray(res);
        return res;                    
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
     * Finds the site corresponding to a web request. This method 
     * does NOT control access permissions and should thus ONLY be 
     * used internally in the request processing. 
     * 
     * @param protocol       the request protocol (i.e. "http")
     * @param hostname       the request host name
     * @param port           the request port number
     * @param path           the full request path
     * 
     * @return the site corresponding to the request, or
     *         null if no matching site was found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly 
     */
    public Site findSite(String protocol, 
                         String hostname, 
                         int port, 
                         String path) 
        throws ContentException {

        Host    host;
        Domain  domain;
        Site[]  sites;
        Site    res = null;
        int     max = 0;
        int     match;
        
        host = (Host) hosts.get(hostname);
        if (host == null) {
            domain = getDomain("ROOT");
        } else {
            domain = getDomain(host.getDomainName());
        }
        sites = getSites(domain);
        for (int i = 0; i < sites.length; i++) {
            match = sites[i].match(protocol, hostname, port, path); 
            if (sites[i].isOnline()
             && sites[i].getRevisionNumber() > 0
             && match > max) {

                res = sites[i];
                max = match;
            }
        }
        return res;
    }

    /**
     * Finds the page content corresponding to a request path. This 
     * method does NOT control access permissions and should thus 
     * ONLY be used internally in the request processing. Also note
     * that any content category matching the request path may be 
     * returned including the parent content object, if the path was
     * empty. 
     * 
     * @param parent         the content parent
     * @param path           the request path after the parent
     * 
     * @return the content object corresponding to the path, or
     *         null if no matching content was found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly 
     */
    public Content findPage(Content parent, String path) 
        throws ContentException {

        Content[]  children;
        Content    page = parent;
        String     match;
        String     str;

        while (path.length() > 0) {
            children = Content.findByParent(parent);
            match = "";
            for (int i = 0; i < children.length; i++) {
                str = findPageMatch(children[i], path);
                if (str != null && str.length() > match.length()) {
                    page = children[i];
                    match = str;
                }
            }
            if (match.length() <= 0) {
                return null;
            }
            path = path.substring(match.length());
            parent = page;
        }
        return page;
    }

    /**
     * Finds the index page for a content parent. This method does 
     * NOT control access permissions and should thus ONLY be used 
     * internally in the request processing.
     * 
     * @param parent         the content parent
     * 
     * @return the index content object, or
     *         null if no matching content was found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly 
     */
    public Content findIndexPage(Content parent) 
        throws ContentException {
            
        String[]  index = { "index.html", "index.htm" };
        Content   page;

        for (int i = 0; i < index.length; i++) {
            page = findPage(parent, index[i]);
            if (page != null) {
                return page;
            }
        }
        return null;
    }

    /**
     * Finds the page match to a specified request path.
     * 
     * @param page           the content object
     * @param path           the request path to check
     * 
     * @return the matching initial sequence of the path, or
     *         an empty string if no match was found
     */
    private String findPageMatch(Content page, String path) {
        if (!page.isOnline() || page.getRevisionNumber() < 1) {
            return ""; 
        }
        switch (page.getCategory()) {
        case Content.FOLDER_CATEGORY:
            if (path.startsWith(page.getName() + "/")) {
                return page.getName() + "/";
            } else {
                return "";
            }
        case Content.FILE_CATEGORY:
            if (path.startsWith(page.getName())) {
                return page.getName();
            } else {
                return "";
            }
        default:
            return "";
        }
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
