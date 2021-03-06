/*
 * ContentBean.java
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
 * Copyright (c) 2004-2007 Per Cederberg. All rights reserved.
 */

package org.liquidsite.app.template;

import java.util.ArrayList;
import java.util.Date;

import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.User;
import org.liquidsite.util.log.Log;

/**
 * A content template bean. This class is used as the base class for
 * all the template beans accessing content objects, such as
 * documents, sections and files.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public abstract class ContentBean extends TemplateBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(ContentBean.class);

    /**
     * The parent content bean.
     */
    private ContentBean parent;

    /**
     * The content being encapsulated.
     */
    private Content content;

    /**
     * The revision content beans. Used for caching.
     */
    private ArrayList revisions = null;

    /**
     * Creates a new content template bean.
     *
     * @param context        the bean context
     * @param content        the content, or null
     */
    ContentBean(BeanContext context, Content content) {
        this(context, null, content);
    }

    /**
     * Creates a new content template bean.
     *
     * @param context        the bean context
     * @param parent         the parent content bean, or null
     * @param content        the content, or null
     */
    ContentBean(BeanContext context, ContentBean parent, Content content) {
        super(context);
        this.parent = parent;
        this.content = content;
    }

    /**
     * Returns a string description of this bean. The description will
     * contain the content name.
     *
     * @return a string description of this bean
     */
    public String toString() {
        return getName();
    }

    /**
     * Returns the encapsulated content object.
     *
     * @return the encapsulated content object, or
     *         null if the content doesn't exist
     */
    protected Content getContent() {
        return content;
    }

    /**
     * Returns the content identifier.
     *
     * @return the content identifier, or
     *         zero (0) if the content doesn't exist
     */
    public int getId() {
        if (content != null) {
            return content.getId();
        }
        return 0;
    }

    /**
     * Returns the content name.
     *
     * @return the content name, or
     *         an empty string if the content doesn't exist
     */
    public String getName() {
        if (content != null) {
            return content.getName();
        }
        return "";
    }

    /**
     * Returns the full content path.
     *
     * @return the content path, or
     *         an empty string if the content doesn't exist
     */
    public String getPath() {
        String  path = getContext().getContentPath(content);

        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        } else {
            return path;
        }
    }

    /**
     * Returns the parent content bean. If there is no content parent
     * section, an emtpy section bean will be returned.
     *
     * @return the parent content, or
     *         an empty section if no parent exists
     */
    public ContentBean getParent() {
        Content  contentParent;

        if (content == null) {
            return this;
        } else if (parent != null) {
            return parent;
        } else {
            try {
                contentParent = content.getParent();
                parent = getContext().createContentBean(contentParent);
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
            if (parent == null) {
                parent = new SectionBean();
            }
            return parent;
        }
    }

    /**
     * Returns the content revision number.
     *
     * @return the content revision number, or
     *         zero (0) if the content doesn't exist
     */
    public int getRevision() {
        if (content == null) {
            return 0;
        } else {
            return content.getRevisionNumber();
        }
    }

    /**
     * Returns a list of content beans for all revisions. The list
     * will be ordered in revision order, i.e. the oldest revision
     * will be first.
     *
     * @return a list of content beans for all revisions
     */
    public ArrayList getRevisions() {
        Content[]  data;

        if (revisions == null) {
            if (content != null) {
                try {
                    data = content.getAllRevisions();
                    revisions = new ArrayList(data.length);
                    for (int i = data.length - 1; i >= 0; i--) {
                        revisions.add(getContext().createContentBean(data[i]));
                    }
                } catch (ContentException e) {
                    LOG.error(e.getMessage());
                }
            }
            if (revisions == null) {
                revisions = new ArrayList(0);
            }
        }
        return revisions;
    }

    /**
     * Returns the content creation date.
     *
     * @return the content creation date, or
     *         the current date and time if the content doesn't exist
     */
    public Date getCreated() {
        Content  revision;

        if (content != null) {
            if (getRevision() > 1) {
                try {
                    revision = content.getRevision(1);
                    return revision.getModifiedDate();
                } catch (ContentException e) {
                    LOG.error(e.getMessage());
                }
            }
            return content.getModifiedDate();
        }
        return new Date();
    }

    /**
     * Returns the content revision date.
     *
     * @return the content revision date, or
     *         the current date and time if the content doesn't exist
     */
    public Date getDate() {
        if (content == null) {
            return new Date();
        } else {
            return content.getModifiedDate();
        }
    }

    /**
     * Returns the content revision author.
     *
     * @return the content revision user bean, or
     *         an empty user if the content doesn't exist
     */
    public UserBean getUser() {
        if (content == null) {
            return new UserBean(getContext(), null);
        } else {
            return getContext().findUser(content.getAuthorName());
        }
    }

    /**
     * Returns the content online flag.
     *
     * @return the content online flag, or
     *         the false if the content doesn't exist
     */
    public boolean getOnline() {
        if (content == null) {
            return false;
        } else {
            return content.isOnline();
        }
    }

    /**
     * Returns the content lock.
     *
     * @return the content lock object, or
     *         an empty lock if the content doesn't exist
     */
    public LockBean getLock() {
        if (content != null) {
            try {
                return new LockBean(getContext(), content.getLock());
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return new LockBean();
    }

    /**
     * Checks if the current user has a specified access permission.
     * The permission names accepted are "read", "write", "publish"
     * and "admin". Any other names will default to false.
     *
     * @param permission     the permission name to check for
     *
     * @return true if the user has the specified permission, or
     *         false otherwise
     */
    public boolean hasAccess(String permission) {
        User  user;

        if (content != null) {
            try {
                user = getContextRequest().getUser();
                if (permission.equals("read")) {
                    return content.hasReadAccess(user);
                } else if (permission.equals("write")) {
                    return content.hasWriteAccess(user);
                } else if (permission.equals("publish")) {
                    return content.hasPublishAccess(user);
                } else if (permission.equals("admin")) {
                    return content.hasAdminAccess(user);
                } else {
                    LOG.warning("unrecognized permission name: " +
                                permission);
                }
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return false;
    }
}
