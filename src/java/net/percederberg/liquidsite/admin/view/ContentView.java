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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.admin.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.admin.AdminUtils;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.DocumentProperty;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.User;

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
    public void viewAddObject(Request request, Object parent) 
        throws ContentException {

        User     user = request.getUser();
        Domain   domain;
        Content  content;

        AdminUtils.setReference(request, parent);
        if (parent instanceof Domain) {
            domain = (Domain) parent;
            if (domain.hasWriteAccess(user)) {
                request.setAttribute("enableSection", true);
            }
        }
        if (parent instanceof ContentSection) {
            content = (Content) parent;
            if (content.hasWriteAccess(user)) {
                request.setAttribute("enableSection", true);
                request.setAttribute("enableDocument", true);
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
                                Object parent, 
                                ContentSection section) 
        throws ContentException {

        String            name;
        String            comment;
        ArrayList         parents = null;
        int               parentId = 0;
        ArrayList         properties = new ArrayList();
        DocumentProperty  property;
        Iterator          iter;
        String            param;
        String            str;

        // Find default values
        if (parent != null) {
            AdminUtils.setReference(request, parent);
            name = "";
            comment = "Created";
        } else {
            AdminUtils.setReference(request, section);
            name = section.getName();
            parents = findSections(request.getUser(), 
                                   section.getDomain(), 
                                   section);
            parentId = section.getParentId();
            comment = "";
            properties = findSectionProperties(section, false);
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
                    property = new DocumentProperty(param.substring(9));
                    str = request.getParameter(param + ".name");
                    property.setName(str);
                    str = request.getParameter(param + ".position");
                    property.setPosition(Integer.parseInt(str));
                    str = request.getParameter(param + ".type");
                    property.setType(Integer.parseInt(str));
                    str = request.getParameter(param + ".description");
                    property.setDescription(str);
                    properties.add(property);
                }
            }
            Collections.sort(properties);
        }

        // Set request parameters
        request.setAttribute("name", name);
        request.setAttribute("parents", parents);
        request.setAttribute("parent", String.valueOf(parentId));
        request.setAttribute("properties", properties);
        request.setAttribute("comment", comment);
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
        Iterator          iter;
        String            param;
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
                str = property.getId();
                data.put(str, doc.getProperty(str));
            }
            comment = "";
        } else {
            name = "";
            section = 0;
            sections = new ArrayList(0);
            properties = findSectionProperties(reference);
            for (i = 0; i < properties.size(); i++) {
                property = (DocumentProperty) properties.get(i);
                data.put(property.getId(), "");
            }
            comment = "Created";
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
            iter = request.getAllParameters().keySet().iterator();
            while (iter.hasNext()) {
                param = iter.next().toString();
                if (param.startsWith("property.")) {
                    data.put(param.substring(9), 
                             request.getParameter(param));
                }
            }
        }

        // Set request parameters
        request.setAttribute("name", name);
        request.setAttribute("section", String.valueOf(section));
        request.setAttribute("sections", sections);
        request.setAttribute("properties", properties);
        request.setAttribute("data", data);
        request.setAttribute("comment", comment);
        request.sendTemplate("admin/edit-document.ftl");
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
    public void viewLoadContentScript(Request request, Object obj) 
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
    public void viewOpenContentScript(Request request, Object obj) 
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
    private boolean isViewable(Object obj) {
        return obj instanceof Domain
            || obj instanceof ContentSection;
    }
    
    /**
     * Finds all content sections in a domain. The sections will not
     * be added directly to the result list, but rather a simplified
     * hash map containing only the id and name of each section will 
     * be added.  
     * 
     * @param user           the user
     * @param domain         the domain
     * @param exclude        the section to exclude, or null
     * 
     * @return the list of sections found (in maps)
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private ArrayList findSections(User user,
                                   Domain domain, 
                                   ContentSection exclude)
        throws ContentException {

        ArrayList  result = new ArrayList();
        
        findSections(user, "", domain, exclude, result);
        return result;
    }

    /**
     * Finds all content sections in a domain. The sections will not
     * be added directly to the result list, but rather a simplified
     * hash map containing only the id and name of each section will 
     * be added.  
     * 
     * @param user           the user
     * @param baseName       the base name
     * @param parent         the parent domain or content object
     * @param exclude        the section to exclude, or null
     * @param result         the list of sections found (in maps)
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void findSections(User user, 
                              String baseName,
                              Object parent,
                              ContentSection exclude,
                              ArrayList result)
        throws ContentException {

        ContentManager  manager = AdminUtils.getContentManager();
        Content[]       children;
        HashMap         values;
        String          name;

        if (parent instanceof Domain) {
            children = manager.getContentChildren(user, (Domain) parent);
        } else {
            children = manager.getContentChildren(user, (Content) parent);
        }
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof ContentSection
             && !children[i].equals(exclude)) {

                values = new HashMap(2);
                values.put("id", String.valueOf(children[i].getId()));
                name = baseName + children[i].getName();
                values.put("name", name);
                result.add(values);
                name += "/";
                findSections(user, name, children[i], exclude, result);
            }
        }
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
