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

package net.percederberg.liquidsite.dbo;

import java.util.ArrayList;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseQuery;

/**
 * A domain database peer. This class contains static methods that
 * handles all accesses to the LS_DOMAIN table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public final class DomainPeer extends AbstractPeer {

    /**
     * The domain peer instance.
     */
    private static final DomainPeer PEER = new DomainPeer();

    /**
     * Returns a list of all domains in the database.
     * 
     * @param con            the database connection to use
     * 
     * @return a list of all domains in the database
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static ArrayList doSelectAll(DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("domain.select.all");
        
        return PEER.selectList(query, con);
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
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static DomainData doSelectByName(String name, 
                                            DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("domain.select.name");
        
        query.addParameter(name);
        return (DomainData) PEER.select(query, con);
    }

    /**
     * Inserts a new domain into the database.
     * 
     * @param data           the domain data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doInsert(DomainData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("domain.insert");

        query.addParameter(data.getString(DomainData.NAME));
        query.addParameter(data.getString(DomainData.DESCRIPTION));
        query.addParameter(data.getString(DomainData.OPTIONS));
        PEER.insert(query, con);
    }
    
    /**
     * Updates a domain in the database.
     * 
     * @param data           the domain data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doUpdate(DomainData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("domain.update");

        query.addParameter(data.getString(DomainData.DESCRIPTION));
        query.addParameter(data.getString(DomainData.OPTIONS));
        query.addParameter(data.getString(DomainData.NAME));
        PEER.update(query, con);
    }
    
    /**
     * Deletes a domain from the database.
     * 
     * @param data           the domain data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doDelete(DomainData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("domain.delete");
        String         domain = data.getString(DomainData.NAME);

        query.addParameter(domain);
        PEER.delete(query, con);
        UserPeer.doDeleteDomain(domain, con);
        GroupPeer.doDeleteDomain(domain, con);
        HostPeer.doDeleteDomain(domain, con);
        ContentPeer.doDeleteDomain(domain, con);
    }
    
    /**
     * Creates a new domain database peer.
     */
    private DomainPeer() {
        super("domain");
    }

    /**
     * Returns a new instance of the data object.
     * 
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new DomainData();
    }
}
