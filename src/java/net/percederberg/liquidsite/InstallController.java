/*
 * InstallController.java
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

package net.percederberg.liquidsite;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;

import net.percederberg.liquidsite.db.DatabaseConnectionException;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseException;
import net.percederberg.liquidsite.db.DatabaseResults;
import net.percederberg.liquidsite.db.MySQLDatabaseConnector;

/**
 * A controller for the installation process.
 *
 * @author   Marielle Fois, <marielle at kth dot se>
 * @version  1.0
 */
public class InstallController extends Controller {

    /**
     * Creates a new install controller. 
     *
     * @param app            the application context
     */
    public InstallController(Application app) {
        super(app);
    }

    /**
     * Destroys this request controller. This method frees all
     * internal resources used by this controller.
     */
    public void destroy() {
    }

    /**
     * Processes a request. This method looks at the request parameter
     * "step" to know the next step in the installation process. If
     * this parameter doesn't exist, it forwards to the start
     * installation page.
     *
     * @param request          the request object to process
     */
    public void process(Request request) {

        // get requested path
        String path = request.getPath();

        // process the request
        if (path.equals("/liquidsite/install.html")) {

            // get current step
            String step = request.getParameter("step");
            if (step == null) {
                step = "1";
            }

            // process step
            if (step.equals("0")) {
                start(request);
            } else if (step.equals("1")) {
                install(request);
            } else if (step.equals("2")) {
                install2(request);
            } else if (step.equals("3")) {
                install3(request);
            } else if (step.equals("4")) {
                install4(request);
            } else if (step.equals("5")) {
                install5(request);
            } else {
                start(request);
            }

        } else {
            start(request);
        }
    }

    /**
     * Forwards to the start installation page.
     *
     * @param request           the request object
     */
    private void start(Request request) {
        request.forward("/install/start.jsp");
    }

    /**
     * Processes the request information and forwards to the first
     * page of the web installation.
     *
     * @param request           the request object
     */
    private void install(Request request) {
        // info
        String host;
        String rootUsername;
        String rootPassword;
        String dbchoice;
        String dbsel;
        String database;
        String userchoice;
        String usersel;
        String username;
        String password;
        String verify;

        // errors
        boolean error;
        boolean errorHost;
        boolean errorUsername;
        boolean errorPassword;
        boolean errorConnection;

        // get info
        host = request.getParameter("host");
        rootUsername = request.getParameter("rootUsername");
        rootPassword = request.getParameter("rootPassword");
        dbchoice = request.getParameter("dbchoice");
        dbsel = request.getParameter("dbsel");
        database = request.getParameter("database");
        userchoice = request.getParameter("userchoice");
        usersel = request.getParameter("usersel");
        username = request.getParameter("username");
        password = request.getParameter("password");
        verify = request.getParameter("verify");

        // set default info
        if (host == null) {
            host = "localhost";
        }
        if (rootUsername == null) {
            rootUsername = "root";
        }
        if (rootPassword == null) {
            rootPassword = "";
        }
        if (dbchoice == null) {
            dbchoice = "create";
        }
        if (dbsel == null) {
            dbsel = "";
        }
        if (database == null) {
            database = "liquidsite";
        }
        if (userchoice == null) {
            userchoice = "create";
        }
        if (usersel == null) {
            usersel = "";
        }
        if (username == null) {
            username = "liquidsite";
        }
        if (password == null) {
            password = "";
        }
        if (verify == null) {
            verify = "";
        }

        // set error variables
        error = false;
        errorHost = false;
        errorUsername = false;
        errorPassword = false;
        errorConnection = false;

        // set attributes in request
        request.setAttribute("host", host);
        request.setAttribute("rootUsername", rootUsername);
        request.setAttribute("rootPassword", rootPassword);
        request.setAttribute("dbchoice", dbchoice);
        request.setAttribute("dbsel", dbsel);
        request.setAttribute("database", database);
        request.setAttribute("userchoice", userchoice);
        request.setAttribute("usersel", usersel);
        request.setAttribute("username", username);
        request.setAttribute("password", password);
        request.setAttribute("verify", verify);
        request.setAttribute("error", error);
        request.setAttribute("errorHost", errorHost);
        request.setAttribute("errorUsername", errorUsername);
        request.setAttribute("errorPassword", errorPassword);
        request.setAttribute("errorConnection", errorConnection);

        // forward to install page
        request.forward("/install/install.jsp");
    }

