/*
 * object.js
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


/**
 * The small icon location path.
 */
var OBJECT_SMALL_ICON_PATH = "images/icons/24x24/";

/**
 * The large icon location path.
 */
var OBJECT_LARGE_ICON_PATH = "images/icons/48x48/";

/**
 * The object view root element. This variable is set by the
 * objectInitialize() function.
 */
var OBJECT_ROOT = null;

/**
 * Initializes the object view.
 *
 * @param id                  the root element id
 */
function objectInitialize(id) {
    OBJECT_ROOT = document.getElementById(id);
}

/**
 * Clears the object view.
 */ 
function objectClear() {
    while (OBJECT_ROOT.hasChildNodes()) {
        OBJECT_ROOT.removeChild(OBJECT_ROOT.lastChild);
    }
}

/**
 * Shows a new object in the object view. This function will clear the
 * object view before showing the new object.
 *
 * @param type                the object type
 * @param id                  the object id
 * @param name                the object name
 */
function objectShow(type, id, name) {
    var parent;
    var child;
    var desc;

    objectClear();
    desc = type.substring(0, 1).toUpperCase() +
           type.substring(1) + ": " + name;
    child = document.createElement("img");
    child.src = OBJECT_LARGE_ICON_PATH + objectInternalGetIcon(type);
    child.alt = desc;
    parent = document.createElement("h2");
    parent.appendChild(child);
    parent.appendChild(document.createTextNode(" " + desc));
    OBJECT_ROOT.appendChild(parent);
    child = document.createElement("tbody");
    child.id = "objectproperties";
    parent = document.createElement("table");
    parent.className = "compact";
    parent.appendChild(child);
    OBJECT_ROOT.appendChild(parent);
    child = document.createElement("p");
    child.id = "objectbuttons";
    OBJECT_ROOT.appendChild(child);
}

/**
 * Adds the URL property to the object view.
 *
 * @param url                 the url to add
 */
function objectAddUrlProperty(url) {
    if (url == null) {
         url = "N/A";
    }
    objectAddProperty("URL", url);
}

/**
 * Adds the online property to the object view.
 *
 * @param online              the object online date (or null)
 * @param offline             the object offline date (or null)
 */
function objectAddOnlineProperty(online, offline) {
    if (online == null) {
        online = "<no online date>";
    }
    if (offline == null) {
        offline = "<no offline date>";
    }
    // TODO: check if better '--' is possible
    objectAddProperty("Online", online + " -- " + offline);
}

/**
 * Adds the status property to the object view.
 *
 * @param status              the object status value
 * @param lock                the object lock user (or null)
 */
function objectAddStatusProperty(status, lock) {
    var text;
    var style;

    if (status == 1) {
        text = "Online";
        style = "online";
    } else if (status == 2) {
        text = "Modified";
        style = "modified";
    } else {
        text = "Offline";
        style = "offline";
    }   
    if (lock != null) {
        text = text + " (Locked by " + lock + ")";
    }
    objectAddProperty("Status", text, style);
}

/**
 * Adds a property to the object view.
 *
 * @param name                the property label
 * @param value               the property value
 * @param style               the property value CSS class (or null)
 */
function objectAddProperty(name, value, style) {
    var tbody = document.getElementById("objectproperties");
    var tr = document.createElement("tr");
    var td = document.createElement("td");

    objectInternalAddElement(tr, "th", name + ":");
    if (style != null) {
        td.className = style;
    }
    td.appendChild(document.createTextNode(value));
    tr.appendChild(td);
    tbody.appendChild(tr);
}

/**
 * Adds a new button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddNewButton(url) {
    var script = "window.location='" + url + "'";

    objectInternalAddButton("New", "add.png", script);
}

/**
 * Adds an edit button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddEditButton(url) {
    var script = "window.location='" + url + "'";

    objectInternalAddButton("Edit", "edit.png", script);
}

/**
 * Adds a delete button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddDeleteButton(url) {
    var script = "objectInternalOpenDialog('" + url + "',550,310)";

    objectInternalAddButton("Delete", "delete.png", script);
}

/**
 * Adds a publish button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddPublishButton(url) {
    var script = "objectInternalOpenDialog('" + url + "',550,300)";

    objectInternalAddButton("Publish", "online.png", script);
}

/**
 * Adds an unpublish button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddUnpublishButton(url) {
    var script = "objectInternalOpenDialog('" + url + "',550,300)";

    objectInternalAddButton("Unpublish", "offline.png", script);
}

/**
 * Adds an unlock button to the object view.
 *
 * @param url                 the URL to visit
 */
