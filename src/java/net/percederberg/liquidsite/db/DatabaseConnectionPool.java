/*
 * DatabaseConnectionPool.java
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

package net.percederberg.liquidsite.db;

import java.util.ArrayList;

import org.liquidsite.util.log.Log;

/**
 * A database connection pool.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DatabaseConnectionPool {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DatabaseConnectionPool.class);

    /**
     * The database connector.
     */
    private DatabaseConnector db;

    /**
     * The minimum pool size.
     */
    private int minSize = 0;

    /**
     * The maximum pool size. If this value is negative, the pool has
     * an infinite size.
     */
    private int maxSize = -1;

    /**
     * The list of connections in the pool.
     */
    private ArrayList connections = new ArrayList();

    /**
     * Creates a new database connection pool. The JDBC driver should
     * have been loaded prior to calling this constructor, as no
     * database connections can be created otherwise.
     *
     * @param db             the database connector to use
     */
    public DatabaseConnectionPool(DatabaseConnector db) {
        this.db = db;
        LOG.info("created connection pool for " + db);
    }

    /**
     * Returns the current connection pool size. This method is
     * synchronized to guarantee that no concurrent operation is
     * being made to the connection list.
     *
     * @return the current connection pool size
     */
    public synchronized int getCurrentSize() {
        return connections.size();
    }

    /**
     * Returns the minium connection pool size. By default the
     * connection pool minimum size is zero (0).
     *
     * @return the minimum connection pool size
     *
     * @see #setMinimumSize
     */
    public int getMinimumSize() {
        return minSize;
    }

    /**
     * Sets the minimum connection pool size. This method will not
     * create new database connections, but only register the new
     * minimum count.
     *
     * @param size           the new minimum pool size
     *
     * @see #getMinimumSize
     * @see #update
     */
    public void setMinimumSize(int size) {
        LOG.info("new connection pool min size: " + size +
                 ", was: " + minSize + ", for " + db);
        this.minSize = size;
    }

    /**
     * Returns the maximum connection pool size. By default the
     * connection pool has no maximum size.
     *
     * @return the maximum connection pool size, or
     *         a negative value for unlimited
     *
     * @see #setMaximumSize
     */
    public int getMaximumSize() {
        return maxSize;
    }

    /**
     * Sets the maximum connection pool size. This method will not
     * close any previously open database connections, but only
     * register the new maximum count.
     *
     * @param size           the new maximum pool size, or
     *                       a negative value for unlimited
     *
     * @see #getMaximumSize
     * @see #update
     */
    public void setMaximumSize(int size) {
        LOG.info("new connection pool max size: " + size +
                 ", was: " + maxSize + ", for " + db);
        this.maxSize = size;
    }

    /**
     * Returns a database connection from the pool. If there is none
     * available, a new connection will be created.
     *
     * @return the database connection
     *
     * @throws DatabaseConnectionException if a new database
     *             connection couldn't be created
     *
     * @see #returnConnection
     */
    public DatabaseConnection getConnection()
        throws DatabaseConnectionException {

        DatabaseConnection  con;

        LOG.trace("getting pooled connection for " + db + "...");
        try {
            con = checkOut();
            if (con == null) {
                con = create();
            }
        } catch (DatabaseConnectionException e) {
            LOG.warning("failed getting pooled connection for " + db, e);
            throw e;
        }
        LOG.trace("got pooled connection");
        return con;
    }

    /**
     * Returns a database connection to the pool. If the connection
     * isn't already in the pool, nothing happens.
     *
     * @param con            the database connection
     *
     * @see #getConnection
     */
    public void returnConnection(DatabaseConnection con) {
        LOG.trace("returning pooled connection for " + db + "...");
        checkIn(con);
        LOG.trace("returned pooled connection");
    }

    /**
     * Updates the connection pool. This method will step through all
     * available database connections in the pool, removing all
     * broken or timed out connections. The connection pool size may
     * also be adjusted to fit in between the minimum and maximum
     * sizes.
     *
     * Note that any call to this method should be made from a
     * background thread, as this method may get stuck waiting for
     * I/O timeouts.
     *
     * @throws DatabaseConnectionException if new database
     *             connections couldn't be created
     */
    public void update() throws DatabaseConnectionException {
        ArrayList           list = new ArrayList();
        DatabaseConnection  con;
        int                 i;

        // Find invalid or old connections
        LOG.info("closing old connections in pool for " + db + "...");
        synchronized (this) {
            for (i = 0; i < connections.size(); i++) {
                con = (DatabaseConnection) connections.get(i);
                if (con.isReserved()) {
                    // Do nothing
                } else if (!con.isValid() || con.isExpired()) {
                    con.setReserved(true);
                    list.add(con);
                }
            }
        }

        // Destroy invalid connections
        for (i = 0; i < list.size(); i++) {
            con = (DatabaseConnection) list.get(i);
            destroy(con);
        }
        LOG.info("closed old connections in pool, count: " + list.size());

        // Create minimum number of connections
        LOG.info("creating new connections in pool for " + db);
        for (i = 0; getCurrentSize() < minSize; i++) {
            try {
                con = create();
            } catch (DatabaseConnectionException e) {
                LOG.warning("failed creating new connections in pool for " +
                            db,
                            e);
                throw e;
            }
            checkIn(con);
        }
        LOG.info("created new connections in pool, count: " + i);
    }

    /**
     * Creates a new database connection. The connection will be
     * reserved and added to the connection pool. This method also
     * checks for the maximum size of the connection pool.
     *
     * @return a new, reserved and pooled connection
     *
     * @throws DatabaseConnectionException if a new database
     *             connection couldn't be created
     */
    private DatabaseConnection create()
        throws DatabaseConnectionException {

        DatabaseConnection  con;
        String              msg;

        if (maxSize > 0 && getCurrentSize() >= maxSize) {
            msg = "cannot create new database connection, " +
                  "pool size maximum of " + maxSize +
                  " already reached";
            LOG.warning(msg);
            throw new DatabaseConnectionException(msg);
        }
        con = new DatabaseConnection(db);
        con.setReserved(true);
        add(con);

        return con;
    }

    /**
     * Destroys a database connection. The connection will be removed
     * from the pool and closed.
     *
     * @param con            the database connection
     */
    private void destroy(DatabaseConnection con) {
        remove(con);
        con.close();
    }

    /**
     * Checks out a connection from the pool. This method will return
     * a connection from the pool that is currently unused.
     *
     * @return the first unused connection in the pool, or
     *         null if no connection found
     *
     * @throws DatabaseConnectionException if the database connection
     *             couldn't be reestablished
     */
    private DatabaseConnection checkOut()
        throws DatabaseConnectionException {

        return checkOut(0);
    }

    /**
     * Checks out a connection from the pool. This method will return
     * a connection from the pool that is currently unused. It is
     * synchronized to guarantee that no concurrent operation is
     * being made to the connection list.
     *
     * @param start          the starting position in the list
     *
     * @return the first unused connection in the pool, or
     *         null if no connection found
     *
     * @throws DatabaseConnectionException if the database connection
     *             couldn't be reestablished
     */
    private synchronized DatabaseConnection checkOut(int start)
        throws DatabaseConnectionException {

        DatabaseConnection  con;

        for (int i = start; i < connections.size(); i++) {
            con = (DatabaseConnection) connections.get(i);
            if (!con.isReserved() && con.isValid() && !con.isExpired()) {
                con.setReserved(true);
                con.reset();
                return con;
            }
        }
        return null;
    }

    /**
     * Checks in a connection to the pool. This method will mark the
     * connection as unused if it already exists in the pool. It is
     * synchronized to guarantee that no concurrent operation is
     * being made to the connection list.
     *
     * @param con            the connection to check in
     */
    private synchronized void checkIn(DatabaseConnection con) {
        if (connections.contains(con)) {
            con.setReserved(false);
        } else {
            con.close();
        }
    }

    /**
     * Adds a new connection to the pool. This method is synchronized
     * to guarantee that no concurrent operation is being made to the
     * connection list.
     *
     * @param con            the connection to add
     */
    private synchronized void add(DatabaseConnection con) {
        if (!connections.contains(con)) {
            connections.add(con);
            LOG.info("added connection to pool for " + db +
                     ", new size: " + connections.size());
        }
    }

    /**
     * Removes a connection from the pool. Note that this will not
     * close the connection itself, only remove it from the pool.
     * This method is synchronized to guarantee that no concurrent
     * operation is being made to the connection list.
     *
     * @param con            the connection to add
     */
    private synchronized void remove(DatabaseConnection con) {
        connections.remove(con);
        LOG.info("removed connection from pool for " + db +
                 ", new size: " + connections.size());
    }
}
