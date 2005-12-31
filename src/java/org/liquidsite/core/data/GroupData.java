/*
 * GroupData.java
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

package org.liquidsite.core.data;

/**
 * A group data object. This object encapsulates a row of data from
 * the LS_GROUP table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class GroupData extends AbstractData {

    /**
     * The domain parameter.
     */
    public static final Parameter DOMAIN =
        new StringParameter(GroupData.class, "DOMAIN", "");

    /**
     * The name parameter.
     */
    public static final Parameter NAME =
        new StringParameter(GroupData.class, "NAME", "");

    /**
     * The description parameter.
     */
    public static final Parameter DESCRIPTION =
        new StringParameter(GroupData.class, "DESCRIPTION", "");

    /**
     * The public parameter.
     */
    public static final Parameter PUBLIC =
        new BooleanParameter(GroupData.class, "PUBLIC", false);

    /**
     * The comment parameter.
     */
    public static final Parameter COMMENT =
        new StringParameter(GroupData.class, "COMMENT", "");

    /**
     * Creates a new group data object with default values.
     */
    public GroupData() {
        // No further initialization needed
    }
}
