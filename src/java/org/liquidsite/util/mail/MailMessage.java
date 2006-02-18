/*
 * MailMessage.java
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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.liquidsite.util.log.Log;

/**
 * An email message. This is the base class for all types of email
 * messages in the outgoing mail queue. The queue will iterate over
 * all the message recipients one by one and send each one a single
 * unique message. Due to queueing considerations, it is possible
 * that other messages are delivered before all emails have been
 * generated for a message.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public abstract class MailMessage {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(MailMessage.class);

    /**
     * The character set to use for the messages.
     */
    private static final String CHARACTER_SET = "ISO-8859-1";

    /**
     * The message from address.
     */
    private InternetAddress from = null;

    /**
     * The message reply-to address.
     */
    private InternetAddress replyTo = null;

    /**
     * The message subject.
     */
    private String subject = "";

    /**
     * The message text.
     */
    private String text = "";

    /**
     * The additional message attributes.
     */
    private HashMap attributes = new HashMap();

    /**
     * Checks if this message is valid. A message becomes valid once
     * it has at least one recipient and non-empty subject and text
     * content.
     *
     * @return true if the message is valid, or
     *         false otherwise
     */
    public boolean isValid() {
        return subject.length() > 0
            && text.length() > 0;
    }

    /**
     * Returns a string representation of the message recipient. This
     * method is used for logging purposes, so the returned string
     * shouldn't be too long. 
     *
     * @return the message recipient
     */
    public abstract String getRecipient();

    /**
     * Returns the message from address.
     *
     * @return the message from address, or
     *         null if none has been set
     */
    public String getFrom() {
        if (from == null) {
            return null;
        } else {
            return from.toString();
        }
    }

    /**
     * Returns the message from address.
     *
     * @return the message from address, or
     *         null if none has been set
     */
    public InternetAddress getFromAddress() {
        return from;
    }

    /**
     * Sets the message from address.
     *
     * @param address        the from address
     *
     * @throws MailMessageException if the address wasn't possible to
     *             parse correctly
     */
    public void setFrom(String address) throws MailMessageException {
        String  error;

        try {
            if (address == null) {
                this.from = null;
            } else {
                this.from = new InternetAddress(address);
            }
        } catch (AddressException e) {
            error = "failed to parse mail from address '" + address + "'";
            LOG.info(error, e);
            throw new MailMessageException(error, e);
        }
    }

    /**
     * Returns the message reply to address.
     *
     * @return the message reply to address, or
     *         null if none has been set
     */
    public String getReplyTo() {
        if (replyTo == null) {
            return null;
        } else {
            return replyTo.toString();
        }
    }

    /**
     * Returns the message reply to address.
     *
     * @return the message reply to address, or
     *         null if none has been set
     */
    public InternetAddress getReplyToAddress() {
        return replyTo;
    }

    /**
     * Sets the message reply-to address.
     *
     * @param address        the reply-to address
     *
     * @throws MailMessageException if the reply-to address wasn't
     *             possible to parse correctly
     */
    public void setReplyTo(String address) throws MailMessageException {
        String  error;

        try {
            if (address == null) {
                this.replyTo = null;
            } else {
                this.replyTo = new InternetAddress(address);
            }
        } catch (AddressException e) {
            error = "failed to parse mail reply-to address '" + address + "'";
            LOG.info(error, e);
            throw new MailMessageException(error, e);
        }
    }

    /**
     * Returns the message subject.
     *
     * @return the message subject, or
     *         an empty string if not set
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the message subject.
     *
     * @param subject        the message subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Returns the message text.
     *
     * @return the message text, or
     *         an empty string if not set
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the message text.
     *
     * @param text           the message text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the value for a specified attribute. Message attributes
     * can be used for storing information about the origin of a
     * message or other information that is not part of the message
     * itself.
     *
     * @param name           the message attribute name
     *
     * @return the message attribute value, or
     *         null if not set
     */
    public String getAttribute(String name) {
        return (String) attributes.get(name);
    }

    /**
     * Returns a collection with all the attribute names. Message
     * attributes can be used for storing information about the origin
     * of a message or other information that is not part of the
     * message itself.
     *
     * @return a collection with the message attribute names
     */
    public Collection getAttributeNames() {
        return attributes.keySet();
    }

    /**
     * Sets a message attribute value. Message attributes can be used
     * for storing information about the origin of a message or other
     * information that is not part of the message itself.
     *
     * @param name           the message attribute name
     * @param value          the message attribute value
     */
    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    /**
     * Checks if there remains any Java mail MIME messages to
     * generate.
     *
     * @return true if there are more messages to generate, or
     *         false otherwise
     */
    protected abstract boolean hasMoreMessages();

    /**
     * Creates the next Java mail MIME message from this mail
     * message.
     *
     * @param session        the Java mail session
     *
     * @return the Java MIME message created
     *
     * @throws MailMessageException if the message couldn't be
     *             created correctly
     */
    protected abstract MimeMessage getNextMessage(Session session)
        throws MailMessageException;

    /**
     * Creates a Java mail MIME message.
     *
     * @param session        the Java mail session
     * @param recipient      the mail recipient
     *
     * @return the Java MIME message created
     *
     * @throws MailMessageException if the message couldn't be
     *             created correctly
     */
    protected MimeMessage createMessage(Session session,
                                        InternetAddress recipient)
        throws MailMessageException {

        MimeMessage       msg = new MimeMessage(session);
        InternetAddress[] addresses;
        String            error;

        try {
            msg.setSentDate(new Date());
            if (getFromAddress() == null) {
                msg.setFrom();
            } else {
                msg.setFrom(getFromAddress());
            }
            if (getReplyToAddress() != null) {
                addresses = new InternetAddress[1];
                addresses[0] = getReplyToAddress();
                msg.setReplyTo(addresses);
            }
            addresses = new InternetAddress[1];
            addresses[0] = recipient;
            msg.setRecipients(Message.RecipientType.TO, addresses);
            msg.setSubject(getSubject(), CHARACTER_SET);
            msg.setText(getText(), CHARACTER_SET);
            msg.saveChanges();
        } catch (MessagingException e) {
            error = "failed to create mail message to '" + recipient + "'";
            LOG.error(error, e);
            throw new MailMessageException(error, e);
        }
        return msg;
    }
}
