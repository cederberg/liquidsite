/*
 * GroupMailMessage.java
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
 * Copyright (c) 2006 Per Cederberg. All rights reserved.
 */

package org.liquidsite.util.mail;

import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.Group;
import org.liquidsite.core.content.User;

/**
 * A group email message. This class is used for creating email
 * messages to all users in a group.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class GroupMailMessage extends MailMessage {

    /**
     * The maximum number of users to retrieve at once.
     */
    private static final int MAX_USERS = 60;

    /**
     * The mail recipient group.
     */
    private Group recipient = null;

    /**
     * The current user offset in the group.
     */
    private int offset = 0;

    /**
     * The array of users read from the group.
     */
    private User[] users = null;

    /**
     * The current position in the user array.
     */
    private int position = 0;

    /**
     * The next recipient address.
     */
    private InternetAddress nextRecipient = null;

    /**
     * Creates a new empty group mail message.
     */
    public GroupMailMessage() {
        // No further initialization needed
    }

    /**
     * Returns a string representation of the message recipient. This
     * method is used for logging purposes, so the returned string
     * shouldn't be too long. 
     *
     * @return the message recipient
     */
    public String getRecipient() {
        if (recipient == null) {
            return "undefined group";
        } else {
            return "group " + recipient.getName() + " in domain " +
                   recipient.getDomainName();
        }
    }

    /**
     * Sets the message recipient group.
     *
     * @param recipient      the message recipient group
     */
    public void setRecipient(Group recipient) {
        this.recipient = recipient;
    }

    /**
     * Checks if there remains any Java mail MIME messages to
     * generate.
     *
     * @return true if there are more messages to generate, or
     *         false otherwise
     */
    protected boolean hasMoreMessages() {
        if (nextRecipient == null) {
            nextRecipient = getNextRecipient();
        }
        return nextRecipient != null;
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

        InternetAddress address;

        if (nextRecipient == null) {
            nextRecipient = getNextRecipient();
        }
        address = nextRecipient;
        nextRecipient = null;
        if (address != null) {
            return createMessage(session, address);
        } else {
            throw new MailMessageException("all valid user already sent to");
        }
    }

    /**
     * Returns the next recipient in the group. This method will read
     * all the group members in blocks and return all valid email
     * addresses.
     *
     * @return the next recipient address in the group, or
     *         null if all recipients have been returned
     */
    private InternetAddress getNextRecipient() {
        String  email;

        while (true) {
            if (users == null) {
                try {
                    users = recipient.getUsers(offset, MAX_USERS);
                } catch (ContentException e) {
                    return null;
                }
                offset += users.length;
                position = 0;
            } else if (position >= users.length) {
                if (users.length < MAX_USERS) {
                    return null;
                }
                users = null;
            } else {
                email = users[position++].getEmail();
                if (email.length() > 0) {
                    try {
                        return new InternetAddress(email);
                    } catch (AddressException e) {
                        // Skip to next user, do nothing
                    }
                }
            }
        }
    }
}
