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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package org.liquidsite.util.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * An error and debug log. This class encapsulates the standard
 * logging in JDK 1.4 to provide some convenience methods and define
 * the log levels more adequately for this application.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Log {

    /**
     * The fully qualified log class name.
     */
    private static final String CLASS_NAME = Log.class.getName();

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
     * Creates a new log. The class name will be used as the log
     * name.
     *
     * @param cls            the class to log
     */
    public Log(Class cls) {
        this.logger = Logger.getLogger(cls.getName());
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
    public final void error(String message) {
        log(Level.SEVERE, message);
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
    public final void error(String message, Throwable thrown) {
        error(message + ": " + thrown.getMessage());
    }

    /**
     * Logs a warning message. A warning message is logged for errors
     * that were recovered automatically or for anomalies in the
     * input. In the typical case, the error was handled by using
     * fallback routines or similar.
     *
     * @param message        the log message
     */
    public final void warning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Logs a warning message. A warning message is logged for errors
     * that were recovered automatically or for anomalies in the
     * input. In the typical case, the error was handled by using
     * fallback routines or similar.
     *
     * @param message        the log message
     * @param thrown         the throwable to log
     */
    public final void warning(String message, Throwable thrown) {
        warning(message + ": " + thrown.getMessage());
    }

    /**
     * Logs an informative message. An info message is logged when
     * performing certain operations that may be interresting to track
     * for a system administrator. In the typical case, an info
     * message is used to report the status when communicating with
     * external systems.
     *
     * @param message        the log message
     */
    public final void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Logs an informative message. An info message is logged when
     * performing certain operations that may be interresting to track
     * for a system administrator. In the typical case, an info
     * message is used to report the status when communicating with
     * external systems.
     *
     * @param message        the log message
     * @param thrown         the throwable to log
     */
    public final void info(String message, Throwable thrown) {
        info(message + ": " + thrown.getMessage());
    }

    /**
     * Logs a trace message. A trace message is logged for debugging
     * normal operation. In the typical case, a trace message is used
     * to log method calls.
     *
     * @param message        the log message
     */
    public final void trace(String message) {
        log(Level.FINER, message);
    }

    /**
     * Logs a trace message. A trace message is logged for debugging
     * normal operation. In the typical case, a trace message is used
     * to log method calls.
     *
     * @param message        the log message
     * @param thrown         the throwable to log
     */
    public final void trace(String message, Throwable thrown) {
        trace(message + ": " + thrown.getMessage());
    }

    /**
     * Logs a message on the specified log level. This method will
     * search for the caller class and method names by creating a
     * stack trace.
     *
     * @param level          the log level to use
     * @param message        the log message
     */
    private final void log(Level level, String message) {
        String               className = null;
        String               methodName = null;
        StackTraceElement[]  stackFrame;
        int                  i = 0;

        if (logger.isLoggable(level)) {
            stackFrame = new Throwable().getStackTrace();
            while (stackFrame != null && i < stackFrame.length) {
                if (!stackFrame[i].getClassName().equals(CLASS_NAME)) {
                    className = stackFrame[i].getClassName();
                    methodName = stackFrame[i].getMethodName();
                    break;
                }
                i++;
            }
            logger.logp(level, className, methodName, message);
        }
    }
}
