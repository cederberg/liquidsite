/*
 * RequestEnvironment.java
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

package net.percederberg.liquidsite.web;

import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentPage;
import net.percederberg.liquidsite.content.ContentSite;
import net.percederberg.liquidsite.content.Domain;

/**
 * The request processing environment. This class is used to store
 * objects used during the request processing.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class RequestEnvironment {

    /**
     * The domain object.
     */
    private Domain domain = null;

    /**
     * The content site.
     */
    private ContentSite site = null;

    /**
     * The content page.
     */
    private ContentPage page = null;

    /**
     * The content document.
     */
    private ContentDocument doc = null;

    /**
     * Creates a new request environment.
     */
    public RequestEnvironment() {
    }

    /**
     * Returns the domain object.
     *
     * @return the domain object
     */
    public Domain getDomain() {
        return domain;
    }

    /**
     * Sets the domain object.
     *
     * @param domain         the domain object
     */
    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    /**
     * Returns the content site.
     *
     * @return the content site
     */
    public ContentSite getSite() {
        return site;
    }

    /**
     * Sets the content site.
     *
     * @param site           the content site
     */
    public void setSite(ContentSite site) {
        this.site = site;
    }

    /**
     * Returns the content page.
     *
     * @return the content page
     */
    public ContentPage getPage() {
        return page;
    }

    /**
     * Sets the content page.
     *
     * @param page           the content page
     */
    public void setPage(ContentPage page) {
        this.page = page;
    }

    /**
     * Returns the content document.
     *
     * @return the content document
     */
    public ContentDocument getDocument() {
        return doc;
    }

    /**
     * Sets the content document.
     *
     * @param doc            the content document
     */
    public void setDocument(ContentDocument doc) {
        this.doc = doc;
    }
}
