/*
 * ContentPost.java
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
 * A discussion forum post.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentPost extends Content {

    /**
     * The subject content attribute.
     */
    private static final String SUBJECT_ATTRIBUTE = "SUBJECT";

    /**
     * The text content attribute.
     */
    private static final String TEXT_ATTRIBUTE = "TEXT";

    /**
     * The text type content attribute.
     */
    private static final String TEXT_TYPE_ATTRIBUTE = "TEXTTYPE";

    /**
     * The plain text type constant. This text type is used for text
     * without other formatting than linebreaks. All text will appear
     * just as typed and may be escaped depending on output media.
     */
    public static final int PLAIN_TEXT_TYPE = 1;

    /**
     * The tagged text type constant. This text type is used for text
     * with simplified tag formatting, similar to the tagged text used
     * document properties.
     */
    public static final int TAGGED_TEXT_TYPE = 2;

    /**
     * Creates a new post with default values.
     *
     * @param manager        the content manager to use
     * @param parent         the parent content topic
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public ContentPost(ContentManager manager, ContentTopic parent)
        throws ContentException {

        super(manager, parent.getDomain(), Content.POST_CATEGORY);
        setParent(parent);
        setAttribute(SUBJECT_ATTRIBUTE, "");
        setAttribute(TEXT_ATTRIBUTE, "");
        setAttribute(TEXT_TYPE_ATTRIBUTE, String.valueOf(PLAIN_TEXT_TYPE));
    }

    /**
     * Creates a new post.
     *
     * @param manager        the content manager to use
     * @param data           the content data object
     * @param src            the data source to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ContentPost(ContentManager manager,
                          ContentData data,
                          DataSource src)
        throws ContentException {

        super(manager, data, src);
    }

    /**
     * Sets the post name. The post name must be numeric and strictly
     * increasing. If no name is set one will be assigned
     * automatically upon insertion in the database. Post names should
     * NEVER be changed.
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
     * Returns the post subject.
     *
     * @return the post subject
     */
    public String getSubject() {
        return getAttribute(SUBJECT_ATTRIBUTE);
    }

    /**
     * Sets the post subject.
     *
     * @param subject        the new post subject
     */
    public void setSubject(String subject) {
        setAttribute(SUBJECT_ATTRIBUTE, subject);
    }

    /**
     * Returns the post text.
     *
     * @return the post text
     */
    public String getText() {
        return getAttribute(TEXT_ATTRIBUTE);
    }

    /**
     * Sets the post text.
     *
     * @param text           the new post text
     */
    public void setText(String text) {
        setAttribute(TEXT_ATTRIBUTE, text);
    }

    /**
     * Returns the post text type.
     *
     * @return the post text type
     *
     * @see #PLAIN_TEXT_TYPE
     * @see #TAGGED_TEXT_TYPE
     */
    public int getTextType() {
        return Integer.parseInt(getAttribute(TEXT_TYPE_ATTRIBUTE));
    }

    /**
     * Sets the post text type.
     *
     * @param type           the new post text type
     *
     * @see #PLAIN_TEXT_TYPE
     * @see #TAGGED_TEXT_TYPE
     */
    public void setTextType(int type) {
        setAttribute(TEXT_TYPE_ATTRIBUTE, String.valueOf(type));
    }

    /**
     * Validates the object data before writing to the database.
     *
     * @throws ContentException if the object data wasn't valid
     */
    protected void doValidate() throws ContentException {
        Content  parent;

        parent = getParent();
        if (getName().equals("")) {
            setName(createName(parent));
        }
        super.doValidate();
        if (parent == null) {
            throw new ContentException("no parent set for post");
        } else if (parent.getCategory() != Content.TOPIC_CATEGORY) {
            throw new ContentException("post parent must be topic");
        }
    }

    /**
     * Inserts the object data into the database. If the restore flag
     * is set, no automatic changes should be made to the data before
     * writing to the database.
     *
     * @param src            the data source to use
     * @param user           the user performing the operation
     * @param restore        the restore flag
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void doInsert(DataSource src, User user, boolean restore)
        throws ContentException {

        ContentTopic  parent;

        super.doInsert(src, user, restore);
        if (!restore) {
            parent = (ContentTopic) getParent();
            parent.doUpdate(src, user);
        }
    }

    /**
     * Creates a new name for the post. The name will be the next
     * available number among the parent thread posts.
     *
     * @param parent         the content parent
     *
     * @return the new name
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String createName(Content parent)
        throws ContentException {

        ContentSelector  selector;
        Content[]        children;
        int              value;

        selector = new ContentSelector(getDomainName());
        selector.requireParent(parent);
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
