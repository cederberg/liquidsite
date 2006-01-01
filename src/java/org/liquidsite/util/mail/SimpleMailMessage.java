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

import java.util.ArrayList;
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
public class SimpleMailMessage extends MailMessage {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(SimpleMailMessage.class);

    /**
     * The list of mail recipients. This list contains InternetAddress
     * instances.
     */
    private ArrayList recipients = new ArrayList();

    /**
     * The current recipient position.
     */
    private int position = 0;

    /**
     * Creates a new empty mail message.
     */
    public SimpleMailMessage() {
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
        return super.isValid() && recipients.size() > 0;
    }

    /**
     * Returns a string representation of the message recipient. This
     * method is used for logging purposes, so the returned string
     * shouldn't be too long. 
     *
     * @return the message recipient
     */
    public String getRecipient() {
        return getRecipients();
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
     * Checks if there remains any Java mail MIME messages to
     * generate.
     *
     * @return true if there are more messages to generate, or
     *         false otherwise
     */
    protected boolean hasMoreMessages() {
        return position < recipients.size();
    }

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
    protected MimeMessage getNextMessage(Session session)
        throws MailMessageException {

        InternetAddress  address;

        address = (InternetAddress) recipients.get(position++);
        return createMessage(session, address);
    }
}
