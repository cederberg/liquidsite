/*
 * SessionBean.java
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

package net.percederberg.liquidsite.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

import net.percederberg.liquidsite.web.RequestSession;

/**
 * A user session bean. This class is used to access and manipulate
 * session data from the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class SessionBean implements TemplateHashModel {

    /**
     * The request data attribute.
     */
    private static final String DATA_ATTRIBUTE = "data";

    /**
     * The bean context.
     */
    private BeanContext context;

    /**
     * Creates a new session template bean.
     *
     * @param context        the bean context
     */
    SessionBean(BeanContext context) {
        this.context = context;
    }

    /**
     * Returns the request session.
     *
     * @return the request session
     */
    private RequestSession getSession() {
        return context.getRequest().getSession();
    }

    /**
     * Checks if the session data hash model is empty.
     *
     * @return false as the hash model can always be extended
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns a session data value as a template model.
     *
     * @param name           the data key name
     *
     * @return the template model object
     */
    public TemplateModel get(String name) {
        RequestSession  session = getSession();
        Object          obj;
        HashMap         map;

        // Handle methods
        if (name.equals("clear")) {
            return new TemplateMethodModel() {
                public Object exec(List args) {
                    clear();
                    return NOTHING;
                }
            };
        } else if (name.equals("destroy")) {
            return new TemplateMethodModel() {
                public Object exec(List args) {
                    destroy();
                    return NOTHING;
                }
            };
        }

        // Handle hash values
        obj = session.getAttribute(DATA_ATTRIBUTE);
        if (obj instanceof HashMap) {
            map = (HashMap) obj;
        } else {
            map = new HashMap();
            session.setAttribute(DATA_ATTRIBUTE, map);
        }
        return new SessionDataBean(map, name, -1);
    }

    /**
     * Clears all the data in the user session.
     */
    public void clear() {
        getSession().setAttribute(DATA_ATTRIBUTE, null);
    }

    /**
     * Destroys the user session. This is identical to logging out the
     * user.
     */
    public void destroy() {
        getSession().invalidate();
    }


    /**
     * The session data model bean. This bean wraps all the session
     * data hierachy. The objects can be treated as scalars, hashes or
     * sequences, mostly interchangably. This class really represents
     * a pointer to the data, making it possible to modify or remove
     * data via this bean.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    public class SessionDataBean implements TemplateScalarModel,
        TemplateHashModel, TemplateSequenceModel {

        /**
         * The parent hash map, containing this value. This references
         * is required in order to manipulate or remove the data.
         */
        private HashMap parent;

        /**
         * The data key name in the parent hash map.
         */
        private String key;

        /**
         * The array index in the data, if it represents an ArrayList.
         * This value is set to -1 if no array index has been
         * specified, which can be interpreted as an implicit 0.
         */
        private int index;

        /**
         * Creates a new session data bean.
         *
         * @param parent         the parent hash map
         * @param key            the data key name in the hash map
         * @param index          the array index, or -1 for none
         */
        SessionDataBean(HashMap parent, String key, int index) {
            this.parent = parent;
            this.key = key;
            this.index = index;
        }

        /**
         * Returns the session data value as a string. If the value
         * pointed to is a hash map, an empty string will be returned.
         * If the value is a list, the first value in the list will be
         * returned.
         *
         * @return the session data value as a string
         *
         * @throws TemplateModelException if this object points to an
         *             index that has been removed
         */
        public String getAsString() throws TemplateModelException {
            Object     obj = parent.get(key);
            ArrayList  list;
            String     message;

            if (obj instanceof String && index < 1) {
                return (String) obj;
            } else if (obj instanceof ArrayList) {
                list = (ArrayList) obj;
                if (index < 0 && list.size() > 0) {
                    obj = list.get(0);
                } else if (index >= 0 && index < list.size()) {
                    obj = list.get(index);
                } else {
                    message = "index " + index + " has been removed";
                    throw new TemplateModelException(message);
                }
                if (obj instanceof String) {
                    return (String) obj;
                }
            }
            return "";
        }

        /**
         * Checks if the hash model is empty.
         *
         * @return false as the hash model can always be extended
         */
        public boolean isEmpty() {
            return false;
        }

        /**
         * Returns a session data value from a hash key name. The
         * value will always be returned as another instance of this
         * class. If the value in this object isn't a hash map, it
         * will be created and any previous string value discarded. If
         * the value in this object is an array list, the first
         * element in the list will be used implicitly.
         *
         * @param name           the session data key name
         *
         * @return the template model object, or
         *         NOTHING if the object points to a removed index
         */
        public TemplateModel get(String name) {
            Object     obj = parent.get(key);
            HashMap    map;
            ArrayList  list;

            // Handle methods
            if (name.equals("add")) {
                return new TemplateMethodModel() {
                    public Object exec(List args)
                        throws TemplateModelException {

                        if (args.size() <= 0) {
                            add(new HashMap());
                        } else {
                            add(args.get(0));
                        }
                        return NOTHING;
                    }
                };
            } else if (name.equals("remove")) {
                return new TemplateMethodModel() {
                    public Object exec(List args) {
                        remove();
                        return NOTHING;
                    }
                };
            }

            // Handle hash values
            if (obj instanceof HashMap) {
                map = (HashMap) obj;
            } else if (obj instanceof ArrayList) {
                list = (ArrayList) obj;
                if (index < 0 && list.size() > 0) {
                    obj = list.get(0);
                } else if (index >= 0 && index < list.size()) {
                    obj = list.get(index);
                } else {
                    return NOTHING;
                }
                if (obj instanceof HashMap) {
                    map = (HashMap) obj;
                } else {
                    map = new HashMap();
                    if (index < 0) {
                        list.add(0, map);
                    } else {
                        list.add(index, map);
                    }
                }
            } else {
                map = new HashMap();
                parent.put(key, map);
            }
            return new SessionDataBean(map, name, -1);
        }

        /**
         * Returns a session data value from an array index. The value
         * will always be returned as another instance of this class.
         * If the value in this object isn't an array, the only index
         * allowed will be zero. Otherwise, only valid indices are
         * allowed, or an index out of bounds exception will be
         * launched.
         *
         * @param index          the session data index
         *
         * @return the template model object, or
         *         NOTHING if the index was out of bounds
         *
         * @throws TemplateModelException if a double index
         *             indirection was attempted
         */
        public TemplateModel get(int index) throws TemplateModelException {
            Object     obj = parent.get(key);
            ArrayList  list;
            String     message;

            if (this.index >= 0) {
                message = "multiple indexes are not supported";
                throw new TemplateModelException(message);
            }
            if (obj instanceof String && index == 0) {
                return new SessionDataBean(parent, key, index);
            } else if (obj instanceof HashMap && index == 0) {
                return new SessionDataBean(parent, key, index);
            } else if (obj instanceof ArrayList) {
                list = (ArrayList) obj;
                if (index >= 0 && index < list.size()) {
                    return new SessionDataBean(parent, key, index);
                }
            }
            return NOTHING;
        }

        /**
         * Returns the size of the session data value list. If the
         * value in this object isn't a list, zero or one will be
         * returned depending on whether it exists or not.
         *
         * @return the size of the session data value list
         */
        public int size() {
            Object  obj = parent.get(key);

            if (this.index >= 0) {
                return 0;
            }
            if (obj instanceof String) {
                return 1;
            } else if (obj instanceof HashMap) {
                return 1;
            } else if (obj instanceof ArrayList) {
                return ((ArrayList) obj).size();
            } else {
                return 0;
            }
        }

        /**
         * Adds a value to this session data object. If the object
         * already contains a value, it will be converted to a list
         * and the new value will be added last.
         *
         * @param value          the data value to add
         *
         * @throws TemplateModelException if an attempt was made to
         *             add values to a specific index
         */
        public void add(Object value) throws TemplateModelException {
            Object     obj = parent.get(key);
            ArrayList  list;
            String     message;

            if (index >= 0) {
                message = "cannot add values to a specific index";
                throw new TemplateModelException(message);
            }
            if (obj == null) {
                parent.put(key, value);
            } else if (obj instanceof ArrayList) {
                list = (ArrayList) obj;
                list.add(value);
            } else {
                list = new ArrayList();
                list.add(obj);
                list.add(value);
                parent.put(key, list);
            }
        }

        /**
         * Removes this session data object. If the object points to
         * an index in a list, only the specified index will be
         * removed.
         */
        public void remove() {
            Object     obj = parent.get(key);
            ArrayList  list;

            if (index >= 0 && obj instanceof ArrayList) {
                list = (ArrayList) obj;
                if (index < list.size()) {
                    list.remove(index);
                }
            } else {
                parent.remove(key);
            }
        }
    }
}
