/*
 * ContentPeer.java
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

package org.liquidsite.core.data;

import java.util.ArrayList;

import org.liquidsite.util.db.DatabaseQuery;

/**
 * A content database peer. This class contains static methods that
 * handles all accesses to the LS_CONTENT table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentPeer extends AbstractPeer {

    /**
     * The content peer instance.
     */
    private static final ContentPeer PEER = new ContentPeer();

    /**
     * The latest content status flag. This flag is set on content
     * objects that are the latest revision, including unpublished or
     * working revisions.
     */
    public static final int LATEST_STATUS = 1;

    /**
     * The published content status flag. This flag is set on content
     * objects that are the latest published revision, not including
     * working revisions.
     */
    public static final int PUBLISHED_STATUS = 2;

    /**
     * Returns the number of content objects matching the specified
     * query.
     *
     * @param src            the data source to use
     * @param query          the content query to use
     *
     * @return the number of matching content objects
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static int doCountByQuery(DataSource src, ContentQuery query)
        throws DataObjectException {

        return (int) PEER.count(src, query.createCountQuery());
    }

    /**
     * Returns a list of all content object revisions with the
     * specified id.
     *
     * @param src            the data source to use
     * @param id             the content id
     *
     * @return the list of content objects found
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectById(DataSource src, int id)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.select.id");

        query.addParameter(id);
        return PEER.selectList(src, query);
    }

    /**
     * Returns the content object with the specified id and revision.
     *
     * @param src            the data source to use
     * @param id             the content id
     * @param revision       the content revision
     *
     * @return the content found, or
     *         null if no matching content existed
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ContentData doSelectByRevision(DataSource src,
                                                 int id,
                                                 int revision)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.select.revision");

        query.addParameter(id);
        query.addParameter(revision);
        return (ContentData) PEER.select(src, query);
    }

    /**
     * Returns the content object with the specified id and highest
     * revision. A flag can be set to regard the revision number zero
     * (0) as the highest one.
     *
     * @param src            the data source to use
     * @param id             the content id
     * @param maxIsZero      the revision zero is max flag
     *
     * @return the content found, or
     *         null if no matching content existed
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ContentData doSelectByMaxRevision(DataSource src,
                                                    int id,
                                                    boolean maxIsZero)
        throws DataObjectException {

        DatabaseQuery  query;
        ContentData    data;

        if (maxIsZero) {
            data = doSelectByRevision(src, id, 0);
            if (data != null) {
                return data;
            }
        }
        query = new DatabaseQuery("content.select.id");
        query.addParameter(id);
        return (ContentData) PEER.select(src, query);
    }

    /**
     * Returns the content object with the specified parent and name.
     * If multiple objects should match, any one of them can be
     * returned. Matches of old revisions of content objects will be
     * discarded, and only the highest revision of a matching object
     * is returned. A flag can be set to regard the revision number
     * zero (0) as the highest one.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     * @param parent         the parent content id
     * @param name           the content name
     * @param maxIsZero      the revision zero is max flag
     *
     * @return the content found, or
     *         null if no matching content existed
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ContentData doSelectByName(DataSource src,
                                             String domain,
                                             int parent,
                                             String name,
                                             boolean maxIsZero)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.select.name");

        query.addParameter(domain);
        query.addParameter(parent);
        query.addParameter(name);
        if (maxIsZero) {
            query.addParameter(LATEST_STATUS);
        } else {
            query.addParameter(PUBLISHED_STATUS);
        }
        return (ContentData) PEER.select(src, query);
    }

    /**
     * Returns a list of content objects matching the specified query.
     *
     * @param src            the data source to use
     * @param query          the content query to use
     *
     * @return a list of all matching content objects
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByQuery(DataSource src,
                                            ContentQuery query)
        throws DataObjectException {

        return PEER.selectList(src, query.createSelectQuery());
    }

    /**
     * Inserts a new content object into the data source. This method
     * will assign a new content id if it is set to zero (0).
     *
     * @param src            the data source to use
     * @param data           the content data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     *
     * @see #doStatusUpdate
     */
    public static synchronized void doInsert(DataSource src,
                                             ContentData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.insert");

        if (data.getInt(ContentData.ID) <= 0) {
            data.setInt(ContentData.ID, getNewId(src));
        }
        query.addParameter(data.getString(ContentData.DOMAIN));
        query.addParameter(data.getInt(ContentData.ID));
        query.addParameter(data.getInt(ContentData.REVISION));
        query.addParameter(data.getInt(ContentData.CATEGORY));
        query.addParameter(data.getString(ContentData.NAME));
        query.addParameter(data.getInt(ContentData.PARENT));
        query.addParameter(data.getDate(ContentData.ONLINE));
        query.addParameter(data.getDate(ContentData.OFFLINE));
        query.addParameter(data.getDate(ContentData.MODIFIED));
        query.addParameter(data.getString(ContentData.AUTHOR));
        query.addParameter(data.getString(ContentData.COMMENT));
        PEER.insert(src, query);
    }

    /**
     * Updates a content object in the data source.
     *
     * @param src            the data source to use
     * @param data           the content data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     *
     * @see #doStatusUpdate
     */
    public static void doUpdate(DataSource src, ContentData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.update");

        query.addParameter(data.getString(ContentData.NAME));
        query.addParameter(data.getInt(ContentData.PARENT));
        query.addParameter(data.getDate(ContentData.ONLINE));
        query.addParameter(data.getDate(ContentData.OFFLINE));
        query.addParameter(data.getDate(ContentData.MODIFIED));
        query.addParameter(data.getString(ContentData.AUTHOR));
        query.addParameter(data.getString(ContentData.COMMENT));
        query.addParameter(data.getInt(ContentData.ID));
        query.addParameter(data.getInt(ContentData.REVISION));
        PEER.update(src, query);
    }

    /**
     * Deletes a content object from the data source. This method
     * will delete all revisions, as well as related attributes,
     * permissions and locks. Note, however, that it will NOT delete
     * referenced child objects.
     *
     * @param src            the data source to use
     * @param data           the content data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDelete(DataSource src, ContentData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.delete");
        String         domain = data.getString(ContentData.DOMAIN);
        int            id = data.getInt(ContentData.ID);

        query.addParameter(id);
        PEER.delete(src, query);
        AttributePeer.doDeleteContent(src, id);
        PermissionPeer.doDelete(src, domain, id);
        LockPeer.doDeleteContent(src, id);
    }

    /**
     * Deletes all content objects in a domain from the data source.
     * This method also deletes all attributes, permissions and locks
     * in the domain.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteDomain(DataSource src, String domain)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.delete.domain");

        query.addParameter(domain);
        PEER.delete(src, query);
        AttributePeer.doDeleteDomain(src, domain);
        PermissionPeer.doDeleteDomain(src, domain);
        LockPeer.doDeleteDomain(src, domain);
    }

    /**
     * Deletes a content object revision from the data source. This
     * method also deletes all related attributes.
     *
     * @param src            the data source to use
     * @param id             the content identifier
     * @param revision       the content revision
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     *
     * @see #doStatusUpdate
     */
    public static void doDeleteRevision(DataSource src,
                                        int id,
                                        int revision)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.delete.revision");

        query.addParameter(id);
        query.addParameter(revision);
        PEER.delete(src, query);
        AttributePeer.doDeleteRevision(src, id, revision);
    }

    /**
     * Updates the status flags for all content object revisions.
     * This method will read the data source to extract the latest
     * revisions, clear any previous status flags, and then set the
     * new flags. This method also updates the status flags in
     * related attributes. When finished inserting, updating or
     * deleting in the content and attribute tables, this method
     * should always be called.
     *
     * @param src            the data source to use
     * @param id             the content identifier
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doStatusUpdate(DataSource src, int id)
        throws DataObjectException {

        DatabaseQuery  query;
        int            min;
        int            max;

        query = new DatabaseQuery("content.select.revision.min");
        query.addParameter(id);
        min = (int) PEER.count(src, query);
        query = new DatabaseQuery("content.select.revision.max");
        query.addParameter(id);
        max = (int) PEER.count(src, query);
        doStatusClear(src, id);
        if (max > 0) {
            doStatusSet(src, id, max, PUBLISHED_STATUS);
        }
        if (min > 0) {
            doStatusSet(src, id, max, LATEST_STATUS);
        } else {
            doStatusSet(src, id, min, LATEST_STATUS);
        }
    }

    /**
     * Clears the status flags for all content object revisions. This
     * method also clears the status flags in related attributes.
     *
     * @param src            the data source to use
     * @param id             the content identifier
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    private static void doStatusClear(DataSource src, int id)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.status.clear");

        query.addParameter(id);
        PEER.update(src, query);
    }

    /**
     * Sets a status flag bit for a content object revision. The bit
     * flag will be added to the status flags without clearing any
     * other bit flag (logical OR). This method also sets the same bit
     * in related attributes.
     *
     * @param src            the data source to use
     * @param id             the content identifier
     * @param revision       the content revision
     * @param flag           the bit flag to set
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    private static void doStatusSet(DataSource src,
                                    int id,
                                    int revision,
                                    int flag)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.status.set");

        query.addParameter(flag);
        query.addParameter(id);
        query.addParameter(revision);
        PEER.update(src, query);
    }

    /**
     * Returns a new unique content identifier. This method will
     * search the data source for the maximum content identifier
     * currently used and add one to it.
     *
     * @param src            the data source to use
     *
     * @return the new unique content identifier
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    private static int getNewId(DataSource src)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.select.id.max");

        return (int) PEER.count(src, query) + 1;
    }

    /**
     * Creates a new content database peer.
     */
    private ContentPeer() {
        super("content");
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new ContentData();
    }
}
