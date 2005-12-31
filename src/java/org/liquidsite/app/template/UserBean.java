/*
 * UserBean.java
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
 * Copyright (c) 2004-2005 Per Cederberg. All rights reserved.
 */

package org.liquidsite.app.template;

import java.util.ArrayList;

import org.liquidsite.core.content.ContentException;
import org.liquidsite.core.content.ContentSecurityException;
import org.liquidsite.core.content.Domain;
import org.liquidsite.core.content.Group;
import org.liquidsite.core.content.User;
import org.liquidsite.core.web.RequestSession;
import org.liquidsite.util.log.Log;

/**
 * A user template bean. This class is used to insert the user object
 * in the into the template data model.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class UserBean {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(UserBean.class);

    /**
     * The session email verification user attribute. This session
     * key value stores the login name for the user for which an
     * email verification request was sent. This value is required
     * for security reasons so that only the correct user can be
     * unlocked. 
     *
     * @see #VERIFY_KEY_ATTRIBUTE
     * @see #sendEmailVerification(String, String, String)
     * @see #verifyEmail(String)
     */
    private static final String VERIFY_USER_ATTRIBUTE =
        "template.verification.user";

    /**
     * The session email verification key attribute. This session key
     * value is only sent to the user email address and is not
     * retreivable through the public template API. If the user can
     * provide the correct verification key, the user object is
     * unlocked for saving by anonymous users. This is used to
     * overwrite the user password without requiring a correct login
     * (used for password "recovery").
     *
     * @see #VERIFY_USER_ATTRIBUTE
     * @see #sendEmailVerification(String, String, String)
     * @see #verifyEmail(String)
     */
    private static final String VERIFY_KEY_ATTRIBUTE =
        "template.verification.key";

    /**
     * The request user.
     */
    private User user;

    /**
     * The bean context.
     */
    private BeanContext context;

    /**
     * The modified login username.
     */
    private String login;

    /**
     * The modified password.
     */
    private String password;

    /**
     * The modified real name.
     */
    private String realName;

    /**
     * The modified email.
     */
    private String email;

    /**
     * The email verified flag. This flag is set when the user has
     * provided the correct verification key, i.e. has been able to
     * read an email that was previously sent. Once the email has
     * been verified, this object is unlocked for saving by anonymous
     * users. This can be used to overwrite the user password without
     * requiring a correct login (used for password "recovery").
     */
    private boolean emailVerified;

    /**
     * Creates a new user template bean.
     *
     * @param context        the bean context
     * @param user           the request user
     */
    UserBean(BeanContext context, User user) {
        this.context = context;
        this.user = user;
        this.login = null;
        this.password = null;
        this.realName = null;
        this.email = null;
        this.emailVerified = false;
    }

    /**
     * Returns a description of this user. The description will
     * contain the real user name and the login name.
     *
     * @return a description of this user, or
     *         an empty string if the user doesn't exist
     */
    public String toString() {
        if (user == null) {
            return "";
        } else {
            return user.getRealName() + " (" + user.getName() + ")";
        }
    }

    /**
     * Returns the user login name.
     *
     * @return the user login name, or
     *         an empty string if the user doesn't exist
     */
    public String getLogin() {
        if (user == null) {
            return "";
        } else {
            return user.getName();
        }
    }

    /**
     * Sets the login user name. This action will not take effect
     * until this object is saved. Note that login names of existing
     * users cannot be modified.
     *
     * @param login          the new use login name
     */
    public void setLogin(String login) {
        if (user == null) {
            this.login = login;
        }
    }

    /**
     * Sets the user password. This action will not take effect
     * until this object is saved.
     *
     * @param password       the new user password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the real user name.
     *
     * @return the real user name, or
     *         an empty string if the user doesn't exist
     */
    public String getRealName() {
        if (user == null) {
            return "";
        } else {
            return user.getRealName();
        }
    }

    /**
     * Sets the real user name. This action will not take effect
     * until this object is saved.
     *
     * @param realName       the new real user name
     */
    public void setRealName(String realName) {
        this.realName = realName;
    }

    /**
     * Returns the user email address.
     *
     * @return the user email address, or
     *         an empty string if the user doesn't exist
     */
    public String getEmail() {
        if (user == null) {
            return "";
        } else {
            return user.getEmail();
        }
    }

    /**
     * Sets the user e-mail address. This action will not take effect
     * until this object is saved.
     *
     * @param email          the new user e-mail address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Checks if the users has administration privileges in the
     * domain. For superusers, this method will always return true.
     *
     * @return true if the user is a domain admin, or
     *         false otherwise
     */
    public boolean getDomainadmin() {
        Domain  domain;

        try {
            domain = user.getDomain();
            if (domain == null) {
                return true;
            } else {
                return domain.hasAdminAccess(user);
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    /**
     * Returns the superuser flag for the user.
     *
     * @return true if the user is a superuser, or
     *         false otherwise
     */
    public boolean getSuperuser() {
        if (user == null) {
            return false;
        } else {
            return user.isSuperUser();
        }
    }

    /**
     * Returns a list of the group names to which the user belong.
     *
     * @return a list of group names
     */
    public ArrayList getGroups() {
        ArrayList  list = new ArrayList();
        Group[]    groups;

        try {
            if (user != null) {
                groups = user.getGroups();
                for (int i = 0; i < groups.length; i++) {
                    list.add(groups[i].getName());
                }
            }
        } catch (ContentException e) {
            LOG.error(e.getMessage());
        }
        return list;
    }

    /**
     * Checks if the user is member of a named group.
     *
     * @param name            the group name
     *
     * @return true if the user is a member of the group, or
     *         false otherwise
     */
    public boolean inGroup(String name) {
        ArrayList  groups = getGroups();

        return groups.contains(name);
    }

    /**
     * Saves all the modifications for this user to the database.
     *
     * @return true if the user could be saved, or
     *         false otherwise
     */
    public boolean save() {
        User    currentUser;
        boolean created = false;

        if (user == null) {
            if (login == null || login.equals("")) {
                LOG.error("no login name given");
                return false;
            }
            user = context.createUser(login);
            if (user == null) {
                LOG.error("couldn't create user with login " + login);
                return false;
            }
            created = true;
        }
        if (password != null) {
            user.setPassword(password);
        }
        if (realName != null) {
            user.setRealName(realName);
        }
        if (email != null) {
            user.setEmail(email);
        }
        try {
            currentUser = context.findUser("").user;
            if (currentUser == null && (created || emailVerified)) {
                currentUser = user;
            }
            user.save(currentUser);
            login = null;
            password = null;
            realName = null;
            email = null;
        } catch (ContentException e) {
            LOG.error(e.getMessage());
            if (created) {
                user = null;
            }
            return false;
        } catch (ContentSecurityException e) {
            LOG.error(e.getMessage());
            if (created) {
                user = null;
            }
            return false;
        }
        return true;
    }

    /**
     * Sends an email verification request to the user. The request
     * email will only be sent if the user exists, has an email
     * address set and has not been modified. A verfication key will
     * be inserted into the email subject and text where a specified
     * replacement text matches. This allows for customizing the
     * verification email while only sending the secret validation
     * key in the email.
     *
     * @param subject        the mail subject
     * @param text           the mail text
     * @param replaceText    the key replacement text
     *
     * @return true if the email verification was sent, or
     *         false otherwise
     *
     * @see #verifyEmail(String)
     */
    public boolean sendEmailVerification(String subject,
                                         String text,
                                         String replaceText) {

        RequestSession session;
        String         key;

        if (user == null || email != null) {
            return false;
        }
        key = User.generatePassword();
        session = context.getRequest().getSession();
        session.setAttribute(VERIFY_USER_ATTRIBUTE, user.getName());
        session.setAttribute(VERIFY_KEY_ATTRIBUTE, key);
        subject = replace(subject, replaceText, key);
        text = replace(text, replaceText, key);
        return context.sendMail(user.getEmail(), subject, text);
    }

    /**
     * Verifies the user email address and unlocks this user. The
     * verification is done by comparing a previously sent key with
     * the specified one. If the two keys match, the user email is
     * considered verified and this user object is unlocked for save
     * operations by an anonymous user.
     *
     * @param key            the verification key
     *
     * @return true if the verification was correct, or
     *         false otherwise
     */
    public boolean verifyEmail(String key) {
        RequestSession session;
        String         verificationUser;
        String         verificationKey;

        session = context.getRequest().getSession();
        verificationUser = (String) session.getAttribute(VERIFY_USER_ATTRIBUTE);
        verificationKey = (String) session.getAttribute(VERIFY_KEY_ATTRIBUTE);
        emailVerified = user != null &&
                        verificationUser != null &&
                        verificationKey != null &&
                        verificationUser.equals(user.getName()) &&
                        verificationKey.equals(key);
        return emailVerified;
    }

    /**
     * Replaces all occurrencies of a text within a string.
     *
     * @param str            the string to process
     * @param from           the text to replace
     * @param to             the new text to use
     *
     * @return the processed string
     */
    private String replace(String str, String from, String to) {
        int pos;

        do {
            pos = str.indexOf(from);
            if (pos >= 0) {
                str = str.substring(0, pos) + to +
                      str.substring(pos + from.length());
            }
        } while (pos >= 0);
        return str;
    }
}
