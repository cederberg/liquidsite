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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package org.liquidsite.core.content;

import org.liquidsite.core.data.PermissionData;

/**
 * A content object permission.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Permission {

    /**
     * The permission data object.
     */
    private PermissionData data;

    /**
     * Creates a new permission with default values.
     *
     * @param user           the user, or null for any user
     * @param group          the gruop, or null for any group
     */
    public Permission(User user, Group group) {
        this.data = new PermissionData();
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
    Permission(PermissionData data) {
        this.data = data;
    }

    /**
     * Checks if this permission equals another object. This method
     * will only return true if the other object is a permission with
     * the same user and group.
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
     * the same user and group.
     *
     * @param obj            the object to compare with
     *
     * @return true if the other object is an identical permission, or
     *         false otherwise
     */
    public boolean equals(Permission obj) {
        return getUserName().equals(obj.getUserName())
            && getGroupName().equals(obj.getGroupName());
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();

        if (!getUserName().equals("")) {
            buffer.append(getUserName());
            buffer.append(" (user)");
        } else if (!getGroupName().equals("")) {
            buffer.append(getGroupName());
            buffer.append(" (group)");
        } else {
            buffer.append("<anonymous>");
        }
        buffer.append(": ");
        buffer.append(getRead() ? "r" : "-");
        buffer.append(getWrite() ? "w" : "-");
        buffer.append(getPublish() ? "p" : "-");
        buffer.append(getAdmin() ? "a" : "-");

        return buffer.toString();
    }

    /**
     * Returns the internal permission data object. The permission
     * data will be modified with the specified permission reference
     * before being returned.
     *
     * @param domain          the domain name
     * @param content         the content identifier
     *
     * @return the internal permission data object
     */
    PermissionData getData(String domain, int content) {
        data.setString(PermissionData.DOMAIN, domain);
        data.setInt(PermissionData.CONTENT, content);
        return data;
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
     * permission. The list of groups should be the user groups, and
     * both must be in the same domain as the permission reference
     * object.
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
        } else if (getUserName().length() > 0) {
            return getUserName().equals(user.getName());
        } else if (groups == null) {
            return false;
        } else {
            for (int i = 0; i < groups.length; i++) {
                if (getGroupName().equals(groups[i].getName())) {
                    return true;
                }
            }
            return false;
        }
    }
}
