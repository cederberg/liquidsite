/*
 * Site.java
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

/**
 * A web site.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Site {

    /**
     * The unique site id. This value is set to zero (0) for sites 
     * that are not present in the database. 
     */
    private int id = 0;

    /**
     * The site name.
     */
    private String name = "";
    
    /**
     * The site host name or IP.
     */
    private String host = "*";
    
    /**
     * The site port number.
     */
    private int port = 0;
    
    /**
     * The site base directory. This string always ends in '/'.
     */
    private String directory = "/";

    /**
     * Creates a new site with default values.
     */
    public Site() {
    }

    /**
     * Checks if this site equals another object. This method will 
     * only return true if the other object is a site with the same
     * unique site id.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the other object is an identical site, or
     *         false otherwise 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Site) {
            return id == ((Site) obj).id;
        } else {
            return false;
        }
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();
        
        buffer.append("Site: ");
        buffer.append(host);
        if (port != 0 && port != 80) {
            buffer.append(":");
            buffer.append(port); 
        }
        buffer.append(directory.substring(directory.length() - 1));
        return buffer.toString();
    }

    /**
     * Returns the unique site id.
     * 
     * @return the unique site id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the site id. This method is only used when reading sites
     * from the database.
     * 
     * @param id             the new site id
     */
    void setId(int id) {
        this.id = id;
    }
    
    /**
     * Returns the site name
     * 
     * @return the site name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the site name.
     * 
     * @param name           the new site name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns the host name or IP address.
     * 
     * @return the host name, or 
     *         "*" if any host matches this site
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Sets the host name or IP address. This value must be the fully 
     * qualified name with domain used in the URL:s to the site.
     * 
     * @param host           the new host name, or "*" for any
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * Returns the port number.
     * 
     * @return the port number, or
     *         zero (0) if any port matches this site
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Sets the port number.
     * 
     * @param port           the new port number, or zero (0) for any
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the base directory. The base directory always ends 
     * with a '/' character.
     * 
     * @return the base directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Sets the base directory.
     * 
     * @param directory      the new base directory
     */
    public void setDirectory(String directory) {
        if (directory == null) {
            this.directory = "/";
        } else {
            this.directory = directory;
        }        
        if (!this.directory.endsWith("/")) {
            this.directory += "/";
        }
    }

    /**
     * Checks if a set of request parameters matches this site.
     * 
     * @param host           the request host name (server name)
     * @param port           the request (server) port
     * @param uri            the request uri
     * 
     * @return true if all the parameters matches this site, or
     *         false otherwise
     */
    public boolean matches(String host, int port, String uri) {
        return matchesHost(host) 
            && matchesPort(port)
            && matchesDirectory(uri);
    }

    /**
     * Checks if a host (or server) name matches this site.
     * 
     * @param host           the request host name (server name)
     * 
     * @return true if the host name matches this site, or
     *         false otherwise
     */
    public boolean matchesHost(String host) {
        return this.host.equals("*") || this.host.equals(host);
    }
    
    /**
     * Checks if a (server) port number matches this site.
     * 
     * @param port           the request (server) port
     * 
     * @return true if the port number matches this site, or
     *         false otherwise
     */
    public boolean matchesPort(int port) {
        return this.port == 0 || this.port == port;
    }
    
    /**
     * Checks if a request URI matches this site.
     * 
     * @param uri            the request uri
     * 
     * @return true if the URI matches this site, or
     *         false otherwise
     */
    public boolean matchesDirectory(String uri) {
        return uri.startsWith(directory);
    }

    /**
     * Saves this site to the database.
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public void save() throws ContentException {
        if (id <= 0) {
            SitePeer.doInsert(this);
        } else {
            SitePeer.doUpdate(this);
        }
    }
}
