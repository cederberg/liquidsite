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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.percederberg.liquidsite.admin.view.AdminView;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;
import net.percederberg.liquidsite.content.ContentFolder;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentPage;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.ContentSite;
import net.percederberg.liquidsite.content.ContentTemplate;
import net.percederberg.liquidsite.content.ContentTranslator;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Host;
import net.percederberg.liquidsite.content.PersistentObject;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.web.FormValidationException;
import net.percederberg.liquidsite.web.Request;
import net.percederberg.liquidsite.web.Request.FileParameter;

/**
 * The site add request handler. This class handles the add workflow
 * for the site view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class SiteAddFormHandler extends AdminFormHandler {

    /**
     * The permitted ZIP file entry name characters.
     */
    private static final String ZIPENTRY_CHARS = CONTENT_CHARS + "/";

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

        String            category = request.getParameter("category", "");
        PersistentObject  parent = AdminUtils.getReference(request);

        if (step == 1) {
            AdminView.SITE.viewAddObject(request, parent);
        } else if (category.equals("domain")) {
            AdminView.SITE.viewEditDomain(request, parent, null);
        } else if (category.equals("site")) {
            AdminView.SITE.viewEditSite(request, parent);
        } else if (category.equals("folder")) {
            AdminView.SITE.viewEditFolder(request, parent, null);
        } else if (category.equals("page")) {
            AdminView.SITE.viewEditPage(request, (Content) parent);
        } else if (category.equals("file")) {
            AdminView.SITE.viewEditFile(request, (Content) parent);
        } else if (category.equals("translator")) {
            AdminView.SITE.viewEditTranslator(request, (Content) parent);
        } else if (category.equals("template")) {
            AdminView.SITE.viewEditTemplate(request, parent, null);
        } else {
            AdminView.SITE.viewAddObject(request, parent);
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

        SiteEditFormHandler  edit = SiteEditFormHandler.getInstance();
        String               category = request.getParameter("category", "");
        FileParameter        param;
        String               message;

        if (step == 1) {
            if (category.equals("")) {
                message = "No content category specified";
                throw new FormValidationException("category", message);
            }
        } else {
            if (category.equals("file")) {
                param = request.getFileParameter("upload");
                if (param == null || param.getSize() <= 0) {
                    message = "No file upload specified";
                    throw new FormValidationException("upload", message);
                }
                if (!request.getParameter("unpack", "").equals("true")) {
                    edit.validateStep(request, step);
                } else if (request.getParameter("comment", "").equals("")) {
                    message = "No comment specified";
                    throw new FormValidationException("comment", message);
                } else if (!param.getName().endsWith(".zip")) {
                    message = "Uploaded file must have .zip extension " +
                              "to be unpacked";
                    throw new FormValidationException("upload", message);
                }
            } else {
                edit.validateStep(request, step);
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
        } else if (category.equals("translator")) {
            handleAddTranslator(request, (Content) parent);
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

        ContentManager  manager = AdminUtils.getContentManager();
        Domain          domain;
        Host            host;
        Iterator        iter;
        String          param;
        String          str;

        domain = new Domain(manager, request.getParameter("name"));
        domain.setDescription(request.getParameter("description"));
        domain.save(request.getUser());
        iter = request.getAllParameters().keySet().iterator();
        while (iter.hasNext()) {
            param = iter.next().toString();
            if (param.startsWith("host.") && param.endsWith(".name")) {
                param = param.substring(0, param.length() - 5);
                str = request.getParameter(param + ".name");
                host = new Host(manager, domain, str);
                str = request.getParameter(param + ".description");
                host.setDescription(str);
                host.save(request.getUser());
            }
        }
        AdminView.SITE.setSiteTreeFocus(request, domain);
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

        ContentManager  manager = AdminUtils.getContentManager();
        User            user = request.getUser();
        ContentSite     site;

        site = new ContentSite(manager, parent);
        site.setName(request.getParameter("name"));
        site.setProtocol(request.getParameter("protocol"));
        site.setHost(request.getParameter("host"));
        site.setPort(Integer.parseInt(request.getParameter("port")));
        site.setDirectory(request.getParameter("dir"));
        site.setAdmin(request.getParameter("admin", "").equals("true"));
        site.setComment(request.getParameter("comment"));
        if (request.getParameter("action", "").equals("publish")) {
            site.setRevisionNumber(1);
            site.setOnlineDate(new Date());
        }
        site.save(user);
        AdminView.SITE.setSiteTreeFocus(request, site);
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

        ContentManager  manager = AdminUtils.getContentManager();
        User            user = request.getUser();
        ContentFolder   folder;

        folder = new ContentFolder(manager, parent);
        folder.setName(request.getParameter("name"));
        folder.setComment(request.getParameter("comment"));
        if (request.getParameter("action", "").equals("publish")) {
            folder.setRevisionNumber(1);
            folder.setOnlineDate(new Date());
        }
        folder.save(user);
        AdminView.SITE.setSiteTreeFocus(request, folder);
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

        ContentManager  manager = AdminUtils.getContentManager();
        ContentPage     page;
        Map             params = request.getAllParameters();
        Iterator        iter = params.keySet().iterator();
        String          name;
        String          value;
        int             id;

        page = new ContentPage(manager, parent);
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
        if (request.getParameter("action", "").equals("publish")) {
            page.setRevisionNumber(1);
            page.setOnlineDate(new Date());
        }
        page.save(request.getUser());
        AdminView.SITE.setSiteTreeFocus(request, page);
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

        ContentManager  manager = AdminUtils.getContentManager();
        FileParameter   param;
        ContentFile     file;

        try {
            param = request.getFileParameter("upload");
            if (request.getParameter("unpack", "").equals("true")) {
                handleAddZipFile(request, parent, param.write());
            } else {
                file = new ContentFile(manager, parent, param.getName());
                file.setName(request.getParameter("name"));
                file.setComment(request.getParameter("comment"));
                if (request.getParameter("action", "").equals("publish")) {
                    file.setRevisionNumber(1);
                    file.setOnlineDate(new Date());
                }
                file.save(request.getUser());
                param.write(file.getFile());
                AdminView.SITE.setSiteTreeFocus(request, file);
            }
        } catch (IOException e) {
            throw new ContentException(e.getMessage());
        }
    }

    /**
     * Handles the add file form for ZIP file unpacking. The ZIP file
     * will be removed by this method once processed.
     *
     * @param request        the request object
     * @param parent         the parent content object
     * @param file           the ZIP file to process
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly or if the ZIP file contained errors
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleAddZipFile(Request request, Content parent, File file)
        throws ContentException, ContentSecurityException {

        ZipFile      zip;
        ZipEntry     entry;
        Enumeration  entries;
        boolean      publish;
        Content      content;
        String       name;
        String       message;

        try {
            zip = new ZipFile(file);
            entries = zip.entries();
            while (entries.hasMoreElements()) {
                entry = (ZipEntry) entries.nextElement();
                name = entry.getName();
                for (int i = 0; i < name.length(); i++) {
                    if (ZIPENTRY_CHARS.indexOf(name.charAt(i)) < 0) {
                        message = "invalid character in ZIP file entry '" +
                                  name + "': '" + name.charAt(i) + "'";
                        throw new ContentException(message);
                    }
                }
            }
            publish = request.getParameter("action", "").equals("publish");
            entries = zip.entries();
            while (entries.hasMoreElements()) {
                entry = (ZipEntry) entries.nextElement();
                content = zipCreateContent(entry,
                                           parent,
                                           request.getParameter("comment"),
                                           publish,
                                           request.getUser());
                if (content instanceof ContentFile) {
                    zipExtract(zip, entry, ((ContentFile) content).getFile());
                }
            }
            zip.close();
        } catch (IOException e) {
            throw new ContentException(e.getMessage());
        } finally {
            file.delete();
        }
    }

    /**
     * Handles the add translator form.
     *
     * @param request        the request object
     * @param parent         the parent content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleAddTranslator(Request request, Content parent)
        throws ContentException, ContentSecurityException {

        ContentManager     manager = AdminUtils.getContentManager();
        ContentTranslator  translator;
        int                id;

        translator = new ContentTranslator(manager, parent);
        translator.setName(request.getParameter("name"));
        try {
            id = Integer.parseInt(request.getParameter("section"));
        } catch (NumberFormatException ignore) {
            id = 0;
        }
        translator.setType(ContentTranslator.SECTION_TYPE);
        translator.setSectionId(id);
        translator.setComment(request.getParameter("comment"));
        if (request.getParameter("action", "").equals("publish")) {
            translator.setRevisionNumber(1);
            translator.setOnlineDate(new Date());
        }
        translator.save(request.getUser());
        AdminView.SITE.setSiteTreeFocus(request, translator);
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

        ContentManager   manager = AdminUtils.getContentManager();
        ContentTemplate  template;
        Map              params = request.getAllParameters();
        Iterator         iter = params.keySet().iterator();
        String           name;
        String           value;

        if (parent instanceof Domain) {
            template = new ContentTemplate(manager, (Domain) parent);
        } else {
            template = new ContentTemplate(manager, (ContentTemplate) parent);
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
        if (request.getParameter("action", "").equals("publish")) {
            template.setRevisionNumber(1);
            template.setOnlineDate(new Date());
        }
        template.save(request.getUser());
        AdminView.SITE.setSiteTreeFocus(request, template);
    }

    /**
     * Creates the content object for a ZIP archive entry. The content
     * object will either be a new object or a new revision.  
     *
     * @param entry          the ZIP archive entry
     * @param parent         the parent content object
     * @param comment        the content comment
     * @param publish        the publish flag
     * @param user           the user performing the operation
     *
     * @return the content object created
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private Content zipCreateContent(ZipEntry entry,
                                     Content parent,
                                     String comment,
                                     boolean publish,
                                     User user)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        Content         content;
        String          name;
        int             pos;

        name = entry.getName();
        while (name.indexOf('/') >= 0) {
            pos = name.indexOf('/');
            if (pos == 0) {
                name = name.substring(1);
            } else {
                content = manager.getContentChild(user,
                                                  parent,
                                                  name.substring(0, pos));
                if (content == null) {
                    content = new ContentFolder(manager, parent);
                    content.setName(name.substring(0, pos));
                    content.setComment(comment);
                    if (publish) {
                        content.setRevisionNumber(1);
                        content.setOnlineDate(new Date());
                    }
                    content.save(user);
                }
                parent = content;
                name = name.substring(pos + 1);
            }
        }
        content = manager.getContentChild(user, parent, name);
        if (content == null) {
            if (entry.isDirectory()) {
                content = new ContentFolder(manager, parent);
            } else {
                content = new ContentFile(manager, parent, name);
            }
            content.setName(name);
            if (publish) {
                content.setRevisionNumber(1);
                content.setOnlineDate(new Date());
            }
        } else {
            content.setRevisionNumber(0);
            if (content instanceof ContentFile) {
                ((ContentFile) content).setFileName(name);
            }
            if (publish) {
                content.setRevisionNumber(content.getMaxRevisionNumber() + 1);
                content.setOnlineDate(new Date());
                content.setOfflineDate(null);
            }
        }
        content.setComment(comment);
        content.save(user);
        return content;
    }

    /**
     * Extracts a file from a ZIP archive. The contents of the file
     * will be saved in the specified file.
     *
     * @param zip            the ZIP archive
     * @param entry          the ZIP archive entry to extract
     * @param dest           the destination file
     *
     * @throws IOException if the file date couldn't be extracted or
     *             written correctly
     */
    private void zipExtract(ZipFile zip, ZipEntry entry, File dest)
        throws IOException {

        InputStream       in;
        FileOutputStream  out;
        byte[]            buffer = new byte[4096];
        int               size;

        in = zip.getInputStream(entry);
        out = new FileOutputStream(dest);
        try {
            do {
                size = in.read(buffer);
                if (size > 0) {
                    out.write(buffer, 0, size);
                }
            } while (size > 0);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
                // Do nothing
            }
            try {
                out.close();
            } catch (IOException ignore) {
                // Do nothing
            }
        }
    }
}
