/*
 * ContentSecurityManager.java
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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
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
public class ContentSecurityManager {

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
     * Creates a new content security manager.
     */
    protected ContentSecurityManager() {
    }

    /**
     * Checks the read access for a user on a domain object.
     *
     * @param user           the user to check, or null for none
     * @param domain         the domain object
     * 
     * @return true if the user has read access, or
     *         false otherwise
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasReadAccess(User user, Domain domain) 
        throws ContentException {

        return hasAccess(user, domain, READ);
    }
    
    /**
     * Checks the read access for a user on a content object.
     *
     * @param user           the user to check, or null for none
     * @param content        the content object
     * 
     * @return true if the user has read access, or
     *         false otherwise
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasReadAccess(User user, Content content) 
        throws ContentException {

        return hasAccess(user, content, READ);
    }
    
    /**
     * Checks the write access for a user on a domain object.
     *
     * @param user           the user to check, or null for none
     * @param domain         the domain object
     * 
     * @return true if the user has write access, or
     *         false otherwise
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasWriteAccess(User user, Domain domain) 
        throws ContentException {

        return hasAccess(user, domain, WRITE);
    }

    /**
     * Checks the write access for a user on a content object.
     *
     * @param user           the user to check, or null for none
     * @param content        the content object
     * 
     * @return true if the user has write access, or
     *         false otherwise
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasWriteAccess(User user, Content content) 
        throws ContentException {

        return hasAccess(user, content, WRITE);
    }

    /**
     * Checks the publish access for a user on a domain object.
     *
     * @param user           the user to check, or null for none
     * @param domain         the domain object
     * 
     * @return true if the user has publish access, or
     *         false otherwise
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasPublishAccess(User user, Domain domain) 
        throws ContentException {

        return hasAccess(user, domain, PUBLISH);
    }

    /**
     * Checks the publish access for a user on a content object.
     *
     * @param user           the user to check, or null for none
     * @param content        the content object
     * 
     * @return true if the user has publish access, or
     *         false otherwise
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasPublishAccess(User user, Content content) 
        throws ContentException {

        return hasAccess(user, content, PUBLISH);
    }

    /**
     * Checks the admin access for a user on a domain object.
     *
     * @param user           the user to check, or null for none
     * @param domain         the domain object
     * 
     * @return true if the user has admin access, or
     *         false otherwise
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasAdminAccess(User user, Domain domain) 
        throws ContentException {

        return hasAccess(user, domain, ADMIN);
    }

    /**
     * Checks the admin access for a user on a content object.
     *
     * @param user           the user to check, or null for none
     * @param content        the content object
     * 
     * @return true if the user has admin access, or
     *         false otherwise
     * 
     * @throws ContentException if the database couldn't be accessed
     *             properly
     */
    public boolean hasAdminAccess(User user, Content content) 
        throws ContentException {

        return hasAccess(user, content, ADMIN);
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
    protected void checkInsert(User user, PersistentObject obj)
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
            // TODO: check permission for writing users?
        } else if (obj instanceof Group) {
            // TODO: check permission for writing groups?
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
    protected void checkUpdate(User user, PersistentObject obj)
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
            // TODO: check permission for writing users?
        } else if (obj instanceof Group) {
            // TODO: check permission for writing groups?
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
    protected void checkDelete(User user, PersistentObject obj) 
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
            // TODO: check permission for deleting users?
        } else if (obj instanceof Group) {
            // TODO: check permission for deleting groups?
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
