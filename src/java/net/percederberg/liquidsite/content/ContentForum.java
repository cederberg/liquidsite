/*
 * ContentForum.java
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

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.ContentData;

/**
 * A discussion forum.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentForum extends Content {

    /**
     * The real name content attribute.
     */
    private static final String REAL_NAME_ATTRIBUTE = "REALNAME";

    /**
     * The description content attribute.
     */
    private static final String DESCRIPTION_ATTRIBUTE = "DESCRIPTION";

    /**
     * Creates a new forum with default values.
     *
     * @param manager        the content manager to use
     * @param parent         the parent content section
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public ContentForum(ContentManager manager, ContentSection parent)
        throws ContentException {

        super(manager, parent.getDomain(), Content.FORUM_CATEGORY);
        setParent(parent);
        setAttribute(REAL_NAME_ATTRIBUTE, "");
        setAttribute(DESCRIPTION_ATTRIBUTE, "");
    }

    /**
     * Creates a new forum.
     *
     * @param manager        the content manager to use
     * @param data           the content data object
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ContentForum(ContentManager manager,
                           ContentData data,
                           DatabaseConnection con)
        throws ContentException {

        super(manager, data, con);
    }

    /**
     * Returns the real forum name.
     *
     * @return the real forum name
     */
    public String getRealName() {
        return getAttribute(REAL_NAME_ATTRIBUTE);
    }

    /**
     * Sets the real forum name.
     *
     * @param name           the new forum name
     */
    public void setRealName(String name) {
        setAttribute(REAL_NAME_ATTRIBUTE, name);
    }

    /**
     * Returns the forum description.
     *
     * @return the forum description
     */
    public String getDescription() {
        return getAttribute(DESCRIPTION_ATTRIBUTE);
    }

    /**
     * Sets the forum description.
     *
     * @param description    the new forum description
     */
    public void setDescription(String description) {
        setAttribute(DESCRIPTION_ATTRIBUTE, description);
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
            throw new ContentException("no parent set for forum");
        } else if (getRealName().equals("")) {
            throw new ContentException("no real name set for forum");
        }
        children = InternalContent.findByParent(getContentManager(),
                                                getParent());
        for (int i = 0; i < children.length; i++) {
            if (children[i].getId() != getId()
             && children[i].getName().equals(getName())) {

                throw new ContentException(
                    "another object with the same name is already " +
                    "present in the parent folder");
            }
        }
    }
}
