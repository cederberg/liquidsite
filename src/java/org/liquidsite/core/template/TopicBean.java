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

package org.liquidsite.core.template;

import java.util.ArrayList;

import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentPost;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.ContentSelector;
import org.liquidsite.core.content.ContentTopic;
import org.liquidsite.core.text.PlainFormatter;
import org.liquidsite.util.log.Log;

/**
 * A topic template bean. This class is used to access topics from
 * the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class TopicBean extends ContentBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(TopicBean.class);

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
     * Creates a new topic template bean based on the request
     * environment topic.
     *
     * @param context        the bean context
     */
    TopicBean(BeanContext context) {
        this(context, context.getRequest().getEnvironment().getTopic());
    }

    /**
     * Creates a new topic template bean.
     *
     * @param context        the bean context
     * @param topic          the content topic, or null
     */
    TopicBean(BeanContext context, ContentTopic topic) {
        super(context, topic);
    }

    /**
     * Returns the topic subject.
     *
     * @return the topic subject, or
     *         an empty string if the topic doesn't exist
     */
    public String getSubject() {
        return PlainFormatter.formatHtml(getSubjectSource());
    }

    /**
     * Returns the unprocessed topic subject.
     *
     * @return the unprocessed topic subject, or
     *         an empty string if the topic doesn't exist
     */
    public String getSubjectSource() {
        if (getContent() != null) {
            return ((ContentTopic) getContent()).getSubject();
        }
        return "";
    }

    /**
     * Returns the topic locked flag.
     *
     * @return the topic locked flag, or
     *         true if the topic doesn't exist
     */
    public boolean getLocked() {
        if (getContent() != null) {
            return ((ContentTopic) getContent()).isLocked();
        }
        return true;
    }

    /**
     * Returns the first post in this topic. The posts are ordered in
     * creation date order.
     *
     * @return the first post in this topic, or
     *         an empty post if the topic doesn't exist
     */
    public PostBean getFirst() {
        ContentSelector  selector;

        if (first == null) {
            if (getContent() != null) {
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
     * @return the last post in this topic, or
     *         an empty post if the topic doesn't exist
     */
    public PostBean getLast() {
        ContentSelector  selector;

        if (last == null) {
            if (getContent() != null) {
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
        if (getContent() != null) {
            try {
                return getContext().countContent(createPostSelector());
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

        if (getContent() != null) {
            try {
                content = getContext().findContent(id);
                if (content != null) {
                    return new PostBean(getContext(), (ContentPost) content);
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

        if (getContent() != null) {
            try {
                selector = createPostSelector();
                selector.sortById(true);
                selector.limitResults(offset, count);
                content = getContext().findContent(selector);
                for (int i = 0; i < content.length; i++) {
                    results.add(new PostBean(getContext(),
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

        selector = new ContentSelector(getContent().getDomain());
        selector.requireParent(getContent());
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
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private PostBean createPost(ContentSelector selector)
        throws ContentException {

        Content[]  content;

        content = getContext().findContent(selector);
        if (content.length > 0) {
            return new PostBean(getContext(), (ContentPost) content[0]);
        } else {
            return null;
        }
    }
}
