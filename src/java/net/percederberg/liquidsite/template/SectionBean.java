/*
 * SectionBean.java
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
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.content.ContentSelector;
import net.percederberg.liquidsite.text.PlainFormatter;

/**
 * A section template bean. This class is used to access sections from
 * the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class SectionBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(SectionBean.class);

    /**
     * The bean context.
     */
    private BeanContext context;

    /**
     * The section being encapsulated.
     */
    private ContentSection section;

    /**
     * Creates a new empty section template bean.
     */
    SectionBean() {
        this(null, null);
    }

    /**
     * Creates a new section template bean based upon the request
     * environment section.
     *
     * @param context        the bean context
     */
    SectionBean(BeanContext context) {
        this(context, context.getRequest().getEnvironment().getSection());
    }

    /**
     * Creates a new section template bean.
     *
     * @param context        the bean context
     * @param section        the content section, or null
     */
    SectionBean(BeanContext context, ContentSection section) {
        this.context = context;
        this.section = section;
    }

    /**
     * Returns the content identifier.
     *
     * @return the content identifier
     */
    public int getId() {
        if (section != null) {
            return section.getId();
        }
        return 0;
    }

    /**
     * Returns the section name.
     *
     * @return the section name
     */
    public String getName() {
        if (section != null) {
            return section.getName();
        }
        return "";
    }

    /**
     * Returns the section description in HTML format.
     *
     * @return the section description
     */
    public String getDescription() {
        if (section != null) {
            return PlainFormatter.formatHtml(section.getDescription());
        }
        return "";
    }

    /**
     * Returns the full section path.
     *
     * @return the section path
     */
    public String getPath() {
        String  path = context.getContentPath(section);

        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        } else {
            return path;
        }
    }

    /**
     * Returns the parent section. If there is no parent section, an
     * emtpy section will be returned.
     *
     * @return the parent section, or
     *         an empty section if no parent exists
     */
    public SectionBean getParent() {
        if (section == null) {
            return this;
        } else {
            try {
                return new SectionBean(context,
                                       (ContentSection) section.getParent());
            } catch (ContentException e) {
                LOG.error(e.getMessage());
                return new SectionBean();
            }
        }
    }

    /**
     * Returns all sections in this section. The sections will be
     * ordered by their name.
     *
     * @return a list of the sections found (as section beans)
     */
    public ArrayList getSections() {
        ArrayList        results = new ArrayList();
        ContentSelector  selector;
        Content[]        content;

        if (section != null) {
            try {
                selector = new ContentSelector(section.getDomain());
                selector.requireParent(section);
                selector.requireCategory(Content.SECTION_CATEGORY);
                selector.sortByName(true);
                selector.limitResults(0, 100);
                content = context.findContent(selector);
                for (int i = 0; i < content.length; i++) {
                    results.add(new SectionBean(context,
                                                (ContentSection) content[i]));
                }
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return results;
    }

    /**
     * Returns all forums in this section. The forums will be ordered
     * by their name.
     *
     * @return a list of the forums found (as forum beans)
     */
    public ArrayList getForums() {
        ArrayList        results = new ArrayList();
        ContentSelector  selector;
        Content[]        content;

        if (section != null) {
            try {
                selector = new ContentSelector(section.getDomain());
                selector.requireParent(section);
                selector.requireCategory(Content.FORUM_CATEGORY);
                selector.sortByName(true);
                selector.limitResults(0, 100);
                content = context.findContent(selector);
                for (int i = 0; i < content.length; i++) {
                    results.add(new ForumBean(context,
                                              (ContentForum) content[i]));
                }
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return results;
    }

    /**
     * Returns the number of documents in this section and any
     * subsections.
     *
     * @return the number of documents found
     */
    public int countDocuments() {
        if (section != null) {
            return context.countDocuments(section);
        }
        return 0;
    }

    /**
     * Returns the document corresponding to the specified path. The
     * path is relative to this section.
     *
     * @param path           the document (and section) path
     *
     * @return the document found, or
     *         an empty document if not found
     */
    public DocumentBean findDocument(String path) {
        if (section != null) {
            return context.findDocument(section, path);
        }
        return new DocumentBean();
    }

    /**
     * Returns all documents in this section and any subsections. At
     * most the specified number of documents will be returned. The
     * documents will be ordered by the publication online date.
     *
     * @param offset         the number of documents to skip
     * @param count          the maximum number of documents
     *
     * @return a list of the documents found (as document beans)
     */
    public ArrayList findDocuments(int offset, int count) {
        return findDocuments("", offset, count);
    }

    /**
     * Returns all documents in this section and any subsections. At
     * most the specified number of documents will be returned. The
     * documents will be ordered by the specified sort order.
     *
     * @param sorting        the sorting information
     * @param offset         the number of documents to skip
     * @param count          the maximum number of documents
     *
     * @return a list of the documents found (as document beans)
     */
    public ArrayList findDocuments(String sorting, int offset, int count) {
        if (section != null) {
            return context.findDocuments(section, sorting, offset, count);
        }
        return new ArrayList(0);
    }

    /**
     * Returns a named forum in this section.
     *
     * @param name           the forum name
     *
     * @return the forum found, or
     *         an empty forum if not found
     */
    public ForumBean findForum(String name) {
        if (section != null) {
            return context.findForum(section, name);
        }
        return new ForumBean();
    }
}
