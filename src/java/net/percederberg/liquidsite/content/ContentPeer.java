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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.content;

import java.util.ArrayList;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseQuery;
import net.percederberg.liquidsite.db.DatabaseResults;

/**
 * A content database peer. This class contains static methods that
 * handles all accesses to the database representation of a content.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentPeer extends Peer {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(ContentPeer.class);

    /**
     * The content peer instance.
     */
    private static final Peer PEER = new ContentPeer();

    /**
     * Returns a list of all content objects revisions with the 
     * specified id.
     * 
     * @param id             the content id
     * 
     * @return the list of content objects found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectById(int id) throws ContentException {
        return doSelectById(id, null);
    }

    /**
     * Returns a list of all content object revisions with the 
     * specified id.
     * 
     * @param id             the content id
     * @param con            the database connection to use
     * 
     * @return the list of content objects found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectById(int id, 
                                         DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("content.select.id");
        DatabaseResults  res;
        
        query.addParameter(id);
        res = execute("reading content list", query, con);
        return PEER.createObjectList(res);
    }

    /**
     * Returns the content object with the specified id and revision.
     * 
     * @param id             the content id
     * @param revision       the content revision
     * 
     * @return the content found, or
     *         null if no matching content existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Content doSelectByRevision(int id, int revision)
        throws ContentException {

        return doSelectByRevision(id, revision, null);
    }

    /**
     * Returns the content object with the specified id and revision.
     * 
     * @param id             the content id
     * @param revision       the content revision
     * @param con            the database connection to use
     * 
     * @return the content found, or
     *         null if no matching content existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Content doSelectByRevision(int id,
                                             int revision, 
                                             DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("content.select.revision");
        DatabaseResults  res;
        
        query.addParameter(id);
        query.addParameter(revision);
        res = execute("reading content", query, con);
        return (Content) PEER.createObject(res);
    }

    /**
     * Returns the content object with the specified id and highest
     * revision.
     * 
     * @param id             the content id
     * 
     * @return the content found, or
     *         null if no matching content existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Content doSelectByMaxRevision(int id)
        throws ContentException {

        return doSelectByMaxRevision(id, null);
    }

    /**
     * Returns the content object with the specified id and highest
     * revision.
     * 
     * @param id             the content id
     * @param con            the database connection to use
     * 
     * @return the content found, or
     *         null if no matching content existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static Content doSelectByMaxRevision(int id, 
                                                DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("content.select.id");
        DatabaseResults  res;
        
        query.addParameter(id);
        res = execute("reading content", query, con);
        return (Content) PEER.createObject(res);
    }

    /**
     * Returns a list of content objects having the specified parent.
     * Only the latest revision of each content object will be 
     * returned.
     * 
     * @param parent         the parent content id
     * 
     * @return a list of all matching content objects
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByParent(int parent) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection();
        
        try {
            return doSelectByParent(parent, con);
        } finally {
            returnDatabaseConnection(con);
        }
    }

    /**
     * Returns a list of content objects having the specified parent.
     * Only the latest revision of each content object will be 
     * returned.
     * 
     * @param parent         the parent content id
     * @param con            the database connection to use
     * 
     * @return a list of all matching content objects
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectByParent(int parent, 
                                             DatabaseConnection con)
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("content.select.parent");
        DatabaseResults  res;
        ArrayList        list = new ArrayList();
        Content          content;
        
        query.addParameter(parent);
        res = execute("reading content list", query, con);
        try {
            for (int i = 0; i < res.getRowCount(); i++) {
                content = doSelectByMaxRevision(res.getRow(i).getInt("ID"),
                                                con);
                if (content.getParentId() == parent) {
                    list.add(content);
                }
            }
        } catch (DatabaseDataException e) {
            throw new ContentException(e.getMessage());
        }
        return list;
    }

    /**
     * Inserts a new content object into the database.
     * 
     * @param content        the content to insert
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doInsert(Content content) throws ContentException {
        doInsert(content, null);
    }

    /**
     * Inserts a new content object into the database.
     * 
     * @param content        the content to insert
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static synchronized void doInsert(Content content, 
                                             DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("content.insert");
        int            id = getNewId(con);

        content.validate();
        content.setId(id);
        query.addParameter(content.getDomainName());
        query.addParameter(id);
        query.addParameter(content.getRevision());
        query.addParameter(content.getCategory());
        query.addParameter(content.getName());
        query.addParameter(content.getParentId());
        query.addParameter(content.getOnline());
        query.addParameter(content.getOffline());
        query.addParameter(content.getModifiedDate());
        query.addParameter(content.getAuthorName());
        execute("inserting content", query, con);
        content.setModified(false);
        content.setPersistent(true);
    }
    
    /**
     * Updates a content object in the database.
     * 
     * @param content        the content to update
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(Content content) throws ContentException {
        doUpdate(content, null);
    }

    /**
     * Updates a content in the database.
     * 
     * @param content        the content to update
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doUpdate(Content content, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("content.update");

        content.validate();
        query.addParameter(content.getName());
        query.addParameter(content.getParentId());
        query.addParameter(content.getOnline());
        query.addParameter(content.getOffline());
        query.addParameter(content.getModifiedDate());
        query.addParameter(content.getAuthorName());
        query.addParameter(content.getId());
        query.addParameter(content.getRevision());
        execute("updating content", query, con);
        content.setModified(false);
        content.setPersistent(true);
    }
    
    /**
     * Deletes a content object revision from the database. This 
     * method also deletes all attributes of the specified object.
     * 
     * @param content        the content revision to delete
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Content content) throws ContentException {
        doDelete(content, null);
    }

    /**
     * Deletes a content object revision from the database. This 
     * method also deletes all attributes of the specified object.
     * 
     * @param content        the content revision to delete
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDelete(Content content, DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("content.delete");

        query.addParameter(content.getId());
        query.addParameter(content.getRevision());
        execute("deleting content", query, con);
        AttributePeer.doDeleteRevision(content.getId(), 
                                       content.getRevision(), 
                                       con);
        content.setModified(true);
        content.setPersistent(false);
    }
    
    /**
     * Deletes all revisions for a content object from the database. 
     * This method also deletes all attributes, permissions and locks
     * to the specified object.
     * 
     * @param id             the content object id to delete
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDeleteAllRevisions(int id) 
        throws ContentException {

        doDeleteAllRevisions(id, null);
    }

    /**
     * Deletes all revisions for a content object from the database. 
     * This method also deletes all attributes, permissions and locks
     * to the specified object.
     * 
     * @param id             the content object id to delete
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static void doDeleteAllRevisions(int id, DatabaseConnection con)
        throws ContentException {

        DatabaseQuery  query = new DatabaseQuery("content.delete.id");

        query.addParameter(id);
        execute("deleting content", query, con);
        AttributePeer.doDeleteAllRevisions(id, con);
        PermissionPeer.doDeleteContent(id, con);
        LockPeer.doDelete(id, con);
    }
    
    /**
     * Returns a new unique content identifier. This method will 
     * search the database for the maximum content identifier 
     * currently used and add one to it.
     * 
     * @param con            the database connection to use
     * 
     * @return the new unique content identifier
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    private static int getNewId(DatabaseConnection con) 
        throws ContentException {

        DatabaseQuery    query = new DatabaseQuery("content.select.maxid");
        DatabaseResults  res;
        int              id = 0;

        res = execute("finding max content id", query, con);
        if (res.getRowCount() > 0) {
            try {
                id = res.getRow(0).getInt(0);
            } catch (DatabaseDataException e) {
                LOG.error(e.getMessage());
                throw new ContentException(e.getMessage());
            }
        }
        return id + 1;
    }

    /**
     * Creates a new content database peer.
     */
    private ContentPeer() {
        super("content", Content.class);
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

        Content  content = (Content) obj;

        content.setDomainName(row.getString("DOMAIN"));
        content.setId(row.getInt("ID"));
        content.setRevision(row.getInt("REVISION"));
        content.setCategory(row.getInt("CATEGORY"));
        content.setName(row.getString("NAME"));
        content.setParentId(row.getInt("PARENT"));
        content.setOnline(row.getDate("ONLINE"));
        content.setOffline(row.getDate("OFFLINE"));
        content.setModifiedDate(row.getDate("MODIFIED"));
        content.setAuthorName(row.getString("AUTHOR"));
        content.setModified(false);
        content.setPersistent(true);
    }
}
