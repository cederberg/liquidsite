/*
 * PreferenceData.java
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
 * A preference data object. This object encapsulates a row of data
 * from the LS_PREFERENCE table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PreferenceData extends AbstractData {

    /**
     * The domain parameter.
     */
    public static final Parameter DOMAIN =
        new StringParameter(PreferenceData.class, "DOMAIN", "");

    /**
     * The user parameter.
     */
    public static final Parameter USER =
        new StringParameter(PreferenceData.class, "USER", "");

    /**
     * The name parameter.
     */
    public static final Parameter NAME =
        new StringParameter(PreferenceData.class, "NAME", "");

    /**
     * The value parameter.
     */
    public static final Parameter VALUE =
        new StringParameter(PreferenceData.class, "VALUE", "");

    /**
     * Creates a new preference data object with default values.
     */
    public PreferenceData() {
        // No further initialization needed
    }
}
