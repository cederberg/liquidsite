/*
 * AdminUtils.java
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.User;

/**
 * A class containing general utility methods for the administration
 * application.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class AdminUtils {

    /**
     * The date format used by this class.
     */
    public static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * Returns the content manager currently in use.
     * 
     * @return the content manager currently in use
     * 
     * @throws ContentException if no content manager exists
     */
    public static ContentManager getContentManager() 
        throws ContentException {

        return ContentManager.getInstance();
    }

    /**
     * Formats a date to a printable string.
     * 
     * @param date           the date to format
     * 
     * @return a printable string with the date
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        } else {
            return DATE_FORMAT.format(date);
        }
    }
    
    /**
     * Parses a date string into a date.
     * 
     * @param str            the string containing the date
     * 
     * @return the date found
     * 
     * @throws ParseException if the string didn't contain a valid 
     *             date
     */
    public static Date parseDate(String str) throws ParseException {
        Date  date = DATE_FORMAT.parse(str);
        
        if (str.equals(formatDate(date))) {
            return date;
        } else {
            throw new ParseException("invalid date: " + str, 0);
        }
    }
        
    /**
     * Returns the JavaScript representation of a date.
     * 
     * @param date           the date to present, or null
     * 
     * @return a JavaScript representation of the date
     */
    public static String getScriptDate(Date date) {
        if (date == null) {
            return "null";
        } else {
            return getScriptString(formatDate(date)); 
        }
    }

    /**
     * Returns the JavaScript representation of a string. This method
     * will take care of required character escapes. 
     * 
     * @param str            the string to present, or null
     * 
     * @return a JavaScript representation of the string
     */
    public static String getScriptString(String str) {
        StringBuffer  buffer = new StringBuffer();

        if (str == null) {
            return "null";
        } else {
            buffer.append("'");
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == '\\') {
                    buffer.append("\\\\");
                } else if (str.charAt(i) == '\n') {
                    buffer.append("\\n");
                } else if (str.charAt(i) == '\r') {
                    buffer.append("\\r");
                } else if (str.charAt(i) == '\'') {
                    buffer.append("\\'");
                } else if (str.charAt(i) == '"') {
                    buffer.append("\\\"");
                } else if (str.charAt(i) == '<') {
                    buffer.append("\\<");
                } else if (str.charAt(i) == '>') {
                    buffer.append("\\>");
                } else {
                    buffer.append(str.charAt(i));
                }
            }
            buffer.append("'");
            return buffer.toString();
        }
    }

    /**
     * Returns an object category name. The category name is used in
     * various places to identify incoming or outgoing objects. 
     * 
     * @param obj            the domain or content object
     * 
     * @return the category name, or 
     *         null if unknown
     */
    public static String getCategory(Object obj) {
        if (obj instanceof Domain) {
            return "domain";
        } else if (obj instanceof Content) {
            switch (((Content) obj).getCategory()) {
            case Content.SITE_CATEGORY:
                return "site";
            case Content.FOLDER_CATEGORY:
                return "folder";
            case Content.PAGE_CATEGORY:
                return "page";
            case Content.FILE_CATEGORY:
                return "file";
            case Content.TEMPLATE_CATEGORY:
                return "template";
            default:
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the domain or content referenced by a request. When
     * returning a content object, the latest revision (including 
     * work revisions) will be returned.
     * 
     * @param request        the request
     * 
     * @return the domain or content object referenced, or
     *         null if not found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    public static Object getReference(Request request) 
        throws ContentException, ContentSecurityException {

        User     user = request.getUser();
        String   type = request.getParameter("type");
        String   id = request.getParameter("id");
        Content  content = null;
        Content  revision = null;
        String   message;
        int      value;
        
        if (type == null || id == null) {
            return null;
        } else if (type.equals("domain")) {
            return getContentManager().getDomain(user, id);
        } else {
            try {
                value = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                message = "invalid content id: " + id;
                throw new ContentSecurityException(message);
            }
            content = getContentManager().getContent(user, value);
            if (content != null) {
                revision = content.getRevision(0);
            }
            return (revision == null) ? content : revision;
        }
    }
    
    /**
     * Sets the domain or content reference attributes in a request.
     * 
     * @param request        the request
     * @param obj            the domain or content object
     */
    public static void setReference(Request request, Object obj) {
        Content  content;

        if (obj instanceof Domain) {
            request.setAttribute("type", "domain");
            request.setAttribute("id", ((Domain) obj).getName());
        } else {
            content = (Content) obj;
            request.setAttribute("type", getCategory(content));
            request.setAttribute("id", 
                                 String.valueOf(content.getId()));
        }
    }
}
