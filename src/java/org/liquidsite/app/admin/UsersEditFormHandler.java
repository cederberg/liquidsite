/*
 * UsersEditFormHandler.java
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

import java.util.ArrayList;
import java.util.Iterator;

import org.liquidsite.app.admin.view.AdminView;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.Group;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.FormValidationException;
import org.liquidsite.core.web.FormValidator;
import org.liquidsite.core.web.Request;

/**
 * The users edit request handler. This class handles the various
 * edit workflows for the users view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class UsersEditFormHandler extends AdminFormHandler {

    /***
     * The latest object instance created.
     */
    private static UsersEditFormHandler instance = null;

    /**
     * The user form validator.
     */
    private FormValidator userValidator = new FormValidator();

    /**
     * The group form validator.
     */
    private FormValidator groupValidator = new FormValidator();

    /**
     * Returns an instance of this class. If a prior instance has
     * been created, it will be returned instead of creating a new
     * one.
     *
     * @return an instance of a users edit form handler
     */
    public static UsersEditFormHandler getInstance() {
        if (instance == null) {
            return new UsersEditFormHandler();
        } else {
            return instance;
        }
    }

    /**
     * Creates a new users edit form handler.
     */
    public UsersEditFormHandler() {
        super("users.html", "edit-users.html", true);
        instance = this;
        initialize();
    }

    /**
     * Initializes all the form validators.
     */
    private void initialize() {
        String  error;

        // Add and edit user validator
        error = "No login name specified";
        userValidator.addRequiredConstraint("name", error);
        error = "Login name contains invalid character";
        userValidator.addCharacterConstraint("name",
                                             User.NAME_CHARS,
                                             error);

        // Add and edit group validator
        error = "No group name specified";
        groupValidator.addRequiredConstraint("name", error);
        error = "Group name contains invalid character";
        groupValidator.addCharacterConstraint("name",
                                              Group.NAME_CHARS,
                                              error);
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

        Object  ref = getReference(request);
        User    user;

        if (ref instanceof User) {
            user = (User) ref;
            AdminView.USER.viewEditUser(request, user.getDomain(), user);
        } else if (ref instanceof Group) {
            AdminView.USER.viewEditGroup(request, (Group) ref);
        } else {
            throw new ContentException("cannot edit this object");
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

        String  type = request.getParameter("type", "");
        String  message;
        String  domain;
        String  name;
        String  enabled;

        if (type.equals("user")) {
            userValidator.validate(request);
            domain = request.getParameter("domain", "");
            name = request.getParameter("name");
            enabled = request.getParameter("enabled", "");
            if (request.getUser().getDomainName().equals(domain)
             && request.getUser().getName().equals(name)
             && !enabled.equals("true")) {

                message = "Cannot disable the current user";
                throw new FormValidationException("enabled", message);
            }
        } else if (type.equals("group")) {
            groupValidator.validate(request);
        } else {
            message = "Unknown object type specified";
            throw new FormValidationException("type", message);
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

        Object  ref = getReference(request);

        if (ref instanceof User) {
            handleEditUser(request, (User) ref);
        } else if (ref instanceof Group) {
            handleEditGroup(request, (Group) ref);
        } else {
            throw new ContentException("cannot edit this object");
        }
        return 0;
    }

    /**
     * Handles the edit user form.
     *
     * @param request        the request object
     * @param user           the user object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleEditUser(Request request, User user)
        throws ContentException, ContentSecurityException {

        user.setEnabled(request.getParameter("enabled") != null);
        if (!request.getParameter("password", "").equals("")) {
            user.setPassword(request.getParameter("password"));
        }
        user.setRealName(request.getParameter("realname", ""));
        user.setEmail(request.getParameter("email", ""));
        user.setComment(request.getParameter("comment", ""));
        setUserGroups(request, user);
        user.save(request.getUser());
    }

    /**
     * Handles the edit group form.
     *
     * @param request        the request object
     * @param group          the group object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleEditGroup(Request request, Group group)
        throws ContentException, ContentSecurityException {

        group.setDescription(request.getParameter("description", ""));
        group.setPublic(request.getParameter("public") != null);
        group.setComment(request.getParameter("comment", ""));
        group.save(request.getUser());
    }

    /**
     * Returns the request reference object. This will be either a
     * user or a group.
     *
     * @param request        the request object
     *
     * @return the request reference, or
     *         null for none
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private Object getReference(Request request)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        String          type = request.getParameter("type", "");
        String          domainName = request.getParameter("domain", "");
        String          name = request.getParameter("name", "");
        Domain          domain;

        if (domainName.equals("")) {
            domain = null;
        } else {
            domain = manager.getDomain(request.getUser(), domainName);
        }
        if (type.equals("user")) {
            return manager.getUser(domain, name);
        } else if (type.equals("group")) {
            return manager.getGroup(domain, name);
        } else {
            return null;
        }
    }

    /**
     * Sets the user groups from the request.
     *
     * @param request        the request
     * @param user           the user to add or remove groups for
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void setUserGroups(Request request, User user)
        throws ContentException {

        ContentManager  manager = AdminUtils.getContentManager();
        ArrayList       remove = new ArrayList();
        Group           group;
        Group[]         groups;
        Iterator        iter;
        String          param;

        // Initialize remove list
        groups = user.getGroups();
        for (int i = 0; i < groups.length; i++) {
            remove.add(groups[i]);
        }

        // Add groups from request
        iter = request.getAllParameters().keySet().iterator();
        while (iter.hasNext()) {
            param = iter.next().toString();
            if (param.startsWith("member")) {
                group = manager.getGroup(user.getDomain(),
                                         request.getParameter(param));
                if (remove.contains(group)) {
                    remove.remove(group);
                } else {
                    user.addToGroup(group);
                }
            }
        }

        // Remove groups not in request
        for (int i = 0; i < remove.size(); i++) {
            user.removeFromGroup((Group) remove.get(i));
        }
    }
}
