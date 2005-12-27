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
 * Copyright (c) 2004-2005 Per Cederberg. All rights reserved.
 */

package org.liquidsite.core.content;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;

import org.liquidsite.core.data.DataObjectException;
import org.liquidsite.core.data.DataSource;
import org.liquidsite.core.data.UserData;
import org.liquidsite.core.data.UserGroupData;
import org.liquidsite.core.data.UserGroupPeer;
import org.liquidsite.core.data.UserPeer;
import org.liquidsite.util.log.Log;

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
     * The permitted user name characters.
     */
    public static final String NAME_CHARS =
        UPPER_CASE + LOWER_CASE + NUMBERS + BINDERS;

    /**
     * The string containing suitable password characters.
     */
    private static final String PASSWORD_CHARS =
        "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRTUVWXYZ2346789#%=+";

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
     * Generates a password suggestion that should be sufficiently
     * hard to crack.
     *
     * @return the generated password
     */
    public static String generatePassword() {
        StringBuffer  result = new StringBuffer();
        int           length = PASSWORD_CHARS.length();
        char          c;

        while (result.length() < 8) {
            c = PASSWORD_CHARS.charAt((int) (Math.random() * length));
            if (result.length() > 0 || Character.isLetter(c)) {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Returns the number of users in a specified domain. Only users
     * with matching names will be counted.
     *
     * @param manager        the content manager to use
     * @param domain         the domain, or null for superusers
     * @param filter         the search filter (empty for all)
     *
     * @return the number of matching users in the domain
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static int countByDomain(ContentManager manager,
                             Domain domain,
                             String filter)
        throws ContentException {

        DataSource  src = getDataSource(manager);
        String      domainName = "";

        try {
            if (domain != null) {
                domainName = domain.getName();
            }
            return UserPeer.doCountByDomain(src, domainName, filter);
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            src.close();
        }
    }

    /**
     * Returns the number of users in a specified group.
     *
     * @param manager        the content manager to use
     * @param group          the group
     *
     * @return the number of users in the group
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static int countByGroup(ContentManager manager,
                            Group group)
        throws ContentException {

        DataSource  src = getDataSource(manager);

        try {
            return UserGroupPeer.doCountByGroup(src,
                                                group.getDomainName(),
                                                group.getName());
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            src.close();
        }
    }

    /**
     * Returns an array of users in a specified domain. Only users
     * with matching names will be returned. Also, only a limited
     * interval of the matching users will be returned.
     *
     * @param manager        the content manager to use
     * @param domain         the domain, or null for superusers
     * @param filter         the search filter (empty for all)
     * @param startPos       the list interval start position
     * @param maxLength      the list interval maximum length
     *
     * @return an array of matching users in the domain
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static User[] findByDomain(ContentManager manager,
                               Domain domain,
                               String filter,
                               int startPos,
                               int maxLength)
        throws ContentException {

        DataSource  src = getDataSource(manager);
        ArrayList   list;
        User[]      res;
        String      domainName = "";

        try {
            if (domain != null) {
                domainName = domain.getName();
            }
            list = UserPeer.doSelectByDomain(src,
                                             domainName,
                                             filter,
                                             startPos,
                                             maxLength);
            res = new User[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = new User(manager, (UserData) list.get(i));
            }
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            src.close();
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

        DataSource  src = getDataSource(manager);
        UserData    data;
        String      domainName = "";

        try {
            if (domain != null) {
                domainName = domain.getName();
            }
            data = UserPeer.doSelectByName(src, domainName, name);
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            src.close();
        }
        if (data == null) {
            return null;
        } else {
            return new User(manager, data);
        }
    }

    /**
     * Returns an array of all users in a certain group. Only a
     * limited interval of the matching users will be returned.
     *
     * @param manager        the content manager to use
     * @param group          the group
     * @param startPos       the list interval start position
     * @param maxLength      the list interval maximum length
     *
     * @return an array of all users in the group
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static User[] findByGroup(ContentManager manager,
                              Group group,
                              int startPos,
                              int maxLength)
        throws ContentException {

        DataSource     src = getDataSource(manager);
        ArrayList      list;
        User[]         res;
        UserGroupData  data;
        UserData       user;
        String         name;

        try {
            list = UserGroupPeer.doSelectByGroup(src,
                                                 group.getDomainName(),
                                                 group.getName(),
                                                 startPos,
                                                 maxLength);
            res = new User[list.size()];
            for (int i = 0; i < list.size(); i++) {
                data = (UserGroupData) list.get(i);
                name = data.getString(UserGroupData.USER);
                user = UserPeer.doSelectByName(src,
                                               group.getDomainName(),
                                               name);
                res[i] = new User(manager, user);
            }
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            src.close();
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
        setPasswordEncoded(createHash(getName() + password));
    }

    /**
     * Sets the encoded user password. This method assumes that the
     * specified password has already been hashed and encoded and
     * should only be used when restoring user passwords from backups. 
     *
     * @param password       the new encoded user password
     */
    public void setPasswordEncoded(String password) {
        data.setString(UserData.PASSWORD, password);
    }

    /**
     * Returns the enabled flag.
     *
     * @return true if the user is enabled, or
     *         false otherwise
     */
    public boolean getEnabled() {
        return data.getBoolean(UserData.ENABLED);
    }

    /**
     * Sets the enabled flag.
     *
     * @param enabled         the new enabled flag
     */
    public void setEnabled(boolean enabled) {
        data.setBoolean(UserData.ENABLED, enabled);
    }

    /**
     * Verifies the user password. This method will hash and encode
     * the specified password, and compare the result with the real
     * password. This method can be used to verify user logins and
     * will return false if the enabled flag isn't set.
     *
     * @param password       the user password
     *
     * @return true if the passwords are identical, or
     *         false otherwise
     */
    public boolean verifyPassword(String password) {
        // TODO: remove hack that allows empty passwords
        if (!getEnabled()) {
            return false;
        } else if (getPassword().equals("")) {
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
     * Returns the user time zone.
     *
     * @return the user time zone
     */
    public TimeZone getTimeZone() {
        // TODO: the timezone should be stored in the database
        return TimeZone.getTimeZone("Europe/Stockholm");
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
     * Validates the object data before writing to the database.
     *
     * @throws ContentException if the object data wasn't valid
     */
    protected void doValidate() throws ContentException {
        ContentManager  manager = getContentManager();

        if (!isPersistent()) {
            if (!getDomainName().equals("") && getDomain() == null) {
                throw new ContentException("domain '" + getDomainName() +
                                           "' does not exist");
            }
            validateSize("user name", getName(), 1, 30);
            validateChars("user name", getName(), NAME_CHARS);
            if (manager.getUser(getDomain(), getName()) != null) {
                throw new ContentException("user '" + getName() +
                                           "' already exists");
            }
        }
        validateSize("user password", getPassword(), 1, 30);
        validateSize("user real name", getRealName(), 0, 100);
        validateSize("user email", getEmail(), 0, 100);
        validateSize("user comment", getComment(), 0, 200);
    }

    /**
     * Inserts the object data into the database. If the restore flag
     * is set, no automatic changes should be made to the data before
     * writing to the database.
     *
     * @param src            the data source to use
     * @param user           the user performing the operation
     * @param restore        the restore flag
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void doInsert(DataSource src, User user, boolean restore)
        throws ContentException {

        try {
            UserPeer.doInsert(src, data);
            doUserGroups(src);
            groups = null;
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Updates the object data in the database.
     *
     * @param src            the data source to use
     * @param user           the user performing the operation
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void doUpdate(DataSource src, User user)
        throws ContentException {

        try {
            UserPeer.doUpdate(src, data);
            doUserGroups(src);
            groups = null;
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Deletes the object data from the database.
     *
     * @param src            the data source to use
     * @param user           the user performing the operation
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void doDelete(DataSource src, User user)
        throws ContentException {

        try {
            UserPeer.doDelete(src, data);
            groups = null;
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Adds and removes user groups from the database.
     *
     * @param src            the data source to use
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    private void doUserGroups(DataSource src) throws DataObjectException {
        UserGroupData  groupData;

        // Handle added groups
        if (groupsAdded != null) {
            for (int i = 0; i < groupsAdded.size(); i++) {
                groupData = new UserGroupData();
                groupData.setString(UserGroupData.DOMAIN, getDomainName());
                groupData.setString(UserGroupData.USER, getName());
                groupData.setString(UserGroupData.GROUP,
                               groupsAdded.get(i).toString());
                UserGroupPeer.doInsert(src, groupData);
            }
            groupsAdded = null;
        }

        // Handle removed groups
        if (groupsRemoved != null) {
            for (int i = 0; i < groupsRemoved.size(); i++) {
                groupData = new UserGroupData();
                groupData.setString(UserGroupData.DOMAIN, getDomainName());
                groupData.setString(UserGroupData.USER, getName());
                groupData.setString(UserGroupData.GROUP,
                               groupsRemoved.get(i).toString());
                UserGroupPeer.doDelete(src, groupData);
            }
            groupsRemoved = null;
        }
    }

    /**
     * Creates an ASCII hash value for a string. The hash value
     * calculation is irreversible, and is calculated with the MD5
     * algorithm and encoded with base-64.
     *
     * @param input           the input string data
     *
     * @return the encoded hash value
     */
    private String createHash(String input) {
        MessageDigest  digest;
        byte           bytes[];

        // Compute MD5 digest
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(input.getBytes());
            bytes = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e.getMessage());
            return "";
        }

        // Base-64 encode digest
        return new String(Base64.encodeBase64(bytes));
    }
}