    /**
     * Processes the request information. If an error happened in the
     * request information, it forwards to the first page in the
     * web installation, otherwise it forwards to the second page.
     *
     * @param request           the request object
     */
    private void install2(Request request) {
        // info
        String host;
        String rootUsername;
        String rootPassword;
        String dbchoice;
        String dbsel;
        String database;
        String userchoice;
        String usersel;
        String username;
        String password;
        String verify;

        // errors
        boolean error;
        boolean errorHost;
        boolean errorUsername;
        boolean errorPassword;
        boolean errorDbExists;
        boolean errorConnection;

        // database results
        Vector databases = null;
        Vector lsdatabases = null;

        // get info
        host = (request.getParameter("host")).trim();
        rootUsername = (request.getParameter("rootUsername")).trim();
        rootPassword = (request.getParameter("rootPassword")).trim();
        dbchoice = request.getParameter("dbchoice");
        dbsel = request.getParameter("dbsel");
        database = request.getParameter("database");
        userchoice = request.getParameter("userchoice");
        usersel = request.getParameter("usersel");
        username = request.getParameter("username");
        password = request.getParameter("password");
        verify = request.getParameter("verify");

        // initialize errors
        error = false;
        errorHost = false;
        errorUsername = false;
        errorPassword = false;
        errorDbExists = false;
        errorConnection = false;

        // check info for errors
        if (host.equals("")) {
            error = true;
            errorHost = true;
        }
        if (rootUsername.equals("")) {
            error = true;
            errorUsername = true;
        }
        if (rootPassword.equals("")) {
            error = true;
            errorPassword = true;
        }

        // if no error happened, open a database connection
        if (!error) {
            // get list of databases
            try {
                databases =
                    getDatabaseNames(host, rootUsername, rootPassword);
                lsdatabases =
                    getLSDatabaseNames(host, rootUsername, rootPassword);
            } catch (DatabaseConnectionException e) {
                // could not open connection
                error = true;
                errorConnection = true;
                databases = null;
                lsdatabases = null;
                // TODO write log
                e.printStackTrace();
            } catch (DatabaseException e) {
                // TODO write log
                e.printStackTrace();
                // forward to error page
                request.forward("/install/error.jsp");
                return;
            } catch (DatabaseDataException e) {
                // TODO write log
                e.printStackTrace();
                // forward to error page
                request.forward("/install/error.jsp");
                return;
            }
        }

        // choose database from liquid site databases
        if (dbsel.equals("") && lsdatabases != null && 
            lsdatabases.size() > 0) {

            dbsel = (String) lsdatabases.elementAt(0);
        }

        // set attributes in request
        request.setAttribute("host", host);
        request.setAttribute("rootUsername", rootUsername);
        request.setAttribute("rootPassword", rootPassword);
        request.setAttribute("dbchoice", dbchoice);
        request.setAttribute("dbsel", dbsel);
        request.setAttribute("database", database);
        request.setAttribute("userchoice", userchoice);
        request.setAttribute("usersel", usersel);
        request.setAttribute("username", username);
        request.setAttribute("password", password);
        request.setAttribute("verify", verify);
        request.setAttribute("error", error);
        request.setAttribute("errorConnection", errorConnection);

        // check if error happened
        if (error) {

            // set attributes in request
            request.setAttribute("errorHost", errorHost);
            request.setAttribute("errorUsername", errorUsername);
            request.setAttribute("errorPassword", errorPassword);

            // forward to same page
            request.forward("/install/install.jsp");

        } else {

            // set attributes in request
            request.setAttribute("databases", databases);
            request.setAttribute("errorDbExists", errorDbExists);

            // forward to next page
            request.forward("/install/install2.jsp");
        }
    }

