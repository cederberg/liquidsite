/*
 * DomainPeer.java
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

import java.util.ArrayList;

import org.liquidsite.util.db.DatabaseQuery;

/**
 * A domain database peer. This class contains static methods that
 * handles all accesses to the LS_DOMAIN table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public final class DomainPeer extends AbstractPeer {

    /**
     * The domain peer instance.
     */
    private static final DomainPeer PEER = new DomainPeer();

    /**
     * Returns a list of all domains in the data source.
     *
     * @param src            the data source to use
     *
     * @return a list of all domains in the database
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectAll(DataSource src)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("domain.select.all");

        return PEER.selectList(src, query);
    }

    /**
     * Returns a domain with a specified name.
     *
     * @param src            the data source to use
     * @param name           the domain name
     *
     * @return the domain found, or
     *         null if no matching domain existed
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static DomainData doSelectByName(DataSource src, String name)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("domain.select.name");

        query.addParameter(name);
        return (DomainData) PEER.select(src, query);
    }

    /**
     * Inserts a new domain into the data source.
     *
     * @param src            the data source to use
     * @param data           the domain data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doInsert(DataSource src, DomainData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("domain.insert");

        query.addParameter(data.getString(DomainData.NAME));
        query.addParameter(data.getString(DomainData.DESCRIPTION));
        query.addParameter(data.getDate(DomainData.CREATED));
        query.addParameter(data.getDate(DomainData.MODIFIED));
        PEER.insert(src, query);
    }

    /**
     * Updates a domain in the data source.
     *
     * @param src            the data source to use
     * @param data           the domain data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doUpdate(DataSource src, DomainData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("domain.update");

        query.addParameter(data.getString(DomainData.DESCRIPTION));
        query.addParameter(data.getDate(DomainData.MODIFIED));
        query.addParameter(data.getString(DomainData.NAME));
        PEER.update(src, query);
    }

    /**
     * Deletes a domain from the data source.
     *
     * @param src            the data source to use
     * @param data           the domain data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDelete(DataSource src, DomainData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("domain.delete");
        String         domain = data.getString(DomainData.NAME);

        query.addParameter(domain);
        PEER.delete(src, query);
        DomainAttributePeer.doDeleteDomain(src, domain);
        UserPeer.doDeleteDomain(src, domain);
        GroupPeer.doDeleteDomain(src, domain);
        ContentPeer.doDeleteDomain(src, domain);
    }

    /**
     * Creates a new domain database peer.
     */
    private DomainPeer() {
        super("domain");
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new DomainData();
    }
}
