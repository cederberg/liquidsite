/*
 * DomainAttributeData.java
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
 * Copyright (c) 2006 Per Cederberg. All rights reserved.
 */

package org.liquidsite.core.data;

/**
 * A domain attribute data object. This object encapsulates a row of
 * data from the LS_DOMAIN_ATTRIBUTE table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DomainAttributeData extends AbstractData {

    /**
     * The domain parameter.
     */
    public static final Parameter DOMAIN =
        new StringParameter(DomainAttributeData.class, "DOMAIN", "");

    /**
     * The name parameter.
     */
    public static final Parameter NAME =
        new StringParameter(DomainAttributeData.class, "NAME", "");

    /**
     * The data parameter.
     */
    public static final Parameter DATA =
        new StringParameter(DomainAttributeData.class, "DATA", "");

    /**
     * Creates a new domain attribute data object with default values.
     */
    public DomainAttributeData() {
        super();
    }
}