    /**
     * Processes the request information. If an error happened in the
     * request information, it forwards to the second page in the
     * web installation, otherwise it forwards to the third page.
     *
     * @param request           the request object
     */
    private void install3(Request request) {
        // info
        String host;
        String rootUsername;
        String rootPassword;
        String dbchoice;
        String dbsel;
        String database;
        String userchoice;
        String usersel;
        String username;
        String password;
        String verify;

        // errors
        boolean error;
        boolean errorDbExists;
        boolean errorVerify;
        boolean errorUserExists;
        boolean errorConnection;

        // database results
        Vector databases = null;
        Vector users = null;
        Vector lsusers = null;

        // get info
        host = request.getParameter("host");
        rootUsername = request.getParameter("rootUsername");
        rootPassword = request.getParameter("rootPassword");
        dbchoice = request.getParameter("dbchoice");
        dbsel = request.getParameter("dbsel");
        database = (request.getParameter("database")).trim();
        userchoice = request.getParameter("userchoice");
        usersel = request.getParameter("usersel");
        username = request.getParameter("username");
        password = request.getParameter("password");
        verify = request.getParameter("verify");
        dbsel = request.getParameter("dbsel");

        // initialize errors
        error = false;
        errorDbExists = false;
        errorVerify = false;
        errorUserExists = false;
        errorConnection = false;

        // get list of databases
        try {
            databases = 
                getDatabaseNames(host, rootUsername, rootPassword);
        } catch (DatabaseConnectionException e) {
            // could not open connection
            error = true;
            errorConnection = true;
            // TODO write log
            e.printStackTrace();
        } catch (DatabaseException e) {
            // TODO write log
            e.printStackTrace();
            // forward to error page
            request.forward("/install/error.jsp");
            return;
        } catch (DatabaseDataException e) {
            // TODO write log
            e.printStackTrace();
            // forward to error page
            request.forward("/install/error.jsp");
            return;
        }

        // check input info
        if (dbchoice.equals("select") && dbsel.equals("")) {
            error = true;
        } else if (dbchoice.equals("create")) {
            if (database.equals("")) {
                error = true;
            } else if (databases.contains(database)) {
                error = true;
                errorDbExists = true;
            }
        }

        // set attributes in request
        request.setAttribute("host", host);
        request.setAttribute("rootUsername", rootUsername);
        request.setAttribute("rootPassword", rootPassword);
        request.setAttribute("dbchoice", dbchoice);
        request.setAttribute("dbsel", dbsel);
        request.setAttribute("database", database);
        request.setAttribute("userchoice", userchoice);
        request.setAttribute("username", username);
        request.setAttribute("password", password);
        request.setAttribute("verify", verify);
        request.setAttribute("error", error);
        request.setAttribute("errorConnection", errorConnection);

        // check if error happened
        if (error) {

            // set attributes in request
            request.setAttribute("usersel", usersel);
            request.setAttribute("databases", databases);
            request.setAttribute("errorDbExists", errorDbExists);

            // forward to same page
            request.forward("/install/install2.jsp");

        } else {

            // get list of users
            try {
                users = getUserNames(host, rootUsername, rootPassword);
            } catch (DatabaseConnectionException e) {
                // could not open connection
                error = true;
                errorConnection = true;
                // TODO write log
                e.printStackTrace();
            } catch (DatabaseException e) {
                // TODO write log
                e.printStackTrace();
                // forward to error page
                request.forward("/install/error.jsp");
                return;
            } catch (DatabaseDataException e) {
                // TODO write log
                e.printStackTrace();
                // forward to error page
                request.forward("/install/error.jsp");
                return;
            }

            // choose user from liquid site users
            if (usersel.equals("") && lsusers != null && lsusers.size() > 0) {
                usersel = (String) lsusers.elementAt(0);
            }

            // set attributes in request
            request.setAttribute("usersel", usersel);
            request.setAttribute("users", users);
            request.setAttribute("errorVerify", errorVerify);
            request.setAttribute("errorUserExists", errorUserExists);

            // forward to next page
            request.forward("/install/install3.jsp");
        }
    }

