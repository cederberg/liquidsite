/*
 * MailException.java
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

package net.percederberg.liquidsite.mail;

/**
 * A mail exception. This exception is thrown when a mail operation
 * couldn't be executed correctly. This may be due to invalid
 * addresses, incorrect mail configuration or similar.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class MailException extends Exception {

    /**
     * Creates a new mail exception.
     *
     * @param message        the error message
     */
    public MailException(String message) {
        super(message);
    }

    /**
     * Creates a new mail exception.
     *
     * @param message        the error message
     * @param cause          the root cause to the error
     */
    public MailException(String message, Exception cause) {
        super(message + ": " + cause.getMessage());
    }
}
