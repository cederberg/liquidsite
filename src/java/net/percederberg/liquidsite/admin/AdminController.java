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

import java.text.SimpleDateFormat;
import java.util.Date;

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
import net.percederberg.liquidsite.content.Lock;
import net.percederberg.liquidsite.content.Permission;
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
        String  category = request.getParameter("category", "");
        
        // TODO: add support for sites
        if (request.getParameter("prev") != null) {
            request.sendRedirect("site.html");
        } else if (category.equals("domain")) {
            processAddDomain(request);
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
        String  step = request.getParameter("step", "");
        String  name = request.getParameter("name", "");
        String  description = request.getParameter("description", "");
        String  hostname = request.getParameter("host", "");
        Domain  domain;
        Host    host;
        
        // TODO: check for existing domains and hosts, and invalid input
        if (request.getParameter("prev") != null) {
            displayAddObject(request);
        } else if (!step.equals("2")) {
            displayAddDomain(request);
        } else if (name.equals("")) {
            request.setAttribute("error", "No domain name specified");
            displayAddDomain(request);
        } else if (hostname.equals("")) {
            request.setAttribute("error", "No host name specified");
            displayAddDomain(request);
        } else {
            try {
                domain = new Domain(name);
                domain.setDescription(description);
                domain.save(request.getUser());
                host = new Host(domain, hostname);
                host.setDescription("Default domain host");
                host.save(request.getUser());
                request.setSessionAttribute("site.view.type", "domain");
                request.setSessionAttribute("site.view.id", name);
                request.sendRedirect("site.html");
            } catch (ContentException e) {
                LOG.error(e.getMessage());
                step = "Failed to save to database. Detailed message: " +
                       e.getMessage();
                request.setAttribute("error", step);
                displayAddDomain(request);
            } catch (ContentSecurityException e) {
                LOG.warning(e.getMessage());
                throw RequestException.FORBIDDEN;
            }
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
        Object        type = request.getSessionAttribute("site.view.type");
        Object        id = request.getSessionAttribute("site.view.id");
        Domain[]      domains;
        StringBuffer  script = new StringBuffer();
        
        try {
            domains = getContentManager().getDomains(request.getUser());
            script.append(getTreeViewScript(domains));
            if (type != null && !type.equals("domain")) {
                // TODO: add all parent content objects
            } 
            if (type != null) {
                script.append("treeSelect('");
                script.append(type);
                script.append("', '");
                script.append(id);
                script.append("');\n");
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        }
        request.setAttribute("initialize", script.toString());
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
        Content[]       children; 
        String          script;

        try {
            if (type.equals("domain")) {
                domain = cm.getDomain(user, id);
                children = cm.getSites(user, domain);
                script = getTreeViewScript(domain, children);
            } else {
                content = cm.getContent(user, Integer.parseInt(id));
                children = cm.getContentChildren(user, content);
                script = getTreeViewScript(content, children);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
        request.sendData("text/javascript", script);
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
        String          script;

        try {
            request.setSessionAttribute("site.view.type", type);
            request.setSessionAttribute("site.view.id", id);
            if (type.equals("domain")) {
                domain = cm.getDomain(user, id);
                script = getObjectViewScript(user, domain);
            } else {
                content = cm.getContent(user, Integer.parseInt(id));
                script = getObjectViewScript(user, content);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
        request.sendData("text/javascript", script);
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
     * Returns the JavaScript for presenting a tree view.
     * 
     * @param domains        the root domain objects
     * 
     * @return the JavaScript for presenting a tree view
     * 
     * @throws RequestException if the database couldn't be accessed
     *             properly
     */
    private String getTreeViewScript(Domain[] domains) 
        throws RequestException {

        StringBuffer  buffer = new StringBuffer();
        
        for (int i = 0; i < domains.length; i++) {
            buffer.append("treeAddItem(0, '");
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
     * Returns the JavaScript for presenting a tree view.
     * 
     * @param domain         the domain object
     * @param children       the child content objects
     * 
     * @return the JavaScript for presenting a tree view
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getTreeViewScript(Domain domain, Content[] children) 
        throws ContentException {

        StringBuffer    buffer = new StringBuffer();

        buffer.append("treeAddContainer('");
        buffer.append(domain.getName());
        buffer.append("');\n");
        for (int i = 0; i < children.length; i++) {
            buffer.append("treeAddItem('");
            buffer.append(domain.getName());
            buffer.append("', ");
            buffer.append(children[i].getId());
            buffer.append(", '");
            buffer.append(getScriptContentCategory(children[i]));
            buffer.append("', '");
            buffer.append(children[i].getName());
            buffer.append("', '");
            buffer.append(children[i].toString());
            buffer.append("', ");
            buffer.append(getScriptContentStatus(children[i]));
            buffer.append(");\n");
        }
        buffer.append("treeOpen('domain', '");
        buffer.append(domain.getName());
        buffer.append("');\n");
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting a tree view.
     * 
     * @param parent         the parent content object
     * @param children       the child content objects
     * 
     * @return the JavaScript for presenting a tree view
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getTreeViewScript(Content parent, Content[] children) 
        throws ContentException {

        StringBuffer    buffer = new StringBuffer();

        buffer.append("treeAddContainer(");
        buffer.append(parent.getId());
        buffer.append(");\n");
        for (int i = 0; i < children.length; i++) {
            buffer.append("treeAddItem(");
            buffer.append(parent.getId());
            buffer.append(", ");
            buffer.append(children[i].getId());
            buffer.append(", '");
            buffer.append(getScriptContentCategory(children[i]));
            buffer.append("', '");
            buffer.append(children[i].getName());
            buffer.append("', '");
            buffer.append(children[i].toString());
            buffer.append("', ");
            buffer.append(getScriptContentStatus(children[i]));
            buffer.append(");\n");
        }
        buffer.append("treeOpen('");
        buffer.append(getScriptContentCategory(parent));
        buffer.append("', ");
        buffer.append(parent.getId());
        buffer.append(");\n");
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting an object view.
     * 
     * @param user           the current user
     * @param domain         the domain object
     * 
     * @return the JavaScript for presenting an object view
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getObjectViewScript(User user, Domain domain) 
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();

        buffer.append("objectShow('domain', '");
        buffer.append(domain.getName());
        buffer.append("', '");
        buffer.append(domain.getName());
        buffer.append("');\n");
        buffer.append("objectAddProperty('Description', '");
        buffer.append(domain.getDescription());
        buffer.append("');\n");
        buffer.append(getButtonsScript(user, domain));
        buffer.append(getPermissionsScript(domain));
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting an object view.
     * 
     * @param user           the current user
     * @param content        the content object
     * 
     * @return the JavaScript for presenting an object view
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getObjectViewScript(User user, Content content) 
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();

        buffer.append("objectShow('");
        buffer.append(getScriptContentCategory(content));
        buffer.append("', ");
        buffer.append(content.getId());
        buffer.append(", '");
        buffer.append(content.getName());
        buffer.append("');\n");
        buffer.append("objectAddUrlProperty('");
        buffer.append(getScriptContentUrl(content));
        buffer.append("');\n");
        buffer.append("objectAddOnlineProperty(");
        buffer.append(getScriptDate(content.getOnlineDate()));
        buffer.append(", ");
        buffer.append(getScriptDate(content.getOfflineDate()));
        buffer.append(");\n");
        buffer.append("objectAddStatusProperty(");
        buffer.append(getScriptContentStatus(content));
        buffer.append(", ");
        buffer.append(getScriptLock(content.getLock()));
        buffer.append(");\n");
        buffer.append(getButtonsScript(user, content));
        buffer.append(getPermissionsScript(content));
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting domain buttons.
     * 
     * @param user           the current user
     * @param domain         the domain object
     * 
     * @return the JavaScript for presenting domain buttons
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getButtonsScript(User user, Domain domain) 
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();
        
        if (domain.hasWriteAccess(user)) {
            buffer.append("objectAddNewButton('add-site.html');\n");
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting domain buttons.
     * 
     * @param user           the current user
     * @param content        the content object
     * 
     * @return the JavaScript for presenting domain buttons
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getButtonsScript(User user, Content content) 
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();
        
        if (content.hasWriteAccess(user)) {
            buffer.append("objectAddNewButton('add-site.html');\n");
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting domain permissions.
     * 
     * @param domain         the domain object
     * 
     * @return the JavaScript for presenting domain permissions
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getPermissionsScript(Domain domain) 
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();
        Permission[]  permissions;
        
        permissions = domain.getPermissions();
        if (permissions.length == 0) {
            buffer.append(getPermissionScript(null, true));
        }
        for (int i = 0; i < permissions.length; i++) {
            buffer.append(getPermissionScript(permissions[i], false));
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting content permissions.
     * 
     * @param content        the content object
     * 
     * @return the JavaScript for presenting content permissions
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getPermissionsScript(Content content) 
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();
        Permission[]  permissions;
        Content       parent = content;
        boolean       inherited = false;
        
        // Find permissions
        permissions = content.getPermissions();
        while (permissions.length == 0 && parent != null) {
            inherited = true;
            parent = parent.getParent();
            if (parent != null) {
                permissions = parent.getPermissions();
            }
        }
        if (parent == null) {
            return getPermissionsScript(content.getDomain());
        }

        // Create permission script
        for (int i = 0; i < permissions.length; i++) {
            buffer.append(getPermissionScript(permissions[i], inherited));
        }

        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting a permission.
     * 
     * @param perm           the permission object, or null
     * @param inherited      the inherited flag
     * 
     * @return the JavaScript for presenting a permission
     */
    private String getPermissionScript(Permission perm, boolean inherited) {
        StringBuffer  buffer = new StringBuffer();
        
        buffer.append("objectAddPermission(");
        if (perm == null) {
            buffer.append("null, null, false, false, false, false");
        } else {
            buffer.append(getScriptString(perm.getUserName()));
            buffer.append(", ");
            buffer.append(getScriptString(perm.getGroupName()));
            buffer.append(", ");
            buffer.append(perm.getRead());
            buffer.append(", ");
            buffer.append(perm.getWrite());
            buffer.append(", ");
            buffer.append(perm.getPublish());
            buffer.append(", ");
            buffer.append(perm.getAdmin());
        }
        buffer.append(", ");
        buffer.append(!inherited);
        buffer.append(");\n");

        return buffer.toString();
    }

    /**
     * Returns the JavaScript representation of a content category.
     * 
     * @param content        the content object
     * 
     * @return the JavaScript representation of a content category
     */
    private String getScriptContentCategory(Content content) {
        switch (content.getCategory()) {
        case Content.SITE_CATEGORY:
            return "site";
        default:
            return "";
        }
    }

    /**
     * Returns the JavaScript representation of a content URL.
     * 
     * @param content        the content object
     * 
     * @return the JavaScript representation of a content URL
     */
    private String getScriptContentUrl(Content content) {
        if (content instanceof Site) {
            return content.toString();
        } else {
            return "N/A";
        }
    }

    /**
     * Returns the JavaScript representation of a content status.
     * 
     * @param content        the content object
     * 
     * @return the JavaScript representation of a content status
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private int getScriptContentStatus(Content content) 
        throws ContentException {

        if (!content.isOnline()) {
            return 0;
        } else if (content.getRevision(0) != null) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * Returns the JavaScript representation of a lock object.
     * 
     * @param lock           the lock object, or null
     * 
     * @return the JavaScript representation of a lock object
     */
    private String getScriptLock(Lock lock) {
        if (lock == null) {
            return "null";
        } else {
            return "'" + lock.getUserName() + "'";
        }
    }

    /**
     * Returns the JavaScript representation of a date.
     * 
     * @param date           the date to present, or null
     * 
     * @return a JavaScript representation of the date
     */
    private String getScriptDate(Date date) {
        if (date == null) {
            return "null";
        } else {
            return "'" + DATE_FORMAT.format(date) + "'"; 
        }
    }

    /**
     * Returns the JavaScript representation of a string. This method
     * will present empty strings as null.
     * 
     * @param str            the string to present, or null
     * 
     * @return a JavaScript representation of the string
     */
    private String getScriptString(String str) {
        if (str == null || str.equals("")) {
            return "null";
        } else {
            return "'" + str + "'";
        }
    }
}
