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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.content;

/**
 * A content security exception. This exception is thrown when the 
 * content object couldn't be read or written by the specified user.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentSecurityException extends Exception {

    /**
     * Creates a new content security exception.
     * 
     * @param user           the user attempting the operation
     * @param op             the operation name
     * @param obj            the object accessed
     */
    public ContentSecurityException(User user, String op, Domain obj) {
        super(user.getName() + " cannot " + op + " domain " + 
              obj.getName());
    }

    /**
     * Creates a new content security exception.
     * 
     * @param user           the user attempting the operation
     * @param op             the operation name
     * @param obj            the object accessed
     */
    public ContentSecurityException(User user, String op, Content obj) {
        super(user.getName() + " cannot " + op + " content " + 
              obj.getId());
    }
}
