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

package net.percederberg.liquidsite.template;

import java.util.ArrayList;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentForum;
import net.percederberg.liquidsite.content.ContentSelector;
import net.percederberg.liquidsite.content.ContentTopic;

/**
 * A forum template bean. This class is used to access forums from
 * the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ForumBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(ForumBean.class);

    /**
     * The base template bean.
     */
    private LiquidSiteBean baseBean;

    /**
     * The forum being encapsulated.
     */
    private ContentForum forum;

    /**
     * Creates a new empty forum template bean.
     */
    ForumBean() {
        this(null, null);
    }

    /**
     * Creates a new forum template bean.
     *
     * @param baseBean       the base template bean
     * @param forum          the content forum, or null
     */
    ForumBean(LiquidSiteBean baseBean, ContentForum forum) {
        this.baseBean = baseBean;
        this.forum = forum;
    }

    /**
     * Returns the content identifier.
     *
     * @return the content identifier
     */
    public int getId() {
        if (forum != null) {
            return forum.getId();
        }
        return 0;
    }

    /**
     * Returns the forum name.
     *
     * @return the forum name
     */
    public String getName() {
        if (forum != null) {
            return forum.getName();
        }
        return "";
    }

    /**
     * Returns the real forum name.
     *
     * @return the real forum name
     */
    public String getRealName() {
        if (forum != null) {
            return forum.getRealName();
        }
        return "";
    }

    /**
     * Returns the forum description.
     *
     * @return the forum description
     */
    public String getDescription() {
        if (forum != null) {
            return forum.getDescription();
        }
        return "";
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
        ArrayList  results = new ArrayList();

        if (forum != null) {
            try {
                findTopics(offset, count, results);
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return results;
    }

    /**
     * Finds all topics in this forum. At most the specified number of
     * topics will be added to the result list. The topics will be
     * ordered by last modification date.
     *
     * @param offset         the number of topics to skip
     * @param count          the maximum number of topics
     *
     * @return a list of the topics found (as topic beans)
     */
    private void findTopics(int offset, int count, ArrayList results)
        throws ContentException {

        ContentSelector  selector;
        Content[]        content;

        selector = new ContentSelector(forum.getDomain());
        selector.requireParent(forum);
        selector.requireCategory(Content.TOPIC_CATEGORY);
        selector.sortByModified(false);
        selector.limitResults(offset, count);
        content = baseBean.selectContent(selector);
        for (int i = 0; i < content.length; i++) {
            results.add(new TopicBean(baseBean, (ContentTopic) content[i]));
        }
    }
}
