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
 * Copyright (c) 2004-2009 Per Cederberg. All rights reserved.
 */

package org.liquidsite.core.text;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A tagged text formatter. This class contains static methods for
 * processing tagged text.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class TaggedFormatter {

    /**
     * The trailing whitespace regex.
     */
    private static Pattern SPACE_END = Pattern.compile("\\s+$");

    /**
     * The horizontal ruler regex.
     */
    private static Pattern HORIZ_RULE = Pattern.compile("((---+)|(___+))(\\n|$)");

    /**
     * The pre-formatted text tag regex.
     */
    private static Pattern TAG_PRE = Pattern.compile("<pre[^>]*>");

    /**
     * Cleans a tagged text string. Unneeded line feeds and space
     * characters will be removed.
     *
     * @param text           the tagged text string
     *
     * @return the cleaned tagged text string
     */
    public static String clean(String text) {
        StringBuffer  result = new StringBuffer();
        int           pos = 0;
        Matcher       m = TAG_PRE.matcher(text);

        while (m.find(pos)) {
            if (m.start() > 0) {
                result.append(cleanMarkup(text.substring(pos, m.start())));
                result.append("\n\n");
            }
            result.append(m.group());
            pos = text.indexOf("</pre>", m.end());
            if (pos < 0) {
                result.append(text.substring(m.end()));
                result.append("</pre>\n\n");
                pos = text.length();
            } else {
                result.append(text.substring(m.end(), pos));
                result.append("</pre>\n\n");
                pos += 6;
            }
        }
        if (pos < text.length()) {
            result.append(cleanMarkup(text.substring(pos)));
        }
        m = SPACE_END.matcher(result);
        if (m.find()) {
            result.setLength(m.start());
        }
        return result.toString();
    }

    /**
     * Cleans a tagged text string. Unneeded line feeds and space
     * characters will be removed. This method doesn't handle
     * pre-formatted text.
     *
     * @param text           the tagged text string
     *
     * @return the cleaned tagged text string
     */
    private static String cleanMarkup(String text) {
        StringBuffer  result = new StringBuffer();
        int           pos = 0;

        text = cleanWhitespace(text);
        while (pos < text.length()) {
            if (text.charAt(pos) == '\n') {
                pos++;
            } else {
                if (result.length() > 0) {
                    result.append("\n\n");
                }
                pos = cleanBlock(text, pos, result);
            }
        }
        return cleanWhitespace(result.toString());
    }


    /**
     * Cleans a single block in a tagged text string. This will
     * normalize all tags and clean an inline content. This method
     * returns when a block break or double newline is encountered.
     *
     * @param text           the tagged text string
     * @param pos            the current text position
     * @param result         the cleaned tagged text
     *
     * @return the new text position
     */
    private static int cleanBlock(String text, int pos, StringBuffer result) {
        Matcher  m = HORIZ_RULE.matcher(text);
        int      backupLength;
        int      newPos;
        String   tag;

        m.region(pos, text.length());
        if (m.lookingAt()) {
            result.append("---");
            pos = m.end();
        } else if (text.charAt(pos) == '<') {
            backupLength = result.length();
            newPos = cleanTag(text, pos, result);
            tag = result.substring(backupLength);
            if (newPos < pos + 3) {
                pos = cleanInline(text, newPos, result);
            } else if (tag.equals("")){  // This is the <p> tag
                pos = cleanInline(text, newPos, result);
                if (text.startsWith("</p>", pos)) {
                    pos += 4;
                }
            } else if (tag.equals("<h1>")) {
                pos = cleanInline(text, newPos, result);
                pos = cleanTagEnd(text, pos, "</h1>", result);
            } else if (tag.equals("<h2>")) {
                pos = cleanInline(text, newPos, result);
                pos = cleanTagEnd(text, pos, "</h2>", result);
            } else if (tag.equals("<h3>")) {
                pos = cleanInline(text, newPos, result);
                pos = cleanTagEnd(text, pos, "</h3>", result);
            } else if (tag.startsWith("<list")) {
                result.append("\n");
                pos = cleanList(text, newPos, result);
                pos = cleanTagEnd(text, pos, "</list>", result);
            } else if (tag.startsWith("<box")) {
                pos = cleanInline(text, newPos, result);
                pos = cleanTagEnd(text, pos, "</box>", result);
            } else {
                result.setLength(backupLength);
                pos = cleanInline(text, pos, result);
            }
        } else {
            pos = cleanInline(text, pos, result);
        }
        return pos;
    }

    /**
     * Cleans the inline content in a tagged text string. This will
     * normalize all tags. This method returns when it encounters a
     * block tag or a double newline.
     *
     * @param text           the tagged text string
     * @param pos            the current text position
     * @param result         the cleaned tagged text
     *
     * @return the new text position
     */
    private static int cleanInline(String text, int pos, StringBuffer result) {
        LinkedList  stack = new LinkedList();
        int         backupLength;
        int         newPos;
        String      tag;

        while (pos < text.length()) {
            if (text.startsWith("\n\n", pos)
             || text.startsWith("\n---", pos)
             || text.startsWith("\n___", pos)
             || text.startsWith("\n<p", pos)
             || text.startsWith("\n<h", pos)
             || text.startsWith("\n<list", pos)
             || text.startsWith("\n</list", pos)
             || text.startsWith("\n<item", pos)
             || text.startsWith("\n<box", pos)) {

                break;
            } else if (text.charAt(pos) == '<') {
                backupLength = result.length();
                newPos = cleanTag(text, pos, result);
                tag = result.substring(backupLength);
                if (newPos < pos + 3) {
                    pos = newPos;
                } else if (tag.equals("<b>")) {
                    stack.addLast("</b>");
                    pos = newPos;
                } else if (tag.equals("<i>")) {
                    stack.addLast("</i>");
                    pos = newPos;
                } else if (tag.equals("<code>")) {
                    stack.addLast("</code>");
                    pos = newPos;
                } else if (tag.startsWith("<link")) {
                    stack.addLast("</link>");
                    pos = newPos;
                } else if (tag.startsWith("<image")) {
                    pos = newPos;
                } else if (tag.equals("</b>")
                        || tag.equals("</i>")
                        || tag.equals("</code>")
                        || tag.equals("</link>")) {

                    result.setLength(backupLength);
                    if (stack.contains(tag)) {
                        while (!stack.getLast().equals(tag)) {
                            result.append(stack.removeLast());
                        }
                        result.append(stack.removeLast());
                    }
                    pos = newPos;
                } else {
                    result.setLength(backupLength);
                    break;
                }
            } else {
                result.append(text.charAt(pos));
                pos++;
            }
        }
        while (stack.size() > 0) {
            result.append(stack.removeLast());
        }
        return pos;
    }

    /**
     * Cleans the list content in a tagged text string. This will
     * normalize all tags. This method returns when it encounters the
     * end of the list.
     *
     * @param text           the tagged text string
     * @param pos            the current text position
     * @param result         the cleaned tagged text
     *
     * @return the new text position
     */
    private static int cleanList(String text, int pos, StringBuffer result) {
        int     backupLength;
        int     newPos;
        String  tag;

        while (pos < text.length()) {
            if (text.charAt(pos) == '\n') {
                pos++;
            } else if (text.charAt(pos) == '<') {
                backupLength = result.length();
                newPos = cleanTag(text, pos, result);
                tag = result.substring(backupLength);
                if (newPos < pos + 3) {
                    result.insert(backupLength, "<item>");
                    pos = cleanInline(text, newPos, result);
                    pos = cleanTagEnd(text, pos, "</item>", result);
                    result.append("\n");
                } else if (tag.equals("<item>")) {
                    pos = cleanInline(text, newPos, result);
                    pos = cleanTagEnd(text, pos, "</item>", result);
                    result.append("\n");
                } else if (tag.equals("</item>")) {
                    result.setLength(backupLength);
                    pos = newPos;
                } else if (tag.startsWith("<list")) {
                    result.append("\n");
                    pos = cleanList(text, newPos, result);
                    pos = cleanTagEnd(text, pos, "</list>", result);
                    result.append("\n");
                } else if (tag.equals("</list>")) {
                    result.setLength(backupLength);
                    break;
                } else if (tag.equals("<b>")
                        || tag.equals("<i>")
                        || tag.equals("<code>")
                        || tag.startsWith("<link")
                        || tag.startsWith("<image")) {

                    result.setLength(backupLength);
                    result.append("<item>");
                    pos = cleanInline(text, pos, result);
                    pos = cleanTagEnd(text, pos, "</item>", result);
                    result.append("\n");
                } else {
                    result.setLength(backupLength);
                    break;
                }
            } else {
                backupLength = result.length();
                result.append("<item>");
                newPos = cleanInline(text, pos, result);
                if (newPos != pos) {
                    pos = cleanTagEnd(text, newPos, "</item>", result);
                    result.append("\n");
                } else {
                    result.setLength(backupLength);
                }
            }
        }
        return pos;
    }

    /**
     * Cleans and normalizes a tag in a tagged text string.
     *
     * @param text           the tagged text string
     * @param pos            the current text position
     * @param result         the cleaned tagged text
     *
     * @return the new text position (after the tag)
     */
    private static int cleanTag(String text, int pos, StringBuffer result) {
        int      start = pos;
        int      end;
        String   name;
        boolean  insideQuote = false;
        boolean  isEnd = false;

        // Find ending '>' character
        while (pos < text.length()
            && (text.charAt(pos) != '>' || insideQuote)) {

            if (text.charAt(pos) == '"') {
                insideQuote = !insideQuote;
            }
            pos++;
        }
        if (pos >= text.length()) {
            result.append("<");
            return start + 1;
        }
        end = pos + 1;

        // Find tag name and attribute start
        pos = text.indexOf(' ', start);
        if (pos < 0 || pos >= end) {
            pos = end - 1;
        }
        name = text.substring(start + 1, pos);
        if (name.startsWith("/")) {
            name = name.substring(1);
            isEnd = true;
        }

        // Check for unknown tag names
        if (!name.equals("h1") && !name.equals("h2")
         && !name.equals("h3") && !name.equals("p") 
         && !name.equals("b") && !name.equals("i")
         && !name.equals("link") && !name.equals("image")
         && !name.equals("list") && !name.equals("item")
         && !name.equals("box") && !name.equals("code")) {

            result.append("<");
            return start + 1;
        }
        if (isEnd && name.equals("image")) {
            return end;
        }

        // Check for suppressed tags
        if (name.equals("p")) {
            return end;
        }

        // Normalize tag
        result.append("<");
        if (isEnd) {
            result.append("/");
        }
        result.append(name);
        
        // Normalize tag attributes
        if (!isEnd) {
            if (name.equals("link")
             || name.equals("image")
             || name.equals("list")
             || name.equals("box")) {

                cleanTagAttributes(text, pos, end - 1, name, result);
            }
            if (name.equals("image")) {
                result.append(" /");
            }
        }
        result.append(">");

        return end;
    }

    /**
     * Cleans an end tag in a tagged text string. This will print the
     * end tag to the result, and if the string contains the
     * specified end tag the current text position will be advanced.
     *
     * @param text           the tagged text string
     * @param pos            the current text position
     * @param tag            the end tag to print
     * @param result         the cleaned tagged text
     *
     * @return the new text position (after the tag)
     */
    private static int cleanTagEnd(String text,
                                   int pos,
                                   String tag,
                                   StringBuffer result) {

        result.append(tag);
        if (text.startsWith(tag, pos)) {
            pos += tag.length();
        }
        return pos;
    }

    /**
     * Cleans and normalizes a tag attribute string.
     *
     * @param text           the tagged text string
     * @param pos            the current text position
     * @param end            the end position of the tag (inclusive)
     * @param tagName        the tag name
     * @param result         the cleaned tagged text
     */
    private static void cleanTagAttributes(String text,
                                           int pos,
                                           int end,
                                           String tagName,
                                           StringBuffer result) {

        HashMap  attributes = new HashMap();
        String   str;

        // Parse tag attributes
        if (text.charAt(end) == '>') {
            end--;
        }
        if (tagName.equals("image") && text.charAt(end) == '/') {
            end--;
        }
        attributes = parseTagAttributes(text, pos, end + 1);

        // Normalize attributes
        if (tagName.equals("list")) {
            str = (String) attributes.get("type");
            if (str != null && !str.equals("")) {
                result.append(" type=\"");
                result.append(str);
                result.append("\"");
            }
        } else if (tagName.equals("link")) {
            str = (String) attributes.get("url");
            result.append(" url=\"");
            if (str != null) {
                result.append(str);
            }
            result.append("\"");
            str = (String) attributes.get("window");
            if (str != null) {
                result.append(" window=\"");
                result.append(str);
                result.append("\"");
            }
        } else if (tagName.equals("image")) {
            str = (String) attributes.get("url");
            result.append(" url=\"");
            if (str != null) {
                result.append(str);
            }
            result.append("\"");
            str = (String) attributes.get("layout");
            if (str != null) {
                result.append(" layout=\"");
                result.append(str);
                result.append("\"");
            }
        } else if (tagName.equals("box")) {
            str = (String) attributes.get("layout");
            if (str != null) {
                result.append(" layout=\"");
                result.append(str);
                result.append("\"");
            }
        }
    }

    /**
     * Cleans a tagged text string for excessive whitespace.
     *
     * @param text           the tagged text string
     *
     * @return the cleaned tagged text string
     */
    private static String cleanWhitespace(String text) {
        StringBuffer  buffer = new StringBuffer();
        int           pos;

        // Trim each line
        while ((pos = text.indexOf("\n")) >= 0) {
            buffer.append(text.substring(0, pos).trim());
            buffer.append("\n");
            text = text.substring(pos + 1);
        }
        buffer.append(text.trim());

        // Remove empty starting and ending lines
        while (buffer.length() > 0 && buffer.charAt(0) == '\n') {
            buffer.deleteCharAt(0);
        }
        while (buffer.length() > 0
            && buffer.charAt(buffer.length() - 1) == '\n') {

            buffer.setLength(buffer.length() - 1);
        }

        // Replace tab characters with spaces
        pos = buffer.indexOf("\t");
        while (pos > 0) {
            buffer.replace(pos, pos + 1, " ");
            pos = buffer.indexOf("\t");
        }

        // Remove duplicate empty lines
        pos = buffer.indexOf("\n\n\n");
        while (pos > 0) {
            buffer.deleteCharAt(pos);
            pos = buffer.indexOf("\n\n\n");
        }

        return buffer.toString();
    }

    /**
     * Formats a tagged text string in HTML. This method will resolve
     * any links in the tagged text and convert the tags to valid
     * HTML tags.
     *
     * @param text           the tagged text string
     * @param context        the formatting context
     *
     * @return the HTML encoded text
     */
    public static String formatHtml(String text, FormattingContext context) {
        StringBuffer  result = new StringBuffer();
        int           pos = 0;
        Matcher       m = TAG_PRE.matcher(text);
        String        str;

        while (m.find(pos)) {
            if (m.start() > 0) {
                str = text.substring(pos, m.start());
                result.append(formatHtmlMarkup(str, context));
            }
            result.append(m.group());
            pos = text.indexOf("</pre>", m.end());
            if (pos < 0) {
                str = text.substring(m.end());
                result.append(PlainFormatter.escapeHtml(str));
                result.append("</pre>");
                pos = text.length();
            } else {
                str = text.substring(m.end(), pos);
                result.append(PlainFormatter.escapeHtml(str));
                result.append("</pre>");
                pos += 6;
            }
        }
        if (pos < text.length()) {
            result.append(formatHtmlMarkup(text.substring(pos), context));
        }
        return result.toString();
    }

    /**
     * Formats a tagged text string in HTML. This method will resolve
     * any links in the tagged text and convert the tags to valid
     * HTML tags. This method doesn't handle pre-formatted text.
     *
     * @param text           the tagged text string
     * @param context        the formatting context
     *
     * @return the HTML encoded text
     */
    private static String formatHtmlMarkup(String text, FormattingContext context) {
        StringBuffer  result = new StringBuffer();
        int           pos = 0;

        while (pos < text.length()) {
            if (text.charAt(pos) == '\n') {
                pos++;
            } else {
                if (result.length() > 0) {
                    result.append("\n\n");
                }
                pos = formatHtmlBlock(text, pos, context, result);
            }
        }
        return result.toString();
    }

    /**
     * Formats a single block in a tagged text string. This method
     * returns when a block break or double newline is encountered.
     *
     * @param text           the tagged text string
     * @param pos            the current text position
     * @param context        the formatting context
     * @param result         the cleaned tagged text
     *
     * @return the new text position
     */
    private static int formatHtmlBlock(String text,
                                       int pos,
                                       FormattingContext context,
                                       StringBuffer result) {

        Matcher  m = HORIZ_RULE.matcher(text);

        m.region(pos, text.length());
        if (m.lookingAt()) {
            result.append("<hr/>");
            pos = m.end();
        } else if (text.startsWith("<list", pos)) {
            pos = formatHtmlList(text, pos, context, result);
        } else if (text.startsWith("<h1>", pos)
                || text.startsWith("<h2>", pos)
                || text.startsWith("<h3>", pos)
                || text.startsWith("<box>", pos)) {

            pos = formatHtmlInline(text, pos, context, result);
        } else {
            result.append("<p>");
            pos = formatHtmlInline(text, pos, context, result);
            result.append("</p>");
        }
        return pos;
    }

    /**
     * Formats an inline text in a tagged text string. This method
     * returns when a double newline is encountered.
     *
     * @param text           the tagged text string
     * @param pos            the current text position
     * @param context        the formatting context
     * @param result         the cleaned tagged text
     *
     * @return the new text position
     */
    private static int formatHtmlInline(String text,
                                        int pos,
                                        FormattingContext context,
                                        StringBuffer result) {

        while (pos < text.length()
            && !text.startsWith("\n\n", pos)
            && !text.startsWith("\n---", pos)
            && !text.startsWith("\n___", pos)
            && !text.startsWith("<list", pos)
            && !text.startsWith("</list>", pos)
            && !text.startsWith("<item>", pos)
            && !text.startsWith("</item>", pos)) {

            switch (text.charAt(pos)) {
            case '<':
                pos = formatHtmlTag(text, pos, context, result);
                break;
            case '>':
                result.append("&gt;");
                pos++;
                break;
            case '&':
                result.append("&amp;");
                pos++;
                break;
            case '\n':
                result.append("<br/>\n");
                pos++;
                break;
            case '\r':
                pos++;
                break;
            case '@':
                result.append("&#64;");
                pos++;
                break;
            default:
                result.append(text.charAt(pos));
                pos++;
            }
        }
        return pos;
    }

    /**
     * Formats a list block in a tagged text string. This method
     * returns when a block break or double newline is encountered.
     *
     * @param text           the tagged text string
     * @param pos            the current text position
     * @param context        the formatting context
     * @param result         the cleaned tagged text
     *
     * @return the new text position
     */
    private static int formatHtmlList(String text,
                                      int pos,
                                      FormattingContext context,
                                      StringBuffer result) {

        pos = formatHtmlTag(text, pos, context, result);
        result.append("\n");
        while (pos < text.length()) {
            if (text.charAt(pos) == '\n') {
                pos++;
            } else if (text.startsWith("<item>", pos)) {
                pos = formatHtmlTag(text, pos, context, result);
                pos = formatHtmlInline(text, pos, context, result);
            } else if (text.startsWith("</item>", pos)) {
                pos = formatHtmlTag(text, pos, context, result);
                result.append("\n\n");
            } else if (text.startsWith("<list", pos)) {
                pos = formatHtmlList(text, pos, context, result);
                result.append("\n");
            } else if (text.startsWith("</list>", pos)) {
                return formatHtmlTag(text, pos, context, result);
            } else {
                result.append("<li>");
                pos = formatHtmlInline(text, pos, context, result);
                result.append("</li>\n");
            }
        }
        result.append("</ul>");
        return pos;
    }

    /**
     * Formats a tag in a tagged text string.
     *
     * @param text           the tagged text string
     * @param pos            the current text position
     * @param context        the formatting context
     * @param result         the cleaned tagged text
     *
     * @return the new text position
     */
    private static int formatHtmlTag(String text,
                                     int pos,
                                     FormattingContext context,
                                     StringBuffer result) {

        int      start = pos;
        int      end;
        String   name;
        HashMap  attributes;
        boolean  insideQuote = false;
        String   str;

        // Find ending '>' character
        while (pos < text.length()
            && (text.charAt(pos) != '>' || insideQuote)) {

            if (text.charAt(pos) == '"') {
                insideQuote = !insideQuote;
            }
            pos++;
        }
        if (pos >= text.length()) {
            result.append("&lt;");
            return start + 1;
        }
        end = pos + 1;

        // Find tag name and attribute start
        pos = text.indexOf(' ', start);
        if (pos < 0 || pos >= end) {
            pos = end - 1;
        }
        name = text.substring(start + 1, pos);

        // Format tag
        if (name.equals("p") || name.equals("/p")
         || name.equals("h1") || name.equals("/h1")
         || name.equals("h2") || name.equals("/h2")
         || name.equals("h3") || name.equals("/h3")
         || name.equals("b") || name.equals("/b")
         || name.equals("i") || name.equals("/i")
         || name.equals("code") || name.equals("/code")) {

            result.append("<");
            result.append(name);
            result.append(">");
        } else if (name.equals("link")) {
            attributes = parseTagAttributes(text, pos, end - 1);
            result.append("<a href=\"");
            str = context.linkTo((String) attributes.get("url"));
            result.append(PlainFormatter.escapeHtml(str));
            result.append("\"");
            str = (String) attributes.get("window");
            if (str != null && str.equals("new")) {
                result.append(" target=\"_blank\"");
            }
            result.append(">");
        } else if (name.equals("/link")) {
            result.append("</a>");
        } else if (name.equals("image")) {
            attributes = parseTagAttributes(text, pos, end - 1);
            result.append("<img src=\"");
            str = context.linkTo((String) attributes.get("url"));
            result.append(PlainFormatter.escapeHtml(str));
            result.append("\" alt=\"\"");
            str = (String) attributes.get("layout");
            if (str != null && str.equals("right")) {
                result.append(" style=\"float: right;\"");
            } else if (str != null && str.equals("left")) {
                result.append(" style=\"float: left;\"");
            }
            result.append(" />");
        } else if (name.equals("list")) {
            attributes = parseTagAttributes(text, pos, end - 1);
            result.append("<ul");
            str = (String) attributes.get("type");
            if (str != null && str.equals("*")) {
                result.append(" style=\"list-style-type: disc;\"");
            } else if (str != null && str.equals("1")) {
                result.append(" style=\"list-style-type: decimal;\"");
            } else if (str != null && str.equals("i")) {
                result.append(" style=\"list-style-type: lower-roman;\"");
            } else if (str != null && str.equals("I")) {
                result.append(" style=\"list-style-type: upper-roman;\"");
            } else if (str != null && str.equals("a")) {
                result.append(" style=\"list-style-type: lower-alpha;\"");
            } else if (str != null && str.equals("A")) {
                result.append(" style=\"list-style-type: upper-alpha;\"");
            }
            result.append(">");
        } else if (name.equals("/list")) {
            result.append("</ul>");
        } else if (name.equals("item")) {
            result.append("<li>");
        } else if (name.equals("/item")) {
            result.append("</li>");
        } else if (name.equals("box")) {
            attributes = parseTagAttributes(text, pos, end - 1);
            result.append("<p class=\"box-layout-");
            str = (String) attributes.get("layout");
            if (str != null && str.equals("right")) {
                result.append("right");
            } else if (str != null && str.equals("left")) {
                result.append("left");
            }
            result.append("\">");
        } else if (name.equals("/box")) {
            result.append("</p>");
        } else {
            result.append("&lt;");
            end = start + 1;
        }
        return end;
    }

    /**
     * Parses the tag attributes. This method extracts all the
     * attributes and their values from the string and returns the
     * mappings in a hash map.
     *
     * @param text           the tagged text string
     * @param pos            the current text position
     * @param end            the end position (exclusive)
     *
     * @return the hash map with attribute names and values
     */
    private static HashMap parseTagAttributes(String text, int pos, int end) {
        HashMap  result = new HashMap();
        String   name;
        String   value;
        int      temp;

        while (pos < end) {
            if (text.charAt(pos) == ' ') {
                pos++;
            } else {
                temp = text.indexOf('=', pos);
                if (temp <= 0 || temp >= end) {
                    name = text.substring(pos, end).trim();
                    result.put(name, "");
                    break;
                }
                name = text.substring(pos, temp).trim();
                pos = temp + 1;
                while (text.charAt(pos) == ' '
                    || text.charAt(pos) == '\n') {

                    pos++;
                    if (pos >= end) {
                        break;
                    }
                }
                if (pos >= end) {
                    result.put(name, "");
                    break;
                } else if (text.charAt(pos) == '"') {
                    temp = text.indexOf('"', pos + 1);
                    if (temp < 0 || temp >= end) {
                        value = text.substring(pos + 1, end);
                        pos = end;
                    } else {
                        value = text.substring(pos + 1, temp);
                        pos = temp + 1;
                    }
                } else {
                    temp = text.indexOf(' ', pos);
                    if (temp < 0 || temp >= end) {
                        value = text.substring(pos, end);
                        pos = end;
                    } else {
                        value = text.substring(pos, temp);
                        pos = temp + 1;
                    }
                }
                result.put(name, value);
            }
        }
        return result;
    }
}
