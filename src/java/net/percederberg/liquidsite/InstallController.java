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

import net.percederberg.liquidsite.db.*;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Vector;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A controller for the installation process.
 *
 * @author   Marielle Fois, <marielle at kth dot se>
 * @version  1.0
 */
public class InstallController implements Controller {

    /**
     * The front controller servlet.
     */
    FrontControllerServlet servlet;

    /**
     * Creates a new install controller. 
     *
     * @param servlet           the front controller servlet
     */
    public InstallController(FrontControllerServlet servlet) {
        this.servlet = servlet;
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

    /*
     * Forwards to the start installation page.
     *
     * @param request           the request object
     */
    private void start(Request request) {
        request.forward("/install/start.jsp");
    }

    /*
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

    /*
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
        if (! error) {
            // get list of databases
            try {
                databases =
                    getVectorDatabases(host, rootUsername, rootPassword);
                lsdatabases =
                    getVectorLSDatabases(host, rootUsername, rootPassword);
            } catch (DatabaseConnectionException e) {
                // could not open connection
                error = true;
                errorConnection = true;
                databases = null;
                lsdatabases = null;
                // TODO write log
                e.printStackTrace();
            } catch (SQLException e) {
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

    /*
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
                getVectorDatabases(host, rootUsername, rootPassword);
        } catch (DatabaseConnectionException e) {
            // could not open connection
            error = true;
            errorConnection = true;
            // TODO write log
            e.printStackTrace();
        } catch (SQLException e) {
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
                users = getVectorUsers(host, rootUsername, rootPassword);
                // TODO uncomment 
//                  if (dbchoice.equals("select")) {
//                      lsusers = getVectorLSUsers(dbsel, host, rootUsername, 
//                                                 rootPassword);
//                  } else {
//                      lsusers = getVectorLSUsers(database, host, rootUsername, 
//                                                 rootPassword);
//                  }
            } catch (DatabaseConnectionException e) {
                // could not open connection
                error = true;
                errorConnection = true;
                // TODO write log
                e.printStackTrace();
            } catch (SQLException e) {
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

    /*
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
            users = getVectorUsers(host, rootUsername, rootPassword);
        } catch (DatabaseConnectionException e) {
            // could not open connection
            error = true;
            errorConnection = true;
            // TODO write log
            e.printStackTrace();
        } catch (SQLException e) {
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
        if (! error && ! password.equals(verify)) {
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

    /*
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
        } catch (SQLException e) {
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

    private Vector getVectorDatabases(String host, String username,
                                      String password) 
        throws DatabaseConnectionException, SQLException {

        String query;
        ResultSet result;
        Vector databases;

        // get all databases 
        query = "show databases";
        result =  executeQuery("mysql", query, host, username, password);

        // create vector of databases
        databases = new Vector();
        while (result.next()) {
            databases.addElement(result.getString(1));
        }
        result.close();

        return databases;
    }

    private Vector getVectorLSDatabases(String host, String username,
                                        String password) 
        throws DatabaseConnectionException, SQLException {

        String query;
        ResultSet result1, result2;
        boolean nameExists, valueExists;
        Vector databases;
        Vector lsdatabases;
        String database;

        // get all databases
        databases = getVectorDatabases(host, username, password);

        // create a vector with all liquid site databases
        lsdatabases = new Vector();
        for (int i=0; i<databases.size(); i++) {
            database = (String) databases.elementAt(i);
            // get database tables
            query = "show tables";
            result1 = executeQuery(database, query, host, username, password);
            while (result1.next()) {
                if (result1.getString(1).equals("Configuration")) {
                    query = "show columns from Configuration";
                    result2 = executeQuery(database, query, host, 
                                           username, password);
                    nameExists = false;
                    valueExists = false;
                    while (result2.next() && (! nameExists || ! valueExists)) {
                        if (result2.getString(1).equals("name")) {
                            nameExists = true;
                        }
                        if (result2.getString(1).equals("value")) {
                            valueExists = true;
                        }
                    }
                    result2.close();
                    if (nameExists && valueExists) {
                        query = "select * from Configuration where name = " +
                            "'name' and value = 'Liquid Site'";
                        result2 = executeQuery(database, query, host, 
                                               username, password);
                        if (result2.next()) {
                            lsdatabases.addElement(databases.elementAt(i));
                        }
                        result2.close();
                    }
                }
            }
            result1.close();
        }

        return lsdatabases;
    }

    private Vector getVectorUsers(String host, String username,
                                  String password) 
        throws DatabaseConnectionException, SQLException {

        String query;
        ResultSet result;
        Vector users;

        // get the list of database users
        query = "select User from user where Password != ''";
        result =  executeQuery("mysql", query, host, username, password);

        // create vector of users
        users = new Vector();
        while (result.next()) {
            users.addElement(result.getString(1));
        }
        result.close();

        return users;
    }

    private Vector getVectorLSUsers(String database, String host, 
                                    String username, String password) 
        throws DatabaseConnectionException, SQLException {

        String query;
        ResultSet result;
        Vector users;
        Vector lsusers;

        // get all users
        users = getVectorUsers(host, username, password);

        // get Liquid Site users
        lsusers = new Vector();
        for (int i=0; i<users.size(); i++) {
            // TODO write query
            query = "";
            result =  executeQuery("mysql", query, host, username, password);
            if (result.next()) {
                lsusers.addElement(users.elementAt(i));
            }
            result.close();
        }

        return lsusers;
    }

    private String getUserPassword(String user, String host, 
                                   String username, String password) 
        throws DatabaseConnectionException, SQLException {

        String query;
        ResultSet result;
        String passwd;

        // get user password
        query = "select Password from user where User='" + user + "'";
        result =  executeQuery("mysql", query, host, username, password);
        if (result.next()) {
            passwd = result.getString(1);
        } else {
            passwd = "";
        }
        result.close();

        return passwd;
    }

    private void dropDatabase(String database, String host, 
                              String username, String password) 
        throws DatabaseConnectionException, SQLException {

        String query;

        // drop database
        query = "drop database " + database;
        execute("mysql", query, host, username, password);
    }

    private void createDatabase(String database, String host, 
                                String username, String password) 
        throws DatabaseConnectionException, SQLException {

        String query;

        // drop database
        query = "create database " + database;
        execute("mysql", query, host, username, password);
    }

    private void grantLSPermissions(String database, String user, 
                                    String passwd, String host, 
                                    String username, String password) 
        throws DatabaseConnectionException, SQLException {

        String query;

        // grant permissions
        query = "grant select on " + database + ".* to " + user;
        if (password != null) {
            query += " identified by password '" + passwd + "'";
        }
        execute(database, query, host, username, password);
        query = "grant insert on " + database + ".* to " + user;
        if (password != null) {
            query += " identified by password '" + passwd + "'";
        }
        execute(database, query, host, username, password);
        query = "grant update on " + database + ".* to " + user;
        if (password != null) {
            query += " identified by password '" + passwd + "'";
        }
        execute(database, query, host, username, password);
        query = "grant delete on " + database + ".* to " + user;
        if (password != null) {
            query += " identified by password '" + passwd + "'";
        }
        execute(database, query, host, username, password);
    }

    private ResultSet executeQuery(String database, String query, String host, 
                                   String username, String password) 
        throws DatabaseConnectionException, SQLException {

        MySQLDatabaseConnector connector;
        DatabaseConnection con;
        PreparedStatement prepStm;

        // open a connection to the database server
        connector =
            new MySQLDatabaseConnector(host, database, username, password);
        con = connector.getConnection();

        // return result of executing the query
        prepStm = con.prepareStatement(query);
        return prepStm.executeQuery();
    }

    private void execute(String database, String query, String host, 
                         String username, String password) 
        throws DatabaseConnectionException, SQLException {

        MySQLDatabaseConnector connector;
        DatabaseConnection con;
        PreparedStatement prepStm;

        // open a connection to the database server
        connector =
            new MySQLDatabaseConnector(host, database, username, password);
        con = connector.getConnection();

        // execute the query
        prepStm = con.prepareStatement(query);
        prepStm.execute();
    }

    private void createTables(String database, String host, 
                              String username, String password) 
        throws DatabaseConnectionException, SQLException,
               FileNotFoundException, IOException {

        String filename;
        FileInputStream queriesFile;
        PropertyResourceBundle queries;
        String query;

        // read properties file
        filename = servlet.getServletContext().getRealPath("/") + 
            "WEB-INF/db_create_tables.properties";
        queriesFile = new FileInputStream(filename);
        queries = new PropertyResourceBundle(queriesFile);

        // perform all queries
        for (Enumeration e = queries.getKeys(); e.hasMoreElements();) {
            execute(database, queries.getString((String) e.nextElement()), 
                    host, username, password);
        }
    }

    private void initTables(String database, String host, 
                            String username, String password) 
        throws DatabaseConnectionException, SQLException,
               FileNotFoundException, IOException {

        String filename;
        FileInputStream queriesFile;
        PropertyResourceBundle queries;
        String query;

        // read properties file
        filename = servlet.getServletContext().getRealPath("/") + 
            "WEB-INF/db_init.properties";
        queriesFile = new FileInputStream(filename);
        queries = new PropertyResourceBundle(queriesFile);

        // perform all queries
        for (Enumeration e = queries.getKeys(); e.hasMoreElements();) {
            execute(database, queries.getString((String) e.nextElement()), 
                    host, username, password);
        }
    }

    private void createConfigProp(String host, String username, 
                                  String password) 
        throws FileNotFoundException {

        String filename;
        PrintStream config;

        // create print stream
        filename = servlet.getServletContext().getRealPath("/") + 
            "WEB-INF/config.properties";
        config = new PrintStream(new FileOutputStream(filename));

        // write to stream
        config.println("HOST=" + host);
        config.println("USERNAME=" + username);
        config.println("PASSWORD=" + password);
    }
}
