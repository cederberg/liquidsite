/*
 * MailQueue.java
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

package org.liquidsite.util.mail;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;

import org.liquidsite.util.log.Log;

/**
 * An outgoing email queue. This class is used for storing outgoing
 * email messages for before being sent. Normally this queue should be
 * empty, as queueing is performed in the receiving SMTP server. This
 * queue is only used to reduce the reponse latency when processing
 * web requests. The actual sending of the mails should be performed
 * by a background thread.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class MailQueue {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(MailQueue.class);

    /**
     * The maximum mail queue size.
     */
    private static final int MAX_QUEUE_SIZE = 1000;

    /**
     * The default mail message header.
     */
    private static final String DEFAULT_HEADER = "";

    /**
     * The default mail message footer.
     */
    private static final String DEFAULT_FOOTER = 
        "\n\n" +
        "-----------------------------------------------------------------\n" +
        "Message created and sent through Liquid Site (www.liquidsite.net)\n" +
        "If you are not the intended recipient of this message, please\n" +
        "forward it to abuse@liquidsite.net for investigation. Be sure to\n" +
        "include the whole message, including the following lines.\n";

    /**
     * The one and only mail queue instance.
     */
    private static MailQueue instance = null;

    /**
     * The actual queue containing the mail messages. New messages are
     * added last and sending is performed in FIFO order.
     */
    private LinkedList queue = new LinkedList();

    /**
     * The mail session. This object contains the SMTP configuration
     * to use.
     */
    private Session session = null;

    /**
     * The header to automatically add to all messages. If set to an
     * empty string, no header will be added. If set to null, a
     * default header will be added.
     */
    private String header = null;

    /**
     * The footer to automatically add to all messages. If set to an
     * empty string, no footer will be added. If set to null, a
     * default footer will be added.
     */
    private String footer = null;

    /**
     * Returns the mail queue instance.
     *
     * @return the mail queue instance 
     */
    public static MailQueue getInstance() {
        if (instance == null) {
            instance = new MailQueue();
        }
        return instance;
    }

    /**
     * Creates a new mail queue. This constructor should only be
     * called once to avoid duplicate queues.
     */
    private MailQueue() {
        // No further initialization needed
    }

    /**
     * Initializes this mail queue.
     *
     * @param host           the mail host, or null for "localhost"
     * @param user           the mail user, or null for none
     * @param from           the mail from address, or null for none
     */
    public void initialize(String host, String user, String from) {
        Properties  props = new Properties();

        props.setProperty("mail.transport.protocol", "smtp");
        if (host == null) {
            host = "localhost";
        }
        props.setProperty("mail.host", host);
        if (user != null) {
            props.setProperty("mail.user", user);
        }
        if (from != null) {
            props.setProperty("mail.from", from);
        }
        props.setProperty("mail.smtp.connectiontimeout", "60000");
        props.setProperty("mail.smtp.timeout", "60000");
        session = Session.getInstance(props);
    }

    /**
     * Returns the current mail header.
     * 
     * @return the current mail header
     */
    public String getHeader() {
        if (header == null) {
            return DEFAULT_HEADER;
        } else  {
            return header;
        }
    }

    /**
     * Sets the mail header.
     *
     * @param header         the new header, or null for default
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Returns the current mail footer.
     * 
     * @return the current mail footer
     */
    public String getFooter() {
        if (footer == null) {
            return DEFAULT_FOOTER;
        } else {
            return footer;
        }
    }

    /**
     * Sets the mail footer.
     *
     * @param footer         the new footer, or null for default
     */
    public void setFooter(String footer) {
        this.footer = footer;
    }

    /**
     * Adds a new message to the queue. This method is thread-safe.
     *
     * @param message        the message to add
     *
     * @throws MailException if the message wasn't valid or if the
     *             queue was full
     */
    public synchronized void add(MailMessage message) throws MailException {
        String        error;

        if (!message.isValid()) {
            error = "attempt to send invalid mail message to '" +
                    message.getRecipients() + "'";
            LOG.warning(error);
            throw new MailException(error);
        }
        if (queue.size() >= MAX_QUEUE_SIZE) {
            error = "mail queue full, message to '" +
                    message.getRecipients() + "' rejected";
            LOG.error(error);
            throw new MailException(error);
        }
        if (session == null) {
            error = "mail not initialized, message to '" +
                    message.getRecipients() + "' rejected";
            LOG.error(error);
            throw new MailException(error);
        }
        addMessageText(message);
        queue.addLast(message);
        LOG.trace("queued mail message to '" + message.getRecipients() + "'");
    }

    /**
     * Adds the configured message header and footer text.
     *
     * @param message        the message to modify
     */
    private void addMessageText(MailMessage message) {
        StringBuffer  buffer = new StringBuffer();
        Iterator      iter;
        String        str;

        buffer.append(getHeader());
        buffer.append(message.getText());
        buffer.append(getFooter());
        buffer.append("\n");
        iter = message.getAttributeNames().iterator();
        while (iter.hasNext()) {
            str = iter.next().toString();
            buffer.append(str);
            buffer.append(": ");
            buffer.append(message.getAttribute(str));
            buffer.append("\n");
        }
        message.setText(buffer.toString());
    }

    /**
     * Processes the first mail message in the queue. If the queue is
     * empty, nothing is done. The message is removed from the queue
     * only if it has been sent correctly. This method is thread-safe.
     *
     * @throws MailException if the message couldn't be sent correctly
     */
    public void process() throws MailException {
        MailMessage  message;
        Transport    transport;
        Message[]    msgs;
        String       error;

        // Get first message
        synchronized (this) {
            if (queue.size() <= 0 || session == null) {
                return;
            }
            message = (MailMessage) queue.getFirst();
            LOG.trace("starting processing of mail message to '" +
                      message.getRecipients() + "'");
        }

        // Connect to SMTP server
        try {
            transport = session.getTransport();
        } catch (NoSuchProviderException e) {
            error = "failed to create SMTP transport";
            LOG.error(error, e);
            throw new MailException(error, e);
        }
        try {
            transport.connect();
        } catch (AuthenticationFailedException e) {
            error = "failed to authenticate to SMTP server";
            LOG.error(error, e);
            throw new MailException(error, e);
        } catch (MessagingException e) {
            error = "unknown error while sending message";
            LOG.error(error, e);
            throw new MailException(error, e);
        } catch (IllegalStateException e) {
            error = "already connected to SMTP transport";
            LOG.error(error, e);
            throw new MailException(error, e);
        }

        // Send mail messages
        try {
            msgs = message.createMessages(session);
            for (int i = 0; i < msgs.length; i++) {
                transport.sendMessage(msgs[i], msgs[i].getAllRecipients());
            }
        } catch (SendFailedException e) {
            error = "failed to send mail message";
            LOG.error(error, e);
            throw new MailException(error, e);
        } catch (MessagingException e) {
            error = "unknown error while sending message";
            LOG.error(error, e);
            throw new MailException(error, e);
        } finally {
            try {
                transport.close();
            } catch (MessagingException ignore) {
                // Ignore this
            }
        }

        // Dequeue sent message
        synchronized (this) {
            queue.removeFirst();
        }
        LOG.trace("finished processing of mail message to '" +
                  message.getRecipients() + "'");
    }
}
