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
 * Copyright (c) 2004-2006 Per Cederberg. All rights reserved.
 */

package org.liquidsite.core.content;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.liquidsite.core.data.DataObjectException;
import org.liquidsite.core.data.DataSource;
import org.liquidsite.core.data.DomainAttributeData;
import org.liquidsite.core.data.DomainAttributePeer;
import org.liquidsite.core.data.DomainData;
import org.liquidsite.core.data.DomainPeer;
import org.liquidsite.core.data.DomainSizeData;
import org.liquidsite.core.data.DomainSizePeer;
import org.liquidsite.util.log.Log;

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
     * The mail sender address attribute name.
     */
    private static final String MAIL_FROM_ATTRIBUTE = "MAIL.FROM";

    /**
     * The host attribute name.
     */
    private static final String HOST_ATTRIBUTE = "HOST.";

    /**
     * The permitted domain name characters.
     */
    public static final String NAME_CHARS =
        UPPER_CASE + NUMBERS + BINDERS + ".";

    /**
     * The permitted host name characters.
     */
    public static final String HOST_NAME_CHARS =
        LOWER_CASE + NUMBERS + BINDERS + ".";

    /**
     * The domain data object.
     */
    private DomainData data;

    /**
     * The domain attributes. The attributes are indexed by their
     * unique names. All attributes are read from and stored to
     * the database upon reading and writing the domain.
     */
    private HashMap attributes;

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

        DataSource  src = getDataSource(manager);
        ArrayList   list;
        Domain[]    res;

        try {
            list = DomainPeer.doSelectAll(src);
            res = new Domain[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = new Domain(manager, (DomainData) list.get(i), src);
            }
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            src.close();
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

        DataSource  src = getDataSource(manager);
        DomainData  data;

        try {
            data = DomainPeer.doSelectByName(src, name);
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            src.close();
        }
        if (data == null) {
            return null;
        } else {
            return new Domain(manager, data, src);
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
        this.data.setDate(DomainData.CREATED, new Date());
        this.data.setDate(DomainData.MODIFIED, new Date());
        this.attributes = new HashMap();
    }

    /**
     * Creates a new domain from a data object. This constructor will
     * also read all domain attributes from the database.
     *
     * @param manager        the content manager to use
     * @param data           the domain data object
     * @param src            the data source to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private Domain(ContentManager manager,
                   DomainData data,
                   DataSource src)
        throws ContentException {

        super(manager, true);
        this.data = data;
        this.attributes = new HashMap();
        try {
            doReadAttributes(src);
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
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
     * Returns the domain creation date.
     *
     * @return the domain creation date
     */
    public Date getCreatedDate() {
        return data.getDate(DomainData.CREATED);
    }

    /**
     * Returns the domain last modification date.
     *
     * @return the domain last modification date
     */
    public Date getModifiedDate() {
        return data.getDate(DomainData.MODIFIED);
    }

    /**
     * Returns the mail sender address. This address will be used
     * for all mails sent from the domain. If this address is not set,
     * the server default address should be used.
     *
     * @return the mail sender address, or
     *         null for none
     */
    public String getMailFrom() {
        return (String) attributes.get(MAIL_FROM_ATTRIBUTE);
    }

    /**
     * Sets the mail sender address. This address will be used for
     * all mails sent from the domain. If this address is not set,
     * the server default address should be used. The address should
     * be formatted as a correct internet mail address, optionally
     * containing both the name and email address. 
     *
     * @param address        the new address, or null to remove
     *
     * @see javax.mail.internet.InternetAddress
     */
    public void setMailFrom(String address) {
        if (address == null || address.trim().length() == 0) {
            attributes.remove(MAIL_FROM_ATTRIBUTE);
        } else {
            attributes.put(MAIL_FROM_ATTRIBUTE, address);
        }
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
     * Returns the hosts belonging to this domain. The returned list can be
     * modified freely, as it is only a copy of the actual host data.
     *
     * @return an array of hosts in this domain
     *
     * @see DomainHost
     */
    public ArrayList getHosts() {
        ArrayList  list = new ArrayList();
        Iterator   iter = attributes.keySet().iterator();
        String     name;
        String     str;

        while (iter.hasNext()) {
            name = (String) iter.next();
            if (name.startsWith(HOST_ATTRIBUTE)) {
                str = name.substring(HOST_ATTRIBUTE.length());
                list.add(new DomainHost(str, (String) attributes.get(name)));
            }
        }
        return list;
    }

    /**
     * Adds or overwrites a host in the domain.
     *
     * @param name           the host name
     * @param description    the host description
     */
    public void addHost(String name, String description) {
        attributes.put(HOST_ATTRIBUTE + name, description);
    }

    /**
     * Removes all hosts belonging to the domain.
     */
    public void removeAllHosts() {
        Iterator  iter = attributes.keySet().iterator();
        String    name;

        while (iter.hasNext()) {
            name = (String) iter.next();
            if (name.startsWith(HOST_ATTRIBUTE)) {
                iter.remove();
            }
        }
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
        File  dir;

        dir = getContentManager().getBaseDir();
        if (dir == null) {
            throw new ContentException(
                "application base file directory not configured");
        }
        dir = new File(dir, getName());
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
     * Calculates the approximate size of a domain per category. This
     * calculation will sum the the size of all files in the domain with
     * an approximate lower estimate for the size of all content in the
     * domain. All values are calculated per content category.
     *
     * @return a list with domain size objects 
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     *
     * @see DomainSize
     */
    public ArrayList getSize() throws ContentException {
        ArrayList      res = new ArrayList();
        DataSource     src = getDataSource(getContentManager());
        ArrayList      list;
        DomainSizeData data;

        try {
            list = DomainSizePeer.doSelectByDomain(src, getName());
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            src.close();
        }
        for (int i = 0; i < list.size(); i++) {
            data = (DomainSizeData) list.get(i);
            if (data.getInt(DomainSizeData.CATEGORY) == Content.FILE_CATEGORY) {
                res.add(new DomainSize(data, getSize(getDirectory())));
            } else {
                res.add(new DomainSize(data));
            }
        }
        return res;
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
        Iterator  iter;
        String    name;
        Domain    domain;

        if (!isPersistent()) {
            validateSize("domain name", getName(), 1, 30);
            validateChars("domain name", getName(), NAME_CHARS);
            if (getContentManager().getDomain(getName()) != null) {
                throw new ContentException("domain '" + getName() +
                                           "' already exists");
            }
        }
        validateSize("domain description", getDescription(), 0, 100);
        iter = attributes.keySet().iterator();
        while (iter.hasNext()) {
            name = (String) iter.next();
            if (name.startsWith(HOST_ATTRIBUTE)) {
                name = name.substring(HOST_ATTRIBUTE.length());
                validateSize("host name", name, 1, 100);
                validateChars("host name", name, HOST_NAME_CHARS);
                domain = getContentManager().getHostDomain(name);
                if (domain != null && !domain.equals(this)) {
                    throw new ContentException("host '" + name +
                                               "' already exists");
                }
            }
        }
    }

    /**
     * Inserts the object data into the database. If the restore flag
     * is set, no automatic changes should be made to the data before
     * writing to the database.
     *
     * @param src            the data source to use
     * @param user           the user performing the operation
     * @param restore        the restore flag
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void doInsert(DataSource src, User user, boolean restore)
        throws ContentException {

        data.setDate(DomainData.CREATED, new Date());
        data.setDate(DomainData.MODIFIED, new Date());
        try {
            DomainPeer.doInsert(src, data);
            doWriteAttributes(src);
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Updates the object data in the database.
     *
     * @param src            the data source to use
     * @param user           the user performing the operation
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void doUpdate(DataSource src, User user)
        throws ContentException {

        data.setDate(DomainData.MODIFIED, new Date());
        try {
            DomainPeer.doUpdate(src, data);
            doWriteAttributes(src);
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Deletes the object data from the database.
     *
     * @param src            the data source to use
     * @param user           the user performing the operation
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void doDelete(DataSource src, User user)
        throws ContentException {

        try {
            DomainPeer.doDelete(src, data);
        } catch (DataObjectException e) {
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

    /**
     * Reads the domain attributes from the database. This method
     * will add all the attributes to the attributes map.
     *
     * @param src            the data source to use
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    private void doReadAttributes(DataSource src)
        throws DataObjectException {

        ArrayList            list;
        DomainAttributeData  attr;

        list = DomainAttributePeer.doSelectByDomain(src, getName());
        for (int i = 0; i < list.size(); i++) {
            attr = (DomainAttributeData) list.get(i);
            attributes.put(attr.getString(DomainAttributeData.NAME),
                           attr.getString(DomainAttributeData.DATA));
        }
    }

    /**
     * Writes the domain attributes to the database. This method
     * will first remove all existing attributes for the  domain,
     * and then insert all the currently existing attributes.
     *
     * @param src            the data source to use
     *
     * @throws DataObjectException if the data source couldn't be
     *             accessed properly
     */
    private void doWriteAttributes(DataSource src)
        throws DataObjectException {

        Iterator             iter = attributes.keySet().iterator();
        DomainAttributeData  attr;
        String               name;

        DomainAttributePeer.doDeleteDomain(src, getName());
        while (iter.hasNext()) {
            name = (String) iter.next();
            attr = new DomainAttributeData();
            attr.setString(DomainAttributeData.DOMAIN, getName());
            attr.setString(DomainAttributeData.NAME, name);
            attr.setString(DomainAttributeData.DATA,
                           attributes.get(name).toString());
            DomainAttributePeer.doInsert(src, attr);
        }
    }
}
