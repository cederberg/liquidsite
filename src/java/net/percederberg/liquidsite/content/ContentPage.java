/*
 * ContentPage.java
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
 * A web page.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentPage extends Content {

    /**
     * The template content attribute.
     */
    private static final String TEMPLATE_ATTRIBUTE = "TEMPLATE";

    /**
     * The page element content attribute prefix.
     */
    private static final String ELEMENT_PREFIX = "ELEMENT.";

    /**
     * Creates a new page with default values.
     * 
     * @param parent         the parent content object
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public ContentPage(Content parent) throws ContentException {
        super(parent.getDomain(), Content.PAGE_CATEGORY);
        setParent(parent);
    }

    /**
     * Creates a new page.
     * 
     * @param data           the content data object
     * @param latest         the latest revision flag
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected ContentPage(ContentData data, 
                          boolean latest, 
                          DatabaseConnection con) 
        throws ContentException {

        super(data, latest, con);
    }

    /**
     * Returns the page template.
     * 
     * @return the page template, or null for none
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public ContentTemplate getTemplate() throws ContentException {
        int  id = getTemplateId();
        
        if (id <= 0) {
            return null;
        } else {
            return (ContentTemplate) getContentManager().getContent(id);
        }
    }

    /**
     * Sets the page template.
     * 
     * @param template       the new template, or null for none
     */
    public void setTemplate(ContentTemplate template) {
        if (template == null) {
            setTemplateId(0);
        } else {
            setTemplateId(template.getId());
        }
    }

    /**
     * Returns the tempate content identifier.
     * 
     * @return the template content identifier
     */
    public int getTemplateId() {
        return Integer.parseInt(getAttribute(TEMPLATE_ATTRIBUTE));
    }
    
    /**
     * Sets the template content identifier.
     * 
     * @param template       the new template identifier
     */
    public void setTemplateId(int template) {
        setAttribute(TEMPLATE_ATTRIBUTE, String.valueOf(template));
    }
    
    /**
     * Returns all page element names in this page and it's template. 
     * The returned collection is guaranteed to not contain any 
     * duplicates.
     * 
     * @return a collection of page element names
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Collection getAllElementNames() throws ContentException {
        ContentTemplate  template;
        Collection       res = getLocalElementNames();
        Iterator         iter;
        Object           obj;

        // TODO: check security implications of this...
        if (getTemplateId() > 0) {
            template = getTemplate();
            iter = template.getAllElementNames().iterator();
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
     * Returns all page element names in this page. If a page element 
     * is not present in this collection, it may still be present in 
     * the template and thus returned by getElement().
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
     * retrieved from this page if possible, or from the template. 
     * The template parents will also be searched in the inheritance 
     * order, starting from the page template.
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
        ContentTemplate  template;
        String           data;
        
        data = getAttribute(ELEMENT_PREFIX + name.toLowerCase());
        if (data == null && getTemplateId() > 0) {
            template = getTemplate();
            data = template.getElement(name);
        }
        return data; 
    }
    
    /**
     * Sets the data for a named page element. The element will be
     * removed from this page if the data value is null. The page
     * element values in the template will NOT be modified.
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
        if (getParent() == null) {
            throw new ContentException("no parent set for page");
        }
        children = Content.findByParent(getParent());
        for (int i = 0; i < children.length; i++) {
            if (children[i].getId() != getId()
             && children[i].getName().equals(getName())) {

                throw new ContentException(
                    "another object with the same name is already " +
                    "present in the same folder");
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
