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

import net.percederberg.liquidsite.content.ContentDocument;

/**
 * A document template bean. This class is used to access document
 * content from the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DocumentBean implements TemplateHashModel {

    /**
     * The document being encapsulated. 
     */
    private ContentDocument document;

    /**
     * The document meta-data bean.
     */
    private MetaDataBean metadata;

    /**
     * Creates a new document template bean.
     * 
     * @param document       the content document
     */
    DocumentBean(ContentDocument document) {
        this.document = document;
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
        if (id.equals("name")) {
            return new SimpleScalar(document.getName());
        } else if (id.equals("meta")) {
            return metadata;
        } else {
            // TODO: text data should have <br/> inserted?
            return new SimpleScalar(document.getProperty(id));
        }
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
            if (name.equals("id")) {
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
