/*
 * CacheManager.java
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

import java.util.Collection;
import java.util.HashMap;

import net.percederberg.liquidsite.Log;

/**
 * A content cache manager. This class will be used to cache objects
 * upon retrieval from a caching content manager. Only a single
 * instance exists, in order to easily guarantee cache consistency.
 * This class also synchronizes any changes to the cache content.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class CacheManager {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(CacheManager.class);

    /**
     * The one and only cache manager instance.
     */
    private static final CacheManager INSTANCE = new CacheManager();

    /**
     * The cache manager instance.
     *
     * @return the cache manager instance
     */
    public static CacheManager getInstance() {
        return INSTANCE;
    }

    /**
     * The domain cache. This is a map of all domains in the system.
     * The domains are indexed by their names.
     */
    private HashMap domains = new HashMap();

    /**
     * The host cache. This is a map of all hosts in the system. The
     * hosts are indexed by their names.
     */
    private HashMap hosts = new HashMap();

    /**
     * The site cache. This is a map of site arrays, containing the
     * highest revision of each one (no work revisions). It may
     * contain sites that are currently offline. The site arrays are
     * indexed by their domain name.
     */
    private HashMap sites = new HashMap();

    /**
     * The content parent identifier cache. This is a map containing
     * content parent identifiers, indexed by the content identifiers.
     * Only content objects where the work and the highest revision
     * correlates will be added to this cache.
     */
    private HashMap parents = new HashMap();

    /**
     * The content object cache. This is a map containing some of the
     * content objects, indexed by the content identifiers. Only
     * content objects where the work and the highest revision
     * correlates and being in a limited set of categories will be
     * added to this cache.
     */
    private HashMap contents = new HashMap();

    /**
     * The permission list cache. This is a map of domain and content
     * permission lists. It is indexed by either the domain name or
     * the content identifier. If the permission list is empty for a
     * specified domain or content object, a null value is stored in
     * this map.
     */
    private HashMap permissions = new HashMap();

    /**
     * Creates a new content cache manager.
     */
    private CacheManager() {
    }

    /**
     * Checks if a specified object is present in the cache. Only
     * objects that can be cached and that is not present will return
     * true. Non-cacheable objects will always return false. This
     * method is suitable to use before calling add() to avoid costs
     * of cache maintenance and object synchronization.
     *
     * @param obj            the object to check
     *
     * @return true if the object is cacheable but not cached, or
     *         false otherwise
     */
    public boolean isCached(PersistentObject obj) {
        Domain          domain;
        Host            host;
        Content         content;
        PermissionList  perms;
        Object          key;

        if (obj instanceof Domain) {
            domain = (Domain) obj;
            return domains.containsKey(domain.getName());
        } else if (obj instanceof Host) {
            host = (Host) obj;
            return hosts.containsKey(host.getName());
        } else if (obj instanceof Content) {
            content = (Content) obj;
            if (content.isLatestRevision() && content.isPublishedRevision()) {
                key = new Integer(content.getId());
                if (parents.containsKey(key)) {
                    if (content instanceof ContentTemplate) {
                        return contents.containsKey(key);
                    } else {
                        return true;
                    }
                }
            }
        } else if (obj instanceof PermissionList) {
            perms = (PermissionList) obj;
            if (perms.getContentId() == 0) {
                key = perms.getDomainName();
            } else {
                key = new Integer(perms.getContentId());
            }
            return permissions.containsKey(key);
        }
        return false;
    }

    /**
     * Adds a persistent object to the cache.
     *
     * @param obj            the object to add
     */
    public synchronized void add(PersistentObject obj) {
        Domain          domain;
        Host            host;
        Content         content;
        PermissionList  perms;
        Object          key;

        if (obj instanceof Domain) {
            domain = (Domain) obj;
            domains.put(domain.getName(), obj);
            LOG.trace("cached domain " + domain.getName());
        } else if (obj instanceof Host) {
            host = (Host) obj;
            hosts.put(host.getName(), obj);
            LOG.trace("cached host " + host.getName());
        } else if (obj instanceof Content) {
            content = (Content) obj;
            if (content instanceof ContentSite) {
                sites.remove(content.getDomainName());
                LOG.trace("uncached site list for " +
                          content.getDomainName());
            }
            if (content.isLatestRevision() && content.isPublishedRevision()) {
                key = new Integer(content.getId());
                parents.put(key, new Integer(content.getParentId()));
                LOG.trace("cached content parent for " + key);
                if (content instanceof ContentTemplate) {
                    contents.put(key, content);
                    LOG.trace("cached content object " + key);
                }
            }
        } else if (obj instanceof PermissionList) {
            perms = (PermissionList) obj;
            if (perms.getContentId() == 0) {
                key = perms.getDomainName();
            } else {
                key = new Integer(perms.getContentId());
            }
            if (perms.isEmpty()) {
                perms = null;
            }
            permissions.put(key, perms);
            LOG.trace("cached permission list for " + key);
        }
    }

    /**
     * Adds a set of persistent objects to the cache. The objects
     * will be added individually. That is, the array itself will not
     * be stored in the cache.
     *
     * @param objs           the objects to add
     */
    public void addAll(PersistentObject[] objs) {
        for (int i = 0; i < objs.length; i++) {
            if (!isCached(objs[i])) {
                add(objs[i]);
            }
        }
    }

    /**
     * Adds an array of content sites to the cache.
     *
     * @param domain         the domain to use for retrieval
     * @param content        the array of content sites
     */
    public synchronized void addSites(Domain domain,
                                      ContentSite[] content) {

        addAll(content);
        sites.put(domain.getName(), content);
        LOG.trace("cached site list for " + domain.getName());
    }

    /**
     * Removes a specified persistent object from the cache.
     *
     * @param obj            the object to remove
     */
    public synchronized void remove(PersistentObject obj) {
        Domain          domain;
        Host            host;
        Content         content;
        PermissionList  perms;

        if (obj instanceof Domain) {
            domain = (Domain) obj;
            domains.remove(domain.getName());
            LOG.trace("uncached domain " + domain.getName());
            hosts.clear();
            LOG.trace("uncached all hosts");
            sites.remove(domain.getName());
            LOG.trace("uncached site list for " + domain.getName());
            parents.clear();
            LOG.trace("uncached all content parents");
            contents.clear();
            LOG.trace("uncached all content objects");
            permissions.clear();
            LOG.trace("uncached all permission lists");
        } else if (obj instanceof Host) {
            host = (Host) obj;
            hosts.remove(host.getName());
            LOG.trace("uncached host " + host.getName());
        } else if (obj instanceof Content) {
            content = (Content) obj;
            if (obj instanceof ContentSite) {
                sites.remove(content.getDomainName());
                LOG.trace("uncached site list for " +
                          content.getDomainName());
            }
            parents.remove(new Integer(content.getId()));
            LOG.trace("uncached content parent for " + content.getId());
            if (obj instanceof ContentTemplate) {
                contents.remove(new Integer(content.getId()));
                LOG.trace("uncached content object " + content.getId());
            }
            permissions.remove(new Integer(content.getId()));
            LOG.trace("uncached permission list for " + content.getId());
        } else if (obj instanceof PermissionList) {
            perms = (PermissionList) obj;
            if (perms.getContentId() == 0) {
                permissions.remove(perms.getDomainName());
                LOG.trace("uncached permission list for " +
                          perms.getDomainName());
            } else {
                permissions.remove(new Integer(perms.getContentId()));
                LOG.trace("uncached permission list for " +
                          perms.getContentId());
            }
        }
    }

    /**
     * Removes all persistent objects from the cache. This is a
     * complete cache flush and should be avoided.
     */
    public synchronized void removeAll() {
        domains.clear();
        hosts.clear();
        sites.clear();
        parents.clear();
        contents.clear();
        permissions.clear();
        LOG.trace("cleared all caches");
    }

    /**
     * Returns a collection of all domains in the cache.
     *
     * @return the collection of domains in the cache
     */
    public Collection getAllDomains() {
        return domains.values();
    }

    /**
     * Returns a domain from the cache.
     *
     * @param name           the domain name
     *
     * @return the domain found, or
     *         null if not present in the cache
     */
    public Domain getDomain(String name) {
        return (Domain) domains.get(name);
    }

    /**
     * Returns a host from the cache.
     *
     * @param name           the host name
     *
     * @return the host found, or
     *         null if not present in the cache
     */
    public Host getHost(String name) {
        Host  host;

        host = (Host) hosts.get(name);
        if (host == null) {
            LOG.trace("cache miss on host " + name);
        } else {
            LOG.trace("cache hit on host " + name);
        }
        return host;
    }

    /**
     * Returns all sites in a domain.
     *
     * @param domain         the domain
     *
     * @return the array of sites in the domain, or
     *         null if not present in the cache
     */
    public ContentSite[] getSites(Domain domain) {
        return (ContentSite[]) sites.get(domain.getName());
    }

    /**
     * Returns a content object from the cache.
     *
     * @param id             the content identifier
     *
     * @return the content object, or
     *         null if not present in the cache
     */
    public Content getContent(int id) {
        Content  content;

        content = (Content) contents.get(new Integer(id));
        if (content == null) {
            LOG.trace("cache miss on content object " + id);
        } else {
            LOG.trace("cache hit on content object " + id);
        }
        return content;
    }

    /**
     * Returns a domain permission list from the cache.
     *
     * @param domain         the domain
     *
     * @return the permission list for the domain, or
     *         null if not present in the cache
     */
    public PermissionList getPermissions(Domain domain) {
        PermissionList  perms;

        if (domain == null) {
            return null;
        }
        perms = (PermissionList) permissions.get(domain.getName());
        if (!permissions.containsKey(domain.getName())) {
            LOG.trace("cache miss on permission list for " +
                      domain.getName());
            return null;
        } else if (perms == null) {
            LOG.trace("cache hit on empty permission list for " +
                      domain.getName());
            return new PermissionList(domain.getContentManager(), domain);
        } else {
            LOG.trace("cache hit on permission list for " +
                      domain.getName());
            return perms;
        }
    }
    
    /**
     * Returns a content permission list from the cache. If the object
     * has no permissions either an empty list or the inherited
     * permission list will be returned.
     *
     * @param content        the content object
     * @param inherit        the search inherited permissions flag
     *
     * @return the permission list for the domain, or
     *         null if not present in the cache
     */
    public PermissionList getPermissions(Content content, boolean inherit) {
        PermissionList  perms;
        Object          key;

        if (content == null) {
            return null;
        }
        key = new Integer(content.getId());
        perms = (PermissionList) permissions.get(key);
        if (inherit) {
            while (perms == null && permissions.containsKey(key)) {
                key = (Integer) parents.get(key);
                if (key == null) {
                    LOG.trace("cache miss on permission list for " +
                              content.getId() + " due to uncached parents");
                    return null;
                } else if (key instanceof Integer
                        && ((Integer) key).intValue() == 0) {

                    return getPermissions(getDomain(content.getDomainName()));
                }
                perms = (PermissionList) permissions.get(key);
            }
        }
        if (!permissions.containsKey(key)) {
            LOG.trace("cache miss on permission list for " +
                      content.getId());
            return null;
        } else if (perms == null) {
            LOG.trace("cache hit on empty permission list for " +
                      content.getId());
            return new PermissionList(content.getContentManager(), content);
        } else {
            LOG.trace("cache hit on permission list for " +
                      content.getId());
            return perms;
        }
    }
}
