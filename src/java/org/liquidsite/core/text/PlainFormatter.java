/*
 * PlainFormatter.java
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

package org.liquidsite.core.text;

/**
 * A plain text formatter. This class contains static methods for
 * processing plain text to HTML.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PlainFormatter {

    /**
     * Cleans a plain text string from excessive characters.
     *
     * @param text           the plain text string
     *
     * @return the cleaned text string
     */
    public static String clean(String text) {
        StringBuffer  buffer = new StringBuffer();
        char          c;

        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            if (c != '\r') {
                buffer.append(c);
            }
        }
        return buffer.toString().trim();
    }

    /**
     * Escapes HTML constructs in a string.
     *
     * @param str            the string to process
     *
     * @return the HTML escaped string
     */
    public static String escapeHtml(String str) {
        StringBuffer  buffer = new StringBuffer();
        char          c;

        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            switch (c) {
            case '<':
                buffer.append("&lt;");
                break;
            case '>':
                buffer.append("&gt;");
                break;
            case '&':
                buffer.append("&amp;");
                break;
            case '@':
                buffer.append("&#64;");
                break;
            default:
                buffer.append(c);
            }
        }
        return buffer.toString();
    }

    /**
     * Formats a plain text string in HTML. This method will escape
     * any occurencies of special HTML characters, insert HTML
     * linebreaks instead of normal line breaks and create links for
     * any absolute URLs found.
     *
     * @param text           the plain text string
     *
     * @return the HTML encoded text
     */
    public static String formatHtml(String text) {
        StringBuffer  buffer = new StringBuffer();
        String        link;
        char          c;

        text = text.trim();
        for (int i = 0; i < text.length(); i++) {
            link = getLink(text, i);
            if (link != null) {
                i += link.length() - 1;
                link = escapeHtml(link);
                buffer.append("<a href=\"");
                buffer.append(link);
                buffer.append("\">");
                buffer.append(link);
                buffer.append("</a>");
            } else {
                c = text.charAt(i);
                switch (c) {
                case '<':
                    buffer.append("&lt;");
                    break;
                case '>':
                    buffer.append("&gt;");
                    break;
                case '&':
                    buffer.append("&amp;");
                    break;
                case '\n':
                    buffer.append("<br/>");
                    break;
                case '\r':
                    // Discard
                    break;
                default:
                    buffer.append(c);
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Returns the link starting at the specified offset.
     *
     * @param str            the text string
     * @param offset         the string offset
     *
     * @return the link URL starting at the specified offset, or
     *         null if no link started at the offset
     */
    private static String getLink(String str, int offset) {
        int  pos;

        if (!str.startsWith("http:", offset)
         && !str.startsWith("https:", offset)) {

            return null;
        }
        for (pos = offset + 1; pos < str.length(); pos++) {
            if (str.charAt(pos) == ' '
             || str.charAt(pos) == '\t'
             || str.charAt(pos) == '\n'
             || str.charAt(pos) == '\r') {

                break;
            }
        }
        return str.substring(offset, pos);
    }
}
