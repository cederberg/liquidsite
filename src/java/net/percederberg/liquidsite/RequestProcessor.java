/*
 * RequestProcessor.java
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

package net.percederberg.liquidsite;

import java.io.File;
import java.io.StringWriter;

import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;
import net.percederberg.liquidsite.content.ContentFolder;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentPage;
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.ContentSite;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.template.Template;
import net.percederberg.liquidsite.template.TemplateException;
import net.percederberg.liquidsite.template.TemplateManager;

/**
 * A request processor.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public abstract class RequestProcessor {

    /**
     * The content manager to use.
     */
    private ContentManager manager;

    /**
     * The base directory for application files.
     */
    private File baseDir;

    /**
     * Creates a new request processor.
     *
     * @param manager        the content manager to use
     * @param baseDir        the base directory for application files
     */
    public RequestProcessor(ContentManager manager, File baseDir) {
        this.manager = manager;
        this.baseDir = baseDir;
    }

    /**
     * Returns the content manager used by this processor.
     *
     * @return the content manager used by this processor
     */
    protected ContentManager getContentManager() {
        return manager;
    }

    /**
     * Returns an application file. The file path should be specified
     * relative to the base application directory.
     *
     * @param path           the relative file path
     *
     * @return the absolute file path
     */
    protected File getFile(String path) {
        return new File(baseDir, path);
    }

    /**
     * Processes a request.
     *
     * @param request        the request to process
     *
     * @throws RequestException if the request couldn't be processed
     */
    public abstract void process(Request request) throws RequestException;

    /**
     * Destroys this request processor. This method frees all
     * internal resources used by this processor.
     */
    public abstract void destroy();

    /**
     * Finds the content page corresponding to a request path. Note
     * that this method may return any type of content object as long
     * as it supports being presented by the sendContent() method.
     *
     * @param request        the request object
     * @param parent         the content parent
     * @param path           the request path after the parent
     *
     * @return the content object corresponding to the path, or
     *         null if no matching content was found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the specified content
     *             object wasn't readable by the user
     */
    protected Content locatePage(Request request, Content parent, String path)
        throws ContentException, ContentSecurityException {

        Content  content = parent;
        String   name;
        int      pos;

        while (content != null && path.length() > 0) {
            pos = path.indexOf('/');
            if (pos <= 0) {
                name = path;
            } else {
                name = path.substring(0, pos);
            }
            content = locateChild(request, parent, name);
            path = path.substring(name.length());
            if (path.startsWith("/")) {
                if (content instanceof ContentSite
                 || content instanceof ContentFolder
                 || content instanceof ContentSection
                 || content instanceof ContentDocument) {

                    path = path.substring(1);
                }
            }
            parent = content;
        }
        return content;
    }

    /**
     * Finds the content child object corresponding to a name. This
     * method will first attempt a direct match. If that fails, it
     * will search through all content pages linked to sections for
     * a matching document. Note that this method may return any type
     * of content object as long as it supports being presented by
     * the sendContent() method.
     *
     * @param request        the request object
     * @param parent         the content parent
     * @param name           the child name
     *
     * @return the content object corresponding to the name, or
     *         null if no matching content was found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the specified content
     *             object wasn't readable by the user
     */
    private Content locateChild(Request request,
                                Content parent,
                                String name)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = getContentManager();
        User            user = request.getUser();
        Content         content;
        Content[]       children;

        content = manager.getContentChild(user, parent, name);
        if (content != null) {
            return content;
        }
        children = manager.getContentChildren(user,
                                              parent,
                                              Content.PAGE_CATEGORY);
        for (int i = 0; i < children.length; i++) {
            content = locateDocument(request,
                                     (ContentPage) children[i],
                                     name);
            if (content != null) {
                return content;
            }
        }
        return null;
    }

    /**
     * Finds the content section or document object corresponding to
     * a name. This will search through a section linked to the page
     * for a matching section or document.
     *
     * @param request        the request object
     * @param page           the content page
     * @param name           the child name
     *
     * @return the content document corresponding to the name, or
     *         null if no matching content was found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the specified content
     *             object wasn't readable by the user
     */
    private Content locateDocument(Request request,
                                   ContentPage page,
                                   String name)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = getContentManager();
        User            user = request.getUser();
        ContentSection  section;
        Content         document;

        section = page.getSection(user);
        if (section == null) {
            return null;
        }
        document = manager.getContentChild(user, section, name);
        if (document != null) {
            request.getEnvironment().setPage(page);
        }
        return document;
    }

    /**
     * Finds the index page for a content parent. This method does
     * NOT control access permissions and should thus ONLY be used
     * internally in the request processing.
     *
     * @param request        the request object
     * @param parent         the content parent
     *
     * @return the index content object, or
     *         null if no matching content was found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the specified content
     *             object wasn't readable by the user
     */
    private Content locateIndexPage(Request request, Content parent)
        throws ContentException, ContentSecurityException {

        String[]  index = { "index.html", "index.htm" };
        Content   page;

        for (int i = 0; i < index.length; i++) {
            page = locatePage(request, parent, index[i]);
            if (page != null) {
                return page;
            }
        }
        return null;
    }

    /**
     * Processes a request to a content object.
     *
     * @param request        the request object
     * @param content        the content object requested
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the requested content
     *             object wasn't readable by the user
     * @throws TemplateException if the page template couldn't be
     *             processed correctly
     * @throws RequestException if the content wasn't found
     */
    protected void sendContent(Request request, Content content)
        throws ContentException, ContentSecurityException,
               TemplateException, RequestException {

        if (content instanceof ContentSite) {
            content = locateIndexPage(request, content);
            sendContent(request, content);
        } else if (content instanceof ContentFolder) {
            if (request.getPath().endsWith("/")) {
                content = locateIndexPage(request, content);
                sendContent(request, content);
            } else {
                request.sendRedirect(request.getPath() + "/");
            }
        } else if (content instanceof ContentPage) {
            request.getEnvironment().setPage((ContentPage) content);
            sendContentPage(request, (ContentPage) content);
        } else if (content instanceof ContentFile) {
            request.sendFile(((ContentFile) content).getFile());
        } else if (content instanceof ContentDocument) {
            if (request.getPath().endsWith("/")) {
                request.getEnvironment().setDocument((ContentDocument) content);
                sendContent(request, request.getEnvironment().getPage());
            } else {
                request.sendRedirect(request.getPath() + "/");
            }
        } else {
            throw RequestException.RESOURCE_NOT_FOUND;
        }
    }

    /**
     * Processes a request to a content page.
     *
     * @param request        the request object
     * @param page           the page requested
     *
     * @throws TemplateException if the page template couldn't be
     *             processed correctly
     */
    private void sendContentPage(Request request, ContentPage page)
        throws TemplateException {

        User          user = request.getUser();
        Template      template;
        StringWriter  buffer = new StringWriter();

        template = TemplateManager.getPageTemplate(user, page);
        template.process(request, getContentManager(), buffer);
        request.sendData("text/html", buffer.toString());
    }
}
