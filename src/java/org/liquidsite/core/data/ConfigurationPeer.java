/*
 * ConfigurationPeer.java
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
 * A configuration database peer. This class contains static methods
 * that handles all accesses to the LS_CONFIGURATION table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ConfigurationPeer extends AbstractPeer {

    /**
     * The configuration peer instance.
     */
    private static final ConfigurationPeer PEER = new ConfigurationPeer();

    /**
     * Returns a list of all configuration data object in the data
     * source.
     *
     * @param src            the data source to use
     *
     * @return a list of all configuration data objects
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectAll(DataSource src)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("config.select.all");

        return PEER.selectList(src, query);
    }

    /**
     * Inserts a new configuration data object into the data source.
     *
     * @param src            the data source to use
     * @param data           the configuration data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doInsert(DataSource src, ConfigurationData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("config.insert");

        query.addParameter(data.getString(ConfigurationData.NAME));
        query.addParameter(data.getString(ConfigurationData.VALUE));
        PEER.insert(src, query);
    }

    /**
     * Deletes all configuration data objects from the data source.
     *
     * @param src            the data source to use
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteAll(DataSource src)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("config.delete.all");

        PEER.delete(src, query);
    }

    /**
     * Creates a new configuration database peer.
     */
    private ConfigurationPeer() {
        super("configuration");
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new ConfigurationData();
    }
}
