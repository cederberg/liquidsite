/*
 * AdminController.java
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

import net.percederberg.liquidsite.Application;
import net.percederberg.liquidsite.Controller;
import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.RequestException;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Host;
import net.percederberg.liquidsite.content.Site;
import net.percederberg.liquidsite.content.User;

/**
 * A controller for requests to the administration site(s).
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class AdminController extends Controller {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(AdminController.class);

    /**
     * The admin view helper.
     */
    private AdminView view;

    /**
     * The admin form validator.
     */
    private AdminValidator validator = new AdminValidator();

    /**
     * Creates a new administration controller. 
     *
     * @param app            the application context
     */
    public AdminController(Application app) {
        super(app);
        view = new AdminView(app.getContentManager());
    }

    /**
     * Destroys this request controller. This method frees all
     * internal resources used by this controller.
     */
    public void destroy() {
    }

    /**
     * Processes a request.
     *
     * @param request        the request object
     */
    public void process(Request request) {
    }
    
    /**
     * Processes an authorized request. This is a request from a user
     * with permissions to access the admin site.
     *
     * @param request        the request object
     * @param path           the request path
     * 
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    public void processAuthorized(Request request, String path) 
        throws RequestException {

        if (path.equals("style.css")) {
            request.sendFile(getFile(path));
        } else if (path.startsWith("images/")) {
            request.sendFile(getFile(path));
        } else if (path.startsWith("script/")) {
            request.sendFile(getFile(path));
        } else if (path.equals("") || path.equals("index.html")) {
            view.pageHome(request);
        } else if (path.equals("home.html")) {
            view.pageHome(request);
        } else if (path.equals("site.html")) {
            view.pageSite(request);
        } else if (path.equals("content.html")) {
            view.pageContent(request);
        } else if (path.equals("users.html")) {
            view.pageUsers(request);
        } else if (path.equals("system.html")) {
            view.pageSystem(request);
        } else if (path.equals("logout.html")) {
            processLogout(request);
        } else if (path.equals("add-site.html")) {
            processAddObject(request);
        } else if (path.equals("delete-site.html")) {
            processDeleteObject(request);
        } else if (path.equals("loadsite.js")) {
            view.scriptLoadSite(request);
        } else if (path.equals("opensite.js")) {
            view.scriptOpenSite(request);
        }
    }

    /**
     * Processes an unauthorized request. This is a request from a 
     * user without permissions to access the admin site.
     *
     * @param request        the request object
     * @param path           the request path
     */
    public void processUnauthorized(Request request, String path) {
        if (path.equals("style.css")) {
            request.sendFile(getFile(path));
        } else if (path.startsWith("images/")) {
            request.sendFile(getFile(path));
        } else if (path.endsWith(".js")) {
            request.sendData("text/javascript", 
                             "window.location.reload(1);\n");
        } else {
            if (request.getUser() != null) {
                request.setAttribute("error", 
                                     "Access denied for your current user."); 
            }
            request.sendTemplate("admin/login.ftl");
        }
    }
    
    /**
     * Processes a logout request.
     * 
     * @param request        the request object
     */
    private void processLogout(Request request) {
        request.setUser(null);
        request.sendRedirect("index.html");
    }

    /**
     * Processes the add object requests for the site view.
     * 
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processAddObject(Request request) throws RequestException {
        String  step = request.getParameter("step", "");
        String  category = request.getParameter("category", "");
        Object  parent;
        
        try {
            parent = getRequestReference(request);
            if (request.getParameter("prev") != null) {
                if (step.equals("1")) {
                    request.sendRedirect("site.html");
                } else {
                    view.pageAddObject(request, parent);
                }
            } else if (category.equals("domain")) {
                if (step.equals("1")) {
                    view.pageAddDomain(request, parent);
                } else {
                    processAddDomain(request, parent);
                }
            } else if (category.equals("site")) {
                if (step.equals("1")) {
                    view.pageAddSite(request, parent);
                } else {
                    processAddSite(request, parent);
                }
            } else {
                view.pageAddObject(request, parent);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            // TODO: show nice error page instead
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            // TODO: show nice error page instead
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Processes the add domain requests for the site view. The 
     * parent domain object must be specified in case the uses 
     * chooses to go back to the preceeding step. 
     * 
     * @param request        the request object
     * @param parent         the parent domain object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void processAddDomain(Request request, Object parent) 
        throws ContentException, ContentSecurityException {

        Domain  domain;
        Host    host;
        
        try {
            validator.validateAddDomain(request);
            domain = new Domain(request.getParameter("name"));
            domain.setDescription(request.getParameter("description"));
            domain.save(request.getUser());
            host = new Host(domain, request.getParameter("host"));
            host.setDescription("Default domain host");
            host.save(request.getUser());
            view.setSiteTreeFocus(request, domain);
            request.sendRedirect("site.html");
        } catch (FormException e) {
            request.setAttribute("error", e.getMessage());
            view.pageAddDomain(request, parent);
        }
    }

    /**
     * Processes the add site requests for the site view.
     * 
     * @param request        the request object
     * @param parent         the parent domain object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void processAddSite(Request request, Object parent) 
        throws ContentException, ContentSecurityException {

        User    user = request.getUser();
        Domain  domain;
        Site    site;
        
        try {
            validator.validateAddSite(request);
            domain = (Domain) parent;
            site = new Site(domain);
            site.setName(request.getParameter("name"));
            site.setProtocol(request.getParameter("protocol"));
            site.setHost(request.getParameter("host"));
            site.setPort(Integer.parseInt(request.getParameter("port")));
            site.setDirectory(request.getParameter("dir"));
            site.setAdmin(request.getParameter("admin") != null);
            site.setComment("Created");
            site.save(user);
            view.setSiteTreeFocus(request, site);
            request.sendRedirect("site.html");
        } catch (FormException e) {
            request.setAttribute("error", e.getMessage());
            view.pageAddSite(request, parent);
        }
    }

    /**
     * Processes the delete object requests for the site view.
     * 
     * @param request        the request object
     */
    private void processDeleteObject(Request request) {
        Object  focus = view.getSiteTreeFocus(request);

        // TODO: remove the use of site tree focus, as it is unsafe
        if (focus instanceof Domain) {
            if (request.getParameter("confirmed") == null) {
                view.dialogDeleteDomain(request);
            } else {
                processDeleteDomain(request);
            }
        } else if (focus instanceof Site) {
            if (request.getParameter("confirmed") == null) {
                view.dialogDeleteSite(request);
            } else {
                processDeleteContent(request);
            }
        }
    }

    /**
     * Processes the confirmed delete domain requests for the site 
     * view.
     * 
     * @param request        the request object
     */
    private void processDeleteDomain(Request request) {
        Domain  domain;
        String  error;
        
        try {
            // TODO: check for request domain!
            // TODO: change line below, as it is unsafe!
            domain = (Domain) view.getSiteTreeFocus(request);
            domain.delete(request.getUser());
            view.setSiteTreeFocus(request, null);
            view.dialogClose(request);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            error = "Failed to write to database, " + e.getMessage();
            request.setAttribute("error", error);
            view.dialogError(request);
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            error = "You don't have permission for removing domain";
            request.setAttribute("error", error);
            view.dialogError(request);
        }
    }

    /**
     * Processes the confirmed delete content requests for the site 
     * view.
     * 
     * @param request        the request object
     */
    private void processDeleteContent(Request request) {
        Content content;
        String  error;
        
        try {
            // TODO: check for request site!
            // TODO: change line below, as it is unsafe!
            content = (Content) view.getSiteTreeFocus(request);
            content.delete(request.getUser());
            view.setSiteTreeFocus(request, content.getParent());
            view.dialogClose(request);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            error = "Failed to write to database, " + e.getMessage();
            request.setAttribute("error", error);
            view.dialogError(request);
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            error = "You don't have permission for removing this object";
            request.setAttribute("error", error);
            view.dialogError(request);
        }
    }
    
    /**
     * Returns the domain or content referenced by in a request.
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
    private Object getRequestReference(Request request) 
        throws ContentException, ContentSecurityException {

        User    user = request.getUser();
        String  type = request.getParameter("type");
        String  id = request.getParameter("id");
        
        if (type == null || id == null) {
            return null;
        } else if (type.equals("domain")) {
            return getContentManager().getDomain(user, id);
        } else {
            return getContentManager().getContent(user, Integer.parseInt(id));
        }
    }
}
