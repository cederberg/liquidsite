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
 * Copyright (c) 2004-2009 Per Cederberg. All rights reserved.
 */

/**
 * The tag editor icon location path.
 */
var TAGEDIT_ICON_PATH = "images/icons/24x24/";

/**
 * The tag editor textarea array.
 */
var TAGEDIT_TEXTAREAS = [];

/**
 * The tag editor images lookup map.
 */
var TAGEDIT_IMAGES = {};

/**
 * The tag editor undo information. Each editor may contain an array
 * of up to 10 stored states. This array contains the undo object for
 * each editor.
 */
var TAGEDIT_UNDO = [];

/**
 * Initializes the tag editor. Several tag editors can be used in
 * parallel on a single page.
 *
 * @param {String} id the id of the editor (also form field name)
 */
function tagEditInitialize(id) {
    var toolbar = document.getElementById(id + ".toolbar");
    var editor = document.getElementById(id + ".editor");
    var count = TAGEDIT_TEXTAREAS.length;
    tagEditInternalAddToolbar(toolbar, count);
    var script = "tagEditInternalStoreUndo(" + count + ");";
    editor.onchange = new Function(script);
    TAGEDIT_TEXTAREAS[count] = editor;
    tagEditInternalStoreUndo(count);
}

/**
 * Adds an image to all the tag editors. The images are shared between
 * all the editors on a single page.
 *
 * @param {String} name the image file name
 * @param {String} url the image preview URL
 */
function tagEditAddImage(name, url) {
    TAGEDIT_IMAGES[name] = url;
}

/**
 * Adds a tag editor toolbar.
 *
 * @param {Node} parent the parent DOM node element
 * @param {Number} editor the editor index (zero-based)
 */
function tagEditInternalAddToolbar(parent, editor) {
    var table = utilAddElement(parent, "table");
    table.className = "toolbar";
    var tbody = utilAddElement(table, "tbody");
    var tr = utilAddElement(tbody, "tr");
    var td = utilAddElement(tr, "td");
    td.width = "100%";
    tagEditInternalAddStyleSelector(td, editor);
    utilAddTextElement(td, "\u00A0\u00A0");
    var img = tagEditInternalAddButton(td, "Bold", "bold.png");
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
    utilAddTextElement(td, "\u00A0\u00A0");
    img = tagEditInternalAddButton(td, "Preview", "source.png");
    img.onclick = function() {
        tagEditInternalPreview(editor);
    };
    utilAddTextElement(td, "\u00A0\u00A0");
    img = tagEditInternalAddButton(td, "Help", "help.png");
    img.onclick = new Function("tagEditInternalHelp();");
}

/**
 * Adds a toolbar style select control.
 *
 * @param {Node} parent the parent DOM node element
 * @param {Number} editor the editor index (zero-based)
 */
