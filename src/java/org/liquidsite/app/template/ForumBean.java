/*
 * ForumBean.java
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

package org.liquidsite.app.template;

import java.util.ArrayList;

import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentForum;
import org.liquidsite.core.content.ContentSelector;
import org.liquidsite.core.content.ContentTopic;
import org.liquidsite.util.log.Log;

/**
 * A forum template bean. This class is used to access forums from
 * the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ForumBean extends ContentBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(ForumBean.class);

    /**
     * The first topic in the forum. This variable is set upon the
     * first request.
     */
    private TopicBean first = null;

    /**
     * The last topic in the forum. This variable is set upon the
     * first request.
     */
    private TopicBean last = null;

    /**
     * Creates a new empty forum template bean.
     */
    ForumBean() {
        this(null, null);
    }

    /**
     * Creates a new forum template bean based on the request
     * environment forum.
     *
     * @param context        the bean context
     */
    ForumBean(BeanContext context) {
        this(context, context.getRequest().getEnvironment().getForum());
    }

    /**
     * Creates a new forum template bean.
     *
     * @param context        the bean context
     * @param forum          the content forum, or null
     */
    ForumBean(BeanContext context, ContentForum forum) {
        super(context, forum);
    }

    /**
     * Returns the real forum name.
     *
     * @return the real forum name, or
     *         an empty string if the forum doesn't exist
     */
    public String getRealName() {
        if (getContent() != null) {
            return ((ContentForum) getContent()).getRealName();
        }
        return "";
    }

    /**
     * Returns the forum description.
     *
     * @return the forum description, or
     *         an empty string if the forum doesn't exist
     */
    public String getDescription() {
        if (getContent() != null) {
            return ((ContentForum) getContent()).getDescription();
        }
        return "";
    }

    /**
     * Checks if the current user is a forum moderator.
     *
     * @return true if the current user is a forum moderator, or
     *         false otherwise
     */
    public boolean getModerator() {
        String  moderator;

        if (getContent() != null) {
            moderator = ((ContentForum) getContent()).getModeratorName();
            return getContext().findUser("").inGroup(moderator);
        }
        return false;
    }

    /**
     * Returns the first topic in this forum. The topics are ordered
     * in modification date order.
     *
     * @return the first topic in this forum, or
     *         an empty topic if the forum doesn't exist
     */
    public TopicBean getFirst() {
        ContentSelector  selector;

        if (first == null) {
            if (getContent() != null) {
                try {
                    selector = createTopicSelector();
                    selector.sortByModified(true);
                    selector.limitResults(0, 1);
                    first = createTopic(selector);
                } catch (ContentException e) {
                    LOG.error(e.getMessage());
                }
            }
            if (first == null) {
                first = new TopicBean();
            }
        }
        return first;
    }

    /**
     * Returns the last topic in this forum. The topics are ordered in
     * modification date order.
     *
     * @return the last topic in this forum, or
     *         an empty topic if the forum doesn't exist
     */
    public TopicBean getLast() {
        ContentSelector  selector;

        if (last == null) {
            if (getContent() != null) {
                try {
                    selector = createTopicSelector();
                    selector.sortByModified(false);
                    selector.limitResults(0, 1);
                    last = createTopic(selector);
                } catch (ContentException e) {
                    LOG.error(e.getMessage());
                }
            }
            if (last == null) {
                last = new TopicBean();
            }
        }
        return last;
    }

    /**
     * Returns the number of topics in this forum.
     *
     * @return the number of topics in this forum
     */
    public int getTopicCount() {
        if (getContent() != null) {
            try {
                return getContext().countContent(createTopicSelector());
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return 0;
    }

    /**
     * Returns all topics in this forum. At most the specified number
     * of topics will be returned. The topics will be ordered by last
     * modification date.
     *
     * @param offset         the number of topics to skip
     * @param count          the maximum number of topics
     *
     * @return a list of the topics found (as topic beans)
     */
    public ArrayList findTopics(int offset, int count) {
        ArrayList        results = new ArrayList();
        ContentSelector  selector;
        Content[]        content;

        if (getContent() != null) {
            try {
                selector = createTopicSelector();
                selector.sortByModified(false);
                selector.limitResults(offset, count);
                content = getContext().findContent(selector);
                for (int i = 0; i < content.length; i++) {
                    results.add(new TopicBean(getContext(),
                                              (ContentTopic) content[i]));
                }
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return results;
    }

    /**
     * Creates a content selector for finding all topics in this
     * forum.
     *
     * @return a content topic selector
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private ContentSelector createTopicSelector()
        throws ContentException {

        ContentSelector  selector;

        selector = new ContentSelector(getContent().getDomain());
        selector.requireParent(getContent());
        selector.requireCategory(Content.TOPIC_CATEGORY);
        return selector;
    }

    /**
     * Returns the first content topic matching the specified
     * selector.
     *
     * @param selector       the content selector to use
     *
     * @return the topic bean for the found topic, or
     *         null if no matching topics were found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private TopicBean createTopic(ContentSelector selector)
        throws ContentException {

        Content[]  content;

        content = getContext().findContent(selector);
        if (content.length > 0) {
            return new TopicBean(getContext(), (ContentTopic) content[0]);
        } else {
            return null;
        }
    }
}
