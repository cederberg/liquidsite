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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.web;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

/**
 * A form field validator. This class contains methods for checking
 * request input parameters in various ways.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class FormValidator {

    /**
     * The field constraints.
     */
    private ArrayList constraints = new ArrayList();

    /**
     * Creates a new form field validator.
     */
    public FormValidator() {
        // No further initialization needed
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
     * Adds a form field length constraint. This will check that
     * the length of the specified form field is in the range.
     *
     * @param field          the field name
     * @param min            the minimum length
     * @param max            the maximum length, or -1 for infinite
     * @param message        the message on error
     */
    public void addLengthConstraint(String field,
                                    int min,
                                    int max,
                                    String message) {

        constraints.add(new LengthConstraint(field, min, max, message));
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
     * @throws FormValidationException if the form validation failed
     */
    public void validate(Request request)
        throws FormValidationException {

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
         * @throws FormValidationException if the form validation
         *             failed
         */
        public void validate(Request request)
            throws FormValidationException {

            validate(request.getParameter(field));
        }

        /**
         * Validates this constraint.
         *
         * @param value          the form field value
         *
         * @throws FormValidationException if the form validation
         *             failed
         */
        protected abstract void validate(String value)
            throws FormValidationException;

        /**
         * Reports a form validation error. This method just throws
         * the adequate form exception.
         *
         * @throws FormValidationException the validation exception
         *             representing the constraint error
         */
        protected void error() throws FormValidationException {
            throw new FormValidationException(field, message);
        }

        /**
         * Reports a form validation error. This method just throws
         * the adequate form exception. Note that the field message
         * will be outputted with the additional detailes appended
         * after ": ".
         *
         * @param details        the additional error details
         *
         * @throws FormValidationException the validation exception
         *             representing the constraint error
         */
        protected void error(String details)
            throws FormValidationException {

            details = message + ": " + details;
            throw new FormValidationException(field, details);
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
         * @throws FormValidationException if the form validation
         *             failed
         */
        protected void validate(String value)
            throws FormValidationException {

            if (value == null || value.equals("")) {
                error();
            }
        }
    }


    /**
     * A form field length constraint.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    private class LengthConstraint extends Constraint {

        /**
         * The minimum length to allow.
         */
        private int min;

        /**
         * The maximum length to allow.
         */
        private int max;

        /**
         * Creates a new form field length constraint.
         *
         * @param field          the field name
         * @param min            the minimum length
         * @param max            the maximum length, or -1 for infinite
         * @param message        the error message
         */
        public LengthConstraint(String field,
                                int min,
                                int max,
                                String message) {
            super(field, message);
            this.min = min;
            this.max = max;
        }

        /**
         * Validates this constraint.
         *
         * @param value          the form field value
         *
         * @throws FormValidationException if the form validation
         *             failed
         */
        protected void validate(String value)
            throws FormValidationException {

            if (value == null) {
                return;
            }
            if (value.length() < min) {
                error();
            }
            if (max > 0 && value.length() > max) {
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
         * @throws FormValidationException if the form validation
         *             failed
         */
        protected void validate(String value)
            throws FormValidationException {

            if (value == null) {
                return;
            }
            for (int i = 0; i < value.length(); i++) {
                if (chars.indexOf(value.charAt(i)) < 0) {
                    error("'" + value.charAt(i) + "'");
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
         * @throws FormValidationException if the form validation
         *             failed
         */
        protected void validate(String value)
            throws FormValidationException {

            Date  date;

            if (value == null) {
                return;
            }
            try {
                date = format.parse(value);
                if (!value.equals(format.format(date))) {
                    error();
                }
            } catch (ParseException e) {
                error();
            }
        }
    }
}
