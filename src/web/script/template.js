/*
 * template.js
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
 * The template icon location path.
 */
var TEMPLATE_ICON_PATH = "images/icons/24x24/";

/**
 * The page element table element. This variable is set by the
 * templateInitialize() function.
 */
var TEMPLATE_ROOT = null;

/**
 * The inherited page elements object. All inherited elements are
 * present as properties in this object.
 */
var TEMPLATE_INHERITED = new Object();

/**
 * The local page elements object. All local elements are present as
 * properties in this object. This object will be updated while the 
 * user edits the template.
 */
var TEMPLATE_LOCAL = new Object();

/**
 * The list of HTML nodes to be removed. This is used when redrawing 
 * the template view, as IE 5.0 (at least) require a short delay 
 * after removing a form element (textarea or input). If all elements
 * are removed instantly, the IE browser will crash.
 */
var TEMPLATE_REMOVE_LIST = new Array();

/**
 * Initializes the template editor.
 *
 * @param id                  the id of the page element table
 */
function templateInitialize(id) {
    var  table;
    var  tbody;
    var  tr;

    table = document.getElementById(id);
    tbody = templateInternalAddElement(table, "tbody");
    tr = templateInternalAddElement(tbody, "tr");
    templateInternalAddElement(tr, "th", "Name");
    templateInternalAddElement(tr, "th", "Content");
    TEMPLATE_ROOT = tbody;
}

/**
 * Displays the template editor. This will clear and redraw all
 * page elements.
 */ 
function templateDisplay() {
    var  tr;
    var  td;
    var  input;
    var  script;

    for (var i = 1; i < TEMPLATE_ROOT.childNodes.length; i++) {
        tr = TEMPLATE_ROOT.childNodes.item(i);
        templateInternalSetRemovalFlag(tr);
        tr.style.display = "none";
    }
    templateInternalRemoveElements();
    for (var elem in TEMPLATE_LOCAL) {
        templateInternalDisplayLocal(elem);
    }
    for (var elem in TEMPLATE_INHERITED) {
        if (TEMPLATE_LOCAL[elem] == undefined) {
            templateInternalDisplayInherited(elem);
        }
    }
    tr = templateInternalAddElement(TEMPLATE_ROOT, "tr");
    td = templateInternalAddElement(tr, "td");
    templateInternalAddElement(td, "strong", "Add New:");
    td = templateInternalAddElement(tr, "td");
    input = templateInternalAddElement(td, "input");
    input.id = "template.new";
    input.type = "text";
    input.size = "20";
    input.tabIndex = "10";
    input.name = "template.new";
    script = "templateInternalAddLocal('template.new');";
    templateInternalAddAction(td, "Add", "add.png", script);
}

/**
 * Adds an inherited page element. This does NOT redraw the template
 * editor, so the templateDisplay() method MUST be called after 
 * adding all the page elements.
 *
 * @param name               the page element name
 * @param data               the page element data
 */
function templateAddInherited(name, data) {
    TEMPLATE_INHERITED[name] = data;
}

/**
 * Removes all inherited page elements. This does NOT redraw the 
 * template editor, so the templateDisplay() method MUST be called 
 * after finishing with adding the new page elements.
 */
function templateRemoveAllInherited() {
    TEMPLATE_INHERITED = new Object();
}

/**
 * Adds a local page element. This does NOT redraw the template
 * editor, so the templateDisplay() method MUST be called after 
 * adding all the page elements.
 *
 * @param name               the page element name
 * @param data               the page element data
 */
function templateAddLocal(name, data) {
    TEMPLATE_LOCAL[name] = data;
}

/**
 * Removes a local page element. This does NOT redraw the template
 * editor, so the templateDisplay() method MUST be called after 
 * removoving and adding the page elements.
 *
 * @param name               the page element name
 */
function templateRemoveLocal(name) {
    delete TEMPLATE_LOCAL[name];
}

/**
 * Adds a local page element. This function retrieves the element
 * name from a form input field. The element name will also be
 * validated.
 *
 * @param id                 the input field containing the name
 */
function templateInternalAddLocal(id) {
    var  name = document.getElementById(id).value.toLowerCase();
    
    if (name.search(/^[a-z0-9]+$/) < 0) {
        alert("Invalid characters in element name.\n" +
              "Only the characters 'a-z' and '0-9' are accepted.");
    } else if (TEMPLATE_LOCAL[name] != undefined) {
        alert("A page element with that name already exists.");
    } else if (TEMPLATE_INHERITED[name] != undefined) {
        templateAddLocal(name, TEMPLATE_INHERITED[name]);
        templateDisplay();
    } else {
        templateAddLocal(name, "");
        templateDisplay();
    }
}

/**
 * Displays an inherited page element in the template editor. This 
 * will add a new row to the template editor table node.
 *
 * @param name               the inherited element name
 */ 
