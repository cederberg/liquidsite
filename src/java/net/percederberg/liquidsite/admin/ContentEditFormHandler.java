/*
 * ContentEditFormHandler.java
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
import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.DocumentProperty;
import net.percederberg.liquidsite.form.FormValidationException;
import net.percederberg.liquidsite.form.FormValidator;

/**
 * The content edit request handler. This class handles the edit 
 * workflow for the content view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentEditFormHandler extends AdminFormHandler {

    /***
     * The latest object instance created.
     */
    private static ContentEditFormHandler instance = null;

    /**
     * The section form validator.
     */
    private FormValidator section = new FormValidator();

    /**
     * The document form validator.
     */
    private FormValidator document = new FormValidator();

    /**
     * Returns an instance of this class. If a prior instance has 
     * been created, it will be returned instead of creating a new
     * one. 
     * 
     * @return an instance of a content edit form handler
     */
    public static ContentEditFormHandler getInstance() {
        if (instance == null) {
            return new ContentEditFormHandler();
        } else {
            return instance;
        }
    }

    /**
     * Creates a new content edit request handler.
     */
    public ContentEditFormHandler() {
        super("content.html", "edit-content.html", true);
        instance = this;
        initialize();
    }

    /**
     * Initializes all the form validators.
     */
    private void initialize() {
        String  upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String  lowerCase = "abcdefghijklmonpqrstuvwxyz";
        String  numbers = "0123456789";
        String  nameChars = upperCase + lowerCase + numbers + ".-_";
        String  error;
        
        // Add and edit section validator
        section.addRequiredConstraint("name", "No section name specified");
        error = "Section name contains invalid character";
        section.addCharacterConstraint("name", nameChars, error);
        error = "No revision comment specified";
        section.addRequiredConstraint("comment", error);

        // Add and edit document validator
        document.addRequiredConstraint("name", "No document name specified");
        error = "Document name contains invalid character";
        document.addCharacterConstraint("name", nameChars, error);
        error = "No revision comment specified";
        document.addRequiredConstraint("comment", error);
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

        Object  ref = AdminUtils.getReference(request);

        if (ref instanceof ContentSection) {
            AdminView.CONTENT.viewEditSection(request, 
                                              null, 
                                              (ContentSection) ref);
        } else if (ref instanceof ContentDocument) {
            AdminView.CONTENT.viewEditDocument(request, 
                                               (ContentDocument) ref);
        } else if (ref instanceof ContentFile) {
            AdminView.CONTENT.viewEditFile(request, (ContentFile) ref);
        } else {
            throw new ContentException("cannot edit this object");
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

        String  category = request.getParameter("category", "");
        String  message;

        if (category.equals("section")) {
            section.validate(request);
        } else if (category.equals("document")) {
            document.validate(request);
        } else if (category.equals("file")) {
            SiteEditFormHandler.getInstance().validateStep(request, step); 
        } else {
            message = "Unknown content category specified";
            throw new FormValidationException("category", message);
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

        Object  ref = AdminUtils.getReference(request);

        if (ref instanceof ContentSection) {
            handleEditSection(request, (ContentSection) ref);
        } else if (ref instanceof ContentDocument) {
            handleEditDocument(request, (ContentDocument) ref);
        } else if (ref instanceof ContentFile) {
            handleEditFile(request, (ContentFile) ref);
        } else {
            throw new ContentException("cannot edit this object");
        }
        return 0;
    }

    /**
     * Handles the edit section form.
     * 
     * @param request        the request object
     * @param section        the section content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleEditSection(Request request, ContentSection section) 
        throws ContentException, ContentSecurityException {

        Map                 params = request.getAllParameters();
        Iterator            iter = params.keySet().iterator();
        DocumentProperty[]  properties;
        DocumentProperty    property;
        String              name;
        String              id;
        String              str;
        int                 parent;

        section.setRevisionNumber(0);
        section.setName(request.getParameter("name"));
        section.setComment(request.getParameter("comment"));
        try {
            parent = Integer.parseInt(request.getParameter("parent"));
            section.setParentId(parent);
        } catch (NumberFormatException ignore) {
            // This is ignored
        }
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
        properties = section.getAllDocumentProperties();
        for (int i = 0; i < properties.length; i++) {
            id = properties[i].getId();
            if (!params.containsKey("property." + id + ".position")) {
                section.setDocumentProperty(id, null);
            }
        }
        section.save(request.getUser());
    }

    /**
     * Handles the edit document form.
     * 
     * @param request        the request object
     * @param doc            the document content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleEditDocument(Request request, ContentDocument doc) 
        throws ContentException, ContentSecurityException {

        Map       params = request.getAllParameters();
        Iterator  iter = params.keySet().iterator();
        int       section;
        String    id;
        int       type;
        String    str;

        doc.setRevisionNumber(0);
        doc.setName(request.getParameter("name"));
        doc.setComment(request.getParameter("comment"));
        try {
            section = Integer.parseInt(request.getParameter("section"));
            doc.setParentId(section);
        } catch (NumberFormatException ignore) {
            // This is ignored
        }
        while (iter.hasNext()) {
            str = iter.next().toString();
            if (str.startsWith("property.")) {
                id = str.substring(9);
                str = request.getParameter("property." + id);
                doc.setProperty(id, str);
                try {
                    str = request.getParameter("propertytype." + id);
                    type = Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    type = DocumentProperty.STRING_TYPE;
                }
                doc.setPropertyType(id, type);
            }
        }
        iter = doc.getPropertyIdentifiers().iterator();
        while (iter.hasNext()) {
            id = iter.next().toString();
            if (!params.containsKey("property." + id)) {
                doc.setProperty(id, null);
            }
        }
        doc.save(request.getUser());
    }

    /**
     * Handles the edit file form.
     * 
     * @param request        the request object
     * @param file           the file content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleEditFile(Request request, ContentFile file) 
        throws ContentException, ContentSecurityException {

        FileParameter  param;
        
        try {
            file.setRevisionNumber(0);
            file.setName(request.getParameter("name"));
            file.setComment(request.getParameter("comment"));
            param = request.getFileParameter("content");
            if (param != null && param.getSize() > 0) {
                file.setFileName(param.getName());
                param.write(file.getFile());
            }
            file.save(request.getUser());
        } catch (IOException e) {
            throw new ContentException(e.getMessage());
        }
    }
}
