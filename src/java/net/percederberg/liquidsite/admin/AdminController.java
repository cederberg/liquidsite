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
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Host;
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
     * The admin script helper.
     */
    private AdminScript script = new AdminScript();

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
            displayHome(request);
        } else if (path.equals("home.html")) {
            displayHome(request);
        } else if (path.equals("site.html")) {
            displaySite(request);
        } else if (path.equals("content.html")) {
            displayContent(request);
        } else if (path.equals("users.html")) {
            displayUsers(request);
        } else if (path.equals("system.html")) {
            displaySystem(request);
        } else if (path.equals("logout.html")) {
            processLogout(request);
        } else if (path.equals("loadsite.js")) {
            displayLoadSite(request);
        } else if (path.equals("opensite.js")) {
            displayOpenSite(request);
        } else if (path.equals("add-site.html")) {
            processAddObject(request);
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
        
        // TODO: add support for sites
        if (request.getParameter("prev") != null) {
            if (step.equals("1")) {
                request.sendRedirect("site.html");
            } else {
                displayAddObject(request);
            }
        } else if (category.equals("domain")) {
            if (step.equals("1")) {
                displayAddDomain(request);
            } else {
                processAddDomain(request);
            }
        } else {
            displayAddObject(request);
        }
    }

    /**
     * Processes the add domain requests for the site view.
     * 
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processAddDomain(Request request) throws RequestException {
        Domain  domain;
        Host    host;
        String  error;
        
        try {
            validator.validateAddDomain(request);
            domain = new Domain(request.getParameter("name"));
            domain.setDescription(request.getParameter("description"));
            domain.save(request.getUser());
            host = new Host(domain, request.getParameter("host"));
            host.setDescription("Default domain host");
            host.save(request.getUser());
            request.setSessionAttribute("site.view.type", "domain");
            request.setSessionAttribute("site.view.id", domain.getName());
            request.sendRedirect("site.html");
        } catch (FormException e) {
            request.setAttribute("error", e.getMessage());
            displayAddDomain(request);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            error = "Failed to save to database, " + e.getMessage();
            request.setAttribute("error", error);
            displayAddDomain(request);
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Displays the home page.
     *
     * @param request        the request object
     */
    private void displayHome(Request request) {
        request.sendTemplate("admin/home.ftl");
    }

    /**
     * Displays the site page.
     *
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void displaySite(Request request) throws RequestException {
        Object          type = request.getSessionAttribute("site.view.type");
        Object          id = request.getSessionAttribute("site.view.id");
        ContentManager  cm = getContentManager();
        User            user = request.getUser();
        Domain[]        domains;
        Content         content;
        StringBuffer    buffer = new StringBuffer();
        String          str;
        
        try {
            domains = cm.getDomains(user);
            buffer.append(script.getTreeView(domains));
            if (type != null && !type.equals("domain")) {
                content = cm.getContent(user, Integer.parseInt(id.toString()));
                str = getTreeView(user, 
                                  content.getDomain(), 
                                  content.getParent(),
                                  true);
                buffer.append(str);
            } 
            if (type != null) {
                buffer.append(script.getTreeViewSelect(type, id));
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
        request.setAttribute("initialize", buffer.toString());
        request.sendTemplate("admin/site.ftl");
    }

    /**
     * Displays the content page.
     *
     * @param request        the request object
     */
    private void displayContent(Request request) {
        request.sendTemplate("admin/content.ftl");
    }

    /**
     * Displays the users page.
     *
     * @param request        the request object
     */
    private void displayUsers(Request request) {
        request.sendTemplate("admin/users.ftl");
    }

    /**
     * Displays the system page.
     *
     * @param request        the request object
     */
    private void displaySystem(Request request) {
        request.sendTemplate("admin/system.ftl");
    }

    /**
     * Displays the load site object JavaScript code.
     * 
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void displayLoadSite(Request request) throws RequestException {
        String          type = request.getParameter("type", "");
        String          id = request.getParameter("id", "0");
        ContentManager  cm = getContentManager();
        User            user = request.getUser();
        Domain          domain;
        Content         content;
        String          buffer;

        try {
            if (type.equals("domain")) {
                domain = cm.getDomain(user, id);
                buffer = getTreeView(user, domain, null, false); 
            } else {
                content = cm.getContent(user, Integer.parseInt(id));
                buffer = getTreeView(user, null, content, false); 
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
        request.sendData("text/javascript", buffer);
    }
    
    /**
     * Displays the open site object JavaScript code.
     * 
     * @param request        the request object
     * 
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void displayOpenSite(Request request) throws RequestException {
        String          type = request.getParameter("type", "");
        String          id = request.getParameter("id", "0");
        ContentManager  cm = getContentManager();
        User            user = request.getUser();
        Domain          domain;
        Content         content;
        String          buffer;

        try {
            request.setSessionAttribute("site.view.type", type);
            request.setSessionAttribute("site.view.id", id);
            if (type.equals("domain")) {
                domain = cm.getDomain(user, id);
                buffer = script.getObjectView(user, domain);
            } else {
                content = cm.getContent(user, Integer.parseInt(id));
                buffer = script.getObjectView(user, content);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
        request.sendData("text/javascript", buffer);
    }

    /**
     * Displays the add object page.
     * 
     * @param request        the request object
     * 
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void displayAddObject(Request request) 
        throws RequestException {

        Object          type = request.getSessionAttribute("site.view.type");
        Object          id = request.getSessionAttribute("site.view.id");
        ContentManager  cm = getContentManager();
        User            user = request.getUser();
        Domain          domain;

        try {
            if (type.equals("domain")) {
                domain = cm.getDomain(user, id.toString());
                if (user.getDomainName().equals("")) {
                    request.setAttribute("enableDomain", true);
                }
                if (domain.hasWriteAccess(user)) {
                    request.setAttribute("enableSite", true);
                }
            } else {
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
        request.sendTemplate("admin/add-object.ftl");
    }

    /**
     * Displays the add domain page.
     * 
     * @param request        the request object
     */
    private void displayAddDomain(Request request) {
        request.setAttribute("name", request.getParameter("name", ""));
        request.setAttribute("description", 
                             request.getParameter("description", ""));
        request.setAttribute("host", request.getParameter("host", ""));
        request.sendTemplate("admin/add-domain.ftl");
    }
    
    /**
     * Returns the JavaScript code for displaying child content 
     * objects. This method may create a nested tree view with all 
     * parent objects up to the domain if the recursive flag is set.
     * 
     * @param user           the current user
     * @param domain         the domain object
     * @param content        the content object
     * @param recursive      the recursive flag
     * 
     * @return the JavaScript code for adding the object to the tree
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getTreeView(User user, 
                               Domain domain, 
                               Content content,
                               boolean recursive) 
        throws ContentException {

        ContentManager  cm = getContentManager();
        Content[]       children; 

        if (content == null) {
            children = cm.getSites(user, domain);
            return script.getTreeView(domain, children);
        } else if (recursive) {
            children = cm.getContentChildren(user, content);
            return getTreeView(user, domain, content.getParent(), true) 
                 + script.getTreeView(content, children);
        } else {
            children = cm.getContentChildren(user, content);
            return script.getTreeView(content, children);
        }
    }
}
