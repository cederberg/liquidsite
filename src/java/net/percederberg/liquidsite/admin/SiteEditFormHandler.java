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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.admin;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import net.percederberg.liquidsite.admin.view.AdminView;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;
import net.percederberg.liquidsite.content.ContentFolder;
import net.percederberg.liquidsite.content.ContentPage;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.ContentSite;
import net.percederberg.liquidsite.content.ContentTemplate;
import net.percederberg.liquidsite.web.FormValidationException;
import net.percederberg.liquidsite.web.FormValidator;
import net.percederberg.liquidsite.web.Request;
import net.percederberg.liquidsite.web.Request.FileParameter;

/**
 * The site edit request handler. This class handles the edit
 * workflow for the site view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class SiteEditFormHandler extends AdminFormHandler {

    /***
     * The latest object instance created.
     */
    private static SiteEditFormHandler instance = null;

    /**
     * The domain form validator.
     */
    private FormValidator domain = new FormValidator();

    /**
     * The site form validator.
     */
    private FormValidator site = new FormValidator();

    /**
     * The folder form validator.
     */
    private FormValidator folder = new FormValidator();

    /**
     * The page form validator.
     */
    private FormValidator page = new FormValidator();

    /**
     * The file form validator.
     */
    private FormValidator file = new FormValidator();

    /**
     * The template form validator.
     */
    private FormValidator template = new FormValidator();

    /**
     * Returns an instance of this class. If a prior instance has
     * been created, it will be returned instead of creating a new
     * one.
     *
     * @return an instance of a site edit form handler
     */
    public static SiteEditFormHandler getInstance() {
        if (instance == null) {
            return new SiteEditFormHandler();
        } else {
            return instance;
        }
    }

    /**
     * Creates a new site edit request handler.
     */
    public SiteEditFormHandler() {
        super("site.html", "edit-site.html", true);
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
        String  domainChars = upperCase + numbers + ".-_";
        String  hostChars = lowerCase + numbers + ".-_";
        String  nameChars = upperCase + lowerCase + numbers + ".-_";
        String  error;

        // Add and edit domain validator
        domain.addRequiredConstraint("name", "No domain name specified");
        error = "Domain name cannot be longer than 30 characters";
        domain.addLengthConstraint("name", 0, 30, error);
        error = "Domain name must be upper-case, invalid character";
        domain.addCharacterConstraint("name", domainChars, error);
        error = "No description specified";
        domain.addRequiredConstraint("description", error);
        domain.addRequiredConstraint("host", "No host name specified");
        error = "Host name must be lower-case, invalid character";
        domain.addCharacterConstraint("host", hostChars, error);

        // Add and edit site validator
        site.addRequiredConstraint("name", "No site name specified");
        site.addRequiredConstraint("protocol", "No protocol specified");
        error = "Protocol must be either 'http' or 'https', " +
                "invalid character";
        site.addCharacterConstraint("protocol", "https", error);
        site.addRequiredConstraint("host", "No host name specified");
        error = "Host name must be lower-case, invalid character";
        site.addCharacterConstraint("host", hostChars + "*", error);
        site.addRequiredConstraint("port", "No port number specified");
        error = "Port number must be numeric, invalid character";
        site.addCharacterConstraint("port", numbers, error);
        site.addRequiredConstraint("dir", "No base directory specified");
        error = "Base directory contains invalid character";
        site.addCharacterConstraint("dir", nameChars + "/", error);
        error = "No revision comment specified";
        site.addRequiredConstraint("comment", error);

        // Add and edit folder validator
        folder.addRequiredConstraint("name", "No folder name specified");
        error = "Folder name contains invalid character";
        folder.addCharacterConstraint("name", nameChars, error);
        folder.addRequiredConstraint("comment", "No comment specified");

        // Add and edit page validator
        page.addRequiredConstraint("name", "No page name specified");
        error = "Page name contains invalid character";
        page.addCharacterConstraint("name", nameChars, error);
        page.addRequiredConstraint("comment", "No comment specified");

        // Add and edit file validator
        file.addRequiredConstraint("name", "No file name specified");
        error = "File name contains invalid character";
        file.addCharacterConstraint("name", nameChars, error);
        file.addRequiredConstraint("comment", "No comment specified");

        // Add and edit template validator
        template.addRequiredConstraint("name", "No template name specified");
        error = "Template name contains invalid character";
        template.addCharacterConstraint("name", nameChars, error);
        template.addRequiredConstraint("comment", "No comment specified");
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

        if (ref instanceof ContentSite) {
            AdminView.SITE.viewEditSite(request, (ContentSite) ref);
        } else if (ref instanceof ContentFolder) {
            AdminView.SITE.viewEditFolder(request,
                                          null,
                                          (ContentFolder) ref);
        } else if (ref instanceof ContentPage) {
            AdminView.SITE.viewEditPage(request, (ContentPage) ref);
        } else if (ref instanceof ContentFile) {
            AdminView.SITE.viewEditFile(request, (ContentFile) ref);
        } else if (ref instanceof ContentTemplate) {
            AdminView.SITE.viewEditTemplate(request,
                                            null,
                                            (ContentTemplate) ref);
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

        if (category.equals("domain")) {
            domain.validate(request);
        } else if (category.equals("site")) {
            site.validate(request);
        } else if (category.equals("folder")) {
            folder.validate(request);
        } else if (category.equals("page")) {
            page.validate(request);
        } else if (category.equals("file")) {
            file.validate(request);
        } else if (category.equals("template")) {
            template.validate(request);
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

        if (ref instanceof ContentSite) {
            handleEditSite(request, (ContentSite) ref);
        } else if (ref instanceof ContentFolder) {
            handleEditFolder(request, (ContentFolder) ref);
        } else if (ref instanceof ContentPage) {
            handleEditPage(request, (ContentPage) ref);
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
        if (request.getParameter("action", "").equals("publish")) {
            site.setRevisionNumber(site.getMaxRevisionNumber() + 1);
            site.setOnlineDate(new Date());
            site.setOfflineDate(null);
        }
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

        int  id;

        folder.setRevisionNumber(0);
        folder.setName(request.getParameter("name"));
        try {
            id = Integer.parseInt(request.getParameter("parent"));
            folder.setParentId(id);
        } catch (NumberFormatException ignore) {
            // This is ignored
        }
        folder.setComment(request.getParameter("comment"));
        if (request.getParameter("action", "").equals("publish")) {
            folder.setRevisionNumber(folder.getMaxRevisionNumber() + 1);
            folder.setOnlineDate(new Date());
            folder.setOfflineDate(null);
        }
        folder.save(request.getUser());
    }

    /**
     * Handles the edit page form.
     *
     * @param request        the request object
     * @param page           the page content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleEditPage(Request request,
                                ContentPage page)
        throws ContentException, ContentSecurityException {

        Map              params = request.getAllParameters();
        Iterator         iter = params.keySet().iterator();
        String           name;
        String           value;
        int              id;

        page.setRevisionNumber(0);
        page.setName(request.getParameter("name"));
        try {
            id = Integer.parseInt(request.getParameter("parent"));
            page.setParentId(id);
        } catch (NumberFormatException ignore) {
            // This is ignored
        }
        try {
            id = Integer.parseInt(request.getParameter("template"));
        } catch (NumberFormatException ignore) {
            id = 0;
        }
        page.setTemplateId(id);
        try {
            id = Integer.parseInt(request.getParameter("section"));
        } catch (NumberFormatException ignore) {
            id = 0;
        }
        page.setSectionId(id);
        page.setComment(request.getParameter("comment"));
        while (iter.hasNext()) {
            name = iter.next().toString();
            if (name.startsWith("element.")) {
                value = params.get(name).toString();
                page.setElement(name.substring(8), value);
            }
        }
        iter = page.getLocalElementNames().iterator();
        while (iter.hasNext()) {
            name = iter.next().toString();
            if (!params.containsKey("element." + name)) {
                page.setElement(name, null);
            }
        }
        if (request.getParameter("action", "").equals("publish")) {
            page.setRevisionNumber(page.getMaxRevisionNumber() + 1);
            page.setOnlineDate(new Date());
            page.setOfflineDate(null);
        }
        page.save(request.getUser());
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
        int            id;
        String         name;

        try {
            file.setRevisionNumber(0);
            file.setName(request.getParameter("name"));
            try {
                id = Integer.parseInt(request.getParameter("parent"));
                file.setParentId(id);
            } catch (NumberFormatException ignore) {
                // This is ignored
            }
            file.setComment(request.getParameter("comment"));
            param = request.getFileParameter("upload");
            if (param != null && param.getSize() > 0) {
                file.setFileName(param.getName());
                param.write(file.getFile());
            } else if (request.getParameter("content") != null) {
                name = file.getFileName();
                name = name.substring(name.indexOf(".") + 1);
                if (name.indexOf(".") <= 0) {
                    file.setFileName(file.getFileName());
                } else {
                    file.setFileName(name);
                }
                file.setTextContent(request.getParameter("content"));
            }
            if (request.getParameter("action", "").equals("publish")) {
                file.setRevisionNumber(file.getMaxRevisionNumber() + 1);
                file.setOnlineDate(new Date());
                file.setOfflineDate(null);
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
        if (request.getParameter("action", "").equals("publish")) {
            template.setRevisionNumber(template.getMaxRevisionNumber() + 1);
            template.setOnlineDate(new Date());
            template.setOfflineDate(null);
        }
        template.save(request.getUser());
    }
}
