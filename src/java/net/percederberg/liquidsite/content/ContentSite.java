/*
 * ContentSite.java
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

package net.percederberg.liquidsite.content;

import java.util.ArrayList;

import org.liquidsite.core.data.ContentData;
import org.liquidsite.core.data.ContentPeer;
import org.liquidsite.core.data.ContentQuery;
import org.liquidsite.core.data.DataObjectException;
import org.liquidsite.core.data.DataSource;
import org.liquidsite.util.log.Log;

/**
 * A web site.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentSite extends Content {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(ContentSite.class);

    /**
     * The protocol content attribute.
     */
    private static final String PROTOCOL_ATTRIBUTE = "PROTOCOL";

    /**
     * The host content attribute.
     */
    private static final String HOST_ATTRIBUTE = "HOST";

    /**
     * The port content attribute.
     */
    private static final String PORT_ATTRIBUTE = "PORT";

    /**
     * The directory content attribute.
     */
    private static final String DIRECTORY_ATTRIBUTE = "DIRECTORY";

    /**
     * The flags content attribute.
     */
    private static final String FLAGS_ATTRIBUTE = "FLAGS";

    /**
     * The administration site flag.
     */
    private static final int ADMIN_FLAG = 1;

    /**
     * Returns an array of content site in the specified domain. Only
     * the latest revision of each content object will be returned.
     * This method differs from the results returned by a search with
     * findByCategory() in that offline objects can also be returned.
     *
     * @param manager        the content manager to use
     * @param domain         the domain
     *
     * @return an array of content site objects found
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected static ContentSite[] findByDomain(ContentManager manager,
                                                Domain domain)
        throws ContentException {

        DataSource     src = getDataSource(manager);
        ContentQuery   query;
        ArrayList      list;
        ContentData    data;
        ContentSite[]  res;

        try {
            query = new ContentQuery(domain.getName());
            query.requireParent(0);
            query.requireCategory(SITE_CATEGORY);
            query.requirePublished(!manager.isAdmin());
            list = ContentPeer.doSelectByQuery(src, query);
            res = new ContentSite[list.size()];
            for (int i = 0; i < list.size(); i++) {
                data = (ContentData) list.get(i);
                res[i] = new ContentSite(manager, data, src);
            }
            return res;
        } catch (DataObjectException e) {
            LOG.error(e.getMessage());
            throw new ContentException(e);
        } finally {
            src.close();
        }
    }

    /**
     * Creates a new site with default values.
     *
     * @param manager        the content manager to use
     * @param domain         the site domain
     */
    public ContentSite(ContentManager manager, Domain domain) {
        super(manager, domain, Content.SITE_CATEGORY);
        setAttribute(PROTOCOL_ATTRIBUTE, "http");
        setAttribute(HOST_ATTRIBUTE, "*");
        setAttribute(PORT_ATTRIBUTE, "0");
        setAttribute(DIRECTORY_ATTRIBUTE, "/");
        setAttribute(FLAGS_ATTRIBUTE, "0");
    }

    /**
     * Creates a new site.
     *
     * @param manager        the content manager to use
     * @param data           the content data object
     * @param src            the data source to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ContentSite(ContentManager manager,
                          ContentData data,
                          DataSource src)
        throws ContentException {

        super(manager, data, src);
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();
        int           port = getPort();
        String        dir = getDirectory();

        buffer.append(getProtocol());
        buffer.append("://");
        if (getHost().equals("*")) {
            buffer.append("<*>");
        } else {
            buffer.append(getHost());
        }
        if (port == 0) {
            buffer.append(":<*>");
        } else if (port != 80) {
            buffer.append(":");
            buffer.append(port);
        }
        buffer.append(dir);
        return buffer.toString();
    }

    /**
     * Returns the site protocol
     *
     * @return the site protocol
     */
    public String getProtocol() {
        return getAttribute(PROTOCOL_ATTRIBUTE);
    }

    /**
     * Sets the site protocol.
     *
     * @param protocol       the new site protocol
     */
    public void setProtocol(String protocol) {
        setAttribute(PROTOCOL_ATTRIBUTE, protocol);
    }

    /**
     * Returns the host name or IP address.
     *
     * @return the host name, or
     *         "*" if any host matches this site
     */
    public String getHost() {
        return getAttribute(HOST_ATTRIBUTE);
    }

    /**
     * Sets the host name or IP address. This value must be the fully
     * qualified name with domain used in the URL:s to the site.
     *
     * @param host           the new host name, or "*" for any
     */
    public void setHost(String host) {
        setAttribute(HOST_ATTRIBUTE, host);
    }

    /**
     * Returns the port number.
     *
     * @return the port number, or
     *         zero (0) if any port matches this site
     */
    public int getPort() {
        return Integer.parseInt(getAttribute(PORT_ATTRIBUTE));
    }

    /**
     * Sets the port number.
     *
     * @param port           the new port number, or zero (0) for any
     */
    public void setPort(int port) {
        setAttribute(PORT_ATTRIBUTE, String.valueOf(port));
    }

    /**
     * Returns the base directory. The base directory always starts
     * and ends with a '/' character.
     *
     * @return the base directory
     */
    public String getDirectory() {
        return getAttribute(DIRECTORY_ATTRIBUTE);
    }

    /**
     * Sets the base directory.
     *
     * @param directory      the new base directory
     */
    public void setDirectory(String directory) {
        if (directory == null) {
            directory = "/";
        }
        if (!directory.startsWith("/")) {
            directory = "/" + directory;
        }
        if (!directory.endsWith("/")) {
            directory += "/";
        }
        setAttribute(DIRECTORY_ATTRIBUTE, directory);
    }

    /**
     * Checks if the admin flag is set.
     *
     * @return true if the admin flag is set, or
     *         false otherwise
     */
    public boolean isAdmin() {
        int flags = Integer.parseInt(getAttribute(FLAGS_ATTRIBUTE));

        return (flags & ADMIN_FLAG) > 0;
    }

    /**
     * Sets the admin flag.
     *
     * @param admin          the new admin flag
     */
    public void setAdmin(boolean admin) {
        int flags = Integer.parseInt(getAttribute(FLAGS_ATTRIBUTE));

        if (admin) {
            flags = (flags | ADMIN_FLAG);
        } else {
            flags = (flags & ~ADMIN_FLAG);
        }
        setAttribute(FLAGS_ATTRIBUTE, String.valueOf(flags));
    }

    /**
     * Matches a set of request parameters to this site. This method
     * returns a numeric value that is higher for better matches. If
     * the request parameters didn't match at all, zero (0) is always
     * returned. The matching algorithm gives priority to exact
     * matches for host and port, and longer matches for paths. An
     * exact match for host or port is also always preferred over a
     * longer path match.
     *
     * @param protocol       the request protocol
     * @param host           the request host name (server name)
     * @param port           the request (server) port
     * @param path           the request path
     *
     * @return the match value (higher is better), or
     *         zero (0) for no match
     */
    public int match(String protocol, String host, int port, String path) {
        int  result = 0;

        // Check protocol match
        if (!getProtocol().equals(protocol)) {
            return 0;
        }

        // Check host match
        if (getHost().equals(host)) {
            result += 10000;
        } else if (getHost().equals("*")) {
            result += 0;
        } else {
            return 0;
        }

        // Check port match
        if (getPort() == port) {
            result += 1000;
        } else if (getPort() == 0) {
            result += 0;
        } else {
            return 0;
        }

        // Check directory match
        if (path.startsWith(getDirectory())) {
            result += getDirectory().length();
        } else {
            return 0;
        }

        return result;
    }

    /**
     * Validates the object data before writing to the database.
     *
     * @throws ContentException if the object data wasn't valid
     */
    protected void doValidate() throws ContentException {
        Content[]    children;
        ContentSite  site;

        super.doValidate();
        if (!getProtocol().equals("http") && !getProtocol().equals("https")) {
            throw new ContentException("protocol must be 'http' or 'https'");
        } else if (getHost().equals("")) {
            throw new ContentException("no host name set for site");
        }
        if (getParent() != null) {
            throw new ContentException("site cannot be located inside " +
                                       "another site or folder");
        }
        children = InternalContent.findByParent(getContentManager(),
                                                getDomain());
        for (int i = 0; i < children.length; i++) {
            if (children[i].getId() != getId()
             && children[i].getName().equals(getName())) {

                throw new ContentException(
                    "another object with the same name already exists");
            }
            if (children[i] instanceof ContentSite) {
                site = (ContentSite) children[i];
                if (site.getId() != getId()
                 && site.getProtocol().equals(getProtocol())
                 && site.getHost().equals(getHost())
                 && site.getPort() == getPort()
                 && site.getDirectory().equals(getDirectory())) {

                    throw new ContentException(
                        "an identical site definition already exists");
                }
            }
        }
    }
}