    /**
     * Processes the request information. If an error happened in the
     * request information, it forwards to the third page in the
     * web installation, otherwise it forwards to the fourth page.
     *
     * @param request           the request object
     */
    private void install4(Request request) {
        // info
        String host;
        String rootUsername;
        String rootPassword;
        String dbchoice;
        String dbsel;
        String database;
        String userchoice;
        String usersel;
        String username;
        String password;
        String verify;

        // errors
        boolean error;
        boolean errorVerify;
        boolean errorUserExists;
        boolean errorConnection;

        // database results
        Vector users = null;

        // get info
        host = request.getParameter("host");
        rootUsername = request.getParameter("rootUsername");
        rootPassword = request.getParameter("rootPassword");
        dbchoice = request.getParameter("dbchoice");
        dbsel = request.getParameter("dbsel");
        database = request.getParameter("database");
        userchoice = request.getParameter("userchoice");
        usersel = request.getParameter("usersel");
        username = (request.getParameter("username")).trim();
        password = (request.getParameter("password")).trim();
        verify = (request.getParameter("verify")).trim();

        // initialize errors
        error = false;
        errorVerify = false;
        errorUserExists = false;
        errorConnection = false;

        // get list of users
        try {
            users = getUserNames(host, rootUsername, rootPassword);
        } catch (DatabaseConnectionException e) {
            // could not open connection
            error = true;
            errorConnection = true;
            // TODO write log
            e.printStackTrace();
        } catch (DatabaseException e) {
            // TODO write log
            e.printStackTrace();
            // forward to error page
            request.forward("/install/error.jsp");
            return;
        } catch (DatabaseDataException e) {
            // TODO write log
            e.printStackTrace();
            // forward to error page
            request.forward("/install/error.jsp");
            return;
        }

        // check input info
        if (userchoice.equals("select") && usersel.equals("")) {
            error = true;
        } else if (userchoice.equals("create")) {
            if (username.equals("") || password.equals("") ||
                verify.equals("")) {
                error = true;
            } else if (users.contains(username)) {
                error = true;
                errorUserExists = true;
            }
        }
        if (!error && !password.equals(verify)) {
            error = true;
            errorVerify = true;
            password = "";
            verify = "";
        }

        // set attributes in request
        request.setAttribute("host", host);
        request.setAttribute("rootUsername", rootUsername);
        request.setAttribute("rootPassword", rootPassword);
        request.setAttribute("dbchoice", dbchoice);
        request.setAttribute("dbsel", dbsel);
        request.setAttribute("database", database);
        request.setAttribute("userchoice", userchoice);
        request.setAttribute("usersel", usersel);
        request.setAttribute("username", username);
        request.setAttribute("password", password);
        request.setAttribute("verify", verify);
        request.setAttribute("error", error);
        request.setAttribute("errorConnection", errorConnection);

        // check if error happened
        if (error) {

            // set attributes in request
            request.setAttribute("users", users);
            request.setAttribute("errorVerify", errorVerify);
            request.setAttribute("errorUserExists", errorUserExists);

            // forward to same page
            request.forward("/install/install3.jsp");

        } else {
            // forward to next page
            request.forward("/install/install4.jsp");
        }
    }

