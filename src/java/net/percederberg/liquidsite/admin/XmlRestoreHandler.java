/*
 * XmlRestoreHandler.java
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

package net.percederberg.liquidsite.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.Group;
import org.liquidsite.core.content.Host;
import org.liquidsite.core.content.Permission;
import org.liquidsite.core.content.PermissionList;
import org.liquidsite.core.content.User;
import org.liquidsite.util.log.Log;

/**
 * A SAX XML handler for restoring backups. This class only restores
 * objects from the XML data, any external files must be restored
 * separately.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class XmlRestoreHandler extends DefaultHandler {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(XmlRestoreHandler.class);

    /**
     * The content manager to use.
     */
    private ContentManager manager;

    /**
     * The user performing the operation.
     */
    private User managerUser;

    /**
     * The content revision restoration mode to use.
     */
    private int mode;

    /**
     * The map from old to new content identifiers.
     */
    private HashMap contentIds = new HashMap();

    /**
     * The map from backup file names to new files.
     */
    private HashMap contentFiles = new HashMap();

    /**
     * The domain currently being processed.
     */
    private Domain currentDomain = null;

    /**
     * The name of the domain currently being processed.
     */
    private String currentDomainName = null;

    /**
     * The user currently being processed.
     */
    private User currentUser = null;

    /**
     * The old id of the content object currently being processed.
     */
    private int currentId = 0;

    /**
     * The category of the content object currently being processed.
     */
    private int currentCategory = 0;

    /**
     * The revisions of the content object currently being processed.
     */
    private ArrayList currentRevisions = null;

    /**
     * The content revision currently being processed.
     */
    private Content currentContent = null; 

    /**
     * The name of the content attribute currently being processed.
     */
    private String currentAttribute = null;

    /**
     * The value of the content attribute currently being processed.
     */
    private StringBuffer currentValue = new StringBuffer();

    /**
     * The list permissions currently being processed.
     */
    private ArrayList currentPermissions = null;

    /**
     * The complete restore flag. This flag is set to false if some
     * element of the backup was skipped, due to conflicting data
     * already in the database. An example would be if an identical
     * hostname was already defined for another domain.
     */
    private boolean complete = true;

    /**
     * Creates a new XML backup restore handler.
     *
     * @param domainName     the new domain to create
     * @param mode           the content revision restoration mode
     * @param user           the user performing the operation
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public XmlRestoreHandler(String domainName, int mode, User user)
        throws ContentException {

        this.manager = AdminUtils.getContentManager();
        this.managerUser = user;
        this.mode = mode;
        this.currentDomain = new Domain(manager, domainName);
    }

    /**
     * Checks if a complete restore operation was made. This flag is
     * set to false if some element of the backup was skipped, due to
     * conflicting data already in the database. An example would be
     * if an identical hostname was already defined for another
     * domain.
     *
     * @return true if the restore was complete, or
     *         false otherwise
     */
    public boolean isCompleteRestore() {
        return complete;
    }

    /**
     * Returns a map from backup file names to new files. The backup
     * file names should be the locations where the existing files
     * can be located inside the backup file. The files mapped to
     * contain the locations where the files should be copied.
     *
     * @return a map from backup file names to new files
     */
    public HashMap getContentFiles() {
        return contentFiles;
    }

    /**
     * Handles the start of an element.
     *
     * @param uri            the namespace URI
     * @param localName      the local name
     * @param qName          the qualified name
     * @param attrs          the element attributes
     *
     * @throws SAXException if the processing of the element failed
     */
    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attrs)
        throws SAXException {

        Group           group;
        User            user;
        Host            host;
        Permission      perm;
        String          str;

        try {
            if (qName.equals("domain")) {
                currentDomainName = attrs.getValue("name");
                currentDomain.setDescription(attrs.getValue("description"));
                currentDomain.restore(managerUser);
            } else if (qName.equals("group")) {
                str = attrs.getValue("name");
                group = new Group(manager, currentDomain, str);
                group.setDescription(attrs.getValue("description"));
                group.setComment(attrs.getValue("comment"));
                group.restore(managerUser);
            } else if (qName.equals("user")) {
                str = attrs.getValue("name");
                currentUser = new User(manager, currentDomain, str);
                currentUser.setPasswordEncoded(attrs.getValue("password"));
                if (attrs.getValue("disabled") != null) {
                    currentUser.setEnabled(false);
                }
                currentUser.setRealName(attrs.getValue("realname"));
                currentUser.setEmail(attrs.getValue("email"));
                currentUser.setComment(attrs.getValue("comment"));
            } else if (qName.equals("member")) {
                str = attrs.getValue("group");
                group = manager.getGroup(currentDomain, str);
                currentUser.addToGroup(group);
            } else if (qName.equals("host")) {
                str = attrs.getValue("name");
                host = new Host(manager, currentDomain, str);
                host.setDescription(attrs.getValue("description"));
                try {
                    host.restore(managerUser);
                } catch (ContentException e) {
                    LOG.error("skipping restore", e);
                    complete = false;
                }
            } else if (qName.equals("content")) {
                if (currentPermissions != null) {
                    createPermissions();
                }
                str = attrs.getValue("id");
                currentId = Integer.parseInt(str);
                str = attrs.getValue("category");
                currentCategory = Integer.parseInt(str);
                currentRevisions = new ArrayList();
            } else if (qName.equals("revision")) {
                currentContent = new Content(manager,
                                             currentDomain,
                                             currentCategory);
                str = attrs.getValue("nr");
                currentContent.setRevisionNumber(Integer.parseInt(str));
                currentContent.setName(attrs.getValue("name"));
                str = attrs.getValue("parent");
                currentContent.setParentId(parseContentId(str));
                str = attrs.getValue("online");
                currentContent.setOnlineDate(parseDate(str));
                str = attrs.getValue("offline");
                currentContent.setOfflineDate(parseDate(str));
                str = attrs.getValue("modified");
                currentContent.setModifiedDate(parseDate(str));
                currentContent.setAuthorName(attrs.getValue("author"));
                currentContent.setComment(attrs.getValue("comment"));
            } else if (qName.equals("attribute")) {
                currentAttribute = attrs.getValue("name");
            } else if (qName.equals("permission")) {
                if (currentPermissions == null) {
                    currentPermissions = new ArrayList();
                }
                str = attrs.getValue("user");
                if (str != null) {
                    user = manager.getUser(currentDomain, str);
                } else {
                    user = null;
                }
                str = attrs.getValue("group");
                if (str != null) {
                    group = manager.getGroup(currentDomain, str);
                } else {
                    group = null;
                }
                perm = new Permission(user, group);
                str = attrs.getValue("flags");
                perm.setRead(str.indexOf("r") >= 0);
                perm.setWrite(str.indexOf("w") >= 0);
                perm.setPublish(str.indexOf("p") >= 0);
                perm.setAdmin(str.indexOf("a") >= 0);
                currentPermissions.add(perm);
            }
        } catch (ContentException e) {
            LOG.error("restore error", e);
            throw new SAXException("restore error", e);
        } catch (ContentSecurityException e) {
            LOG.error("restore security error", e);
            throw new SAXException("restore security error", e);
        }
    }

    /**
     * Handles the end of an element.
     *
     * @param uri            the namespace URI
     * @param localName      the local name
     * @param qName          the qualified name
     *
     * @throws SAXException if the processing of the element failed
     */
    public void endElement(String uri, String localName, String qName)
        throws SAXException {

        int  id;

        try {
            if (qName.equals("domain")) {
                if (currentPermissions != null) {
                    createPermissions();
                }
            } else if (qName.equals("user")) {
                currentUser.restore(managerUser);
                currentUser = null;
            } else if (qName.equals("content")) {
                if (mode == 1) {
                    currentContent = findLatestRevision();
                    if (currentContent.getRevisionNumber() > 0) {
                        currentContent.setRevisionNumber(1);
                    }
                    currentContent.restore(managerUser);
                    addContentFile();
                } else if (mode == 2) {
                    currentContent = findLatestRevision();
                    currentContent.setRevisionNumber(0);
                    currentContent.setOnlineDate(null);
                    currentContent.setOfflineDate(null);
                    currentContent.restore(managerUser);
                    addContentFile();
                } else {
                    currentContent = (Content) currentRevisions.get(0);
                    currentContent.restore(managerUser);
                    addContentFile();
                    id = currentContent.getId();
                    for (int i = 1; i < currentRevisions.size(); i++) {
                        currentContent = (Content) currentRevisions.get(i);
                        currentContent.setId(id);
                        currentContent.restore(managerUser);
                        addContentFile();
                    }
                }
                contentIds.put(new Integer(currentId),
                               new Integer(currentContent.getId()));
                if (currentPermissions != null) {
                    createPermissions();
                }
                currentId = 0;
                currentCategory = 0;
                currentRevisions = null;
                currentContent = null;
            } else if (qName.equals("revision")) {
                currentRevisions.add(currentContent);
                currentContent = null;
            } else if (qName.equals("attribute")) {
                currentContent.setAttribute(currentAttribute,
                                            currentValue.toString());
                currentAttribute = null;
                currentValue.setLength(0);
            }
        } catch (ContentException e) {
            LOG.error("restore error", e);
            throw new SAXException("restore error", e);
        } catch (ContentSecurityException e) {
            LOG.error("restore security error", e);
            throw new SAXException("restore security error", e);
        }
    }

    /**
     * Handles characters inside an element.
     *
     * @param ch             the characters from the XML
     * @param start          the start position in the array
     * @param length         the number of characters to read
     */
    public void characters(char[] ch, int start, int length) {
        String  str;

        if (currentAttribute != null) {
            if (currentAttribute.equals("TEMPLATE")) {
                str = new String(ch, start, length);
                currentValue.append(parseContentId(str));
            } else if (currentAttribute.equals("LINK")) {
                str = new String(ch, start, length);
                try {
                    Integer.parseInt(str);
                    str = String.valueOf(parseContentId(str));
                } catch (NumberFormatException ignore) {
                    // Do nothing, i.e. store the original text
                }
                currentValue.append(str);
            } else {
                currentValue.append(ch, start, length);
            }
        }
    }

    /**
     * Returns the date representation for a string.
     *
     * @param str            the date string
     *
     * @return the string date representation
     */
    private Date parseDate(String str) {
        return new Date(Long.parseLong(str)); 
    }

    /**
     * Returns the content identifier from the specified string. The
     * content identifier will be translated from old to new values
     * if possible, otherwise zero (0) will be returned.
     *
     * @param str            the numeric string
     *
     * @return the new content identifier, or
     *         zero (0) if not found
     */
    private int parseContentId(String str) {
        Integer  value;

        try {
            value = new Integer(str);
        } catch (NumberFormatException e) {
            return 0;
        }
        if (contentIds.containsKey(value)) {
            return ((Integer) contentIds.get(value)).intValue();
        } else {
            return 0;
        }
    }

    /**
     * Returns the latest revision from the current ones.
     *
     * @return the latest revision
     */
    private Content findLatestRevision() {
        Content  content;
        Content  revision;
        int      pos;

        content = (Content) currentRevisions.get(0);
        if (currentRevisions.size() > 1) {
            pos = currentRevisions.size() - 1;
            revision = (Content) currentRevisions.get(pos);
            if (revision.getRevisionNumber() == 0) {
                content = revision;
            }
        }
        return content;
    }

    /**
     * Adds a content file record if the current content is a file.
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void addContentFile() throws ContentException {
        String  name;
        String  path;
        File    dir;

        if (currentCategory == Content.FILE_CATEGORY) {
            name = currentContent.getAttribute("FILENAME");
            path = currentDomainName + "/" + currentId + "/" + name;
            dir = new File(currentDomain.getDirectory(),
                           String.valueOf(currentContent.getId()));
            contentFiles.put(path, new File(dir, name));
        }
    }

    /**
     * Stores the current permissions in the database.
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void createPermissions()
        throws ContentException, ContentSecurityException {

        PermissionList  permissions;
        Permission[]    perms;

        if (currentContent == null) {
            permissions = new PermissionList(manager, currentDomain);
        } else {
            permissions = new PermissionList(manager, currentContent);
        }
        perms = new Permission[currentPermissions.size()];
        currentPermissions.toArray(perms);
        permissions.setPermissions(perms);
        permissions.restore(managerUser);
        currentPermissions = null;
    }
}
