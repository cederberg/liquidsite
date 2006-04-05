/*
 * permission.js
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

/**
 * The permission table root element. This variable is set by the
 * permissionInitialize() function.
 */
var PERMISSION_ROOT = null;

/**
 * The permission group names available.
 */
var PERMISSION_GROUPS = new Array();

/**
 * The list of iniherited permissions.
 */
var PERMISSION_INHERITED = new Array();

/**
 * The list of current local permissions.
 */
var PERMISSION_LOCAL = new Array();

/**
 * The show local permissions flag.
 */
var PERMISSION_SHOW_LOCAL = false;

/**
 * Initializes the permission table.
 *
 * @param id                  the root element id
 */
function permissionInitialize(id) {
    var  table;

    table = document.getElementById(id);
    PERMISSION_ROOT = utilAddElement(table, "tbody");
}

/**
 * Displays the permission table. This will clear and redraw all
 * permissions.
 */
function permissionDisplay() {
    var  tr;
    var  td;
    var  script;
    var  i;

    utilRemoveChildElements(PERMISSION_ROOT);
    tr = utilAddElement(PERMISSION_ROOT, "tr");
    utilAddElement(tr, "th", "User/Group");
    utilAddElement(tr, "th", "Read");
    utilAddElement(tr, "th", "Write");
    utilAddElement(tr, "th", "Publish");
    utilAddElement(tr, "th", "Admin");
    utilAddElement(tr, "th", "");
    if (PERMISSION_SHOW_LOCAL) {
        for (i = 0; i < PERMISSION_LOCAL.length; i++) {
            permissionInternalDisplayPermission(i, PERMISSION_LOCAL[i], false);
        }
        tr = utilAddElement(PERMISSION_ROOT, "tr");
        td = utilAddElement(tr, "td");
        utilAddElement(td, "strong", "Add New:");
        td = utilAddElement(tr, "td");
        td.colSpan = 5;
        script = "permissionInternalNewLocal();";
        utilAddLinkElement(td, "Add", "add.png", "Add", script);
    } else {
        for (i = 0; i < PERMISSION_INHERITED.length; i++) {
            permissionInternalDisplayPermission(i, PERMISSION_INHERITED[i], true);
        }
    }
}

/**
 * Toggles the local permission editing. By default local permission
 * editing is off.
 */
function permissionToggleEdit() {
    var  perm;

    PERMISSION_SHOW_LOCAL = !PERMISSION_SHOW_LOCAL;
    if (PERMISSION_SHOW_LOCAL && PERMISSION_LOCAL.length == 0) {
        for (var i = 0; i < PERMISSION_INHERITED.length; i++) {
            perm = new Object();
            perm.type = PERMISSION_INHERITED[i].type;
            perm.user = PERMISSION_INHERITED[i].user;
            perm.group = PERMISSION_INHERITED[i].group;
            perm.read = PERMISSION_INHERITED[i].read;
            perm.write = PERMISSION_INHERITED[i].write;
            perm.publish = PERMISSION_INHERITED[i].publish;
            perm.admin = PERMISSION_INHERITED[i].admin;
            PERMISSION_LOCAL[i] = perm;
        }
    }
    permissionDisplay();
}

/**
 * Adds a new group to the permission group alternatives.
 *
 * @param group               the group name
 */
function permissionAddGroup(group) {
    PERMISSION_GROUPS[PERMISSION_GROUPS.length] = group;
}

/**
 * Adds an inherited permission.
 *
 * @param user                the user name, or null
 * @param group               the group name, or null
 * @param read                the read access flag
 * @param write               the write access flag
 * @param publish             the publish access flag
 * @param admin               the admin access flag
 */
function permissionAddInherited(user, group, read, write, publish, admin) {
    var  perm = new Object();

    if (user != null) {
        perm.type = 1;
    } else if (group != null) {
        perm.type = 2;
    } else {
        perm.type = 0;
    }
    perm.user = user;
    perm.group = group;
    perm.read = read;
    perm.write = write;
    perm.publish = publish;
    perm.admin = admin;
    PERMISSION_INHERITED[PERMISSION_INHERITED.length] = perm;
}

/**
 * Adds an inital local permission.
 *
 * @param user                the user name, or null
 * @param group               the group name, or null
 * @param read                the read access flag
 * @param write               the write access flag
 * @param publish             the publish access flag
 * @param admin               the admin access flag
 */
function permissionAddLocal(user, group, read, write, publish, admin) {
    var  perm = new Object();

    if (user != null) {
        perm.type = 1;
    } else if (group != null) {
        perm.type = 2;
    } else {
        perm.type = 0;
    }
    perm.user = user;
    perm.group = group;
    perm.read = read;
    perm.write = write;
    perm.publish = publish;
    perm.admin = admin;
    PERMISSION_LOCAL[PERMISSION_LOCAL.length] = perm;
    PERMISSION_SHOW_LOCAL = true;
}

/**
 * Adds a new default local permission.
 */
function permissionInternalNewLocal() {
    var  perm = new Object();

    perm.type = 0;
    perm.user = null;
    perm.group = null;
    perm.read = false;
    perm.write = false;
    perm.publish = false;
    perm.admin = false;
    PERMISSION_LOCAL[PERMISSION_LOCAL.length] = perm;
    permissionDisplay();
}

/**
 * Removes a local permission.
 *
 * @param index               the index of the permission
 */
function permissionInternalRemoveLocal(index) {
    var  i;

    for (i = index; i + 1 < PERMISSION_LOCAL.length; i++) {
        PERMISSION_LOCAL[i] = PERMISSION_LOCAL[i + 1];
    }
    PERMISSION_LOCAL.length = PERMISSION_LOCAL.length - 1;
    if (PERMISSION_LOCAL.length == 0) {
        permissionInternalNewLocal();
    } else {
        permissionDisplay();
    }
}

