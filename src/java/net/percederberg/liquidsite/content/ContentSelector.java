/*
 * ContentSelector.java
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

package net.percederberg.liquidsite.content;

import org.liquidsite.core.data.ContentQuery;

/**
 * A content selector. This class controls the selection of content
 * objects when making database queries. 
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentSelector {

    /**
     * The content query.
     */
    private ContentQuery query;

    /**
     * Creates a new content selector for the specified domain. Note
     * that this content selector cannot return content objects from
     * another domain.
     *
     * @param domain         the domain name
     */
    ContentSelector(String domain) {
        query = new ContentQuery(domain);
    }

    /**
     * Creates a new content selector for the specified domain. Note
     * that this content selector cannot return content objects from
     * another domain.
     *
     * @param domain         the domain
     */
    public ContentSelector(Domain domain) {
        this(domain.getName());
    }

    /**
     * Adds a content parent requirement. By default any content
     * parent will be accepted. By calling this method several times
     * with different parents, a set of content parents will be
     * accepted.
     */
    public void requireRootParent() {
        query.requireParent(0);
    }

    /**
     * Adds a content parent requirement. By default any content
     * parent will be accepted. By calling this method several times
     * with different parents, a set of content parents will be
     * accepted.
     *
     * @param parent         the parent content object
     */
    public void requireParent(Content parent) {
        query.requireParent(parent.getId());
    }

    /**
     * Sets the content category requirement. By default any content
     * category will be accepted.
     *
     * @param category       the content category required
     */
    public void requireCategory(int category) {
        query.requireCategory(category);
    }

    /**
     * Adds a content identifier sort key to the query. Several sort
     * keys can be added, giving priority to the first ones added.
     *
     * @param ascending      the ascending sort order flag
     */
    public void sortById(boolean ascending) {
        query.sortByKey(ContentQuery.ID_KEY, ascending);
    }

    /**
     * Adds a content revision sort key to the query. Several sort
     * keys can be added, giving priority to the first ones added.
     *
     * @param ascending      the ascending sort order flag
     */
    public void sortByRevision(boolean ascending) {
        query.sortByKey(ContentQuery.REVISION_KEY, ascending);
    }

    /**
     * Adds a content category sort key to the query. Several sort
     * keys can be added, giving priority to the first ones added.
     *
     * @param ascending      the ascending sort order flag
     */
    public void sortByCategory(boolean ascending) {
        query.sortByKey(ContentQuery.CATEGORY_KEY, ascending);
    }

    /**
     * Adds a content name sort key to the query. Several sort
     * keys can be added, giving priority to the first ones added.
     *
     * @param ascending      the ascending sort order flag
     */
    public void sortByName(boolean ascending) {
        query.sortByKey(ContentQuery.NAME_KEY, ascending);
    }

    /**
     * Adds a content parent identifier sort key to the query. Several
     * sort keys can be added, giving priority to the first ones
     * added.
     *
     * @param ascending      the ascending sort order flag
     */
    public void sortByParent(boolean ascending) {
        query.sortByKey(ContentQuery.PARENT_KEY, ascending);
    }

    /**
     * Adds a content online date sort key to the query. Several sort
     * keys can be added, giving priority to the first ones added.
     *
     * @param ascending      the ascending sort order flag
     */
    public void sortByOnline(boolean ascending) {
        query.sortByKey(ContentQuery.ONLINE_KEY, ascending);
    }

    /**
     * Adds a content modified date sort key to the query. Several
     * sort keys can be added, giving priority to the first ones
     * added.
     *
     * @param ascending      the ascending sort order flag
     */
    public void sortByModified(boolean ascending) {
        query.sortByKey(ContentQuery.MODIFIED_KEY, ascending);
    }

    /**
     * Adds a content modified author sort key to the query. Several
     * sort keys can be added, giving priority to the first ones
     * added.
     *
     * @param ascending      the ascending sort order flag
     */
    public void sortByAuthor(boolean ascending) {
        query.sortByKey(ContentQuery.AUTHOR_KEY, ascending);
    }

    /**
     * Adds a document property sort key to the query. Several sort
     * keys can be added, giving priority to the first ones added.
     *
     * @param property       the document property  name
     * @param ascending      the ascending sort order flag
     */
    public void sortByDocumentProperty(String property, boolean ascending) {
        query.sortByAttribute(ContentDocument.PROPERTY_PREFIX + property,
                              ascending);
    }

    /**
     * Sets the result start and count limitations. By default a
     * maximum of 100 objects are retrieved, starting from index zero
     * (0).
     *
     * @param start          the index of the first result object
     * @param count          the maximum number of result objects
     */
    public void limitResults(int start, int count) {
        query.limitResults(start, count);
    }

    /**
     * Returns the content query corresponding to this selector. The
     * query will be adjusted to compensate for the online and sorting
     * requirements of the content manager.
     *
     * @param manager        the content manager
     *
     * @return the content query
     */
    ContentQuery getContentQuery(ContentManager manager) {
        query.requirePublished(!manager.isAdmin());
        query.requireOnline(!manager.isAdmin());
        if (!query.isSorting()) {
            if (manager.isAdmin()) {
                query.sortByKey(ContentQuery.CATEGORY_KEY, true);
                query.sortByKey(ContentQuery.NAME_KEY, true);
            } else {
                query.sortByKey(ContentQuery.ONLINE_KEY, false);
            }
        }
        return query;
    }
}
