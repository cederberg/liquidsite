/*
 * TemplateBean.java
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
 * Copyright (c) 2006 Per Cederberg. All rights reserved.
 */

package org.liquidsite.app.template;

import org.liquidsite.core.web.Request;

/**
 * The base class for all template beans. This class serves as the
 * superclass for all template beans.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public abstract class TemplateBean {

    /**
     * The bean context.
     */
    private BeanContext context;

    /**
     * Creates a new template bean.
     *
     * @param context        the bean context
     */
    protected TemplateBean(BeanContext context) {
        this.context = context;
    }

    /**
     * Returns the bean context.
     *
     * @return the bean context
     */
    protected BeanContext getContext() {
        return context;
    }

    /**
     * Returns the bean context request.
     *
     * @return the bean context request
     */
    protected Request getContextRequest() {
        return context.getRequest();
    }
}
