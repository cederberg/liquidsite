/*
 * Content.java
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
 * A web content object.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Content extends DataObject {

    /**
     * The site content category.
     */
    public static final int SITE_CATEGORY = 1;

    /**
     * The folder content category.
     */
    public static final int FOLDER_CATEGORY = 2;
    
    /**
     * The page content category.
     */
    public static final int PAGE_CATEGORY = 3;

    /**
     * The file content category.
     */
    public static final int FILE_CATEGORY = 4;

    /**
     * The domain the content belongs to.
     */
    private String domain = "";

    /**
     * The unique content identifier.
     */
    private int id = 0;
    
    /**
     * The content revision.
     */
    private int revision = 0;
    
    /**
     * The content category.
     */
    private int category = 0;
    
    /**
     * The content name.
     */
    private String name = "";
    
    /**
     * The parent content identifier.
     */
    private int parent = 0;
    
    /**
     * The publishing on-line date and time.
     */
    private Date online = new Date(0);
    
    /**
     * The publishing off-line date and time.
     */
    private Date offline = new Date(0);
    
    /**
     * The latest modification date.
     */
    private Date modified = null;
    
    /**
     * The latest modification author.
     */
    private String author = "";

    /**
     * Creates a new content with default values.
     */
    public Content() {
    }

    /**
     * Checks if this content equals another object. This method will 
     * only return true if the other object is a content with the 
     * same id and revision.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical content, or
     *         false otherwise 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Content) {
            return equals((Content) obj);
        } else {
            return false;
        }
    }

    /**
     * Checks if this content equals another object. This method will 
     * only return true if the other object is a content with the 
     * same id and revision.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical content, or
     *         false otherwise 
     */
    public boolean equals(Content obj) {
        return id == obj.id && revision == obj.revision;
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
    public int getId() {
        return id;
    }

    /**
     * Sets the content identifier.
     * 
     * @param id             the new identifier
     */
    public void setId(int id) {
        this.id = id;
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
     * @param category       the new category
     */
    public void setCategory(int category) {
        this.category = category;
        setModified(true);
    }

    /**
     * Returns the content name.
     * 
     * @return the content name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the content name.
     * 
     * @param name           the new content name
     */
    public void setName(String name) {
        this.name = name;
        setModified(true);
    }
    
    /**
     * Returns the parent content identifier.
     * 
     * @return the parent content identifier
     */
    public int getParentId() {
        return parent;
    }

    /**
     * Sets the parent content identifier.
     * 
     * @param parent         the new parent identifier
     */
    public void setParentId(int parent) {
        this.parent = parent;
        setModified(true);
    }

    /**
     * Returns the publishing on-line date.
     * 
     * @return the publishing on-line date
     */
    public Date getOnline() {
        return online;
    }

    /**
     * Sets the publishing on-line date.
     * 
     * @param online         the new on-line date
     */
    public void setOnline(Date online) {
        this.online = online;
        setModified(true);
    }

    /**
     * Returns the publishing off-line date.
     * 
     * @return the publishing off-line date
     */
    public Date getOffline() {
        return offline;
    }

    /**
     * Sets the publishing off-line date.
     * 
     * @param offline        the new off-line date
     */
    public void setOffline(Date offline) {
        this.offline = offline;
        setModified(true);
    }

    /**
     * Returns the latest modification date.
     * 
     * @return the latest modification date
     */
    public Date getModifiedDate() {
        return modified;
    }

    /**
     * Sets the latest modification date.
     * 
     * @param modified       the new modification date
     */
    public void setModifiedDate(Date modified) {
        this.modified = modified;
        setModified(true);
    }

    /**
     * Sets the latest modification author.
     * 
     * @param author         the new modification author
     */
    public void setAuthor(User author) {
        setAuthorName(author.getName());
    }

    /**
     * Returns the latest modification author name.
     * 
     * @return the latest modification author name
     */
    public String getAuthorName() {
        return author;
    }

    /**
     * Sets the latest modification author name.
     * 
     * @param author         the new modification author name
     */
    public void setAuthorName(String author) {
        this.author = author;
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
            throw new ContentException("no domain set for content object");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + domain + 
                                       "'does not exist");
        } else if (category <= 0) {
            throw new ContentException("no category set for content object");
        } else if (name.equals("")) {
            throw new ContentException("no name set for content object");
        } else if (author.equals("")) {
            throw new ContentException("no author set for content object");
        }
    }

    /**
     * Saves this content to the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public void save(DatabaseConnection con) throws ContentException {
        if (!isPersistent()) {
            ContentPeer.doInsert(this, con);
        } else if (isModified()) {
            ContentPeer.doUpdate(this, con);
        }
    }
}
