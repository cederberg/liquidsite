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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.content;

import java.util.ArrayList;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseQuery;
import net.percederberg.liquidsite.db.DatabaseResults;

/**
 * A content permission database peer. This class contains static 
 * methods that handles all accesses to the database representation 
 * of a content permission.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PermissionPeer extends Peer {

    /**
     * The permission peer instance.
     */
    private static final Peer PEER = new PermissionPeer();

    /**
     * Returns a list of all permission objects with the specified 
     * content id.
     * 
     * @param id             the content id
     * 
     * @return the list of permission objects found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectById(int id) throws ContentException {
        return doSelectById(id, null);
    }

    /**
     * Returns a list of all permission objects with the specified 
     * content id.
     * 
     * @param id             the content id
     * @param con            the database connection to use
     * 
     * @return the list of permission objects found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectById(int id, DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("permission.select.id");
        DatabaseResults  res;
        
        query.addParameter(id);
        res = execute("reading permission list", query, con);
        return PEER.createObjectList(res);
    }

    /**
     * Returns the permission object with the specified content id,
     * user, and group.
     * 
     * @param id             the content id
     * @param user           the permission user name
     * @param group          the permission group name
     * 
     * @return the permission found, or
     *         null if no matching permission existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Permission doSelectByUser(int id,
                                            String user,
                                            String group)
        throws ContentException {

        return doSelectByUser(id, user, group, null);
    }

    /**
     * Returns the permission object with the specified content id,
     * user, and group.
     * 
     * @param id             the content id
     * @param user           the permission user name
     * @param group          the permission group name
     * @param con            the database connection to use
     * 
     * @return the permission found, or
     *         null if no matching permission existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Permission doSelectByUser(int id,
                                            String user,
                                            String group, 
                                            DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("permission.select.user");
        DatabaseResults  res;
        
        query.addParameter(id);
        query.addParameter(user);
        query.addParameter(group);
        res = execute("reading permission", query, con);
        return (Permission) PEER.createObject(res);
    }

    /**
     * Inserts a new permission object into the database.
     * 
     * @param permission     the permission to insert
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(Permission permission) 
        throws ContentException {

        doInsert(permission, null);
    }

    /**
     * Inserts a new permission object into the database.
     * 
     * @param permission     the permission to insert
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(Permission permission, 
                                DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("permission.insert");

        permission.validate();
        query.addParameter(permission.getDomainName());
        query.addParameter(permission.getContentId());
        query.addParameter(permission.getUserName());
        query.addParameter(permission.getGroupName());
        query.addParameter(permission.getRead());
        query.addParameter(permission.getWrite());
        query.addParameter(permission.getPublish());
        query.addParameter(permission.getAdmin());
        execute("inserting permission", query, con);
        permission.setModified(false);
        permission.setPersistent(true);
    }
    
    /**
     * Updates a permission in the database.
     * 
     * @param permission     the permission to update
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(Permission permission) 
        throws ContentException {

        doUpdate(permission, null);
    }

    /**
     * Updates a permission in the database.
     * 
     * @param permission     the permission to update
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(Permission permission, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("permission.update");

        permission.validate();
        query.addParameter(permission.getRead());
        query.addParameter(permission.getWrite());
        query.addParameter(permission.getPublish());
        query.addParameter(permission.getAdmin());
        query.addParameter(permission.getContentId());
        query.addParameter(permission.getUserName());
        query.addParameter(permission.getGroupName());
        execute("updating permission", query, con);
        permission.setModified(false);
        permission.setPersistent(true);
    }

    /**
     * Deletes a permission object from the database.
     * 
     * @param permission     the permission to delete
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Permission permission)
        throws ContentException {

        doDelete(permission, null);
    }

    /**
     * Deletes a permission object from the database.
     * 
     * @param permission     the permission to delete
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Permission permission, DatabaseConnection con)
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("permission.delete");

        query.addParameter(permission.getContentId());
        query.addParameter(permission.getUserName());
        query.addParameter(permission.getGroupName());
        execute("deleting permission", query, con);
        permission.setModified(true);
        permission.setPersistent(false);
    }
    
    /**
     * Deletes all permissions for a content object from the database.
     * 
     * @param id             the content object id
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDeleteContent(int id) throws ContentException {
        doDeleteContent(id, null);
    }

    /**
     * Deletes all permissions for a content object from the database.
     * 
     * @param id             the content object id
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDeleteContent(int id, DatabaseConnection con)
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("permission.delete.content");

        query.addParameter(id);
        execute("deleting permissions", query, con);
    }
    
    /**
     * Creates a new permission database peer.
     */
    private PermissionPeer() {
        super("permission", Permission.class);
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

        Permission  permission = (Permission) obj;

        permission.setDomainName(row.getString("DOMAIN"));
        permission.setContentId(row.getInt("CONTENT"));
        permission.setUserName(row.getString("USER"));
        permission.setGroupName(row.getString("GROUP"));
        permission.setRead(row.getBoolean("READ"));
        permission.setWrite(row.getBoolean("WRITE"));
        permission.setPublish(row.getBoolean("PUBLISH"));
        permission.setAdmin(row.getBoolean("ADMIN"));
        permission.setModified(false);
        permission.setPersistent(true);
    }
}
