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

import java.util.ArrayList;
import java.util.HashMap;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.DatabaseObjectException;
import net.percederberg.liquidsite.dbo.HostData;
import net.percederberg.liquidsite.dbo.HostPeer;

/**
 * A web site host.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Host extends PersistentObject {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(Host.class);

    /**
     * The host data object.
     */
    private HostData data;

    /**
     * The host options. These options are read from and stored to
     * the data object upon reading and writing. 
     */
    private HashMap options;
    
    /**
     * Returns an array of all hosts in the database.
     *
     * @param manager        the content manager to use
     *  
     * @return an array of all hosts in the database
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    static Host[] findAll(ContentManager manager) throws ContentException {
        DatabaseConnection  con = getDatabaseConnection(manager);
        ArrayList           list;
        Host[]              res;

        try {
            list = HostPeer.doSelectAll(con);
            res = new Host[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = new Host(manager, (HostData) list.get(i));
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
        return res;
    }

    /**
     * Returns an array of all hosts in a certain domain.
     * 
     * @param manager        the content manager to use
     * @param domain         the domain
     * 
     * @return an array of all hosts in the domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    static Host[] findByDomain(ContentManager manager, Domain domain)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ArrayList           list;
        Host[]              res;

        try {
            list = HostPeer.doSelectByDomain(domain.getName(), con);
            res = new Host[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = new Host(manager, (HostData) list.get(i));
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
        return res;
    }

    /**
     * Returns a host with a specified name.
     * 
     * @param manager        the content manager to use
     * @param name           the host name
     * 
     * @return the host found, or
     *         null if no matching host existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    static Host findByName(ContentManager manager, String name)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        HostData            data;

        try {
            data = HostPeer.doSelectByName(name, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
        if (data == null) {
            return null;
        } else {
            return new Host(manager, data);
        }
    }

    /**
     * Creates a new host with default values.
     * 
     * @param manager        the content manager to use 
     * @param domain         the domain
     * @param name           the host name
     */
    public Host(ContentManager manager, Domain domain, String name) {
        super(manager, false, true);
        this.data = new HostData();
        this.data.setString(HostData.DOMAIN, domain.getName());
        this.data.setString(HostData.NAME, name);
        this.options = new HashMap();
    }

    /**
     * Creates a new host from a data object.
     * 
     * @param manager        the content manager to use 
     * @param data           the host data object
     */
    private Host(ContentManager manager, HostData data) {
        super(manager, true, true);
        this.data = data;
        this.options = decodeMap(data.getString(HostData.OPTIONS));
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
            return getName().equals(((Host) obj).getName());
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
        return getName();
    }

    /**
     * Returns the host domain.
     * 
     * @return the host domain
     * 
     * @throws ContentException if no content manager is available
     */
    public Domain getDomain() throws ContentException {
        return getContentManager().getDomain(getDomainName());
    }
    
    /**
     * Returns the host domain name.
     * 
     * @return the host domain name
     */
    public String getDomainName() {
        return data.getString(HostData.DOMAIN);
    }
    
    /**
     * Returns the unique host name.
     * 
     * @return the unique host name
     */
    public String getName() {
        return data.getString(HostData.NAME);
    }
    
    /**
     * Returns the host description.
     * 
     * @return the host description
     */
    public String getDescription() {
        return data.getString(HostData.DESCRIPTION);
    }
    
    /**
     * Sets the host description.
     * 
     * @param description    the new description
     */
    public void setDescription(String description) {
        data.setString(HostData.DESCRIPTION, description);
    }
    
    /**
     * Validates this data object. This method checks that all 
     * required fields have been filled with suitable values.
     * 
     * @throws ContentException if the data object contained errors
     */
    public void validate() throws ContentException {
        Host  host = getContentManager().getHost(getName());

        if (getDomainName().equals("")) {
            throw new ContentException("no domain set for host object");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + getDomainName() + 
                                       "'does not exist");
        } else if (getName().equals("")) {
            throw new ContentException("no name set for host object");
        } else if (!isPersistent() && host != null) {
            throw new ContentException("host '" + getName() + 
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
        data.setString(HostData.OPTIONS, encodeMap(options));
        try {
            HostPeer.doInsert(data, con);
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
        data.setString(HostData.OPTIONS, encodeMap(options));
        try {
            HostPeer.doUpdate(data, con);
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
            HostPeer.doDelete(data, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }
}
