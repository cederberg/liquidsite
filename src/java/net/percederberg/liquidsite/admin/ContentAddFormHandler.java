/*
 * ContentAddFormHandler.java
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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.Request.FileParameter;
import net.percederberg.liquidsite.admin.view.AdminView;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.DocumentProperty;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.form.FormValidationException;

/**
 * The content add request handler. This class handles the add 
 * workflow for the content view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentAddFormHandler extends AdminFormHandler {

    /**
     * Creates a new content add request handler.
     */
    public ContentAddFormHandler() {
        super("content.html", "add-content.html", false);
    }

    /**
     * Displays a form for the specified workflow step. This method
     * will NOT be called when returning to the start page.
     * 
     * @param request        the request object
     * @param step           the workflow step
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    protected void displayStep(Request request, int step)
        throws ContentException, ContentSecurityException {

        String  category = request.getParameter("category", "");
        Object  parent = AdminUtils.getReference(request);

        if (step == 1) {
            AdminView.CONTENT.viewAddObject(request, parent);
        } else if (category.equals("section")) {
            AdminView.CONTENT.viewEditSection(request, parent, null);
        } else if (category.equals("document")) {
            AdminView.CONTENT.viewEditDocument(request, (Content) parent);
        } else if (category.equals("file")) {
            AdminView.CONTENT.viewEditFile(request, (Content) parent);
        } else {
            AdminView.CONTENT.viewAddObject(request, parent);
        }
    }

    /**
     * Validates a form for the specified workflow step. If the form
     * validation fails in this step, the form page for the workflow 
     * step will be displayed again with an 'error' attribute 
     * containing the message in the validation exception.
     * 
     * @param request        the request object
     * @param step           the workflow step
     * 
     * @throws FormValidationException if the form request data 
     *             validation failed
     */
    protected void validateStep(Request request, int step)
        throws FormValidationException {

        ContentEditFormHandler  edit = ContentEditFormHandler.getInstance(); 
        String                  category = request.getParameter("category", "");
        FileParameter           param;
        String                  message;

        if (step == 1) {
            if (category.equals("")) {
                message = "No content category specified";
                throw new FormValidationException("category", message);
            }
        } else {
            edit.validateStep(request, step);
            if (category.equals("file")) {
                param = request.getFileParameter("content");
                if (param == null || param.getSize() <= 0) {
                    message = "No file content specified"; 
                    throw new FormValidationException("content", message);
                }
            }
        }
    }

    /**
     * Handles a validated form for the specified workflow step. This
     * method returns the next workflow step, i.e. the step used when
     * calling the display method. If the special zero (0) workflow 
     * step is returned, the workflow is assumed to have terminated.
     * Note that this method also allows additional validation to 
     * occur. By returning the incoming workflow step number and 
     * setting the appropriate request attributes the same results as
     * in the normal validate method can be achieved. For recoverable
     * errors, this is the recommended course of action.
     *  
     * @param request        the request object
     * @param step           the workflow step
     * 
     * @return the next workflow step, or 
     *         zero (0) if the workflow has finished
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    protected int handleStep(Request request, int step) 
        throws ContentException, ContentSecurityException {

        String  category = request.getParameter("category", "");
        Object  parent = AdminUtils.getReference(request);

        if (step == 1) {
            return 2;
        } else if (category.equals("section")) {
            handleAddSection(request, parent);
        } else if (category.equals("document")) {
            handleAddDocument(request, (ContentSection) parent);
        } else if (category.equals("file")) {
            handleAddFile(request, (ContentDocument) parent);
        }
        return 0;
    }

    /**
     * Handles the add section form.
     * 
     * @param request        the request object
     * @param parent         the parent domain or section object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleAddSection(Request request, Object parent) 
        throws ContentException, ContentSecurityException {

        ContentManager    manager = AdminUtils.getContentManager();
        ContentSection    section;
        Map               params = request.getAllParameters();
        Iterator          iter = params.keySet().iterator();
        DocumentProperty  property;
        String            name;
        String            id;
        String            str;

        if (parent instanceof Domain) {
            section = new ContentSection(manager, (Domain) parent);
        } else {
            section = new ContentSection(manager, (ContentSection) parent);
        }
        section.setName(request.getParameter("name"));
        section.setComment(request.getParameter("comment"));
        while (iter.hasNext()) {
            name = iter.next().toString();
            if (name.startsWith("property.") && name.endsWith(".position")) {
                name = name.substring(0, name.length() - 9);
                id = name.substring(9);
                property = new DocumentProperty(id);
                str = request.getParameter(name + ".name", id);
                property.setName(str);
                try {
                    str = request.getParameter(name + ".position", "1");
                    property.setPosition(Integer.parseInt(str));
                } catch (NumberFormatException ignore) {
                    // This is ignored
                }
                try {
                    str = request.getParameter(name + ".type", "1");
                    property.setType(Integer.parseInt(str));
                } catch (NumberFormatException ignore) {
                    // This is ignored
                }
                str = request.getParameter(name + ".description", "");
                property.setDescription(str);
                section.setDocumentProperty(id, property);
            }
        }
        section.save(request.getUser());
        AdminView.CONTENT.setContentTreeFocus(request, section);
    }

    /**
     * Handles the add document form.
     * 
     * @param request        the request object
     * @param parent         the parent section object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleAddDocument(Request request, ContentSection parent) 
        throws ContentException, ContentSecurityException {

        ContentManager   manager = AdminUtils.getContentManager();
        ContentDocument  doc;
        Map              params = request.getAllParameters();
        Iterator         iter = params.keySet().iterator();
        String           id;
        int              type;
        String           str;

        doc = new ContentDocument(manager, parent);
        doc.setName(request.getParameter("name"));
        doc.setComment(request.getParameter("comment"));
        while (iter.hasNext()) {
            str = iter.next().toString();
            if (str.startsWith("property.")) {
                id = str.substring(9);
                try {
                    str = request.getParameter("propertytype." + id);
                    type = Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    type = DocumentProperty.STRING_TYPE;
                }
                doc.setPropertyType(id, type);
                str = request.getParameter("property." + id);
                if (type == DocumentProperty.HTML_TYPE) {
                    str = AdminUtils.cleanHtml(str);
                }
                doc.setProperty(id, str);
            }
        }
        doc.save(request.getUser());
        AdminView.CONTENT.setContentTreeFocus(request, doc);
    }

    /**
     * Handles the add file form.
     * 
     * @param request        the request object
     * @param parent         the parent document object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleAddFile(Request request, ContentDocument parent) 
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        FileParameter   param;
        ContentFile     file;
        
        try {
            param = request.getFileParameter("content");
            file = new ContentFile(manager, parent, param.getName());
            file.setName(request.getParameter("name"));
            file.setComment(request.getParameter("comment"));
            file.save(request.getUser());
            param.write(file.getFile());
            AdminView.CONTENT.setContentTreeFocus(request, file);
        } catch (IOException e) {
            throw new ContentException(e.getMessage());
        }
    }
}
