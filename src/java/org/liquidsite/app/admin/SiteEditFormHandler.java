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

package org.liquidsite.app.admin;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.liquidsite.app.admin.view.AdminView;
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
import org.liquidsite.core.web.FormValidationException;
import org.liquidsite.core.web.FormValidator;
import org.liquidsite.core.web.Request;
import org.liquidsite.core.web.Request.FileParameter;

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
    private FormValidator domainValidator = new FormValidator();

    /**
     * The site form validator.
     */
    private FormValidator siteValidator = new FormValidator();

    /**
     * The folder form validator.
     */
    private FormValidator folderValidator = new FormValidator();

    /**
     * The page form validator.
     */
    private FormValidator pageValidator = new FormValidator();

    /**
     * The file form validator.
     */
    private FormValidator fileValidator = new FormValidator();

    /**
     * The translator form validator.
     */
    private FormValidator translatorValidator = new FormValidator();

    /**
     * The template form validator.
     */
    private FormValidator templateValidator = new FormValidator();

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
        String  error;

        // Add and edit domain validator
        error = "No domain name specified";
        domainValidator.addRequiredConstraint("name", error);
        error = "Domain name cannot be longer than 30 characters";
        domainValidator.addLengthConstraint("name", 0, 30, error);
        error = "Domain name must be upper-case, invalid character";
        domainValidator.addCharacterConstraint("name", DOMAIN_CHARS, error);
        error = "No description specified";
        domainValidator.addRequiredConstraint("description", error);
        error = "Host name must be lower-case, invalid character";
        domainValidator.addCharacterConstraint("host.0.name",
                                               HOST_CHARS,
                                               error);
        domainValidator.addCharacterConstraint("host.1.name",
                                               HOST_CHARS,
                                               error);
        domainValidator.addCharacterConstraint("host.2.name",
                                               HOST_CHARS,
                                               error);
        domainValidator.addCharacterConstraint("host.3.name",
                                               HOST_CHARS,
                                               error);
        domainValidator.addCharacterConstraint("host.4.name",
                                               HOST_CHARS,
                                               error);

        // Add and edit site validator
        error = "No site name specified";
        siteValidator.addRequiredConstraint("name", error);
        error = "No protocol specified";
        siteValidator.addRequiredConstraint("protocol", error);
        error = "Protocol must be either 'http' or 'https', " +
                "invalid character";
        siteValidator.addCharacterConstraint("protocol", "https", error);
        error = "No host name specified";
        siteValidator.addRequiredConstraint("host", error);
        error = "Host name must be lower-case, invalid character";
        siteValidator.addCharacterConstraint("host",
                                             HOST_CHARS + "*",
                                             error);
        error = "No port number specified";
        siteValidator.addRequiredConstraint("port", error);
        error = "Port number must be numeric, invalid character";
        siteValidator.addCharacterConstraint("port", NUMBERS, error);
        error = "No base directory specified";
        siteValidator.addRequiredConstraint("dir", error);
        error = "Base directory contains invalid character";
        siteValidator.addCharacterConstraint("dir",
                                             CONTENT_CHARS + "/",
                                             error);
        error = "No revision comment specified";
        siteValidator.addRequiredConstraint("comment", error);

        // Add and edit folder validator
        error = "No folder name specified";
        folderValidator.addRequiredConstraint("name", error);
        error = "Folder name contains invalid character";
        folderValidator.addCharacterConstraint("name",
                                               CONTENT_CHARS,
                                               error);
        error = "No comment specified";
        folderValidator.addRequiredConstraint("comment", error);

        // Add and edit page validator
        error = "No page name specified";
        pageValidator.addRequiredConstraint("name", error);
        error = "Page name contains invalid character";
        pageValidator.addCharacterConstraint("name",
                                             CONTENT_CHARS,
                                             error);
        error = "No comment specified";
        pageValidator.addRequiredConstraint("comment", error);

        // Add and edit file validator
        error = "No file name specified";
        fileValidator.addRequiredConstraint("name", error);
        error = "File name contains invalid character";
        fileValidator.addCharacterConstraint("name",
                                             CONTENT_CHARS,
                                             error);
        error = "No comment specified";
        fileValidator.addRequiredConstraint("comment", error);

        // Add and edit translator validator
        error = "No translator name specified";
        translatorValidator.addRequiredConstraint("name", error);
        error = "File name contains invalid character";
        translatorValidator.addCharacterConstraint("name",
                                                   CONTENT_CHARS,
                                                   error);
        error = "No comment specified";
        translatorValidator.addRequiredConstraint("comment", error);

        // Add and edit template validator
        error = "No template name specified";
        templateValidator.addRequiredConstraint("name", error);
        error = "Template name contains invalid character";
        templateValidator.addCharacterConstraint("name",
                                                 CONTENT_CHARS,
                                                 error);
        error = "No comment specified";
        templateValidator.addRequiredConstraint("comment", error);
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

        if (ref instanceof Domain) {
            AdminView.SITE.viewEditDomain(request, null, (Domain) ref);
        } else if (ref instanceof ContentSite) {
            AdminView.SITE.viewEditSite(request, (ContentSite) ref);
        } else if (ref instanceof ContentFolder) {
            AdminView.SITE.viewEditFolder(request,
                                          null,
                                          (ContentFolder) ref);
        } else if (ref instanceof ContentPage) {
            AdminView.SITE.viewEditPage(request, (ContentPage) ref);
        } else if (ref instanceof ContentFile) {
            AdminView.SITE.viewEditFile(request, (ContentFile) ref);
        } else if (ref instanceof ContentTranslator) {
            AdminView.SITE.viewEditTranslator(request,
                                              (ContentTranslator) ref);
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

        String  category = request.getParameter("category", "");
        String  message;

        if (category.equals("domain")) {
            domainValidator.validate(request);
            if (!request.getParameter("name").equals("ROOT")
             && request.getParameter("host.0.name", "").length() <= 0) {

                message = "No host name specified";
                throw new FormValidationException("host.0.name", message);
            }
        } else if (category.equals("site")) {
            siteValidator.validate(request);
            // TODO: check parameters for conflicting sites & objects
        } else if (category.equals("folder")) {
            folderValidator.validate(request);
            message = "Another object with identical name already " +
                      "exists in the parent folder";
            validateParent(request, "parent", message);
        } else if (category.equals("page")) {
            pageValidator.validate(request);
            message = "Another object with identical name already " +
                      "exists in the parent folder";
            validateParent(request, "parent", message);
        } else if (category.equals("file")) {
            validateFile(request);
            message = "Another object with identical name already " +
                      "exists in the parent folder";
            validateParent(request, "parent", message);
        } else if (category.equals("translator")) {
            translatorValidator.validate(request);
            message = "Another object with identical name already " +
                      "exists in the parent folder";
            validateParent(request, "parent", message);
        } else if (category.equals("template")) {
            templateValidator.validate(request);
            message = "Another object with identical name already " +
                      "exists in the base template or domain";
            validateParent(request, "parent", message);
        } else {
            message = "Unknown content category specified";
            throw new FormValidationException("category", message);
        }
    }

    /**
     * Validates a file edit form.
     *
     * @param request        the request object
     *
     * @throws FormValidationException if the form request data
     *             validation failed
     */
    protected void validateFile(Request request)
        throws FormValidationException {

        fileValidator.validate(request);
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

        if (ref instanceof Domain) {
            handleEditDomain(request, (Domain) ref);
        } else if (ref instanceof ContentSite) {
            handleEditSite(request, (ContentSite) ref);
        } else if (ref instanceof ContentFolder) {
            handleEditFolder(request, (ContentFolder) ref);
        } else if (ref instanceof ContentPage) {
            handleEditPage(request, (ContentPage) ref);
        } else if (ref instanceof ContentFile) {
            handleEditFile(request, (ContentFile) ref);
        } else if (ref instanceof ContentTranslator) {
            handleEditTranslator(request, (ContentTranslator) ref);
        } else if (ref instanceof ContentTemplate) {
            handleEditTemplate(request, (ContentTemplate) ref);
        } else {
            throw new ContentException("cannot edit this object");
        }
        return 0;
    }

    /**
     * Handles the edit domain form.
     *
     * @param request        the request object
     * @param domain         the domain object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleEditDomain(Request request, Domain domain)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        Host[]          hosts;
        Host            host;
        HashMap         unprocessed = new HashMap();
        Iterator        iter;
        String          param;
        String          str;

        domain.setDescription(request.getParameter("description"));
        domain.save(request.getUser());
        hosts = domain.getHosts();
        for (int i = 0; i < hosts.length; i++) {
            unprocessed.put(hosts[i].getName(), hosts[i]);
        }
        iter = request.getAllParameters().keySet().iterator();
        while (iter.hasNext()) {
            param = iter.next().toString();
            if (param.startsWith("host.") && param.endsWith(".name")) {
                param = param.substring(0, param.length() - 5);
                str = request.getParameter(param + ".name");
                host = (Host) unprocessed.get(str);
                if (host == null) {
                    host = new Host(manager, domain, str);
                } else {
                    unprocessed.remove(str);
                }
                str = request.getParameter(param + ".description");
                host.setDescription(str);
                host.save(request.getUser());
            }
        }
        iter = unprocessed.values().iterator();
        while (iter.hasNext()) {
            host = (Host) iter.next();
            host.delete(request.getUser());
        }
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
     * Handles the edit translator form.
     *
     * @param request        the request object
     * @param translator     the translator content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleEditTranslator(Request request,
                                      ContentTranslator translator)
        throws ContentException, ContentSecurityException {

        int  id;

        translator.setRevisionNumber(0);
        translator.setName(request.getParameter("name"));
        try {
            id = Integer.parseInt(request.getParameter("parent"));
            translator.setParentId(id);
        } catch (NumberFormatException ignore) {
            // This is ignored
        }
        try {
            id = Integer.parseInt(request.getParameter("section"));
        } catch (NumberFormatException ignore) {
            id = 0;
        }
        translator.setType(ContentTranslator.SECTION_TYPE);
        translator.setSectionId(id);
        translator.setComment(request.getParameter("comment"));
        if (request.getParameter("action", "").equals("publish")) {
            id = translator.getMaxRevisionNumber() + 1;
            translator.setRevisionNumber(id);
            translator.setOnlineDate(new Date());
            translator.setOfflineDate(null);
        }
        translator.save(request.getUser());
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
