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
    }

    /**
     * Processes a request.
     *
     * @param request        the request object
     */
    public void process(Request request) {
    }

    /**
     * Processes a forum post request action. The processing of the
     * posting may produce a response to the request object, which
     * must be checked before contining processing after calling this
     * method.
     *
     * @param request        the request object
     *
     * @throws RequestException if the action couldn't be processed
     */
    public void processPost(Request request) throws RequestException {
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
            request.setAttribute("result", "posted");
        }
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
            LOG.debug(e.getMessage());
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
            LOG.debug(e.getMessage());
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
     * Posts a message to the specified forum and topic.
     *
     * @param forum          the content forum
     * @param topic          the content topic, or null for new
     * @param subject        the message subject
     * @param text           the message text
     * @param user           the user posting the message
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
                topic.setComment("Forum post");
                topic.save(user);
            }
            post = new ContentPost(manager, topic);
            post.setSubject(subject);
            post.setTextType(ContentPost.PLAIN_TEXT_TYPE);
            post.setText(text);
            post.setRevisionNumber(1);
            post.setComment("Forum post");
            post.save(user);
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            throw RequestException.INTERNAL_ERROR;
        } catch (ContentSecurityException e) {
            LOG.debug(e.getMessage());
            throw RequestException.FORBIDDEN;
        }
    }
}
