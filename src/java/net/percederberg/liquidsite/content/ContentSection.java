/*
 * ContentSection.java
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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.ContentData;

/**
 * A content section.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentSection extends Content {

    /**
     * The document property  content attribute prefix.
     */
    private static final String DOCUMENT_PREFIX = "DOCUMENT.";

    /**
     * Creates a new root section with default values.
     * 
     * @param domain         the domain
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public ContentSection(Domain domain) throws ContentException {
        super(domain, Content.SECTION_CATEGORY);
        setParent(null);
    }

    /**
     * Creates a new section with default values.
     * 
     * @param parent         the parent content section
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public ContentSection(ContentSection parent) throws ContentException {
        super(parent.getDomain(), Content.SECTION_CATEGORY);
        setParent(parent);
    }

    /**
     * Creates a new section.
     * 
     * @param data           the content data object
     * @param latest         the latest revision flag
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected ContentSection(ContentData data, 
                             boolean latest, 
                             DatabaseConnection con) 
        throws ContentException {

        super(data, latest, con);
    }

    /**
     * Returns all document properties for this section. The document 
     * properties define the available properties for any document 
     * created in this section. If this array does contain any 
     * properties at all, the parent section properties should be 
     * used. The document properties array is ordered by increasing 
     * positions.
     * 
     * @return an array of all document properties for this section
     */
    public DocumentProperty[] getAllDocumentProperties() {
        ArrayList           list = new ArrayList();
        Iterator            iter = getAttributeNames();
        DocumentProperty[]  res;
        String              name;
        
        while (iter.hasNext()) {
            name = iter.next().toString();
            if (name.startsWith(DOCUMENT_PREFIX)) {
                name = name.substring(DOCUMENT_PREFIX.length());
                list.add(getDocumentProperty(name));
            }
        }
        Collections.sort(list);
        res = new DocumentProperty[list.size()];
        list.toArray(res);
        return res;
    }

    /**
     * Returns an identified document property. The document 
     * properties define the available properties for any document 
     * created in this section. Note that a given document does not
     * have to specify any of the properties specified in it's parent
     * section, as documents can be moved between sections.
     * 
     * @param id             the document property identifier 
     * 
     * @return the document property, or
     *         null if not found
     */
    public DocumentProperty getDocumentProperty(String id) {
        String  str;
        
        str = getAttribute(DOCUMENT_PREFIX + id);
        if (str == null) {
            return null;
        } else {
            return new DocumentProperty(id, str);
        }
    }

    /**
     * Sets a document property. If the document property specified
     * is null, the specified document property will be removed.
     * 
     * @param id             the document property identifier
     * @param property       the document property, or null
     */
    public void setDocumentProperty(String id, DocumentProperty property) {
        if (property == null) {
            setAttribute(DOCUMENT_PREFIX + id, null);
        } else {
            setAttribute(DOCUMENT_PREFIX + id, property.encodeXml());
        }
    }

    /**
     * Validates this data object. This method checks that all 
     * required fields have been filled with suitable values.
     * 
     * @throws ContentException if the data object contained errors
     */
    public void validate() throws ContentException {
        Content[]  children;

        super.validate();
        if (getParent() == null) {
            children = Content.findByParent(getDomain());
        } else {
            children = Content.findByParent(getParent());
        }
        for (int i = 0; i < children.length; i++) {
            if (children[i].getId() != getId()
             && children[i].getName().equals(getName())
             && children[i].getCategory() == SECTION_CATEGORY) {

                throw new ContentException(
                    "another section with the same name is already " +
                    "present in the parent section");
            }
        }
    }


    /**
     * A document property container. This class defines the 
     * meta-data for a document property, but not the property value
     * itself.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    public class DocumentProperty implements Comparable {

        /**
         * The string type constant. The string type represents 
         * document properties that are input using a simple HTML
         * input field, i.e. a string without newlines.
         */
        public static final int STRING_TYPE = 1;

        /**
         * The text type constant. The text type represents document 
         * properties that are input using a textarea HTML input 
         * field, i.e. plain text with possible newlines.
         */
        public static final int TEXT_TYPE = 2;

        /**
         * The HTML type constant. The HTML type represents document 
         * properties that are input using an HTML editor field, i.e. 
         * a string containing HTML markup.
         */
        public static final int HTML_TYPE = 3;

        /**
         * The unique document property identifier.
         */
        private String id = "";

        /**
         * The document property name. The name is used for 
         * presenting the property for the user. 
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
         * Creates a document property from an XML-encoded string 
         * with values.
         * 
         * @param id             the document property identifier
         * @param xml            the XML-encoded property values
         */
        DocumentProperty(String id, String xml) {
            this.id = id;
            decodeXml(xml);
        }

        /**
         * Compares this object to another object. The comparison
         * will only work with other document property objects, and
         * will compare the position numbers.
         * 
         * @param obj            the object to compare with
         * 
         * @return less than zero (0) if this object is previous, 
         *         zero if the objects are equal, or
         *         greater than zero otherwise
         * 
         * @throws ClassCastException if the specified object wasn't
         *             a document property
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
         * @see #TEXT_TYPE
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
         * @see #TEXT_TYPE
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
         * Decodes all the values in the document property from an 
         * XML string.
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
                decodeXmlTag(tagName, tagAttributes, xml.substring(pos));
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
         * Encodes a string as an XML data string. This will escape 
         * any occurencies of special XML characters.
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
                } else if (str.startsWith("&gt;")) {
                    buffer.append(">");
                } else if (str.startsWith("&amp;")) {
                    buffer.append("&");
                } else if (str.startsWith("&quot;")) {
                    buffer.append("\"");
                } else if (str.startsWith("&apos;")) {
                    buffer.append("'");
                } else {
                    buffer.append(str.charAt(0));
                    str = str.substring(1);
                }
            }
            return buffer.toString();
        }
    }
}
