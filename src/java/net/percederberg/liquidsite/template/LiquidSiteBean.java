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

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSection;
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
     * The request being processed. 
     */
    private Request request;

    /**
     * The content manager to use.
     */
    private ContentManager manager;

    /**
     * The site bean.
     */
    private SiteBean site;

    /**
     * The page bean.
     */
    private PageBean page;

    /**
     * The user bean.
     */
    private UserBean user;

    /**
     * Creates a new LiquidSite template bean.
     * 
     * @param request        the request object
     * @param manager        the content manager to use
     */
    LiquidSiteBean(Request request, ContentManager manager) {
        this.request = request;
        this.manager = manager;
        this.site = new SiteBean(request);
        this.page = new PageBean(request);
        this.user = new UserBean(request.getUser());
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
     * Returns the site bean for the current site. 
     * 
     * @return the site bean for the current site
     */
    public SiteBean getSite() {
        return site;
    }
    
    /**
     * Returns the page bean for the current page. 
     * 
     * @return the page bean for the current page
     */
    public PageBean getPage() {
        return page;
    }

    /**
     * Returns the user bean for the currently logged in user. 
     * 
     * @return the user bean for the currently logged in user
     */
    public UserBean getUser() {
        return user;
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
        // TODO: add content publish checks (online)
        try {
            domain = request.getSite().getDomain();
            section = findSection(path, domain);
            if (section == null) {
                // TODO: log this
            } else {
                findDocuments(section, result);
            }
        } catch (ContentException e) {
            // TODO: log this!
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
