/*
 * UsersDeleteDialogHandler.java
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

package net.percederberg.liquidsite.admin;

import net.percederberg.liquidsite.admin.view.AdminView;
import net.percederberg.liquidsite.web.Request;

import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.Group;
import org.liquidsite.core.content.User;

/**
 * The users delete request handler. This class handles the delete
 * dialog workflow for user and group objects.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class UsersDeleteDialogHandler extends AdminDialogHandler {

    /**
     * Creates a new users delete dialog handler.
     */
    public UsersDeleteDialogHandler() {
        super("index.html", "delete-user.html", true);
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

        AdminView.DIALOG.viewDeleteUser(request, ref);
    }

    /**
     * Validates a form for the specified workflow step. If the form
     * validation fails in this step, the form page for the workflow
     * step will be displayed again with an 'error' attribute
     * containing the message in the validation exception.
     *
     * @param request        the request object
     * @param step           the workflow step
     */
    protected void validateStep(Request request, int step) {
        // Nothing to do here
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

        Object   ref = getReference(request);
        User     user;
        Group    group;

        if (ref instanceof User) {
            user = (User) ref;
            if (user.equals(request.getUser())) {
                throw new ContentSecurityException(
                    "Cannot delete the current user");
            }
            user.delete(request.getUser());
        } else if (ref instanceof Group) {
            group = (Group) ref;
            group.delete(request.getUser());
        }
        return 0;
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
}
