/*
 * InternalContent.java
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

import java.util.ArrayList;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.ContentData;
import net.percederberg.liquidsite.dbo.ContentPeer;
import net.percederberg.liquidsite.dbo.ContentQuery;
import net.percederberg.liquidsite.dbo.DatabaseObjectException;

/**
 * A class with static helper methods for content objects. This class
 * mostly contains methods for reading content objects from the
 * database. None of the methods in this class are visible outside
 * this package.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class InternalContent {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(InternalContent.class);

    /**
     * Returns an array of content object revisions with the
     * specified identifier.
     *
     * @param manager        the content manager to use
     * @param id             the content identifier
     *
     * @return an array of the content object revisions found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Content[] findById(ContentManager manager, int id)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ArrayList           list;

        try {
            list = ContentPeer.doSelectById(id, con);
            return createContent(manager, list, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
    }

    /**
     * Returns the content object with the specified identifier and
     * highest revision.
     *
     * @param manager        the content manager to use
     * @param id             the content identifier
     *
     * @return the content object found, or
     *         null if no matching content existed
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Content findByMaxRevision(ContentManager manager, int id)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ContentData         data;

        try {
            data = ContentPeer.doSelectByMaxRevision(id,
                                                     manager.isAdmin(),
                                                     con);
            if (data != null) {
                return createContent(manager, data, con);
            } else {
                return null;
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
    }

    /**
     * Returns the content object with the specified identifier and
     * revision.
     *
     * @param manager        the content manager to use
     * @param id             the content identifier
     * @param revision       the content revision
     *
     * @return the content object found, or
     *         null if no matching content existed
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Content findByRevision(ContentManager manager,
                                  int id,
                                  int revision)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ContentData         data;

        try {
            data = ContentPeer.doSelectByRevision(id, revision, con);
            if (data != null) {
                return createContent(manager, data, con);
            } else {
                return null;
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
    }

    /**
     * Returns the content object with the specified name in the
     * domain root. Only the latest revision of the content object
     * will be returned.
     *
     * @param manager        the content manager to use
     * @param domain         the domain
     * @param name           the content name
     *
     * @return the content object found, or
     *         null if no matching content existed
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Content findByName(ContentManager manager,
                              Domain domain,
                              String name)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ContentData         data;

        try {
            data = ContentPeer.doSelectByName(domain.getName(),
                                              0,
                                              name,
                                              manager.isAdmin(),
                                              con);
            if (data != null) {
                return createContent(manager, data, con);
            } else {
                return null;
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
    }

    /**
     * Returns the content object with the specified parent and name.
     * Only the latest revision of the content object will be
     * returned.
     *
     * @param manager        the content manager to use
     * @param parent         the parent content
     * @param name           the content name
     *
     * @return the content object found, or
     *         null if no matching content existed
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Content findByName(ContentManager manager,
                              Content parent,
                              String name)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ContentData         data;

        try {
            data = ContentPeer.doSelectByName(parent.getDomainName(),
                                              parent.getId(),
                                              name,
                                              manager.isAdmin(),
                                              con);
            if (data != null) {
                return createContent(manager, data, con);
            } else {
                return null;
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
    }

    /**
     * Returns an array of root content objects in the specified
     * domain. Only the latest revision of each content object will
     * be returned.
     *
     * @param manager        the content manager to use
     * @param domain         the domain
     *
     * @return an array of content objects found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Content[] findByParent(ContentManager manager, Domain domain)
        throws ContentException {

        ContentSelector  selector = new ContentSelector(domain);

        selector.requireRootParent();
        return findBySelector(manager, selector);
    }

    /**
     * Returns an array of content objects having the specified
     * parent. Only the latest revision of each content object will
     * be returned.
     *
     * @param manager        the content manager to use
     * @param parent         the parent content
     *
     * @return an array of content objects found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Content[] findByParent(ContentManager manager, Content parent)
        throws ContentException {

        ContentSelector  selector;

        selector = new ContentSelector(parent.getDomainName());
        selector.requireParent(parent);
        return findBySelector(manager, selector);
    }

    /**
     * Returns an array of root content objects with the specified
     * category. Only the latest revision of each content object will
     * be returned.
     *
     * @param manager        the content manager to use
     * @param domain         the domain
     * @param category       the content category
     *
     * @return an array of content objects found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Content[] findByCategory(ContentManager manager,
                                    Domain domain,
                                    int category)
        throws ContentException {

        ContentSelector  selector = new ContentSelector(domain);

        selector.requireRootParent();
        selector.requireCategory(category);
        return findBySelector(manager, selector);
    }

    /**
     * Returns an array of content objects with the specified parent
     * and category. Only the latest revision of each content object
     * will be returned.
     *
     * @param manager        the content manager to use
     * @param parent         the parent content object
     * @param category       the content category
     *
     * @return an array of content objects found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Content[] findByCategory(ContentManager manager,
                                    Content parent,
                                    int category)
        throws ContentException {

        ContentSelector  selector;

        selector = new ContentSelector(parent.getDomainName());
        selector.requireParent(parent);
        selector.requireCategory(category);
        return findBySelector(manager, selector);
    }

    /**
     * Returns an array of content objects matching the specified
     * selector. Only the latest revision of each content object will
     * be returned.
     *
     * @param manager        the content manager to use
     * @param selecto        the content selector
     *
     * @return an array of content objects found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Content[] findBySelector(ContentManager manager,
                                    ContentSelector selector)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ContentQuery        query;
        ArrayList           list;

        try {
            query = selector.getContentQuery(manager);
            list = ContentPeer.doSelectByQuery(query, con);
            return createContent(manager, list, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
    }

    /**
     * Creates an array of content objects. The content subclass
     * matching the category value will be used.
     *
     * @param manager        the content manager to use
     * @param list           the list of content object data
     * @param con            the database connection to use
     *
     * @return the array of new content objects
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly, or if the content category was unknown
     */
    private static Content[] createContent(ContentManager manager,
                                           ArrayList list,
                                           DatabaseConnection con)
        throws ContentException {

        Content[]    res;
        ContentData  data;

        res = new Content[list.size()];
        for (int i = 0; i < list.size(); i++) {
            data = (ContentData) list.get(i);
            res[i] = createContent(manager, data, con);
        }
        return res;
    }

    /**
     * Creates a content object. The content subclass matching the
     * category value will be used.
     *
     * @param manager        the content manager to use
     * @param data           the content object data
     * @param con            the database connection to use
     *
     * @return the new content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly, or if the content category was unknown
     */
    private static Content createContent(ContentManager manager,
                                         ContentData data,
                                         DatabaseConnection con)
        throws ContentException {

        switch (data.getInt(ContentData.CATEGORY)) {
        case Content.SITE_CATEGORY:
            return new ContentSite(manager, data, con);
        case Content.TRANSLATOR_CATEGORY:
            return new ContentTranslator(manager, data,  con);
        case Content.FOLDER_CATEGORY:
            return new ContentFolder(manager, data,  con);
        case Content.PAGE_CATEGORY:
            return new ContentPage(manager, data, con);
        case Content.FILE_CATEGORY:
            return new ContentFile(manager, data, con);
        case Content.TEMPLATE_CATEGORY:
            return new ContentTemplate(manager, data, con);
        case Content.SECTION_CATEGORY:
            return new ContentSection(manager, data, con);
        case Content.DOCUMENT_CATEGORY:
            return new ContentDocument(manager, data, con);
        case Content.FORUM_CATEGORY:
            return new ContentForum(manager, data, con);
        case Content.THREAD_CATEGORY:
            return new ContentThread(manager, data, con);
        case Content.POST_CATEGORY:
            return new ContentPost(manager, data, con);
        default:
            throw new ContentException(
                "content category " + data.getInt(ContentData.CATEGORY) +
                " hasn't been defined");
        }
    }

    /**
     * Returns a database connection.
     *
     * @param manager        the content manager to use
     *
     * @return a database connection
     *
     * @throws ContentException if no database connector is available
     *             or no database connection could be made
     */
    static DatabaseConnection getDatabaseConnection(ContentManager manager)
        throws ContentException {

        return PersistentObject.getDatabaseConnection(manager);
    }

    /**
     * Disposes of a database connection.
     *
     * @param manager        the content manager to use
     * @param con            the database connection
     */
    static void returnDatabaseConnection(ContentManager manager,
                                         DatabaseConnection con) {

        PersistentObject.returnDatabaseConnection(manager, con);
    }
}
