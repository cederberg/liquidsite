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
import net.percederberg.liquidsite.admin.view.ContentView;
import net.percederberg.liquidsite.admin.view.DialogView;
import net.percederberg.liquidsite.admin.view.HomeView;
import net.percederberg.liquidsite.admin.view.ScriptView;
import net.percederberg.liquidsite.admin.view.SiteView;
import net.percederberg.liquidsite.admin.view.SystemView;
import net.percederberg.liquidsite.admin.view.UserView;

/**
 * A helper class for creating the HTML and JavaScript output for the
 * administration application.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class AdminView {

    /**
     * The base view class.
     */
    public static final AdminView BASE = new AdminView();

    /**
     * The dialog view helper.
     */
    public static final DialogView DIALOG = new DialogView();

    /**
     * The home view helper.
     */
    public static final HomeView HOME = new HomeView();

    /**
     * The site view helper.
     */
    public static final SiteView SITE = new SiteView();

    /**
     * The content view helper.
     */
    public static final ContentView CONTENT = new ContentView();

    /**
     * The user view helper.
     */
    public static final UserView USER = new UserView();

    /**
     * The system view helper.
     */
    public static final SystemView SYSTEM = new SystemView();

    /**
     * The script view helper.
     */
    public static final ScriptView SCRIPT = new ScriptView();

    /**
     * Creates a new admin view helper.
     */
    public AdminView() {
    }

    /**
     * Shows the error message page. When the user presses the 
     * confirmation button on the error page, the browser will be
     * redirected to the specified page.
     * 
     * @param request        the request object
     * @param message        the error message
     * @param page           the redirect page
     */
    public void viewError(Request request, String message, String page) {
        request.setAttribute("error", message);
        request.setAttribute("page", page);
        request.sendTemplate("admin/error.ftl");
    }
}
