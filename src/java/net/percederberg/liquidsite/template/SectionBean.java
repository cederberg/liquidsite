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

import java.util.ArrayList;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentSection;

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
     * The base template bean.
     */
    private LiquidSiteBean baseBean;

    /**
     * The section being encapsulated.
     */
    private ContentSection section;

    /**
     * Creates a new empty section template bean.
     */
    SectionBean() {
        this(null, null);
    }

    /**
     * Creates a new section template bean.
     *
     * @param baseBean       the base template bean
     * @param section        the content section, or null
     */
    SectionBean(LiquidSiteBean baseBean, ContentSection section) {
        this.baseBean = baseBean;
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

    /**
     * Returns all documents in this section and any subsections. At
     * most the specified number of documents will be returned. The
     * documents will be ordered by last modification date.
     *
     * @param offset         the number of documents to skip
     * @param count          the maximum number of documents
     *
     * @return a list of the documents found (as document beans)
     */
    public ArrayList findDocuments(int offset, int count) {
        ArrayList  results = new ArrayList();

        // TODO: add sorting (and remove the one that exists)
        // TODO: add filtering
        if (section != null) {
            try {
                baseBean.findDocuments(section, offset, count, results);
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return results;
    }
}
