/*
 * GroupPeer.java
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
 * A group database peer. This class contains static methods that
 * handles all accesses to the LS_GROUP table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class GroupPeer extends AbstractPeer {

    /**
     * The group peer instance.
     */
    private static final GroupPeer PEER = new GroupPeer();

    /**
     * Returns a list of matching groups in a specified domain. Only
     * groups with matching names will be returned.
     *
     * @param domain         the domain name
     * @param filter         the search filter (empty for all)
     * @param con            the database connection to use
     *
     * @return a list of matching groups in the domain
     *
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static ArrayList doSelectByDomain(String domain,
                                             String filter,
                                             DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("group.select.domain");
        String         filterSql = "%" + filter + "%";

        query.addParameter(domain);
        query.addParameter(filterSql);
        query.addParameter(filterSql);
        return PEER.selectList(query, con);
    }

    /**
     * Returns a group with a specified name.
     * 
     * @param domain         the domain name
     * @param name           the group name
     * @param con            the database connection to use
     * 
     * @return the group found, or
     *         null if no matching group existed
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static GroupData doSelectByName(String domain, 
                                           String name, 
                                           DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("group.select.name");
        
        query.addParameter(domain);
        query.addParameter(name);
        return (GroupData) PEER.select(query, con);
    }

    /**
     * Inserts a new group into the database.
     * 
     * @param data           the group data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doInsert(GroupData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("group.insert");

        query.addParameter(data.getString(GroupData.DOMAIN));
        query.addParameter(data.getString(GroupData.NAME));
        query.addParameter(data.getString(GroupData.DESCRIPTION));
        query.addParameter(data.getString(GroupData.COMMENT));
        PEER.insert(query, con);
    }
    
    /**
     * Updates a group in the database.
     * 
     * @param data           the group data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doUpdate(GroupData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("group.update");

        query.addParameter(data.getString(GroupData.DESCRIPTION));
        query.addParameter(data.getString(GroupData.COMMENT));
        query.addParameter(data.getString(GroupData.DOMAIN));
        query.addParameter(data.getString(GroupData.NAME));
        PEER.update(query, con);
    }
    
    /**
     * Deletes a group from the database. This method also deletes 
     * all related user group and permission entries.
     * 
     * @param data           the group data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(GroupData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("group.delete");
        String         domain = data.getString(GroupData.DOMAIN);
        String         name = data.getString(GroupData.NAME);

        query.addParameter(domain);
        query.addParameter(name);
        PEER.delete(query, con);
        UserGroupPeer.doDeleteGroup(domain, name, con);
        PermissionPeer.doDeleteGroup(domain, name, con);
    }

    /**
     * Deletes all groups in a domain from the database. This method 
     * also deletes all user group entries in the domain.
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

        DatabaseQuery  query = new DatabaseQuery("group.delete.domain");

        query.addParameter(domain);
        PEER.delete(query, con);
        UserGroupPeer.doDeleteDomain(domain, con);
    }
    
    /**
     * Creates a new group database peer.
     */
    private GroupPeer() {
        super("group");
    }

    /**
     * Returns a new instance of the data object.
     * 
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new GroupData();
    }
}
