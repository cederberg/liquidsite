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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.content;

import java.util.Date;

import net.percederberg.liquidsite.db.DatabaseConnection;

/**
 * A content object lock.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Lock extends DataObject {

    /**
     * The domain the content object belongs to.
     */
    private String domain = "";

    /**
     * The content identifier.
     */
    private int content = 0;
    
    /**
     * The user name.
     */
    private String user = "";

    /**
     * The lock acquiring date.
     */
    private Date acquired = new Date();

    /**
     * Creates a new lock with default values.
     */
    public Lock() {
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
        return content == obj.content;
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();
        
        buffer.append("Object: ");
        buffer.append(content);
        buffer.append(" locked by ");
        buffer.append(user);
        buffer.append(" at ");
        buffer.append(acquired);

        return buffer.toString();
    }
    
    /**
     * Returns the content domain.
     * 
     * @return the content domain
     * 
     * @throws ContentException if no content manager is available
     */
    public Domain getDomain() throws ContentException {
        return ContentManager.getInstance().getDomain(domain);
    }
    
    /**
     * Sets the content domain.
     * 
     * @param domain         the new domain
     */
    public void setDomain(Domain domain) {
        setDomainName(domain.getName());
    }
    
    /**
     * Returns the content domain name
     * 
     * @return the content domain name
     */
    public String getDomainName() {
        return domain;
    }
    
    /**
     * Sets the content domain.
     * 
     * @param domain         the new domain name
     */
    public void setDomainName(String domain) {
        this.domain = domain;
        setModified(true);
    }
    
    /**
     * Returns the content identifier.
     * 
     * @return the content identifier
     */
    public int getContentId() {
        return content;
    }

    /**
     * Sets the content identifier.
     * 
     * @param content        the new identifier
     */
    public void setContentId(int content) {
        this.content = content;
        setModified(true);
    }

    /**
     * Returns the lock user name.
     * 
     * @return the lock user name
     */
    public String getUserName() {
        return user;
    }

    /**
     * Sets the lock user name.
     * 
     * @param user           the new lock user name
     */
    public void setUserName(String user) {
        this.user = user;
        setModified(true);
    }

    /**
     * Returns the lock acquired date and time.
     * 
     * @return the lock acquired date and time
     */
    public Date getAcquired() {
        return acquired;
    }

    /**
     * Sets the lock acquired date and time.
     * 
     * @param acquired       the new lock acquired date and time
     */
    public void setAcquired(Date acquired) {
        this.acquired = acquired;
        setModified(true);
    }

    /**
     * Validates this data object. This method checks that all 
     * required fields have been filled with suitable values.
     * 
     * @throws ContentException if the data object contained errors
     */
    public void validate() throws ContentException {
        if (domain.equals("")) {
            throw new ContentException("no domain set for lock object");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + domain + 
                                       "'does not exist");
        } else if (content <= 0) {
            throw new ContentException("no content id set for " +
                                       "lock object");
        } else if (user.equals("")) {
            throw new ContentException("no user set for lock object");
        } 
    }

    /**
     * Saves this lock to the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public void save(DatabaseConnection con) throws ContentException {
        LockPeer.doInsert(this, con);
    }
}
