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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package org.liquidsite.app.admin;

import java.io.File;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.liquidsite.app.servlet.Application;
import org.liquidsite.app.servlet.Configuration;
import org.liquidsite.app.template.Template;
import org.liquidsite.app.template.TemplateException;
import org.liquidsite.app.template.TemplateManager;
import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.PersistentObject;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.Request;
import org.liquidsite.util.log.Log;

/**
 * A class containing general utility methods for the administration
 * application.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class AdminUtils {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(AdminUtils.class);

    /**
     * The application used.
     */
    private static Application application = null;

    /**
     * The content manager used for administration.
     */
    private static ContentManager manager = null;

    /**
     * Returns the application currently used
     *
     * @return the application currently used
     */
    public static Application getApplication() {
        return application;
    }

    /**
     * Sets the application currently used.
     *
     * @param application    the application to use
     */
    static void setApplication(Application application) {
        AdminUtils.application = application;
    }

    /**
     * Returns the content manager for administration.
     *
     * @return the content manager for administration
     *
     * @throws ContentException if no content manager exists
     */
    public static ContentManager getContentManager()
        throws ContentException {

        if (manager == null) {
            throw new ContentException("no content manager set");
        }
        return manager;
    }

    /**
     * Sets the content manager for administration.
     *
     * @param manager        the content manager for administration
     */
    static void setContentManager(ContentManager manager) {
        AdminUtils.manager = manager;
    }

    /**
     * Returns the backup directory for the system.
     *
     * @return the backup directory, or
     *         null if not readable
     */
    public static File getBackupDir() {
        Configuration  config;
        String         basedir;
        File           dir;

        config = getApplication().getConfig();
        basedir = config.get(Configuration.FILE_DIRECTORY, null);
        if (basedir == null) {
            LOG.error("application base file directory not configured");
            return null;
        }
        dir = new File(basedir, "backup");
        try {
            if (!dir.exists() && !dir.mkdirs()) {
                LOG.error("couldn't create backup directory: " + dir);
                return null;
            }
        } catch (SecurityException e) {
            LOG.error("access denied while creating backup directory: " +
                      dir);
            return null;
        }
        return dir;
    }

    /**
     * Returns the statistics directory for a domain.
     *
     * @param domain         the domain object
     *
     * @return the statistics directory, or
     *         null if not configured or readable
     */
    public static File getStatisticsDir(Domain domain) {
        Configuration  config;
        String         dir;
        File           file;

        config = getApplication().getConfig();
        dir = config.get(Configuration.STATS_DIRECTORY, null);
        if (dir == null) {
            return null;
        }
        file = new File(dir, domain.getName());
        if (file.exists() && file.canRead()) {
            return file;
        } else {
            return null;
        }
    }

    /**
     * Returns a date format for the specified user.
     *
     * @param user           the user to create a date format for
     *
     * @return a date format for the specified user
     */
    public static SimpleDateFormat getDateFormat(User user) {
        SimpleDateFormat  df;

        df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        df.setCalendar(Calendar.getInstance(user.getTimeZone()));
        return df;
    }

    /**
     * Formats a date to a printable string. The user timezone will
     * be used.
     *
     * @param user           the current user
     * @param date           the date to format
     *
     * @return a printable string with the date
     */
    public static String formatDate(User user, Date date) {
        if (date == null) {
            return "";
        } else {
            return getDateFormat(user).format(date);
        }
    }

    /**
     * Parses a date string into a date. The user timezone will be
     * used.
     *
     * @param user           the current user
     * @param str            the string containing the date
     *
     * @return the date found
     *
     * @throws ParseException if the string didn't contain a valid
     *             date
     */
    public static Date parseDate(User user, String str)
        throws ParseException {

        Date  date;

        date = getDateFormat(user).parse(str);
        if (str.equals(formatDate(user, date))) {
            return date;
        } else {
            throw new ParseException("invalid date: " + str, 0);
        }
    }

    /**
     * Formats a file size to a printable string.
     *
     * @param size           the file size to format
     *
     * @return a printable string with the file size
     */
    public static String formatFileSize(long size) {
        float   value = size;
        String  unit;

        if (size > 1000000) {
            value /= 1000000;
            unit = " MB";
        } else if (size > 2000) {
            value /= 1000;
            unit = " kB";
        } else {
            unit = " bytes";
        }
        value = Math.round(value * 10) / 10.0f;
        if (value == (int) value) {
            return (int) value + unit;
        } else {
            return value + unit;
        }
    }

    /**
     * Returns the JavaScript representation of a date. The user
     * timezone will be used.
     *
     * @param user           the user
     * @param date           the date to present, or null
     *
     * @return a JavaScript representation of the date
     */
    public static String getScriptDate(User user, Date date) {
        if (date == null) {
            return "null";
        } else {
            return getScriptString(formatDate(user, date));
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
     * Returns the XML representation of a string. This method will
     * escape any XML reserved characters.
     *
     * @param str            the string to transform
     *
     * @return an XML representation of the string
     */
    public static String getXmlString(String str) {
        StringBuffer  buffer = new StringBuffer();

        if (str == null) {
            return "";
        } else {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == '<') {
                    buffer.append("&lt;");
                } else if (str.charAt(i) == '>') {
                    buffer.append("&gt;");
                } else if (str.charAt(i) == '&') {
                    buffer.append("&amp;");
                } else if (str.charAt(i) == '"') {
                    buffer.append("&quot;");
                } else {
                    buffer.append(str.charAt(i));
                }
            }
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
            case Content.TRANSLATOR_CATEGORY:
                return "translator";
            case Content.FOLDER_CATEGORY:
                return "folder";
            case Content.PAGE_CATEGORY:
                return "page";
            case Content.FILE_CATEGORY:
                return "file";
            case Content.TEMPLATE_CATEGORY:
                return "template";
            case Content.SECTION_CATEGORY:
                return "section";
            case Content.DOCUMENT_CATEGORY:
                return "document";
            case Content.FORUM_CATEGORY:
                return "forum";
            case Content.TOPIC_CATEGORY:
                return "topic";
            case Content.POST_CATEGORY:
                return "post";
            default:
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Checks is a specified object is online. This method will check
     * that all parent content objects are also online. For domain
     * objects or null, this method always returns true.
     *
     * @param obj             the domain or content object
     *
     * @return true if the object and all parents are online, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public static boolean isOnline(Object obj)
        throws ContentException {

        Content  content;

        if (obj instanceof Content) {
            content = (Content) obj;
            return content.isOnline() && isOnline(content.getParent());
        } else {
            return true;
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
    public static PersistentObject getReference(Request request)
        throws ContentException, ContentSecurityException {

        User     user = request.getUser();
        String   type = request.getParameter("type");
        String   id = request.getParameter("id");
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
            return getContentManager().getContent(user, value);
        }
    }

    /**
     * Sets the domain or content reference attributes in a request.
     *
     * @param request        the request
     * @param obj            the domain or content object
     */
    public static void setReference(Request request, PersistentObject obj) {
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

    /**
     * Processes a request to a template file. The template file name
     * is relative to the web context directory, and the output MIME
     * type will always be set to "text/html".
     *
     * @param request        the request object
     * @param templateName   the template file name
     */
    public static void sendTemplate(Request request, String templateName) {
        Template      template;
        StringWriter  buffer = new StringWriter();

        try {
            template = TemplateManager.getFileTemplate(templateName);
            template.process(request, getContentManager(), buffer);
            request.sendData("text/html", buffer.toString());
        } catch (ContentException e) {
            request.sendData("text/plain", "Error: " + e.getMessage());
        } catch (TemplateException e) {
            request.sendData("text/plain", "Error: " + e.getMessage());
        }
    }
}
