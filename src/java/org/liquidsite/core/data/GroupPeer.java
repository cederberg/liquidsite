/*
 * GroupPeer.java
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
 * A group database peer. This class contains static methods that
 * handles all accesses to the LS_GROUP table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class GroupPeer extends AbstractPeer {

    /**
     * The group peer instance.
     */
    private static final GroupPeer PEER = new GroupPeer();

    /**
     * Returns a list of matching groups in a specified domain. Only
     * groups with matching names will be returned.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param filter         the search filter (empty for all)
     *
     * @return a list of matching groups in the domain
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByDomain(DataSource src,
                                             String domain,
                                             String filter)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("group.select.domain");
        String         filterSql = "%" + filter + "%";

        query.addParameter(domain);
        query.addParameter(filterSql);
        query.addParameter(filterSql);
        return PEER.selectList(src, query);
    }

    /**
     * Returns a group with a specified name.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param name           the group name
     *
     * @return the group found, or
     *         null if no matching group existed
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static GroupData doSelectByName(DataSource src,
                                           String domain,
                                           String name)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("group.select.name");

        query.addParameter(domain);
        query.addParameter(name);
        return (GroupData) PEER.select(src, query);
    }

    /**
     * Inserts a new group into the data source.
     *
     * @param src            the data source to use
     * @param data           the group data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doInsert(DataSource src, GroupData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("group.insert");

        query.addParameter(data.getString(GroupData.DOMAIN));
        query.addParameter(data.getString(GroupData.NAME));
        query.addParameter(data.getString(GroupData.DESCRIPTION));
        query.addParameter(data.getString(GroupData.COMMENT));
        PEER.insert(src, query);
    }

    /**
     * Updates a group in the data source.
     *
     * @param src            the data source to use
     * @param data           the group data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doUpdate(DataSource src, GroupData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("group.update");

        query.addParameter(data.getString(GroupData.DESCRIPTION));
        query.addParameter(data.getString(GroupData.COMMENT));
        query.addParameter(data.getString(GroupData.DOMAIN));
        query.addParameter(data.getString(GroupData.NAME));
        PEER.update(src, query);
    }

    /**
     * Deletes a group from the data source. This method also deletes
     * all related user group and permission entries.
     *
     * @param src            the data source to use
     * @param data           the group data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDelete(DataSource src, GroupData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("group.delete");
        String         domain = data.getString(GroupData.DOMAIN);
        String         name = data.getString(GroupData.NAME);

        query.addParameter(domain);
        query.addParameter(name);
        PEER.delete(src, query);
        UserGroupPeer.doDeleteGroup(src, domain, name);
        PermissionPeer.doDeleteGroup(src, domain, name);
    }

    /**
     * Deletes all groups in a domain from the data source. This
     * method also deletes all user group entries in the domain.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteDomain(DataSource src, String domain)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("group.delete.domain");

        query.addParameter(domain);
        PEER.delete(src, query);
        UserGroupPeer.doDeleteDomain(src, domain);
    }

    /**
     * Creates a new group database peer.
     */
    private GroupPeer() {
        super("group");
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new GroupData();
    }
}
