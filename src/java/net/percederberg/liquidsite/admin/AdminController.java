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

import net.percederberg.liquidsite.Application;
import net.percederberg.liquidsite.Controller;
import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.RequestException;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Host;
import net.percederberg.liquidsite.content.Lock;
import net.percederberg.liquidsite.content.Site;
import net.percederberg.liquidsite.content.User;

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
    private AdminView view;

    /**
     * The admin form validator.
     */
    private AdminValidator validator = new AdminValidator();

    /**
     * Creates a new administration controller. 
     *
     * @param app            the application context
     */
    public AdminController(Application app) {
        super(app);
        view = new AdminView(app.getContentManager());
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
        } else if (path.equals("add-site.html")) {
            processAddObject(request);
        } else if (path.equals("edit-site.html")) {
            processEditObject(request);
        } else if (path.equals("delete-site.html")) {
            processDeleteObject(request);
        } else if (path.equals("publish-site.html")) {
            processPublishObject(request);
        } else if (path.equals("unpublish-site.html")) {
            processUnpublishObject(request);
        } else if (path.equals("unlock-site.html")) {
            processUnlockObject(request);
        } else if (path.equals("loadsite.js")) {
            processLoadSite(request);
        } else if (path.equals("opensite.js")) {
            processOpenSite(request);
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
     * Processes the add object requests for the site view.
     * 
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processAddObject(Request request) throws RequestException {
        String  step = request.getParameter("step", "");
        String  category = request.getParameter("category", "");
        Object  parent;
        
        try {
            parent = view.getRequestReference(request);
            if (request.getParameter("prev") != null) {
                if (step.equals("1")) {
                    request.sendRedirect("site.html");
                } else {
                    view.pageAddObject(request, parent);
                }
            } else if (category.equals("domain")) {
                if (step.equals("1")) {
                    view.pageAddDomain(request, parent);
                } else {
                    processAddDomain(request, parent);
                }
            } else if (category.equals("site")) {
                if (step.equals("1")) {
                    view.pageAddSite(request, parent);
                } else {
                    processAddSite(request, parent);
                }
            } else {
                view.pageAddObject(request, parent);
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
     * Processes the add domain requests for the site view. The 
     * parent domain object must be specified in case the uses 
     * chooses to go back to the preceeding step. 
     * 
     * @param request        the request object
     * @param parent         the parent domain object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void processAddDomain(Request request, Object parent) 
        throws ContentException, ContentSecurityException {

        Domain  domain;
        Host    host;
        
        try {
            validator.validateAddDomain(request);
            domain = new Domain(request.getParameter("name"));
            domain.setDescription(request.getParameter("description"));
            domain.save(request.getUser());
            host = new Host(domain, request.getParameter("host"));
            host.setDescription("Default domain host");
            host.save(request.getUser());
            view.setSiteTreeFocus(request, domain);
            request.sendRedirect("site.html");
        } catch (FormException e) {
            request.setAttribute("error", e.getMessage());
            view.pageAddDomain(request, parent);
        }
    }

    /**
     * Processes the add site requests for the site view.
     * 
     * @param request        the request object
     * @param parent         the parent domain object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void processAddSite(Request request, Object parent) 
        throws ContentException, ContentSecurityException {

        User    user = request.getUser();
        Domain  domain;
        Site    site;
        
        try {
            validator.validateSite(request);
            domain = (Domain) parent;
            site = new Site(domain);
            site.setName(request.getParameter("name"));
            site.setProtocol(request.getParameter("protocol"));
            site.setHost(request.getParameter("host"));
            site.setPort(Integer.parseInt(request.getParameter("port")));
            site.setDirectory(request.getParameter("dir"));
            site.setAdmin(request.getParameter("admin") != null);
            site.setComment("Created");
            site.save(user);
            view.setSiteTreeFocus(request, site);
            request.sendRedirect("site.html");
        } catch (FormException e) {
            request.setAttribute("error", e.getMessage());
            view.pageAddSite(request, parent);
        }
    }

    /**
     * Processes the edit object requests for the site view.
     * 
     * @param request        the request object
     *
     * @throws RequestException if the request couldn't be processed
     *             correctly
     */
    private void processEditObject(Request request) throws RequestException {
        String   step = request.getParameter("step");
        Object   obj;
        Content  content;
        
        try {
            obj = view.getRequestReference(request);
            if (obj instanceof Content) {
                content = ((Content) obj).getRevision(0);
                if (content != null) {
                    obj = content;
                }
            }
            if (request.getParameter("prev") != null) {
                request.sendRedirect("site.html");
            } else if (obj instanceof Site) {
                if (step == null) {
                    view.pageEditSite(request, (Site) obj);
                } else {
                    processEditSite(request, (Site) obj);
                }
            } else {
                request.sendRedirect("site.html");
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
     * Processes the edit site requests for the site view.
     * 
     * @param request        the request object
     * @param site           the site object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    private void processEditSite(Request request, Site site) 
        throws ContentException, ContentSecurityException {

        try {
            validator.validateSite(request);
            site.setRevisionNumber(0);
            site.setName(request.getParameter("name"));
            site.setProtocol(request.getParameter("protocol"));
            site.setHost(request.getParameter("host"));
            site.setPort(Integer.parseInt(request.getParameter("port")));
            site.setDirectory(request.getParameter("dir"));
            site.setComment("Modified");
            site.save(request.getUser());
            request.sendRedirect("site.html");
        } catch (FormException e) {
            request.setAttribute("error", e.getMessage());
            view.pageAddSite(request, site);
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
            if (obj instanceof Domain) {
                if (request.getParameter("confirmed") == null) {
                    view.dialogDeleteDomain(request, (Domain) obj);
                } else {
                    processDeleteDomain(request, (Domain) obj);
                }
            } else if (obj instanceof Site) {
                if (request.getParameter("confirmed") == null) {
                    view.dialogDeleteSite(request, (Site) obj);
                } else {
                    processDeleteContent(request, (Site) obj);
                }
            } else {
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
        
        try {
            content = (Content) view.getRequestReference(request);
            if (request.getParameter("cancel") != null) {
                removeLock(content, request.getUser(), false);
                view.dialogClose(request);
            } else if (request.getParameter("date") == null) {
                checkLock(content, request.getUser(), true);
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
        } catch (FormException e) {
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
        } catch (FormException e) {
            request.setAttribute("error", e.getMessage());
            view.dialogPublish(request, content);
        } catch (ParseException e) {
            comment = "Date format error, " + e.getMessage();
            request.setAttribute("error", comment);
            view.dialogPublish(request, content);
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
