/*
 * DatabaseConnection.java
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

package net.percederberg.liquidsite.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * A database connection. This class encapsulates a JDBC database
 * connection and holds some additional information needed by the
 * connection pool. When the database connection is no longer needed, 
 * it MUST be returned to the database connector so that the used
 * resources can be reused or freed.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 * 
 * @see DatabaseConnector
 */
public class DatabaseConnection {

    /**
     * The default query timeout in seconds. No queries are allowed
     * to run longer than this timout value.
     */
    public static final int DEFAULT_QUERY_TIMEOUT = 5;

    /**
     * The JDBC database connection.
     */
    private Connection con;

    /**
     * The valid connection flag. This flag is set to false if an 
     * error is encountered while executing SQL statements on the 
     * connection. After this flag has been set, the connection 
     * shouldn't be used.
     */
    private boolean valid = true;

    /**
     * The reserved connection flag. This flag is set to true before
     * the connection is being used. 
     */
    private boolean reserved = false;

    /**
     * The connection creation time.
     */
    private long creationTime = System.currentTimeMillis(); 

    /**
     * The connection expiration timeout in milliseconds. If this 
     * value is negative the database connection will never expire.
     */
    private long timeout = -1;

    /**
     * Creates a new database connection. The database JDBC driver 
     * must have been previously loaded, or a connection exception
     * will be thrown.
     * 
     * @param url            the JDBC url to use
     * @param properties     the JDBC properties to use
     * 
     * @throws DatabaseConnectionException if the database connection 
     *             couldn't be created
     */
    DatabaseConnection(String url, Properties properties) 
        throws DatabaseConnectionException {

        try {
            con = DriverManager.getConnection(url, properties);
            con.setAutoCommit(true);
            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }
    
    /**
     * Checks if the connection is valid. A connection is valid until
     * an error is encountered while executing some SQL statement. 
     * If the connection is not valid, the connection shouldn't be 
     * used. 
     * 
     * @return true if the connection is valid, or
     *         false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Checks if this connection is reserved. A connection is 
     * reserved when it is being used.
     * 
     * @return true if this connection is reserved, or
     *         false otherwise
     */
    public boolean isReserved() {
        return reserved;
    }
    
    /**
     * Sets the connection reserved flag.
     * 
     * @param reserved       the new value of the reserved flag
     */
    void setReserved(boolean reserved) {
        this.reserved = reserved;
    }
    
    /**
     * Checks if this connection has expired. A connection expires 
     * when the connection age is more than the timeout.
     * 
     * @return true if the connection has expired, or
     *         false otherwise
     */
    public boolean isExpired() {
        return timeout >= 0 && 
               timeout < (System.currentTimeMillis() - creationTime);
    }

    /**
     * Sets the connection timeout value. By default a connection has
     * no timeout.
     * 
     * @param timeout        the new connection timeout (in millisec)
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Executes an SQL query or statement. 
     * 
     * @param sql            the SQL query or statement to execute
     * 
     * @return the database results
     * 
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     */
    public DatabaseResults execute(String sql) 
        throws DatabaseException {

        Statement        stmt;
        ResultSet        set;
        DatabaseResults  res;

        // Execute query
        try {
            stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                       ResultSet.CONCUR_READ_ONLY,
                                       ResultSet.CLOSE_CURSORS_AT_COMMIT);
            stmt.setQueryTimeout(DEFAULT_QUERY_TIMEOUT);
            set = stmt.executeQuery(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }

        // Extract results
        try {
            res = new DatabaseResults(set);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            try {
                set.close();
            } catch (SQLException ignore) {
                // Do nothing
            }
        }

        return res;
    }

    /**
     * Closes the connection.
     */
    public void close() {
        valid = false;
        try {
            if (!con.isClosed()) {
                con.close();
            }
        } catch (SQLException ignore) {
            // Ignore this error
        }
    }
}
