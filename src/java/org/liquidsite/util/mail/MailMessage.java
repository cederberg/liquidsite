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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package org.liquidsite.util.mail;

import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.liquidsite.util.log.Log;

/**
 * An email message. This class is used for creating email messages
 * for the outgoing mail queue. Even though a message is registered
 * with multiple recipients, a single unique message will be sent to
 * each of the recipients.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class MailMessage {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(MailMessage.class);

    /**
     * The character set to use for the messages.
     */
    private static final String CHARACTER_SET = "ISO-8859-1";

    /**
     * The list of mail recipients. This list contains InternetAddress
     * instances.
     */
    private ArrayList recipients = new ArrayList();

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
     * Creates a new empty mail message.
     */
    public MailMessage() {
        // No further initialization needed
    }

    /**
     * Returns a string representation of this mail message.
     *
     * @return a string representation of this mail message
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();
        String        str;

        buffer.append("To: ");
        buffer.append(getRecipients());
        buffer.append("\n");
        str = getReplyTo();
        if (str != null) {
            buffer.append("Reply-To: ");
            buffer.append(str);
            buffer.append("\n");
        }
        buffer.append("Subject: ");
        buffer.append(getSubject());
        buffer.append("\n\n");
        buffer.append(getText());
        return buffer.toString();
    }

    /**
     * Checks if this message is valid. A message becomes valid once
     * it has at least one recipient and non-empty subject and text
     * content.
     *
     * @return true if the message is valid, or
     *         false otherwise
     */
    public boolean isValid() {
        return recipients.size() > 0
            && subject.length() > 0
            && text.length() > 0;
    }

    /**
     * Returns a string representation of the list of message
     * recipients.
     *
     * @return the message recipients, each separated with a comma (',')
     */
    public String getRecipients() {
        StringBuffer  buffer = new StringBuffer();

        for (int i = 0; i < recipients.size(); i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(recipients.get(i).toString());
        }
        return buffer.toString();
    }

    /**
     * Sets the list of message recipients. The list is a string of
     * comma separated mail addesses with the format specified in RFC
     * 822. This method will clear any previously existing message
     * recipients.
     *
     * @param recipients     the message recipient addresses
     *
     * @throws MailMessageException if the list of message recipient
     *             addresses wasn't possible to parse correctly
     */
    public void setRecipients(String recipients)
        throws MailMessageException {

        this.recipients.clear();
        addRecipients(recipients);
    }

    /**
     * Adds a list of message recipients. The list is a string of
     * comma separated mail addesses with the format specified in RFC
     * 822.
     *
     * @param recipients     the message recipient addresses
     *
     * @throws MailMessageException if the list of message recipient
     *             addresses wasn't possible to parse correctly
     */
    public void addRecipients(String recipients)
        throws MailMessageException {

        InternetAddress[]  addresses;
        String             error;

        try {
            addresses = InternetAddress.parse(recipients);
        } catch (AddressException e) {
            error = "failed to parse mail address(es) '" + recipients + "'";
            LOG.info(error, e);
            throw new MailMessageException(error, e);
        }
        for (int i = 0; i < addresses.length; i++) {
            this.recipients.add(addresses[i]);
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
            this.replyTo = new InternetAddress(address);
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
     * Creates the Java mail MIME messages corresponding to this
     * message.
     *
     * @param session        the Java mail session
     *
     * @return the Java MIME messages created
     *
     * @throws MailMessageException if the messages couldn't be
     *             created correctly
     */
    protected MimeMessage[] createMessages(Session session)
        throws MailMessageException {

        MimeMessage[]      msgs = new MimeMessage[recipients.size()];
        InternetAddress[]  addresses;
        String             error;

        try {
            for (int i = 0; i < recipients.size(); i++) {
                msgs[i] = new MimeMessage(session);
                msgs[i].setSentDate(new Date());
                msgs[i].setFrom();
                if (replyTo != null) {
                    addresses = new InternetAddress[1];
                    addresses[0] = replyTo;
                    msgs[i].setReplyTo(addresses);
                }
                addresses = new InternetAddress[1];
                addresses[0] = (InternetAddress) recipients.get(i);
                msgs[i].setRecipients(Message.RecipientType.TO, addresses);
                msgs[i].setSubject(subject, CHARACTER_SET);
                msgs[i].setText(text, CHARACTER_SET);
                msgs[i].saveChanges();
            }
        } catch (MessagingException e) {
            error = "failed to create mail message to '" + getRecipients() +
                    "'";
            LOG.error(error, e);
            throw new MailMessageException(error, e);
        }
        return msgs;
    }
}
