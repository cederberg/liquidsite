/*
 * Request.java
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A document request.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Request {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(Request.class);

    /**
     * The HTTP request. 
     */
    private HttpServletRequest request;
    
    /**
     * The HTTP response. 
     */
    private HttpServletResponse response;

    /**
     * The request processed flag. This flag is set to true if the 
     * response object has been modified.
     */
    private boolean processed = false;

    /**
     * The request forward path. 
     */
    private String forwardPath = null;

    /**
     * Creates a new request. 
     * 
     * @param request        the HTTP request
     * @param response       the HTTP response
     */
    public Request(HttpServletRequest request, 
                   HttpServletResponse response) {

        this.request = request;
        this.response = response;
    }
    
    /**
     * Checks if this request has been processed. A request is 
     * considered processed when the response object has been 
     * modified.
     * 
     * @return true if the request has been processed, or
     *         false otherwise
     */
    public boolean isProcessed() {
        return processed;
    }

    /**
     * Checks if this request should be forwarded. 
     * 
     * @return true if this request should be forwarded, or
     *         false otherwise
     */
    public boolean isForward() {
        return forwardPath != null;
    }

    /**
     * Returns the forward path. 
     * 
     * @return the request forward path, or
     *         null if the request shouldn't be forwarded
     */
    public String getForwardPath() {
        return forwardPath;
    }

    /**
     * Forwards this request within the same servlet context.
     * 
     * @param path           the path to the resource
     */
    public void forward(String path) {
        forwardPath = path; 
    }
    
    /**
     * Redirects this request by sending a temporary redirection URL
     * to the browser. The location specified may be either an 
     * absolute or a relative URL.
     * 
     * @param location       the destination location
     */
    public void redirect(String location) {
        processed = true;
        try {
            response.sendRedirect(location);
        } catch (IllegalStateException e) {
            LOG.warning("couldn't redirect request, already processed", e);
        } catch (IOException e) {
            LOG.warning("couldn't redirect request", e);
        }
    }

    /**
     * Returns the request path with file name.
     * 
     * @return the request path with file name
     */
    public String getPath() {
        return request.getRequestURI();
    }

    /**
     * Returns the value of a request attribute.
     * 
     * @param name           the attribute name 
     *
     * @return the attribute value, or
     *         null if no such attribute was found
     */
    public Object getAttribute(String name) {
        return getAttribute(name, null);
    }

    /**
     * Returns the value of a request attribute. If the specified
     * attribute does not exist, a default value will be returned.
     * 
     * @param name           the attribute name
     * @param defVal         the default attribute value 
     *
     * @return the attribute value, or
     *         the default value if no such attribute was found
     */
    public Object getAttribute(String name, Object defVal) {
        Object  value = request.getAttribute(name);
        
        return (value == null) ? defVal : value;
    }

    /**
     * Sets a request attribute value.
     *
     * @param name           the attribute name
     * @param value          the attribute value
     */
    public void setAttribute(String name, Object value) {
        request.setAttribute(name, value);
    }

    /**
     * Sets a request attribute value.
     *
     * @param name           the attribute name
     * @param value          the attribute value
     */
    public void setAttribute(String name, boolean value) {
        setAttribute(name, new Boolean(value));
    }

    /**
     * Returns the value of a request parameter.
     *
     * @param name           the request parameter name
     *
     * @return the request parameter value, or
     *         null if no such parameter was found
     */
    public String getParameter(String name) {
        return getParameter(name, null);
    }

    /**
     * Returns the value of a request parameter. If the specified
     * parameter does not exits, a default value will be returned.
     *
     * @param name           the request parameter name
     * @param defVal         the default parameter value
     *
     * @return the request parameter value, or
     *         the default value if no such parameter was found
     */
    public String getParameter(String name, String defVal) {
        String  value = request.getParameter(name);
        
        return (value == null) ? defVal : value;
    }
    
    /**
     * Returns a string representation of this request.
     * 
     * @return a string representation of this request
     */
    public String toString() {
        return request.getRequestURI();
    }
}
