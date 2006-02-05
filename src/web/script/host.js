/*
 * host.js
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
 * The host icon location path.
 */
var HOST_ICON_PATH = "images/icons/24x24/";

/**
 * The hosts table body. This variable is set by the hostInitialize()
 * function.
 */
var HOST_ROOT = null;

/**
 * The hosts values array. All host objects are present in this array.
 * This object will be updated while the user edits the hosts.
 */
var HOST_VALUES = new Array();

/**
 * Initializes the host editor.
 *
 * @param id                  the id of the hosts table
 */
function hostInitialize(id) {
    var  table;
    var  tbody;
    var  tr;

    table = document.getElementById(id);
    tbody = utilAddElement(table, "tbody");
    tr = utilAddElement(tbody, "tr");
    utilAddElement(tr, "th", "Host Name");
    utilAddElement(tr, "th", "Description");
    HOST_ROOT = tbody;
}

/**
 * Displays the hosts editor. This will clear and redraw all hosts.
 */
function hostDisplay() {
    var  tr;
    var  td;
    var  input;
    var  script;

    utilRemoveChildElements(HOST_ROOT);
    for (var i = 0; i < HOST_VALUES.length; i++) {
        hostInternalDisplayHost(i, HOST_VALUES[i]);
    }
    tr = utilAddElement(HOST_ROOT, "tr");
    td = utilAddElement(tr, "td");
    utilAddElement(td, "strong", "Add New:");
    td = utilAddElement(tr, "td");
    input = utilAddElement(td, "input");
    input.id = "internal.new";
    input.size = "30";
    input.tabIndex = "10";
    input.name = "internal.new";
    script = "hostInternalAdd(document.getElementById(" +
             "'internal.new').value.toLowerCase());";
    hostInternalAddAction(td, "Add", "add.png", script);
}

/**
 * Adds a host to the list. Note that this method will not update
 * the host view.
 *
 * @param name               the host name
 * @param description        the host description
 */
function hostAdd(name, description) {
    var  host = new Object();

    host.name = name;
    host.description = description;
    HOST_VALUES[HOST_VALUES.length] = host;
}

/**
 * Adds a host to the list. The host description will be initialized
 * with an empty value.
 *
 * @param name               the host name
 */
function hostInternalAdd(name) {
    var  host;

    if (name.search(/^[a-z0-9\._-]+$/) < 0) {
        alert("Invalid characters in host name.\n" +
              "Only lower-case English characters are accepted.");
        return;
    }
    for (var i = 0; i < HOST_VALUES.length; i++) {
        if (HOST_VALUES[i].name == name) {
            alert("A host with that name already exists.");
            return;
        }
    }
    host = new Object();
    host.name = name;
    host.description = "";
    HOST_VALUES[HOST_VALUES.length] = host;
    hostDisplay();
}

/**
 * Removes a host from the list.
 *
 * @param position           the host position
 */
function hostInternalRemove(position) {
    for (var i = position + 1; i < HOST_VALUES.length; i++) {
        HOST_VALUES[i - 1] = HOST_VALUES[i];
    }
    HOST_VALUES.length = HOST_VALUES.length - 1;
    hostDisplay();
}

/**
 * Displays a host in the host editor. This will add a new row to the
 * host editor table node.
 *
 * @param position           the host position
 * @param host               the host object
 */
function hostInternalDisplayHost(position, host) {
    var  tr = utilAddElement(HOST_ROOT, "tr");
    var  td;
    var  a;
    var  span;
    var  input;
    var  select;
    var  option;
    var  textarea;
    var  script;

    td = utilAddElement(tr, "td");
    utilAddElement(td, "strong", host.name);
    utilAddElement(td, "br");
    utilAddElement(td, "br");
    script = "hostInternalRemove(" + position + ");";
    hostInternalAddAction(td, "Delete", "delete.png", script);

    td = utilAddElement(tr, "td");
    input = document.createElement("input");
    input.type = "hidden";
    input.name = "host." + position + ".name";
    input.value = host.name;
    td.appendChild(input);
    input = utilAddElement(td, "input")
    input.size = "40";
    input.tabIndex = "10";
    input.name = "host." + position + ".description";
    input.value = host.description;
    input.onchange = new Function("HOST_VALUES[" + position +
                                  "].description = this.value");
}

/**
 * Creates and adds an action link.
 *
 * @param parent             the parent HTML node
 * @param description        the action description
 * @param image              the action image file name
 * @param script             the script to execute on the action
 *
 * @return the HTML element created
 */
function hostInternalAddAction(parent, description, image, script) {
    var  a = utilAddElement(parent, "a");

    a.href = "#";
    a.title = description;
    a.tabIndex = "10";
    a.onclick = new Function(script + " return false;");
    img = utilAddElement(a, "img");
    img.src = HOST_ICON_PATH + image;
    img.alt = description;
    utilAddTextElement(a, " " + description);
    return a;
}
