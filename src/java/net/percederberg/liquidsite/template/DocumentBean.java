/*
 * DocumentBean.java
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

import java.util.ArrayList;
import java.util.HashMap;

import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.content.DocumentProperty;
import net.percederberg.liquidsite.text.PlainFormatter;

/**
 * A document template bean. This class is used to access document
 * content from the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DocumentBean implements TemplateHashModel {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DocumentBean.class);

    /**
     * The bean context.
     */
    private BeanContext context;

    /**
     * The document being encapsulated.
     */
    private ContentDocument document;

    /**
     * The base section of the document being encapsulated. If set
     * the document name will have any additional sections between
     * the document and the base section added to the "name"
     * property.
     */
    private ContentSection baseSection;

    /**
     * The document meta-data template model.
     */
    private TemplateModel metadata = null;

    /**
     * Creates a new empty document template bean.
     */
    DocumentBean() {
        this(null, null, null);
    }

    /**
     * Creates a new document template bean from the request
     * environment document.
     *
     * @param context        the bean context
     */
    DocumentBean(BeanContext context) {
        this(context,
             context.getRequest().getEnvironment().getDocument(),
             null);
    }

    /**
     * Creates a new document template bean.
     *
     * @param context        the bean context
     * @param document       the content document, or null
     * @param baseSection    the base section for the document
     */
    DocumentBean(BeanContext context,
                 ContentDocument document,
                 ContentSection baseSection) {

        this.context = context;
        this.document = document;
        this.baseSection = baseSection;
    }

    /**
     * Checks if the hash model is empty.
     *
     * @return false as the hash model is never empty
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns a document property as a template model. The special
     * properties "name" and "meta" return the document name and
     * meta-data object respectively.
     *
     * @param id             the document property id
     *
     * @return the template model object, or
     *         null if the document property wasn't found
     *
     * @throws TemplateModelException if an appropriate template model
     *             couldn't be created
     */
    public TemplateModel get(String id) throws TemplateModelException {
        Object  obj;
        String  str;
        int     type;

        if (id.equals("meta")) {
            if (metadata == null) {
                obj = new DocumentMetaDataBean(context, document);
                metadata = ObjectWrapper.BEANS_WRAPPER.wrap(obj);
            }
            return metadata;
        } else if (document == null) {
            return new SimpleScalar("");
        } else if (id.equals("name")) {
            try {
                return new SimpleScalar(getName(document));
            } catch (ContentException e) {
                LOG.error(e.getMessage());
                return new SimpleScalar("");
            }
        } else {
            str = document.getProperty(id);
            type = document.getPropertyType(id);
            if (type == DocumentProperty.TAGGED_TYPE) {
                str = processTaggedText(str);
            } else if (type == DocumentProperty.HTML_TYPE) {
                str = processHtmlText(str);
            } else {
                str = PlainFormatter.formatHtml(str);
            }
            return new SimpleScalar(str);
        }
    }

    /**
     * Returns the name of a content object. The name will include
     * any document section up to the base section.
     *
     * @param content        the content object
     *
     * @return the content object name
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getName(Content content)
        throws ContentException {

        if (content instanceof ContentSection) {
            if (baseSection.equals(content)) {
                return "";
            } else {
                return getName(content.getParent()) +
                       content.getName() + "/";
            }
        } else if (content instanceof ContentDocument) {
            if (baseSection == null) {
                return document.getName();
            } else {
                return getName(document.getParent()) +
                       document.getName();
            }
        } else {
            return "";
        }
    }

    /**
     * Processes an HTML text string. This method will adjust any
     * links and image sources.
     *
     * @param text           the HTML text to process
     *
     * @return the HTML encoded string
     */
    private String processHtmlText(String text) {
        StringBuffer  buffer = new StringBuffer();
        int           start;
        int           end;

        while ((start = text.indexOf("<")) >= 0) {
            end = text.indexOf(">", start);
            buffer.append(text.substring(0, start));
            buffer.append(processHtmlTag(text.substring(start, end)));
            text = text.substring(end);
        }
        buffer.append(text);
        return buffer.toString();
    }

    /**
     * Processes an HTML tag. This method will adjust any links and
     * image sources.
     *
     * @param tag            the HTML tag
     *
     * @return the modified HTML tag
     */
    private String processHtmlTag(String tag) {
        int  start = 0;
        int  end;

        // Find link start position
        if (tag.startsWith("<a ")) {
            start = tag.indexOf(" href=");
            if (start > 0) {
                start += 6;
            }
        } else if (tag.startsWith("<img ")) {
            start = tag.indexOf(" src=");
            if (start > 0) {
                start += 5;
            }
        }

        // Adjust link
        if (start > 0) {
            end = tag.indexOf(" ", start);
            if (end <= 0) {
                end = tag.length() - 1;
            }
            if (tag.charAt(start) == '"') {
                start++;
            }
            if (tag.charAt(end - 1) == '"') {
                end--;
            }
            return tag.substring(0, start) +
                   linkTo(tag.substring(start, end)) +
                   tag.substring(end);
        } else {
            return tag;
        }
    }

    /**
     * Processes a tagged text string. This method will insert HTML
     * paragraph and line breaks, while also adjusting links and other
     * special tags.
     *
     * @param text           the text string to process
     *
     * @return the HTML encoded string
     */
    private String processTaggedText(String text) {
        ArrayList     paragraphs = new ArrayList();
        StringBuffer  buffer = new StringBuffer();
        String        str;
        char          c;

        // Trim and remove carriage return characters
        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            if (c != '\r') {
                buffer.append(c);
            }
        }
        text = buffer.toString().trim();

        // Split into paragraphs
        while (text.indexOf("\n\n") >= 0) {
            str = text.substring(0, text.indexOf("\n\n")).trim();
            if (str.length() > 0) {
                paragraphs.add(str);
            }
            text = text.substring(text.indexOf("\n\n") + 2);
        }
        if (text.length() > 0) {
            paragraphs.add(text);
        }

        // Process paragraphs
        buffer = new StringBuffer();
        for (int i = 0; i < paragraphs.size(); i++) {
            str = (String) paragraphs.get(i);
            buffer.append(processTaggedParagraph(str));
            buffer.append("\n\n");
        }

        return buffer.toString();
    }

    /**
     * Processes a tagged paragraph string. This method will insert
     * HTML paragraph and line breaks, while also adjusting links and
     * other special tags.
     *
     * @param str            the string to process
     *
     * @return the HTML encoded string
     */
    private String processTaggedParagraph(String str) {
        StringBuffer  buffer = new StringBuffer();
        boolean       isBlock;
        boolean       isList;
        char          c;
        int           pos;

        isBlock = startsWithBlockTag(str);
        isList = str.startsWith("<list");
        if (!isBlock && !isList) {
            buffer.append("<p>");
        }
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            switch (c) {
            case '<':
                pos = str.indexOf(">", i);
                if (pos > 0) {
                    buffer.append(processTag(str.substring(i, pos + 1)));
                    i = pos;
                } else {
                    // Discard
                    i = str.length();
                }
                break;
            case '>':
                // Discard
                break;
            case '&':
                buffer.append("&amp;");
                break;
            case '\n':
                if (isList) {
                    buffer.append("\n");
                } else {
                    buffer.append("<br/>\n");
                }
                break;
            case '\r':
                // Discard
                break;
            default:
                buffer.append(c);
            }
        }
        if (!isBlock && !isList) {
            buffer.append("</p>");
        }

        return buffer.toString();
    }

    /**
     * Processes a tag. This method will insert the HTML code
     * corresponding to the tag.
     *
     * @param tag             the tag to process
     *
     * @return the HTML encoded result
     */
    private String processTag(String tag) {
        String   name;
        HashMap  attrs;
        int      pos;
        String   str;

        // Parse tag
        pos = tag.indexOf(" ");
        if (pos > 0) {
            name = tag.substring(1, pos);
            attrs = processTagAttributes(tag.substring(pos, tag.length() - 1));
        } else {
            name = tag.substring(1, tag.length() - 1);
            attrs = new HashMap(0);
        }
        if (name.equals("p") || name.equals("/p")
         || name.equals("h1") || name.equals("/h1")
         || name.equals("h2") || name.equals("/h2")
         || name.equals("h3") || name.equals("/h3")
         || name.equals("b") || name.equals("/b")
         || name.equals("i") || name.equals("/i")) {

            return "<" + name + ">";
        } else if (name.equals("link")) {
            str = (String) attrs.get("window");
            if (str != null && str.equals("new")) {
                return "<a href=\"" + linkTo((String) attrs.get("url")) +
                       "\" target=\"_new\">";
            } else {
                return "<a href=\"" + linkTo((String) attrs.get("url")) +
                       "\">";
            }
        } else if (name.equals("/link")) {
            return "</a>";
        } else if (name.equals("image")) {
            str = (String) attrs.get("layout");
            if (str != null && str.equals("right")) {
                return "<img src=\"" + linkTo((String) attrs.get("url")) +
                       "\" style=\"float: right;\" />";
            } else if (str != null && str.equals("left")) {
                return "<img src=\"" + linkTo((String) attrs.get("url")) +
                       "\" style=\"float: left;\" />";
            } else {
                return "<img src=\"" + linkTo((String) attrs.get("url")) +
                       "\" />";
            }
        } else if (name.equals("list")) {
            str = (String) attrs.get("type");
            if (str != null && str.equals("*")) {
                return "<ul style=\"list-style-type: disc;\">";
            } else if (str != null && str.equals("1")) {
                return "<ul style=\"list-style-type: decimal;\">";
            } else if (str != null && str.equals("i")) {
                return "<ul style=\"list-style-type: lower-roman;\">";
            } else if (str != null && str.equals("I")) {
                return "<ul style=\"list-style-type: upper-roman;\">";
            } else if (str != null && str.equals("a")) {
                return "<ul style=\"list-style-type: lower-alpha;\">";
            } else if (str != null && str.equals("A")) {
                return "<ul style=\"list-style-type: upper-alpha;\">";
            } else {
                return "<ul>";
            }
        } else if (name.equals("/list")) {
            return "</ul>";
        } else if (name.equals("item")) {
            return "<li>";
        } else {
            return "";
        }
    }

    /**
     * Processes a tag attribute string. This method extracts all the
     * attributes and their values from the string and returns the
     * mappings in a hash map.
     *
     * @param attrs          the tag attribute string
     *
     * @return the hash map with attribute names and values
     */
    private HashMap processTagAttributes(String attrs) {
        HashMap  result = new HashMap();
        String   name;
        String   value;
        String   str;
        int      pos;

        attrs = attrs.trim();
        while (attrs.length() > 0) {
            pos = attrs.indexOf(" ");
            if (pos > 0) {
                str = attrs.substring(0, pos);
                attrs = attrs.substring(pos).trim();
            } else {
                str = attrs;
                attrs = "";
            }
            pos = str.indexOf("=");
            if (pos > 0) {
                name = str.substring(0, pos);
                value = str.substring(pos + 1);
            } else {
                name = str;
                value = "";
            }
            if (value.startsWith("\"")) {
                value = value.substring(1);
            }
            if (value.endsWith("\"")) {
                value = value.substring(0, value.length() - 1);
            }
            result.put(name, value);
        }
        return result;
    }

    /**
     * Checks if a string starts with an HTML block tag.
     *
     * @return true if the string starts with a block tag, or
     *         false otherwise
     */
    private boolean startsWithBlockTag(String str) {
        return str.startsWith("<p>")
            || str.startsWith("<h1>")
            || str.startsWith("<h2>")
            || str.startsWith("<h3>");
    }

    /**
     * Returns a relative link to an object. If the specified path
     * starts with '/' it is assumed to be relative to the site root
     * directory, otherwise it is assumed to be relative to the
     * document directory.
     *
     * @param path           the site- or document-relative link path
     *
     * @return the path relative to the request path
     */
    private String linkTo(String path) {
        if (path.indexOf(":") >= 0) {
            return path;
        } else if (path.startsWith("/")) {
            return context.getSitePath() + path.substring(1);
        } else if (baseSection == null) {
            return path;
        } else {
            // TODO: use translator path if possible
            return context.getSitePath() + "liquidsite.obj/" + 
                   document.getId() + "/" + path;
        }
    }
}
