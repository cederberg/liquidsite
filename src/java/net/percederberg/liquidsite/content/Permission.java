/*
 * Permission.java
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

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.DatabaseObjectException;
import net.percederberg.liquidsite.dbo.PermissionData;
import net.percederberg.liquidsite.dbo.PermissionPeer;

/**
 * A content object permission.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Permission extends PersistentObject {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(Permission.class);

    /**
     * The permission data object.
     */
    private PermissionData data;

    /**
     * Returns an array of all permission objects for the specified 
     * domain object. Note that this method only returns the list of 
     * permissions set on the domain object, not all the permissions
     * for content objects in the domain.
     * 
     * @param domain         the domain
     * 
     * @return an array of permission objects found, or 
     *         an empty array if no permissions were found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected static Permission[] findByDomain(Domain domain) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection();
        ArrayList           list;
        Permission[]        res;

        try {
            list = PermissionPeer.doSelectByContent(domain.getName(),
                                                    0, 
                                                    con);
            res = new Permission[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = new Permission((PermissionData) list.get(i));
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(con);
        }
        return res;
    }

    /**
     * Returns an array of all permission objects for the specified 
     * content object. Note that this method only returns the list of 
     * permissions set on the content object, not any inherited 
     * permissions.
     * 
     * @param content        the content object
     * 
     * @return an array of permission objects found, or 
     *         an empty array if no permissions were found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected static Permission[] findByContent(Content content) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection();
        ArrayList           list;
        Permission[]        res;

        try {
            list = PermissionPeer.doSelectByContent(content.getDomainName(),
                                                    content.getId(), 
                                                    con);
            res = new Permission[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = new Permission((PermissionData) list.get(i));
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(con);
        }
        return res;
    }

    /**
     * Returns the permission object with the specified content id,
     * user, and group.
     * 
     * @param content        the content object
     * @param user           the user
     * @param group          the group
     * 
     * @return the permission found, or
     *         null if no matching permission existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected static Permission findByUser(Content content,
                                           User user,
                                           Group group)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection();
        PermissionData      data;

        try {
            data = PermissionPeer.doSelectByUser(content.getDomainName(),
                                                 content.getId(), 
                                                 user.getName(),
                                                 group.getName(),
                                                 con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(con);
        }
        if (data == null) {
            return null;
        } else {
            return new Permission(data);
        }
    }

    /**
     * Creates a new permission with default values. This permission
     * is valid for the domain root.
     * 
     * @param domain         the domain object
     * @param user           the user, or null for any user
     * @param group          the gruop, or null for any group
     */
    public Permission(Domain domain, User user, Group group) {
        super(false, true);
        this.data = new PermissionData();
        this.data.setString(PermissionData.DOMAIN, domain.getName());
        this.data.setInt(PermissionData.CONTENT, 0);
        if (user != null) {
            this.data.setString(PermissionData.USER, user.getName());
        }
        if (group != null) {
            this.data.setString(PermissionData.GROUP, group.getName());
        }
    }

    /**
     * Creates a new permission with default values.
     * 
     * @param content        the content object
     * @param user           the user, or null for any user
     * @param group          the gruop, or null for any group
     */
    public Permission(Content content, User user, Group group) {
        super(false, true);
        this.data = new PermissionData();
        this.data.setString(PermissionData.DOMAIN, content.getDomainName());
        this.data.setInt(PermissionData.CONTENT, content.getId());
        if (user != null) {
            this.data.setString(PermissionData.USER, user.getName());
        }
        if (group != null) {
            this.data.setString(PermissionData.GROUP, group.getName());
        }
    }

    /**
     * Creates a new permission from a data object.
     * 
     * @param data           the permission data object
     */
    private Permission(PermissionData data) {
        super(true, true);
        this.data = data;
    }

    /**
     * Checks if this permission equals another object. This method 
     * will only return true if the other object is a permission with 
     * the same domain, content identifier, user and group.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical permission, or
     *         false otherwise 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Permission) {
            return equals((Permission) obj);
        } else {
            return false;
        }
    }

    /**
     * Checks if this permission equals another object. This method 
     * will only return true if the other object is a permission with 
     * the same domain, content identifier, user and group.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical permission, or
     *         false otherwise 
     */
    public boolean equals(Permission obj) {
        return getDomainName().equals(obj.getDomainName())
            && getContentId() == obj.getContentId()
            && getUserName().equals(obj.getUserName())
            && getGroupName().equals(obj.getGroupName());
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();
        
        buffer.append("Domain: ");
        buffer.append(getDomainName());
        buffer.append(", Content: ");
        buffer.append(getContentId());
        buffer.append(", ");
        if (!getUserName().equals("")) {
            buffer.append("User: ");
            buffer.append(getUserName());
        } else if (!getGroupName().equals("")) {
            buffer.append("Group ");
            buffer.append(getGroupName());
        } else {
            buffer.append("Any User");
        }
        buffer.append(", Permissions: ");
        buffer.append(toString(getRead(), "r"));
        buffer.append(toString(getWrite(), "w"));
        buffer.append(toString(getPublish(), "p"));
        buffer.append(toString(getAdmin(), "a"));

        return buffer.toString();
    }
    
    /**
     * Returns a string representation of a permission flag.
     * 
     * @param flag           the permission flag
     * @param symbol         the flag symbol
     * 
     * @return the string representation fo the permission flag
     */
    private String toString(boolean flag, String symbol) {
        if (flag) {
            return symbol;
        } else {
            return "-";
        }
    }

    /**
     * Returns the content domain.
     * 
     * @return the content domain
     * 
     * @throws ContentException if no content manager is available
     */
    public Domain getDomain() throws ContentException {
        return getContentManager().getDomain(getDomainName());
    }
    
    /**
     * Returns the content domain name
     * 
     * @return the content domain name
     */
    public String getDomainName() {
        return data.getString(PermissionData.DOMAIN);
    }
    
    /**
     * Returns the content object.
     * 
     * @return the content object
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Content getContent() throws ContentException {
        return getContentManager().getContent(getContentId());
    }

    /**
     * Returns the content identifier.
     * 
     * @return the content identifier
     */
    public int getContentId() {
        return data.getInt(PermissionData.CONTENT);
    }

    /**
     * Returns the permission user.
     * 
     * @return the permission user, or
     *         null if any user matches this permission
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public User getUser() throws ContentException {
        String  name = getUserName();

        if (name.equals("")) {
            return null;
        } else {
            return getContentManager().getUser(getDomain(), name);
        }
    }

    /**
     * Returns the permission user name.
     * 
     * @return the permission user name
     */
    public String getUserName() {
        return data.getString(PermissionData.USER);
    }

    /**
     * Returns the permission group.
     * 
     * @return the permission group, or
     *         null if any group matches this permission
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Group getGroup() throws ContentException {
        String  name = getGroupName();

        if (name.equals("")) {
            return null;
        } else {
            return getContentManager().getGroup(getDomain(), name);
        }
    }

    /**
     * Returns the permission group name.
     * 
     * @return the permission group name
     */
    public String getGroupName() {
        return data.getString(PermissionData.GROUP);
    }

    /**
     * Returns the read permission flag.
     * 
     * @return the read permission flag
     */
    public boolean getRead() {
        return data.getBoolean(PermissionData.READ);
    }

    /**
     * Sets the read permission flag.
     * 
     * @param read           the new read permission flag
     */
    public void setRead(boolean read) {
        data.setBoolean(PermissionData.READ, read);
    }

    /**
     * Returns the write permission flag.
     * 
     * @return the write permission flag
     */
    public boolean getWrite() {
        return data.getBoolean(PermissionData.WRITE);
    }

    /**
     * Sets the write permission flag.
     * 
     * @param write          the new write permission flag
     */
    public void setWrite(boolean write) {
        data.setBoolean(PermissionData.WRITE, write);
    }

    /**
     * Returns the publish permission flag.
     * 
     * @return the publish permission flag
     */
    public boolean getPublish() {
        return data.getBoolean(PermissionData.PUBLISH);
    }

    /**
     * Sets the publish permission flag.
     * 
     * @param publish        the new publish permission flag
     */
    public void setPublish(boolean publish) {
        data.setBoolean(PermissionData.PUBLISH, publish);
    }

    /**
     * Returns the admin permission flag.
     * 
     * @return the admin permission flag
     */
    public boolean getAdmin() {
        return data.getBoolean(PermissionData.ADMIN);
    }

    /**
     * Sets the admin permission flag.
     * 
     * @param admin          the new admin permission flag
     */
    public void setAdmin(boolean admin) {
        data.setBoolean(PermissionData.ADMIN, admin);
    }

    /**
     * Checks if the specified user or group list matches this 
     * permission. The list of groups should be the user groups.
     * 
     * @param user           the user to check, or null for none
     * @param groups         the group list to check, or null for none
     * 
     * @return true if this permission matches, or
     *         false otherwise
     */
    public boolean isMatch(User user, Group[] groups) {
        if (getUserName().equals("") && getGroupName().equals("")) {
            return true;
        } else if (user == null) {
            return false;
        } else if (!getUserName().equals("")) {
            return getUserName().equals(user.getName());
        } else if (groups == null) {
            return false;
        } else {
            for (int i = 0; i < groups.length; i++) {
                if (groups[i].getName().equals(getGroupName())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Validates this data object. This method checks that all 
     * required fields have been filled with suitable values.
     * 
     * @throws ContentException if the data object contained errors
     */
    public void validate() throws ContentException {
        if (getDomainName().equals("")) {
            throw new ContentException("no domain set for permission object");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + getDomainName() + 
                                       "'does not exist");
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
     * @throws ContentSecurityException if the user specified didn't
     *             have insert permissions
     */
    protected void doInsert(User user, DatabaseConnection con)
        throws ContentException, ContentSecurityException {

        if (getContentId() > 0 && !getContent().hasAdminAccess(user)) {
            throw new ContentSecurityException(user, "delete", this);
        } else if (getContentId() == 0 && !getDomain().hasAdminAccess(user)) {
            throw new ContentSecurityException(user, "delete", this);
        }
        validate();
        try {
            PermissionPeer.doInsert(data, con);
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
     * @throws ContentSecurityException if the user specified didn't
     *             have update permissions
     */
    protected void doUpdate(User user, DatabaseConnection con)
        throws ContentException, ContentSecurityException {

        if (getContentId() > 0 && !getContent().hasAdminAccess(user)) {
            throw new ContentSecurityException(user, "delete", this);
        } else if (getContentId() == 0 && !getDomain().hasAdminAccess(user)) {
            throw new ContentSecurityException(user, "delete", this);
        }
        validate();
        try {
            PermissionPeer.doUpdate(data, con);
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
     * @throws ContentSecurityException if the user specified didn't
     *             have delete permissions
     */
    protected void doDelete(User user, DatabaseConnection con)
        throws ContentException, ContentSecurityException {

        if (getContentId() > 0 && !getContent().hasAdminAccess(user)) {
            throw new ContentSecurityException(user, "delete", this);
        } else if (getContentId() == 0 && !getDomain().hasAdminAccess(user)) {
            throw new ContentSecurityException(user, "delete", this);
        }
        try {
            PermissionPeer.doDelete(data, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }
}
