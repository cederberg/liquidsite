/*
 * PermissionsDialogHandler.java
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

import java.util.ArrayList;

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.admin.view.AdminView;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Permission;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.content.Group;
import net.percederberg.liquidsite.form.FormValidationException;
import net.percederberg.liquidsite.form.FormValidator;

/**
 * The permission editing request handler. This class handles the
 * permissions dialog workflow for domain and content objects.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PermissionsDialogHandler extends AdminDialogHandler {

    /**
     * Creates a new permissions dialog handler.
     */
    public PermissionsDialogHandler() {
        super("index.html", "permissions.html", true);
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

        Object  obj = AdminUtils.getReference(request);

        AdminView.DIALOG.viewPermissions(request, obj);
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

        ContentManager  manager = AdminUtils.getContentManager();
        Object          ref = AdminUtils.getReference(request);
        int             index = 0;
        Domain          domain;
        User            user;
        Group           group;
        String          str;

        if (ref instanceof Domain) {
            domain = (Domain) ref;
        } else {
            domain = ((Content) ref).getDomain();
        }
        if (!request.getParameter("inherit", "").equals("true")) {
            while (request.getParameter("perm_" + index + "_type") != null) {
                str = request.getParameter("perm_" + index + "_user");
                if (str != null) {
                    user = manager.getUser(domain, str);
                    if (user == null || user.isSuperUser()) {
                        str = "No user '" + str + "' found";
                        throw new FormValidationException("user", str);
                    }
                }
                str = request.getParameter("perm_" + index + "_group");
                if (str != null) {
                    group = manager.getGroup(domain, str);
                    if (group == null) {
                        str = "No group '" + str + "' found";
                        throw new FormValidationException("group", str);
                    }
                }
                index++;
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

        ContentManager  manager = AdminUtils.getContentManager();
        Object          ref = AdminUtils.getReference(request);
        User            user = request.getUser();
        Permission[]    permissions;
        ArrayList       list = new ArrayList();
        int             index = 0;

        if (!request.getParameter("inherit", "").equals("true")) {
            while (request.getParameter("perm_" + index + "_type") != null) {
                list.add(getPermission(request, index, ref));
                index++;
            }
        }
        permissions = new Permission[list.size()];
        list.toArray(permissions);
        if (ref instanceof Domain) {
            ((Domain) ref).setPermissions(user, permissions);
        } else {
            ((Content) ref).setPermissions(user, permissions);
        }
        return 0;
    }

    /**
     * Returns a permission from the request object. The permission
     * index and reference object must be supplied.
     *
     * @param request         the request
     * @param index           the permission index (from 0)
     * @param ref             the permission reference object
     *
     * @return the permission found in the request
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private Permission getPermission(Request request, int index, Object ref)
        throws ContentException {

        ContentManager  manager = AdminUtils.getContentManager();
        String          prefix = "perm_" + index + "_";
        Domain          domain = null;
        Content         content = null;
        User            user = null;
        Group           group = null;
        Permission      perm;
        String          str;

        if (ref instanceof Domain) {
            domain = (Domain) ref;
        } else {
            content = (Content) ref;
            domain = content.getDomain();
        }
        str = request.getParameter(prefix + "user");
        if (str != null) {
            user = manager.getUser(domain, str);
        }
        str = request.getParameter(prefix + "group");
        if (str != null) {
            group = manager.getGroup(domain, str);
        }
        if (ref instanceof Domain) {
            perm = new Permission(manager, domain, user, group);
        } else {
            perm = new Permission(manager, content, user, group);
        }
        str = request.getParameter(prefix + "read");
        if (str != null) {
            perm.setRead(true);
        }
        str = request.getParameter(prefix + "write");
        if (str != null) {
            perm.setWrite(true);
        }
        str = request.getParameter(prefix + "publish");
        if (str != null) {
            perm.setPublish(true);
        }
        str = request.getParameter(prefix + "admin");
        if (str != null) {
            perm.setAdmin(true);
        }

        return perm;
    }
}
