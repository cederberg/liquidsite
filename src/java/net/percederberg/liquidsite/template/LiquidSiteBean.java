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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.template;

import java.util.ArrayList;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFolder;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentPage;
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.content.ContentSite;
import net.percederberg.liquidsite.content.Domain;

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
     * The request being processed. 
     */
    private Request request;

    /**
     * The content manager to use.
     */
    private ContentManager manager;

    /**
     * The request bean.
     */
    private RequestBean requestBean = null;

    /**
     * The document bean.
     */
    private DocumentBean docBean = null;

    /**
     * The user bean.
     */
    private UserBean userBean = null;

    /**
     * The relative path to the site root directory. The path is
     * relative to the request URL and may be empty if the request
     * refers to an object in the site root directory. Otherwise the
     * path ends with an "/" character. 
     */
    private String sitePath = null;
    
    /**
     * The relative path to the page directory. The path is relative
     * to the request URL and may be empty if the request refers to
     * an object in the page directory. Otherwise the path ends with
     * an "/" character. Note that the page path is NOT always an
     * empty string (consider dynamic pages linked to sections).
     */
    private String pagePath = null;

    /**
     * Creates a new LiquidSite template bean.
     * 
     * @param request        the request object
     * @param manager        the content manager to use
     */
    LiquidSiteBean(Request request, ContentManager manager) {
        this.request = request;
        this.manager = manager;
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
            requestBean = new RequestBean(request);
        }
        return requestBean;
    }

    /**
     * Returns the document bean. 
     *
     * @return the document bean
     */
    public DocumentBean getDoc() {
        if (docBean == null) {
            docBean = new DocumentBean(request.getEnvironment().getDocument());
        }
        return docBean;
    }

    /**
     * Returns the user bean for the current user. 
     *
     * @return the user bean for the current user
     */
    public UserBean getUser() {
        if (userBean == null) {
            userBean = new UserBean(request.getUser());
        }
        return userBean;
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
    private String getSitePath() {
        ContentSite   site;

        if (sitePath == null) {
            site = request.getEnvironment().getSite();
            sitePath = getRelativePath(site.getDirectory());
        }
        return sitePath;
    }
    
    /**
     * Returns the relative path to the page directory. The path is
     * relative to the request URL and may be empty if the request
     * refers directly to an object in the page directory. Otherwise
     * the path ends with an "/" character. Note that the page path 
     * is NOT always an empty string (consider dynamic pages linked
     * to sections).
     * 
     * @return the relative path to the page directory
     */
    private String getPagePath() {
        ContentPage   page;

        if (pagePath == null) {
            page = request.getEnvironment().getPage();
            if (page == null) {
                pagePath = getSitePath();
            } else {
                try {
                    pagePath = getRelativePath(getContentPath(page));
                } catch (ContentException e) {
                    LOG.error(e.getMessage());
                    pagePath = "";
                }
            }
        }
        return pagePath;
    }

    /**
     * Returns the absolute path to a content object.
     *
     * @param content        the content object
     *
     * @return the absolute path to a content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getContentPath(Content content) throws ContentException {
        if (content instanceof ContentSite) {
            return request.getEnvironment().getSite().getDirectory();
        } else if (content instanceof ContentFolder) {
            return getContentPath(content.getParent()) +
                   content.getName() + "/";
        } else if (content != null) {
            return getContentPath(content.getParent());
        } else {
            return null;
        }
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
    private String getRelativePath(String basePath) {
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
        if (path.startsWith("/")) {
            return getSitePath() + path.substring(1);
        } else {
            return getPagePath() + path;
        }
    }

    /**
     * Returns all document in the specified section path.
     * 
     * @param path           the section path
     * 
     * @return a list of the documents found (as document beans)
     */
    public ArrayList findDocuments(String path) {
        ArrayList       result = new ArrayList();
        Domain          domain;
        ContentSection  section;

        // TODO: add sorting (and remove the one that exists)
        // TODO: add filtering
        try {
            domain = request.getEnvironment().getDomain();
            section = findSection(path, domain);
            if (section == null) {
                LOG.error("failed to find section: " + path);
            } else {
                findDocuments(section, result);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }
    
    /**
     * Finds a specified section.
     * 
     * @param path           the section path
     * @param parent         the parent domain or section
     * 
     * @return the section found, or
     *         null if not found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private ContentSection findSection(String path, Object parent)
        throws ContentException {

        Content[]  children;

        if (path == null || path.equals("")) {
            if (parent instanceof ContentSection) {
                return (ContentSection) parent;
            } else {
                return null;
            }
        }
        if (manager == null) {
            return null;
        } else if (parent instanceof Domain) {
            children = manager.getContentChildren(request.getUser(),
                                                  (Domain) parent);
        } else {
            children = manager.getContentChildren(request.getUser(),
                                                  (Content) parent);
        }
        return findSection(path, children);
    }

    /**
     * Finds a specified section.
     * 
     * @param path           the section path
     * @param content        the content objects to search
     * 
     * @return the section found, or
     *         null if not found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private ContentSection findSection(String path, Content[] content) 
        throws ContentException {
        
        String  name;
        int     pos;

        // Find first section name
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

        // Search for section name
        for (int i = 0; i < content.length; i++) {
            if (content[i] instanceof ContentSection
             && content[i].getName().equals(name)) {

                return findSection(path, (ContentSection) content[i]);
            }
        }
        
        return null;
    }

    /**
     * Finds all documents in the specified section (and in 
     * subsections). The documents will be added (as document beans)
     * to the specified list.
     * 
     * @param section        the content section
     * @param results        the list with results
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void findDocuments(ContentSection section, ArrayList results) 
        throws ContentException {

        Content[]  children;

        children = manager.getContentChildren(request.getUser(), section);
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof ContentSection) {
                findDocuments((ContentSection) children[i], results);
            } else if (children[i] instanceof ContentDocument) {
                results.add(new DocumentBean((ContentDocument) children[i]));
            }
        }
    }
}
