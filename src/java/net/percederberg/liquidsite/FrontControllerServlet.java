/*
 * FrontControllerServlet.java
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

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.percederberg.liquidsite.db.DatabaseConnectionException;
import net.percederberg.liquidsite.db.MySQLDatabaseConnector;

/**
 * A front controller servlet. This class handles all incoming HTTP
 * requests.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class FrontControllerServlet extends HttpServlet {

    /**
     * The installation controller.
     */
    private InstallController install;

    /**
     * Initializes this servlet.
     * 
     * @throws ServletException if the servlet failed to initialize
     */
    public void init() throws ServletException {

        // Load MySQL JDBC driver
        try {
            MySQLDatabaseConnector.loadDriver();
        } catch (DatabaseConnectionException e) {
            // TODO: log this error properly
            e.printStackTrace();
        }

        // Create install controller
        install = new InstallController(this);
    }

    /**
     * Handles an incoming HTTP request.
     * 
     * @param request        the HTTP request object
     * @param response       the HTTP response object
     * 
     * @throws ServletException if the request couldn't be handled by
     *             this servlet
     * @throws IOException if an IO error occured while attempting to
     *             service this request
     */
    public void service(HttpServletRequest request, 
                        HttpServletResponse response) 
        throws ServletException, IOException {

        Request            r = new Request(request, response);
        RequestDispatcher  disp;
        String             str;
        
        
        // Process request
        process(r);
        
        // Handle response
        if (r.isProcessed()) {
            // Do nothing
        } else if (r.isForward()) {
            str = r.getForwardPath();
            disp = getServletContext().getRequestDispatcher(str);
            if (disp != null) {
                disp.forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND); 
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND); 
        }
    }
    
    /**
     * Processes an incoming request.
     * 
     * @param request        the request object
     */
    private void process(Request request) {
        // TODO: implement this method properly
        install.process(request);
    }
}
