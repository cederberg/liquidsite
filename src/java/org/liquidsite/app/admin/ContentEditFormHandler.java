/*
 * ContentEditFormHandler.java
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

package org.liquidsite.app.admin;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.liquidsite.app.admin.view.AdminView;
import org.liquidsite.core.content.Content;
import org.liquidsite.core.content.ContentDocument;
import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentFile;
import org.liquidsite.core.content.ContentForum;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.core.content.ContentPost;
import org.liquidsite.core.content.ContentSection;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.ContentTopic;
import org.liquidsite.core.content.DocumentProperty;
import org.liquidsite.core.web.FormHandlingException;
import org.liquidsite.core.web.FormValidationException;
import org.liquidsite.core.web.FormValidator;
import org.liquidsite.core.web.Request;
import org.liquidsite.core.web.Request.FileParameter;
import org.liquidsite.core.web.RequestSession;

/**
 * The content edit request handler. This class handles the edit
 * workflow for the content view.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ContentEditFormHandler extends AdminFormHandler {

    /***
     * The latest object instance created.
     */
    private static ContentEditFormHandler instance = null;

    /**
     * The section form validator.
     */
    private FormValidator sectionValidator = new FormValidator();

    /**
     * The document form validator.
     */
    private FormValidator documentValidator = new FormValidator();

    /**
     * The forum form validator.
     */
    private FormValidator forumValidator = new FormValidator();

    /**
     * The topic form validator.
     */
    private FormValidator topicValidator = new FormValidator();

    /**
     * The post form validator.
     */
    private FormValidator postValidator = new FormValidator();

    /**
     * Returns an instance of this class. If a prior instance has
     * been created, it will be returned instead of creating a new
     * one.
     *
     * @return an instance of a content edit form handler
     */
    public static ContentEditFormHandler getInstance() {
        if (instance == null) {
            return new ContentEditFormHandler();
        } else {
            return instance;
        }
    }

    /**
     * Creates a new content edit request handler.
     */
    public ContentEditFormHandler() {
        super("content.html", "edit-content.html", true);
        instance = this;
        initialize();
    }

    /**
     * Initializes all the form validators.
     */
    private void initialize() {
        String  error;

        // Add and edit section validator
        error = "No section name specified";
        sectionValidator.addRequiredConstraint("name", error);
        error = "Section name contains invalid character";
        sectionValidator.addCharacterConstraint("name",
                                                CONTENT_CHARS,
                                                error);
        error = "No revision comment specified";
        sectionValidator.addRequiredConstraint("comment", error);

        // Add and edit document validator
        error = "No document name specified";
        documentValidator.addRequiredConstraint("name", error);
        error = "Document name contains invalid character";
        documentValidator.addCharacterConstraint("name",
                                                 CONTENT_CHARS,
                                                 error);
        error = "No revision comment specified";
        documentValidator.addRequiredConstraint("comment", error);

        // Add and edit forum validator
        error = "No forum name specified";
        forumValidator.addRequiredConstraint("name", error);
        error = "Forum name contains invalid character";
        forumValidator.addCharacterConstraint("name",
                                              CONTENT_CHARS,
                                              error);
        error = "No real forum name specified";
        forumValidator.addRequiredConstraint("realname", error);
        error = "No forum description specified";
        forumValidator.addRequiredConstraint("description", error);
        error = "No revision comment specified";
        forumValidator.addRequiredConstraint("comment", error);

        // Add and edit topic validator
        error = "No topic subject specified";
        topicValidator.addRequiredConstraint("subject", error);
        error = "No revision comment specified";
        topicValidator.addRequiredConstraint("comment", error);

        // Add and edit post validator
        error = "No post subject specified";
        postValidator.addRequiredConstraint("subject", error);
        error = "No post text specified";
        postValidator.addRequiredConstraint("text", error);
        error = "No revision comment specified";
        postValidator.addRequiredConstraint("comment", error);
    }

    /**
     * Displays a form for the specified workflow step. This method
     * will NOT be called when returning to the start page.
     *
     * @param request        the request object
     * @param step           the workflow step
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    protected void displayStep(Request request, int step)
        throws ContentException, ContentSecurityException {

        Object  ref = AdminUtils.getReference(request);

        if (ref instanceof ContentSection) {
            AdminView.CONTENT.viewEditSection(request,
                                              null,
                                              (ContentSection) ref);
        } else if (ref instanceof ContentDocument) {
            AdminView.CONTENT.viewEditDocument(request,
                                               (ContentDocument) ref);
        } else if (ref instanceof ContentFile) {
            AdminView.CONTENT.viewEditFile(request, (ContentFile) ref);
        } else if (ref instanceof ContentForum) {
            AdminView.CONTENT.viewEditForum(request, (ContentForum) ref);
        } else if (ref instanceof ContentTopic) {
            AdminView.CONTENT.viewEditTopic(request, (ContentTopic) ref);
        } else if (ref instanceof ContentPost) {
            AdminView.CONTENT.viewEditPost(request, (ContentPost) ref);
        } else {
            throw new ContentException("cannot edit this object");
        }
    }

    /**
     * Validates a form for the specified workflow step. If the form
     * validation fails in this step, the form page for the workflow
     * step will be displayed again with an 'error' attribute
     * containing the message in the validation exception.
     *
     * @param request        the request object
     * @param step           the workflow step
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     * @throws FormValidationException if the form request data
     *             validation failed
     */
    protected void validateStep(Request request, int step)
        throws ContentException, ContentSecurityException,
               FormValidationException {

        String  category;
        String  action;
        String  message;

        category = request.getParameter("category", "");
        if (category.equals("section")) {
            sectionValidator.validate(request);
            message = "Another object with identical name already " +
                      "exists in the parent section or domain";
            validateParent(request, "parent", message);
        } else if (category.equals("document")) {
            action = request.getParameter("action", "");
            if (action.equals("reload") || action.equals("filedelete")) {
                // No validation needed
            } else if (action.equals("upload")) {
                validateUpload(request);
            } else {
                documentValidator.validate(request);
                message = "Another object with identical name " +
                          "already exists in the parent section";
                validateParent(request, "section", message);
            }
        } else if (category.equals("file")) {
            validateFile(request);
        } else if (category.equals("forum")) {
            forumValidator.validate(request);
            message = "Another object with identical name already " +
                      "exists in the parent section";
            validateParent(request, "section", message);
        } else if (category.equals("topic")) {
            topicValidator.validate(request);
        } else if (category.equals("post")) {
            postValidator.validate(request);
        } else {
            message = "Unknown content category specified";
            throw new FormValidationException("category", message);
        }
    }

    /**
     * Validates a content file edit form.
     *
     * @param request        the request object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     * @throws FormValidationException if the form request data
     *             validation failed
     */
    private void validateFile(Request request)
        throws ContentException, ContentSecurityException,
               FormValidationException {

        ContentManager  manager = AdminUtils.getContentManager();
        Content         content;
        String          name;
        int             id;
        String          message;

        name = request.getParameter("name");
        if (name == null || name.equals("")) {
            message = "No file name specified";
            throw new FormValidationException("name", message);
        }
        for (int i = 0; i < name.length(); i++) {
            if (CONTENT_CHARS.indexOf(name.charAt(i)) < 0) {
                message = "File name contains invalid character: " +
                          "'" + name.charAt(i) + "'";
                throw new FormValidationException("name", message);
            }
        }
        content = (Content) AdminUtils.getReference(request);
        id = content.getId();
        content = manager.getContentChild(request.getUser(),
                                          content.getParent(),
                                          name);
        if (content != null && content.getId() != id) {
            message = "Another file with identical name is already " +
                      "attached to the parent document";
            throw new FormValidationException("name", message);
        }
        validateComment(request);
    }

    /**
     * Validates a document file upload.
     *
     * @param request        the request object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     * @throws FormValidationException if the form request data
     *             validation failed
     */
    private void validateUpload(Request request)
        throws ContentException, ContentSecurityException,
               FormValidationException {

        ContentManager  manager = AdminUtils.getContentManager();
        RequestSession  session = request.getSession();
        FileParameter   param;
        String          name;
        Content         content;
        String          message;

        param = request.getFileParameter("upload");
        if (param == null || param.getSize() <= 0) {
            message = "No file to add specified";
            throw new FormValidationException("upload", message);
        }
        name = convertFileName(param.getName());
        if (session.getFile(name) != null) {
            message = "Another file named '" + name + "' already exists";
            throw new FormValidationException("upload", message);
        }
        content = (Content) AdminUtils.getReference(request);
        if (content instanceof ContentDocument) {
            content = manager.getContentChild(request.getUser(),
                                              content,
                                              name);
            if (content != null) {
                message = "Another file named '" + name +
                          "' already exists";
                throw new FormValidationException("upload", message);
            }
        }
    }

    /**
     * Handles a validated form for the specified workflow step. This
     * method returns the next workflow step, i.e. the step used when
     * calling the display method. If the special zero (0) workflow
     * step is returned, the workflow is assumed to have terminated.
     * Note that this method also allows additional validation to
     * occur. By returning the incoming workflow step number and
     * setting the appropriate request attributes the same results as
     * in the normal validate method can be achieved. For recoverable
     * errors, this is the recommended course of action.
     *
     * @param request        the request object
     * @param step           the workflow step
     *
     * @return the next workflow step, or
     *         zero (0) if the workflow has finished
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    protected int handleStep(Request request, int step)
        throws ContentException, ContentSecurityException {

        Object  ref = AdminUtils.getReference(request);
        String  action;

        if (ref instanceof ContentSection) {
            handleEditSection(request, (ContentSection) ref);
        } else if (ref instanceof ContentDocument) {
            action = request.getParameter("action", "");
            if (action.equals("reload")) {
                return step;
            } else if (action.equals("upload")) {
                handleFileUpload(request);
                return step;
            } else if (action.equals("filedelete")) {
                handleFileRemoval(request);
                return step;
            } else {
                handleEditDocument(request, (ContentDocument) ref);
            }
        } else if (ref instanceof ContentFile) {
            handleEditFile(request, (ContentFile) ref);
        } else if (ref instanceof ContentForum) {
            handleEditForum(request, (ContentForum) ref);
        } else if (ref instanceof ContentTopic) {
            handleEditTopic(request, (ContentTopic) ref);
        } else if (ref instanceof ContentPost) {
            handleEditPost(request, (ContentPost) ref);
        } else {
            throw new ContentException("cannot edit this object");
        }
        return 0;
    }

    /**
     * Handles the edit section form.
     *
     * @param request        the request object
     * @param section        the section content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleEditSection(Request request, ContentSection section)
        throws ContentException, ContentSecurityException {

        Map                 params = request.getAllParameters();
        Iterator            iter = params.keySet().iterator();
        DocumentProperty[]  properties;
        DocumentProperty    property;
        String              name;
        String              id;
        String              str;
        int                 parent;

        section.setRevisionNumber(0);
        section.setName(request.getParameter("name"));
        section.setDescription(request.getParameter("description"));
        section.setComment(request.getParameter("comment"));
        try {
            parent = Integer.parseInt(request.getParameter("parent"));
            section.setParentId(parent);
        } catch (NumberFormatException ignore) {
            // This is ignored
        }
        while (iter.hasNext()) {
            name = iter.next().toString();
            if (name.startsWith("property.") && name.endsWith(".position")) {
                name = name.substring(0, name.length() - 9);
                id = name.substring(9);
                property = new DocumentProperty(id);
                str = request.getParameter(name + ".name", id);
                property.setName(str);
                try {
                    str = request.getParameter(name + ".position", "1");
                    property.setPosition(Integer.parseInt(str));
                } catch (NumberFormatException ignore) {
                    // This is ignored
                }
                try {
                    str = request.getParameter(name + ".type", "1");
                    property.setType(Integer.parseInt(str));
                } catch (NumberFormatException ignore) {
                    // This is ignored
                }
                str = request.getParameter(name + ".description", "");
                property.setDescription(str);
                section.setDocumentProperty(id, property);
            }
        }
        properties = section.getAllDocumentProperties();
        for (int i = 0; i < properties.length; i++) {
            id = properties[i].getId();
            if (!params.containsKey("property." + id + ".position")) {
                section.setDocumentProperty(id, null);
            }
        }
        if (request.getParameter("action", "").equals("publish")) {
            section.setRevisionNumber(section.getMaxRevisionNumber() + 1);
            section.setOnlineDate(new Date());
            section.setOfflineDate(null);
        }
        section.save(request.getUser());
    }

    /**
     * Handles the edit document form.
     *
     * @param request        the request object
     * @param doc            the document content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleEditDocument(Request request, ContentDocument doc)
        throws ContentException, ContentSecurityException {

        Map       params = request.getAllParameters();
        Iterator  iter = params.keySet().iterator();
        int       section;
        String    id;
        int       type;
        String    str;

        doc.setRevisionNumber(0);
        doc.setName(request.getParameter("name"));
        doc.setComment(request.getParameter("comment"));
        try {
            section = Integer.parseInt(request.getParameter("section"));
            doc.setParentId(section);
        } catch (NumberFormatException ignore) {
            // This is ignored
        }
        while (iter.hasNext()) {
            str = iter.next().toString();
            if (str.startsWith("property.")) {
                id = str.substring(9);
                try {
                    str = request.getParameter("propertytype." + id);
                    type = Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    type = DocumentProperty.STRING_TYPE;
                }
                doc.setPropertyType(id, type);
                str = request.getParameter("property." + id);
                if (type == DocumentProperty.HTML_TYPE) {
                    str = AdminUtils.cleanHtml(str);
                }
                doc.setProperty(id, str);
            }
        }
        iter = doc.getPropertyIdentifiers().iterator();
        while (iter.hasNext()) {
            id = iter.next().toString();
            if (!params.containsKey("property." + id)) {
                doc.setProperty(id, null);
            }
        }
        if (request.getParameter("action", "").equals("publish")) {
            doc.setRevisionNumber(doc.getMaxRevisionNumber() + 1);
            doc.setOnlineDate(new Date());
            doc.setOfflineDate(null);
        }
        doc.save(request.getUser());
        handleAddDocumentFiles(request, doc);
    }

    /**
     * Handles the edit file form.
     *
     * @param request        the request object
     * @param file           the file content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleEditFile(Request request, ContentFile file)
        throws ContentException, ContentSecurityException {

        FileParameter  param;
        String         name;

        try {
            file.setRevisionNumber(0);
            file.setName(request.getParameter("name"));
            file.setComment(request.getParameter("comment"));
            param = request.getFileParameter("upload");
            if (param != null && param.getSize() > 0) {
                file.setFileName(param.getName());
                param.write(file.getFile());
            } else if (request.getParameter("content") != null) {
                name = file.getFileName();
                name = name.substring(name.indexOf(".") + 1);
                if (name.indexOf(".") <= 0) {
                    file.setFileName(file.getFileName());
                } else {
                    file.setFileName(name);
                }
                file.setTextContent(request.getParameter("content"));
            }
            if (request.getParameter("action", "").equals("publish")) {
                file.setRevisionNumber(file.getMaxRevisionNumber() + 1);
                file.setOnlineDate(new Date());
                file.setOfflineDate(null);
            }
            file.save(request.getUser());
        } catch (IOException e) {
            throw new ContentException(e.getMessage());
        }
    }

    /**
     * Handles the edit forum form.
     *
     * @param request        the request object
     * @param forum          the forum content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleEditForum(Request request, ContentForum forum)
        throws ContentException, ContentSecurityException {

        int  section;

        forum.setRevisionNumber(0);
        forum.setName(request.getParameter("name"));
        forum.setRealName(request.getParameter("realname"));
        forum.setDescription(request.getParameter("description"));
        forum.setModeratorName(request.getParameter("moderator"));
        forum.setComment(request.getParameter("comment"));
        try {
            section = Integer.parseInt(request.getParameter("section"));
            forum.setParentId(section);
        } catch (NumberFormatException ignore) {
            // This is ignored
        }
        if (request.getParameter("action", "").equals("publish")) {
            forum.setRevisionNumber(forum.getMaxRevisionNumber() + 1);
            forum.setOnlineDate(new Date());
            forum.setOfflineDate(null);
        }
        forum.save(request.getUser());
    }

    /**
     * Handles the edit topic form.
     *
     * @param request        the request object
     * @param topic          the topic content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleEditTopic(Request request, ContentTopic topic)
        throws ContentException, ContentSecurityException {

        int  forum;

        topic.setRevisionNumber(0);
        topic.setSubject(request.getParameter("subject"));
        topic.setLocked(request.getParameter("action", "").equals("true"));
        topic.setComment(request.getParameter("comment"));
        try {
            forum = Integer.parseInt(request.getParameter("forum"));
            topic.setParentId(forum);
        } catch (NumberFormatException ignore) {
            // This is ignored
        }
        if (request.getParameter("action", "").equals("publish")) {
            topic.setRevisionNumber(topic.getMaxRevisionNumber() + 1);
            topic.setOnlineDate(new Date());
            topic.setOfflineDate(null);
        }
        topic.save(request.getUser());
    }

    /**
     * Handles the edit post form.
     *
     * @param request        the request object
     * @param post           the post content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    private void handleEditPost(Request request, ContentPost post)
        throws ContentException, ContentSecurityException {

        post.setRevisionNumber(0);
        post.setSubject(request.getParameter("subject"));
        post.setTextType(ContentPost.PLAIN_TEXT_TYPE);
        post.setText(request.getParameter("text"));
        post.setComment(request.getParameter("comment"));
        if (request.getParameter("action", "").equals("publish")) {
            post.setRevisionNumber(post.getMaxRevisionNumber() + 1);
            post.setOnlineDate(new Date());
            post.setOfflineDate(null);
        }
        post.save(request.getUser());
    }

    /**
     * Handles a file upload (in the document editing form). The file
     * will be added to the request session.
     *
     * @param request        the request object
     *
     * @throws ContentException if the file couldn't be added to the
     *             session correctly
     */
    public void handleFileUpload(Request request)
        throws ContentException {

        FileParameter   param;
        String          name;
        File            file;

        try {
            param = request.getFileParameter("upload");
            name = convertFileName(param.getName());
            file = param.write();
            request.getSession().addFile(name, file);
        } catch (IOException e) {
            throw new ContentException(e.getMessage());
        }
    }

    /**
     * Handles a file removal (in the document editing form). The
     * file will either be removed from the request session or
     * deleted as a content object.
     *
     * @param request        the request object
     */
    public void handleFileRemoval(Request request) {
        request.getSession().removeFile(request.getParameter("filename"));
    }

    /**
     * Handles adding all session files to a document.
     *
     * @param request        the request object
     * @param doc            the document content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             required permissions
     */
    public void handleAddDocumentFiles(Request request, ContentDocument doc)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        RequestSession  session = request.getSession();
        ContentFile     file;
        Iterator        iter;
        String          name;
        File            data;
        String          error;

        iter = session.getAllFiles().keySet().iterator();
        while (iter.hasNext()) {
            name = (String) iter.next();
            data = session.getFile(name);
            file = new ContentFile(manager, doc, name);
            file.setName(name);
            file.setComment(doc.getComment());
            file.setRevisionNumber(1);
            file.setOnlineDate(new Date());
            file.save(request.getUser());
            try {
                data.renameTo(file.getFile());
            } catch (Exception e) {
                error = "couldn't move session file " + data + " to " +
                        file.getFile() + ": " + e.getMessage();
                throw new ContentException(error);
            }
        }
        session.removeAllFiles();
    }

    /**
     * This method removes any files attached to the user session. It
     * also calls the superclass implementation to unlock any locked
     * objects.
     *
     * @param request        the request object
     *
     * @throws FormHandlingException if an error was encountered
     *             while processing the form
     */
    protected void workflowExited(Request request)
        throws FormHandlingException {

        super.workflowExited(request);
        request.getSession().removeAllFiles();
    }
}
