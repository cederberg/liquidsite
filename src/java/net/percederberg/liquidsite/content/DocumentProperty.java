/*
 * DocumentProperty.java
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

package net.percederberg.liquidsite.content;

/**
 * A document property container. This class defines the meta-data
 * for a document property, but not the property value itself.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DocumentProperty implements Comparable {

    /**
     * The string type constant. The string type represents document
     * properties that are input using a simple HTML input field,
     * i.e. a string without newlines.
     */
    public static final int STRING_TYPE = 1;

    /**
     * The tagged type constant. The tagged type represents document
     * properties that are input using a textarea HTML input field
     * and contains special tags for formatting.
     */
    public static final int TAGGED_TYPE = 2;

    /**
     * The HTML type constant. The HTML type represents document
     * properties that are input using an HTML editor field, i.e. a
     * string containing HTML markup.
     */
    public static final int HTML_TYPE = 3;

    /**
     * The unique document property identifier.
     */
    private String id = "";

    /**
     * The document property name. The name is used for presenting
     * the property for the user.
     */
    private String name = "";

    /**
     * The document property position.
     */
    private int position = 0;

    /**
     * The document property type.
     */
    private int type = STRING_TYPE;

    /**
     * The document property description.
     */
    private String description = "";

    /**
     * Creates a new document property.
     *
     * @param id             the document property identifier
     */
    public DocumentProperty(String id) {
        this.id = id;
    }

    /**
     * Creates a document property from an XML-encoded string with
     * values.
     *
     * @param id             the document property identifier
     * @param xml            the XML-encoded property values
     */
    DocumentProperty(String id, String xml) {
        this.id = id;
        decodeXml(xml);
    }

    /**
     * Compares this object to another object. The comparison will
     * only work with other document property objects, and will
     * compare the position numbers.
     *
     * @param obj            the object to compare with
     *
     * @return less than zero (0) if this object is previous,
     *         zero if the objects are equal, or
     *         greater than zero otherwise
     *
     * @throws ClassCastException if the specified object wasn't a
     *             document property
     */
    public int compareTo(Object obj) throws ClassCastException {
        DocumentProperty  prop = (DocumentProperty) obj;
        int               diff = position - prop.position; 

        if (diff == 0) {
            return name.compareTo(prop.name);
        } else {
            return diff;
        }
    }

    /**
     * Returns the unique document property identifier.
     *
     * @return the unique document property identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the document property name.
     *
     * @return the document property name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the document property name.
     *
     * @param name           the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the document property position.
     *
     * @return the document property position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the document property position.
     *
     * @param position       the new position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Returns the document property type.
     *
     * @return the document property type
     *
     * @see #STRING_TYPE
     * @see #TAGGED_TYPE
     * @see #HTML_TYPE
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the document property type.
     *
     * @param type           the new type
     *
     * @see #STRING_TYPE
     * @see #TAGGED_TYPE
     * @see #HTML_TYPE
     */
    public void setType(int type)  {
        this.type = type;
    }

    /**
     * Returns the document property description.
     *
     * @return the document property description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the document property description.
     *
     * @param description    the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Encodes all the values in the document property to an XML
     * string.
     *
     * @return the XML-encoded property values
     */
    public String encodeXml() {
        StringBuffer  buffer = new StringBuffer();

        buffer.append("<property id='");
        buffer.append(encodeXmlData(id));
        buffer.append("'>");
        buffer.append("<name>");
        buffer.append(encodeXmlData(name));
        buffer.append("</name>");
        buffer.append("<position>");
        buffer.append(position);
        buffer.append("</position>");
        buffer.append("<type>");
        buffer.append(type);
        buffer.append("</type>");
        buffer.append("<description>");
        buffer.append(encodeXmlData(description));
        buffer.append("</description>");
        buffer.append("</property>");
        return buffer.toString();
    }

    /**
     * Decodes all the values in the document property from an XML
     * string.
     *
     * @param xml            the XML-encoded property values
     */
    private void decodeXml(String xml) {
        String  tag;
        String  tagName;
        String  tagAttributes;
        int     pos;

        while (xml.length() > 0) {
            pos = xml.indexOf("<");
            if (pos < 0) {
                break;
            }
            xml = xml.substring(pos + 1);
            pos = xml.indexOf(">");
            tag = xml.substring(0, pos);
            xml = xml.substring(pos + 1);
            pos = tag.indexOf(" ");
            if (pos < 0) {
                tagName = tag;
                tagAttributes = "";
            } else {
                tagName = tag.substring(0, pos);
                tagAttributes = tag.substring(pos + 1);
            }
            pos = xml.indexOf("</" + tagName);
            decodeXmlTag(tagName, tagAttributes, xml.substring(0, pos));
            pos = xml.indexOf(">", pos);
            xml = xml.substring(pos + 1);
        }
    }

    /**
     * Decodes an XML tag.
     *
     * @param tag            the XML tag name
     * @param attributes     the encoded attribute string
     * @param data           the encoded data string
     */
    private void decodeXmlTag(String tag,
                              String attributes, 
                              String data) {

        if (tag.equals("name")) {
            name = decodeXmlData(data);
        } else if (tag.equals("position")) {
            position = Integer.parseInt(data);
        } else if (tag.equals("type")) {
            type = Integer.parseInt(data);
        } else if (tag.equals("description")) {
            description = decodeXmlData(data);
        } else {
            decodeXml(data);
        }
    }

    /**
     * Encodes a string as an XML data string. This will escape any
     * occurencies of special XML characters.
     *
     * @param str            the string to encode
     *
     * @return the XML-encoded data string
     */
    private String encodeXmlData(String str) {
        StringBuffer  buffer = new StringBuffer();
        char          c;

        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (c == '<') {
                buffer.append("&lt;");
            } else if (c == '>') {
                buffer.append("&gt;");
            } else if (c == '&') {
                buffer.append("&amp;");
            } else if (c == '"') {
                buffer.append("&quot;");
            } else if (c == '\'') {
                buffer.append("&apos;");
            } else {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }

    /**
     * Decodes an XML data string to a normal string. This will
     * unescape any occurencies of special XML entities.
     *
     * @param str            the string to decode
     *
     * @return the unencoded data string
     */
    private String decodeXmlData(String str) {
        StringBuffer  buffer = new StringBuffer();

        while (str.length() > 0) {
            if (str.startsWith("&lt;")) {
                buffer.append("<");
                str = str.substring(4);
            } else if (str.startsWith("&gt;")) {
                buffer.append(">");
                str = str.substring(4);
            } else if (str.startsWith("&amp;")) {
                buffer.append("&");
                str = str.substring(5);
            } else if (str.startsWith("&quot;")) {
                buffer.append("\"");
                str = str.substring(6);
            } else if (str.startsWith("&apos;")) {
                buffer.append("'");
                str = str.substring(6);
            } else {
                buffer.append(str.charAt(0));
                str = str.substring(1);
            }
        }
        return buffer.toString();
    }
}
