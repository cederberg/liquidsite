/*
 * Application.java
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

import net.percederberg.liquidsite.db.DatabaseConnector;

/**
 * The application context. This interface provides the basic 
 * infrastructure for inter-application communication and for sharing
 * global datastructures.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public interface Application {

    /**
     * Checks if the application has been properly installed. This 
     * method will return true if the configuration files written
     * during the installation exists. This method returning true is
     * no guarantee for the application functioning properly.
     * 
     * @return true if the application has been installed, or
     *         false otherwise
     */
    boolean isInstalled();

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
    boolean isOnline();

    /**
     * Restarts the application. This will perform a partial shutdown 
     * followed by a startup, flushing and rereading all 
     * datastructures, such as configuration, database connections,
     * and similar.
     */
    void restart();
    
    /**
     * Returns the base application directory. This is the directory
     * containing all the application files (i.e. the corresponding
     * webapps directory).
     * 
     * @return the base application directory
     */
    File getBaseDir();

    /**
     * Returns the application configuration. The object returned by
     * this method will not change, unless a reset is made, but the
     * parameter values in the configuration may be modified.
     * 
     * @return the application configuration
     */
    Configuration getConfig();
    
    /**
     * Returns the application database connector. The object 
     * returned by this method will not change, unless a reset is 
     * made.
     * 
     * @return the application database connector
     */
    DatabaseConnector getDatabase();
}
