/*
 * AdminView.java
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

package net.percederberg.liquidsite.admin.view;

import java.util.ArrayList;
import java.util.HashMap;

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.admin.AdminUtils;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFolder;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.content.ContentSite;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.User;

/**
 * A helper class for creating the HTML and JavaScript output for the
 * administration application.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class AdminView {

    /**
     * The base view class.
     */
    public static final AdminView BASE = new AdminView();

    /**
     * The dialog view helper.
     */
    public static final DialogView DIALOG = new DialogView();

    /**
     * The home view helper.
     */
    public static final HomeView HOME = new HomeView();

    /**
     * The site view helper.
     */
    public static final SiteView SITE = new SiteView();

    /**
     * The content view helper.
     */
    public static final ContentView CONTENT = new ContentView();

    /**
     * The user view helper.
     */
    public static final UserView USER = new UserView();

    /**
     * The system view helper.
     */
    public static final SystemView SYSTEM = new SystemView();

    /**
     * The script view helper.
     */
    public static final ScriptView SCRIPT = new ScriptView();

    /**
     * Creates a new admin view helper.
     */
    AdminView() {
    }

    /**
     * Shows the error message page. When the user presses the 
     * confirmation button on the error page, the browser will be
     * redirected to the specified page.
     * 
     * @param request        the request object
     * @param message        the error message
     * @param page           the redirect page
     */
    public void viewError(Request request, String message, String page) {
        request.setAttribute("error", message);
        request.setAttribute("page", page);
        request.sendTemplate("admin/error.ftl");
    }

    /**
     * Finds all content folders in a site. The folders will not be
     * added directly to the result list, but rather a simplified
     * hash map containing only the id and name of each folder will 
     * be added.
     *
     * @param user           the user
     * @param site           the content site
     * @param exclude        the folder to exclude, or null
     * 
     * @return the list of folders found (in maps)
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ArrayList findFolders(User user,
                                    ContentSite site, 
                                    ContentFolder exclude)
        throws ContentException {

        ArrayList  result = new ArrayList();
        findFolders(user, "", site, exclude, result);
        return result;
    }

    /**
     * Finds all content folders in a site. The folders will not be
     * added directly to the result list, but rather a simplified
     * hash map containing only the id and name of each folder will 
     * be added.
     * 
     * @param user           the user
     * @param baseName       the base name
     * @param parent         the parent site or folder
     * @param exclude        the folder to exclude, or null
     * @param result         the list of folders found (in maps)
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void findFolders(User user, 
                             String baseName,
                             Content parent,
                             ContentFolder exclude,
                             ArrayList result)
        throws ContentException {

        ContentManager  manager = AdminUtils.getContentManager();
        Content[]       children;
        HashMap         values;

        if (parent instanceof ContentSite) {
            baseName = "/";
            values = new HashMap(2);
            values.put("id", String.valueOf(parent.getId()));
            values.put("name", baseName);
            result.add(values);
        } else if (parent instanceof ContentFolder) {
            baseName = baseName + parent.getName() + "/";
            values = new HashMap(2);
            values.put("id", String.valueOf(parent.getId()));
            values.put("name", baseName);
            result.add(values);
        } else {
            return;
        }
        children = manager.getContentChildren(user, parent);
        for (int i = 0; i < children.length; i++) {
            if (!children[i].equals(exclude)) {
                findFolders(user, baseName, children[i], exclude, result);
            }
        }
    }

    /**
     * Finds all content sections in a domain. The sections will not
     * be added directly to the result list, but rather a simplified
     * hash map containing only the id and name of each section will 
     * be added.  
     * 
     * @param user           the user
     * @param domain         the domain
     * @param exclude        the section to exclude, or null
     * 
     * @return the list of sections found (in maps)
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ArrayList findSections(User user,
                                     Domain domain, 
                                     ContentSection exclude)
        throws ContentException {

        ArrayList  result = new ArrayList();
        
        findSections(user, "", domain, exclude, result);
        return result;
    }

    /**
     * Finds all content sections in a domain. The sections will not
     * be added directly to the result list, but rather a simplified
     * hash map containing only the id and name of each section will 
     * be added.  
     * 
     * @param user           the user
     * @param baseName       the base name
     * @param parent         the parent domain or content object
     * @param exclude        the section to exclude, or null
     * @param result         the list of sections found (in maps)
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void findSections(User user, 
                              String baseName,
                              Object parent,
                              ContentSection exclude,
                              ArrayList result)
        throws ContentException {

        ContentManager  manager = AdminUtils.getContentManager();
        Content[]       children;
        HashMap         values;
        String          name;

        if (parent instanceof Domain) {
            children = manager.getContentChildren(user, (Domain) parent);
        } else {
            children = manager.getContentChildren(user, (Content) parent);
        }
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof ContentSection
             && !children[i].equals(exclude)) {

                values = new HashMap(2);
                values.put("id", String.valueOf(children[i].getId()));
                name = baseName + children[i].getName();
                values.put("name", name);
                result.add(values);
                name += "/";
                findSections(user, name, children[i], exclude, result);
            }
        }
    }
}
