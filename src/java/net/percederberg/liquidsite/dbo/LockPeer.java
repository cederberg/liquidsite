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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.dbo;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseQuery;

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
     * @param content        the content id
     * @param con            the database connection to use
     * 
     * @return the lock found, or
     *         null if no matching lock existed
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static LockData doSelectByContent(int content, 
                                             DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("lock.select.content");
        
        query.addParameter(content);
        return (LockData) PEER.select(query, con);
    }

    /**
     * Inserts a new lock object into the database.
     * 
     * @param data           the lock data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doInsert(LockData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("lock.insert");

        query.addParameter(data.getString(LockData.DOMAIN));
        query.addParameter(data.getInt(LockData.CONTENT));
        query.addParameter(data.getString(LockData.USER));
        query.addParameter(data.getDate(LockData.ACQUIRED));
        PEER.insert(query, con);
    }
    
    /**
     * Deletes a lock object from the database.
     * 
     * @param data           the lock data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doDelete(LockData data, DatabaseConnection con)
        throws DatabaseObjectException {

        doDeleteContent(data.getInt(LockData.CONTENT), con);
    }
    
    /**
     * Deletes a lock object from the database.
     * 
     * @param content        the content object id
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doDeleteContent(int content, 
                                       DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("lock.delete");

        query.addParameter(content);
        PEER.delete(query, con);
    }
    
    /**
     * Deletes all lock objects in a specified domain from the 
     * database.
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

        DatabaseQuery  query = new DatabaseQuery("lock.delete.domain");

        query.addParameter(domain);
        PEER.delete(query, con);
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
