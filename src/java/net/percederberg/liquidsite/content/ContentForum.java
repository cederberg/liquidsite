/*
 * ContentForum.java
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

package net.percederberg.liquidsite.content;

import net.percederberg.liquidsite.dbo.ContentData;

import org.liquidsite.util.db.DatabaseConnection;

/**
 * A discussion forum.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentForum extends Content {

    /**
     * The real name content attribute.
     */
    private static final String REAL_NAME_ATTRIBUTE = "REALNAME";

    /**
     * The description content attribute.
     */
    private static final String DESCRIPTION_ATTRIBUTE = "DESCRIPTION";

    /**
     * The moderator content attribute.
     */
    private static final String MODERATOR_ATTRIBUTE = "MODERATOR";

    /**
     * Creates a new forum with default values.
     *
     * @param manager        the content manager to use
     * @param parent         the parent content section
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public ContentForum(ContentManager manager, ContentSection parent)
        throws ContentException {

        super(manager, parent.getDomain(), Content.FORUM_CATEGORY);
        setParent(parent);
        setAttribute(REAL_NAME_ATTRIBUTE, "");
        setAttribute(DESCRIPTION_ATTRIBUTE, "");
        setAttribute(MODERATOR_ATTRIBUTE, "");
    }

    /**
     * Creates a new forum.
     *
     * @param manager        the content manager to use
     * @param data           the content data object
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ContentForum(ContentManager manager,
                           ContentData data,
                           DatabaseConnection con)
        throws ContentException {

        super(manager, data, con);
    }

    /**
     * Returns the real forum name.
     *
     * @return the real forum name
     */
    public String getRealName() {
        return getAttribute(REAL_NAME_ATTRIBUTE);
    }

    /**
     * Sets the real forum name.
     *
     * @param name           the new forum name
     */
    public void setRealName(String name) {
        setAttribute(REAL_NAME_ATTRIBUTE, name);
    }

    /**
     * Returns the forum description.
     *
     * @return the forum description
     */
    public String getDescription() {
        return getAttribute(DESCRIPTION_ATTRIBUTE);
    }

    /**
     * Sets the forum description.
     *
     * @param description    the new forum description
     */
    public void setDescription(String description) {
        setAttribute(DESCRIPTION_ATTRIBUTE, description);
    }

    /**
     * Returns the moderator group.
     *
     * @return the moderator group, or
     *         null for none
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public Group getModerator() throws ContentException {
        return getContentManager().getGroup(getDomain(), getModeratorName());
    }

    /**
     * Sets the moderator group.
     *
     * @param moderator      the new moderator group, or null for none
     */
    public void setModerator(Group moderator) {
        if (moderator == null) {
            setModeratorName("");
        } else {
            setModeratorName(moderator.getName());
        }
    }

    /**
     * Returns the moderator group name.
     *
     * @return the moderator group name, or
     *         an empty string for none
     */
    public String getModeratorName() {
        String  name = getAttribute(MODERATOR_ATTRIBUTE);

        return (name == null) ? "" : name;
    }

    /**
     * Sets the moderator group name.
     *
     * @param moderator      the new moderator group
     */
    public void setModeratorName(String moderator) {
        setAttribute(MODERATOR_ATTRIBUTE, moderator);
    }

    /**
     * Checks if the specified user is a forum moderator.
     *
     * @param user           the user to check
     *
     * @return true if the user is a forum moderator, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean isModerator(User user) throws ContentException {
        Group[]  groups = user.getGroups();

        for (int i = 0; i < groups.length; i++) {
            if (groups[i].getDomainName().equals(getDomainName())
             && groups[i].getName().equals(getModeratorName())) {

                return true;
            }
        }
        return false;
    }

    /**
     * Validates the object data before writing to the database.
     *
     * @throws ContentException if the object data wasn't valid
     */
    protected void doValidate() throws ContentException {
        Content[]  children;

        super.doValidate();
        if (getParent() == null) {
            throw new ContentException("no parent set for forum");
        } else if (getRealName().equals("")) {
            throw new ContentException("no real name set for forum");
        }
        children = InternalContent.findByParent(getContentManager(),
                                                getParent());
        for (int i = 0; i < children.length; i++) {
            if (children[i].getId() != getId()
             && children[i].getName().equals(getName())) {

                throw new ContentException(
                    "another object with the same name is already " +
                    "present in the parent folder");
            }
        }
    }
}
