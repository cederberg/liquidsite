/*
 * DefaultRequestProcessor.java
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

package net.percederberg.liquidsite;

import net.percederberg.liquidsite.admin.AdminRequestProcessor;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.ContentSite;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.template.TemplateException;
import net.percederberg.liquidsite.web.Request;

/**
 * The default request processor for normal requests.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DefaultRequestProcessor extends RequestProcessor {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DefaultRequestProcessor.class);

    /**
     * The delay in milliseconds on a failed login attempt.
     */
    private static final int FAILED_LOGIN_DELAY = 5000;

    /**
     * The admin request processor.
     */
    private AdminRequestProcessor admin;

    /**
     * The forum request processor.
     */
    private ForumRequestProcessor forum;

    /**
     * Creates a new default request processor.
     *
     * @param app            the application context
     */
    public DefaultRequestProcessor(Application app) {
        super(app.getContentManager(), app.getBaseDir());
        admin = new AdminRequestProcessor(app);
        forum = new ForumRequestProcessor(app);
    }

    /**
     * Destroys this request processor. This method frees all
     * internal resources used by this processor.
     */
    public void destroy() {
        admin.destroy();
        forum.destroy();
    }

    /**
     * Processes a request.
     *
     * @param request        the request object to process
     *
     * @throws RequestException if the request couldn't be processed
     */
    public void process(Request request) throws RequestException {
        String       path = request.getPath();
        User         user;
        ContentSite  site;
        Content      content;

        // Find domain & site
        try {
            site = getContentManager().findSite(request.getProtocol(),
                                                request.getHost(),
                                                request.getPort(),
                                                request.getPath());
            if (site == null) {
                return;
            }
            request.getEnvironment().setDomain(site.getDomain());
            request.getEnvironment().setSite(site);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        }

        // Process request action
        if (request.getParameter("liquidsite.action") != null) {
            processAction(request);
            if (request.hasResponse()) {
                return;
            }
        }

        // Find page and create response
        path = path.substring(site.getDirectory().length());
        try {
            if (site.isAdmin()) {
                user = request.getUser();
                if (user != null && site.hasReadAccess(user)) {
                    admin.processAuthorized(request, path);
                } else {
                    admin.processUnauthorized(request, path);
                }
            } else {
                content = locatePage(request, site, path);
                processContent(request, content);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            LOG.info(e.getMessage());
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Processes a request for a content object.
     *
     * @param request        the request object
     * @param content        the content object
     *
     * @throws RequestException if the request couldn't be processed
     */
    private void processContent(Request request, Content content)
        throws RequestException {

        try {
            sendContent(request, content);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            LOG.info(e.getMessage());
            throw RequestException.FORBIDDEN;
        } catch (TemplateException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        }
    }

    /**
     * Processes a request action. The processing of the action may
     * produce a response to the request object, which must be checked
     * before contining processing after calling this method.
     *
     * @param request        the request object
     *
     * @throws RequestException if the action couldn't be processed
     */
    private void processAction(Request request)
        throws RequestException {

        String  action = request.getParameter("liquidsite.action");

        if (action.equals("login")) {
            processLogin(request);
        } else if (action.equals("logout")) {
            processLogout(request);
        } else if (action.startsWith("forum.")) {
            forum.process(request);
        } else {
            LOG.warning(request + ": request action '" + action +
                        "' is undefined");
            throw RequestException.INTERNAL_ERROR;
        }
    }

    /**
     * Processes a login request. This will set the request user if
     * successful, and send a redirect to the requested page. If
     * unsuccessful, a request error attribute is set. Note that the
     * request must be further processed in the case of an
     * unsuccessful login, as the login page must be displayed again.
     *
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     */
    private void processLogin(Request request)
        throws RequestException {

        ContentSite  site = request.getEnvironment().getSite();
        String       name;
        String       password;
        User         user;

        name = request.getParameter("liquidsite.login", "");
        password = request.getParameter("liquidsite.password", "");
        try {
            user = getContentManager().getUser(site.getDomain(), name);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        }
        if (user != null && user.verifyPassword(password)) {
            request.setUser(user);
            request.sendRedirect(request.getPath());
        } else {
            try {
                Thread.sleep(FAILED_LOGIN_DELAY);
            } catch (InterruptedException ignore) {
                // Do nothing
            }
            request.setUser(null);
            request.setAttribute("error", "Invalid user name or password");
        }
    }

    /**
     * Processes a logout request. This will set the request user to
     * null, which invalidates the user session. The request will be
     * redirected to the requested page.
     *
     * @param request        the request object
     */
    private void processLogout(Request request) {
        request.setUser(null);
        request.sendRedirect(request.getPath());
    }
}
