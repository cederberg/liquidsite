/*
 * DefaultController.java
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

import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Host;
import net.percederberg.liquidsite.content.Site;
import net.percederberg.liquidsite.content.User;

/**
 * A controller for normal requests.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DefaultController extends Controller {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DefaultController.class);

    /**
     * The admin controller.
     */
    private AdminController admin;

    /**
     * Creates a new default controller. 
     *
     * @param app            the application context
     */
    public DefaultController(Application app) {
        super(app);
        admin = new AdminController(app);
    }

    /**
     * Destroys this request controller. This method frees all
     * internal resources used by this controller.
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
        Domain  domain;
        Site    site;
        User    user;
        String  path = request.getPath();
        boolean access;

        // Find domain & site
        try {
            domain = getDomain(request);
            if (domain == null) {
                return;
            }
            site = getSite(request, domain);
            if (site == null) {
                return;
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        }

        // Find and validate user
        path = path.substring(site.getDirectory().length());
        if (request.getParameter("liquidsite.login") != null) {
            processLogin(request, site, path);
            return;
        }
        user = request.getUser();

        // Process site request
        try {
            access = site.hasReadAccess(user);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        }
        if (access) {
            processAuthorized(request, site, path);
        } else { 
            processUnauthorized(request, site, path);
        }
    }
    
    /**
     * Processes an authorized request.
     *
     * @param request        the request object
     * @param site           the site
     * @param path           the request path
     * 
     * @throws RequestException if the request couldn't be processed
     */
    private void processAuthorized(Request request, Site site, String path)
        throws RequestException {

        if (site.isAdmin()) {
            admin.processAuthorized(request, path);
        } else {
            throw RequestException.RESOURCE_NOT_FOUND;
        }
    }

    /**
     * Processes an unauthorized request.
     *
     * @param request        the request object
     * @param site           the site
     * @param path           the request path
     * 
     * @throws RequestException if the request couldn't be processed
     */
    private void processUnauthorized(Request request, Site site, String path)
        throws RequestException {

        if (site.isAdmin()) {
            admin.processUnauthorized(request, path);
        } else {
            throw RequestException.UNAUTHORIZED;
        }
    }

    /**
     * Processes a login request.
     *
     * @param request        the request object
     * @param site           the site
     * @param path           the request path
     * 
     * @throws RequestException if the request couldn't be processed
     */
    private void processLogin(Request request, Site site, String path) 
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
            request.setUser(null);
            request.setAttribute("error", "Invalid user name or password");
            processUnauthorized(request, site, path);
        }
    }

    /**
     * Returns the application content manager.
     * 
     * @return the application content manager
     */
    private ContentManager getContentManager() {
        return getApplication().getContentManager();
    }

    /**
     * Finds the domain corresponding to a request.
     * 
     * @param request        the request
     * 
     * @return the domain corresponding to the request, or
     *         null if no matching domain was found
     */
    private Domain getDomain(Request request) {
        Host  host = getContentManager().getHost(request.getHost());

        if (host != null) {
            return getContentManager().getDomain(host.getDomainName());
        } else {
            return getContentManager().getRootDomain();
        }
    }

    /**
     * Finds the site corresponding to a request.
     * 
     * @param request        the request
     * @param domain         the request domain
     * 
     * @return the site corresponding to the request, or
     *         null if no matching site was found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly 
     */
    private Site getSite(Request request, Domain domain) 
        throws ContentException {

        Site[]  sites;
        Site    res = null;
        int     max = 0;
        int     match;
        
        sites = getContentManager().getSites(domain);
        for (int i = 0; i < sites.length; i++) {
            match = sites[i].match(request.getProtocol(), 
                                   request.getHost(), 
                                   request.getPort(), 
                                   request.getPath());
            if (match > max) {
                res = sites[i];
                max = match;
            }
        }
        return res;
    }
}
