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

package net.percederberg.liquidsite.dbo;

import java.util.ArrayList;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseQuery;
import net.percederberg.liquidsite.db.DatabaseResults;

/**
 * A content database peer. This class contains static methods that 
 * handles all accesses to the LS_CONTENT table.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentPeer extends AbstractPeer {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(ContentPeer.class);

    /**
     * The content peer instance.
     */
    private static final ContentPeer PEER = new ContentPeer();

    /**
     * Returns a list of all content object revisions with the 
     * specified id.
     * 
     * @param id             the content id
     * @param con            the database connection to use
     * 
     * @return the list of content objects found
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static ArrayList doSelectById(int id, 
                                         DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.select.id");
        
        query.addParameter(id);
        return PEER.selectList(query, con);
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
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static ContentData doSelectByRevision(int id,
                                                 int revision, 
                                                 DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.select.revision");
        
        query.addParameter(id);
        query.addParameter(revision);
        return (ContentData) PEER.select(query, con);
    }

    /**
     * Returns the content object with the specified id and highest
     * revision. A flag can be set to regard the revision number zero
     * (0) as the highest one.
     * 
     * @param id             the content id
     * @param maxIsZero      the revision zero is max flag
     * @param con            the database connection to use
     * 
     * @return the content found, or
     *         null if no matching content existed
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static ContentData doSelectByMaxRevision(int id,
                                                    boolean maxIsZero,
                                                    DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query;
        ContentData    data;

        if (maxIsZero) {
            data = doSelectByRevision(id, 0, con);
            if (data != null) {
                return data;
            }
        }
        query = new DatabaseQuery("content.select.id");
        query.addParameter(id);
        return (ContentData) PEER.select(query, con);
    }

    /**
     * Returns a list of content objects having the specified parent.
     * By setting the parent content id to zero (0), all the root
     * content objects in a domain can be retrieved. Only the highest
     * revision of each content object will be returned. A flag can
     * be set to regard the revision number zero (0) as the highest
     * one.
     * 
     * @param domain         the domain name
     * @param parent         the parent content id
     * @param maxIsZero      the revision zero is max flag
     * @param con            the database connection to use
     * 
     * @return a list of all matching content objects
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static ArrayList doSelectByParent(String domain,
                                             int parent,
                                             boolean maxIsZero,
                                             DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery    query = new DatabaseQuery("content.select.parent");
        DatabaseResults  res;
        ArrayList        list = new ArrayList();
        ContentData      data;
        
        query.addParameter(domain);
        query.addParameter(parent);
        res = PEER.execute("reading content list", query, con);
        try {
            for (int i = 0; i < res.getRowCount(); i++) {
                data = doSelectByMaxRevision(res.getRow(i).getInt("ID"),
                                             maxIsZero,
                                             con);
                if (data.getInt(ContentData.PARENT) == parent) {
                    list.add(data);
                }
            }
        } catch (DatabaseDataException e) {
            LOG.error(e.getMessage());
            throw new DatabaseObjectException(e);
        }
        return list;
    }

    /**
     * Returns a list of content objects having one of the specified
     * parents. By adding a parent content id of zero (0), the root
     * content objects in a domain can be retrieved. Only the highest
     * revision of each content object will be returned. A flag can
     * be set to regard the revision number zero (0) as the highest
     * one.
     *
     * @param domain         the domain name
     * @param parents        the array of parent content identifiers
     * @param maxIsZero      the revision zero is max flag
     * @param con            the database connection to use
     *
     * @return a list of all matching content objects
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static ArrayList doSelectByParents(String domain,
                                              int[] parents,
                                              boolean maxIsZero,
                                              DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery    query = new DatabaseQuery();
        StringBuffer     sql = new StringBuffer();
        DatabaseResults  res;
        ArrayList        list = new ArrayList();
        ContentData      data;

        sql.append("SELECT DISTINCT ID FROM LS_CONTENT WHERE DOMAIN = ");
        appendSql(sql, domain);
        sql.append(" AND ID NOT IN ");
        appendSql(sql, parents);
        sql.append(" AND PARENT IN ");
        appendSql(sql, parents);
        query.setSql(sql.toString());
        res = PEER.execute("reading content list", query, con);
        try {
            for (int i = 0; i < res.getRowCount(); i++) {
                data = doSelectByMaxRevision(res.getRow(i).getInt("ID"),
                                             maxIsZero,
                                             con);
                for (int j = 0; j < parents.length; j++) {
                    if (data.getInt(ContentData.PARENT) == parents[j]) {
                        list.add(data);
                        break;
                    }
                }
            }
        } catch (DatabaseDataException e) {
            LOG.error(e.getMessage());
            throw new DatabaseObjectException(e);
        }
        return list;
    }

    /**
     * Returns the content object with the specified parent and name.
     * If multiple objects should match, any one of them can be
     * returned. Matches of old revisions of content objects will be
     * discarded, and only the highest revision of a matching object
     * is returned. A flag can be set to regard the revision number 
     * zero (0) as the highest one.
     *
     * @param domain         the domain name
     * @param parent         the parent content id
     * @param name           the content name
     * @param maxIsZero      the revision zero is max flag
     * @param con            the database connection to use
     *
     * @return the content found, or
     *         null if no matching content existed
     *
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static ContentData doSelectByName(String domain,
                                             int parent,
                                             String name,
                                             boolean maxIsZero,
                                             DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery    query = new DatabaseQuery("content.select.name");
        DatabaseResults  res;
        ContentData      data;
        
        query.addParameter(domain);
        query.addParameter(parent);
        query.addParameter(name);
        res = PEER.execute("reading content list", query, con);
        try {
            for (int i = 0; i < res.getRowCount(); i++) {
                data = doSelectByMaxRevision(res.getRow(i).getInt("ID"),
                                             maxIsZero,
                                             con);
                if (data.getString(ContentData.NAME).equals(name)) {
                    return data;
                }
            }
        } catch (DatabaseDataException e) {
            LOG.error(e.getMessage());
            throw new DatabaseObjectException(e);
        }
        return null;
    }

    /**
     * Returns a list of content objects having the specified 
     * category. Only the highest revision of each content object
     * will be returned. A flag can be set to regard the revision
     * number zero (0) as the highest one.
     * 
     * @param domain         the domain name
     * @param parent         the parent content id
     * @param category       the category
     * @param maxIsZero      the revision zero is max flag
     * @param con            the database connection to use
     * 
     * @return a list of all matching content objects
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static ArrayList doSelectByCategory(String domain,
                                               int parent,
                                               int category,
                                               boolean maxIsZero,
                                               DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery    query = new DatabaseQuery("content.select.category");
        DatabaseResults  res;
        ArrayList        list = new ArrayList();
        ContentData      data;
        
        query.addParameter(domain);
        query.addParameter(parent);
        query.addParameter(category);
        res = PEER.execute("reading content list", query, con);
        try {
            for (int i = 0; i < res.getRowCount(); i++) {
                data = doSelectByMaxRevision(res.getRow(i).getInt("ID"),
                                             maxIsZero,
                                             con);
                list.add(data);
            }
        } catch (DatabaseDataException e) {
            LOG.error(e.getMessage());
            throw new DatabaseObjectException(e);
        }
        return list;
    }

    /**
     * Inserts a new content object into the database. This method 
     * will assign a new content id if it is set to zero (0).
     * 
     * @param data           the content data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static synchronized void doInsert(ContentData data, 
                                             DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.insert");

        if (data.getInt(ContentData.ID) <= 0) {
            data.setInt(ContentData.ID, getNewId(con));
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
        PEER.insert(query, con);
    }
    
    /**
     * Updates a content object in the database.
     * 
     * @param data           the content data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doUpdate(ContentData data, DatabaseConnection con) 
        throws DatabaseObjectException {

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
        PEER.update(query, con);
    }
    
    /**
     * Deletes a content object from the database. This method will
     * delete all revisions, as well as related attributes, 
     * permissions and locks. Note, however, that it will NOT delete 
     * referenced child objects.
     * 
     * @param data           the content data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doDelete(ContentData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.delete");
        int            id = data.getInt(ContentData.ID);

        query.addParameter(id);
        PEER.delete(query, con);
        AttributePeer.doDeleteContent(id, con);
        PermissionPeer.doDeleteContent(id, con);
        LockPeer.doDeleteContent(id, con);
    }
    
    /**
     * Deletes all content objects in a domain from the database. 
     * This method also deletes all attributes, permissions and locks
     * in the domain.
     * 
     * @param domain         the domain name
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    public static void doDeleteDomain(String domain, DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseQuery  query = new DatabaseQuery("content.delete.domain");

        query.addParameter(domain);
        PEER.delete(query, con);
        AttributePeer.doDeleteDomain(domain, con);
        PermissionPeer.doDeleteDomain(domain, con);
        LockPeer.doDeleteDomain(domain, con);
    }
    
    /**
     * Deletes a content object revision from the database. This 
     * method also deletes all related attributes.
     * 
     * @param id             the content identifier
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

        DatabaseQuery  query = new DatabaseQuery("content.delete.revision");

        query.addParameter(id);
        query.addParameter(revision);
        PEER.delete(query, con);
        AttributePeer.doDeleteRevision(id, revision, con);
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
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    private static int getNewId(DatabaseConnection con) 
        throws DatabaseObjectException {

        DatabaseQuery    query = new DatabaseQuery("content.select.maxid");
        DatabaseResults  res;
        int              id = 0;

        res = PEER.execute("finding max content id", query, con);
        if (res.getRowCount() > 0) {
            try {
                id = res.getRow(0).getInt(0);
            } catch (DatabaseDataException e) {
                LOG.error(e.getMessage());
                throw new DatabaseObjectException(e);
            }
        }
        return id + 1;
    }

    /**
     * Appends a string to an SQL statement.
     *
     * @param sql            the SQL statement
     * @param value          the value to append
     */
    private static void appendSql(StringBuffer sql, String value) {
        char  c;

        if (value == null) {
            sql.append("null");
        } else {
            sql.append("'");
            for (int i = 0; i < value.length(); i++) {
                c = value.charAt(i);
                if (c == '\'') {
                    sql.append("\\'"); 
                } else {
                    sql.append(c);
                }
            }
            sql.append("'");
        }
    }

    /**
     * Appends an array to an SQL statement.
     *
     * @param sql            the SQL statement
     * @param values         the array of values to append
     */
    private static void appendSql(StringBuffer sql, int[] values) {
        sql.append("(");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append(values[i]);
        }
        sql.append(")");
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
