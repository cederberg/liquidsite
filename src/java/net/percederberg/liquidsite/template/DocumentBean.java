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

import java.util.Date;

import freemarker.template.SimpleScalar;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSection;

/**
 * A document template bean. This class is used to access document
 * content from the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DocumentBean {

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
     * The document data bean.
     */
    private DocumentDataBean data = null;

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
     * Returns the document identifier.
     *
     * @return the document identifier number, or
     *         zero (0) for a non-existing document
     */
    public int getId() {
        if (document == null) {
            return 0;
        } else {
            return document.getId();
        }
    }

    /**
     * Returns the name of the document.
     *
     * @return the document name, or
     *         an empty string for a non-existing document
     */
    public String getName() {
        try {
            return getName(document);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            return "";
        }
    }

    /**
     * Returns the full document path.
     *
     * @return the document path, or
     *         an empty string for a non-existing document
     */
    public String getPath() {
        String  path = context.getContentPath(document);

        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        } else {
            return path;
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
     * Returns the document parent (always a section).
     *
     * @return the document parent
     */
    public SectionBean getParent() {
        if (document != null) {
            try {
                return new SectionBean(context,
                                       (ContentSection) document.getParent());
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return new SectionBean();
    }

    /**
     * Returns the document revision number.
     *
     * @return the document revision number, or
     *         zero (0) for a non-existing document
     */
    public int getRevision() {
        if (document == null) {
            return 0;
        } else {
            return document.getRevisionNumber();
        }
    }

    /**
     * Returns the document revision date.
     *
     * @return the document revision date, or
     *         the current date and time for a non-existing document
     */
    public Date getDate() {
        if (document == null) {
            return new Date();
        } else {
            return document.getModifiedDate();
        }
    }

    /**
     * Returns the document revision author login name.
     *
     * @return the document revision user name, or
     *         an empty string for a non-existing document
     */
    public String getUser() {
        if (document == null) {
            return "";
        } else {
            return document.getAuthorName();
        }
    }

    /**
     * Returns the document online flag.
     *
     * @return the document online flag, or
     *         the false for a non-existing document
     */
    public boolean getOnline() {
        if (document == null) {
            return false;
        } else {
            return document.isOnline();
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
            data = new DocumentDataBean(context, document);
        }
        return data;
    }

    /**
     * Returns the document lock.
     *
     * @return the document lock object, or
     *         an empty lock for a non-existing document
     */
    public LockBean getLock() {
        if (document != null) {
            try {
                return new LockBean(document.getLock());
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return new LockBean(null);
    }
}
