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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package org.liquidsite.core.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import org.liquidsite.core.data.ContentData;
import org.liquidsite.core.data.DataSource;

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
     * Returns the MIME type of a file. The MIME types are configured
     * in the servlet context.
     *
     * @param context        the servlet context
     * @param fileName       the file name
     *
     * @return the MIME type of the file, or
     *         a default binary MIME type if unknown
     */
    public static String getMimeType(ServletContext context,
                                     String fileName) {

        String  type = context.getMimeType(fileName);

        return (type == null) ? "application/octet-stream" : type;
    }

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
     * @param src            the data source to use
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected ContentFile(ContentManager manager,
                          ContentData data,
                          DataSource src)
        throws ContentException {

        super(manager, data, src);
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
     * Returns the MIME type of the file. The MIME types are
     * configured in the servlet context.
     *
     * @param context        the servlet context
     *
     * @return the MIME type of the file, or
     *         a default binary MIME type if unknown
     */
    public String getMimeType(ServletContext context) {
        return getMimeType(context, getFileName());
    }

    /**
     * Returns the text content of a file. If the file isn't a text
     * file or if the file size is too big, null will be returned.
     * Also, if the text in the file isn't encoded as UTF-8, null
     * will be returned.
     *
     * @param context        the servlet context (for the MIME type)
     *
     * @return the text file contents, or
     *         null for non-text files
     *
     * @throws ContentException if the file data couldn't be read
     *             properly
     */
    public String getTextContent(ServletContext context)
        throws ContentException {

        File             file = getFile();
        String           mimeType = getMimeType(context);
        FileInputStream  stream;
        byte[]           buffer;
        int              length;
        boolean          skip = false;

        // Check for text file and maximum size
        if (!file.exists() || file.length() > 100000) {
            return null;
        } else if (mimeType == null || !mimeType.startsWith("text/")) {
            return null;
        }

        // Read text file to buffer
        try {
            stream = new FileInputStream(file);
            buffer = new byte[(int) file.length()];
            length = stream.read(buffer);
            stream.close();
        } catch (IOException e) {
            throw new ContentException("couldn't read file "+ getFileName(),
                                       e);
        }

        // Simple check for valid UTF-8 (only works for latin-1 text)
        for (int i = 0; i < length; i++) {
            if (skip) {
                skip = false;
            } else if (buffer[i] == (byte) 0xC2
                    || buffer[i] == (byte) 0xC3
                    || buffer[i] == (byte) 0xC5) {

                skip = true;
            } else if (buffer[i] < 0) {
                return null;
            }
        }

        // Convert buffer to unicode string 
        try {
            return new String(buffer, 0, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * Sets the content of the file to the specified string. If the
     * file doesn't exist it will be created, otherwise overwritten.
     *
     * @param content         the new file contents
     *
     * @throws ContentException if the file data couldn't be written
     *             properly
     */
    public void setTextContent(String content) throws ContentException {
        FileOutputStream    stream;
        OutputStreamWriter  writer;

        try {
            stream = new FileOutputStream(getFile());
            writer = new OutputStreamWriter(stream, "UTF-8");
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            throw new ContentException("couldn't write file "+ getFileName(),
                                       e);
        }
    }

    /**
     * Deletes this content revision from the database.
     *
     * @param user           the user performing the operation
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user specified didn't
     *             have write permissions
     */
    public void deleteRevision(User user)
        throws ContentException, ContentSecurityException {

        super.deleteRevision(user);
        cleanUnusedFiles();
    }

    /**
     * Validates the object data before writing to the database.
     *
     * @throws ContentException if the object data wasn't valid
     */
    protected void doValidate() throws ContentException {
        Content[]  children;

        super.doValidate();
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
     * Updates the object data in the database.
     *
     * @param src            the data source to use
     * @param user           the user performing the operation
     *
     * @throws ContentException if the object data didn't validate or
     *             if the database couldn't be accessed properly
     */
    protected void doUpdate(DataSource src, User user)
        throws ContentException {

        super.doUpdate(src, user);
        cleanUnusedFiles();
    }

    /**
     * Deletes the object data from the database.
     *
     * @param src            the data source to use
     * @param user           the user performing the operation
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    protected void doDelete(DataSource src, User user)
        throws ContentException {

        File    dir;
        File[]  files;

        super.doDelete(src, user);
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

    /**
     * Removes any unused files in the data directory. An unused file
     * is one that isn't referenced by any revision in the database.
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private void cleanUnusedFiles() throws ContentException {
        Content[]  content = getAllRevisions();
        ArrayList  usedFiles = new ArrayList();
        File       dir;
        File[]     files;

        // Find all used files
        for (int i = 0; i < content.length; i++) {
            usedFiles.add(((ContentFile) content[i]).getFile());
        }

        // Delete unused files
        try {
            dir = getDirectory();
            files = dir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!usedFiles.contains(files[i])) {
                        files[i].delete();
                    }
                }
            }
        } catch (SecurityException e) {
            throw new ContentException(
                "access denied while deleting unused files");
        }
    }
}
