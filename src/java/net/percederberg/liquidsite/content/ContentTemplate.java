/*
 * ContentTemplate.java
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
import java.util.Collection;
import java.util.Iterator;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.ContentData;

/**
 * A web page template.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentTemplate extends Content {

    /**
     * The page element content attribute prefix.
     */
    private static final String ELEMENT_PREFIX = "ELEMENT.";

    /**
     * Creates a new template with default values.
     *
     * @param manager        the content manager to use
     * @param domain         the template domain
     */
    public ContentTemplate(ContentManager manager, Domain domain) {
        super(manager, domain, Content.TEMPLATE_CATEGORY);
    }

    /**
     * Creates a new template with default values.
     *
     * @param manager        the content manager to use
     * @param parent         the parent template
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public ContentTemplate(ContentManager manager, ContentTemplate parent)
        throws ContentException {

        this(manager, parent.getDomain());
        setParent(parent);
    }

    /**
     * Creates a new template.
     *
     * @param manager        the content manager to use
     * @param data           the content data object
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ContentTemplate(ContentManager manager,
                              ContentData data,
                              DatabaseConnection con)
        throws ContentException {

        super(manager, data, con);
    }

    /**
     * Returns all page element names in this template and it's
     * parents. The returned collection is guaranteed to not contain
     * any duplicates.
     *
     * @return a collection of page element names
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public Collection getAllElementNames() throws ContentException {
        ContentTemplate  parent;
        Collection       res = getLocalElementNames();
        Iterator         iter;
        Object           obj;

        if (getParentId() > 0) {
            parent = (ContentTemplate) getParent();
            iter = parent.getAllElementNames().iterator();
            while (iter.hasNext()) {
                obj = iter.next();
                if (!res.contains(obj)) {
                    res.add(obj);
                }
            }
        }
        return res;
    }

    /**
     * Returns all page element names in this template. If a page
     * element is not present in this collection, it may still be
     * present in a parent template and thus returned by
     * getElement().
     *
     * @return a collection of page element names
     */
    public Collection getLocalElementNames() {
        ArrayList  list = new ArrayList();
        Iterator   iter = getAttributeNames();
        String     name;

        while (iter.hasNext()) {
            name = iter.next().toString();
            if (name.startsWith(ELEMENT_PREFIX)) {
                list.add(name.substring(ELEMENT_PREFIX.length()));
            }
        }
        return list;
    }

    /**
     * Returns a named page element. The element data will be
     * retrieved from this template if possible, or from one of the
     * parent templates. The templates will be searched in the
     * inheritance order, starting from this template.
     *
     * @param name           the page element name
     *
     * @return the page element data, or
     *         null if the page element didn't exist
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public String getElement(String name) throws ContentException {
        ContentTemplate  parent;
        String           data;

        data = getAttribute(ELEMENT_PREFIX + name.toLowerCase());
        if (data == null && getParentId() > 0) {
            parent = (ContentTemplate) getParent();
            data = parent.getElement(name);
        }
        return data;
    }

    /**
     * Sets the data for a named page element. The element will be
     * removed from this template if the data value is null. The page
     * element values in the parent templates will NOT be modified.
     *
     * @param name           the page element name
     * @param data           the page element data, or null
     */
    public void setElement(String name, String data) {
        setAttribute(ELEMENT_PREFIX + name.toLowerCase(), data);
    }

    /**
     * Validates this data object. This method checks that all
     * required fields have been filled with suitable values.
     *
     * @throws ContentException if the data object contained errors
     */
    public void validate() throws ContentException {
        Content[]  children;
        Iterator   iter = getLocalElementNames().iterator();
        String     str;
        char       c;

        super.validate();
        if (getParent() != null) {
            children = InternalContent.findByParent(getContentManager(),
                                                    getParent());
            for (int i = 0; i < children.length; i++) {
                if (children[i].getId() != getId()
                 && children[i].getName().equals(getName())) {

                    throw new ContentException(
                        "another template with the same name " +
                        "already exists");
                }
            }
        }
        while (iter.hasNext()) {
            str = iter.next().toString();
            for (int i = 0; i < str.length(); i++) {
                c = str.charAt(i);
                if ((c < 'a' || c > 'z') && (c < '0' || c > '9')) {
                    throw new ContentException(
                        "page element name contains invalid " +
                        "characters '" + str + "'");
                }
            }
        }
    }
}
