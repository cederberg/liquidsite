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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.content;

import java.util.ArrayList;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.ContentData;
import net.percederberg.liquidsite.dbo.ContentPeer;
import net.percederberg.liquidsite.dbo.DatabaseObjectException;

/**
 * A web site.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentSite extends Content {

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
     * Returns an array of all domain sites in the database.
     *
     * @param manager        the content manager to use
     * @param domain         the domain
     * 
     * @return an array of all domain sites in the database
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    static ContentSite[] findByDomain(ContentManager manager, Domain domain) 
        throws ContentException {

        DatabaseConnection  con = getDatabaseConnection(manager);
        ArrayList           list;
        ContentSite[]       res;

        try {
            list = ContentPeer.doSelectByCategory(domain.getName(),
                                                  Content.SITE_CATEGORY,
                                                  con);
            res = new ContentSite[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = new ContentSite((ContentData) list.get(i), true, con);
            }
        } catch (DatabaseObjectException e) {
            throw new ContentException(e);
        } finally {
            returnDatabaseConnection(manager, con);
        }
        return res;
    }

    /**
     * Creates a new site with default values.
     * 
     * @param domain         the site domain
     */
    public ContentSite(Domain domain) {
        super(domain, Content.SITE_CATEGORY);
        setAttribute(PROTOCOL_ATTRIBUTE, "http");
        setAttribute(HOST_ATTRIBUTE, "*");
        setAttribute(PORT_ATTRIBUTE, "0");
        setAttribute(DIRECTORY_ATTRIBUTE, "/");
        setAttribute(FLAGS_ATTRIBUTE, "0");
    }

    /**
     * Creates a new site.
     * 
     * @param data           the content data object
     * @param latest         the latest revision flag
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected ContentSite(ContentData data, 
                          boolean latest, 
                          DatabaseConnection con) 
        throws ContentException {

        super(data, latest, con);
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
     * Validates this data object. This method checks that all 
     * required fields have been filled with suitable values.
     * 
     * @throws ContentException if the data object contained errors
     */
    public void validate() throws ContentException {
        ContentSite[]  sites = findByDomain(getContentManager(), getDomain());

        super.validate();
        if (!getProtocol().equals("http") && !getProtocol().equals("https")) {
            throw new ContentException("protocol must be 'http' or 'https'");
        } else if (getHost().equals("")) {
            throw new ContentException("no host name set for site");
        }
        for (int i = 0; i < sites.length; i++) {
            if (sites[i].getId() != getId()
             && sites[i].getProtocol().equals(getProtocol())
             && sites[i].getHost().equals(getHost())
             && sites[i].getPort() == getPort()
             && sites[i].getDirectory().equals(getDirectory())) {

                throw new ContentException("an identical site is already " +
                                           "in the database");
            }
        }
    }
}
