/*
 * Domain.java
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

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.DatabaseObjectException;
import net.percederberg.liquidsite.dbo.DomainData;
import net.percederberg.liquidsite.dbo.DomainPeer;

/**
 * A resource and user domain.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Domain extends PersistentObject implements Comparable {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(Domain.class);

    /**
     * The domain data object.
     */
    private DomainData data;
    
    /**
     * The domain options. These options are read from and stored to
     * the data object upon reading and writing.
     */
    private HashMap options;
    
    /**
     * Returns an array of all domains in the database.
     * 
     * @return an array of all domains in the database
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected static Domain[] findAll() throws ContentException {
        DatabaseConnection  con = getDatabaseConnection();
        ArrayList           list;
        Domain[]            res;

        try {
            list = DomainPeer.doSelectAll(con);
            res = new Domain[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = new Domain((DomainData) list.get(i));
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(con);
        }
        return res;
    }

    /**
     * Returns a domain with a specified name.
     * 
     * @param name           the domain name
     * 
     * @return the domain found, or
     *         null if no matching domain existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected static Domain findByName(String name) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection();
        DomainData          data;

        try {
            data = DomainPeer.doSelectByName(name, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(con);
        }
        if (data == null) {
            return null;
        } else {
            return new Domain(data);
        }
    }

    /**
     * Creates a new domain with default values.
     * 
     * @param name           the domain name
     */
    public Domain(String name) {
        super(false, true);
        this.data = new DomainData();
        this.data.setString(DomainData.NAME, name);
        this.options = new HashMap();
    }

    /**
     * Creates a new domain from a data object.
     * 
     * @param data           the domain data object
     */
    private Domain(DomainData data) {
        super(true, true);
        this.data = data;
        this.options = decodeMap(data.getString(DomainData.OPTIONS));
    }

    /**
     * Checks if this domain equals another object. This method will 
     * only return true if the other object is a domain with the same
     * name.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical domain, or
     *         false otherwise 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Domain) {
            return getName().equals(((Domain) obj).getName());
        } else {
            return false;
        }
    }

    /**
     * Compares this object with the specified object for order. 
     * Returns a negative integer, zero, or a positive integer as 
     * this object is less than, equal to, or greater than the 
     * specified object.
     * 
     * @param obj            the object to compare to
     * 
     * @return a negative integer, zero, or a positive integer as 
     *         this object is less than, equal to, or greater than 
     *         the specified object
     * 
     * @throws ClassCastException if the object isn't a Domain object
     */
    public int compareTo(Object obj) throws ClassCastException {
        return compareTo((Domain) obj);
    }

    /**
     * Compares this object with the specified domain for order. 
     * Returns a negative integer, zero, or a positive integer as 
     * this object is less than, equal to, or greater than the 
     * specified object. The ordering is based on domain name.
     * 
     * @param domain         the domain to compare to
     * 
     * @return a negative integer, zero, or a positive integer as 
     *         this object is less than, equal to, or greater than 
     *         the specified object
     */
    public int compareTo(Domain domain) {
        return getName().compareTo(domain.getName());
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();
        
        buffer.append("Domain: ");
        if (getDescription().equals("")) {
            buffer.append(getName());
        } else {
            buffer.append(getDescription());
        }
        return buffer.toString();
    }

    /**
     * Returns the unique domain name.
     * 
     * @return the unique domain name
     */
    public String getName() {
        return data.getString(DomainData.NAME);
    }
    
    /**
     * Returns the domain description.
     * 
     * @return the domain description
     */
    public String getDescription() {
        return data.getString(DomainData.DESCRIPTION);
    }
    
    /**
     * Sets the domain description.
     * 
     * @param description    the new description
     */
    public void setDescription(String description) {
        data.setString(DomainData.DESCRIPTION, description);
    }
    
    /**
     * Returns the permissions applicable to this domain object. 
     * 
     * @return an array of permissions for this object
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Permission[] getPermissions() throws ContentException {
        return Permission.findByDomain(this);
    }

    /**
     * Returns the hosts registered to this domain object. 
     * 
     * @return an array of hosts in this domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Host[] getHosts() throws ContentException {
        return Host.findByDomain(this);
    }

    /**
     * Checks the read access for a user.
     *
     * @param user           the user to check, or null for none
     * 
     * @return true if the user has read access, or
     *         false otherwise
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasReadAccess(User user) throws ContentException {
        return getSecurityManager().hasReadAccess(user, this);
    }

    /**
     * Checks the write access for a user.
     *
     * @param user           the user to check, or null for none
     * 
     * @return true if the user has write access, or
     *         false otherwise
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasWriteAccess(User user) throws ContentException {
        return getSecurityManager().hasWriteAccess(user, this);
    }

    /**
     * Checks the publish access for a user.
     *
     * @param user           the user to check, or null for none
     * 
     * @return true if the user has publish access, or
     *         false otherwise
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasPublishAccess(User user) throws ContentException {
        return getSecurityManager().hasPublishAccess(user, this);
    }

    /**
     * Checks the admin access for a user.
     *
     * @param user           the user to check, or null for none
     * 
     * @return true if the user has admin access, or
     *         false otherwise
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasAdminAccess(User user) throws ContentException {
        return getSecurityManager().hasAdminAccess(user, this);
    }

    /**
     * Validates this data object. This method checks that all 
     * required fields have been filled with suitable values.
     * 
     * @throws ContentException if the data object contained errors
     */
    public void validate() throws ContentException {
        Domain  domain = getContentManager().getDomain(getName());

        if (getName().equals("")) {
            throw new ContentException("no name set for domain object");
        } else if (!isPersistent() && domain != null) {
            throw new ContentException("domain '" + getName() + 
                                       "' already exists");
        }
    }

    /**
     * Inserts the object data into the database.
     * 
     * @param user           the user performing the operation
     * @param con            the database connection to use
     * 
     * @throws ContentException if the object data didn't validate or 
     *             if the database couldn't be accessed properly
     */
    protected void doInsert(User user, DatabaseConnection con)
        throws ContentException {

        validate();
        data.setString(DomainData.OPTIONS, encodeMap(options));
        try {
            DomainPeer.doInsert(data, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Updates the object data in the database.
     * 
     * @param user           the user performing the operation
     * @param con            the database connection to use
     * 
     * @throws ContentException if the object data didn't validate or 
     *             if the database couldn't be accessed properly
     */
    protected void doUpdate(User user, DatabaseConnection con)
        throws ContentException {

        validate();
        data.setString(DomainData.OPTIONS, encodeMap(options));
        try {
            DomainPeer.doUpdate(data, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Deletes the object data from the database.
     * 
     * @param user           the user performing the operation
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected void doDelete(User user, DatabaseConnection con)
        throws ContentException {

        try {
            DomainPeer.doDelete(data, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }
}
