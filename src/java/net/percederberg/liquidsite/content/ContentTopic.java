/*
 * ContentTopic.java
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
 * A discussion forum topic.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentTopic extends Content {

    /**
     * The locked content attribute.
     */
    private static final String LOCKED_ATTRIBUTE = "LOCKED";

    /**
     * Creates a new topic with default values.
     *
     * @param manager        the content manager to use
     * @param parent         the parent content forum
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public ContentTopic(ContentManager manager, ContentForum parent)
        throws ContentException {

        super(manager, parent.getDomain(), Content.TOPIC_CATEGORY);
        setParent(parent);
        setAttribute(LOCKED_ATTRIBUTE, "0");
    }

    /**
     * Creates a new topic.
     *
     * @param manager        the content manager to use
     * @param data           the content data object
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ContentTopic(ContentManager manager,
                           ContentData data,
                           DatabaseConnection con)
        throws ContentException {

        super(manager, data, con);
    }

    /**
     * Sets the topic name. The topic name must be numeric and
     * strictly increasing. If no name is set one will be assigned
     * automatically upon insertion in the database. Topic names
     * should NEVER be changed.
     *
     * @param name           the new name
     */
    public void setName(String name) {
        try {
            super.setName(String.valueOf(Integer.parseInt(name)));
        } catch (NumberFormatException e) {
            // Do nothing
        }
    }

    /**
     * Checks if the topic locked flag is set.
     *
     * @return true if the topic locked flag is set, or
     *         false otherwise
     */
    public boolean isLocked() {
        return getAttribute(LOCKED_ATTRIBUTE).equals("1");
    }

    /**
     * Sets the topic locked flag.
     *
     * @param locked         the new topic locked flag
     */
    public void setLocked(boolean locked) {
        if (locked) {
            setAttribute(LOCKED_ATTRIBUTE, "1");
        } else {
            setAttribute(LOCKED_ATTRIBUTE, "0");
        }
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
            throw new ContentException("no parent set for topic");
        }
        if (getName().equals("")) {
            setName(createName());
        }
    }

    /**
     * Creates a new name for the topic. The name will be the next
     * available number among the parent forum topics.
     *
     * @return the new name
     */
    private String createName() throws ContentException {
        ContentSelector  selector;
        Content[]        children;
        int              value;

        selector = new ContentSelector(getDomainName());
        selector.requireParent(getParent());
        selector.requireCategory(getCategory());
        selector.sortById(false);
        selector.limitResults(0, 1);
        children = InternalContent.findBySelector(getContentManager(),
                                                  selector);
        if (children.length == 0) {
            return "1";
        } else {
            value = Integer.parseInt(children[0].getName());
            return String.valueOf(value + 1);
        }
    }
}
