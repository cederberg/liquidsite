/*
 * MySQLDatabaseConnector.java
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

import java.util.ArrayList;

import net.percederberg.liquidsite.Log;

/**
 * A MySQL database connector.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class MySQLDatabaseConnector extends DatabaseConnector {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(MySQLDatabaseConnector.class);

    /**
     * Loads the MySQL database driver. This method must be called 
     * once before attempting to connect with the specified driver.
     * Calling this method several times has no effect.
     * 
     * @throws DatabaseConnectionException if the driver couldn't be
     *             found or loaded correctly 
     */
    public static void loadDriver() throws DatabaseConnectionException {
        loadDriver("com.mysql.jdbc.Driver");
    }

    /**
     * Creates a new MySQL database connector.
     * 
     * @param host           the host name
     * @param user           the user name
     * @param password       the user password
     */
    public MySQLDatabaseConnector(String host, 
                                  String user, 
                                  String password) {

        super("jdbc:mysql://" + host + "/");
        setProperty("user", user);
        setProperty("password", password);
    }

    /**
     * Creates a new MySQL database connector.
     * 
     * @param host           the host name
     * @param database       the database name
     * @param user           the user name
     * @param password       the user password
     */
    public MySQLDatabaseConnector(String host, 
                                  String database,
                                  String user,
                                  String password) {

        super("jdbc:mysql://" + host + "/" + database);
        setProperty("user", user);
        setProperty("password", password);
    }

    /**
     * Checks if the database user is an administrator.
     * 
     * @return true if the database user is an administrator, or
     *         false otherwise
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     * @throws DatabaseException if user privileges couldn't be 
     *             determined
     */
    public boolean isAdministrator() 
        throws DatabaseConnectionException, DatabaseException {

        DatabaseResults  res;
        String           str;
        
        // Retrieve basic privileges
        try {
            res = executeSql("SHOW GRANTS FOR " + getDatabaseUser());
            str = res.getRow(0).getString(0);
        } catch (DatabaseDataException e) {
            LOG.debug("failed to read user privileges", e);
            throw new DatabaseException("cannot determine privileges", e);
        }

        // Check privileges
        if (str.indexOf("ALL PRIVILEGES") > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the database user being used. This is a string in the
     * form "user@host", where the host name is the name of the 
     * connecting host.
     * 
     * @return the database user
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     * @throws DatabaseException if the database user couldn't be 
     *             determined
     */
    public String getDatabaseUser() 
        throws DatabaseConnectionException, DatabaseException {

        DatabaseResults  res;

        try {
            res = executeSql("SELECT USER()");
            return res.getRow(0).getString(0);
        } catch (DatabaseDataException e) {
            LOG.debug("failed to read database user name", e);
            throw new DatabaseException("cannot determine user", e);
        }
    }

    /**
     * Lists the visible databases.
     * 
     * @return a list of the database names
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     * @throws DatabaseException if the list of databases couldn't be 
     *             retrieved
     */
    public ArrayList listDatabases()
        throws DatabaseConnectionException, DatabaseException {

        ArrayList        list = new ArrayList();
        DatabaseResults  res;
        
        try {
            res = executeSql("SHOW DATABASES");
            for (int i = 0; i < res.getRowCount(); i++) {
                list.add(res.getRow(i).getString(0));
            }
        } catch (DatabaseDataException e) {
            LOG.debug("failed to read database list", e);
            throw new DatabaseException("cannot list databases", e);
        }
        return list;
    }
    
    /**
     * Lists the tables in a database. Note that this operation will
     * return a database exception if the database user hasn't got
     * privileges to list the tables in the database.
     * 
     * @param database       the database name
     * 
     * @return a list of the database table names
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     * @throws DatabaseException if the list of tables couldn't be 
     *             retrieved
     */
    public ArrayList listTables(String database)
        throws DatabaseConnectionException, DatabaseException {

        ArrayList        list = new ArrayList();
        DatabaseResults  res;
        
        try {
            res = executeSql("SHOW TABLES IN " + database);
            for (int i = 0; i < res.getRowCount(); i++) {
                list.add(res.getRow(i).getString(0));
            }
        } catch (DatabaseDataException e) {
            LOG.debug("failed to read table list", e);
            throw new DatabaseException("cannot list tables", e);
        }
        return list;
    }
    
    /**
     * Lists the users in a database. Note that this operation will
     * return a database exception if the database user hasn't got
     * administrator privileges.
     * 
     * @return a list of database user names
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     * @throws DatabaseException if the list of users couldn't be 
     *             retrieved
     */
    public ArrayList listUsers()
        throws DatabaseConnectionException, DatabaseException {

        ArrayList        list = new ArrayList();
        DatabaseResults  res;
        String           str;
        
        try {
            res = executeSql("SELECT DISTINCT user FROM mysql.user");
            for (int i = 0; i < res.getRowCount(); i++) {
                str = res.getRow(i).getString(0);
                if (str != null && !str.equals("")) {
                    list.add(str);
                }
            }
        } catch (DatabaseDataException e) {
            LOG.debug("failed to read user list", e);
            throw new DatabaseException("cannot list users", e);
        }
        return list;
    }

    /**
     * Creates a new database. This operation requires that the 
     * database user is an administrator, or a database exception 
     * will be thrown.
     * 
     * @param database       the database name
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     * @throws DatabaseException if the database couldn't be created 
     *             properly
     */
    public void createDatabase(String database) 
        throws DatabaseConnectionException, DatabaseException {

        executeSql("CREATE DATABASE " + database);
    }

    /**
     * Deletes an existing database. This operation requires that the 
     * database user has the correct permissions to the database, or 
     * a database exception will be thrown.
     * 
     * @param database       the database name
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     * @throws DatabaseException if the database couldn't be deleted 
     *             properly
     */
    public void deleteDatabase(String database) 
        throws DatabaseConnectionException, DatabaseException {

        executeSql("DROP DATABASE " + database);
    }

    /**
     * Creates a new database user. This operation requires that the 
     * current database user is an administrator, or a database 
     * exception will be thrown. Also note that the new user will 
     * only be given access privilege from the same host as the 
     * current user.
     * 
     * @param user           the new database user name
     * @param password       the new database user password
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     * @throws DatabaseException if the database user couldn't be 
     *             created properly
     */
    public void createUser(String user, String password)
        throws DatabaseConnectionException, DatabaseException {

        String  host = getDatabaseUser();
        
        host = host.substring(host.indexOf("@"));
        executeSql("GRANT USAGE ON *.* TO " + user + host +
                " IDENTIFIED BY '" + password + "'");
    }

    /**
     * Deletes an existing database user. This operation requires 
     * that the current database user is an administrator, or a 
     * database exception will be thrown.
     * 
     * @param user           the database user name
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     * @throws DatabaseException if the database user couldn't be 
     *             deleted properly
     */
    public void deleteUser(String user)
        throws DatabaseConnectionException, DatabaseException {

        String  host = getDatabaseUser();
        
        host = host.substring(host.indexOf("@"));
        executeSql("REVOKE USAGE ON *.* FROM " + user + host);
        executeSql("DELETE FROM mysql.user WHERE user = '" + user + "'");
        executeSql("FLUSH PRIVILEGES");
    }

    /**
     * Adds normal access privileges to a database for a user. The
     * access privileges are select, insert, update and delete. This 
     * operation requires that the current database user is an 
     * administrator, or a database exception will be thrown. Also 
     * note that the user will only be given access privilege from 
     * the same host as the current user. 
     * 
     * @param database       the database name
     * @param user           the database user name
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     * @throws DatabaseException if the database privileges couldn't 
     *             be set properly
     */
    public void addAccessPrivileges(String database, String user)
        throws DatabaseConnectionException, DatabaseException {

        String  host = getDatabaseUser();
        
        host = host.substring(host.indexOf("@"));
        executeSql("GRANT SELECT,INSERT,UPDATE,DELETE ON " + database +
                ".* TO " + user + host);
    }

    /**
     * Removes normal access privileges to a database for a user. The
     * access privileges are select, insert, update and delete. This 
     * operation requires that the current database user is an 
     * administrator, or a database exception will be thrown. Also 
     * note that only the access privileges from the same host as the
     * current user will be removed. 
     * 
     * @param database       the database name
     * @param user           the database user name
     * 
     * @throws DatabaseConnectionException if a database connection 
     *             couldn't be established
     * @throws DatabaseException if the database privileges couldn't 
     *             be set properly
     */
    public void removeAccessPrivilege(String database, String user)
        throws DatabaseConnectionException, DatabaseException {

        String  host = getDatabaseUser();
        
        host = host.substring(host.indexOf("@"));
        executeSql("REVOKE SELECT,INSERT,UPDATE,DELETE ON " + database +
                ".* FROM " + user + host);
    }
}
