/*
 * Log.java
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
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * An error and debug log. This class encapsulates the standard 
 * logging in JDK 1.4 to allow provide some convenience methods and
 * to defined the log levels more adequately for this application. 
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Log {

    /**
     * The logger being used.
     */
    private Logger logger;

    /**
     * Initializes the logging subsystem with a configuration file.
     * 
     * @param file           the logging configuration file
     * 
     * @throws IOException it the configuration file couldn't be read
     */
    public static void initialize(File file) throws IOException {
        LogManager       manager;
        FileInputStream  input;

        manager = LogManager.getLogManager();
        input = new FileInputStream(file);
        manager.readConfiguration(input);
        input.close();
    }

    /**
     * Creates a new log.
     * 
     * @param name           the log name
     */
    public Log(String name) {
        logger = Logger.getLogger(name);
    }
    
    /**
     * Creates a new log. The class name will be used as the log 
     * name.
     * 
     * @param cls            the class to log
     */
    public Log(Class cls) {
        this(cls.getName());
    }
    
    /**
     * Creates a new log. The object class name will be used as the
     * log name.
     * 
     * @param obj            the object to log
     */
    public Log(Object obj) {
        this(obj.getClass());
    }
    
    /**
     * Logs an error message. An error message is logged for errors 
     * that require attention from a system administrator. In the 
     * typical case, some kind of error message is also presented to
     * the user.
     * 
     * @param message        the log message
     */
    public void error(String message) {
        logger.severe(message);
    }
    
    /**
     * Logs an error message. An error message is logged for errors 
     * that require attention from a system administrator. In the 
     * typical case, some kind of error message is also presented to
     * the user.
     * 
     * @param message        the log message
     * @param thrown         the throwable to log
     */
    public void error(String message, Throwable thrown) {
        error(message + ": " + thrown.getMessage());
    }
    
    /**
     * Logs a warning message. A warning message is logged for errors 
     * that were recovered automatically. In the typical case, the
     * error was handled by using fallback routines or similar.
     * 
     * @param message        the log message
     */
    public void warning(String message) {
        logger.warning(message);
    }

    /**
     * Logs a warning message. A warning message is logged for errors 
     * that were recovered automatically. In the typical case, the
     * error was handled by using fallback routines or similar.
     * 
     * @param message        the log message
     * @param thrown         the throwable to log
     */
    public void warning(String message, Throwable thrown) {
        warning(message + ": " + thrown.getMessage());
    }
    
    /**
     * Logs a debug message. A debug message is logged for debugging
     * rare circumstances and potential problems. In the typical 
     * case, a debug message is used when an error condition occurs.
     * 
     * @param message        the log message
     */
    public void debug(String message) {
        logger.info(message);
    }

    /**
     * Logs a debug message. A debug message is logged for debugging
     * rare circumstances and potential problems. In the typical 
     * case, a debug message is used when an error condition occurs.
     * 
     * @param message        the log message
     * @param thrown         the throwable to log
     */
    public void debug(String message, Throwable thrown) {
        debug(message + ": " + thrown.getMessage());
    }

    /**
     * Logs a trace message. A trace message is logged for debugging
     * normal operation. In the typical case, a trance message is 
     * used to log method calls.
     * 
     * @param message        the log message
     */
    public void trace(String message) {
        logger.finer(message);
    }
    
    /**
     * Logs a trace message. A trace message is logged for debugging
     * normal operation. In the typical case, a trance message is 
     * used to log method calls.
     * 
     * @param message        the log message
     * @param thrown         the throwable to log
     */
    public void trace(String message, Throwable thrown) {
        trace(message + ": " + thrown.getMessage());
    }
}
