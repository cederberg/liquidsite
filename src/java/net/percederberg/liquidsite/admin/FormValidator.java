/*
 * FormValidator.java
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

package net.percederberg.liquidsite.admin;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;

import net.percederberg.liquidsite.Request;

/**
 * A form field validator. This class contains methods for checking
 * request input parameters in various ways.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class FormValidator {

    /**
     * The field constraints.
     */
    private ArrayList constraints = new ArrayList();

    /**
     * Creates a new form field validator.
     */
    public FormValidator() {
    }

    /**
     * Adds a form field required constraint. This will check that 
     * the specified form field isn't empty or null.
     * 
     * @param field          the field name
     * @param message        the message on error 
     */
    public void addRequiredConstraint(String field, String message) {
        constraints.add(new RequiredConstraint(field, message)); 
    }
    
    /**
     * Adds a form field character constraint. This will check that 
     * the specified form field only contains characters from the 
     * specified set.
     * 
     * @param field          the field name
     * @param chars          the character set to allow
     * @param message        the message on error 
     */
    public void addCharacterConstraint(String field, 
                                       String chars, 
                                       String message) {

        constraints.add(new CharacterConstraint(field, chars, message)); 
    }

    /**
     * Adds a form field date constraint. This will check that the 
     * specified form field is parseable with the date formatter.
     * 
     * @param field          the field name
     * @param format         the date format to allow
     * @param message        the message on error 
     */
    public void addDateConstraint(String field, 
                                  DateFormat format, 
                                  String message) {

        constraints.add(new DateConstraint(field, format, message)); 
    }

    /**
     * Validates the parameters in a request. The constraints will be
     * checked in the same order as they were added to this 
     * validator.
     * 
     * @param request        the form request
     * 
     * @throws FormException if the form validation failed
     */
    public void validate(Request request) throws FormException {
        for (int i = 0; i < constraints.size(); i++) {
            ((Constraint) constraints.get(i)).validate(request);
        }
    }
    

    /**
     * A form field constraint.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */    
    private abstract class Constraint {
        
        /**
         * The field name.
         */
        private String field;
        
        /**
         * The error message.
         */
        private String message;
        
        /**
         * Creates a new form field constraint.
         * 
         * @param field          the field name
         * @param message        the error message
         */
        public Constraint(String field, String message) {
            this.field = field;
            this.message = message;
        }

        /**
         * Validates this constraint.
         * 
         * @param request        the form request
         * 
         * @throws FormException if the form validation failed
         */
        public void validate(Request request) throws FormException {
            validate(request.getParameter(field));
        }

        /**
         * Validates this constraint.
         * 
         * @param value          the form field value
         * 
         * @throws FormException if the form validation failed
         */
        protected abstract void validate(String value) 
            throws FormException;
            
        /**
         * Reports a form validation error. This method just throws
         * the adequate form exception.
         * 
         * @throws FormException the form exception representing the
         *             constraint error
         */
        protected void error() throws FormException {
            throw new FormException(field, message);
        }

        /**
         * Reports a form validation error. This method just throws
         * the adequate form exception. Note that the field message
         * will be outputted with the additional detailes appended 
         * after ": ".
         * 
         * @param details        the additional error details
         * 
         * @throws FormException the form exception representing the
         *             constraint error
         */
        protected void error(String details) throws FormException {
            throw new FormException(field, message + ": " + details);
        }
    }
    

    /**
     * A required form field constraint.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */    
    private class RequiredConstraint extends Constraint {

        /**
         * Creates a new required form field constraint.
         * 
         * @param field          the field name
         * @param message        the error message
         */
        public RequiredConstraint(String field, String message) {
            super(field, message);
        }

        /**
         * Validates this constraint.
         * 
         * @param value          the form field value
         * 
         * @throws FormException if the form validation failed
         */
        protected void validate(String value) throws FormException {
            if (value == null || value.equals("")) {
                error();
            }
        }
    }
    

    /**
     * A character set form field constraint.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */    
    private class CharacterConstraint extends Constraint {

        /**
         * The character set to allow.
         */
        private String chars;

        /**
         * Creates a new character set form field constraint.
         * 
         * @param field          the field name
         * @param chars          the character set to allow
         * @param message        the error message
         */
        public CharacterConstraint(String field, 
                                   String chars, 
                                   String message) {
            super(field, message);
            this.chars = chars;
        }

        /**
         * Validates this constraint.
         * 
         * @param value          the form field value
         * 
         * @throws FormException if the form validation failed
         */
        protected void validate(String value) throws FormException {
            char c;

            if (value == null) {
                return;
            }
            for (int i = 0; i < value.length(); i++) {
                if (chars.indexOf(value.charAt(i)) < 0) {
                    c = value.charAt(i);
                    if (c < 128) {
                        error("'" + c + "'");
                    } else {
                        error("'" + value.substring(i, i + 2) + "'");
                    }
                }
            }
        }
    }


    /**
     * A date set form field constraint.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */    
    private class DateConstraint extends Constraint {

        /**
         * The date format to allow.
         */
        private DateFormat format;

        /**
         * Creates a new date format form field constraint.
         * 
         * @param field          the field name
         * @param format         the date format to allow
         * @param message        the error message
         */
        public DateConstraint(String field, 
                              DateFormat format, 
                              String message) {
            super(field, message);
            this.format = format;
        }

        /**
         * Validates this constraint.
         * 
         * @param value          the form field value
         * 
         * @throws FormException if the form validation failed
         */
        protected void validate(String value) throws FormException {
            if (value == null) {
                return;
            }
            try {
                format.parse(value);
            } catch (ParseException e) {
                error();
            }
        }
    }
}
