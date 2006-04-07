/*
 * RequestBean.java
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
 * Copyright (c) 2004-2006 Per Cederberg. All rights reserved.
 */

package org.liquidsite.app.template;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;

import org.liquidsite.core.web.Request;

/**
 * A request bean. This class is used to access the request object
 * from the templates.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class RequestBean extends TemplateBean {

    /**
     * The request header bean.
     */
    private RequestHeaderBean headerBean = null;

    /**
     * The request parameter bean.
     */
    private RequestParameterBean paramBean = null;

    /**
     * Creates a new request bean.
     *
     * @param context        the bean context
     */
    RequestBean(BeanContext context) {
        super(context);
    }

    /**
     * Returns the site path.
     *
     * @return the site path
     */
    public String getSite() {
        return getContextRequest().getEnvironment().getSite().getDirectory();
    }

    /**
     * Returns the request path.
     *
     * @return the request path
     */
    public String getPath() {
        return getContextRequest().getPath();
    }

    /**
     * Returns the complete request URL. The URL will NOT include
     * request parameters or similar.
     *
     * @return the complete request URL
     */
    public String getUrl() {
        return getContextRequest().getUrl();
    }

    /**
     * Returns the remote IP address. This is the IP address from where
     * the request was supposedly sent.
     *
     * @return the remote IP address
     */
    public String getIp() {
        return getContextRequest().getRemoteAddr();
    }
    
    /**
     * Returns a map with all the request header names and values.
     *
     * @return the map with request header names and values
     */
    public TemplateHashModel getHeader() {
        if (headerBean == null) {
            headerBean = new RequestHeaderBean(getContextRequest());
        }
        return headerBean;
    }

    /**
     * Returns a map with all the request parameter names and values.
     *
     * @return the map with request parameter names and values
     */
    public TemplateHashModel getParam() {
        if (paramBean == null) {
            paramBean = new RequestParameterBean(getContextRequest());
        }
        return paramBean;
    }

    /**
     * Sets the response content MIME type. By default the HTML MIME
     * type will be used.
     *
     * @param mimeType       the response MIME type
     */
    public void responseMimeType(String mimeType) {
        getContext().setMimeType(mimeType);
    }

    /**
     * Redirects the users web browser to the specified location. The
     * location specified may be either a relative or an absolute URL.
     * Note that page processing does not stop after calling this
     * method. Also, a previous redirect may be cancelled by calling
     * this method with an empty string.
     *
     * @param location       the new location, or an empty string
     */
    public void redirect(String location) {
        if (location == null || location.trim().length() == 0) {
            getContextRequest().sendClear();
        } else {
            if (location.startsWith("/")) {
                location = getContext().getSitePath() + location.substring(1);
            }
            getContextRequest().sendRedirect(location);
        }
    }


    /**
     * A request header bean. This bean exposes all the request
     * headers as a template hash model with the header names as
     * keys, and their values always returned as strings.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    public class RequestHeaderBean implements TemplateHashModel {
        
        /**
         * The request containing the headers.
         */
        private Request request;

        /**
         * Creates a new request header bean.
         *
         * @param request        the request containing headers
         */
        RequestHeaderBean(Request request) {
            this.request = request;
        }

        /**
         * Checks if the header hash model is empty.
         *
         * @return this method always returns false
         */
        public boolean isEmpty() {
            return false;
        }

        /**
         * Returns a header value. If the header didn't exist, a
         * null template model will be returned. Existing header
         * values will be returned as scalar strings.
         *
         * @param name           the header name
         *
         * @return the template model for the value, or
         *         null if the header didn't exist
         */
        public TemplateModel get(String name) {
            String  value = request.getHeader(name);

            return (value == null) ? null : new SimpleScalar(value);
        }
    }


    /**
     * A request parameter bean. This bean exposes all the request
     * parameters as a template hash model with the parameter names as
     * keys, and their values always returned as strings.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    public class RequestParameterBean implements TemplateHashModel {
        
        /**
         * The request containing the parameters.
         */
        private Request request;

        /**
         * Creates a new request parameter bean.
         *
         * @param request        the request containing parameters
         */
        RequestParameterBean(Request request) {
            this.request = request;
        }

        /**
         * Checks if the parameter hash model is empty.
         *
         * @return this method always returns false
         */
        public boolean isEmpty() {
            return false;
        }

        /**
         * Returns a parameter value. If the parameter didn't exist, a
         * null template model will be returned. Existing parameter
         * values will be returned as scalars.
         *
         * @param name           the parameter name
         *
         * @return the template model for the value, or
         *         null if the parameter didn't exist
         */
        public TemplateModel get(String name) {
            String  value = request.getParameter(name);

            return (value == null) ? null : new SimpleScalar(value);
        }
    }
}
