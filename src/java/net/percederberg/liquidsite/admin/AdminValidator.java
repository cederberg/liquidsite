/*
 * AdminValidator.java
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

import java.text.SimpleDateFormat;

import net.percederberg.liquidsite.Request;

/**
 * A form validator for the administration application.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class AdminValidator {

    /**
     * The set of ASCII uppercase characters.
     */
    private static final String UPPERCASE_CHARACTERS = 
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        
    /**
     * The set of ASCII lowercase characters.
     */
    private static final String LOWERCASE_CHARACTERS = 
        "abcdefghijklmonpqrstuvwxyz";
        
    /**
     * The set of ASCII number characters.
     */
    private static final String NUMBER_CHARACTERS = 
        "0123456789";

    /**
     * The date format used by this class.
     */
    private static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * The add domain form validator.
     */
    private FormValidator addDomain = new FormValidator();

    /**
     * The site form validator.
     */
    private FormValidator site = new FormValidator();

    /**
     * The publish and unpublish form validator.
     */
    private FormValidator publish = new FormValidator();

    /**
     * Creates a new administration validator.
     */
    public AdminValidator() {
        String  error;
        String  chars;
        
        // Add domain validator
        addDomain.addRequiredConstraint("name", "No domain name specified");
        chars = UPPERCASE_CHARACTERS + NUMBER_CHARACTERS;
        error = "Domain name must be upper-case, invalid character";
        addDomain.addCharacterConstraint("name", chars, error);
        error = "No description specified";
        addDomain.addRequiredConstraint("description", error);
        addDomain.addRequiredConstraint("host", "No host name specified");
        chars = LOWERCASE_CHARACTERS + NUMBER_CHARACTERS + ".-_";
        error = "Host name must be lower-case, invalid character";
        addDomain.addCharacterConstraint("host", chars, error);

        // Add and edit site validator
        site.addRequiredConstraint("name", "No site name specified");
        site.addRequiredConstraint("protocol", "No protocol specified");
        error = "Protocol must be either 'http' or 'https', " +
                "invalid character";
        site.addCharacterConstraint("protocol", "https", error);
        site.addRequiredConstraint("host", "No host name specified");
        chars = LOWERCASE_CHARACTERS + NUMBER_CHARACTERS + ".-_*";
        error = "Host name must be lower-case, invalid character";
        site.addCharacterConstraint("host", chars, error);
        site.addRequiredConstraint("port", "No port number specified");
        error = "Port number must be numeric, invalid character";
        site.addCharacterConstraint("port", NUMBER_CHARACTERS, error);
        site.addRequiredConstraint("dir", "No base directory specified");
        chars = UPPERCASE_CHARACTERS + LOWERCASE_CHARACTERS +
                NUMBER_CHARACTERS + ".-_/";
        error = "Base directory contains invalid character";
        site.addCharacterConstraint("dir", chars, error);

        // Add publish validator
        publish.addRequiredConstraint("date", "No publish date specified");
        error = "Date format should be 'YYYY-MM-DD HH:MM'";
        publish.addDateConstraint("date", DATE_FORMAT, error); 
        publish.addRequiredConstraint("comment", "No comment specified");
    }

    /**
     * Validates the parameters in an add domain request.
     * 
     * @param request        the add domain request
     * 
     * @throws FormException if the form validation failed
     */
    public void validateAddDomain(Request request) throws FormException {
        addDomain.validate(request);
    }

    /**
     * Validates the parameters in an add and edit site request.
     * 
     * @param request        the add or edit site request
     * 
     * @throws FormException if the form validation failed
     */
    public void validateSite(Request request) throws FormException {
        site.validate(request);
    }

    /**
     * Validates the parameters in a publish request.
     * 
     * @param request        the publish request
     * 
     * @throws FormException if the form validation failed
     */
    public void validatePublish(Request request) throws FormException {
        publish.validate(request);
    }
}
