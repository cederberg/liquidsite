/*
 * UserBean.java
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

package net.percederberg.liquidsite.template;

import java.util.ArrayList;

import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.Group;
import org.liquidsite.core.content.User;
import org.liquidsite.util.log.Log;

/**
 * A user template bean. This class is used to insert the user object
 * in the into the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class UserBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(UserBean.class);

    /**
     * The request user.
     */
    private User user;

    /**
     * Creates a new user template bean.
     *
     * @param user           the request user
     */
    UserBean(User user) {
        this.user = user;
    }

    /**
     * Returns a description of this user. The description will
     * contain the real user name and the login name.
     *
     * @return a description of this user, or
     *         an empty string if the user doesn't exist
     */
    public String toString() {
        if (user == null) {
            return "";
        } else {
            return user.getRealName() + " (" + user.getName() + ")";
        }
    }

    /**
     * Returns the user login name.
     *
     * @return the user login name, or
     *         an empty string if the user doesn't exist
     */
    public String getLogin() {
        if (user == null) {
            return "";
        } else {
            return user.getName();
        }
    }

    /**
     * Returns the real user name.
     *
     * @return the real user name, or
     *         an empty string if the user doesn't exist
     */
    public String getRealName() {
        if (user == null) {
            return "";
        } else {
            return user.getRealName();
        }
    }

    /**
     * Returns the user email address.
     *
     * @return the user email address, or
     *         an empty string if the user doesn't exist
     */
    public String getEmail() {
        if (user == null) {
            return "";
        } else {
            return user.getEmail();
        }
    }

    /**
     * Checks if the users has administration privileges in the
     * domain. For superusers, this method will always return true.
     *
     * @return true if the user is a domain admin, or
     *         false otherwise
     */
    public boolean getDomainadmin() {
        Domain  domain;

        try {
            domain = user.getDomain();
            if (domain == null) {
                return true;
            } else {
                return domain.hasAdminAccess(user);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    /**
     * Returns the superuser flag for the user.
     *
     * @return true if the user is a superuser, or
     *         false otherwise
     */
    public boolean getSuperuser() {
        if (user == null) {
            return false;
        } else {
            return user.isSuperUser();
        }
    }

    /**
     * Returns a list of the group names to which the user belong.
     *
     * @return a list of group names
     */
    public ArrayList getGroups() {
        ArrayList  list = new ArrayList();
        Group[]    groups;

        try {
            if (user != null) {
                groups = user.getGroups();
                for (int i = 0; i < groups.length; i++) {
                    list.add(groups[i].getName());
                }
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        }
        return list;
    }

    /**
     * Checks if the user is member of a named group.
     *
     * @param name            the group name
     *
     * @return true if the user is a member of the group, or
     *         false otherwise
     */
    public boolean inGroup(String name) {
        ArrayList  groups = getGroups();

        return groups.contains(name);
    }
}
