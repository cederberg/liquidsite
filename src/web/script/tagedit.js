/*
 * tagedit.js
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
 * The tag editor icon location path.
 */
var TAGEDIT_ICON_PATH = "images/icons/24x24/";

/**
 * The tag editor textarea array.
 */
var TAGEDIT_TEXTAREAS = new Array();

/**
 * The tag editor images array.
 */
var TAGEDIT_IMAGES = new Array();

/**
 * The tag editor undo information. Each editor may contain an array
 * of up to 10 stored states. This array contains the undo object for
 * each editor.
 */
var TAGEDIT_UNDO = new Array();

/**
 * Initializes the tag editor. Several tag editors can be used in
 * parallell on a single page.
 *
 * @param id                 the id of the editor (also form field name)
 */
function tagEditInitialize(id) {
    var  toolbar = document.getElementById(id + ".toolbar");
    var  editor = document.getElementById(id + ".editor");
    var  count = TAGEDIT_TEXTAREAS.length;
    var  script;

    tagEditInternalAddToolbar(toolbar, count);
    script = "tagEditInternalStoreUndo(" + count + ");";
    editor.onchange = new Function(script);
    TAGEDIT_TEXTAREAS[count] = editor;
    tagEditInternalStoreUndo(count);
}

/**
 * Adds an image to all the tag editors. The images are shared between
 * all the editors on a single page.
 *
 * @param name               the image file name
 */
function tagEditAddImage(name) {
    TAGEDIT_IMAGES[TAGEDIT_IMAGES.length] = name;
}

/**
 * Adds a tag editor toolbar.
 *
 * @param parent             the parent element
 * @param editor             the editor number
 */
function tagEditInternalAddToolbar(parent, editor) {
    var  table;
    var  tbody;
    var  tr;
    var  td;
    var  input;
    var  img;

    table = utilAddElement(parent, "table");
    table.className = "toolbar";
    tbody = utilAddElement(table, "tbody");
    tr = utilAddElement(tbody, "tr");
    td = utilAddElement(tr, "td");
    td.width = "100%";
    tagEditInternalAddStyleSelector(td, editor);
    utilAddTextElement(td, "\u00A0\u00A0");
    img = tagEditInternalAddButton(td, "Bold", "bold.png");
    img.onclick = new Function("tagEditInternalFormat(" + editor +
                               ", '<b>', '</b>');");
    img = tagEditInternalAddButton(td, "Italic", "italic.png");
    img.onclick = new Function("tagEditInternalFormat(" + editor +
                               ", '<i>', '</i>');");
    img = tagEditInternalAddButton(td, "Unformat", "noformat.png");
    img.onclick = new Function("tagEditInternalUnformat(" + editor + ");");
    utilAddTextElement(td, "\u00A0\u00A0");
    img = tagEditInternalAddButton(td, "Add Link", "link.png");
    img.onclick = new Function("tagEditInternalAddLink(" + editor + ");");
    img = tagEditInternalAddButton(td, "Add Image", "image.png");
    img.onclick = new Function("tagEditInternalAddImage(" + editor + ");");
    utilAddTextElement(td, "\u00A0\u00A0");
    img = tagEditInternalAddButton(td, "Undo", "undo.png");
    img.onclick = new Function("tagEditInternalUndo(" + editor + ");");
    img = tagEditInternalAddButton(td, "Redo", "redo.png");
    img.onclick = new Function("tagEditInternalRedo(" + editor + ");");
}

/**
 * Adds a toolbar style select control.
 *
 * @param parent             the parent element
 * @param editor             the editor number
 */
function tagEditInternalAddStyleSelector(parent, editor) {
    var  select;
    var  option;

    select = utilAddElement(parent, "select");
    select.name = "tagedit.internal." + editor;
    select.onchange = new Function("tagEditInternalStyleSelect(" +
                                   editor + ", this);");
    option = utilAddElement(select, "option", "< Select Style >");
    option.value = "";
    option = utilAddElement(select, "option", "Normal");
    option.value = "";
    option = utilAddElement(select, "option", "Heading 1");
    option.value = "<h1>";
    option = utilAddElement(select, "option", "Heading 2");
    option.value = "<h2>";
    option = utilAddElement(select, "option", "Heading 3");
    option.value = "<h3>";
}

/**
 * Adds a toolbar image button.
 *
 * @param parent             the parent element
 * @param text               the button help text
 * @param image              the button image
 */
function tagEditInternalAddButton(parent, text, image) {
    var  img;

    img = utilAddElement(parent, "img");
    img.className = "button";
    img.src = HTMLEDIT_ICON_PATH + image;
    img.alt = text;
    img.title = text;
    img.onmouseover = tagEditInternalMouseOver;
    img.onmouseout = tagEditInternalMouseOut;
    img.onmousedown = tagEditInternalMouseDown;
    img.onmouseup = tagEditInternalMouseUp;
    return img;
}

