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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import net.percederberg.liquidsite.Application;
import net.percederberg.liquidsite.Log;

/**
 * The content manager. This class provides an interface for 
 * retrieving objects from the database. The content manager 
 * guarantees that security considerations are taken into account
 * before returning object (if a method comment does not explicitly 
 * say otherwise). The content manager also makes sure not to return
 * unpublished content if it's policy does not allow that (only used
 * by the web administration).
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
     * The application context.
     */
    private Application app;
    
    /**
     * The admin flag. When this flag is set, the content manager may
     * return content objects being offline or having work revisions.
     * Also, when this flag is set caching will be turned off.
     */
    private boolean admin;

    /**
     * Creates a new content manager. All new content handling 
     * requests will pass through the newly created content manager.
     * If the admin content manager flag is set, the content manager
     * will NOT cache content objects and the latest revision 
     * returned will always be a work revision if one is available.
     *  
     * 
     * @param app            the application context
     * @param admin          the admin content manager flag
     */
    public ContentManager(Application app, boolean admin) {
        this.app = app;
        this.admin = admin;
    }

    /**
     * Checks if the admin flag is set.
     * 
     * @return true if the admin flag is set, or
     *         false otherwise
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Checks if the specified user can read a content object. This
     * method will check that the object is online (unless the admin
     * flag is set) and that the user has read permissions to the
     * object.
     *
     * @param user           the user
     * @param content        the content object
     *
     * @return true if the user can read the content object, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    private boolean canRead(User user, Content content)
        throws ContentException {

        return (admin || content.isOnline())
            && content.hasReadAccess(user);
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
        CacheManager  cache = CacheManager.getInstance();
        Collection    domains;
        Iterator      iter;
        ArrayList     list = new ArrayList();
        Domain[]      res;
        Domain        domain;
        
        // Retrieve domain collection
        domains = cache.getAllDomains();
        if (domains.isEmpty()) {
            cache.addAll(Domain.findAll(this));
            domains = cache.getAllDomains();
        }

        // Find all readable domains
        iter = domains.iterator();
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
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    Domain getDomain(String name) throws ContentException {
        Domain  domain;
        
        domain = CacheManager.getInstance().getDomain(name);
        if (domain == null) {
            CacheManager.getInstance().addAll(Domain.findAll(this));
            domain = CacheManager.getInstance().getDomain(name);
        }
        return domain;
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
     *
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    Host getHost(String name) throws ContentException {
        Host  host;
        
        host = CacheManager.getInstance().getHost(name);
        if (host == null) {
            CacheManager.getInstance().addAll(Host.findAll(this));
            host = CacheManager.getInstance().getHost(name);
        }
        return host;
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
    ContentSite[] getSites(Domain domain) throws ContentException {
        ContentSite[]  res;
        
        res = CacheManager.getInstance().getSites(domain);
        if (res == null) {
            res = ContentSite.findByDomain(this, domain);
            CacheManager.getInstance().add(domain, res);
        }
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
        return Content.findByMaxRevision(this, id);
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

        if (content != null && !canRead(user, content)) {
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

        return Content.findByRevision(this, id, revision);
    }

    /**
     * Returns the user readable domain root content objects. Only 
     * the highest revision of each object will be returned.
     * 
     * @param user           the user requesting the content
     * @param domain         the domain
     * 
     * @return the user readable domain root content objects
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public Content[] getContentChildren(User user, Domain domain) 
        throws ContentException {

        Content[]  children = Content.findByParent(this, domain);
        ArrayList  list = new ArrayList(children.length);
        Content[]  res;

        for (int i = 0; i < children.length; i++) {
            if (canRead(user, children[i])) {
                list.add(children[i]);
            }
        }
        Collections.sort(list);
        res = new Content[list.size()];
        list.toArray(res);
        return res;                    
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

        Content[]  children = Content.findByParent(this, parent);
        ArrayList  list = new ArrayList(children.length);
        Content[]  res;

        for (int i = 0; i < children.length; i++) {
            if (canRead(user, children[i])) {
                list.add(children[i]);
            }
        } 
        Collections.sort(list);
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
        
        user = User.findByName(this, domain, name);
        if (user == null) {
            user = User.findByName(this, null, name);
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

        return Group.findByName(this, domain, name);
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
    public ContentSite findSite(String protocol, 
                                String hostname, 
                                int port, 
                                String path) 
        throws ContentException {

        Host           host;
        Domain         domain;
        ContentSite[]  sites;
        ContentSite    res = null;
        int            max = 0;
        int            match;
        
        host = getHost(hostname);
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
            children = Content.findByParent(this, parent);
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
        case Content.PAGE_CATEGORY:
            if (path.startsWith(page.getName())) {
                return page.getName();
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
     * Closes this content manager and frees all resources. This 
     * method should be called in order to garbage collect the 
     * resources used by this manager.
     */
    public void close() {
        CacheManager.getInstance().removeAll();
        LOG.trace("closed content manager");
    }
}
