/*
 * SystemView.java
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

import java.io.File;
import java.util.ArrayList;

import net.percederberg.liquidsite.admin.AdminUtils;

import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.Request;

/**
 * A helper class for the system view. This class contains methods
 * for creating the HTML responses to the pages in the system view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class SystemView {

    /**
     * Creates a new system view helper.
     */
    SystemView() {
        // Nothing to initialize
    }

    /**
     * Shows the system page.
     *
     * @param request        the request object
     */
    public void viewSystem(Request request) {
        AdminUtils.sendTemplate(request, "admin/system.ftl");
    }

    /**
     * Shows the system backup form.
     *
     * @param request        the request object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewBackup(Request request) throws ContentException {
        ContentManager  manager = AdminUtils.getContentManager();
        User            user = request.getUser();
        Domain[]        domains;
        ArrayList       list = new ArrayList();

        domains = manager.getDomains(user);
        for (int i = 0; i < domains.length; i++) {
            list.add(domains[i].getName());
        }
        request.setAttribute("domains", list);
        AdminUtils.sendTemplate(request, "admin/system-backup.ftl");
    }

    /**
     * Shows the system restore form.
     *
     * @param request        the request object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public void viewRestore(Request request) throws ContentException {
        File        backupDir;
        String[]    files;
        String      backup;
        ArrayList   backups = new ArrayList();
        String      domain;
        String      revisions;

        backupDir = AdminUtils.getBackupDir();
        if (backupDir == null) {
            throw new ContentException("no backup directory found");
        }
        files = backupDir.list();
        for (int i = 0; i < files.length; i++) {
            backups.add(files[i]);
        }
        backup = request.getParameter("backup", "");
        domain = request.getParameter("domain", "");
        revisions = request.getParameter("revisions", "");
        request.setAttribute("backup", backup);
        request.setAttribute("backups", backups);
        request.setAttribute("domain", domain);
        request.setAttribute("revisions", revisions);
        AdminUtils.sendTemplate(request, "admin/system-restore.ftl");
    }
}
