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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.dbo;

import java.util.ArrayList;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseQuery;
import net.percederberg.liquidsite.db.DatabaseResults;

import org.liquidsite.util.log.Log;

/**
 * A user group database peer. This class contains static methods
 * that handles all accesses to the LS_USER_GROUP table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class UserGroupPeer extends AbstractPeer {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(UserGroupPeer.class);

    /**
     * The user group peer instance.
     */
    private static final UserGroupPeer PEER = new UserGroupPeer();

    /**
     * Returns a list of all user groups for a certain group.
     *
     * @param domain         the domain name
     * @param group          the group name
     * @param con            the database connection to use
     *
     * @return a list of all user groups for the group
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static int doCountByGroup(String domain,
                                     String group,
                                     DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery    query = new DatabaseQuery("usergroup.count.group");
        DatabaseResults  res;

        query.addParameter(domain);
        query.addParameter(group);
        res = PEER.execute("counting users", query, con);
        try {
            return res.getRow(0).getInt(0);
        } catch (DatabaseDataException e) {
            LOG.error(e.getMessage());
            throw new DatabaseObjectException(e);
        }
    }

    /**
     * Returns a list of all user groups for a certain user.
     *
     * @param domain         the domain name
     * @param user           the user name
     * @param con            the database connection to use
     *
     * @return a list of all user groups for the user
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByUser(String domain,
                                           String user,
                                           DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.select.user");

        query.addParameter(domain);
        query.addParameter(user);
        return PEER.selectList(query, con);
    }

    /**
     * Returns a list of all user groups for a certain group. Only a
     * limited interval of the matching user groups will be returned.
     *
     * @param domain         the domain name
     * @param group          the group name
     * @param startPos       the list interval start position
     * @param maxLength      the list interval maximum length
     * @param con            the database connection to use
     *
     * @return a list of all user groups for the group
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByGroup(String domain,
                                            String group,
                                            int startPos,
                                            int maxLength,
                                            DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.select.group");

        query.addParameter(domain);
        query.addParameter(group);
        query.addParameter(startPos);
        query.addParameter(maxLength);
        return PEER.selectList(query, con);
    }

    /**
     * Inserts a new user group into the database.
     *
     * @param data           the user group data object
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doInsert(UserGroupData data, DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.insert");

        query.addParameter(data.getString(UserGroupData.DOMAIN));
        query.addParameter(data.getString(UserGroupData.USER));
        query.addParameter(data.getString(UserGroupData.GROUP));
        PEER.insert(query, con);
    }

    /**
     * Deletes a user group from the database.
     *
     * @param data           the user group data object
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doDelete(UserGroupData data, DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.delete");

        query.addParameter(data.getString(UserGroupData.DOMAIN));
        query.addParameter(data.getString(UserGroupData.USER));
        query.addParameter(data.getString(UserGroupData.GROUP));
        PEER.delete(query, con);
    }

    /**
     * Deletes all user groups in a domain from the database.
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

        DatabaseQuery  query = new DatabaseQuery("usergroup.delete.domain");

        query.addParameter(domain);
        PEER.delete(query, con);
    }

    /**
     * Deletes all user groups connected to a specified user from
     * the database.
     *
     * @param domain         the domain name
     * @param user           the user name
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doDeleteUser(String domain,
                                    String user,
                                    DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.delete.user");

        query.addParameter(domain);
        query.addParameter(user);
        PEER.delete(query, con);
    }

    /**
     * Deletes all user groups connected to a specified group from
     * the database.
     *
     * @param domain         the domain name
     * @param group          the group name
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doDeleteGroup(String domain,
                                     String group,
                                     DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.delete.group");

        query.addParameter(domain);
        query.addParameter(group);
        PEER.delete(query, con);
    }

    /**
     * Creates a new user group database peer.
     */
    private UserGroupPeer() {
        super("user group");
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new UserGroupData();
    }
}
