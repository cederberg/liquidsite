/*
 * AbstractPeer.java
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

import org.liquidsite.util.db.DatabaseDataException;
import org.liquidsite.util.db.DatabaseException;
import org.liquidsite.util.db.DatabaseQuery;
import org.liquidsite.util.db.DatabaseResults;
import org.liquidsite.util.log.Log;

/**
 * An abstract data object peer. This class provides some of the
 * functionality needed by all data object peers. The data object
 * peers each provide specialized static methods for operating on the
 * data objects by retrieving and storing them to the data source
 * (i.e. a database).
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public abstract class AbstractPeer {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(AbstractPeer.class);

    /**
     * The data object name.
     */
    private String name;

    /**
     * Creates a new peer for the specified database object.
     *
     * @param name           the database object name
     */
    protected AbstractPeer(String name) {
        this.name = name;
    }

    /**
     * Returns a new instance of the data object.
     *
     * @return a new instance of the data object
     */
    protected abstract AbstractData getDataObject();

    /**
     * Performs a database query that returns a single number as the
     * result. The data source will NOT be closed after this
     * operation.
     *
     * @param src            the data source to use
     * @param query          the database query
     *
     * @return the query result number, or
     *         zero (0) if no results were found
     *
     * @throws DataObjectException if the query couldn't be executed
     *             correctly, or the results were malformed
     */
    protected long count(DataSource src, DatabaseQuery query)
        throws DataObjectException {

        DatabaseResults  res;

        res = execute(src, "counting " + name + " rows or size", query);
        try {
            if (res.getRowCount() > 0) {
                return res.getRow(0).getLong(0);
            } else {
                return 0;
            }
        } catch (DatabaseDataException e) {
            LOG.error(e.getMessage());
            throw new DataObjectException(e);
        }
    }

    /**
     * Performs a database select query. This query is supposed to
     * return either one or zero rows. The data source will NOT be
     * closed after this operation.
     *
     * @param src            the data source to use
     * @param query          the database query
     *
     * @return the database object found, or
     *         null if the query didn't match any row
     *
     * @throws DataObjectException if the query couldn't be executed
     *             correctly, or the results were malformed
     */
    protected AbstractData select(DataSource src, DatabaseQuery query)
        throws DataObjectException {

        DatabaseResults  res;

        res = execute(src, "reading " + name, query);
        return createObject(res);
    }

    /**
     * Performs a database list select query. This query is supposed
     * to zero or more rows. The data source will NOT be closed after
     * this operation.
     *
     * @param src            the data source to use
     * @param query          the database query
     *
     * @return the list of database objects found
     *
     * @throws DataObjectException if the query couldn't be executed
     *             correctly, or the results were malformed
     */
    protected ArrayList selectList(DataSource src, DatabaseQuery query)
        throws DataObjectException {

        DatabaseResults  res;

        res = execute(src, "reading " + name + " list", query);
        return createObjectList(res);
    }

    /**
     * Performs a database insert statement. No database results are
     * returned. The data source will NOT be closed after this
     * operation.
     *
     * @param src            the data source to use
     * @param query          the database query
     *
     * @throws DataObjectException if the statement couldn't be
     *             executed correctly
     */
    protected void insert(DataSource src, DatabaseQuery query)
        throws DataObjectException {

        execute(src, "inserting " + name, query);
    }

    /**
     * Performs a database update statement. No database results are
     * returned. The data source will NOT be closed after this
     * operation.
     *
     * @param src            the data source to use
     * @param query          the database query
     *
     * @throws DataObjectException if the statement couldn't be
     *             executed correctly
     */
    protected void update(DataSource src, DatabaseQuery query)
        throws DataObjectException {

        execute(src, "updating " + name, query);
    }

    /**
     * Performs a database delete statement. No database results are
     * returned. The data source will NOT be closed after this
     * operation.
     *
     * @param src            the data source to use
     * @param query          the database query
     *
     * @throws DataObjectException if the statement couldn't be
     *             executed correctly
     */
    protected void delete(DataSource src, DatabaseQuery query)
        throws DataObjectException {

        execute(src, "deleting " + name, query);
    }

    /**
     * Executes a database query or statement. This method should
     * normally not be called directly by subclasses, unless a query
     * does not return results compatible with the data object.
     *
     * @param src            the data source to use
     * @param log            the log message
     * @param query          the database query
     *
     * @return the database results for a query, or
     *         null for database statements
     *
     * @throws DataObjectException if the query or statement
     *             couldn't be executed correctly
     */
    private DatabaseResults execute(DataSource src,
                                    String log,
                                    DatabaseQuery query)
        throws DataObjectException {

        DatabaseResults    res;

        try {
            LOG.trace(log);
            res = src.getConnection().execute(query);
            LOG.trace("done " + log);
        } catch (DatabaseException e) {
            LOG.error(log, e);
            throw new DataObjectException(log, e);
        }
        return res;
    }

    /**
     * Creates a new instance of the data object. The object will be
     * initialized with data from the specified database results.
     *
     * @param res            the database results
     *
     * @return a new instance of the data object, or
     *         null if the database results were empty
     *
     * @throws DataObjectException if the database results were
     *             malformed
     */
    private AbstractData createObject(DatabaseResults res)
        throws DataObjectException {

        AbstractData  data;
        String        message;

        if (res.getRowCount() < 1) {
            return null;
        } else {
            try {
                data = getDataObject();
                data.setAll(res.getRow(0));
            } catch (DatabaseDataException e) {
                message = "reading " + name;
                LOG.error(message, e);
                throw new DataObjectException(message, e);
            }
        }
        return data;
    }

    /**
     * Creates a list of new instances of data objects. The objects
     * will be initialized with data from the specified database
     * results.
     *
     * @param res            the database results
     *
     * @return a list of new data objects (may be empty)
     *
     * @throws DataObjectException if the database results were
     *             malformed
     */
    private ArrayList createObjectList(DatabaseResults res)
        throws DataObjectException {

        ArrayList     list = new ArrayList();
        AbstractData  data;
        String        message;

        for (int i = 0; i < res.getRowCount(); i++) {
            data = getDataObject();
            try {
                data.setAll(res.getRow(i));
            } catch (DatabaseDataException e) {
                message = "reading " + name + " list";
                LOG.error(message, e);
                throw new DataObjectException(message, e);
            }
            list.add(data);
        }
        return list;
    }
}
