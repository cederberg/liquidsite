/*
 * MySQLDatabaseConnector.java
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

package net.percederberg.liquidsite.db;

/**
 * A MySQL database connector.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class MySQLDatabaseConnector extends DatabaseConnector {

    /**
     * Loads the MySQL database driver. This method must be called 
     * once before attempting to connect with the specified driver.
     * Calling this method several times has no effect.
     * 
     * @throws DatabaseConnectionException if the driver couldn't be
     *             found or loaded correctly 
     */
    public static void loadDriver() throws DatabaseConnectionException {
        loadDriver("com.mysql.jdbc.Driver");
    }

    /**
     * Creates a new MySQL database connector.
     * 
     * @param host           the host name
     * @param user           the user name
     * @param password       the user password
     */
    public MySQLDatabaseConnector(String host, 
                                  String user, 
                                  String password) {

        super("jdbc:mysql://" + host + "/");
        setProperty("user", user);
        setProperty("password", password);
    }

    /**
     * Creates a new MySQL database connector.
     * 
     * @param host           the host name
     * @param database       the database name
     * @param user           the user name
     * @param password       the user password
     */
    public MySQLDatabaseConnector(String host, 
                                  String database,
                                  String user,
                                  String password) {

        super("jdbc:mysql://" + host + "/" + database);
        setProperty("user", user);
        setProperty("password", password);
    }
}
