/*
 * HomeEditFormHandler.java
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

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.admin.view.AdminView;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.form.FormValidationException;
import net.percederberg.liquidsite.form.FormValidator;

/**
 * The home edit request handler. This class handles the edit user 
 * and password workflows for the home view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class HomeEditFormHandler extends AdminFormHandler {

    /**
     * The edit user form validator.
     */
    private FormValidator editUser = new FormValidator();

    /**
     * The edit password form validator.
     */
    private FormValidator editPassword = new FormValidator();

    /**
     * Creates a new home edit request handler.
     */
    public HomeEditFormHandler() {
        super("home.html", "edit-home.html", false);
        initialize();
    }
        
    /**
     * Initializes the form validators.
     */
    private void initialize() {
        String  error;

        // Edit user validator
        error = "No user name specified";
        editUser.addRequiredConstraint("name", error);

        // Edit password validator
        error = "New password not specified";
        editPassword.addRequiredConstraint("password1", error);
        error = "Verification of new password not specified";
        editPassword.addRequiredConstraint("password2", error);
    }

    /**
     * Displays a form for the specified workflow step. This method
     * will NOT be called when returning to the start page.
     * 
     * @param request        the request object
     * @param step           the workflow step
     */
    protected void displayStep(Request request, int step) {
        if (request.getParameter("edituser", "").equals("true")) {
            AdminView.HOME.viewEditUser(request);
        } else {
            AdminView.HOME.viewEditPassword(request);
        }
    }

    /**
     * Validates a form for the specified workflow step. If the form
     * validation fails in this step, the form page for the workflow 
     * step will be displayed again with an 'error' attribute 
     * containing the message in the validation exception.
     * 
     * @param request        the request object
     * @param step           the workflow step
     * 
     * @throws FormValidationException if the form request data 
     *             validation failed
     */
    protected void validateStep(Request request, int step)
        throws FormValidationException {

        User    user = request.getUser();
        String  pass1;
        String  pass2;

        if (request.getParameter("edituser", "").equals("true")) {
            editUser.validate(request);
        } else {
            editPassword.validate(request);
            pass1 = request.getParameter("password1");
            pass2 = request.getParameter("password2");
            if (!pass1.equals(pass2)) {
                pass1 = "The password verification doesn't match new password";
                throw new FormValidationException("password2", pass1);
            }
            if (!user.verifyPassword(request.getParameter("password0"))) {
                throw new FormValidationException(
                    "password0",
                    "Current password was incorrect");
            }
        }
    }

    /**
     * Handles a validated form for the specified workflow step. This
     * method returns the next workflow step, i.e. the step used when
     * calling the display method. If the special zero (0) workflow 
     * step is returned, the workflow is assumed to have terminated.
     * Note that this method also allows additional validation to 
     * occur. By returning the incoming workflow step number and 
     * setting the appropriate request attributes the same results as
     * in the normal validate method can be achieved. For recoverable
     * errors, this is the recommended course of action.
     *  
     * @param request        the request object
     * @param step           the workflow step
     * 
     * @return the next workflow step, or 
     *         zero (0) if the workflow has finished
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    protected int handleStep(Request request, int step) 
        throws ContentException, ContentSecurityException {

        User  user = request.getUser();

        if (request.getParameter("edituser", "").equals("true")) {
            user.setRealName(request.getParameter("name"));
            user.setEmail(request.getParameter("email"));
            user.save(user);
        } else {
            user.setPassword(request.getParameter("password1"));
            user.save(user);
        }
        return 0;
    }
}
