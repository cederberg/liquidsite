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
import java.util.Comparator;
import java.util.Date;
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
     * The content name comparator. This is used by administration
     * content managers.
     */
    private static final Comparator NAME_COMPARATOR =
        new NameComparator();

    /**
     * The content online comparator. This is used by normal content
     * managers.
     */
    private static final Comparator ONLINE_COMPARATOR =
        new OnlineComparator();

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
     * Checks if a content object is online. If the admin flag is set
     * all objects are considered online.
     *
     * @param content        the content object
     *
     * @return true if the object is online, or
     *         false otherwise
     */
    private boolean isOnline(Content content) {
        return admin || content.isOnline();
    }

    /**
     * Checks if a content object is readable by a user. This method
     * will check the read permissions in the database.
     *
     * @param user           the user
     * @param content        the content object
     *
     * @return true if the object is readable, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    private boolean isReadable(User user, Content content)
        throws ContentException {

        return content.hasReadAccess(user);
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
    Content[] getSites(Domain domain) throws ContentException {
        Content[]  res;
        
        res = CacheManager.getInstance().getSites(domain);
        if (res == null) {
            res = InternalContent.findByCategory(this,
                                                 domain,
                                                 Content.SITE_CATEGORY);
            CacheManager.getInstance().addSites(domain, res);
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
        return InternalContent.findByMaxRevision(this, id);
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

        return postProcess(user, getContent(id));
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

        return InternalContent.findByRevision(this, id, revision);
    }

    /**
     * Returns the domain root content object with the specified name
     * and highest revision readable by the user.
     *
     * @param user           the user requesting the content
     * @param domain         the domain
     * @param name           the child name
     *
     * @return the content object found, or
     *         null if no matching content existed
     *
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     * @throws ContentSecurityException if the specified content 
     *             object wasn't readable by the user
     */
    public Content getContentChild(User user, Domain domain, String name) 
        throws ContentException, ContentSecurityException {

        Content  content = InternalContent.findByName(this, domain, name);

        return postProcess(user, content);
    }
    
    /**
     * Returns the child content object with the specified name and 
     * highest revision readable by the user.
     *
     * @param user           the user requesting the content
     * @param parent         the content parent
     * @param name           the child name
     *
     * @return the content object found, or
     *         null if no matching content existed
     *
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     * @throws ContentSecurityException if the specified content 
     *             object wasn't readable by the user
     */
    public Content getContentChild(User user, Content parent, String name) 
        throws ContentException, ContentSecurityException {

        Content  content = InternalContent.findByName(this, parent, name);

        return postProcess(user, content);
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

        Content[]  children = InternalContent.findByParent(this, domain);

        return postProcess(user, children, isAdmin());
    }

    /**
     * Returns the user readable domain root content objects in the
     * specified category. Only the highest revision of each object
     * will be returned.
     *
     * @param user           the user requesting the content
     * @param domain         the domain
     * @param category       the content category
     *
     * @return the user readable domain root content objects
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public Content[] getContentChildren(User user,
                                        Domain domain,
                                        int category) 
        throws ContentException {

        Content[]  children;

        children = InternalContent.findByCategory(this, domain, category);
        return postProcess(user, children, isAdmin());
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

        Content[]  children = InternalContent.findByParent(this, parent);

        return postProcess(user, children, isAdmin());                    
    }

    /**
     * Returns the user readable child content objects in the
     * specified category. Only the highest revision of each object
     * will be returned.
     *
     * @param user           the user requesting the content
     * @param parent         the content parent
     * @param category       the content category
     *
     * @return the user readable child content objects
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public Content[] getContentChildren(User user,
                                        Content parent,
                                        int category) 
        throws ContentException {

        Content[]  children;

        children = InternalContent.findByCategory(this, parent, category);
        return postProcess(user, children, isAdmin());                    
    }

    /**
     * Returns the user readable child content objects. Only the 
     * highest revision of each object will be returned.
     * 
     * @param user           the user requesting the content
     * @param parents        the content parents
     * 
     * @return the user readable child content objects
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public Content[] getContentChildren(User user, Content[] parents) 
        throws ContentException {

        Content[]  children = InternalContent.findByParents(this, parents);

        return postProcess(user, children, false);                    
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
        Content[]      sites;
        ContentSite    site;
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
            site = (ContentSite) sites[i];
            match = site.match(protocol, hostname, port, path); 
            if (sites[i].isOnline()
             && sites[i].getRevisionNumber() > 0
             && match > max) {

                res = site;
                max = match;
            }
        }
        return res;
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

    /**
     * Post-processes a retrieved content object. This method checks
     * that the object is safe to return to the specified user.
     *
     * @param user           the user requesting the object
     * @param content        the content object found, or null
     *
     * @return the content object found, or
     *         null if the object isn't visible for the user
     *
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     * @throws ContentSecurityException if the specified content 
     *             object wasn't readable by the user
     */
    private Content postProcess(User user, Content content)
        throws ContentException, ContentSecurityException {

        if (content != null && !isReadable(user, content)) {
            throw new ContentSecurityException(user, "read", content);
        } else if (content != null && !isOnline(content)) {
            return null;
        } else {
            return content;
        }
    }

    /**
     * Post-processes an array of retrieved content objects. This
     * method checks that the objects are safe to return to the
     * specified user, filtering out the ones that are not. This 
     * method will also sort the content objects.
     *
     * @param user           the user requesting the object
     * @param content        the content objects found
     * @param compareByName  the name-based comparison flag
     *
     * @return the readable and sorted content objects
     *
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    private Content[] postProcess(User user,
                                  Content[] content,
                                  boolean compareByName)
        throws ContentException {

        ArrayList  list = new ArrayList(content.length);
        Content[]  res;

        for (int i = 0; i < content.length; i++) {
            if (isOnline(content[i]) && isReadable(user, content[i])) {
                list.add(content[i]);
            }
        }
        if (compareByName) {
            Collections.sort(list, NAME_COMPARATOR);
        } else {
            Collections.sort(list, ONLINE_COMPARATOR);
        }
        res = new Content[list.size()];
        list.toArray(res);
        return res;                    
    }
    
    
    /**
     * A content name comparator. This class compares the content 
     * categories and names to put objects in lexical order.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    private static class NameComparator implements Comparator {

        /**
         * Compares two objects for order. Returns a negative 
         * integer, zero, or a positive integer as the first object
         * is less than, equal to, or greater than the second object.
         * 
         * @param obj1            the first object
         * @param obj2            the second object
         * 
         * @return a negative integer, zero, or a positive integer as 
         *         the first object is less than, equal to, or
         *         greater than the second object
         * 
         * @throws ClassCastException if both objects aren't Content
         *             objects
         */
        public int compare(Object obj1, Object obj2)
            throws ClassCastException {

            return compareInternal((Content) obj1, (Content) obj2);
        }

        /**
         * Compares two content objects for order. Returns a negative 
         * integer, zero, or a positive integer as the first object
         * is less than, equal to, or greater than the second object.
         * 
         * @param obj1            the first content object
         * @param obj2            the second content object
         * 
         * @return a negative integer, zero, or a positive integer as 
         *         the first object is less than, equal to, or
         *         greater than the second object
         */
        private int compareInternal(Content obj1, Content obj2) {
            int category = obj1.getCategory() - obj2.getCategory();
        
            if (category != 0) {
                return category;
            } else {
                return obj1.getName().compareTo(obj2.getName());
            }
        }
    }


    /**
     * A content online comparator. This class compares the online
     * publishing dates. Newer dates are considered to have 
     * precedence over older dates.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    private static class OnlineComparator implements Comparator {

        /**
         * Compares two objects for order. Returns a negative 
         * integer, zero, or a positive integer as the first object
         * is less than, equal to, or greater than the second object.
         * 
         * @param obj1            the first object
         * @param obj2            the second object
         * 
         * @return a negative integer, zero, or a positive integer as 
         *         the first object is less than, equal to, or
         *         greater than the second object
         * 
         * @throws ClassCastException if both objects aren't Content
         *             objects
         */
        public int compare(Object obj1, Object obj2)
            throws ClassCastException {

            return compareInternal((Content) obj1, (Content) obj2);
        }

        /**
         * Compares two content objects for order. Returns a negative 
         * integer, zero, or a positive integer as the first object
         * is less than, equal to, or greater than the second object.
         * 
         * @param obj1            the first content object
         * @param obj2            the second content object
         * 
         * @return a negative integer, zero, or a positive integer as 
         *         the first object is less than, equal to, or
         *         greater than the second object
         */
        private int compareInternal(Content obj1, Content obj2) {
            Date  date1 = obj1.getOnlineDate();
            Date  date2 = obj2.getOnlineDate();

            if (date1 == null && date2 == null) {
                return obj2.getId() - obj1.getId();
            } else if (date1 == null) {
                return -1;
            } else if (date2 == null) {
                return 1;
            } else {
                return date2.compareTo(date1);
            }
        }
    }
}
