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

package net.percederberg.liquidsite.dbo;

import java.util.ArrayList;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseException;
import net.percederberg.liquidsite.db.DatabaseQuery;
import net.percederberg.liquidsite.db.DatabaseResults;

import org.liquidsite.util.log.Log;

/**
 * An abstract database peer. This class provides some of the
 * functionality common to all database peers. Normally a database
 * peer provides static methods operating on it's one and only
 * instance.
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
     * Performs a database select query. This query is supposed to
     * return either one or zero rows.
     *
     * @param query          the database query
     * @param con            the database connection
     *
     * @return the database object found, or
     *         null if the query didn't match any row
     *
     * @throws DatabaseObjectException if the query couldn't be
     *             executed correctly, or the results were malformed
     */
    protected AbstractData select(DatabaseQuery query,
                                  DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseResults  res;

        res = execute("reading " + name, query, con);
        return createObject(res);
    }

    /**
     * Performs a database list select query. This query is supposed
     * to zero or more rows.
     *
     * @param query          the database query
     * @param con            the database connection
     *
     * @return the list of database objects found
     *
     * @throws DatabaseObjectException if the query couldn't be
     *             executed correctly, or the results were malformed
     */
    protected ArrayList selectList(DatabaseQuery query,
                                   DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseResults  res;

        res = execute("reading " + name + " list", query, con);
        return createObjectList(res);
    }

    /**
     * Performs a database insert statement. No database results are
     * returned
     *
     * @param query          the database query
     * @param con            the database connection
     *
     * @throws DatabaseObjectException if the statement couldn't be
     *             executed correctly
     */
    protected void insert(DatabaseQuery query, DatabaseConnection con)
        throws DatabaseObjectException {

        execute("inserting " + name, query, con);
    }

    /**
     * Performs a database update statement. No database results are
     * returned
     *
     * @param query          the database query
     * @param con            the database connection
     *
     * @throws DatabaseObjectException if the statement couldn't be
     *             executed correctly
     */
    protected void update(DatabaseQuery query, DatabaseConnection con)
        throws DatabaseObjectException {

        execute("updating " + name, query, con);
    }

    /**
     * Performs a database delete statement. No database results are
     * returned
     *
     * @param query          the database query
     * @param con            the database connection
     *
     * @throws DatabaseObjectException if the statement couldn't be
     *             executed correctly
     */
    protected void delete(DatabaseQuery query, DatabaseConnection con)
        throws DatabaseObjectException {

        execute("deleting " + name, query, con);
    }

    /**
     * Executes a database query or statement. This method should
     * normally not be called directly by subclasses, unless a query
     * does not return results compatible with the data object.
     *
     * @param log            the log message
     * @param query          the database query
     * @param con            the database connection to use, or null
     *
     * @return the database results for a query, or
     *         null for database statements
     *
     * @throws DatabaseObjectException if the query or statement
     *             couldn't be executed correctly
     */
    protected DatabaseResults execute(String log,
                                      DatabaseQuery query,
                                      DatabaseConnection con)
        throws DatabaseObjectException {

        DatabaseResults    res;

        try {
            LOG.trace(log);
            res = con.execute(query);
            LOG.trace("done " + log);
        } catch (DatabaseException e) {
            LOG.error(log, e);
            throw new DatabaseObjectException(log, e);
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
     * @throws DatabaseObjectException if the database results were
     *             malformed
     */
    private AbstractData createObject(DatabaseResults res)
        throws DatabaseObjectException {

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
                throw new DatabaseObjectException(message, e);
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
     * @throws DatabaseObjectException if the database results were
     *             malformed
     */
    private ArrayList createObjectList(DatabaseResults res)
        throws DatabaseObjectException {

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
                throw new DatabaseObjectException(message, e);
            }
            list.add(data);
        }
        return list;
    }
}
