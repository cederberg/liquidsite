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

import net.percederberg.liquidsite.Log;
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
public abstract class Content extends PersistentObject implements Comparable {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(Content.class);

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
     * The template content category.
     */
    public static final int TEMPLATE_CATEGORY = 5;

    /**
     * The section content category.
     */
    public static final int SECTION_CATEGORY = 11;

    /**
     * The document content category.
     */
    public static final int DOCUMENT_CATEGORY = 12;

    /**
     * The content data object.
     */
    private ContentData data;

    /**
     * The previous content revision number. This number is set only
     * when reading a content data object from the database, and is
     * used to track changes to the revision number.
     */
    private int oldRevision = 0;

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
     * The content attribute data objects removed. 
     */
    private ArrayList attributesRemoved = new ArrayList();

    /**
     * Returns an array of content object revisions with the 
     * specified identifier.
     * 
     * @param manager        the content manager to use
     * @param id             the content identifier
     * 
     * @return an array of the content object revisions found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    static Content[] findById(ContentManager manager, int id)
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ArrayList           list;

        try {
            list = ContentPeer.doSelectById(id, con);
            return createContent(manager, list, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
    }

    /**
     * Returns the content object with the specified identifier and 
     * highest revision.
     * 
     * @param manager        the content manager to use
     * @param id             the content identifier
     * 
     * @return the content object found, or
     *         null if no matching content existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    static Content findByMaxRevision(ContentManager manager, int id) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ContentData         data;

        try {
            data = ContentPeer.doSelectByMaxRevision(id, con);
            if (data != null) {
                return createContent(manager, data, con);
            } else {
                return null;
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
    }

    /**
     * Returns the content object with the specified identifier and 
     * revision.
     * 
     * @param manager        the content manager to use
     * @param id             the content identifier
     * @param revision       the content revision
     * 
     * @return the content object found, or
     *         null if no matching content existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    static Content findByRevision(ContentManager manager,
                                  int id,
                                  int revision) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ContentData         data;

        try {
            data = ContentPeer.doSelectByRevision(id, revision, con);
            if (data != null) {
                return createContent(manager, data, con);
            } else {
                return null;
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
    }

    /**
     * Returns an array of root content objects in the specified 
     * domain. Only the latest revision of each content object will 
     * be returned.
     * 
     * @param manager        the content manager to use
     * @param domain         the domain
     * 
     * @return an array of content objects found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    static Content[] findByParent(ContentManager manager, Domain domain) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ArrayList           list;

        try {
            list = ContentPeer.doSelectByParent(domain.getName(), 0, con);
            return createContent(manager, list, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
    }

    /**
     * Returns an array of content objects having the specified 
     * parent. Only the latest revision of each content object will 
     * be returned.
     * 
     * @param manager        the content manager to use
     * @param parent         the parent content
     * 
     * @return an array of content objects found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    static Content[] findByParent(ContentManager manager, Content parent) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ArrayList           list;

        try {
            list = ContentPeer.doSelectByParent(parent.getDomainName(),
                                                parent.getId(), 
                                                con);
            return createContent(manager, list, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
    }

    /**
     * Creates an array of content objects. The content subclass
     * matching the category value will be used.
     * 
     * @param manager        the content manager to use 
     * @param list           the list of content object data
     * @param con            the database connection to use
     * 
     * @return the array of new content objects
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly, or if the content category was unknown
     */
    private static Content[] createContent(ContentManager manager,
                                           ArrayList list,
                                           DatabaseConnection con)
        throws ContentException {

        Content[]    res;
        ContentData  data;

        res = new Content[list.size()];
        for (int i = 0; i < list.size(); i++) {
            data = (ContentData) list.get(i);
            res[i] = createContent(manager, data, con);
        }
        return res;
    }

    /**
     * Creates a content object. The content subclass matching the
     * category value will be used.
     * 
     * @param manager        the content manager to use 
     * @param data           the content object data
     * @param con            the database connection to use
     * 
     * @return the new content object
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly, or if the content category was unknown
     */
    private static Content createContent(ContentManager manager,
                                         ContentData data,
                                         DatabaseConnection con)
        throws ContentException {

        switch (data.getInt(ContentData.CATEGORY)) {
        case SITE_CATEGORY:
            return new ContentSite(manager, data, con);
        case FOLDER_CATEGORY:
            return new ContentFolder(manager, data,  con);
        case PAGE_CATEGORY:
            return new ContentPage(manager, data, con);
        case FILE_CATEGORY:
            return new ContentFile(manager, data, con);
        case TEMPLATE_CATEGORY:
            return new ContentTemplate(manager, data, con);
        case SECTION_CATEGORY:
            return new ContentSection(manager, data, con);
        case DOCUMENT_CATEGORY:
            return new ContentDocument(manager, data, con);
        default:
            throw new ContentException(
                "content category " + data.getInt(ContentData.CATEGORY) +
                " hasn't been defined"); 
        }
    }

