/*
 * SiteView.java
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

package net.percederberg.liquidsite.admin.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.admin.AdminUtils;
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
 * A helper class for the site view. This class contains methods for
 * creating the HTML and JavaScript responses to the various pages in
 * the site view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class SiteView extends AdminView {

    /**
     * Creates a new site view helper.
     */
    SiteView() {
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
    public void viewSite(Request request) 
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        Object          focus = getSiteTreeFocus(request);
        User            user = request.getUser();
        Domain[]        domains;
        Content         content;
        StringBuffer    buffer = new StringBuffer();
        String          str;
        
        domains = manager.getDomains(user);
        buffer.append(SCRIPT.getTreeView(domains));
        if (focus != null && focus instanceof Content) {
            content = manager.getContent(user, ((Content) focus).getId());
            if (content != null) {
                str = getSiteTreeScript(user, 
                                        content.getDomain(), 
                                        content.getParent(),
                                        true);
                buffer.append(str);
                buffer.append(SCRIPT.getTreeViewSelect(content));
            }
        } else if (focus != null && focus instanceof Domain) {
            buffer.append(SCRIPT.getTreeViewSelect((Domain) focus));
        } else if (focus == null && domains.length > 0) {
            buffer.append(SCRIPT.getTreeViewSelect(domains[0]));
        }
        request.setAttribute("initialize", buffer.toString());
        request.sendTemplate("admin/site.ftl");
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
    public void viewAddObject(Request request, Object parent) 
        throws ContentException {

        User     user = request.getUser();
        Domain   domain;
        Content  content;

        AdminUtils.setReference(request, parent);
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
    public void viewAddDomain(Request request, Object parent) {
        AdminUtils.setReference(request, parent);
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
    public void viewEditSite(Request request, Object reference) 
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
            if (site.getRevisionNumber() == 0) {
                defaultComment = site.getComment();
            } else {
                defaultComment = "";
            }
        }
        hosts = domain.getHosts();
        for (int i = 0; i < hosts.length; i++) {
            list.add(hosts[i].getName());
        }
        AdminUtils.setReference(request, reference);
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
     * @throws ContentSecurityException if the user didn't have read
     *             access to the template
     */
    public void viewEditPage(Request request, Object reference) 
        throws ContentException, ContentSecurityException {

        ContentPage  page;
        Content      content;
        ContentSite  site;
        String       name;
        int          parent;
        ArrayList    folders = null;
        String       template;
        int          section;
        ArrayList    sections;
        String       comment;
        HashMap      locals = new HashMap();
        Iterator     iter;
        String       value;
        String       str;

        // Find default values
        AdminUtils.setReference(request, reference);
        if (reference instanceof ContentPage) {
            page = (ContentPage) reference;
            setRequestTemplates(request, page.getDomain(), 0);
            name = page.getName();
            parent = page.getParentId();
            site = findSite(page);
            folders = findFolders(request.getUser(), site, null);
            template = String.valueOf(page.getTemplateId());
            section = page.getSectionId();
            sections = findSections(request.getUser(), page.getDomain(), null); 
            if (page.getRevisionNumber() == 0) {
                comment = page.getComment();
            } else {
                comment = "";
            }
            iter = page.getLocalElementNames().iterator();
            while (iter.hasNext()) {
                str = iter.next().toString();
                value = page.getElement(request.getUser(), str);
                locals.put(str, AdminUtils.getScriptString(value));
            }
        } else {
            content = (Content) reference;
            setRequestTemplates(request, 
                                content.getDomain(), 
                                0);
            name = "";
            parent = 0;
            template = "0";
            section = 0;
            sections = findSections(request.getUser(),
                                    content.getDomain(),
                                    null); 
            comment = "Created";
        }

        // Adjust for incoming request
        if (request.getParameter("name") != null) {
            name = request.getParameter("name", "");
            try {
                str = request.getParameter("parent", "0");
                parent = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                parent = 0;
            }
            template = request.getParameter("template", "0");
            try {
                str = request.getParameter("section", "0");
                section = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                section = 0;
            }
            comment = request.getParameter("comment", "");
            locals.clear();
            iter = request.getAllParameters().keySet().iterator();
            while (iter.hasNext()) {
                str = iter.next().toString();
                if (str.startsWith("element.")) {
                    value = request.getParameter(str);
                    locals.put(str.substring(8), 
                               AdminUtils.getScriptString(value));
                }
            }
        }

        // Set request parameters
        request.setAttribute("name", name);
        request.setAttribute("parent", String.valueOf(parent));
        request.setAttribute("folders", folders);
        request.setAttribute("template", template);
        request.setAttribute("section", String.valueOf(section));
        request.setAttribute("sections", sections);
        request.setAttribute("comment", comment);
        request.setAttribute("locals", locals);
        request.sendTemplate("admin/edit-page.ftl");
    }

    /**
     * Shows the add or edit file page.
     *
     * @param request        the request object
     * @param reference      the reference object (parent or file)
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewEditFile(Request request, Object reference)
        throws ContentException {

        ContentFile  file;
        ContentSite  site;
        String       name;
        int          parent;
        ArrayList    folders = null;
        String       comment;
        String       str;

        // Find default values
        AdminUtils.setReference(request, reference);
        if (reference instanceof ContentFile) {
            file = (ContentFile) reference;
            name = file.getName();
            parent = file.getParentId();
            site = findSite(file);
            folders = findFolders(request.getUser(), site, null);
            if (file.getRevisionNumber() == 0) {
                comment = file.getComment();
            } else {
                comment = "";
            }
        } else {
            name = "";
            parent = 0;
            comment = "Created";
        }

        // Adjust for incoming request
        if (request.getParameter("name") != null) {
            name = request.getParameter("name", "");
            try {
                str = request.getParameter("parent", "0");
                parent = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                parent = 0;
            }
            comment = request.getParameter("comment", "");
        }

        // Set request parameters
        request.setAttribute("name", name);
        request.setAttribute("parent", String.valueOf(parent));
        request.setAttribute("folders", folders);
        request.setAttribute("comment", comment);
        request.sendTemplate("admin/edit-file.ftl");
    }
    
    /**
     * Shows the add or edit folder page. Either the parent or the
     * folder object must be specified.
     * 
     * @param request        the request object
     * @param parent         the parent object, or null
     * @param folder         the folder object, or null
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewEditFolder(Request request, 
                               Object parent, 
                               ContentFolder folder)
        throws ContentException {

        ContentSite  site;
        String       name;
        int          parentId;
        ArrayList    folders = null;
        String       comment;
        String       str;

        // Find default values
        if (parent != null) {
            AdminUtils.setReference(request, parent);
            name = "";
            parentId = 0;
            comment = "Created";
        } else {
            AdminUtils.setReference(request, folder);
            name = folder.getName();
            parentId = folder.getParentId();
            site = findSite(folder);
            folders = findFolders(request.getUser(), site, folder);
            if (folder.getRevisionNumber() == 0) {
                comment = folder.getComment();
            } else {
                comment = "";
            }
        }

        // Adjust for incoming request
        if (request.getParameter("name") != null) {
            name = request.getParameter("name", "");
            try {
                str = request.getParameter("parent", "0");
                parentId = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                parentId = 0;
            }
            comment = request.getParameter("comment", "");
        }

        // Set request parameters
        request.setAttribute("name", name);
        request.setAttribute("parent", String.valueOf(parentId));
        request.setAttribute("folders", folders);
        request.setAttribute("comment", comment);
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
    public void viewEditTemplate(Request request, 
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
            AdminUtils.setReference(request, parent);
            name = "";
            comment = "Created";
            if (parent instanceof Domain) {
                locals.put("root", AdminUtils.getScriptString(""));
                inherited = 0;
            } else {
                inherited = ((Content) parent).getId();
            }
        } else {
            AdminUtils.setReference(request, template);
            setRequestTemplates(request, 
                                template.getDomain(), 
                                template.getId());
            name = template.getName();
            if (template.getRevisionNumber() == 0) {
                comment = template.getComment();
            } else {
                comment = "";
            }
            iter = template.getLocalElementNames().iterator();
            while (iter.hasNext()) {
                str = iter.next().toString();
                value = template.getElement(str);
                locals.put(str, AdminUtils.getScriptString(value));
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
                    locals.put(str.substring(8), 
                               AdminUtils.getScriptString(value));
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
     * Shows the template preview page.
     * 
     * @param request        the request object
     * @param template       the template object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewTemplatePreview(Request request,
                                    ContentTemplate template)
        throws ContentException {

        HashMap   locals = new HashMap();
        HashMap   inherited = new HashMap();
        Iterator  iter;
        String    name;

        iter = template.getLocalElementNames().iterator();
        while (iter.hasNext()) {
            name = iter.next().toString();
            locals.put(name, template.getElement(name));
        }
        iter = template.getAllElementNames().iterator();
        while (iter.hasNext()) {
            name = iter.next().toString();
            if (!locals.containsKey(name)) {
                inherited.put(name, template.getElement(name));
            }
        }
        request.setAttribute("locals", locals);
        request.setAttribute("inherited", inherited);
        request.sendTemplate("admin/preview-template.ftl");
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
    public void viewLoadSiteScript(Request request, Object obj) 
        throws ContentException {

        User     user = request.getUser();
        Content  content;
        String   buffer;

        if (obj instanceof Domain) {
            buffer = getSiteTreeScript(user, (Domain) obj, null, false); 
        } else {
            content = (Content) obj;
            buffer = getSiteTreeScript(user, null, content, false); 
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
    public void viewOpenSiteScript(Request request, Object obj) 
        throws ContentException {

        User     user = request.getUser();
        Content  content;
        String   buffer;

        if (obj instanceof Domain) {
            buffer = SCRIPT.getObjectView(user, (Domain) obj, "site");
            setSiteTreeFocus(request, obj);
        } else {
            content = (Content) obj;
            buffer = SCRIPT.getObjectView(user, content, "site");
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
    public void viewOpenTemplateScript(Request request,
                                       ContentTemplate template)
        throws ContentException {

        String   buffer;

        buffer = SCRIPT.getTemplateElements(template);
        request.sendData("text/javascript", buffer);
    }

    /**
     * Finds the content site for a specified page.
     * 
     * @param page           the content page, file or folder
     * 
     * @return the containing content site
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private ContentSite findSite(Content page) throws ContentException {
        while (page != null && !(page instanceof ContentSite)) {
            page = page.getParent();
        }
        return (ContentSite) page;
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
    private String getSiteTreeScript(User user, 
                                     Domain domain, 
                                     Content content,
                                     boolean recursive) 
        throws ContentException {

        ContentManager  manager = AdminUtils.getContentManager();
        Content[]       children;
        ArrayList       list = new ArrayList(); 

        if (content == null) {
            children = manager.getContentChildren(user, domain);
            for (int i = 0; i < children.length; i++) {
                if (isViewable(children[i])) {
                    list.add(children[i]);
                }
            }
            children = new Content[list.size()];
            list.toArray(children);
            return SCRIPT.getTreeView(domain, children);
        } else if (recursive) {
            children = manager.getContentChildren(user, content);
            return getSiteTreeScript(user, domain, content.getParent(), true) 
                 + SCRIPT.getTreeView(content, children);
        } else {
            children = manager.getContentChildren(user, content);
            return SCRIPT.getTreeView(content, children);
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

        ContentManager  manager = AdminUtils.getContentManager();
        User            user = request.getUser();
        ArrayList       templateIds = new ArrayList();
        HashMap         templateNames = new HashMap();
        Content[]       children;
    
        children = manager.getContentChildren(user,
                                              domain,
                                              Content.TEMPLATE_CATEGORY);
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

        ContentManager  manager = AdminUtils.getContentManager();
        Content[]       children;
        String          id;
        String          name;

        for (int i = 0; i < content.length; i++) {
            if (content[i].getId() != excludeId) {
                id = String.valueOf(content[i].getId());
                name = baseName + content[i].getName();
                ids.add(id);
                names.put(id, name);
                children = manager.getContentChildren(user,
                                                      content[i],
                                                      Content.TEMPLATE_CATEGORY);
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
     * Checks if an object should be shown in the site view. 
     * 
     * @param obj            the object to check
     * 
     * @return true if the object should be shown, or
     *         false otherwise
     */
    private boolean isViewable(Object obj) {
        return obj instanceof Domain
            || obj instanceof ContentSite
            || obj instanceof ContentFolder
            || obj instanceof ContentFile
            || obj instanceof ContentPage
            || obj instanceof ContentTemplate;
    }
}
