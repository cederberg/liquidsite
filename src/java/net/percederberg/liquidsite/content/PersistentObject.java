/*
 * PersistentObject.java
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

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.db.DatabaseConnectionException;
import net.percederberg.liquidsite.db.DatabaseConnector;
import net.percederberg.liquidsite.dbo.DatabaseObjectException;

/**
 * A persistent object.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public abstract class PersistentObject {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(PersistentObject.class);

    /**
     * The persistent data flag. This flag should be set when the 
     * object is read or written to the database. 
     */
    private boolean persistent = false;

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
     * Returns the current application database connector.
     * 
     * @return the current application database connector
     * 
     * @throws ContentException if no content manager or database
     *             connector is available
     */
    protected static DatabaseConnector getDatabase() 
        throws ContentException {
        
        DatabaseConnector  db;

        db = getContentManager().getApplication().getDatabase();
        if (db == null) {
            LOG.error("no database connector available");
            throw new ContentException("no database connector available");
        }
        return db;
    }

    /**
     * Returns a database connection.
     * 
     * @return a database connection
     * 
     * @throws ContentException if no database connector is available
     *             or connection could be made
     */
    protected static DatabaseConnection getDatabaseConnection() 
        throws ContentException {
        
        try {
            return getDatabase().getConnection();
        } catch (DatabaseConnectionException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Disposes of a database connection.
     * 
     * @param con            the database connection
     */
    protected static void returnDatabaseConnection(DatabaseConnection con) {
        try {
            getDatabase().returnConnection(con);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            con.close();
        }
    }

    /**
     * Creates a new persistent object.
     * 
     * @param persistent     the persistent flag
     */
    protected PersistentObject(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * Checks if this object is persistent. I.e. if it has been 
     * stored to the database.
     * 
     * @return true if the object is persistent, or
     *         false otherwise
     */
    public boolean isPersistent() {
        return persistent;
    }

    /**
     * Validates this content object. This method is called before 
     * writing the data to the database. 
     * 
     * @throws ContentException if the data object contained errors
     */
    public abstract void validate() throws ContentException;

    /**
     * Saves this object to the database.
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public void save() throws ContentException {
        DatabaseConnection  con;
        
        validate();
        con = getDatabaseConnection();
        try {
            if (!isPersistent()) {
                doInsert(con);
                persistent = true;
            } else {
                doUpdate(con);
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(con);
        }
    }
    
    /**
     * Deletes this object from the database.
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public void delete() throws ContentException {
        DatabaseConnection  con = getDatabaseConnection();

        try {
            doDelete(con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(con);
        }
    }

    /**
     * Inserts the object data into the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    protected abstract void doInsert(DatabaseConnection con)
        throws DatabaseObjectException;

    /**
     * Updates the object data in the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    protected abstract void doUpdate(DatabaseConnection con)
        throws DatabaseObjectException;

    /**
     * Deletes the object data from the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    protected abstract void doDelete(DatabaseConnection con)
        throws DatabaseObjectException;
}
