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

/**
 * A post template bean. This class is used to access posts from the
 * template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PostBean {

    /**
     * The base template bean.
     */
    private LiquidSiteBean baseBean;

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
     * @param baseBean       the base template bean
     * @param post           the content post, or null
     */
    PostBean(LiquidSiteBean baseBean, ContentPost post) {
        this.baseBean = baseBean;
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
     * Returns the post subject.
     *
     * @return the post subject
     */
    public String getSubject() {
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
            // TODO: convert to HTML
            return post.getText();
        }
        return "";
    }

    /**
     * Returns the post author.
     *
     * @return the post author
     */
    public String getUser() {
        // TODO: implement this properly with user bean?
        if (post != null) {
            return post.getAuthorName();
        }
        return "";
    }

    /**
     * Returns the post modification date.
     *
     * @return the post modification date
     */
    public Date getDate() {
        if (post != null) {
            return post.getModifiedDate();
        }
        return new Date();
    }
}
