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

package net.percederberg.liquidsite.content;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseQuery;
import net.percederberg.liquidsite.db.DatabaseResults;

/**
 * A content lock database peer. This class contains static methods
 * that handles all accesses to the database representation of a 
 * content lock.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class LockPeer extends Peer {

    /**
     * The lock peer instance.
     */
    private static final Peer PEER = new LockPeer();

    /**
     * Returns the lock object with the specified content id.
     * 
     * @param id             the content id
     * 
     * @return the lock found, or
     *         null if no matching lock existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Lock doSelectById(int id) throws ContentException {
        return doSelectById(id, null);
    }

    /**
     * Returns the lock object with the specified content id.
     * 
     * @param id             the content id
     * @param con            the database connection to use
     * 
     * @return the lock found, or
     *         null if no matching lock existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Lock doSelectById(int id, DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("lock.select.id");
        DatabaseResults  res;
        
        query.addParameter(id);
        res = execute("reading lock", query, con);
        return (Lock) PEER.createObject(res);
    }

    /**
     * Inserts a new lock object into the database.
     * 
     * @param lock           the lock to insert
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(Lock lock) throws ContentException {
        doInsert(lock, null);
    }

    /**
     * Inserts a new lock object into the database.
     * 
     * @param lock           the lock to insert
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(Lock lock, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("lock.insert");

        lock.validate();
        query.addParameter(lock.getDomainName());
        query.addParameter(lock.getContentId());
        query.addParameter(lock.getUserName());
        query.addParameter(lock.getAcquired());
        execute("inserting lock", query, con);
        lock.setModified(false);
        lock.setPersistent(true);
    }
    
    /**
     * Deletes a lock object from the database.
     * 
     * @param lock           the lock to delete
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Lock lock)
        throws ContentException {

        doDelete(lock, null);
    }

    /**
     * Deletes a lock object from the database.
     * 
     * @param lock           the lock to delete
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Lock lock, DatabaseConnection con)
        throws ContentException {

        doDelete(lock.getContentId(), con);
        lock.setModified(true);
        lock.setPersistent(false);
    }
    
    /**
     * Deletes a lock object from the database.
     * 
     * @param content        the content object id
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(int content)
        throws ContentException {

        doDelete(content, null);
    }

    /**
     * Deletes a lock object from the database.
     * 
     * @param content        the content object id
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(int content, DatabaseConnection con)
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("lock.delete");

        query.addParameter(content);
        execute("deleting lock", query, con);
    }
    
    /**
     * Creates a new lock database peer.
     */
    private LockPeer() {
        super("lock", Lock.class);
    }

    /**
     * Transfers a database result row to a data object.
     * 
     * @param row            the database result row
     * @param obj            the data object
     * 
     * @throws DatabaseDataException if the database results didn't
     *             contain the expected column names
     */
    protected void transfer(DatabaseResults.Row row, DataObject obj) 
        throws DatabaseDataException {

        Lock  lock = (Lock) obj;

        lock.setDomainName(row.getString("DOMAIN"));
        lock.setContentId(row.getInt("CONTENT"));
        lock.setUserName(row.getString("USER"));
        lock.setAcquired(row.getDate("ACQUIRED"));
        lock.setModified(false);
        lock.setPersistent(true);
    }
}
