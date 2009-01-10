/*
 * ContentData.java
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

package org.liquidsite.core.data;

import java.util.Date;

/**
 * A content data object. This object encapsulates a row of data from
 * the LS_CONTENT table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentData extends AbstractData {

    /**
     * The domain parameter.
     */
    public static final Parameter DOMAIN =
        new StringParameter(ContentData.class, "DOMAIN", "");

    /**
     * The id parameter.
     */
    public static final Parameter ID =
        new IntegerParameter(ContentData.class, "ID", -1);

    /**
     * The revision parameter.
     */
    public static final Parameter REVISION =
        new IntegerParameter(ContentData.class, "REVISION", 0);

    /**
     * The category parameter.
     */
    public static final Parameter CATEGORY =
        new IntegerParameter(ContentData.class, "CATEGORY", 0);

    /**
     * The name parameter.
     */
    public static final Parameter NAME =
        new StringParameter(ContentData.class, "NAME", "");

    /**
     * The parent parameter.
     */
    public static final Parameter PARENT =
        new IntegerParameter(ContentData.class, "PARENT", 0);

    /**
     * The online parameter.
     */
    public static final Parameter ONLINE =
        new DateParameter(ContentData.class, "ONLINE", null);

    /**
     * The offline parameter.
     */
    public static final Parameter OFFLINE =
        new DateParameter(ContentData.class, "OFFLINE", null);

    /**
     * The modified parameter.
     */
    public static final Parameter MODIFIED =
        new DateParameter(ContentData.class, "MODIFIED", new Date(0));

    /**
     * The author parameter.
     */
    public static final Parameter AUTHOR =
        new StringParameter(ContentData.class, "AUTHOR", "");

    /**
     * The comment parameter.
     */
    public static final Parameter COMMENT =
        new StringParameter(ContentData.class, "COMMENT", "");

    /**
     * The status parameter.
     */
    public static final Parameter STATUS =
        new IntegerParameter(ContentData.class, "STATUS", 0);

    /**
     * Creates a new content data object with default values.
     */
    public ContentData() {
        super();
    }
}
