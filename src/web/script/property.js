/*
 * property.js
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
 * The property icon location path.
 */
var PROPERTY_ICON_PATH = "images/icons/24x24/";

/**
 * The properties table element. This variable is set by the
 * propertiesInitialize() function.
 */
var PROPERTY_ROOT = null;

/**
 * The property values object. All local elements are present as
 * properties in this object. This object will be updated while the 
 * user edits the template.
 */
var PROPERTY_VALUES = new Array();

/**
 * Initializes the property editor.
 *
 * @param id                  the id of the property table
 */
function propertyInitialize(id) {
    var  table;
    var  tbody;
    var  tr;

    table = document.getElementById(id);
    tbody = utilAddElement(table, "tbody");
    tr = utilAddElement(tbody, "tr");
    utilAddElement(tr, "th", "Identifier");
    utilAddElement(tr, "th", "Document Editing Interface");
    PROPERTY_ROOT = tbody;
}

/**
 * Displays the property editor. This will clear and redraw all
 * properties.
 */ 
function propertyDisplay() {
    var  tr;
    var  td;
    var  input;
    var  script;

    for (var i = 1; i < PROPERTY_ROOT.childNodes.length; i++) {
        tr = PROPERTY_ROOT.childNodes.item(i);
        utilSetRemovalFlag(tr);
        tr.style.display = "none";
    }
    utilRemoveElements();
    for (var i = 0; i < PROPERTY_VALUES.length; i++) {
        propertyInternalDisplayProperty(i, PROPERTY_VALUES[i]);
    }
    tr = utilAddElement(PROPERTY_ROOT, "tr");
    td = utilAddElement(tr, "td");
    utilAddElement(td, "strong", "Add New:");
    td = utilAddElement(tr, "td");
    input = utilAddElement(td, "input");
    input.id = "internal.new";
    input.size = "20";
    input.tabIndex = "10";
    input.name = "internal.new";
    script = "propertyInternalAdd(document.getElementById(" +
             "'internal.new').value.toLowerCase());";
    propertyInternalAddAction(td, "Add", "add.png", script);
}

/**
 * Adds a property to the list. Note that this method will not update
 * the property view.
 *
 * @param id                 the property identifier
 * @param name               the property name
 * @param type               the property type number
 * @param description        the property description
 */
function propertyAdd(id, name, type, description) {
    var  property = new Object();

    property.id = id;
    property.name = name;
    property.type = type;
    property.description = description;
    PROPERTY_VALUES[PROPERTY_VALUES.length] = property;
}

/**
 * Adds a property to the list. All property values will be 
 * initialized with empty values.
 *
 * @param id                 the property identifier
 */
function propertyInternalAdd(id) {
    var  property;

    if (id.search(/^[a-z0-9]+$/) < 0) {
        alert("Invalid characters in property identifier.\n" +
              "Only the characters 'a-z' and '0-9' are accepted.");
        return;
    }
    for (var i = 0; i < PROPERTY_VALUES.length; i++) {
        if (PROPERTY_VALUES[i].id == id) {
            alert("A property with that identifier already exists.");
            return;
        }
    }
    property = new Object();
    property.id = id;
    property.name = "";
    property.type = 1;
    property.description = "";
    PROPERTY_VALUES[PROPERTY_VALUES.length] = property;
    propertyDisplay();
}

/**
 * Removes a property from the list.
 *
 * @param position           the property position
 */
function propertyInternalRemove(position) {
    for (var i = position + 1; i < PROPERTY_VALUES.length; i++) {
        PROPERTY_VALUES[i - 1] = PROPERTY_VALUES[i];
    }
    PROPERTY_VALUES.length = PROPERTY_VALUES.length - 1;
    propertyDisplay();
}

/**
 * Moves a property up in the list.
 *
 * @param position           the current property position
 */
function propertyInternalMoveUp(position) {
    var  property = PROPERTY_VALUES[position];

    PROPERTY_VALUES[position] = PROPERTY_VALUES[position - 1];
    PROPERTY_VALUES[position - 1] = property;
    propertyDisplay();
}

/**
 * Moves a property down in the list.
 *
 * @param position           the current property position
 */
function propertyInternalMoveDown(position) {
    propertyInternalMoveUp(position + 1);
}

/**
 * Displays a property in the property editor. This will add a new
 * row to the property editor table node.
 *
 * @param position           the property position
 * @param property           the property object
 */ 
function propertyInternalDisplayProperty(position, property) {
    var  tr = utilAddElement(PROPERTY_ROOT, "tr");
    var  td;
    var  a;
    var  span;
    var  input;
    var  select;
    var  option;
    var  textarea;
    var  script;

    td = utilAddElement(tr, "td");
    utilAddElement(td, "strong", property.id);
    utilAddElement(td, "br");
    utilAddElement(td, "br");
    if (position > 0) {
        script = "propertyInternalMoveUp(" + position + ");";
        propertyInternalAddAction(td, "Up", "up_arrow.png", script);
        utilAddElement(td, "br");
    }
    if (position + 1 < PROPERTY_VALUES.length) {
        script = "propertyInternalMoveDown(" + position + ");";
        propertyInternalAddAction(td, "Down", "down_arrow.png", script);
        utilAddElement(td, "br");
    }
    utilAddElement(td, "br");
    script = "propertyInternalRemove(" + position + ");";
    propertyInternalAddAction(td, "Delete", "delete.png", script);

    td = utilAddElement(tr, "td");
    input = document.createElement("input");
    input.type = "hidden";
    input.name = "property." + property.id + ".position";
    input.value = position;
    td.appendChild(input);
    span = utilAddElement(td, "span", "Name: ");
    span.title = "The property name is the only visible name of the property.";
    input = utilAddElement(td, "input")
    input.tabIndex = "10";
    input.name = "property." + property.id + ".name";
    input.value = property.name;
    input.onchange = new Function("PROPERTY_VALUES[" + position + 
                                  "].name = this.value");
    span = utilAddElement(td, "span", " Type: ");
    span.title = "The property type controls how property data is entered.";
    select = utilAddElement(td, "select")
    select.tabIndex = "10";
    select.name = "property." + property.id + ".type";
    select.onchange = new Function("PROPERTY_VALUES[" + position + 
                                   "].type = this.value");
	option = utilAddElement(select, "option", "Single-line Text");
	option.value = 1;
	option = utilAddElement(select, "option", "Multi-line Text");
	option.value = 2;
	if (property.type == 2) {
	    option.selected = "selected";
	}
/* TODO: uncomment this when HTML is supported
	option = utilAddElement(select, "option", "Formatted Text (HTML)");
	option.value = 3;
	if (property.type == 3) {
	    option.selected = "selected";
	}
*/
    utilAddElement(td, "br");
    utilAddElement(td, "br");
    span = utilAddElement(td, "span", "Description:");
    span.title = "The property description is shown as the user help " +
                 "for the property.";
    utilAddElement(td, "br");
    textarea = utilAddElement(td, "textarea")
    textarea.rows = "4";
    textarea.cols = "60";
    textarea.tabIndex = "10";
    textarea.name = "property." + property.id + ".description";
    textarea.value = property.description;
    textarea.onchange = new Function("PROPERTY_VALUES[" + position + 
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
function propertyInternalAddAction(parent, description, image, script) {
    var  a = utilAddElement(parent, "a");

    a.href = "#";
    a.title = description;
    a.tabIndex = "10";
    a.onclick = new Function(script + " return false;");
    img = utilAddElement(a, "img");
    img.src = PROPERTY_ICON_PATH + image;
    img.alt = description;
    utilAddTextElement(a, " " + description);
    return a;
}