/**
 * Handles a style select event.
 *
 * @param editor             the editor number
 * @param select             the select control
 */
function tagEditInternalStyleSelect(editor, select) {
    var  area = TAGEDIT_TEXTAREAS[editor];
    var  selection;
    var  tag;
    var  text;

    if (select.selectedIndex > 0) {
        tag = select.value;
        tagEditInternalAdjustSelection(editor, true);
        selection = tagEditInternalGetSelection(editor);
        text = area.value.substring(selection.start, selection.end);
        if (text.indexOf("<") == 0) {
            text = text.substring(0, text.indexOf(">") + 1);
        } else {
            text = null;
        }
        if (tagEditInternalIsBlockTag(text)) {
            tagEditInternalRemove(editor, text, "</" + text.substring(1));
        }
        if (tag != "") {
            tagEditInternalInsert(editor, tag, "</" + tag.substring(1));
        }
        tagEditInternalStoreUndo(editor);
        select.selectedIndex = 0;
    }
    area.focus();
}

/**
 * Inserts a format tag in an editor.
 *
 * @param editor             the editor number
 * @param start              the starting format tag
 * @param end                the ending format tag
 */
function tagEditInternalFormat(editor, start, end) {
    var  area = TAGEDIT_TEXTAREAS[editor];
    var  selection;
    var  pos;

    tagEditInternalAdjustSelection(editor, false);
    selection = tagEditInternalGetSelection(editor);
    pos = selection.start
    if (area.value.substring(pos, pos + start.length) == start) {
        tagEditInternalRemove(editor, start, end);
    } else {
        tagEditInternalInsert(editor, start, end);
    }
    tagEditInternalStoreUndo(editor);
}

/**
 * Removes all format tags from an editor.
 *
 * @param editor             the editor number
 */
function tagEditInternalUnformat(editor) {
    var  area = TAGEDIT_TEXTAREAS[editor];
    var  selection;
    var  text;
    var  startPos = 0;
    var  endPos;

    selection = tagEditInternalGetSelection(editor);
    text = area.value.substring(selection.start, selection.end);
    while (startPos >= 0) {
        startPos = text.indexOf("<");
        if (startPos >= 0) {
            endPos = text.indexOf(">");
            if (endPos > startPos) {
                text = text.substring(0, startPos) +
                       text.substring(endPos + 1);
            } else {
                text = text.substring(0, startPos);
            }
        }
    }
    area.value = area.value.substring(0, selection.start) + text +
                 area.value.substring(selection.end);
    selection.end = selection.start + text.length;
    tagEditInternalSetSelection(editor, selection);
    tagEditInternalStoreUndo(editor);
}

/**
 * Handles an add link event.
 *
 * @param editor             the editor number
 */
function tagEditInternalAddLink(editor) {
    var  html;
    var  js;

    html = "<tr>\n" +
           "<th width='20%'>URL:</th>\n" +
           "<td width='80%'>\n" +
           "<input name='url' size='40' tabindex='1' />\n" + 
           "<script type='text/javascript'>\n" +
           "document.getElementsByName('url').item(0).focus();\n" +
           "</script>\n" +
           "</td>\n" +
           "</tr>\n" +
           "<tr>\n" +
           "<th>Type:</th>\n" +
           "<td>\n" +
           "<select name='type' tabindex='2'>\n" +
           "<option value=''>Normal</option>\n" +
           "<option value='new'>New Window</option>\n" +
           "<option value='mail'>Mail Address</option>\n" +
           "</select>\n" +
           "</td>\n" +
           "</tr>\n";
    js = "var url = document.getElementsByName('url').item(0).value;\n" +
         "var type = document.getElementsByName('type').item(0).value;\n" +
         "opener.tagEditInternalInsertLink(" + editor + ", url, type);\n" +
         "window.close();\n";
    utilCreateDialog("Insert Link",
                     "Enter link URL and type.",
                     html,
                     js,
                     380,
                     170);
}

/**
 * Inserts a link.
 *
 * @param editor             the editor number
 * @param url                the URL
 * @param type               the link type
 */
function tagEditInternalInsertLink(editor, url, type) {
    var  tag;

    if (type == "new") {
        tag = "<link url=" + url + " window=new>";
    } else if (type == "mail") {
        tag = "<link url=mailto:" + url + ">";
    } else {
        tag = "<link url=" + url + ">";
    }
    if (url != "") {
        tagEditInternalInsert(editor, tag, "</link>");
        tagEditInternalStoreUndo(editor);
    }
}

