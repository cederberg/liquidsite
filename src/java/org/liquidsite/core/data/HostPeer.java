/*
 * HostPeer.java
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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package org.liquidsite.core.data;

import java.util.ArrayList;

import org.liquidsite.util.db.DatabaseQuery;

/**
 * A host database peer. This class contains static methods that
 * handles all accesses to the LS_HOST table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class HostPeer extends AbstractPeer {

    /**
     * The host peer instance.
     */
    private static final HostPeer PEER = new HostPeer();

    /**
     * Returns a list of all hosts in the data source.
     *
     * @param src            the data source to use
     *
     * @return a list of all hosts in the database
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectAll(DataSource src)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.select.all");

        return PEER.selectList(src, query);
    }

    /**
     * Returns a list of all hosts in a certain domain.
     *
     * @param src            the data source to use
     * @param domain         the domain name
    *
     * @return a list of all hosts in the domain
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByDomain(DataSource src, String domain)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.select.domain");

        query.addParameter(domain);
        return PEER.selectList(src, query);
    }

    /**
     * Returns a host with a specified name.
     *
     * @param src            the data source to use
     * @param name           the host name
     *
     * @return the host found, or
     *         null if no matching host existed
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static HostData doSelectByName(DataSource src, String name)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.select.name");

        query.addParameter(name);
        return (HostData) PEER.select(src, query);
    }

    /**
     * Inserts a new host into the data source.
     *
     * @param src            the data source to use
     * @param data           the host data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doInsert(DataSource src, HostData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.insert");

        query.addParameter(data.getString(HostData.DOMAIN));
        query.addParameter(data.getString(HostData.NAME));
        query.addParameter(data.getString(HostData.DESCRIPTION));
        query.addParameter(data.getString(HostData.OPTIONS));
        PEER.insert(src, query);
    }

    /**
     * Updates a host in the data source.
     *
     * @param src            the data source to use
     * @param data           the host data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doUpdate(DataSource src, HostData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.update");

        query.addParameter(data.getString(HostData.DESCRIPTION));
        query.addParameter(data.getString(HostData.OPTIONS));
        query.addParameter(data.getString(HostData.NAME));
        PEER.update(src, query);
    }

    /**
     * Deletes a host from the data source.
     *
     * @param src            the data source to use
     * @param data           the host data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDelete(DataSource src, HostData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.delete");

        query.addParameter(data.getString(HostData.NAME));
        PEER.delete(src, query);
    }

    /**
     * Deletes all hosts in a domain from the data source.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteDomain(DataSource src, String domain)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("host.delete.domain");

        query.addParameter(domain);
        PEER.delete(src, query);
    }

    /**
     * Creates a new host database peer.
     */
    private HostPeer() {
        super("host");
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new HostData();
    }
}
