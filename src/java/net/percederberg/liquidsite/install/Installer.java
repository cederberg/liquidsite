/*
 * Installer.java
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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.install;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import net.percederberg.liquidsite.Configuration;
import net.percederberg.liquidsite.ConfigurationException;
import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseConnectionException;
import net.percederberg.liquidsite.db.DatabaseException;
import net.percederberg.liquidsite.db.MySQLDatabaseConnector;

/**
 * The install helper class. This class handles all the installation
 * tasks, such as database updates and configuration writing.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Installer {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(Installer.class);

    /**
     * The version to install.
     */
    private String version;

    /**
     * The database connector to use.
     */
    private MySQLDatabaseConnector connector;

    /**
     * The SQL file directory.
     */
    private File sqlDir;

    /**
     * The database updaters available.
     */
    private ArrayList updaters = new ArrayList();

    /**
     * Create a new installer.
     *
     * @param version        the application version to install
     * @param connector      the database connector to use
     * @param sqlDir         the base directory for the SQL files
     */
    public Installer(String version,
                     MySQLDatabaseConnector connector,
                     File sqlDir) {

        this.version = version;
        this.connector = connector;
        this.sqlDir = sqlDir;
        updaters.add(new DatabaseUpdater("0.3",
                                         "0.4",
                                         "UPDATE_LIQUIDSITE_TABLES_0.4.sql"));
        updaters.add(new DatabaseUpdater("0.4",
                                         "0.5",
                                         "UPDATE_LIQUIDSITE_TABLES_0.5.sql"));
        updaters.add(new DatabaseUpdater("0.5",
                                         "0.6",
                                         "UPDATE_LIQUIDSITE_TABLES_0.6.sql"));
    }

    /**
     * Checks if the specified Liquid Site version can be updated by
     * this installer.
     *
     * @param fromVersion    the version to update from
     *
     * @return true if the installer supports the update, or
     *         false otherwise
     */
    public boolean canUpdate(String fromVersion) {
        DatabaseUpdater  updater;

        if (version.equals(fromVersion)) {
            return true;
        } else {
            for (int i = 0; i < updaters.size(); i++) {
                updater = (DatabaseUpdater) updaters.get(i);
                if (updater.canUpdate(this, fromVersion)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates the Liquid Site database tables. The database must
     * already exist for this method to work.
     *
     * @param database       the database name
     *
     * @throws InstallException if the database tables couldn't be
     *             created correctly
     */
    public void createTables(String database) throws InstallException {
        DatabaseConnection  con = null;

        try {
            con = connector.getConnection();
            con.setCatalog(database);
            con.execute(new File(sqlDir, "CREATE_LIQUIDSITE_TABLES.sql"));
        } catch (DatabaseConnectionException e) {
            LOG.error("couldn't create tables", e);
            throw new InstallException("couldn't create tables", e);
        } catch (DatabaseException e) {
            LOG.error("couldn't create tables", e);
            throw new InstallException("couldn't create tables", e);
        } catch (FileNotFoundException e) {
            LOG.error("couldn't find create table SQL file", e);
            throw new InstallException("couldn't find create table SQL file",
                                       e);
        } catch (IOException e) {
            LOG.error("couldn't read create table SQL file", e);
            throw new InstallException("couldn't read create table SQL file",
                                       e);
        } finally {
            if (con != null) {
                connector.returnConnection(con);
            }
        }
    }

    /**
     * Updates the Liquid Site database tables. The database must
     * already contain the specified Liquid Site version for this
     * method to work.
     *
     * @param database       the database name
     * @param fromVersion    the database Liquid Site version
     *
     * @throws InstallException if the database tables couldn't be
     *             updated correctly
     */
    public void updateTables(String database, String fromVersion)
        throws InstallException {

        DatabaseConnection  con = null;

        if (!canUpdate(fromVersion)) {
            throw new InstallException("cannot update from version " +
                                       fromVersion);
        }
        try {
            con = connector.getConnection();
            con.setCatalog(database);
            updateTables(con, fromVersion);
        } catch (DatabaseConnectionException e) {
            LOG.error("couldn't update tables", e);
            throw new InstallException("couldn't update tables", e);
        } catch (DatabaseException e) {
            LOG.error("couldn't update tables", e);
            throw new InstallException("couldn't update tables", e);
        } catch (FileNotFoundException e) {
            LOG.error("couldn't find update table SQL file", e);
            throw new InstallException("couldn't find update table SQL file",
                                       e);
        } catch (IOException e) {
            LOG.error("couldn't read update table SQL file", e);
            throw new InstallException("couldn't read update table SQL file",
                                       e);
        } finally {
            if (con != null) {
                connector.returnConnection(con);
            }
        }
    }

    /**
     * Updates the Liquid Site database tables. The database must
     * already contain the specified Liquid Site version for this
     * method to work.
     *
     * @param con            the database connection to use
     * @param fromVersion    the database Liquid Site version
     *
     * @throws DatabaseException if a database statement execution
     *             failed
     * @throws FileNotFoundException if an update tables SQL file
     *             couldn't be found
     * @throws IOException if an update tables SQL file couldn't be
     *             read
     */
    void updateTables(DatabaseConnection con, String fromVersion)
        throws DatabaseException, FileNotFoundException, IOException {

        DatabaseUpdater  updater;

        if (version.equals(fromVersion)) {
            return;
        } else {
            for (int i = 0; i < updaters.size(); i++) {
                updater = (DatabaseUpdater) updaters.get(i);
                if (updater.canUpdate(this, fromVersion)) {
                    updater.updateTables(this, con);
                    return;
                }
            }
        }
    }


    /**
     * A database updater. This class handles a database update from
     * one version to another. Multiple database updaters can be
     * chained together to provide an update path across several
     * versions.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    private class DatabaseUpdater {

        /**
         * The version to update from.
         */
        private String fromVersion;

        /**
         * The version to update to.
         */
        private String toVersion;

        /**
         * The SQL file containing the update statements.
         */
        private String sqlFile;

        /**
         * Creates a new database update.
         *
         * @param fromVersion    the version to update from
         * @param toVersion      the version to update to
         * @param sqlFile        the SQL file with update statements
         */
        public DatabaseUpdater(String fromVersion,
                               String toVersion,
                               String sqlFile) {

            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
            this.sqlFile = sqlFile;
        }

        /**
         * Checks if this updater can handle a specified version. This
         * method will call the installer to check if the version this
         * updater creates is valid or if it in turn can be updated.
         * The update search is thus exhaustive, checking for every
         * possible path from the specified version to the installer
         * version.
         *
         * @param installer      the installer to use
         * @param fromVersion    the version to update from
         */
        public boolean canUpdate(Installer installer, String fromVersion) {
            return this.fromVersion.equals(fromVersion)
                && installer.canUpdate(this.toVersion);
        }

        /**
         * Updates the Liquid Site database tables. The database must
         * already contain the specified Liquid Site version for this
         * method to work. This method will call the installer to
         * update the version created if needed. The update can thus
         * handle an update path consisting of chaining several
         * updaters together.
         *
         * @param con            the database connection to use
         * @param fromVersion    the database Liquid Site version
         *
         * @throws DatabaseException if a database statement execution
         *             failed
         * @throws FileNotFoundException if an update tables SQL file
         *             couldn't be found
         * @throws IOException if an update tables SQL file couldn't be
         *             read
         */
        public void updateTables(Installer installer, DatabaseConnection con)
            throws DatabaseException, FileNotFoundException, IOException {

            con.execute(new File(sqlDir, sqlFile));
            installer.updateTables(con, toVersion);
        }
    }
}
