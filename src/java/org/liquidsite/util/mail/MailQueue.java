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
 * Copyright (c) 2004-2006 Per Cederberg. All rights reserved.
 */

package org.liquidsite.util.mail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * The maximum mail process queue size.
     */
    private static final int MAX_PROCESS_SIZE = 5;

    /**
     * The maximum mail wait queue size.
     */
    private static final int MAX_WAIT_SIZE = 1000;

    /**
     * The maximum number of email messages to send from each
     * processing round of a message.
     */
    private static final int MAX_SEND_COUNT = 10;

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
     * The email address regular expression.
     */
    private static final Pattern ADDRESS_RE = Pattern.compile("<.+>");

    /**
     * The one and only mail queue instance.
     */
    private static MailQueue instance = null;

    /**
     * The mail processing queue. This queue is completely internal
     * and is only modified when processing mails (and there is
     * therefore no need for thread synchronization). The mail
     * messages in this queue are processed in a round-robin fashion
     * until all mails have been generated. At that point the mail
     * message is removed from this queue. 
     */
    private ArrayList processQueue = new ArrayList(MAX_PROCESS_SIZE);

    /**
     * The mail wait queue. New messages are added last to this queue
     * by any thread in the system. All accesses to this queue must
     * therefore be strictly synchronized to avoid race conditions.
     * The mail messages are moved from this queue to the process
     * queue in FIFO order when a processing slot is available.
     */
    private LinkedList waitQueue = new LinkedList();

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
        Matcher     m;

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
            m = ADDRESS_RE.matcher(from);
            from = m.find() ? m.group() : "<" + from + ">";
            props.setProperty("mail.smtp.from", from);
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
     * @throws MailMessageException if the message wasn't valid or if
     *             the queue was full
     */
    public void add(MailMessage message) throws MailMessageException {
        String  error;

        if (!message.isValid()) {
            error = "invalid mail message to '" +
                    message.getRecipient() + "'";
            LOG.warning(error);
            throw new MailMessageException(error);
        }
        if (waitQueue.size() >= MAX_WAIT_SIZE) {
            error = "mail queue full, message to '" +
                    message.getRecipient() + "' rejected";
            LOG.error(error);
            throw new MailMessageException(error);
        }
        if (session == null) {
            error = "mail not initialized, message to '" +
                    message.getRecipient() + "' rejected";
            LOG.error(error);
            throw new MailMessageException(error);
        }
        adjustMessageText(message);
        enqueue(message);
        LOG.trace("queued mail message to '" + message.getRecipient() + "'");
    }

    /**
     * Adjust the message text by adding the configured message
     * header and footer.
     *
     * @param message        the message to modify
     */
    private void adjustMessageText(MailMessage message) {
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
     * Processes the mail messages in the queue. If the queue is
     * empty, nothing is done. The messages are removed from the
     * queue only if sent correctly or if they are invalid. On mail
     * transport error, the mail messages will remain in the queue.
     *
     * @throws MailTransportException if the mail transport couldn't
     *             be initialized correctly
     */
    public void process() throws MailTransportException {
        MailMessage  message;

        // Fill up process queue
        while (processQueue.size() < MAX_PROCESS_SIZE &&
               waitQueue.size() > 0) {

            message = dequeue();
            processQueue.add(message);
            LOG.trace("starting processing of mail message to '" +
                      message.getRecipient() + "'");
        }

        // Loop through process queue once
        for (int i = 0; i < processQueue.size(); i++) {
            message = (MailMessage) processQueue.get(i);
            try {
                processMessage(message);
            } catch (MailMessageException e) {
                // Do nothing, message will skip to next address
            }
            if (!message.hasMoreMessages()) {
                processQueue.remove(i--);
                LOG.trace("finished processing of mail message to '" +
                          message.getRecipient() + "'");
            }
        }
    }

    /**
     * Processes the specified mail message. This will attempt to
     * send a number of generated mails from the message, although
     * not all.
     *
     * @param message        the mail message to send
     *
     * @throws MailTransportException if the mail transport couldn't
     *             be initialized correctly
     * @throws MailMessageException if the mail message couldn't be
     *             sent due to an error in the message 
     */
    private void processMessage(MailMessage message)
        throws MailTransportException, MailMessageException {

        Transport  transport;
        Message    msg;
        String     error;
        int        count = 0;

        // Connect to SMTP server
        try {
            transport = session.getTransport();
        } catch (NoSuchProviderException e) {
            error = "failed to create SMTP transport";
            LOG.error(error, e);
            throw new MailTransportException(error, e);
        }
        try {
            transport.connect();
        } catch (AuthenticationFailedException e) {
            error = "failed to authenticate to SMTP server";
            LOG.error(error, e);
            throw new MailTransportException(error, e);
        } catch (MessagingException e) {
            error = "unknown error while sending message";
            LOG.error(error, e);
            throw new MailTransportException(error, e);
        } catch (IllegalStateException e) {
            error = "already connected to SMTP transport";
            LOG.error(error, e);
            throw new MailTransportException(error, e);
        }

        // Send a number of mail messages
        try {
            while (message.hasMoreMessages() && count < MAX_SEND_COUNT) {
                msg = message.getNextMessage(session);
                transport.sendMessage(msg, msg.getAllRecipients());
                count++;
            }
        } catch (SendFailedException e) {
            error = "failed to send mail message";
            LOG.error(error, e);
            throw new MailMessageException(error, e);
        } catch (MessagingException e) {
            error = "unknown error while sending message";
            LOG.error(error, e);
            throw new MailMessageException(error, e);
        } finally {
            try {
                transport.close();
            } catch (MessagingException ignore) {
                // Ignore this
            }
        }
    }

    /**
     * Adds a new message last in the wait queue. This method is
     * thread-safe.
     *
     * @param message        the mail message to add
     */
    private synchronized void enqueue(MailMessage message) {
        waitQueue.addLast(message);
    }

    /**
     * Removes the first message in the wait queue and returns it.
     * This method is thread-safe.
     *
     * @return the dequeued mail message, or
     *         null if the queue was empty
     */
    private synchronized MailMessage dequeue() {
        if (waitQueue.isEmpty()) {
            return null;
        } else {
            return (MailMessage) waitQueue.removeFirst();
        }
    }
}
