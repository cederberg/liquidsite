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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.template;

import net.percederberg.liquidsite.content.User;

/**
 * A user template bean. This class is used to insert the user object
 * in the into the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class UserBean {

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
     * Returns the user login name.
     * 
     * @return the user login name
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
     * @return the real user name
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
     * @return the user email address
     */
    public String getEmail() {
        if (user == null) {
            return "";
        } else {
            return user.getEmail();
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
}
