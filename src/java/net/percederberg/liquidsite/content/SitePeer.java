/*
 * SitePeer.java
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
import net.percederberg.liquidsite.db.DatabaseDataException;
import net.percederberg.liquidsite.db.DatabaseResults;

/**
 * A web site database peer. This class contains static methods that
 * handles all accesses to the database representation of a web site.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class SitePeer extends Peer {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(SitePeer.class);

    /**
     * Returns a list of all sites in the database.
     * 
     * @return a list of all sites in the database
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static ArrayList doSelectAll() throws ContentException {
        DatabaseResults  res;
        ArrayList        list = new ArrayList();
        Site             site;        
        
        res = execute("site.select", "reading sites");
        for (int i = 0; i < res.getRowCount(); i++) {
            site = new Site();
            try {
                transfer(res.getRow(i), site);
            } catch (DatabaseDataException e) {
                LOG.error("reading sites", e);
                throw new ContentException("reading sites", e);
            }
            list.add(site);
        }
        return list;
    }

    /**
     * Inserts a new site into the database. This method also updates 
     * the content manager site cache.
     * 
     * @param site           the site to insert
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static synchronized void doInsert(Site site) 
        throws ContentException {

        ArrayList  params = new ArrayList();
        int        id = getMaxId() + 1;

        params.add(new Integer(id));
        params.add(site.getName());
        params.add(site.getHost());
        params.add(new Integer(site.getPort()));
        params.add(site.getDirectory());
        execute("site.insert", params, "inserting site");
        site.setId(id);
        ContentManager.getInstance().addSite(site);
    }
    
    /**
     * Updates a site in the database. This method also updates the
     * content manager site cache.
     * 
     * @param site           the site to update
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static synchronized void doUpdate(Site site) 
        throws ContentException {

        ArrayList  params = new ArrayList();

        params.add(site.getName());
        params.add(site.getHost());
        params.add(new Integer(site.getPort()));
        params.add(site.getDirectory());
        params.add(new Integer(site.getId()));
        execute("site.update", params, "updating site");
        ContentManager.getInstance().addSite(site);
    }
    
    /**
     * Deletes a site from the database. This method also updates the 
     * content manager site cache.
     * 
     * @param site           the site to delete
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public static synchronized void doDelete(Site site) 
        throws ContentException {

        ArrayList  params = new ArrayList();

        params.add(new Integer(site.getId()));
        execute("site.delete", params, "deleting site");
        ContentManager.getInstance().removeSite(site);
    }
    
    /**
     * Returns the maximum site id in the database.
     * 
     * @return the maximum site id in the database, or
     *         zero (0) if no sites exists in the database
     *  
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    private static int getMaxId() throws ContentException {
        DatabaseResults  res;
        int              max = 0;

        res = execute("site.select.maxid", "reading max site id");
        if (res.getRowCount() > 0) {
            try {
                max = res.getRow(0).getInt(0);
            } catch (DatabaseDataException e) {
                LOG.error("reading max site id", e);
                throw new ContentException("reading max site id", e);
            }
        }
        return max;
    }

    /**
     * Transfers all a database result row to a site object.
     * 
     * @param row            the database result row
     * @param site           the site object
     * 
     * @throws DatabaseDataException if the database results didn't
     *             contain the expected column names
     */
    private static void transfer(DatabaseResults.Row row, Site site) 
        throws DatabaseDataException {

        site.setId(row.getInt("ID"));
        site.setName(row.getString("NAME"));
        site.setHost(row.getString("HOST"));
        site.setPort(row.getInt("PORT"));
        site.setDirectory(row.getString("DIRECTORY"));
    }
}
