/*
 * LockPeer.java
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

import java.util.Date;

import org.liquidsite.util.db.DatabaseQuery;

/**
 * A content lock database peer. This class contains static methods
 * that handles all accesses to the LS_LOCK table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class LockPeer extends AbstractPeer {

    /**
     * The lock peer instance.
     */
    private static final LockPeer PEER = new LockPeer();

    /**
     * Returns the lock object with the specified content id.
     *
     * @param src            the data source to use
     * @param content        the content id
     *
     * @return the lock found, or
     *         null if no matching lock existed
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static LockData doSelectByContent(DataSource src, int content)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("lock.select.content");

        query.addParameter(content);
        return (LockData) PEER.select(src, query);
    }

    /**
     * Inserts a new lock object into the data source.
     *
     * @param src            the data source to use
     * @param data           the lock data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doInsert(DataSource src, LockData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("lock.insert");

        query.addParameter(data.getString(LockData.DOMAIN));
        query.addParameter(data.getInt(LockData.CONTENT));
        query.addParameter(data.getString(LockData.USER));
        query.addParameter(data.getDate(LockData.ACQUIRED));
        PEER.insert(src, query);
    }

    /**
     * Deletes a lock object from the data source.
     *
     * @param src            the data source to use
     * @param data           the lock data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDelete(DataSource src, LockData data)
        throws DataObjectException {

        doDeleteContent(src, data.getInt(LockData.CONTENT));
    }

    /**
     * Deletes a lock object from the data source.
     *
     * @param src            the data source to use
     * @param content        the content object id
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteContent(DataSource src, int content)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("lock.delete");

        query.addParameter(content);
        PEER.delete(src, query);
    }

    /**
     * Deletes all lock objects in a specified domain from the
     * data source.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteDomain(DataSource src, String domain)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("lock.delete.domain");

        query.addParameter(domain);
        PEER.delete(src, query);
    }

    /**
     * Deletes all lock objects that are outdated. The outdated locks
     * are all the locks created more than 24 hours ago.
     *
     * @param src            the data source to use
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteOutdated(DataSource src)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("lock.delete.outdated");
        long           time = System.currentTimeMillis();

        query.addParameter(new Date(time - 24L * 60L * 60L * 1000L));
        PEER.delete(src, query);
    }

    /**
     * Creates a new content lock database peer.
     */
    private LockPeer() {
        super("content lock");
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new LockData();
    }
}