function objectAddUnlockButton(url) {
    var script = "objectInternalOpenDialog('" + url + "',550,300)";

    objectInternalAddButton("Unlock", "lock.png", script);
}

/**
 * Adds an object revision to the object view.
 *
 * @param revision            the revision number
 * @param date                the revision date
 * @param user                the revision creator user name
 * @param comment             the revision comment
 * @param viewurl             the URL to view the revision (or null)
 * @param removeurl           the URL to remove the revision (or null)
 */
function objectAddRevision(revision, date, user, comment, viewurl, removeurl) {
    var tbody = objectInternalGetRevisionTable();
    var tr = document.createElement("tr");
    var a1 = document.createElement("a");
    var a2 = null;
    var img;

    if (viewurl != null) {
        a1.href = viewurl;
    }
    if (revision == 0) {
        a1.className = "modified";
        a1.appendChild(document.createTextNode("Work"));
    } else {
        a1.appendChild(document.createTextNode(revision));
    }
    if (removeurl != null) {
        a2 = document.createElement("a");
        a2.href = "javascript:objectInternalOpenDialog('" + removeurl +
                  "',550,300)";
        img = document.createElement("img");
        img.src = OBJECT_SMALL_ICON_PATH + "delete.png";
        img.alt = "Delete Revision";
        a2.appendChild(img);
    }
    objectInternalAddElement(tr, "td", a1);
    objectInternalAddElement(tr, "td", date);
    objectInternalAddElement(tr, "td", user);
    objectInternalAddElement(tr, "td", comment);
    objectInternalAddElement(tr, "td", a2);
    tbody.appendChild(tr);
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
    var tbody = objectInternalGetPermissionTable();
    var tr = document.createElement("tr");
    var td1 = document.createElement("td");
    var td2 = document.createElement("td");
    var img = document.createElement("img");
    var name;
    
    if (user != null) {
        img.src = OBJECT_SMALL_ICON_PATH + "user.png";
        img.alt = "User";
        name = user;
    } else if (group != null) {
        img.src = OBJECT_SMALL_ICON_PATH + "group.png";
        img.alt = "Group";
        name = group;
    } else {
        img.src = OBJECT_SMALL_ICON_PATH + "anonymous.png";
        img.alt = "Anonymous";
        name = "Anonymous";
    }
    td1.appendChild(img);
    td1.appendChild(document.createTextNode(" " + name));
    tr.appendChild(td1);
    objectInternalAddPermission(tr, read, local);
    objectInternalAddPermission(tr, write, local);
    objectInternalAddPermission(tr, publish, local);
    objectInternalAddPermission(tr, admin, local);
    tbody.appendChild(tr);
}

/**
 * Adds a domain host name to the object view.
 *
 * @param host                the host name
 * @param comment             the host comment text
 */
function objectAddHost(host, comment) {
    var tbody = objectInternalGetHostTable();
    var tr = document.createElement("tr");

    objectInternalAddElement(tr, "td", host);
    objectInternalAddElement(tr, "td", comment);
    tbody.appendChild(tr);
}

/**
 * Adds a button to the object view.
 *
 * @param text                the button text
 * @param image               the button image file name
 * @param script              the script to run on click
 */
function objectInternalAddButton(text, image, script) {
    var p = document.getElementById("objectbuttons");
    var button = document.createElement("button");
    var img = document.createElement("img");

    img.src = OBJECT_SMALL_ICON_PATH + image;
    button.onclick = new Function(script);
    button.appendChild(img);
    button.appendChild(document.createTextNode(" " + text));
    p.appendChild(button);
    p.appendChild(document.createTextNode(" "));
}

/**
 * Adds a permission table cell to a row.
 *
 * @param row                 the table row object
 * @param value               the permission flag
 * @param local               the local override flag
 */
function objectInternalAddPermission(row, value, local) {
    var td = document.createElement("td");

    if (!local) {
        td.className = "inherited";
    }
    if (value) {
        td.appendChild(document.createTextNode("X"));
    } else {
        td.appendChild(document.createTextNode("-"));
    }
    row.appendChild(td);
}

