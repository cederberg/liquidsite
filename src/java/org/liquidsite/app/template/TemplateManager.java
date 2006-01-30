/*
 * TemplateManager.java
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

package org.liquidsite.app.template;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import freemarker.cache.TemplateLoader;
import freemarker.core.Configurable;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;

import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentPage;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.User;
import org.liquidsite.util.log.Log;

/**
 * A simple FreeMarker template manager. This class provides static
 * methods that simplify the creation of templates.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class TemplateManager {

    /**
     * The class logger.
     */
    static final Log LOG = new Log(TemplateManager.class);

    /**
     * The build version to return.
     */
    private static String buildVersion = null;

    /**
     * The build date to return.
     */
    private static String buildDate = null;

    /**
     * The file template configuration.
     */
    private static Configuration fileConfig = null;

    /**
     * The page template configuration.
     */
    private static Configuration pageConfig = null;

    /**
     * The page template loader.
     */
    private static PageLoader pageLoader = new PageLoader();

    /**
     * Initializes the template manager.
     *
     * @param baseDir        the application base directory
     * @param version        the application version number
     * @param date           the application version date
     *
     * @throws TemplateException if the template base directory
     *             couldn't be read properly
     */
    public static void initialize(File baseDir,
                                  String version,
                                  String date)
        throws TemplateException {

        buildVersion = version;
        buildDate = date;
        fileConfig = new Configuration();
        fileConfig.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
        fileConfig.setStrictSyntaxMode(true);
        fileConfig.setDefaultEncoding("UTF-8");
        fileConfig.setLocalizedLookup(false);
        try {
            fileConfig.setSetting(Configurable.OUTPUT_ENCODING_KEY, "UTF-8");
		} catch (freemarker.template.TemplateException e) {
            LOG.error(e.getMessage());
            throw new TemplateException("couldn't set output encoding: " +
            		                       e.getMessage());
		}
        try {
            fileConfig.setDirectoryForTemplateLoading(baseDir);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new TemplateException("couldn't read " + baseDir);
        }
        pageConfig = new Configuration();
        pageConfig.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
        pageConfig.setStrictSyntaxMode(true);
        pageConfig.setDefaultEncoding("UTF-8");
        pageConfig.setLocalizedLookup(false);
        try {
            pageConfig.setSetting(Configurable.OUTPUT_ENCODING_KEY, "UTF-8");
        } catch (freemarker.template.TemplateException e) {
            LOG.error(e.getMessage());
            throw new TemplateException("couldn't set output encoding: " +
                                        e.getMessage());
        }
        pageConfig.setTemplateLoader(pageLoader);
    }

    /**
     * Returns the application build version.
     *
     * @return the application build version
     */
    public static String getBuildVersion() {
        return buildVersion;
    }

    /**
     * Returns the application build date.
     *
     * @return the application build date
     */
    public static String getBuildDate() {
        return buildDate;
    }

    /**
     * Returns a template from the file system. The file name must be
     * specified relative to the base directory used in the
     * initialize() call.
     *
     * @param path           the relative file name
     *
     * @return the template found
     *
     * @throws TemplateException if the template couldn't be read
     *             correctly
     */
    public static Template getFileTemplate(String path)
        throws TemplateException {

        try {
            return new Template(fileConfig.getTemplate(path));
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new TemplateException("couldn't read " + path, e);
        }
    }

    /**
     * Returns a template from a content page.
     *
     * @param user           the user performing the operation
     * @param page           the content page
     *
     * @return the template found
     *
     * @throws TemplateException if the template couldn't be read
     *             correctly
     */
    public static Template getPageTemplate(User user, ContentPage page)
        throws TemplateException {

        Template  template;
        String    name;
        String    message;

        try {
            pageLoader.setUser(user);
            pageLoader.setPage(page);
            if (page.getContentManager().isAdmin()) {
                name = "preview/" + page.getId() + "/root";
            } else {
                name = "normal/" + page.getId() + "/root";
            }
            template = new Template(pageConfig.getTemplate(name));
            return template;
        } catch (IOException e) {
            message = "couldn't read page " + page.getId();
            LOG.error(message + ": " + e.getMessage());
            throw new TemplateException(message, e);
        }
    }


    /**
     * A page template loader. This class provides the page elements
     * to the FreeMarker template engine. Before a template is loaded
     * with this loader, the corresponding content page must be set
     * with the setPage() method. This class is thread-safe, as it
     * uses a thread local storage for the pages.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    private static class PageLoader implements TemplateLoader {

        /**
         * The content pages to load. This is a thread local variable
         * as the loader needs to be thread-safe.
         */
        private ThreadLocal pages = new ThreadLocal();

        /**
         * The user performing the operation. This is a thread local
         * variable as the loader needs to be thread-safe.
         */
        private ThreadLocal users = new ThreadLocal();

        /**
         * Creates a new page template loader.
         */
        public PageLoader() {
            // No further initialization needed
        }

        /**
         * Returns the content page to load (for the calling thread).
         *
         * @return the content page to load
         */
        public ContentPage getPage() {
            return (ContentPage) pages.get();
        }

        /**
         * Sets the content page to load (for the calling thread).
         * This method should be called once before loading the
         * template.
         *
         * @param page           the content page to load, or null
         */
        public void setPage(ContentPage page) {
            pages.set(page);
        }

        /**
         * Returns the user performing the operation (for the calling
         * thread).
         *
         * @return the user performing the operation
         */
        public User getUser() {
            return (User) users.get();
        }

        /**
         * Sets the the user performing the operation (for the
         * calling thread). This method should be called once before
         * loading the template.
         *
         * @param user           the user performing the operation
         */
        public void setUser(User user) {
            users.set(user);
        }

        /**
         * Returns the page element data. The template name specified
         * consists of the page content identifier and the page
         * element name.
         *
         * @param name           the template name
         *
         * @return the page element data
         *
         * @throws IOException if the page element couldn't be read
         *             properly
         */
        public Object findTemplateSource(String name) throws IOException {
            ContentPage  page = getPage();
            String       elemName;
            String       message;

            // Check for missing page
            if (page == null) {
                message = "page not found for page element " + name;
                LOG.error(message);
                throw new IOException(message);
            }

            // Extract page element data
            try {
                elemName = name.substring(name.lastIndexOf("/") + 1);
                return page.getElement(getUser(), elemName);
            } catch (ContentException e) {
                message = "while reading page element " + name +
                          ": " + e.getMessage();
                LOG.error(message);
                throw new IOException(message);
            } catch (ContentSecurityException e) {
                message = "while reading page element " + name +
                          ": " + e.getMessage();
                LOG.error(message);
                throw new IOException(message);
            }
        }

        /**
         * Returns the last modification time for a template. This
         * method always returns the current system time to avoid
         * caching.
         *
         * @param source         the template source
         *
         * @return the current system time
         */
        public long getLastModified(Object source) {
            return System.currentTimeMillis();
        }

        /**
         * Returns a reader for a template.
         *
         * @param source         the template source
         * @param encoding       the suggested encoding (ignored)
         *
         * @return a reader for the page element data
         *
         * @throws IOException if a template couldn't be accessed
         *             properly
         */
        public Reader getReader(Object source, String encoding)
            throws IOException {

            if (source == null) {
                return new StringReader("");
            } else {
                return new StringReader(source.toString());
            }
        }

        /**
         * Closes the FreeMarker template. This method does nothing.
         *
         * @param source         the template source
         */
        public void closeTemplateSource(Object source) {
            // Nothing to do here
        }
    }
}
