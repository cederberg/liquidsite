/*
 * FrontControllerServlet.java
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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class FrontControllerServlet extends HttpServlet 
    implements Application {

    /**
     * The application monitor thread.
     */
    private ApplicationMonitor monitor = null;

    /**
     * The application configuration.
     */
    private Configuration config = null;
    
    /**
     * The application database connector.
     */
    private DatabaseConnector database = null;

    /**
     * The installation controller.
     */
    // TODO: replace with array of controllers
    private InstallController install;

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
        File    file;
        String  host;
        String  name;
        String  user;
        String  password;
        int     size;
        
        // Initialize configuration
        file = new File(getBaseDir(), "WEB-INF/config.properties");
        config = new Configuration(file);

        // Initialize database
        try {
            MySQLDatabaseConnector.loadDriver();
        } catch (DatabaseConnectionException e) {
            // TODO: log this
        }
        host = config.get(Configuration.DATABASE_HOSTNAME, "");
        name = config.get(Configuration.DATABASE_NAME, "");
        user = config.get(Configuration.DATABASE_USER, "");
        password = config.get(Configuration.DATABASE_PASSWORD, "");
        size = config.getInt(Configuration.DATABASE_POOL_SIZE, 0);
        database = new MySQLDatabaseConnector(host, name, user, password);
        database.setPoolSize(size);
        
        // Read configuration table
        try {
            config.read(database);
        } catch (ConfigurationException e) {
            // TODO: log this
        }

        // Initialize controllers
        // TODO: only create install if no config file
        install = new InstallController(this);
    }
    
    /**
     * Shuts down the application. This will deinitialize all 
     * controllers and the database.
     */
    public void shutdown() {
        // TODO: deinitialize all controllers
        install = null;
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
     * Handles an incoming HTTP request.
     * 
     * @param request        the HTTP request object
     * @param response       the HTTP response object
     * 
     * @throws ServletException if the request couldn't be handled by
     *             this servlet
     * @throws IOException if an IO error occured while attempting to
     *             service this request
     */
    public void service(HttpServletRequest request, 
                        HttpServletResponse response) 
        throws ServletException, IOException {

        Request            r = new Request(request, response);
        RequestDispatcher  disp;
        String             str;
        
        
        // Process request
        process(r);
        
        // Handle response
        if (r.isProcessed()) {
            // Do nothing
        } else if (r.isForward()) {
            str = r.getForwardPath();
            disp = getServletContext().getRequestDispatcher(str);
            if (disp != null) {
                disp.forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND); 
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND); 
        }
    }
    
    /**
     * Processes an incoming request.
     * 
     * @param request        the request object
     */
    private void process(Request request) {
        // TODO: implement this method properly
        install.process(request);
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
        public void run() {
            while (alive) {

                // Update database
                databaseCounter++;
                if (databaseCounter >= DATABASE_UPDATE_THRESHOLD) {
                    databaseCounter = 0;
                    try {
                        getDatabase().update();
                    } catch (DatabaseConnectionException e) {
                        // TODO: log this exception
                    }
                }

                // Delay execution
                try {
                    Thread.sleep(LOOP_DELAY);
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
        public void stop() {
            alive = false;
            try {
                wait(STOP_TIMEOUT);
            } catch (InterruptedException ignore) {
                // Do nothing
            }
        }
    }
}
