/*
 * PermissionList.java
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

import java.util.ArrayList;

import org.liquidsite.core.data.DataObjectException;
import org.liquidsite.core.data.DataSource;
import org.liquidsite.core.data.PermissionData;
import org.liquidsite.core.data.PermissionPeer;
import org.liquidsite.util.log.Log;

/**
 * A content object permission list. All the permissions in the list
 * must reference a single object, the permission reference object.
 * This object must be either a domain or a content object.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PermissionList extends PersistentObject {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(PermissionList.class);

    /**
     * The permission reference domain name.
     */
    private String domain = "";

    /**
     * The permission reference content identifier. This value is set
     * to zero (0) if the permission reference object is a domain.
     */
    private int content = 0;

    /**
     * The list of permission objects.
     */
    private ArrayList permissions = new ArrayList();

    /**
     * Returns a permission list for the specified domain object. Note
     * that this method only returns the list of permissions set on
     * the domain object, not all the permissions for content objects
     * in the domain.
     *
     * @param manager        the content manager to use
     * @param domain         the domain object
     *
     * @return the permission list for the domain
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static PermissionList findByDomain(ContentManager manager, Domain domain)
        throws ContentException {

        DataSource  src = getDataSource(manager);
        ArrayList   list;

        try {
            list = PermissionPeer.doSelectByContent(src, domain.getName(), 0);
            return new PermissionList(manager, domain.getName(), 0, list);
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            src.close();
        }
    }

    /**
     * Returns a permission list for the specified content object.
     * Note that this method only returns the list of permissions set
     * on the content object, not any inherited permissions.
     *
     * @param manager        the content manager to use
     * @param content        the content object
     *
     * @return the permission list for the content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    static PermissionList findByContent(ContentManager manager,
                                        Content content)
        throws ContentException {

        DataSource  src = getDataSource(manager);
        ArrayList   list;

        try {
            list = PermissionPeer.doSelectByContent(src,
                                                    content.getDomainName(),
                                                    content.getId());
            return new PermissionList(manager,
                                      content.getDomainName(),
                                      content.getId(),
                                      list);
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            src.close();
        }
    }

    /**
     * Creates a new empty permission list for a domain.
     *
     * @param manager        the content manager to use
     * @param domain         the domain object
     */
    public PermissionList(ContentManager manager, Domain domain) {
        super(manager, false);
        this.domain = domain.getName();
        this.content = 0;
    }

    /**
     * Creates a new empty permission list for a content object.
     *
     * @param manager        the content manager to use
     * @param content        the content object
     */
    public PermissionList(ContentManager manager, Content content) {
        super(manager, false);
        this.domain = content.getDomainName();
        this.content = content.getId();
    }

    /**
     * Creates a new permission list from a list of data objects.
     *
     * @param manager        the content manager to use
     * @param domain         the permission reference domain
     * @param content        the permission reference content id
     * @param data           the list of permission data objects
     */
    private PermissionList(ContentManager manager,
                           String domain,
                           int content,
                           ArrayList data) {

        super(manager, true);
        this.domain = domain;
        this.content = content;
        for (int i = 0; i < data.size(); i++) {
            permissions.add(new Permission((PermissionData) data.get(i)));
        }
    }

    /**
     * Checks if this permission list equals another object. This
     * method will only return true if the other object is a
     * permission list for the same domain or content object.
     *
     * @param obj            the object to compare with
     *
     * @return true if the other object is an identical permission list, or
     *         false otherwise
     */
    public boolean equals(Object obj) {
        if (obj instanceof PermissionList) {
            return equals((PermissionList) obj);
        } else {
            return false;
        }
    }

    /**
     * Checks if this permission list equals another object. This
     * method will only return true if the other object is a
     * permission list for the same domain or content object.
     *
     * @param obj            the object to compare with
     *
     * @return true if the other object is an identical permission list, or
     *         false otherwise
     */
    public boolean equals(PermissionList obj) {
        return getDomainName().equals(obj.getDomainName())
            && getContentId() == obj.getContentId();
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
        buffer.append(", Permission List:\n");
        if (permissions.size() == 0) {
            buffer.append("  <none>\n");
        }
        for (int i = 0; i < permissions.size(); i++) {
            buffer.append("  ");
            buffer.append(permissions.get(i));
            buffer.append("\n");
        }

        return buffer.toString();
    }

    /**
     * Checks if the permission list is empty. An empty permission
     * list means that the parent object permission list should be
     * used instead.
     *
     * @return true if the permission list is empty, or
     *         false otherwise
     */
    public boolean isEmpty() {
        return permissions.size() <= 0;
    }

    /**
     * Returns the permission reference domain.
     *
     * @return the permission reference domain
     *
     * @throws ContentException if no content manager is available
     */
    public Domain getDomain() throws ContentException {
        return getContentManager().getDomain(getDomainName());
    }

    /**
     * Returns the permission reference domain name
     *
     * @return the permission reference domain name
     */
    public String getDomainName() {
        return domain;
    }

    /**
     * Returns the permission reference content object.
     *
     * @return the permission reference content object, or
     *         null if the permission reference is a domain object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public Content getContent() throws ContentException {
        return getContentManager().getContent(getContentId());
    }

    /**
     * Returns the permission reference content identifier.
     *
     * @return the permission reference content identifier, or
     *         zero (0) if the permission reference is a domain object
     */
    public int getContentId() {
        return content;
    }

    /**
     * Returns an array of the permissions in this list.
     *
     * @return an array of the permissions in this list
     */
    public Permission[] getPermissions() {
        Permission[]  res;

        res = new Permission[permissions.size()];
        permissions.toArray(res);
        return res;
    }

    /**
     * Sets the content of the permission list. All previous
     * permissions will be removed by this method.
     *
     * @param permissions     the new array of permissions
     */
    public void setPermissions(Permission[] permissions) {
        this.permissions.clear();
        for (int i = 0; i < permissions.length; i++) {
            this.permissions.add(permissions[i]);
        }
    }

    /**
     * Validates the object data before writing to the database.
     *
     * @throws ContentException if the object data wasn't valid
     */
    protected void doValidate() throws ContentException {
        if (getDomainName().equals("")) {
            throw new ContentException("no domain set for permission list");
        } else if (getDomain() == null) {
            throw new ContentException("domain '" + getDomainName() +
                                       "'does not exist");
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
    protected void doInsert(DataSource src, User user, boolean restore)
        throws ContentException {

        Permission      perm;
        PermissionData  data;

        try {
            for (int i = 0; i < permissions.size(); i++) {
                perm = (Permission) permissions.get(i);
                data = perm.getData(domain, content);
                PermissionPeer.doInsert(src, data);
            }
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

        try {
            PermissionPeer.doDelete(src, domain, content);
            doInsert(src, user, false);
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
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
            PermissionPeer.doDelete(src, domain, content);
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        }
    }
}
