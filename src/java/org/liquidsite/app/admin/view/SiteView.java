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
 * Copyright (c) 2004-2006 Per Cederberg. All rights reserved.
 */

package org.liquidsite.app.admin.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.liquidsite.app.admin.AdminUtils;
import org.liquidsite.app.servlet.Application;
import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentFile;
import org.liquidsite.core.content.ContentFolder;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentPage;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.ContentSite;
import org.liquidsite.core.content.ContentTemplate;
import org.liquidsite.core.content.ContentTranslator;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.Host;
import org.liquidsite.core.content.PersistentObject;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.Request;
import org.liquidsite.core.web.Request.FileParameter;

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
        // Nothing to initialize
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
                str = getTreeScript(user,
                                    content.getDomain(),
                                    content.getParent(),
                                    true,
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
        AdminUtils.sendTemplate(request, "admin/site.ftl");
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
    public void viewAddObject(Request request, PersistentObject parent)
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
                request.setAttribute("enableTranslator", true);
            }
        }
        if (parent instanceof ContentTranslator) {
            content = (Content) parent;
            if (content.hasWriteAccess(user)) {
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
        AdminUtils.sendTemplate(request, "admin/add-object.ftl");
    }

    /**
     * Shows the add or edit domain page.
     *
     * @param request        the request object
     * @param parent         the parent object for adding
     * @param domain         the domain object for editing
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewEditDomain(Request request,
                               PersistentObject parent,
                               Domain domain)
        throws ContentException {

        String     name;
        String     description;
        ArrayList  hosts = new ArrayList();
        Host[]     hostArray;
        Iterator   iter;
        HashMap    map;
        String     param;
        String     value;

        // Find default values
        if (parent == null) {
            AdminUtils.setReference(request, domain);
            name = domain.getName();
            description = domain.getDescription();
            hostArray = domain.getHosts();
            for (int i = 0; i < hostArray.length; i++) {
                map = new HashMap(2);
                value = hostArray[i].getName();
                map.put("name", AdminUtils.getScriptString(value));
                value = hostArray[i].getDescription();
                map.put("description", AdminUtils.getScriptString(value));
                hosts.add(map);
            }
        } else {
            AdminUtils.setReference(request, parent);
            name = "";
            description = "";
        }

        // Adjust for incoming request
        if (request.getParameter("name") != null) {
            name = request.getParameter("name", "");
            description = request.getParameter("description", "");
            hosts.clear();
            iter = request.getAllParameters().keySet().iterator();
            while (iter.hasNext()) {
                param = iter.next().toString();
                if (param.startsWith("host.") && param.endsWith(".name")) {
                    param = param.substring(0, param.length() - 5);
                    map = new HashMap(2);
                    value = request.getParameter(param + ".name");
                    map.put("name", AdminUtils.getScriptString(value));
                    value = request.getParameter(param + ".description");
                    map.put("description", AdminUtils.getScriptString(value));
                    hosts.add(map);
                }
            }
        }

        // Set request parameters
        request.setAttribute("name", name);
        request.setAttribute("description", description);
        request.setAttribute("hosts", hosts);
        AdminUtils.sendTemplate(request, "admin/edit-domain.ftl");
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
    public void viewEditSite(Request request, PersistentObject reference)
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
        boolean      publish;
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
        publish = reference.hasPublishAccess(request.getUser());
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
        request.setAttribute("publish", String.valueOf(publish));
        AdminUtils.sendTemplate(request, "admin/edit-site.ftl");
    }

    /**
     * Shows the add or edit translator page. Either the parent or the
     * translator object must be specified.
     *
     * @param request        the request object
     * @param reference      the parent or translator object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewEditTranslator(Request request, Content reference)
        throws ContentException {

        User               user = request.getUser();
        ContentTranslator  translator;
        String             name;
        int                parent;
        ArrayList          folders = null;
        int                section;
        ArrayList          sections;
        String             comment;
        boolean            publish;
        String             str;

        // Find default values
        AdminUtils.setReference(request, reference);
        if (reference instanceof ContentTranslator) {
            translator = (ContentTranslator) reference;
            name = translator.getName();
            parent = translator.getParentId();
            folders = findFolders(user, findSite(translator), null, false);
            section = translator.getSectionId();
            sections = findSections(user, translator.getDomain(), null);
            if (translator.getRevisionNumber() == 0) {
                comment = translator.getComment();
            } else {
                comment = "";
            }
            publish = translator.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(translator.getParent());
        } else {
            name = "";
            parent = 0;
            section = 0;
            sections = findSections(user, reference.getDomain(), null);
            comment = "Created";
            publish = reference.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(reference);
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
            try {
                str = request.getParameter("section", "0");
                section = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                section = 0;
            }
            comment = request.getParameter("comment", "");
        }

        // Set request parameters
        request.setAttribute("name", name);
        request.setAttribute("parent", String.valueOf(parent));
        request.setAttribute("folders", folders);
        request.setAttribute("section", String.valueOf(section));
        request.setAttribute("sections", sections);
        request.setAttribute("comment", comment);
        request.setAttribute("publish", String.valueOf(publish));
        AdminUtils.sendTemplate(request, "admin/edit-translator.ftl");
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
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    public void viewEditPage(Request request, Content reference)
        throws ContentException, ContentSecurityException {

        User          user = request.getUser();
        ContentPage   page;
        ContentSite   site;
        String        name;
        int           parent;
        ArrayList     folders = null;
        String        template;
        ArrayList     templates;
        String        comment;
        boolean       publish;
        List          localNames;
        LinkedHashMap locals = new LinkedHashMap();
        Iterator      iter;
        String        value;
        String        str;

        // Find default values
        AdminUtils.setReference(request, reference);
        if (reference instanceof ContentPage) {
            page = (ContentPage) reference;
            name = page.getName();
            parent = page.getParentId();
            site = findSite(page);
            folders = findFolders(user, site, null, true);
            template = String.valueOf(page.getTemplateId());
            templates = findTemplates(user, page.getDomain(), null);
            if (page.getRevisionNumber() == 0) {
                comment = page.getComment();
            } else {
                comment = "";
            }
            localNames = page.getLocalElementNames();
            Collections.sort(localNames);
            iter = localNames.iterator();
            while (iter.hasNext()) {
                str = iter.next().toString();
                value = page.getElement(user, str);
                locals.put(str, AdminUtils.getScriptString(value));
            }
            publish = page.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(page.getParent());
        } else {
            name = "";
            parent = 0;
            template = "0";
            templates = findTemplates(user, reference.getDomain(), null);
            comment = "Created";
            publish = reference.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(reference);
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
        request.setAttribute("templates", templates);
        request.setAttribute("comment", comment);
        request.setAttribute("locals", locals);
        request.setAttribute("publish", String.valueOf(publish));
        AdminUtils.sendTemplate(request, "admin/edit-page.ftl");
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
    public void viewEditFile(Request request, Content reference)
        throws ContentException {

        Application    app = AdminUtils.getApplication();
        ContentFile    file;
        ContentSite    site;
        String         name;
        int            parent;
        ArrayList      folders = null;
        String         upload = "";
        String         content = null;
        String         comment;
        boolean        publish;
        FileParameter  param;
        String         str;

        // Find default values
        AdminUtils.setReference(request, reference);
        if (reference instanceof ContentFile) {
            file = (ContentFile) reference;
            name = file.getName();
            parent = file.getParentId();
            site = findSite(file);
            folders = findFolders(request.getUser(), site, null, true);
            content = file.getTextContent(app.getServletContext());
            if (file.getRevisionNumber() == 0) {
                comment = file.getComment();
            } else {
                comment = "";
            }
            publish = file.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(file.getParent());
        } else {
            name = "";
            parent = 0;
            comment = "Created";
            publish = reference.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(reference);
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
            param = request.getFileParameter("upload");
            if (param != null) {
                upload = param.getPath();
            }
            if (request.getParameter("content") != null) {
                content = request.getParameter("content");
            }
            comment = request.getParameter("comment", "");
        }

        // Set request parameters
        request.setAttribute("name", name);
        request.setAttribute("parent", String.valueOf(parent));
        request.setAttribute("folders", folders);
        request.setAttribute("upload", upload);
        if (content != null) {
            request.setAttribute("content", content);
        }
        request.setAttribute("comment", comment);
        request.setAttribute("publish", String.valueOf(publish));
        AdminUtils.sendTemplate(request, "admin/edit-file.ftl");
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
                               PersistentObject parent,
                               ContentFolder folder)
        throws ContentException {

        ContentSite  site;
        String       name;
        int          parentId;
        ArrayList    folders = null;
        String       comment;
        boolean      publish;
        String       str;

        // Find default values
        if (parent != null) {
            AdminUtils.setReference(request, parent);
            name = "";
            parentId = 0;
            comment = "Created";
            publish = parent.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(parent);
        } else {
            AdminUtils.setReference(request, folder);
            name = folder.getName();
            parentId = folder.getParentId();
            site = findSite(folder);
            folders = findFolders(request.getUser(), site, folder, false);
            if (folder.getRevisionNumber() == 0) {
                comment = folder.getComment();
            } else {
                comment = "";
            }
            publish = folder.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(folder.getParent());
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
        request.setAttribute("publish", String.valueOf(publish));
        AdminUtils.sendTemplate(request, "admin/edit-folder.ftl");
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
                                 PersistentObject parent,
                                 ContentTemplate template)
        throws ContentException {

        String        name;
        String        comment;
        List          localNames;
        LinkedHashMap locals = new LinkedHashMap();
        int           inherited;
        ArrayList     templates = null;
        boolean       publish;
        Iterator      iter;
        String        value;
        String        str;

        // Find default values
        if (parent != null) {
            AdminUtils.setReference(request, parent);
            name = "";
            if (parent instanceof Domain) {
                locals.put("root", AdminUtils.getScriptString(""));
                inherited = 0;
            } else {
                inherited = ((Content) parent).getId();
            }
            comment = "Created";
            publish = parent.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(parent);
        } else {
            AdminUtils.setReference(request, template);
            name = template.getName();
            inherited = template.getParentId();
            templates = findTemplates(request.getUser(),
                                      template.getDomain(),
                                      template);
            if (template.getRevisionNumber() == 0) {
                comment = template.getComment();
            } else {
                comment = "";
            }
            localNames = template.getLocalElementNames();
            Collections.sort(localNames);
            iter = localNames.iterator();
            while (iter.hasNext()) {
                str = iter.next().toString();
                value = template.getElement(str);
                locals.put(str, AdminUtils.getScriptString(value));
            }
            publish = template.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(template.getParent());
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
        request.setAttribute("templates", templates);
        request.setAttribute("publish", String.valueOf(publish));
        AdminUtils.sendTemplate(request, "admin/edit-template.ftl");
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

        LinkedHashMap locals = new LinkedHashMap();
        LinkedHashMap inherited = new LinkedHashMap();
        List          names;
        Iterator      iter;
        String        name;

        names = template.getLocalElementNames();
        Collections.sort(names);
        iter = names.iterator();
        while (iter.hasNext()) {
            name = iter.next().toString();
            locals.put(name, template.getElement(name));
        }
        names = template.getAllElementNames();
        Collections.sort(names);
        iter = names.iterator();
        while (iter.hasNext()) {
            name = iter.next().toString();
            if (!locals.containsKey(name)) {
                inherited.put(name, template.getElement(name));
            }
        }
        request.setAttribute("locals", locals);
        request.setAttribute("inherited", inherited);
        AdminUtils.sendTemplate(request, "admin/preview-template.ftl");
    }

    /**
     * Shows the load site object JavaScript code.
     *
     * @param request        the request object
     * @param obj            the domain or content parent object
     * @param open           the open object flag
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewLoadSiteScript(Request request,
                                   PersistentObject obj,
                                   boolean open)
        throws ContentException {

        User     user = request.getUser();
        String   buffer;

        if (obj instanceof Domain) {
            buffer = getTreeScript(user, (Domain) obj, null, false, open);
        } else {
            buffer = getTreeScript(user, null, (Content) obj, false, open);
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
    public void viewOpenSiteScript(Request request, PersistentObject obj)
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
     * @param open           the open object flag
     *
     * @return the JavaScript code for adding the object to the tree
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getTreeScript(User user,
                                 Domain domain,
                                 Content content,
                                 boolean recursive,
                                 boolean open)
        throws ContentException {

        ContentManager  manager = AdminUtils.getContentManager();
        Content[]       children;
        ArrayList       list = new ArrayList();
        StringBuffer    buffer = new StringBuffer();
        String          str;

        if (content == null) {
            children = manager.getContentChildren(user, domain);
            for (int i = 0; i < children.length; i++) {
                if (isViewable(children[i])) {
                    list.add(children[i]);
                }
            }
            children = new Content[list.size()];
            list.toArray(children);
            buffer.append(SCRIPT.getTreeView(domain, children, open));
        } else if (recursive) {
            children = manager.getContentChildren(user, content);
            str = getTreeScript(user, domain, content.getParent(), true, open);
            buffer.append(str);
            buffer.append(SCRIPT.getTreeView(content, children, open));
        } else {
            children = manager.getContentChildren(user, content);
            buffer.append(SCRIPT.getTreeView(content, children, open));
        }
        return buffer.toString();
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
        return request.getSession().getAttribute("site.tree.focus");
    }

    /**
     * Sets the site tree focus object. The focus object is either a
     * domain or a content object, and is stored in the session.
     *
     * @param request        the request
     * @param obj            the new focus object
     */
    public void setSiteTreeFocus(Request request, Object obj) {
        request.getSession().setAttribute("site.tree.focus", obj);
    }

    /**
     * Checks if an object should be shown in the site view.
     *
     * @param obj            the object to check
     *
     * @return true if the object should be shown, or
     *         false otherwise
     */
    private boolean isViewable(PersistentObject obj) {
        return obj instanceof Domain
            || obj instanceof ContentSite
            || obj instanceof ContentFolder
            || obj instanceof ContentFile
            || obj instanceof ContentPage
            || obj instanceof ContentTemplate;
    }
}
