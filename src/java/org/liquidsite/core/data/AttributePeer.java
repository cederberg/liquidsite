/*
 * AttributePeer.java
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
 * A content attribute database peer. This class contains static
 * methods that handles all accesses to the LS_ATTRIBUTE table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class AttributePeer extends AbstractPeer {

    /**
     * The attribute peer instance.
     */
    private static final AttributePeer PEER = new AttributePeer();

    /**
     * Calculates the aggregate size in the database of all attributes
     * in a domain.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     *
     * @return the size in bytes of all the attributes in the domain
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static long doCalculateDomainSize(DataSource src, String domain)
        throws DataObjectException {

        DatabaseQuery  query;

        query = new DatabaseQuery("attribute.select.domainsize");
        query.addParameter(domain);
        return PEER.count(src, query);
    }

    /**
     * Returns a list of all attribute objects with the specified
     * content id and revision.
     *
     * @param src            the data source to use
     * @param id             the content id
     * @param revision       the content revision
     *
     * @return the list of attribute objects found
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByRevision(DataSource src,
                                               int id,
                                               int revision)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.select.revision");

        query.addParameter(id);
        query.addParameter(revision);
        return PEER.selectList(src, query);
    }

    /**
     * Returns the attribute object with the specified content id,
     * content revision, and attribute name.
     *
     * @param src            the data source to use
     * @param id             the content id
     * @param revision       the content revision
     * @param name           the attribute name
     *
     * @return the attribute found, or
     *         null if no matching attribute existed
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static AttributeData doSelectByName(DataSource src,
                                               int id,
                                               int revision,
                                               String name)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.select.name");

        query.addParameter(id);
        query.addParameter(revision);
        query.addParameter(name);
        return (AttributeData) PEER.select(src, query);
    }

    /**
     * Inserts a new attribute object into the data source.
     *
     * @param src            the data source to use
     * @param data           the attribute data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doInsert(DataSource src, AttributeData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.insert");

        query.addParameter(data.getString(AttributeData.DOMAIN));
        query.addParameter(data.getInt(AttributeData.CONTENT));
        query.addParameter(data.getInt(AttributeData.REVISION));
        query.addParameter(data.getString(AttributeData.NAME));
        query.addParameter(data.getString(AttributeData.DATA));
        PEER.insert(src, query);
    }

    /**
     * Updates an attribute in the data source.
     *
     * @param src            the data source to use
     * @param data           the attribute data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doUpdate(DataSource src, AttributeData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.update");

        query.addParameter(data.getString(AttributeData.DATA));
        query.addParameter(data.getInt(AttributeData.CONTENT));
        query.addParameter(data.getInt(AttributeData.REVISION));
        query.addParameter(data.getString(AttributeData.NAME));
        PEER.update(src, query);
    }

    /**
     * Deletes an attribute object from the data source.
     *
     * @param src            the data source to use
     * @param data           the attribute data object
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDelete(DataSource src, AttributeData data)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.delete");

        query.addParameter(data.getInt(AttributeData.CONTENT));
        query.addParameter(data.getInt(AttributeData.REVISION));
        query.addParameter(data.getString(AttributeData.NAME));
        PEER.delete(src, query);
    }

    /**
     * Deletes all attributes for a domain from the data source.
     *
     * @param src            the data source to use
     * @param domain         the domain name
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteDomain(DataSource src, String domain)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.delete.domain");

        query.addParameter(domain);
        PEER.delete(src, query);
    }

    /**
     * Deletes all attributes for a content object from the data
     * source.
     *
     * @param src            the data source to use
     * @param id             the content object id
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteContent(DataSource src, int id)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.delete.content");

        query.addParameter(id);
        PEER.delete(src, query);
    }

    /**
     * Deletes all attributes for a content revision from the data
     * source.
     *
     * @param src            the data source to use
     * @param id             the content object id
     * @param revision       the content revision
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    public static void doDeleteRevision(DataSource src,
                                        int id,
                                        int revision)
        throws DataObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.delete.revision");

        query.addParameter(id);
        query.addParameter(revision);
        PEER.delete(src, query);
    }

    /**
     * Creates a new content attribute database peer.
     */
    private AttributePeer() {
        super("content attribute");
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected AbstractData getDataObject() {
        return new AttributeData();
    }
}
