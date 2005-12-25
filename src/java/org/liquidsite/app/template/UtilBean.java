/*
 * UtilBean.java
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

package org.liquidsite.app.template;

import java.util.Date;

import org.liquidsite.core.content.User;

/**
 * A utility template bean. This class is used to provide utility data
 * and functions to the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class UtilBean {

    /**
     * Creates a new utility bean.
     */
    UtilBean() {
        // No initialization needed
    }

    /**
     * Returns the current date and time.
     *
     * @return the current date and time
     */
    public Date getCurrentTime() {
        return new Date();
    }

    /**
     * Returns a new random password. The generated password can be
     * used for various types of authentication.
     *
     * @return the new random password
     */
    public String getRandomPassword() {
        return User.generatePassword();
    }
}
