/*
 * PluginBean.java
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
 * Copyright (c) 2006 Per Cederberg. All rights reserved.
 */

package org.liquidsite.app.template;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.liquidsite.util.log.Log;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;

/**
 * The plugin bean. This class provides a template bean with a list
 * of all currently loaded plugin beans. Upon access to a plugin
 * bean, a new instance will also be created. The instance of each
 * plugin bean is kept for the lifetime of this bean.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class PluginBean extends TemplateBean
    implements TemplateHashModel {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(PluginBean.class);

    /**
     * The plugin class map. Each plugin bean class is indexed by the
     * plugin bean name.
     */
    private static HashMap pluginClasses = new HashMap();

    /**
     * The template model map. Each template model in this map
     * corresponds to a plugin bean instance and is indexed by
     * the plugin bean name.
     */
    private HashMap models = new HashMap();

    /**
     * Adds a plugin class mapping.
     *
     * @param name           the template bean name
     * @param cls            the template bean class
     *
     * @throws TemplateException if the template bean mapping couldn't
     *             be added
     */
    public static void add(String name, Class cls) throws TemplateException {
        String  msg;

        if (pluginClasses.containsKey(name)) {
            msg = "cannot redefine plugin class for '" + name + "'";
            throw new TemplateException(msg);
        }
        if (!TemplateBean.class.isAssignableFrom(cls)) {
            msg = "template bean plugin class " + cls.getName() +
                  " is not instance of " + TemplateBean.class.getName();
            throw new TemplateException(msg);
        }
        pluginClasses.put(name, cls);
    }

    /**
     * Removes all registered plugin class mappings.
     */
    public static void removeAll() {
        pluginClasses.clear();
    }

    /**
     * Creates a new plugin bean.
     *
     * @param context        the bean context
     */
    PluginBean(BeanContext context) {
        super(context);
    }

    /**
     * Checks if the hash model is empty.
     *
     * @return true if no plugin beans are available, or
     *         false otherwise
     */
    public boolean isEmpty() {
        return pluginClasses.isEmpty();
    }

    /**
     * Returns a plugin bean as a template model.
     *
     * @param id             the plugin bean name
     *
     * @return the template model object, or
     *         null if the plugin name isn't defined
     */
    public TemplateModel get(String id) {
        TemplateModel  model;
        Class          cls;
        Constructor    cons;
        Object         obj;

        if (models.containsKey(id)) {
            return (TemplateModel) models.get(id);
        } else if (!pluginClasses.containsKey(id)) {
            return null;
        } else {
            cls = (Class) pluginClasses.get(id);
            try {
                cons = cls.getConstructor(new Class[] { BeanContext.class });
                obj = cons.newInstance(new Object[] { getContext() });
                if (obj instanceof TemplateModel) {
                    model = (TemplateModel) obj;
                } else {
                    model = BeansWrapper.getDefaultInstance().wrap(obj);
                }
            } catch (Exception e) {
                LOG.error("failed to create plugin bean '" + id + "': " +
                          e.toString());
                return null;
            }
            models.put(id, model);
            return model;
        }
    }
}