/**
 * Displays a permission. This will add a new row to the permission
 * table.
 *
 * @param index              the index of the permission
 * @param name               the permission object
 * @param inherited          the inherited flag
 */
function permissionInternalDisplayPermission(index, perm, inherited) {
    var  tr = utilAddElement(PERMISSION_ROOT, "tr");
    var  td;
    var  input;
    var  select;
    var  option;
    var  script;

    td = utilAddElement(tr, "td");

    input = document.createElement("input");
    input.type = "radio";
    input.tabIndex = 10;
    td.appendChild(input);
    if (perm.type == 0) {
        input.checked = "checked";
    }
    input.name = "perm_" + index + "_type";
    if (inherited) {
        input.disabled = "disabled";
    } else {
        script = "permissionInternalSetType(" + index + ", 0);";
        input.onclick = new Function(script);
    }
    utilAddTextElement(td, " Anonymous");
    utilAddElement(td, "br");

    input = document.createElement("input");
    input.type = "radio";
    input.tabIndex = 10;
    td.appendChild(input);
    if (perm.type == 1) {
        input.checked = "checked";
    }
    input.name = "perm_" + index + "_type";
    if (inherited) {
        input.disabled = "disabled";
    } else {
        script = "permissionInternalSetType(" + index + ", 1);";
        input.onclick = new Function(script);
    }
    if (perm.type != 1) {
        utilAddTextElement(td, " User");
    } else {
        utilAddTextElement(td, " User: ");
        input = document.createElement("input");
        input.size = 15;
        input.name = "perm_" + index + "_user";
        input.value = (perm.user == null) ? "" : perm.user;
        if (inherited) {
            input.disabled = "disabled";
        } else {
            script = "permissionInternalSetUser(" + index + ", this.value);";
            input.onchange = new Function(script);
        }
        td.appendChild(input);
    }
    utilAddElement(td, "br");

    input = document.createElement("input");
    input.type = "radio";
    input.tabIndex = 10;
    td.appendChild(input);
    if (perm.type == 2) {
        input.checked = "checked";
    }
    input.name = "perm_" + index + "_type";
    if (inherited) {
        input.disabled = "disabled";
    } else {
        script = "permissionInternalSetType(" + index + ", 2);";
        input.onclick = new Function(script);
    }
    if (perm.type != 2) {
        utilAddTextElement(td, " Group");
    } else {
        utilAddTextElement(td, " Group: ");
        select = document.createElement("select");
        select.name = "perm_" + index + "_group";
        for (var i = 0; i < PERMISSION_GROUPS.length; i++) {
            option = document.createElement("option");
            utilAddTextElement(option, PERMISSION_GROUPS[i]);
            if (perm.group == PERMISSION_GROUPS[i]) {
                option.selected = "selected";
            }
            select.appendChild(option);
        }
        if (inherited) {
            select.disabled = "disabled";
        } else {
            script = "permissionInternalSetGroup(" + index + ", this.value);";
            select.onchange = new Function(script);
        }
        td.appendChild(select);
    }

    td = utilAddElement(tr, "td");
    permissionInternalAddFlag(td, index, "read", perm.read, inherited);
    td = utilAddElement(tr, "td");
    permissionInternalAddFlag(td, index, "write", perm.write, inherited);
    td = utilAddElement(tr, "td");
    permissionInternalAddFlag(td, index, "publish", perm.publish, inherited);
    td = utilAddElement(tr, "td");
    permissionInternalAddFlag(td, index, "admin", perm.admin, inherited);
    td = utilAddElement(tr, "td");
    if (!inherited) {
        script = "permissionInternalRemoveLocal(" + index + ");";
        utilAddLinkElement(td, "", "delete.png", "Remove", script);
    }
}

/**
 * Adds a permission flag checkbox.
 *
 * @param parent             the parent element
 * @param index              the index of the permission
 * @param name               the permission flag name
 * @param flag               the permission flag initial value
 * @param inherited          the inherited flag
 */
function permissionInternalAddFlag(parent, index, name, flag, inherited) {
    var  input;
    var  script;

    input = document.createElement("input");
    input.type = "checkbox";
    parent.appendChild(input);
    input.tabIndex = 10;
    input.checked = flag;
    input.name = "perm_" + index + "_" + name;
    input.value = "true";
    if (inherited) {
        input.disabled = "disabled";
    } else {
        script = "permissionInternalSetFlag(" + index + ", '" + name +
                                            "', this.checked);";
        input.onclick = new Function(script);
    }
}

/**
 * Sets the type of a local permission.
 *
 * @param index               the index of the permission
 * @param type                the permission type
 */
function permissionInternalSetType(index, type) {
    PERMISSION_LOCAL[index].type = type;
    permissionDisplay();
}

/**
 * Sets the user name of a local permission.
 *
 * @param index               the index of the permission
 * @param user                the user name
 */
function permissionInternalSetUser(index, user) {
    PERMISSION_LOCAL[index].user = user;
}

/**
 * Sets the group name of a local permission.
 *
 * @param index               the index of the permission
 * @param group               the group name
 */
function permissionInternalSetGroup(index, group) {
    PERMISSION_LOCAL[index].group = group;
}

/**
 * Sets the named access flag of a local permission.
 *
 * @param index               the index of the permission
 * @param name                the access flag name
 * @param flag                the access flag value
 */
function permissionInternalSetFlag(index, name, flag) {
    PERMISSION_LOCAL[index][name] = flag;
}
