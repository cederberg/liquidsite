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

import java.util.ArrayList;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.RequestException;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Host;
import net.percederberg.liquidsite.content.Site;
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
     * The class logger.
     */
    private static final Log LOG = new Log(AdminView.class);

    /**
     * The admin script helper.
     */
    private AdminScript script = new AdminScript();

    /**
     * The content manager to use.
     */
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
     * Shows the delete domain confirmation dialog.
     * 
     * @param request        the request object
     * @param domain         the domain to delete
     */
    public void dialogDeleteDomain(Request request, Domain domain) {
        setRequestReference(request, domain);
        request.setAttribute("name", domain.getName());
        request.sendTemplate("admin/dialog/delete-domain.ftl");
    }

    /**
     * Shows the delete site confirmation dialog.
     * 
     * @param request        the request object
     * @param site           the site to delete
     */
    public void dialogDeleteSite(Request request, Site site) {
        setRequestReference(request, site);
        request.setAttribute("name", site.getName());
        request.sendTemplate("admin/dialog/delete-site.ftl");
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
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    public void pageSite(Request request) throws RequestException {
        Object          focus = getSiteTreeFocus(request);
        User            user = request.getUser();
        Domain[]        domains;
        Content         content;
        StringBuffer    buffer = new StringBuffer();
        String          str;
        
        try {
            domains = manager.getDomains(user);
            buffer.append(script.getTreeView(domains));
            if (focus != null && focus instanceof Content) {
                content = manager.getContent(user, 
                                             ((Content) focus).getId());
                if (content != null) {
                    str = getTreeView(user, 
                                      content.getDomain(), 
                                      content.getParent(),
                                      true);
                    buffer.append(str);
                    buffer.append(script.getTreeViewSelect(content));
                }
            } else if (focus != null && focus instanceof Domain) {
                buffer.append(script.getTreeViewSelect((Domain) focus));
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
     * Shows the add site page.
     * 
     * @param request        the request object
     * @param parent         the parent object
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void pageAddSite(Request request, Object parent) 
        throws ContentException {

        Domain     domain;
        Host[]     hosts;
        ArrayList  list = new ArrayList();
        String     defaultHost = "*";
        
        if (parent instanceof Domain) {
            domain = (Domain) parent;
            hosts = domain.getHosts();
            for (int i = 0; i < hosts.length; i++) {
                if (i == 0) {
                    defaultHost = hosts[i].getName();
                }
                list.add(hosts[i].getName());
            }
        }
        setRequestReference(request, parent);
        request.setAttribute("hostnames", list);
        request.setAttribute("name", 
                             request.getParameter("name", ""));
        request.setAttribute("protocol", 
                             request.getParameter("protcol", ""));
        request.setAttribute("host", 
                             request.getParameter("host", defaultHost));
        request.setAttribute("port", 
                             request.getParameter("port", "80"));
        request.setAttribute("dir", 
                             request.getParameter("dir", "/"));
        request.sendTemplate("admin/add-site.ftl");
    }

    /**
     * Shows the load site object JavaScript code.
     * 
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    public void scriptLoadSite(Request request) throws RequestException {
        String          type = request.getParameter("type", "");
        String          id = request.getParameter("id", "0");
        User            user = request.getUser();
        Domain          domain;
        Content         content;
        String          buffer;

        try {
            if (type.equals("domain")) {
                domain = manager.getDomain(user, id);
                buffer = getTreeView(user, domain, null, false); 
            } else {
                content = manager.getContent(user, Integer.parseInt(id));
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
     * Shows the open site object JavaScript code.
     * 
     * @param request        the request object
     * 
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    public void scriptOpenSite(Request request) throws RequestException {
        String          type = request.getParameter("type", "");
        String          id = request.getParameter("id", "0");
        User            user = request.getUser();
        Domain          domain;
        Content         content;
        String          buffer;

        try {
            if (type.equals("domain")) {
                domain = manager.getDomain(user, id);
                buffer = script.getObjectView(user, domain);
                setSiteTreeFocus(request, domain);
            } else {
                content = manager.getContent(user, Integer.parseInt(id));
                buffer = script.getObjectView(user, content);
                setSiteTreeFocus(request, content);
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

        Content[]  children; 

        if (content == null) {
            children = manager.getSites(user, domain);
            return script.getTreeView(domain, children);
        } else if (recursive) {
            children = manager.getContentChildren(user, content);
            return getTreeView(user, domain, content.getParent(), true) 
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
}
