/*
 * User.java
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
import net.percederberg.liquidsite.dbo.DatabaseObjectException;
import net.percederberg.liquidsite.dbo.UserData;
import net.percederberg.liquidsite.dbo.UserPeer;

/**
 * A system user.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class User extends PersistentObject {

    /**
     * The user data.
     */
    private UserData data;

    /**
     * The list of user group data objects. This variable is null
     * until the list of user groups has been requested the first 
     * time.
     */
    private ArrayList groups = null;

    /**
     * Returns an array of all users in a certain domain.
     * 
     * @param domain         the domain
     * 
     * @return an array of all users in the domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static User[] findByDomain(Domain domain) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection();
        ArrayList           list;
        User[]              res;

        try {
            list = UserPeer.doSelectByDomain(domain.getName(), con);
            res = new User[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = new User((UserData) list.get(i));
            }
        } catch (DatabaseObjectException e) {
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(con);
        }
        return res;
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
    public static User findByName(Domain domain, String name)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection();
        UserData            data;

        try {
            data = UserPeer.doSelectByName(domain.getName(), name, con);
        } catch (DatabaseObjectException e) {
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(con);
        }
        if (data == null) {
            return null;
        } else {
            return new User(data);
        }
    }

    /**
     * Creates a new user with default values.
     * 
     * @param domain         the domain
     * @param name           the user name
     */
    public User(Domain domain, String name) {
        super(false);
        this.data = new UserData();
        this.data.setString(UserData.DOMAIN, domain.getName());
        this.data.setString(UserData.NAME, name);
    }

    /**
     * Creates a new user from a data object.
     * 
     * @param data           the user data object
     */
    private User(UserData data) {
        super(true);
        this.data = data;
    }

    /**
     * Checks if this user equals another object. This method will 
     * only return true if the other object is a user with the same
     * domain and user name.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical user, or
     *         false otherwise 
     */
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            return equals((User) obj);
        } else {
            return false;
        }
    }

    /**
     * Checks if this user equals another user. This method will 
     * only return true if the other object is a user with the same
     * domain and user name.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical user, or
     *         false otherwise 
     */
    public boolean equals(User obj) {
        return getDomainName().equals(obj.getDomainName())
            && getName().equals(obj.getName());
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        return getName();
    }

    /**
     * Returns the user domain.
     * 
     * @return the user domain
     * 
     * @throws ContentException if no content manager is available
     */
    public Domain getDomain() throws ContentException {
        return getContentManager().getDomain(getDomainName());
    }
    
    /**
     * Returns the user domain name
     * 
     * @return the user domain name
     */
    public String getDomainName() {
        return data.getString(UserData.DOMAIN);
    }
    
    /**
     * Returns the user name.
     * 
     * @return the user name
     */
    public String getName() {
        return data.getString(UserData.NAME);
    }
    
    /**
     * Returns the encoded user password.
     * 
     * @return the encoded user password
     */
    public String getPassword() {
        return data.getString(UserData.PASSWORD);
    }
    
    /**
     * Sets the user password. This method will also encode the user
     * password.
     * 
     * @param password       the new user password
     */
    public void setPassword(String password) {
        // TODO: encode the password
        data.setString(UserData.PASSWORD, password);
    }
    
    /**
     * Returns the real user name.
     * 
     * @return the real user name
     */
    public String getRealName() {
        return data.getString(UserData.REAL_NAME);
    }
    
    /**
     * Sets the real user name.
     * 
     * @param realName       the new real user name
     */
    public void setRealName(String realName) {
        data.setString(UserData.REAL_NAME, realName);
    }
    
    /**
     * Returns the user e-mail address.
     * 
     * @return the user e-mail address
     */
    public String getEmail() {
        return data.getString(UserData.EMAIL);
    }
    
    /**
     * Sets the user e-mail address.
     * 
     * @param email          the new user e-mail address
     */
    public void setEmail(String email) {
        data.setString(UserData.EMAIL, email);
    }
    
    /**
     * Returns the user comment.
     * 
     * @return the user comment
     */
    public String getComment() {
        return data.getString(UserData.COMMENT);
    }
    
    /**
     * Sets the user comment.
     * 
     * @param comment        the new user comment
     */
    public void setComment(String comment) {
        data.setString(UserData.COMMENT, comment);
    }
    
    /**
     * Validates this data object. This method checks that all 
     * required fields have been filled with suitable values.
     * 
     * @throws ContentException if the data object contained errors
     */
    public void validate() throws ContentException {
        if (getDomainName().equals("")) {
            throw new ContentException("no domain set for user object");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + getDomainName() + 
                                       "' does not exist");
        } else if (getName().equals("")) {
            throw new ContentException("no name set for user object");
        } else if (getPassword().equals("")) {
            throw new ContentException("no password set for user object");
        }
    }

    /**
     * Inserts the object data into the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    protected void doInsert(DatabaseConnection con)
        throws DatabaseObjectException {

        UserPeer.doInsert(data, con);
    }

    /**
     * Updates the object data in the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    protected void doUpdate(DatabaseConnection con)
        throws DatabaseObjectException {

        UserPeer.doUpdate(data, con);
    }

    /**
     * Deletes the object data from the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    protected void doDelete(DatabaseConnection con)
        throws DatabaseObjectException {

        UserPeer.doDelete(data, con);
    }
}
