/*
 * LiquidSiteBean.java
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
 * A LiquidSite template bean. This class is used to insert the 
 * "liquidsite" namespace into the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class LiquidSiteBean {

    /**
     * The request being processed. 
     */
    private Request request;

    /**
     * The page bean.
     */
    private PageBean page;

    /**
     * Creates a new LiquidSite template bean.
     * 
     * @param request        the request object
     */
    LiquidSiteBean(Request request) {
        this.request = request;
        this.page = new PageBean(request);
    }

    /**
     * Returns the build version name.
     * 
     * @return the build version name
     */
    public String getVersion() {
        return TemplateManager.getApplication().getBuildVersion();
    }

    /**
     * Returns the build date.
     * 
     * @return the build date
     */
    public String getDate() {
        return TemplateManager.getApplication().getBuildDate();
    }
    
    /**
     * Returns the user name for the currently logged in user. 
     * 
     * @return the user name for the currently logged in user
     */
    public String getUser() {
        if (request.getUser() == null) {
            return "";
        } else {
            return request.getUser().getName(); 
        }
    }
    
    /**
     * Returns the page bean for the current page. 
     * 
     * @return the page bean for the current page
     */
    public PageBean getPage() {
        return page;
    }
}