/**
 * Handles an add image event.
 *
 * @param editor             the editor number
 */
function tagEditInternalAddImage(editor) {
    var  html;
    var  js;

    html = "<tr>\n" +
           "<th width='50%'>Image:</th>\n" +
           "<td width='50%'><select name='url' tabindex='1'>\n";
    for (var i = 0; i < TAGEDIT_IMAGES.length; i++) {
        html += "<option value='" + TAGEDIT_IMAGES[i] + "'>" +
                TAGEDIT_IMAGES[i] + "</option>\n";
    }
    html += "</select>\n" +
            "<script type='text/javascript'>\n" +
            "document.getElementsByName('url').item(0).focus();\n" +
            "</script>\n" +
            "</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "<th>Layout:</th>\n" +
            "<td>\n" +
            "<select name='layout' tabindex='2'>\n" +
            "<option value=''>Normal</option>\n" +
            "<option value='left'>Floating Left</option>\n" +
            "<option value='right'>Floating Right</option>\n" +
            "</select>\n" +
            "</td>\n" +
            "</tr>\n";
    js = "var url = document.getElementsByName('url').item(0).value;\n" +
         "var layout = document.getElementsByName('layout').item(0).value;\n" +
         "opener.tagEditInternalInsertImage(" + editor + ", url, layout);\n" +
         "window.close();\n";
    if (TAGEDIT_IMAGES.length > 0) {
        utilCreateDialog("Insert Image",
                         "Choose image to insert and it's layout.",
                         html,
                         js,
                         320,
                         170);
    } else {
        alert("No images available for insertion.");
    }
}

/**
 * Inserts an image.
 *
 * @param editor             the editor number
 * @param url                the URL
 * @param layout             the layout style
 */
function tagEditInternalInsertImage(editor, url, layout) {
    var  tag;

    if (layout != "") {
        tag = "<image url=" + url + " layout=" + layout + ">";
    } else {
        tag = "<image url=" + url + ">";
    }
    tagEditInternalInsert(editor, tag, null);
    tagEditInternalStoreUndo(editor);
}

/**
 * Adjusts the tag editor current selection. This makes sure that the
 * selection does not exceed various paragraphs. It also enlarges
 * selections to the whole paragraph if the paragraph flag is set.
 *
 * @param editor             the editor number
 * @param paragraph          the paragraph flag
 */
function tagEditInternalAdjustSelection(editor, paragraph) {
    var  area = TAGEDIT_TEXTAREAS[editor];
    var  selection = tagEditInternalGetSelection(editor);
    var  pos = selection.start;
    var  value = area.value;

    while (pos < selection.end) {
        if (value.charAt(pos) == '\n' || value.charAt(pos) == '\r') {
            selection.end = pos;
        }
        pos++;
    }
    if (paragraph) {
        pos = selection.start;
        while (pos > 0) {
            if (value.charAt(pos-1) == '\n' || value.charAt(pos-1) == '\r') {
                break;
            }
            pos--;
        }
        selection.start = pos;
        pos = selection.end;
        while (pos < value.length) {
            if (value.charAt(pos) == '\n' || value.charAt(pos) == '\r') {
                break;
            }
            pos++;
        }
        selection.end = pos;
    }
    tagEditInternalSetSelection(editor, selection);
}

/**
 * Inserts text into an editor.
 *
 * @param editor             the editor number
 * @param start              the starting text
 * @param end                the ending text, or null
 */
function tagEditInternalInsert(editor, start, end) {
    var  area = TAGEDIT_TEXTAREAS[editor];
    var  value = area.value;
    var  selection;

    selection = tagEditInternalGetSelection(editor);
    if (end == null) {
        area.value = value.substring(0, selection.start) + start +
                     value.substring(selection.start);
        selection.end = selection.start + start.length;
    } else {
        area.value = value.substring(0, selection.start) + start +
                     value.substring(selection.start, selection.end) +
                     end + value.substring(selection.end);
        selection.end = selection.end + start.length + end.length;
    }
    tagEditInternalSetSelection(editor, selection);
}

/**
 * Removes text from an editor.
 *
 * @param editor             the editor number
 * @param start              the starting text
 * @param end                the ending text
 */
function tagEditInternalRemove(editor, start, end) {
    var  area = TAGEDIT_TEXTAREAS[editor];
    var  selection = tagEditInternalGetSelection(editor);
    var  value = area.value;
    var  text = value.substring(selection.start, selection.end);

    if (text.length >= start.length && text.indexOf(start) == 0) {
        text = text.substring(start.length);
    }
    if (text.length >= end.length
     && text.lastIndexOf(end) == text.length - end.length) {

        text = text.substring(0, text.length - end.length);
    }
    area.value = value.substring(0, selection.start) + text +
                 value.substring(selection.end);
    selection.end = selection.start + text.length;
    tagEditInternalSetSelection(editor, selection);
}