    /**
     * Creates a new content object with default values. The content
     * identifier will be set to the next available one after storing
     * to the database, and the content revision is set to zero (0).
     * 
     * @param manager        the content manager to use 
     * @param domain         the domain
     * @param category       the category
     */
    protected Content(ContentManager manager, Domain domain, int category) {
        super(manager, false);
        this.data = new ContentData();
        this.data.setString(ContentData.DOMAIN, domain.getName());
        this.data.setInt(ContentData.CATEGORY, category);
        this.data.setDate(ContentData.MODIFIED, new Date());
    }

    /**
     * Creates a new content object. This constructor will also read
     * all content attributes from the database.
     * 
     * @param manager        the content manager to use 
     * @param data           the content data object
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected Content(ContentManager manager,
                      ContentData data, 
                      DatabaseConnection con) 
        throws ContentException {

        super(manager, true);
        this.data = data;
        this.oldRevision = data.getInt(ContentData.REVISION);
        try {
            doReadAttributes(con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }
    
    /**
     * Checks if this content object equals another object. This 
     * method will only return true if the other object is a content
     * object with the same id.
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
     * object with the same id.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is identical, or
     *         false otherwise 
     */
    public boolean equals(Content obj) {
        return obj != null && getId() == obj.getId();
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
     * @throws ClassCastException if the object isn't a Content
     *             object
     */
    public int compareTo(Object obj) throws ClassCastException {
        return compareTo((Content) obj);
    }

    /**
     * Compares this object with the specified content object for 
     * order. Returns a negative integer, zero, or a positive integer 
     * as this object is less than, equal to, or greater than the 
     * specified object. The ordering is based primarily on category
     * and secondarily on name.
     * 
     * @param content        the content object to compare to
     * 
     * @return a negative integer, zero, or a positive integer as 
     *         this object is less than, equal to, or greater than 
     *         the specified object
     */
    public int compareTo(Content content) {
        int category = getCategory() - content.getCategory();
        
        if (category != 0) {
            return category;
        } else {
            return getName().compareTo(content.getName());
        }
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
     * Checks if this content object revision is online. Note that 
     * this method does NOT take other revisions into account. A 
     * later revision may set different online and offline dates,
     * causing the content object to actually be offline.   
     * 
     * @return true if the content object revision is online, or
     *         false otherwise
     */
    public boolean isOnline() {
        Date  online = getOnlineDate();
        Date  offline = getOfflineDate();
        Date  now = new Date();
        
        return online != null 
            && online.before(now)
            && (offline == null || offline.after(now));
    }

    /**
     * Returns the content domain.
     * 
     * @return the content domain
     * 
     * @throws ContentException if no content manager is available
     */
    public Domain getDomain() throws ContentException {
        // TODO: this is not security-wise correct
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
     * Returns the content revision number.
     * 
     * @return the content revision number
     */
    public int getRevisionNumber() {
        return data.getInt(ContentData.REVISION);
    }

    /**
     * Sets the content revision number. Note that the revision zero 
     * (0) is treated specially in several ways. First, when moving 
     * from revision zero, the old zero revision will be deleted from
     * the database (corresponding to a revision promotion). Also,
     * when storing a non-zero revision, permissions to publish are
     * required by save(). 
     * 
     * @param revision       the new content revision number
     */
    public void setRevisionNumber(int revision) {
        AttributeData  attr;
        Iterator       iter;

        data.setInt(ContentData.REVISION, revision);
        iter = attributes.values().iterator();
        while (iter.hasNext()) {
            attr = (AttributeData) iter.next();
            attr.setInt(AttributeData.REVISION, revision);
        }
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
        
        // TODO: this is not security-wise correct
        if (parent <= 0) {
            return null;
        } else {
            return getContentManager().getContent(parent);
        }
    }

    /**
     * Sets the content parent.
     * 
     * @param parent         the new parent, or null for none
     */
    public void setParent(Content parent) {
        if (parent == null) {
            setParentId(0);
        } else {
            setParentId(parent.getId());
        }
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
     * Returns the content last modification author. The author name
     * is set automatically by the save method.
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
     * Returns the content last modification author. The author name
     * is set automatically by the save method.
     * 
     * @return the content last modification author
     */
    public String getAuthorName() {
        return data.getString(ContentData.AUTHOR);
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
     * Returns an iterator for all the attribute names.
     * 
     * @return an iterator for all the attribute names
     */
    protected Iterator getAttributeNames() {
        return attributes.keySet().iterator();
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
        if (value == null) {
            if (attr != null) {
                attributesRemoved.add(attr);
                attributes.remove(name);
            }
        } else {
            if (attr == null) {
                attr = new AttributeData();
                attr.setString(AttributeData.DOMAIN, getDomainName());
                attr.setInt(AttributeData.CONTENT, getId());
                attr.setInt(AttributeData.REVISION, getRevisionNumber());
                attr.setInt(AttributeData.CATEGORY, getCategory());
                attr.setString(AttributeData.NAME, name);
                attributes.put(name, attr);
                attributesAdded.add(name);
            }
            attr.setString(AttributeData.DATA, value);
            attr.setBoolean(AttributeData.SEARCHABLE, searchable);
        }
    }

    /**
     * Returns the specified content object revision.
     * 
     * @param revision       the content revision
     * 
     * @return the content object revision found, or
     *         null if no matching content existed
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Content getRevision(int revision) throws ContentException {
        return findByRevision(getContentManager(), getId(), revision);
    }

    /**
     * Returns an array of all content object revisions.
     * 
     * @return an array of the content object revisions found
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Content[] getAllRevisions() throws ContentException {
        return findById(getContentManager(), getId());
    }

    /**
     * Returns the lock applicable to this content object. If no lock
     * has been set on this object, null will be returned. 
     * 
     * @return the content lock object found, or
     *         null if this object is not locked
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public Lock getLock() throws ContentException {
        return Lock.findByContent(getContentManager(), this);
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
        return Permission.findByContent(getContentManager(), this);
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
        }
    }

    /**
     * Deletes this content revision from the database.
     * 
     * @param user           the user performing the operation
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     * @throws ContentSecurityException if the user specified didn't
     *             have write permissions
     */
    public void deleteRevision(User user) 
        throws ContentException, ContentSecurityException {

        DatabaseConnection  con = getDatabaseConnection(getContentManager());

        // Delete from database
        try {
            SecurityManager.getInstance().checkDelete(user, this);
            ContentPeer.doDeleteRevision(getId(), getRevisionNumber(), con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(getContentManager(), con);
        }

        // Remove from cache
        CacheManager.getInstance().remove(this);
    }

    /**
     * Inserts the object data into the database.
     * 
     * @param user           the user performing the operation
     * @param con            the database connection to use
     * 
     * @throws ContentException if the object data didn't validate or 
     *             if the database couldn't be accessed properly
     */
    protected void doInsert(User user, DatabaseConnection con)
        throws ContentException {

        validate();
        data.setString(ContentData.AUTHOR, user.getName());
        data.setDate(ContentData.MODIFIED, new Date());
        try {
            ContentPeer.doInsert(data, con);
            doWriteAttributes(con, true);
            oldRevision = getRevisionNumber();
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Updates the object data in the database.
     * 
     * @param user           the user performing the operation
     * @param con            the database connection to use
     * 
     * @throws ContentException if the object data didn't validate or 
     *             if the database couldn't be accessed properly
     */
    protected void doUpdate(User user, DatabaseConnection con)
        throws ContentException {

        validate();
        data.setString(ContentData.AUTHOR, user.getName());
        data.setDate(ContentData.MODIFIED, new Date());
        try {
            if (oldRevision != getRevisionNumber()) {
                ContentPeer.doInsert(data, con);
                doWriteAttributes(con, true);
                if (oldRevision == 0) {
                    ContentPeer.doDeleteRevision(getId(), 0, con);
                }
                oldRevision = getRevisionNumber();
            } else {
                ContentPeer.doUpdate(data, con);
                doWriteAttributes(con, false);
            }
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }

    /**
     * Deletes the object data from the database. This method will 
     * also delete any child content object recursively.
     * 
     * @param user           the user performing the operation
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected void doDelete(User user, DatabaseConnection con)
        throws ContentException {

        Content[]  children = findByParent(getContentManager(), this);

        try {
            for (int i = 0; i < children.length; i++) {
                children[i].doDelete(user, con);
            }
            ContentPeer.doDelete(data, con);
        } catch (DatabaseObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
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
                                                getRevisionNumber(), 
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
     * @param insert         the force insert flag
     * 
     * @throws DatabaseObjectException if the database couldn't be 
     *             accessed properly
     */
    private void doWriteAttributes(DatabaseConnection con, boolean insert)
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
            if (insert || attributesAdded.contains(name)) {
                AttributePeer.doInsert(attr, con);
            } else {
                AttributePeer.doUpdate(attr, con);
            }
        }
        if (!insert) {
            for (int i = 0; i < attributesRemoved.size(); i++) {
                attr = (AttributeData) attributesRemoved.get(i);
                AttributePeer.doDelete(attr, con);
            }
        }
        attributesAdded.clear();
        attributesRemoved.clear();
    }
}
