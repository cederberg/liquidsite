/*
 * DocumentBean.java
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

import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.content.DocumentProperty;
import net.percederberg.liquidsite.text.HtmlFormatter;
import net.percederberg.liquidsite.text.PlainFormatter;
import net.percederberg.liquidsite.text.TaggedFormatter;

/**
 * A document template bean. This class is used to access document
 * content from the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DocumentBean implements TemplateHashModel {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DocumentBean.class);

    /**
     * The bean context.
     */
    private BeanContext context;

    /**
     * The document being encapsulated.
     */
    private ContentDocument document;

    /**
     * The base section of the document being encapsulated. If set
     * the document name will have any additional sections between
     * the document and the base section added to the "name"
     * property.
     */
    private ContentSection baseSection;

    /**
     * The document formatting context.
     */
    private DocumentFormattingContext docContext = null;

    /**
     * The document meta-data template model.
     */
    private TemplateModel metadata = null;

    /**
     * Creates a new empty document template bean.
     */
    DocumentBean() {
        this(null, null, null);
    }

    /**
     * Creates a new document template bean from the request
     * environment document.
     *
     * @param context        the bean context
     */
    DocumentBean(BeanContext context) {
        this(context,
             context.getRequest().getEnvironment().getDocument(),
             null);
    }

    /**
     * Creates a new document template bean.
     *
     * @param context        the bean context
     * @param document       the content document, or null
     * @param baseSection    the base section for the document
     */
    DocumentBean(BeanContext context,
                 ContentDocument document,
                 ContentSection baseSection) {

        this.context = context;
        this.document = document;
        this.baseSection = baseSection;
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
     * Returns a document property as a template model. The special
     * properties "name" and "meta" return the document name and
     * meta-data object respectively.
     *
     * @param id             the document property id
     *
     * @return the template model object, or
     *         null if the document property wasn't found
     *
     * @throws TemplateModelException if an appropriate template model
     *             couldn't be created
     */
    public TemplateModel get(String id) throws TemplateModelException {
        Object  obj;
        String  str;
        int     type;

        if (id.equals("meta")) {
            if (metadata == null) {
                obj = new DocumentMetaDataBean(context, document);
                metadata = ObjectWrapper.BEANS_WRAPPER.wrap(obj);
            }
            return metadata;
        } else if (document == null) {
            return new SimpleScalar("");
        } else if (id.equals("name")) {
            try {
                return new SimpleScalar(getName(document));
            } catch (ContentException e) {
                LOG.error(e.getMessage());
                return new SimpleScalar("");
            }
        } else {
            str = document.getProperty(id);
            type = document.getPropertyType(id);
            if (type == DocumentProperty.TAGGED_TYPE) {
                str = TaggedFormatter.formatHtml(str, getDocContext());
            } else if (type == DocumentProperty.HTML_TYPE) {
                str = HtmlFormatter.formatHtml(str, getDocContext());
            } else {
                str = PlainFormatter.formatHtml(str);
            }
            return new SimpleScalar(str);
        }
    }

    /**
     * Returns the name of a content object. The name will include
     * any document section up to the base section.
     *
     * @param content        the content object
     *
     * @return the content object name
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getName(Content content)
        throws ContentException {

        if (content instanceof ContentSection) {
            if (baseSection.equals(content)) {
                return "";
            } else {
                return getName(content.getParent()) +
                       content.getName() + "/";
            }
        } else if (content instanceof ContentDocument) {
            if (baseSection == null) {
                return document.getName();
            } else {
                return getName(document.getParent()) +
                       document.getName();
            }
        } else {
            return "";
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
