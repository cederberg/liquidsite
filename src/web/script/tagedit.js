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
    var  tag;
    var  sel;

    if (select.selectedIndex > 0) {
        tag = select.value;
        tagEditInternalAdjustSelection(editor, true);
        sel = area.value.substring(area.selectionStart, area.selectionEnd);
        if (sel.indexOf("<") == 0) {
            sel = sel.substring(0, sel.indexOf(">") + 1);
        } else {
            sel = null;
        }
        if (tagEditInternalIsBlockTag(sel)) {
            tagEditInternalRemove(editor, sel, "</" + sel.substring(1));
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
    var  pos;

    tagEditInternalAdjustSelection(editor, false);
    pos = area.selectionStart;
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
    var  sel;
    var  selStart;
    var  selEnd;
    var  startPos = 0;
    var  endPos;

    selStart = area.selectionStart;
    selEnd = area.selectionEnd;
    sel = area.value.substring(selStart, selEnd);
    while (startPos >= 0) {
        startPos = sel.indexOf("<");
        if (startPos >= 0) {
            endPos = sel.indexOf(">");
            if (endPos > startPos) {
                sel = sel.substring(0, startPos) + sel.substring(endPos + 1);
            } else {
                sel = sel.substring(0, startPos);
            }
        }
    }
    area.value = area.value.substring(0, selStart) + sel + 
                 area.value.substring(selEnd);
    area.selectionStart = selStart;
    area.selectionEnd = selStart + sel.length;
    tagEditInternalStoreUndo(editor);
}

/**
 * Handles an add link event.
 *
 * @param editor             the editor number
 */
function tagEditInternalAddLink(editor) {
    var  url;

    url = prompt("Enter the link (URL) for the selection:", "");
    if (url != null) {
        tagEditInternalAdjustSelection(editor, false);
        tagEditInternalInsert(editor, "<link=" + url + ">", "</link>");
        tagEditInternalStoreUndo(editor);
    }
}

/**
 * Handles an add image event.
 *
 * @param editor             the editor number
 */
function tagEditInternalAddImage(editor) {
    var  url;

    url = prompt("Enter image location (URL):", "");
    if (url != null) {
        tagEditInternalInsert(editor, "<image=" + url + ">", null);
        tagEditInternalStoreUndo(editor);
    }
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
    var  pos = area.selectionStart;
    var  value = area.value;

    while (pos < area.selectionEnd) {
        if (value.charAt(pos) == '\n' || value.charAt(pos) == '\r') {
            area.selectionEnd = pos;
        }
        pos++;
    }
    if (paragraph) {
        pos = area.selectionStart;
        while (pos > 0) {
            if (value.charAt(pos) == '\n' || value.charAt(pos) == '\r') {
                pos++;
                break;
            }
            pos--;
        }
        area.selectionStart = pos;
        pos = area.selectionEnd;
        while (pos < value.length) {
            if (value.charAt(pos) == '\n' || value.charAt(pos) == '\r') {
                break;
            }
            pos++;
        }
        area.selectionEnd = pos;
    }
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
    var  startPos = area.selectionStart;
    var  endPos = area.selectionEnd;
    var  value = area.value;

    if (end == null) {
        area.value = value.substring(0, startPos) + start + 
                     value.substring(startPos);
        area.selectionStart = startPos;
        area.selectionEnd = startPos + start.length;
    } else {
        area.value = value.substring(0, startPos) + start + 
                     value.substring(startPos, endPos) + end + 
                     value.substring(endPos);
        area.selectionStart = startPos;
        area.selectionEnd = endPos + start.length + end.length;
    }
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
    var  startPos = area.selectionStart;
    var  endPos = area.selectionEnd;
    var  value = area.value;
    var  sel = value.substring(startPos, endPos);

    if (sel.length >= start.length && sel.indexOf(start) == 0) {
        sel = sel.substring(start.length);
    }
    if (sel.length >= end.length
     && sel.lastIndexOf(end) == sel.length - end.length) {

        sel = sel.substring(0, sel.length - end.length);
    }
    area.value = value.substring(0, startPos) + sel + 
                 value.substring(endPos);
    area.selectionStart = startPos;
    area.selectionEnd = startPos + sel.length;
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
