/*
 * PostBean.java
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
import java.util.Date;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentPost;
import net.percederberg.liquidsite.content.ContentSelector;
import net.percederberg.liquidsite.content.ContentTopic;
import net.percederberg.liquidsite.text.PlainFormatter;

/**
 * A post template bean. This class is used to access posts from the
 * template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PostBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(PostBean.class);

    /**
     * The bean context.
     */
    private BeanContext context;

    /**
     * The post being encapsulated.
     */
    private ContentPost post;

    /**
     * Creates a new empty post template bean.
     */
    PostBean() {
        this(null, null);
    }

    /**
     * Creates a new post template bean.
     *
     * @param context        the bean context
     * @param post           the content post, or null
     */
    PostBean(BeanContext context, ContentPost post) {
        this.context = context;
        this.post = post;
    }

    /**
     * Returns the content identifier.
     *
     * @return the content identifier
     */
    public int getId() {
        if (post != null) {
            return post.getId();
        }
        return 0;
    }

    /**
     * Returns the post parent (always a topic).
     *
     * @return the post parent
     */
    public TopicBean getParent() {
        if (post != null) {
            try {
                return new TopicBean(context,
                                     (ContentTopic) post.getParent());
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return new TopicBean();
    }

    /**
     * Returns the content revision number.
     *
     * @return the content revision number
     */
    public int getRevision() {
        if (post != null) {
            return post.getRevisionNumber();
        }
        return 0;
    }

    /**
     * Returns the post subject.
     *
     * @return the post subject
     */
    public String getSubject() {
        if (post != null) {
            return PlainFormatter.formatHtml(post.getSubject());
        }
        return "";
    }

    /**
     * Returns the unprocessed post subject.
     *
     * @return the unprocessed post subject
     */
    public String getSubjectSource() {
        if (post != null) {
            return post.getSubject();
        }
        return "";
    }

    /**
     * Returns the post text.
     *
     * @return the post text
     */
    public String getText() {
        if (post != null) {
            return PlainFormatter.formatHtml(post.getText());
        }
        return "";
    }

    /**
     * Returns the unprocessed post text.
     *
     * @return the unprocessed post text
     */
    public String getTextSource() {
        if (post != null) {
            return post.getText();
        }
        return "";
    }

    /**
     * Returns the post author.
     *
     * @return the post author
     */
    public UserBean getUser() {
        if (post != null) {
            return context.findUser(post.getAuthorName());
        }
        return new UserBean(null);
    }

    /**
     * Returns the post creation date.
     *
     * @return the post creation date
     */
    public Date getDate() {
        Content  revision;

        if (post != null) {
            if (getRevision() > 1) {
                try {
                    revision = post.getRevision(1);
                    return revision.getModifiedDate();
                } catch (ContentException e) {
                    LOG.error(e.getMessage());
                }
            }
            return post.getModifiedDate();
        }
        return new Date();
    }

    /**
     * Returns the last post modification date.
     *
     * @return the last post modification date
     */
    public Date getUpdate() {
        if (post != null) {
            return post.getModifiedDate();
        }
        return new Date();
    }
}
