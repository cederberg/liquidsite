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

import java.util.Date;

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.admin.AdminUtils;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Group;
import net.percederberg.liquidsite.content.User;

/**
 * A helper class for the dialog views. This class contains methods 
 * for creating the HTML responses to the various dialog pages.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DialogView {

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
     * Shows the content unlock dialog.
     * 
     * @param request        the request object
     * @param content        the content object to unlock
     */
    public void viewUnlock(Request request, Content content) {
        AdminUtils.setReference(request, content);
        request.sendTemplate("admin/dialog/unlock-object.ftl");
    }
}
