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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.admin.view;

import java.util.ArrayList;
import java.util.HashMap;

import net.percederberg.liquidsite.admin.AdminUtils;

import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentFolder;
import org.liquidsite.core.content.ContentForum;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentSection;
import org.liquidsite.core.content.ContentSelector;
import org.liquidsite.core.content.ContentSite;
import org.liquidsite.core.content.ContentTemplate;
import org.liquidsite.core.content.ContentTranslator;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.Group;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.Request;

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
        // Nothing to initialize
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
     * Shows the information message page. When the user presses the
     * confirmation button on the page, the browser will be
     * redirected to the specified page.
     *
     * @param request        the request object
     * @param message        the information message
     * @param page           the redirect page
     */
    public void viewInfo(Request request, String message, String page) {
        request.setAttribute("message", message);
        request.setAttribute("page", page);
        request.sendTemplate("admin/info.ftl");
    }

    /**
     * Shows the redirect page. This page is sometimes needed to
     * compensate for erroneous 403 handling by some browsers (i.e.
     * Internet Explorer).
     *
     * @param request        the request object
     * @param url            the destination URL
     */
    public void viewRedirect(Request request, String url) {
        request.setAttribute("url", url);
        request.sendTemplate("admin/redirect.ftl");
    }

    /**
     * Finds all domains readable by a user. The domains will not be
     * added directly to the result list, but rather only the domain
     * names.
     *
     * @param user           the user
     *
     * @return a list of domains found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ArrayList findDomains(User user)
        throws ContentException {

        ContentManager  manager = AdminUtils.getContentManager();
        ArrayList       result = new ArrayList();
        Domain[]        domains;

        domains =  manager.getDomains(user);
        for (int i = 0; i < domains.length; i++) {
            result.add(domains[i].getName());
        }
        return result;
    }

    /**
     * Finds all content folders in a site. The folders will not be
     * added directly to the result list, but rather a simplified hash
     * map containing only the id and name of each folder will be
     * added. This method will return the site as the base folder and
     * may also include translators if the corresponding flag is set.
     *
     * @param user           the user
     * @param site           the content site
     * @param exclude        the folder to exclude, or null
     * @param translators    the include translators flag
     *
     * @return the list of folders found (in maps)
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ArrayList findFolders(User user,
                                    ContentSite site,
                                    ContentFolder exclude,
                                    boolean translators)
        throws ContentException {

        ArrayList  result = new ArrayList();
        findFolders(user, "", site, exclude, translators, result);
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
     * @param translators    the include translators flag
     * @param result         the list of folders found (in maps)
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void findFolders(User user,
                             String baseName,
                             Content parent,
                             ContentFolder exclude,
                             boolean translators,
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
        } else if (translators && parent instanceof ContentTranslator) {
            baseName = baseName + "*/ (" + parent.getName() + ")";
            values = new HashMap(2);
            values.put("id", String.valueOf(parent.getId()));
            values.put("name", baseName);
            result.add(values);
            return;
        } else {
            return;
        }
        children = manager.getContentChildren(user, parent);
        for (int i = 0; i < children.length; i++) {
            if (!children[i].equals(exclude)) {
                findFolders(user,
                            baseName,
                            children[i],
                            exclude,
                            translators,
                            result);
            }
        }
    }

    /**
     * Finds all content templates in a domain. The templates will
     * not be added directly to the result list, but rather a
     * simplified hash map containing only the id and name of each
     * template will be added.
     *
     * @param user           the user
     * @param domain         the domain
     * @param exclude        the template to exclude, or null
     *
     * @return the list of templates found (in maps)
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ArrayList findTemplates(User user,
                                      Domain domain,
                                      ContentTemplate exclude)
        throws ContentException {

        ArrayList  result = new ArrayList();

        findTemplates(user, "", domain, exclude, result);
        return result;
    }

    /**
     * Finds all content templates in a domain. The templates will
     * not be added directly to the result list, but rather a
     * simplified hash map containing only the id and name of each
     * template will be added.
     *
     * @param user           the user
     * @param baseName       the base name
     * @param parent         the parent domain or content object
     * @param exclude        the template to exclude, or null
     * @param result         the list of templates found (in maps)
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void findTemplates(User user,
                               String baseName,
                               Object parent,
                               ContentTemplate exclude,
                               ArrayList result)
        throws ContentException {

        ContentManager   manager = AdminUtils.getContentManager();
        Content[]        children;
        ContentTemplate  template;
        HashMap          values;

        if (parent instanceof Domain) {
            children = manager.getContentChildren(user,
                                                  (Domain) parent,
                                                  Content.TEMPLATE_CATEGORY);
        } else if (parent instanceof ContentTemplate) {
            template = (ContentTemplate) parent;
            baseName += template.getName();
            values = new HashMap(2);
            values.put("id", String.valueOf(template.getId()));
            values.put("name", baseName);
            result.add(values);
            baseName += "/";
            children = manager.getContentChildren(user,
                                                  template,
                                                  Content.TEMPLATE_CATEGORY);
        } else {
            return;
        }
        for (int i = 0; i < children.length; i++) {
            if (!children[i].equals(exclude)) {
                findTemplates(user, baseName, children[i], exclude, result);
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
        ContentSection  section;
        HashMap         values;

        if (parent instanceof Domain) {
            children = manager.getContentChildren(user,
                                                  (Domain) parent,
                                                  Content.SECTION_CATEGORY);
        } else if (parent instanceof ContentSection) {
            section = (ContentSection) parent;
            baseName += section.getName();
            values = new HashMap(2);
            values.put("id", String.valueOf(section.getId()));
            values.put("name", baseName);
            result.add(values);
            baseName += "/";
            children = manager.getContentChildren(user,
                                                  section,
                                                  Content.SECTION_CATEGORY);
        } else {
            return;
        }
        for (int i = 0; i < children.length; i++) {
            if (!children[i].equals(exclude)) {
                findSections(user, baseName, children[i], exclude, result);
            }
        }
    }

    /**
     * Finds all content forums in a domain. The forums will not
     * be added directly to the result list, but rather a simplified
     * hash map containing only the id and name of each forum will
     * be added.
     *
     * @param user           the user
     * @param domain         the domain
     *
     * @return the list of sections found (in maps)
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ArrayList findForums(User user, Domain domain)
        throws ContentException {

        ContentManager   manager = AdminUtils.getContentManager();
        ContentSelector  selector;
        Content[]        forums;
        ContentForum     forum;
        HashMap          values;
        ArrayList        result = new ArrayList();

        selector = new ContentSelector(domain);
        selector.requireCategory(Content.FORUM_CATEGORY);
        selector.sortByName(true);
        forums = manager.getContentObjects(user, selector);
        for (int i = 0; i < forums.length; i++) {
            forum = (ContentForum) forums[i];
            values = new HashMap(2);
            values.put("id", String.valueOf(forum.getId()));
            values.put("name", forum.getRealName());
            result.add(values);
        }
        return result;
    }

    /**
     * Finds the matching groups in a domain. The groups will not be
     * added directly to the result list, but rather a simplified
     * hash map containing only certain properties will be added.
     *
     * @param domain         the domain
     * @param filter         the search filter (empty for all)
     *
     * @return the list of groups found (in maps)
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ArrayList findGroups(Domain domain, String filter)
        throws ContentException {

        ContentManager  manager = AdminUtils.getContentManager();
        ArrayList       result = new ArrayList();
        Group[]         groups;
        HashMap         map;

        groups = manager.getGroups(domain, filter);
        for (int i = 0; i < groups.length; i++) {
            map = new HashMap(4);
            map.put("name", groups[i].getName());
            map.put("description", groups[i].getDescription());
            map.put("comment", groups[i].getComment());
            map.put("members", String.valueOf(groups[i].getUserCount()));
            result.add(map);
        }
        return result;
    }
}
