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
import java.util.Iterator;

import net.percederberg.liquidsite.admin.AdminUtils;

import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.Group;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.Request;
import org.liquidsite.core.web.RequestSession;

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
        // Nothing to initialize
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
        RequestSession  session = request.getSession();
        User            user = request.getUser();
        Domain          domain = null;
        String          filter;
        ArrayList       users = null;
        ArrayList       groups = null;
        int             page;
        int             count;
        String          str;

        // Find users or groups
        str = (String) session.getAttribute("users.list.domain",
                                            user.getDomainName());
        str = request.getParameter("domain", str);
        session.setAttribute("users.list.domain", str);
        if (!str.equals("")) {
            domain = manager.getDomain(user, str);
        }
        str = (String) session.getAttribute("users.list.filter", "");
        filter = request.getParameter("filter", str);
        session.setAttribute("users.list.filter", filter);
        str = request.getParameter("page", "1");
        try {
            page = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            page = 1;
        }
        str = (String) session.getAttribute("users.list.type", "user");
        str = request.getParameter("type", str);
        if (!str.equals("group")) {
            session.setAttribute("users.list.type", "user");
            users = findUsers(domain, filter, page);
            count = manager.getUserCount(domain, filter);
        } else {
            session.setAttribute("users.list.type", "group");
            groups = findGroups(domain, filter);
            count = manager.getUserCount(domain, "");
        }

        // Set request parameters
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
        if (count == 0 || groups != null) {
            request.setAttribute("pages", 1);
        } else if (count % PAGE_SIZE == 0) {
            request.setAttribute("pages", count / PAGE_SIZE);
        } else {
            request.setAttribute("pages", count / PAGE_SIZE + 1);
        }
        request.setAttribute("userCount", count);
        request.sendTemplate("admin/users.ftl");
    }

    /**
     * Shows the view group detail page.
     *
     * @param request        the request object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    public void viewGroup(Request request)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        Domain          domain = null;
        Group           group;
        ArrayList       users = null;
        int             page;
        int             count;
        String          str;

        // Find users or groups
        str = request.getParameter("domain", "");
        if (!str.equals("")) {
            domain = manager.getDomain(request.getUser(), str);
        }
        str = request.getParameter("page", "1");
        try {
            page = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            page = 1;
        }
        group = manager.getGroup(domain, request.getParameter("name"));
        users = findUsers(group, page);
        count = group.getUserCount();

        // Set request parameters
        request.setAttribute("domain", group.getDomainName());
        request.setAttribute("name", group.getName());
        request.setAttribute("users", users);
        request.setAttribute("page", page);
        if (count == 0) {
            request.setAttribute("pages", 1);
        } else if (count % PAGE_SIZE == 0) {
            request.setAttribute("pages", count / PAGE_SIZE);
        } else {
            request.setAttribute("pages", count / PAGE_SIZE + 1);
        }
        request.setAttribute("userCount", count);
        request.sendTemplate("admin/view-group.ftl");
    }

    /**
     * Shows the add or edit user page.
     *
     * @param request        the request object
     * @param domain         the user domain
     * @param user           the user to edit, or null to add new
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewEditUser(Request request, Domain domain, User user)
        throws ContentException {

        String     defaultName;
        boolean    defaultEnabled;
        String     defaultRealName;
        String     defaultEmail;
        String     defaultComment;
        Group[]    groups;
        HashMap    memberships = new HashMap();
        ArrayList  list;
        Iterator   iter;
        String     str;

        // Find default values
        if (user != null) {
            defaultName = user.getName();
            defaultEnabled = user.getEnabled();
            defaultRealName = user.getRealName();
            defaultEmail = user.getEmail();
            defaultComment = user.getComment();
        } else {
            defaultName = "";
            defaultEnabled = true;
            defaultRealName = "";
            defaultEmail = "";
            defaultComment = "";
        }

        // Set request parameters
        if (domain == null) {
            request.setAttribute("domain", "");
        } else {
            request.setAttribute("domain", domain.getName());
        }
        str = request.getParameter("name", defaultName);
        request.setAttribute("name", str);
        str = request.getParameter("password", "");
        request.setAttribute("password", str);
        str = request.getParameter("realname", defaultRealName);
        request.setAttribute("realname", str);
        str = request.getParameter("email", defaultEmail);
        request.setAttribute("email", str);
        str = request.getParameter("comment", defaultComment);
        request.setAttribute("comment", str);
        list = findGroups(domain, "");
        request.setAttribute("groups", list);
        if (request.getParameter("comment") == null) {
            request.setAttribute("enabled", String.valueOf(defaultEnabled));
            if (user != null) {
                groups = user.getGroups();
                for (int i = 0; i < groups.length; i++) {
                    memberships.put(groups[i].getName(), "true");
                }
            }
        } else {
            if (request.getParameter("enabled") != null) {
                request.setAttribute("enabled", "true");
            } else {
                request.setAttribute("enabled", "false");
            }
            iter = request.getAllParameters().keySet().iterator();
            while (iter.hasNext()) {
                str = iter.next().toString();
                if (str.startsWith("member")) {
                    memberships.put(request.getParameter(str), "true");
                }
            }
        }
        request.setAttribute("memberships", memberships);
        request.sendTemplate("admin/edit-user.ftl");
    }

    /**
     * Shows the add or edit group page.
     *
     * @param request        the request object
     * @param group          the group to edit, or null to add new
     */
    public void viewEditGroup(Request request, Group group) {
        String  defaultName;
        String  defaultDescription;
        String  defaultComment;
        String  str;

        // Find default values
        if (group != null) {
            defaultName = group.getName();
            defaultDescription = group.getDescription();
            defaultComment = group.getComment();
        } else {
            defaultName = "";
            defaultDescription = "";
            defaultComment = "";
        }

        // Set request parameters
        str = request.getParameter("domain", "");
        request.setAttribute("domain", str);
        str = request.getParameter("name", defaultName);
        request.setAttribute("name", str);
        str = request.getParameter("description", defaultDescription);
        request.setAttribute("description", str);
        str = request.getParameter("comment", defaultComment);
        request.setAttribute("comment", str);
        request.sendTemplate("admin/edit-group.ftl");
    }

    /**
     * Finds the matching users in a domain. The users will not be
     * added directly to the result list, but rather a simplified
     * hash map containing only certain properties will be added.
     *
     * @param domain         the domain
     * @param filter         the search filter (empty for all)
     * @param page           the page to return (>= 1)
     *
     * @return the list of users found (in maps)
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private ArrayList findUsers(Domain domain, String filter, int page)
        throws ContentException {

        ContentManager  manager = AdminUtils.getContentManager();
        ArrayList       result = new ArrayList();
        User[]          users;
        HashMap         map;
        int             start = (page - 1) * PAGE_SIZE;

        users = manager.getUsers(domain, filter, start, PAGE_SIZE);
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
     * Finds the users in a group. The users will not be added
     * directly to the result list, but rather a simplified hash map
     * containing only certain properties will be added.
     *
     * @param group          the group
     * @param page           the page to return (>= 1)
     *
     * @return the list of users found (in maps)
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private ArrayList findUsers(Group group,
                                int page)
        throws ContentException {

        ArrayList  result = new ArrayList();
        User[]     users;
        HashMap    map;
        int        start = (page - 1) * PAGE_SIZE;

        users = group.getUsers(start, PAGE_SIZE);
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
}
