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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.web;

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

import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.template.Template;
import net.percederberg.liquidsite.template.TemplateException;
import net.percederberg.liquidsite.template.TemplateManager;

import org.liquidsite.util.log.Log;

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
     * The response limited cache flag.
     */
    private boolean responseLimitCache = false;

    /**
     * The request environment.
     */
    private RequestEnvironment environment = new RequestEnvironment();

    /**
     * The request session.
     */
    private RequestSession session = null;

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
     * Returns a string representation of this request.
     *
     * @return a string representation of this request
     */
    public String toString() {
        return getUrl();
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
        String  path = request.getPathInfo();

        if (path == null) {
            return request.getContextPath();
        } else {
            return request.getContextPath() + path;
        }
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
     * Returns the full request URL with protocol, hostname and path.
     * No query parameters will be included in the URL, however.
     *
     * @return the full request URL
     */
    public String getUrl() {
        return request.getRequestURL().toString();
    }

    /**
     * Returns the IP address of the request sender.
     *
     * @return the IP address of the request sender
     */
    public String getRemoteAddr() {
        return request.getRemoteAddr();
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
    public Map getAllAttributes() {
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
     * Sets a request attribute value.
     *
     * @param name           the attribute name
     * @param value          the attribute value
     */
    public void setAttribute(String name, int value) {
        setAttribute(name, new Integer(value));
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
     * Returns the session user. The session user is null until it is
     * set by the setUser() method. Normally the session user is not
     * set until the user has been authenticated. If no sesssion
     * existed, this method will return null.
     *
     * @return the session user, or
     *         null if no user has been set
     *
     * @see #setUser
     */
    public User getUser() {
        if (request.getSession(false) == null) {
            return null;
        } else {
            return getSession().getUser();
        }
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
                getSession().invalidate();
                session = null;
            }
        } else {
            getSession().setUser(user);
        }
    }

    /**
     * Returns the request environment object. The request
     * environment is used for storing references to objects that the
     * request touches.
     *
     * @return the request environment object
     */
    public RequestEnvironment getEnvironment() {
        return environment;
    }

    /**
     * Returns the request session. If no session existed a new one
     * will be created.
     *
     * @return the new or existing request session 
     */
    public RequestSession getSession() {
        if (session == null) {
            session = new RequestSession(request.getSession());
        }
        return session;
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
     * the file contents. The cache header for the file may be
     * limited to private by setting the limit cache header.
     *
     * @param file           the file containing the response
     * @param limitCache     the limited cache flag
     */
    public void sendFile(File file, boolean limitCache) {
        responseType = FILE_RESPONSE;
        responseMimeType = null;
        responseData = file.toString();
        responseLimitCache = limitCache;
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
        environment = null;
        session = null;
    }

    /**
     * Sends the request response to the underlying HTTP response
     * object. This method shouldn't be called more than once per
     * request, and should not be called in case no response has been
     * stored in the request. The response can be committed either
     * completely or solely with the response headers.
     *
     * @param context        the servlet context
     * @param content        the complete content response flag
     *
     * @throws IOException if an IO error occured while attempting to
     *             commit the response
     * @throws ServletException if a configuration error was
     *             encountered while sending the response
     */
    public void commit(ServletContext context, boolean content)
        throws IOException, ServletException {

        switch (responseType) {
        case DATA_RESPONSE:
            commitData(content);
            break;
        case FILE_RESPONSE:
            commitFile(context, content);
            break;
        case TEMPLATE_RESPONSE:
            commitTemplate(content);
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
     * Sets the dynamic HTTP response headers. The current system time
     * will be used as the last modification time.
     */
    private void commitDynamicHeaders() {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Expires", "-1");
        response.setDateHeader("Last-Modified", System.currentTimeMillis());
    }

    /**
     * Sets the static HTTP response headers. The specified system
     * time will be used as the last modification time.
     *
     * @param lastModified   the last modification time, or
     *                       zero (0) for the current system time
     */
    private void commitStaticHeaders(long lastModified) {
        if (responseLimitCache) {
            response.setHeader("Cache-Control", "private");
        } else {
            response.setHeader("Cache-Control", "public");
        }
        if (lastModified > 0) {
            response.setDateHeader("Last-Modified", lastModified);
        } else {
            response.setDateHeader("Last-Modified",
                                   System.currentTimeMillis());
        }
    }

    /**
     * Sends the data response to the underlying HTTP response object.
     * The response can be committed either completely or solely with
     * the response headers.
     *
     * @param content        the complete content response flag
     *
     * @throws IOException if an IO error occured while attempting to
     *             commit the response
     */
    private void commitData(boolean content) throws IOException {
        PrintWriter  out;

        LOG.info("Handling request for " + this + " with string data");
        commitDynamicHeaders();
        if (responseMimeType.indexOf("charset") > 0) {
            response.setContentType(responseMimeType);
        } else {
            response.setContentType(responseMimeType + "; charset=UTF-8");
        }
        if (content) {
            out = response.getWriter();
            out.write(responseData);
            out.close();
        }
    }

    /**
     * Sends the file response to the underlying HTTP response object.
     * The response can be committed either completely or solely with
     * the response headers.
     *
     * @param context        the servlet context
     * @param content        the complete content response flag
     *
     * @throws IOException if an IO error occured while attempting to
     *             commit the response
     */
    private void commitFile(ServletContext context, boolean content)
        throws IOException {

        File             file;
        long             modified;
        FileInputStream  input;
        OutputStream     output;
        byte[]           buffer = new byte[4096];
        int              length;

        LOG.info("Handling request for " + this + " with file " +
                 responseData);
        file = new File(responseData);
        modified = request.getDateHeader("If-Modified-Since");
        if (modified != -1 && file.lastModified() < modified + 1000) {
            LOG.trace("request response: HTTP 304, file " + file +
                      " not modified");
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        commitStaticHeaders(file.lastModified());
        response.setContentType(context.getMimeType(responseData));
        response.setContentLength((int) file.length());
        if (content) {
            try {
                input = new FileInputStream(file);
            } catch (IOException e) {
                LOG.error("failed to read HTTP response file " + file, e);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            output = response.getOutputStream();
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            input.close();
            output.close();
        }
    }

    /**
     * Sends the processed template response to the underlying HTTP
     * response object.
     *
     * @param content        the complete content response flag
     *
     * @throws IOException if an IO error occured while attempting to
     *             commit the response
     */
    private void commitTemplate(boolean content) throws IOException {
        PrintWriter  out;
        Template     template;

        LOG.info("Handling request for " + this + " with template " +
                 responseData);
        commitDynamicHeaders();
        response.setContentType("text/html; charset=UTF-8");
        if (content) {
            out = response.getWriter();
            try {
                template = TemplateManager.getFileTemplate(responseData);
                template.process(this, null, out);
            } catch (TemplateException e) {
                LOG.error("while processing " + responseData + " template", e);
                throw new IOException(e.getMessage());
            } finally {
                out.close();
            }
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
        LOG.info("Redirecting request for " + this + " to " + responseData);
        commitDynamicHeaders();
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
         * Returns the full file name including path and extension.
         * The file name returned should be exactly the one sent by
         * the browser.
         *
         * @return the full file path
         */
        String getPath();

        /**
         * Returns the file size.
         *
         * @return the file size
         */
        long getSize();

        /**
         * Writes this file to a temporary file in the upload
         * directory. After calling this method, no other methods in
         * this interfaced should be called.
         *
         * @return the file created
         *
         * @throws IOException if the file parameter couldn't be
         *             written
         */
        File write() throws IOException;

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
