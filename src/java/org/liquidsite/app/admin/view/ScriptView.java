/*
 * ScriptView.java
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

package org.liquidsite.app.admin.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.liquidsite.app.admin.AdminUtils;
import org.liquidsite.app.servlet.Application;
import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentDocument;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentFile;
import org.liquidsite.core.content.ContentFolder;
import org.liquidsite.core.content.ContentForum;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentPage;
import org.liquidsite.core.content.ContentSection;
import org.liquidsite.core.content.ContentSite;
import org.liquidsite.core.content.ContentTemplate;
import org.liquidsite.core.content.ContentTopic;
import org.liquidsite.core.content.ContentTranslator;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.DomainHost;
import org.liquidsite.core.content.Lock;
import org.liquidsite.core.content.Permission;
import org.liquidsite.core.content.PermissionList;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.Request;

/**
 * A helper class for creating JavaScript code to the administration
 * application.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ScriptView {

    /**
     * Creates a new admin script helper.
     */
    ScriptView() {
        // Nothing to initialize
    }

    /**
     * Sends a page reload script.
     *
     * @param request        the request object
     */
    public void viewReload(Request request) {
        request.sendData("text/javascript",
                         "var win = window;\n" +
                         "if (win.parent) {\n" +
                         "    win = win.parent;\n" +
                         "}\n" +
                         "win.location.reload(1);\n");
    }

    /**
     * Returns the JavaScript for selecting an item in a tree view.
     *
     * @param domain         the domain object to select
     *
     * @return the JavaScript for selecting the item
     */
    public String getTreeViewSelect(Domain domain) {
        StringBuffer  buffer = new StringBuffer();

        buffer.append("treeSelect('domain', '");
        buffer.append(domain.getName());
        buffer.append("');\n");
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for selecting an item in a tree view.
     *
     * @param content        the content object to select
     *
     * @return the JavaScript for selecting the item
     */
    public String getTreeViewSelect(Content content) {
        StringBuffer  buffer = new StringBuffer();

        buffer.append("treeSelect('");
        buffer.append(AdminUtils.getCategory(content));
        buffer.append("', ");
        buffer.append(content.getId());
        buffer.append(");\n");
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
        String        str;

        for (int i = 0; i < domains.length; i++) {
            buffer.append("treeAddItem(0, '");
            buffer.append(domains[i].getName());
            buffer.append("', 'domain', '");
            buffer.append(domains[i].getName());
            buffer.append("', ");
            str = domains[i].getDescription();
            buffer.append(AdminUtils.getScriptString(str));
            buffer.append(", 1);\n");
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting a tree view.
     *
     * @param domain         the domain object
     * @param children       the child content objects
     * @param open           the open (domain) object flag
     *
     * @return the JavaScript for presenting a tree view
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public String getTreeView(Domain domain,
                              Content[] children,
                              boolean open)
        throws ContentException {

        StringBuffer    buffer = new StringBuffer();
        String          str;

        buffer.append("treeAddContainer('");
        buffer.append(domain.getName());
        buffer.append("');\n");
        for (int i = 0; i < children.length; i++) {
            buffer.append("treeAddItem('");
            buffer.append(domain.getName());
            buffer.append("', ");
            buffer.append(children[i].getId());
            buffer.append(", '");
            buffer.append(AdminUtils.getCategory(children[i]));
            buffer.append("', ");
            str = children[i].getName();
            buffer.append(AdminUtils.getScriptString(str));
            buffer.append(", ");
            str = children[i].toString();
            buffer.append(AdminUtils.getScriptString(str));
            buffer.append(", ");
            buffer.append(getContentStatus(children[i]));
            buffer.append(");\n");
        }
        if (open) {
            buffer.append("treeOpen('domain', '");
            buffer.append(domain.getName());
            buffer.append("');\n");
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting a tree view.
     *
     * @param parent         the parent content object
     * @param children       the child content objects
     * @param open           the open content object flag
     *
     * @return the JavaScript for presenting a tree view
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public String getTreeView(Content parent,
                              Content[] children,
                              boolean open)
        throws ContentException {

        StringBuffer    buffer = new StringBuffer();
        String          str;

        buffer.append("treeAddContainer(");
        buffer.append(parent.getId());
        buffer.append(");\n");
        for (int i = 0; i < children.length; i++) {
            buffer.append("treeAddItem(");
            buffer.append(parent.getId());
            buffer.append(", ");
            buffer.append(children[i].getId());
            buffer.append(", '");
            buffer.append(AdminUtils.getCategory(children[i]));
            buffer.append("', ");
            str = children[i].getName();
            buffer.append(AdminUtils.getScriptString(str));
            buffer.append(", ");
            str = children[i].toString();
            buffer.append(AdminUtils.getScriptString(str));
            buffer.append(", ");
            buffer.append(getContentStatus(children[i]));
            buffer.append(");\n");
        }
        if (open) {
            buffer.append("treeOpen('");
            buffer.append(AdminUtils.getCategory(parent));
            buffer.append("', ");
            buffer.append(parent.getId());
            buffer.append(");\n");
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting an object view.
     *
     * @param user           the current user
     * @param domain         the domain object
     * @param view           the view name
     *
     * @return the JavaScript for presenting an object view
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public String getObjectView(User user, Domain domain, String view)
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();

        buffer.append("objectShow('domain', '");
        buffer.append(domain.getName());
        buffer.append("', '");
        buffer.append(domain.getName());
        buffer.append("');\n");
        buffer.append("objectAddProperty('Description', ");
        buffer.append(AdminUtils.getScriptString(domain.getDescription()));
        buffer.append(");\n");
        buffer.append("objectAddProperty('Created', '");
        buffer.append(AdminUtils.formatDate(user, domain.getCreatedDate()));
        buffer.append(" (last modified ");
        buffer.append(AdminUtils.formatDate(user, domain.getModifiedDate()));
        buffer.append(")');\n");
        buffer.append(getButtons(user, domain, view));
        buffer.append(getHosts(domain));
        buffer.append(getPermissions(domain));
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting an object view.
     *
     * @param user           the current user
     * @param content        the content object
     * @param view           the view name
     *
     * @return the JavaScript for presenting an object view
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public String getObjectView(User user, Content content, String view)
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();
        int           status = getContentStatus(content);
        Lock          lock = content.getLock();
        Date          date;

        buffer.append("objectShow('");
        buffer.append(AdminUtils.getCategory(content));
        buffer.append("', ");
        buffer.append(content.getId());
        buffer.append(", ");
        buffer.append(AdminUtils.getScriptString(content.getName()));
        buffer.append(");\n");
        buffer.append("objectAddUrlProperty(");
        buffer.append(AdminUtils.getScriptString(getContentUrl(content)));
        buffer.append(");\n");
        buffer.append("objectAddOnlineProperty(");
        date = content.getOnlineDate();
        buffer.append(AdminUtils.getScriptDate(user, date));
        buffer.append(", ");
        date = content.getOfflineDate();
        buffer.append(AdminUtils.getScriptDate(user, date));
        buffer.append(");\n");
        buffer.append("objectAddStatusProperty(");
        buffer.append(status);
        buffer.append(", ");
        buffer.append(getLock(user, lock));
        buffer.append(");\n");
        if (content instanceof ContentSite) {
            buffer.append(getSiteProperties((ContentSite) content));
        } else if (content instanceof ContentFile) {
            buffer.append(getFileProperties((ContentFile) content));
        }
        buffer.append(getButtons(user, content, view, status, lock));
        buffer.append(getRevisions(user, content));
        buffer.append(getPermissions(content));
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting special site properties.
     *
     * @param site           the site
     *
     * @return the JavaScript for additional site properties
     */
    private String getSiteProperties(ContentSite site) {
        if (site.isAdmin()) {
            return "objectAddProperty('Note', 'Administration Site');\n";
        } else {
            return "";
        }
    }

    /**
     * Returns the JavaScript for presenting additional file
     * properties.
     *
     * @param file           the content file
     *
     * @return the JavaScript for additional file properties
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getFileProperties(ContentFile file)
        throws ContentException {

        Application   app = AdminUtils.getApplication();
        StringBuffer  buffer = new StringBuffer();
        String        str;

        buffer.append("objectAddProperty('Size', '");
        buffer.append(AdminUtils.formatFileSize(file.getFile().length()));
        buffer.append("');\n");
        buffer.append("objectAddProperty('Type', ");
        str = file.getMimeType(app.getServletContext());
        if (str == null) {
            buffer.append("'Unknown'");
        } else {
            buffer.append(AdminUtils.getScriptString(str));
        }
        buffer.append(");\n");
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting domain buttons.
     *
     * @param user           the current user
     * @param domain         the domain object
     * @param view           the view name
     *
     * @return the JavaScript for presenting domain buttons
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getButtons(User user, Domain domain, String view)
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();

        if (domain.hasWriteAccess(user)) {
            buffer.append("objectAddNewButton('add-");
            buffer.append(view);
            buffer.append(".html");
            buffer.append(getLinkParameters(domain));
            buffer.append("');\n");
        }
        if (user.isSuperUser()) {
            if (view.equals("site")) {
                buffer.append("objectAddEditButton('edit-site.html");
                buffer.append(getLinkParameters(domain));
                buffer.append("');\n");
            }
            if (!domain.getName().equals("ROOT")) {
                buffer.append("objectAddDeleteButton('delete.html");
                buffer.append(getLinkParameters(domain));
                buffer.append("');\n");
            }
        }
        if (domain.hasAdminAccess(user)) {
            buffer.append("objectAddPermissionsButton('permissions.html");
            buffer.append(getLinkParameters(domain));
            buffer.append("');\n");
        }
        if (domain.hasPublishAccess(user)) {
            buffer.append("objectAddStatisticsButton('statistics.html");
            buffer.append(getLinkParameters(domain));
            buffer.append("');\n");
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting domain buttons.
     *
     * @param user           the current user
     * @param content        the content object
     * @param view           the view name
     * @param status         the content status
     * @param lock           the content lock
     *
     * @return the JavaScript for presenting domain buttons
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getButtons(User user,
                              Content content,
                              String view,
                              int status,
                              Lock lock)
        throws ContentException {

        ContentManager manager = AdminUtils.getContentManager();
        StringBuffer   buffer = new StringBuffer();

        if (lock != null) {
            if (content.hasWriteAccess(user)) {
                buffer.append("objectAddUnlockButton('unlock.html");
                buffer.append(getLinkParameters(content));
                buffer.append("');\n");
            }
        } else {
            if (content.hasWriteAccess(user)) {
                if (isContainer(content)) {
                    buffer.append("objectAddNewButton('add-");
                    buffer.append(view);
                    buffer.append(".html");
                    buffer.append(getLinkParameters(content));
                    buffer.append("');\n");
                }
                buffer.append("objectAddEditButton('edit-");
                buffer.append(view);
                buffer.append(".html");
                buffer.append(getLinkParameters(content));
                buffer.append("');\n");
            }
            if (content.hasPublishAccess(user)) {
                if (status == 1) {
                    buffer.append("objectAddUnpublishButton('");
                    buffer.append("unpublish.html");
                    buffer.append(getLinkParameters(content));
                    buffer.append("');\n");
                } else if (AdminUtils.isOnline(content.getParent(manager))) {
                    buffer.append("objectAddPublishButton('");
                    buffer.append("publish.html");
                    buffer.append(getLinkParameters(content));
                    buffer.append("');\n");
                }
                if (content.getAllRevisions().length > 1) {
                    buffer.append("objectAddRevertButton('revert.html");
                    buffer.append(getLinkParameters(content));
                    buffer.append("');\n");
                }
                buffer.append("objectAddDeleteButton('delete.html");
                buffer.append(getLinkParameters(content));
                buffer.append("');\n");
            }
            if (content.hasAdminAccess(user)) {
                buffer.append("objectAddPermissionsButton('permissions.html");
                buffer.append(getLinkParameters(content));
                buffer.append("');\n");
            }
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting domain hosts.
     *
     * @param domain         the domain object
     *
     * @return the JavaScript for presenting domain hosts
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getHosts(Domain domain)
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();
        ArrayList     list;
        DomainHost    host;

        list = domain.getHosts();
        Collections.sort(list);
        if (list.size() == 0) {
            buffer.append("objectAddHost('N/A', 'No hosts registered');\n");
        }
        for (int i = 0; i < list.size(); i++) {
            host = (DomainHost) list.get(i);
            buffer.append("objectAddHost(");
            buffer.append(AdminUtils.getScriptString(host.getName()));
            buffer.append(", ");
            buffer.append(AdminUtils.getScriptString(host.getDescription()));
            buffer.append(");\n");
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting content revisions.
     *
     * @param user           the current user
     * @param content        the content object
     *
     * @return the JavaScript for presenting content revisions
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getRevisions(User user, Content content)
        throws ContentException {

        StringBuffer  buffer = new StringBuffer();
        Content[]     revisions;
        Content       work = null;
        String        previewUrl = getPreviewUrl(content);

        revisions = content.getAllRevisions();
        for (int i = 0; i < revisions.length; i++) {
            if (revisions[i].getRevisionNumber() == 0) {
                work = revisions[i];
            }
        }
        if (work != null) {
            buffer.append(getRevision(user, work, previewUrl));
        }
        for (int i = 0; i < revisions.length; i++) {
            if (revisions[i] != work) {
                buffer.append(getRevision(user, revisions[i], previewUrl));
            }
        }
        return buffer.toString();
    }

    /**
     * Returns the JavaScript for presenting a content revision.
     *
     * @param user           the current user
     * @param revision       the content revison object
     * @param previewUrl     the content preview URL, or null
     *
     * @return the JavaScript for presenting a content revision
     */
    private String getRevision(User user,
                               Content revision,
                               String previewUrl) {

        StringBuffer  buffer = new StringBuffer();
        Date          date;

        buffer.append("objectAddRevision(");
        if (revision.getRevisionNumber() == 0) {
            buffer.append("'Work'");
        } else {
            buffer.append(revision.getRevisionNumber());
        }
        buffer.append(", ");
        date = revision.getModifiedDate();
        buffer.append(AdminUtils.getScriptDate(user, date));
        buffer.append(", ");
        buffer.append(AdminUtils.getScriptString(revision.getAuthorName()));
        buffer.append(", ");
        buffer.append(AdminUtils.getScriptString(revision.getComment()));
        buffer.append(", ");
        if (previewUrl == null) {
            buffer.append("null");
        } else {
            buffer.append("'");
            buffer.append(previewUrl);
            buffer.append("?revision=");
            buffer.append(revision.getRevisionNumber());
            buffer.append("'");
        }
        buffer.append(");\n");
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

        return getPermissions(domain.getPermissions(), false);
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

        PermissionList  permissions;
        boolean         inherited = false;

        permissions = content.getPermissions(false);
        if (permissions.isEmpty()) {
            inherited = true;
            permissions = content.getPermissions(true);
        }
        return getPermissions(permissions, inherited);
    }

    /**
     * Returns the JavaScript for presenting a list of permissions.
     *
     * @param permissions     the permission list
     * @param inherited       the inherited flag
     *
     * @return the JavaScript for presenting the permissions
     */
    private String getPermissions(PermissionList permissions,
                                  boolean inherited) {

        Permission[]  perms = permissions.getPermissions();
        StringBuffer  buffer = new StringBuffer();
        String        str;

        if (permissions.isEmpty()) {
            buffer.append("objectAddPermission(null, null, ");
            buffer.append("false, false, false, false, false);\n");
        }
        for (int i = 0; i < perms.length; i++) {
            buffer.append("objectAddPermission(");
            if (perms[i].getUserName().equals("")) {
                buffer.append("null");
            } else {
                str = perms[i].getUserName();
                buffer.append(AdminUtils.getScriptString(str));
            }
            buffer.append(", ");
            if (perms[i].getGroupName().equals("")) {
                buffer.append("null");
            } else {
                str = perms[i].getGroupName();
                buffer.append(AdminUtils.getScriptString(str));
            }
            buffer.append(", ");
            buffer.append(perms[i].getRead());
            buffer.append(", ");
            buffer.append(perms[i].getWrite());
            buffer.append(", ");
            buffer.append(perms[i].getPublish());
            buffer.append(", ");
            buffer.append(perms[i].getAdmin());
            buffer.append(", ");
            buffer.append(!inherited);
            buffer.append(");\n");
        }

        return buffer.toString();
    }

    /**
     * Returns the JavaScript representation of a content URL.
     *
     * @param content        the content object
     *
     * @return the JavaScript representation of a content URL
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getContentUrl(Content content) throws ContentException {
        ContentManager manager = AdminUtils.getContentManager();
        String         str;

        if (content instanceof ContentSite) {
            return content.toString();
        } else if (content instanceof ContentFolder) {
            return getContentUrl(content.getParent(manager)) +
                   content.toString() + "/";
        } else if (content instanceof ContentPage) {
            return getContentUrl(content.getParent(manager)) +
                   content.toString();
        } else if (content instanceof ContentFile) {
            str = getContentUrl(content.getParent(manager));
            if (str.endsWith("/")) {
                return str + content.toString();
            } else {
                return str;
            }
        } else if (content instanceof ContentTranslator) {
            return getContentUrl(content.getParent(manager)) + "*/";
        } else {
            return "N/A";
        }
    }

    /**
     * Returns the preview URL for a content object.
     *
     * @param content        the content object
     *
     * @return the preview URL, or
     *         null if the object cannot be previewed
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getPreviewUrl(Content content) throws ContentException {
        ContentManager manager = AdminUtils.getContentManager();
        String         str;

        if (content instanceof ContentSite) {
            if (((ContentSite) content).isAdmin()) {
                return null;
            } else {
                return "preview/" + content.getId() + "/";
            }
        } else if (content instanceof ContentFolder) {
            return getPreviewUrl(content.getParent(manager)) +
                   content.toString() + "/";
        } else if (content instanceof ContentPage) {
            str = getPreviewUrl(content.getParent(manager));
            if (str != null) {
                return str + content.toString();
            } else {
                return null;
            }
        } else if (content instanceof ContentFile) {
            str = getPreviewUrl(content.getParent(manager));
            if (str != null) {
                return str + content.toString();
            } else {
                return "preview/" + content.getId() + "/";
            }
        } else if (content instanceof ContentTemplate) {
            return "preview/" + content.getId() + "/";
        } else if (content instanceof ContentSection) {
            return "preview/" + content.getId() + "/";
        } else if (content instanceof ContentDocument) {
            return "preview/" + content.getId() + "/";
        } else {
            return null;
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

        if (!AdminUtils.isOnline(content)) {
            return 0;
        } else if (content.getRevisionNumber() == 0) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * Returns the JavaScript representation of a lock object.
     *
     * @param user           the current user
     * @param lock           the lock object, or null
     *
     * @return the JavaScript representation of a lock object
     */
    private String getLock(User user, Lock lock) {
        String  str;

        if (lock == null) {
            return "null, null";
        } else {
            str = AdminUtils.formatDate(user, lock.getAcquiredDate());
            return AdminUtils.getScriptString(lock.getUserName()) +
                   ", " + AdminUtils.getScriptString(str);
        }
    }

    /**
     * Returns the JavaScript for setting all the inherited template
     * page elements.
     *
     * @param template       the content template, or null
     *
     * @return the JavaScript for setting the page elements
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public String getTemplateElements(ContentTemplate template)
        throws ContentException {

        ContentManager manager = AdminUtils.getContentManager();
        StringBuffer   buffer = new StringBuffer();
        List           names;
        Iterator       iter = null;
        String         name;
        String         str;

        buffer.append("templateRemoveAllInherited();\n");
        if (template != null) {
            names = template.getAllElementNames(manager);
            Collections.sort(names);
            iter = names.iterator();
        }
        while (iter != null && iter.hasNext()) {
            name = iter.next().toString();
            buffer.append("templateAddInherited(");
            buffer.append(AdminUtils.getScriptString(name));
            buffer.append(", ");
            str = template.getElement(manager, name);
            buffer.append(AdminUtils.getScriptString(str));
            buffer.append(");\n");
        }
        buffer.append("templateDisplay();\n");
        return buffer.toString();
    }

    /**
     * Returns the domain link parameters.
     *
     * @param domain         the domain object
     *
     * @return the domain URL parameter string
     */
    private String getLinkParameters(Domain domain) {
        return "?type=domain&id=" + domain.getName();
    }

    /**
     * Returns the content link parameters.
     *
     * @param content        the content object
     *
     * @return the content URL parameter string
     */
    private String getLinkParameters(Content content) {
        return "?type=" + AdminUtils.getCategory(content) +
               "&id=" + content.getId();
    }

    /**
     * Checks if the specified content object is a container. I.e. if
     * the content object supports having child content objects.
     *
     * @param content        the content object to check
     *
     * @return true if the content object is a container, or
     *         false otherwise
     */
    public boolean isContainer(Content content) {
        if (content instanceof ContentSite) {
            return !((ContentSite) content).isAdmin();
        } else {
            return content instanceof ContentTranslator
                || content instanceof ContentFolder
                || content instanceof ContentTemplate
                || content instanceof ContentSection
                || content instanceof ContentDocument
                || content instanceof ContentForum
                || content instanceof ContentTopic;
        }
    }
}
