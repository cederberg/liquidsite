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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.dbo;

import java.util.ArrayList;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseQuery;

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
     * Returns a list of all users in a certain domain.
     * 
     * @param domain         the domain name
     * @param con            the database connection to use
     * 
     * @return a list of all users in the domain
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static ArrayList doSelectByDomain(String domain,
                                             DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("user.select.domain");
        
        query.addParameter(domain);
        return PEER.selectList(query, con);
    }

    /**
     * Returns a user with a specified name.
     * 
     * @param domain         the domain name
     * @param name           the user name
     * @param con            the database connection to use
     * 
     * @return the user found, or
     *         null if no matching user existed
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static UserData doSelectByName(String domain, 
                                          String name, 
                                          DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("user.select.name");
        
        query.addParameter(domain);
        query.addParameter(name);
        return (UserData) PEER.select(query, con);
    }

    /**
     * Inserts a new user into the database.
     * 
     * @param data           the user data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doInsert(UserData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("user.insert");

        query.addParameter(data.getString(UserData.DOMAIN));
        query.addParameter(data.getString(UserData.NAME));
        query.addParameter(data.getString(UserData.PASSWORD));
        query.addParameter(data.getString(UserData.REAL_NAME));
        query.addParameter(data.getString(UserData.EMAIL));
        query.addParameter(data.getString(UserData.COMMENT));
        PEER.insert(query, con);
    }

    /**
     * Updates a user in the database.
     * 
     * @param data           the user data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doUpdate(UserData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("user.update");

        query.addParameter(data.getString(UserData.PASSWORD));
        query.addParameter(data.getString(UserData.REAL_NAME));
        query.addParameter(data.getString(UserData.EMAIL));
        query.addParameter(data.getString(UserData.COMMENT));
        query.addParameter(data.getString(UserData.DOMAIN));
        query.addParameter(data.getString(UserData.NAME));
        PEER.update(query, con);
    }
    
    /**
     * Deletes a user from the database. This method also deletes 
     * all related user group and permission entries.
     * 
     * @param data           the user data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doDelete(UserData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("user.delete");
        String         domain = data.getString(UserData.DOMAIN);
        String         name = data.getString(UserData.NAME);

        query.addParameter(domain);
        query.addParameter(name);
        PEER.delete(query, con);
        UserGroupPeer.doDeleteUser(domain, name, con);
        PermissionPeer.doDeleteUser(domain, name, con);
    }
    
    /**
     * Deletes all users in a domain from the database. This method 
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

        DatabaseQuery  query = new DatabaseQuery("user.delete.domain");

        query.addParameter(domain);
        PEER.delete(query, con);
        UserGroupPeer.doDeleteDomain(domain, con);
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
