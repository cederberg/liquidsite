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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseConnectionException;
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
     * The selected database.
     */
    private String dbsel;
    
    /**
     * The entered database.
     */
    private String database;
    
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
     * Error in the username.
     */
    private boolean errorUsername;
    
    /**
     * Error in the password.
     */
    private boolean errorPassword;
    
    /**
     * Error in re-typed password.
     */
    private boolean errorVerify;
    
    /**
     * The database to create already exists.
     */
    private boolean errorDbExists;

    /**
     * Password verification failed.
     */
    private boolean errorVerification;
    
    /**
     * The liquid site user to create already exists.
     */
    private boolean errorUserExists;

    /**
     * Error in attempting to connect to the database.
     */
    private boolean errorConnection;

    /**
     * Technical error.
     */
    private boolean errorTech;

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
        closeConnector();
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
        String step = request.getParameter("step");
        if (step != null && step.equals("1")) {
            install2(request);
        } else if (step != null && step.equals("2")) {
            install3(request);
        } else if (step != null && step.equals("3")) {
            install4(request);
        } else if (step != null && step.equals("4")) {
            install5(request);
        } else {
            install(request);
        }
    }

    /**
     * Processes the request information and forwards to the first
     * page of the web installation.
     *
     * @param request           the request object
     */
    private void install(Request request) {
        // initialize error variables
        initializeErrors();
        
        // get request parameters
        getParameters(request);
        
        // set installation default values
        setDefaultValues();
        
        // set request attributes
        setAttributes(request);
        
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
        ArrayList dbsInfo = null;
        boolean isAdmin = false;

        // initialize error variables
        initializeErrors();

        // get request parameters
        getParameters(request);

        // check database connection info
        checkDbConnInfo();
        
        if (!error) {
            // close connector if already created
            closeConnector();
            
            // create connector if connector not already created
            createConnector(host, rootUsername, rootPassword);
            
            // get databases information 
            dbsInfo = getDatabasesInfo();
            
            // get if database user entered is administrator
            isAdmin = isAdministrator();
            
            // initialize dbsel variable
            initializeDbsel(dbsInfo);
        }

        if (errorTech) {
            request.forward("/install/error.jsp");

        } else {
            // set request attributes
            setAttributes(request);
        
            if (error) {
                request.forward("/install/install.jsp");
            } else {
                request.setAttribute("dbsInfo", dbsInfo);
                request.setAttribute("isAdmin", new Boolean(isAdmin));
                request.forward("/install/install2.jsp");
            }
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
        ArrayList dbsInfo = null;
        ArrayList users = null;
        boolean isAdmin = false;
                
        // initialize error variables
        initializeErrors();

        // get request parameters
        getParameters(request);

        // check database info
        checkDbInfo();

        if (! errorTech) {
            // if error, get the databases information,  
            // if not error, get the list of users
            if (error) {
                dbsInfo = getDatabasesInfo();
            } else {
                users = getUserNames();
            }

            // get whether the connection database user is admin
            isAdmin = isAdministrator();
        }
        
        if (errorTech) {
            request.forward("/install/error.jsp");

        } else {
            // set request attributes
            setAttributes(request);
            request.setAttribute("isAdmin", new Boolean(isAdmin));
        
            if (error) {
                request.setAttribute("dbsInfo", dbsInfo);
                request.forward("/install/install2.jsp");
            } else {
                request.setAttribute("users", users);
                request.forward("/install/install3.jsp");
            }
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

        // get request parameters
        getParameters(request);

        // get the list of users
        users = getUserNames();

        if (!error) {        
            // check user info
            checkUserInfo(users);
        }
        
        if (errorTech) {
            request.forward("/install/error.jsp");

        } else {
            // reset passwords if they didn't match  
            if (errorVerification) {
                password = "";
                verify = "";
            }
            
            // set request attributes
            setAttributes(request);

            if (error) {
                request.setAttribute("users", users);
                request.setAttribute("isAdmin", 
                    new Boolean(isAdministrator()));
                request.forward("/install/install3.jsp");
            } else {
                request.forward("/install/install4.jsp");
            }
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

        // get info from request
        getParameters(request);
        
        // get selected database and user
        db = (dbsel.equals("")) ? database : dbsel;
        user = (usersel.equals("")) ? username : usersel;

        try {
            // drop database if exists
            if (!dbsel.equals("")) {
                con.deleteDatabase(db);
            }

            // create database
            con.createDatabase(db);

            // create user if it doesn't exist
            con.createUser(user, password);

            // grant permissions to user
            con.addAccessPrivileges(db, user);

            // create tables
            createTables(db); 

            // create config file and table
            writeConfiguration(host, db, user, password);
            
        } catch (DatabaseConnectionException e) {
            error = true;
            errorConnection = true;
            e.printStackTrace();
        } catch (DatabaseException e) {
            error = true;
            errorTech = true;
            e.printStackTrace();
        } catch (ConfigurationException e) {
            error = true;
            errorTech = true;
            e.printStackTrace();
        }
        
        if (errorTech) {
            request.forward("/install/error.jsp");
        } else {
            getApplication().restart();
            request.forward("/install/install5.jsp");
        }
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
        dbsel = request.getParameter("dbsel");
        database = request.getParameter("database");
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
        request.setAttribute("dbsel", dbsel);
        request.setAttribute("database", database);
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
        request.setAttribute("errorVerification", errorVerification);
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
        if (database == null) {
            database = "liquidsite";
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
     * Initializes the variable dbsel.
     * 
     * @param dbsInfo        databases info from which to 
     *                       initialize dbsel
     */
    private void initializeDbsel(ArrayList dbsInfo) {
        if (dbsel == null) {
            if (isAdministrator()) {
                dbsel = "";
            } else if (dbsInfo.size() > 0) {
                dbsel = (String) ((Hashtable) dbsInfo.get(0)).
                    get("name");
            }

        } else {
            boolean contained = false;

            for (int i=0; i<dbsInfo.size() && !contained; i++) {
                if (dbsel.equals((String) 
                    ((Hashtable) dbsInfo.get(i)).get("name"))) {
                    contained = true;
                }
            }
            
            if (!contained) {
                if (isAdministrator()) {
                    dbsel = "";
                } else if (dbsInfo.size() > 0) {
                    dbsel = (String) ((Hashtable) dbsInfo.get(0)).
                        get("name");
                }
            }
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
        errorVerification = false;
        errorUserExists = false;
        errorConnection = false;
        errorTech = false;
    }

    /**
     * Checks the correctness of the host, root username, and root
     * password fields. If errors are found, the corresponding error 
     * variable are updated.
     */
    private void checkDbConnInfo() {
        // check host name
        if (host.equals("")) {
            error = true;
            errorHost = true;
        }
        
        // check username
        if (rootUsername.equals("")) {
            error = true;
            errorUsername = true;
        }
        
        // check password
        if (rootPassword.equals("")) {
            error = true;
            errorPassword = true;
        }
    }

    /**
     * Checks the correctness of the database selection information.
     */
    private void checkDbInfo() {
        if (dbsel.equals("") && database.equals("")) {
            error = true;
        } else if (dbsel.equals("") && databaseExists(database)) {
            error = true;
            errorDbExists = true;
        }
    }

    /**
     * Checks the correctness of the user selection information.
     *
     * @param users          the names of the database users 
     */
    private void checkUserInfo(ArrayList users) {
        if (isAdministrator()) {
            if (usersel.equals("")) {
                if (username.equals("")) {
                    error = true;
                    errorUsername = true;
                }
                if (password.equals("")) {
                    error = true;
                    errorPassword = true;
                }
                if (verify.equals("")) {
                    error = true;
                    errorVerify = true;
                }
                if (users.contains(username)) {
                    error = true;
                    errorUserExists = true;
                }
        
                if (!errorPassword && !errorVerify && 
                    !password.equals(verify)) {
                    error = true;
                    errorVerification = true;
                }

            } else {
                if (password.equals("")) {
                    error = true;
                    errorPassword = true;
                } else {
                    MySQLDatabaseConnector c;

                    // set a connection and perform a query
                    c = new MySQLDatabaseConnector(host, usersel, 
                        password);
                    try {
                        c.setPoolSize(1);
                        c.listDatabases();
                    } catch (Exception e) {
                        error = true;
                        errorVerification = true;
                    } finally {
                        // free connection
                        c.setPoolSize(0);
                        try {
                            c.update();
                        } catch (Exception ignore) {
                            // Do ignore
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a connector to the database.
     *
     * @param host           the host name
     * @param database       the database name
     * @param user           the user name
     * @param password       the user password
     */
    private void createConnector(String host, 
                                 String username, 
                                 String password) {
        con = new MySQLDatabaseConnector(host, username, password);
        con.setPoolSize(1);
    }

    /**
     * Closes the connector if it was created.
     */
    private void closeConnector() {
        if (con != null) {
            con.setPoolSize(0);
            try {
                con.update();
            } catch (DatabaseConnectionException ignore) {
                // Do ignore
            }
            con = null;
        }
    }

    /**
     * Checks if the connector's database user is administrator.
     * 
     * @return true if the connector's database user is administrator,
     *         false otherwise
     */

    private boolean isAdministrator() {
        boolean isAdmin = false;
        
        try {
            isAdmin = con.isAdministrator();
        } catch (DatabaseConnectionException e) {
            error = true;
            errorConnection = true;
            e.printStackTrace();
        } catch (DatabaseException e) {
            error = true;
            errorTech = true;
            e.printStackTrace();
        }
        
        return isAdmin;
    }

    /**
     * Returns a list with information about all databases. Each
     * component of the returned list is a list with the database
     * name, whether it it a liquid site database, and what type of
     * access it is allowed to the user used to create the connector
     * with.
     * 
     * @return a list with database names
     * 
     * @see #getDatabaseInfo
     */
    private ArrayList getDatabasesInfo() {
        ArrayList            dbs = new ArrayList();
        ArrayList            dbsInfo = new ArrayList();
        Hashtable            info;
        
        try {
            // load database functions
            con.loadFunctions(getFile("WEB-INF/database.properties"));

            // get databases information
            dbs = con.listDatabases();
            for (int i = 0; i < dbs.size(); i++) {
                info = getDatabaseInfo((String) dbs.get(i));
                dbsInfo.add(info);
            }
        } catch (FileNotFoundException e) {
            error = true;
            errorTech = true;
            e.printStackTrace();
        } catch (IOException e) {
            error = true;
            errorTech = true;
            e.printStackTrace();
        } catch (DatabaseException e) {
            error = true;
            errorTech = true;
            e.printStackTrace();
        } catch (DatabaseConnectionException e) {
            error = true;
            errorConnection = true;
            e.printStackTrace();
        }
        
        return dbsInfo;
    }

    /**
     * Returns information about a database as a hash table. The 
     * information returned is the database name, how many tables
     * were found in the database, whether it it a liquid site 
     * database, whether conflicts were detected in the database
     * table names, and whether the database has reading access.
     * 
     * A database has conflicts in its table names if it is not a
     * liquid site database, and contains at least one table 
     * beginning with "LS_".
     * 
     * @param db             a database name
     * @param c              a connection
     * 
     * @return a hash table with the database information
     * 
     * @throws DatabaseConnectionException if the connection failed
     */
    private Hashtable getDatabaseInfo(String db) 
        throws DatabaseConnectionException {

        DatabaseConnection c;
        Hashtable          info = new Hashtable(3);
        Configuration      config = new Configuration();
        ArrayList          tables = null;
        int                noTables = 0;
        String             type = "normal";
        boolean            conflict;
        
        try {
            // get list of tables in database
            tables = con.listTables(db);
            noTables = tables.size();

            // open a database connection
            c = con.getConnection();
            
            try {
                // check if LiquidSite database
                c.setCatalog(db);
                config.read(c);
                type = config.get(Configuration.VERSION, "normal");
            } catch (DatabaseException e) {
                type = "noaccess";
            } catch (ConfigurationException ignore) {
                // Do ignore
            } finally {
                // close the database connection
                con.returnConnection(c);
            }
            
            // check for conflicts in table names
            if (type.equals("normal")) {
                conflict = false;
                for (int i=0; i<noTables && !conflict; i++) {
                    if (((String) tables.get(i)).startsWith("LS_")) {
                        type = "conflict";
                        conflict = true;
                    }
                }
            }

        } catch (DatabaseException e) {
            // database not readable
            type = "noaccess";
            noTables = 0;
        }
        
        
        // insert information
        info.put("name", db);
        info.put("noTables", new Integer(noTables));
        info.put("type", type);
                        
        return info;
    }

    /**
     * Returns a list with all database names.
     * 
     * @return a list with database names
     */
    private ArrayList getDatabaseNames() {
        ArrayList databases = new ArrayList();
        
        // get list of databases
        try {
            databases = con.listDatabases();
        } catch (DatabaseConnectionException e) {
            error = true;
            errorConnection = true;
            e.printStackTrace();
        } catch (DatabaseException e) {
            error = true;
            errorTech = true;
            e.printStackTrace();
        }
        
        return databases;
    }

    /**
     * Returns whether a database exists, given its name.
     * 
     * @param db             the database name
     * 
     * @return true if the database exists, or
     *         false otherwise
     */
    private boolean databaseExists(String db) {
        ArrayList databases;
        
        databases = getDatabaseNames();
        return databases.contains(db);
    }
    
    /**
     * Returns a list with all user names.
     * 
     * @return a list with the user names
     */
    private ArrayList getUserNames() {
        ArrayList users = new ArrayList();
        
        // get list of users
        if (isAdministrator()) {
            try {
                users = con.listUsers();
            } catch (DatabaseConnectionException e) {
                error = true;
                errorConnection = true;
                e.printStackTrace();
            } catch (DatabaseException e) {
                error = true;
                errorTech = true;
                e.printStackTrace();
            }
            
        } else {
            users.add(rootUsername);
        }
            
        return users;
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

    /**
     * Creates the LiquidSite tables in a given database.
     *
     * @param database       the database name
     *
     * @throws DatabaseConnectionException if the connection to the
     *             database couldn't be established
     * @throws DatabaseException if the tables couldn't be created
     */
    private void createTables(String database) 
        throws DatabaseConnectionException, DatabaseException {
        
        DatabaseConnection c;
        
        c = con.getConnection();
        try {
            c.setCatalog(database);
            c.executeSql(getFile("WEB-INF/sql/create-tables.sql"));
        } catch (FileNotFoundException e) {
            throw new DatabaseException("couldn't find file " +
                "'create-tables.sql'", e);
        } catch (IOException e) {
            throw new DatabaseException("couldn't read file " +
                "'create-tables.sql'", e);
        } finally {
            con.returnConnection(c);
        }
    }
    
    /**
     * Writes the configuration file and database table. 
     *
     * @param host           the host name
     * @param database       the database name
     * @param username       the user name
     * @param password       the user password
     *
     * @throws DatabaseConnectionException if a connection to
     *             the database could not be established
     * @throws DatabaseException if the given database name does
     *             not exist
     * @throws ConfigurationException if the configuration
     *             could not be written
     */
    private void writeConfiguration(String host,
                                    String database,
                                    String username, 
                                    String password) 
        throws DatabaseConnectionException, DatabaseException, 
               ConfigurationException {

        DatabaseConnection c;
        Configuration      config;
        
        // load functions
        try {
            con.loadFunctions(getFile("WEB-INF/database.properties"));
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("cannot find file " +
                "'database.properties'", e);
        } catch (IOException e) {
            throw new ConfigurationException("cannot read file " +
                "'database.properties'", e);
        }
        
        // get the application configuration
        config = getApplication().getConfig();

        // open a database connection
        c = con.getConnection();
            
        try {
            // select database
            c.setCatalog(database);
        } finally {
            // close the database connection
            con.returnConnection(c);
        }

        // set configuration information
        config.set(Configuration.VERSION, "1.0");
        config.set(Configuration.DATABASE_HOSTNAME, host);
        config.set(Configuration.DATABASE_NAME, database);
        config.set(Configuration.DATABASE_USER, username);
        config.set(Configuration.DATABASE_PASSWORD, password);
        
        // write out configuration
        config.write(c);
    }
}

