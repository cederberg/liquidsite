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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.content;

import java.util.Collection;
import java.util.HashMap;

/**
 * A content cache manager. This class will be used to cache objects
 * upon retrieval from a caching content manager. Only a single 
 * instance exists, in order to easily guarantee cache consistency.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class CacheManager {

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
     * Creates a new content cache manager.
     */
    private CacheManager() {
    }

    /**
     * Adds a persistent object to the cache.
     * 
     * @param obj            the object to add
     */
    public void add(PersistentObject obj) { 
        remove(obj);
        if (obj instanceof Domain) {
            domains.put(((Domain) obj).getName(), obj);
        } else if (obj instanceof Host) {
            hosts.put(((Host) obj).getName(), obj);
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
            add(objs[i]);
        }
    }

    /**
     * Adds an array of content sites to the cache.
     * 
     * @param domain         the domain to use for retrieval
     * @param content        the array of content sites
     */
    public void addSites(Domain domain, Content[] content) {
        sites.put(domain.getName(), content);
    }

    /**
     * Removes a specified persistent object from the cache.
     * 
     * @param obj            the object to remove
     */
    public void remove(PersistentObject obj) {
        if (obj instanceof Domain) {
            domains.remove(((Domain) obj).getName());
        } else if (obj instanceof Host) {
            hosts.remove(((Host) obj).getName());
        } else if (obj instanceof ContentSite) {
            sites.remove(((ContentSite) obj).getDomainName());
        }
    }

    /**
     * Removes all persistent objects from the cache. This is a
     * complete cache flush and should be avoided.
     */
    public void removeAll() {
        domains.clear();
        hosts.clear();
        sites.clear();
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
        return (Host) hosts.get(name);
    }

    /**
     * Returns all sites in a domain.
     * 
     * @param domain         the domain
     * 
     * @return the array of sites in the domain, or
     *         null if not present in the cache
     */
    public Content[] getSites(Domain domain) {
        return (Content[]) sites.get(domain.getName());
    }
}
