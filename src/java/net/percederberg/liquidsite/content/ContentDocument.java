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
     * @param parent         the parent content section
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public ContentDocument(ContentSection parent) throws ContentException {
        super(parent.getDomain(), Content.DOCUMENT_CATEGORY);
        setParent(parent);
    }

    /**
     * Creates a new document.
     * 
     * @param data           the content data object
     * @param latest         the latest revision flag
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected ContentDocument(ContentData data, 
                              boolean latest, 
                              DatabaseConnection con) 
        throws ContentException {

        super(data, latest, con);
    }

    /**
     * Returns an identified document property value.
     * 
     * @param id             the document property identifier 
     * 
     * @return the document property value, or
     *         null if not found
     */
    public String getProperty(String id) {
        return getAttribute(PROPERTY_PREFIX + id);
    }

    /**
     * Sets a document property value. If the value specified is
     * null, the specified property will be removed.
     * 
     * @param id             the document property identifier
     * @param value          the document property value, or null
     */
    public void setDocumentProperty(String id, String value) {
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
