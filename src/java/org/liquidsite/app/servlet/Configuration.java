/*
 * Configuration.java
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

package org.liquidsite.app.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.liquidsite.core.data.ConfigurationData;
import org.liquidsite.core.data.ConfigurationPeer;
import org.liquidsite.core.data.DataObjectException;
import org.liquidsite.core.data.DataSource;
import org.liquidsite.util.db.DatabaseConnector;

/**
 * The application configuration. This class contains support for
 * reading and writing the configuration, as well as retrieving the
 * individual properties. The configuration is divided into a
 * configuration file containing the database properties, and a
 * configuration table containing the rest of the configuration
 * properties.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Configuration {

    /**
     * The configuration file header.
     */
    private static final String FILE_HEADER =
        "Liquid Site Database Configuration File";

    /**
     * The version number key.
     */
    public static final String VERSION =
        "liquidsite.version";

    /**
     * The database host name key.
     */
    public static final String DATABASE_HOSTNAME =
        "liquidsite.db.hostname";

    /**
     * The database name key.
     */
    public static final String DATABASE_NAME =
        "liquidsite.db.name";

    /**
     * The database user name key.
     */
    public static final String DATABASE_USER =
        "liquidsite.db.user";

    /**
     * The database password key.
     */
    public static final String DATABASE_PASSWORD =
        "liquidsite.db.password";

    /**
     * The database pool size key.
     */
    public static final String DATABASE_POOL_SIZE =
        "liquidsite.db.pool.size";

    /**
     * The mail server host name key.
     */
    public static final String MAIL_HOST =
        "liquidsite.mail.host";

    /**
     * The mail user name key.
     */
    public static final String MAIL_USER =
        "liquidsite.mail.user";

    /**
     * The mail from address key.
     */
    public static final String MAIL_FROM =
        "liquidsite.mail.from";

    /**
     * The mail header text key.
     */
    public static final String MAIL_HEADER =
        "liquidsite.mail.header";

    /**
     * The mail footer text key.
     */
    public static final String MAIL_FOOTER =
        "liquidsite.mail.footer";

    /**
     * The file data directory key.
     */
    public static final String FILE_DIRECTORY =
        "liquidsite.file.dir";

    /**
     * The statistics data directory key.
     */
    public static final String STATS_DIRECTORY =
        "liquidsite.stats.dir";

    /**
     * The temporary file upload directory key.
     */
    public static final String UPLOAD_DIRECTORY =
        "liquidsite.upload.tmpdir";

    /**
     * The file upload maximum size key.
     */
    public static final String UPLOAD_MAX_SIZE =
        "liquidsite.upload.maxsize";

    /**
     * The configuration file. If this file is set to null, the
     * configuration is read-only.
     */
    private File file;

    /**
     * The configuration properties present in the file.
     */
    private Properties fileProperties;

    /**
     * The configuration properties present in the database.
     */
    private Properties databaseProperties;

    /**
     * The initialized flag. This flag is set to true once the
     * configuration file has been read correctly.
     */
    private boolean initialized = false;

    /**
     * Creates a new read-only configuration. This constructor is
     * only used for accessing configuration data in databases during
     * the installation.
     */
    public Configuration() {
        this(null);
    }

    /**
     * Creates a new configuration.
     *
     * @param file           the configuration file to use
     */
    public Configuration(File file) {
        this.file = file;
        this.databaseProperties = new Properties();
        this.fileProperties = new Properties(databaseProperties);
        try {
            readFile();
        } catch (ConfigurationException ignore) {
            // Do nothing
        }
    }

    /**
     * Checks if this configuration has been properly initialized.
     * The configuration is considered initialized if the config file
     * could be read properly.
     *
     * @return true if this configuration was properly initialized, or
     *         false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns a configuration property string value.
     *
     * @param key            the configuration property key
     * @param defaultValue   the value to use if not set
     *
     * @return the configuration property string value, or
     *         the default value if undefined
     */
    public String get(String key, String defaultValue) {
        return fileProperties.getProperty(key, defaultValue);
    }

    /**
     * Returns a configuration property integer value.
     *
     * @param key            the configuration property key
     * @param defaultValue   the value to use if not set
     *
     * @return the configuration property integer value, or
     *         the default value if undefined
     */
    public int getInt(String key, int defaultValue) {
        String  str = get(key, String.valueOf(defaultValue));

        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Sets a configuration property value.
     *
     * @param key            the configuration property key
     * @param value          the configuration property value
     */
    public void set(String key, String value) {
        if (key.startsWith("liquidsite.db.")) {
            fileProperties.setProperty(key, value);
        } else {
            databaseProperties.setProperty(key, value);
        }
    }

    /**
     * Sets a configuration property value.
     *
     * @param key            the configuration property key
     * @param value          the configuration property value
     */
    public void set(String key, int value) {
        set(key, String.valueOf(value));
    }

    /**
     * Sets all configuration properties from a set. Any existing
     * configuration properties with identical names will be
     * overwritten.
     *
     * @param config         the configuration properties
     */
    public void setAll(Configuration config) {
        Enumeration  e;
        String       name;

        e = config.fileProperties.propertyNames();
        while (e.hasMoreElements()) {
            name = (String) e.nextElement();
            set(name, config.get(name, ""));
        }
        e = config.databaseProperties.propertyNames();
        while (e.hasMoreElements()) {
            name = (String) e.nextElement();
            set(name, config.get(name, ""));
        }
    }

    /**
     * Reads the configuration. The configuration is read from both
     * file and database.
     *
     * @param db             the database connector to use
     *
     * @throws ConfigurationException if the configuration couldn't
     *             be read properly
     */
    public void read(DatabaseConnector db)
        throws ConfigurationException {

        DataSource  src = new DataSource(db);

        readFile();
        try {
            readDatabase(src);
        } finally {
            src.close();
        }
    }

    /**
     * Reads the configuration file. The configuration file contains
     * the file properties.
     *
     * @throws ConfigurationException if the configuration file
     *             couldn't be read properly
     */
    private void readFile() throws ConfigurationException {
        FileInputStream  input;
        String           message;

        // Check for missing file
        if (file == null) {
            return;
        }

        // Open configuration file
        try {
            input = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            message = "couldn't read config file: " + file;
            throw new ConfigurationException(message, e);
        }

        // Read configuration file
        try {
            fileProperties.load(input);
            input.close();
            initialized = true;
        } catch (IOException e) {
            try {
                input.close();
            } catch (IOException ignore) {
                // Do nothing
            }
            message = "couldn't read config file: " + file;
            throw new ConfigurationException(message, e);
        }
    }

    /**
     * Reads the configuration database table.
     *
     * @param src            the data source to use
     *
     * @throws ConfigurationException if the database table couldn't
     *             be read properly
     */
    private void readDatabase(DataSource src)
        throws ConfigurationException {

        ArrayList          list;
        ConfigurationData  data;
        String             name;
        String             value;
        String             message;

        try {
            list = ConfigurationPeer.doSelectAll(src);
            for (int i = 0; i < list.size(); i++) {
                data = (ConfigurationData) list.get(i);
                name = data.getString(ConfigurationData.NAME);
                value = data.getString(ConfigurationData.VALUE);
                set(name, value);
            }
        } catch (DataObjectException e) {
            message = "couldn't read configuration database table";
            throw new ConfigurationException(message, e);
        }
    }

    /**
     * Writes the configuration. The configuration is written to both
     * file and database.
     *
     * @param db             the database connector to use
     *
     * @throws ConfigurationException if the configuration couldn't
     *             be written properly
     */
    public void write(DatabaseConnector db)
        throws ConfigurationException {

        DataSource  src = new DataSource(db);

        writeFile();
        try {
            writeDatabase(src);
        } finally {
            src.close();
        }
    }

    /**
     * Writes the configuration file. The configuration file contains
     * the file properties.
     *
     * @throws ConfigurationException if the configuration file
     *             couldn't be written properly
     */
    private void writeFile() throws ConfigurationException {
        FileOutputStream  output;
        String            message;

        // Check for missing file
        if (file == null) {
            message = "no configuration file has been set";
            throw new ConfigurationException(message);
        }

        // Open configuration file
        try {
            output = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            message = "couldn't write config file: " + file;
            throw new ConfigurationException(message, e);
        }

        // Write configuration file
        try {
            fileProperties.store(output, FILE_HEADER);
            output.close();
            initialized = true;
        } catch (IOException e) {
            try {
                output.close();
            } catch (IOException ignore) {
                // Do nothing
            }
            message = "couldn't write config file: " + file;
            throw new ConfigurationException(message, e);
        }
    }

    /**
     * Writes the configuration database table. The configuration
     * table is first cleared, then all the configuration keys are
     * inserted.
     *
     * @param src            the data source to use
     *
     * @throws ConfigurationException if the database table couldn't
     *             be written properly
     */
    private void writeDatabase(DataSource src)
        throws ConfigurationException {

        ConfigurationData  data;
        Enumeration        iter;
        String             name;
        String             value;
        String             message;

        try {
            ConfigurationPeer.doDeleteAll(src);
            iter = databaseProperties.propertyNames();
            while (iter.hasMoreElements()) {
                name = (String) iter.nextElement();
                value = get(name, "");
                data = new ConfigurationData();
                data.setString(ConfigurationData.NAME, name);
                data.setString(ConfigurationData.VALUE, value);
                ConfigurationPeer.doInsert(src, data);
            }
        } catch (DataObjectException e) {
            message = "couldn't write configuration database table";
            throw new ConfigurationException(message, e);
        }
    }
}
