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
        if (section != null) {
            return section.getId();
        }
        return 0;
    }

    /**
     * Returns the section name.
     *
     * @return the section name
     */
    public String getName() {
        if (section != null) {
            return section.getName();
        }
        return "";
    }

    /**
     * Returns the full section path.
     *
     * @return the section path
     */
    public String getPath() {
        try {
            return getPath(section);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        }
        return "";
    }

    /**
     * Returns the full section path of the specified section.
     *
     * @param section        the section
     *
     * @return the section path
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private String getPath(ContentSection section) throws ContentException {
        if (section == null) {
            return "";
        } else {
            return getPath((ContentSection) section.getParent()) + "/" +
                   section.getName();
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