    /**
     * Performs the installation and forwards to the last page of the
     * installation.
     *
     * @param request           the request object
     */
    private void install5(Request request) {
        // info
        String host;
        String rootUsername;
        String rootPassword;
        String dbchoice;
        String dbsel;
        String database;
        String userchoice;
        String usersel;
        String username;
        String password;
        String db;
        String user;
        String passwd;

        // get info from request
        host = request.getParameter("host");
        rootUsername = request.getParameter("rootUsername");
        rootPassword = request.getParameter("rootPassword");
        dbchoice = request.getParameter("dbchoice");
        dbsel = request.getParameter("dbsel");
        database = request.getParameter("database");
        userchoice = request.getParameter("userchoice");
        usersel = request.getParameter("usersel");
        username = request.getParameter("username");
        password = request.getParameter("password");

        try {

            // get selected database and user
            if (dbchoice.equals("select")) {
                db = dbsel;
            } else {
                db = database;
            }
            if (userchoice.equals("select")) {
                user = usersel;
                passwd = getUserPassword(user, host, rootUsername, 
                                         rootPassword);
            } else {
                user = username;
                passwd = password;
            }

            // drop database if exists
            if (dbchoice.equals("select")) {
                dropDatabase(db, host, rootUsername, rootPassword);
            }

            // create database
            createDatabase(db, host, rootUsername, rootPassword);

            // grant permissions to user to read/write rows in database
//              grantLSPermissions(db, user, passwd, host, 
//                                 rootUsername, rootPassword);

            // create tables
            createTables(db, host, rootUsername, rootPassword);

            // populate tables
            initTables(db, host, rootUsername, rootPassword);

            // create config.properties file
            createConfigProp(host, user, passwd);

        } catch (DatabaseConnectionException e) {
            // TODO write log
            e.printStackTrace();
        } catch (DatabaseException e) {
            // TODO write log
            e.printStackTrace();
            // forward to error page
            request.forward("/install/error.jsp");
            return;
        } catch (DatabaseDataException e) {
            // TODO write log
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO write log
            e.printStackTrace();
            // forward to error page
            request.forward("/install/error.jsp");
            return;
        } catch (IOException e) {
            // TODO write log
            e.printStackTrace();
            // forward to error page
            request.forward("/install/error.jsp");
            return;
        }

        // forward to next page
        request.forward("/install/install5.jsp");
    }

    /**
     * Returns a list with all database names.
     * 
     * @param host           the database host
     * @param username       the database user name
     * @param password       the database user password
     * 
     * @return a vector with database names
     * 
     * @throws DatabaseConnectionException if a new database 
     *             connection couldn't be established
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     * @throws DatabaseDataException if the database table structure
     *             didn't match the expected one
     */
    private Vector getDatabaseNames(String host, 
                                    String username,
                                    String password) 
        throws DatabaseConnectionException, DatabaseException,
               DatabaseDataException {

        Vector           databases = new Vector();
        DatabaseResults  res;

        res =  execute(host, username, password, "SHOW DATABASES");
        for (int i = 0; i < res.getRowCount(); i++) {
            databases.add(res.getRow(i).getString("Database"));
        }

        return databases;
    }

    /**
     * Returns a list with all Liquid Site database names.
     * 
     * @param host           the database host
     * @param username       the database user name
     * @param password       the database user password
     * 
     * @return a vector with database names
     * 
     * @throws DatabaseConnectionException if a new database 
     *             connection couldn't be established
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     * @throws DatabaseDataException if the database table structure
     *             didn't match the expected one
     */
    private Vector getLSDatabaseNames(String host, 
                                      String username,
                                      String password) 
        throws DatabaseConnectionException, DatabaseException,
               DatabaseDataException {

        Vector           lsDatabases = new Vector();
        Vector           databases;
        DatabaseResults  res;
        String           db;
        String           sql;
        
        databases = getDatabaseNames(host, username, password);
        sql = "SELECT * FROM CONFIGURATION WHERE NAME='liquidsite.version'";
        for (int i = 0; i < databases.size(); i++) {
            db = (String) databases.get(i);
            try {
                res = execute(db, host, username, password, sql);
                if (res.getRowCount() == 1) {
                    lsDatabases.add(db);
                }
            } catch (DatabaseException ignore) {
                // Do nothing
            }
        }
        return lsDatabases;
    }

