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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.admin;

import java.util.Date;

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.Domain;

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
    public DialogView() {
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
     * @param obj            the domain or content object to delete
     */
    public void viewDelete(Request request, Object obj) {
        String  name;

        setReference(request, obj);
        if (obj instanceof Domain) {
            name = ((Domain) obj).getName();
        } else {
            name = ((Content) obj).getName();
        }
        request.setAttribute("name", name);
        request.sendTemplate("admin/dialog/delete-object.ftl");
    }

    /**
     * Shows the content publish dialog.
     * 
     * @param request        the request object
     * @param content        the content object to publish
     */
    public void viewPublish(Request request, Content content) {
        String dateStr = request.getParameter("date");
        String comment = request.getParameter("comment");

        if (dateStr == null) {
            dateStr = formatDate(new Date());
        }
        if (comment == null) {
            comment = content.getComment();
        }
        setReference(request, content);
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
        String dateStr = request.getParameter("date");
        String comment = request.getParameter("comment");

        if (dateStr == null) {
            dateStr = formatDate(content.getOfflineDate());
        }
        if (dateStr.equals("")) {
            dateStr = formatDate(new Date());
        }
        if (comment == null) {
            comment = "Unpublished";
        }
        setReference(request, content);
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

        setReference(request, content);
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
        setReference(request, content);
        request.sendTemplate("admin/dialog/unlock-object.ftl");
    }

    /**
     * Formats a date for form input.
     * 
     * @param date           the date to format
     * 
     * @return a formatted date string
     */
    private String formatDate(Date date) {
        if (date == null) {
            return "";
        } else {
            return DATE_FORMAT.format(date);
        }
    }
}
