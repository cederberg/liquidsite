/*
 * Peer.java
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
import net.percederberg.liquidsite.db.DatabaseConnectionException;
import net.percederberg.liquidsite.db.DatabaseConnector;
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseException;
import net.percederberg.liquidsite.db.DatabaseResults;

/**
 * The base database peer class. This class provides some of the 
 * functionality common to all database peers. This class is only 
 * instantiated to provide some convenience methods for its 
 * subclasses.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
abstract class Peer {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(Peer.class);

    /**
     * The data object name.
     */
    private String name;
    
    /**
     * The data object class.
     */
    private Class dataClass;
    
    /**
     * Returns the current content manager.
     * 
     * @return the current content manager
     * 
     * @throws ContentException if no content manager is available
     */
    protected static ContentManager getContentManager() 
        throws ContentException {

        return ContentManager.getInstance();
    }

    /**
     * Executes a database function with parameters.
     * 
     * @param name           the database function name
     * @param params         the database function parameters, or null
     * @param log            the log message on error
     * @param con            the database connection to use, or null
     * 
     * @return the database results
     * 
     * @throws ContentException if the query or statement couldn't 
     *             be executed correctly
     */
    protected static DatabaseResults execute(String name, 
                                             ArrayList params,
                                             String log,
                                             DatabaseConnection con)
        throws ContentException {

        DatabaseConnector  db = null;
             
        if (params == null) {
            params = new ArrayList(0);
        }
        if (con == null) {
            db = getContentManager().getApplication().getDatabase();
        }
        try {
            if (db != null) {
                return db.execute(name, params);
            } else if (con != null) {
                return con.execute(name, params);
            } else {
                LOG.error(log + ": no database available");
                throw new ContentException(log + ": no database available");
            }
        } catch (DatabaseConnectionException e) {
            LOG.error(log, e);
            throw new ContentException(log, e);
        } catch (DatabaseException e) {
            LOG.error(log, e);
            throw new ContentException(log, e);
        }
    }

    /**
     * Creates a new database peer.
     * 
     * @param name           the data object name
     * @param dataClass      the data object class
     */
    protected Peer(String name, Class dataClass) {
        this.name = name;
        this.dataClass = dataClass;
    }

    /**
     * Creates a new instance of the data object.
     * 
     * @return a new instance of the data object
     * 
     * @throws ContentException if a new instance couldn't be created
     */
    protected DataObject createObject() throws ContentException {
        Object  obj;
        String  message;

        // Create object instance
        try {
            obj = dataClass.newInstance();
        } catch (InstantiationException e) {
            message = "couldn't create " + name + " object";
            LOG.error(message, e);
            throw new ContentException(message, e);
        } catch (IllegalAccessException e) {
            message = "couldn't create " + name + " object";
            LOG.error(message, e);
            throw new ContentException(message, e);
        }

        // Check for data object instance
        if (obj instanceof DataObject) {
            return (DataObject) obj;
        } else {
            message = "created " + name + " object is not a data object";
            LOG.error(message);
            throw new ContentException(message);
        }
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
     * @throws ContentException if a new instance couldn't be created
     *             or if the database results were malformed
     */
    protected DataObject createObject(DatabaseResults res) 
        throws ContentException {

        DataObject  obj;
        
        if (res.getRowCount() < 1) {
            return null;
        } else {
            try {
                obj = createObject();
                transfer(res.getRow(0), obj);
            } catch (DatabaseDataException e) {
                LOG.error("reading " + name, e);
                throw new ContentException("reading " + name, e);
            }
        }
        return obj;
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
     * @throws ContentException if the new instances couldn't be 
     *             created or if the database results were malformed
     */
    protected ArrayList createObjectList(DatabaseResults res) 
        throws ContentException {

        ArrayList   list = new ArrayList();
        DataObject  obj;
        String      str;
        
        for (int i = 0; i < res.getRowCount(); i++) {
            obj = createObject();
            try {
                transfer(res.getRow(i), obj);
            } catch (DatabaseDataException e) {
                str = "reading " + name + " list";
                LOG.error(str, e);
                throw new ContentException(str, e);
            }
            list.add(obj);
        }
        return list;
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
    protected abstract void transfer(DatabaseResults.Row row, 
                                     DataObject obj) 
        throws DatabaseDataException;
}
