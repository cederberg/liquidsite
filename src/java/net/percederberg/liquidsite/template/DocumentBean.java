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

import java.util.ArrayList;

import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentDocument;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentFile;
import org.liquidsite.core.content.ContentSection;
import org.liquidsite.core.content.ContentSelector;
import org.liquidsite.util.log.Log;

/**
 * A document template bean. This class is used to access document
 * content from the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DocumentBean extends ContentBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DocumentBean.class);

    /**
     * The base section of the document being encapsulated. If set
     * the document name will have any additional sections between
     * the document and the base section added to the "name"
     * property.
     */
    private ContentSection baseSection;

    /**
     * The document data bean.
     */
    private DocumentDataBean data = null;

    /**
     * The document file beans.
     */
    private ArrayList files = null;

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

        super(context, document);
        this.baseSection = baseSection;
    }

    /**
     * Returns the name of the document.
     *
     * @return the document name, or
     *         an empty string for a non-existing document
     */
    public String getName() {
        try {
            return getName(getContent());
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            return "";
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
                return content.getName();
            } else {
                return getName(content.getParent()) +
                       content.getName();
            }
        } else {
            return "";
        }
    }

    /**
     * Returns the document data.
     *
     * @return the document data object, or
     *         an empty data object for a non-existing document
     */
    public DocumentDataBean getData() {
        if (data == null) {
            data = new DocumentDataBean(getContext(),
                                        (ContentDocument) getContent());
        }
        return data;
    }

    /**
     * Returns a list of all the document files.
     *
     * @return the document file list, or
     *         an empty list of a non-existing document
     */
    public ArrayList getFiles() {
        ContentSelector   selector;
        Content[]         content;
        DocumentFileBean  file;

        if (files == null) {
            files = new ArrayList();
            if (getContent() != null) {
                try {
                    selector = new ContentSelector(getContent().getDomain());
                    selector.requireParent(getContent());
                    selector.requireCategory(Content.FILE_CATEGORY);
                    selector.sortByName(true);
                    selector.limitResults(0, 100);
                    content = getContext().findContent(selector);
                    for (int i = 0; i < content.length; i++) {
                        file = new DocumentFileBean(getContext(),
                                                    this,
                                                    (ContentFile) content[i]);
                        files.add(file);
                    }
                } catch (ContentException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
        return files;
    }
}
