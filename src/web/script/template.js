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
 * Initializes the template editor.
 *
 * @param id                  the id of the page element table
 */
function templateInitialize(id) {
    var  table;
    var  tbody;
    var  tr;

    table = document.getElementById(id);
    tbody = utilAddElement(table, "tbody");
    tr = utilAddElement(tbody, "tr");
    utilAddElement(tr, "th", "Name");
    utilAddElement(tr, "th", "Content");
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
        utilSetRemovalFlag(tr);
        tr.style.display = "none";
    }
    utilRemoveElements();
    for (var elem in TEMPLATE_LOCAL) {
        templateInternalDisplayLocal(elem);
    }
    for (var elem in TEMPLATE_INHERITED) {
        if (TEMPLATE_LOCAL[elem] == undefined) {
            templateInternalDisplayInherited(elem);
        }
    }
    tr = utilAddElement(TEMPLATE_ROOT, "tr");
    td = utilAddElement(tr, "td");
    utilAddElement(td, "strong", "Add New:");
    td = utilAddElement(tr, "td");
    input = utilAddElement(td, "input");
    input.id = "template.new";
    input.type = "text";
    input.size = "20";
    input.tabIndex = "10";
    input.name = "template.new";
    script = "templateInternalAddLocal('template.new');";
    utilAddLinkElement(td, "Add", "add.png", "Add", script);
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
    var  tr = utilAddElement(TEMPLATE_ROOT, "tr");
    var  td;

    tr.className = "inherited";
    td = utilAddElement(tr, "td");
    utilAddElement(td, "strong", name);
    utilAddElement(td, "br");
    utilAddTextElement(td, "Inherited");
    utilAddElement(td, "br");
    utilAddElement(td, "br");
    script = "templateAddLocal('" + name + "', TEMPLATE_INHERITED." + 
             name + "); templateDisplay();";
    utilAddLinkElement(td, "Edit", "edit.png", "Edit", script);
    td = utilAddElement(tr, "td");
    templateInternalAddEditor(td, name, TEMPLATE_INHERITED[name], false);
}

/**
 * Displays a local page element in the template editor. This will 
 * add a new row to the template editor table node.
 *
 * @param name               the local element name
 */ 
function templateInternalDisplayLocal(name) {
    var  tr = utilAddElement(TEMPLATE_ROOT, "tr");
    var  td;
    var  a;
    var  script;

    td = utilAddElement(tr, "td");
    utilAddElement(td, "strong", name);
    utilAddElement(td, "br");
    utilAddTextElement(td, "Local");
    utilAddElement(td, "br");
    utilAddElement(td, "br");
    script = "templateRemoveLocal('" + name + "'); templateDisplay();";
    utilAddLinkElement(td, "Delete", "delete.png", "Delete", script);
    td = utilAddElement(tr, "td");
    templateInternalAddEditor(td, name, TEMPLATE_LOCAL[name], true);
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
    var  textarea = utilAddElement(parent, "textarea");
    var  rows = 3;
    
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
