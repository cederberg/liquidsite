/*
 * DomainSizePeer.java
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

import java.util.ArrayList;

import org.liquidsite.util.db.DatabaseQuery;

/**
 * A domain size database peer. This class contains static methods that
 * handles all accesses to the domain size query.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DomainSizePeer extends AbstractPeer {

    /**
     * The domain size peer instance.
     */
    private static final DomainSizePeer PEER = new DomainSizePeer();

    /**
     * Calculates the aggregate size in the database of all attributes
     * in a domain.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     *
     * @return the size in bytes of all the attributes in the domain
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByDomain(DataSource src, String domain)
        throws DataObjectException {

        DatabaseQuery  query;

        query = new DatabaseQuery("query.domainsize");
        query.addParameter(domain);
        return PEER.selectList(src, query);
    }

    /**
     * Creates a new domain size database peer.
     */
    private DomainSizePeer() {
        super("domain size");
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new DomainSizeData();
    }
}
