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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.admin;

import java.text.SimpleDateFormat;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.Lock;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.form.FormHandler;
import net.percederberg.liquidsite.form.FormHandlingException;
import net.percederberg.liquidsite.form.FormValidationException;

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
     * The date format used by this class.
     */
    public static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * The administration site view helper.
     */
    protected static final AdminView SITE_VIEW = new AdminView();

    /**
     * The administration validator helper.
     */
    protected static final AdminValidator VALIDATOR = new AdminValidator();

    /**
     * The start page for the workflow. This is where redirects go
     * when the workflow is finished.
     */
    private String startPage;

    /**
     * The form page for the workflow. This is where all form request
     * will be posted.
     */
    private String formPage;
    
    /**
     * The content object lock flag. When this flag is set, the 
     * content objects referenced will be locked and unlocked upon
     * entering and exiting the workflow.
     */
    private boolean lock;

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
        this.lock = lock;
    }

    /**
     * Returns the start page for the workflow. This is where 
     * redirects go when the workflow is finished.
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

    /**
     * Displays a form for the specified workflow step. The special
     * workflow step zero (0) is used for indicating display of the
     * originating page outside the form workflow. Normally that 
     * would cause a redirect.
     * 
     * @param request        the request object
     * @param step           the workflow step, or zero (0)
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    protected abstract void displayStep(Request request, int step)
        throws ContentException, ContentSecurityException;

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
            if (lock) {
                ref = getReference(request);
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

        if (lock) {
            try {
                ref = getReference(request);
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

        if (lock) {
            try {
                ref = getReference(request);
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
     * Returns the domain or content referenced by a request. When
     * returning a content object, the latest revision (including 
     * work revisions) will be returned.
     * 
     * @param request        the request
     * 
     * @return the domain or content object referenced, or
     *         null if not found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    protected Object getReference(Request request) 
        throws ContentException, ContentSecurityException {

        User     user = request.getUser();
        String   type = request.getParameter("type");
        String   id = request.getParameter("id");
        Content  content;
        Content  revision;
        String   message;
        int      value;
        
        if (type == null || id == null) {
            return null;
        } else if (type.equals("domain")) {
            return getContentManager().getDomain(user, id);
        } else {
            try {
                value = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                message = "invalid content id: " + id;
                LOG.warning(message);
                throw new ContentSecurityException(message);
            }
            content = getContentManager().getContent(user, value);
            revision = content.getRevision(0);
            return (revision == null) ? content : revision;
        }
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
     * Returns the content manager currently in use.
     * 
     * @return the content manager currently in use
     * 
     * @throws ContentException if no content manager exists
     */
    protected ContentManager getContentManager() throws ContentException {
        return ContentManager.getInstance();
    }
}
