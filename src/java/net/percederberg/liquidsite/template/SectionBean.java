/*
 * SectionBean.java
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

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.content.ContentSection;
import net.percederberg.liquidsite.content.ContentException;

/**
 * A section template bean. This class is used to access sections from
 * the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class SectionBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(SectionBean.class);

    /**
     * The section being encapsulated.
     */
    private ContentSection section;

    /**
     * Creates a new empty section template bean.
     */
    SectionBean() {
        this(null);
    }

    /**
     * Creates a new section template bean.
     *
     * @param section         the content section, or null
     */
    SectionBean(ContentSection section) {
        this.section = section;
    }

    /**
     * Returns the section identifier.
     *
     * @return the section identifier
     */
    public int getId() {
        if (section == null) {
            return 0;
        } else {
            return section.getId();
        }
    }
}
