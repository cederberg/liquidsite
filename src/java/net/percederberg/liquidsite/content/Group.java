/*
 * Group.java
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

import net.percederberg.liquidsite.db.DatabaseConnection;

/**
 * A domain group.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Group extends DataObject {

    /**
     * The domain the group belongs to.
     */
    private String domain = "";

    /**
     * The group name.
     */
    private String name = "";
    
    /**
     * The group description.
     */
    private String description = "";
    
    /**
     * The group comment.
     */
    private String comment = "";

    /**
     * Creates a new group with default values.
     */
    public Group() {
    }

    /**
     * Checks if this group equals another object. This method will 
     * only return true if the other object is a group with the same
     * domain and group name.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical group, or
     *         false otherwise 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Group) {
            return equals((Group) obj);
        } else {
            return false;
        }
    }

    /**
     * Checks if this group equals another group. This method will 
     * only return true if the other object is a group with the same
     * domain and group name.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical group, or
     *         false otherwise 
     */
    public boolean equals(Group obj) {
        return domain.equals(obj.domain)
            && name.equals(obj.name);
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        return name;
    }

    /**
     * Returns the group domain.
     * 
     * @return the group domain
     * 
     * @throws ContentException if no content manager is available
     */
    public Domain getDomain() throws ContentException {
        return ContentManager.getInstance().getDomain(domain);
    }
    
    /**
     * Sets the group domain.
     * 
     * @param domain         the new domain
     */
    public void setDomain(Domain domain) {
        setDomainName(domain.getName());
    }
    
    /**
     * Returns the group domain name
     * 
     * @return the group domain name
     */
    public String getDomainName() {
        return domain;
    }
    
    /**
     * Sets the group domain.
     * 
     * @param domain         the new domain name
     */
    public void setDomainName(String domain) {
        this.domain = domain;
        setModified(true);
    }
    
    /**
     * Returns the group name.
     * 
     * @return the group name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the group name.
     * 
     * @param name           the new group name
     */
    public void setName(String name) {
        this.name = name;
        setModified(true);
    }
    
    /**
     * Returns the group description.
     * 
     * @return the group description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the the group description.
     * 
     * @param description    the new group description
     */
    public void setDescription(String description) {
        this.description = description;
        setModified(true);
    }
    
    /**
     * Returns the group comment.
     * 
     * @return the group comment
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * Sets the group comment.
     * 
     * @param comment        the new group comment
     */
    public void setComment(String comment) {
        this.comment = comment;
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
            throw new ContentException("no domain set for group object");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + domain + 
                                       "'does not exist");
        } else if (name.equals("")) {
            throw new ContentException("no name set for group object");
        }
    }

    /**
     * Saves this group to the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public void save(DatabaseConnection con) throws ContentException {
        if (!isPersistent()) {
            GroupPeer.doInsert(this, con);
        } else if (isModified()) {
            GroupPeer.doUpdate(this, con);
        }
    }
}
