/*
 * AdminView.java
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
import java.util.ArrayList;
import java.util.Date;

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;
import net.percederberg.liquidsite.content.ContentFolder;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.ContentSite;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Host;
import net.percederberg.liquidsite.content.User;

/**
 * A helper class for creating the HTML and JavaScript output for the
 * administration application.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class AdminView {

    /**
     * The date format used by this class.
     */
    private static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * The admin script helper.
     */
    private AdminScript script = new AdminScript();

    /**
     * The content manager to use.
     */
    // FIXME: cannot cling to content manager instance!
    private ContentManager manager;

    /**
     * Creates a new admin view helper.
     * 
     * @param manager        the content manager to use
     */
    public AdminView(ContentManager manager) {
        this.manager = manager;
    }

    /**
     * Shows the automatic close dialog.
     * 
     * @param request        the request object
     */
    public void dialogClose(Request request) {
        request.sendTemplate("admin/dialog/close.ftl");
    }

    /**
     * Shows the error message dialog.
     * 
     * @param request        the request object
     * @param message        the error message
     */
    public void dialogError(Request request, String message) {
        request.setAttribute("error", message);
        request.sendTemplate("admin/dialog/error.ftl");
    }

    /**
     * Shows the error message dialog.
     * 
     * @param request        the request object
     * @param e              the content database error
     */
    public void dialogError(Request request, ContentException e) {
        dialogError(request, "Database access error, " + e.getMessage());
    }

    /**
     * Shows the error message dialog.
     * 
     * @param request        the request object
     * @param e              the content security error
     */
    public void dialogError(Request request, ContentSecurityException e) {
        dialogError(request, "Security violation, " + e.getMessage());
    }

    /**
     * Shows the delete object confirmation dialog.
     * 
     * @param request        the request object
     * @param obj            the domain or content object to delete
     */
    public void dialogDelete(Request request, Object obj) {
        String  name;

        setRequestReference(request, obj);
        if (obj instanceof Domain) {
            name = ((Domain) obj).getName();
        } else {
            name = ((Content) obj).getName();
        }
        request.setAttribute("name", name);
        request.sendTemplate("admin/dialog/delete-object.ftl");
    }

    /**
     * Shows the content publish dialog.
     * 
     * @param request        the request object
     * @param content        the content object to publish
     */
    public void dialogPublish(Request request, Content content) {
        String dateStr = request.getParameter("date");
        String comment = request.getParameter("comment");

        if (dateStr == null) {
            dateStr = formatDate(new Date());
        }
        if (comment == null) {
            comment = content.getComment();
        }
        setRequestReference(request, content);
        request.setAttribute("date", dateStr);
        request.setAttribute("comment", comment);
        request.sendTemplate("admin/dialog/publish-object.ftl");
    }

    /**
     * Shows the content unpublish dialog.
     * 
     * @param request        the request object
     * @param content        the content object to unpublish
     */
    public void dialogUnpublish(Request request, Content content) {
        String dateStr = request.getParameter("date");
        String comment = request.getParameter("comment");

        if (dateStr == null) {
            dateStr = formatDate(content.getOfflineDate());
        }
        if (dateStr.equals("")) {
            dateStr = formatDate(new Date());
        }
        if (comment == null) {
            comment = "Unpublished";
        }
        setRequestReference(request, content);
        request.setAttribute("date", dateStr);
        request.setAttribute("comment", comment);
        request.sendTemplate("admin/dialog/unpublish-object.ftl");
    }

    /**
     * Shows the content revert dialog.
     * 
     * @param request        the request object
     * @param content        the content object to revert
     */
    public void dialogRevert(Request request, Content content) {
        String  str;

        setRequestReference(request, content);
        if (content.getRevisionNumber() == 0) {
            str = "Work";
        } else {
            str = String.valueOf(content.getRevisionNumber());
        }
        request.setAttribute("revision", str);
        request.sendTemplate("admin/dialog/revert-object.ftl");
    }

    /**
     * Shows the content unlock dialog.
     * 
     * @param request        the request object
     * @param content        the content object to unlock
     */
    public void dialogUnlock(Request request, Content content) {
        setRequestReference(request, content);
        request.sendTemplate("admin/dialog/unlock-object.ftl");
    }

    /**
     * Shows the error message page.
     * 
     * @param request        the request object
     * @param message        the error message
     */
    public void pageError(Request request, String message) {
        request.setAttribute("error", message);
        request.setAttribute("page", "index.html");
        request.sendTemplate("admin/error.ftl");
    }

    /**
     * Shows the error message page.
     * 
     * @param request        the request object
     * @param e              the content database error
     */
    public void pageError(Request request, ContentException e) {
        pageError(request, "Database access error, " + e.getMessage());
    }

    /**
     * Shows the error message page.
     * 
     * @param request        the request object
     * @param e              the content security error
     */
    public void pageError(Request request, ContentSecurityException e) {
        pageError(request, "Security violation, " + e.getMessage());
    }

    /**
     * Shows the home page.
     *
     * @param request        the request object
     */
    public void pageHome(Request request) {
        request.sendTemplate("admin/home.ftl");
    }

    /**
     * Shows the site page.
     *
     * @param request        the request object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    public void pageSite(Request request) 
        throws ContentException, ContentSecurityException {

        Object          focus = getSiteTreeFocus(request);
        User            user = request.getUser();
        Domain[]        domains;
        Content         content;
        StringBuffer    buffer = new StringBuffer();
        String          str;
        
        domains = manager.getDomains(user);
        buffer.append(script.getTreeView(domains));
        if (focus != null && focus instanceof Content) {
            content = manager.getContent(user, ((Content) focus).getId());
            if (content != null) {
                str = scriptSiteTree(user, 
                                     content.getDomain(), 
                                     content.getParent(),
                                     true);
                buffer.append(str);
                buffer.append(script.getTreeViewSelect(content));
            }
        } else if (focus != null && focus instanceof Domain) {
            buffer.append(script.getTreeViewSelect((Domain) focus));
        } else if (focus == null && domains.length > 0) {
            buffer.append(script.getTreeViewSelect(domains[0]));
        }
        request.setAttribute("initialize", buffer.toString());
        request.sendTemplate("admin/site.ftl");
    }

    /**
     * Shows the content page.
     *
     * @param request        the request object
     */
    public void pageContent(Request request) {
        request.sendTemplate("admin/content.ftl");
    }

    /**
     * Shows the users page.
     *
     * @param request        the request object
     */
    public void pageUsers(Request request) {
        request.sendTemplate("admin/users.ftl");
    }

    /**
     * Shows the system page.
     *
     * @param request        the request object
     */
    public void pageSystem(Request request) {
        request.sendTemplate("admin/system.ftl");
    }

    /**
     * Shows the edit user page.
     * 
     * @param request        the request object
     */
    public void pageEditUser(Request request) {
        User    user = request.getUser();
        String  str;

        str = request.getParameter("name", user.getRealName());
        request.setAttribute("name", str);
        str = request.getParameter("email", user.getEmail());
        request.setAttribute("email", str);
        request.sendTemplate("admin/edit-user.ftl");
    }

    /**
     * Shows the edit password page.
     * 
     * @param request        the request object
     */
    public void pageEditPassword(Request request) {
        request.sendTemplate("admin/edit-password.ftl");
    }

    /**
     * Shows the add object page.
     * 
     * @param request        the request object
     * @param parent         the parent object
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void pageAddObject(Request request, Object parent) 
        throws ContentException {

        User     user = request.getUser();
        Domain   domain;
        Content  content;

        setRequestReference(request, parent);
        if (parent instanceof Domain) {
            domain = (Domain) parent;
            if (user.isSuperUser()) {
                request.setAttribute("enableDomain", true);
            }
            if (domain.hasWriteAccess(user)) {
                request.setAttribute("enableSite", true);
            }
        }
        if (parent instanceof ContentSite
         || parent instanceof ContentFolder) {

            content = (Content) parent;
            if (content.hasWriteAccess(user)) {
                request.setAttribute("enableFolder", true);
                request.setAttribute("enableFile", true);
            }
        }
        request.sendTemplate("admin/add-object.ftl");
    }

    /**
     * Shows the add domain page.
     * 
     * @param request        the request object
     * @param parent         the parent object
     */
    public void pageAddDomain(Request request, Object parent) {
        setRequestReference(request, parent);
        request.setAttribute("name", request.getParameter("name", ""));
        request.setAttribute("description", 
                             request.getParameter("description", ""));
        request.setAttribute("host", request.getParameter("host", ""));
        request.sendTemplate("admin/add-domain.ftl");
    }
    
    /**
     * Shows the add or edit site page.
     * 
     * @param request        the request object
     * @param reference      the reference object (domain or site)
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void pageEditSite(Request request, Object reference) 
        throws ContentException {

        Domain       domain;
        ContentSite  site;
        Host[]       hosts;
        ArrayList    list = new ArrayList();
        String       defaultName;
        String       defaultProtocol;
        String       defaultHost;
        String       defaultPort;
        String       defaultDir;
        String       defaultComment;
        String       str;
        
        if (reference instanceof Domain) {
            domain = (Domain) reference;
            defaultName = "";
            defaultProtocol = "";
            defaultHost = "*";
            defaultPort = "80";
            defaultDir = "/";
            defaultComment = "Created";
        } else {
            site = (ContentSite) reference;
            domain = site.getDomain();
            defaultName = site.getName();
            defaultProtocol = site.getProtocol();
            defaultHost = site.getHost();
            defaultPort = String.valueOf(site.getPort());
            defaultDir = site.getDirectory();
            defaultComment = "";
        }
        hosts = domain.getHosts();
        for (int i = 0; i < hosts.length; i++) {
            list.add(hosts[i].getName());
        }
        setRequestReference(request, reference);
        request.setAttribute("hostnames", list);
        str = request.getParameter("name", defaultName);
        request.setAttribute("name", str);
        str = request.getParameter("protcol", defaultProtocol);
        request.setAttribute("protocol", str);
        str = request.getParameter("host", defaultHost);
        request.setAttribute("host", str);
        str = request.getParameter("port", defaultPort);
        request.setAttribute("port", str);
        str = request.getParameter("dir", defaultDir);
        request.setAttribute("dir", str);
        str = request.getParameter("comment", defaultComment);
        request.setAttribute("comment", str);
        request.sendTemplate("admin/edit-site.ftl");
    }

    /**
     * Shows the add or edit file page.
     * 
     * @param request        the request object
     * @param reference      the reference object (parent or file)
     */
    public void pageEditFile(Request request, Object reference) {
        String  name;
        String  comment;

        setRequestReference(request, reference);
        if (reference instanceof ContentFile) {
            name = ((ContentFile) reference).getName();
            comment = "";
        } else {
            name = "";
            comment = "Created";
        }
        request.setAttribute("name", request.getParameter("name", name));
        request.setAttribute("comment", 
                             request.getParameter("comment", comment));
        request.sendTemplate("admin/edit-file.ftl");
    }
    
    /**
     * Shows the add or edit folder page. Either the parent or the
     * folder object must be specified.
     * 
     * @param request        the request object
     * @param parent         the parent object, or null
     * @param folder         the folder object, or null
     */
    public void pageEditFolder(Request request, 
                               Object parent, 
                               ContentFolder folder) {

        String  name;
        String  comment;

        if (parent != null) {
            setRequestReference(request, parent);
            name = "";
            comment = "Created";
        } else {
            setRequestReference(request, folder);
            name = folder.getName();
            comment = "";
        }
        request.setAttribute("name", request.getParameter("name", name));
        request.setAttribute("comment", 
                             request.getParameter("comment", comment));
        request.sendTemplate("admin/edit-folder.ftl");
    }
    
    /**
     * Shows the load site object JavaScript code.
     * 
     * @param request        the request object
     * @param obj            the domain or content parent object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void scriptLoadSite(Request request, Object obj) 
        throws ContentException {

        User     user = request.getUser();
        Content  content;
        String   buffer;

        if (obj instanceof Domain) {
            buffer = scriptSiteTree(user, (Domain) obj, null, false); 
        } else {
            content = (Content) obj;
            buffer = scriptSiteTree(user, null, content, false); 
        }
        request.sendData("text/javascript", buffer);
    }
    
    /**
     * Shows the open site object JavaScript code.
     * 
     * @param request        the request object
     * @param obj            the domain or content object
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void scriptOpenSite(Request request, Object obj) 
        throws ContentException {

        User     user = request.getUser();
        Content  content;
        Content  revision;
        String   buffer;

        if (obj instanceof Domain) {
            buffer = script.getObjectView(user, (Domain) obj);
            setSiteTreeFocus(request, obj);
        } else {
            content = (Content) obj;
            revision = content.getRevision(0);
            if (revision != null) {
                content = revision;
            }
            buffer = script.getObjectView(user, content);
            setSiteTreeFocus(request, content);
        }
        request.sendData("text/javascript", buffer);
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
    private String scriptSiteTree(User user, 
                                  Domain domain, 
                                  Content content,
                                  boolean recursive) 
        throws ContentException {

        Content[]  children; 

        if (content == null) {
            children = manager.getSites(user, domain);
            return script.getTreeView(domain, children);
        } else if (recursive) {
            children = manager.getContentChildren(user, content);
            return scriptSiteTree(user, domain, content.getParent(), true) 
                 + script.getTreeView(content, children);
        } else {
            children = manager.getContentChildren(user, content);
            return script.getTreeView(content, children);
        }
    }
    
    /**
     * Returns the site tree focus object. The focus object is either 
     * a domain or a content object, and is stored in the session. It 
     * is not possible to trust the focus object for any operations.
     * It may have been removed from the database by another user.
     * 
     * @param request        the request
     * 
     * @return the site view focus object, or 
     *         null for none
     */
    public Object getSiteTreeFocus(Request request) {
        return request.getSessionAttribute("site.tree.focus");
    }

    /**
     * Sets the site tree focus object. The focus object is either a
     * domain or a content object, and is stored in the session.
     * 
     * @param request        the request
     * @param obj            the new focus object
     */
    public void setSiteTreeFocus(Request request, Object obj) {
        request.setSessionAttribute("site.tree.focus", obj);
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
    public Object getRequestReference(Request request) 
        throws ContentException, ContentSecurityException {

        User    user = request.getUser();
        String  type = request.getParameter("type");
        String  id = request.getParameter("id");
        
        if (type == null || id == null) {
            return null;
        } else if (type.equals("domain")) {
            return manager.getDomain(user, id);
        } else {
            return manager.getContent(user, Integer.parseInt(id));
        }
    }

    /**
     * Sets the domain or content reference attributes in a request.
     * 
     * @param request        the request
     * @param obj            the domain or content object
     */
    private void setRequestReference(Request request, Object obj) {
        Content  content;

        if (obj instanceof Domain) {
            request.setAttribute("type", "domain");
            request.setAttribute("id", ((Domain) obj).getName());
        } else {
            content = (Content) obj;
            request.setAttribute("type", 
                                 script.getContentCategory(content));
            request.setAttribute("id", 
                                 String.valueOf(content.getId()));
        }
    }
    
    /**
     * Formats a date for form input.
     * 
     * @param date           the date to format
     * 
     * @return a formatted date string
     */
    private String formatDate(Date date) {
        if (date == null) {
            return "";
        } else {
            return DATE_FORMAT.format(date);
        }
    }
}
