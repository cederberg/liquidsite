/*
 * Host.java
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

import net.percederberg.liquidsite.db.DatabaseConnection;

/**
 * A web site host.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Host extends DataObject {

    /**
     * The domain the host belongs to.
     */
    private String domain = "";

    /**
     * The fully qualified host name.
     */
    private String name = "";
    
    /**
     * The host description.
     */
    private String description = "";
    
    /**
     * The host options.
     */
    private HashMap options = new HashMap();
    
    /**
     * Creates a new host with default values.
     */
    public Host() {
    }

    /**
     * Checks if this host equals another object. This method will 
     * only return true if the other object is a host with the same
     * name.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical host, or
     *         false otherwise 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Host) {
            return name.equals(((Host) obj).name);
        } else {
            return false;
        }
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        return name;
    }

    /**
     * Returns the host domain.
     * 
     * @return the host domain
     * 
     * @throws ContentException if no content manager is available
     */
    public Domain getDomain() throws ContentException {
        return ContentManager.getInstance().getDomain(domain);
    }
    
    /**
     * Sets the host domain.
     * 
     * @param domain         the new domain
     */
    public void setDomain(Domain domain) {
        setDomainName(domain.getName());
    }
    
    /**
     * Returns the host domain name
     * 
     * @return the host domain name
     */
    public String getDomainName() {
        return domain;
    }
    
    /**
     * Sets the host domain.
     * 
     * @param domain         the new domain name
     */
    public void setDomainName(String domain) {
        this.domain = domain;
        setModified(true);
    }
    
    /**
     * Returns the unique host name
     * 
     * @return the unique host name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the host name.
     * 
     * @param name           the new host name
     */
    public void setName(String name) {
        this.name = name;
        setModified(true);
        setPersistent(false);
    }
    
    /**
     * Returns the host description.
     * 
     * @return the host description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the host description.
     * 
     * @param description    the new description
     */
    public void setDescription(String description) {
        this.description = description;
        setModified(true);
    }
    
    /**
     * Returns the encoded options.
     * 
     * @return the encoded options
     */
    String getOptions() {
        StringBuffer  buffer = new StringBuffer();
        Iterator      iter = options.keySet().iterator();
        String        name;
        Object        value;

        while (iter.hasNext()) {
            name = (String) iter.next();
            value = options.get(name);
            if (buffer.length() > 0) {
                buffer.append(":");
            }
            buffer.append(name);
            if (value != null) {
                buffer.append("=");
                buffer.append(value);
            }
        }
        return buffer.toString();
    }

    /**
     * Sets the encoded options.
     * 
     * @param options        the new options
     */
    void setOptions(String options) {
        String  name;
        String  value;
        String  str;
        int     pos;

        this.options.clear();
        while (options.length() > 0) {
            pos = options.indexOf(":");
            if (pos > 0) {
                str = options.substring(0, pos);
                options = options.substring(pos + 1);
            } else {
                str = options;
                options = "";
            }
            pos = str.indexOf("=");
            if (pos > 0) {
                name = str.substring(0, pos);
                value = str.substring(pos + 1);
            } else {
                name = str;
                value = null;
            }
            this.options.put(name, value);
        }
        setModified(true);
    }

    /**
     * Validates this data object. This method checks that all 
     * required fields have been filled with suitable values.
     * 
     * @throws ContentException if the data object contained errors
     */
    public void validate() throws ContentException {
        Host  host = ContentManager.getInstance().getHost(name);

        if (domain.equals("")) {
            throw new ContentException("no domain set for host object");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + domain + 
                                       "'does not exist");
        } else if (name.equals("")) {
            throw new ContentException("no name set for host object");
        } else if (isPersistent() && host == null) {
            throw new ContentException("host '" + name + 
                                       "' does not exist");
        } else if (!isPersistent() && host != null) {
            throw new ContentException("host '" + name + 
                                       "' already exists");
        }
    }

    /**
     * Saves this host to the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public void save(DatabaseConnection con) throws ContentException {
        if (!isPersistent()) {
            HostPeer.doInsert(this, con);
        } else if (isModified()) {
            HostPeer.doUpdate(this, con);
        }
    }
}
