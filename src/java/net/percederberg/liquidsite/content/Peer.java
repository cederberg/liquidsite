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
import net.percederberg.liquidsite.db.DatabaseConnectionException;
import net.percederberg.liquidsite.db.DatabaseConnector;
import net.percederberg.liquidsite.db.DatabaseException;
import net.percederberg.liquidsite.db.DatabaseResults;

/**
 * The base database peer class. This class provides some of the 
 * functionality common to all database peers.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public abstract class Peer {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(Peer.class);

    /**
     * Executes a database function without parameters. 
     * 
     * @param name           the database function name
     * @param log            the message on error
     * 
     * @return the database results
     * 
     * @throws ContentException if the query or statement couldn't 
     *             be executed correctly
     */
    protected static DatabaseResults execute(String name, String log)
        throws ContentException {

        return execute(name, new ArrayList(), log);
    }

    /**
     * Executes a database function with parameters. 
     * 
     * @param name           the database function name
     * @param params         the database function parameters
     * @param log            the message on error
     * 
     * @return the database results
     * 
     * @throws ContentException if the query or statement couldn't 
     *             be executed correctly
     */
    protected static DatabaseResults execute(String name, 
                                             ArrayList params, 
                                             String log)
        throws ContentException {

        DatabaseConnector  db;
             
        db = ContentManager.getInstance().getApplication().getDatabase();
        if (db == null) {
            LOG.error("no database available");
            throw new ContentException("no database available");
        }
        try {
            return db.execute(name, params);
        } catch (DatabaseConnectionException e) {
            LOG.error(log, e);
            throw new ContentException(log, e);
        } catch (DatabaseException e) {
            LOG.error(log, e);
            throw new ContentException(log, e);
        }
    }
}
