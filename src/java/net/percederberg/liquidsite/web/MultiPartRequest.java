/*
 * MultiPartRequest.java
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

package net.percederberg.liquidsite.web;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import org.liquidsite.util.log.Log;

/**
 * An HTTP multi-part request and response.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class MultiPartRequest extends Request {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(MultiPartRequest.class);

    /**
     * The temporary upload directory.
     */
    private String uploadDir = "";

    /**
     * The maximum upload size.
     */
    private int uploadSize = 0;

    /**
     * The normal query or post parameters.
     */
    private HashMap parameters = new HashMap();

    /**
     * The file parameters.
     */
    private HashMap files = new HashMap();

    /**
     * Creates a new multi-part request.
     *
     * @param context        the servlet context
     * @param request        the HTTP request
     * @param response       the HTTP response
     * @param tempDir        the temporary upload directory
     * @param maxSize        the maximum upload size (in bytes)
     *
     * @throws ServletException if the request couldn't be parsed
     *             correctly
     */
    public MultiPartRequest(ServletContext context,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            String tempDir,
                            int maxSize)
        throws ServletException {

        super(context, request, response);
        this.uploadDir = tempDir;
        this.uploadSize = maxSize;
        try {
            parse(request);
        } catch (FileUploadException e) {
            LOG.error(e.getMessage());
            throw new ServletException(
                "Couldn't handle multipart request: " + e.getMessage());
        }
    }

    /**
     * Parses the incoming multi-part HTTP request.
     *
     * @param request        the HTTP request
     *
     * @throws FileUploadException if the request couldn't be parsed
     *             correctly
     */
    private void parse(HttpServletRequest request)
        throws FileUploadException {

        DiskFileUpload  parser = new DiskFileUpload();
        List            list;
        FileItem        item;
        String          value;

        // Create multi-part parser
        parser.setRepositoryPath(uploadDir);
        parser.setSizeMax(uploadSize);
        parser.setSizeThreshold(4096);

        // Parse request
        list = parser.parseRequest(request);
        for (int i = 0; i < list.size(); i++) {
            item = (FileItem) list.get(i);
            if (item.isFormField()) {
                try {
                    value = item.getString("UTF-8");
                } catch (UnsupportedEncodingException ignore) {
                    value = item.getString();
                }
                parameters.put(item.getFieldName(), value);
            } else {
                files.put(item.getFieldName(), new MultiPartFile(item));
            }
        }
    }

    /**
     * Returns a map with all the request parameter names and values.
     *
     * @return the map with request parameter names and values
     */
    public Map getAllParameters() {
        return parameters;
    }

    /**
     * Returns the value of a request parameter. If the specified
     * parameter does not exits, a default value will be returned.
     *
     * @param name           the request parameter name
     * @param defVal         the default parameter value
     *
     * @return the request parameter value, or
     *         the default value if no such parameter was found
     */
    public String getParameter(String name, String defVal) {
        String  value = (String) parameters.get(name);

        return (value == null) ? defVal : value;
    }

    /**
     * Returns the specified file request parameter.
     *
     * @param name           the request parameter name
     *
     * @return the request file parameter, or
     *         null if no such file parameter was found
     */
    public FileParameter getFileParameter(String name) {
        return (FileParameter) files.get(name);
    }

    /**
     * Disposes of all resources used by this request object. This
     * method shouldn't be called until a response has been written.
     */
    public void dispose() {
        Iterator  iter = files.values().iterator();

        super.dispose();
        while (iter.hasNext()) {
            ((MultiPartFile) iter.next()).dispose();
        }
        parameters.clear();
        files.clear();
        parameters = null;
        files = null;
    }

    /**
     * Returns the upload directory.
     *
     * @return the upload directory
     */
    protected String getUploadDir() {
        return uploadDir; 
    }

    /**
     * A request file parameter.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  1.0
     */
    private class MultiPartFile implements FileParameter {

        /**
         * The file item.
         */
        private FileItem item;

        /**
         * Creates a new request file parameter.
         *
         * @param item           the file item
         */
        MultiPartFile(FileItem item) {
            this.item = item;
        }

        /**
         * Returns the base file name including the extension. The
         * file name returned is guaranteed to not contain any file
         * path or directory name.
         *
         * @return the base file name (with extension)
         */
        public String getName() {
            String  name = item.getName();

            if (name.lastIndexOf("/") >= 0) {
                name = name.substring(name.lastIndexOf("/") + 1);
            }
            if (name.lastIndexOf("\\") >= 0) {
                name = name.substring(name.lastIndexOf("\\") + 1);
            }
            return name;
        }

        /**
         * Returns the full file name including path and extension.
         * The file name returned should be exactly the one sent by
         * the browser.
         *
         * @return the full file path
         */
        public String getPath() {
            return item.getName();
        }

        /**
         * Returns the file size.
         *
         * @return the file size
         */
        public long getSize() {
            return item.getSize();
        }

        /**
         * Writes this file to a temporary file in the upload
         * directory. After calling this method, only the dispose()
         * method can be called.
         *
         * @return the file created
         *
         * @throws IOException if the file parameter couldn't be
         *             written
         */
        public File write() throws IOException {
            String  name = getName();
            File    file;

            file = new File(getUploadDir(), name);
            for (int i = 1; file.exists(); i++) {
                file = new File(getUploadDir(), i + "." + name);
            }
            write(file);
            return file;
        }

        /**
         * Writes this file to the specified destination file. After
         * calling this method, only the dispose() method can be
         * called.
         *
         * @param dest           the destination file
         *
         * @throws IOException if the file parameter couldn't be
         *             written to the specified file
         */
        public void write(File dest) throws IOException {
            String  error;

            try {
                item.write(dest);
            } catch (Exception e) {
                error = "couldn't move request file to " + dest +
                        ": " + e.getMessage();
                throw new IOException(error);
            }
        }

        /**
         * Disposes of all resources used by this object. This method
         * shouldn't be called until the file parameter should no
         * longer be used.
         */
        void dispose() {
            item.delete();
        }
    }
}
