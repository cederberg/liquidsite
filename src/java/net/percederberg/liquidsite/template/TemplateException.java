/*
 * TemplateException.java
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

package net.percederberg.liquidsite.template;

/**
 * A template processing exception. This is thrown when a template
 * couldn't be read or processed correctly.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class TemplateException extends Exception {

    /**
     * The root cause to the error.
     */
    private Exception rootCause = null;

    /**
     * Creates a new template exception.
     * 
     * @param message        the error message
     */
    public TemplateException(String message) {
        super(message);
    }

    /**
     * Creates a new template exception.
     * 
     * @param cause          the root cause to the error
     */
    public TemplateException(Exception cause) {
        this("template processing error", cause);
    }
    
    /**
     * Creates a new template exception.
     * 
     * @param message        the error message
     * @param cause          the root cause to the error
     */
    public TemplateException(String message, Exception cause) {
        super(message);
        this.rootCause = cause;
    }
    
    /**
     * Returns the detailed error message. This error message will 
     * include any messages from the root errors.
     * 
     * @return the detailed error message
     */
    public String getMessage() {
        StringBuffer  message = new StringBuffer();
        Throwable     cause = rootCause;

        message.append(super.getMessage());
        while (cause != null) {
            message.append(": ");
            message.append(cause.getMessage());
            cause = cause.getCause();
        }
        return message.toString();
    }
    
    /**
     * Returns the root exception cause.
     * 
     * @return the root exception cause, or
     *         null for none
     */
    public Throwable getCause() {
        return rootCause;
    }
}
