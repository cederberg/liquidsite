/*
 * util.js
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
 * The icon location path.
 */
var UTIL_ICON_PATH = "images/icons/24x24/";

/**
 * The list of HTML nodes to be removed. This is used as IE 5.0 (at
 * least) require a short delay after removing a form element
 * (textarea or input). If all elements are removed instantly, the IE
 * browser will crash. So, elements are first flagged for removal and
 * added to this list, then they are removed in the background. To
 * make this process invisible to the user, a style="display: none;"
 * CSS can be added to the elements to be removed.
 */
var UTIL_REMOVE_LIST = new Array();

/**
 * Opens a new dialog window.
 *
 * @param url                 the URL to visit
 * @param width               the dialog width
 * @param height              the dialog height
 */
function utilOpenDialog(url, width, height) {
    var top = (screen.height - height) / 2;
    var left = (screen.width - width) / 2;
    var attr = "top=" + top + ",left=" + left +
               ",width=" + width + ",height=" + height +
               ",resizable=yes";

    window.open(url, "", attr);
}

/**
 * Focuses a named HTML element. Note that the element must have a
 * name attribute with the specified name value. If several such
 * elements exist, the first will be focused.
 *
 * @param name               the element name
 */
function utilFocusElement(name) {
    var elems = document.getElementsByName(name);

    if (elems != null && elems.length > 0) {
        elems.item(0).focus();
    }
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
function utilAddElement(parent, name, child) {
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
function utilAddTextElement(parent, text) {
    var elem = document.createTextNode(text);

    parent.appendChild(elem);
    return elem;
}

/**
 * Creates and adds a link element.
 *
 * @param parent             the parent HTML node
 * @param text               the link text
 * @param image              the link icon image
 * @param alt                the link alternative/caption text
 * @param script             the script to execute on click
 *
 * @return the HTML element created
 */
function utilAddLinkElement(parent, text, image, alt, script) {
    var  a = utilAddElement(parent, "a");

    a.href = "#";
    a.title = alt;
    a.tabIndex = "10";
    a.onclick = new Function(script + " return false;");
    img = utilAddElement(a, "img");
    img.src = UTIL_ICON_PATH + image;
    img.alt = alt;
    if (text != "") {
        utilAddTextElement(a, " " + text);
    }
    return a;
}

/**
 * Flags an HTML element for removal. All subelements will be flagged
 * for removal too.
 *
 * @param elem               the element to remove
 */
function utilSetRemovalFlag(elem) {
    var  child;

    UTIL_REMOVE_LIST[UTIL_REMOVE_LIST.length] = elem;
    for (var i = 0; i < elem.childNodes.length; i++) {
        child = elem.childNodes.item(i);
        if (child.tagName != undefined) {
            utilSetRemovalFlag(child);
        }
    }
}

/**
 * Removes all elements flagged for removal. This function may return
 * prior to all elements being removed, due to processing some
 * removals after a delay. When removing form elements in IE, a delay
 * must be added to avoid a browser crash.
 */
function utilRemoveElements() {
    var  elem;
    var  tag;

    if (UTIL_REMOVE_LIST.length <= 0) {
        return;
    }
    elem = UTIL_REMOVE_LIST[UTIL_REMOVE_LIST.length - 1];
    tag = elem.tagName.toLowerCase();
    elem.parentNode.removeChild(elem);
    UTIL_REMOVE_LIST.length = UTIL_REMOVE_LIST.length - 1;
    if (tag == "textarea" || tag == "input" || tag == "select") {
        setTimeout("utilRemoveElements();", 1);
    } else {
        utilRemoveElements();
    }
}

/**
 * A key event handler that filters out some <ENTER> keypresses. This
 * will make it impossible to use the <ENTER> key to submit a form in
 * IE. This function has no effect in Mozilla.
 */
function utilDisableEnterSubmitForIE() {
    var  src;
    var  name = "";

    if (window.event) {
        src = window.event.srcElement
        if (src != undefined) {
            name = src.tagName.toLowerCase();
        }
        if (window.event.keyCode == 13 && name == "input") {
            window.event.keyCode = 0;
        }
    }
}

/**
 * A session keep-alive function. This function only needs to be
 * called once upon loading the page to maintain the session open
 * until the user leaves the page. It does this by calling itself
 * every ten minutes.
 */
function utilSessionKeepAlive() {
    var  script

    script = document.createElement('script');
    script.type = "text/javascript";
    script.src = "sessionping.js";
    document.getElementsByTagName("head")[0].appendChild(script);
	setTimeout("utilSessionKeepAlive();", 600000);
}
