/*
 * LiquidSiteServlet.java
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
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.db.DatabaseConnectionException;
import net.percederberg.liquidsite.db.DatabaseConnector;
import net.percederberg.liquidsite.db.MySQLDatabaseConnector;

/**
 * A front controller servlet. This class handles all incoming HTTP
 * requests.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class LiquidSiteServlet extends HttpServlet 
    implements Application {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(LiquidSiteServlet.class);

    /**
     * The application monitor thread.
     */
    private ApplicationMonitor monitor = null;

    /**
     * The application build and version properties.
     */
    private Properties build = new Properties();

    /**
     * The application configuration.
     */
    private Configuration config = null;
    
    /**
     * The application database connector.
     */
    private DatabaseConnector database = null;

    /**
     * The application content manager.
     */
    private ContentManager contentManager = null;

    /**
     * The currently active controller.
     */
    private Controller controller = null;
    
    /**
     * The application online flag.
     */
    private boolean online = false;

    /**
     * Checks if the application is running correctly. This method 
     * will return true if no major errors have been encountered, 
     * such as database connections are not working or similar. Note
     * that this method may return true even if the application is
     * not installed, assuming that the installed has been properly 
     * launched.  
     * 
     * @return true if the application is online and working, or
     *         false otherwise
     */    
    public boolean isOnline() {
        return online;
    }

    /**
     * Initializes this servlet.
     */
    public void init() {
        startup();
        monitor = new ApplicationMonitor();
    }

    /**
     * Uninitializes this servlet.
     */
    public void destroy() {
        monitor.stop();
        shutdown();
        super.destroy();
    }

    /**
     * Starts up the application. This will initialize the 
     * configuration, the database, and all the relevant controllers.
     */
    public void startup() {
        int     errors = 0;
        File    configDir;
        URL     url;
        String  host;
        String  name;
        String  user;
        String  password;
        String  str;
        int     size;

        // Initialize configuration
        configDir = new File(getBaseDir(), "WEB-INF");
        config = new Configuration(new File(configDir, "config.properties"));
        try {
            Log.initialize(new File(configDir, "logging.properties"));
        } catch (IOException e) {
            errors++;
            LOG.error("couldn't read logging configuration: " + 
                      e.getMessage());
        }

        // Initialize build information
        str = "net/percederberg/liquidsite/build.properties";
        url = getClass().getClassLoader().getResource(str);
        try {
            build.load(url.openStream());
        } catch (IOException e) {
            errors++;
            LOG.error(e.getMessage());
        }

        // Initialize database
        try {
            MySQLDatabaseConnector.loadDriver();
        } catch (DatabaseConnectionException e) {
            errors++;
            LOG.error(e.getMessage());
        }
        host = config.get(Configuration.DATABASE_HOSTNAME, "");
        name = config.get(Configuration.DATABASE_NAME, "");
        user = config.get(Configuration.DATABASE_USER, "");
        password = config.get(Configuration.DATABASE_PASSWORD, "");
        size = config.getInt(Configuration.DATABASE_POOL_SIZE, 0);
        database = new MySQLDatabaseConnector(host, name, user, password);
        database.setPoolSize(size);
        try {
            database.loadFunctions(new File(configDir, "database.properties"));
        } catch (IOException e) {
            errors++;
            LOG.error("couldn't read database configuration: " + 
                      e.getMessage());
        }
        
        // Read configuration table
        try {
            if (config.isInitialized()) {
                config.read(database);
            }
        } catch (ConfigurationException e) {
            errors++;
            LOG.error(e.getMessage());
        }

        // Initialize content manager
        try {
            contentManager = new ContentManager(this);
        } catch (ContentException e) {
            errors++;
            LOG.error(e.getMessage());
        }

        // Initialize controllers
        if (!config.isInitialized()) {
            controller = new InstallController(this);
        } else {
            controller = new DefaultController(this);
        }
        
        // Set the online status
        online = (errors == 0);
    }
    
    /**
     * Shuts down the application. This will deinitialize all 
     * controllers and the database.
     */
    public void shutdown() {
        controller.destroy();
        contentManager.close();
        database.setPoolSize(0);
        try {
            database.update();
        } catch (DatabaseConnectionException ignore) {
            // Do nothing
        }
    }

    /**
     * Restarts the application. This will perform a partial shutdown 
     * followed by a startup, flushing and rereading all 
     * datastructures, such as configuration, database connections,
     * and similar.
     */
    public void restart() {
        shutdown();
        startup();
    }

    /**
     * Handles an incoming HTTP GET request.
     * 
     * @param request        the HTTP request object
     * @param response       the HTTP response object
     * 
     * @throws ServletException if the request couldn't be handled by
     *             this servlet
     * @throws IOException if an IO error occured while attempting to
     *             service this request
     */
    protected void doGet(HttpServletRequest request, 
                         HttpServletResponse response) 
        throws ServletException, IOException {

        Request  r = new Request(request, response);
        
        // TODO: handle offline state gracefully
        // TODO: handle exceptions and error responses
        
        // Process request
        LOG.debug("Incoming request: " + r);
        try {
            controller.process(r);
            if (r.hasResponse()) {
                r.setAttribute("liquidsite.build.version", 
                               getBuildVersion());
                r.setAttribute("liquidsite.build.date", getBuildDate());
                r.commit(getServletContext());
            } else {
                LOG.debug("Unhandled request: " + r);
                response.sendError(HttpServletResponse.SC_NOT_FOUND); 
            }
        } catch (RequestException e) {
            LOG.debug("Erroneous request: " + r + ", Message: " + 
                      e.getMessage());
            response.sendError(e.getCode(), e.getMessage()); 
        }
    }
    
    /**
     * Handles an incoming HTTP POST request.
     * 
     * @param request        the HTTP request object
     * @param response       the HTTP response object
     * 
     * @throws ServletException if the request couldn't be handled by
     *             this servlet
     * @throws IOException if an IO error occured while attempting to
     *             service this request
     */
    protected void doPost(HttpServletRequest request, 
                          HttpServletResponse response)
        throws ServletException, IOException {

        doGet(request, response);
    }

    /**
     * Returns the application build version number.
     * 
     * @return the application build version number
     */
    public String getBuildVersion() {
        return build.getProperty("build.version", "<unknown>");
    }

    /**
     * Returns the application build date.
     * 
     * @return the application build date
     */
    public String getBuildDate() {
        return build.getProperty("build.date", "<unknown>");
    }

    /**
     * Returns the base application directory. This is the directory
     * containing all the application files (i.e. the corresponding
     * webapps directory).
     * 
     * @return the base application directory
     */
    public File getBaseDir() {
        return new File(getServletContext().getRealPath("/"));
    }

    /**
     * Returns the application configuration. The object returned by
     * this method will not change, unless a reset is made, but the
     * parameter values in the configuration may be modified.
     * 
     * @return the application configuration
     */
    public Configuration getConfig() {
        return config;
    }
    
    /**
     * Returns the application database connector. The object 
     * returned by this method will not change, unless a reset is 
     * made.
     * 
     * @return the application database connector
     */
    public DatabaseConnector getDatabase() {
        return database;
    }
    
    /**
     * Returns the application content manager. The object returned 
     * by this method will not change, unless a reset is made.
     * 
     * @return the application content manager
     */
    public ContentManager getContentManager() {
        return contentManager;
    }

    /**
     * An application monitor thread. This thread call the database
     * update method regularly, and performs other monitoring tasks.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    private class ApplicationMonitor implements Runnable {

        /**
         * The loop delay in milliseconds. The thread will wait for 
         * this period of time in each pass of the monitor loop.   
         */
        private static final int LOOP_DELAY = 100;
        
        /**
         * The stop timeout in milliseconds.
         */
        private static final int STOP_TIMEOUT = 1000;

        /**
         * The database update threshold. This is the number of loop 
         * iterations to skip in between calls to update() in the 
         * database connector.
         */
        private static final int DATABASE_UPDATE_THRESHOLD = 600;

        /**
         * The alive flag. If this flag is set to false, the thread 
         * is supposed to die.
         */
        private boolean alive = true;

        /**
         * The database update counter. This counter is increased 
         * every second and is used to determine when the database
         * update method should be called.
         */
        private int databaseCounter = 0;

        /**
         * Creates a new application monitor. This will also create 
         * and start the actual thread running this monitor.
         */
        public ApplicationMonitor() {
            Thread  thread;
            
            thread = new Thread(this);
            thread.start();
        }

        /**
         * Runs the thread. This method is not supposed to be called
         * directly, but rather by the monitor thread.
         */
        public synchronized void run() {
            while (alive) {

                // Update database
                databaseCounter++;
                if (databaseCounter >= DATABASE_UPDATE_THRESHOLD) {
                    databaseCounter = 0;
                    try {
                        if (config == null || !config.isInitialized()) {
                            // Do nothing
                        } else if (!isOnline()) {
                            restart();
                        } else {
                            getDatabase().update();
                        }
                    } catch (DatabaseConnectionException e) {
                        // TODO: restart() if repeated various times
                        LOG.error(e.getMessage());
                    }
                }

                // Delay execution
                try {
                    wait(LOOP_DELAY);
                } catch (InterruptedException ignore) {
                    // Do nothing
                }
            }
            notifyAll();
        }
        
        /**
         * Stops the monitor thread. Once the thread is stopped, it
         * cannot be started again. This method will not return until
         * the monitor thread has stopped, or a timeout has passed.
         */
        public synchronized void stop() {
            alive = false;
            try {
                notifyAll();
                wait(STOP_TIMEOUT);
            } catch (InterruptedException ignore) {
                // Do nothing
            }
        }
    }
}
