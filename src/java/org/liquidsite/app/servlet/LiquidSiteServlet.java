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
 * Copyright (c) 2003-2006 Per Cederberg. All rights reserved.
 */

package org.liquidsite.app.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.liquidsite.app.install.InstallRequestProcessor;
import org.liquidsite.app.plugin.PluginException;
import org.liquidsite.app.plugin.PluginLoader;
import org.liquidsite.app.template.TemplateException;
import org.liquidsite.app.template.TemplateManager;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.data.DataObjectException;
import org.liquidsite.core.data.DataSource;
import org.liquidsite.core.data.LockPeer;
import org.liquidsite.core.web.MultiPartRequest;
import org.liquidsite.core.web.Request;
import org.liquidsite.util.db.DatabaseConnectionException;
import org.liquidsite.util.db.DatabaseConnector;
import org.liquidsite.util.db.MySQLDatabaseConnector;
import org.liquidsite.util.log.Log;
import org.liquidsite.util.mail.MailTransportException;
import org.liquidsite.util.mail.MailQueue;

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
    protected static final Log LOG = new Log(LiquidSiteServlet.class);

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
     * The application plugin loader.
     */
    private PluginLoader pluginLoader = null;

    /**
     * The application database connector.
     */
    private DatabaseConnector database = null;

    /**
     * The application content manager.
     */
    private ContentManager contentManager = null;

    /**
     * The main request processor.
     */
    private RequestProcessor processor = null;

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
     * configuration, the database, and the relevant request
     * processor.
     */
    public void startup() {
        int     errors = 0;
        File    dir;
        URL     url;
        String  host;
        String  name;
        String  user;
        String  password;
        String  str;
        int     size;

        // Initialize configuration
        dir = new File(getBaseDir(), "WEB-INF");
        config = new Configuration(new File(dir, "config.properties"));
        try {
            Log.initialize(new File(dir, "logging.properties"));
        } catch (IOException e) {
            errors++;
            LOG.error("couldn't read logging configuration: " +
                      e.getMessage());
        }

        // Initialize build information
        str = "org/liquidsite/build.properties";
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
            database.loadFunctions(new File(dir, "database.properties"));
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

        // Initialize mail queue
        host = config.get(Configuration.MAIL_HOST, "localhost");
        user = config.get(Configuration.MAIL_USER, null);
        str = config.get(Configuration.MAIL_FROM, null);
        MailQueue.getInstance().initialize(host, user, str);
        str = config.get(Configuration.MAIL_HEADER, null);
        MailQueue.getInstance().setHeader(str);
        str = config.get(Configuration.MAIL_FOOTER, null);
        MailQueue.getInstance().setFooter(str);

        // Initialize content and template managers
        str = config.get(Configuration.FILE_DIRECTORY, null);
        dir = (str == null) ? null : new File(str);
        contentManager = new ContentManager(database, dir, false);
        try {
            TemplateManager.initialize(getBaseDir(),
                                       getBuildVersion(),
                                       getBuildDate());
        } catch (TemplateException e) {
            errors++;
            LOG.error(e.getMessage());
        }

        // Initialize plugin loader
        pluginLoader = new PluginLoader();
        try {
            pluginLoader.startup(new File(getBaseDir(), "plugins"));
        } catch (PluginException e) {
            errors++;
            LOG.error(e.getMessage());
        }

        // Initialize request processor
        if (!config.isInitialized()) {
            processor = new InstallRequestProcessor(this);
        } else {
            processor = new DefaultRequestProcessor(this);
        }

        // Set the online status
        online = (errors == 0);
    }

    /**
     * Shuts down the application. This will deinitialize the request
     * processor and the database connector.
     */
    public void shutdown() {
        processor.destroy();
        pluginLoader.shutdown();
        contentManager.reset();
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

        process(request, response, true);
    }

    /**
     * Handles an incoming HTTP HEAD request.
     *
     * @param request        the HTTP request object
     * @param response       the HTTP response object
     *
     * @throws ServletException if the request couldn't be handled by
     *             this servlet
     * @throws IOException if an IO error occured while attempting to
     *             service this request
     */
    protected void doHead(HttpServletRequest request,
                          HttpServletResponse response)
        throws ServletException, IOException {

        process(request, response, false);
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

        process(request, response, true);
    }

    /**
     * Handles an incoming HTTP request. The response can be sent
     * either completely or solely with the response headers.
     *
     * @param request        the HTTP request object
     * @param response       the HTTP response object
     * @param content        the complete content response flag
     *
     * @throws ServletException if the request couldn't be handled by
     *             this servlet
     * @throws IOException if an IO error occured while attempting to
     *             service this request
     */
    private void process(HttpServletRequest request,
                         HttpServletResponse response,
                         boolean content)
        throws ServletException, IOException {

        Request  r;
        int      size;
        String   str;

        // Create request object
        str = request.getContentType();
        if (str != null && str.startsWith("multipart")) {
            try {
                str = config.get(Configuration.UPLOAD_DIRECTORY, "/tmp");
                size = config.getInt(Configuration.UPLOAD_MAX_SIZE, 10000000);
                r = new MultiPartRequest(getServletContext(),
                                         request,
                                         response,
                                         str,
                                         size);
            } catch (ServletException e) {
                LOG.warning("parse error on multi-part request", e);
                response.sendError(
                    HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                    e.getMessage());
                return;
            }
        } else {
            r = new Request(getServletContext(), request, response);
        }

        // TODO: handle offline state gracefully

        // Process request
        LOG.info("Incoming request: " + r);
        try {
            processor.process(r);
        } catch (RequestException e) {
            LOG.info("Erroneous request: " + r + ", Message: " +
                     e.getMessage());
            processError(r, e);
        }
        try {
            if (r.hasResponse()) {
                r.commit(getServletContext(), content);
            } else {
                LOG.info("Unhandled request: " + r);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IOException e) {
            LOG.info("IO error when processing request: " + r +
                     ", Message: " + e.getMessage());
        }
        r.dispose();
    }

    /**
     * Processes a request error.
     *
     * @param request        the request object
     * @param error          the request error
     */
    private void processError(Request request, RequestException error) {
        String  text;

        switch (error.getCode()) {
        case 401:
            request.setAttribute("heading", "Authentication Required (401)");
            text = "You must be logged in to access the document " +
                   "or resource located at this URL.";
            break;
        case 403:
            request.setAttribute("heading", "Access Forbidden (403)");
            text = "You don't have permission to access the document " +
                   "or resource located at this URL.";
            break;
        case 404:
            request.setAttribute("heading", "Document Not Found (404)");
            text = "The document or resource pointed to by this URL " +
                   "doesn't exist. This may be caused by the document " +
                   "having been moved.";
            break;
        default:
            text = "Internal Error (" + error.getCode() + ")";
            request.setAttribute("heading", text);
            text = "An internal error has ocurred. " +
                   "Please try again later.";
        }
        request.setAttribute("text", text);
        try {
            processor.sendError(request, error.getCode());
        } catch (TemplateException e) {
            text = "Error: " + e.getMessage();
            request.sendError(error.getCode(), "text/plain", text);
        }
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
        private static final int LOOP_DELAY = 1000;

        /**
         * The stop timeout in milliseconds.
         */
        private static final int STOP_TIMEOUT = 3000;

        /**
         * The database update threshold. This is the number of loop
         * iterations to skip in between calls to update() in the
         * database connector.
         */
        private static final int DATABASE_UPDATE_THRESHOLD = 60;

        /**
         * The mail processing threshold. This is the number of loop
         * iterations to skip in between calls to process() in the
         * mail queue.
         */
        private static final int MAIL_PROCESS_THRESHOLD = 5;

        /**
         * The mail error threshold. This is the number of loop
         * iterations to skip mail processing upon a mail transport
         * error.
         */
        private static final int MAIL_ERROR_THRESHOLD = 60;

        /**
         * The lock removal threshold. This is the number of loop
         * iterations to skip in between calls to delete the outdated
         * locks in the database.
         */
        private static final int LOCK_REMOVE_THRESHOLD = 3600;

        /**
         * The alive flag. If this flag is set to false, the thread
         * is supposed to die.
         */
        private boolean alive = true;

        /**
         * The database update counter. This counter is increased
         * every once in a while and is used to determine when the
         * database update method should be called.
         */
        private int databaseCounter = 0;

        /**
         * The mail process counter. This counter is increased every
         * once in a while and is used to determine when the mail
         * process method should be called.
         */
        private int mailCounter = 0;

        /**
         * The lock removal counter. This counter is increased every
         * once in a while and is used to determine when the lock
         * remove method should be called.
         */
        private int lockCounter = 0;

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
                if (getConfig() == null || !getConfig().isInitialized()) {
                    // Do nothing
                } else if (!isOnline()) {
                    restart();
                } else {
                    runMonitorPass();
                }
                try {
                    wait(LOOP_DELAY);
                } catch (InterruptedException ignore) {
                    // Do nothing
                }
            }
            notifyAll();
        }

        /**
         * Runs a single pass in the monitoring loop. Most of the
         * time, this will only result in updated counters. Once the
         * respective thresholds have been exceeded however, the
         * corresponding update method will be called. This method
         * will not be called if the system hasn't been configured and
         * properly initialized.
         */
        private void runMonitorPass() {
            // Update database
            databaseCounter++;
            if (databaseCounter >= DATABASE_UPDATE_THRESHOLD) {
                databaseCounter = 0;
                try {
                    getDatabase().update();
                } catch (DatabaseConnectionException e) {
                    LOG.error(e.getMessage());
                }
            }

            // Process mail
            mailCounter++;
            if (mailCounter >= MAIL_PROCESS_THRESHOLD) {
                mailCounter = 0;
                try {
                    MailQueue.getInstance().process();
                } catch (MailTransportException e) {
                    LOG.error(e.getMessage());
                    mailCounter = MAIL_PROCESS_THRESHOLD -
                                  MAIL_ERROR_THRESHOLD;
                }
            }

            // Remove outdated locks
            lockCounter++;
            if (lockCounter >= LOCK_REMOVE_THRESHOLD) {
                lockCounter = 0;
                try {
                    LockPeer.doDeleteOutdated(new DataSource(getDatabase()));
                } catch (DataObjectException e) {
                    LOG.error(e.getMessage());
                }
            }
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
