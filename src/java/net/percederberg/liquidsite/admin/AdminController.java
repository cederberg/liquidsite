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

import java.util.ArrayList;

import net.percederberg.liquidsite.Application;
import net.percederberg.liquidsite.Controller;
import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.RequestException;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.ContentTemplate;
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
    private AdminView view = new AdminView();

    /**
     * The home view helper.
     */
    private HomeView homeView = new HomeView();

    /**
     * The admin form handlers (workflows).
     */
    private ArrayList workflows = new ArrayList();

    /**
     * Creates a new administration controller. 
     *
     * @param app            the application context
     */
    public AdminController(Application app) {
        super(app);
        workflows.add(new HomeEditFormHandler());
        workflows.add(new SiteAddFormHandler());
        workflows.add(new SiteEditFormHandler());
        workflows.add(new PublishDialogHandler());
        workflows.add(new UnpublishDialogHandler());
        workflows.add(new RevertDialogHandler());
        workflows.add(new DeleteDialogHandler());
        workflows.add(new UnlockDialogHandler());
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
            homeView.viewHome(request);
        } else if (path.equals("home.html")) {
            homeView.viewHome(request);
        } else if (path.equals("site.html")) {
            processViewSite(request);
        } else if (path.equals("content.html")) {
            view.pageContent(request);
        } else if (path.equals("users.html")) {
            view.pageUsers(request);
        } else if (path.equals("system.html")) {
            view.pageSystem(request);
        } else if (path.equals("logout.html")) {
            processLogout(request);
        } else if (path.equals("view.html")) {
            processView(request);
        } else if (path.equals("loadsite.js")) {
            processLoadSite(request);
        } else if (path.equals("opensite.js")) {
            processOpenSite(request);
        } else if (path.equals("opentemplate.js")) {
            processOpenTemplate(request);
        } else {
            processWorkflow(request, path);
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
     * Processes a form workflow request. This method will check the
     * specified page for a matching workflow. If no workflow is 
     * found, no processing takes place.
     * 
     * @param request        the request object
     * @param page           the request page
     */
    private void processWorkflow(Request request, String page) {
        AdminFormHandler  formHandler;
        
        for (int i = 0; i < workflows.size(); i++) {
            formHandler = (AdminFormHandler) workflows.get(i);
            if (formHandler.getFormPage().equals(page)) {
                formHandler.process(request);
                return;
            }
        }
    }

    /**
     * Processes a view site request.
     * 
     * @param request        the request object
     * 
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processViewSite(Request request) 
        throws RequestException {

        try {
            view.pageSite(request);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            view.viewError(request, e.getMessage(), "site.html");
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            view.viewError(request, e.getMessage(), "site.html");
        }
    }

    /**
     * Processes the view content object requests.
     * 
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processView(Request request) throws RequestException {
        Content  content;
        String   revision;

        try {
            content = (Content) getRequestReference(request);
            revision = request.getParameter("revision");
            if (revision != null) {
                content = content.getRevision(Integer.parseInt(revision));
            }
            if (content instanceof ContentFile) {
                request.sendFile(((ContentFile) content).getFile());
            } else {
                view.viewError(request, 
                               "Cannot preview this object", 
                               "site.html");
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Processes a JavaScript load site tree item request.
     * 
     * @param request        the request object
     * 
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processLoadSite(Request request) throws RequestException {
        Object  obj;

        try {
            obj = getRequestReference(request);
            view.scriptLoadSite(request, obj);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Processes a JavaScript open site tree item request.
     * 
     * @param request        the request object
     * 
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processOpenSite(Request request) throws RequestException {
        Object  obj;

        try {
            obj = getRequestReference(request);
            view.scriptOpenSite(request, obj);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Processes a JavaScript open template request.
     * 
     * @param request        the request object
     * 
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processOpenTemplate(Request request) throws RequestException {
        int      id;
        Content  content;

        try {
            id = Integer.parseInt(request.getParameter("id", "0"));
            content = getContentManager().getContent(request.getUser(), id);
            if (content instanceof ContentTemplate) {
                view.scriptOpenTemplate(request, 
                                        (ContentTemplate) content);
            } else {
                view.scriptOpenTemplate(request, null);
            }
        } catch (NumberFormatException e) {
            throw RequestException.FORBIDDEN;
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
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

        ContentManager  manager = getContentManager();
        User            user = request.getUser();
        String          type = request.getParameter("type");
        String          id = request.getParameter("id");

        if (type == null || id == null) {
            return null;
        } else if (type.equals("domain")) {
            return manager.getDomain(user, id);
        } else {
            return manager.getContent(user, Integer.parseInt(id));
        }
    }
}
