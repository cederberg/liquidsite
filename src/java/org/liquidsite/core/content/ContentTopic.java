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

package org.liquidsite.core.content;

import org.liquidsite.core.data.ContentData;
import org.liquidsite.core.data.DataSource;

/**
 * A discussion forum topic.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentTopic extends Content {

    /**
     * The subject content attribute.
     */
    private static final String SUBJECT_ATTRIBUTE = "SUBJECT";

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
        setAttribute(SUBJECT_ATTRIBUTE, "");
        setAttribute(LOCKED_ATTRIBUTE, "0");
    }

    /**
     * Creates a new topic.
     *
     * @param manager        the content manager to use
     * @param data           the content data object
     * @param src            the data source to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ContentTopic(ContentManager manager,
                           ContentData data,
                           DataSource src)
        throws ContentException {

        super(manager, data, src);
    }

    /**
     * Sets the topic name. The topic name must be numeric and if
     * assigned manually, care must be taken to make sure all topics
     * in a forum have assigned names (or errors may occur). If no
     * name is set one will be assigned automatically upon insertion
     * in the database. Topic names should NEVER be changed.
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
     * Returns the topic subject.
     *
     * @return the topic subject
     */
    public String getSubject() {
        return getAttribute(SUBJECT_ATTRIBUTE);
    }

    /**
     * Sets the topic subject.
     *
     * @param subject        the new topic subject
     */
    public void setSubject(String subject) {
        setAttribute(SUBJECT_ATTRIBUTE, subject);
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
     * Validates the object data before writing to the database.
     *
     * @throws ContentException if the object data wasn't valid
     */
    protected void doValidate() throws ContentException {
        Content  parent;

        if (getName().equals("")) {
            setName(createName());
        }
        super.doValidate();
        parent = getParent();
        if (parent == null) {
            throw new ContentException("no parent set for topic");
        } else if (parent.getCategory() != Content.FORUM_CATEGORY) {
            throw new ContentException("topic parent must be forum");
        } else if (getSubject().equals("")) {
            throw new ContentException("no topic title set");
        }
    }

    /**
     * Creates a new name for the topic. The name will be the next
     * available number among the parent forum topics.
     *
     * @return the new name
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
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