function tagEditInternalAddStyleSelector(parent, editor) {
    var select = utilAddElement(parent, "select");
    select.name = "tagedit.internal." + editor;
    select.onchange = new Function("tagEditInternalStyleSelect(" +
                                   editor + ", this);");
    var option = utilAddElement(select, "option", "< Select Style >");
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
 * @param {Node} parent the parent DOM node element
 * @param {String} text the button help text
 * @param {String} image the button image URL (minus base URL path)
 */
function tagEditInternalAddButton(parent, text, image) {
    var img = utilAddElement(parent, "img");
    img.className = "button";
    img.src = HTMLEDIT_ICON_PATH + image;
    img.alt = text;
    img.title = text;
    img.onmouseover = function () {
        this.className = "buttonup";
    };
    img.onmouseout = function () {
        this.className = "button";
    };
    img.onmousedown = function (event) {
        this.className = "buttondown";
        if (event != null && event.stopPropagation) {
            event.stopPropagation();
            event.preventDefault();
        } else {
            // Hack to avoid script error on IE
            window.event.cancelBubble = true;
            window.event.returnValue = false;
        }
    };
    img.onmouseup = function () {
        this.className = "buttonup";
    };
    return img;
}

/**
 * Handles a style select event.
 *
 * @param {Number} editor the editor index (zero-based)
 * @param {Node} select the select DOM node
 */
function tagEditInternalStyleSelect(editor, select) {
    var area = TAGEDIT_TEXTAREAS[editor];
    var endTag;
    if (select.selectedIndex > 0) {
        var tag = select.value;
        tagEditInternalAdjustSelection(editor, true);
        var selection = tagEditInternalGetSelection(editor);
        var text = area.value.substring(selection.start, selection.end);
        if (text.indexOf("<") == 0) {
            text = text.substring(0, text.indexOf(">") + 1);
        } else {
            text = null;
        }
        if (tagEditInternalIsBlockTag(text)) {
            endTag = "</" + text.substring(1);
            tagEditInternalRemove(editor, selection, text, endTag);
        }
        if (tag != "") {
            endTag = "</" + tag.substring(1);
            tagEditInternalInsert(editor, selection, tag, endTag);
        }
        tagEditInternalStoreUndo(editor);
        select.selectedIndex = 0;
    }
    area.focus();
}

/**
 * Inserts a format tag in an editor.
 *
 * @param {Number} editor the editor index (zero-based)
 * @param {String} start the starting format tag
 * @param {String} end the ending format tag
 */
function tagEditInternalFormat(editor, start, end) {
    var area = TAGEDIT_TEXTAREAS[editor];
    tagEditInternalAdjustSelection(editor, false);
    var selection = tagEditInternalGetSelection(editor);
    var pos = selection.start
    if (area.value.substring(pos, pos + start.length) == start) {
        tagEditInternalRemove(editor, selection, start, end);
    } else {
        tagEditInternalInsert(editor, selection, start, end);
    }
    tagEditInternalStoreUndo(editor);
}

/**
 * Removes all format tags from an editor.
 *
 * @param {Number} editor the editor index (zero-based)
 */
function tagEditInternalUnformat(editor) {
    var area = TAGEDIT_TEXTAREAS[editor];
    var selection = tagEditInternalGetSelection(editor);
    var text = area.value.substring(selection.start, selection.end);
    var startPos = 0;
    while (startPos >= 0) {
        startPos = text.indexOf("<");
        if (startPos >= 0) {
            var endPos = text.indexOf(">");
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
 * @param {Number} editor the editor index (zero-based)
 */
function tagEditInternalAddLink(editor) {
    var text = "Enter link URL and type. External URLs must start with " +
               "\"<code>http://</code>\" and site-relative URLs with " +
               "\"<code>/</code>\".";
    var html = "<tr>\n" +
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
    var js = "var url = document.getElementsByName('url').item(0).value;\n" +
             "var type = document.getElementsByName('type').item(0).value;\n" +
             "opener.tagEditInternalInsertLink(" + editor + ", url, type);\n" +
             "window.close();\n";
    utilCreateDialog("Insert Link", text, html, js, 380, 190);
}

/**
 * Inserts a link.
 *
 * @param {Number} editor the editor index (zero-based)
 * @param {String} url the link URL
 * @param {String} type the link type
 */
function tagEditInternalInsertLink(editor, url, type) {
    var tag;
    if (type == "new") {
        tag = "<link url=" + url + " window=new>";
    } else if (type == "mail") {
        tag = "<link url=mailto:" + url + ">";
    } else {
        tag = "<link url=" + url + ">";
    }
    if (url != "") {
        var selection = tagEditInternalGetSelection(editor);
        tagEditInternalInsert(editor, selection, tag, "</link>");
        tagEditInternalStoreUndo(editor);
    }
}

/**
 * Handles an add image event.
 *
 * @param {Number} editor the editor index (zero-based)
 */
function tagEditInternalAddImage(editor) {
    var length = 0;
    var html = "<tr>\n" +
               "<th width='50%'>Image:</th>\n" +
               "<td width='50%'><select name='url' tabindex='1'>\n";
    for (var k in TAGEDIT_IMAGES) {
        length++;
        html += "<option value='" + k + "'>" + k + "</option>\n";
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
    var js = "var url = document.getElementsByName('url').item(0).value;\n" +
             "var layout = document.getElementsByName('layout').item(0).value;\n" +
             "opener.tagEditInternalInsertImage(" + editor + ", url, layout);\n" +
             "window.close();\n";
    if (length > 0) {
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
 * @param {Number} editor the editor index (zero-based)
 * @param {String} url the image URL
 * @param {String} layout the layout style
 */
function tagEditInternalInsertImage(editor, url, layout) {
    var tag;
    if (layout != "") {
        tag = "<image url=" + url + " layout=" + layout + ">";
    } else {
        tag = "<image url=" + url + ">";
    }
    var selection = tagEditInternalGetSelection(editor);
    tagEditInternalInsert(editor, selection, tag, null);
    tagEditInternalStoreUndo(editor);
}

/**
 * Displays a preview dialog for the current text.
 *
 * @param {Number} editor the editor index (zero-based)
 */
function tagEditInternalPreview(editor) {
    function parseTagAttrs(str) {
        // BUG: attribute values containing whitespace breaks this split...
        var pairs = str.substring(1, str.length - 2).split(/\s/);
        var res = {};
        for (var i = 1; i < pairs.length; i++) {
            var kv = pairs[i].split("=");
            var key = (kv.length < 1) ? null : kv[0].replace(/\s$/, "");
            var value = (kv.length <= 1) ? "" : kv.slice(1).join("=");
            value = value.replace(/^\s/, "");
            if (/^[\'\"]/.test(value)) {
                value = value.substring(1);
            }
            if (/[\'\"]$/.test(value)) {
                value = value.substring(0, value.length - 1);
            }
            if (key) {
                res[key] = value;
            }
        }
        return res;
    }
    function replacer(str) {
        var tags = { "</link>": "</a>", "</list>": "</ul>", "</box>": "</p>",
                     "<item>": "<li>", "</item>": "</li>" }
        var attrs = parseTagAttrs(str);
        if (/<link/.test(str)) {
            str = "<a href='" + attrs.url + "'";
            if (attrs["window"] === "new") {
                str += " target='_blank'";
            }
            return str + ">";
        } else if (/<image/.test(str)) {
            var url = TAGEDIT_IMAGES[attrs.url] || attrs.url;
            str = "<img src='" + url + "'";
            if (attrs.layout) {
                str += " style='float: " + attrs.layout + ";'";
            }
            return str + " />";
        } else if (/<list/.test(str)) {
            var type = attrs.type || "*";
            var style = { "*": "disc",         "1": "decimal",
                          "i": "lower-roman",  "I": "upper-roman",
                          "a": "lower-alpha",  "A": "upper-alpha" };
            return "<ul style='list-style-type: " + style[type] + ";'>";
        } else if (/<box/.test(str)) {
            var layout = attrs.layout || "right";
            return "<p class='box-layout-" + layout + "'>";
        } else {
            return tags[str] || str;
        }
    }
    var area = TAGEDIT_TEXTAREAS[editor];
    var html = area.value.replace(/\r/g, "").replace(/<[^>]*>/g, replacer);
    var blocks = html.split(/\n\n+/);
    for (var i = 0; i < blocks.length; i++) {
        var b = blocks[i];
        if (/^<h[0-9]/.test(b) || /^<p/.test(b) || /^<ul/.test(b)) {
            // Do nothing for block-level tags
        } else {
            b = "<p>" + b + "</p>";
        }
        blocks[i] = b.replace(/\n/g, "<br/>\n");
    }
    html = blocks.join("\n\n");
    // BUG: handle pre-formatted text properly
    // BUG: handle single <, > and & characters
    utilCreateDialog("Tag Editor Preview",
                     "",
                     "<tr>\n<td>\n" + html + "\n</td>\n</tr>\n",
                     "window.close();",
                     650,
                     600);
}

/**
 * Handles a help event.
 */
function tagEditInternalHelp() {
    var html = "<tr>\n" +
               "<th width='15%'>text</th>\n" +
               "<td width='85%' style='padding-bottom: 1em;'>\n" +
               "Plain text. No special tags are needed for\n" +
               "plain text. Linebreaks are respected so do not use unless\n" +
               "required. A single empty line is used as a paragraph break.\n" +
               "</td>\n" +
               "</tr>\n" +
               "<tr>\n" +
               "<th style='padding-bottom: 1em;'>&lt;h1&gt;<br/>\n" +
               "&lt;h2&gt;<br/>\n" +
               "&lt;h3&gt;</th>\n" +
               "<td style='padding-bottom: 1em;'>\n" +
               "Heading levels 1 through 3. The heading levels control the\n" +
               "size and formatting of the heading. Level 1 is usually\n" +
               "reserved for the document title.\n" +
               "</td>\n" +
               "</tr>\n" +
               "<tr>\n" +
               "<th>&lt;b&gt;</th>\n" +
               "<td style='padding-bottom: 1em;'>\n" +
               "Bold text. This tag is used inside a paragraph to mark text\n" +
               "that should be shown in boldface.\n" +
               "</td>\n" +
               "</tr>\n" +
               "<tr>\n" +
               "<th>&lt;i&gt;</th>\n" +
               "<td style='padding-bottom: 1em;'>\n" +
               "Italic text. This tag is used inside a paragraph to mark text\n" +
               "that should be shown in italics.\n" +
               "</td>\n" +
               "</tr>\n" +
               "<tr>\n" +
               "<th>&lt;link&gt;</th>\n" +
               "<td style='padding-bottom: 1em;'>\n" +
               "Used for linking to web pages or mail addresses. The link tag\n" +
               "has the attributes <strong>url</strong> for the link URL and\n" +
               "<strong>window</strong> for the target browser window. The URL\n" +
               "of the link is relative to the document unless it starts with\n" +
               "a <code>/</code> character and becomes relative to the web\n" +
               "site.<br/><br/>\n" +
               "Examples:<br/>\n" +
               "<code>&lt;link url=attach.pdf&gt;link text&lt;/link&gt;<br/>\n" +
               "&lt;link url=/forums/index.html&gt;link text&lt;/link&gt;<br/>\n" +
               "&lt;link url=http://www.liquidsite.org window=new&gt;link " +
               "text&lt;/link&gt;</code>\n" +
               "</td>\n" +
               "</tr>\n" +
               "<tr>\n" +
               "<th>&lt;image&gt;</th>\n" +
               "<td style='padding-bottom: 1em;'>\n" +
               "Used for inserting images. The image tag has the attributes\n" +
               "<strong>url</strong> for the image URL and\n" +
               "<strong>layout</strong> for the layout style to use. The URL\n" +
               "of the image is relative to the document unless it starts\n" +
               "with a <code>/</code> character and becomes relative to the\n" +
               "web site.<br/><br/>\n" +
               "Examples:<br/>\n" +
               "<code>&lt;image url=image.gif&gt;<br/>\n" +
               "&lt;image url=/images/logo.jpeg layout=right&gt;</code>\n" +
               "</td>\n" +
               "</tr>\n";
    utilCreateDialog("Tag Editor Help",
                     "Quick reference for the tagged text editor.",
                     html,
                     "window.close();",
                     650,
                     550);
}

/**
 * Adjusts the tag editor current selection. This makes sure that the
 * selection does not exceed various paragraphs. It also enlarges
 * selections to the whole paragraph if the paragraph flag is set.
 *
 * @param {Number} editor the editor index (zero-based)
 * @param {Boolean} paragraph the paragraph flag
 */
function tagEditInternalAdjustSelection(editor, paragraph) {
    var area = TAGEDIT_TEXTAREAS[editor];
    var selection = tagEditInternalGetSelection(editor);
    var pos = selection.start;
    var value = area.value;
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
 * @param {Number} editor the editor index (zero-based)
 * @param {Object} selection the editor selection (start and end properties)
 * @param {String} start the starting text
 * @param {String} [end] the ending text, or null
 */
function tagEditInternalInsert(editor, selection, start, end) {
    var area = TAGEDIT_TEXTAREAS[editor];
    var value = area.value;
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
 * @param {Number} editor the editor index (zero-based)
 * @param {Object} selection the editor selection (start and end properties)
 * @param {String} start the starting text
 * @param {String} end the ending text
 */
function tagEditInternalRemove(editor, selection, start, end) {
    var area = TAGEDIT_TEXTAREAS[editor];
    var value = area.value;
    var text = value.substring(selection.start, selection.end);
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
 * @param {Number} editor the editor index (zero-based)
 */
function tagEditInternalUndo(editor) {
    var area = TAGEDIT_TEXTAREAS[editor];
    var undo = tagEditInternalGetUndo(editor);
    if (undo.pos > 1) {
        undo.pos--;
        area.value = undo.state[undo.pos - 1];
    }
}

/**
 * Redo previously undone changes in the specified tag editor.
 *
 * @param {Number} editor the editor index (zero-based)
 */
function tagEditInternalRedo(editor) {
    var area = TAGEDIT_TEXTAREAS[editor];
    var undo = tagEditInternalGetUndo(editor);
    if (undo.pos < undo.state.length) {
        undo.pos++;
        area.value = undo.state[undo.pos - 1];
    }
}

/**
 * Store undo changes for the specified tag editor.
 *
 * @param {Number} editor the editor index (zero-based)
 */
function tagEditInternalStoreUndo(editor) {
    var area = TAGEDIT_TEXTAREAS[editor];
    var undo = tagEditInternalGetUndo(editor);
    undo.state.length = undo.pos;
    if (undo.state.length >= 10) {
        for (var i = 0; i < 9; i++) {
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
 * @param {Number} editor the editor index (zero-based)
 */
function tagEditInternalGetUndo(editor) {
    while (TAGEDIT_UNDO.length <= editor) {
        TAGEDIT_UNDO.push({ pos: 0, state: [] });
    }
    return TAGEDIT_UNDO[editor];
}

/**
 * Checks if the specified tag is a block tag.
 *
 * @param {String} tag the tag to check (including <> markers)
 *
 * @return {Boolean} true if the tag is a block tag, or
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
 * @param {Number} editor the editor index (zero-based)
 *
 * @return {Object} the editor selection (start and end properties)
 */
function tagEditInternalGetSelection(editor) {
    var area = TAGEDIT_TEXTAREAS[editor];
    var selection = { start: 0, end: 0 };
    if (document.selection) {
        // IE selection handling
        area.focus();
        var range = document.selection.createRange().duplicate();
        var text = range.text;
        var length = text.length;
        for (var i = 0; i < text.length; i++) {
            if (text.charAt(i) == '\r') {
                length--;
            }
        }
        range.text = "#~^"  + text;
        range.moveStart("character", 0 - length - 3);
        selection.start = area.value.indexOf("#~^");
        if (selection.start >= 0) {
            selection.end = selection.start + text.length;
        } else {
            selection.start = area.value.length;
            selection.end = area.value.length;
        }
        range.text = text;
        range.moveStart("character", 0 - length);
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
 * @param {Number} editor the editor index (zero-based)
 * @param {Object} selection the editor selection (start and end properties)
 */
function tagEditInternalSetSelection(editor, selection) {
    var area = TAGEDIT_TEXTAREAS[editor];
    var newlines = 0;
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
