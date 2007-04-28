/*
 * DomainSize.java
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

package org.liquidsite.core.content;

import org.liquidsite.core.data.DomainSizeData;

/**
 * A domain size object.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DomainSize {

    /**
     * The domain size data object.
     */
    private DomainSizeData data;

    /**
     * The extra size value.
     */
    private long extraSize;

    /**
     * Creates a new domain size from a data object.
     *
     * @param data           the domain size data object
     */
    DomainSize(DomainSizeData data) {
        this(data, 0);
    }

    /**
     * Creates a new domain size from a data object.
     *
     * @param data           the domain size data object
     * @param extraSize      the extra size value
     */
    DomainSize(DomainSizeData data, long extraSize) {
        this.data = data;
        this.extraSize = extraSize;
    }

    /**
     * Returns the content category.
     *
     * @return the content category
     */
    public int getCategory() {
        return data.getInt(DomainSizeData.CATEGORY);
    }

    /**
     * Returns the category object count.
     *
     * @return the category object count
     */
    public int getCount() {
        return data.getInt(DomainSizeData.COUNT);
    }

    /**
     * Returns the category object size.
     *
     * @return the category object size
     */
    public long getSize() {
        return data.getLong(DomainSizeData.SIZE) + extraSize;
    }
}
