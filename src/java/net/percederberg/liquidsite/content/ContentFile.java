/*
 * ContentFile.java
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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.liquidsite.content;

import java.io.File;

import net.percederberg.liquidsite.db.DatabaseConnection;
import net.percederberg.liquidsite.dbo.ContentData;

/**
 * A web site file.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentFile extends Content {

    /**
     * The file name content attribute.
     */
    private static final String FILE_NAME_ATTRIBUTE = "FILENAME";

    /**
     * Creates a new file with default values.
     * 
     * @param manager        the content manager to use
     * @param parent         the parent content object
     * @param name           the file name
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public ContentFile(ContentManager manager, Content parent, String name) 
        throws ContentException {

        super(manager, parent.getDomain(), Content.FILE_CATEGORY);
        setParent(parent);
        setAttribute(FILE_NAME_ATTRIBUTE, name);
    }

    /**
     * Creates a new file.
     * 
     * @param manager        the content manager to use
     * @param data           the content data object
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected ContentFile(ContentManager manager,
                          ContentData data, 
                          DatabaseConnection con) 
        throws ContentException {

        super(manager, data, con);
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        return getName();
    }

    /**
     * Returns the file used for storing the data content. Note that 
     * this method cannot be called before writing the file content 
     * object to the database, as it requires the content id to 
     * create a unique file name.
     * 
     * @return the file used for storing the data content
     * 
     * @throws ContentException if the content base directory wasn't
     *             found or couldn't be created
     */
    public File getFile() throws ContentException {
        return new File(getDirectory(), getFileName());
    }

    /**
     * Returns the file name. This is the unique file name stored in
     * the database, and used to access the actual file in the 
     * content object directory. Note that the file name does NOT 
     * contain the path of the file.
     * 
     * @return the file name.
     */
    public String getFileName() {
        return getAttribute(FILE_NAME_ATTRIBUTE);
    }

    /**
     * Sets the file name. Note that the specified file name will 
     * only be used to create the new unique file name that will 
     * actually be used. Also note that an previous file will NOT be
     * renamed, effectively making getFile() return a non-existent 
     * file. After changing the file name, the desired data must be
     * written to the new file. This method has the same requirements
     * as getFile(). 
     * 
     * @param name           the new file name
     * 
     * @see #getFile
     * 
     * @throws ContentException if the content base directory wasn't
     *             found or couldn't be created
     */
    public void setFileName(String name) throws ContentException {
        File  dir = getDirectory();
        File  file;
        int   counter = 0;
        
        file = new File(dir, name);
        while (file.exists()) {
            counter++;
            file = new File(dir, counter + "." + name); 
        }
        setAttribute(FILE_NAME_ATTRIBUTE, file.getName());
    }
    
    /**
     * Returns the content directory. This directory is composed of 
     * the domain file directory and the unique content identifier. 
     * Note that this method requires that the content object has a 
     * valid content identifier, or an exception will be thrown. Also 
     * note that the content directory will be created if it doesn 
     * not already exist. 
     * 
     * @return the content directory
     * 
     * @throws ContentException if the content base directory wasn't
     *             found or couldn't be created
     */
    private File getDirectory() throws ContentException {
        File  dir;
        
        if (getId() <= 0) {
            throw new ContentException(
                "content file hasn't got a valid content id");
        }
        dir = new File(getDomain().getDirectory(), 
                       String.valueOf(getId()));
        try {
            if (!dir.exists() && !dir.mkdirs()) {
                throw new ContentException(
                    "couldn't create content file directory");
            }
        } catch (SecurityException e) {
            throw new ContentException(
                "access denied while creating content file directory");
        }
        return dir;
    }

    /**
     * Validates this data object. This method checks that all 
     * required fields have been filled with suitable values.
     * 
     * @throws ContentException if the data object contained errors
     */
    public void validate() throws ContentException {
        Content[]  children;

        super.validate();
        if (getParent() == null) {
            throw new ContentException("no parent set for file");
        }
        children = InternalContent.findByParent(getContentManager(),
                                                getParent());
        for (int i = 0; i < children.length; i++) {
            if (children[i].getId() != getId()
             && children[i].getName().equals(getName())) {

                throw new ContentException(
                    "another object with the same name is already " +
                    "present in the same folder");
            }
        }
    }

    /**
     * Deletes the object data from the database.
     * 
     * @param user           the user performing the operation
     * @param con            the database connection to use
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    protected void doDelete(User user, DatabaseConnection con)
        throws ContentException {

        File    dir;
        File[]  files;

        super.doDelete(user, con);
        try {
            dir = getDirectory();
            files = dir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
            }
            dir.delete();
        } catch (SecurityException e) {
            throw new ContentException(
                "access denied while deleting content file directory");
        }
    }
}
