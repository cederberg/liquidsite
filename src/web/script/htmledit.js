/*
 * htmledit.js
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
 * The HTML editor icon location path.
 */
var HTMLEDIT_ICON_PATH = "images/icons/24x24/";

/**
 * The HTML editor frames array.
 */
var HTMLEDIT_FRAMES = new Array();

/**
 * The HTML editor input fields array.
 */
var HTMLEDIT_INPUTS = new Array();

/**
 * Initializes an HTML editor. Several HTML editors can be run in
 * parallell on a single page.
 *
 * @param id                 the id of the editor (also form field name)
 * @param data               the initial editor data
 * @param tabindex           the editor tab index
 */
function htmlEditInitialize(id, data, tabindex) {
    var  toolbar = document.getElementById(id + ".toolbar");
    var  editor = document.getElementById(id + ".editor");
    var  count = HTMLEDIT_FRAMES.length;
    var  iframe;
    var  win;
    var  input;

    htmlEditInternalAddToolbar(toolbar, count);
    input = document.createElement("input");
    input.type = "hidden";
    input.name = id;
    input.value = "";
    toolbar.appendChild(input);
    iframe = htmlEditInternalCreateFrame(editor, count);
    if (iframe.contentWindow) {
        win = iframe.contentWindow;
    } else {
        win = iframe;
    }
    win.document.designMode = "on";
    win.document.write(data);
    win.document.close();
    HTMLEDIT_FRAMES[count] = iframe;
    HTMLEDIT_INPUTS[count] = input;
}

/**
 * Handles the submittal of all the fields. It is required to call
 * this function before submitting the form in order to extract all
 * the HTML code from the inner frames and put it in hidded form
 * fields.
 */
function htmlEditSubmit() {
    var  html;

    for (var i = 0; i < HTMLEDIT_FRAMES.length; i++) {
        html = htmlEditInternalGetFrameWindow(i).document.body.innerHTML;
        HTMLEDIT_INPUTS[i].value = "" + html;
    }
}

/**
 * Adds an HTML editor toolbar.
 *
 * @param parent             the parent element
 * @param editor             the editor number
 */
function htmlEditInternalAddToolbar(parent, editor) {
    var  table;
    var  tbody;
    var  tr;
    var  td;
    var  div;
    var  input;
    var  img;

    table = utilAddElement(parent, "table");
    table.className = "toolbar";
    tbody = utilAddElement(table, "tbody");
    tr = utilAddElement(tbody, "tr");
    td = utilAddElement(tr, "td");
    td.width = "100%";
    div = utilAddElement(td, "div");
    div.id = "htmledit.toolbar." + editor;
    div.style.display = "block";
    htmlEditInternalAddStyleSelector(div, editor);
    utilAddTextElement(div, "\u00A0\u00A0");
    img = htmlEditInternalAddButton(div, "Bold", "bold.png");
    img.onclick = new Function("htmlEditInternalCommandSelect(" + editor +
                               ", 'bold');");
    img = htmlEditInternalAddButton(div, "Italic", "italic.png");
    img.onclick = new Function("htmlEditInternalCommandSelect(" + editor +
                               ", 'italic');");
    utilAddTextElement(div, "\u00A0\u00A0");
    img = htmlEditInternalAddButton(div, "Add Link", "link.png");
    img.onclick = new Function("htmlEditInternalAddLink(" + editor + ");");
    img = htmlEditInternalAddButton(div, "Add Image", "image.png");
    img.onclick = new Function("htmlEditInternalAddImage(" + editor + ");");
    utilAddTextElement(div, "\u00A0\u00A0");
    img = htmlEditInternalAddButton(div, "Undo", "undo.png");
    img.onclick = new Function("htmlEditInternalCommandSelect(" + editor +
                               ", 'undo');");
    img = htmlEditInternalAddButton(div, "Redo", "redo.png");
    img.onclick = new Function("htmlEditInternalCommandSelect(" + editor +
                               ", 'redo');");
    td = utilAddElement(tr, "td");
    img = htmlEditInternalAddButton(td, "View HTML Source", "source.png");
    img.onclick = new Function("htmlEditInternalViewSource(" + editor + ");");
}

/**
 * Adds a toolbar style select control.
 *
 * @param parent             the parent element
 * @param editor             the editor number
 */
function htmlEditInternalAddStyleSelector(parent, editor) {
    var  select;
    var  option;

    select = utilAddElement(parent, "select");
    select.name = "htmledit.internal." + editor;
    select.onchange = new Function("htmlEditInternalStyleSelect(" + 
                                   editor + ", this);");
    option = utilAddElement(select, "option", "< Select Style >");
    option.value = "";
    option = utilAddElement(select, "option", "Normal");
    option.value = "<p>";
    option = utilAddElement(select, "option", "Heading 1");
    option.value = "<h1>";
    option = utilAddElement(select, "option", "Heading 2");
    option.value = "<h2>";
    option = utilAddElement(select, "option", "Heading 3");
    option.value = "<h3>";
    option = utilAddElement(select, "option", "Heading 4");
    option.value = "<h4>";
    option = utilAddElement(select, "option", "Preformatted");
    option.value = "<pre>";
}

