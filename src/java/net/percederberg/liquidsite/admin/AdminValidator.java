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
import net.percederberg.liquidsite.form.FormValidationException;
import net.percederberg.liquidsite.form.FormValidator;

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
     * The edit user form validator.
     */
    private FormValidator editUser = new FormValidator();

    /**
     * The edit password form validator.
     */
    private FormValidator editPassword = new FormValidator();

    /**
     * The add domain form validator.
     */
    private FormValidator addDomain = new FormValidator();

    /**
     * The site form validator.
     */
    private FormValidator site = new FormValidator();

    /**
     * The folder form validator.
     */
    private FormValidator folder = new FormValidator();

    /**
     * The page form validator.
     */
    private FormValidator page = new FormValidator();

    /**
     * The file form validator.
     */
    private FormValidator file = new FormValidator();

    /**
     * The template form validator.
     */
    private FormValidator template = new FormValidator();

    /**
     * The publish and unpublish form validator.
     */
    private FormValidator publish = new FormValidator();

    /**
     * Creates a new administration validator.
     */
    public AdminValidator() {
        initialize();
    }
    
    /**
     * Initializes all the form validators.
     */
    private void initialize() {
        String  error;
        String  chars;
        
        // Edit user validator
        error = "No user name specified";
        editUser.addRequiredConstraint("name", error);

        // Edit password validator
        error = "Existing password not specified";
        editPassword.addRequiredConstraint("password0", error);
        error = "New password not specified";
        editPassword.addRequiredConstraint("password1", error);
        error = "Verification of new password not specified";
        editPassword.addRequiredConstraint("password2", error);

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
        error = "No revision comment specified";
        site.addRequiredConstraint("comment", error);

        // Add and edit folder validator
        folder.addRequiredConstraint("name", "No folder name specified");
        chars = UPPERCASE_CHARACTERS + LOWERCASE_CHARACTERS +
                NUMBER_CHARACTERS + ".-_";
        error = "Folder name contains invalid character";
        folder.addCharacterConstraint("name", chars, error);
        folder.addRequiredConstraint("comment", "No comment specified");
    
        // Add and edit page validator
        page.addRequiredConstraint("name", "No page name specified");
        chars = UPPERCASE_CHARACTERS + LOWERCASE_CHARACTERS +
                NUMBER_CHARACTERS + ".-_";
        error = "Page name contains invalid character";
        page.addCharacterConstraint("name", chars, error);
        page.addRequiredConstraint("comment", "No comment specified");

        // Add and edit file validator
        file.addRequiredConstraint("name", "No file name specified");
        chars = UPPERCASE_CHARACTERS + LOWERCASE_CHARACTERS +
                NUMBER_CHARACTERS + ".-_";
        error = "File name contains invalid character";
        file.addCharacterConstraint("name", chars, error);
        file.addRequiredConstraint("comment", "No comment specified");

        // Add and edit template validator
        template.addRequiredConstraint("name", "No template name specified");
        template.addRequiredConstraint("comment", "No comment specified");

        // Add publish validator
        publish.addRequiredConstraint("date", "No publish date specified");
        error = "Date format should be 'YYYY-MM-DD HH:MM'";
        publish.addDateConstraint("date", DATE_FORMAT, error); 
        publish.addRequiredConstraint("comment", "No comment specified");
    }

    /**
     * Validates the parameters in an edit user request.
     * 
     * @param request        the edit user request
     * 
     * @throws FormValidationException if the form validation failed
     */
    public void validateEditUser(Request request)
        throws FormValidationException {

        editUser.validate(request);
    }

    /**
     * Validates the parameters in an edit password request.
     * 
     * @param request        the edit password request
     * 
     * @throws FormValidationException if the form validation failed
     */
    public void validateEditPassword(Request request)
        throws FormValidationException {

        String  pass1 = request.getParameter("password1");
        String  pass2 = request.getParameter("password2");

        editPassword.validate(request);
        if (!pass1.equals(pass2)) {
            pass1 = "The password verification doesn't match new password";
            throw new FormValidationException("password2", pass1);
        }
    }

    /**
     * Validates the parameters in an add domain request.
     * 
     * @param request        the add domain request
     * 
     * @throws FormValidationException if the form validation failed
     */
    public void validateAddDomain(Request request)
        throws FormValidationException {

        addDomain.validate(request);
    }

    /**
     * Validates the parameters in an add and edit site request.
     * 
     * @param request        the add or edit site request
     * 
     * @throws FormValidationException if the form validation failed
     */
    public void validateSite(Request request)
        throws FormValidationException {

        site.validate(request);
    }

    /**
     * Validates the parameters in an add and edit folder request.
     * 
     * @param request        the add or edit folder request
     * 
     * @throws FormValidationException if the form validation failed
     */
    public void validateFolder(Request request)
        throws FormValidationException {

        folder.validate(request);
    }

    /**
     * Validates the parameters in an add and edit page request.
     * 
     * @param request        the add or edit page request
     * 
     * @throws FormValidationException if the form validation failed
     */
    public void validatePage(Request request) 
        throws FormValidationException {

        page.validate(request);
    }

    /**
     * Validates the parameters in an add and edit file request.
     * 
     * @param request        the add or edit file request
     * 
     * @throws FormValidationException if the form validation failed
     */
    public void validateFile(Request request) 
        throws FormValidationException {

        file.validate(request);
    }

    /**
     * Validates the parameters in an add and edit template request.
     * 
     * @param request        the add or edit template request
     * 
     * @throws FormValidationException if the form validation failed
     */
    public void validateTemplate(Request request) 
        throws FormValidationException {

        template.validate(request);
    }

    /**
     * Validates the parameters in a publish request.
     * 
     * @param request        the publish request
     * 
     * @throws FormValidationException if the form validation failed
     */
    public void validatePublish(Request request) 
        throws FormValidationException {

        publish.validate(request);
    }
}