function templateInternalDisplayInherited(name) {
    var  tr = templateInternalAddElement(TEMPLATE_ROOT, "tr");
    var  td;

    tr.className = "inherited";
    td = templateInternalAddElement(tr, "td");
    templateInternalAddElement(td, "strong", name);
    templateInternalAddElement(td, "br");
    templateInternalAddTextElement(td, "Inherited");
    templateInternalAddElement(td, "br");
    templateInternalAddElement(td, "br");
    script = "templateAddLocal('" + name + "', TEMPLATE_INHERITED." + 
             name + "); templateDisplay();";
    templateInternalAddAction(td, "Edit", "edit.png", script);
    td = templateInternalAddElement(tr, "td");
    templateInternalAddEditor(td, name, TEMPLATE_INHERITED[name], false);
}

/**
 * Displays a local page element in the template editor. This will 
 * add a new row to the template editor table node.
 *
 * @param name               the local element name
 */ 
function templateInternalDisplayLocal(name) {
    var  tr = templateInternalAddElement(TEMPLATE_ROOT, "tr");
    var  td;
    var  a;
    var  script;

    td = templateInternalAddElement(tr, "td");
    templateInternalAddElement(td, "strong", name);
    templateInternalAddElement(td, "br");
    templateInternalAddTextElement(td, "Local");
    templateInternalAddElement(td, "br");
    templateInternalAddElement(td, "br");
    script = "templateRemoveLocal('" + name + "'); templateDisplay();";
    templateInternalAddAction(td, "Delete", "delete.png", script);
    td = templateInternalAddElement(tr, "td");
    templateInternalAddEditor(td, name, TEMPLATE_LOCAL[name], true);
}

/**
 * Creates and adds an action link.
 *
 * @param parent             the parent HTML node
 * @param name               the action name
 * @param image              the action image file name
 * @param script             the script to execute on the action
 *
 * @return the HTML element created
 */
function templateInternalAddAction(parent, name, image, script) {
    var  a = templateInternalAddElement(parent, "a");

    a.href = "#";
    a.title = name;
    a.tabIndex = "10";
    a.onclick = new Function(script + " return false;");
    img = templateInternalAddElement(a, "img");
    img.src = TEMPLATE_ICON_PATH + image;
    img.alt = name;
    templateInternalAddTextElement(a, " " + name);
    return a;
}

/**
 * Creates and adds a page element editor field.
 *
 * @param parent             the parent HTML node
 * @param name               the page element name
 * @param data               the page element data
 * @param local              the local page element flag
 *
 * @return the HTML element created
 */
function templateInternalAddEditor(parent, name, data, local) {
    var  textarea = templateInternalAddElement(parent, "textarea");
    var  rows = 1;
    
    for (var i = 0; i <= data.indexOf("\n", i); ) {
        i = data.indexOf("\n", i) + 1;
        rows++;
    }
    if (!local || rows < 6) {
        textarea.rows = "6";
    } else {
        textarea.rows = rows;
    }
    textarea.cols = "70";
    textarea.value = data;
    if (local) {
        textarea.tabIndex = "10";
        textarea.name = "element." + name;
        textarea.onchange = new Function("TEMPLATE_LOCAL." + name + 
                                         " = this.value");
    } else {
        textarea.name = "dummy." + name;
        textarea.disabled = "disabled";
    }
    return textarea;
}

/**
 * Creates and adds an HTML element to a parent node.
 *
 * @param parent              the parent HTML node
 * @param name                the new element name
 * @param child               the element child (or null)
 *
 * @return the HTML element created
 */
function templateInternalAddElement(parent, name, child) {
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
 * Creates and adds an HTML text element to a parent node.
 *
 * @param parent              the parent HTML node
 * @param text                the text content
 *
 * @return the HTML text element created
 */
function templateInternalAddTextElement(parent, text) {
    var elem = document.createTextNode(text);

    parent.appendChild(elem);
    return elem;
}

/**
 * Flags an HTML element for removal. All subelements will be flagged
 * for removal too.
 *
 * @param elem               the element to remove
 */
function templateInternalSetRemovalFlag(elem) {
    var  child;

    TEMPLATE_REMOVE_LIST[TEMPLATE_REMOVE_LIST.length] = elem;
    for (var i = 0; i < elem.childNodes.length; i++) {
        child = elem.childNodes.item(i);
        if (child.tagName != undefined) {
            templateInternalSetRemovalFlag(child);
        }
    }
}

/**
 * Removes all elements flagged for removal. This function may return
 * prior to all elements being removed, due to processing some 
 * removals after a delay. When removing form elements in IE, a delay
 * must be added to avoid a browser crash.
 */
function templateInternalRemoveElements() {
    var  elem;
    var  tag;
    
    if (TEMPLATE_REMOVE_LIST.length <= 0) {
        return;
    }
    elem = TEMPLATE_REMOVE_LIST[TEMPLATE_REMOVE_LIST.length - 1];
    tag = elem.tagName.toLowerCase();
    elem.parentNode.removeChild(elem);
    TEMPLATE_REMOVE_LIST.length = TEMPLATE_REMOVE_LIST.length - 1;
    if (tag == "textarea" || tag == "input") {
        setTimeout("templateInternalRemoveElements();", 1);
    } else {
        templateInternalRemoveElements();
    }
}
