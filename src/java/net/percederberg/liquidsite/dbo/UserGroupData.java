/*
 * UserGroupData.java
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

package net.percederberg.liquidsite.dbo;

/**
 * A user group data object. This object encapsulates a row of data 
 * from the LS_USER_GROUP table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class UserGroupData extends AbstractData {

    /**
     * The domain parameter.
     */
    public static final Parameter DOMAIN =
        new StringParameter(UserGroupData.class, "DOMAIN", "");
    
    /**
     * The user parameter.
     */
    public static final Parameter USER =
        new StringParameter(UserGroupData.class, "USER", "");

    /**
     * The group parameter.
     */
    public static final Parameter GROUP =
        new StringParameter(UserGroupData.class, "GROUP", "");

    /**
     * Creates a new user group data object with default values.
     */
    public UserGroupData() {
    }
}
