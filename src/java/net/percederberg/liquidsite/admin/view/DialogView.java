/*
 * DialogView.java
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

package net.percederberg.liquidsite.admin.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.admin.AdminUtils;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Group;
import net.percederberg.liquidsite.content.Permission;
import net.percederberg.liquidsite.content.PermissionList;
import net.percederberg.liquidsite.content.User;

/**
 * A helper class for the dialog views. This class contains methods
 * for creating the HTML responses to the various dialog pages.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DialogView extends AdminView {

    /**
     * Creates a new dialog view helper.
     */
    DialogView() {
    }

    /**
     * Shows the automatic close dialog.
     *
     * @param request        the request object
     */
    public void viewClose(Request request) {
        request.sendTemplate("admin/dialog/close.ftl");
    }

    /**
     * Shows the error message dialog.
     *
     * @param request        the request object
     * @param message        the error message
     */
    public void viewError(Request request, String message) {
        request.setAttribute("error", message);
        request.sendTemplate("admin/dialog/error.ftl");
    }

    /**
     * Shows the error message dialog.
     *
     * @param request        the request object
     * @param e              the content database error
     */
    public void viewError(Request request, ContentException e) {
        viewError(request, "Database access error, " + e.getMessage());
    }

    /**
     * Shows the error message dialog.
     *
     * @param request        the request object
     * @param e              the content security error
     */
    public void viewError(Request request, ContentSecurityException e) {
        viewError(request, "Security violation, " + e.getMessage());
    }

    /**
     * Shows the delete object confirmation dialog.
     *
     * @param request        the request object
     * @param obj            the object to delete
     */
    public void viewDelete(Request request, Object obj) {
        String  name;

        AdminUtils.setReference(request, obj);
        if (obj instanceof Domain) {
            name = ((Domain) obj).getName();
        } else {
            name = ((Content) obj).getName();
        }
        request.setAttribute("name", name);
        request.sendTemplate("admin/dialog/delete-object.ftl");
    }

    /**
     * Shows the delete user confirmation dialog.
     *
     * @param request        the request object
     * @param obj            the user or group to delete
     */
    public void viewDeleteUser(Request request, Object obj) {
        String  type;
        String  name;

        if (obj instanceof User) {
            type = "user";
            name = ((User) obj).getName();
        } else {
            type = "group";
            name = ((Group) obj).getName();
        }
        request.setAttribute("type", type);
        request.setAttribute("domain", request.getParameter("domain"));
        request.setAttribute("name", name);
        request.sendTemplate("admin/dialog/delete-user.ftl");
    }

    /**
     * Shows the content publish dialog.
     *
     * @param request        the request object
     * @param content        the content object to publish
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewPublish(Request request, Content content)
        throws ContentException {

        User       user = request.getUser();
        String     dateStr = request.getParameter("date");
        String     comment = request.getParameter("comment");
        Content[]  revisions;
        Date       date;

        if (dateStr == null) {
            revisions = content.getAllRevisions();
            date = revisions[0].getOnlineDate();
            if (date == null || date.after(new Date())) {
                date = new Date();
            }
            dateStr = AdminUtils.formatDate(user, date);
        }
        if (comment == null) {
            if (content.getRevisionNumber() == 0) {
                comment = content.getComment();
            } else {
                comment = "Published";
            }
        }
        AdminUtils.setReference(request, content);
        request.setAttribute("date", dateStr);
        request.setAttribute("comment", comment);
        request.sendTemplate("admin/dialog/publish-object.ftl");
    }

    /**
     * Shows the content unpublish dialog.
     *
     * @param request        the request object
     * @param content        the content object to unpublish
     */
    public void viewUnpublish(Request request, Content content) {
        User    user = request.getUser();
        String  dateStr = request.getParameter("date");
        String  comment = request.getParameter("comment");

        if (dateStr == null) {
            dateStr = AdminUtils.formatDate(user, content.getOfflineDate());
        }
        if (dateStr.equals("")) {
            dateStr = AdminUtils.formatDate(user, new Date());
        }
        if (comment == null) {
            comment = "Unpublished";
        }
        AdminUtils.setReference(request, content);
        request.setAttribute("date", dateStr);
        request.setAttribute("comment", comment);
        request.sendTemplate("admin/dialog/unpublish-object.ftl");
    }

    /**
     * Shows the content revert dialog.
     *
     * @param request        the request object
     * @param content        the content object to revert
     */
    public void viewRevert(Request request, Content content) {
        String  str;

        AdminUtils.setReference(request, content);
        if (content.getRevisionNumber() == 0) {
            str = "Work";
        } else {
            str = String.valueOf(content.getRevisionNumber());
        }
        request.setAttribute("revision", str);
        request.sendTemplate("admin/dialog/revert-object.ftl");
    }

    /**
     * Shows the permissions editing dialog.
     *
     * @param request         the request object
     * @param obj             the domain or content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewPermissions(Request request, Object obj)
        throws ContentException {

        Domain     domain;
        Content    content;
        ArrayList  inherited;
        ArrayList  local;
        HashMap    map;
        int        index = 0;
        String     str;

        AdminUtils.setReference(request, obj);
        if (obj instanceof Domain) {
            domain = (Domain) obj;
            inherited = getInheritedPermissions(domain);
            local = getPermissions(domain.getPermissions().getPermissions(),
                                   false);
        } else {
            content = (Content) obj;
            domain = content.getDomain();
            inherited = getInheritedPermissions(content);
            local = getPermissions(content.getPermissions().getPermissions(),
                                   false);
        }
        if (request.getParameter("perm_0_type") != null) {
            local.clear();
            while (request.getParameter("perm_" + index + "_type") != null) {
                map = new HashMap();
                str = request.getParameter("perm_" + index + "_user", "");
                map.put("user", str);
                str = request.getParameter("perm_" + index + "_group", "");
                map.put("group", str);
                str = request.getParameter("perm_" + index + "_read");
                map.put("read", (str == null) ? "false" : "true");
                str = request.getParameter("perm_" + index + "_write");
                map.put("write", (str == null) ? "false" : "true");
                str = request.getParameter("perm_" + index + "_publish");
                map.put("publish", (str == null) ? "false" : "true");
                str = request.getParameter("perm_" + index + "_admin");
                map.put("admin", (str == null) ? "false" : "true");
                local.add(map);
                index++;
            }
        }
        request.setAttribute("groups", findGroups(domain, ""));
        request.setAttribute("inherited", inherited);
        request.setAttribute("local", local);
        request.sendTemplate("admin/dialog/permissions-object.ftl");
    }

    /**
     * Shows the content unlock dialog.
     *
     * @param request        the request object
     * @param content        the content object to unlock
     */
    public void viewUnlock(Request request, Content content) {
        AdminUtils.setReference(request, content);
        request.sendTemplate("admin/dialog/unlock-object.ftl");
    }

    /**
     * Finds all inherited permissions for a domain. The permissions
     * will not be added directly to the result list, but rather a
     * simplified hash map containing the fields of each permission
     * will be added.
     *
     * @param domain         the domain object
     *
     * @return the list of permissions found (in maps)
     */
    private ArrayList getInheritedPermissions(Domain domain) {
        return getPermissions(new Permission[0], true);
    }

    /**
     * Finds all inherited permissions for a content object. The
     * permissions will not be added directly to the result list, but
     * rather a simplified hash map containing the fields of each
     * permission will be added.
     *
     * @param content         the content object
     *
     * @return the list of permissions found (in maps)
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private ArrayList getInheritedPermissions(Content content)
        throws ContentException {

        PermissionList  permissions;
        Content         parent = content;

        do {
            parent = parent.getParent();
            if (parent == null) {
                permissions = content.getDomain().getPermissions();
            } else {
                permissions = parent.getPermissions();
            }
        } while (permissions.isEmpty() && parent != null);

        return getPermissions(permissions.getPermissions(), true);
    }

    /**
     * Returns a list of simplified permission maps.
     *
     * @param permissions     the array of permission objects
     * @param addEmpty        the add empty permission flag
     *
     * @return the list of permission maps
     */
    private ArrayList getPermissions(Permission[] permissions,
                                     boolean addEmpty) {

        ArrayList  result = new ArrayList();
        HashMap    map;

        if (permissions.length == 0 && addEmpty) {
            map = new HashMap();
            map.put("user", "");
            map.put("group", "");
            map.put("read", "false");
            map.put("write", "false");
            map.put("publish", "false");
            map.put("admin", "false");
            result.add(map);
        }
        for (int i = 0; i < permissions.length; i++) {
            map = new HashMap();
            map.put("user", permissions[i].getUserName());
            map.put("group", permissions[i].getGroupName());
            map.put("read", String.valueOf(permissions[i].getRead()));
            map.put("write", String.valueOf(permissions[i].getWrite()));
            map.put("publish", String.valueOf(permissions[i].getPublish()));
            map.put("admin", String.valueOf(permissions[i].getAdmin()));
            result.add(map);
        }

        return result;
    }
}
