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

package org.liquidsite.app.servlet;

import java.io.File;
import java.io.StringWriter;

import org.liquidsite.app.template.Template;
import org.liquidsite.app.template.TemplateException;
import org.liquidsite.app.template.TemplateManager;
import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentDocument;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentFile;
import org.liquidsite.core.content.ContentFolder;
import org.liquidsite.core.content.ContentForum;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentPage;
import org.liquidsite.core.content.ContentSection;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.ContentSite;
import org.liquidsite.core.content.ContentTopic;
import org.liquidsite.core.content.ContentTranslator;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.Request;

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
     * This method may set parts of the request environment while
     * processing the path.
     *
     * @param request        the request object
     * @param site           the content site
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
    protected Content locatePage(Request request,
                                 ContentSite site,
                                 String path)
        throws ContentException, ContentSecurityException {

        Content  content = site;
        String   name;
        int      pos;

        if (path.startsWith("liquidsite.obj/")) {
            return locateObject(request, path.substring(15));
        }
        while (content != null && path.length() > 0) {
            pos = path.indexOf('/');
            if (pos <= 0) {
                name = path;
            } else {
                name = path.substring(0, pos);
            }
            content = locateChild(request, content, name);
            path = path.substring(name.length());
            if (path.startsWith("/")) {
                if (isDirectory(content)) {
                    path = path.substring(1);
                }
            }
        }
        return content;
    }

    /**
     * Finds the content child object corresponding to a name. This
     * method will first attempt a direct match. If that fails, it
     * will search translators and content pages linked to sections
     * for a matching document. Note that this method may return any
     * type of content object as long as it supports being presented
     * by the sendContent() method.
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

        User       user = request.getUser();
        Content    content;
        Content[]  children;

        // Check translator children
        content = request.getEnvironment().getTranslator();
        if (content != null) {
            content = manager.getContentChild(user, content, name);
            if (isPage(content)) {
                updateRequestEnvironment(request, content);
                return content;
            }
        }

        // Check parent object children
        content = manager.getContentChild(user, parent, name);
        if (isDirectory(content) || isPage(content)) {
            updateRequestEnvironment(request, content);
            return content;
        }

        // Check parent translators
        children = manager.getContentChildren(user,
                                              parent,
                                              Content.TRANSLATOR_CATEGORY);
        for (int i = 0; i < children.length; i++) {
            content = locateTranslated(request,
                                       (ContentTranslator) children[i],
                                       name);
            if (content != null) {
                updateRequestEnvironment(request, content);
                return content;
            }
        }

        return null;
    }

    /**
     * Finds the translator child object corresponding to a name. This
     * will search any object linked to the translator for a matching
     * section or document. Note that this method may return any type
     * of content object as long as it supports being presented by the
     * sendContent() method.
     *
     * @param request        the request object
     * @param translator     the content translator
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
    private Content locateTranslated(Request request,
                                     ContentTranslator translator,
                                     String name)
        throws ContentException, ContentSecurityException {

        User            user = request.getUser();
        ContentSection  section;
        Content         content = null;

        if (translator.getType() == ContentTranslator.ALIAS_TYPE) {
            /* TODO: enable when env.setPage() works for aliases
            content = translator.getAlias(user);
            content = manager.getContentChild(user, content, name);
            */
        } else if (translator.getType() ==  ContentTranslator.REDIRECT_TYPE) {
            // TODO: implement this!
        } else if (translator.getType() ==  ContentTranslator.SECTION_TYPE) {
            section = translator.getSection(user);
            content = manager.getContentChild(user, translator, name);
            if (content == null) {
                content = manager.getContentChild(user, section, name);
            }
            if (content != null) {
                request.getEnvironment().setTranslator(translator);
                request.getEnvironment().setSection(section);
            }
        }
        return content;
    }

    /**
     * Finds the content object corresponding to an object request
     * path. Note that this method may return any type of content
     * object as long as it supports being presented by the
     * sendContent() method. The object request path should start with
     * a content id, followed by the name of a child object.
     *
     * @param request        the request object
     * @param path           the object request path
     *
     * @return the content object corresponding to the path, or
     *         null if no matching content was found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the specified content
     *             object wasn't readable by the user
     */
    private Content locateObject(Request request, String path)
        throws ContentException, ContentSecurityException {

        User     user = request.getUser();
        Content  content;
        int      id;
        String   name;
        int      pos;

        pos = path.indexOf("/");
        if (pos <= 0) {
            return null;
        }
        try {
            id = Integer.parseInt(path.substring(0, pos));
            name = path.substring(pos + 1);
        } catch (NumberFormatException e) {
            return null;
        }
        content = manager.getContent(user, id);
        if (content == null) {
            return null;
        }
        return locateChild(request, content, name);
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
            page = locateChild(request, parent, index[i]);
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

        if (isDirectory(content) && !request.getPath().endsWith("/")) {
            request.sendRedirect(request.getPath() + "/");
        } else if (content instanceof ContentSite) {
            sendContent(request, locateIndexPage(request, content));
        } else if (content instanceof ContentFolder) {
            sendContent(request, locateIndexPage(request, content));
        } else if (content instanceof ContentPage) {
            request.getEnvironment().setPage((ContentPage) content);
            sendContentPage(request, (ContentPage) content);
        } else if (content instanceof ContentFile) {
            request.sendFile(((ContentFile) content).getFile(),
                             !content.hasReadAccess(null));
        } else if (content instanceof ContentSection) {
            if (request.getEnvironment().getTranslator() != null) {
                content = request.getEnvironment().getTranslator();
                sendContent(request, locateIndexPage(request, content));
            } else {
                throw RequestException.RESOURCE_NOT_FOUND;
            }
        } else if (content instanceof ContentDocument) {
            if (request.getEnvironment().getTranslator() != null) {
                content = request.getEnvironment().getTranslator();
                sendContent(request, locateIndexPage(request, content));
            } else {
                sendContent(request, request.getEnvironment().getPage());
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

    /**
     * Processes a request to a template file. The template file name
     * is relative to the web context directory, and the output MIME
     * type will always be set to "text/html".
     *
     * @param request        the request object
     * @param templateName   the template file name
     *
     * @throws TemplateException if the file template couldn't be
     *             processed correctly
     */
    protected void sendTemplate(Request request, String templateName)
        throws TemplateException {

        Template      template;
        StringWriter  buffer = new StringWriter();

        template = TemplateManager.getFileTemplate(templateName);
        template.process(request, getContentManager(), buffer);
        request.sendData("text/html", buffer.toString());
    }

    /**
     * Checks if the specified content object represents a directory.
     *
     * @param content        the content object to check
     *
     * @return true if the content object is a directory, or
     *         false otherwise
     */
    private boolean isDirectory(Content content) {
        return content instanceof ContentSite
            || content instanceof ContentFolder
            || content instanceof ContentSection
            || content instanceof ContentDocument
            || content instanceof ContentForum
            || content instanceof ContentTopic;
    }

    /**
     * Checks if the specified content object represents a page.
     *
     * @param content        the content object to check
     *
     * @return true if the content object is a page, or
     *         false otherwise
     */
    private boolean isPage(Content content) {
        return content instanceof ContentPage
            || content instanceof ContentFile;
    }

    /**
     * Updates the request environment with the specified content
     * object.
     *
     * @param request        the request
     * @param content        the content object
     */
    private void updateRequestEnvironment(Request request, Content content) {
        if (content instanceof ContentPage) {
            request.getEnvironment().setPage((ContentPage) content);
        } else if (content instanceof ContentSection) {
            request.getEnvironment().setSection((ContentSection) content);
        } else if (content instanceof ContentDocument) {
            request.getEnvironment().setDocument((ContentDocument) content);
        } else if (content instanceof ContentForum) {
            request.getEnvironment().setForum((ContentForum) content);
        } else if (content instanceof ContentTopic) {
            request.getEnvironment().setTopic((ContentTopic) content);
        }
    }
}
