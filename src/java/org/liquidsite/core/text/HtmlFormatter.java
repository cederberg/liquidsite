/*
 * HtmlFormatter.java
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
 * An HTML text formatter. This class contains static methods for
 * processing HTML text.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class HtmlFormatter {

    /**
     * Formats an HTML text string in HTML. This method will resolve
     * any links in the HTML, but otherwise leave the HTML code
     * unmodified.
     *
     * @param text           the HTML text string
     * @param context        the formatting context
     *
     * @return the HTML encoded text
     */
    public static String formatHtml(String text, FormattingContext context) {
        StringBuffer  buffer = new StringBuffer();
        int           start;
        int           end;

        while ((start = text.indexOf("<")) >= 0) {
            end = text.indexOf(">", start);
            buffer.append(text.substring(0, start));
            buffer.append(formatHtmlTag(text.substring(start, end), context));
            text = text.substring(end);
        }
        buffer.append(text);
        return buffer.toString();
    }

    /**
     * Formats an HTML tag. This method will adjust any links and
     * image sources.
     *
     * @param tag            the HTML tag
     * @param context        the formatting context
     *
     * @return the modified HTML tag
     */
    private static String formatHtmlTag(String tag,
                                        FormattingContext context) {

        String  lowerTag = tag.toLowerCase();
        int     start = 0;
        int     end;

        // Find link start position
        if (lowerTag.startsWith("<a ")) {
            start = lowerTag.indexOf(" href=");
            if (start > 0) {
                start += 6;
            }
        } else if (lowerTag.startsWith("<img ")) {
            start = lowerTag.indexOf(" src=");
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
            if (tag.charAt(start) == '"' || tag.charAt(start) == '\'') {
                start++;
            }
            if (tag.charAt(end - 1) == '"' || tag.charAt(end - 1) == '\'') {
                end--;
            }
            return tag.substring(0, start) +
                   context.linkTo(tag.substring(start, end)) +
                   tag.substring(end);
        } else {
            return tag;
        }
    }
}
