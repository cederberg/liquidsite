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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
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
 * The HTML editor images array.
 */
var HTMLEDIT_IMAGES = new Array();

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
 * Adds an image to all the HTML editors. The images are shared
 * between all the editors on a single page.
 *
 * @param name               the image file name
 */
function htmlEditAddImage(name) {
    HTMLEDIT_IMAGES[HTMLEDIT_IMAGES.length] = name;
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
        html = htmlEditInternalGetHtml(i);
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
    utilAddTextElement(div, "\u00A0\u00A0");
    img = htmlEditInternalAddButton(div, "Help", "help.png");
    img.onclick = new Function("htmlEditInternalHelp();");
    td = utilAddElement(tr, "td");
    img = htmlEditInternalAddButton(td, "Toggle HTML View", "source.png");
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
    var  text;
    var  html;
    var  js;

    text = "Enter link URL. External URLs must start with " +
           "\"<code>http://</code>\" and site-relative URLs with " +
           "\"<code>/</code>\".";
    html = "<tr>\n" +
           "<th width='20%'>URL:</th>\n" +
           "<td width='80%'>\n" +
           "<input name='url' size='40' tabindex='1' />\n" + 
           "<script type='text/javascript'>\n" +
           "document.getElementsByName('url').item(0).focus();\n" +
           "</script>\n" +
           "</td>\n" +
           "</tr>\n";
    js = "var url = document.getElementsByName('url').item(0).value;\n" +
         "opener.htmlEditInternalInsertLink(" + editor + ", url);\n" +
         "window.close();\n";
    utilCreateDialog("Insert Link", text, html, js, 380, 170);
}

/**
 * Inserts a link.
 *
 * @param editor             the editor number
 * @param url                the URL
 */
function htmlEditInternalInsertLink(editor, url) {
    if (url != "") {
        htmlEditInternalExecCommand(editor, "createlink", url);
    }
}

/**
 * Handles an add image event.
 *
 * @param editor             the editor number
 */
function htmlEditInternalAddImage(editor) {
    var  html;
    var  js;

    html = "<tr>\n" +
           "<th width='50%'>Image:</th>\n" +
           "<td width='50%'><select name='url' tabindex='1'>\n";
    for (var i = 0; i < HTMLEDIT_IMAGES.length; i++) {
        html += "<option value='" + HTMLEDIT_IMAGES[i] + "'>" +
                HTMLEDIT_IMAGES[i] + "</option>\n";
    }
    html += "</select>\n" +
            "<script type='text/javascript'>\n" +
            "document.getElementsByName('url').item(0).focus();\n" +
            "</script>\n" +
            "</td>\n" +
            "</tr>\n";
    js = "var url = document.getElementsByName('url').item(0).value;\n" +
         "opener.htmlEditInternalInsertImage(" + editor + ", url);\n" +
         "window.close();\n";
    if (HTMLEDIT_IMAGES.length > 0) {
        utilCreateDialog("Insert Image",
                         "Choose image to insert.",
                         html,
                         js,
                         300,
                         150);
    } else {
        alert("No images available for insertion.");
    }
}

/**
 * Inserts an image.
 *
 * @param editor             the editor number
 * @param url                the URL
 */
function htmlEditInternalInsertImage(editor, url) {
    if (url != "") {
        htmlEditInternalExecCommand(editor, "insertimage", url);
    }
}

/**
 * Handles a help event.
 */
function htmlEditInternalHelp() {
    var  html;

    html = "<tr>\n" +
           "<td>\n" +
           "<h3>Editing</h3>\n" +
           "<p>Editing in the HTML editor is done just as in a normal text\n" +
           "editor. Use the button toolbar for formatting text is special\n" +
           "ways. If you know HTML and prefer editing the code directly,\n" +
           "you can use the view source button to switch editing mode.</p>\n" +
           "<h3>Links &amp; Images</h3>\n" +
           "<p>Links and images can be inserted in the HTML editor, but\n" +
           "will not work directly from the editor. That is, many links\n" +
           "and images will appear broken in the editor although they are\n" +
           "correct. Please use the document preview function for checking\n" +
           "links and images before publishing.<br/><br/>\n" +
           "URLs in links and images are relative to the document\n" +
           "unless they start with a <code>/</code> character and becomes\n" +
           "relative to the web site. Absolute links starting with\n" +
           "&rdquo;<code>http://</code>&rdquo; are also supported.</p>\n" +
           "</td>\n" +
           "</tr>\n";
    utilCreateDialog("HTML Editor Help",
                     "Help information for the HTML text editor.",
                     html,
                     "window.close();",
                     500,
                     450);
}

/**
 * Handles the view source event.
 *
 * @param editor             the editor number
 */
function htmlEditInternalViewSource(editor) {
    var  doc = htmlEditInternalGetFrameWindow(editor).document;
    var  div = htmlEditInternalGetToolbar(editor);
    var  html;

    html = htmlEditInternalGetHtml(editor);
    if (div.style.display == "block") {
        div.style.display = "none";
        doc.body.innerHTML = "";
        doc.body.appendChild(doc.createTextNode(html));
    } else {
        div.style.display = "block";
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
 * method. Also note that the parent object MUST exist as text in
 * the HTML, or the frame cannot be created. Any previous contents
 * of the parent will be replaced with the frame.
 *
 * @param parent             the parent element
 * @param editor             the editor number
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

/**
 * Returns the HTML editor toolbar.
 *
 * @param editor             the editor number
 *
 * @return the HTML editor toolbar element
 */
function htmlEditInternalGetToolbar(editor) {
    return document.getElementById("htmledit.toolbar." + editor);
}

/**
 * Returns the HTML source code in the editor. This function hides
 * the differences between IE and Mozilla.
 *
 * @param editor             the editor number
 *
 * @return the HTML editor source code
 */
function htmlEditInternalGetHtml(editor) {
    var  doc = htmlEditInternalGetFrameWindow(editor).document;
    var  div = htmlEditInternalGetToolbar(editor);
    var  range;

    if (div.style.display == "block") {
        return doc.body.innerHTML;
    } else if (doc.body.innerText) {
        return doc.body.innerText;
    } else {
        range = doc.createRange();
        range.selectNodeContents(doc.body);
        return range.toString();
    }
}
