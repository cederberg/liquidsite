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

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.db.DatabaseDataException;
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
     * The class logger.
     */
    private static final Log LOG = new Log(HostPeer.class);

    /**
     * Returns a list of all hosts in the database.
     * 
     * @return a list of all hosts in the database
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectAll() throws ContentException {
        DatabaseResults  res;
        ArrayList        list = new ArrayList();
        Host             host;        
        
        res = execute("host.select.all", "reading hosts");
        for (int i = 0; i < res.getRowCount(); i++) {
            host = new Host();
            try {
                transfer(res.getRow(i), host);
            } catch (DatabaseDataException e) {
                LOG.error("reading hosts", e);
                throw new ContentException("reading hosts", e);
            }
            list.add(host);
        }
        return list;
    }

    /**
     * Returns a list of all hosts in a certain domain.
     * 
     * @param domain         the domain
     * 
     * @return a list of all hosts in a certain domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByDomain(Domain domain)
        throws ContentException {

        ArrayList        params = new ArrayList();
        DatabaseResults  res;
        ArrayList        list = new ArrayList();
        Host             host;        
        
        params.add(domain.getName());
        res = execute("host.select.domain", "reading hosts");
        for (int i = 0; i < res.getRowCount(); i++) {
            host = new Host();
            try {
                transfer(res.getRow(i), host);
            } catch (DatabaseDataException e) {
                LOG.error("reading hosts", e);
                throw new ContentException("reading hosts", e);
            }
            list.add(host);
        }
        return list;
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

        ArrayList        params = new ArrayList();
        DatabaseResults  res;
        Host             host;
        
        params.add(name);
        res = execute("host.select.name", "reading host");
        if (res.getRowCount() < 1) {
            return null;
        } else {
            try {
                host = new Host();
                transfer(res.getRow(0), host);
            } catch (DatabaseDataException e) {
                LOG.error("reading host", e);
                throw new ContentException("reading host", e);
            }
        }
        return host;
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
    public static synchronized void doInsert(Host host) 
        throws ContentException {

        ArrayList  params = new ArrayList();

        params.add(host.getName());
        params.add(host.getDescription());
        params.add(host.getOptions());
        execute("host.insert", params, "inserting host");
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
    public static synchronized void doUpdate(Host host) 
        throws ContentException {

        ArrayList  params = new ArrayList();

        params.add(host.getDescription());
        params.add(host.getOptions());
        params.add(host.getName());
        execute("host.update", params, "updating host");
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
    public static synchronized void doDelete(Host host) 
        throws ContentException {

        ArrayList  params = new ArrayList();

        params.add(host.getName());
        execute("host.delete", params, "deleting host");
        ContentManager.getInstance().removeHost(host);
    }
    
    /**
     * Transfers a database result row to a host object.
     * 
     * @param row            the database result row
     * @param host           the host object
     * 
     * @throws DatabaseDataException if the database results didn't
     *             contain the expected column names
     */
    private static void transfer(DatabaseResults.Row row, Host host) 
        throws DatabaseDataException {

        host.setDomainName(row.getString("DOMAIN"));
        host.setName(row.getString("NAME"));
        host.setDescription(row.getString("DESCRIPTION"));
        host.setOptions(row.getString("OPTIONS"));
        host.setModified(false);
        host.setPersistent(true);
    }

}
