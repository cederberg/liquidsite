/*
 * DocumentFileBean.java
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

import java.util.Date;

import net.percederberg.liquidsite.Log;
import net.percederberg.liquidsite.content.ContentDocument;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentFile;

/**
 * A document file bean. This class is used to access document files
 * from the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class DocumentFileBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(DocumentFileBean.class);

    /**
     * The bean context.
     */
    private BeanContext context;

    /**
     * The parent document bean.
     */
    private DocumentBean parent;

    /**
     * The file being encapsulated.
     */
    private ContentFile file;

    /**
     * Creates a new document file template bean.
     *
     * @param context        the bean context
     * @param parent         the parent document
     * @param file           the content file
     */
    DocumentFileBean(BeanContext context,
                     DocumentBean parent,
                     ContentFile file) {

        this.context = context;
        this.parent = parent;
        this.file = file;
    }

    /**
     * Returns the file identifier.
     *
     * @return the file identifier number
     */
    public int getId() {
        return file.getId();
    }

    /**
     * Returns the name of the file.
     *
     * @return the file name
     */
    public String getName() {
        return file.getName();
    }

    /**
     * Returns the full file path.
     *
     * @return the file path
     */
    public String getPath() {
        return parent.getPath() + "/" + getName();
    }

    /**
     * Returns the file parent (always a document).
     *
     * @return the file parent
     */
    public DocumentBean getParent() {
        return parent;
    }

    /**
     * Returns the file revision number.
     *
     * @return the file revision number 
     */
    public int getRevision() {
        return file.getRevisionNumber();
    }

    /**
     * Returns the file revision date.
     *
     * @return the file revision date
     */
    public Date getDate() {
        return file.getModifiedDate();
    }

    /**
     * Returns the file revision author login name.
     *
     * @return the file revision user name
     */
    public String getUser() {
        return file.getAuthorName();
    }

    /**
     * Returns the file online flag.
     *
     * @return the file online flag
     */
    public boolean getOnline() {
        return file.isOnline();
    }

    /**
     * Returns the file size.
     *
     * @return the file size
     */
    public long getSize() {
        try {
            return file.getFile().length();
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        }
        return 0L;
    }

    /**
     * Returns the file MIME type.
     *
     * @return the file MIME type
     */
    public String getMimeType() {
        return file.getMimeType();
    }

    /**
     * Returns the file lock.
     *
     * @return the file lock object
     */
    public LockBean getLock() {
        if (file != null) {
            try {
                return new LockBean(file.getLock());
            } catch (ContentException e) {
                LOG.error(e.getMessage());
            }
        }
        return new LockBean(null);
    }
}
