/*
 * PageBean.java
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

package net.percederberg.liquidsite.template;

import net.percederberg.liquidsite.Request;

/**
 * A page template bean. This class is used to insert the page object
 * in the into the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PageBean {

    /**
     * The request being processed. 
     */
    private Request request;

    /**
     * Creates a new page template bean.
     * 
     * @param request        the request object
     */
    PageBean(Request request) {
        this.request = request;
    }
    
    /**
     * Returns the full page URL path.
     * 
     * @return the full page URL path
     */
    public String getPath() {
        return request.getPath();
    }
}
