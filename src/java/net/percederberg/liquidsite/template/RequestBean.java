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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.template;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;

import net.percederberg.liquidsite.web.Request;

/**
 * A request bean. This class is used to access the request object
 * from the templates.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class RequestBean {

    /**
     * The request being processed.
     */
    private Request request;

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
        this.request = context.getRequest();
    }

    /**
     * Returns the site path.
     *
     * @return the site path
     */
    public String getSite() {
        return request.getEnvironment().getSite().getDirectory();
    }

    /**
     * Returns the request path.
     *
     * @return the request path
     */
    public String getPath() {
        return request.getPath();
    }

    /**
     * Returns the complete request URL. The URL will NOT include
     * request parameters or similar.
     *
     * @return the complete request URL
     */
    public String getUrl() {
        StringBuffer  buffer = new StringBuffer();

        buffer.append(request.getProtocol());
        buffer.append("://");
        buffer.append(request.getHost());
        if (request.getPort() != 80) {
            buffer.append(":");
            buffer.append(request.getPort());
        }
        buffer.append(request.getPath());
        return buffer.toString();
    }

    /**
     * Returns a map with all the request parameter names and values.
     *
     * @return the map with request parameter names and values
     */
    public TemplateHashModel getParam() {
        if (paramBean == null) {
            paramBean = new RequestParameterBean(request);
        }
        return paramBean;
    }


    /**
     * A request parameter bean. This bean exposes all the request
     * parameters as a template hash model with the parameter names a
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
