/*
 * AbstractData.java
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

package net.percederberg.liquidsite.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.liquidsite.util.db.DatabaseDataException;
import org.liquidsite.util.db.DatabaseResults;

/**
 * An abstract data object. This is the base class for all the data
 * objects in this package. It contains methods for handling the data
 * parameters.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public abstract class AbstractData {

    /**
     * The parameter sets for all data objects. The parameter sets
     * are indexed by their data object class.
     */
    private static HashMap parameterSets = new HashMap();

    /**
     * Returns the parameter set for a specified data class. If no
     * parameter set existed for the specified class, a new one will
     * be created.
     *
     * @param dataClass      the data class
     *
     * @return the parameter set for the specified data class
     */
    protected static ParameterSet getParameterSet(Class dataClass) {
        ParameterSet  set;

        set = (ParameterSet) parameterSets.get(dataClass);
        if (set == null) {
            set = new ParameterSet();
            parameterSets.put(dataClass, set);
        }
        return set;
    }

    /**
     * The parameter values.
     */
    private HashMap values = new HashMap();

    /**
     * Creates a new data object. This will initialize all parameters
     * to their default values.
     */
    protected AbstractData() {
        getParameterSet(this.getClass()).initialize(this);
    }

    /**
     * Returns a parameter object value.
     *
     * @param param          the parameter name
     *
     * @return the parameter object value, or
     *         null if the parameter doesn't exist
     */
    private Object getObject(Parameter param) {
        return values.get(param);
    }

    /**
     * Returns a parameter boolean value.
     *
     * @param param          the parameter name
     *
     * @return the parameter boolean value, or
     *         false if the parameter doesn't exist
     */
    public boolean getBoolean(Parameter param) {
        Object  obj = getObject(param);

        if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        } else {
            return false;
        }
    }

    /**
     * Returns a parameter date value.
     *
     * @param param          the parameter name
     *
     * @return the parameter date value, or
     *         a zero date if the parameter doesn't exist
     */
    public Date getDate(Parameter param) {
        Object  obj = getObject(param);

        if (obj instanceof Date) {
            return (Date) obj;
        } else {
            return new Date(0);
        }
    }

    /**
     * Returns a parameter integer value.
     *
     * @param param          the parameter name
     *
     * @return the parameter integer value, or
     *         zero (0) if the parameter doesn't exist
     */
    public int getInt(Parameter param) {
        Object  obj = getObject(param);

        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else {
            return 0;
        }
    }

    /**
     * Returns a parameter string value.
     *
     * @param param          the parameter name
     *
     * @return the parameter string value, or
     *         an empty string if the parameter doesn't exist
     */
    public String getString(Parameter param) {
        Object  obj = getObject(param);

        if (obj != null) {
            return obj.toString();
        } else {
            return "";
        }
    }

    /**
     * Sets a parameter object value.
     *
     * @param param          the parameter name
     * @param value          the parameter value
     */
    private void setObject(Parameter param, Object value) {
        values.put(param, value);
    }

    /**
     * Sets a parameter boolean value.
     *
     * @param param          the parameter name
     * @param value          the parameter value
     */
    public void setBoolean(Parameter param, boolean value) {
        setObject(param, new Boolean(value));
    }

    /**
     * Sets a parameter date value.
     *
     * @param param          the parameter name
     * @param value          the parameter value
     */
    public void setDate(Parameter param, Date value) {
        setObject(param, value);
    }

    /**
     * Sets a parameter integer value.
     *
     * @param param          the parameter name
     * @param value          the parameter value
     */
    public void setInt(Parameter param, int value) {
        setObject(param, new Integer(value));
    }

    /**
     * Sets a parameter string value.
     *
     * @param param          the parameter name
     * @param value          the parameter value
     */
    public void setString(Parameter param, String value) {
        setObject(param, value);
    }

    /**
     * Sets all parameters with values from a database row.
     *
     * @param row            the database row
     *
     * @throws DatabaseDataException if the database row contained
     *             malformed data
     */
    void setAll(DatabaseResults.Row row) throws DatabaseDataException {
        getParameterSet(this.getClass()).transfer(row, this);
    }


    /**
     * A set of parameters. A parameter set is created for each data
     * object class, containing all the parameters for that data
     * object.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    private static class ParameterSet {

        /**
         * The parameters in the set.
         */
        private ArrayList parameters = new ArrayList();

        /**
         * Creates a new empty parameter set.
         */
        public ParameterSet() {
            // No further initialization needed
        }

        /**
         * Adds a parameter to the set.
         *
         * @param param          the parameter to add
         */
        public void add(Parameter param) {
            parameters.add(param);
        }

        /**
         * Initializes a data object with the default values for all
         * parameters.
         *
         * @param data           the data object
         */
        public void initialize(AbstractData data) {
            Parameter  param;

            for (int i = 0; i < parameters.size(); i++) {
                param = (Parameter) parameters.get(i);
                param.initialize(data);
            }
        }

        /**
         * Transfers a database row to a data object. This will set
         * the values for all data object parameters.
         *
         * @param row            the database row
         * @param data           the data object
         *
         * @throws DatabaseDataException if the database row
         *             contained malformed data
         */
        public void transfer(DatabaseResults.Row row, AbstractData data)
            throws DatabaseDataException {

            Parameter  param;

            for (int i = 0; i < parameters.size(); i++) {
                param = (Parameter) parameters.get(i);
                param.transfer(row, data);
            }
        }
    }


    /**
     * A data object parameter. A parameter corresponds to a column
     * in the database table.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    protected abstract static class Parameter {

        /**
         * The parameter column name.
         */
        private String column;

        /**
         * Creates a new parameter. The new parameter will be added
         * to the corresponding parameter set in the set of all data
         * object parameters. If no parameter set exists for the data
         * object class, a new parameter set will be created.
         *
         * @param dataClass      the data object class
         * @param column         the column name
         */
        protected Parameter(Class dataClass, String column) {
            this.column = column;
            getParameterSet(dataClass).add(this);
        }

        /**
         * Returns the parameter column name.
         *
         * @return the parameter column name
         */
        public String getColumn() {
            return column;
        }

        /**
         * Initializes a data object with the default value for this
         * parameter.
         *
         * @param data           the data object
         */
        public abstract void initialize(AbstractData data);

        /**
         * Transfers this parameter from a database row to a data
         * object.
         *
         * @param row            the database row
         * @param data           the data object
         *
         * @throws DatabaseDataException if the database row
         *             contained malformed data
         */
        public abstract void transfer(DatabaseResults.Row row,
                                      AbstractData data)
            throws DatabaseDataException;
    }


    /**
     * A boolean data object parameter. A parameter corresponds to a
     * column in the database table.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    protected static class BooleanParameter extends Parameter {

        /**
         * The default parameter value.
         */
        private boolean defaultValue;

        /**
         * Creates a new boolean parameter.
         *
         * @param dataClass      the data object class
         * @param column         the column name
         * @param defaultValue   the default value
         */
        public BooleanParameter(Class dataClass,
                                String column,
                                boolean defaultValue) {
            super(dataClass, column);
            this.defaultValue = defaultValue;
        }

        /**
         * Initializes a data object with the default value for this
         * parameter.
         *
         * @param data           the data object
         */
        public void initialize(AbstractData data) {
            data.setBoolean(this, defaultValue);
        }

        /**
         * Transfers this parameter from a database row to a data
         * object.
         *
         * @param row            the database row
         * @param data           the data object
         *
         * @throws DatabaseDataException if the database row
         *             contained malformed data
         */
        public void transfer(DatabaseResults.Row row, AbstractData data)
            throws DatabaseDataException {

            data.setBoolean(this, row.getBoolean(getColumn()));
        }
    }


    /**
     * A date data object parameter. A parameter corresponds to a
     * column in the database table.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    protected static class DateParameter extends Parameter {

        /**
         * The default parameter value.
         */
        private Date defaultValue;

        /**
         * Creates a new date parameter.
         *
         * @param dataClass      the data object class
         * @param column         the column name
         * @param defaultValue   the default value
         */
        public DateParameter(Class dataClass,
                             String column,
                             Date defaultValue) {
            super(dataClass, column);
            this.defaultValue = defaultValue;
        }

        /**
         * Initializes a data object with the default value for this
         * parameter.
         *
         * @param data           the data object
         */
        public void initialize(AbstractData data) {
            data.setDate(this, defaultValue);
        }

        /**
         * Transfers this parameter from a database row to a data
         * object.
         *
         * @param row            the database row
         * @param data           the data object
         *
         * @throws DatabaseDataException if the database row
         *             contained malformed data
         */
        public void transfer(DatabaseResults.Row row, AbstractData data)
            throws DatabaseDataException {

            data.setDate(this, row.getDate(getColumn()));
        }
    }


    /**
     * An integer data object parameter. A parameter corresponds to a
     * column in the database table.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    protected static class IntegerParameter extends Parameter {

        /**
         * The default parameter value.
         */
        private int defaultValue;

        /**
         * Creates a new integer parameter.
         *
         * @param dataClass      the data object class
         * @param column         the column name
         * @param defaultValue   the default value
         */
        public IntegerParameter(Class dataClass,
                                String column,
                                int defaultValue) {
            super(dataClass, column);
            this.defaultValue = defaultValue;
        }

        /**
         * Initializes a data object with the default value for this
         * parameter.
         *
         * @param data           the data object
         */
        public void initialize(AbstractData data) {
            data.setInt(this, defaultValue);
        }

        /**
         * Transfers this parameter from a database row to a data
         * object.
         *
         * @param row            the database row
         * @param data           the data object
         *
         * @throws DatabaseDataException if the database row
         *             contained malformed data
         */
        public void transfer(DatabaseResults.Row row, AbstractData data)
            throws DatabaseDataException {

            data.setInt(this, row.getInt(getColumn()));
        }
    }


    /**
     * A string data object parameter. A parameter corresponds to a
     * column in the database table.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    protected static class StringParameter extends Parameter {

        /**
         * The default parameter value.
         */
        private String defaultValue;

        /**
         * Creates a new string parameter.
         *
         * @param dataClass      the data object class
         * @param column         the column name
         * @param defaultValue   the default value
         */
        public StringParameter(Class dataClass,
                               String column,
                               String defaultValue) {
            super(dataClass, column);
            this.defaultValue = defaultValue;
        }

        /**
         * Initializes a data object with the default value for this
         * parameter.
         *
         * @param data           the data object
         */
        public void initialize(AbstractData data) {
            data.setString(this, defaultValue);
        }

        /**
         * Transfers this parameter from a database row to a data
         * object.
         *
         * @param row            the database row
         * @param data           the data object
         *
         * @throws DatabaseDataException if the database row
         *             contained malformed data
         */
        public void transfer(DatabaseResults.Row row, AbstractData data)
            throws DatabaseDataException {

            data.setString(this, row.getString(getColumn()));
        }
    }
}