/**
 * Adds a toolbar image button.
 *
 * @param parent             the parent element
 * @param text               the button help text
 * @param image              the button image
 */
function htmlEditInternalAddButton(parent, text, image) {
    var  img;

    img = utilAddElement(parent, "img");
    img.className = "button";
    img.src = HTMLEDIT_ICON_PATH + image;
    img.alt = text;
    img.title = text;
    img.onmouseover = htmlEditInternalMouseOver;
    img.onmouseout = htmlEditInternalMouseOut;
    img.onmousedown = htmlEditInternalMouseDown;
    img.onmouseup = htmlEditInternalMouseUp;
    return img;
}

/**
 * Handles a style select event.
 *
 * @param editor             the editor number
 * @param select             the select control
 */
function htmlEditInternalStyleSelect(editor, select) {
    if (select.selectedIndex > 0) {
        htmlEditInternalExecCommand(editor, 'formatblock', select.value);
        select.selectedIndex = 0;
    }
    htmlEditInternalGetFrameWindow(editor).focus();
}

/**
 * Handles a command select event.
 *
 * @param editor             the editor number
 * @param command            the command to execute
 */
function htmlEditInternalCommandSelect(editor, command) {
    htmlEditInternalExecCommand(editor, command, null);
}

/**
 * Handles an add link event.
 *
 * @param editor             the editor number
 */
function htmlEditInternalAddLink(editor) {
    var  url;

    url = prompt("Enter the link (URL) for the selection:", "");
    htmlEditInternalExecCommand(editor, "createlink", url);
}

/**
 * Handles an add image event.
 *
 * @param editor             the editor number
 */
function htmlEditInternalAddImage(editor) {
    var  url;

    url = prompt("Enter image location (URL):", "");
    htmlEditInternalExecCommand(editor, "insertimage", url);
}

/**
 * Handles the view source event.
 *
 * @param editor             the editor number
 */
function htmlEditInternalViewSource(editor) {
    var  doc = htmlEditInternalGetFrameWindow(editor).document;
    var  div = document.getElementById("htmledit.toolbar." + editor);
    var  html;
    var  range;
    
    if (div.style.display == "block") {
        div.style.display = "none";
        html = doc.body.innerHTML;
        doc.body.innerHTML = "";
        doc.body.appendChild(doc.createTextNode(html));
    } else {
        div.style.display = "block";
        if (doc.body.innerText) {
            html = doc.body.innerText;
        } else {
            range = doc.createRange();
            range.selectNodeContents(doc.body);
            html = range.toString();
        }
        doc.body.innerHTML = html;
    }
}

/**
 * Handles a mouse over event for a button.
 *
 * @param event              the event object
 */
function htmlEditInternalMouseOver(event) {
    this.className = "buttonup";
}

/**
 * Handles a mouse out event for a button.
 *
 * @param event              the event object
 */
function htmlEditInternalMouseOut(event) {
    this.className = "button";
}

/**
 * Handles a mouse down event for a button.
 *
 * @param event              the event object
 */
function htmlEditInternalMouseDown(event) {
    this.className = "buttondown";
    if (window.event) {
        // Hack to avoid script error on IE
    } else {
        event.preventDefault();
    }
}

/**
 * Handles a mouse up event for a button.
 *
 * @param event              the event object
 */
function htmlEditInternalMouseUp(event) {
    this.className = "buttonup";
}

/**
 * Executes a command on the specified editor document.
 *
 * @param editor             the editor number
 * @param command            the command
 * @param value              the additional command value
 */
function htmlEditInternalExecCommand(editor, command, value) {
    var  win = htmlEditInternalGetFrameWindow(editor);

    win.document.execCommand(command, false, value);
}

/**
 * Creates an inner frame. This method is needed as IE 5.0 does not
 * support creating frames with the normal DOM createElement()
 * method. Also note that the parent object MUST exit as text in
 * the HTML, or the frame cannot be created. Any previous contents
 * of the parent will be replaced with the frame.
 *
 * @param parent             the parent element
 *
 * @return the frame created
 */
function htmlEditInternalCreateFrame(parent, editor) {
    var  id = "htmledit.frame." + editor;
    var  html;

    html = "<iframe id='" + id + "' width='100%' height='400'>" +
           "Your browser does not support HTML editing." +
           "</iframe>";
    parent.innerHTML = html;
    if (document.getElementById(id).contentDocument) {
    	return document.getElementById(id);
    } else {
        // IE 5.0 hack
        return document.frames(editor);
    }
}

/**
 * Returns the HTML editor frame window. This function is needed to
 * hide differences between older versions of IE and DOM.
 *
 * @param editor             the editor number
 *
 * @return the HTML editor frame window
 */
function htmlEditInternalGetFrameWindow(editor) {
    if (HTMLEDIT_FRAMES[editor].contentWindow) {
        return HTMLEDIT_FRAMES[editor].contentWindow;
    } else {
        return HTMLEDIT_FRAMES[editor];
    }
}
