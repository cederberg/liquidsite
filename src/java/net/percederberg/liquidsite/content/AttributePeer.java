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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.content;

import java.util.ArrayList;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseQuery;
import net.percederberg.liquidsite.db.DatabaseResults;

/**
 * A content attribute database peer. This class contains static 
 * methods that handles all accesses to the database representation 
 * of a content attribute.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class AttributePeer extends Peer {

    /**
     * The attribute peer instance.
     */
    private static final Peer PEER = new AttributePeer();

    /**
     * Returns a list of all attribute objects with the specified 
     * content id and revision.
     * 
     * @param id             the content id
     * @param revision       the content revision
     * 
     * @return the list of attribute objects found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByRevision(int id, int revision) 
        throws ContentException {

        return doSelectByRevision(id, revision, null);
    }

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
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByRevision(int id,
                                               int revision, 
                                               DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("attribute.select.revision");
        DatabaseResults  res;
        
        query.addParameter(id);
        query.addParameter(revision);
        res = execute("reading attribute list", query, con);
        return PEER.createObjectList(res);
    }

    /**
     * Returns the attribute object with the specified content id,
     * content revision, and attribute name.
     * 
     * @param id             the content id
     * @param revision       the content revision
     * @param name           the attribute name
     * 
     * @return the attribute found, or
     *         null if no matching attribute existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Attribute doSelectByName(int id, 
                                           int revision, 
                                           String name)
        throws ContentException {

        return doSelectByName(id, revision, name, null);
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
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Attribute doSelectByName(int id,
                                           int revision,
                                           String name, 
                                           DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("attribute.select.name");
        DatabaseResults  res;
        
        query.addParameter(id);
        query.addParameter(revision);
        query.addParameter(name);
        res = execute("reading attribute", query, con);
        return (Attribute) PEER.createObject(res);
    }

    /**
     * Inserts a new attribute object into the database.
     * 
     * @param attribute      the attribute to insert
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(Attribute attribute) 
        throws ContentException {

        doInsert(attribute, null);
    }

    /**
     * Inserts a new attribute object into the database.
     * 
     * @param attribute      the attribute to insert
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(Attribute attribute, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("attribute.insert");

        attribute.validate();
        query.addParameter(attribute.getDomainName());
        query.addParameter(attribute.getContentId());
        query.addParameter(attribute.getRevision());
        query.addParameter(attribute.getCategory());
        query.addParameter(attribute.getName());
        query.addParameter(attribute.getData());
        query.addParameter(attribute.getSearchable());
        execute("inserting attribute", query, con);
        attribute.setModified(false);
        attribute.setPersistent(true);
    }
    
    /**
     * Updates an attribute in the database.
     * 
     * @param attribute      the attribute to update
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(Attribute attribute) throws ContentException {
        doUpdate(attribute, null);
    }

    /**
     * Updates an attribute in the database.
     * 
     * @param attribute      the attribute to update
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(Attribute attribute, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("attribute.update");

        attribute.validate();
        query.addParameter(attribute.getData());
        query.addParameter(attribute.getSearchable());
        query.addParameter(attribute.getContentId());
        query.addParameter(attribute.getRevision());
        query.addParameter(attribute.getName());
        execute("updating attribute", query, con);
        attribute.setModified(false);
        attribute.setPersistent(true);
    }
    
    /**
     * Deletes an attribute object from the database.
     * 
     * @param attribute      the attribute to delete
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Attribute attribute)
        throws ContentException {

        doDelete(attribute, null);
    }

    /**
     * Deletes an attribute object revision from the database.
     * 
     * @param attribute      the attribute to delete
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Attribute attribute, DatabaseConnection con)
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("attribute.delete");

        query.addParameter(attribute.getContentId());
        query.addParameter(attribute.getRevision());
        query.addParameter(attribute.getName());
        execute("deleting attribute", query, con);
        attribute.setModified(true);
        attribute.setPersistent(false);
    }
    
    /**
     * Deletes all attributes for all revisions of a content object 
     * from the database.
     * 
     * @param id             the content object id
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDeleteAllRevisions(int id) 
        throws ContentException {

        doDeleteAllRevisions(id, null);
    }

    /**
     * Deletes all attributes for all revisions of a content object 
     * from the database.
     * 
     * @param id             the content object id
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDeleteAllRevisions(int id, DatabaseConnection con)
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("attribute.delete.content");

        query.addParameter(id);
        execute("deleting attributes", query, con);
    }
    
    /**
     * Deletes all attributes for a specified revision of a content 
     * object from the database.
     * 
     * @param id             the content object id
     * @param revision       the content revision
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDeleteRevision(int id, int revision) 
        throws ContentException {

        doDeleteRevision(id, revision, null);
    }

    /**
     * Deletes all attributes for a specified revision of a content 
     * object from the database.
     * 
     * @param id             the content object id
     * @param revision       the content revision
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDeleteRevision(int id,
                                        int revision,
                                        DatabaseConnection con)
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("attribute.delete.revision");

        query.addParameter(id);
        query.addParameter(revision);
        execute("deleting attributes", query, con);
    }
    
    /**
     * Creates a new attribute database peer.
     */
    private AttributePeer() {
        super("attribute", Attribute.class);
    }

    /**
     * Transfers a database result row to a data object.
     * 
     * @param row            the database result row
     * @param obj            the data object
     * 
     * @throws DatabaseDataException if the database results didn't
     *             contain the expected column names
     */
    protected void transfer(DatabaseResults.Row row, DataObject obj) 
        throws DatabaseDataException {

        Attribute  attribute = (Attribute) obj;

        attribute.setDomainName(row.getString("DOMAIN"));
        attribute.setContentId(row.getInt("CONTENT"));
        attribute.setRevision(row.getInt("REVISION"));
        attribute.setCategory(row.getInt("CATEGORY"));
        attribute.setName(row.getString("NAME"));
        attribute.setData(row.getString("DATA"));
        attribute.setSearchable(row.getBoolean("SEARCHABLE"));
        attribute.setModified(false);
        attribute.setPersistent(true);
    }
}
