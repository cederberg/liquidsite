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

import net.percederberg.liquidsite.Request;

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
     * Creates a new request bean.
     *
     * @param request        the request
     */
    RequestBean(Request request) {
        this.request = request;
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
}
