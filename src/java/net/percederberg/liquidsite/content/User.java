/*
 * User.java
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
 * A domain user.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class User extends DataObject {

    /**
     * The domain the user belongs to.
     */
    private String domain = "";

    /**
     * The user name.
     */
    private String name = "";
    
    /**
     * The encoded user password.
     */
    private String password = "";
    
    /**
     * The real user name.
     */
    private String realName = "";
    
    /**
     * The user e-mail address.
     */
    private String email = "";
    
    /**
     * The user comment.
     */
    private String comment = "";

    /**
     * Creates a new user with default values.
     */
    public User() {
    }

    /**
     * Checks if this user equals another object. This method will 
     * only return true if the other object is a user with the same
     * domain and user name.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical user, or
     *         false otherwise 
     */
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            return equals((User) obj);
        } else {
            return false;
        }
    }

    /**
     * Checks if this user equals another user. This method will 
     * only return true if the other object is a user with the same
     * domain and user name.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical user, or
     *         false otherwise 
     */
    public boolean equals(User obj) {
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
     * Returns the user domain.
     * 
     * @return the user domain
     * 
     * @throws ContentException if no content manager is available
     */
    public Domain getDomain() throws ContentException {
        return ContentManager.getInstance().getDomain(domain);
    }
    
    /**
     * Sets the user domain.
     * 
     * @param domain         the new domain
     */
    public void setDomain(Domain domain) {
        setDomainName(domain.getName());
    }
    
    /**
     * Returns the user domain name
     * 
     * @return the user domain name
     */
    public String getDomainName() {
        return domain;
    }
    
    /**
     * Sets the user domain.
     * 
     * @param domain         the new domain name
     */
    public void setDomainName(String domain) {
        this.domain = domain;
        setModified(true);
    }
    
    /**
     * Returns the user name.
     * 
     * @return the user name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the user name.
     * 
     * @param name           the new user name
     */
    public void setName(String name) {
        this.name = name;
        setModified(true);
    }
    
    /**
     * Returns the encoded user password.
     * 
     * @return the encoded user password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the encoded user password.
     * 
     * @param password       the new encoded user password
     */
    public void setPassword(String password) {
        this.password = password;
        setModified(true);
    }
    
    /**
     * Returns the real user name.
     * 
     * @return the real user name
     */
    public String getRealName() {
        return realName;
    }
    
    /**
     * Sets the real user name.
     * 
     * @param realName       the new real user name
     */
    public void setRealName(String realName) {
        this.realName = realName;
        setModified(true);
    }
    
    /**
     * Returns the user e-mail address.
     * 
     * @return the user e-mail address
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Sets the user e-mail address.
     * 
     * @param email          the new user e-mail address
     */
    public void setEmail(String email) {
        this.email = email;
        setModified(true);
    }
    
    /**
     * Returns the user comment.
     * 
     * @return the user comment
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * Sets the user comment.
     * 
     * @param comment        the new user comment
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
            throw new ContentException("no domain set for user object");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + domain + 
                                       "'does not exist");
        } else if (name.equals("")) {
            throw new ContentException("no name set for user object");
        } else if (password.equals("")) {
            throw new ContentException("no password set for user object");
        }
    }

    /**
     * Saves this user to the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public void save(DatabaseConnection con) throws ContentException {
        if (!isPersistent()) {
            UserPeer.doInsert(this, con);
        } else if (isModified()) {
            UserPeer.doUpdate(this, con);
        }
    }
}
