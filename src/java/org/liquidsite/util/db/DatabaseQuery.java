/*
 * DatabaseQuery.java
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

package org.liquidsite.util.db;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * A database query. This class encapsulates an SQL query or
 * statement that can be executed on a database connection.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DatabaseQuery {

    /**
     * The query name. The name can be used to retrieve the SQL text
     * for the query.
     */
    private String name = null;

    /**
     * The SQL text.
     */
    private String sql = null;

    /**
     * The query parameters.
     */
    private ArrayList params = new ArrayList();

    /**
     * Creates a new empty database query. In order to execute this
     * query, the SQL text must be set.
     *
     * @see #setSql
     */
    public DatabaseQuery() {
        // No further initialization here, call the setSql() method
    }

    /**
     * Creates a new database query with the specified name. The
     * query name can be used by the database connector to retrieve
     * the SQL for the query.
     *
     * @param name           the query name
     */
    public DatabaseQuery(String name) {
        this.name = name;
    }

    /**
     * Checks if this query has SQL text. This method will return
     * false for named queries, until they have been executed once.
     *
     * @return true if the query has SQL text, or
     *         false otherwise
     */
    public boolean hasSql() {
        return sql != null;
    }

    /**
     * Checks if this database query will create a result. This
     * method will always return false, if no SQL has been set to
     * the query.
     *
     * @return true if the query will return a result, or
     *         false otherwise
     */
    public boolean hasResults() {
        String  str = "";

        if (sql != null) {
            str = sql.trim().toUpperCase();
        }
        return str.startsWith("SELECT ")
            || str.startsWith("SHOW ");
    }

    /**
     * Returns the query name.
     *
     * @return the query name, or
     *         null if no name has been set
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the SQL text for the query.
     *
     * @return the SQL text for the query, or
     *         null if no SQL text has been set
     */
    public String getSql() {
        return sql;
    }

    /**
     * Sets the SQL text for the query. This is normally not needed
     * when using named queries, as the database connector will then
     * retrieve the SQL text and call this method.
     *
     * @param sql            the SQL text
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * Returns the number of query parameters.
     *
     * @return the number of query parameters
     */
    public int getParameterCount() {
        return params.size();
    }

    /**
     * Returns a query parameter.
     *
     * @param pos            the parameter position, 0 <= pos < count
     *
     * @return the query parameter object
     */
    public Object getParameter(int pos) {
        return params.get(pos);
    }

    /**
     * Adds a query parameter. Note that this method DOES NOT support
     * adding arrays are parameters.
     *
     * @param obj            the query parameter
     */
    public void addParameter(Object obj) {
        if (obj instanceof Date) {
            obj = new Timestamp(((Date) obj).getTime());
        }
        params.add(obj);
    }

    /**
     * Adds an integer query parameter.
     *
     * @param value          the query parameter value
     */
    public void addParameter(int value) {
        addParameter(new Integer(value));
    }

    /**
     * Adds a boolean query parameter. This method converts the
     * boolean value to an integer before adding it, using zero (0)
     * for false and one (1) for true.
     *
     * @param value          the query parameter value
     */
    public void addParameter(boolean value) {
        if (value) {
            addParameter(1);
        } else {
            addParameter(0);
        }
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();

        if (name != null) {
            buffer.append("database query ");
            buffer.append(name);
        } else {
            buffer.append("database SQL '");
            buffer.append(sql);
            buffer.append("'");
        }
        if (params.size() > 0) {
            buffer.append("(");
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(params.get(i));
            }
            buffer.append(")");
        }
        return buffer.toString();
    }
}
