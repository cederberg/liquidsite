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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import net.percederberg.liquidsite.content.User;

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
     * The no response type. This type is used when no request 
     * response has been issued. 
     */
    private static final int NO_RESPONSE = 0;
    
    /**
     * The data response type. This type is used when a data string
     * has been set as the request response.
     */
    private static final int DATA_RESPONSE = 1;
     
    /**
     * The file response type. This type is used when a file has been
     * set as the request response. The response data contains the 
     * absolute file name when this type is set.
     */
    private static final int FILE_RESPONSE = 2;

    /**
     * The template response type. This type is used when a template
     * has been set as the request response. The response data 
     * contains the template file name (relative to the web context 
     * directory) when this type is set.
     */
    private static final int TEMPLATE_RESPONSE = 3;
     
    /**
     * The forward response type. This type is used when a request
     * forward has been issued. The response data contains the 
     * forwarding location when this type is set.
     */
    private static final int FORWARD_RESPONSE = 5;
     
    /**
     * The redirect response type. This type is used when a request
     * redirect has been issued. The response data contains the 
     * redirect URI (absolute or relative) when this type is set.
     */
    private static final int REDIRECT_RESPONSE = 6;

    /**
     * The HTTP request. 
     */
    private HttpServletRequest request;
    
    /**
     * The HTTP response. 
     */
    private HttpServletResponse response;

    /**
     * The reqponse type. This flag is set to true if the 
     * response object has been modified.
     */
    private int responseType = NO_RESPONSE;

    /**
     * The response MIME type.
     */
    private String responseMimeType = null;

    /**
     * The response data. 
     */
    private String responseData = null;

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
     * Checks if this request contains a response.
     * 
     * @return true if the request contains a response, or
     *         false otherwise
     */
    public boolean hasResponse() {
        return responseType != NO_RESPONSE;
    }

    /**
     * Returns the protocol name in the request, i.e. "http" or 
     * "https".
     * 
     * @return the protocol name
     */
    public String getProtocol() {
        return request.getScheme();
    }
    
    /**
     * Returns the host name in the request.
     * 
     * @return the host name
     */
    public String getHost() {
        return request.getServerName();
    }
    
    /**
     * Returns the port number in the request.
     * 
     * @return the port number
     */
    public int getPort() {
        return request.getServerPort();
    }
    
    /**
     * Returns the request path with file name. This will include the
     * servlet portion of the path.
     * 
     * @return the request path with file name
     */
    public String getPath() {
        return request.getRequestURI(); 
    }

    /**
     * Returns the servlet portion of the request path.
     * 
     * @return the servlet portion of the request path
     */
    public String getServletPath() {
        return request.getContextPath();
    }

    /**
     * Returns the session user. The session user is null until it is
     * set by the setUser() method. Normally the session user is not
     * set until the user has been authenticated. 
     * 
     * @return the session user, or
     *         null if no user has been set
     * 
     * @see #setUser
     */
    public User getUser() {
        HttpSession  session = request.getSession(false);
        
        if (session != null && session.getAttribute("user") != null) {
            return (User) session.getAttribute("user");
        } else {
            return null;
        }
    }
    
    /**
     * Sets the session user. The session user is null until it is
     * set by this method. Normally the session user is not set until
     * the user has been authenticated. 
     * 
     * @param user           the new session user
     * 
     * @see #getUser
     */
    public void setUser(User user) {
        if (user != null) {
            request.getSession().setAttribute("user", user);
        } else {
            request.getSession().removeAttribute("user");
        }
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
     * Returns all the request attributes in a map.
     * 
     * @return the map with request attribute names and values
     */
    public HashMap getAllAttributes() {
        HashMap      map = new HashMap();
        Enumeration  names;
        String       name;

        names = request.getAttributeNames();
        while (names.hasMoreElements()) {
            name = names.nextElement().toString();
            map.put(name, request.getAttribute(name));
        }
        return map;
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

    /**
     * Forwards this request to within the same servlet context. This
     * method will set a request response.
     * 
     * @param path           the path to the resource
     */
    public void forward(String path) {
        responseType = FORWARD_RESPONSE;
        responseData = path;
    }
    
    /**
     * Redirects this request by sending a temporary redirection URL
     * to the browser. The location specified may be either an 
     * absolute or a relative URL. This method will set a request 
     * response.
     * 
     * @param location       the destination location
     */
    public void sendRedirect(String location) {
        responseType = REDIRECT_RESPONSE;
        responseData = location;
    }

    /**
     * Sends the specified data as the request response.
     * 
     * @param mimeType       the data MIME type
     * @param data           the data to send
     */
    public void sendData(String mimeType, String data) {
        responseType = DATA_RESPONSE;
        responseMimeType = mimeType;
        responseData = data;
    }

    /**
     * Sends the contents of a file as the request response. The file
     * name extension will be used for determining the MIME type for
     * the file contents.
     * 
     * @param file           the file containing the response
     */
    public void sendFile(File file) {
        responseType = FILE_RESPONSE;
        responseMimeType = null;
        responseData = file.toString();
    }
    
    /**
     * Sends the results from processing a template as the request 
     * response. The template file name is relative to the web 
     * context directory, and the output MIME type will always be set
     * to "text/html".
     * 
     * @param template       the template file name
     */
    public void sendTemplate(String template) {
        responseType = TEMPLATE_RESPONSE;
        responseMimeType = null;
        responseData = template;
    }
    
    /**
     * Sends the request response to the underlying HTTP response 
     * object. 
     * 
     * @param context        the servlet context
     * 
     * @throws IOException if an IO error occured while attempting to
     *             commit the response
     * @throws ServletException if a configuration error was 
     *             encountered while sending the response
     */
    void commit(ServletContext context) 
        throws IOException, ServletException {

        switch (responseType) {
        case DATA_RESPONSE:
            commitData();
            break;
        case FILE_RESPONSE:
            commitFile(context);
            break;
        case TEMPLATE_RESPONSE:
            commitTemplate();
            break;
        case FORWARD_RESPONSE:
            commitForward(context);
            break;
        case REDIRECT_RESPONSE:
            commitRedirect();
            break;
        default:
            throw new ServletException("No request response available: " + 
                                       this);
        }
    }

    /**
     * Sends the forward response to the underlying HTTP response 
     * object. 
     * 
     * @param context        the servlet context
     * 
     * @throws IOException if an IO error occured while attempting to
     *             forward the request
     * @throws ServletException if the JSP servlet wasn't found
     */
    private void commitForward(ServletContext context)
        throws IOException, ServletException {

        RequestDispatcher  disp;

        LOG.debug("Forwarding request for " + this + " to " + responseData);
        disp = context.getNamedDispatcher("JspServlet");
        if (disp == null) {
            throw new ServletException(
                "Couldn't find 'JspServlet' in context");
        }
        disp.forward(new ForwardRequestWrapper(request, responseData), 
                     response);
    }

    /**
     * Sends the redirect response to the underlying HTTP response 
     * object. 
     * 
     * @throws IOException if an IO error occured while attempting to
     *             redirect the request
     */
    private void commitRedirect() throws IOException {
        LOG.debug("Redirecting request for " + this + " to " + responseData);
        response.sendRedirect(responseData);
    }
    
    /**
     * Sends the data response to the underlying HTTP response object. 
     * 
     * @throws IOException if an IO error occured while attempting to
     *             commit the response
     */
    private void commitData() throws IOException {
        PrintWriter  out;

        LOG.debug("Handling request for " + this + " with string data");
        response.setContentType(responseMimeType);
        out = new PrintWriter(response.getOutputStream());
        out.write(responseData);
        out.close();
    }

    /**
     * Sends the file response to the underlying HTTP response object. 
     * 
     * @param context        the servlet context
     * 
     * @throws IOException if an IO error occured while attempting to
     *             commit the response
     */
    private void commitFile(ServletContext context) throws IOException {
        File             file;
        FileInputStream  input;
        OutputStream     output;
        byte[]           buffer = new byte[4096];
        int              length;      

        LOG.debug("Handling request for " + this + " with file " + 
                  responseData);
        file = new File(responseData);
        try {
            input = new FileInputStream(file);
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(context.getMimeType(responseData));
        response.setContentLength((int) file.length());
        output = response.getOutputStream();
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
        input.close();
        output.close();
    }

    /**
     * Sends the processed template response to the underlying HTTP 
     * response object. 
     * 
     * @throws IOException if an IO error occured while attempting to
     *             commit the response
     */
    private void commitTemplate() throws IOException {
        PrintWriter               out;
        Template                  template;
        TemplateExceptionHandler  handler;

        LOG.debug("Handling request for " + this + " with template " +
                  responseData);
        template = TemplateManager.getFileTemplate(responseData);
        response.setContentType("text/html");
        out = new PrintWriter(response.getOutputStream());
        try {
            handler = TemplateExceptionHandler.RETHROW_HANDLER;
            template.setTemplateExceptionHandler(handler);
            template.process(getAllAttributes(), out);
        } catch (TemplateException e) {
            LOG.error("while processing " + responseData + "template", e);
            throw new IOException(e.getMessage());
        } finally {
            out.close();
        }
    }

    /**
     * An HTTP forwarding request. This request wrapper is used to 
     * forward request to JSP:s. 
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    private class ForwardRequestWrapper extends HttpServletRequestWrapper {

        /**
         * The request forward path.
         */
        private String forward;

        /**
         * Creates a new forwarding request.
         * 
         * @param request        the original HTTP request
         * @param forward        the forwarding path
         */
        public ForwardRequestWrapper(HttpServletRequest request, 
                                     String forward) {

            super(request);
            this.forward = forward;
        }
        
        /**
         * Returns the forwarding path.
         * 
         * @return the forwarding path
         */
        public String getServletPath() {
            return forward;
        }

        /**
         * Returns the additional path. This method always returns 
         * null, as the forwarding request does not support 
         * additional paths. 
         * 
         * @return null as no additional path exists
         */
        public String getPathInfo() {
            return null;
        }
    }
}
