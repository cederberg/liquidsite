/*
 * UserView.java
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

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.admin.AdminUtils;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Group;
import net.percederberg.liquidsite.content.User;

/**
 * A helper class for the user view. This class contains methods 
 * for creating the HTML responses to the pages in the user view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class UserView extends AdminView {

    /**
     * The maximum number of users or groups on a page. 
     */
    private static final int PAGE_SIZE = 25;

    /**
     * Creates a new user view helper.
     */
    UserView() {
    }

    /**
     * Shows the users page.
     *
     * @param request        the request object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the 
     *             required permissions 
     */
    public void viewUsers(Request request)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        User            user = request.getUser();
        Domain          domain = user.getDomain();
        String          filter;
        ArrayList       users = null;
        ArrayList       groups = null;
        int             page;
        int             count;
        String          str;

        // Find users or groups
        str = request.getParameter("domain");
        if (str != null) {
            domain = manager.getDomain(user, str);
        }
        filter = request.getParameter("filter", "");
        str = request.getParameter("page", "1");
        try {
            page = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            page = 1;
        }
        str = request.getParameter("type");
        if (str == null || !str.equals("group")) {
            users = findUsers(user, domain, filter, page);
            count = manager.getUserCount(domain, filter);
        } else {
            groups = findGroups(user, domain, filter);
            count = 0;
        }

        // Set request parameters
        request.setAttribute("enableDomains", user.isSuperUser());
        if (domain == null) {
            request.setAttribute("domain", "");
        } else {
            request.setAttribute("domain", domain.getName());
        }
        request.setAttribute("domains", findDomains(user));
        request.setAttribute("filter", filter);
        request.setAttribute("users", users);        
        request.setAttribute("groups", groups);
        request.setAttribute("page", page);
        if (count == 0) {
            request.setAttribute("pages", 1);
        } else if (count % PAGE_SIZE == 0) {
            request.setAttribute("pages", count / PAGE_SIZE);
        } else {
            request.setAttribute("pages", count / PAGE_SIZE + 1);
        }
        request.sendTemplate("admin/users.ftl");
    }

    /**
     * Finds all domains readable by a user. The domains will not be
     * added directly to the result list, but rather the domain 
     * names.
     *
     * @param user           the user
     *
     * @return a list of domains found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private ArrayList findDomains(User user)
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
     * Finds the matching users in a domain. The users will not be
     * added directly to the result list, but rather a simplified
     * hash map containing only certain properties will be added.
     *
     * @param user           the user
     * @param domain         the domain
     * @param filter         the search filter (empty for all)
     * @param page           the page to return (>= 1)
     *
     * @return the list of users found (in maps)
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user isn't allowed to
     *             list users in the domain
     */
    private ArrayList findUsers(User user, 
                                Domain domain, 
                                String filter, 
                                int page)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        ArrayList       result = new ArrayList();
        User[]          users;
        HashMap         map;
        int             start = (page - 1) * PAGE_SIZE;

        users = manager.getUsers(user, domain, filter, start, PAGE_SIZE);
        for (int i = 0; i < users.length; i++) {
            map = new HashMap(5);
            map.put("name", users[i].getName());
            map.put("realName", users[i].getRealName());
            map.put("email", users[i].getEmail());
            map.put("comment", users[i].getComment());
            result.add(map);
        }
        return result;
    }

    /**
     * Finds the matching groups in a domain. The groups will not be
     * added directly to the result list, but rather a simplified
     * hash map containing only certain properties will be added.
     *
     * @param user           the user
     * @param domain         the domain
     * @param filter         the search filter (empty for all)
     *
     * @return the list of groups found (in maps)
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user isn't allowed to
     *             list users in the domain
     */
    private ArrayList findGroups(User user, 
                                 Domain domain, 
                                 String filter)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        ArrayList       result = new ArrayList();
        Group[]         groups;
        HashMap         map;

        groups = manager.getGroups(user, domain, filter);
        for (int i = 0; i < groups.length; i++) {
            map = new HashMap(4);
            map.put("name", groups[i].getName());
            map.put("description", groups[i].getDescription());
            map.put("comment", groups[i].getComment());
            result.add(map);
        }
        return result;
    }
}
