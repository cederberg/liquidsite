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

import java.text.SimpleDateFormat;
import java.util.Date;

import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSecurityException;
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
     * The offline status constant.
     */
    private static final int OFFLINE_STATUS = 0;

    /**
     * The online status constant.
     */
    private static final int ONLINE_STATUS = 1;
    
    /**
     * The modified status constant.
     */
    private static final int MODIFIED_STATUS = 2;

    /**
     * The date format used by this class.
     */
    private static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm");

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
        if (path.equals("style.css")) {
            request.sendFile(getFile(path));
        } else if (path.startsWith("images/")) {
            request.sendFile(getFile(path));
        } else {
            if (request.getUser() != null) {
                request.setAttribute("error", 
                                     "Access denied for your current user."); 
            }
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
        request.setAttribute("initialize", 
                             getSiteInitializeScript(request.getUser()));
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
        StringBuffer    buffer = new StringBuffer();
        String          type = request.getParameter("type", "");
        String          id = request.getParameter("id", "0");
        ContentManager  cm = getContentManager();
        User            user = request.getUser();
        Domain          domain = null;
        Content         content = null;
        Content[]       children; 

        // Find object and children
        try {
            if (type.equals("domain")) {
                domain = cm.getDomain(user, id);
                children = cm.getSites(user, domain);
            } else {
                content = cm.getContent(user, Integer.parseInt(id));
                children = cm.getContentChildren(user, content);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }

        // Create JavaScript output
        buffer.append("treeAddContainer('");
        buffer.append(id);
        buffer.append("');\n");
        for (int i = 0; i < children.length; i++) {
            buffer.append("treeAddItem('");
            buffer.append(id);
            buffer.append("', ");
            buffer.append(children[i].getId());
            buffer.append(", '");
            buffer.append(getContentType(children[i].getCategory()));
            buffer.append("', '");
            buffer.append(children[i].getName());
            buffer.append("', '");
            buffer.append(children[i].toString());
            buffer.append("', ");
            // TODO: add real status
            buffer.append(ONLINE_STATUS);
            buffer.append(");\n");
        }
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
        StringBuffer    buffer = new StringBuffer();
        String          type = request.getParameter("type", "");
        String          id = request.getParameter("id", "0");
        ContentManager  cm = getContentManager();
        User            user = request.getUser();
        Domain          domain = null;
        Content         content = null;

        // Find object
        try {
            if (type.equals("domain")) {
                domain = cm.getDomain(user, id);
            } else {
                content = cm.getContent(user, Integer.parseInt(id));
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }

        // Create JavaScript output
        buffer.append("objectShow('");
        buffer.append(type);
        buffer.append("', '");
        buffer.append(id);
        buffer.append("', '");
        if (domain != null) {
            buffer.append(domain.getName());
        } else {
            buffer.append(content.getName());
        }
        buffer.append("');\n");
        if (domain != null) {
            buffer.append("objectAddProperty('Name', '");
            buffer.append(domain.getDescription());
            buffer.append("');\n");
        } else {
            buffer.append("objectAddUrlProperty('");
            // TODO: add content URL
            buffer.append(content.toString());
            buffer.append("');\n");
            buffer.append("objectAddOnlineProperty(");
            buffer.append(getDateString(content.getOnlineDate()));
            buffer.append(", ");
            buffer.append(getDateString(content.getOfflineDate()));
            buffer.append(");\n");
            buffer.append("objectAddStatusProperty(");
            // TODO: get correct status value
            buffer.append(ONLINE_STATUS);
            buffer.append(", null);\n");
        }
        request.sendData("text/javascript", buffer.toString());
    }

    /**
     * Returns the JavaScript for initializing the site view.
     * 
     * @param user           the user performing the operation
     * 
     * @return the JavaScript for initializing the site view
     */
    private String getSiteInitializeScript(User user) {
        StringBuffer  buffer = new StringBuffer();
        Domain[]      domains = getContentManager().getDomains(user);

        for (int i = 0; i < domains.length; i++) {
            buffer.append("        treeAddItem(0, '");
            buffer.append(domains[i].getName());
            buffer.append("', 'domain', '");
            buffer.append(domains[i].getName());
            buffer.append("', '");
            buffer.append(domains[i].getDescription());
            buffer.append("', ");
            // TODO: set the correct status
            buffer.append(ONLINE_STATUS);
            buffer.append(");\n");
        }
        return buffer.toString();
    }

    /**
     * Returns the content type name for a content category.
     * 
     * @param category       the content category
     * 
     * @return the content type name
     */
    private String getContentType(int category) {
        switch (category) {
        case Content.SITE_CATEGORY:
            return "site";
        default:
            return "";
        }
    }
    
    /**
     * Returns a JavaScript string representation of a date.
     * 
     * @param date           the date to present, or null
     * 
     * @return a JavaScript string representation of the date
     */
    private String getDateString(Date date) {
        if (date == null) {
            return "null";
        } else {
            return "'" + DATE_FORMAT.format(date) + "'"; 
        }
    }
}
