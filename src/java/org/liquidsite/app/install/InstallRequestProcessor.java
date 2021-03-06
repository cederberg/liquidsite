/*
 * InstallRequestProcessor.java
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

package org.liquidsite.app.install;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.liquidsite.app.servlet.Application;
import org.liquidsite.app.servlet.Configuration;
import org.liquidsite.app.servlet.ConfigurationException;
import org.liquidsite.app.servlet.RequestException;
import org.liquidsite.app.servlet.RequestProcessor;
import org.liquidsite.app.template.TemplateException;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.ContentSite;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.Request;
import org.liquidsite.util.db.DatabaseConnection;
import org.liquidsite.util.db.DatabaseConnectionException;
import org.liquidsite.util.db.DatabaseDataException;
import org.liquidsite.util.db.DatabaseException;
import org.liquidsite.util.db.DatabaseQuery;
import org.liquidsite.util.db.DatabaseResults;
import org.liquidsite.util.db.MySQLDatabaseConnector;
import org.liquidsite.util.log.Log;

/**
 * The installation request processor. This processor differs from
 * the other processors in that it uses instance variables to keep
 * session information. This makes this processor impossible to use
 * in a multi-user scenario, but the installation process is supposed
 * to be run by a single user.
 *
 * @author   Marielle Fois, <marielle at kth dot se>
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class InstallRequestProcessor extends RequestProcessor {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(InstallRequestProcessor.class);

    /**
     * The application context.
     */
    private Application application;

    /**
     * The installer to use. This variable is initially set to null if
     * no valid database connection has been made.
     */
    private Installer installer = null;

    /**
     * The MySQL database connector. This variable is set to null if
     * no valid database connection has been made.
     */
    private MySQLDatabaseConnector connector = null;

    /**
     * The description of the last error encountered. If this
     * variable is set to null, no error has ocurred.
     */
    private String lastError = null;

    /**
     * The database host name.
     */
    private String host = "localhost";

    /**
     * The database name.
     */
    private String database = "liquidsite";

    /**
     * The data directory.
     */
    private String dataDir = "/var/lib/liquidsite";

    /**
     * The database user used in the installation.
     */
    private String installUser = "";

    /**
     * The database user password used in the installation.
     */
    private String installPassword = "";

    /**
     * The database user name for Liquid Site.
     */
    private String databaseUser = "liquidsite";

    /**
     * The database user password for Liquid Site.
     */
    private String databasePassword = "";

    /**
     * The administrator user name for Liquid Site.
     */
    private String adminUser = "root";

    /**
     * The administrator user password for Liquid Site.
     */
    private String adminPassword = "";

    /**
     * The create database flag.
     */
    private boolean createDatabase = false;

    /**
     * The create database user flag.
     */
    private boolean createDatabaseUser = false;

    /**
     * The update version number. This is set to null if no update
     * should be attempted.
     */
    private String updateVersion = null;

    /**
     * Creates a new install request processor.
     *
     * @param app            the application context
     */
    public InstallRequestProcessor(Application app) {
        super(app.getContentManager(), app.getBaseDir());
        this.application = app;
    }

    /**
     * Destroys this request processor. This method frees all
     * internal resources used by this processor.
     */
    public void destroy() {
        closeConnector();
    }

    /**
     * Processes a request.
     *
     * @param request        the request object to process
     *
     * @throws RequestException if the request couldn't be processed
     */
    public void process(Request request) throws RequestException {
        String       path = request.getPath();
        String       step = request.getParameter("step", "");
        Domain       domain;
        ContentSite  site;

        // Fake request environment
        path = path.substring(request.getServletPath().length());
        domain = new Domain(getContentManager(), "ROOT");
        request.getEnvironment().setDomain(domain);
        site = new ContentSite(getContentManager(), domain);
        site.setDirectory(request.getServletPath());
        request.getEnvironment().setSite(site);

        // Process request
        lastError = null;
        if (path.startsWith("/liquidsite/")) {
            processLiquidSite(request, path.substring(12));
        } else if (!path.equals("/") && !path.equals("/install.html")) {
            throw RequestException.RESOURCE_NOT_FOUND;
        } else if (request.getParameter("prev", "").equals("true")) {
            processPrevious(request, step);
        } else if (step.equals("1")) {
            processStep1(request);
        } else if (step.equals("2")) {
            processStep2(request);
        } else if (step.equals("3")) {
            processStep3(request);
        } else if (step.equals("4")) {
            processStep4(request);
        } else if (step.equals("5")) {
            processStep5(request);
        } else {
            displayStep1(request);
        }
    }

    /**
     * Processes a request for the previous page.
     *
     * @param request        the request object to process
     * @param step           the request step
     */
    private void processPrevious(Request request, String step) {
        if (step.equals("5")) {
            displayStep4(request);
        } else if (step.equals("4")) {
            displayStep3(request);
        } else if (step.equals("3")) {
            displayStep2(request);
        } else {
            displayStep1(request);
        }
    }

    /**
     * Processes a request originating from step 1.
     *
     * @param request        the request object to process
     */
    private void processStep1(Request request) {
        host = request.getParameter("host", "").trim();
        installUser = request.getParameter("user", "").trim();
        installPassword = request.getParameter("password", "");
        createConnector();
        if (lastError == null && !isAdministrator()) {
            databaseUser = installUser;
            databasePassword = installPassword;
        }
        if (lastError != null) {
            displayStep1(request);
        } else {
            displayStep2(request);
        }
    }

    /**
     * Processes a request originating from step 2.
     *
     * @param request        the request object to process
     */
    private void processStep2(Request request) {
        createDatabase = false;
        updateVersion = null;
        database = request.getParameter("database1", "");
        if (database.equals("")) {
            createDatabase = true;
            database = request.getParameter("database2", "").trim();
        } else {
            updateVersion = getVersion(database);
        }
        if (database.equals("")) {
            lastError = "No database selected";
        } else if (createDatabase && listDatabases().contains(database)) {
            lastError = "Cannot create a database that already exists";
        }
        if (lastError != null) {
            displayStep2(request);
        } else {
            displayStep3(request);
        }
    }

    /**
     * Processes a request originating from step 3.
     *
     * @param request        the request object to process
     */
    private void processStep3(Request request) {
        MySQLDatabaseConnector  test;
        String                  str;

        // Extract form data
        createDatabaseUser = false;
        databaseUser = request.getParameter("user1", "");
        if (databaseUser.equals("")) {
            createDatabaseUser = true;
            databaseUser = request.getParameter("user2", "").trim();
        }
        databasePassword = request.getParameter("password1", "");
        str = request.getParameter("password2", "");

        // Validate form data
        if (databaseUser.equals("")) {
            lastError = "No user name selected";
        } else if (createDatabaseUser && !databasePassword.equals(str)) {
            lastError = "The two passwords must be identical";
            databasePassword = "";
        } else if (createDatabaseUser && listUsers().contains(databaseUser)) {
            lastError = "Cannot create a user that already exists";
        } else if (!createDatabaseUser) {
            if (createDatabase) {
                test = new MySQLDatabaseConnector(host,
                                                  databaseUser,
                                                  databasePassword);
            } else {
                test = new MySQLDatabaseConnector(host,
                                                  database,
                                                  databaseUser,
                                                  databasePassword);
            }
            try {
                test.returnConnection(test.getConnection());
            } catch (DatabaseConnectionException e) {
                lastError = "Couldn't connect to database with specified " +
                            "user name and password";
            }
        }

        // Print results
        if (lastError != null) {
            displayStep3(request);
        } else {
            displayStep4(request);
        }
    }

    /**
     * Processes a request originating from step 4.
     *
     * @param request        the request object to process
     */
    private void processStep4(Request request) {
        File    file;
        String  str;

        // Validate form data
        if (updateVersion == null) {
            dataDir = request.getParameter("dir", "").trim();
            adminUser = request.getParameter("user", "").trim();
            adminPassword = request.getParameter("password1", "");
            str = request.getParameter("password2", "");
            if (dataDir.equals("")) {
                lastError = "No data directory specified";
            } else if (adminUser.equals("")) {
                lastError = "No administrator user name specified";
            } else if (adminPassword.equals("")) {
                lastError = "No administrator password specified";
            } else if (!adminPassword.equals(str)) {
                lastError = "The two passwords must be identical";
                adminPassword = "";
            } else {
                file = new File(dataDir);
                if (!file.exists()) {
                    lastError = "Data directory does not exist";
                } else if (!file.canWrite()) {
                    lastError = "Cannot write to data directory, " +
                                "check permissions";
                }
            }
        }

        // Print results
        if (lastError != null) {
            displayStep4(request);
        } else {
            displayStep5(request);
        }
    }

    /**
     * Processes a request originating from step 5.
     *
     * @param request        the request object to process
     */
    private void processStep5(Request request) {

        // Write database and configuration
        try {
            if (createDatabase) {
                connector.createDatabase(database);
            }
            if (createDatabaseUser) {
                connector.createUser(databaseUser, databasePassword);
            }
            if (isAdministrator()) {
                connector.addAccessPrivileges(database, databaseUser);
            }
            if (updateVersion == null) {
                installer.createTables(database);
            } else {
                installer.updateTables(database, updateVersion);
            }
            writeConfiguration();
            createDirs();
            application.restart();
            if (updateVersion == null) {
                writeDefaultData(request.getProtocol(),
                                 request.getServletPath());
            }
        } catch (InstallException e) {
            LOG.error("couldn't finish installation", e);
            lastError = e.getMessage();
        } catch (DatabaseConnectionException e) {
            LOG.error("couldn't finish installation", e);
            lastError = e.getMessage();
        } catch (DatabaseException e) {
            LOG.error("couldn't finish installation", e);
            lastError = e.getMessage();
        } catch (FileNotFoundException e) {
            LOG.error("couldn't finish installation", e);
            lastError = e.getMessage();
        } catch (IOException e) {
            LOG.error("couldn't finish installation", e);
            lastError = e.getMessage();
        } catch (ConfigurationException e) {
            LOG.error("couldn't finish installation", e);
            lastError = e.getMessage();
        }

        // Display errors or start application
        if (lastError != null) {
            displayStep5(request);
        } else {
            request.sendRedirect("index.html");
        }
    }

    /**
     * Displays the step 1 page.
     *
     * @param request        the request object
     */
    private void displayStep1(Request request) {
        request.setAttribute("error", lastError);
        request.setAttribute("host", host);
        request.setAttribute("user", installUser);
        request.setAttribute("password", installPassword);
        displayTemplate(request, "install/install1.ftl");
    }

    /**
     * Displays the step 2 page.
     *
     * @param request        the request object
     */
    private void displayStep2(Request request) {
        String     originalError = lastError;
        ArrayList  databaseInfo = new ArrayList();
        boolean    enableNext = false;
        ArrayList  databases;
        ArrayList  tables;
        String     version;
        HashMap    info;
        String     str;

        // Find database information
        databases = listDatabases();
        for (int i = 0; i < databases.size(); i++) {
            info = new HashMap();
            databaseInfo.add(info);
            lastError = null;
            tables = listTables(databases.get(i).toString());
            if (tables.contains("LS_CONFIGURATION")) {
                version = getVersion(databases.get(i).toString());
            } else {
                version = null;
            }
            info.put("name", databases.get(i));
            info.put("tables", new Integer(tables.size()));
            if (lastError != null) {
                info.put("status", new Integer(0));
                info.put("info", "Couldn't read database");
            } else if (databases.get(i).equals("mysql")) {
                info.put("status", new Integer(0));
                info.put("info", "MySQL administration database");
            } else if (databases.get(i).equals("information_schema")) {
                info.put("status", new Integer(0));
                info.put("info", "MySQL information schema");
            } else if (version != null) {
                str = "Liquid Site version " + version;
                if (installer.canUpdate(version)) {
                    info.put("status", new Integer(1));
                } else {
                    str += " (Unsupported)";
                    info.put("status", new Integer(0));
                }
                info.put("info", str);
            } else if (getTableConflicts(tables) > 0) {
                str = getTableConflicts(tables) +
                      " conflicting tables found";
                info.put("status", new Integer(0));
                info.put("info", str);
            } else {
                info.put("status", new Integer(1));
                info.put("info", "");
                enableNext = true;
            }
        }
        lastError = originalError;
        if (!enableNext) {
            if (isAdministrator()) {
                enableNext = true;
            } else {
                lastError = "No databases available for selection";
            }
        }

        // Display database list
        request.setAttribute("error", lastError);
        request.setAttribute("database", database);
        request.setAttribute("databaseInfo", databaseInfo);
        request.setAttribute("enableCreate", isAdministrator());
        request.setAttribute("enableNext", enableNext);
        displayTemplate(request, "install/install2.ftl");
    }

    /**
     * Displays the step 3 page.
     *
     * @param request        the request object
     */
    private void displayStep3(Request request) {
        request.setAttribute("error", lastError);
        request.setAttribute("user", databaseUser);
        request.setAttribute("password", databasePassword);
        request.setAttribute("userNames", listUsers());
        request.setAttribute("enableCreate", isAdministrator());
        displayTemplate(request, "install/install3.ftl");
    }

    /**
     * Displays the step 4 page.
     *
     * @param request        the request object
     */
    private void displayStep4(Request request) {
        request.setAttribute("error", lastError);
        request.setAttribute("updateVersion", updateVersion);
        if (updateVersion == null) {
            request.setAttribute("dir", dataDir);
            request.setAttribute("user", adminUser);
            request.setAttribute("password", adminPassword);
        }
        displayTemplate(request, "install/install4.ftl");
    }

    /**
     * Displays the step 5 page.
     *
     * @param request        the request object
     */
    private void displayStep5(Request request) {
        request.setAttribute("error", lastError);
        request.setAttribute("host", host);
        request.setAttribute("database", database);
        request.setAttribute("databaseUser", databaseUser);
        request.setAttribute("createDatabase", createDatabase);
        request.setAttribute("createDatabaseUser", createDatabaseUser);
        request.setAttribute("updateVersion", updateVersion);
        if (updateVersion == null) {
            request.setAttribute("dataDir", dataDir);
            request.setAttribute("adminUser", adminUser);
        }
        displayTemplate(request, "install/install5.ftl");
    }

    /**
     * Processes a request to a template file. The template file name
     * is relative to the web context directory, and the output MIME
     * type will always be set to "text/html".
     *
     * @param request        the request object
     * @param templateName   the template file name
     */
    private void displayTemplate(Request request, String templateName) {
        try {
            sendTemplate(request, templateName);
        } catch (TemplateException e) {
            request.sendData("text/plain", "Error: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new database connector and tests it. If an old
     * database connector exists, it will be closed. The instance
     * variables are used for passing the connection details. As a
     * side-effect, this method will also log any error encountered,
     * and set the lastError variable.
     */
    private void createConnector() {
        if (connector != null) {
            closeConnector();
        }
        connector = new MySQLDatabaseConnector(host,
                                               installUser,
                                               installPassword);
        connector.setPoolSize(3);
        try {
            connector.loadFunctions(getFile("WEB-INF/database.properties"));
            connector.returnConnection(connector.getConnection());
            int[] ver = connector.getVersion();
            if (ver[0] < 5) {
                lastError = "MySQL server version 5.0.0 or higher is " +
                            "required, found version " + ver[0] + "." +
                            ver[1] + "." + ver[2];
                LOG.error(lastError);
                connector = null;
            } else {
                installer = new Installer(application.getBuildVersion(),
                                          connector,
                                          getFile("WEB-INF/sql"));
            }
        } catch (FileNotFoundException e) {
            LOG.error("couldn't read database functions", e);
            lastError = "Couldn't find 'database.properties' file";
            connector = null;
        } catch (IOException e) {
            LOG.error("couldn't read database functions", e);
            lastError = "Couldn't read 'database.properties' file";
            connector = null;
        } catch (DatabaseConnectionException e) {
            LOG.error("couldn't connect to database", e);
            lastError = e.getMessage();
            connector = null;
        } catch (DatabaseException e) {
            LOG.error("couldn't read database version", e);
            lastError = e.getMessage();
            connector = null;
        }
    }

    /**
     * Closes the connector if it was created.
     */
    private void closeConnector() {
        if (connector != null) {
            connector.setPoolSize(0);
            try {
                connector.update();
            } catch (DatabaseConnectionException ignore) {
                // Do ignore
            }
            connector = null;
            installer = null;
        }
    }

    /**
     * Checks if the current connector is has administrator
     * privileges. As a side-effect, this method will log any error
     * encountered, and set the lastError variable.
     *
     * @return true if the connector user is administrator, or
     *         false otherwise
     */
    private boolean isAdministrator() {
        try {
            return connector.isAdministrator();
        } catch (DatabaseConnectionException e) {
            LOG.error("couldn't connect to database", e);
            lastError = e.getMessage();
        } catch (DatabaseException e) {
            LOG.error("couldn't get user admin status", e);
            lastError = e.getMessage();
        }
        return false;
    }

    /**
     * Returns a list of databases found with the current connector.
     * As a side-effect, this method will log any error encountered,
     * and set the lastError variable.
     *
     * @return the list of database names found
     */
    private ArrayList listDatabases() {
        try {
            return connector.listDatabases();
        } catch (DatabaseConnectionException e) {
            LOG.error("couldn't connect to database", e);
            lastError = e.getMessage();
        } catch (DatabaseException e) {
            LOG.error("couldn't list databases", e);
            lastError = e.getMessage();
        }
        return new ArrayList();
    }

    /**
     * Returns a list of tables found in a specified database with
     * the current connector. As a side-effect, this method will log
     * any error encountered, and set the lastError variable.
     *
     * @param database       the database to check
     *
     * @return the list of database names found
     */
    private ArrayList listTables(String database) {
        try {
            return connector.listTables(database);
        } catch (DatabaseConnectionException e) {
            LOG.error("couldn't connect to database", e);
            lastError = e.getMessage();
        } catch (DatabaseException e) {
            LOG.error("couldn't list tables", e);
            lastError = e.getMessage();
        }
        return new ArrayList();
    }

    /**
     * Returns a list of all users found with the current connector.
     * This method will ignore any error encountered.
     *
     * @return the list with user names, or
     *         a list with the install user if no users could be found
     */
    private ArrayList listUsers() {
        ArrayList users;

        try {
            return connector.listUsers();
        } catch (DatabaseConnectionException ignore) {
            // Do nothing
        } catch (DatabaseException ignore) {
            // Do noting
        }
        users = new ArrayList();
        users.add(installUser);
        return users;
    }

    /**
     * Returns the Liquid Site version of the specified database. This
     * method will ignore any error encountered.
     *
     * @param database       the database to check
     *
     * @return the version number found in the database, or
     *         null if the database wasn't a Liquid Site database
     */
    private String getVersion(String database) {
        DatabaseConnection  con = null;
        DatabaseQuery       query = new DatabaseQuery("config.select.name");
        DatabaseResults     res;

        try {
            con = connector.getConnection();
            con.setCatalog(database);
            query.addParameter(Configuration.VERSION);
            res = con.execute(query);
            return res.getRow(0).getString(0);
        } catch (DatabaseConnectionException ignore) {
            // Do nothing
        } catch (DatabaseException ignore) {
            // Do nothing
        } catch (DatabaseDataException ignore) {
            // Do nothing
        } finally {
            if (con != null) {
                connector.returnConnection(con);
            }
        }
        return null;
    }

    /**
     * Returns the number of tables in a list that may cause
     * conflicts. A conflicting table is one that has a name starting
     * with "LS_".
     *
     * @param tables         the list of table names to check
     *
     * @return the number of conflicting table names
     */
    private int getTableConflicts(ArrayList tables) {
        int     conflicts = 0;
        String  name;

        for (int i = 0; i < tables.size(); i++) {
            name = ((String) tables.get(i)).toUpperCase();
            if (name.startsWith("LS_")) {
                conflicts++;
            }
        }
        return conflicts;
    }

    /**
     * Writes the Liquid Site configuration file and database table.
     *
     * @throws FileNotFoundException if the database functions file
     *             couldn't be found
     * @throws IOException if the database functions file couldn't be
     *             read
     * @throws ConfigurationException if the configuration couldn't
     *             be written
     */
    private void writeConfiguration()
        throws FileNotFoundException, IOException, ConfigurationException {

        MySQLDatabaseConnector  con = null;
        Configuration           config;
        Configuration           oldConfig;

        // Create database connector
        con = new MySQLDatabaseConnector(host,
                                         database,
                                         databaseUser,
                                         databasePassword);
        con.loadFunctions(getFile("WEB-INF/database.properties"));

        // Write configuration
        config = application.getConfig();
        if (updateVersion != null) {
            oldConfig = new Configuration();
            oldConfig.read(con);
            config.setAll(oldConfig);
        }
        config.set(Configuration.VERSION, application.getBuildVersion());
        config.set(Configuration.DATABASE_HOSTNAME, host);
        config.set(Configuration.DATABASE_NAME, database);
        config.set(Configuration.DATABASE_USER, databaseUser);
        config.set(Configuration.DATABASE_PASSWORD, databasePassword);
        config.set(Configuration.DATABASE_POOL_SIZE, 50);
        if (updateVersion == null) {
            config.set(Configuration.FILE_DIRECTORY, dataDir);
            config.set(Configuration.UPLOAD_DIRECTORY,
                       application.getBaseDir() + "/tmp");
            config.set(Configuration.UPLOAD_MAX_SIZE, 10000000);
        }
        config.write(con);
    }

    /**
     * Creates various required application directories.
     */
    private void createDirs()
    {
        Configuration  config;
        File           dir;

        // Create temporary upload directory
        config = application.getConfig();
        dir = new File(config.get(Configuration.UPLOAD_DIRECTORY,
        		                     application.getBaseDir() + "/tmp"));
        dir.mkdirs();

        // Create plugin directory
        dir = new File(application.getBaseDir(), "plugins");
        dir.mkdirs();
    }

    /**
     * Writes the Liquid Site database default data. The servlet path
     * will be used to place the admin site in the correct base
     * directory.
     *
     * @param protocol       the servlet protocol
     * @param path           the servlet path
     */
    private void writeDefaultData(String protocol, String path) {
        ContentManager  manager = application.getContentManager();
        Domain          domain = new Domain(manager, "ROOT");
        User            user = new User(manager, null, adminUser);
        ContentSite     site = new ContentSite(manager, domain);

        try {
            domain.setDescription("Root Domain");
            domain.save(user);
            user.setRealName("Administrator");
            user.setPassword(adminPassword);
            user.save(user);
            site.setName("Admin");
            site.setRevisionNumber(1);
            site.setProtocol(protocol);
            site.setHost("*");
            site.setPort(0);
            site.setDirectory(path);
            site.setAdmin(true);
            site.setOnlineDate(new Date());
            site.setOfflineDate(null);
            site.setComment("Created");
            site.save(user);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        } catch (ContentSecurityException e) {
            LOG.error(e.getMessage());
        }
    }
}
