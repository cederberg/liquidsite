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

package net.percederberg.liquidsite.content;

import java.util.ArrayList;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseQuery;
import net.percederberg.liquidsite.db.DatabaseResults;

/**
 * A host database peer. This class contains static methods that
 * handles all accesses to the database representation of a host.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class HostPeer extends Peer {

    /**
     * The host peer instance.
     */
    private static final Peer PEER = new HostPeer();

    /**
     * Returns a list of all hosts in the database.
     * 
     * @return a list of all hosts in the database
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectAll() throws ContentException {
        return doSelectAll(null);
    }

    /**
     * Returns a list of all hosts in the database.
     * 
     * @param con            the database connection to use
     * 
     * @return a list of all hosts in the database
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectAll(DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("host.select.all");
        DatabaseResults  res;
        
        res = execute("reading hosts", query, con);
        return PEER.createObjectList(res);
    }

    /**
     * Returns a list of all hosts in a certain domain.
     * 
     * @param domain         the domain
     * 
     * @return a list of all hosts in the domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByDomain(Domain domain)
        throws ContentException {

        return doSelectByDomain(domain, null);
    }

    /**
     * Returns a list of all hosts in a certain domain.
     * 
     * @param domain         the domain
     * @param con            the database connection to use
     * 
     * @return a list of all hosts in the domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByDomain(Domain domain,
                                             DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("host.select.domain");
        DatabaseResults  res;
        
        query.addParameter(domain.getName());
        res = execute("reading hosts", query, con);
        return PEER.createObjectList(res);
    }

    /**
     * Returns a host with a specified name.
     * 
     * @param name           the host name
     * 
     * @return the host found, or
     *         null if no matching host existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Host doSelectByName(String name)
        throws ContentException {

        return doSelectByName(name, null);
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
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Host doSelectByName(String name, DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("host.select.name");
        DatabaseResults  res;
        
        query.addParameter(name);
        res = execute("reading host", query, con);
        return (Host) PEER.createObject(res);
    }

    /**
     * Inserts a new host into the database. This method also updates
     * the content manager cache.
     * 
     * @param host           the host to insert
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(Host host) throws ContentException {
        doInsert(host, null);
    }

    /**
     * Inserts a new host into the database. This method also updates
     * the content manager cache.
     * 
     * @param host           the host to insert
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(Host host, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("host.insert");

        host.validate();
        query.addParameter(host.getName());
        query.addParameter(host.getDescription());
        query.addParameter(host.getOptions());
        execute("inserting host", query, con);
        host.setModified(false);
        host.setPersistent(true);
        ContentManager.getInstance().addHost(host);
    }
    
    /**
     * Updates a host in the database. This method also updates the
     * content manager cache.
     * 
     * @param host           the host to update
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(Host host) throws ContentException {
        doUpdate(host, null);
    }

    /**
     * Updates a host in the database. This method also updates the
     * content manager cache.
     * 
     * @param host           the host to update
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(Host host, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("host.update");

        host.validate();
        query.addParameter(host.getDescription());
        query.addParameter(host.getOptions());
        query.addParameter(host.getName());
        execute("updating host", query, con);
        host.setModified(false);
        host.setPersistent(true);
        ContentManager.getInstance().addHost(host);
    }
    
    /**
     * Deletes a host from the database. This method also updates 
     * the content manager cache.
     * 
     * @param host           the host to delete
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Host host) throws ContentException {
        doDelete(host, null);
    }

    /**
     * Deletes a host from the database. This method also updates 
     * the content manager cache.
     * 
     * @param host           the host to delete
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Host host, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("host.delete");

        query.addParameter(host.getName());
        execute("deleting host", query, con);
        host.setModified(true);
        host.setPersistent(false);
        ContentManager.getInstance().removeHost(host);
    }
    
    /**
     * Creates a new host database peer.
     */
    private HostPeer() {
        super("host", Host.class);
    }

    /**
     * Transfers a database result row to a data object.
     * 
     * @param row            the database result row
     * @param obj            the data object
     * 
     * @throws DatabaseDataException if the database results didn't
     *             contain the expected column names
     */
    protected void transfer(DatabaseResults.Row row, DataObject obj) 
        throws DatabaseDataException {

        Host  host = (Host) obj;

        host.setDomainName(row.getString("DOMAIN"));
        host.setName(row.getString("NAME"));
        host.setDescription(row.getString("DESCRIPTION"));
        host.setOptions(row.getString("OPTIONS"));
        host.setModified(false);
        host.setPersistent(true);
    }
}
