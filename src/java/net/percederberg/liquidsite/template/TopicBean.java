/*
 * TopicBean.java
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

package net.percederberg.liquidsite.template;

import java.util.ArrayList;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentPost;
import net.percederberg.liquidsite.content.ContentSelector;
import net.percederberg.liquidsite.content.ContentTopic;

/**
 * A topic template bean. This class is used to access topics from
 * the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class TopicBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(TopicBean.class);

    /**
     * The base template bean.
     */
    private LiquidSiteBean baseBean;

    /**
     * The topic being encapsulated.
     */
    private ContentTopic topic;

    /**
     * Creates a new empty topic template bean.
     */
    TopicBean() {
        this(null, null);
    }

    /**
     * Creates a new topic template bean.
     *
     * @param baseBean       the base template bean
     * @param topic          the content topic, or null
     */
    TopicBean(LiquidSiteBean baseBean, ContentTopic topic) {
        this.baseBean = baseBean;
        this.topic = topic;
    }

    /**
     * Returns the content identifier.
     *
     * @return the content identifier
     */
    public int getId() {
        if (topic != null) {
            return topic.getId();
        }
        return 0;
    }

    /**
     * Returns the topic name.
     *
     * @return the topic name
     */
    public String getName() {
        if (topic != null) {
            return topic.getName();
        }
        return "";
    }

    /**
     * Returns the topic subject.
     *
     * @return the topic subject
     */
    public String getSubject() {
        if (topic != null) {
            return topic.getSubject();
        }
        return "";
    }

    /**
     * Returns the topic locked flag.
     *
     * @return the topic locked flag
     */
    public boolean getLocked() {
        if (topic != null) {
            return topic.isLocked();
        }
        return true;
    }

    /**
     * Returns all posts in this topic. At most the specified number
     * of posts will be returned. The posts will be ordered by
     * creation date.
     *
     * @param offset         the number of post to skip
     * @param count          the maximum number of posts
     *
     * @return a list of the posts found (as post beans)
     */
    public ArrayList findPosts(int offset, int count) {
        ArrayList  results = new ArrayList();

        if (topic != null) {
            try {
                findPosts(offset, count, results);
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return results;
    }

    /**
     * Finds all posts in this topic. At most the specified number of
     * posts will be added to the result list. The posts will be
     * ordered by creation date.
     *
     * @param offset         the number of post to skip
     * @param count          the maximum number of posts
     *
     * @return a list of the posts found (as post beans)
     */
    private void findPosts(int offset, int count, ArrayList results)
        throws ContentException {

        ContentSelector  selector;
        Content[]        content;

        selector = new ContentSelector(topic.getDomain());
        selector.requireParent(topic);
        selector.requireCategory(Content.POST_CATEGORY);
        selector.sortById(true);
        selector.limitResults(offset, count);
        content = baseBean.selectContent(selector);
        for (int i = 0; i < content.length; i++) {
            results.add(new PostBean(baseBean, (ContentPost) content[i]));
        }
    }
}