    /**
     * Returns a list with all database users.
     * 
     * @param host           the database host
     * @param username       the database user name
     * @param password       the database user password
     * 
     * @return a vector with database user names
     * 
     * @throws DatabaseConnectionException if a new database 
     *             connection couldn't be established
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     * @throws DatabaseDataException if the database table structure
     *             didn't match the expected one
     */
    private Vector getUserNames(String host, 
                                String username,
                                String password) 
        throws DatabaseConnectionException, DatabaseException,
               DatabaseDataException {

        Vector           users = new Vector();
        DatabaseResults  res;
        String           sql;

        sql = "SELECT User FROM user WHERE Password != ''";
        res = execute(host, username, password, sql);
        for (int i = 0; i < res.getRowCount(); i++) {
            users.add(res.getRow(i).getString("User"));
        }
        return users;
    }

    /**
     * Returns the password hash for a database user. 
     * 
     * @param user           the user to search for
     * @param host           the database host
     * @param username       the database user name
     * @param password       the database user password
     *
     * @return the password hash for a database user
     *  
     * @throws DatabaseConnectionException if a new database 
     *             connection couldn't be established
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     * @throws DatabaseDataException if the database table structure
     *             didn't match the expected one
     */
    private String getUserPassword(String user, 
                                   String host, 
                                   String username,
                                   String password) 
        throws DatabaseConnectionException, DatabaseException, 
               DatabaseDataException {

        String           sql;
        DatabaseResults  res;

        sql = "SELECT Password FROM user WHERE User='" + user + "'";
        res =  execute(host, username, password, sql);
        if (res.getRowCount() > 0) {
            return res.getRow(0).getString("Password");
        } else {
            return "";
        }
    }

    /**
     * Drops a database.
     * 
     * @param database       the database name
     * @param host           the database host
     * @param username       the database user name
     * @param password       the database user password
     * 
     * @throws DatabaseConnectionException if a new database 
     *             connection couldn't be established
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     */
    private void dropDatabase(String database, 
                              String host, 
                              String username, 
                              String password) 
        throws DatabaseConnectionException, DatabaseException {

        String  sql;

        sql = "DROP DATABASE " + database;
        execute(host, username, password, sql);
    }

    /**
     * Creates a database.
     * 
     * @param database       the database name
     * @param host           the database host
     * @param username       the database user name
     * @param password       the database user password
     * 
     * @throws DatabaseConnectionException if a new database 
     *             connection couldn't be established
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     */
    private void createDatabase(String database, 
                                String host, 
                                String username, 
                                String password) 
        throws DatabaseConnectionException, DatabaseException {

        String  sql;

        sql = "CREATE DATABASE " + database;
        execute(host, username, password, sql);
    }

    /**
     * Adds permissions for a user to a database.
     * 
     * @param database       the database name
     * @param user           the user name
     * @param passwd         the user password hash
     * @param host           the database host
     * @param username       the database user name
     * @param password       the database user password
     * 
     * @throws DatabaseConnectionException if a new database 
     *             connection couldn't be established
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     */
    private void grantLSPermissions(String database, 
                                    String user, 
                                    String passwd, 
                                    String host, 
                                    String username, 
                                    String password) 
        throws DatabaseConnectionException, DatabaseException {

        String  sql;

        // grant permissions
        sql = "grant select on " + database + ".* to " + user;
        if (password != null) {
            sql += " identified by password '" + passwd + "'";
        }
        execute(database, host, username, password, sql);
        sql = "grant insert on " + database + ".* to " + user;
        if (password != null) {
            sql += " identified by password '" + passwd + "'";
        }
        execute(database, host, username, password, sql);
        sql = "grant update on " + database + ".* to " + user;
        if (password != null) {
            sql += " identified by password '" + passwd + "'";
        }
        execute(database, host, username, password, sql);
        sql = "grant delete on " + database + ".* to " + user;
        if (password != null) {
            sql += " identified by password '" + passwd + "'";
        }
        execute(database, host, username, password, sql);
    }

