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
     * Checks if this is a document request. A document request is
     * one that requests either a directory (URI ending with '/'), or
     * an HTML file (URI ending with '.html'). 
     * 
     * @return true if this is a document request, or
     *         false otherwise
     */
    public boolean isDocumentRequest() {
        String  uri = request.getRequestURI();
        
        return uri.endsWith("/") || uri.endsWith(".html");
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
     * Forwards this request to another resource.
     * 
     * @param path           the path to the resource
     */
    public void forward(String path) {
        forwardPath = path; 
    }
    
    /**
     * Returns the request path with file name.
     * 
     * @return the request path with file name
     */
    public String getPath() {
        return request.getRequestURI();
    }
}
