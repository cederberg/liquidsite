/*
 * object-iframe.js
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
 * Copyright (c) 2003-2005 Per Cederberg. All rights reserved.
 */


/**
 * Shows a new object in the object view. This function will clear the
 * object view before showing the new object.
 *
 * @param type                the object type
 * @param id                  the object id
 * @param name                the object name
 */
function objectShow(type, id, name) {
    window.parent.objectShow(type, id, name);
}

/**
 * Adds the URL property to the object view.
 *
 * @param url                 the url to add
 */
function objectAddUrlProperty(url) {
    window.parent.objectAddUrlProperty(url);
}

/**
 * Adds the online property to the object view.
 *
 * @param online              the object online date (or null)
 * @param offline             the object offline date (or null)
 */
function objectAddOnlineProperty(online, offline) {
    window.parent.objectAddOnlineProperty(online, offline);
}

/**
 * Adds the status property to the object view.
 *
 * @param status              the object status value
 * @param lockUser            the object lock user (or null)
 * @param lockDate            the object lock date (or null)
 */
function objectAddStatusProperty(status, lockUser, lockDate) {
    window.parent.objectAddStatusProperty(status, lockUser, lockDate);
}

/**
 * Adds a property to the object view.
 *
 * @param name                the property label
 * @param value               the property value
 * @param style               the property value CSS class (or null)
 * @param link                the property URL link (or null)
 */
function objectAddProperty(name, value, style, link) {
    window.parent.objectAddProperty(name, value, style, link);
}

/**
 * Adds a new button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddNewButton(url) {
    window.parent.objectAddNewButton(url);
}

/**
 * Adds an edit button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddEditButton(url) {
    window.parent.objectAddEditButton(url);
}

/**
 * Adds a delete button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddDeleteButton(url) {
    window.parent.objectAddDeleteButton(url);
}

/**
 * Adds a publish button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddPublishButton(url) {
    window.parent.objectAddPublishButton(url);
}

/**
 * Adds an unpublish button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddUnpublishButton(url) {
    window.parent.objectAddUnpublishButton(url);
}

/**
 * Adds a revert button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddRevertButton(url) {
    window.parent.objectAddRevertButton(url);
}

/**
 * Adds a permissions button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddPermissionsButton(url) {
    window.parent.objectAddPermissionsButton(url);
}

/**
 * Adds an unlock button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddUnlockButton(url) {
    window.parent.objectAddUnlockButton(url);
}

/**
 * Adds an object revision to the object view.
 *
 * @param revision            the revision number
 * @param date                the revision date
 * @param user                the revision creator user name
 * @param comment             the revision comment
 * @param viewurl             the URL to view the revision (or null)
 */
function objectAddRevision(revision, date, user, comment, viewurl) {
    window.parent.objectAddRevision(revision, date, user, comment, viewurl);
}

/**
 * Adds an object permission to the object view.
 *
 * @param user                the user name (or null)
 * @param group               the group name (or null)
 * @param read                the read permission flag
 * @param write               the write permission flag
 * @param publish             the publish permission flag
 * @param admin               the admin permission flag
 * @param local               the local override flag
 */
function objectAddPermission(user, group, read, write, publish, admin, local) {
    window.parent.objectAddPermission(user,
                                      group,
                                      read,
                                      write,
                                      publish,
                                      admin,
                                      local);
}

/**
 * Adds a domain host name to the object view.
 *
 * @param host                the host name
 * @param comment             the host comment text
 */
function objectAddHost(host, comment) {
    window.parent.objectAddHost(host, comment);
}
