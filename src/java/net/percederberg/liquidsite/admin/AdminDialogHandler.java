/*
 * AdminDialogHandler.java
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
import org.liquidsite.core.web.Request;

/**
 * The administration base dialog request handler. This class
 * provides some of the basic dialog handling for all administration
 * workflows.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
abstract class AdminDialogHandler extends AdminFormHandler {

    /**
     * Creates a new administration dialog handler. If the content
     * lock flag is set, a referenced content object will be locked
     * when starting the workflow. Upon further requests the
     * existance of the lock will be checked. When the workflow
     * exits, finally, the lock will be removed.
     *
     * @param start          the start page (originating page)
     * @param page           the form page
     * @param lock           the content locking flag
     */
    protected AdminDialogHandler(String start, String page, boolean lock) {
        super(start, page, lock);
    }

    /**
     * Displays the workflow finished page. By default this method
     * sends a redirect to the start page.
     *
     * @param request        the request object
     */
    protected void displayDone(Request request) {
        AdminView.DIALOG.viewClose(request);
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
        AdminView.DIALOG.viewError(request, message);
    }
}
