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
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseResults;

/**
 * A domain database peer. This class contains static methods that
 * handles all accesses to the database representation of a domain.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DomainPeer extends Peer {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DomainPeer.class);

    /**
     * Returns a list of all domains in the database.
     * 
     * @return a list of all domains in the database
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectAll() throws ContentException {
        DatabaseResults  res;
        ArrayList        list = new ArrayList();
        Domain           domain;        
        
        res = execute("domain.select.all", "reading domains");
        for (int i = 0; i < res.getRowCount(); i++) {
            domain = new Domain();
            try {
                transfer(res.getRow(i), domain);
            } catch (DatabaseDataException e) {
                LOG.error("reading domains", e);
                throw new ContentException("reading domains", e);
            }
            list.add(domain);
        }
        return list;
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

        ArrayList        params = new ArrayList();
        DatabaseResults  res;
        Domain           domain;
        
        params.add(name);
        res = execute("domain.select.name", "reading domain");
        if (res.getRowCount() < 1) {
            return null;
        } else {
            try {
                domain = new Domain();
                transfer(res.getRow(0), domain);
            } catch (DatabaseDataException e) {
                LOG.error("reading domain", e);
                throw new ContentException("reading domain", e);
            }
        }
        return domain;
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
    public static synchronized void doInsert(Domain domain) 
        throws ContentException {

        ArrayList  params = new ArrayList();

        params.add(domain.getName());
        params.add(domain.getDescription());
        params.add(domain.getOptions());
        execute("domain.insert", params, "inserting domain");
        ContentManager.getInstance().addDomain(domain);
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
    public static synchronized void doUpdate(Domain domain) 
        throws ContentException {

        ArrayList  params = new ArrayList();

        params.add(domain.getDescription());
        params.add(domain.getOptions());
        params.add(domain.getName());
        execute("domain.update", params, "updating domain");
        ContentManager.getInstance().addDomain(domain);
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
    public static synchronized void doDelete(Domain domain) 
        throws ContentException {

        ArrayList  params = new ArrayList();

        params.add(domain.getName());
        execute("domain.delete", params, "deleting domain");
        ContentManager.getInstance().removeDomain(domain);
    }
    
    /**
     * Transfers a database result row to a domain object.
     * 
     * @param row            the database result row
     * @param domain         the domain object
     * 
     * @throws DatabaseDataException if the database results didn't
     *             contain the expected column names
     */
    private static void transfer(DatabaseResults.Row row, Domain domain) 
        throws DatabaseDataException {

        domain.setName(row.getString("NAME"));
        domain.setDescription(row.getString("DESCRIPTION"));
        domain.setOptions(row.getString("OPTIONS"));
        domain.setModified(false);
        domain.setPersistent(true);
    }
}
