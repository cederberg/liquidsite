/*
 * HostPeer.java
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

package net.percederberg.liquidsite.dbo;

import java.util.ArrayList;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseQuery;

/**
 * A host database peer. This class contains static methods that
 * handles all accesses to the LS_HOST table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class HostPeer extends AbstractPeer {

    /**
     * The host peer instance.
     */
    private static final HostPeer PEER = new HostPeer();

    /**
     * Returns a list of all hosts in the database.
     * 
     * @param con            the database connection to use
     * 
     * @return a list of all hosts in the database
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static ArrayList doSelectAll(DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.select.all");
        
        return PEER.selectList(query, con);
    }

    /**
     * Returns a list of all hosts in a certain domain.
     * 
     * @param domain         the domain name
     * @param con            the database connection to use
     * 
     * @return a list of all hosts in the domain
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static ArrayList doSelectByDomain(String domain,
                                             DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.select.domain");
        
        query.addParameter(domain);
        return PEER.selectList(query, con);
    }

    /**
     * Returns a host with a specified name.
     * 
     * @param name           the host name
     * @param con            the database connection to use
     * 
     * @return the host found, or
     *         null if no matching host existed
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static HostData doSelectByName(String name, 
                                          DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.select.name");
        
        query.addParameter(name);
        return (HostData) PEER.select(query, con);
    }

    /**
     * Inserts a new host into the database.
     * 
     * @param data           the host data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doInsert(HostData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.insert");

        query.addParameter(data.getString(HostData.DOMAIN));
        query.addParameter(data.getString(HostData.NAME));
        query.addParameter(data.getString(HostData.DESCRIPTION));
        query.addParameter(data.getString(HostData.OPTIONS));
        PEER.insert(query, con);
    }
    
    /**
     * Updates a host in the database.
     * 
     * @param data           the host data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doUpdate(HostData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.update");

        query.addParameter(data.getString(HostData.DESCRIPTION));
        query.addParameter(data.getString(HostData.OPTIONS));
        query.addParameter(data.getString(HostData.NAME));
        PEER.update(query, con);
    }
    
    /**
     * Deletes a host from the database.
     * 
     * @param data           the host data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doDelete(HostData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.delete");

        query.addParameter(data.getString(HostData.NAME));
        PEER.delete(query, con);
    }

    /**
     * Deletes all hosts in a domain from the database.
     * 
     * @param domain         the domain name
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doDeleteDomain(String domain, 
                                      DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.delete.domain");

        query.addParameter(domain);
        PEER.delete(query, con);
    }

    /**
     * Creates a new host database peer.
     */
    private HostPeer() {
        super("host");
    }

    /**
     * Returns a new instance of the data object.
     * 
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new HostData();
    }
}
