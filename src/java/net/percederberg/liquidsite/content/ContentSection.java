/*
 * ContentSection.java
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

package net.percederberg.liquidsite.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.ContentData;

/**
 * A content section.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentSection extends Content {

    /**
     * The description content attribute.
     */
    private static final String DESCRIPTION_ATTRIBUTE = "DESCRIPTION";

    /**
     * The document property  content attribute prefix.
     */
    private static final String DOCUMENT_PREFIX = "DOCUMENT.";

    /**
     * Creates a new root section with default values.
     *
     * @param manager        the content manager to use
     * @param domain         the domain
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public ContentSection(ContentManager manager, Domain domain)
        throws ContentException {

        super(manager, domain, Content.SECTION_CATEGORY);
        setParent(null);
        setAttribute(DESCRIPTION_ATTRIBUTE, "");
    }

    /**
     * Creates a new section with default values.
     *
     * @param manager        the content manager to use
     * @param parent         the parent content section
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public ContentSection(ContentManager manager, ContentSection parent)
        throws ContentException {

        super(manager, parent.getDomain(), Content.SECTION_CATEGORY);
        setParent(parent);
        setAttribute(DESCRIPTION_ATTRIBUTE, "");
    }

    /**
     * Creates a new section.
     *
     * @param manager        the content manager to use
     * @param data           the content data object
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ContentSection(ContentManager manager,
                             ContentData data,
                             DatabaseConnection con)
        throws ContentException {

        super(manager, data, con);
    }

    /**
     * Returns the section description.
     *
     * @return the section description, or
     *         an empty string if not set
     */
    public String getDescription() {
        String desc = getAttribute(DESCRIPTION_ATTRIBUTE);

        return (desc == null) ? "" : desc;
    }

    /**
     * Sets the section description.
     *
     * @param description     the new section description
     */
    public void setDescription(String description) {
        setAttribute(DESCRIPTION_ATTRIBUTE, description);
    }

    /**
     * Returns all document properties for this section. The document
     * properties define the available properties for any document
     * created in this section. If this array does contain any
     * properties at all, the parent section properties should be
     * used. The document properties array is ordered by increasing
     * positions.
     *
     * @return an array of all document properties for this section
     */
    public DocumentProperty[] getAllDocumentProperties() {
        ArrayList           list = new ArrayList();
        Iterator            iter = getAttributeNames();
        DocumentProperty[]  res;
        String              name;

        while (iter.hasNext()) {
            name = iter.next().toString();
            if (name.startsWith(DOCUMENT_PREFIX)) {
                name = name.substring(DOCUMENT_PREFIX.length());
                list.add(getDocumentProperty(name));
            }
        }
        Collections.sort(list);
        res = new DocumentProperty[list.size()];
        list.toArray(res);
        return res;
    }

    /**
     * Returns an identified document property. The document
     * properties define the available properties for any document
     * created in this section. Note that a given document does not
     * have to specify any of the properties specified in it's parent
     * section, as documents can be moved between sections.
     *
     * @param id             the document property identifier
     *
     * @return the document property, or
     *         null if not found
     */
    public DocumentProperty getDocumentProperty(String id) {
        String  str;

        str = getAttribute(DOCUMENT_PREFIX + id);
        if (str == null) {
            return null;
        } else {
            return new DocumentProperty(id, str);
        }
    }

    /**
     * Sets a document property. If the document property specified
     * is null, the specified document property will be removed.
     *
     * @param id             the document property identifier
     * @param property       the document property, or null
     */
    public void setDocumentProperty(String id, DocumentProperty property) {
        if (property == null) {
            setAttribute(DOCUMENT_PREFIX + id, null);
        } else {
            setAttribute(DOCUMENT_PREFIX + id, property.encodeXml());
        }
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
            children = InternalContent.findByParent(getContentManager(),
                                                    getDomain());
        } else {
            children = InternalContent.findByParent(getContentManager(),
                                                    getParent());
        }
        for (int i = 0; i < children.length; i++) {
            if (children[i].getId() != getId()
             && children[i].getName().equals(getName())
             && children[i].getCategory() == SECTION_CATEGORY) {

                throw new ContentException(
                    "another section with the same name is already " +
                    "present in the parent section");
            }
        }
    }
}
