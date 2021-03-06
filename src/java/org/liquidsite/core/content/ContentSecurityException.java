/*
 * ContentSecurityException.java
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

package org.liquidsite.core.content;

/**
 * A content security exception. This exception is thrown when the
 * content object couldn't be read or written by a specific user.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentSecurityException extends Exception {

    /**
     * The user the security error covers.
     */
    private User user = null;

    /**
     * Creates a new content security exception.
     *
     * @param message        the error message
     */
    public ContentSecurityException(String message) {
        super(message);
    }

    /**
     * Creates a new content security exception.
     *
     * @param user           the user attempting the operation
     * @param op             the operation name
     * @param obj            the object accessed
     */
    public ContentSecurityException(User user, String op, Domain obj) {
        super("cannot " + op + " domain " + obj.getName());
        this.user = user;
    }

    /**
     * Creates a new content security exception.
     *
     * @param user           the user attempting the operation
     * @param op             the operation name
     * @param obj            the object accessed
     */
    public ContentSecurityException(User user, String op, Content obj) {
        super("cannot " + op + " content " + obj.getId());
        this.user = user;
    }

    /**
     * Creates a new content security exception.
     *
     * @param user           the user attempting the operation
     * @param op             the operation name
     * @param obj            the object accessed
     */
    public ContentSecurityException(User user, String op, User obj) {
        super("cannot " + op + " user " + obj.getName());
        this.user = user;
    }

    /**
     * Creates a new content security exception.
     *
     * @param user           the user attempting the operation
     * @param op             the operation name
     * @param obj            the object accessed
     */
    public ContentSecurityException(User user, String op, Group obj) {
        super("cannot " + op + " group " + obj.getName());
        this.user = user;
    }

    /**
     * Returns the detailed error message.
     *
     * @return the detailed error message
     */
    public String getMessage() {
        if (user == null) {
            return "<anonymous> " + super.getMessage();
        } else {
            return user.getName() + " " + super.getMessage();
        }
    }
}
