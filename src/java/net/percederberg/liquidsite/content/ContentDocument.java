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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.ContentData;

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
    private static final String PROPERTY_PREFIX = "PROPERTY.";

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
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected ContentDocument(ContentManager manager,
                              ContentData data, 
                              DatabaseConnection con) 
        throws ContentException {

        super(manager, data, con);
    }

    /**
     * Returns all property names in this document. Note that this
     * set may differ from what the section specifies, normally due
     * to some properties not being defined in the document. If a
     * document has been moved, however, additional properties may be
     * present.
     * 
     * @return a collection of property names
     */
    public Collection getPropertyNames() {
        ArrayList  list = new ArrayList();
        Iterator   iter = getAttributeNames();
        String     name;
        
        while (iter.hasNext()) {
            name = iter.next().toString();
            if (name.startsWith(PROPERTY_PREFIX)) {
                list.add(name.substring(PROPERTY_PREFIX.length()));
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
    }

    /**
     * Validates this data object. This method checks that all 
     * required fields have been filled with suitable values.
     * 
     * @throws ContentException if the data object contained errors
     */
    public void validate() throws ContentException {
        super.validate();
        if (getParent() == null) {
            throw new ContentException("no parent set for document");
        }
    }
}
