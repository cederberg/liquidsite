/*
 * Attribute.java
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
 * A content attribute object.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Attribute extends DataObject {

    /**
     * The domain the content object belongs to.
     */
    private String domain = "";

    /**
     * The content identifier.
     */
    private int content = 0;
    
    /**
     * The content revision.
     */
    private int revision = 0;
    
    /**
     * The content category.
     */
    private int category = 0;
    
    /**
     * The attribute name.
     */
    private String name = "";

    /**
     * The attribute data.
     */
    private String data = "";
    
    /**
     * The searchable attribute data flag.
     */
    private boolean searchable = false;
    
    /**
     * Creates a new attribute with default values.
     */
    public Attribute() {
    }

    /**
     * Checks if this attribute equals another object. This method will 
     * only return true if the other object is a attribute with the 
     * same content id, content revision, and attribute name.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical attribute, or
     *         false otherwise 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Attribute) {
            return equals((Attribute) obj);
        } else {
            return false;
        }
    }

    /**
     * Checks if this attribute equals another object. This method will 
     * only return true if the other object is a attribute with the 
     * same content id, content revision, and attribute name.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical attribute, or
     *         false otherwise 
     */
    public boolean equals(Attribute obj) {
        return content == obj.content 
            && revision == obj.revision
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
     * Returns the content revision.
     * 
     * @return the content revision
     */
    public int getRevision() {
        return revision;
    }

    /**
     * Sets the content revision.
     * 
     * @param revision       the new revision
     */
    public void setRevision(int revision) {
        this.revision = revision;
        setModified(true);
    }

    /**
     * Returns the content category.
     * 
     * @return the content category
     */
    public int getCategory() {
        return category;
    }

    /**
     * Sets the content category.
     * 
     * @param category       the new content category
     */
    public void setCategory(int category) {
        this.category = category;
        setModified(true);
    }

    /**
     * Returns the attribute name.
     * 
     * @return the attribute name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the attribute name.
     * 
     * @param name           the new attribute name
     */
    public void setName(String name) {
        this.name = name;
        setModified(true);
    }
    
    /**
     * Returns the attribute data.
     * 
     * @return the attribute data
     */
    public String getData() {
        return data;
    }
    
    /**
     * Sets the attribute data.
     * 
     * @param data           the new attribute data
     */
    public void setData(String data) {
        this.data = data;
        setModified(true);
    }
    
    /**
     * Returns the attribute data searchable flag.
     * 
     * @return the attribute data searchable flag
     */
    public boolean getSearchable() {
        return searchable;
    }

    /**
     * Sets the attribute data searchable flag.
     * 
     * @param searchable     the new searchable flag
     */
    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
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
            throw new ContentException("no domain set for attribute object");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + domain + 
                                       "'does not exist");
        } else if (content <= 0) {
            throw new ContentException("no content id set for " +
                                       "attribute object");
        } else if (category <= 0) {
            throw new ContentException("no category set for attribute object");
        } else if (name.equals("")) {
            throw new ContentException("no name set for attribute object");
        }
    }

    /**
     * Saves this attribute to the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public void save(DatabaseConnection con) throws ContentException {
        if (!isPersistent()) {
            AttributePeer.doInsert(this, con);
        } else if (isModified()) {
            AttributePeer.doUpdate(this, con);
        }
    }
}
