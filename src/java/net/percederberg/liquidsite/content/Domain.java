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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.content;

/**
 * A resource and user domain.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Domain extends DataObject {

    /**
     * The domain name.
     */
    private String name = "";
    
    /**
     * The domain description.
     */
    private String description = "";
    
    /**
     * The domain comment.
     */
    private String comment = "";
    
    /**
     * Creates a new domain with default values.
     */
    public Domain() {
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
            return name.equals(((Domain) obj).name);
        } else {
            return false;
        }
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();
        
        buffer.append("Domain: ");
        if (description.equals("")) {
            buffer.append(name);
        } else {
            buffer.append(description);
        }
        return buffer.toString();
    }

    /**
     * Returns the unique domain name
     * 
     * @return the unique domain name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the domain name.
     * 
     * @param name           the new domain name
     */
    public void setName(String name) {
        this.name = name;
        setModified(true);
        setPersistent(false);
    }
    
    /**
     * Returns the domain description.
     * 
     * @return the domain description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the domain description.
     * 
     * @param description    the new description
     */
    public void setDescription(String description) {
        this.description = description;
        setModified(true);
    }
    
    /**
     * Returns the optional comment.
     * 
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment.
     * 
     * @param comment        the new comment
     */
    public void setComment(String comment) {
        this.comment = comment;
        setModified(true);
    }

    /**
     * Saves this domain to the database.
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public void save() throws ContentException {
        if (!isPersistent()) {
            DomainPeer.doInsert(this);
        } else if (isModified()) {
            DomainPeer.doUpdate(this);
        }
    }
}
