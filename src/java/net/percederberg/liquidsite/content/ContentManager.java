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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
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
     * Checks if a persistent object is readable by a user. This
     * method will check the read permissions in the database. If the
     * admin flag is set, the user must also be in the same domain as
     * the object requested.
     *
     * @param user           the user
     * @param obj            the persistent object
     *
     * @return true if the object is readable, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private boolean isReadable(User user, PersistentObject obj)
        throws ContentException {

        String   domain = "";
        boolean  sameDomain;

        // Find domain name
        if (obj instanceof Domain) {
            domain = ((Domain) obj).getName();
        } else if (obj instanceof Content) {
            domain = ((Content) obj).getDomainName();
        }

        // Compare to user domain name
        if (user == null) {
            sameDomain = false;
        } else if (user.isSuperUser()) {
            sameDomain = true;
        } else {
            sameDomain = user.getDomainName().equals(domain);
        }

        // Check read access if not admin or same domain
        if (!admin || sameDomain) {
            return obj.hasReadAccess(user);
        } else {
            return false;
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
            if (isReadable(user, domain)) {
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

        if (domain != null && !isReadable(user, domain)) {
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

        return postProcess(user, children);
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
        return postProcess(user, children);
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

        return postProcess(user, children);
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
        return postProcess(user, children);
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

        return postProcess(user, children);
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
     * Returns the number of users in a specified domain. Only users
     * with matching names will be counted.
     *
     * @param domain         the domain, or null for superusers
     * @param filter         the user search filter (empty for all)
     *
     * @return the number of matching users in the domain
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public int getUserCount(Domain domain, String filter)
        throws ContentException {

        return User.countByDomain(this, domain, filter);
    }

    /**
     * Returns an array of users in a specified domain. Only users
     * with matching names will be returned. Also, only a limited
     * interval of the matching users will be returned.
     *
     * @param domain         the domain, or null for superusers
     * @param filter         the user search filter (empty for all)
     * @param startPos       the list interval start position
     * @param maxLength      the list interval maximum length
     *
     * @return an array of matching users in the domain
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public User[] getUsers(Domain domain,
                           String filter,
                           int startPos,
                           int maxLength)
        throws ContentException {

        return User.findByDomain(this, domain, filter, startPos, maxLength);
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
     * Returns an array of groups in a specified domain. Only groups
     * with matching names will be returned.
     *
     * @param domain         the domain
     * @param filter         the search filter (empty for all)
     *
     * @return an array of matching groups in the domain
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public Group[] getGroups(Domain domain, String filter)
        throws ContentException {

        if (domain == null) {
            return new Group[0];
        } else {
            return Group.findByDomain(this, domain, filter);
        }
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
     * specified user, filtering out the ones that are not.
     *
     * @param user           the user requesting the object
     * @param content        the content objects found
     *
     * @return the readable and sorted content objects
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private Content[] postProcess(User user, Content[] content)
        throws ContentException {

        ArrayList  list = new ArrayList(content.length);
        Content[]  res;

        for (int i = 0; i < content.length; i++) {
            if (isOnline(content[i]) && isReadable(user, content[i])) {
                list.add(content[i]);
            }
        }
        res = new Content[list.size()];
        list.toArray(res);
        return res;
    }
}
