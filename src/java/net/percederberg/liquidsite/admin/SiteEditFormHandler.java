/*
 * SiteEditFormHandler.java
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
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;
import net.percederberg.liquidsite.content.ContentFolder;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.ContentSite;
import net.percederberg.liquidsite.content.ContentTemplate;
import net.percederberg.liquidsite.form.FormValidationException;

/**
 * The site edit request handler. This class handles the edit 
 * workflow for the site view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class SiteEditFormHandler extends AdminFormHandler {

    /**
     * Creates a new site edit request handler.
     */
    public SiteEditFormHandler() {
        super("site.html", "edit-site.html", true);
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

        Object  ref = getReference(request);

        if (ref instanceof ContentSite) {
            SITE_VIEW.pageEditSite(request, (ContentSite) ref);
        } else if (ref instanceof ContentFolder) {
            SITE_VIEW.pageEditFolder(request, null, (ContentFolder) ref);
        } else if (ref instanceof ContentFile) {
            SITE_VIEW.pageEditFile(request, (ContentFile) ref);
        } else if (ref instanceof ContentTemplate) {
            SITE_VIEW.pageEditTemplate(request, null, (ContentTemplate) ref);
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
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     * @throws FormValidationException if the form request data 
     *             validation failed
     */
    protected void validateStep(Request request, int step)
        throws ContentException, ContentSecurityException, 
               FormValidationException {

        Object  ref = getReference(request);
        String  message;

        if (ref instanceof ContentSite) {
            VALIDATOR.validateSite(request);
        } else if (ref instanceof ContentFolder) {
            VALIDATOR.validateFolder(request);
        } else if (ref instanceof ContentFile) {
            VALIDATOR.validateFile(request);
        } else if (ref instanceof ContentTemplate) {
            VALIDATOR.validateTemplate(request);
        } else {
            message = "Cannot edit this object";
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

        Object  ref = getReference(request);

        if (ref instanceof ContentSite) {
            handleEditSite(request, (ContentSite) ref);
        } else if (ref instanceof ContentFolder) {
            handleEditFolder(request, (ContentFolder) ref);
        } else if (ref instanceof ContentFile) {
            handleEditFile(request, (ContentFile) ref);
        } else if (ref instanceof ContentTemplate) {
            handleEditTemplate(request, (ContentTemplate) ref);
        } else {
            throw new ContentException("cannot edit this object");
        }
        return 0;
    }

    /**
     * Handles the edit site form.
     * 
     * @param request        the request object
     * @param site           the site object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleEditSite(Request request, ContentSite site) 
        throws ContentException, ContentSecurityException {

        site.setRevisionNumber(0);
        site.setName(request.getParameter("name"));
        site.setProtocol(request.getParameter("protocol"));
        site.setHost(request.getParameter("host"));
        site.setPort(Integer.parseInt(request.getParameter("port")));
        site.setDirectory(request.getParameter("dir"));
        site.setComment(request.getParameter("comment"));
        site.save(request.getUser());
    }

    /**
     * Handles the edit folder form.
     * 
     * @param request        the request object
     * @param folder         the folder content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleEditFolder(Request request, ContentFolder folder) 
        throws ContentException, ContentSecurityException {

        folder.setRevisionNumber(0);
        folder.setName(request.getParameter("name"));
        folder.setComment(request.getParameter("comment"));
        folder.save(request.getUser());
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

    /**
     * Handles the edit template form.
     * 
     * @param request        the request object
     * @param template       the template content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleEditTemplate(Request request, 
                                    ContentTemplate template) 
        throws ContentException, ContentSecurityException {

        Map              params = request.getAllParameters();
        Iterator         iter = params.keySet().iterator();
        String           name;
        String           value;
        int              parent;

        template.setRevisionNumber(0);
        template.setName(request.getParameter("name"));
        template.setComment(request.getParameter("comment"));
        try {
            parent = Integer.parseInt(request.getParameter("parent"));
            template.setParentId(parent);
        } catch (NumberFormatException ignore) {
            // This is ignored
        }
        while (iter.hasNext()) {
            name = iter.next().toString();
            if (name.startsWith("element.")) {
                value = params.get(name).toString();
                template.setElement(name.substring(8), value);
            }
        }
        iter = template.getLocalElementNames().iterator();
        while (iter.hasNext()) {
            name = iter.next().toString();
            if (!params.containsKey("element." + name)) {
                template.setElement(name, null);
            }
        }
        template.save(request.getUser());
    }
}
