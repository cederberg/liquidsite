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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import net.percederberg.liquidsite.Log;

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
     * The class logger.
     */
    private static final Log LOG = new Log(DatabaseConnection.class);

    /**
     * The database connector.
     */
    private DatabaseConnector db;

    /**
     * The JDBC database connection.
     */
    private Connection con;

    /**
     * The initial connection catalog (database). This is used to be
     * able to reset the connection to it's initial state.
     * 
     * @see #reset
     */
    private String catalog;

    /**
     * The valid connection flag. This flag is set to false if an 
     * error is encountered while executing SQL statements on the 
     * connection. After this flag has been set, the connection 
     * shouldn't be used.
     */
    private boolean valid = true;

    /**
     * The reserved connection flag. This flag is used by the 
     * connection pool to determine if the connection is being used.
     */
    private boolean reserved = false;

    /**
     * The connection creation time.
     */
    private long creationTime = System.currentTimeMillis(); 

    /**
     * The query execution timeout in seconds. If this value is 
     * negative the queries can run without limitation.
     */
    private int queryTimeout = DatabaseConnector.DEFAULT_QUERY_TIMEOUT;

    /**
     * Creates a new database connection. The database JDBC driver 
     * must have been previously loaded, or a connection exception
     * will be thrown.
     * 
     * @param db             the database connector to use
     * 
     * @throws DatabaseConnectionException if the database connection 
     *             couldn't be created
     */
    DatabaseConnection(DatabaseConnector db) 
        throws DatabaseConnectionException {

        this.db = db;
        try {
            LOG.trace("creating connection to " + db + "...");
            con = DriverManager.getConnection(db.getUrl(), 
                                              db.getProperties());
            catalog = con.getCatalog();
            if (catalog.equals("")) {
                catalog = null;
            }
            reset();
            LOG.trace("created connection to " + db);
        } catch (SQLException e) {
            LOG.debug("failed to create connection to " + db, e);
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
     * Checks if this connection has expired. A connection expires 
     * when the connection age is more than the timeout.
     * 
     * @return true if the connection has expired, or
     *         false otherwise
     */
    public boolean isExpired() {
        long  timeout = db.getConnectionTimeout();
        
        return timeout >= 0 && 
               timeout < (System.currentTimeMillis() - creationTime);
    }

    /**
     * Checks if this connection is reserved. A connection is 
     * reserved when it is being used.
     * 
     * @return true if this connection is reserved, or
     *         false otherwise
     */
    boolean isReserved() {
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
     * Returns the query execution timeout. If this value is negative
     * the queries can run without limitation. New connections and
     * connections returned from a connection pool always have a 
     * default timeout value.
     * 
     * @return the query execution timeout in seconds, or
     *         a negative value for unlimited
     * 
     * @see #setQueryTimeout
     * @see DatabaseConnector#DEFAULT_QUERY_TIMEOUT
     */
    public int getQueryTimeout() {
        return queryTimeout;
    }
    
    /**
     * Sets the query execution timeout. If this value is negative
     * the queries can run without limitation.
     * 
     * @param timeout        the query execution timeout in seconds, or
     *                       a negative value for unlimited
     * 
     * @see #getQueryTimeout
     */
    public void setQueryTimeout(int timeout) {
        this.queryTimeout = timeout;
    }
    
    /**
     * Resets the database connection to default values. This will 
     * reset the connection to the same state it had when first 
     * created. This method is used by the connection pool to 
     * guarantee that all connections are returned identical. 
     * 
     * @throws DatabaseConnectionException if the database connection 
     *             couldn't be reestablished
     */
    public void reset() throws DatabaseConnectionException {
        this.queryTimeout = DatabaseConnector.DEFAULT_QUERY_TIMEOUT;
        try {
            if (catalog != null) {
                con.setCatalog(catalog);
            }
            con.setAutoCommit(true);
            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } catch (SQLException e) {
            valid = false;
            LOG.debug("failed to reset connection to " + db, e);
            throw new DatabaseConnectionException(e);
        }
    }

    /**
     * Executes a database function with no parameters. 
     * 
     * @param name           the database function name
     * 
     * @return the database results
     * 
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     */
    public DatabaseResults execute(String name)
        throws DatabaseException {

        return execute(name, new ArrayList(0));
    }

    /**
     * Executes a database function with parameters. 
     * 
     * @param name           the database function name
     * @param params         the database function parameters
     * 
     * @return the database results
     * 
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     */
    public DatabaseResults execute(String name, ArrayList params)
        throws DatabaseException {

        PreparedStatement  stmt;
        ResultSet          set;
        String             sql;
        
        try {
            LOG.trace("executing query '" + name + "'...");
            sql = db.getFunction(name);
            if (sql == null) {
                sql = "no database function '" + name + "'";
                LOG.debug(sql);
                throw new DatabaseException(sql);
            }
            stmt = con.prepareStatement(sql,
                                        ResultSet.TYPE_FORWARD_ONLY,
                                        ResultSet.CONCUR_READ_ONLY,
                                        ResultSet.CLOSE_CURSORS_AT_COMMIT);
            stmt.setQueryTimeout(queryTimeout);
            stmt.clearParameters();
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            set = stmt.executeQuery();
            LOG.trace("executed query '" + name + "'");
        } catch (SQLException e) {
            LOG.debug("failed to execute query '" + name + "'", e);
            throw new DatabaseException(e);
        }

        return extractResults(set);
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
    public DatabaseResults executeSql(String sql) 
        throws DatabaseException {

        Statement        stmt;
        ResultSet        set;

        try {
            LOG.trace("executing SQL '" + sql + "'...");
            stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                       ResultSet.CONCUR_READ_ONLY,
                                       ResultSet.CLOSE_CURSORS_AT_COMMIT);
            stmt.setQueryTimeout(queryTimeout);
            set = stmt.executeQuery(sql);
            LOG.trace("executed SQL '" + sql + "'");
        } catch (SQLException e) {
            LOG.debug("failed to execute SQL '" + sql + "'", e);
            throw new DatabaseException(e);
        }

        return extractResults(set);
    }

    /**
     * Executes a set of SQL statements from a file. Each SQL 
     * statement must be terminated by a ';' character. 
     * 
     * @param file           the file with SQL statements
     * 
     * @throws FileNotFoundException if the file couldn't be found
     * @throws IOException if the file couldn't be read properly
     * @throws DatabaseException if some statement couldn't be 
     *             executed correctly
     */
    public void executeSql(File file) 
        throws FileNotFoundException, IOException, DatabaseException {

        BufferedReader  input;
        StringBuffer    sql = new StringBuffer();
        String          line;
        
        input = new BufferedReader(new FileReader(file));
        try {
            while ((line = input.readLine()) != null) {
                line = line.trim();
                if (line.equals("") || line.startsWith("--")) {
                    // Do nothing
                } else if (line.endsWith(";")) {
                    sql.append(line.substring(line.length() - 1));
                    executeSql(sql.toString());
                    sql.setLength(0);
                } else {
                    sql.append(line);
                    sql.append(" ");
                }
            }
        } finally {
            try { 
                input.close();
            } catch (IOException ignore) {
                // Do nothing
            }
        }
    }

    /**
     * Extracts the result from a JDBC result set. The result set 
     * will be closed after all the results have been extracted. 
     * 
     * @param set            the result set to extract data from
     * 
     * @return the database results
     * 
     * @throws DatabaseException if the results couldn't be extracted 
     *             properly
     */
    private DatabaseResults extractResults(ResultSet set) 
        throws DatabaseException {
            
        DatabaseResults  res;

        try {
            LOG.trace("extracting database results...");
            res = new DatabaseResults(set);
            LOG.trace("extracted database results");
        } catch (SQLException e) {
            LOG.debug("failed to extract database results", e);
            throw new DatabaseException(e);
        } finally {
            try {
                LOG.trace("closing result set");
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
        LOG.trace("closing connection to " + db + "...");
        valid = false;
        try {
            if (!con.isClosed()) {
                con.close();
            }
        } catch (SQLException ignore) {
            // Ignore this error
        }
        LOG.trace("closed connection to " + db);
    }
}
