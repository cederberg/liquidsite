/*
 * FormValidationException.java
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

package net.percederberg.liquidsite.form;

/**
 * A form validation exception. This exception is thrown when an 
 * error is encountered while validating form input.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class FormValidationException extends Exception {

    /**
     * The field name.
     */
    private String field;
    
    /**
     * Creates a new form validation exception.
     * 
     * @param field          the field name
     * @param message        the error message
     */
    public FormValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    /**
     * Returns the field name.
     * 
     * @return the field name
     */
    public String getField() {
        return field;
    }
}
