/*
 * SiteBean.java
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
 * Copyright (c) 2006 Per Cederberg. All rights reserved.
 */

package org.liquidsite.app.template;

import org.liquidsite.core.content.ContentSite;
import org.liquidsite.core.web.Request;

/**
 * A site template bean. This class is used to access sites from
 * the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class SiteBean extends ContentBean {

    /**
     * Creates a new site template bean based on the request
     * site.
     *
     * @param context        the bean context
     */
    SiteBean(BeanContext context) {
        this(context, context.getRequest().getEnvironment().getSite());
    }

    /**
     * Creates a new forum template bean.
     *
     * @param context        the bean context
     * @param forum          the content forum, or null
     */
    SiteBean(BeanContext context, ContentSite site) {
        super(context, site);
    }

    /**
     * Returns the site protocol. This string is identical to the protocol
     * specified in the URL, i.e. "http" or "https".
     *
     * @return the site protocol
     */
    public String getProtocol() {
        return ((ContentSite) getContent()).getProtocol();
    }

    /**
     * Returns the site host name. Note that if this site has a default
     * host name, the current request host name will be returned.
     *
     * @return the site or request host name
     */
    public String getHost() {
        ContentSite site = (ContentSite) getContent();

        if (site.getHost().equals("*")) {
            return getContextRequest().getHost();
        } else {
            return site.getHost();
        }
    }

    /**
     * Returns the site port number. Note that if this site has a
     * default port number, the current request port number will be
     * returned.
     *
     * @return the site or request port number
     */
    public int getPort() {
        ContentSite site = (ContentSite) getContent();

        if (site.getPort() == 0) {
            return getContextRequest().getPort();
        } else {
            return site.getPort();
        }
    }

    /**
     * Returns the site base directory.
     *
     * @return the site base directory
     */
    public String getDirectory() {
        return ((ContentSite) getContent()).getDirectory();
    }

    /**
     * Checks if this site is an admin site.
     *
     * @return true if this is an admin site, or
     *         false otherwise
     */
    public boolean isAdmin() {
        return ((ContentSite) getContent()).isAdmin();
    }

    /**
     * Returns a link to an object in the site. All paths must be
     * relative to the root directory of the site. Also, this method
     * uses the request host name and port numbers if this site uses
     * default values.
     *
     * @param path           the site-relative link path
     *
     * @return the URL to the site path
     */
    protected String linkTo(String path) {
        // TODO: either remove this method or find some use for it
        ContentSite site = (ContentSite) getContent();
        Request     request = getContextRequest();
        String      protocol = request.getProtocol();
        String      host = request.getHost();
        int         port = request.getPort();
        String      dir = request.getPath();
        String      base;

        if (site.isMatch(protocol, host, port, dir)) {
            base = getContext().getRelativePath(site.getDirectory());
        } else if (site.isMatch(protocol, host, port)) {
            base = getContext().getRelativePath("/") + site.getDirectory();
        } else {
            protocol = getProtocol();
            host = getHost();
            port = getPort();
            dir = getDirectory();
            base = protocol + "://" + host;
            if (port == 80 && protocol.equals("http")) {
                // Skip printing port
            } else if (port == 443 && protocol.equals("https")) {
                // Skip printing port
            } else {
                base += ":" + port;
            }
            base += dir;
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return base + path;
    }
}
