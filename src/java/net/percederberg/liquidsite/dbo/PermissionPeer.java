/*
 * PermissionPeer.java
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

import org.liquidsite.util.db.DatabaseConnection;
import org.liquidsite.util.db.DatabaseQuery;

/**
 * A content permission database peer. This class contains static
 * methods that handles all accesses to the LS_PERMISSION table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PermissionPeer extends AbstractPeer {

    /**
     * The permission peer instance.
     */
    private static final PermissionPeer PEER = new PermissionPeer();

    /**
     * Returns a list of all permission objects with the specified
     * domain and content id. If the content id is zero (0), the
     * permissions for the domain root is returned.
     *
     * @param domain         the domain name
     * @param content        the content id, or zero (0)
     * @param con            the database connection to use
     *
     * @return the list of permission objects found
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByContent(String domain,
                                              int content,
                                              DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("permission.select.content");

        query.addParameter(domain);
        query.addParameter(content);
        return PEER.selectList(query, con);
    }

    /**
     * Inserts a new permission object into the database.
     *
     * @param data           the permission data object
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doInsert(PermissionData data, DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("permission.insert");

        query.addParameter(data.getString(PermissionData.DOMAIN));
        query.addParameter(data.getInt(PermissionData.CONTENT));
        query.addParameter(data.getString(PermissionData.USER));
        query.addParameter(data.getString(PermissionData.GROUP));
        query.addParameter(data.getBoolean(PermissionData.READ));
        query.addParameter(data.getBoolean(PermissionData.WRITE));
        query.addParameter(data.getBoolean(PermissionData.PUBLISH));
        query.addParameter(data.getBoolean(PermissionData.ADMIN));
        PEER.insert(query, con);
    }

    /**
     * Deletes all permissions for a content object from the database.
     *
     * @param domain         the domain name
     * @param id             the content object id
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doDelete(String domain,
                                int id,
                                DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("permission.delete");

        query.addParameter(domain);
        query.addParameter(id);
        PEER.delete(query, con);
    }

    /**
     * Deletes all permissions for a domain from the database.
     *
     * @param domain         the domain name
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doDeleteDomain(String domain, DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("permission.delete.domain");

        query.addParameter(domain);
        PEER.delete(query, con);
    }

    /**
     * Deletes all permissions for a user from the database.
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

        DatabaseQuery  query = new DatabaseQuery("permission.delete.user");

        query.addParameter(domain);
        query.addParameter(user);
        PEER.delete(query, con);
    }

    /**
     * Deletes all permissions for a group from the database.
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

        DatabaseQuery  query = new DatabaseQuery("permission.delete.group");

        query.addParameter(domain);
        query.addParameter(group);
        PEER.delete(query, con);
    }

    /**
     * Creates a new content permission database peer.
     */
    private PermissionPeer() {
        super("content permission");
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new PermissionData();
    }
}
