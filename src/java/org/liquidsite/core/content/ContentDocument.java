/*
 * ContentDocument.java
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

package org.liquidsite.core.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.liquidsite.core.data.ContentData;
import org.liquidsite.core.data.DataSource;

/**
 * A content document.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentDocument extends Content {

    /**
     * The document property content attribute prefix.
     */
    static final String PROPERTY_PREFIX = "PROPERTY.";

    /**
     * The document property type content attribute prefix.
     */
    static final String PROPERTY_TYPE_PREFIX = "PROPERTYTYPE.";

    /**
     * Creates a new document with default values.
     *
     * @param manager        the content manager to use
     * @param parent         the parent content section
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public ContentDocument(ContentManager manager, ContentSection parent)
        throws ContentException {

        super(manager, parent.getDomain(), Content.DOCUMENT_CATEGORY);
        setParent(parent);
    }

    /**
     * Creates a new document.
     *
     * @param manager        the content manager to use
     * @param data           the content data object
     * @param src            the data source to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ContentDocument(ContentManager manager,
                              ContentData data,
                              DataSource src)
        throws ContentException {

        super(manager, data, src);
    }

    /**
     * Returns all property identfiers in this document. Note that
     * this set may differ from what the section specifies, normally
     * due to some properties not being defined in the document. If a
     * document has been moved, however, additional properties may be
     * present.
     *
     * @return a collection of property identifiers
     */
    public Collection getPropertyIdentifiers() {
        ArrayList  list = new ArrayList();
        Iterator   iter = getAttributeNames();
        String     id;

        while (iter.hasNext()) {
            id = iter.next().toString();
            if (id.startsWith(PROPERTY_PREFIX)) {
                list.add(id.substring(PROPERTY_PREFIX.length()));
            }
        }
        return list;
    }

    /**
     * Returns an identified document property value.
     *
     * @param id             the document property identifier
     *
     * @return the document property value, or
     *         an empty string if not found
     */
    public String getProperty(String id) {
        String  value = getAttribute(PROPERTY_PREFIX + id);

        return (value == null) ? "" : value;
    }

    /**
     * Sets a document property value. If the value specified is
     * null, the specified property will be removed.
     *
     * @param id             the document property identifier
     * @param value          the document property value, or null
     */
    public void setProperty(String id, String value) {
        setAttribute(PROPERTY_PREFIX + id, value);
        if (value == null) {
            setAttribute(PROPERTY_TYPE_PREFIX + id, null);
        }
    }

    /**
     * Returns an identified document property type.
     *
     * @param id             the document property identifier
     *
     * @return the document property type, or
     *         STRING_TYPE if not set
     *
     * @see DocumentProperty#STRING_TYPE
     * @see DocumentProperty#TAGGED_TYPE
     * @see DocumentProperty#HTML_TYPE
     */
    public int getPropertyType(String id) {
        String  value = getAttribute(PROPERTY_TYPE_PREFIX + id);

        if (value == null) {
            return DocumentProperty.STRING_TYPE;
        } else {
            return Integer.parseInt(value);
        }
    }

    /**
     * Sets a document property type.
     *
     * @param id             the document property identifier
     * @param type           the document property type
     */
    public void setPropertyType(String id, int type) {
        setAttribute(PROPERTY_TYPE_PREFIX + id, String.valueOf(type));
    }

    /**
     * Validates the object data before writing to the database.
     *
     * @throws ContentException if the object data wasn't valid
     */
    protected void doValidate() throws ContentException {
        Content[]  children;

        super.doValidate();
        if (getParent() == null) {
            throw new ContentException("no parent set for document");
        }
        children = InternalContent.findByParent(getContentManager(),
                                                getParent());
        for (int i = 0; i < children.length; i++) {
            if (children[i].getId() != getId()
             && children[i].getName().equals(getName())) {

                throw new ContentException(
                    "another object with the same name is already " +
                    "present in the parent section");
            }
        }
    }
}
