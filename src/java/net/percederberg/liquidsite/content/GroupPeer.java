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

package net.percederberg.liquidsite.content;

import java.util.ArrayList;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseQuery;
import net.percederberg.liquidsite.db.DatabaseResults;

/**
 * A group database peer. This class contains static methods that
 * handles all accesses to the database representation of a group.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class GroupPeer extends Peer {

    /**
     * The group peer instance.
     */
    private static final Peer PEER = new GroupPeer();

    /**
     * Returns a list of all groups in a certain domain.
     * 
     * @param domain         the domain
     * 
     * @return a list of all groups in the domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByDomain(Domain domain)
        throws ContentException {

        return doSelectByDomain(domain, null);
    }

    /**
     * Returns a list of all groups in a certain domain.
     * 
     * @param domain         the domain
     * @param con            the database connection to use
     * 
     * @return a list of all groups in the domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByDomain(Domain domain,
                                             DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("group.select.domain");
        DatabaseResults  res;
        
        query.addParameter(domain.getName());
        res = execute("reading groups", query, con);
        return PEER.createObjectList(res);
    }

    /**
     * Returns a group with a specified name.
     * 
     * @param domain         the domain
     * @param name           the group name
     * 
     * @return the group found, or
     *         null if no matching group existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Group doSelectByName(Domain domain, String name)
        throws ContentException {

        return doSelectByName(domain, name, null);
    }

    /**
     * Returns a group with a specified name.
     * 
     * @param domain         the domain
     * @param name           the group name
     * @param con            the database connection to use
     * 
     * @return the group found, or
     *         null if no matching group existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Group doSelectByName(Domain domain, 
                                       String name, 
                                       DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("group.select.name");
        DatabaseResults  res;
        
        query.addParameter(domain.getName());
        query.addParameter(name);
        res = execute("reading group", query, con);
        return (Group) PEER.createObject(res);
    }

    /**
     * Inserts a new group into the database.
     * 
     * @param group          the group to insert
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(Group group) throws ContentException {
        doInsert(group, null);
    }

    /**
     * Inserts a new group into the database.
     * 
     * @param group          the group to insert
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(Group group, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("group.insert");

        group.validate();
        query.addParameter(group.getDomainName());
        query.addParameter(group.getName());
        query.addParameter(group.getDescription());
        query.addParameter(group.getComment());
        execute("inserting group", query, con);
        group.setModified(false);
        group.setPersistent(true);
    }
    
    /**
     * Updates a group in the database.
     * 
     * @param group          the group to update
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(Group group) throws ContentException {
        doUpdate(group, null);
    }

    /**
     * Updates a group in the database.
     * 
     * @param group          the group to update
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(Group group, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("group.update");

        group.validate();
        query.addParameter(group.getDescription());
        query.addParameter(group.getComment());
        query.addParameter(group.getDomainName());
        query.addParameter(group.getName());
        execute("updating group", query, con);
        group.setModified(false);
        group.setPersistent(true);
    }
    
    /**
     * Deletes a group from the database. This method also deletes 
     * all user connections.
     * 
     * @param group          the group to delete
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Group group) throws ContentException {
        doDelete(group, null);
    }

    /**
     * Deletes a group from the database. This method also deletes 
     * all user connections.
     * 
     * @param group          the group to delete
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Group group, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("group.delete");

        query.addParameter(group.getDomainName());
        query.addParameter(group.getName());
        execute("deleting group", query, con);
        UserGroupPeer.doDelete(group, con);
        group.setModified(true);
        group.setPersistent(false);
    }
    
    /**
     * Creates a new group database peer.
     */
    private GroupPeer() {
        super("group", Group.class);
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

        Group  group = (Group) obj;

        group.setDomainName(row.getString("DOMAIN"));
        group.setName(row.getString("NAME"));
        group.setDescription(row.getString("DESCRIPTION"));
        group.setComment(row.getString("COMMENT"));
        group.setModified(false);
        group.setPersistent(true);
    }
}
