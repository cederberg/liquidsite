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

package net.percederberg.liquidsite.content;

import java.util.ArrayList;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseQuery;
import net.percederberg.liquidsite.db.DatabaseResults;

/**
 * A user database peer. This class contains static methods that
 * handles all accesses to the database representation of a user.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class UserPeer extends Peer {

    /**
     * The user peer instance.
     */
    private static final Peer PEER = new UserPeer();

    /**
     * Returns a list of all users in a certain domain.
     * 
     * @param domain         the domain
     * 
     * @return a list of all users in the domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByDomain(Domain domain)
        throws ContentException {

        return doSelectByDomain(domain, null);
    }

    /**
     * Returns a list of all users in a certain domain.
     * 
     * @param domain         the domain
     * @param con            the database connection to use
     * 
     * @return a list of all users in the domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByDomain(Domain domain,
                                             DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("user.select.domain");
        DatabaseResults  res;
        
        query.addParameter(domain.getName());
        res = execute("reading users", query, con);
        return PEER.createObjectList(res);
    }

    /**
     * Returns a user with a specified name.
     * 
     * @param domain         the domain
     * @param name           the user name
     * 
     * @return the user found, or
     *         null if no matching user existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static User doSelectByName(Domain domain, String name)
        throws ContentException {

        return doSelectByName(domain, name, null);
    }

    /**
     * Returns a user with a specified name.
     * 
     * @param domain         the domain
     * @param name           the user name
     * @param con            the database connection to use
     * 
     * @return the user found, or
     *         null if no matching user existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static User doSelectByName(Domain domain, 
                                      String name, 
                                      DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("user.select.name");
        DatabaseResults  res;
        
        query.addParameter(domain.getName());
        query.addParameter(name);
        res = execute("reading user", query, con);
        return (User) PEER.createObject(res);
    }

    /**
     * Inserts a new user into the database.
     * 
     * @param user           the user to insert
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(User user) throws ContentException {
        doInsert(user, null);
    }

    /**
     * Inserts a new user into the database.
     * 
     * @param user           the user to insert
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(User user, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("user.insert");

        user.validate();
        query.addParameter(user.getDomainName());
        query.addParameter(user.getName());
        query.addParameter(user.getPassword());
        query.addParameter(user.getRealName());
        query.addParameter(user.getEmail());
        query.addParameter(user.getComment());
        execute("inserting user", query, con);
        user.setModified(false);
        user.setPersistent(true);
    }
    
    /**
     * Updates a user in the database.
     * 
     * @param user           the user to update
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(User user) throws ContentException {
        doUpdate(user, null);
    }

    /**
     * Updates a user in the database.
     * 
     * @param user           the user to update
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(User user, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("user.update");

        user.validate();
        query.addParameter(user.getPassword());
        query.addParameter(user.getRealName());
        query.addParameter(user.getEmail());
        query.addParameter(user.getComment());
        query.addParameter(user.getDomainName());
        query.addParameter(user.getName());
        execute("updating user", query, con);
        user.setModified(false);
        user.setPersistent(true);
    }
    
    /**
     * Deletes a user from the database. This method also deletes all
     * group connections.
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
     * Deletes a user from the database. This method also deletes all
     * group connections.
     * 
     * @param user           the user to delete
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(User user, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("user.delete");

        query.addParameter(user.getDomainName());
        query.addParameter(user.getName());
        execute("deleting user", query, con);
        UserGroupPeer.doDelete(user, con);
        user.setModified(true);
        user.setPersistent(false);
    }
    
    /**
     * Creates a new user database peer.
     */
    private UserPeer() {
        super("user", User.class);
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

        User  user = (User) obj;

        user.setDomainName(row.getString("DOMAIN"));
        user.setName(row.getString("NAME"));
        user.setPassword(row.getString("PASSWORD"));
        user.setRealName(row.getString("REAL_NAME"));
        user.setEmail(row.getString("EMAIL"));
        user.setComment(row.getString("COMMENT"));
        user.setModified(false);
        user.setPersistent(true);
    }
}
