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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;

import net.percederberg.liquidsite.db.DatabaseConnection;
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
     * The MySQL database connector.
     */
    private MySQLDatabaseConnector con = null;
     
    /**
     * The database host name.
     */
    private String host;
    
    /**
     * The database user name.
     */
    private String rootUsername;
    
    /**
     * The database password.
     */
    private String rootPassword;
    
    /**
     * The database choice specification.
     */
    private String dbchoice;
    
    /**
     * The selected database.
     */
    private String dbsel;
    
    /**
     * The entered database.
     */
    private String database;
    
    /**
     * The liquid site user choice specification.
     */
    private String userchoice;
    
    /**
     * The selected liquid site user name.
     */
    private String usersel;
    
    /**
     * The entered liquid site user name.
     */
    private String username;
    
    /**
     * The liquid site password.
     */
    private String password;
    
    /**
     * The liquid site password verification.
     */
    private String verify;

    /**
     * Any error.
     */
    private boolean error;
    
    /**
     * Error in the host name.
     */
    private boolean errorHost;
    
    /**
     * Error in the root username.
     */
    private boolean errorUsername;
    
    /**
     * Error in the root password.
     */
    private boolean errorPassword;
    
    /**
     * The database to create already exists.
     */
    private boolean errorDbExists;

    /**
     * Password verification failed for liquid site user.
     */
    private boolean errorVerify;
    
    /**
     * The liquid site user to create already exists.
     */
    private boolean errorUserExists;

    /**
     * Error in attempting to connect to the database.
     */
    private boolean errorConnection;

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
     * @param request        the request object to process
     */
    public void process(Request request) {
        String path;
        
        // process the requested path
        path = request.getPath();
        if (path.equals("/liquidsite/install.html")) {
            // installation
            String step = request.getParameter("step");
            if (step != null && step.equals("1")) {
                install(request);
            } else if (step != null && step.equals("2")) {
                install2(request);
            } else if (step != null && step.equals("3")) {
                install3(request);
            } else if (step != null && step.equals("4")) {
                install4(request);
            } else if (step != null && step.equals("5")) {
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
        // initialize errors
        initializeErrors();

        // get info
        getParameters(request);
        
        // set default info
        setDefaultValues();

        // set attributes in request
        setAttributes(request);

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
        ArrayList databases = null;
        ArrayList lsdatabases = null;

        // initialize errors
        initializeErrors();

        // get info
        getParameters(request);

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

            dbsel = (String) lsdatabases.get(0);
        }

        // set attributes in request
        setAttributes(request);

        // check if error happened
        if (error) {

            // forward to same page
            request.forward("/install/install.jsp");

        } else {

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
        ArrayList databases = null;
        ArrayList users = null;
        ArrayList lsusers = null;
        
        // initialize errors
        initializeErrors();

        // get info
        getParameters(request);
        
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
        setAttributes(request);

        // check if error happened
        if (error) {

            // set attributes in request
            request.setAttribute("databases", databases);

            // forward to same page
            request.forward("/install/install2.jsp");

        } else {

            // get list of users
            try {
                users = con.listUsers();
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
            }

            // choose user from liquid site users
            if (usersel.equals("") && lsusers != null && lsusers.size() > 0) {
                usersel = (String) lsusers.get(0);
            }

            // set attributes in request
            request.setAttribute("usersel", usersel);
            request.setAttribute("users", users);

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
        ArrayList users = null;

        // initialize errors
        initializeErrors();

        // get info
        getParameters(request);
        
        // get list of users
        try {
            users = con.listUsers();
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
        setAttributes(request);

        // check if error happened
        if (error) {

            // set attributes in request
            request.setAttribute("users", users);

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
        String db;
        String user;
        String passwd = "";

        // get info from request
        getParameters(request);
        
        try {

            // get selected database and user
            db = (dbchoice.equals("select")) ? dbsel : database;
            user = (userchoice.equals("select")) ? usersel : username;

            // drop database if exists
            if (dbchoice.equals("select")) {
                con.deleteDatabase(db);
            }

            // create database
            con.createDatabase(db);

            // grant permissions to user to read/write rows in database
            con.addAccessPrivileges(db, user);

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
     * Gets the installation information from the request parameters.
     *
     * @param request        the request object from which to 
     *                       get the information
     */
    private void getParameters(Request request) {
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
    }
    
    /**
     * Sets the installation information as request attributes.
     *
     * @param request        the request object where to set
     *                       the information
     */
    private void setAttributes(Request request) {
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
        request.setAttribute("errorDbExists", errorDbExists);
        request.setAttribute("errorVerify", errorVerify);
        request.setAttribute("errorUserExists", errorUserExists);
        request.setAttribute("errorConnection", errorConnection);
    }

    /**
     * Sets the default values for the installation process if no
     * values were already set.
     */
    private void setDefaultValues() {
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
    }
    
    /**
     * Sets the error variables to false.
     */
    private void initializeErrors() {
        error = false;
        errorHost = false;
        errorUsername = false;
        errorPassword = false;
        errorDbExists = false;
        errorVerify = false;
        errorUserExists = false;
        errorConnection = false;
    }
    
    /**
     * Returns a list with all database names.
     * 
     * @param host           the database host
     * @param username       the database user name
     * @param password       the database user password
     * 
     * @return a list with database names
     * 
     * @throws DatabaseConnectionException if a new database 
     *             connection couldn't be established
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     * @throws DatabaseDataException if the database table structure
     *             didn't match the expected one
     */
    private ArrayList getDatabaseNames(String host, 
                                       String username,
                                       String password) 
        throws DatabaseConnectionException, DatabaseException,
               DatabaseDataException {

        // create connection if not already created
        if (con == null) {
            con = new MySQLDatabaseConnector(host, username, password);
            con.setPoolSize(1);
        }
        
        return con.listDatabases();
    }

    /**
     * Returns a list with all Liquid Site database names.
     * 
     * @param host           the database host
     * @param username       the database user name
     * @param password       the database user password
     * 
     * @return a list with database names
     * 
     * @throws DatabaseConnectionException if a new database 
     *             connection couldn't be established
     * @throws DatabaseException if the query or statement couldn't 
     *             be executed correctly
     * @throws DatabaseDataException if the database table structure
     *             didn't match the expected one
     */
    private ArrayList getLSDatabaseNames(String host, 
                                         String username,
                                         String password) 
        throws DatabaseConnectionException, DatabaseException,
               DatabaseDataException {

        ArrayList            lsDatabases = new ArrayList();
        ArrayList            databases;
        String               db;
        Configuration        config;
        DatabaseConnection   c;
        
        databases = getDatabaseNames(host, username, password);
        c = con.getConnection();
        for (int i = 0; i < databases.size(); i++) {
            db = (String) databases.get(i);
            config = new Configuration();
            try {
                c.setCatalog(db);
                config.read(c);
                if (config.get(Configuration.VERSION, null) != null) {
                    lsDatabases.add(db);
                }
            } catch (ConfigurationException ignore) {
                // Do nothing
            }
        }
        return lsDatabases;
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

