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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.dbo;

import java.util.Date;

/**
 * A content data object. This object encapsulates a row of data from
 * the LS_CONTENT table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentData extends AbstractData implements Comparable {

    /**
     * The domain parameter.
     */
    public static final Parameter DOMAIN = 
        new StringParameter(ContentData.class, "DOMAIN", "");

    /**
     * The id parameter.
     */
    public static final Parameter ID = 
        new IntegerParameter(ContentData.class, "ID", 0);

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
        new DateParameter(ContentData.class, "ONLINE", new Date(0));
    
    /**
     * The offline parameter.
     */
    public static final Parameter OFFLINE = 
        new DateParameter(ContentData.class, "OFFLINE", new Date(0));
    
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
     * Creates a new content data object with default values.
     */
    public ContentData() {
        super();
    }
    
    /**
     * Compares this object with the specified object for order. 
     * Returns a negative integer, zero, or a positive integer as 
     * this object is less than, equal to, or greater than the 
     * specified object.
     * 
     * @param obj            the object to compare to
     * 
     * @return a negative integer, zero, or a positive integer as 
     *         this object is less than, equal to, or greater than 
     *         the specified object
     * 
     * @throws ClassCastException if the object isn't a ContentData
     *             object
     */
    public int compareTo(Object obj) throws ClassCastException {
        return compareTo((ContentData) obj);
    }

    /**
     * Compares this object with the specified content data for 
     * order. Returns a negative integer, zero, or a positive integer 
     * as this object is less than, equal to, or greater than the 
     * specified object. The ordering is based primarily on category
     * and secondarily on name.
     * 
     * @param data           the content data to compare to
     * 
     * @return a negative integer, zero, or a positive integer as 
     *         this object is less than, equal to, or greater than 
     *         the specified object
     */
    public int compareTo(ContentData data) {
        int category = data.getInt(CATEGORY) - getInt(CATEGORY);
        
        if (category != 0) {
            return category;
        } else {
            return getString(NAME).compareTo(data.getString(NAME));
        }
    }
}
