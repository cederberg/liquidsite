/*
 * DataSource.java
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

import org.liquidsite.util.db.DatabaseConnection;
import org.liquidsite.util.db.DatabaseConnectionException;
import org.liquidsite.util.db.DatabaseConnector;
import org.liquidsite.util.log.Log;

/**
 * A data source. This object encapsulates a database connection that
 * is used during for a set of data operations.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DataSource {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DataSource.class);

    /**
     * The database connector to use.
     */
    private DatabaseConnector database = null;

    /**
     * The database connection currently in use.
     */
    private DatabaseConnection connection = null;

    /**
     * Creates a new data source. The specified database connector
     * will be used to get and return a database connection.
     *
     * @param database       the database connector to use
     */
    public DataSource(DatabaseConnector database) {
        this.database = database;
    }

    /**
     * Creates a new data source. The specified database connection
     * will be used continously. The open and close operation will
     * not have any effect for data sources created in this way.
     *
     * @param connection     the database connection to use
     */
    public DataSource(DatabaseConnection connection) {
        this.connection = connection;
    }

    /**
     * Checks if this object has an open connection to its data
     * source.
     *
     * @return true if a connection is already open, or
     *         false otherwise
     */
    public boolean isOpen() {
        return connection != null;
    }

    /**
     * Opens a connection to the data source. Normally this method
     * only retrieves an already established connection from the
     * database connection pool (for efficiency). This method is
     * implicitly called when using the data source for retrieving
     * or storing data. Note that the corresponding close() method
     * should be called after finishing the data operations to free
     * unneeded resources.
     *
     * @throws DataObjectException if no data source connection could
     *             be established
     *
     * @see #close()
     */
    public void open() throws DataObjectException {
        if (connection == null) {
            try {
                connection = database.getConnection();
            } catch (DatabaseConnectionException e) {
                LOG.error(e.getMessage());
                throw new DataObjectException(e);
            }
        }
    }

    /**
     * Closes the connection to the data source. Normally this method
     * doesn't really close the physical connection, but rather
     * returns it to the database connection pool. This method can be
     * called without adverse effects and should be called whenever
     * the data operations have finished. The data source can be
     * opened and closed any number of times.
     *
     * @see #open()
     */
    public void close() {
        if (database != null && connection != null) {
            database.returnConnection(connection);
            connection = null;
        }
    }

    /**
     * Returns the database connection. If no previous connection
     * exists, a new one will be created.
     *
     * @return a database connection
     *
     * @throws DataObjectException if no database connection could be
     *             established
     */
    protected DatabaseConnection getConnection()
        throws DataObjectException {

        if (connection == null) {
            open();
        }
        return connection;
    }

    /**
     * Calls the close() method to free resources.
     *
     * @see #close()
     */
    protected void finalize() {
        close();
    }
}
