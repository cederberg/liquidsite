/*
 * FormHandlingException.java
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

package net.percederberg.liquidsite.web;

/**
 * A form handling exception. This exception is thrown when the form
 * handling failed. This can be caused by both user actions (such as
 * removed locks) or by critical application errors (such as database
 * connection problems). Any critical error present in this exception
 * must already have been logged as such. This exception should only
 * ever be logged as a warning.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class FormHandlingException extends Exception {

    /**
     * The root cause to the error.
     */
    private Exception rootCause = null;

    /**
     * Creates a new form handling exception.
     *
     * @param message        the error message
     */
    public FormHandlingException(String message) {
        super(message);
    }

    /**
     * Creates a new form handling exception.
     *
     * @param cause          the root cause to the error
     */
    public FormHandlingException(Exception cause) {
        super(cause.getMessage());
        this.rootCause = cause;
    }

    /**
     * Creates a new form handling exception.
     *
     * @param message        the error message
     * @param cause          the root cause to the error
     */
    public FormHandlingException(String message, Exception cause) {
        super(message + ": " + cause.getMessage());
        this.rootCause = cause;
    }
}