/**
 * Adds an HTML element to a parent node.
 *
 * @param parent              the parent HTML element
 * @param name                the new element name
 * @param child               the element child (or null)
 *
 * ®return the HTML element created
 */
function objectInternalAddElement(parent, name, child) {
    var elem = document.createElement(name);

    if (typeof(child) == "string") {
        child = document.createTextNode(child);
    }
    if (child != null) {
        elem.appendChild(child);
    }
    parent.appendChild(elem);
    return elem;
} 

/**
 * Adds an HTML table element to a parent node. An table title will
 * also be added before the table, and an empty paragraph after the
 * table.
 *
 * @param parent              the parent HTML element
 * @param id                  the table body id
 * @param title               the table title
 *
 * @return the HTML table body element created
 */
function objectInternalAddTable(parent, id, title) {
    var table;
    var tbody;

    objectInternalAddElement(parent, "h3", title);
    table = document.createElement("table");
    table.className = "border";
    tbody = document.createElement("tbody");
    tbody.id = id;
    table.appendChild(tbody);
    parent.appendChild(table);
    objectInternalAddElement(parent, "p");
    return tbody;
}

/**
 * Returns the revision table body. If no revision table exists,
 * a new one will be created and added to the object view.
 *
 * @return the revision table body
 */
function objectInternalGetRevisionTable() {    
    var tbody = document.getElementById("objectrevisions");
    var tr;

    if (tbody == null) {
        tbody = objectInternalAddTable(OBJECT_ROOT, 
                                       "objectrevisions", 
                                       "Revision History");
        tr = objectInternalAddElement(tbody, "tr");
        objectInternalAddElement(tr, "th", "Revision");
        objectInternalAddElement(tr, "th", "Date");
        objectInternalAddElement(tr, "th", "User");
        objectInternalAddElement(tr, "th", "Comment");
        objectInternalAddElement(tr, "th", "");
    }
    return tbody;
}

/**
 * Returns the permission table body. If no permission table exists,
 * a new one will be created and added to the object view.
 *
 * @return the permission table body
 */
function objectInternalGetPermissionTable() {
    var table;
    var tbody = document.getElementById("objectpermissions");
    var tr;

    if (tbody == null) {
        tbody = objectInternalAddTable(OBJECT_ROOT, 
                                       "objectpermissions", 
                                       "Permissions");
        tr = objectInternalAddElement(tbody, "tr");
        objectInternalAddElement(tr, "th", "User/Group");
        objectInternalAddElement(tr, "th", "Read");
        objectInternalAddElement(tr, "th", "Write");
        objectInternalAddElement(tr, "th", "Publish");
        objectInternalAddElement(tr, "th", "Admin");
    }
    return tbody;
}

/**
 * Returns the hosts table body. If no hosts table exists, a new one
 * will be created and added to the object view.
 *
 * @return the hosts table body
 */
function objectInternalGetHostTable() {
    var table;
    var tbody = document.getElementById("objecthosts");
    var tr;

    if (tbody == null) {
        tbody = objectInternalAddTable(OBJECT_ROOT, 
                                       "objecthosts", 
                                       "Host Names");
        tr = objectInternalAddElement(tbody, "tr");
        objectInternalAddElement(tr, "th", "Web Host");
        objectInternalAddElement(tr, "th", "Comment");
    }
    return tbody;
}

/**
 * Returns the icon image for a specified object type.
 *
 * @param type                the object type
 *
 * @return the icon image file name
 */
function objectInternalGetIcon(type) {
    if (type == "domain") {
        return "domain.png";
    } else if (type == "site") {
        return "site.png";
    } else if (type == "folder") {
        return "folder.png";
    } else if (type == "page") {
        return "page.png";
    } else if (type == "alias") {
        return "alias.png";
    } else {
        return "file.png";
    }
}

/**
 * Opens a new dialog window.
 *
 * @param url                 the URL to visit
 * @param width               the dialog width
 * @param height              the dialog height
 */
function objectInternalOpenDialog(url, width, height) {
    var top = (screen.height - height) / 2;
    var left = (screen.width - width) / 2;
    var attr = "top=" + top + ",left=" + left + 
               ",width=" + width + ",height=" + height + 
               ",resizable=yes";

    window.open(url, "", attr);
}
