/*
 * DeleteDialogHandler.java
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

import net.percederberg.liquidsite.admin.view.AdminView;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.PersistentObject;
import net.percederberg.liquidsite.web.Request;

/**
 * The delete request handler. This class handles the delete dialog
 * workflow for domain and content objects.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class DeleteDialogHandler extends AdminDialogHandler {

    /**
     * Creates a new delete request handler.
     */
    public DeleteDialogHandler() {
        super("index.html", "delete.html", true);
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

        PersistentObject  ref = AdminUtils.getReference(request);

        AdminView.DIALOG.viewDelete(request, ref);
    }

    /**
     * Validates a form for the specified workflow step. If the form
     * validation fails in this step, the form page for the workflow
     * step will be displayed again with an 'error' attribute
     * containing the message in the validation exception.
     *
     * @param request        the request object
     * @param step           the workflow step
     */
    protected void validateStep(Request request, int step) {
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

        Object   ref = AdminUtils.getReference(request);
        Domain   domain;
        Content  content;

        if (ref instanceof Domain) {
            domain = (Domain) ref;
            if (domain.equals(request.getEnvironment().getDomain())) {
                throw new ContentSecurityException(
                    "cannot remove the domain containing the site " +
                    "currently being used");
            }
            domain.delete(request.getUser());
        } else if (ref instanceof Content) {
            content = (Content) ref;
            if (content.equals(request.getEnvironment().getSite())) {
                throw new ContentSecurityException(
                    "cannot remove the site currently being used");
            }
            content.delete(request.getUser());
        }
        unfocus(request, ref);
        return 0;
    }

    /**
     * Unfocuses the specified object. This will modify the site or
     * content tree focuses, if they were pointing to the specified
     * object.
     *
     * @param request        the request object
     * @param ref            the object to unfocus
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void unfocus(Request request, Object ref)
        throws ContentException {

        Object  focus;

        focus = AdminView.SITE.getSiteTreeFocus(request);
        if (focus != null && focus.equals(ref)) {
            if (ref instanceof Domain) {
                focus = null;
            } else {
                focus = ((Content) ref).getParent();
                if (focus == null) {
                    focus = ((Content) ref).getDomain();
                }
            }
            AdminView.SITE.setSiteTreeFocus(request, focus);
        }
        focus = AdminView.CONTENT.getContentTreeFocus(request);
        if (focus != null && focus.equals(ref)) {
            if (ref instanceof Domain) {
                focus = null;
            } else {
                focus = ((Content) ref).getParent();
                if (focus == null) {
                    focus = ((Content) ref).getDomain();
                }
            }
            AdminView.CONTENT.setContentTreeFocus(request, focus);
        }
    }
}
