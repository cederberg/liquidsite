/*
 * UsersAddFormHandler.java
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
 * Copyright (c) 2004-2005 Per Cederberg. All rights reserved.
 */

package org.liquidsite.app.admin;

import org.liquidsite.app.admin.view.AdminView;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.Group;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.FormValidationException;
import org.liquidsite.core.web.Request;

/**
 * The users add request handler. This class handles the various
 * add workflows for the users view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class UsersAddFormHandler extends AdminFormHandler {

    /**
     * Creates a new users add form handler.
     */
    public UsersAddFormHandler() {
        super("users.html", "add-users.html", true);
    }

    /**
     * Displays a form for the specified workflow step. This method
     * will NOT be called when returning to the start page.
     *
     * @param request        the request object
     * @param step           the workflow step
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    protected void displayStep(Request request, int step)
        throws ContentException, ContentSecurityException {

        String  type = request.getParameter("type", "");

        if (type.equals("user")) {
            AdminView.USER.viewEditUser(request, getDomain(request), null);
        } else if (type.equals("group")) {
            AdminView.USER.viewEditGroup(request, null);
        } else {
            throw new ContentException("cannot add this object");
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
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     * @throws FormValidationException if the form request data
     *             validation failed
     */
    protected void validateStep(Request request, int step)
        throws ContentException, ContentSecurityException,
               FormValidationException {

        UsersEditFormHandler  edit = UsersEditFormHandler.getInstance();
        String                type = request.getParameter("type", "");

        edit.validateStep(request, step);
        if (type.equals("user")) {
            validateUser(request);
        } else if (type.equals("group")) {
            validateGroup(request);
        }
    }

    /**
     * Validates a user add form.
     *
     * @param request        the request object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     * @throws FormValidationException if the form request data
     *             validation failed
     */
    private void validateUser(Request request)
        throws ContentException, ContentSecurityException,
               FormValidationException {

        ContentManager  manager = AdminUtils.getContentManager();
        String          name;
        String          message;

        if (request.getParameter("password", "").length() <= 0) {
            message = "The user password field cannot be empty";
            throw new FormValidationException("password", message);
        }
        name = request.getParameter("name");
        if (manager.getUser(getDomain(request), name) != null) {
            message = "A user with the specified login name already exists";
            throw new FormValidationException("name", message);
        }
    }

    /**
     * Validates a group add form.
     *
     * @param request        the request object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     * @throws FormValidationException if the form request data
     *             validation failed
     */
    private void validateGroup(Request request)
        throws ContentException, ContentSecurityException,
               FormValidationException {

        ContentManager  manager = AdminUtils.getContentManager();
        String          name;
        String          message;

        name = request.getParameter("name");
        if (manager.getGroup(getDomain(request), name) != null) {
            message = "A group with the specified name already exists";
            throw new FormValidationException("name", message);
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

        String  type = request.getParameter("type", "");

        if (type.equals("user")) {
            handleAddUser(request, getDomain(request));
        } else if (type.equals("group")) {
            handleAddGroup(request, getDomain(request));
        } else {
            throw new ContentException("cannot add this object");
        }
        return 0;
    }

    /**
     * Handles the add user form.
     *
     * @param request        the request object
     * @param domain         the domain
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleAddUser(Request request, Domain domain)
        throws ContentException, ContentSecurityException {

        ContentManager        manager = AdminUtils.getContentManager();
        UsersEditFormHandler  edit = UsersEditFormHandler.getInstance();
        User                  user;

        user = new User(manager, domain, request.getParameter("name"));
        user.setEnabled(request.getParameter("enabled") != null);
        user.setPassword(request.getParameter("password"));
        user.setRealName(request.getParameter("realname", ""));
        user.setEmail(request.getParameter("email", ""));
        user.setComment(request.getParameter("comment", ""));
        edit.setUserGroups(request, user);
        user.save(request.getUser());
    }

    /**
     * Handles the add group form.
     *
     * @param request        the request object
     * @param domain         the domain
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleAddGroup(Request request, Domain domain)
        throws ContentException, ContentSecurityException {

        ContentManager   manager = AdminUtils.getContentManager();
        Group            group;

        group = new Group(manager, domain, request.getParameter("name"));
        group.setDescription(request.getParameter("description", ""));
        group.setPublic(request.getParameter("public") != null);
        group.setComment(request.getParameter("comment", ""));
        group.save(request.getUser());
    }

    /**
     * Returns the domain referenced in the request.
     *
     * @param request        the request object
     *
     * @return the domain referenced in the request, or
     *         null for none
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private Domain getDomain(Request request)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        String          name = request.getParameter("domain", "");

        if (name.equals("")) {
            return null;
        } else {
            return manager.getDomain(request.getUser(), name);
        }
    }
}
