/*
 * Permission.java
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
 * A content object permission.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Permission extends DataObject {

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
     * The group name.
     */
    private String group = "";

    /**
     * The read permission flag.
     */
    private boolean read = false;
    
    /**
     * The write permission flag.
     */
    private boolean write = false;
    
    /**
     * The publish permission flag.
     */
    private boolean publish = false;
    
    /**
     * The admin permission flag.
     */
    private boolean admin = false;
    
    /**
     * Creates a new permission with default values.
     */
    public Permission() {
    }

    /**
     * Checks if this permission equals another object. This method 
     * will only return true if the other object is a permission with 
     * the same content identifier.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical permission, or
     *         false otherwise 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Permission) {
            return equals((Permission) obj);
        } else {
            return false;
        }
    }

    /**
     * Checks if this permission equals another object. This method 
     * will only return true if the other object is a permission with 
     * the same content identifier.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical permission, or
     *         false otherwise 
     */
    public boolean equals(Permission obj) {
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
        buffer.append(", ");
        if (!user.equals("")) {
            buffer.append("User: ");
            buffer.append(user);
        } else if (!group.equals("")) {
            buffer.append("Group ");
            buffer.append(group);
        } else {
            buffer.append("Any User");
        }
        buffer.append(", Permissions: ");
        buffer.append(toString(read, "r"));
        buffer.append(toString(write, "w"));
        buffer.append(toString(publish, "p"));
        buffer.append(toString(admin, "a"));

        return buffer.toString();
    }
    
    /**
     * Returns a string representation of a permission flag.
     * 
     * @param flag           the permission flag
     * @param symbol         the flag symbol
     * 
     * @return the string representation fo the permission flag
     */
    private String toString(boolean flag, String symbol) {
        if (flag) {
            return symbol;
        } else {
            return "-";
        }
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
     * Returns the permission user name.
     * 
     * @return the permission user name
     */
    public String getUserName() {
        return user;
    }

    /**
     * Sets the permission user name.
     * 
     * @param user           the new permission user name
     */
    public void setUserName(String user) {
        this.user = user;
        setModified(true);
    }

    /**
     * Returns the permission group name.
     * 
     * @return the permission group name
     */
    public String getGroupName() {
        return group;
    }

    /**
     * Sets the permission group name.
     * 
     * @param group          the new permission group name
     */
    public void setGroupName(String group) {
        this.group = group;
        setModified(true);
    }

    /**
     * Returns the read permission flag.
     * 
     * @return the read permission flag
     */
    public boolean getRead() {
        return read;
    }

    /**
     * Sets the read permission flag.
     * 
     * @param read           the new read permission flag
     */
    public void setRead(boolean read) {
        this.read = read;
        setModified(true);
    }

    /**
     * Returns the write permission flag.
     * 
     * @return the write permission flag
     */
    public boolean getWrite() {
        return write;
    }

    /**
     * Sets the write permission flag.
     * 
     * @param write          the new write permission flag
     */
    public void setWrite(boolean write) {
        this.write = write;
        setModified(true);
    }

    /**
     * Returns the publish permission flag.
     * 
     * @return the publish permission flag
     */
    public boolean getPublish() {
        return publish;
    }

    /**
     * Sets the publish permission flag.
     * 
     * @param publish        the new publish permission flag
     */
    public void setPublish(boolean publish) {
        this.publish = publish;
        setModified(true);
    }

    /**
     * Returns the admin permission flag.
     * 
     * @return the admin permission flag
     */
    public boolean getAdmin() {
        return admin;
    }

    /**
     * Sets the admin permission flag.
     * 
     * @param admin          the new admin permission flag
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
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
            throw new ContentException("no domain set for permission object");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + domain + 
                                       "'does not exist");
        } else if (content <= 0) {
            throw new ContentException("no content id set for " +
                                       "permission object");
        }
    }

    /**
     * Saves this permission to the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public void save(DatabaseConnection con) throws ContentException {
        if (!isPersistent()) {
            PermissionPeer.doInsert(this, con);
        } else if (isModified()) {
            PermissionPeer.doUpdate(this, con);
        }
    }
}
