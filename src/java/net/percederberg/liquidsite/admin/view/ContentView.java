/*
 * ContentView.java
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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.admin.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.percederberg.liquidsite.admin.AdminUtils;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.DocumentProperty;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.PersistentObject;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.web.Request;
import net.percederberg.liquidsite.web.Request.FileParameter;

/**
 * A helper class for the content view. This class contains methods
 * for creating the HTML and JavaScript responses to the various
 * pages in the content view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentView extends AdminView {

    /**
     * Creates a new content view helper.
     */
    ContentView() {
    }

    /**
     * Shows the content page.
     *
     * @param request        the request object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    public void viewContent(Request request)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        Object          focus = getContentTreeFocus(request);
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
                str = getContentTreeScript(user,
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
        request.sendTemplate("admin/content.ftl");
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

        User  user = request.getUser();

        AdminUtils.setReference(request, parent);
        if (parent instanceof Domain) {
            if (parent.hasWriteAccess(user)) {
                request.setAttribute("enableSection", true);
            }
        }
        if (parent instanceof ContentSection) {
            if (parent.hasWriteAccess(user)) {
                request.setAttribute("enableSection", true);
                request.setAttribute("enableDocument", true);
            }
        }
        if (parent instanceof ContentDocument) {
            if (parent.hasWriteAccess(user)) {
                request.setAttribute("enableFile", true);
            }
        }
        request.sendTemplate("admin/add-object.ftl");
    }

    /**
     * Shows the add or edit section page. Either the parent or the
     * section object must be specified.
     *
     * @param request        the request object
     * @param parent         the parent object, or null
     * @param section        the section object, or null
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewEditSection(Request request,
                                PersistentObject parent,
                                ContentSection section)
        throws ContentException {

        String            name;
        String            comment;
        ArrayList         parents = null;
        int               parentId = 0;
        ArrayList         properties = new ArrayList();
        DocumentProperty  property;
        boolean           publish;
        HashMap           map;
        int               position;
        Iterator          iter;
        String            param;
        String            str;

        // Find default values
        if (parent != null) {
            AdminUtils.setReference(request, parent);
            name = "";
            comment = "Created";
            publish = parent.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(parent);
        } else {
            AdminUtils.setReference(request, section);
            name = section.getName();
            parents = findSections(request.getUser(),
                                   section.getDomain(),
                                   section);
            parentId = section.getParentId();
            if (section.getRevisionNumber() == 0) {
                comment = section.getComment();
            } else {
                comment = "";
            }
            iter = findSectionProperties(section, false).iterator();
            while (iter.hasNext()) {
                property = (DocumentProperty) iter.next();
                map = new HashMap();
                map.put("id", property.getId());
                str = property.getName();
                map.put("name", AdminUtils.getScriptString(str));
                map.put("type", "" + property.getType());
                str = property.getDescription();
                map.put("description", AdminUtils.getScriptString(str));
                properties.add(map);
            }
            publish = section.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(section.getParent());
        }

        // Adjust for incoming request
        if (request.getParameter("name") != null) {
            name = request.getParameter("name", "");
            str = request.getParameter("parent", "0");
            try {
                parentId = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                parentId = 0;
            }
            comment = request.getParameter("comment", "");
            properties.clear();
            iter = request.getAllParameters().keySet().iterator();
            while (iter.hasNext()) {
                param = iter.next().toString();
                if (param.startsWith("property.")
                 && param.endsWith(".position")) {

                    param = param.substring(0, param.length() - 9);
                    map = new HashMap();
                    map.put("id", param.substring(9));
                    str = request.getParameter(param + ".name");
                    map.put("name", AdminUtils.getScriptString(str));
                    str = request.getParameter(param + ".type");
                    map.put("type", str);
                    str = request.getParameter(param + ".description");
                    map.put("description", AdminUtils.getScriptString(str));
                    str = request.getParameter(param + ".position");
                    position = Integer.parseInt(str);
                    while (position >= properties.size()) {
                        properties.add(null);
                    }
                    properties.set(position, map);
                }
            }
        }

        // Set request parameters
        request.setAttribute("name", name);
        request.setAttribute("parents", parents);
        request.setAttribute("parent", String.valueOf(parentId));
        request.setAttribute("properties", properties);
        request.setAttribute("comment", comment);
        request.setAttribute("publish", String.valueOf(publish));
        request.sendTemplate("admin/edit-section.ftl");
    }

    /**
     * Shows the add or edit document page. Either the parent or the
     * document object must be specified.
     *
     * @param request        the request object
     * @param reference      the parent or document object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewEditDocument(Request request,
                                 Content reference)
        throws ContentException {

        ContentDocument   doc;
        String            name;
        String            comment;
        int               section;
        ArrayList         sections;
        ArrayList         properties = new ArrayList();
        HashMap           data = new HashMap();
        DocumentProperty  property;
        boolean           publish;
        String            str;
        int               i;

        // Find default values
        AdminUtils.setReference(request, reference);
        if (reference instanceof ContentDocument) {
            doc = (ContentDocument) reference;
            name = doc.getName();
            section = doc.getParentId();
            sections = findSections(request.getUser(),
                                    doc.getDomain(),
                                    null);
            properties = findSectionProperties(doc);
            for (i = 0; i < properties.size(); i++) {
                property = (DocumentProperty) properties.get(i);
                str = doc.getProperty(property.getId());
                if (property.getType() == DocumentProperty.HTML_TYPE) {
                    str = AdminUtils.getScriptString(str);
                }
                data.put(property.getId(), str);
            }
            if (doc.getRevisionNumber() == 0) {
                comment = doc.getComment();
            } else {
                comment = "";
            }
            publish = doc.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(doc.getParent());
        } else {
            name = "";
            section = 0;
            sections = new ArrayList(0);
            properties = findSectionProperties(reference);
            for (i = 0; i < properties.size(); i++) {
                property = (DocumentProperty) properties.get(i);
                str = "";
                if (property.getType() == DocumentProperty.HTML_TYPE) {
                    str = AdminUtils.getScriptString(str);
                }
                data.put(property.getId(), str);
            }
            comment = "Created";
            publish = reference.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(reference);
        }

        // Adjust for incoming request
        if (request.getParameter("name") != null) {
            name = request.getParameter("name", "");
            str = request.getParameter("section", "0");
            try {
                section = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                section = 0;
            }
            comment = request.getParameter("comment", "");
            data.clear();
            for (i = 0; i < properties.size(); i++) {
                property = (DocumentProperty) properties.get(i);
                str = request.getParameter("property." + property.getId());;
                if (property.getType() == DocumentProperty.HTML_TYPE) {
                    str = AdminUtils.getScriptString(str);
                }
                data.put(property.getId(), str);
            }
        }

        // Set request parameters
        request.setAttribute("name", name);
        request.setAttribute("section", String.valueOf(section));
        request.setAttribute("sections", sections);
        request.setAttribute("properties", properties);
        request.setAttribute("data", data);
        request.setAttribute("comment", comment);
        request.setAttribute("publish", String.valueOf(publish));
        if (request.getParameter("liquidsite.startpage") != null) {
            request.setAttribute("startpage", 
                                 request.getParameter("liquidsite.startpage"));
        }
        request.sendTemplate("admin/edit-document.ftl");
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
    public void viewEditFile(Request request, PersistentObject reference)
        throws ContentException {

        ContentFile    file;
        String         name;
        String         upload = "";
        String         content = null;
        String         comment;
        boolean        publish;
        FileParameter  param;

        // Find default values
        AdminUtils.setReference(request, reference);
        if (reference instanceof ContentFile) {
            file = (ContentFile) reference;
            name = file.getName();
            content = file.getTextContent();
            if (file.getRevisionNumber() == 0) {
                comment = file.getComment();
            } else {
                comment = "";
            }
            publish = file.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(file.getParent());
        } else {
            name = "";
            comment = "Created";
            publish = reference.hasPublishAccess(request.getUser()) &&
                      AdminUtils.isOnline(reference);
        }

        // Adjust for incoming request
        if (request.getParameter("name") != null) {
            name = request.getParameter("name", "");
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
        request.setAttribute("upload", upload);
        if (content != null) {
            request.setAttribute("content", content);
        }
        request.setAttribute("comment", comment);
        request.setAttribute("publish", String.valueOf(publish));
        request.sendTemplate("admin/edit-file.ftl");
    }

    /**
     * Shows the section preview page.
     *
     * @param request        the request object
     * @param section        the section object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewSectionPreview(Request request, ContentSection section)
        throws ContentException {

        request.setAttribute("properties", findSectionProperties(section));
        request.sendTemplate("admin/preview-section.ftl");
    }

    /**
     * Shows the document preview page.
     *
     * @param request        the request object
     * @param doc            the document object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewDocumentPreview(Request request, ContentDocument doc)
        throws ContentException {

        request.getEnvironment().setDocument(doc);
        request.setAttribute("properties", findSectionProperties(doc));
        request.sendTemplate("admin/preview-document.ftl");
    }

    /**
     * Shows the load content object JavaScript code.
     *
     * @param request        the request object
     * @param obj            the domain or content parent object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewLoadContentScript(Request request, PersistentObject obj)
        throws ContentException {

        User     user = request.getUser();
        Content  content;
        String   buffer;

        if (obj instanceof Domain) {
            buffer = getContentTreeScript(user, (Domain) obj, null, false);
        } else {
            content = (Content) obj;
            buffer = getContentTreeScript(user, null, content, false);
        }
        request.sendData("text/javascript", buffer);
    }

    /**
     * Shows the open content object JavaScript code.
     *
     * @param request        the request object
     * @param obj            the domain or content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewOpenContentScript(Request request, PersistentObject obj)
        throws ContentException {

        User     user = request.getUser();
        Content  content;
        String   buffer;

        if (obj instanceof Domain) {
            buffer = SCRIPT.getObjectView(user, (Domain) obj, "content");
            setContentTreeFocus(request, obj);
        } else {
            content = (Content) obj;
            buffer = SCRIPT.getObjectView(user, content, "content");
            setContentTreeFocus(request, content);
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
    private String getContentTreeScript(User user,
                                        Domain domain,
                                        Content content,
                                        boolean recursive)
        throws ContentException {

        ContentManager  manager = AdminUtils.getContentManager();
        Content[]       children;
        Content         parent;
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
            parent = content.getParent();
            return getContentTreeScript(user, domain, parent, true)
                 + SCRIPT.getTreeView(content, children);
        } else {
            children = manager.getContentChildren(user, content);
            return SCRIPT.getTreeView(content, children);
        }
    }

    /**
     * Returns the content tree focus object. The focus object is
     * either a domain or a content object, and is stored in the
     * session. It is not possible to trust the focus object for any
     * operations. It may have been removed from the database by
     * another user.
     *
     * @param request        the request
     *
     * @return the content view focus object, or
     *         null for none
     */
    public Object getContentTreeFocus(Request request) {
        return request.getSessionAttribute("content.tree.focus");
    }

    /**
     * Sets the content tree focus object. The focus object is either
     * a domain or a content object, and is stored in the session.
     *
     * @param request        the request
     * @param obj            the new focus object
     */
    public void setContentTreeFocus(Request request, Object obj) {
        request.setSessionAttribute("content.tree.focus", obj);
    }

    /**
     * Checks if an object should be shown in the content view.
     *
     * @param obj            the object to check
     *
     * @return true if the object should be shown, or
     *         false otherwise
     */
    private boolean isViewable(PersistentObject obj) {
        return obj instanceof Domain
            || obj instanceof ContentSection;
    }

    /**
     * Finds all content document properties and adds them to a list.
     * This method will retrieve the document properties from the
     * first content section having any.
     *
     * @param content        the content object
     *
     * @return the list of properties found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private ArrayList findSectionProperties(Content content)
        throws ContentException {

        if (content == null) {
            return new ArrayList(0);
        } else if (content instanceof ContentSection) {
            return findSectionProperties((ContentSection) content, true);
        } else {
            return findSectionProperties(content.getParent());
        }
    }

    /**
     * Finds all content document properties and adds them to a list.
     * This method may retrieve the document properties from the
     * parent section if the recurse flag is set and the section does
     * not have any properties.
     *
     * @param section        the content section
     * @param recurse        the recursive lookup flag
     *
     * @return the list of properties found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private ArrayList findSectionProperties(ContentSection section,
                                            boolean recurse)
        throws ContentException {

        DocumentProperty[]  properties;
        ArrayList           result;

        properties = section.getAllDocumentProperties();
        if (recurse && properties.length <= 0) {
            return findSectionProperties(section.getParent());
        }
        result = new ArrayList();
        for (int i = 0; i < properties.length; i++) {
            result.add(properties[i]);
        }
        return result;
    }
}
