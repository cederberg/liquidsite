/*
 * TaggedFormatter.java
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

package net.percederberg.liquidsite.text;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A tagged text formatter. This class contains static methods for
 * processing tagged text.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class TaggedFormatter {

    /**
     * Formats a tagged text string in HTML. This method will resolve
     * any links in the tagged text and convert the tags to valid HTML
     * tags.
     *
     * @param text           the tagged text string
     * @param context        the formatting context
     *
     * @return the HTML encoded text
     */
    public static String formatHtml(String text, FormattingContext context) {
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
            buffer.append(formatHtmlParagraph(str, context));
            buffer.append("\n\n");
        }

        return buffer.toString();
    }

    /**
     * Formats a tagged paragraph in HTML. This method will insert
     * HTML paragraph and line breaks, while also adjusting links and
     * other special tags.
     *
     * @param str            the string to process
     * @param context        the formatting context
     *
     * @return the HTML encoded string
     */
    private static String formatHtmlParagraph(String str,
                                              FormattingContext context) {

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
                    buffer.append(formatHtmlTag(str.substring(i, pos + 1),
                                                context));
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
     * Formats a tag in HTML. This method will return the HTML code
     * corresponding to the tag.
     *
     * @param tag            the tag to process
     * @param context        the formatting context
     *
     * @return the HTML encoded result
     */
    private static String formatHtmlTag(String tag,
                                        FormattingContext context) {

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
                return "<a href=\"" +
                       context.linkTo((String) attrs.get("url")) +
                       "\" target=\"_new\">";
            } else {
                return "<a href=\"" +
                       context.linkTo((String) attrs.get("url")) +
                       "\">";
            }
        } else if (name.equals("/link")) {
            return "</a>";
        } else if (name.equals("image")) {
            str = (String) attrs.get("layout");
            if (str != null && str.equals("right")) {
                return "<img src=\"" +
                       context.linkTo((String) attrs.get("url")) +
                       "\" style=\"float: right;\" />";
            } else if (str != null && str.equals("left")) {
                return "<img src=\"" +
                       context.linkTo((String) attrs.get("url")) +
                       "\" style=\"float: left;\" />";
            } else {
                return "<img src=\"" +
                       context.linkTo((String) attrs.get("url")) +
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
    private static HashMap processTagAttributes(String attrs) {
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
    private static boolean startsWithBlockTag(String str) {
        return str.startsWith("<p>")
            || str.startsWith("<h1>")
            || str.startsWith("<h2>")
            || str.startsWith("<h3>");
    }
}
