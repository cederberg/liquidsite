/*
 * DomainHost.java
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

package org.liquidsite.core.content;

/**
 * A domain web site host.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DomainHost implements Comparable {

    /**
     * The host name.
     */
    private String name;

    /**
     * The host description.
     */
    private String description;

    /**
     * Creates a new domain host.
     *
     * @param name           the host name
     * @param description    the host description
     */
    DomainHost(String name, String description) {
        this.name = name;
        this.description = description;
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
     * @throws ClassCastException if the object isn't a DomainHost object
     */
    public int compareTo(Object obj) throws ClassCastException {
        return name.compareTo(((DomainHost) obj).name);
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return getName();
    }

    /**
     * Returns the unique host name.
     *
     * @return the unique host name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the host description.
     *
     * @return the host description
     */
    public String getDescription() {
        return description;
    }
}
