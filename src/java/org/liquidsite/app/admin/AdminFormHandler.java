/*
 * AdminFormHandler.java
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

import org.liquidsite.app.admin.view.AdminView;
import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.Lock;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.FormHandler;
import org.liquidsite.core.web.FormHandlingException;
import org.liquidsite.core.web.FormValidationException;
import org.liquidsite.core.web.Request;
import org.liquidsite.util.log.Log;

/**
 * The administration base form request handler. This class provides
 * some of the basic form handling for all administration workflows.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
abstract class AdminFormHandler extends FormHandler {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(AdminFormHandler.class);

    /**
     * The ASCII upper-case characters.
     */
    public static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * The ASCII lower-case characters.
     */
    public static final String LOWER_CASE = "abcdefghijklmonpqrstuvwxyz";

    /**
     * The ASCII numerical characters.
     */
    public static final String NUMBERS = "0123456789";

    /**
     * The permitted domain name characters.
     */
    public static final String DOMAIN_CHARS = UPPER_CASE + NUMBERS + ".-_";

    /**
     * The permitted host name characters.
     */
    public static final String HOST_CHARS = LOWER_CASE + NUMBERS + ".-_";

    /**
     * The permitted content name characters.
     */
    public static final String CONTENT_CHARS =
        UPPER_CASE + LOWER_CASE + NUMBERS + ".-_";

    /**
     * The default start page for the workflow. This is where
     * redirects go when the workflow is finished unless another url
     * is specified in the form.
     */
    private String startPage;

    /**
     * The form page for the workflow. This is where all form request
     * will be posted.
     */
    private String formPage;

    /**
     * The automatic content object lock flag. When this flag is set,
     * the content objects referenced will be locked and unlocked
     * upon entering and exiting the workflow.
     */
    private boolean autoLock;

    /**
     * Creates a new administration form handler. If the content lock
     * flag is set, a referenced content object will be locked when
     * starting the workflow. Upon further requests the existance of
     * the lock will be checked. When the workflow exits, finally,
     * the lock will be removed.
     *
     * @param start          the start page (originating page)
     * @param page           the form page
     * @param lock           the content locking flag
     */
    protected AdminFormHandler(String start, String page, boolean lock) {
        this.startPage = start;
        this.formPage = page;
        this.autoLock = lock;
    }

    /**
     * Returns the default start page for the workflow. This is where
     * redirects go when the workflow is finished, unless a start page
     * is specified in the form.
     *
     * @return the start page for the workflow
     */
    public String getStartPage() {
        return startPage;
    }

    /**
     * Returns the form page for the workflow. This is where all form
     * request will be posted.
     *
     * @return the form page for the workflow
     */
    public String getFormPage() {
        return formPage;
    }

    /**
     * Displays a form for the specified workflow step. The special
     * workflow step zero (0) is used for indicating display of the
     * originating page outside the form workflow. Normally that
     * would cause a redirect.
     *
     * @param request        the request object
     * @param step           the workflow step, or zero (0)
     *
     * @throws FormHandlingException if an error was encountered
     *             while processing the form
     */
    protected final void display(Request request, int step)
        throws FormHandlingException {

        if (step == 0) {
            displayDone(request);
        } else {
            try {
                displayStep(request, step);
            } catch (ContentException e) {
                LOG.error(e.getMessage());
                throw new FormHandlingException(e);
            } catch (ContentSecurityException e) {
                LOG.warning(e.getMessage());
                throw new FormHandlingException(e);
            }
        }
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
    protected abstract void displayStep(Request request, int step)
        throws ContentException, ContentSecurityException;

    /**
     * Displays the workflow finished page. By default this method
     * sends a redirect to the start page.
     *
     * @param request        the request object
     */
    protected void displayDone(Request request) {
        String  url;

        url = request.getParameter("liquidsite.startpage", startPage);
        AdminView.BASE.viewRedirect(request, url);
    }

    /**
     * Displays the workflow error page. By default this method
     * displays the site view error page and then redirects to the
     * start page.
     *
     * @param request        the request object
     * @param message        the error message
     */
    protected void displayError(Request request, String message) {
        String  url;

        url = request.getParameter("liquidsite.startpage", startPage);
        AdminView.BASE.viewError(request, message, url);
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
     * @throws FormHandlingException if an error was encountered
     *             while processing the form
     * @throws FormValidationException if the form request data
     *             validation failed
     */
    protected final void validate(Request request, int step)
        throws FormHandlingException, FormValidationException {

        Object  ref;

        try {
            if (autoLock) {
                ref = AdminUtils.getReference(request);
                if (ref instanceof Content) {
                    lock((Content) ref, request.getUser(), false);
                }
            }
            validateStep(request, step);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw new FormHandlingException(e);
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            throw new FormHandlingException(e);
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
    protected abstract void validateStep(Request request, int step)
        throws ContentException, ContentSecurityException,
               FormValidationException;

    /**
     * Validates the comment field. The comment field is required to
     * be non-empty.
     *
     * @param request        the request object
     *
     * @throws FormValidationException if the form request data
     *             validation failed
     */
    protected void validateComment(Request request)
        throws FormValidationException {

        String  value = request.getParameter("comment");

        if (value == null || value.equals("")) {
            throw new FormValidationException("comment",
                                              "No comment specified");
        }
    }

    /**
     * Validates a content name with regard to its parent. That is,
     * this method will check that no other content objects with the
     * same parent have the same name.
     *
     * @param request        the request object
     * @param field          the parent id field
     * @param error          the message to use on validation error
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     * @throws FormValidationException if the form request data
     *             validation failed
     */
    protected void validateParent(Request request,
                                  String field,
                                  String error)
        throws ContentException, ContentSecurityException,
               FormValidationException {

        ContentManager   manager = AdminUtils.getContentManager();
        String           name;
        String           parentId;
        Object           parent;
        int              id;
        Content          content;

        // Find parent object and content object id
        name = request.getParameter("name");
        parentId = request.getParameter(field);
        if (parentId == null) {
            parent = AdminUtils.getReference(request);
            id = 0;
        } else {
            content = (Content) AdminUtils.getReference(request);
            try {
                id = Integer.parseInt(parentId);
                if (id <= 0) {
                    parent = content.getDomain();
                } else {
                    parent = manager.getContent(request.getUser(), id);
                }
            } catch (NumberFormatException ignore) {
                parent = content.getDomain();
            }
            id = content.getId();
        }

        // Check for existing child with identical name
        if (parent instanceof Domain) {
            content = manager.getContentChild(request.getUser(),
                                              (Domain) parent,
                                              name);
        } else {
            content = manager.getContentChild(request.getUser(),
                                              (Content) parent,
                                              name);
        }
        if (content != null && content.getId() != id) {
            throw new FormValidationException("name", error);
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
     * @throws FormHandlingException if an error was encountered
     *             while processing the form
     */
    protected final int handle(Request request, int step)
        throws FormHandlingException {

        try {
            return handleStep(request, step);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw new FormHandlingException(e);
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            throw new FormHandlingException(e);
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
    protected abstract int handleStep(Request request, int step)
        throws ContentException, ContentSecurityException;

    /**
     * This method locks content objects when entering the workflow.
     * Nothing will be done if the lock flag hasn't been set of if
     * the request doesn't reference a content object.
     *
     * @param request        the request object
     *
     * @throws FormHandlingException if an error was encountered
     *             while processing the form
     */
    protected void workflowEntered(Request request)
        throws FormHandlingException {

        Object  ref;

        if (autoLock) {
            try {
                ref = AdminUtils.getReference(request);
                if (ref instanceof Content) {
                    lock((Content) ref, request.getUser(), true);
                }
            } catch (ContentException e) {
                LOG.error(e.getMessage());
                throw new FormHandlingException(e);
            } catch (ContentSecurityException e) {
                LOG.warning(e.getMessage());
                throw new FormHandlingException(e);
            }
        }
    }

    /**
     * This method unlocks content objects when exiting the workflow.
     * Nothing will be done if the lock flag hasn't been set of if
     * the request doesn't reference a content object.
     *
     * @param request        the request object
     *
     * @throws FormHandlingException if an error was encountered
     *             while processing the form
     */
    protected void workflowExited(Request request)
        throws FormHandlingException {

        Object  ref;

        if (autoLock) {
            try {
                ref = AdminUtils.getReference(request);
                if (ref instanceof Content) {
                    unlock((Content) ref, request.getUser(), false);
                }
            } catch (ContentException e) {
                LOG.error(e.getMessage());
                throw new FormHandlingException(e);
            } catch (ContentSecurityException e) {
                LOG.warning(e.getMessage());
                throw new FormHandlingException(e);
            }
        }
    }


    /**
     * This method is called when an error was encountered during the
     * processing. This event can be used for logging errors and
     * displaying a simple error page. By default this method does
     * nothing.
     *
     * @param request        the request object
     * @param e              the form handling exception
     */
    protected void workflowError(Request request, FormHandlingException e) {
        displayError(request, e.getMessage());
    }

    /**
     * Checks or acquires a content lock. This method will verify
     * that any existing lock is owned by the correct user. If the
     * acquire flag is not set, an existing lock will be checked for.
     * If such a lock does not belong to the specified user, an
     * exception will be thrown.
     *
     * @param content        the content object
     * @param user           the user acquiring the lock
     * @param acquire        the acquire lock flag
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't own and
     *             couldn't acquire the lock
     */
    protected void lock(Content content, User user, boolean acquire)
        throws ContentException, ContentSecurityException {

        Lock  lock = content.getLock();

        if (lock == null && acquire) {
            lock = new Lock(AdminUtils.getContentManager(), content);
            lock.save(user);
        } else if (lock == null) {
            throw new ContentSecurityException(
                "object is not locked by " + user.getName());
        } else if (!lock.isOwner(user)) {
            throw new ContentSecurityException(
                "object locked by " + lock.getUserName() +
                " since " + lock.getAcquiredDate());
        }
    }

    /**
     * Removes a content lock. This method will quietly ignore a
     * missing lock or a lock owner by another user. If the force
     * flag is specified, any existing lock will be removed.
     *
     * @param content        the content object
     * @param user           the user removing the lock
     * @param force          the force removal flag
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have
     *             permission to remove the lock
     */
    protected void unlock(Content content, User user, boolean force)
        throws ContentException, ContentSecurityException {

        Lock  lock = content.getLock();

        if (lock == null) {
            // Do nothing
        } else if (lock.isOwner(user) || force) {
            lock.delete(user);
        }
    }

    /**
     * Converts a file name to a valid file name. All space
     * characters and non-ASCII letters will be converted to '_' or
     * '-'.
     *
     * @param name           the file name to convert
     *
     * @return the converted file name
     */
    protected String convertFileName(String name) {
        StringBuffer  buffer = new StringBuffer();
        char          c;

        for (int i = 0; i < name.length(); i++) {
            c = name.charAt(i);
            if (c == ' ') {
                buffer.append("_");
            } else if (CONTENT_CHARS.indexOf(c) >= 0) {
                buffer.append(c);
            } else {
                buffer.append("-");
            }
        }
        return buffer.toString();
    }
}