    /**
     * Executes an SQL statement or query on a database. A new 
     * connection will be opened to the database, the SQL statement 
     * will be executed, and finally the connection is closed.
     * 
     * @param host           the database host name
     * @param username       the database user name
     * @param password       the database user password
     * @param sql            the SQL query to execute
     * 
     * @return the database results
     * 
     * @throws DatabaseConnectionException if a new database 
     *             connection couldn't be established
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     */
    private DatabaseResults execute(String host, 
                                    String username, 
                                    String password,
                                    String sql)
        throws DatabaseConnectionException, DatabaseException {

        MySQLDatabaseConnector  con;

        con = new MySQLDatabaseConnector(host, username, password);
        return con.executeSql(sql);
    }

    /**
     * Executes an SQL statement or query on a database. A new 
     * connection will be opened to the database, the SQL statement 
     * will be executed, and finally the connection is closed.
     * 
     * @param database       the database name
     * @param host           the database host name
     * @param username       the database user name
     * @param password       the database user password
     * @param sql            the SQL query to execute
     * 
     * @return the database results
     * 
     * @throws DatabaseConnectionException if a new database 
     *             connection couldn't be established
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     */
    private DatabaseResults execute(String database, 
                                    String host, 
                                    String username, 
                                    String password,
                                    String sql)
        throws DatabaseConnectionException, DatabaseException {

        MySQLDatabaseConnector  con;

        con = new MySQLDatabaseConnector(host, database, username, password);
        return con.executeSql(sql);
    }

    // TODO: rewrite this method to NOT use resource properties
    private void createTables(String database, 
                              String host, 
                              String username, 
                              String password) 
        throws DatabaseConnectionException, DatabaseException,
               FileNotFoundException, IOException {

        File file;
        FileInputStream queriesFile;
        PropertyResourceBundle queries;
        String sql;

        // read properties file
        file = getFile("WEB-INF/db_create_tables.properties");
        queriesFile = new FileInputStream(file);
        queries = new PropertyResourceBundle(queriesFile);

        // perform all queries
        for (Enumeration e = queries.getKeys(); e.hasMoreElements();) {
            sql = queries.getString((String) e.nextElement());
            execute(database, host, username, password, sql);
        }
    }

    // TODO: rewrite this method to NOT use resource properties
    private void initTables(String database, 
                            String host, 
                            String username, 
                            String password) 
        throws DatabaseConnectionException, DatabaseException,
               FileNotFoundException, IOException {

        File file;
        FileInputStream queriesFile;
        PropertyResourceBundle queries;
        String sql;

        // read properties file
        file = getFile("WEB-INF/db_init.properties");
        queriesFile = new FileInputStream(file);
        queries = new PropertyResourceBundle(queriesFile);

        // perform all queries
        for (Enumeration e = queries.getKeys(); e.hasMoreElements();) {
            sql = queries.getString((String) e.nextElement());
            execute(database, host, username, password, sql);
        }
    }

    private void createConfigProp(String host, 
                                  String username, 
                                  String password) 
        throws FileNotFoundException {

        File file;
        PrintStream config;

        // create print stream
        file = getFile("WEB-INF/config.properties");
        config = new PrintStream(new FileOutputStream(file));

        // write to stream
        config.println("HOST=" + host);
        config.println("USERNAME=" + username);
        config.println("PASSWORD=" + password);
    }
}
