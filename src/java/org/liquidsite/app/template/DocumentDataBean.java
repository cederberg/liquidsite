/*
 * DocumentDataBean.java
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

package org.liquidsite.app.template;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;

import org.liquidsite.core.content.ContentDocument;
import org.liquidsite.core.content.DocumentProperty;
import org.liquidsite.core.text.HtmlFormatter;
import org.liquidsite.core.text.PlainFormatter;
import org.liquidsite.core.text.TaggedFormatter;
import org.liquidsite.util.log.Log;

/**
 * A document template bean. This class is used to access document
 * content from the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DocumentDataBean implements TemplateHashModel {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DocumentDataBean.class);

    /**
     * The bean context.
     */
    private BeanContext context;

    /**
     * The document being encapsulated.
     */
    private ContentDocument document;

    /**
     * The document formatting context.
     */
    private DocumentFormattingContext docContext = null;

    /**
     * Creates a new document data template bean.
     *
     * @param context        the bean context
     * @param document       the content document, or null
     */
    DocumentDataBean(BeanContext context, ContentDocument document) {
        this.context = context;
        this.document = document;
    }

    /**
     * Checks if the hash model is empty.
     *
     * @return false as the hash model is never empty
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns a document property as a template model.
     *
     * @param id             the document property id
     *
     * @return the template model object, or
     *         an empty scalar if the document property wasn't found
     */
    public TemplateModel get(String id) {
        String  str;
        int     type;

        if (document == null) {
            return new SimpleScalar("");
        } else {
            str = document.getProperty(id);
            type = document.getPropertyType(id);
            try {
                if (type == DocumentProperty.TAGGED_TYPE) {
                    str = TaggedFormatter.formatHtml(str, getDocContext());
                } else if (type == DocumentProperty.HTML_TYPE) {
                    str = HtmlFormatter.formatHtml(str, getDocContext());
                } else {
                    str = PlainFormatter.formatHtml(str);
                }
                return new SimpleScalar(str);
            } catch (Exception e) {
                e.printStackTrace();
                str = "runtime exception encountered while formatting '" +
                      id + "' of type " + type;
                LOG.error(str, e);
                return new SimpleScalar("");
            }
        }
    }

    /**
     * Returns the document formatting context. If no document
     * formatting context exists, a new one will be created.
     *
     * @return the document formatting context
     */
    private DocumentFormattingContext getDocContext() {
        if (docContext == null) {
            docContext = new DocumentFormattingContext(context, document);
        }
        return docContext;
    }
}
