/*
 * SystemRequestProcessor.java
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

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.RequestException;
import net.percederberg.liquidsite.admin.view.AdminView;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.web.Request;

/**
 * The request processor for the system view in the administration
 * site(s).
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class SystemRequestProcessor {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(SystemRequestProcessor.class);

    /**
     * Creates a new administration request processor.
     */
    public SystemRequestProcessor() {
    }

    /**
     * Processes a request.
     *
     * @param request        the request object
     * @param path           the request path
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    public void process(Request request, String path)
        throws RequestException {

        User  user = request.getUser();

        if (user != null && user.isSuperUser()) {
            processAuthorized(request, path);
        } else {
            processUnauthorized(request, path);
        }
    }

    /**
     * Processes an authorized request. This is a request from a user
     * with permissions to access the system view.
     *
     * @param request        the request object
     * @param path           the request path
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processAuthorized(Request request, String path)
        throws RequestException {

        String  action = request.getParameter("action", "");

        if (action.equals("restart")) {
            handleRestart(request);
        } else if (action.equals("backup")) {
            handleBackup(request);
        } else if (action.equals("restore")) {
            handleRestore(request);
        } else {
            AdminView.SYSTEM.viewSystem(request);
        }
    }

    /**
     * Processes an unauthorized request. This is a request from a
     * user without permissions to access the system view.
     *
     * @param request        the request object
     * @param path           the request path
     */
    private void processUnauthorized(Request request, String path) {
        LOG.warning("unauthorized access admin system view by user " +
                    request.getUser());
        AdminView.BASE.viewError(request,
                                 "Access denied for the current user.",
                                 "index.html");
    }

    /**
     * Handles the system restart requests.
     *
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void handleRestart(Request request) throws RequestException {
        try {
            AdminUtils.getContentManager().getApplication().restart();
            AdminView.BASE.viewInfo(request,
                                    "System restarted successfully",
                                    "system.html");
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        }
    }

    /**
     * Handles the system backup requests.
     *
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void handleBackup(Request request) throws RequestException {
        try {
            if (!validateBackup(request)) {
                AdminView.SYSTEM.viewBackup(request);
            } else {
                AdminView.BASE.viewInfo(request,
                                        "Not implemented yet",
                                        "system.html");
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        }
    }

    /**
     * Handles the system restore requests.
     *
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void handleRestore(Request request) throws RequestException {
        try {
            if (!validateRestore(request)) {
                AdminView.SYSTEM.viewRestore(request);
            } else {
                AdminView.BASE.viewInfo(request,
                                        "Not implemented yet",
                                        "system.html");
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        }
    }

    /**
     * Validates the backup form.
     *
     * @param request        the request object
     *
     * @return true if the form parameters validated correctly, or
     *         false otherwise
     */
    private boolean validateBackup(Request request) {
        String  message;

        if (request.getParameter("domain") == null) {
            return false;
        }
        if (request.getParameter("domain", "").equals("")) {
            message = "Please select a domain to backup";
            request.setAttribute("error", message);
            return false;
        }
        return true;
    }

    /**
     * Validates the restore form.
     *
     * @param request        the request object
     *
     * @return true if the form parameters validated correctly, or
     *         false otherwise
     */
    private boolean validateRestore(Request request) {
        String  message;
        String  name;

        if (request.getParameter("backup") == null) {
            return false;
        }
        if (request.getParameter("backup", "").equals("")) {
            message = "Please select a backup file to restore";
            request.setAttribute("error", message);
            return false;
        }
        name = request.getParameter("domain", "");
        if (name.equals("")) {
            message = "Please enter the name of a domain to create";
            request.setAttribute("error", message);
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            if (AdminFormHandler.DOMAIN_CHARS.indexOf(name.charAt(i)) < 0) {
                message = "invalid character in domain name: '" +
                           name.charAt(i) + "'";
                request.setAttribute("error", message);
                return false;
            }
        }
        return true;
    }
}
