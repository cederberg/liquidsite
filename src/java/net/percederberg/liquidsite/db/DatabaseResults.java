/*
 * DatabaseResults.java
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

package net.percederberg.liquidsite.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * A database result container.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DatabaseResults {

    /**
     * The map with column names and indices. Each column name is
     * mapped to it's corresponding ordinal number (starting from 0).
     */
    private HashMap columnName = new HashMap();

    /**
     * The list of rows in the result. This list contains a Row
     * object for each database row.
     */
    private ArrayList rows = new ArrayList();

    /**
     * Creates a new empty database results container.
     */
    DatabaseResults() {
        // No further initialization needed
    }

    /**
     * Creates a new database results container.
     *
     * @param results        the result set to use
     *
     * @throws SQLException if the data couldn't be extracted from
     *             the result set
     */
    DatabaseResults(ResultSet results) throws SQLException {
        ResultSetMetaData  meta;
        int                cols;
        Row                row;
        int                i;

        // Extract column names
        meta = results.getMetaData();
        cols = meta.getColumnCount();
        for (i = 0; i < cols; i++) {
            columnName.put(meta.getColumnName(i + 1), new Integer(i));
        }

        // Extract result data
        while (results.next()) {
            row = new Row();
            for (i = 0; i < cols; i++) {
                 row.add(results.getObject(i + 1));
            }
            rows.add(row);
        }
    }

    /**
     * Returns the number of columns in the result.
     *
     * @return the number of columns in the result
     */
    public int getColumnCount() {
        return columnName.size();
    }

    /**
     * Returns the column position, starting from zero (0).
     *
     * @param name           the column name
     *
     * @return the column position, or
     *         -1 if the column name wasn't recognized
     */
    public int getColumnPosition(String name) {
        Integer  pos = (Integer) columnName.get(name);

        if (pos == null) {
            return -1;
        } else {
            return pos.intValue();
        }
    }

    /**
     * Returns the number of rows in the result.
     *
     * @return the number of rows in the result
     */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * Returns a specified row in the result.
     *
     * @param row            the row number
     *
     * @return the database result row, or
     *         null if no such row exists
     *
     * @throws DatabaseDataException if the row number was out of
     *             bounds
     */
    public Row getRow(int row) throws DatabaseDataException {
        if (row < 0 || row >= getRowCount()) {
            throw new DatabaseDataException(
                "no row " + row + " in database results (" +
                getRowCount() + " rows present)");
        }
        return (Row) rows.get(row);
    }


    /**
     * A database results row.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    public class Row {

        /**
         * The list of row elements.
         */
        private ArrayList elements = new ArrayList();

        /**
         * Creates a new empty row.
         */
        Row() {
            // No further initialization needed
        }

        /**
         * Adds an element to the row.
         *
         * @param elem       the element to add
         */
        void add(Object elem) {
            elements.add(elem);
        }

        /**
         * Returns the number of columns in the row.
         *
         * @return the number of columns in the row
         */
        public int getColumnCount() {
            return elements.size();
        }

        /**
         * Returns the row value in the specified column.
         *
         * @param column     the column number, 0 <= column < count
         *
         * @return the row value in the specified column, or
         *         null if the column contained a NULL value
         *
         * @throws DatabaseDataException if the column number was out
         *             of bounds
         */
        public Object get(int column) throws DatabaseDataException {
            if (column < 0 || column >= elements.size()) {
                throw new DatabaseDataException(
                    "no column " + column + " in database results (" +
                    elements.size() + " columns present)");
            }
            return elements.get(column);
        }

        /**
         * Returns the row value in the specified column.
         *
         * @param column     the column name
         *
         * @return the row value in the specified column, or
         *         null if the column contained a NULL value
         *
         * @throws DatabaseDataException if the column name wasn't
         *             present in the results
         */
        public Object get(String column) throws DatabaseDataException {
            int  pos = getColumnPosition(column);

            if (pos < 0) {
                throw new DatabaseDataException(
                    "no column named '" + column + "' in results");
            }
            return elements.get(pos);
        }

        /**
         * Returns the row boolean value in the specified column.
         *
         * @param column     the column number, 0 <= column < count
         *
         * @return the row boolean value in the specified column, or
         *         false if the column contained a NULL value
         *
         * @throws DatabaseDataException if the column number was out
         *             of bounds, or if the value wasn't a boolean
         *             value
         */
        public boolean getBoolean(int column) throws DatabaseDataException {
            Object  obj = get(column);

            if (obj == null) {
                return false;
            } else if (obj instanceof Boolean) {
                return ((Boolean) obj).booleanValue();
            } else if (obj instanceof Number) {
                return ((Number) obj).intValue() != 0;
            } else {
                throw new DatabaseDataException(
                    "column " + column + " didn't contain a " +
                    "boolean value: " + obj);
            }
        }

        /**
         * Returns the row boolean value in the specified column.
         *
         * @param column     the column name
         *
         * @return the row boolean value in the specified column, or
         *         false if the column contained a NULL value
         *
         * @throws DatabaseDataException if the column name wasn't
         *             present in the results, or if the value wasn't
         *             a boolean value
         */
        public boolean getBoolean(String column) throws DatabaseDataException {
            Object  obj = get(column);

            if (obj == null) {
                return false;
            } else if (obj instanceof Boolean) {
                return ((Boolean) obj).booleanValue();
            } else if (obj instanceof Number) {
                return ((Number) obj).intValue() != 0;
            } else {
                throw new DatabaseDataException(
                    "column '" + column + "' didn't contain a " +
                    "boolean value: " + obj);
            }
        }

        /**
         * Returns the row date value in the specified column.
         *
         * @param column     the column number, 0 <= column < count
         *
         * @return the row date value in the specified column, or
         *         null if the column contained a NULL value
         *
         * @throws DatabaseDataException if the column number was out
         *             of bounds, or if the value wasn't a date value
         */
        public Date getDate(int column) throws DatabaseDataException {
            Object  obj = get(column);

            if (obj == null || obj instanceof Date) {
                return (Date) obj;
            } else {
                throw new DatabaseDataException(
                    "column " + column + " didn't contain a " +
                    "date value: " + obj);
            }
        }

        /**
         * Returns the row date value in the specified column.
         *
         * @param column     the column name
         *
         * @return the row date value in the specified column, or
         *         null if the column contained a NULL value
         *
         * @throws DatabaseDataException if the column name wasn't
         *             present in the results, or if the value wasn't
         *             a date value
         */
        public Date getDate(String column) throws DatabaseDataException {
            Object  obj = get(column);

            if (obj == null || obj instanceof Date) {
                return (Date) obj;
            } else {
                throw new DatabaseDataException(
                    "column '" + column + "' didn't contain a " +
                    "date value: " + obj);
            }
        }

        /**
         * Returns the row integer value in the specified column.
         *
         * @param column     the column number, 0 <= column < count
         *
         * @return the row integer value in the specified column, or
         *         zero (0) if the column contained a NULL value
         *
         * @throws DatabaseDataException if the column number was out
         *             of bounds, or if the value wasn't an integer
         *             value
         */
        public int getInt(int column) throws DatabaseDataException {
            Object  obj = get(column);

            if (obj == null) {
                return 0;
            } else if (obj instanceof Number) {
                return ((Number) obj).intValue();
            } else {
                throw new DatabaseDataException(
                    "column " + column + " didn't contain an " +
                    "integer value: " + obj);
            }
        }

        /**
         * Returns the row integer value in the specified column.
         *
         * @param column     the column name
         *
         * @return the row integer value in the specified column, or
         *         zero (0) if the column contained a NULL value
         *
         * @throws DatabaseDataException if the column name wasn't
         *             present in the results, or if the value wasn't
         *             an integer value
         */
        public int getInt(String column) throws DatabaseDataException {
            Object  obj = get(column);

            if (obj == null) {
                return 0;
            } else if (obj instanceof Number) {
                return ((Number) obj).intValue();
            } else {
                throw new DatabaseDataException(
                    "column '" + column + "' didn't contain an " +
                    "integer value: " + obj);
            }
        }

        /**
         * Returns the row long value in the specified column.
         *
         * @param column     the column number, 0 <= column < count
         *
         * @return the row long value in the specified column, or
         *         zero (0) if the column contained a NULL value
         *
         * @throws DatabaseDataException if the column number was out
         *             of bounds, or if the value wasn't a long value
         */
        public long getLong(int column) throws DatabaseDataException {
            Object  obj = get(column);

            if (obj == null) {
                return 0;
            } else if (obj instanceof Number) {
                return ((Number) obj).longValue();
            } else {
                throw new DatabaseDataException(
                    "column " + column + " didn't contain a " +
                    "long value: " + obj);
            }
        }

        /**
         * Returns the row long value in the specified column.
         *
         * @param column     the column name
         *
         * @return the row long value in the specified column, or
         *         zero (0) if the column contained a NULL value
         *
         * @throws DatabaseDataException if the column name wasn't
         *             present in the results, or if the value wasn't
         *             a long value
         */
        public long getLong(String column) throws DatabaseDataException {
            Object  obj = get(column);

            if (obj == null) {
                return 0;
            } else if (obj instanceof Number) {
                return ((Number) obj).longValue();
            } else {
                throw new DatabaseDataException(
                    "column '" + column + "' didn't contain a " +
                    "long value: " + obj);
            }
        }

        /**
         * Returns the row string value in the specified column.
         *
         * @param column     the column number, 0 <= column < count
         *
         * @return the row string value in the specified column, or
         *         null if the column contained a NULL value
         *
         * @throws DatabaseDataException if the column number was out
         *             of bounds
         */
        public String getString(int column) throws DatabaseDataException {
            Object  obj = get(column);

            if (obj == null) {
                return null;
            } else {
                return obj.toString();
            }
        }

        /**
         * Returns the row string value in the specified column.
         *
         * @param column     the column name
         *
         * @return the row string value in the specified column, or
         *         null if the column contained a NULL value
         *
         * @throws DatabaseDataException if the column name wasn't
         *             present in the results
         */
        public String getString(String column) throws DatabaseDataException {
            Object  obj = get(column);

            if (obj == null) {
                return null;
            } else {
                return obj.toString();
            }
        }
    }
}
