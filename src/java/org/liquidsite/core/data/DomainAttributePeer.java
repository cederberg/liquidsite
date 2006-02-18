/*
 * DomainAttributePeer.java
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
 * A domain attribute database peer. This class contains static
 * methods that handles all accesses to the LS_DOMAIN_ATTRIBUTE
 * table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DomainAttributePeer extends AbstractPeer {

    /**
     * The domain attribute peer instance.
     */
    private static final DomainAttributePeer PEER = new DomainAttributePeer();

    /**
     * Returns a list of all domain attributes in a specified domain.
     *
     * @param src            the data source to use
     * @param domain         the domain name
    *
     * @return a list of all domain attributes in the domain
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByDomain(DataSource src, String domain)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("domainattribute.select");

        query.addParameter(domain);
        return PEER.selectList(src, query);
    }

    /**
     * Inserts a new domain attribute into the data source.
     *
     * @param src            the data source to use
     * @param data           the domain attribute data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doInsert(DataSource src, DomainAttributeData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("domainattribute.insert");

        query.addParameter(data.getString(DomainAttributeData.DOMAIN));
        query.addParameter(data.getString(DomainAttributeData.NAME));
        query.addParameter(data.getString(DomainAttributeData.DATA));
        PEER.insert(src, query);
    }

    /**
     * Deletes all domain attributes in a domain from the data source.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteDomain(DataSource src, String domain)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("domainattribute.delete");

        query.addParameter(domain);
        PEER.delete(src, query);
    }

    /**
     * Creates a new domain attribute database peer.
     */
    private DomainAttributePeer() {
        super("domain attribute");
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new DomainAttributeData();
    }
}
