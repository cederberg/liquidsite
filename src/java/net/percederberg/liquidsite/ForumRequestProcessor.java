/*
 * ForumRequestProcessor.java
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

package net.percederberg.liquidsite;

import java.util.Date;

import org.liquidsite.util.log.Log;

import net.percederberg.liquidsite.content.Content;
import net.percederberg.liquidsite.content.ContentForum;
import net.percederberg.liquidsite.content.ContentException;
import net.percederberg.liquidsite.content.ContentManager;
import net.percederberg.liquidsite.content.ContentPost;
import net.percederberg.liquidsite.content.ContentSecurityException;
import net.percederberg.liquidsite.content.ContentTopic;
import net.percederberg.liquidsite.content.User;
import net.percederberg.liquidsite.text.PlainFormatter;
import net.percederberg.liquidsite.web.Request;

/**
 * The forum request processor for forum posts and previews.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class ForumRequestProcessor extends RequestProcessor {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(ForumRequestProcessor.class);

    /**
     * Creates a new forum request processor.
     *
     * @param app            the application context
     */
    public ForumRequestProcessor(Application app) {
        super(app.getContentManager(), app.getBaseDir());
    }

    /**
     * Destroys this request processor. This method frees all internal
     * resources used by this processor.
     */
    public void destroy() {
        // Nothing need to be done
    }

    /**
     * Processes a forum request.
     *
     * @param request        the request object
     *
     * @throws RequestException if the action couldn't be processed
     */
    public void process(Request request) throws RequestException {
        String  action = request.getParameter("liquidsite.action");

        if (action.equals("forum.post")) {
            processPost(request);
        } else if (action.equals("forum.edit")) {
            processEdit(request);
        } else if (action.equals("forum.delete")) {
            processDelete(request);
        } else {
            LOG.warning(request + ": forum request action '" + 
                        action + "' is undefined");
            throw RequestException.INTERNAL_ERROR;
        }
    }

    /**
     * Processes a forum post request action.
     *
     * @param request        the request object
     *
     * @throws RequestException if the action couldn't be processed
     */
    private void processPost(Request request) throws RequestException {
        ContentForum    forum;
        ContentTopic    topic;
        String          subject;
        String          text;
        String          preview;
        String          str;

        // Find request parameters
        forum = findForum(request);
        topic = findTopic(request, forum);
        str = request.getParameter("liquidsite.subject", "");
        subject = PlainFormatter.clean(str);
        str = request.getParameter("liquidsite.textformat", "");
        if (!str.equals("plain")) {
            LOG.warning(request + ": text format '" + str +
                        "' is undefined");
            throw RequestException.INTERNAL_ERROR;
        }
        str = request.getParameter("liquidsite.text", "");
        text = PlainFormatter.clean(str);
        preview = request.getParameter("liquidsite.preview", "");

        // Post forum message
        if (subject.length() <= 0) {
            str = "Subject must be longer than zero characters";
            request.setAttribute("error", str);
        } else if (subject.length() > 80) {
            str = "Subject mustn't be longer than 80 characters";
            request.setAttribute("error", str);
        } else if (text.length() <= 0) {
            str = "Text must be longer than zero characters";
            request.setAttribute("error", str);
        } else if (preview.equals("true")) {
            request.setAttribute("subject", subject);
            request.setAttribute("text", text);
            request.setAttribute("previewsubject",
                                 PlainFormatter.formatHtml(subject));
            request.setAttribute("previewtext",
                                 PlainFormatter.formatHtml(text));
        } else {
            post(forum, topic, subject, text, request.getUser());
            request.setAttribute("redirect", "true");
        }
    }

    /**
     * Processes a forum edit request action.
     *
     * @param request        the request object
     *
     * @throws RequestException if the action couldn't be processed
     */
    private void processEdit(Request request) throws RequestException {
        ContentForum    forum;
        ContentTopic    topic;
        ContentPost     post;
        String          subject;
        String          text;
        String          preview;
        String          str;

        // Find request parameters
        forum = findForum(request);
        topic = findTopic(request, forum);
        if (topic == null) {
            LOG.warning(request + ": no topic in forum edit request");
            throw RequestException.INTERNAL_ERROR;
        }
        post = findPost(request, topic);
        if (post == null) {
            LOG.warning(request + ": no post in forum edit request");
            throw RequestException.INTERNAL_ERROR;
        }
        str = request.getParameter("liquidsite.subject", "");
        subject = PlainFormatter.clean(str);
        str = request.getParameter("liquidsite.textformat", "");
        if (!str.equals("plain")) {
            LOG.warning(request + ": text format '" + str +
                        "' is undefined");
            throw RequestException.INTERNAL_ERROR;
        }
        str = request.getParameter("liquidsite.text", "");
        text = PlainFormatter.clean(str);
        preview = request.getParameter("liquidsite.preview", "");

        // Post forum message
        if (subject.length() <= 0) {
            str = "Subject must be longer than zero characters";
            request.setAttribute("error", str);
        } else if (subject.length() > 80) {
            str = "Subject mustn't be longer than 80 characters";
            request.setAttribute("error", str);
        } else if (text.length() <= 0) {
            str = "Text must be longer than zero characters";
            request.setAttribute("error", str);
        } else if (preview.equals("true")) {
            request.setAttribute("subject", subject);
            request.setAttribute("text", text);
            request.setAttribute("previewsubject",
                                 PlainFormatter.formatHtml(subject));
            request.setAttribute("previewtext",
                                 PlainFormatter.formatHtml(text));
        } else {
            edit(post, subject, text, request.getUser());
            request.setAttribute("redirect", "true");
        }
    }

    /**
     * Processes a forum delete request action.
     *
     * @param request        the request object
     *
     * @throws RequestException if the action couldn't be processed
     */
    private void processDelete(Request request) throws RequestException {
        ContentForum    forum;
        ContentTopic    topic;
        ContentPost     post;

        // Find request parameters
        forum = findForum(request);
        topic = findTopic(request, forum);
        if (topic == null) {
            LOG.warning(request + ": no topic in forum delete request");
            throw RequestException.INTERNAL_ERROR;
        }
        post = findPost(request, topic);

        // Delete forum message
        delete(forum, topic, post, request.getUser());
    }

    /**
     * Finds the content forum referenced in the request.
     *
     * @param request        the request object
     *
     * @return the content forum referenced in the request
     *
     * @throws RequestException if the forum couldn't be found
     */
    private ContentForum findForum(Request request)
        throws RequestException {

        ContentManager  manager = getContentManager();
        User            user = request.getUser();
        Content         content;
        String          forum;

        // Find content object
        forum = request.getParameter("liquidsite.forum", "0");
        try {
            content = manager.getContent(user, Integer.parseInt(forum));
        } catch (NumberFormatException e) {
            LOG.warning(request + ": forum id '" + forum +
                        "' is not a number");
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            LOG.info(e.getMessage());
            throw RequestException.FORBIDDEN;
        }

        // Check content forum
        if (!(content instanceof ContentForum)) {
            LOG.warning(request + ": content id '" + forum +
                        "' is not a forum");
            throw RequestException.INTERNAL_ERROR;
        }
        return (ContentForum) content;
    }

    /**
     * Finds the content topic referenced in the request.
     *
     * @param request        the request object
     * @param forum          the content forum
     *
     * @return the content topic referenced in the request, or
     *         null if no topic was referenced
     *
     * @throws RequestException if the topic refererenced was invalid
     */
    private ContentTopic findTopic(Request request, ContentForum forum)
        throws RequestException {

        ContentManager  manager = getContentManager();
        User            user = request.getUser();
        Content         content;
        String          topic;
        int             id;

        // Find content object
        topic = request.getParameter("liquidsite.topic", "0");
        try {
            id = Integer.parseInt(topic);
            if (id == 0) {
                return null;
            }
            content = manager.getContent(user, id);
        } catch (NumberFormatException e) {
            LOG.warning(request + ": topic id '" + topic +
                        "' is not a number");
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            LOG.info(e.getMessage());
            throw RequestException.FORBIDDEN;
        }

        // Check content topic
        if (!(content instanceof ContentTopic)) {
            LOG.warning(request + ": content id '" + topic +
                        "' is not a topic");
            throw RequestException.INTERNAL_ERROR;
        } else if (content.getParentId() != forum.getId()) {
            LOG.warning(request + ": content topic '" + topic +
                        "' does not belong to forum " + forum.getId());
            throw RequestException.INTERNAL_ERROR;
        }

        return (ContentTopic) content;
    }

    /**
     * Finds the content post referenced in the request.
     *
     * @param request        the request object
     * @param topic          the content topic
     *
     * @return the content post referenced in the request, or
     *         null if no post was referenced
     *
     * @throws RequestException if the post refererenced was invalid
     */
    private ContentPost findPost(Request request, ContentTopic topic)
        throws RequestException {

        ContentManager  manager = getContentManager();
        User            user = request.getUser();
        Content         content;
        String          post;
        int             id;

        // Find content object
        post = request.getParameter("liquidsite.post", "0");
        try {
            id = Integer.parseInt(post);
            if (id == 0) {
                return null;
            }
            content = manager.getContent(user, id);
        } catch (NumberFormatException e) {
            LOG.warning(request + ": post id '" + post +
                        "' is not a number");
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            LOG.info(e.getMessage());
            throw RequestException.FORBIDDEN;
        }

        // Check content post
        if (!(content instanceof ContentPost)) {
            LOG.warning(request + ": content id '" + post +
                        "' is not a post");
            throw RequestException.INTERNAL_ERROR;
        } else if (content.getParentId() != topic.getId()) {
            LOG.warning(request + ": content post '" + post +
                        "' does not belong to topic " + topic.getId());
            throw RequestException.INTERNAL_ERROR;
        }

        return (ContentPost) content;
    }

    /**
     * Posts a message to the specified forum and topic.
     *
     * @param forum          the content forum
     * @param topic          the content topic, or null for new
     * @param subject        the message subject
     * @param text           the message text
     * @param user           the user posting the message
     *
     * @throws RequestException if an error occurred while storing
     *             the message
     */
    private void post(ContentForum forum,
                      ContentTopic topic,
                      String subject,
                      String text,
                      User user)
        throws RequestException {

        ContentManager  manager = getContentManager();
        ContentPost     post;

        try {
            if (topic == null) {
                topic = new ContentTopic(manager, forum);
                topic.setSubject(subject);
                topic.setRevisionNumber(1);
                topic.setOnlineDate(new Date());
                topic.setComment("Forum post");
                topic.save(user);
            }
            post = new ContentPost(manager, topic);
            post.setSubject(subject);
            post.setTextType(ContentPost.PLAIN_TEXT_TYPE);
            post.setText(text);
            post.setRevisionNumber(1);
            post.setOnlineDate(new Date());
            post.setComment("Forum post");
            post.save(user);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            LOG.info(e.getMessage());
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Edits a posted message.
     *
     * @param post           the content post
     * @param subject        the message subject
     * @param text           the message text
     * @param user           the user posting the message
     *
     * @throws RequestException if an error occurred while storing
     *             the message
     */
    private void edit(ContentPost post,
                      String subject,
                      String text,
                      User user)
        throws RequestException {

        if (user == null) {
            LOG.info("anonymous user cannot delete posts");
            throw RequestException.FORBIDDEN;
        } else if (!user.getName().equals(post.getAuthorName())) {
            LOG.info("user '" + user + "' cannot edit post " + 
                     post + " from '" + post.getAuthorName() + "'");
            throw RequestException.FORBIDDEN;
        }
        try {
            post.setSubject(subject);
            post.setTextType(ContentPost.PLAIN_TEXT_TYPE);
            post.setText(text);
            post.setRevisionNumber(post.getRevisionNumber() + 1);
            post.setComment("Forum post");
            post.save(user);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            LOG.info(e.getMessage());
            throw RequestException.FORBIDDEN;
        }
    }

    /**
     * Deletes a message from the specified forum and topic.
     *
     * @param forum          the content forum
     * @param topic          the content topic
     * @param post           the content post
     * @param user           the user deleting the message
     *
     * @throws RequestException if an error occurred while deleting
     *             the message
     */
    private void delete(ContentForum forum,
                        ContentTopic topic,
                        ContentPost  post,
                        User user)
        throws RequestException {

        ContentManager   manager = getContentManager();
        Content[]        posts;
        boolean          moderator;

        if (user == null) {
            LOG.info("anonymous user cannot delete posts");
            throw RequestException.FORBIDDEN;
        }
        try {
            moderator = user.isSuperUser() || forum.isModerator(user);
            if (post == null) {
                if (!moderator) {
                    LOG.info("user '" + user + "' cannot delete topic " +
                             topic);
                    throw RequestException.FORBIDDEN;
                }
                topic.delete(user);
            } else {
                if (!moderator && !user.equals(post.getAuthor())) {
                    LOG.info("user '" + user + "' cannot delete post " + 
                             post + " in topic " + topic);
                    throw RequestException.FORBIDDEN;
                }
                post.delete(user);
                posts = manager.getContentChildren(user,
                                                   topic,
                                                   Content.POST_CATEGORY);
                if (posts.length == 0) {
                    topic.delete(user);
                }
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            LOG.info(e.getMessage());
            throw RequestException.FORBIDDEN;
        }
    }
}
