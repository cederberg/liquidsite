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

package net.percederberg.liquidsite.dbo;

import java.util.ArrayList;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseQuery;

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
     * Returns a list of all attribute objects with the specified
     * content id and revision.
     *
     * @param id             the content id
     * @param revision       the content revision
     * @param con            the database connection to use
     *
     * @return the list of attribute objects found
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static ArrayList doSelectByRevision(int id,
                                               int revision,
                                               DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.select.revision");

        query.addParameter(id);
        query.addParameter(revision);
        return PEER.selectList(query, con);
    }

    /**
     * Returns the attribute object with the specified content id,
     * content revision, and attribute name.
     *
     * @param id             the content id
     * @param revision       the content revision
     * @param name           the attribute name
     * @param con            the database connection to use
     *
     * @return the attribute found, or
     *         null if no matching attribute existed
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static AttributeData doSelectByName(int id,
                                               int revision,
                                               String name,
                                               DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.select.name");

        query.addParameter(id);
        query.addParameter(revision);
        query.addParameter(name);
        return (AttributeData) PEER.select(query, con);
    }

    /**
     * Inserts a new attribute object into the database.
     *
     * @param data           the attribute data object
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doInsert(AttributeData data, DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.insert");

        query.addParameter(data.getString(AttributeData.DOMAIN));
        query.addParameter(data.getInt(AttributeData.CONTENT));
        query.addParameter(data.getInt(AttributeData.REVISION));
        query.addParameter(data.getString(AttributeData.NAME));
        query.addParameter(data.getString(AttributeData.DATA));
        PEER.insert(query, con);
    }

    /**
     * Updates an attribute in the database.
     *
     * @param data           the attribute data object
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doUpdate(AttributeData data, DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.update");

        query.addParameter(data.getString(AttributeData.DATA));
        query.addParameter(data.getInt(AttributeData.CONTENT));
        query.addParameter(data.getInt(AttributeData.REVISION));
        query.addParameter(data.getString(AttributeData.NAME));
        PEER.update(query, con);
    }

    /**
     * Deletes an attribute object from the database.
     *
     * @param data           the attribute data object
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doDelete(AttributeData data, DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.delete");

        query.addParameter(data.getInt(AttributeData.CONTENT));
        query.addParameter(data.getInt(AttributeData.REVISION));
        query.addParameter(data.getString(AttributeData.NAME));
        PEER.delete(query, con);
    }

    /**
     * Deletes all attributes for a domain from the database.
     *
     * @param domain         the domain name
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doDeleteDomain(String domain, DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.delete.domain");

        query.addParameter(domain);
        PEER.delete(query, con);
    }

    /**
     * Deletes all attributes for a content object from the database.
     *
     * @param id             the content object id
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doDeleteContent(int id, DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.delete.content");

        query.addParameter(id);
        PEER.delete(query, con);
    }

    /**
     * Deletes all attributes for a content revision from the
     * database.
     *
     * @param id             the content object id
     * @param revision       the content revision
     * @param con            the database connection to use
     *
     * @throws DatabaseObjectException if the database couldn't be
     *             accessed properly
     */
    public static void doDeleteRevision(int id,
                                        int revision,
                                        DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("attribute.delete.revision");

        query.addParameter(id);
        query.addParameter(revision);
        PEER.delete(query, con);
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
