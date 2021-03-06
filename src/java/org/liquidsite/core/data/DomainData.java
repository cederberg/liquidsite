/*
 * DomainData.java
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
 * Copyright (c) 2004-2006 Per Cederberg. All rights reserved.
 */

package org.liquidsite.core.data;

import java.util.Date;

/**
 * A domain data object. This object encapsulates a row of data from
 * the LS_DOMAIN table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DomainData extends AbstractData {

    /**
     * The name parameter.
     */
    public static final Parameter NAME =
        new StringParameter(DomainData.class, "NAME", "");

    /**
     * The description parameter.
     */
    public static final Parameter DESCRIPTION =
        new StringParameter(DomainData.class, "DESCRIPTION", "");

    /**
     * The created parameter.
     */
    public static final Parameter CREATED =
        new DateParameter(DomainData.class, "CREATED", new Date(0));

    /**
     * The modified parameter.
     */
    public static final Parameter MODIFIED =
        new DateParameter(DomainData.class, "MODIFIED", new Date(0));

    /**
     * Creates a new domain data object with default values.
     */
    public DomainData() {
        super();
    }
}
