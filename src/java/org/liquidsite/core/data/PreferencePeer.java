/*
 * PreferencePeer.java
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
 * A user preference database peer. This class contains static methods
 * that handles all accesses to the LS_PREFERENCE table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PreferencePeer extends AbstractPeer {

    /**
     * The preference peer instance.
     */
    private static final PreferencePeer PEER = new PreferencePeer();

    /**
     * Returns a list of preferences for a specified user.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param user           the user name
     *
     * @return a list of preferences for the user
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByUser(DataSource src,
                                           String domain,
                                           String user)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("preference.select.user");

        query.addParameter(domain);
        query.addParameter(user);
        return PEER.selectList(src, query);
    }

    /**
     * Inserts a new preference into the data source.
     *
     * @param src            the data source to use
     * @param data           the preference data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doInsert(DataSource src, PreferenceData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("preference.insert");

        query.addParameter(data.getString(PreferenceData.DOMAIN));
        query.addParameter(data.getString(PreferenceData.USER));
        query.addParameter(data.getString(PreferenceData.NAME));
        query.addParameter(data.getString(PreferenceData.VALUE));
        PEER.insert(src, query);
    }

    /**
     * Deletes all preferences in a domain from the data source.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteDomain(DataSource src, String domain)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("preference.delete.domain");

        query.addParameter(domain);
        PEER.delete(src, query);
    }

    /**
     * Deletes all preferences for a user from the data source.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param user           the user name
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteUser(DataSource src,
                                    String domain,
                                    String user)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("preference.delete.user");

        query.addParameter(domain);
        query.addParameter(user);
        PEER.delete(src, query);
    }

    /**
     * Creates a new preference database peer.
     */
    private PreferencePeer() {
        super("preference");
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new PreferenceData();
    }
}
