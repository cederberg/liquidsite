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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseConnectionException;
import net.percederberg.liquidsite.db.DatabaseConnector;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseException;
import net.percederberg.liquidsite.db.DatabaseResults;

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
     * The configuration file.
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
     * Reads the configuration. The configuration is read from both
     * file and database.
     * 
     * @param database       the database connector to use
     * 
     * @throws ConfigurationException if the configuration couldn't 
     *             be read properly
     */
    public void read(DatabaseConnector database) 
        throws ConfigurationException {

        readFile();
        readDatabase(database);
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
     * @param database       the database connector to use
     * 
     * @throws ConfigurationException if the database table couldn't 
     *             be read properly
     */
    private void readDatabase(DatabaseConnector database) 
        throws ConfigurationException {

        DatabaseResults  res;
        String           name;
        String           value;
        String           message;

        // TODO: change to use a database query pool
        try {
            res = database.execute("SELECT * FROM CONFIGURATION");
            for (int i = 0; i < res.getRowCount(); i++) {
                name = res.getRow(i).getString("NAME");
                value = res.getRow(i).getString("VALUE");
                set(name, value);         
            }
        } catch (DatabaseConnectionException e) {
            message = "couldn't read configuration database table"; 
            throw new ConfigurationException(message, e);
        } catch (DatabaseException e) {
            message = "couldn't read configuration database table"; 
            throw new ConfigurationException(message, e);
        } catch (DatabaseDataException e) {
            message = "couldn't read configuration database table"; 
            throw new ConfigurationException(message, e);
        }
    }

    /**
     * Writes the configuration. The configuration is written to both
     * file and database.
     * 
     * @param database       the database connector to use
     * 
     * @throws ConfigurationException if the configuration couldn't 
     *             be written properly
     */
    public void write(DatabaseConnector database) 
        throws ConfigurationException {
            
        DatabaseConnection  con;
        String              message;

        writeFile();
        try {
            con = database.getConnection();
        } catch (DatabaseConnectionException e) {
            message = "couldn't write configuration database table"; 
            throw new ConfigurationException(message, e);
        }
        try {
            writeDatabase(con);
        } catch (DatabaseException e) {
            message = "couldn't write configuration database table"; 
            throw new ConfigurationException(message, e);
        } finally {
            database.returnConnection(con);
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
     * @param con            the database connection to use
     * 
     * @throws DatabaseException if the database table couldn't be 
     *             written properly
     */
    private void writeDatabase(DatabaseConnection con) 
        throws DatabaseException {

        Enumeration  e;
        String       sql;
        String       name;

        // TODO: change to use a database query pool
        con.execute("DELETE FROM CONFIGURATION");
        e = databaseProperties.propertyNames();
        while (e.hasMoreElements()) {
            name = (String) e.nextElement();
            sql = "INSERT INTO CONFIGURATION (NAME, VALUE) VALUES ('" +
                  name + "', '" + get(name, "") + "')";
            con.execute(sql);
        }
    }
}
