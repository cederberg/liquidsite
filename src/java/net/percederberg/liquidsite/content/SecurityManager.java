/*
 * SecurityManager.java
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

package net.percederberg.liquidsite.content;

/**
 * A content security manager. This class will be called to check all
 * protected operations on domain and content objects. Other database
 * objects may use the security manager to check the permissions on
 * related domain or content objects.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
class SecurityManager {

    /**
     * The read access level.
     */
    private static final int READ = 1;

    /**
     * The write access level.
     */
    private static final int WRITE = 2;

    /**
     * The publish access level.
     */
    private static final int PUBLISH = 3;

    /**
     * The admin access level.
     */
    private static final int ADMIN = 4;

    /**
     * The one and only security manager instance.
     */
    private static final SecurityManager INSTANCE = new SecurityManager();

    /**
     * The security manager instance.
     *
     * @return the security manager instance
     */
    public static SecurityManager getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a new content security manager.
     */
    private SecurityManager() {
    }

    /**
     * Checks the read access for a user on a persistent object.
     *
     * @param user           the user to check, or null for none
     * @param obj            the persistent object
     *
     * @return true if the user has read access, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasReadAccess(User user, PersistentObject obj)
        throws ContentException {

        return hasAccess(user, obj, READ);
    }

    /**
     * Checks the write access for a user on a persistent object.
     *
     * @param user           the user to check, or null for none
     * @param obj            the persistent object
     *
     * @return true if the user has write access, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasWriteAccess(User user, PersistentObject obj)
        throws ContentException {

        return hasAccess(user, obj, WRITE);
    }

    /**
     * Checks the publish access for a user on a persistent object.
     * Note that false if always returned for the object where
     * publish access isn't applicable.
     *
     * @param user           the user to check, or null for none
     * @param obj            the persistent object
     *
     * @return true if the user has publish access, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasPublishAccess(User user, PersistentObject obj)
        throws ContentException {

        return hasAccess(user, obj, PUBLISH);
    }

    /**
     * Checks the admin access for a user on a persistent object.
     * Note that false if always returned for the object where
     * admin access isn't applicable.
     *
     * @param user           the user to check, or null for none
     * @param obj            the persistent object
     *
     * @return true if the user has admin access, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasAdminAccess(User user, PersistentObject obj)
        throws ContentException {

        return hasAccess(user, obj, ADMIN);
    }

    /**
     * Checks the access for a user on a persistent object. In the
     * absence of permissions or if a permission isn't applicable,
     * false is returned.
     *
     * @param user           the user to check, or null for none
     * @param obj            the persistent object to check
     * @param access         the access level to check for
     *
     * @return true if the user has the specified access level, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private boolean hasAccess(User user, PersistentObject obj, int access)
        throws ContentException {

        if (obj instanceof Domain) {
            return hasAccess(user, (Domain) obj, access);
        } else if (obj instanceof Host) {
            if (access == READ || access == WRITE) {
                return hasAccess(user, ((Host) obj).getDomain(), access);
            } else {
                return false;
            }
        } else if (obj instanceof Content) {
            return hasAccess(user, (Content) obj, access);
        } else if (obj instanceof Permission) {
            if (((Permission) obj).getContentId() > 0) {
                obj = ((Permission) obj).getContent();
            } else {
                obj = ((Permission) obj).getDomain();
            }
            if (access == READ) {
                return hasAccess(user, obj, READ);
            } else if (access == WRITE) {
                return hasAccess(user, obj, ADMIN);
            } else {
                return false;
            }
        } else if (obj instanceof Lock) {
            if (access == READ || access == WRITE) {
                return hasAccess(user, ((Lock) obj).getContent(), access);
            } else {
                return false;
            }
        } else if (obj instanceof User) {
            return hasAccess(user, (User) obj, access);
        } else if (obj instanceof Group) {
            return hasAccess(user, (Group) obj, access);
        } else {
            return false;
        }
    }

    /**
     * Checks the domain access for a user. In the absence of
     * permissions, false is returned.
     *
     * @param user           the user to check, or null for none
     * @param domain         the domain to check
     * @param access         the access level to check for
     *
     * @return true if the user has the specified access level, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private boolean hasAccess(User user, Domain domain, int access)
        throws ContentException {

        Permission[]  perms = domain.getPermissions();
        Group[]       groups = null;

        // Check for superuser and empty permission list
        if (user != null && user.isSuperUser()) {
            return true;
        } else if (perms.length == 0) {
            return false;
        }

        // Check domain permissions
        if (user != null) {
            groups = user.getGroups();
        }
        for (int i = 0; i < perms.length; i++) {
            if (hasAccess(user, groups, perms[i], access)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks the content access for a user. If no content
     * permissions are set for the content object, the parent
     * permissions will be checked instead. In the absence of a
     * content parent, the domain permissions will be checked.
     *
     * @param user           the user to check, or null for none
     * @param content        the content object to check
     * @param access         the access level to check for
     *
     * @return true if the user has the specified access level, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private boolean hasAccess(User user, Content content, int access)
        throws ContentException {

        Permission[]  perms = content.getPermissions();
        Group[]       groups = null;

        // Check for superuser or inherited permissions
        if (user != null && user.isSuperUser()) {
            return true;
        } else if (perms.length == 0 && content.getParentId() <= 0) {
            return hasAccess(user, content.getDomain(), access);
        } else if (perms.length == 0) {
            return hasAccess(user, content.getParent(), access);
        }

        // Check content permissions
        if (user != null) {
            groups = user.getGroups();
        }
        for (int i = 0; i < perms.length; i++) {
            if (hasAccess(user, groups, perms[i], access)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks the user object access for a user. Any user can read a
     * user object, but only domain administrators and the user
     * itself can write a user object.
     *
     * @param user           the user, or null for none
     * @param obj            the user object to check
     * @param access         the access level to check for
     *
     * @return true if the user has the specified access level, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private boolean hasAccess(User user, User obj, int access)
        throws ContentException {

        if (access == READ) {
            return true;
        } else if (access == WRITE && user != null) {
            return user.isSuperUser()
                || user.equals(obj)
                || (obj.getDomain() != null &&
                    hasAccess(user, obj.getDomain(), ADMIN));
        } else {
            return false;
        }
    }

    /**
     * Checks the group object access for a user. Any user can read a
     * group object, but only domain administrators can write a group
     * object.
     *
     * @param user           the user, or null for none
     * @param obj            the group object to check
     * @param access         the access level to check for
     *
     * @return true if the user has the specified access level, or
     *         false otherwise
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    private boolean hasAccess(User user, Group obj, int access)
        throws ContentException {

        if (access == READ) {
            return true;
        } else if (access == WRITE && user != null) {
            return user.isSuperUser()
                || hasAccess(user, obj.getDomain(), ADMIN);
        } else {
            return false;
        }
    }

    /**
     * Checks the access level for a user on a permission.
     *
     * @param user           the user to check, or null for none
     * @param groups         the user groups, or null for none
     * @param perm           the permission to check
     * @param access         the access level to check for
     *
     * @return true if the user has the specified access level, or
     *         false otherwise
     */
    private boolean hasAccess(User user,
                              Group[] groups,
                              Permission perm,
                              int access) {

        if (!perm.isMatch(user, groups)) {
            return false;
        } else if (access == READ && perm.getRead()) {
            return true;
        } else if (access == WRITE && perm.getWrite()) {
            return true;
        } else if (access == PUBLISH && perm.getPublish()) {
            return true;
        } else if (access == ADMIN && perm.getAdmin()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Verifies that a user has access to insert a persistent object.
     *
     * @param user           the user to check, or null for none
     * @param obj            the persistent object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             specified access
     */
    public void checkInsert(User user, PersistentObject obj)
        throws ContentException, ContentSecurityException {

        if (obj instanceof Domain) {
            checkSuperUserAccess(user, (Domain) obj);
        } else if (obj instanceof Host) {
            checkSuperUserAccess(user, ((Host) obj).getDomain());
        } else if (obj instanceof Content) {
            if (((Content) obj).getRevisionNumber() > 0) {
                checkPublishAccess(user, (Content) obj);
            } else {
                checkWriteAccess(user, (Content) obj);
            }
        } else if (obj instanceof Permission) {
            if (((Permission) obj).getContentId() > 0) {
                checkAdminAccess(user, ((Permission) obj).getContent());
            } else {
                checkAdminAccess(user, ((Permission) obj).getDomain());
            }
        } else if (obj instanceof Lock) {
            checkWriteAccess(user, ((Lock) obj).getContent());
        } else if (obj instanceof User) {
            checkWriteAccess(user, (User) obj);
        } else if (obj instanceof Group) {
            checkWriteAccess(user, (Group) obj);
        } else {
            throw new ContentSecurityException("persistent object " +
                                               "class unknown: " +
                                               obj.getClass());
        }
    }

    /**
     * Verifies that a user has access to update a persistent object.
     *
     * @param user           the user to check, or null for none
     * @param obj            the persistent object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             specified access
     */
    public void checkUpdate(User user, PersistentObject obj)
        throws ContentException, ContentSecurityException {

        if (obj instanceof Domain) {
            checkSuperUserAccess(user, (Domain) obj);
        } else if (obj instanceof Host) {
            checkSuperUserAccess(user, ((Host) obj).getDomain());
        } else if (obj instanceof Content) {
            if (((Content) obj).getRevisionNumber() > 0) {
                checkPublishAccess(user, (Content) obj);
            } else {
                checkWriteAccess(user, (Content) obj);
            }
        } else if (obj instanceof Permission) {
            if (((Permission) obj).getContentId() > 0) {
                checkAdminAccess(user, ((Permission) obj).getContent());
            } else {
                checkAdminAccess(user, ((Permission) obj).getDomain());
            }
        } else if (obj instanceof Lock) {
            throw new ContentSecurityException("content locks cannot " +
                                               "be updated");
        } else if (obj instanceof User) {
            checkWriteAccess(user, (User) obj);
        } else if (obj instanceof Group) {
            checkWriteAccess(user, (Group) obj);
        } else {
            throw new ContentSecurityException("persistent object " +
                                               "class unknown: " +
                                               obj.getClass());
        }
    }

    /**
     * Verifies that a user has access to delete a persistent object.
     *
     * @param user           the user to check, or null for none
     * @param obj            the persistent object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             specified access
     */
    public void checkDelete(User user, PersistentObject obj)
        throws ContentException, ContentSecurityException {

        if (obj instanceof Domain) {
            checkSuperUserAccess(user, (Domain) obj);
        } else if (obj instanceof Host) {
            checkSuperUserAccess(user, ((Host) obj).getDomain());
        } else if (obj instanceof Content) {
            checkPublishAccess(user, (Content) obj);
        } else if (obj instanceof Permission) {
            if (((Permission) obj).getContentId() > 0) {
                checkAdminAccess(user, ((Permission) obj).getContent());
            } else {
                checkAdminAccess(user, ((Permission) obj).getDomain());
            }
        } else if (obj instanceof Lock) {
            checkWriteAccess(user, ((Lock) obj).getContent());
        } else if (obj instanceof User) {
            checkWriteAccess(user, (User) obj);
        } else if (obj instanceof Group) {
            checkWriteAccess(user, (Group) obj);
        } else {
            throw new ContentSecurityException("persistent object " +
                                               "class unknown: " +
                                               obj.getClass());
        }
    }

    /**
     * Verifies that a user has superuser access. The domain being
     * modified must also be specified.
     *
     * @param user           the user to check, or null for none
     * @param domain         the domain object
     *
     * @throws ContentSecurityException if the user didn't have the
     *             specified access
     */
    private void checkSuperUserAccess(User user, Domain domain)
        throws ContentSecurityException {

        if (!user.isSuperUser()) {
            throw new ContentSecurityException(user, "modify", domain);
        }
    }

    /**
     * Verifies that a user has write access on a content object.
     *
     * @param user           the user to check, or null for none
     * @param content        the content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             specified access
     */
    private void checkWriteAccess(User user, Content content)
        throws ContentException, ContentSecurityException {

        if (!hasWriteAccess(user, content)) {
            throw new ContentSecurityException(user, "write", content);
        }
    }

    /**
     * Verifies that a user has write access on a user object.
     *
     * @param user           the user to check, or null for none
     * @param obj            the user object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             specified access
     */
    private void checkWriteAccess(User user, User obj)
        throws ContentException, ContentSecurityException {

        if (!hasWriteAccess(user, obj)) {
            throw new ContentSecurityException(user, "write", obj);
        }
    }

    /**
     * Verifies that a user has write access on a group object.
     *
     * @param user           the user to check, or null for none
     * @param obj            the group object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             specified access
     */
    private void checkWriteAccess(User user, Group obj)
        throws ContentException, ContentSecurityException {

        if (!hasWriteAccess(user, obj)) {
            throw new ContentSecurityException(user, "write", obj);
        }
    }

    /**
     * Verifies that a user has publish access on a content object.
     *
     * @param user           the user to check, or null for none
     * @param content        the content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             specified access
     */
    private void checkPublishAccess(User user, Content content)
        throws ContentException, ContentSecurityException {

        if (!hasPublishAccess(user, content)) {
            throw new ContentSecurityException(user, "publish", content);
        }
    }

    /**
     * Verifies that a user has admin access on a domain object.
     *
     * @param user           the user to check, or null for none
     * @param domain         the domain object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             specified access
     */
    private void checkAdminAccess(User user, Domain domain)
        throws ContentException, ContentSecurityException {

        if (!hasAdminAccess(user, domain)) {
            throw new ContentSecurityException(user, "admin", domain);
        }
    }

    /**
     * Verifies that a user has admin access on a content object.
     *
     * @param user           the user to check, or null for none
     * @param content        the content object
     *
     * @throws ContentException if the database couldn't be accessed
     *             properly
     * @throws ContentSecurityException if the user didn't have the
     *             specified access
     */
    private void checkAdminAccess(User user, Content content)
        throws ContentException, ContentSecurityException {

        if (!hasAdminAccess(user, content)) {
            throw new ContentSecurityException(user, "admin", content);
        }
    }
}
