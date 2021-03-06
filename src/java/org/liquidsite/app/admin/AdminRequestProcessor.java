/*
 * AdminRequestProcessor.java
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

import java.io.File;
import java.util.ArrayList;

import org.liquidsite.app.admin.view.AdminView;
import org.liquidsite.app.servlet.Application;
import org.liquidsite.app.servlet.RequestException;
import org.liquidsite.app.servlet.RequestProcessor;
import org.liquidsite.app.template.TemplateException;
import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentDocument;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentFile;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentSection;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.ContentSite;
import org.liquidsite.core.content.ContentTemplate;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.PersistentObject;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.Request;
import org.liquidsite.util.log.Log;

/**
 * The request processor for the administration site(s).
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class AdminRequestProcessor extends RequestProcessor {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(AdminRequestProcessor.class);

    /**
     * The system view request processor.
     */
    private SystemRequestProcessor system = new SystemRequestProcessor();

    /**
     * The admin form handlers (workflows).
     */
    private ArrayList workflows = new ArrayList();

    /**
     * Creates a new administration request processor.
     *
     * @param app            the application context
     */
    public AdminRequestProcessor(Application app) {
        super(new ContentManager(app.getContentManager(), true),
              app.getBaseDir());
        AdminUtils.setApplication(app);
        AdminUtils.setContentManager(getContentManager());
        workflows.add(new HomeEditFormHandler());
        workflows.add(new SiteAddFormHandler());
        workflows.add(new SiteEditFormHandler());
        workflows.add(new ContentAddFormHandler());
        workflows.add(new ContentEditFormHandler());
        workflows.add(new PublishDialogHandler());
        workflows.add(new UnpublishDialogHandler());
        workflows.add(new RevertDialogHandler());
        workflows.add(new DeleteDialogHandler());
        workflows.add(new PermissionsDialogHandler());
        workflows.add(new UnlockDialogHandler());
        workflows.add(new UsersAddFormHandler());
        workflows.add(new UsersEditFormHandler());
        workflows.add(new UsersDeleteDialogHandler());
    }

    /**
     * Destroys this request processor. This method frees all
     * internal resources used by this processor.
     */
    public void destroy() {
        // Nothing to be done
    }

    /**
     * Processes a request.
     *
     * @param request        the request object
     */
    public void process(Request request) {
        // Requests must be either authorized or unauthorized,
        // which is decided in the default request processor.
    }

    /**
     * Processes an authorized request. This is a request from a user
     * with permissions to access the admin site.
     *
     * @param request        the request object
     * @param path           the request path
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    public void processAuthorized(Request request, String path)
        throws RequestException {

        if (path.equals("style.css")) {
            request.sendFile(getFile(path), false);
        } else if (path.startsWith("images/")) {
            request.sendFile(getFile(path), false);
        } else if (path.startsWith("script/")) {
            request.sendFile(getFile(path), false);
        } else if (path.equals("") || path.equals("index.html")) {
            AdminView.HOME.viewHome(request);
        } else if (path.equals("home.html")) {
            AdminView.HOME.viewHome(request);
        } else if (path.equals("site.html")) {
            processViewSite(request);
        } else if (path.equals("content.html")) {
            processViewContent(request);
        } else if (path.equals("users.html")) {
            processViewUsers(request);
        } else if (path.equals("view-users.html")) {
            processViewUserDetails(request);
        } else if (path.equals("system.html")) {
            system.process(request, path);
        } else if (path.equals("statistics.html")) {
            processViewStatistics(request);
        } else if (path.equals("logout.html")) {
            processLogout(request);
        } else if (path.equals("loadsite.js")) {
            processLoadSite(request);
        } else if (path.equals("loadcontent.js")) {
            processLoadContent(request);
        } else if (path.equals("opensite.js")) {
            processOpenSite(request);
        } else if (path.equals("opencontent.js")) {
            processOpenContent(request);
        } else if (path.equals("opentemplate.js")) {
            processOpenTemplate(request);
        } else if (path.equals("sessionping.js")) {
            processSessionPing(request);
        } else if (path.startsWith("preview/")) {
            processPreview(request, path.substring(8));
        } else if (path.startsWith("sessionpreview/")) {
            processSessionPreview(request, path.substring(15));
        } else if (path.startsWith("stats/")) {
            processStats(request, path.substring(6));
        } else {
            processWorkflow(request, path);
        }
        if (!request.hasResponse()) {
            throw RequestException.RESOURCE_NOT_FOUND;
        }
    }

    /**
     * Processes an unauthorized request. This is a request from a
     * user without permissions to access the admin site.
     *
     * @param request        the request object
     * @param path           the request path
     */
    public void processUnauthorized(Request request, String path) {
        String  str;

        if (path.equals("style.css")) {
            request.sendFile(getFile(path), false);
        } else if (path.startsWith("images/")) {
            request.sendFile(getFile(path), false);
        } else if (path.endsWith(".js")) {
            AdminView.SCRIPT.viewReload(request);
        } else if (path.equals("")
                || path.equals("index.html")
                || path.equals("home.html")
                || path.equals("site.html")
                || path.equals("content.html")
                || path.equals("users.html")
                || path.equals("system.html")) {

            if (request.getUser() != null) {
                request.setAttribute("error",
                                     "Access denied for your current user.");
            }
            AdminUtils.sendTemplate(request, "admin/login.ftl");
        } else {
            str = request.getEnvironment().getSite().getDirectory();
            request.sendRedirect(str);
        }
    }

    /**
     * Processes a logout request.
     *
     * @param request        the request object
     */
    private void processLogout(Request request) {
        request.setUser(null);
        request.sendRedirect("index.html");
    }

    /**
     * Processes a form workflow request. This method will check the
     * specified page for a matching workflow. If no workflow is
     * found, no processing takes place.
     *
     * @param request        the request object
     * @param page           the request page
     */
    private void processWorkflow(Request request, String page) {
        AdminFormHandler  formHandler;

        for (int i = 0; i < workflows.size(); i++) {
            formHandler = (AdminFormHandler) workflows.get(i);
            if (formHandler.getFormPage().equals(page)) {
                formHandler.process(request);
                return;
            }
        }
    }

    /**
     * Processes a view site request.
     *
     * @param request        the request object
     */
    private void processViewSite(Request request) {
        try {
            AdminView.SITE.viewSite(request);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            AdminView.BASE.viewError(request, e.getMessage(), "index.html");
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            AdminView.BASE.viewError(request, e.getMessage(), "index.html");
        }
    }

    /**
     * Processes a view content request.
     *
     * @param request        the request object
     */
    private void processViewContent(Request request) {
        try {
            AdminView.CONTENT.viewContent(request);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            AdminView.BASE.viewError(request, e.getMessage(), "index.html");
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            AdminView.BASE.viewError(request, e.getMessage(), "index.html");
        }
    }

    /**
     * Processes a view users request.
     *
     * @param request        the request object
     */
    private void processViewUsers(Request request) {
        try {
            AdminView.USER.viewUsers(request);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            AdminView.BASE.viewError(request, e.getMessage(), "index.html");
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            AdminView.BASE.viewError(request, e.getMessage(), "index.html");
        }
    }

    /**
     * Processes a view user or group detail request.
     *
     * @param request        the request object
     */
    private void processViewUserDetails(Request request) {
        try {
            AdminView.USER.viewGroup(request);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            AdminView.BASE.viewError(request, e.getMessage(), "index.html");
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            AdminView.BASE.viewError(request, e.getMessage(), "index.html");
        }
    }

    /**
     * Processes a view statistics request.
     *
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processViewStatistics(Request request) throws RequestException {
        PersistentObject  obj;

        try {
            obj = AdminUtils.getReference(request);
            if (!obj.hasPublishAccess(request.getUser())) {
                throw RequestException.FORBIDDEN;
            }
            if (obj instanceof Domain) {
                AdminView.DIALOG.viewStatistics(request, (Domain) obj);
            } else {
                AdminView.BASE.viewError(request,
                                         "Cannot display statistics for object",
                                         "index.html");
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            AdminView.BASE.viewError(request, e.getMessage(), "index.html");
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            AdminView.BASE.viewError(request, e.getMessage(), "index.html");
        }
    }

    /**
     * Processes a JavaScript load site tree item request.
     *
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processLoadSite(Request request) throws RequestException {
        PersistentObject  obj;
        boolean           open;

        try {
            obj = AdminUtils.getReference(request);
            open = request.getParameter("open", "").equals("true");
            AdminView.SITE.viewLoadSiteScript(request, obj, open);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Processes a JavaScript load content tree item request.
     *
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processLoadContent(Request request) throws RequestException {
        PersistentObject  obj;
        boolean           open;

        try {
            obj = AdminUtils.getReference(request);
            open = request.getParameter("open", "").equals("true");
            AdminView.CONTENT.viewLoadContentScript(request, obj, open);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Processes a JavaScript open site tree item request.
     *
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processOpenSite(Request request) throws RequestException {
        PersistentObject  obj;

        try {
            obj = AdminUtils.getReference(request);
            AdminView.SITE.viewOpenSiteScript(request, obj);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Processes a JavaScript open content tree item request.
     *
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processOpenContent(Request request) throws RequestException {
        PersistentObject  obj;

        try {
            obj = AdminUtils.getReference(request);
            AdminView.CONTENT.viewOpenContentScript(request, obj);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Processes a JavaScript open template request.
     *
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processOpenTemplate(Request request) throws RequestException {
        int              id;
        Content          content;
        ContentTemplate  template;

        try {
            id = Integer.parseInt(request.getParameter("id", "0"));
            content = getContentManager().getContent(request.getUser(), id);
            if (content instanceof ContentTemplate) {
                template = (ContentTemplate) content;
                AdminView.SITE.viewOpenTemplateScript(request, template);
            } else {
                AdminView.SITE.viewOpenTemplateScript(request, null);
            }
        } catch (NumberFormatException e) {
            throw RequestException.FORBIDDEN;
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Processes a sesssion keep-alive ping request.
     *
     * @param request        the request object
     */
    private void processSessionPing(Request request) {
        request.sendData("text/javascript",
                         "/* Pong, session active. */");
    }

    /**
     * Processes the preview requests.
     *
     * @param request        the request object
     * @param path           the preview path
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processPreview(Request request, String path)
        throws RequestException {

        Content  content;
        int      id;
        int      pos;

        try {
            pos = path.indexOf("/");
            id = Integer.parseInt(path.substring(0, pos));
            path = path.substring(pos + 1);
            content = getContentManager().getContent(request.getUser(), id);
            if (content instanceof ContentSite) {
                processPreview(request, (ContentSite) content, path);
            } else if (content instanceof ContentTemplate) {
                processPreview(request, (ContentTemplate) content);
            } else if (content instanceof ContentDocument) {
                processPreview(request, (ContentDocument) content, path);
            } else if (content instanceof ContentSection) {
                processPreview(request, (ContentSection) content);
            } else if (content instanceof ContentFile) {
                processPreview(request, (ContentFile) content);
            } else {
                throw RequestException.FORBIDDEN;
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            request.sendData("text/plain", e.getMessage());
        } catch (ContentSecurityException e) {
            request.sendData("text/plain", e.getMessage());
        } catch (TemplateException e) {
            LOG.error(e.getMessage());
            request.sendData("text/plain", e.getMessage());
        } catch (RuntimeException e) {
            request.sendData("text/plain", "Cannot preview this object");
        }
    }

    /**
     * Processes a preview request for a specific site.
     *
     * @param request        the request object
     * @param site           the content site object
     * @param path           the preview path (within the site)
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the request referred to an
     *             object that wasn't readable by the user
     * @throws TemplateException if the page template couldn't be
     *             processed correctly
     */
    private void processPreview(Request request,
                                ContentSite site,
                                String path)
        throws RequestException, ContentException,
               ContentSecurityException, TemplateException {

        String  dir;

        // Adjust the request environment
        request.getEnvironment().setDomain(site.getDomain());
        dir = request.getEnvironment().getSite().getDirectory() +
              "preview/" + site.getId() + "/";
        site.setDirectory(dir);
        request.getEnvironment().setSite(site);

        // Process preview request
        if (path.startsWith("liquidsite/")) {
            processLiquidSite(request, path.substring(11));
        } else {
            processNormal(request, site, path, true);
        }
    }

    /**
     * Processes a preview request for a specific file.
     *
     * @param request        the request object
     * @param file           the content file object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the request referred to an
     *             object that wasn't readable by the user
     * @throws TemplateException if the page template couldn't be
     *             processed correctly
     */
    private void processPreview(Request request,
                                ContentFile file)
        throws RequestException, ContentException,
               ContentSecurityException, TemplateException {

        String  revision;

        revision = request.getParameter("revision");
        if (revision != null) {
            file = (ContentFile) file.getRevision(Integer.parseInt(revision));
        }
        sendContent(request, file);
    }

    /**
     * Processes a preview request for a content template.
     *
     * @param request        the request object
     * @param template       the content template object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void processPreview(Request request, ContentTemplate template)
        throws RequestException, ContentException {

        Content  content;
        String   revision;

        content = template;
        revision = request.getParameter("revision");
        if (content != null && revision != null) {
            content = content.getRevision(Integer.parseInt(revision));
        }
        if (content instanceof ContentTemplate) {
            AdminView.SITE.viewTemplatePreview(request,
                                               (ContentTemplate) content);
        } else {
            throw RequestException.RESOURCE_NOT_FOUND;
        }
    }

    /**
     * Processes a preview request for a specific document.
     *
     * @param request        the request object
     * @param doc            the content document object
     * @param path           the preview path (within the document)
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the request referred to an
     *             object that wasn't readable by the user
     */
    private void processPreview(Request request,
                                ContentDocument doc,
                                String path)
        throws RequestException, ContentException, ContentSecurityException {

        ContentManager  manager = getContentManager();
        Content         content = null;
        String          revision;

        if (path.equals("")) {
            content = doc;
        } else {
            content = manager.getContentChild(request.getUser(), doc, path);
        }
        revision = request.getParameter("revision");
        if (content != null && revision != null) {
            content = content.getRevision(Integer.parseInt(revision));
        }
        if (content instanceof ContentDocument) {
            AdminView.CONTENT.viewDocumentPreview(request,
                                                  (ContentDocument) content);
        } else if (content instanceof ContentFile) {
            request.sendFile(((ContentFile) content).getFile(),
                             !content.hasReadAccess(null));
        } else {
            throw RequestException.RESOURCE_NOT_FOUND;
        }
    }

    /**
     * Processes a preview request for a content section.
     *
     * @param request        the request object
     * @param section        the content section object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void processPreview(Request request, ContentSection section)
        throws RequestException, ContentException {

        Content  content;
        String   revision;

        content = section;
        revision = request.getParameter("revision");
        if (content != null && revision != null) {
            content = content.getRevision(Integer.parseInt(revision));
        }
        if (content instanceof ContentSection) {
            AdminView.CONTENT.viewSectionPreview(request,
                                                 (ContentSection) content);
        } else {
            throw RequestException.RESOURCE_NOT_FOUND;
        }
    }

    /**
     * Processes the session preview requests.
     *
     * @param request        the request object
     * @param path           the preview path
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processSessionPreview(Request request, String path)
        throws RequestException {

        File  file = request.getSession().getFile(path);

        if (file == null) {
            throw RequestException.RESOURCE_NOT_FOUND;
        } else {
            request.sendFile(file, true);
        }
    }

    /**
     * Processes the statistics requests.
     *
     * @param request        the request object
     * @param path           the statistics path
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processStats(Request request, String path)
        throws RequestException {

        User    user = request.getUser();
        Domain  domain;
        File    file;
        String  str;
        int     pos;

        try {
            pos = path.indexOf("/");
            str = path.substring(0, pos);
            path = path.substring(pos + 1);
            domain = getContentManager().getDomain(user, str);
            if (!domain.hasPublishAccess(user)) {
                throw RequestException.FORBIDDEN;
            }
            file = AdminUtils.getStatisticsDir(domain);
            if (file == null) {
                throw RequestException.RESOURCE_NOT_FOUND;
            }
            file = new File(file, path);
            if (file.canRead()) {
                request.sendFile(file, true);
            } else {
                throw RequestException.RESOURCE_NOT_FOUND;
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            request.sendData("text/plain", e.getMessage());
        } catch (ContentSecurityException e) {
            request.sendData("text/plain", e.getMessage());
        }
    }
}
