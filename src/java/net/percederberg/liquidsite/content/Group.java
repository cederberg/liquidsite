/*
 * Group.java
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

package net.percederberg.liquidsite.content;

import java.util.ArrayList;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.DatabaseObjectException;
import net.percederberg.liquidsite.dbo.GroupData;
import net.percederberg.liquidsite.dbo.GroupPeer;
import net.percederberg.liquidsite.dbo.UserGroupData;
import net.percederberg.liquidsite.dbo.UserGroupPeer;

/**
 * A system group.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Group extends PersistentObject {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(Group.class);

    /**
     * The group data.
     */
    private GroupData data;

    /**
     * The list of user names added since the object was saved.
     */
    private ArrayList usersAdded = null;

    /**
     * The list of user names removed since the object was saved.
     */
    private ArrayList usersRemoved = null;

    /**
     * Returns a list of matching groups in a specified domain. Only
     * groups with matching names will be returned.
     *
     * @param manager        the content manager to use
     * @param domain         the domain
     * @param filter         the search filter (empty for all)
     *
     * @return an array of all groups in the domain
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Group[] findByDomain(ContentManager manager,
                                Domain domain,
                                String filter)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ArrayList           list;
        Group[]             res;

        try {
            list = GroupPeer.doSelectByDomain(domain.getName(), filter, con);
            res = new Group[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = new Group(manager, (GroupData) list.get(i));
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
     * Returns a group with a specified name.
     *
     * @param manager        the content manager to use
     * @param domain         the domain
     * @param name           the group name
     *
     * @return the group found, or
     *         null if no matching group existed
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Group findByName(ContentManager manager,
                            Domain domain,
                            String name)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        GroupData           data;

        try {
            data = GroupPeer.doSelectByName(domain.getName(), name, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
        if (data == null) {
            return null;
        } else {
            return new Group(manager, data);
        }
    }

    /**
     * Returns an array of all groups a certain user belongs to.
     *
     * @param manager        the content manager to use
     * @param user           the user
     *
     * @return an array of all groups the user belongs to
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Group[] findByUser(ContentManager manager, User user)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ArrayList           list;
        Group[]             res;
        UserGroupData       data;
        GroupData           group;
        String              name;

        try {
            list = UserGroupPeer.doSelectByUser(user.getDomainName(),
                                                user.getName(),
                                                con);
            res = new Group[list.size()];
            for (int i = 0; i < list.size(); i++) {
                data = (UserGroupData) list.get(i);
                name = data.getString(UserGroupData.GROUP);
                group = GroupPeer.doSelectByName(user.getDomainName(),
                                                 name,
                                                 con);
                res[i] = new Group(manager, group);
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
     * Creates a new group with default values.
     *
     * @param manager        the content manager to use
     * @param domain         the domain
     * @param name           the group name
     */
    public Group(ContentManager manager, Domain domain, String name) {
        super(manager, false);
        this.data = new GroupData();
        this.data.setString(GroupData.DOMAIN, domain.getName());
        this.data.setString(GroupData.NAME, name);
    }

    /**
     * Creates a new group from a data object.
     *
     * @param manager        the content manager to use
     * @param data           the group data object
     */
    private Group(ContentManager manager, GroupData data) {
        super(manager, true);
        this.data = data;
    }

    /**
     * Checks if this group equals another object. This method will
     * only return true if the other object is a group with the same
     * domain and group name.
     *
     * @param obj            the object to compare with
     *
     * @return true if the other object is an identical group, or
     *         false otherwise
     */
    public boolean equals(Object obj) {
        if (obj instanceof Group) {
            return equals((Group) obj);
        } else {
            return false;
        }
    }

    /**
     * Checks if this group equals another group. This method will
     * only return true if the other object is a group with the same
     * domain and group name.
     *
     * @param obj            the object to compare with
     *
     * @return true if the other object is an identical group, or
     *         false otherwise
     */
    public boolean equals(Group obj) {
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
     * Returns the group domain.
     *
     * @return the group domain
     *
     * @throws ContentException if no content manager is available
     */
    public Domain getDomain() throws ContentException {
        return getContentManager().getDomain(getDomainName());
    }

    /**
     * Returns the group domain name
     *
     * @return the group domain name
     */
    public String getDomainName() {
        return data.getString(GroupData.DOMAIN);
    }

    /**
     * Returns the group name.
     *
     * @return the group name
     */
    public String getName() {
        return data.getString(GroupData.NAME);
    }

    /**
     * Returns the group description.
     *
     * @return the group description
     */
    public String getDescription() {
        return data.getString(GroupData.DESCRIPTION);
    }

    /**
     * Sets the the group description.
     *
     * @param description    the new group description
     */
    public void setDescription(String description) {
        data.setString(GroupData.DESCRIPTION, description);
    }

    /**
     * Returns the group comment.
     *
     * @return the group comment
     */
    public String getComment() {
        return data.getString(GroupData.COMMENT);
    }

    /**
     * Sets the group comment.
     *
     * @param comment        the new group comment
     */
    public void setComment(String comment) {
        data.setString(GroupData.COMMENT, comment);
    }

    /**
     * Returns the number of users that belong to this group. This
     * method will only count the users registered to this group in
     * the database.
     *
     * @return the number of users that belong to this group
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public int getUserCount() throws ContentException {
        return User.countByGroup(getContentManager(), this);
    }

    /**
     * Returns the users that belong to this group. This method will
     * only return the users registered to this group in the database.
     * Also, only a limited interval of the matching users will be
     * returned.
     *
     * @param startPos       the list interval start position
     * @param maxLength      the list interval maximum length
     *
     * @return an array of users belonging to this group
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public User[] getUsers(int startPos, int maxLength)
        throws ContentException {

        return User.findByGroup(getContentManager(),
                                this,
                                startPos,
                                maxLength);
    }

    /**
     * Adds the specified user to this  group. This action will not
     * take effect until this object is saved.
     *
     * @param user           the user object
     */
    public void addUser(User user) {
        if (getDomainName().equals(user.getDomainName())) {
            if (usersAdded == null) {
                usersAdded = new ArrayList();
            }
            usersAdded.add(user.getName());
        }
    }

    /**
     * Removes the specified user from this group. This action will
     * not take effect until this object is saved.
     *
     * @param user           the user object
     */
    public void removeUser(User user) {
        if (getDomainName().equals(user.getDomainName())) {
            if (usersRemoved == null) {
                usersRemoved = new ArrayList();
            }
            usersRemoved.add(user.getName());
        }
    }

    /**
     * Validates the object data before writing to the database.
     *
     * @throws ContentException if the object data wasn't valid
     */
    protected void doValidate() throws ContentException {
        if (getDomainName().equals("")) {
            throw new ContentException("no domain set for group object");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + getDomainName() +
                                       "' does not exist");
        } else if (getName().equals("")) {
            throw new ContentException("no name set for group object");
        }
    }

    /**
     * Inserts the object data into the database. If the restore flag
     * is set, no automatic changes should be made to the data before
     * writing to the database.
     *
     * @param user           the user performing the operation
     * @param con            the database connection to use
     * @param restore        the restore flag
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void doInsert(User user,
                            DatabaseConnection con,
                            boolean restore)
        throws ContentException {

        try {
            GroupPeer.doInsert(data, con);
            doUserGroups(con);
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
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void doUpdate(User user, DatabaseConnection con)
        throws ContentException {

        try {
            GroupPeer.doUpdate(data, con);
            doUserGroups(con);
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
            GroupPeer.doDelete(data, con);
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

        UserGroupData  userData;

        // Handle added users
        if (usersAdded != null) {
            for (int i = 0; i < usersAdded.size(); i++) {
                userData = new UserGroupData();
                userData.setString(UserGroupData.DOMAIN, getDomainName());
                userData.setString(UserGroupData.USER,
                               usersAdded.get(i).toString());
                userData.setString(UserGroupData.GROUP, getName());
                UserGroupPeer.doInsert(userData, con);
            }
            usersAdded = null;
        }

        // Handle removed users
        if (usersRemoved != null) {
            for (int i = 0; i < usersRemoved.size(); i++) {
                userData = new UserGroupData();
                userData.setString(UserGroupData.DOMAIN, getDomainName());
                userData.setString(UserGroupData.USER,
                               usersRemoved.get(i).toString());
                userData.setString(UserGroupData.GROUP, getName());
                UserGroupPeer.doDelete(userData, con);
            }
            usersRemoved = null;
        }
    }
}
