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

import org.liquidsite.core.content.ContentDocument;
import org.liquidsite.core.content.ContentForum;
import org.liquidsite.core.content.ContentPage;
import org.liquidsite.core.content.ContentSection;
import org.liquidsite.core.content.ContentSite;
import org.liquidsite.core.content.ContentTopic;
import org.liquidsite.core.content.ContentTranslator;
import org.liquidsite.core.content.Domain;

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
     * The content translator.
     */
    private ContentTranslator translator = null;

    /**
     * The content page.
     */
    private ContentPage page = null;

    /**
     * The content section.
     */
    private ContentSection section = null;

    /**
     * The content document.
     */
    private ContentDocument doc = null;

    /**
     * The content forum.
     */
    private ContentForum forum = null;

    /**
     * The content topic.
     */
    private ContentTopic topic = null;

    /**
     * Creates a new request environment.
     */
    public RequestEnvironment() {
        // No further initialization needed
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
     * Returns the content translator.
     *
     * @return the content translator
     */
    public ContentTranslator getTranslator() {
        return translator;
    }

    /**
     * Sets the content translator.
     *
     * @param translator     the content translator
     */
    public void setTranslator(ContentTranslator translator) {
        this.translator = translator;
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
     * Returns the content section.
     *
     * @return the content section
     */
    public ContentSection getSection() {
        return section;
    }

    /**
     * Sets the content section.
     *
     * @param section        the content section
     */
    public void setSection(ContentSection section) {
        this.section = section;
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

    /**
     * Returns the content forum.
     *
     * @return the content forum
     */
    public ContentForum getForum() {
        return forum;
    }

    /**
     * Sets the content forum.
     *
     * @param forum          the content forum
     */
    public void setForum(ContentForum forum) {
        this.forum = forum;
    }

    /**
     * Returns the content topic.
     *
     * @return the content topic
     */
    public ContentTopic getTopic() {
        return topic;
    }

    /**
     * Sets the content topic.
     *
     * @param topic          the content topic
     */
    public void setTopic(ContentTopic topic) {
        this.topic = topic;
    }
}
