/*
 * ContentView.java
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

package net.percederberg.liquidsite.admin.view;

import net.percederberg.liquidsite.Request;

/**
 * A helper class for the content view. This class contains methods 
 * for creating the HTML and JavaScript responses to the various 
 * pages in the content view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentView {

    /**
     * Creates a new content view helper.
     */
    ContentView() {
    }

    /**
     * Shows the content page.
     *
     * @param request        the request object
     */
    public void viewContent(Request request) {
        request.sendTemplate("admin/content.ftl");
    }
}
