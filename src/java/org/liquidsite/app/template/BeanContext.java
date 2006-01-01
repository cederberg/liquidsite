/*
 * BeanContext.java
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
 * Copyright (c) 2004-2006 Per Cederberg. All rights reserved.
 */

package org.liquidsite.app.template;

import java.util.ArrayList;
import java.util.HashMap;

import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentDocument;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentFolder;
import org.liquidsite.core.content.ContentForum;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentSection;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.ContentSelector;
import org.liquidsite.core.content.ContentSite;
import org.liquidsite.core.content.ContentTopic;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.Group;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.Request;
import org.liquidsite.util.log.Log;
import org.liquidsite.util.mail.MailMessageException;
import org.liquidsite.util.mail.MailQueue;
import org.liquidsite.util.mail.SimpleMailMessage;

/**
 * A template bean context. This class holds contains references to
 * common objects used by all beans. It also contains several utility
 * methods to simplify the code in the various beans.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class BeanContext {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(BeanContext.class);

    /**
     * The request being processed.
     */
    private Request request;

    /**
     * The content manager to use.
     */
    private ContentManager manager;

    /**
     * The relative path to the site root directory. The path is
     * relative to the request URL and may be empty if the request
     * refers to an object in the site root directory. Otherwise the
     * path ends with an "/" character.
     */
    private String sitePath = null;

    /**
     * The cache of user beans. Each user is indexed by the user name.
     * The special user name "" (i.e. an empty string) is used for the
     * current user.
     */
    private HashMap usersCache = new HashMap();

    /**
     * Creates a new bean context.
     *
     * @param request        the request object
     * @param manager        the content manager to use
     */
    public BeanContext(Request request, ContentManager manager) {
        UserBean  user;

        this.request = request;
        this.manager = manager;
        user = new UserBean(this, request.getUser());
        usersCache.put(user.getLogin(), user);
        if (!user.getLogin().equals("")) {
            usersCache.put("", user);
        }
    }

    /**
     * Returns the request object.
     *
     * @return the request object
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Returns the relative path to the site root directory. The path
     * is relative to the request URL and may be empty if the request
     * refers to an object in the site root directory. Otherwise the
     * path ends with an "/" character. The result of this method
     * will be stored in an instance variable to avoid calculations
     * on further requests.
     *
     * @return the relative path to the site root directory
     */
    public String getSitePath() {
        ContentSite   site;

        if (sitePath == null) {
            site = request.getEnvironment().getSite();
            sitePath = getRelativePath(site.getDirectory());
        }
        return sitePath;
    }

    /**
     * Returns the relative path to a specified base path. The
     * specified path is assumed to be a part of the complete request
     * path. This method will return an empty string if the request
     * refers to a object in the specified directory.
     *
     * @param basePath       the absolute base path
     *
     * @return the relative path to the base path for the request
     */
    public String getRelativePath(String basePath) {
        StringBuffer  buffer = new StringBuffer();
        String        path;
        int           pos;

        path = request.getPath();
        path = path.substring(basePath.length());
        while ((pos = path.indexOf('/')) >= 0) {
            buffer.append("../");
            path = path.substring(pos + 1);
        }
        return buffer.toString();
    }

    /**
     * Returns the absolute path to a content object.
     *
     * @param content        the content object
     *
     * @return the absolute path to a content object
     */
    public String getContentPath(Content content) {
        try {
            if (content instanceof ContentSite) {
                return request.getEnvironment().getSite().getDirectory();
            } else if (content instanceof ContentFolder
                    || content instanceof ContentSection
                    || content instanceof ContentDocument
                    || content instanceof ContentForum
                    || content instanceof ContentTopic) {

                return getContentPath(content.getParent()) +
                       content.getName() + "/";
            } else if (content != null) {
                return getContentPath(content.getParent()) +
                       content.getName();
            } else {
                return "/";
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            return "";
        }
    }

    /**
     * Counts the number of content objects matching a content
     * selector.
     *
     * @param selector       the content selector
     *
     * @return the number of matching content objects
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public int countContent(ContentSelector selector)
        throws ContentException {

        return manager.getContentCount(selector);
    }

    /**
     * Counts the number of documents under the specified absolute
     * path.
     *
     * @param path           the section path
     *
     * @return the number of documents found
     */
    public int countDocuments(String path) {
        Content  content;

        try {
            content = findContent(path);
            if (content instanceof ContentSection) {
                return countDocuments((ContentSection) content);
            } else {
                LOG.error("failed to find section: " + path);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
        }
        return 0;
    }

    /**
     * Counts the number of documents under the specified section (and
     * subsections).
     *
     * @param section        the parent section
     *
     * @return the number of documents found
     */
    public int countDocuments(ContentSection section) {
        ContentSelector   selector;

        try {
            selector = new ContentSelector(section.getDomain());
            selector.requireCategory(Content.DOCUMENT_CATEGORY);
            setSelectorParents(selector, section);
            return countContent(selector);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        }
        return 0;
    }

    /**
     * Finds a content object from a specified content id.
     *
     * @param id             the content identifier
     *
     * @return the content object found, or
     *         null for none
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the content couldn't be
     *             read by the current user
     */
    public Content findContent(int id)
        throws ContentException, ContentSecurityException {

        return manager.getContent(request.getUser(), id);
    }

    /**
     * Finds a content object from an absolute path. The object found
     * can be any object present in a section, that is either a
     * section, a document or a forum.
     *
     * @param path           the content path (from the domain root)
     *
     * @return the content object found, or
     *         null if not found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the content couldn't be
     *             read by the current user
     */
    public Content findContent(String path)
        throws ContentException, ContentSecurityException {

        Domain   domain;

        domain = request.getEnvironment().getDomain();
        return findContent(domain, path);
    }

    /**
     * Finds a content object from a relative path. The object found
     * can be any object present in a section, that is either a
     * section, a document or a forum.
     *
     * @param parent         the parent domain or section
     * @param path           the content path (relative to the parent)
     *
     * @return the content object found, or
     *         null if not found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the content couldn't be
     *             read by the current user
     */
    public Content findContent(Object parent, String path)
        throws ContentException, ContentSecurityException {

        String  name;
        int     pos;

        // Check for empty path or null parent
        if (path == null || path.equals("") || parent == null) {
            if (parent instanceof Content) {
                return (Content) parent;
            } else {
                return null;
            }
        }

        // Find child name
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        pos = path.indexOf("/");
        if (pos > 0) {
            name = path.substring(0, pos);
            path = path.substring(pos + 1);
        } else {
            name = path;
            path = "";
        }

        // Find child content
        return findContent(findContentChild(parent, name), path);
    }

    /**
     * Finds a named child object. The parent may be either a domain
     * or a content section. The child returned can be either a
     * content section or some content object present directly in the
     * section.
     *
     * @param parent         the parent domain or section
     * @param name           the child name
     *
     * @return the child content object found, or
     *         null if not found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the section couldn't be
     *             read by the current user
     */
    private Content findContentChild(Object parent, String name)
        throws ContentException, ContentSecurityException {

        Content[]  children;

        if (manager == null) {
            return null;
        } else if (parent instanceof Domain) {
            // TODO: implement this more efficiently
            children = manager.getContentChildren(request.getUser(),
                                                  (Domain) parent,
                                                  Content.SECTION_CATEGORY);
            for (int i = 0; i < children.length; i++) {
                if (children[i].getName().equals(name)) {
                    return children[i];
                }
            }
        } else {
            return manager.getContentChild(request.getUser(),
                                           (Content) parent,
                                           name);
        }
        return null;
    }

    /**
     * Finds a set of content objects with a content selector.
     *
     * @param selector       the content selector
     *
     * @return the array of content objects found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public Content[] findContent(ContentSelector selector)
        throws ContentException {

        return manager.getContentObjects(request.getUser(), selector);
    }

    /**
     * Finds a document corresponding to the specified absolute path.
     *
     * @param path           the document (and section) path
     *
     * @return the document found, or
     *         an empty document if not found
     */
    public DocumentBean findDocument(String path) {
        Domain   domain;

        domain = request.getEnvironment().getDomain();
        return findDocument(domain, path);
    }

    /**
     * Finds a document corresponding to the specified relative path.
     *
     * @param parent         the parent domain or section
     * @param path           the document (and section) path
     *
     * @return the document found, or
     *         an empty document if not found
     */
    public DocumentBean findDocument(Object parent, String path) {
        Content  content;

        try {
            content = findContent(parent, path);
            if (content instanceof ContentDocument) {
                return new DocumentBean(this,
                                        (ContentDocument) content,
                                        (ContentSection) content.getParent());
            } else {
                LOG.warning("failed to find document: " + path);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
        }
        return new DocumentBean();
    }

    /**
     * Finds all documents under the specified absolute path. The
     * documents will be returned (as document beans) in a list.
     *
     * @param path           the section path
     * @param sorting        the sorting information
     * @param offset         the number of documents to skip
     * @param count          the maximum number of documents
     *
     * @return a list of the documents found (as document beans)
     */
    public ArrayList findDocuments(String path,
                                   String sorting,
                                   int offset,
                                   int count) {

        Content  content;

        try {
            content = findContent(path);
            if (content instanceof ContentSection) {
                return findDocuments((ContentSection) content,
                                     sorting,
                                     offset,
                                     count);
            } else {
                LOG.error("failed to find section: " + path);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
        }
        return new ArrayList(0);
    }

    /**
     * Finds all documents under the specified section (and
     * subsections). The documents will be returned (as document
     * beans) in a list.
     *
     * @param section        the parent section
     * @param sorting        the sorting information
     * @param offset         the number of documents to skip
     * @param count          the maximum number of documents
     *
     * @return a list of the documents found (as document beans)
     */
    public ArrayList findDocuments(ContentSection section,
                                   String sorting,
                                   int offset,
                                   int count) {

        ArrayList        results = new ArrayList();
        ContentSelector  selector;
        Content[]        children;
        DocumentBean     doc;

        try {
            selector = new ContentSelector(section.getDomain());
            selector.requireCategory(Content.DOCUMENT_CATEGORY);
            setSelectorParents(selector, section);
            setSelectorSorting(selector, sorting.trim());
            selector.limitResults(offset, count);
            children = findContent(selector);
            for (int i = 0; i < children.length; i++) {
                doc = new DocumentBean(this,
                                       (ContentDocument) children[i],
                                       section);
                results.add(doc);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        }
        return results;
    }

    /**
     * Finds a named forum in a section.
     *
     * @param section        the parent section
     * @param name           the forum name
     *
     * @return the forum found, or
     *         an empty forum if not found
     */
    public ForumBean findForum(ContentSection section, String name) {
        Content[]  content;

        try {
            // TODO: implement this more efficiently
            content = manager.getContentChildren(request.getUser(),
                                                 section,
                                                 Content.FORUM_CATEGORY);
            for (int i = 0; i < content.length; i++) {
                if (content[i].getName().equals(name)) {
                    return new ForumBean(this,
                                         (ContentForum) content[i]);
                }
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        }
        return new ForumBean();
    }

    /**
     * Finds a section corresponding to the specified absolute path.
     *
     * @param path           the section path
     *
     * @return the section found, or
     *         an empty section if not found
     */
    public SectionBean findSection(String path) {
        Domain   domain;

        domain = request.getEnvironment().getDomain();
        return findSection(domain, path);
    }

    /**
     * Finds a section corresponding to the specified relative path.
     *
     * @param parent         the parent domain or section
     * @param path           the section path
     *
     * @return the section found, or
     *         an empty section if not found
     */
    public SectionBean findSection(Object parent, String path) {
        Content  content;

        try {
            content = findContent(parent, path);
            if (content instanceof ContentSection) {
                return new SectionBean(this, (ContentSection) content);
            } else {
                LOG.error("failed to find section: " + path);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
        }
        return new SectionBean();
    }

    /**
     * Finds a specified user. All users found will be added to an
     * internal cache, making repetitive queries efficient.
     *
     * @param name            the user name, or "" for the current user
     *
     * @return the user bean for the specified user, or
     *         an empty bean if no such user could be found
     */
    public UserBean findUser(String name) {
        Domain   domain;
        User     user = null;

        if (!usersCache.containsKey(name)) {
            try {
                domain = request.getEnvironment().getDomain();
                user = manager.getUser(domain, name);
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
            usersCache.put(name, new UserBean(this, user));
        }
        return (UserBean) usersCache.get(name);
    }

    /**
     * Finds a specified group.
     *
     * @param name           the group name
     *
     * @return the group found, or
     *         null if not found
     */
    public Group findGroup(String name) {
        Domain  domain;
        Group   group = null;

        try {
            domain = request.getEnvironment().getDomain();
            group = manager.getGroup(domain, name);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        }
        return group;
    }

    /**
     * Creates a new user with the specified name in the current
     * environment domain. The user object will not be saved to the
     * database, only instantiated with the correct parameters.
     *
     * @param name           the login name
     *
     * @return the user object create, or
     *         null if no such user could be created
     */
    public User createUser(String name) {
        return new User(manager,
                        request.getEnvironment().getDomain(),
                        name);
    }

    /**
     * Sends an email to the specified receiver. The email will not be
     * sent immediately, but rather queued in the outgoing mail queue.
     *
     * @param receiver       the message recipient address
     * @param subject        the message subject line
     * @param text           the message text
     *
     * @return true if the mail could be queued correctly, or
     *         false otherwise
     */
    public boolean sendMail(String receiver, String subject, String text) {
        SimpleMailMessage  msg = new SimpleMailMessage();

        try {
            msg.setRecipients(receiver);
            msg.setSubject(subject);
            msg.setText(text);
            msg.setAttribute("URL", request.getUrl() );
            msg.setAttribute("IP", request.getRemoteAddr());
            MailQueue.getInstance().add(msg);
            return true;
        } catch (MailMessageException e) {
            LOG.warning("couldn't send mail", e);
            return false;
        }
    }

    /**
     * Sends an email to all the members in a group. The email will
     * not be sent immediately, but rather queued in the outgoing
     * mail queue.
     *
     * @param receiver       the message recipient group
     * @param subject        the message subject line
     * @param text           the message text
     *
     * @return true if the mail could be queued correctly, or
     *         false otherwise
     */
    public boolean sendMail(Group receiver, String subject, String text) {
        GroupMailMessage  msg = new GroupMailMessage();

        try {
            msg.setRecipient(receiver);
            msg.setSubject(subject);
            msg.setText(text);
            msg.setAttribute("URL", request.getUrl() );
            msg.setAttribute("IP", request.getRemoteAddr());
            MailQueue.getInstance().add(msg);
            return true;
        } catch (MailMessageException e) {
            LOG.warning("couldn't send mail", e);
            return false;
        }
    }

    /**
     * Sets the selector parent requirement from the specified section
     * and all its subsections.
     *
     * @param selector       the content selector
     * @param section        the content section
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void setSelectorParents(ContentSelector selector,
                                    ContentSection section)
        throws ContentException {

        Content[]  children;

        selector.requireParent(section);
        children = manager.getContentChildren(request.getUser(),
                                              section,
                                              Content.SECTION_CATEGORY);
        for (int i = 0; i < children.length; i++) {
            setSelectorParents(selector, (ContentSection) children[i]);
        }
    }

    /**
     * Sets the selector sorting from the specified sorting
     * information.
     *
     * @param selector       the content selector
     * @param sorting        the sorting information
     */
    private void setSelectorSorting(ContentSelector selector,
                                    String sorting) {

        String   str;
        boolean  ascending;
        int      pos;

        if (sorting.equals("")) {
            selector.sortByModified(false);
        }
        while (sorting.length() > 0) {
            pos = sorting.indexOf(",");
            if (pos > 0) {
                str = sorting.substring(0, pos).trim();
                sorting = sorting.substring(pos + 1).trim();
            } else {
                str = sorting;
                sorting = "";
            }
            ascending = true;
            if (str.startsWith("+") || str.startsWith("-")) {
                ascending = str.startsWith("+");
                str = str.substring(1);
            }
            if (str.equals("id")) {
                selector.sortById(ascending);
            } else if (str.equals("name")) {
                selector.sortByName(ascending);
            } else if (str.equals("path")) {
                selector.sortByParent(ascending);
                selector.sortByName(ascending);
            } else if (str.equals("parent")) {
                selector.sortByParent(ascending);
            } else if (str.equals("revision")) {
                selector.sortByRevision(ascending);
            } else if (str.equals("date")) {
                selector.sortByModified(ascending);
            } else if (str.equals("user")) {
                selector.sortByAuthor(ascending);
            } else if (str.equals("online")) {
                selector.sortByOnline(ascending);
            } else if (str.startsWith("data.")) {
                selector.sortByDocumentProperty(str.substring(5), ascending);
            } else {
                LOG.warning("invalid sorting column: " + sorting + 
                            " in page " + request.getUrl());
            }
        }
    }
}
