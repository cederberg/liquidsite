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

package net.percederberg.liquidsite;

/**
 * A controller for the administration site.
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
     * Creates a new administration controller. 
     *
     * @param app            the application context
     */
    public AdminController(Application app) {
        super(app);
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
     * @param request        the request object to process
     */
    public void process(Request request) {
        String  path = request.getPath();

        if (path.equals("/style.css") || path.startsWith("/images")) {
            request.sendFile(getFile(path));
        } else {
            displayLogin(request, null);
        }
    }

    /**
     * Displays the login page.
     *
     * @param request        the request object
     * @param error          the error message, or null
     */
    private void displayLogin(Request request, String error) {
        request.setAttribute("error", error);
        request.forward("/admin/login.jsp");
    }
}
