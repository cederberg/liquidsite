/*
 * RequestSession.java
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

package net.percederberg.liquidsite.web;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import net.percederberg.liquidsite.content.User;

/**
 * An HTTP session.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class RequestSession {

    /**
     * The session counter attribute name.
     */
    private static final String COUNTER_ATTRIBUTE = "counter";

    /**
     * The session files attribute name.
     */
    private static final String FILES_ATTRIBUTE = "files";

    /**
     * The session user attribute name.
     */
    private static final String USER_ATTRIBUTE = "user";

    /**
     * The number of currently active sessions.
     */
    static int activeSessions = 0;

    /**
     * The HTTP session to encapsulate.
     */
    private HttpSession session;

    /**
     * Returns the number of currently active sessions.
     *
     * @return the number of active sessions
     */
    public static int getActiveCount() {
        return activeSessions;
    }

    /**
     * Creates a new request session.
     *
     * @param session         the request session
     */
    public RequestSession(HttpSession session) {
        this.session = session;
        if (getAttribute(COUNTER_ATTRIBUTE) == null) {
            setAttribute(COUNTER_ATTRIBUTE, SessionCounter.getInstance());
        }
    }

    /**
     * Returns the value of a session attribute.
     *
     * @param name           the attribute name
     *
     * @return the attribute value, or
     *         null if no such attribute was found
     */
    public Object getAttribute(String name) {
        return getAttribute(name, null);
    }

    /**
     * Returns the value of a session attribute. If the specified
     * attribute does not exist, a default value will be returned.
     *
     * @param name           the attribute name
     * @param defVal         the default attribute value
     *
     * @return the attribute value, or
     *         the default value if no such attribute was found
     */
    public Object getAttribute(String name, Object defVal) {
        if (session.getAttribute(name) != null) {
            return session.getAttribute(name);
        } else {
            return defVal;
        }
    }

    /**
     * Sets a session attribute value.
     *
     * @param name           the attribute name
     * @param value          the attribute value, or null to remove
     */
    public void setAttribute(String name, Object value) {
        if (value == null) {
            session.removeAttribute(name);
        } else {
            session.setAttribute(name, value);
        }
    }

    /**
     * Returns a session file with a specified name.
     *
     * @param name           the file name
     *
     * @return the session file found, or
     *         null if no such file exists in the session
     */
    public File getFile(String name) {
        SessionFiles  files = (SessionFiles) getAttribute(FILES_ATTRIBUTE);

        return (files == null) ? null : files.getFile(name);
    }

    /**
     * Returns a map of all session files. The files are indexed by
     * their file names.
     *
     * @return a map of all session files
     */
    public Map getAllFiles() {
        SessionFiles  files = (SessionFiles) getAttribute(FILES_ATTRIBUTE);

        return (files == null) ? new HashMap() : files.getAllFiles();
    }

    /**
     * Adds a file to the session. After this point, the file will be
     * managed by the session, meaning that the session is responsable
     * for deleting the file once it is no longer accessible.
     *
     * @param name           the file name
     * @param file           the file to add
     */
    public void addFile(String name, File file) {
        SessionFiles  files = (SessionFiles) getAttribute(FILES_ATTRIBUTE);

        if (files == null) {
            files = new SessionFiles();
            setAttribute(FILES_ATTRIBUTE, files);
        }
        files.addFile(name, file);
    }

    /**
     * Removes a file from the session.
     *
     * @param name           the file name
     */
    public void removeFile(String name) {
        SessionFiles  files = (SessionFiles) getAttribute(FILES_ATTRIBUTE);

        if (files != null) {
            files.removeFile(name);
        }
    }

    /**
     * Removes all files from the session.
     */
    public void removeAllFiles() {
        SessionFiles  files = (SessionFiles) getAttribute(FILES_ATTRIBUTE);

        if (files != null) {
            files.removeAllFiles();
        }
    }

    /**
     * Returns the session user. The session user is null until it is
     * set by the setUser() method. Normally the session user is not
     * set until the user has been authenticated.
     *
     * @return the session user, or
     *         null if no user has been set
     *
     * @see #setUser
     */
    public User getUser() {
        return (User) getAttribute(USER_ATTRIBUTE);
    }

    /**
     * Sets the session user. The session user is null until it is
     * set by this method. Normally the session user is not set until
     * the user has been authenticated.
     *
     * @param user           the new session user
     *
     * @see #getUser
     */
    public void setUser(User user) {
        setAttribute(USER_ATTRIBUTE, user);
    }

    /**
     * Invalidates this session. This method will remove any files
     * still in the session. After calling this method, no other
     * methods in this class should be called, as their results would
     * be unpredictable.
     */
    public void invalidate() {
        session.invalidate();
    }


    /**
     * The session counter. This class counts session binding and
     * unbinding events in order to update the number of active
     * sessions.
     */
    private static class SessionCounter
        implements HttpSessionBindingListener {

        /**
         * The single instance of this class.
         */
        private static SessionCounter instance = new SessionCounter();

        /**
         * Returns the session counter instance.
         */
        public static SessionCounter getInstance() {
            return instance;
        }

        /**
         * Creates a new session counter.
         */
        private SessionCounter() {
        }

        /**
         * Notifies the manager that it is being bound to a session.
         *
         * @param event          the binding event
         */
        public void valueBound(HttpSessionBindingEvent event) {
            activeSessions++;
        }

        /**
         * Notifies the manager that it is being unbound from a
         * session.
         *
         * @param event          the unbinding event
         */
        public void valueUnbound(HttpSessionBindingEvent event) {
            activeSessions--;
        }
    }


    /**
     * The session file manager. This class handles all the files
     * stored in the session. Once this object is removed from the
     * session, or the session is invalidated, the files will be
     * deleted.
     */
    private class SessionFiles implements HttpSessionBindingListener {

        /**
         * The map with all files. The file names are mapped to the
         * actual File objects.
         */
        private HashMap files = new HashMap();

        /**
         * Creates a new session file manager.
         */
        public SessionFiles() {
        }

        /**
         * Returns a file with a specified name.
         *
         * @param name           the file name
         *
         * @return the file found, or
         *         null if no such file exists
         */
        public File getFile(String name) {
            return (File) files.get(name);
        }

        /**
         * Returns a map of all files. The files are indexed by their
         * file names.
         *
         * @return a map of all files
         */
        public Map getAllFiles() {
            return new HashMap(files);
        }

        /**
         * Adds a file. After this point, the file will be managed by
         * this object, meaning that it will be deleted once it is no
         * longer accessible from this object.
         *
         * @param name           the file name
         * @param file           the file to add
         */
        public void addFile(String name, File file) {
            files.put(name, file);
        }

        /**
         * Removes a file.
         *
         * @param name           the file name
         */
        public void removeFile(String name) {
            File  file;

            file = (File) files.get(name);
            if (file != null) {
                files.remove(name);
                file.delete();
            }
        }

        /**
         * Removes all files.
         */
        public void removeAllFiles() {
            Iterator  iter;
            File      file;

            iter = files.values().iterator();
            while (iter.hasNext()) {
                file = (File) iter.next();
                file.delete();
            }
            files.clear();
        }

        /**
         * Notifies the manager that it is being bound to a session.
         *
         * @param event          the binding event
         */
        public void valueBound(HttpSessionBindingEvent event) {
        }

        /**
         * Notifies the manager that it is being unbound from a
         * session.
         *
         * @param event          the unbinding event
         */
        public void valueUnbound(HttpSessionBindingEvent event) {
            removeAllFiles();
        }
    }
}
