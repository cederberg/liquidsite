/*
 * UserGroupPeer.java
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
 * A user group database peer. This class contains static methods 
 * that handles all accesses to the database representation of the 
 * user and group connection.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class UserGroupPeer extends Peer {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(UserGroupPeer.class);

    /**
     * Returns a list of all groups that a certain user belongs to.
     * 
     * @param user           the user
     * 
     * @return a list of all groups for the user
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByUser(User user)
        throws ContentException {

        return doSelectByUser(user, null);
    }

    /**
     * Returns a list of all groups that a certain user belongs to.
     * 
     * @param user           the user
     * @param con            the database connection to use
     * 
     * @return a list of all groups for the user
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByUser(User user, 
                                           DatabaseConnection con)
        throws ContentException {

        ArrayList        params = new ArrayList();
        DatabaseResults  res;
        
        params.add(user.getDomainName());
        params.add(user.getName());
        res = execute("usergroup.select.user", 
                      params, 
                      "reading user groups", 
                      con);
        return createGroupList(res, con);
    }

    /**
     * Returns a list of all users belonging to a certain group.
     * 
     * @param group          the group
     * 
     * @return a list of all users in the group
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByGroup(Group group)
        throws ContentException {

        return doSelectByGroup(group, null);
    }

    /**
     * Returns a list of all users belonging to a certain group.
     * 
     * @param group          the group
     * @param con            the database connection to use
     * 
     * @return a list of all users in the group
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByGroup(Group group, 
                                            DatabaseConnection con)
        throws ContentException {

        ArrayList        params = new ArrayList();
        DatabaseResults  res;
        
        params.add(group.getDomainName());
        params.add(group.getName());
        res = execute("usergroup.select.group", 
                      params, 
                      "reading group users", 
                      con);
        return createUserList(res, con);
    }

    /**
     * Inserts a new user and group connection into the database.
     *
     * @param user           the user to insert 
     * @param group          the group to insert
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(User user, Group group) 
        throws ContentException {

        doInsert(user, group, null);
    }

    /**
     * Inserts a new user and group connection into the database.
     * 
     * @param user           the user to insert 
     * @param group          the group to insert
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(User user, 
                                Group group, 
                                DatabaseConnection con) 
        throws ContentException {

        ArrayList  params = new ArrayList();
        String     domain;
        String     message;

        user.validate();
        group.validate();
        domain = user.getDomainName();
        if (!domain.equals(group.getDomainName())) {
            message = "cannot add user in '" + domain + 
                      "' to group in '" + group.getDomainName() + "'"; 
            LOG.error(message);
            throw new ContentException(message);
        }
        params.add(domain);
        params.add(user.getName());
        params.add(group.getName());
        execute("usergroup.insert", params, "inserting user group", con);
    }
    
    /**
     * Deletes all connections to a specified user from the database.
     * 
     * @param user           the user to delete
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(User user) throws ContentException {
        doDelete(user, null);
    }

    /**
     * Deletes all connections to a specified user from the database.
     * 
     * @param user           the user to delete
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(User user, DatabaseConnection con) 
        throws ContentException {

        ArrayList  params = new ArrayList();

        params.add(user.getDomainName());
        params.add(user.getName());
        execute("usergroup.delete.user", params, "deleting user", con);
    }
    
    /**
     * Deletes all connections to a specified group from the database.
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
     * Deletes all connections to a specified group from the database.
     * 
     * @param group          the group to delete
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Group group, DatabaseConnection con) 
        throws ContentException {

        ArrayList  params = new ArrayList();

        params.add(group.getDomainName());
        params.add(group.getName());
        execute("usergroup.delete.group", params, "deleting group", con);
    }
    

    /**
     * Creates a list of user data objects from the list of names. 
     * The objects will be initialized by reading from the database.
     *
     * @param res            the database results
     * @param con            the database connection to use
     *  
     * @return a list of new data objects (may be empty)
     * 
     * @throws ContentException if the new instances couldn't be 
     *             created or if the database results were malformed
     */
    private static ArrayList createUserList(DatabaseResults res,
                                            DatabaseConnection con) 
        throws ContentException {

        ArrayList   list = new ArrayList();
        User        user;
        String      domain;
        String      name;
        
        for (int i = 0; i < res.getRowCount(); i++) {
            try {
                domain = res.getRow(i).getString("DOMAIN");
                name = res.getRow(i).getString("USER");
            } catch (DatabaseDataException e) {
                LOG.error("reading group user list", e);
                throw new ContentException("reading group user list", e);
            }
            user = UserPeer.doSelectByName(getDomain(domain), name);
            if (user == null) {
                throw new ContentException("couldn't find user '" + 
                                           name + "' in domain '" +
                                           domain + "'");
            }
            list.add(user);
        }
        return list;
    }

    /**
     * Creates a list of group data objects from the list of names. 
     * The objects will be initialized by reading from the database.
     *
     * @param res            the database results
     * @param con            the database connection to use
     *  
     * @return a list of new data objects (may be empty)
     * 
     * @throws ContentException if the new instances couldn't be 
     *             created or if the database results were malformed
     */
    private static ArrayList createGroupList(DatabaseResults res,
                                             DatabaseConnection con) 
        throws ContentException {

        ArrayList   list = new ArrayList();
        Group       group;
        String      domain;
        String      name;
        
        for (int i = 0; i < res.getRowCount(); i++) {
            try {
                domain = res.getRow(i).getString("DOMAIN");
                name = res.getRow(i).getString("GROUP");
            } catch (DatabaseDataException e) {
                LOG.error("reading user group list", e);
                throw new ContentException("reading user group list", e);
            }
            group = GroupPeer.doSelectByName(getDomain(domain), name);
            if (group == null) {
                throw new ContentException("couldn't find group '" + 
                                           name + "' in domain '" +
                                           domain + "'");
            }
            list.add(group);
        }
        return list;
    }

    /**
     * Returns the domain with the specified name.
     * 
     * @param name           the domain name
     * 
     * @return the domain object found
     * 
     * @throws ContentException if no content manager is available or
     *             if the domain couldn't be found
     */
    private static Domain getDomain(String name) throws ContentException {
        Domain  domain;

        domain = getContentManager().getDomain(name);
        if (domain == null) {
            throw new ContentException("couldn't find domain '" +
                                       name + "'");
        }
        return domain;
    }

    /**
     * Creates a new user group database peer.
     */
    private UserGroupPeer() {
        super("user group", Object.class);
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

        throw new DatabaseDataException("no user group data object exists");
    }

}
