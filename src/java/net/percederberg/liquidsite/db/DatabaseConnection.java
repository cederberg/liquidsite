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
import java.sql.SQLException;
import java.util.Properties;

/**
 * A database connection. 
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DatabaseConnection {

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
    protected DatabaseConnection(String url, Properties properties) 
        throws DatabaseConnectionException {

        try {
            con = DriverManager.getConnection(url, properties);
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
    protected void setReserved(boolean reserved) {
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
