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
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.ContentSelector;
import net.percederberg.liquidsite.content.ContentTopic;
import net.percederberg.liquidsite.text.PlainFormatter;

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
     * The first post in the forum. This variable is set upon the
     * first request.
     */
    private PostBean first = null;

    /**
     * The last post in the forum. This variable is set upon the first
     * request.
     */
    private PostBean last = null;

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
            return PlainFormatter.formatHtml(topic.getSubject());
        }
        return "";
    }

    /**
     * Returns the unprocessed topic subject.
     *
     * @return the unprocessed topic subject
     */
    public String getSubjectSource() {
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
     * Returns the first post in this topic. The posts are ordered in
     * creation date order.
     *
     * @return the first post in this topic
     */
    public PostBean getFirst() {
        ContentSelector  selector;

        if (first == null) {
            if (topic != null) {
                try {
                    selector = createPostSelector();
                    selector.sortById(true);
                    selector.limitResults(0, 1);
                    first = createPost(selector);
                } catch (ContentException e) {
                    LOG.error(e.getMessage());
                }
            }
            if (first == null) {
                first = new PostBean();
            }
        }
        return first;
    }

    /**
     * Returns the last post in this topic. The posts are ordered in
     * creation date order.
     *
     * @return the last post in this topic
     */
    public PostBean getLast() {
        ContentSelector  selector;

        if (last == null) {
            if (topic != null) {
                try {
                    selector = createPostSelector();
                    selector.sortById(false);
                    selector.limitResults(0, 1);
                    last = createPost(selector);
                } catch (ContentException e) {
                    LOG.error(e.getMessage());
                }
            }
            if (last == null) {
                last = new PostBean();
            }
        }
        return last;
    }

    /**
     * Returns the number of posts in this topic.
     *
     * @return the number of posts in this topic
     */
    public int getPostCount() {
        if (topic != null) {
            try {
                return baseBean.countContent(createPostSelector());
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return 0;
    }

    /**
     * Returns a specified post in this topic. The post content
     * identifier must be specified.
     *
     * @param id             the post content identifier
     *
     * @return the post found (as a post bean), or
     *         an empty post if not found
     */
    public PostBean findPost(int id) {
        Content  content;

        if (topic != null) {
            try {
                content = baseBean.selectContent(id);
                if (content != null) {
                    return new PostBean(baseBean, (ContentPost) content);
                }
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            } catch (ContentSecurityException e) {
                LOG.warning(e.getMessage());
            }
        }
        return new PostBean();
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
        ArrayList        results = new ArrayList();
        ContentSelector  selector;
        Content[]        content;

        if (topic != null) {
            try {
                selector = createPostSelector();
                selector.sortById(true);
                selector.limitResults(offset, count);
                content = baseBean.selectContent(selector);
                for (int i = 0; i < content.length; i++) {
                    results.add(new PostBean(baseBean,
                                             (ContentPost) content[i]));
                }
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return results;
    }

    /**
     * Creates a content selector for finding all posts in this topic.
     *
     * @return a content post selector
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private ContentSelector createPostSelector()
        throws ContentException {

        ContentSelector  selector;

        selector = new ContentSelector(topic.getDomain());
        selector.requireParent(topic);
        selector.requireCategory(Content.POST_CATEGORY);
        return selector;
    }

    /**
     * Returns the first content post matching the specified selector.
     *
     * @param selector       the content selector to use
     *
     * @return the post bean for the found post, or
     *         null if no matching posts were found
     */
    private PostBean createPost(ContentSelector selector)
        throws ContentException {

        Content[]  content;

        content = baseBean.selectContent(selector);
        if (content.length > 0) {
            return new PostBean(baseBean, (ContentPost) content[0]);
        } else {
            return null;
        }
    }
}