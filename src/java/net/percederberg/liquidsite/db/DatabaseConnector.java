/*
 * DatabaseConnector.java
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

import java.util.Properties;

import net.percederberg.liquidsite.Log;

/**
 * A database connector. 
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DatabaseConnector {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DatabaseConnector.class);

    /**
     * The default minimum connection pool size (0).
     */
    private static final int DEFAULT_POOL_MINSIZE = 1;
    
    /**
     * The default maximum connection pool size (10).
     */
    private static final int DEFAULT_POOL_MAXSIZE = 10;

    /**
     * The default connection pool timeout (4 h = 14400000 ms).
     */
    private static final long DEFAULT_POOL_TIMEOUT = 14400000L;

    /**
     * The JDBC database URL.
     */
    private String url;

    /**
     * The JDBC database properties.
     */    
    private Properties properties;

    /**
     * The database connection pool. By default no connection pooling 
     * is used. A connection pool is created if the pool size is set. 
     */
    private DatabaseConnectionPool pool = null;

    /**
     * Loads the specified JDBC database driver. This method must be
     * called once before attempting to connect with the specified 
     * driver. Calling this method several times has no effect.
     * 
     * @param driver         the fully qualified classname
     * 
     * @throws DatabaseConnectionException if the class couldn't be
     *             found or loaded correctly 
     */
    public static void loadDriver(String driver) 
        throws DatabaseConnectionException {

        String  message;
        try {
            Class.forName(driver).newInstance();
        } catch (Exception e) {
            message = "couldn't find JDBC driver " + driver;
            LOG.debug(message, e);
            throw new DatabaseConnectionException(message, e);
        }
    }

    /**
     * Creates a new database connector. This connector properties
     * will initially be empty.
     * 
     * @param url            the JDBC database URL
     */
    public DatabaseConnector(String url) {
        this(url, new Properties());
    }

    /**
     * Creates a new database connector.
     * 
     * @param url            the JDBC database URL
     * @param properties     the JDBC database properties
     */
    public DatabaseConnector(String url, Properties properties) {
        this.url = url;
        this.properties = properties;
        LOG.trace("created database connector for " + url);
    }
    
    /**
     * Returns a specified database connector property.
     * 
     * @param name           the property name
     * 
     * @return the property value, or
     *         null if not found
     */
    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    /**
     * Sets the specified database connector property.
     * 
     * @param name           the property name
     * @param value          the property value
     */
    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    /**
     * Returns the maximum database connection pool size. By default 
     * no connection pooling is used, and the pool size is therefore 
     * zero (0). A new connection pool is created the first time the
     * setPoolSize() method is called.
     * 
     * @return the database connection pool size
     * 
     * @see #setPoolSize 
     */
    public int getPoolSize() {
        if (pool == null) {
            return 0;
        } else {
            return pool.getMaximumSize();
        }
    }
    
    /**
     * Sets the maximum database connection pool size. The first time
     * this method is called, a new database connection pool is 
     * created. The pool minimum size and timeout will be set to 
     * default values.
     * 
     * @param size           the new maximum connection pool size
     * 
     * @see #getPoolSize
     */
    public void setPoolSize(int size) {
        if (pool == null) {
            pool = new DatabaseConnectionPool(url, properties);
            pool.setTimeout(DEFAULT_POOL_TIMEOUT);
        }
        if (size < 1) {
            pool.setMinimumSize(0);
        } else {
            pool.setMinimumSize(1);
        }
        pool.setMaximumSize(size);
    }

    /**
     * Returns a database connection. This method will either create 
     * a new connection, or return a previous connection from the
     * connection pool (if one exists). All connections returned by
     * this method must be disposed of by calling the 
     * returnConnection() method.  
     * 
     * @return a database connection
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     * 
     * @see #returnConnection
     */
    public DatabaseConnection getConnection() 
        throws DatabaseConnectionException {

        LOG.trace("database connection requested for " + url);
        if (pool == null) {
            return new DatabaseConnection(url, properties);
        } else {
            return pool.getConnection();
        }
    }
    
    /**
     * Disposes of a database connection. This method will either 
     * return the connection to the connection pool, or close the
     * connection, depending on if a pool exists or not.
     * 
     * @param con            the database connection
     * 
     * @see #getConnection
     */
    public void returnConnection(DatabaseConnection con) {
        LOG.trace("database connection returned for " + url);
        if (pool == null) {
            con.close();
        } else {
            pool.returnConnection(con);
        }
    }

    /**
     * Updates the connection pool. This method will step through all
     * available database connections in the pool, removing all 
     * broken or timed out connections. The connection pool size may 
     * also be adjusted.
     * 
     * Note that any call to this method should be made from a 
     * background thread, as this method may get stuck waiting for 
     * I/O timeouts.
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     */
    public void update() throws DatabaseConnectionException {
        if (pool != null) {
            pool.update();
        }
    }
    
    /**
     * Executes an SQL query or statement. 
     * 
     * @param sql            the SQL query or statement to execute
     * 
     * @return the database results
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     */
    public DatabaseResults execute(String sql) 
        throws DatabaseConnectionException, DatabaseException { 

        DatabaseConnection  con;
        DatabaseResults     res;
        
        con = getConnection();
        try {
            res = con.execute(sql);
        } finally {
            returnConnection(con);
        }

        return res;
    }
}
