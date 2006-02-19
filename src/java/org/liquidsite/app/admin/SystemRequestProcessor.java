/*
 * SystemRequestProcessor.java
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

package org.liquidsite.app.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import org.liquidsite.app.admin.view.AdminView;
import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.ContentSelector;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.DomainHost;
import org.liquidsite.core.content.Group;
import org.liquidsite.core.content.Lock;
import org.liquidsite.core.content.Permission;
import org.liquidsite.core.content.PermissionList;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.Request;
import org.liquidsite.util.log.Log;

/**
 * The request processor for the system view in the administration
 * site(s).
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class SystemRequestProcessor {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(SystemRequestProcessor.class);

    /**
     * The name of the XML file containing the backup data. This file
     * is packaged inside a ZIP file together with all the binary
     * files referenced by the XML.
     */
    private static final String BACKUP_FILE = "liquidsite.data";

    /**
     * Creates a new administration request processor.
     */
    public SystemRequestProcessor() {
        // Nothing to initialize
    }

    /**
     * Processes a request.
     *
     * @param request        the request object
     * @param path           the request path
     */
    public void process(Request request, String path) {
        User  user = request.getUser();

        if (user != null && user.isSuperUser()) {
            processAuthorized(request, path);
        } else {
            processUnauthorized(request, path);
        }
    }

    /**
     * Processes an authorized request. This is a request from a user
     * with permissions to access the system view.
     *
     * @param request        the request object
     * @param path           the request path
     */
    private void processAuthorized(Request request, String path) {
        String  action = request.getParameter("action", "");

        if (action.equals("restart")) {
            handleRestart(request);
        } else if (action.equals("backup")) {
            handleBackup(request);
        } else if (action.equals("restore")) {
            handleRestore(request);
        } else {
            AdminView.SYSTEM.viewSystem(request);
        }
    }

    /**
     * Processes an unauthorized request. This is a request from a
     * user without permissions to access the system view.
     *
     * @param request        the request object
     * @param path           the request path
     */
    private void processUnauthorized(Request request, String path) {
        LOG.warning("unauthorized access admin system view by user " +
                    request.getUser());
        AdminView.BASE.viewError(request,
                                 "Access denied for the current user.",
                                 "index.html");
    }

    /**
     * Handles the system restart requests.
     *
     * @param request        the request object
     */
    private void handleRestart(Request request) {
        AdminUtils.getApplication().restart();
        AdminView.BASE.viewInfo(request,
                                "System restarted successfully",
                                "system.html");
    }

    /**
     * Handles the system backup requests.
     *
     * @param request        the request object
     */
    private void handleBackup(Request request) {
        SimpleDateFormat  df;
        String            domain;
        String            name;
        File              dir;
        File              file;

        try {
            if (!validateBackup(request)) {
                AdminView.SYSTEM.viewBackup(request);
            } else {
                df = new SimpleDateFormat("yyyy-MM-dd-HHmm");
                domain = request.getParameter("domain");
                name = domain + "." + df.format(new Date()) + ".liquidsite";
                dir = AdminUtils.getBackupDir();
                file = new File(dir, name);
                backup(file, domain, request.getUser());
                AdminView.BASE.viewInfo(request,
                                        "Backup stored successfully",
                                        "system.html");
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            AdminView.BASE.viewError(request,
                                     "Failed to backup: " + e.getMessage(),
                                     "system.html");
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            AdminView.BASE.viewError(request,
                                     "Access denied for the current user.",
                                     "system.html");
        }
    }

    /**
     * Handles the system restore requests.
     *
     * @param request        the request object
     */
    private void handleRestore(Request request) {
        File     dir;
        File     file;
        String   domain;
        int      mode;
        String   str;

        try {
            if (!validateRestore(request)) {
                AdminView.SYSTEM.viewRestore(request);
            } else {
                dir = AdminUtils.getBackupDir();
                file = new File(dir, request.getParameter("backup"));
                domain = request.getParameter("domain");
                str = request.getParameter("revisions", "");
                if (str.equals("latest")) {
                    mode = 1;
                } else if (str.equals("work")) {
                    mode = 2;
                } else {
                    mode = 0;
                }
                if (restore(file, domain, mode, request.getUser())) {
                    str = "Successfully restored backup";
                } else {
                    str = "Successfully restored backup, but some " +
                          "elements were omitted. Check logs " +
                          "for details.";
                }
                AdminView.BASE.viewInfo(request, str, "system.html");
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            AdminView.BASE.viewError(request,
                                     "Restore failed: " + e.getMessage(),
                                     "system.html");
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
            AdminView.BASE.viewError(request,
                                     "Access denied for the current user.",
                                     "system.html");
        }
    }

    /**
     * Validates the backup form.
     *
     * @param request        the request object
     *
     * @return true if the form parameters validated correctly, or
     *         false otherwise
     */
    private boolean validateBackup(Request request) {
        String  message;

        if (request.getParameter("domain") == null) {
            return false;
        }
        if (request.getParameter("domain", "").equals("")) {
            message = "Please select a domain to backup";
            request.setAttribute("error", message);
            return false;
        }
        return true;
    }

    /**
     * Validates the restore form.
     *
     * @param request        the request object
     *
     * @return true if the form parameters validated correctly, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if some object that wasn't
     *             readable by the user
     */
    private boolean validateRestore(Request request)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        String          message;
        String          name;

        if (request.getParameter("backup") == null) {
            return false;
        }
        if (request.getParameter("backup", "").equals("")) {
            message = "Please select a backup file to restore";
            request.setAttribute("error", message);
            return false;
        }
        name = request.getParameter("domain", "");
        if (name.equals("")) {
            message = "Please enter the name of a domain to create";
            request.setAttribute("error", message);
            return false;
        }
        if (manager.getDomain(request.getUser(), name) != null) {
            message = "The domain specified already exists";
            request.setAttribute("error", message);
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            if (Domain.NAME_CHARS.indexOf(name.charAt(i)) < 0) {
                message = "invalid character in domain name: '" +
                           name.charAt(i) + "'";
                request.setAttribute("error", message);
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a complete backup for the specified domain.
     *
     * @param dest           the destination file
     * @param domainName     the name of the domain to backup 
     * @param user           the user performing the operation
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if some object that wasn't
     *             readable by the user
     */
    private void backup(File dest, String domainName, User user)
        throws ContentException, ContentSecurityException {

        ContentManager   manager = AdminUtils.getContentManager();
        ZipOutputStream  zip;
        PrintWriter      writer;
        Domain           domain;
        String           message;

        try {
            zip = new ZipOutputStream(new FileOutputStream(dest));
            zip.putNextEntry(new ZipEntry(BACKUP_FILE));
            writer = new PrintWriter(new OutputStreamWriter(zip, "UTF-8"));
            domain = manager.getDomain(user, domainName);
            backupXml(writer, domain, user);
            writer.flush();
            zip.closeEntry();
            backupFile(zip, "", domain.getDirectory());
            zip.close();
            manager.reset();
        } catch (IOException e) {
            dest.delete();
            message = "IO error while writing to " + dest;
            LOG.error(message, e);
            throw new ContentException(message, e);
        }
    }

    /**
     * Creates a complete XML backup for the specified domain.
     *
     * @param out            the output stream
     * @param domain         the domain to backup 
     * @param user           the user performing the operation
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void backupXml(PrintWriter out, Domain domain, User user)
        throws ContentException {

        ContentManager   manager = AdminUtils.getContentManager();
        Group[]          groups;
        User[]           users;
        ArrayList        hosts;
        DomainHost       host;
        Content[]        content;
        ContentSelector  selector;
        int              count;

        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println();
        out.println("<liquidsite-data version=\"1\">");
        out.print("  <domain name=\"");
        out.print(domain.getName());
        out.print("\" description=\"");
        out.print(AdminUtils.getXmlString(domain.getDescription()));
        out.print("\" created=\"");
        out.print(domain.getCreatedDate().getTime());
        out.print("\" modified=\"");
        out.print(domain.getModifiedDate().getTime());
        if (domain.getMailFrom() != null) {
            out.print("\" mailfrom=\"");
            out.print(AdminUtils.getXmlString(domain.getMailFrom()));
        }
        out.println("\">");
        hosts = domain.getHosts();
        for (int i = 0; i < hosts.size(); i++) {
            host = (DomainHost) hosts.get(i);
            out.print("    <host name=\"");
            out.print(host.getName());
            out.print("\" description=\"");
            out.print(AdminUtils.getXmlString(host.getDescription()));
            out.println("\" />");
        }
        groups = manager.getGroups(domain, "");
        for (int i = 0; i < groups.length; i++) {
            backupXml(out, groups[i]);
        }
        count = manager.getUserCount(domain, "");
        for (int i = 0; i < count; i += 100) {
            users = manager.getUsers(domain, "", i, 100);
            for (int j = 0; j < users.length; j++) {
                backupXml(out, users[j]);
            }
        }
        backupXml(out, domain.getPermissions());
        selector = new ContentSelector(domain);
        selector.requireRootParent();
        selector.sortByCategory(false);
        count = manager.getContentCount(selector);
        for (int i = 0; i < count; i += 10) {
            selector.limitResults(i, 10);
            content = manager.getContentObjects(user, selector);
            for (int j = 0; j < content.length; j++) {
                backupXml(out, content[j], user);
            }
        }
        out.println("  </domain>");
        out.println("</liquidsite-data>");
    }

    /**
     * Creates an XML backup for the specified group.
     *
     * @param out            the output stream
     * @param group          the group to backup
     */
    private void backupXml(PrintWriter out, Group group) {
        out.print("    <group name=\"");
        out.print(group.getName());
        out.print("\" description=\"");
        out.print(AdminUtils.getXmlString(group.getDescription()));
        if (group.isPublic()) {
            out.print("\" public=\"public");
        }
        out.print("\" comment=\"");
        out.print(AdminUtils.getXmlString(group.getComment()));
        out.println("\" />");
    }

    /**
     * Creates an XML backup for the specified user.
     *
     * @param out            the output stream
     * @param user           the user to backup
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void backupXml(PrintWriter out, User user)
        throws ContentException {

        Group[]  groups;

        out.print("    <user name=\"");
        out.print(user.getName());
        out.print("\" password=\"");
        out.print(AdminUtils.getXmlString(user.getPassword()));
        if (!user.getEnabled()) {
            out.print("\" disabled=\"disabled");
        }
        out.print("\" realname=\"");
        out.print(AdminUtils.getXmlString(user.getRealName()));
        out.print("\" email=\"");
        out.print(AdminUtils.getXmlString(user.getEmail()));
        out.print("\" comment=\"");
        out.print(AdminUtils.getXmlString(user.getComment()));
        out.println("\">");
        groups = user.getGroups();
        for (int i = 0; i < groups.length; i++) {
            out.print("      <member group=\"");
            out.print(groups[i].getName());
            out.println("\" />");
        }
        out.println("    </user>");
    }

    /**
     * Creates an XML backup for the specified content object.
     *
     * @param out            the output stream
     * @param content        the content to backup
     * @param user           the user performing the operation
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void backupXml(PrintWriter out, Content content, User user)
        throws ContentException {

        ContentManager   manager = AdminUtils.getContentManager();
        Content[]        contents;
        ContentSelector  selector;
        Iterator         iter;
        String           str;
        Date             date;
        int              count;

        out.print("    <content id=\"");
        out.print(content.getId());
        out.print("\" category=\"");
        out.print(content.getCategory());
        out.println("\">");
        contents = content.getAllRevisions();
        for (int i = 0; i < contents.length; i++) {
            out.print("      <revision nr=\"");
            out.print(contents[i].getRevisionNumber());
            out.print("\" name=\"");
            out.print(contents[i].getName());
            out.print("\" parent=\"");
            out.print(contents[i].getParentId());
            out.print("\" online=\"");
            date = contents[i].getOnlineDate();
            if (date == null) {
                out.print(0);
            } else {
                out.print(date.getTime());
            }
            out.print("\" offline=\"");
            date = contents[i].getOfflineDate();
            if (date == null) {
                out.print(0);
            } else {
                out.print(date.getTime());
            }
            out.print("\" modified=\"");
            date = contents[i].getModifiedDate();
            if (date == null) {
                out.print(0);
            } else {
                out.print(date.getTime());
            }
            out.print("\" author=\"");
            out.print(contents[i].getAuthorName());
            out.print("\" comment=\"");
            out.print(AdminUtils.getXmlString(contents[i].getComment()));
            out.println("\">");
            iter = contents[i].getAttributeNames();
            while (iter.hasNext()) {
                str = iter.next().toString();
                out.print("        <attribute name=\"");
                out.print(str);
                out.print("\">");
                str = contents[i].getAttribute(str);
                out.print(AdminUtils.getXmlString(str));
                out.println("</attribute>");
            }
            out.println("      </revision>");
        }
        backupXml(out, content.getPermissions(false));
        backupXml(out, content.getLock());
        out.println("    </content>");
        selector = new ContentSelector(content.getDomain());
        selector.requireParent(content);
        selector.sortById(true);
        count = manager.getContentCount(selector);
        for (int i = 0; i < count; i += 10) {
            selector.limitResults(i, 10);
            contents = manager.getContentObjects(user, selector);
            for (int j = 0; j < contents.length; j++) {
                backupXml(out, contents[j], user);
            }
        }
    }

    /**
     * Creates an XML backup for the specified permission list.
     *
     * @param out            the output stream
     * @param permlist       the permission list to backup
     */
    private void backupXml(PrintWriter out, PermissionList permlist) {
        Permission[]  perms;

        if (permlist != null && !permlist.isEmpty()) {
            perms = permlist.getPermissions();
            for (int i = 0; i < perms.length; i++) {
                if (permlist.getContentId() == 0) {
                    out.print("    ");
                } else {
                    out.print("      ");
                }
                out.print("<permission user=\"");
                out.print(perms[i].getUserName());
                out.print("\" group=\"");
                out.print(perms[i].getGroupName());
                out.print("\" flags=\"");
                out.print(perms[i].getRead() ? "r" : "");
                out.print(perms[i].getWrite() ? "w" : "");
                out.print(perms[i].getPublish() ? "p" : "");
                out.print(perms[i].getAdmin() ? "a" : "");
                out.println("\" />");
            }
        }
    }

    /**
     * Creates an XML backup for the specified content lock.
     *
     * @param out            the output stream
     * @param lock           the content lock to backup, or null
     */
    private void backupXml(PrintWriter out, Lock lock) {
        if (lock != null) {
            out.print("      <lock user=\"");
            out.print(lock.getUserName());
            out.print("\" acquired=\"");
            out.print(lock.getAcquiredDate().getTime());
            out.println("\" />");
        }
    }

    /**
     * Creates a ZIP file backup of the specified file or directory.
     * In the case of a directory, all the files in the directory
     * will be written to the backup.
     *
     * @param out            the output ZIP stream
     * @param dir            the base directory name
     * @param file           the file to backup
     *
     * @throws IOException if the file couldn't be read correctly
     */
    private void backupFile(ZipOutputStream out, String dir, File file)
        throws IOException {

        File[]           files;
        FileInputStream  in;
        byte[]           buffer;
        int              size;

        if (file.isDirectory()) {
            files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                backupFile(out, dir + file.getName() + "/", files[i]);
            }
        } else {
            out.putNextEntry(new ZipEntry(dir + file.getName()));
            in = new FileInputStream(file);
            buffer = new byte[4096];
            while ((size = in.read(buffer)) > 0) {
                out.write(buffer, 0, size);
            }
            in.close();
            out.closeEntry();
        }
    }

    /**
     * Restores a complete backup to the specified domain. In case of
     * failure this method attempts to not leave any partial data
     * behind.
     *
     * @param file           the backup file
     * @param domain         the name of the domain to create
     * @param mode           the content revision policy 
     * @param user           the user performing the operation
     *
     * @return true if the restore was complete, or 
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private boolean restore(File file, String domain, int mode, User user)
        throws ContentException {

        ZipFile            zip = null;
        ZipEntry           entry;
        SAXParser          parser;
        XmlRestoreHandler  handler;
        HashMap            files;
        Iterator           iter;
        String             name;
        String             message;

        try {
            zip = new ZipFile(file);
            entry = zip.getEntry(BACKUP_FILE);
            if (entry == null) {
                message = "failed to locate XML data file " + BACKUP_FILE;
                LOG.error(message);
                throw new ContentException(message);
            }
            handler = new XmlRestoreHandler(domain, mode, user);
            parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(zip.getInputStream(entry), handler);
            files = handler.getContentFiles();
            iter = files.keySet().iterator();
            while (iter.hasNext()) {
                name = iter.next().toString();
                entry = zip.getEntry(name);
                restoreFile(zip, entry, (File) files.get(name));
            }
            AdminUtils.getContentManager().reset();
            return handler.isCompleteRestore();
        } catch (IOException e) {
            message = "IO error while reading " + file;
            LOG.error(message, e);
            throw new ContentException(message, e);
        } catch (ParserConfigurationException e) {
            message = "XML parser configuration error";
            LOG.error(message, e);
            throw new ContentException(message, e);
        } catch (SAXException e) {
            restoreUndo(domain, user);
            if (e.getException() == null) {
                message = "XML parser error while reading " + file;
                LOG.error(message, e);
                throw new ContentException(message, e);
            } else {
                message = "error while reading " + file;
                LOG.error(message, e.getException());
                throw new ContentException(message, e.getException());
            }
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException ignore) {
                    // Do nothing
                }
            }
        }
    }

    /**
     * Restores a file from a ZIP archive. The contents of the file
     * will be saved in the specified file.
     *
     * @param zip            the ZIP archive
     * @param entry          the ZIP archive entry to extract
     * @param dest           the destination file
     *
     * @throws IOException if the file date couldn't be extracted or
     *             written correctly
     */
    private void restoreFile(ZipFile zip, ZipEntry entry, File dest)
        throws IOException {

        InputStream       in;
        FileOutputStream  out;
        byte[]            buffer = new byte[4096];
        int               size;

        dest.getParentFile().mkdirs();
        in = zip.getInputStream(entry);
        out = new FileOutputStream(dest);
        try {
            do {
                size = in.read(buffer);
                if (size > 0) {
                    out.write(buffer, 0, size);
                }
            } while (size > 0);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
                // Do nothing
            }
            try {
                out.close();
            } catch (IOException ignore) {
                // Do nothing
            }
        }
    }

    /**
     * Attempts to undo a restore operation. This method will remove
     * the domain specified if it exists and the specified user has
     * permission to remove it.
     *
     * @param domainName     the name of the domain
     * @param user           the user
     */
    private void restoreUndo(String domainName, User user) {
        ContentManager  manager;
        Domain          domain;
        
        try {
            manager = AdminUtils.getContentManager();
            domain = manager.getDomain(user, domainName);
            if (domain != null) {
                domain.delete(user);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        } catch (ContentSecurityException e) {
            LOG.warning(e.getMessage());
        }
    }
}
