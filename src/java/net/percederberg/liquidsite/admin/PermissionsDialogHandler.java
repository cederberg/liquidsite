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
import net.percederberg.liquidsite.content.PermissionList;
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
     * @throws FormValidationException if the form request data
     *             validation failed
     */
    protected void validateStep(Request request, int step)
        throws ContentException, FormValidationException {

        Permission[]  permissions;
        User          user;
        Group[]       groups;
        boolean       read = false;
        boolean       write = false;
        boolean       publish = false;
        boolean       admin = false;
        String        str;

        // Check for unexisting users or groups
        try {
            permissions = getPermissions(request);
        } catch (ContentSecurityException e) {
            throw new FormValidationException("", e.getMessage());
        }

        // Check permissions for sanity
        user = request.getUser();
        groups = user.getGroups();
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].isMatch(null, null)
             && permissions[i].getAdmin()) {

                str = "Cannot set admin permission for anonymous user";
                throw new FormValidationException("", str);
            }
            if (permissions[i].isMatch(user, groups)) {
                if (permissions[i].getRead()) {
                    read  = true;
                }
                if (permissions[i].getWrite()) {
                    write  = true;
                }
                if (permissions[i].getPublish()) {
                    publish  = true;
                }
                if (permissions[i].getAdmin()) {
                    admin  = true;
                }
            }
        }
        if (!user.isSuperUser() && !read) {
            str = "Cannot remove read permission for current user";
            throw new FormValidationException("", str);
        }
        if (!user.isSuperUser() && !write) {
            str = "Cannot remove write permission for current user";
            throw new FormValidationException("", str);
        }
        if (!user.isSuperUser() && !publish) {
            str = "Cannot remove publish permission for current user";
            throw new FormValidationException("", str);
        }
        if (!user.isSuperUser() && !admin) {
            str = "Cannot remove admin permission for current user";
            throw new FormValidationException("", str);
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

        Object          ref = AdminUtils.getReference(request);
        PermissionList  permissions;
        Permission[]    perms;

        perms = getPermissions(request);
        if (ref instanceof Domain) {
            permissions = ((Domain) ref).getPermissions();
        } else {
            permissions = ((Content) ref).getPermissions();
        }
        permissions.setPermissions(perms);
        permissions.save(request.getUser());
        return 0;
    }

    /**
     * Returns the permissions from the request object. If the
     * inherited flag is set in the request, an empty array will be
     * returned.
     *
     * @param request         the request
     *
     * @return an array of permissions found in the request
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if a permission user or
     *             group didn't exist in the database
     */
    private Permission[] getPermissions(Request request)
        throws ContentException, ContentSecurityException {

        Object        ref = AdminUtils.getReference(request);
        ArrayList     list = new ArrayList();
        Permission[]  permissions;
        int           index = 0;

        if (!request.getParameter("inherit", "").equals("true")) {
            while (request.getParameter("perm_" + index + "_type") != null) {
                list.add(getPermission(request, index, ref));
                index++;
            }
        }
        permissions = new Permission[list.size()];
        list.toArray(permissions);
        return permissions;
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
     * @throws ContentSecurityException if the permission user or
     *             group didn't exist in the database
     */
    private Permission getPermission(Request request, int index, Object ref)
        throws ContentException, ContentSecurityException {

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
            if (user == null) {
                str = "Couldn't find user '" + str + "'";
                throw new ContentSecurityException(str);
            } else if (user.isSuperUser()) {
                str = "Cannot set permissions for superuser '" + str + "'";
                throw new ContentSecurityException(str);
            }
        }
        str = request.getParameter(prefix + "group");
        if (str != null) {
            group = manager.getGroup(domain, str);
            if (group == null) {
                str = "Couldn't find group '" + str + "'";
                throw new ContentSecurityException(str);
            }
        }
        if (ref instanceof Domain) {
            perm = new Permission(user, group);
        } else {
            perm = new Permission(user, group);
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