/**
 * Undo changes in the specified tag editor.
 *
 * @param editor             the editor number
 */
function tagEditInternalUndo(editor) {
    var  area = TAGEDIT_TEXTAREAS[editor];
    var  undo = tagEditInternalGetUndo(editor);

    if (undo.pos > 1) {
        undo.pos--;
        area.value = undo.state[undo.pos - 1];
    }
}

/**
 * Redo previously undone changes in the specified tag editor.
 *
 * @param editor             the editor number
 */
function tagEditInternalRedo(editor) {
    var  area = TAGEDIT_TEXTAREAS[editor];
    var  undo = tagEditInternalGetUndo(editor);

    if (undo.pos < undo.state.length) {
        undo.pos++;
        area.value = undo.state[undo.pos - 1];
    }
}

/**
 * Store undo changes for the specified tag editor.
 *
 * @param editor             the editor number
 */
function tagEditInternalStoreUndo(editor) {
    var  area = TAGEDIT_TEXTAREAS[editor];
    var  undo = tagEditInternalGetUndo(editor);
    var  i;

    undo.state.length = undo.pos;
    if (undo.state.length >= 10) {
        for (i = 0; i < 9; i++) {
            undo.state[i] = undo.state[i + 1];
        }
        undo.state.length = 9;
        undo.pos = 9;
    }
    undo.state[undo.pos] = area.value;
    undo.pos++;
}

/**
 * Returns the undo state object for the specified editor.
 *
 * @param editor             the editor number
 */
function tagEditInternalGetUndo(editor) {
    var  undo;

    for (var i = 0; i <= editor; i++) {
        if (TAGEDIT_UNDO.length <= i) {
            undo = new Object();
            undo.pos = 0;
            undo.state = new Array();
            TAGEDIT_UNDO[i] = undo;
        }
    }
    return TAGEDIT_UNDO[editor];
}

/**
 * Checks if the specified tag is a block tag.
 *
 * @param tag                 the tag to check
 *
 * @return true if the tag is a block tag, or
 *         false otherwise
 */
function tagEditInternalIsBlockTag(tag) {
    return tag == "<p>"
        || tag == "<h1>"
        || tag == "<h2>"
        || tag == "<h3>";
}

/**
 * Returns the current text selection within the tag editor.
 *
 * @param editor             the editor number
 *
 * @return an object containing the selection start and end
 */
function tagEditInternalGetSelection(editor) {
    var  area = TAGEDIT_TEXTAREAS[editor];
    var  selection = new Object();
    var  range;
    var  text;

    if (document.selection) {
        // IE selection handling
        range = document.selection.createRange().duplicate();
        text = range.text;
        range.text = "#~^"  + text;
        range.moveStart("character", 0 - text.length - 3);
        selection.start = area.value.indexOf("#~^");
        if (selection.start >= 0) {
            selection.end = selection.start + text.length;
        } else {
            selection.start = area.value.length;
            selection.end = area.value.length;
        }
        range.text = text;
        range.moveStart("character", 0 - text.length);
    } else {
        // Mozilla selection handling
        selection.start = area.selectionStart;
        selection.end = area.selectionEnd;
    }
    return selection;
}

/**
 * Sets the current text selection within the tag editor.
 *
 * @param editor             the editor number
 * @param selection          the selection object
 */
function tagEditInternalSetSelection(editor, selection) {
    var  area = TAGEDIT_TEXTAREAS[editor];
    var  newlines = 0;

    if (area.createTextRange) {
        // IE selection handling
        for (var i = 0; i < selection.start; i++) {
            if (area.value.charAt(i) == '\r') {
                newlines++;
            }
        }
        range = area.createTextRange();
        range.collapse();
        range.moveEnd("character", selection.end - newlines);
        range.moveStart("character", selection.start - newlines);
        range.select();
    } else {
        // Mozilla selection handling
        area.selectionStart = selection.start;
        area.selectionEnd = selection.end;
    }
}

/**
 * Handles a mouse over event for a button.
 *
 * @param event              the event object
 */
function tagEditInternalMouseOver(event) {
    this.className = "buttonup";
}

/**
 * Handles a mouse out event for a button.
 *
 * @param event              the event object
 */
function tagEditInternalMouseOut(event) {
    this.className = "button";
}

/**
 * Handles a mouse down event for a button.
 *
 * @param event              the event object
 */
function tagEditInternalMouseDown(event) {
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
function tagEditInternalMouseUp(event) {
    this.className = "buttonup";
}
