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
 * Copyright (c) 2003-2005 Per Cederberg. All rights reserved.
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
 * The number of inner frames that have been added. This number is
 * used to assign a unique identifier to each new frame that is
 * added.
 */
var UTIL_IFRAME_COUNT = 0;

/**
 * Opens a new dialog window.
 *
 * @param url                the URL to visit
 * @param width              the dialog width
 * @param height             the dialog height
 *
 * @return the dialog window opened, or
 *         null if no window could be opened
 */
function utilOpenDialog(url, width, height) {
    var top = (screen.height - height) / 2;
    var left = (screen.width - width) / 2;
    var attr = "top=" + top + ",left=" + left +
               ",width=" + width + ",height=" + height +
               ",resizable=yes";

    return window.open(url, "", attr);
}

/**
 * Creates a new dialog window. The contents will be created from a
 * HTML template, inserting some specified code.
 *
 * @param title              the dialog title
 * @param text               the dialog help text
 * @param form               the dialog table HTML
 * @param script             the JavaScript for the OK button
 * @param width              the dialog width
 * @param height             the dialog height
 *
 * @return the dialog window opened, or
 *         null if no window could be opened
 */
function utilCreateDialog(title, text, form, script, width, height) {
    var  win;
    var  html;

    html = "<html>\n" +
           "<head>\n" +
           "<title>" + title + "</title>\n" +
           "<style type='text/css'>\n" +
           "h1     { margin-bottom: 0.5em; padding-left: 5px;\n" +
           "         font-size: 12pt; background: rgb(160,160,160);\n" +
           "         color: white; }\n" +
           "p      { margin-top: 0.5em; margin-bottom: 0.5em; }\n" +
           "table  { font-size: 10pt; }\n" +
           "th, td { text-align: left; vertical-align: top; }\n" +
           "button { margin: 7px; font-weight: bold; }\n" +
           "</style>\n" +
           "<script type='text/javascript'>\n" +
           "function doCancel() {\n" +
           "window.close();\n" +
           "}\n" +
           "function doOk() {\n" +
           script +
           "}\n" +
           "</script>\n" +
           "</head>\n" +
           "<body>\n" +
           "<form method='post' accept-charset='UTF-8'>\n" +
           "<table width='100%'>\n" +
           "<tr>\n" +
           "<td colspan='2'>\n" +
           "<h1>" + title + "</h1>\n" +
           "<p>" + text + "</p>\n" +
           "</td>\n" +
           "</tr>\n" +
           form +
           "<tr>\n" +
           "<td colspan='2' style='text-align: right;'>\n" +
           "<button type='button' tabindex='11' onclick='doCancel();'>\n" +
           "Cancel\n" +
           "</button>\n" +
           "<button type='submit' tabindex='10' " +
           "onclick='doOk(); return false;'>\n" +
           "OK\n" +
           "</button>\n" +
           "</td>\n" +
           "</tr>\n" +
           "</table>\n" +
           "</form>\n" +
           "</body>\n" +
           "</html>\n";
    win = utilOpenDialog("", width, height);
    win.document.write(html);
    win.document.close();
    return win;
}

/**
 * Returns a named HTML element. Note that the element must have a
 * name attribute with the specified name value. If several such
 * elements exist, the first will be returned.
 *
 * @param name               the element name
 *
 * @return the named HTML element, or
 *         null if not found
 */
function utilGetElement(name) {
    var elems = document.getElementsByName(name);

    if (elems != null && elems.length > 0) {
        return elems.item(0);
    } else {
        return null;
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

/**
 * Loads and executes a JavaScript from the specified URL. This
 * function will return *before* the script has been executed, as the
 * script loading and execution is run in the background. Due to the
 * lack of proper DHTML support in some browsers, iframe elements may
 * be used. In that case an empty div element where the frames will
 * be stored must be provided, as well as a "glue" JavaScript that
 * diverts function calls to the "window.parent" evironment.
 *
 * @param url                the JavaScript URL
 * @param iframeParentId     the id of the iframe parent element
 * @param iframeScriptUrl    the JavaScript URL for a glue script
 */
function utilLoadScript(url, iframeParentId, iframeScriptUrl) {
    var parent;
    var script;
    var iframe;

    if (utilInternalIsDynamicallyScriptable()) {
        parent = document.getElementsByTagName("head")[0];
        script = document.createElement('script');
        script.type = "text/javascript";
        script.src = url;
        parent.appendChild(script);
    } else {
        parent = document.getElementById(iframeParentId);
        iframe = document.createElement("iframe");
        iframe.id = "util.iframe." + UTIL_IFRAME_COUNT++;
        iframe.style.border = "0px";
        iframe.style.width = "0px";
        iframe.style.height = "0px";
        parent.appendChild(iframe);
        script = "utilInternalLoadScript('" + iframe.id + "','" +
                 iframeScriptUrl + "','" + url + "')"
        setTimeout(script, 10);
    }
}

/**
 * Checks if the navigtor is dynamically scriptable. This check is
 * performed by controlling the browser user agent string for the
 * Apple WebKit or KHTML, none of which supports the dynamic addition
 * of scripts to a page.
 */
function utilInternalIsDynamicallyScriptable() {
    var agent = navigator.userAgent.toLowerCase();

    return agent.indexOf("applewebkit/") < 0
        && agent.indexOf("khtml") < 0;
}

/**
 * Internal helper function to utilLoadScript(), shouldn't be called
 * directly. This function will change the inner frame document HTML
 * to one that loads the specified JavaScript URL:s.
 *
 * @param id                 the unique frame identifier
 * @param url1               the first JavaScript URL to load
 * @param url1               the second JavaScript URL to load
 */
function utilInternalLoadScript(id, url1, url2) {
    var  win;
    var  html;

    win = document.getElementById(id);
    if (win.contentWindow) {
        win = win.contentWindow;
    }
    html = "<html>\n" +
           "<head>\n" +
           "<script type='text/javascript' src='" + url1 + "'></script>\n" +
           "<script type='text/javascript' src='" + url2 + "'></script>\n" +
           "</head>\n" +
           "<body>\n" +
           "</body>\n" +
           "</html>\n";
    win.document.open("about:blank", "replace");
    win.document.write(html);
    win.document.close();
}
