/*
 * DocumentFormattingContext.java
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

package net.percederberg.liquidsite.template;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.text.FormattingContext;
import net.percederberg.liquidsite.web.RequestEnvironment;

/**
 * A document formatting context. This class is used when formatting
 * document data.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class DocumentFormattingContext implements FormattingContext {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DocumentFormattingContext.class);

    /**
     * The bean context.
     */
    private BeanContext context;

    /**
     * The document relative path. This is the relative path to the
     * document, and may be empty if the request refers to an object
     * in the document directory. Otherwise the path ends with an "/"
     * character.
     */
    private String docPath = "";

    /**
     * Creates a new document context with the specified relative
     * document path.
     *
     * @param context        the bean context
     * @param document       the content document, or null
     */
    DocumentFormattingContext(BeanContext context, ContentDocument document) {
        this.context = context;
        this.docPath = getDocPath(document);
    }

    /**
     * Returns the relative path to the document directory. The path
     * is relative to the request URL and may be empty if the request
     * refers to an object in the document directory. Otherwise the
     * path ends with an "/" character.
     *
     * @param document       the content document, or null
     *
     * @return the relative path to the document directory
     */
    private String getDocPath(ContentDocument document) {
        String  path;

        if (document == null) {
            return "";
        } else {
            path = getDocEnvPath(document);
            if (path == null) {
                path = context.getSitePath() + "liquidsite.obj/" +
                       document.getId() + "/";
            }
            return path;
        }
    }

    /**
     * Returns the relative path to the document directory in the
     * request environment. If the document or one of it's parent
     * sections aren't found in the request environment, null is
     * returned. The path is relative to the request URL and may be
     * empty if the request environment points to the document.
     * Otherwise the path ends with an "/" character.
     *
     * @param content        the content document
     *
     * @return the relative path to the document directory, or
     *         null if not in the request environment
     */
    private String getDocEnvPath(Content content) {
        RequestEnvironment  env = context.getRequest().getEnvironment();
        ContentSection      section = env.getSection();
        ContentDocument     doc = env.getDocument();
        String              path = "";

        if (doc != null) {
            if (doc.getId() == content.getId()) {
                return "";
            }
        } else if (section != null) {
            while (content != null) {
                path = content.getName() + "/" + path;
                try {
                    content = content.getParent();
                } catch (ContentException e) {
                    LOG.error(e.getMessage());
                    return null;
                }
                if (section.getId() == content.getId()) {
                    return path;
                }
            }
        }
        return null;
    }

    /**
     * Returns a relative link to an object. If the specified path
     * starts with '/' it is assumed to be relative to the site root
     * directory and will be modified to a page relative link. If the
     * link starts with a protocol (for example "http:") it is
     * considered absolute and will not be modified. Otherwise it is
     * assumed to be relative to the document and may be modified to
     * point to the document directory.
     *
     * @param link           the link URL
     *
     * @return the resolved URL link
     */
    public String linkTo(String link) {
        if (link.indexOf(":") >= 0) {
            return link;
        } else if (link.startsWith("/")) {
            return context.getSitePath() + link.substring(1);
        } else {
            return docPath + link;
        }
    }
}
