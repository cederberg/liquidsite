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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite;

import java.io.StringWriter;

import net.percederberg.liquidsite.admin.AdminRequestProcessor;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;
import net.percederberg.liquidsite.content.ContentFolder;
import net.percederberg.liquidsite.content.ContentPage;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.ContentSite;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.template.Template;
import net.percederberg.liquidsite.template.TemplateException;
import net.percederberg.liquidsite.template.TemplateManager;

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
     * Creates a new default request processor. 
     *
     * @param app            the application context
     */
    public DefaultRequestProcessor(Application app) {
        super(app.getContentManager(), app.getBaseDir());
        admin = new AdminRequestProcessor(app);
    }

    /**
     * Destroys this request processor. This method frees all
     * internal resources used by this processor.
     */
    public void destroy() {
        admin.destroy();
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
        Content      page;
        boolean      access;

        // Find domain & site
        try {
            site = getContentManager().findSite(request.getProtocol(), 
                                                request.getHost(), 
                                                request.getPort(), 
                                                request.getPath());
            request.setSite(site);
            if (site == null) {
                return;
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        }

        // Find and validate user
        if (request.getParameter("liquidsite.login") != null) {
            processLogin(request, site);
            if (request.hasResponse()) {
                return;
            }
        } else if (request.getParameter("liquidsite.logout") != null) {
            processLogout(request);
            return;
        }
        user = request.getUser();

        // Find page and create response
        path = path.substring(site.getDirectory().length());
        try {
            if (site.isAdmin()) {
                access = site.hasReadAccess(user);
                if (access && user != null) {
                    admin.processAuthorized(request, path);
                } else {
                    admin.processUnauthorized(request, path);
                }
            } else {
                page = findPage(user, site, path);
                // TODO: check for folder paths not ending with /
                if (page instanceof ContentSite
                 || page instanceof ContentFolder) {

                    page = findIndexPage(user, page);
                }
                if (page == null) {
                    throw RequestException.RESOURCE_NOT_FOUND;
                }
                processAuthorized(request, page);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            LOG.debug(e.getMessage());
            throw RequestException.FORBIDDEN;
        }
    }
    
    /**
     * Processes an authorized request.
     *
     * @param request        the request object
     * @param page           the page requested
     * 
     * @throws RequestException if the request couldn't be processed
     */
    private void processAuthorized(Request request, Content page)
        throws RequestException {

        if (page instanceof ContentPage) {
            processContentPage(request, (ContentPage) page);
        } else if (page instanceof ContentFile) {
            try {
                request.sendFile(((ContentFile) page).getFile());
            } catch (ContentException e) {
                LOG.error("couldn't find content file for " + 
                          page.getId() + ": " + e.getMessage());
                throw RequestException.INTERNAL_ERROR;
            }
        } else {
            LOG.error("unrecognized page content category " + 
                      page.getCategory());
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
     * @param site           the site
     * 
     * @throws RequestException if the request couldn't be processed
     */
    private void processLogin(Request request, ContentSite site)
        throws RequestException {

        String  name = request.getParameter("liquidsite.login");
        String  password = request.getParameter("liquidsite.password");
        User    user;
        
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

    /**
     * Processes a request to a content page.
     *
     * @param request        the request object
     * @param page           the page requested
     * 
     * @throws RequestException if the request couldn't be processed
     */
    private void processContentPage(Request request, ContentPage page) 
        throws RequestException {

        User          user = request.getUser();
        Template      template;
        StringWriter  buffer = new StringWriter();

        try {
            template = TemplateManager.getPageTemplate(user, page);
            template.process(request, getContentManager(), buffer);
            request.sendData("text/html", buffer.toString());
        } catch (TemplateException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        }
    }
}
