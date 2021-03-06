/*
 * Lock.java
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

package org.liquidsite.core.content;

import java.util.Date;

import org.liquidsite.core.data.DataObjectException;
import org.liquidsite.core.data.DataSource;
import org.liquidsite.core.data.LockData;
import org.liquidsite.core.data.LockPeer;
import org.liquidsite.util.log.Log;

/**
 * A content object lock.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Lock extends PersistentObject {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(Lock.class);

    /**
     * The lock data object.
     */
    private LockData data;

    /**
     * Returns the lock object with the specified content id.
     *
     * @param manager        the content manager to use
     * @param content        the content object
     *
     * @return the lock found, or
     *         null if no matching lock existed
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static Lock findByContent(ContentManager manager, Content content)
        throws ContentException {

        DataSource  src = getDataSource(manager);
        LockData    data;

        try {
            data = LockPeer.doSelectByContent(src, content.getId());
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            src.close();
        }
        if (data == null) {
            return null;
        } else {
            return new Lock(manager, data);
        }
    }

    /**
     * Creates a new lock with default values.
     *
     * @param manager        the content manager to use
     * @param content        the content object
     */
    public Lock(ContentManager manager, Content content) {
        super(manager, false);
        this.data = new LockData();
        this.data.setString(LockData.DOMAIN, content.getDomainName());
        this.data.setInt(LockData.CONTENT, content.getId());
        this.data.setString(LockData.USER, "");
        this.data.setDate(LockData.ACQUIRED, new Date());
    }

    /**
     * Creates a new lock from a data object.
     *
     * @param manager        the content manager to use
     * @param data           the lock data object
     */
    private Lock(ContentManager manager, LockData data) {
        super(manager, true);
        this.data = data;
    }

    /**
     * Checks if this lock equals another object. This method will
     * only return true if the other object is a lock with the same
     * content identifier.
     *
     * @param obj            the object to compare with
     *
     * @return true if the other object is an identical lock, or
     *         false otherwise
     */
    public boolean equals(Object obj) {
        if (obj instanceof Lock) {
            return equals((Lock) obj);
        } else {
            return false;
        }
    }

    /**
     * Checks if this lock equals another object. This method will
     * only return true if the other object is a lock with the same
     * content identifier.
     *
     * @param obj            the object to compare with
     *
     * @return true if the other object is an identical lock, or
     *         false otherwise
     */
    public boolean equals(Lock obj) {
        return getContentId() == obj.getContentId();
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();

        buffer.append("Domain: ");
        buffer.append(getDomainName());
        buffer.append(", Content: ");
        buffer.append(getContentId());
        buffer.append(", User: ");
        buffer.append(getUserName());
        buffer.append(", Acquired: ");
        buffer.append(getAcquiredDate());

        return buffer.toString();
    }

    /**
     * Checks if the specified user is the lock owner.
     *
     * @param user           the user to check
     *
     * @return true if the user is the lock owner, or
     *         false otherwise
     */
    public boolean isOwner(User user) {
        return user != null
            && getUserName().equals(user.getName());
    }

    /**
     * Returns the content domain.
     *
     * @return the content domain
     *
     * @throws ContentException if no content manager is available
     */
    public Domain getDomain() throws ContentException {
        return getContentManager().getDomain(getDomainName());
    }

    /**
     * Returns the content domain name
     *
     * @return the content domain name
     */
    public String getDomainName() {
        return data.getString(LockData.DOMAIN);
    }

    /**
     * Returns the content object.
     *
     * @return the content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public Content getContent() throws ContentException {
        return getContentManager().getContent(getContentId());
    }

    /**
     * Returns the content identifier.
     *
     * @return the content identifier
     */
    public int getContentId() {
        return data.getInt(LockData.CONTENT);
    }

    /**
     * Returns the lock owner user.
     *
     * @return the lock owner user
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public User getUser() throws ContentException {
        return getContentManager().getUser(getDomain(), getUserName());
    }

    /**
     * Returns the lock owner user name.
     *
     * @return the lock owner user name
     */
    public String getUserName() {
        return data.getString(LockData.USER);
    }

    /**
     * Returns the lock acquired date.
     *
     * @return the lock acquired date
     */
    public Date getAcquiredDate() {
        return data.getDate(LockData.ACQUIRED);
    }

    /**
     * Validates the object data before writing to the database.
     *
     * @throws ContentException if the object data wasn't valid
     */
    protected void doValidate() throws ContentException {
        if (getDomainName().equals("")) {
            throw new ContentException("no domain set for lock object");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + getDomainName() +
                                       "'does not exist");
        } else if (getContentId() <= 0) {
            throw new ContentException("no content object set for lock");
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
    protected void doInsert(DataSource src,
                            User user,
                            boolean restore)
        throws ContentException {

        if (!restore) {
            data.setString(LockData.USER, user.getName());
            data.setDate(LockData.ACQUIRED, new Date());
        }
        try {
            LockPeer.doInsert(src, data);
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

        throw new ContentException("content locks cannot be updated");
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
            LockPeer.doDelete(src, data);
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }
}
