/*
 * ContentQuery.java
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

package org.liquidsite.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.liquidsite.util.db.DatabaseQuery;

/**
 * A content database query. This class is used to compose complex
 * select queries for selecting lists of content objects. 
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentQuery {

    /**
     * The content identifier sorting key.
     */
    public static final String ID_KEY = "ID";

    /**
     * The content revision sorting key.
     */
    public static final String REVISION_KEY = "REVISION";

    /**
     * The content category sorting key.
     */
    public static final String CATEGORY_KEY = "CATEGORY";

    /**
     * The content name sorting key.
     */
    public static final String NAME_KEY = "NAME";

    /**
     * The content parent identifier sorting key.
     */
    public static final String PARENT_KEY = "PARENT";

    /**
     * The content online date sorting key.
     */
    public static final String ONLINE_KEY = "ONLINE";

    /**
     * The content revision modified date sorting key.
     */
    public static final String MODIFIED_KEY = "MODIFIED";

    /**
     * The content revision author sorting key.
     */
    public static final String AUTHOR_KEY = "AUTHOR";

    /**
     * The content revision comment sorting key.
     */
    public static final String COMMENT_KEY = "COMMENT";

    /**
     * The domain name.
     */
    private String domain = null;

    /**
     * The parent content identifiers. If this list is empty any
     * content parent is accepted.
     */
    private ArrayList parents = new ArrayList();

    /**
     * The content category. If set to zero (0) any content category
     * is accepted.
     */
    private int category = 0;

    /**
     * The published content flag. If this flag is set only published
     * and content will be returned. Otherwise the latest version
     * is accepted.
     */
    private boolean published = false;

    /**
     * The online content flag. If this flag is set only online
     * content will be returned.
     */
    private boolean online = false;

    /**
     * The map of attribute values. The attribute names are used as
     * keys in the map and the accepted values are stored in an array
     * list.
     */
    private HashMap attributeValues = new HashMap();

    /**
     * The list of attribute names to join to the query. The attribute
     * tables will be named "a0", "a1" and so on depending on the
     * position in this list.
     */
    private ArrayList joins = new ArrayList();

    /**
     * The list of sort columns to use. The ordering in this list
     * represents the sort priority.
     */
    private ArrayList sortColumns = new ArrayList();

    /**
     * The first row number to retrieve on select.
     */
    private int start = 0;

    /**
     * The maximum number of rows to retrieve on select.
     */
    private int count = 100;

    /**
     * Creates a new content query for objects in the specified
     * domain.
     *
     * @param domain         the domain name
     */
    public ContentQuery(String domain) {
        this.domain = domain;
    }

    /**
     * Checks if the query contains sort columns.
     *
     * @return true if the query sorts the results, or
     *         false otherwise
     */
    public boolean isSorting() {
        return sortColumns.size() > 0;
    }

    /**
     * Adds a content parent requirement. By default any content
     * parent will be accepted. By calling this method several times
     * with different parents, a set of content parents will be
     * accepted.
     *
     * @param parent         the content parent identifier
     */
    public void requireParent(int parent) {
        parents.add(new Integer(parent));
    }

    /**
     * Sets the content category requirement. By default any content
     * category will be accepted.
     *
     * @param category       the content category required
     */
    public void requireCategory(int category) {
        this.category = category;
    }

    /**
     * Sets the published content requirement. By default the latest
     * (work) revision will be accepted. When this flag is set, only
     * published objects will be returned.
     *
     * @param published      the published flag
     */
    public void requirePublished(boolean published) {
        this.published = published;
    }

    /**
     * Sets the online content requirement. By default no checks for
     * online dates are made. When this flag is set, only objects
     * that are currently online will be returned.
     *
     * @param online         the online flag
     */
    public void requireOnline(boolean online) {
        this.online = online;
    }

    /**
     * Sets the attribute value requirement. By default any attribute
     * values will be accepted. When an attribute value is required,
     * only objects having the specified value will be returned. By
     * calling this method several times with different values, a set
     * of attribute values will be accepted.
     *
     * @param attribute      the attribute name
     * @param value          the attribute value
     */
    public void requireAttribute(String attribute, String value) {
        ArrayList  values;

        if (!joins.contains(attribute)) {
            joins.add(attribute);
        }
        values = (ArrayList) attributeValues.get(attribute);
        if (values == null) {
            values = new ArrayList();
            attributeValues.put(attribute, values);
        }
        values.add(value);
    }

    /**
     * Adds a sort key column to the query. Several columns can be
     * added, giving priority to the first ones added.
     *
     * @param key            the sort column key
     * @param ascending      the ascending sort order flag
     */
    public void sortByKey(String key, boolean ascending) {
        sortColumns.add(new SortColumn(key, false, ascending));
    }

    /**
     * Adds a sort key attribute to the query. Several attributes can
     * be added, giving priority to the first ones added.
     *
     * @param attribute      the sort attribute name
     * @param ascending      the ascending sort order flag
     */
    public void sortByAttribute(String attribute, boolean ascending) {
        if (!joins.contains(attribute)) {
            joins.add(attribute);
        }
        sortColumns.add(new SortColumn(attribute, true, ascending));
    }

    /**
     * Sets the result start and count limitations. By default a
     * maximum of 100 rows are retrieved, starting from index zero
     * (0).
     *
     * @param start          the index of the first result row
     * @param count          the maximum number of result rows
     */
    public void limitResults(int start, int count) {
        this.start = start;
        this.count = count;
    }

    /**
     * Creates a new database count query containing all of the
     * parameters in this query.
     *
     * @return the database count query
     */
    public DatabaseQuery createCountQuery() {
        DatabaseQuery  query = new DatabaseQuery();
        StringBuffer   sql = new StringBuffer();

        sql.append("SELECT COUNT(*) FROM LS_CONTENT AS c");
        appendWhere(sql);
        query.setSql(sql.toString());
        return query;
    }

    /**
     * Creates a new database select query containing all of the
     * parameters in this query.
     *
     * @return the database select query 
     */
    public DatabaseQuery createSelectQuery() {
        DatabaseQuery  query = new DatabaseQuery();
        StringBuffer   sql = new StringBuffer();

        sql.append("SELECT c.* FROM LS_CONTENT AS c");
        appendJoin(sql);
        appendWhere(sql);
        appendOrderBy(sql);
        appendLimit(sql);
        query.setSql(sql.toString());
        return query;
    }

    /**
     * Appends the SQL join clauses to the specified SQL statement.
     *
     * @param sql            the SQL statement
     */
    private void appendJoin(StringBuffer sql) {
        String  id;

        for (int i = 0; i < joins.size(); i++) {
            id = "a" + i;
            sql.append(" JOIN LS_ATTRIBUTE AS ");
            sql.append(id);
            sql.append(" ON c.ID = ");
            sql.append(id);
            sql.append(".CONTENT AND c.REVISION = ");
            sql.append(id);
            sql.append(".REVISION AND ");
            sql.append(id);
            sql.append(".NAME = ");
            appendSql(sql, joins.get(i).toString());
        }
    }

    /**
     * Appends the SQL where clause to the specified SQL statement.
     *
     * @param sql            the SQL statement
     */
    private void appendWhere(StringBuffer sql) {
        Iterator   iter;
        String     attribute;
        ArrayList  values;

        sql.append(" WHERE c.DOMAIN = ");
        appendSql(sql, domain);
        if (parents.size() == 1) {
            sql.append(" AND c.PARENT = ");
            sql.append(parents.get(0));
        } else if (parents.size() > 1) {
            sql.append(" AND c.PARENT IN ");
            appendSql(sql, parents);
        }
        if (category > 0) {
            sql.append(" AND c.CATEGORY = ");
            sql.append(category);
        }
        sql.append(" AND (c.STATUS & ");
        if (published) {
            sql.append(ContentPeer.PUBLISHED_STATUS);
        } else {
            sql.append(ContentPeer.LATEST_STATUS);
        }
        sql.append(") > 0");
        if (online) {
            sql.append(" AND c.ONLINE > '1970-01-01' AND c.ONLINE < NOW()");
            sql.append(" AND (c.OFFLINE < '1970-01-02' OR c.OFFLINE > NOW())");
        }
        iter = attributeValues.keySet().iterator();
        while (iter.hasNext()) {
            attribute = (String) iter.next();
            values = (ArrayList) attributeValues.get(attribute);
            sql.append(" AND a");
            sql.append(joins.indexOf(attribute));
            sql.append(".DATA");
            if (values.size() == 1) {
                sql.append(" = ");
                appendSql(sql, values.get(0).toString());
            } else {
                sql.append(" IN ");
                appendSql(sql, values);
            }
        }
    }

    /**
     * Appends the SQL order by clause to the specified SQL statement.
     *
     * @param sql            the SQL statement
     */
    private void appendOrderBy(StringBuffer sql) {
        SortColumn  column;

        if (sortColumns.size() > 0) {
            sql.append(" ORDER BY ");
        }
        for (int i = 0; i < sortColumns.size(); i++) {
            column = (SortColumn) sortColumns.get(i);
            if (i > 0) {
                sql.append(", ");
            }
            if (column.isAttribute()) {
                sql.append("a");
                sql.append(joins.indexOf(column.getName()));
                sql.append(".DATA");
            } else {
                sql.append("c.");
                sql.append(column.getName());
            }
            if (!column.isAscending()) {
                sql.append(" DESC");
            }
        }
    }

    /**
     * Appends the SQL limit clause to the specified SQL statement.
     *
     * @param sql            the SQL statement
     */
    private void appendLimit(StringBuffer sql) {
        sql.append(" LIMIT ");
        sql.append(start);
        sql.append(", ");
        sql.append(count);
    }

    /**
     * Appends a string value to an SQL statement.
     *
     * @param sql            the SQL statement
     * @param value          the value to append
     */
    private void appendSql(StringBuffer sql, String value) {
        char  c;

        if (value == null) {
            sql.append("null");
        } else {
            sql.append("'");
            for (int i = 0; i < value.length(); i++) {
                c = value.charAt(i);
                if (c == '\'') {
                    sql.append("\\'");
                } else {
                    sql.append(c);
                }
            }
            sql.append("'");
        }
    }

    /**
     * Appends an integer value to an SQL statement.
     *
     * @param sql            the SQL statement
     * @param value          the value to append
     */
    private void appendSql(StringBuffer sql, Integer value) {
        if (value == null) {
            sql.append("null");
        } else {
            sql.append(value);
        }
    }

    /**
     * Appends an array value to an SQL statement.
     *
     * @param sql            the SQL statement
     * @param values         the array of values to append
     */
    private void appendSql(StringBuffer sql, ArrayList values) {
        Object  value;

        sql.append("(");
        for (int i = 0; i < values.size(); i++) {
            value = values.get(i);
            if (i > 0) {
                sql.append(",");
            }
            if (value instanceof Integer) {
                appendSql(sql, (Integer) value);
            } else {
                appendSql(sql, value.toString());
            }
        }
        sql.append(")");
    }


    /**
     * A sort column class. This class is used to hold the details of
     * a sort column.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    private class SortColumn {

        /**
         * The content sort key. This value is only set when content
         * column sorting should be used, and contains the sorting key
         * (column name).
         */
        private String key = null;

        /**
         * The attribute sort key. This value is only set when
         * attribute sorting should be used, and contains the
         * attribute name.
         */
        private String attribute = null;

        /**
         * The ascending sort order flag.
         */
        private boolean ascending = true;

        /**
         * Creates a new sort column.
         *
         * @param key            the sorting key
         * @param attribute      the attribute key flag
         * @param ascending      the ascending sort order flag
         */
        public SortColumn(String key, boolean attribute, boolean ascending) {
            if (attribute) {
                this.attribute = key;
            } else {
                this.key = key;
            }
            this.ascending = ascending;
        }

        /**
         * Checks if the sort column name is an attribute name.
         *
         * @return true if the sort column name is an attribute, or
         *         false otherwise
         */
        public boolean isAttribute() {
            return attribute != null;
        }

        /**
         * Checks if the sort order is ascending.
         *
         * @return true if the sort order is ascending, or
         *         false otherwise
         */
        public boolean isAscending() {
            return ascending;
        }

        /**
         * Returns the sort column name.
         *
         * @return the sort column name
         */
        public String getName() {
            if (attribute != null) {
                return attribute;
            } else {
                return key;
            }
        }
    }
}
