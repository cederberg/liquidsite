/*
 * ContentTranslator.java
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

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.ContentData;

/**
 * A web site URL translator.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentTranslator extends Content {

    /**
     * The type content attribute.
     */
    private static final String TYPE_ATTRIBUTE = "TYPE";

    /**
     * The link content attribute.
     */
    private static final String LINK_ATTRIBUTE = "LINK";

    /**
     * The error translator type. Translators of this type have no
     * additional link information, but may have pages or files with
     * HTTP error code names. A page "404" would then match any "HTTP
     * 404 Not Found" error.
     */
    public static final int ERROR_TYPE = 1;

    /**
     * The alias translator type. Alias translators are linked to a
     * site content object (in the same domain).
     *
     * @see #getAlias
     * @see #setAlias
     * @see #getAliasId
     * @see #setAliasId
     */
    public static final int ALIAS_TYPE = 2;

    /**
     * The redirect translator type. Redirect translators sends a
     * "HTTP 304 Moved Permanently" response with a base URL prepended
     * to the request path.
     *
     * @see #getRedirectUrl
     * @see #setRedirectUrl
     */
    public static final int REDIRECT_TYPE = 3;

    /**
     * The section translator type. Section translators are linked to
     * a content section object (in the same domain). It performs
     * directory matches to child objects in the section.
     *
     * @see #getSection
     * @see #setSection
     * @see #getSectionId
     * @see #setSectionId
     */
    public static final int SECTION_TYPE = 4;

    /**
     * Creates a new translator with default values.
     *
     * @param manager        the content manager to use
     * @param parent         the parent content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public ContentTranslator(ContentManager manager, Content parent)
        throws ContentException {

        super(manager, parent.getDomain(), Content.TRANSLATOR_CATEGORY);
        setParent(parent);
        setAttribute(TYPE_ATTRIBUTE, String.valueOf(ERROR_TYPE));
        setAttribute(LINK_ATTRIBUTE, "");
    }

    /**
     * Creates a new translator.
     *
     * @param manager        the content manager to use
     * @param data           the content data object
     * @param con            the database connection to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ContentTranslator(ContentManager manager,
                                ContentData data,
                                DatabaseConnection con)
        throws ContentException {

        super(manager, data, con);
    }

    /**
     * Returns the translator type.
     *
     * @return the translator type
     */
    public int getType() {
        return Integer.parseInt(getAttribute(TYPE_ATTRIBUTE));
    }

    /**
     * Sets the translator type.
     *
     * @param type           the new translator type
     */
    public void setType(int type) {
        setAttribute(TYPE_ATTRIBUTE, String.valueOf(type));
    }

    /**
     * Returns the translator alias content. If the translator isn't
     * of alias type, this method will return null.
     *
     * @param user           the user performing the operation
     *
     * @return the translator alias content, or
     *         null for none
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have read
     *             access to the content
     */
    public Content getAlias(User user)
        throws ContentException, ContentSecurityException {

        int  id = getAliasId();

        if (id <= 0) {
            return null;
        } else {
            return getContentManager().getContent(user, id);
        }
    }

    /**
     * Sets the translator alias content. This method also sets the
     * translator type.
     *
     * @param alias          the new alias content, or null for none
     */
    public void setAlias(Content alias) {
        if (alias == null) {
            setAliasId(0);
        } else {
            setAliasId(alias.getId());
        }
    }

    /**
     * Returns the translator alias content identifier.
     *
     * @return the alias content identifier, or
     *         zero (0) if the translator isn't of alias type
     */
    public int getAliasId() {
        String  value = getAttribute(LINK_ATTRIBUTE);

        if (getType() == ALIAS_TYPE && value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignore) {
                // Ignore
            }
        }
        return 0;
    }

    /**
     * Sets the translation alias content identifier. This method
     * also sets the translator type.
     *
     * @param alias          the new alias content identifier
     */
    public void setAliasId(int alias) {
        setType(ALIAS_TYPE);
        setAttribute(LINK_ATTRIBUTE, String.valueOf(alias));
    }

    /**
     * Returns the translator redirect URL.
     *
     * @return the redirect URL, or
     *         null if the translator isn't of redirect type
     */
    public String getRedirectUrl() {
        if (getType() == REDIRECT_TYPE) {
            return getAttribute(LINK_ATTRIBUTE);
        } else {
            return null;
        }
    }

    /**
     * Sets the translation alias redirect URL. This method also sets
     * the translator type.
     *
     * @param url            the new redirect URL
     */
    public void setRedirectUrl(String url) {
        setType(REDIRECT_TYPE);
        setAttribute(LINK_ATTRIBUTE, url);
    }

    /**
     * Returns the translator section. If the translator isn't of
     * section type, this method will return null.
     *
     * @param user           the user performing the operation
     *
     * @return the translator section, or
     *         null for none
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have read
     *             access to the section
     */
    public ContentSection getSection(User user)
        throws ContentException, ContentSecurityException {

        int  id = getSectionId();

        if (id <= 0) {
            return null;
        } else {
            return (ContentSection) getContentManager().getContent(user, id);
        }
    }

    /**
     * Sets the translator section. This method also sets the
     * translator type.
     *
     * @param section        the new section, or null for none
     */
    public void setSection(ContentSection section) {
        if (section == null) {
            setSectionId(0);
        } else {
            setSectionId(section.getId());
        }
    }

    /**
     * Returns the translator section content identifier.
     *
     * @return the section content identifier, or
     *         zero (0) if the translator isn't of section type
     */
    public int getSectionId() {
        String  value = getAttribute(LINK_ATTRIBUTE);

        if (getType() == SECTION_TYPE && value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignore) {
                // Ignore
            }
        }
        return 0;
    }

    /**
     * Sets the translation section content identifier. This method
     * also sets the translator type.
     *
     * @param section        the new section identifier
     */
    public void setSectionId(int section) {
        setType(SECTION_TYPE);
        setAttribute(LINK_ATTRIBUTE, String.valueOf(section));
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
            throw new ContentException("no parent set for translator");
        }
        children = InternalContent.findByParent(getContentManager(),
                                                getParent());
        for (int i = 0; i < children.length; i++) {
            if (children[i].getId() != getId()
             && children[i].getName().equals(getName())) {

                throw new ContentException(
                    "another object with the same name is already " +
                    "present in the same folder");
            }
        }
        switch (getType()) {
        case ERROR_TYPE:
            break;
        case ALIAS_TYPE:
            if (getAliasId() <= 0) {
                throw new ContentException("translator alias id missing");
            }
            break;
        case REDIRECT_TYPE:
            if (getRedirectUrl() == null || getRedirectUrl().length() <= 0) {
                throw new ContentException("translator redirect url missing");
            }
            break;
        case SECTION_TYPE:
            if (getSectionId() <= 0) {
                throw new ContentException("translator section id missing");
            }
            break;
        default:
            throw new ContentException("translator type unknown: " +
                                       getType());
        }
    }
}
