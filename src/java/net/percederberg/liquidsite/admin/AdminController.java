/*
 * AdminController.java
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import net.percederberg.liquidsite.Application;
import net.percederberg.liquidsite.Controller;
import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.RequestException;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Lock;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.form.FormHandlingException;
import net.percederberg.liquidsite.form.FormValidationException;

/**
 * A controller for requests to the administration site(s).
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class AdminController extends Controller {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(AdminController.class);

    /**
     * The date format used by this class.
     */
    private static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * The admin view helper.
     */
    private AdminView view = new AdminView();

    /**
     * The admin form validator.
     */
    private AdminValidator validator = new AdminValidator();

    /**
     * The admin form handlers (workflows).
     */
    private ArrayList workflows = new ArrayList();

    /**
     * Creates a new administration controller. 
     *
     * @param app            the application context
     */
    public AdminController(Application app) {
        super(app);
        workflows.add(new SiteAddFormHandler());
        workflows.add(new SiteEditFormHandler());
    }

    /**
     * Destroys this request controller. This method frees all
     * internal resources used by this controller.
     */
    public void destroy() {
    }

    /**
     * Processes a request.
     *
     * @param request        the request object
     */
    public void process(Request request) {
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
            request.sendFile(getFile(path));
        } else if (path.startsWith("images/")) {
            request.sendFile(getFile(path));
        } else if (path.startsWith("script/")) {
            request.sendFile(getFile(path));
        } else if (path.equals("") || path.equals("index.html")) {
            view.pageHome(request);
        } else if (path.equals("home.html")) {
            view.pageHome(request);
        } else if (path.equals("site.html")) {
            processViewSite(request);
        } else if (path.equals("content.html")) {
            view.pageContent(request);
        } else if (path.equals("users.html")) {
            view.pageUsers(request);
        } else if (path.equals("system.html")) {
            view.pageSystem(request);
        } else if (path.equals("logout.html")) {
            processLogout(request);
        } else if (path.equals("edit-home.html")) {
            processEditUser(request);
        } else if (path.equals("delete-site.html")) {
            processDeleteObject(request);
        } else if (path.equals("publish-site.html")) {
            processPublishObject(request);
        } else if (path.equals("unpublish-site.html")) {
            processUnpublishObject(request);
        } else if (path.equals("revert-site.html")) {
            processRevertObject(request);
        } else if (path.equals("unlock-site.html")) {
            processUnlockObject(request);
        } else if (path.equals("view.html")) {
            processView(request);
        } else if (path.equals("loadsite.js")) {
            processLoadSite(request);
        } else if (path.equals("opensite.js")) {
            processOpenSite(request);
        } else {
            processWorkflow(request, path);
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
        if (path.equals("style.css")) {
            request.sendFile(getFile(path));
        } else if (path.startsWith("images/")) {
            request.sendFile(getFile(path));
        } else if (path.endsWith(".js")) {
            request.sendData("text/javascript", 
                             "window.location.reload(1);\n");
        } else {
            if (request.getUser() != null) {
                request.setAttribute("error", 
                                     "Access denied for your current user."); 
            }
            request.sendTemplate("admin/login.ftl");
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
                try {
                    formHandler.process(request);
                } catch (FormHandlingException e) {
                    page = formHandler.getStartPage();
                    view.pageError(request, e.getMessage(), page);
                }
                return;
            }
        }
    }

    /**
     * Processes an edit user request.
     * 
     * @param request        the request object
     */
    private void processEditUser(Request request) {
        try {
            if (request.getParameter("prev", "").equals("true")) {
                request.sendRedirect("home.html");
            } else if (request.getParameter("editpassword") != null) {
                if (request.getParameter("step") == null) {
                    view.pageEditPassword(request);
                } else {
                    processEditPassword(request);
                }
            } else {
                if (request.getParameter("step") == null) {
                    view.pageEditUser(request);
                } else {
                    processEditUserDetails(request);
                }
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            view.pageError(request, e);
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            view.pageError(request, e);
        }
    }

    /**
     * Processes the edit user details requests for the home view.
     * 
     * @param request        the request object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void processEditUserDetails(Request request) 
        throws ContentException, ContentSecurityException {
        
        User  user = request.getUser();
        
        try {
            validator.validateEditUser(request);
            user.setRealName(request.getParameter("name"));
            user.setEmail(request.getParameter("email"));
            user.save(user);
            request.sendRedirect("home.html");
        } catch (FormValidationException e) {
            request.setAttribute("error", e.getMessage());
            view.pageEditUser(request);
        }
    }

    /**
     * Processes the edit password requests for the home view.
     * 
     * @param request        the request object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void processEditPassword(Request request) 
        throws ContentException, ContentSecurityException {
        
        User  user = request.getUser();
        
        try {
            validator.validateEditPassword(request);
            if (!user.verifyPassword(request.getParameter("password0"))) {
                throw new FormValidationException(
                    "password0",
                    "Current password was incorrect");
            }
            user.setPassword(request.getParameter("password1"));
            user.save(user);
            request.sendRedirect("home.html");
        } catch (FormValidationException e) {
            request.setAttribute("error", e.getMessage());
            view.pageEditPassword(request);
        }
    }

    /**
     * Processes a view site request.
     * 
     * @param request        the request object
     * 
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processViewSite(Request request) 
        throws RequestException {

        try {
            view.pageSite(request);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            view.pageError(request, e);
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            view.pageError(request, e);
        }
    }

    /**
     * Processes the delete object requests for the site view.
     * 
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processDeleteObject(Request request) 
        throws RequestException {

        Object  obj;
        
        try {
            obj = view.getRequestReference(request);
            if (request.getParameter("confirmed") == null) {
                view.dialogDelete(request, obj);
            } else if (obj instanceof Domain) {
                processDeleteDomain(request, (Domain) obj);
            } else {
                processDeleteContent(request, (Content) obj);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            view.dialogError(request, e);
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            view.dialogError(request, e);
        }
    }

    /**
     * Processes the confirmed delete domain requests for the site 
     * view.
     * 
     * @param request        the request object
     * @param domain         the domain to delete
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void processDeleteDomain(Request request, Domain domain) 
        throws ContentException, ContentSecurityException {

        if (domain.equals(request.getSite().getDomain())) {
            throw new ContentSecurityException(
                "cannot remove the domain containing the site " +
                "currently being used");
        }
        domain.delete(request.getUser());
        view.setSiteTreeFocus(request, null);
        view.dialogClose(request);
    }

    /**
     * Processes the confirmed delete content requests for the site 
     * view.
     * 
     * @param request        the request object
     * @param content        the content object to delete
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void processDeleteContent(Request request, Content content)
        throws ContentException, ContentSecurityException {

        Content  parent;

        if (content.equals(request.getSite())) {
            throw new ContentSecurityException(
                "cannot remove the site currently being used");
        }
        content.delete(request.getUser());
        parent = content.getParent();
        if (parent == null) {
            view.setSiteTreeFocus(request, content.getDomain());
        } else {
            view.setSiteTreeFocus(request, parent);
        }
        view.dialogClose(request);
    }

    /**
     * Processes the publish object requests for the site view.
     * 
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processPublishObject(Request request) 
        throws RequestException {

        Content  content;
        Content  revision;
        
        try {
            content = (Content) view.getRequestReference(request);
            if (request.getParameter("cancel") != null) {
                removeLock(content, request.getUser(), false);
                view.dialogClose(request);
            } else if (request.getParameter("date") == null) {
                checkLock(content, request.getUser(), true);
                revision = content.getRevision(0);
                if (revision != null) {
                    content = revision;
                }
                view.dialogPublish(request, content);
            } else {
                processPublishContent(request, content);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            view.dialogError(request, e);
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            view.dialogError(request, e);
        }
    }

    /**
     * Processes the confirmed publish requests for the site view.
     * 
     * @param request        the request object
     * @param content        the content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void processPublishContent(Request request, Content content) 
        throws ContentException, ContentSecurityException {

        String   date = request.getParameter("date");
        String   comment = request.getParameter("comment");
        Content  work = content.getRevision(0);

        try {
            checkLock(content, request.getUser(), false);
            validator.validatePublish(request);
            if (work != null) {
                work.setRevisionNumber(content.getRevisionNumber() + 1);
                work.setOfflineDate(content.getOfflineDate());
                content = work;
            } else {
                content.setRevisionNumber(content.getRevisionNumber() + 1);
            }
            content.setOnlineDate(DATE_FORMAT.parse(date));
            content.setOfflineDate(null);
            content.setComment(comment);
            content.save(request.getUser());
            removeLock(content, request.getUser(), false);
            view.dialogClose(request);
        } catch (FormValidationException e) {
            request.setAttribute("error", e.getMessage());
            view.dialogPublish(request, content);
        } catch (ParseException e) {
            comment = "Date format error, " + e.getMessage();
            request.setAttribute("error", comment);
            view.dialogPublish(request, content);
        }
    }

    /**
     * Processes the unpublish object requests for the site view.
     * 
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processUnpublishObject(Request request) 
        throws RequestException {

        Content  content;
        
        try {
            content = (Content) view.getRequestReference(request);
            if (request.getParameter("cancel") != null) {
                removeLock(content, request.getUser(), false);
                view.dialogClose(request);
            } else if (request.getParameter("date") == null) {
                checkLock(content, request.getUser(), true);
                view.dialogUnpublish(request, content);
            } else {
                processUnpublishContent(request, content);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            view.dialogError(request, e);
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            view.dialogError(request, e);
        }
    }

    /**
     * Processes the confirmed unpublish requests for the site view.
     * 
     * @param request        the request object
     * @param content        the content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void processUnpublishContent(Request request, Content content) 
        throws ContentException, ContentSecurityException {

        String   date = request.getParameter("date");
        String   comment = request.getParameter("comment");

        try {
            checkLock(content, request.getUser(), false);
            validator.validatePublish(request);
            content.setRevisionNumber(content.getRevisionNumber() + 1);
            content.setOfflineDate(DATE_FORMAT.parse(date));
            content.setComment(comment);
            content.save(request.getUser());
            removeLock(content, request.getUser(), false);
            view.dialogClose(request);
        } catch (FormValidationException e) {
            request.setAttribute("error", e.getMessage());
            view.dialogPublish(request, content);
        } catch (ParseException e) {
            comment = "Date format error, " + e.getMessage();
            request.setAttribute("error", comment);
            view.dialogPublish(request, content);
        }
    }

    /**
     * Processes the revert object requests for the site view.
     * 
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processRevertObject(Request request) 
        throws RequestException {

        User     user = request.getUser();
        Content  content;
        Content  revision;
        
        try {
            content = (Content) view.getRequestReference(request);
            revision = content.getRevision(0);
            if (revision != null) {
                content = revision;
            }
            if (request.getParameter("cancel") != null) {
                removeLock(content, user, false);
                view.dialogClose(request);
            } else if (request.getParameter("confirmed") == null) {
                checkLock(content, user, true);
                view.dialogRevert(request, content);
            } else {
                checkLock(content, user, false);
                content.deleteRevision(user);
                removeLock(content, user, false);
                view.dialogClose(request);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            view.dialogError(request, e);
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            view.dialogError(request, e);
        }
    }

    /**
     * Processes the unlock object requests for the site view.
     * 
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processUnlockObject(Request request) 
        throws RequestException {

        Content  content;
        
        try {
            content = (Content) view.getRequestReference(request);
            if (request.getParameter("confirmed") == null) {
                view.dialogUnlock(request, content);
            } else {
                removeLock(content, request.getUser(), true);
                view.dialogClose(request);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            view.dialogError(request, e);
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            view.dialogError(request, e);
        }
    }

    /**
     * Processes the view content object requests.
     * 
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processView(Request request) throws RequestException {
        Content  content;
        String   revision;

        try {
            content = (Content) view.getRequestReference(request);
            revision = request.getParameter("revision");
            if (revision != null) {
                content = content.getRevision(Integer.parseInt(revision));
            }
            if (content instanceof ContentFile) {
                request.sendFile(((ContentFile) content).getFile());
            } else {
                view.pageError(request, 
                               "Cannot preview this object", 
                               "site.html");
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
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
        Object  obj;

        try {
            obj = view.getRequestReference(request);
            view.scriptLoadSite(request, obj);
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
        Object  obj;

        try {
            obj = view.getRequestReference(request);
            view.scriptOpenSite(request, obj);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            throw RequestException.FORBIDDEN;
        }
    }
    
    /**
     * Checks or acquires a content lock. This method will verify 
     * that any existing lock is owned by the correct user.
     * 
     * @param content        the content object
     * @param user           the user owning the lock
     * @param acquire        the acquire lock flag
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't own and
     *             couldn't acquire the lock 
     */
    private void checkLock(Content content, User user, boolean acquire) 
        throws ContentException, ContentSecurityException {

        Lock  lock = content.getLock();

        if (lock == null && acquire) {
            lock = new Lock(content);
            lock.save(user);
        } else if (lock == null) {
            throw new ContentSecurityException(
                "object is not locked by " + user.getName());
        } else if (!lock.isOwner(user)) {
            throw new ContentSecurityException(
                "object locked by " + lock.getUserName() +
                " since " + DATE_FORMAT.format(lock.getAcquiredDate()));
        }
    }
    
    /**
     * Removes a content lock. This method will quietly ignore a 
     * missing lock or a lock owner by another user. If the force
     * flag is specified, an existing lock will be removed.
     * 
     * @param content        the content object
     * @param user           the user owning the lock
     * @param force          the force removal flag
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have 
     *             permission to remove the lock 
     */
    private void removeLock(Content content, User user, boolean force)
        throws ContentException, ContentSecurityException {

        Lock  lock = content.getLock();

        if (lock == null) {
            // Do nothing
        } else if (lock.isOwner(user) || force) {
            lock.delete(user);
        }
    }
}
