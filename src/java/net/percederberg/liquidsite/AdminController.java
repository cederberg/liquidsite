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

package net.percederberg.liquidsite;

import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.Domain;
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
        if (path.equals("style.css") || path.startsWith("images/")) {
            request.sendFile(getFile(path));
        } else {
            request.forward("/admin/login.jsp");
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
     * Displays the home page.
     *
     * @param request        the request object
     */
    private void displayHome(Request request) {
        request.forward("/admin/home.jsp");
    }

    /**
     * Displays the site page.
     *
     * @param request        the request object
     */
    private void displaySite(Request request) {
        request.setAttribute("initialize", getSiteInitializeScript());
        request.forward("/admin/site.jsp");
    }

    /**
     * Displays the content page.
     *
     * @param request        the request object
     */
    private void displayContent(Request request) {
        request.forward("/admin/content.jsp");
    }

    /**
     * Displays the users page.
     *
     * @param request        the request object
     */
    private void displayUsers(Request request) {
        request.forward("/admin/users.jsp");
    }

    /**
     * Displays the system page.
     *
     * @param request        the request object
     */
    private void displaySystem(Request request) {
        request.forward("/admin/system.jsp");
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
        StringBuffer  buffer = new StringBuffer();
        String        type = request.getParameter("type", "");
        String        id = request.getParameter("id", "0");

        buffer.append("treeAddContainer('");
        buffer.append(id);
        buffer.append("');\n");
// TODO:        treeAddItem('SAK', 1, "site", "www.sak.se", "www.sak.se/", 1);
// TODO:        treeAddItem('SAK', 11, "site", "Intranet", "www.sak.se/intranet/", 1);
        buffer.append("treeOpen('");
        buffer.append(type);
        buffer.append("', '");
        buffer.append(id);
        buffer.append("');\n");
        request.sendData("text/javascript", buffer.toString());
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
        StringBuffer  buffer = new StringBuffer();
        String        type = request.getParameter("type", "");
        String        id = request.getParameter("id", "0");
        Domain        domain = null;
        Content       content = null;

        buffer.append("objectShow('");
        buffer.append(type);
        buffer.append("', '");
        buffer.append(id);
        buffer.append("', '");
        if (type.equals("domain")) {
            domain = getDomain(request.getUser(), id);
            buffer.append(domain.getName());
        } else {
            content = getContent(request.getUser(), Integer.parseInt(id));
            buffer.append(content.getName());
        }
        buffer.append("');\n");
        request.sendData("text/javascript", buffer.toString());
    }

    /**
     * Returns the JavaScript for initializing the site view.
     * 
     * @return the JavaScript for initializing the site view
     */
    private String getSiteInitializeScript() {
        StringBuffer  buffer = new StringBuffer();
        Domain[]      domains = getContentManager().getDomains();

        // TODO: authorize the user access to the domains
        for (int i = 0; i < domains.length; i++) {
            buffer.append("        treeAddItem(0, '");
            buffer.append(domains[i].getName());
            buffer.append("', 'domain', '");
            buffer.append(domains[i].getName());
            buffer.append("', '");
            buffer.append(domains[i].getDescription());
            buffer.append("', 1);\n");
        }
        return buffer.toString();
    }

    /**
     * Finds a specified domain object. This method also checks that
     * the user has read access to the domain object.
     * 
     * @param user           the user performing the operation
     * @param name           the domain name
     * 
     * @return the domain object, or
     *         null if no domain object was found
     * 
     * @throws RequestException if the user didn't have access to the
     *             domain object
     */
    private Domain getDomain(User user, String name) 
        throws RequestException {

        Domain  domain = getContentManager().getDomain(name);

        if (domain == null || domain.getName().equals(user.getDomainName())) {
            return domain;
        } else {
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Finds a specified content object. This method also checks that
     * the user has read access to the content object.
     * 
     * @param user           the user performing the operation
     * @param id             the content id
     * 
     * @return the content object, or
     *         null if no content object was found
     * 
     * @throws RequestException if the database couldn't be accessed
     *             properly, or if the user didn't have access to the
     *             content object
     */
    private Content getContent(User user, int id) throws RequestException {
        Content  content;

        try {
            content = getContentManager().getContent(id);
            if (content == null || content.hasReadAccess(user)) {
                return content;
            } else {
                throw RequestException.FORBIDDEN;
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        }
    }
}
