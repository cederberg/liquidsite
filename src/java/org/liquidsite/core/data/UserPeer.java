/*
 * UserPeer.java
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
 * A user database peer. This class contains static methods that
 * handles all accesses to the LS_USER table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class UserPeer extends AbstractPeer {

    /**
     * The user peer instance.
     */
    private static final UserPeer PEER = new UserPeer();

    /**
     * Returns the number of users in a specified domain. Only users
     * with matching names will be counted.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param filter         the search filter (empty for all)
     *
     * @return the number of matching users in the domain
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static int doCountByDomain(DataSource src,
                                      String domain,
                                      String filter)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("user.count");
        String         filterSql = "%" + filter + "%";

        query.addParameter(domain);
        query.addParameter(filterSql);
        query.addParameter(filterSql);
        query.addParameter(filterSql);
        return (int) PEER.count(src, query);
    }

    /**
     * Returns a list of matching users in a specified domain. Only
     * users with matching names will be returned. Also, only a
     * limited interval of the matching users will be returned.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param filter         the search filter (empty for all)
     * @param startPos       the list interval start position
     * @param maxLength      the list interval maximum length
     *
     * @return a list of matching users in the domain
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByDomain(DataSource src,
                                             String domain,
                                             String filter,
                                             int startPos,
                                             int maxLength)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("user.select.domain");
        String         filterSql = "%" + filter + "%";

        query.addParameter(domain);
        query.addParameter(filterSql);
        query.addParameter(filterSql);
        query.addParameter(filterSql);
        query.addParameter(startPos);
        query.addParameter(maxLength);
        return PEER.selectList(src, query);
    }

    /**
     * Returns a user with a specified name.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param name           the user name
     *
     * @return the user found, or
     *         null if no matching user existed
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static UserData doSelectByName(DataSource src,
                                          String domain,
                                          String name)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("user.select.name");

        query.addParameter(domain);
        query.addParameter(name);
        return (UserData) PEER.select(src, query);
    }

    /**
     * Returns a user with a specified email address.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param name           the user email address
     *
     * @return the user found, or
     *         null if no matching user existed
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static UserData doSelectByEmail(DataSource src,
                                           String domain,
                                           String email)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("user.select.email");

        query.addParameter(domain);
        query.addParameter(email);
        return (UserData) PEER.select(src, query);
    }

    /**
     * Inserts a new user into the data source.
     *
     * @param src            the data source to use
     * @param data           the user data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doInsert(DataSource src, UserData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("user.insert");

        query.addParameter(data.getString(UserData.DOMAIN));
        query.addParameter(data.getString(UserData.NAME));
        query.addParameter(data.getString(UserData.PASSWORD));
        query.addParameter(data.getBoolean(UserData.ENABLED));
        query.addParameter(data.getString(UserData.REAL_NAME));
        query.addParameter(data.getString(UserData.EMAIL));
        query.addParameter(data.getString(UserData.COMMENT));
        PEER.insert(src, query);
    }

    /**
     * Updates a user in the data source.
     *
     * @param src            the data source to use
     * @param data           the user data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doUpdate(DataSource src, UserData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("user.update");

        query.addParameter(data.getString(UserData.PASSWORD));
        query.addParameter(data.getBoolean(UserData.ENABLED));
        query.addParameter(data.getString(UserData.REAL_NAME));
        query.addParameter(data.getString(UserData.EMAIL));
        query.addParameter(data.getString(UserData.COMMENT));
        query.addParameter(data.getString(UserData.DOMAIN));
        query.addParameter(data.getString(UserData.NAME));
        PEER.update(src, query);
    }

    /**
     * Deletes a user from the data source. This method also deletes
     * all related user group and permission entries.
     *
     * @param src            the data source to use
     * @param data           the user data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDelete(DataSource src, UserData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("user.delete");
        String         domain = data.getString(UserData.DOMAIN);
        String         name = data.getString(UserData.NAME);

        query.addParameter(domain);
        query.addParameter(name);
        PEER.delete(src, query);
        PreferencePeer.doDeleteUser(src, domain, name);
        UserGroupPeer.doDeleteUser(src, domain, name);
        PermissionPeer.doDeleteUser(src, domain, name);
    }

    /**
     * Deletes all users in a domain from the data source. This
     * method also deletes all user group entries in the domain.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteDomain(DataSource src, String domain)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("user.delete.domain");

        query.addParameter(domain);
        PEER.delete(src, query);
        PreferencePeer.doDeleteDomain(src, domain);
        UserGroupPeer.doDeleteDomain(src, domain);
    }

    /**
     * Creates a new user database peer.
     */
    private UserPeer() {
        super("user");
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new UserData();
    }
}
