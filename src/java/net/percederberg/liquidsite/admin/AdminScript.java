/*
 * AdminScript.java
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

package net.percederberg.liquidsite.admin;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.Domain;
import net.percederberg.liquidsite.content.Lock;
import net.percederberg.liquidsite.content.Permission;
import net.percederberg.liquidsite.content.Site;
import net.percederberg.liquidsite.content.User;

/**
 * A helper class for creating JavaScript code to the administration 
 * application.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class AdminScript {

    /**
     * The date format used by this class.
     */
    private static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * Creates a new admin script helper.
     */
    public AdminScript() {
    }

    /**
     * Returns the JavaScript for selecting an item in a tree view.
     * 
     * @param type           the item type
     * @param id             the item id
     * 
     * @return the JavaScript for selecting the item
     */
    public String getTreeViewSelect(Object type, Object id) {
        StringBuffer  buffer = new StringBuffer();

        buffer.append("treeSelect('");
        buffer.append(type);
        buffer.append("', '");
        buffer.append(id);
        buffer.append("');\n");
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting a tree view.
     * 
     * @param domains        the root domain objects
     * 
     * @return the JavaScript for presenting a tree view
     */
    public String getTreeView(Domain[] domains) {

        StringBuffer  buffer = new StringBuffer();
        
        for (int i = 0; i < domains.length; i++) {
            buffer.append("treeAddItem(0, '");
            buffer.append(domains[i].getName());
            buffer.append("', 'domain', '");
            buffer.append(domains[i].getName());
            buffer.append("', '");
            buffer.append(domains[i].getDescription());
            buffer.append("', 1);\n");
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting a tree view.
     * 
     * @param domain         the domain object
     * @param children       the child content objects
     * 
     * @return the JavaScript for presenting a tree view
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public String getTreeView(Domain domain, Content[] children) 
        throws ContentException {

        StringBuffer    buffer = new StringBuffer();

        buffer.append("treeAddContainer('");
        buffer.append(domain.getName());
        buffer.append("');\n");
        for (int i = 0; i < children.length; i++) {
            buffer.append("treeAddItem('");
            buffer.append(domain.getName());
            buffer.append("', ");
            buffer.append(children[i].getId());
            buffer.append(", '");
            buffer.append(getContentCategory(children[i]));
            buffer.append("', '");
            buffer.append(children[i].getName());
            buffer.append("', '");
            buffer.append(children[i].toString());
            buffer.append("', ");
            buffer.append(getContentStatus(children[i]));
            buffer.append(");\n");
        }
        buffer.append("treeOpen('domain', '");
        buffer.append(domain.getName());
        buffer.append("');\n");
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting a tree view.
     * 
     * @param parent         the parent content object
     * @param children       the child content objects
     * 
     * @return the JavaScript for presenting a tree view
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public String getTreeView(Content parent, Content[] children) 
        throws ContentException {

        StringBuffer    buffer = new StringBuffer();

        buffer.append("treeAddContainer(");
        buffer.append(parent.getId());
        buffer.append(");\n");
        for (int i = 0; i < children.length; i++) {
            buffer.append("treeAddItem(");
            buffer.append(parent.getId());
            buffer.append(", ");
            buffer.append(children[i].getId());
            buffer.append(", '");
            buffer.append(getContentCategory(children[i]));
            buffer.append("', '");
            buffer.append(children[i].getName());
            buffer.append("', '");
            buffer.append(children[i].toString());
            buffer.append("', ");
            buffer.append(getContentStatus(children[i]));
            buffer.append(");\n");
        }
        buffer.append("treeOpen('");
        buffer.append(getContentCategory(parent));
        buffer.append("', ");
        buffer.append(parent.getId());
        buffer.append(");\n");
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting an object view.
     * 
     * @param user           the current user
     * @param domain         the domain object
     * 
     * @return the JavaScript for presenting an object view
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public String getObjectView(User user, Domain domain) 
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();

        buffer.append("objectShow('domain', '");
        buffer.append(domain.getName());
        buffer.append("', '");
        buffer.append(domain.getName());
        buffer.append("');\n");
        buffer.append("objectAddProperty('Description', '");
        buffer.append(domain.getDescription());
        buffer.append("');\n");
        buffer.append(getButtons(user, domain));
        buffer.append(getPermissions(domain));
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting an object view.
     * 
     * @param user           the current user
     * @param content        the content object
     * 
     * @return the JavaScript for presenting an object view
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public String getObjectView(User user, Content content) 
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();

        buffer.append("objectShow('");
        buffer.append(getContentCategory(content));
        buffer.append("', ");
        buffer.append(content.getId());
        buffer.append(", '");
        buffer.append(content.getName());
        buffer.append("');\n");
        buffer.append("objectAddUrlProperty('");
        buffer.append(getContentUrl(content));
        buffer.append("');\n");
        buffer.append("objectAddOnlineProperty(");
        buffer.append(getDate(content.getOnlineDate()));
        buffer.append(", ");
        buffer.append(getDate(content.getOfflineDate()));
        buffer.append(");\n");
        buffer.append("objectAddStatusProperty(");
        buffer.append(getContentStatus(content));
        buffer.append(", ");
        buffer.append(getLock(content.getLock()));
        buffer.append(");\n");
        buffer.append(getButtons(user, content));
        buffer.append(getPermissions(content));
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting domain buttons.
     * 
     * @param user           the current user
     * @param domain         the domain object
     * 
     * @return the JavaScript for presenting domain buttons
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getButtons(User user, Domain domain) 
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();
        
        if (domain.hasWriteAccess(user)) {
            buffer.append("objectAddNewButton('add-site.html');\n");
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting domain buttons.
     * 
     * @param user           the current user
     * @param content        the content object
     * 
     * @return the JavaScript for presenting domain buttons
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getButtons(User user, Content content) 
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();
        
        if (content.hasWriteAccess(user)) {
            buffer.append("objectAddNewButton('add-site.html');\n");
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting domain permissions.
     * 
     * @param domain         the domain object
     * 
     * @return the JavaScript for presenting domain permissions
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getPermissions(Domain domain) 
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();
        Permission[]  permissions;
        
        permissions = domain.getPermissions();
        if (permissions.length == 0) {
            buffer.append(getPermission(null, true));
        }
        for (int i = 0; i < permissions.length; i++) {
            buffer.append(getPermission(permissions[i], false));
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting content permissions.
     * 
     * @param content        the content object
     * 
     * @return the JavaScript for presenting content permissions
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getPermissions(Content content) 
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();
        Permission[]  permissions;
        Content       parent = content;
        boolean       inherited = false;
        
        // Find permissions
        permissions = content.getPermissions();
        while (permissions.length == 0 && parent != null) {
            inherited = true;
            parent = parent.getParent();
            if (parent != null) {
                permissions = parent.getPermissions();
            }
        }
        if (parent == null) {
            return getPermissions(content.getDomain());
        }

        // Create permission script
        for (int i = 0; i < permissions.length; i++) {
            buffer.append(getPermission(permissions[i], inherited));
        }

        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting a permission.
     * 
     * @param perm           the permission object, or null
     * @param inherited      the inherited flag
     * 
     * @return the JavaScript for presenting a permission
     */
    private String getPermission(Permission perm, boolean inherited) {
        StringBuffer  buffer = new StringBuffer();
        
        buffer.append("objectAddPermission(");
        if (perm == null) {
            buffer.append("null, null, false, false, false, false");
        } else {
            buffer.append(getString(perm.getUserName()));
            buffer.append(", ");
            buffer.append(getString(perm.getGroupName()));
            buffer.append(", ");
            buffer.append(perm.getRead());
            buffer.append(", ");
            buffer.append(perm.getWrite());
            buffer.append(", ");
            buffer.append(perm.getPublish());
            buffer.append(", ");
            buffer.append(perm.getAdmin());
        }
        buffer.append(", ");
        buffer.append(!inherited);
        buffer.append(");\n");

        return buffer.toString();
    }

    /**
     * Returns the JavaScript representation of a content category.
     * 
     * @param content        the content object
     * 
     * @return the JavaScript representation of a content category
     */
    private String getContentCategory(Content content) {
        switch (content.getCategory()) {
        case Content.SITE_CATEGORY:
            return "site";
        default:
            return "";
        }
    }

    /**
     * Returns the JavaScript representation of a content URL.
     * 
     * @param content        the content object
     * 
     * @return the JavaScript representation of a content URL
     */
    private String getContentUrl(Content content) {
        if (content instanceof Site) {
            return content.toString();
        } else {
            return "N/A";
        }
    }

    /**
     * Returns the JavaScript representation of a content status.
     * 
     * @param content        the content object
     * 
     * @return the JavaScript representation of a content status
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private int getContentStatus(Content content) 
        throws ContentException {

        if (!content.isOnline()) {
            return 0;
        } else if (content.getRevision(0) != null) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * Returns the JavaScript representation of a lock object.
     * 
     * @param lock           the lock object, or null
     * 
     * @return the JavaScript representation of a lock object
     */
    private String getLock(Lock lock) {
        if (lock == null) {
            return "null";
        } else {
            return "'" + lock.getUserName() + "'";
        }
    }

    /**
     * Returns the JavaScript representation of a date.
     * 
     * @param date           the date to present, or null
     * 
     * @return a JavaScript representation of the date
     */
    private String getDate(Date date) {
        if (date == null) {
            return "null";
        } else {
            return "'" + DATE_FORMAT.format(date) + "'"; 
        }
    }

    /**
     * Returns the JavaScript representation of a string. This method
     * will present empty strings as null.
     * 
     * @param str            the string to present, or null
     * 
     * @return a JavaScript representation of the string
     */
    private String getString(String str) {
        if (str == null || str.equals("")) {
            return "null";
        } else {
            return "'" + str + "'";
        }
    }
}