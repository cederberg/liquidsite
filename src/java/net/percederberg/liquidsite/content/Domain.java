/*
 * Domain.java
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import net.percederberg.liquidsite.Configuration;
import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.DatabaseObjectException;
import net.percederberg.liquidsite.dbo.DomainData;
import net.percederberg.liquidsite.dbo.DomainPeer;

/**
 * A resource and user domain.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Domain extends PersistentObject implements Comparable {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(Domain.class);

    /**
     * The domain data object.
     */
    private DomainData data;

    /**
     * The domain options. These options are read from and stored to
     * the data object upon reading and writing.
     */
    private HashMap options;

    /**
     * Returns an array of all domains in the database.
     *
     * @param manager        the content manager to use
     *
     * @return an array of all domains in the database
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Domain[] findAll(ContentManager manager)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ArrayList           list;
        Domain[]            res;

        try {
            list = DomainPeer.doSelectAll(con);
            res = new Domain[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = new Domain(manager, (DomainData) list.get(i));
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
        return res;
    }

    /**
     * Returns a domain with a specified name.
     *
     * @param manager        the content manager to use
     * @param name           the domain name
     *
     * @return the domain found, or
     *         null if no matching domain existed
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Domain findByName(ContentManager manager, String name)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        DomainData          data;

        try {
            data = DomainPeer.doSelectByName(name, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
        if (data == null) {
            return null;
        } else {
            return new Domain(manager, data);
        }
    }

    /**
     * Creates a new domain with default values.
     *
     * @param manager        the content manager to use
     * @param name           the domain name
     */
    public Domain(ContentManager manager, String name) {
        super(manager, false);
        this.data = new DomainData();
        this.data.setString(DomainData.NAME, name);
        this.options = new HashMap();
    }

    /**
     * Creates a new domain from a data object.
     *
     * @param manager        the content manager to use
     * @param data           the domain data object
     */
    private Domain(ContentManager manager, DomainData data) {
        super(manager, true);
        this.data = data;
        this.options = decodeMap(data.getString(DomainData.OPTIONS));
    }

    /**
     * Checks if this domain equals another object. This method will
     * only return true if the other object is a domain with the same
     * name.
     *
     * @param obj            the object to compare with
     *
     * @return true if the other object is an identical domain, or
     *         false otherwise
     */
    public boolean equals(Object obj) {
        if (obj instanceof Domain) {
            return getName().equals(((Domain) obj).getName());
        } else {
            return false;
        }
    }

    /**
     * Compares this object with the specified object for order.
     * Returns a negative integer, zero, or a positive integer as
     * this object is less than, equal to, or greater than the
     * specified object.
     *
     * @param obj            the object to compare to
     *
     * @return a negative integer, zero, or a positive integer as
     *         this object is less than, equal to, or greater than
     *         the specified object
     *
     * @throws ClassCastException if the object isn't a Domain object
     */
    public int compareTo(Object obj) throws ClassCastException {
        return compareTo((Domain) obj);
    }

    /**
     * Compares this object with the specified domain for order.
     * Returns a negative integer, zero, or a positive integer as
     * this object is less than, equal to, or greater than the
     * specified object. The ordering is based on domain name.
     *
     * @param domain         the domain to compare to
     *
     * @return a negative integer, zero, or a positive integer as
     *         this object is less than, equal to, or greater than
     *         the specified object
     */
    public int compareTo(Domain domain) {
        return getName().compareTo(domain.getName());
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();

        buffer.append("Domain: ");
        if (getDescription().equals("")) {
            buffer.append(getName());
        } else {
            buffer.append(getDescription());
        }
        return buffer.toString();
    }

    /**
     * Returns the unique domain name.
     *
     * @return the unique domain name
     */
    public String getName() {
        return data.getString(DomainData.NAME);
    }

    /**
     * Returns the domain description.
     *
     * @return the domain description
     */
    public String getDescription() {
        return data.getString(DomainData.DESCRIPTION);
    }

    /**
     * Sets the domain description.
     *
     * @param description    the new description
     */
    public void setDescription(String description) {
        data.setString(DomainData.DESCRIPTION, description);
    }

    /**
     * Returns the permission list applicable to this domain object. 
     * If the object has no permissions an empty permission list will
     * be returned.
     *
     * @return the permission list for this object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public PermissionList getPermissions() throws ContentException {
        return getContentManager().getPermissions(this);
    }

    /**
     * Returns the hosts registered to this domain object.
     *
     * @return an array of hosts in this domain
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public Host[] getHosts() throws ContentException {
        return Host.findByDomain(getContentManager(), this);
    }

    /**
     * Returns the domain file directory. This directory is composed
     * of the application file directory and the domain name. Note
     * that this method will create the domain directory if it does
     * not already exist.
     *
     * @return the domain file directory
     *
     * @throws ContentException if the domain file directory wasn't
     *             found or couldn't be created
     */
    public File getDirectory() throws ContentException {
        Configuration  config;
        String         basedir;
        File           dir;

        config = getContentManager().getApplication().getConfig();
        basedir = config.get(Configuration.FILE_DIRECTORY, null);
        if (basedir == null) {
            throw new ContentException(
                "application base file directory not configured");
        }
        dir = new File(basedir, getName());
        try {
            if (!dir.exists() && !dir.mkdirs()) {
                throw new ContentException(
                    "couldn't create domain file directory");
            }
        } catch (SecurityException e) {
            throw new ContentException(
                "access denied while creating domain file directory");
        }
        return dir;
    }

    /**
     * Calculates the approximate size of a domain. This calculation
     * will sum the the size of all files in the domain with an
     * approximate lower estimate for the size of all content in the
     * domain.
     *
     * @return the approx. size in bytes of the domain
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public long getSize() throws ContentException {
        return getSize(getDirectory()) +
               InternalContent.calculateDomainSize(getContentManager(), this);
    }

    /**
     * Calculates the size of a file or a directory. For directories
     * the sizes of all contained files will be summed and returned.
     *
     * @param file           the file or directory
     *
     * @return the size in bytes of the file or directory
     *
     * @throws ContentException if the file or directory couldn't be read
     *             properly
     */
    private long getSize(File file) throws ContentException {
        long    size = 0;
        File[]  files;

        try {
            if (file.isDirectory()) {
                files = file.listFiles();
                for (int i = 0; files != null && files.length > i; i++) {
                    size += getSize(files[i]);
                }
            } else {
                size = file.length();
            }
        } catch (SecurityException e) {
            throw new ContentException(
                "access denied while reading domain file directory");
        }
        return size;
    }

    /**
     * Validates the object data before writing to the database.
     *
     * @throws ContentException if the object data wasn't valid
     */
    protected void doValidate() throws ContentException {
        Domain  domain = getContentManager().getDomain(getName());

        if (getName().equals("")) {
            throw new ContentException("no name set for domain object");
        } else if (!isPersistent() && domain != null) {
            throw new ContentException("domain '" + getName() +
                                       "' already exists");
        }
    }

    /**
     * Inserts the object data into the database.
     *
     * @param user           the user performing the operation
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void doInsert(User user, DatabaseConnection con)
        throws ContentException {

        data.setString(DomainData.OPTIONS, encodeMap(options));
        try {
            DomainPeer.doInsert(data, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Updates the object data in the database.
     *
     * @param user           the user performing the operation
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void doUpdate(User user, DatabaseConnection con)
        throws ContentException {

        data.setString(DomainData.OPTIONS, encodeMap(options));
        try {
            DomainPeer.doUpdate(data, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Deletes the object data from the database.
     *
     * @param user           the user performing the operation
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void doDelete(User user, DatabaseConnection con)
        throws ContentException {

        try {
            DomainPeer.doDelete(data, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
        doDelete(getDirectory());
    }

    /**
     * Deletes a file or directory. All contents of a directory will
     * be deleted recursively.
     *
     * @param file           the file or directory to delete
     *
     * @throws ContentException if the file or directory couldn't be
     *             deleted properly
     */
    private void doDelete(File file) throws ContentException {
        File[]  files;

        try {
            files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    doDelete(files[i]);
                }
            }
            file.delete();
        } catch (SecurityException e) {
            LOG.error("deleting domain directory for " + getName() +
                      ": " + e.getMessage());
            throw new ContentException(
                "access denied while deleting domain file directory");
        }
    }
}
