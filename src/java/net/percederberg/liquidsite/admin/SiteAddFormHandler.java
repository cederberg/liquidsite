/*
 * SiteAddFormHandler.java
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
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;
import net.percederberg.liquidsite.content.ContentFolder;
import net.percederberg.liquidsite.content.ContentPage;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.ContentSite;
import net.percederberg.liquidsite.content.ContentTemplate;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Host;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.form.FormValidationException;

/**
 * The site add request handler. This class handles the add workflow 
 * for the site view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class SiteAddFormHandler extends AdminFormHandler {

    /**
     * Creates a new site add request handler.
     */
    public SiteAddFormHandler() {
        super("site.html", "add-site.html", false);
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
        Object  parent = getReference(request);

        if (step == 1) {
            SITE_VIEW.pageAddObject(request, parent);
        } else if (category.equals("domain")) {
            SITE_VIEW.pageAddDomain(request, parent);
        } else if (category.equals("site")) {
            SITE_VIEW.pageEditSite(request, parent);
        } else if (category.equals("folder")) {
            SITE_VIEW.pageEditFolder(request, parent, null);
        } else if (category.equals("page")) {
            SITE_VIEW.pageEditPage(request, parent);
        } else if (category.equals("file")) {
            SITE_VIEW.pageEditFile(request, parent);
        } else if (category.equals("template")) {
            SITE_VIEW.pageEditTemplate(request, parent, null);
        } else {
            SITE_VIEW.pageAddObject(request, parent);
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

        String         category = request.getParameter("category", "");
        FileParameter  param;
        String         message;

        if (category.equals("domain")) {
            if (step == 2) {
                VALIDATOR.validateAddDomain(request);
            }
        } else if (category.equals("site")) {
            if (step == 2) {
                VALIDATOR.validateSite(request);
            }
        } else if (category.equals("folder")) {
            if (step == 2) {
                VALIDATOR.validateFolder(request);
            }
        } else if (category.equals("page")) {
            if (step == 2) {
                VALIDATOR.validatePage(request);
            }
        } else if (category.equals("file")) {
            if (step == 2) {
                VALIDATOR.validateFile(request);
                param = request.getFileParameter("content");
                if (param == null || param.getSize() <= 0) {
                    message = "No file content specified"; 
                    throw new FormValidationException("content", message);
                }
            }
        } else if (category.equals("template")) {
            if (step == 2) {
                VALIDATOR.validateTemplate(request);
            }
        } else {
            message = "No content category specified";
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

        String  category = request.getParameter("category", "");
        Object  parent = getReference(request);

        if (step == 1) {
            return 2;
        } else if (category.equals("domain")) {
            handleAddDomain(request, (Domain) parent);
        } else if (category.equals("site")) {
            handleAddSite(request, (Domain) parent);
        } else if (category.equals("folder")) {
            handleAddFolder(request, (Content) parent);
        } else if (category.equals("page")) {
            handleAddPage(request, (Content) parent);
        } else if (category.equals("file")) {
            handleAddFile(request, (Content) parent);
        } else if (category.equals("template")) {
            handleAddTemplate(request, parent);
        }
        return 0;
    }

    /**
     * Handles the add domain form. The parent domain object must be
     * specified in case the uses chooses to go back to the 
     * previous step.
     * 
     * @param request        the request object
     * @param parent         the parent domain object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleAddDomain(Request request, Domain parent) 
        throws ContentException, ContentSecurityException {

        Domain  domain;
        Host    host;
        
        domain = new Domain(request.getParameter("name"));
        domain.setDescription(request.getParameter("description"));
        domain.save(request.getUser());
        host = new Host(domain, request.getParameter("host"));
        host.setDescription("Default domain host");
        host.save(request.getUser());
        SITE_VIEW.setSiteTreeFocus(request, domain);
    }

    /**
     * Handles the add site form.
     * 
     * @param request        the request object
     * @param parent         the parent domain object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleAddSite(Request request, Domain parent) 
        throws ContentException, ContentSecurityException {

        User         user = request.getUser();
        ContentSite  site;
        
        site = new ContentSite(parent);
        site.setName(request.getParameter("name"));
        site.setProtocol(request.getParameter("protocol"));
        site.setHost(request.getParameter("host"));
        site.setPort(Integer.parseInt(request.getParameter("port")));
        site.setDirectory(request.getParameter("dir"));
        site.setAdmin(request.getParameter("admin", "").equals("true"));
        site.setComment(request.getParameter("comment"));
        site.save(user);
        SITE_VIEW.setSiteTreeFocus(request, site);
    }

    /**
     * Handles the add folder form.
     * 
     * @param request        the request object
     * @param parent         the parent content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleAddFolder(Request request, Content parent) 
        throws ContentException, ContentSecurityException {

        User           user = request.getUser();
        ContentFolder  folder;
        
        folder = new ContentFolder(parent);
        folder.setName(request.getParameter("name"));
        folder.setComment(request.getParameter("comment"));
        folder.save(user);
        SITE_VIEW.setSiteTreeFocus(request, folder);
    }

    /**
     * Handles the add page form.
     * 
     * @param request        the request object
     * @param parent         the parent content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleAddPage(Request request, Content parent) 
        throws ContentException, ContentSecurityException {

        ContentPage  page;
        Map          params = request.getAllParameters();
        Iterator     iter = params.keySet().iterator();
        String       name;
        String       value;
        int          id;
        
        page = new ContentPage(parent);
        page.setName(request.getParameter("name"));
        try {
            id = Integer.parseInt(request.getParameter("template"));
        } catch (NumberFormatException ignore) {
            id = 0;
        }
        page.setTemplateId(id);
        page.setComment(request.getParameter("comment"));
        while (iter.hasNext()) {
            name = iter.next().toString();
            if (name.startsWith("element.")) {
                value = params.get(name).toString();
                page.setElement(name.substring(8), value);
            }
        }
        page.save(request.getUser());
        SITE_VIEW.setSiteTreeFocus(request, page);
    }

    /**
     * Handles the add file form.
     * 
     * @param request        the request object
     * @param parent         the parent content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleAddFile(Request request, Content parent) 
        throws ContentException, ContentSecurityException {

        User           user = request.getUser();
        FileParameter  param;
        ContentFile    file;
        
        try {
            param = request.getFileParameter("content");
            file = new ContentFile(parent, param.getName());
            file.setName(request.getParameter("name"));
            file.setComment(request.getParameter("comment"));
            file.save(user);
            param.write(file.getFile());
            SITE_VIEW.setSiteTreeFocus(request, file);
        } catch (IOException e) {
            throw new ContentException(e.getMessage());
        }
    }

    /**
     * Handles the add template form.
     * 
     * @param request        the request object
     * @param parent         the parent domain or template object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void handleAddTemplate(Request request, Object parent) 
        throws ContentException, ContentSecurityException {

        ContentTemplate  template;
        Map              params = request.getAllParameters();
        Iterator         iter = params.keySet().iterator();
        String           name;
        String           value;
        
        if (parent instanceof Domain) {
            template = new ContentTemplate((Domain) parent);
        } else {
            template = new ContentTemplate((ContentTemplate) parent);
        }
        template.setName(request.getParameter("name"));
        template.setComment(request.getParameter("comment"));
        while (iter.hasNext()) {
            name = iter.next().toString();
            if (name.startsWith("element.")) {
                value = params.get(name).toString();
                template.setElement(name.substring(8), value);
            }
        }
        template.save(request.getUser());
        SITE_VIEW.setSiteTreeFocus(request, template);
    }
}
