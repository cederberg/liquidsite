/*
 * PermissionData.java
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

package net.percederberg.liquidsite.dbo;

/**
 * A permission data object. This object encapsulates a row of data
 * from the LS_PERMISSION table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PermissionData extends AbstractData {

    /**
     * The domain parameter.
     */
    public static final Parameter DOMAIN =
        new StringParameter(PermissionData.class, "DOMAIN", "");

    /**
     * The content parameter.
     */
    public static final Parameter CONTENT =
        new IntegerParameter(PermissionData.class, "CONTENT", 0);

    /**
     * The user parameter.
     */
    public static final Parameter USER =
        new StringParameter(PermissionData.class, "USER", "");

    /**
     * The group parameter.
     */
    public static final Parameter GROUP =
        new StringParameter(PermissionData.class, "GROUP", "");

    /**
     * The read parameter.
     */
    public static final Parameter READ =
        new BooleanParameter(PermissionData.class, "READ", false);

    /**
     * The write parameter.
     */
    public static final Parameter WRITE =
        new BooleanParameter(PermissionData.class, "WRITE", false);

    /**
     * The publish parameter.
     */
    public static final Parameter PUBLISH =
        new BooleanParameter(PermissionData.class, "PUBLISH", false);

    /**
     * The admin parameter.
     */
    public static final Parameter ADMIN =
        new BooleanParameter(PermissionData.class, "ADMIN", false);

    /**
     * Creates a new permission data object with default values.
     */
    public PermissionData() {
        super();
    }
}
