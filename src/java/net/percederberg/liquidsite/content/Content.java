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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.AttributeData;
import net.percederberg.liquidsite.dbo.AttributePeer;
import net.percederberg.liquidsite.dbo.ContentData;
import net.percederberg.liquidsite.dbo.ContentPeer;
import net.percederberg.liquidsite.dbo.DatabaseObjectException;

/**
 * The base class for all content objects.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public abstract class Content extends PersistentObject {

    /**
     * The site content category.
     */
    public static final int SITE_CATEGORY = 1;

    /**
     * The content data object.
     */
    private ContentData data;

    /**
     * The content attribute data objects. The data objects are 
     * indexed by the attribute name.
     */
    private HashMap attributes = new HashMap();

    /**
     * The names of content attributes added. 
     */
    private ArrayList attributesAdded = new ArrayList();

    /**
     * Returns the content object with the specified identifier and 
     * highest revision.
     * 
     * @param id             the content identifier
     * 
     * @return the content object found, or
     *         null if no matching content existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected static Content findById(int id) throws ContentException {
        DatabaseConnection  con = getDatabaseConnection();
        ContentData         data;

        try {
            data = ContentPeer.doSelectByMaxRevision(id, con);
            if (data != null) {
                return createContent(data, con);
            } else {
                return null;
            }
        } catch (DatabaseObjectException e) {
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(con);
        }
    }

    /**
     * Returns the content object with the specified identifier and 
     * revision.
     * 
     * @param id             the content identifier
     * @param revision       the content revision
     * 
     * @return the content object found, or
     *         null if no matching content existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected static Content findByRevision(int id, int revision) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection();
        ContentData         data;

        try {
            data = ContentPeer.doSelectByRevision(id, revision, con);
            if (data != null) {
                return createContent(data, con);
            } else {
                return null;
            }
        } catch (DatabaseObjectException e) {
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(con);
        }
    }

    /**
     * Returns an array of content objects having the specified 
     * parent. Only the latest revision of each content object will 
     * be returned.
     * 
     * @param parent         the parent content
     * 
     * @return an array of content objects found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected static Content[] findByParent(Content parent) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection();
        ArrayList           list;
        Content[]           res;

        try {
            list = ContentPeer.doSelectByParent(parent.getId(), con);
            res = new Content[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = createContent((ContentData) list.get(i), con);
            }
        } catch (DatabaseObjectException e) {
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(con);
        }
        return res;
    }

    /**
     * Creates a content subclass depending on the content category.
     * 
     * @param data           the content object data
     * @param con            the database connection to use
     * 
     * @return the new content object, or
     *         null if the content category is unknown
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    private static Content createContent(ContentData data, 
                                         DatabaseConnection con)
        throws DatabaseObjectException {

        switch (data.getInt(ContentData.CATEGORY)) {
        case SITE_CATEGORY:
            return new Site(data, con);
        default:
            return null;
        }
    }

    /**
     * Creates a new content object with default values.
     * 
     * @param domain         the domain
     * @param revision       the revision
     * @param category       the category
     */
    protected Content(Domain domain, int revision, int category) {
        super(false);
        this.data = new ContentData();
        this.data.setString(ContentData.DOMAIN, domain.getName());
        this.data.setInt(ContentData.REVISION, revision);
        this.data.setInt(ContentData.CATEGORY, category);
        this.data.setDate(ContentData.MODIFIED, new Date());
    }

    /**
     * Creates a new revision of a content object with default values.
     * 
     * @param domain         the domain
     * @param id             the content identifier
     * @param revision       the revision
     * @param category       the category
     */
    protected Content(Domain domain, int id, int revision, int category) {
        super(false);
        this.data = new ContentData();
        this.data.setString(ContentData.DOMAIN, domain.getName());
        this.data.setInt(ContentData.ID, id);
        this.data.setInt(ContentData.REVISION, revision);
        this.data.setInt(ContentData.CATEGORY, category);
        this.data.setDate(ContentData.MODIFIED, new Date());
    }

    /**
     * Creates a new content object. This constructor will also read
     * all content attributes from the database.
     * 
     * @param data           the content data object
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    protected Content(ContentData data, DatabaseConnection con) 
        throws DatabaseObjectException {

        super(true);
        this.data = data;
        doReadAttributes(con);
    }
    
    /**
     * Checks if this content object equals another object. This 
     * method will only return true if the other object is a content
     * object with the same id and revision.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is identical, or
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
     * Checks if this content object equals another object. This 
     * method will only return true if the other object is a content
     * object with the same id and revision.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is identical, or
     *         false otherwise 
     */
    public boolean equals(Content obj) {
        return getId() == obj.getId()
            && getRevision() == obj.getRevision();
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        return getName();
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
     * Returns the content domain name.
     * 
     * @return the content domain name
     */
    public String getDomainName() {
        return data.getString(ContentData.DOMAIN);
    }

    /**
     * Returns the content identifier.
     * 
     * @return the content identifier
     */
    public int getId() {
        return data.getInt(ContentData.ID);
    }

    /**
     * Returns the content revision.
     * 
     * @return the content revision
     */
    public int getRevision() {
        return data.getInt(ContentData.REVISION);
    }


    /**
     * Returns the content category.
     * 
     * @return the content category
     */
    public int getCategory() {
        return data.getInt(ContentData.CATEGORY);
    }

    /**
     * Returns the content name.
     * 
     * @return the content name
     */
    public String getName() {
        return data.getString(ContentData.NAME);
    }

    /**
     * Sets the content name.
     * 
     * @param name           the new name
     */
    public void setName(String name) {
        data.setString(ContentData.NAME, name);
    }

    /**
     * Returns the content parent.
     * 
     * @return the content parent
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Content getParent() throws ContentException {
        int  parent = getParentId();
        
        if (parent <= 0) {
            return null;
        } else {
            return getContentManager().getContent(parent);
        }
    }

    /**
     * Sets the content parent.
     * 
     * @param parent         the new parent
     */
    public void setParent(Content parent) {
        setParentId(parent.getId());
    }

    /**
     * Returns the content parent identifier.
     * 
     * @return the content parent identifier
     */
    public int getParentId() {
        return data.getInt(ContentData.PARENT);
    }
    
    /**
     * Sets the content parent identifier.
     * 
     * @param parent         the new parent identifier
     */
    public void setParentId(int parent) {
        data.setInt(ContentData.PARENT, parent);
    }
    
    /**
     * Returns the content publishing online date.
     * 
     * @return the content publishing online date
     */
    public Date getOnlineDate() {
        Date  date = data.getDate(ContentData.ONLINE);
        
        return (date.getTime() == 0) ? null : date;
    }

    /**
     * Sets the content publishing online date.
     * 
     * @param online         the new publishing online date
     */
    public void setOnlineDate(Date online) {
        if (online == null) {
            online = new Date(0);
        }
        data.setDate(ContentData.ONLINE, online);
    }

    /**
     * Returns the content publishing offline date.
     * 
     * @return the content publishing offline date
     */
    public Date getOfflineDate() {
        Date  date = data.getDate(ContentData.OFFLINE);
        
        return (date.getTime() == 0) ? null : date;
    }

    /**
     * Sets the content publishing offline date.
     * 
     * @param offline        the new publishing offline date
     */
    public void setOfflineDate(Date offline) {
        if (offline == null) {
            offline = new Date(0);
        }
        data.setDate(ContentData.OFFLINE, offline);
    }

    /**
     * Returns the content last modification date.
     * 
     * @return the content last modification date
     */
    public Date getModifiedDate() {
        return data.getDate(ContentData.MODIFIED); 
    }

    /**
     * Returns the content last modification author.
     * 
     * @return the content last modification author
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public User getAuthor() throws ContentException {
        return getContentManager().getUser(getDomain(), getAuthorName());
    }

    /**
     * Sets the content last modification author.
     * 
     * @param author         the author user
     */
    public void setAuthor(User author) {
        setAuthorName(author.getName());
    }

    /**
     * Returns the content last modification author.
     * 
     * @return the content last modification author
     */
    public String getAuthorName() {
        return data.getString(ContentData.AUTHOR);
    }
    
    /**
     * Sets the content last modification author.
     * 
     * @param author         the author user name
     */
    public void setAuthorName(String author) {
        data.setString(ContentData.AUTHOR, author);
    }

    /**
     * Returns the content revision comment.
     * 
     * @return the content revision comment
     */
    public String getComment() {
        return data.getString(ContentData.COMMENT);
    }
    
    /**
     * Sets the content revision comment.
     * 
     * @param comment        the content revision comment
     */
    public void setComment(String comment) {
        data.setString(ContentData.COMMENT, comment);
    }
    
    /**
     * Returns a content attribute value.
     * 
     * @param name           the content attribute name
     * 
     * @return the content attribute value, or
     *         null if not found
     */
    protected String getAttribute(String name) {
        AttributeData  attr;
        
        attr = (AttributeData) attributes.get(name);
        if (attr == null) {
            return null; 
        } else {
            return attr.getString(AttributeData.DATA);
        }
    }
    
    /**
     * Sets a content attribute value. If the attribute does not 
     * exist it will be created. This method will set the attribute
     * searchable flag to false.
     * 
     * @param name           the content attribute name
     * @param value          the content attribute value
     */
    protected void setAttribute(String name, String value) {
        setAttribute(name, value, false);
    }

    /**
     * Sets a content attribute value. If the attribute does not 
     * exist it will be created.
     * 
     * @param name           the content attribute name
     * @param value          the content attribute value
     * @param searchable     the content attribute searchable flag
     */
    protected void setAttribute(String name, 
                                String value, 
                                boolean searchable) {

        AttributeData  attr;
        
        attr = (AttributeData) attributes.get(name);
        if (attr == null) {
            attr = new AttributeData();
            attr.setString(AttributeData.DOMAIN, getDomainName());
            attr.setInt(AttributeData.CONTENT, getId());
            attr.setInt(AttributeData.REVISION, getRevision());
            attr.setInt(AttributeData.CATEGORY, getCategory());
            attr.setString(AttributeData.NAME, name);
            attributes.put(name, attr);
            attributesAdded.add(name);
        }
        attr.setString(AttributeData.DATA, value);
        attr.setBoolean(AttributeData.SEARCHABLE, searchable);
    }

    /**
     * Returns the permissions applicable to this content object. 
     * This method will not return the inherited permissions. 
     * 
     * @return an array of permissions for this object
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Permission[] getPermissions() throws ContentException {
        return Permission.findByContent(this);
    }

    /**
     * Checks the read access for a user. If no content permissions 
     * are set, this method will return true for all but root content 
     * objects. 
     *
     * @param user           the user to check, or null for none
     * 
     * @return true if the user has read access, or
     *         false otherwise
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasReadAccess(User user) 
        throws ContentException {

        Permission[]  perms = getPermissions();
        Group[]       groups = null;
        
        if (user != null && user.getDomainName().equals("")) {
            return true;
        }
        if (perms.length == 0) {
            return getParentId() > 0;
        }
        if (user != null) {
            groups = user.getGroups();
        }
        for (int i = 0; i < perms.length; i++) {
            if (perms[i].isMatch(user, groups) && perms[i].getRead()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the child content objects. Only the highest revision 
     * of each object will be returned.
     * 
     * @return the child content objects
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public Content[] getChildren() throws ContentException {
        return findByParent(this);
    }

    /**
     * Validates this data object. This method checks that all 
     * required fields have been filled with suitable values.
     * 
     * @throws ContentException if the data object contained errors
     */
    public void validate() throws ContentException {
        if (getDomain().equals("")) {
            throw new ContentException("no domain set for content object");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + getDomainName() + 
                                       "'does not exist");
        } else if (getName().equals("")) {
            throw new ContentException("no name set for content object");
        } else if (getAuthorName().equals("")) {
            throw new ContentException("no author set for content object");
        }
    }

    /**
     * Inserts the object data into the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    protected void doInsert(DatabaseConnection con)
        throws DatabaseObjectException {

        data.setDate(ContentData.MODIFIED, new Date());
        ContentPeer.doInsert(data, con);
        doWriteAttributes(con);
    }

    /**
     * Updates the object data in the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    protected void doUpdate(DatabaseConnection con)
        throws DatabaseObjectException {

        data.setDate(ContentData.MODIFIED, new Date());
        ContentPeer.doInsert(data, con);
        doWriteAttributes(con);
    }

    /**
     * Deletes the object data from the database.
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    protected void doDelete(DatabaseConnection con)
        throws DatabaseObjectException {

        ContentPeer.doDelete(data, con);
    }

    /**
     * Reads the content attributes from the database. This method 
     * will add all found attributes to the attributes map. 
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    private void doReadAttributes(DatabaseConnection con) 
        throws DatabaseObjectException {     

        ArrayList      list;
        AttributeData  attr;

        list = AttributePeer.doSelectByRevision(getId(), 
                                                getCategory(), 
                                                con);
        for (int i = 0; i < list.size(); i++) {
            attr = (AttributeData) list.get(i);
            attributes.put(attr.getString(AttributeData.NAME), attr);
        }
    }

    /**
     * Writes the content attributes to the database. This method 
     * will either insert of update each attribute depending on 
     * whether it is present in the added attributes list or not. 
     * 
     * @param con            the database connection to use
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    private void doWriteAttributes(DatabaseConnection con)
        throws DatabaseObjectException {

        Iterator       iter = attributes.keySet().iterator();
        AttributeData  attr;
        String         name;

        while (iter.hasNext()) {
            name = (String) iter.next();
            attr = (AttributeData) attributes.get(name);
            if (attr.getInt(AttributeData.CONTENT) <= 0) {
                attr.setInt(AttributeData.CONTENT, getId());
            }
            if (attributesAdded.contains(name)) {
                AttributePeer.doInsert(attr, con);
            } else {
                AttributePeer.doUpdate(attr, con);
            }
        }
        attributesAdded.clear();
    }
}
