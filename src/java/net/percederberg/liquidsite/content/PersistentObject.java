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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.content;

import java.util.HashMap;
import java.util.Iterator;

import org.liquidsite.util.db.DatabaseConnection;
import org.liquidsite.util.db.DatabaseConnectionException;
import org.liquidsite.util.db.DatabaseConnector;
import org.liquidsite.util.log.Log;

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
     * The content manager used for this object.
     */
    private ContentManager manager;

    /**
     * The persistent data flag. This flag should be set when the
     * object is read or written to the database.
     */
    private boolean persistent = false;

    /**
     * Returns the content manager database connector.
     *
     * @param manager        the content manager to use
     *
     * @return the content manager database connector
     *
     * @throws ContentException if no database connector was
     *             available
     */
    static DatabaseConnector getDatabase(ContentManager manager)
        throws ContentException {

        DatabaseConnector  db;

        db = manager.getApplication().getDatabase();
        if (db == null) {
            LOG.error("no database connector available");
            throw new ContentException("no database connector available");
        }
        return db;
    }

    /**
     * Returns a database connection.
     *
     * @param manager        the content manager to use
     *
     * @return a database connection
     *
     * @throws ContentException if no database connector is available
     *             or no database connection could be made
     */
    static DatabaseConnection getDatabaseConnection(ContentManager manager)
        throws ContentException {

        try {
            return getDatabase(manager).getConnection();
        } catch (DatabaseConnectionException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Disposes of a database connection.
     *
     * @param manager        the content manager to use
     * @param con            the database connection
     */
    static void returnDatabaseConnection(ContentManager manager,
                                         DatabaseConnection con) {
        try {
            getDatabase(manager).returnConnection(con);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            con.close();
        }
    }

    /**
     * Encodes a map into a string. The map values or keys must
     * consist of normal ASCII characters, and may NOT contain the
     * ':' or '=' characters as they are used in the encoding.
     *
     * @param map            the map to encode
     *
     * @return the encoded map string
     *
     * @see #decodeMap
     */
    protected static String encodeMap(HashMap map) {
        StringBuffer  buffer = new StringBuffer();
        Iterator      iter = map.keySet().iterator();
        String        name;
        Object        value;

        while (iter.hasNext()) {
            name = (String) iter.next();
            value = map.get(name);
            if (buffer.length() > 0) {
                buffer.append(":");
            }
            buffer.append(name);
            if (value != null) {
                buffer.append("=");
                buffer.append(value);
            }
        }
        return buffer.toString();
    }

    /**
     * Decodes a string into a map. The string must previously have
     * been encoded with the encodeMap method.
     *
     * @param str            the encoded string
     *
     * @return the unencoded map
     *
     * @see #encodeMap
     */
    protected static HashMap decodeMap(String str) {
        HashMap  map = new HashMap();
        String   name;
        String   value;
        String   temp;
        int      pos;

        while (str.length() > 0) {
            pos = str.indexOf(":");
            if (pos > 0) {
                temp = str.substring(0, pos);
                str = str.substring(pos + 1);
            } else {
                temp = str;
                str = "";
            }
            pos = temp.indexOf("=");
            if (pos > 0) {
                name = temp.substring(0, pos);
                value = temp.substring(pos + 1);
            } else {
                name = temp;
                value = null;
            }
            map.put(name, value);
        }
        return map;
    }

    /**
     * Creates a new persistent object.
     *
     * @param manager        the content manager to use
     * @param persistent     the persistent flag
     */
    protected PersistentObject(ContentManager manager,
                               boolean persistent) {

        this.manager = manager;
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
     * Checks the read access for a user.
     *
     * @param user           the user to check, or null for none
     *
     * @return true if the user has read access, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public final boolean hasReadAccess(User user)
        throws ContentException {

        return SecurityManager.getInstance().hasReadAccess(user, this);
    }

    /**
     * Checks the write access for a user.
     *
     * @param user           the user to check, or null for none
     *
     * @return true if the user has write access, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public final boolean hasWriteAccess(User user)
        throws ContentException {

        return SecurityManager.getInstance().hasWriteAccess(user, this);
    }

    /**
     * Checks the publish access for a user.
     *
     * @param user           the user to check, or null for none
     *
     * @return true if the user has publish access, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public final boolean hasPublishAccess(User user)
        throws ContentException {

        return SecurityManager.getInstance().hasPublishAccess(user, this);
    }

    /**
     * Checks the admin access for a user.
     *
     * @param user           the user to check, or null for none
     *
     * @return true if the user has admin access, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public final boolean hasAdminAccess(User user)
        throws ContentException {

        return SecurityManager.getInstance().hasAdminAccess(user, this);
    }

    /**
     * Returns the content manager used by this object.
     *
     * @return the content manager used by this object
     */
    public ContentManager getContentManager() {
        return manager;
    }

    /**
     * Saves this object to the database.
     *
     * @param user           the user performing the operation
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user specified didn't
     *             have write permissions
     */
    public final void save(User user)
        throws ContentException, ContentSecurityException {

        DatabaseConnection  con;

        con = getDatabaseConnection(getContentManager());
        try {
            save(user, con);
        } finally {
            returnDatabaseConnection(getContentManager(), con);
        }
    }

    /**
     * Saves this object to the database.
     *
     * @param user           the user performing the operation
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user specified didn't
     *             have write permissions
     */
    public final void save(User user, DatabaseConnection con)
        throws ContentException, ContentSecurityException {

        // Save to database
        if (!isPersistent()) {
            SecurityManager.getInstance().checkInsert(user, this);
            doValidate();
            doInsert(user, con, false);
            persistent = true;
        } else {
            SecurityManager.getInstance().checkUpdate(user, this);
            doValidate();
            doUpdate(user, con);
        }

        // Remove from cache
        CacheManager.getInstance().remove(this);
    }

    /**
     * Restores this object to the database. This method is only used
     * when restoring objects from a backup. Superuser permissions
     * are normally required for this operation.
     *
     * @param user           the user performing the operation
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user specified didn't
     *             have write permissions
     */
    public final void restore(User user)
        throws ContentException, ContentSecurityException {

        DatabaseConnection  con = getDatabaseConnection(getContentManager());

        try {
            restore(user, con);
        } finally {
            returnDatabaseConnection(getContentManager(), con);
        }
    }            

    /**
     * Restores this object to the database. This method is only used
     * when restoring objects from a backup. Superuser permissions
     * are normally required for this operation.
     *
     * @param user           the user performing the operation
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user specified didn't
     *             have write permissions
     */
    public final void restore(User user, DatabaseConnection con)
        throws ContentException, ContentSecurityException {

        // Store to database
        SecurityManager.getInstance().checkRestore(user, this);
        doValidate();
        doInsert(user, con, true);
        persistent = true;

        // Remove from cache
        CacheManager.getInstance().remove(this);
    }

    /**
     * Deletes this object from the database.
     *
     * @param user           the user performing the operation
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user specified didn't
     *             have write permissions
     */
    public final void delete(User user)
        throws ContentException, ContentSecurityException {

        DatabaseConnection  con;

        con = getDatabaseConnection(getContentManager());
        try {
            delete(user, con);
        } finally {
            returnDatabaseConnection(getContentManager(), con);
        }
    }

    /**
     * Deletes this object from the database.
     *
     * @param user           the user performing the operation
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user specified didn't
     *             have write permissions
     */
    public final void delete(User user, DatabaseConnection con)
        throws ContentException, ContentSecurityException {

        // Delete from database
        if (isPersistent()) {
            SecurityManager.getInstance().checkDelete(user, this);
            doDelete(user, con);
            persistent = false;
        }

        // Remove from cache
        CacheManager.getInstance().remove(this);
    }

    /**
     * Validates the object data before writing to the database.
     *
     * @throws ContentException if the object data wasn't valid
     */
    protected abstract void doValidate() throws ContentException;

    /**
     * Inserts the object data into the database. If the restore flag
     * is set, no automatic changes should be made to the data before
     * writing to the database.
     *
     * @param user           the user performing the operation
     * @param con            the database connection to use
     * @param restore        the restore flag
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected abstract void doInsert(User user,
                                     DatabaseConnection con,
                                     boolean restore)
        throws ContentException;

    /**
     * Updates the object data in the database.
     *
     * @param user           the user performing the operation
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected abstract void doUpdate(User user, DatabaseConnection con)
        throws ContentException ;

    /**
     * Deletes the object data from the database.
     *
     * @param user           the user performing the operation
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected abstract void doDelete(User user, DatabaseConnection con)
        throws ContentException;
}
