/*
 * LiquidSiteBean.java
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

package net.percederberg.liquidsite.template;

import java.util.ArrayList;

import org.liquidsite.util.log.Log;

/**
 * A LiquidSite template bean. This class is used to insert the
 * "liquidsite" namespace into the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class LiquidSiteBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(LiquidSiteBean.class);

    /**
     * The bean context.
     */
    private BeanContext context;

    /**
     * The request bean.
     */
    private RequestBean requestBean = null;

    /**
     * The user session bean.
     */
    private SessionBean sessionBean = null;

    /**
     * The section bean.
     */
    private SectionBean sectionBean = null;

    /**
     * The document bean.
     */
    private DocumentBean docBean = null;

    /**
     * The forum bean.
     */
    private ForumBean forumBean = null;

    /**
     * The topic bean.
     */
    private TopicBean topicBean = null;

    /**
     * Creates a new LiquidSite template bean.
     *
     * @param context        the bean context
     */
    LiquidSiteBean(BeanContext context) {
        this.context = context;
    }

    /**
     * Returns the build version name.
     *
     * @return the build version name
     */
    public String getVersion() {
        return TemplateManager.getApplication().getBuildVersion();
    }

    /**
     * Returns the build date.
     *
     * @return the build date
     */
    public String getDate() {
        return TemplateManager.getApplication().getBuildDate();
    }

    /**
     * Returns the request bean.
     *
     * @return the request bean
     */
    public RequestBean getRequest() {
        if (requestBean == null) {
            requestBean = new RequestBean(context);
        }
        return requestBean;
    }

    /**
     * Returns the user session bean.
     *
     * @return the user session bean
     */
    public SessionBean getSession() {
        if (sessionBean == null) {
            sessionBean = new SessionBean(context);
        }
        return sessionBean;
    }

    /**
     * Returns the section bean.
     *
     * @return the section bean
     */
    public SectionBean getSection() {
        if (sectionBean == null) {
            sectionBean = new SectionBean(context);
        }
        return sectionBean;
    }

    /**
     * Returns the document bean.
     *
     * @return the document bean
     */
    public DocumentBean getDoc() {
        if (docBean == null) {
            docBean = new DocumentBean(context);
        }
        return docBean;
    }

    /**
     * Returns the forum bean.
     *
     * @return the forum bean
     */
    public ForumBean getForum() {
        if (forumBean == null) {
            forumBean = new ForumBean(context);
        }
        return forumBean;
    }

    /**
     * Returns the topic bean.
     *
     * @return the topic bean
     */
    public TopicBean getTopic() {
        if (topicBean == null) {
            topicBean = new TopicBean(context);
        }
        return topicBean;
    }

    /**
     * Returns the user bean for the current user.
     *
     * @return the user bean for the current user
     */
    public UserBean getUser() {
        return context.findUser("");
    }

    /**
     * Returns the utility bean.
     *
     * @return the utility bean
     */
    public UtilBean getUtil() {
        return new UtilBean();
    }

    /**
     * Returns a relative link to an object in the same site. If the
     * specified path starts with '/' it is assumed to be relative to
     * the site root directory, otherwise it is assumed to be
     * relative to the page directory. Note that the page directory
     * is NOT always an empty string (consider dynamic pages linked
     * to sections).
     *
     * @param path           the site- or page-relative link path
     *
     * @return the path relative to the request path
     */
    public String linkTo(String path) {
        LOG.trace("call to linkTo: " + path);
        if (path.indexOf(":") >= 0) {
            return path;
        } else if (path.startsWith("/")) {
            return context.getSitePath() + path.substring(1);
        } else {
            return path;
        }
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
    public boolean mailTo(String receiver, String subject, String text) {
        LOG.trace("call to mailTo: " + receiver + "," + subject + ",...");
        return context.sendMail(receiver, subject, text);
    }

    /**
     * Returns the number of documents in the specified section and
     * any subsections.
     *
     * @param path           the section path
     *
     * @return the number of documents found
     */
    public int countDocuments(String path) {
        LOG.trace("call to countDocuments: " + path);
        return context.countDocuments(path);
    }

    /**
     * Returns the document corresponding to the specified path.
     *
     * @param path           the document (and section) path
     *
     * @return the document found, or
     *         an empty document if not found
     */
    public DocumentBean findDocument(String path) {
        LOG.trace("call to findDocument: " + path);
        return context.findDocument(path);
    }

    /**
     * Returns all documents in the specified section path. All
     * documents in subsections will also be returned. The documents
     * will be ordered by the publication online date.
     *
     * @param path           the section path
     * @param offset         the number of documents to skip
     * @param count          the maximum number of documents
     *
     * @return a list of the documents found (as document beans)
     */
    public ArrayList findDocuments(String path, int offset, int count) {
        return findDocuments(path, "", offset, count);
    }

    /**
     * Returns all document in the specified section path. All
     * documents in subsections will also be returned. The documents
     * will be ordered by the specified sort order.
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

        LOG.trace("call to findDocuments: " + path + "," + sorting +
                  "," + offset + "," + count);
        return context.findDocuments(path, sorting, offset, count);
    }

    /**
     * Returns the section corresponding to the specified path.
     *
     * @param path           the section path
     *
     * @return the section found, or
     *         an empty section if not found
     */
    public SectionBean findSection(String path) {
        LOG.trace("call to findSection: " + path);
        return context.findSection(path);
    }

    /**
     * Returns the user corresponding to the specified name.
     *
     * @param name           the user name
     *
     * @return the user found, or
     *         an empty user if not found
     */
    public UserBean findUser(String name) {
        LOG.trace("call to findUser: " + name);
        return context.findUser(name);
    }
}
