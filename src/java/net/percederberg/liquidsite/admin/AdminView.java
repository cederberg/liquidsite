/*
 * AdminView.java
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

import net.percederberg.liquidsite.Request;

/**
 * A helper class for creating the HTML and JavaScript output for the
 * administration application.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class AdminView {

    /**
     * Creates a new admin view helper.
     */
    public AdminView() {
    }

    /**
     * Shows the automatic close dialog.
     * 
     * @param request        the request object
     */
    public void dialogClose(Request request) {
        request.sendTemplate("admin/dialog/close.ftl");
    }

    /**
     * Shows the error message dialog.
     * 
     * @param request        the request object
     */
    public void dialogError(Request request) {
        request.sendTemplate("admin/dialog/error.ftl");
    }

    /**
     * Shows the delete domain confirmation dialog.
     * 
     * @param request        the request object
     */
    public void dialogDeleteDomain(Request request) {
        request.sendTemplate("admin/dialog/delete-domain.ftl");
    }

    /**
     * Shows the delete site confirmation dialog.
     * 
     * @param request        the request object
     */
    public void dialogDeleteSite(Request request) {
        request.sendTemplate("admin/dialog/delete-site.ftl");
    }
}
