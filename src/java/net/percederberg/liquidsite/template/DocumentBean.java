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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.template;

import freemarker.template.SimpleDate;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.content.DocumentProperty;

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
     * The document meta-data bean.
     */
    private MetaDataBean metadata;

    /**
     * Creates a new document template bean.
     * 
     * @param document       the content document, or null
     */
    DocumentBean(ContentDocument document) {
        this(document, null);
    }

    /**
     * Creates a new document template bean.
     * 
     * @param document       the content document, or null
     * @param baseSection    the base section for the document
     */
    DocumentBean(ContentDocument document, ContentSection baseSection) {
        this.document = document;
        this.baseSection = baseSection;
        this.metadata = new MetaDataBean(document);
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
     */
    public TemplateModel get(String id) {
        String  str;

        if (id.equals("meta")) {
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
            if (document.getPropertyType(id) != DocumentProperty.HTML_TYPE) {
                str = createHtmlText(str);
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
     * Creates an HTML escaped version of a string. This method will
     * escape any occurencies of special HTML characters while also
     * inserting HTML linebreaks instead of normal line breaks.
     *
     * @param str            the string to process
     *
     * @return the HTML encoded string
     */
    private String createHtmlText(String str) {
        StringBuffer  buffer = new StringBuffer();
        char          c;
        
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (c == '<') {
                buffer.append("&lt;");
            } else if (c == '>') {
                buffer.append("&gt;");
            } else if (c == '&') {
                buffer.append("&amp;");
            } else if (c == '\n') {
                buffer.append("<br/>");
            } else if (c == '\r') {
                // Discard
            } else {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }


    /**
     * A document meta-data bean.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    private class MetaDataBean implements TemplateHashModel {

        /**
         * The document being encapsulated. 
         */
        private ContentDocument document;

        /**
         * Creates a new meta-data bean.
         * 
         * @param document       the content document
         */
        public MetaDataBean(ContentDocument document) {
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
         * Returns a meta-data property as a template model.
         * 
         * @param name           the property name
         * 
         * @return the template model object, or
         *         null if the property wasn't found
         */
        public TemplateModel get(String name) {
            if (document == null) {
                return new SimpleScalar("");
            } else if (name.equals("id")) {
                return new SimpleNumber(document.getId());
            } else if (name.equals("revision")) {
                return new SimpleNumber(document.getRevisionNumber());
            } else if (name.equals("date")) {
                return new SimpleDate(document.getModifiedDate(),
                                      SimpleDate.DATETIME);
            } else if (name.equals("user")) {
                return new SimpleScalar(document.getAuthorName());
            } else {
                return null;
            }
        }
    }
}
