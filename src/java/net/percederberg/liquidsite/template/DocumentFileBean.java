/*
 * DocumentFileBean.java
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

import javax.servlet.ServletContext;

import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;

import org.liquidsite.util.log.Log;

/**
 * A document file bean. This class is used to access document files
 * from the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DocumentFileBean extends ContentBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DocumentFileBean.class);

    /**
     * Creates a new document file template bean.
     *
     * @param context        the bean context
     * @param parent         the parent document
     * @param file           the content file
     */
    DocumentFileBean(BeanContext context,
                     DocumentBean parent,
                     ContentFile file) {

        super(context, parent, file);
    }

    /**
     * Returns the file size.
     *
     * @return the file size
     */
    public long getSize() {
        try {
            return ((ContentFile) getContent()).getFile().length();
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        }
        return 0L;
    }

    /**
     * Returns the file MIME type.
     *
     * @return the file MIME type
     */
    public String getMimeType() {
        ServletContext  context;

        context = getContext().getRequest().getServletContext();
        return ((ContentFile) getContent()).getMimeType(context);
    }
}
