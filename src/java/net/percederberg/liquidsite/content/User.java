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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.DatabaseObjectException;
import net.percederberg.liquidsite.dbo.UserData;
import net.percederberg.liquidsite.dbo.UserGroupData;
import net.percederberg.liquidsite.dbo.UserGroupPeer;
import net.percederberg.liquidsite.dbo.UserPeer;

/**
 * A system user.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class User extends PersistentObject {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(User.class);

    /**
     * The user data.
     */
    private UserData data;

    /**
     * The user groups found in the database.
     */
    private Group[] groups = null;

    /**
     * The list of group names added since the object was saved.
     */
    private ArrayList groupsAdded = null;

    /**
     * The list of group names removed since the object was saved.
     */
    private ArrayList groupsRemoved = null;

    /**
     * Returns an array of all users in a certain domain.
     * 
     * @param manager        the content manager to use
     * @param domain         the domain, or null for superusers
     * 
     * @return an array of all users in the domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    static User[] findByDomain(ContentManager manager, Domain domain) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ArrayList           list;
        User[]              res;
        String              domainName = "";

        try {
            if (domain != null) {
                domainName = domain.getName();
            }
            list = UserPeer.doSelectByDomain(domainName, con);
            res = new User[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = new User(manager, (UserData) list.get(i));
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
        return res;
    }

    /**
     * Returns a user with a specified name.
     * 
     * @param manager        the content manager to use
     * @param domain         the domain, or null for superusers
     * @param name           the user name
     * 
     * @return the user found, or
     *         null if no matching user existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    static User findByName(ContentManager manager,
                           Domain domain,
                           String name)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        UserData            data;
        String              domainName = "";

        try {
            if (domain != null) {
                domainName = domain.getName();
            }
            data = UserPeer.doSelectByName(domainName, name, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
        if (data == null) {
            return null;
        } else {
            return new User(manager, data);
        }
    }

    /**
     * Returns an array of all users in a certain group.
     * 
     * @param manager        the content manager to use
     * @param group          the group
     * 
     * @return an array of all users in the group
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    static User[] findByGroup(ContentManager manager, Group group) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ArrayList           list;
        User[]              res;
        UserGroupData       data;
        UserData            user;
        String              name;

        try {
            list = UserGroupPeer.doSelectByGroup(group.getDomainName(),
                                                 group.getName(), 
                                                 con);
            res = new User[list.size()];
            for (int i = 0; i < list.size(); i++) {
                data = (UserGroupData) list.get(i);
                name = data.getString(UserGroupData.USER);
                user = UserPeer.doSelectByName(group.getDomainName(),
                                               name, 
                                               con);
                res[i] = new User(manager, user);
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
        return res;
    }

    /**
     * Creates a new user with default values.
     * 
     * @param manager        the content manager to use 
     * @param domain         the domain, or null for a superuser
     * @param name           the user name
     */
    public User(ContentManager manager, Domain domain, String name) {
        super(manager, false);
        this.data = new UserData();
        if (domain == null) {
            this.data.setString(UserData.DOMAIN, "");
        } else {
            this.data.setString(UserData.DOMAIN, domain.getName());
        }
        this.data.setString(UserData.NAME, name);
    }

    /**
     * Creates a new user from a data object.
     * 
     * @param manager        the content manager to use 
     * @param data           the user data object
     */
    private User(ContentManager manager, UserData data) {
        super(manager, true);
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
     * Checks if this user is a super user. A super user should have
     * access to all objects in the system, and does not belong to 
     * any domain.
     * 
     * @return true if the user represents a super user, or
     *         false otherwise
     */
    public boolean isSuperUser() {
        return getDomainName().equals("");
    }

    /**
     * Returns the user domain.
     * 
     * @return the user domain, or null if the user is a superuser
     * 
     * @throws ContentException if no content manager is available
     */
    public Domain getDomain() throws ContentException {
        if (getDomainName().equals("")) {
            return null;
        } else {
            return getContentManager().getDomain(getDomainName());
        }
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
     * Sets the user password. This method will hash and encode the 
     * specified password, which is an irreversible process.
     * 
     * @param password       the new user password
     */
    public void setPassword(String password) {
        data.setString(UserData.PASSWORD, 
                       createHash(getName() + password));
    }
    
    /**
     * Verifies the user password. This method will hash and encode 
     * the specified password, and compare the result with the real 
     * password. This method can be used to verify user logins.
     * 
     * @param password       the new user password
     * 
     * @return true if the passwords are identical, or
     *         false otherwise
     */
    public boolean verifyPassword(String password) {
        // TODO: remove hack that allows empty passwords
        if (getPassword().equals("")) {
            return true;
        } else {
            return getPassword().equals(createHash(getName() + password));
        }
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
     * Returns the groups that this user belongs to. This method will
     * only return the groups registered to this user in the database.
     * The results will also be cached to return the same list every
     * time, until this object is written to the database.
     * 
     * @return an array of groups this user belongs to
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Group[] getGroups() throws ContentException {
        if (groups == null) {
            groups = Group.findByUser(getContentManager(), this);
        }
        return groups;
    }

    /**
     * Adds this user to the specified group. This action will not 
     * take effect until this object is saved.
     * 
     * @param group          the group object 
     */
    public void addToGroup(Group group) {
        if (getDomainName().equals(group.getDomainName())) {
            if (groupsAdded == null) {
                groupsAdded = new ArrayList();
            }
            groupsAdded.add(group.getName());
        }
    }
    
    /**
     * Removes this user from the specified group. This action will 
     * not take effect until this object is saved.
     * 
     * @param group          the group object 
     */
    public void removeFromGroup(Group group) {
        if (getDomainName().equals(group.getDomainName())) {
            if (groupsRemoved == null) {
                groupsRemoved = new ArrayList();
            }
            groupsRemoved.add(group.getName());
        }
    }

    /**
     * Validates this data object. This method checks that all 
     * required fields have been filled with suitable values.
     * 
     * @throws ContentException if the data object contained errors
     */
    public void validate() throws ContentException {
        if (!getDomainName().equals("") && getDomain() == null) {
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
     * @param user           the user performing the operation
     * @param con            the database connection to use
     * 
     * @throws ContentException if the object data didn't validate or 
     *             if the database couldn't be accessed properly
     */
    protected void doInsert(User user, DatabaseConnection con)
        throws ContentException {

        validate();
        try {
            UserPeer.doInsert(data, con);
            doUserGroups(con);
            groups = null;
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Updates the object data in the database.
     * 
     * @param user           the user performing the operation
     * @param con            the database connection to use
     * 
     * @throws ContentException if the object data didn't validate or 
     *             if the database couldn't be accessed properly
     */
    protected void doUpdate(User user, DatabaseConnection con)
        throws ContentException {

        validate();
        try {
            UserPeer.doUpdate(data, con);
            doUserGroups(con);
            groups = null;
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Deletes the object data from the database.
     * 
     * @param user           the user performing the operation
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected void doDelete(User user, DatabaseConnection con)
        throws ContentException {

        try {
            UserPeer.doDelete(data, con);
            groups = null;
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }
    
    /**
     * Adds and removes user groups from the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    private void doUserGroups(DatabaseConnection con)
        throws DatabaseObjectException {

        UserGroupData  data;
        
        // Handle added groups
        if (groupsAdded != null) {
            for (int i = 0; i < groupsAdded.size(); i++) {
                data = new UserGroupData();
                data.setString(UserGroupData.DOMAIN, getDomainName());
                data.setString(UserGroupData.USER, getName());
                data.setString(UserGroupData.GROUP,
                               groupsAdded.get(i).toString());
                UserGroupPeer.doInsert(data, con);
            }
            groupsAdded = null;
        }

        // Handle removed groups
        if (groupsRemoved != null) {
            for (int i = 0; i < groupsRemoved.size(); i++) {
                data = new UserGroupData();
                data.setString(UserGroupData.DOMAIN, getDomainName());
                data.setString(UserGroupData.USER, getName());
                data.setString(UserGroupData.GROUP,
                               groupsRemoved.get(i).toString());
                UserGroupPeer.doDelete(data, con);
            }
            groupsRemoved = null;
        }
    }
    
    /**
     * Creates an ASCII hash value for a string. The hash value
     * calculation is irreversible, and is calculated with the MD5
     * algorithm and encoded with base-64.
     *
     * @param data           the input string data
     *
     * @return the encoded hash value
     */
    private String createHash(String data) {
        MessageDigest  digest;
        byte           bytes[];
 
        // Compute MD5 digest
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(data.getBytes());
            bytes = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e.getMessage());
            return "";
        }
                                                                                
        // Base-64 encode digest
        return new String(Base64.encodeBase64(bytes));
    }
}
