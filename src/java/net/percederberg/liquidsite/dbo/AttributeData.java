/*
 * AttributeData.java
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
 * An attribute data object. This object encapsulates a row of data 
 * from the LS_ATTRIBUTE table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class AttributeData extends AbstractData {

    /**
     * The domain parameter.
     */
    public static final Parameter DOMAIN = 
        new StringParameter(AttributeData.class, "DOMAIN", "");

    /**
     * The content parameter.
     */
    public static final Parameter CONTENT = 
        new IntegerParameter(AttributeData.class, "CONTENT", 0);

    /**
     * The revision parameter.
     */
    public static final Parameter REVISION = 
        new IntegerParameter(AttributeData.class, "REVISION", 0);

    /**
     * The category parameter.
     */
    public static final Parameter CATEGORY = 
        new IntegerParameter(AttributeData.class, "CATEGORY", 0);

    /**
     * The name parameter.
     */
    public static final Parameter NAME = 
        new StringParameter(AttributeData.class, "NAME", "");

    /**
     * The data parameter.
     */
    public static final Parameter DATA = 
        new StringParameter(AttributeData.class, "DATA", "");
    
    /**
     * The searchable parameter.
     */
    public static final Parameter SEARCHABLE = 
        new BooleanParameter(AttributeData.class, "SEARCHABLE", false);
    
    /**
     * Creates a new attribute data object with default values.
     */
    public AttributeData() {
        super();
    }
}
