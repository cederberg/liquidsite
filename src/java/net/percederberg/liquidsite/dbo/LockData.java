/*
 * LockData.java
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
 * A lock data object. This object encapsulates a row of data from 
 * the LS_LOCK table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class LockData extends AbstractData {

    /**
     * The domain parameter.
     */
    public static final Parameter DOMAIN = 
        new StringParameter(LockData.class, "DOMAIN", "");

    /**
     * The content parameter.
     */
    public static final Parameter CONTENT = 
        new IntegerParameter(LockData.class, "CONTENT", 0);

    /**
     * The user parameter.
     */
    public static final Parameter USER = 
        new StringParameter(LockData.class, "USER", "");

    /**
     * The acquired parameter.
     */
    public static final Parameter ACQUIRED = 
        new DateParameter(LockData.class, "ACQUIRED", new Date(0));
    
    /**
     * Creates a new lock data object with default values.
     */
    public LockData() {
        super();
    }
}
