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

import java.util.Iterator;
import java.util.Map;

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.admin.view.AdminView;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.content.ContentSecurityException;
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
        String                  message;

        if (step == 1) {
            if (category.equals("")) {
                message = "No content category specified";
                throw new FormValidationException("category", message);
            }
        } else {
            edit.validateStep(request, step);
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

        ContentSection  section;
        Map             params = request.getAllParameters();
        Iterator        iter = params.keySet().iterator();
        String          name;
        String          value;
        
        if (parent instanceof Domain) {
            section = new ContentSection((Domain) parent);
        } else {
            section = new ContentSection((ContentSection) parent);
        }
        section.setName(request.getParameter("name"));
        section.setComment(request.getParameter("comment"));
/* TODO: set document properties
        while (iter.hasNext()) {
            name = iter.next().toString();
            if (name.startsWith("element.")) {
                value = params.get(name).toString();
                template.setElement(name.substring(8), value);
            }
        }
*/
        section.save(request.getUser());
        AdminView.CONTENT.setContentTreeFocus(request, section);
    }
}