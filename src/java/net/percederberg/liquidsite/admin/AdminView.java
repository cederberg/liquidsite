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
import java.util.HashMap;
import java.util.Iterator;

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;
import net.percederberg.liquidsite.content.ContentFolder;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentPage;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.ContentSite;
import net.percederberg.liquidsite.content.ContentTemplate;
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
    public static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * The admin script helper.
     */
    private AdminScript script = new AdminScript();

    /**
     * Creates a new admin view helper.
     */
    public AdminView() {
    }

    /**
     * Shows the error message page. When the user presses the 
     * confirmation button on the error page, the browser will be
     * redirected to the specified page.
     * 
     * @param request        the request object
     * @param message        the error message
     * @param page           the redirect page
     */
    public void pageError(Request request, String message, String page) {
        request.setAttribute("error", message);
        request.setAttribute("page", page);
        request.sendTemplate("admin/error.ftl");
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

        ContentManager  manager = getContentManager();
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

        setReference(request, parent);
        if (parent instanceof Domain) {
            domain = (Domain) parent;
            if (user.isSuperUser()) {
                request.setAttribute("enableDomain", true);
            }
            if (domain.hasWriteAccess(user)) {
                request.setAttribute("enableSite", true);
                request.setAttribute("enableTemplate", true);
            }
        }
        if (parent instanceof ContentSite
         || parent instanceof ContentFolder) {

            content = (Content) parent;
            if (content.hasWriteAccess(user)) {
                request.setAttribute("enableFolder", true);
                request.setAttribute("enablePage", true);
                request.setAttribute("enableFile", true);
            }
        }
        if (parent instanceof ContentTemplate) {
            content = (Content) parent;
            if (content.hasWriteAccess(user)) {
                request.setAttribute("enableTemplate", true);
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
        setReference(request, parent);
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
        setReference(request, reference);
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
     * Shows the add or edit page page. Either the parent or the page 
     * object must be specified.
     * 
     * @param request        the request object
     * @param reference      the parent or page object 
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void pageEditPage(Request request, Object reference) 
        throws ContentException {

        ContentPage  page;

        String     name;
        String     template;
        String     comment;
        HashMap    locals = new HashMap();
        Iterator   iter;
        String     value;
        String     str;

        // Find default values
        setReference(request, reference);
        if (reference instanceof ContentPage) {
            page = (ContentPage) reference;
            setRequestTemplates(request, page.getDomain(), 0);
            name = page.getName();
            template = String.valueOf(page.getTemplateId());
            comment = "";
            iter = page.getLocalElementNames().iterator();
            while (iter.hasNext()) {
                str = iter.next().toString();
                value = page.getElement(str);
                locals.put(str, script.getString(value));
            }
        } else {
            setRequestTemplates(request, 
                                ((Content) reference).getDomain(), 
                                0);
            name = "";
            template = "0";
            comment = "Created";
        }

        // Adjust for incoming request
        if (request.getParameter("name") != null) {
            name = request.getParameter("name", "");
            template = request.getParameter("template", "0");
            comment = request.getParameter("comment", "");
            locals.clear();
            iter = request.getAllParameters().keySet().iterator();
            while (iter.hasNext()) {
                str = iter.next().toString();
                if (str.startsWith("element.")) {
                    value = request.getParameter(str);
                    locals.put(str.substring(8), script.getString(value));
                }
            }
        }

        // Set request parameters
        request.setAttribute("name", name);
        request.setAttribute("template", template);
        request.setAttribute("comment", comment);
        request.setAttribute("locals", locals);
        request.sendTemplate("admin/edit-page.ftl");
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

        setReference(request, reference);
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
            setReference(request, parent);
            name = "";
            comment = "Created";
        } else {
            setReference(request, folder);
            name = folder.getName();
            comment = "";
        }
        request.setAttribute("name", request.getParameter("name", name));
        request.setAttribute("comment", 
                             request.getParameter("comment", comment));
        request.sendTemplate("admin/edit-folder.ftl");
    }
    
    /**
     * Shows the add or edit template page. Either the parent or the
     * template object must be specified.
     * 
     * @param request        the request object
     * @param parent         the parent object, or null
     * @param template       the template object, or null
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void pageEditTemplate(Request request, 
                                 Object parent, 
                                 ContentTemplate template) 
        throws ContentException {

        String     name;
        String     comment;
        HashMap    locals = new HashMap();
        int        inherited;
        Iterator   iter;
        String     value;
        String     str;

        // Find default values
        if (parent != null) {
            setReference(request, parent);
            name = "";
            comment = "Created";
            if (parent instanceof Domain) {
                locals.put("root", script.getString(""));
                inherited = 0;
            } else {
                inherited = ((Content) parent).getId();
            }
        } else {
            setReference(request, template);
            setRequestTemplates(request, 
                                template.getDomain(), 
                                template.getId());
            name = template.getName();
            comment = "";
            iter = template.getLocalElementNames().iterator();
            while (iter.hasNext()) {
                str = iter.next().toString();
                value = template.getElement(str);
                locals.put(str, script.getString(value));
            }
            inherited = template.getParentId();
        }

        // Adjust for incoming request
        if (request.getParameter("name") != null) {
            name = request.getParameter("name", "");
            if (request.getParameter("parent") != null) {
                str = request.getParameter("parent", "0");
                try {
                    inherited = Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    inherited = 0;
                }
            }
            comment = request.getParameter("comment", "");
            locals.clear();
            iter = request.getAllParameters().keySet().iterator();
            while (iter.hasNext()) {
                str = iter.next().toString();
                if (str.startsWith("element.")) {
                    value = request.getParameter(str);
                    locals.put(str.substring(8), script.getString(value));
                }
            }
        }

        // Set request parameters
        request.setAttribute("name", name);
        request.setAttribute("comment", comment);
        request.setAttribute("locals", locals);
        request.setAttribute("parent", String.valueOf(inherited));
        request.sendTemplate("admin/edit-template.ftl");
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
     * Shows the open template JavaScript code.
     * 
     * @param request        the request object
     * @param template       the content template, or null
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void scriptOpenTemplate(Request request,
                                   ContentTemplate template)
        throws ContentException {

        String   buffer;

        // TODO: show work revision elements
        buffer = script.getTemplateElements(template);
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

        ContentManager  manager = getContentManager();
        Content[]       children; 

        if (content == null) {
            children = manager.getContentChildren(user, domain);
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
     * Sets the domain or content reference attributes in a request.
     * 
     * @param request        the request
     * @param obj            the domain or content object
     */
    protected void setReference(Request request, Object obj) {
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
     * Sets template id and name attributes in a request. This method
     * will retrieve all templates in the domain, excluding the 
     * specified content id.
     * 
     * @param request        the request
     * @param domain         the domain object
     * @param excludeId      the content id to exclude
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void setRequestTemplates(Request request, 
                                     Domain domain,
                                     int excludeId)
        throws ContentException {

        User       user = request.getUser();
        ArrayList  templateIds = new ArrayList();
        HashMap    templateNames = new HashMap();
        Content[]  children;
    
        children = getContentManager().getContentChildren(user, domain);
        addTemplates(user, 
                     "", 
                     children, 
                     excludeId, 
                     templateIds, 
                     templateNames);
        request.setAttribute("templateIds", templateIds);
        request.setAttribute("templateNames", templateNames);
    }

    /**
     * Adds all content templates to lists of template ids and names.
     * This method will retrieve all child templates as well, 
     * excluding only a specified content id.
     * 
     * @param user           the user
     * @param baseName       the base template name
     * @param content        the array of content objects
     * @param excludeId      the content id to exclude
     * @param ids            the list of template ids to add to
     * @param names          the map of template names to add to
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void addTemplates(User user, 
                              String baseName,
                              Content[] content,
                              int excludeId,
                              ArrayList ids,
                              HashMap names)
        throws ContentException {

        ContentManager  cm = getContentManager();
        Content[]       children;
        String          id;
        String          name;

        for (int i = 0; i < content.length; i++) {
            if (content[i] instanceof ContentTemplate 
             && content[i].getId() != excludeId) {

                id = String.valueOf(content[i].getId());
                name = baseName + content[i].getName();
                ids.add(id);
                names.put(id, name);
                children = cm.getContentChildren(user, content[i]);
                addTemplates(user, 
                             name + " / ", 
                             children, 
                             excludeId, 
                             ids, 
                             names);
            }
        }
    }

    /**
     * Returns the content manager currently in use.
     * 
     * @return the content manager currently in use
     * 
     * @throws ContentException if no content manager exists
     */
    private ContentManager getContentManager() throws ContentException {
        return ContentManager.getInstance();
    }
}
