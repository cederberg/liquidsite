/*
 * PublishDialogHandler.java
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

package net.percederberg.liquidsite.admin;

import java.text.ParseException;
import java.util.Date;

import net.percederberg.liquidsite.Request;
import net.percederberg.liquidsite.admin.view.AdminView;
import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.form.FormValidationException;
import net.percederberg.liquidsite.form.FormValidator;

/**
 * The publish request handler. This class handles the publish dialog 
 * workflow for content objects.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class PublishDialogHandler extends AdminDialogHandler {

    /**
     * The form validator.
     */
    private FormValidator validator = new FormValidator();

    /**
     * Creates a new publish request handler.
     */
    public PublishDialogHandler() {
        super("index.html", "publish.html", true);
        initialize();
    }

    /**
     * Initializes the form validator.
     */
    private void initialize() {
        String  error;
        
        validator.addRequiredConstraint("date", "No publish date specified");
        error = "Date format should be 'YYYY-MM-DD HH:MM'";
        validator.addDateConstraint("date", AdminUtils.DATE_FORMAT, error); 
        validator.addRequiredConstraint("comment", "No comment specified");
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

        Content  content = (Content) AdminUtils.getReference(request);

        AdminView.DIALOG.viewPublish(request, content);
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
     * @throws FormValidationException if the form request data 
     *             validation failed
     */
    protected void validateStep(Request request, int step) 
        throws FormValidationException {

        validator.validate(request);
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

        Content  content = (Content) AdminUtils.getReference(request);
        Date     date;
        String   comment;
        boolean  recursive;

        try {
            date = AdminUtils.parseDate(request.getParameter("date"));
        } catch (ParseException e) {
            date = new Date();
        }
        recursive = request.getParameter("recursive", "").equals("true");
        comment = request.getParameter("comment");
        publish(request.getUser(), content, date, comment, recursive);
        return 0;
    }
    
    private void publish(User user,
                         Content content,
                         Date date,
                         String comment,
                         boolean recursive)
        throws ContentException, ContentSecurityException {

        ContentManager  manager = AdminUtils.getContentManager();
        Content[]       list;
        int             max = content.getRevisionNumber();

        if (content.getRevisionNumber() == 0) {
            list = content.getAllRevisions();
            for (int i = 0; i < list.length; i++) {
                if (max < list[i].getRevisionNumber()) {
                    max = list[i].getRevisionNumber();
                }
            }
            content.setRevisionNumber(max + 1);
        } else {
            content.setRevisionNumber(content.getRevisionNumber() + 1);
            content.setComment(comment);
        }
        content.setOnlineDate(date);
        content.setOfflineDate(null);
        content.save(user);
        if (recursive) {
            list = manager.getContentChildren(user, content);
            for (int i = 0; i < list.length; i++) {
                if (!list[i].isOnline() || list[i].getRevisionNumber() == 0) {
                    publish(user, list[i], date, comment, recursive);
                }
            }
        }
    }
}
