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
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.percederberg.liquidsite.content.ContentSite;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.template.Template;
import net.percederberg.liquidsite.template.TemplateException;
import net.percederberg.liquidsite.template.TemplateManager;

/**
 * An HTTP request and response.
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
     * The redirect response type. This type is used when a request
     * redirect has been issued. The response data contains the 
     * redirect URI (absolute or relative) when this type is set.
     */
    private static final int REDIRECT_RESPONSE = 4;

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
     * The request site.
     */
    private ContentSite site = null;

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
        if (request.getCharacterEncoding() == null) {
            try {
                request.setCharacterEncoding("UTF-8");
            } catch (UnsupportedEncodingException ignore) {
                // Do nothing
            }
        }
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
     * Returns the request site. The site is set for the request upon 
     * dispatching the request. 
     * 
     * @return the request site object
     */
    public ContentSite getSite() {
        return site;
    }
    
    /**
     * Sets the request site. This method is called by the request
     * dispatcher to associate the request with a site.
     * 
     * @param site           the request site
     */
    public void setSite(ContentSite site) {
        this.site = site;
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
        return (User) getSessionAttribute("user");
    }
    
    /**
     * Sets the session user. The session user is null until it is
     * set by this method. Normally the session user is not set until
     * the user has been authenticated. If the user is set to null, 
     * the whole session will be invalidated.
     * 
     * @param user           the new session user, or null to logout
     * 
     * @see #getUser
     */
    public void setUser(User user) {
        if (user == null) {
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }
        } else {
            setSessionAttribute("user", user);
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
        // TODO: refactor to return pure map
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
     * Returns a map with all the request parameter names and values.
     * 
     * @return the map with request parameter names and values
     */
    public Map getAllParameters() {
        HashMap      params = new HashMap();
        Enumeration  names = request.getParameterNames();
        String       name;
        
        while (names.hasMoreElements()) {
            name = names.nextElement().toString();
            params.put(name, request.getParameter(name));
        }
        return params;
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
     * Returns the specified file request parameter. The default 
     * request container doesn't support file parameters, and will
     * return null on each call. Other request containers may 
     * override this method, however, if they are able to handle 
     * incoming files.
     * 
     * @param name           the request parameter name
     *
     * @return the request file parameter, or
     *         null if no such file parameter was found
     */
    public FileParameter getFileParameter(String name) {
        return null;
    }

    /**
     * Returns the value of a session attribute. This method will not
     * create a new session if one didn't already exist.
     * 
     * @param name           the attribute name 
     *
     * @return the attribute value, or
     *         null if no such attribute was found
     */
    public Object getSessionAttribute(String name) {
        return getSessionAttribute(name, null);
    }

    /**
     * Returns the value of a session attribute. If the specified
     * attribute does not exist, a default value will be returned.
     * This method will not create a new session if one didn't 
     * already exist.
     * 
     * @param name           the attribute name
     * @param defVal         the default attribute value 
     *
     * @return the attribute value, or
     *         the default value if no such attribute was found
     */
    public Object getSessionAttribute(String name, Object defVal) {
        HttpSession  session = request.getSession(false);
        
        if (session != null && session.getAttribute(name) != null) {
            return session.getAttribute(name);
        } else {
            return defVal;
        }
    }

    /**
     * Sets a session attribute value. This method creates a new 
     * session if one didn't already exist.
     *
     * @param name           the attribute name
     * @param value          the attribute value, or null to remove
     */
    public void setSessionAttribute(String name, Object value) {
        if (value == null) {
            request.getSession().removeAttribute(name);
        } else {
            request.getSession().setAttribute(name, value);
        }
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
     * Disposes of all resources used by this request object. This 
     * method shouldn't be called until a response has been written.
     */
    public void dispose() {
        request = null;
        response = null;
        responseType = NO_RESPONSE;
        responseMimeType = null;
        responseData = null;
        site = null;
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

        if (responseType == FILE_RESPONSE) {
            response.setHeader("Cache-Control", "private");
        } else {
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Expires", "-1");
        }
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
        case REDIRECT_RESPONSE:
            commitRedirect();
            break;
        default:
            throw new ServletException("No request response available: " + 
                                       this);
        }
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
        if (responseMimeType.indexOf("charset") > 0) {
            response.setContentType(responseMimeType);
        } else {
            response.setContentType(responseMimeType + "; charset=UTF-8");
        }
        out = response.getWriter();
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
        PrintWriter  out;
        Template     template;

        LOG.debug("Handling request for " + this + " with template " +
                  responseData);
        response.setContentType("text/html; charset=UTF-8");
        out = response.getWriter();
        try {
            template = TemplateManager.getFileTemplate(responseData);
            template.process(this, out);
        } catch (TemplateException e) {
            LOG.error("while processing " + responseData + " template", e);
            throw new IOException(e.getMessage());
        } finally {
            out.close();
        }
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
     * A request file parameter.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    public interface FileParameter { 

        /**
         * Returns the base file name including the extension. The
         * file name returned is guaranteed to not contain any file
         * path or directory name.
         * 
         * @return the base file name (with extension)
         */
        String getName();

        /**
         * Returns the file size.
         * 
         * @return the file size
         */
        long getSize();
        
        /**
         * Writes this file to the specified destination file. After
         * calling this method, no other methods in this interface
         * should be called.
         * 
         * @param dest           the destination file
         * 
         * @throws IOException if the file parameter couldn't be 
         *             written to the specified file
         */
        void write(File dest) throws IOException;
    }
}
