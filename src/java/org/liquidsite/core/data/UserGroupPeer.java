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

package org.liquidsite.core.data;

import java.util.ArrayList;

import org.liquidsite.util.db.DatabaseQuery;

/**
 * A user group database peer. This class contains static methods
 * that handles all accesses to the LS_USER_GROUP table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class UserGroupPeer extends AbstractPeer {

    /**
     * The user group peer instance.
     */
    private static final UserGroupPeer PEER = new UserGroupPeer();

    /**
     * Returns a list of all user groups for a certain group.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param group          the group name
     *
     * @return a list of all user groups for the group
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static int doCountByGroup(DataSource src,
                                     String domain,
                                     String group)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.count.group");

        query.addParameter(domain);
        query.addParameter(group);
        return (int) PEER.count(src, query);
    }

    /**
     * Returns a list of all user groups for a certain user.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param user           the user name
     *
     * @return a list of all user groups for the user
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByUser(DataSource src,
                                           String domain,
                                           String user)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.select.user");

        query.addParameter(domain);
        query.addParameter(user);
        return PEER.selectList(src, query);
    }

    /**
     * Returns a list of all user groups for a certain group. Only a
     * limited interval of the matching user groups will be returned.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param group          the group name
     * @param startPos       the list interval start position
     * @param maxLength      the list interval maximum length
     *
     * @return a list of all user groups for the group
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByGroup(DataSource src,
                                            String domain,
                                            String group,
                                            int startPos,
                                            int maxLength)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.select.group");

        query.addParameter(domain);
        query.addParameter(group);
        query.addParameter(startPos);
        query.addParameter(maxLength);
        return PEER.selectList(src, query);
    }

    /**
     * Inserts a new user group into the data source.
     *
     * @param src            the data source to use
     * @param data           the user group data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doInsert(DataSource src, UserGroupData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.insert");

        query.addParameter(data.getString(UserGroupData.DOMAIN));
        query.addParameter(data.getString(UserGroupData.USER));
        query.addParameter(data.getString(UserGroupData.GROUP));
        PEER.insert(src, query);
    }

    /**
     * Deletes a user group from the data source.
     *
     * @param src            the data source to use
     * @param data           the user group data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDelete(DataSource src, UserGroupData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.delete");

        query.addParameter(data.getString(UserGroupData.DOMAIN));
        query.addParameter(data.getString(UserGroupData.USER));
        query.addParameter(data.getString(UserGroupData.GROUP));
        PEER.delete(src, query);
    }

    /**
     * Deletes all user groups in a domain from the data source.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteDomain(DataSource src, String domain)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.delete.domain");

        query.addParameter(domain);
        PEER.delete(src, query);
    }

    /**
     * Deletes all user groups connected to a specified user from
     * the data source.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param user           the user name
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteUser(DataSource src,
                                    String domain,
                                    String user)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.delete.user");

        query.addParameter(domain);
        query.addParameter(user);
        PEER.delete(src, query);
    }

    /**
     * Deletes all user groups connected to a specified group from
     * the data source.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param group          the group name
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteGroup(DataSource src,
                                     String domain,
                                     String group)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("usergroup.delete.group");

        query.addParameter(domain);
        query.addParameter(group);
        PEER.delete(src, query);
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
