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

import net.percederberg.liquidsite.content.ContentPost;
import net.percederberg.liquidsite.text.PlainFormatter;

/**
 * A post template bean. This class is used to access posts from the
 * template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PostBean extends ContentBean {

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
        super(context, post);
    }

    /**
     * Returns the post subject.
     *
     * @return the post subject, or
     *         an empty string if the post doesn't exist
     */
    public String getSubject() {
        return PlainFormatter.formatHtml(getSubjectSource());
    }

    /**
     * Returns the unprocessed post subject.
     *
     * @return the unprocessed post subject, or
     *         an empty string if the post doesn't exist
     */
    public String getSubjectSource() {
        if (getContent() != null) {
            return ((ContentPost) getContent()).getSubject();
        }
        return "";
    }

    /**
     * Returns the post text.
     *
     * @return the post text, or
     *         an empty string if the post doesn't exist
     */
    public String getText() {
        return PlainFormatter.formatHtml(getTextSource());
    }

    /**
     * Returns the unprocessed post text.
     *
     * @return the unprocessed post text, or
     *         an empty string if the post doesn't exist
     */
    public String getTextSource() {
        if (getContent() != null) {
            return ((ContentPost) getContent()).getText();
        }
        return "";
    }
}
