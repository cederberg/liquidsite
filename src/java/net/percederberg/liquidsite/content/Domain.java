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
public class Domain extends PersistentObject {

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
     * Checks the read access for a user. In the absence of 
     * permissions, false is returned.
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
        Permission[]  perms = getPermissions();
        Group[]       groups = null;
        
        // Check for superuser and empty permission list
        if (user != null && user.getDomainName().equals("")) {
            return true;
        } else if (perms.length == 0) {
            return false; 
        }

        // Check content permissions
        if (user != null) {
            groups = user.getGroups();
        }
        for (int i = 0; i < perms.length; i++) {
            if (perms[i].isMatch(user, groups) && perms[i].getRead()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks the write access for a user. In the absence of 
     * permissions, false is returned.
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
        Permission[]  perms = getPermissions();
        Group[]       groups = null;
        
        // Check for superuser and empty permission list
        if (user != null && user.getDomainName().equals("")) {
            return true;
        } else if (perms.length == 0) {
            return false; 
        }

        // Check content permissions
        if (user != null) {
            groups = user.getGroups();
        }
        for (int i = 0; i < perms.length; i++) {
            if (perms[i].isMatch(user, groups) && perms[i].getWrite()) {
                return true;
            }
        }

        return false;
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
     * Saves this object to the database. This method checks the 
     * permissions of the specified user for performing the 
     * operation.
     * 
     * @param user           the user performing the operation
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     * @throws ContentSecurityException if the user specified didn't
     *             have write permissions
     */
    public void save(User user) 
        throws ContentException, ContentSecurityException {

        if (!hasWriteAccess(user)) {
            throw new ContentSecurityException(user, "write", this);
        }
        super.save(user);
    }
    
    /**
     * Deletes this object from the database. This method to check 
     * the permissions of the specified user for performing the 
     * operation.
     * 
     * @param user           the user performing the operation
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     * @throws ContentSecurityException if the user specified didn't
     *             have write permissions
     */
    public void delete(User user) 
        throws ContentException, ContentSecurityException {

        if (!hasWriteAccess(user)) {
            throw new ContentSecurityException(user, "delete", this);
        }
        super.delete(user);
    }

    /**
     * Inserts the object data into the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    protected void doInsert(DatabaseConnection con)
        throws DatabaseObjectException {

        data.setString(DomainData.OPTIONS, encodeMap(options));
        DomainPeer.doInsert(data, con);
    }

    /**
     * Updates the object data in the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    protected void doUpdate(DatabaseConnection con)
        throws DatabaseObjectException {

        data.setString(DomainData.OPTIONS, encodeMap(options));
        DomainPeer.doUpdate(data, con);
    }

    /**
     * Deletes the object data from the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    protected void doDelete(DatabaseConnection con)
        throws DatabaseObjectException {

        DomainPeer.doDelete(data, con);
    }
}
