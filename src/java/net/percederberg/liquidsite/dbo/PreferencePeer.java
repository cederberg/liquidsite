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

package net.percederberg.liquidsite.dbo;

import java.util.ArrayList;

import org.liquidsite.util.db.DatabaseConnection;
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
     * @param domain         the domain name
     * @param user           the user name
     * @param con            the database connection to use
     *
     * @return a list of preferences for the user
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByUser(String domain,
                                           String user,
                                           DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("preference.select.user");

        query.addParameter(domain);
        query.addParameter(user);
        return PEER.selectList(query, con);
    }

    /**
     * Inserts a new preference into the database.
     *
     * @param data           the preference data object
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doInsert(PreferenceData data,
                                DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("preference.insert");

        query.addParameter(data.getString(PreferenceData.DOMAIN));
        query.addParameter(data.getString(PreferenceData.USER));
        query.addParameter(data.getString(PreferenceData.NAME));
        query.addParameter(data.getString(PreferenceData.VALUE));
        PEER.insert(query, con);
    }

    /**
     * Deletes all preferences in a domain from the database.
     *
     * @param domain         the domain name
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doDeleteDomain(String domain,
                                      DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("preference.delete.domain");

        query.addParameter(domain);
        PEER.delete(query, con);
    }

    /**
     * Deletes all preferences for a user from the database.
     *
     * @param domain         the domain name
     * @param user           the user name
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doDeleteUser(String domain,
                                    String user,
                                    DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("preference.delete.user");

        query.addParameter(domain);
        query.addParameter(user);
        PEER.delete(query, con);
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
