/*
 * DomainPeer.java
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
import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseResults;

/**
 * A domain database peer. This class contains static methods that
 * handles all accesses to the database representation of a domain.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public final class DomainPeer extends Peer {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DomainPeer.class);

    /**
     * The domain peer instance.
     */
    private static final Peer PEER = new DomainPeer();

    /**
     * Returns a list of all domains in the database.
     * 
     * @return a list of all domains in the database
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectAll() throws ContentException {
        return doSelectAll(null);
    }

    /**
     * Returns a list of all domains in the database.
     * 
     * @param con            the database connection to use
     * 
     * @return a list of all domains in the database
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectAll(DatabaseConnection con) 
        throws ContentException {

        DatabaseResults  res;
        
        res = execute("domain.select.all", null, "reading domains", con);
        return PEER.createObjectList(res);
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
    public static Domain doSelectByName(String name)
        throws ContentException {

        return doSelectByName(name, null);
    }

    /**
     * Returns a domain with a specified name.
     * 
     * @param name           the domain name
     * @param con            the database connection to use
     * 
     * @return the domain found, or
     *         null if no matching domain existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Domain doSelectByName(String name, 
                                        DatabaseConnection con)
        throws ContentException {

        ArrayList        params = new ArrayList();
        DatabaseResults  res;
        
        params.add(name);
        res = execute("domain.select.name", params, "reading domain", con);
        return (Domain) PEER.createObject(res);
    }

    /**
     * Inserts a new domain into the database. This method also 
     * updates the content manager cache.
     * 
     * @param domain         the domain to insert
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(Domain domain) 
        throws ContentException {

        doInsert(domain, null);
    }
    
    /**
     * Inserts a new domain into the database. This method also 
     * updates the content manager cache.
     * 
     * @param domain         the domain to insert
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(Domain domain, DatabaseConnection con) 
        throws ContentException {

        ArrayList  params = new ArrayList();

        domain.validate();
        params.add(domain.getName());
        params.add(domain.getDescription());
        params.add(domain.getOptions());
        execute("domain.insert", params, "inserting domain", con);
        getContentManager().addDomain(domain);
    }
    
    /**
     * Updates a domain in the database. This method also updates the
     * content manager cache.
     * 
     * @param domain         the domain to update
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(Domain domain) 
        throws ContentException {

        doUpdate(domain, null);
    }
    
    /**
     * Updates a domain in the database. This method also updates the
     * content manager cache.
     * 
     * @param domain         the domain to update
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(Domain domain, DatabaseConnection con) 
        throws ContentException {

        ArrayList  params = new ArrayList();

        domain.validate();
        params.add(domain.getDescription());
        params.add(domain.getOptions());
        params.add(domain.getName());
        execute("domain.update", params, "updating domain", con);
        getContentManager().addDomain(domain);
    }
    
    /**
     * Deletes a domain from the database. This method also updates 
     * the content manager cache.
     * 
     * @param domain         the domain to delete
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Domain domain) 
        throws ContentException {

        doDelete(domain, null);
    }
    
    /**
     * Deletes a domain from the database. This method also updates 
     * the content manager cache.
     * 
     * @param domain         the domain to delete
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Domain domain, DatabaseConnection con) 
        throws ContentException {

        ArrayList  params = new ArrayList();

        params.add(domain.getName());
        execute("domain.delete", params, "deleting domain", con);
        getContentManager().removeDomain(domain);
    }
    
    /**
     * Creates a new domain database peer.
     */
    private DomainPeer() {
        super("domain", Domain.class);
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

        Domain  domain = (Domain) obj;

        domain.setName(row.getString("NAME"));
        domain.setDescription(row.getString("DESCRIPTION"));
        domain.setOptions(row.getString("OPTIONS"));
        domain.setModified(false);
        domain.setPersistent(true);
    }
}
