/*
 * tree.js
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
 * The tree icon location path.
 */
var TREE_ICON_PATH = "images/icons/48x24/";

/**
 * The tree view root element. This variable is set by the
 * treeInitialize() function.
 */
var TREE_ROOT = null;

/**
 * The tree call-back function used for loading subtrees. This
 * variable is set by the treeInitialize() function.
 */
var TREE_LOAD_FUNCTION = null;

/**
 * The tree call-back function used when selecting items. This
 * variable is set by the treeInitialize() function.
 */
var TREE_SELECT_FUNCTION = null;

/**
 * The currently selected tree view item. This object should be either
 * null, or a paragraph node object.
 */
var treeSelected = null;

/**
 * The last opened tree view item. This object should contain the
 * unique id of the tree item.
 */
var treeLastOpened = 0;

/**
 * The list of tree items to load in the background. This list is
 * cleared when a new subtree is opened and then filled with items
 * when a new subtree is loaded.
 */
var treeAutoLoad = new Array();

/**
 * Initializes the tree view.
 *
 * @param id                  the root element id
 * @param loadFunc            the function for loading subtrees
 * @param selectFunc          the function for displaying items
 */
function treeInitialize(id, loadFunc, selectFunc) {
    TREE_ROOT = document.getElementById(id);
    TREE_LOAD_FUNCTION = loadFunc;
    TREE_SELECT_FUNCTION = selectFunc;
}

/**
 * Adds an item to the tree.
 *
 * @param parent              the parent item id
 * @param id                  the item id
 * @param type                the item type
 * @param name                the item name
 * @param desc                the item description
 * @param status              the item status
 */
function treeAddItem(parent, id, type, name, desc, status) {
    if (document.getElementById("treeitem" + id) == null) {
        var container = document.getElementById("treegroup" + parent);
        var p = document.createElement("p");
        var img = document.createElement("img");
        var a = document.createElement("a");
        var text = document.createTextNode(" " + name);
        var item;

        desc = type.substring(0, 1).toUpperCase() +
               type.substring(1) + ": " + desc;
        img.id = "treeicon" + id;
        img.src = TREE_ICON_PATH + treeInternalGetIcon(type, false);
        img.alt = desc;
        img.onclick = new Function("treeToggle('" + type + "','" + id + "')");
        a.href = "javascript:treeSelect('" + type + "','" + id + "')";
        a.title = desc;
        a.className = treeInternalGetStatusClass(status);
        a.appendChild(text);
        p.id = "treeitem" + id;
        p.appendChild(img);
        p.appendChild(a);
        if (container == null) {
            TREE_ROOT.appendChild(p);
        } else {
            container.appendChild(p);
        }
        if (parent == treeLastOpened && treeInternalIsContainer(type)) {
            item = new Object();
            item.id = id;
            item.type = type;
            treeAutoLoad[treeAutoLoad.length] = item;
        }
    }
}

/**
 * Adds an item container to the tree. This function must be called
 * before adding any child item to a parent item. It it used to
 * verify that the contents of the parent has been loaded.
 *
 * @param id                  the container item id
 */
function treeAddContainer(id) {
    if (document.getElementById("treegroup" + id) == null) {
        var p = document.getElementById("treeitem" + id);
        var parent = p.parentNode;
        var div = document.createElement("div");

        div.id = "treegroup" + id;
        div.style.display = "none";
        if (p.nextSibling != null) {
            parent.insertBefore(div, p.nextSibling);
        } else {
            parent.appendChild(div);
        }
    }
    setTimeout("treeInternalAutoLoad();", 100);
}

/**
 * Selects an item in the tree. If the item has a container it will
 * also be opened.
 *
 * @param type                the item type
 * @param id                  the item id
 */
function treeSelect(type, id) {
    if (treeSelected != null) {
        treeSelected.className = "";
    }
    if (treeInternalIsContainer(type)) {
        treeOpen(type, id);
    }
    treeSelected = document.getElementById("treeitem" + id);
    treeSelected.className = "selected";
    TREE_SELECT_FUNCTION(type, id);
}

/**
 * Toggles showing and hiding contents of a container in the tree. If
 * the specified item is not a container, the item will be selected.
 *
 * @param type                the item type
 * @param id                  the item id
 */
function treeToggle(type, id) {
    var div = document.getElementById("treegroup" + id);

    if (!treeInternalIsContainer(type)) {
        treeSelect(type, id);
    } else if (div == null || div.style.display != "block") {
        treeOpen(type, id);
    } else {
        treeClose(type, id);
    }
}

/**
 * Shows the contents of a container in the tree.
 *
 * @param type                the item type
 * @param id                  the item id
 */
function treeOpen(type, id) {
    var div = document.getElementById("treegroup" + id);
    var img  = document.getElementById("treeicon" + id);

    treeLastOpened = id;
    if (div == null) {
        treeAutoLoad = new Array();
        TREE_LOAD_FUNCTION(type, id, "true");
    } else {
        div.style.display = "block";
        if (img != null) {
            img.src = TREE_ICON_PATH + treeInternalGetIcon(type, true);
        }
    }
}

/**
 * Hides the contents of a container in the tree.
 *
 * @param type                the item type
 * @param id                  the item id
 */
function treeClose(type, id) {
    var div = document.getElementById("treegroup" + id);
    var img  = document.getElementById("treeicon" + id);

    if (div != null) {
        div.style.display = "none";
    }
    if (img != null) {
        img.src = TREE_ICON_PATH + treeInternalGetIcon(type, false);
    }
}

/**
 * Checks if the specified type is a container type.
 *
 * @param type                the item type
 *
 * @return true if the type is a container, or
 *         false otherwise
 */
function treeInternalIsContainer(type) {
    return type == "domain"
        || type == "site"
        || type == "translator"
        || type == "folder"
        || type == "template"
        || type == "section"
        || type == "document"
        || type == "forum"
        || type == "topic";
}

/**
 * Returns the icon image for a specified tree item type.
 *
 * @param type                the tree item type
 * @param open                the open flag
 *
 * @return the icon image file name
 */
function treeInternalGetIcon(type, open) {
    if (treeInternalIsContainer(type)) {
        if (open == true) {
            return type + "_open.png";
        } else {
            return type + "_closed.png";
        }
    } else {
        return type + ".png";
    }
}

/**
 * Returns the status CSS class name.
 *
 * @param status              the status value
 *
 * @return the status class name
 */
function treeInternalGetStatusClass(status) {
    if (status == 1) {
        return "online";
    } else if (status == 2) {
        return "modified";
    } else {
        return "offline";
    }
}

/**
 * Loads one item off the automatic tree loading list. The next item
 * will be loaded after the results from the first loading has been
 * received. This in effect creates a background tree loading thread.
 */
function treeInternalAutoLoad() {
    var  item;

    if (treeAutoLoad.length > 0) {
        item = treeAutoLoad[treeAutoLoad.length - 1];
        treeAutoLoad.length = treeAutoLoad.length - 1;
        TREE_LOAD_FUNCTION(item.type, item.id, "false");
    }
}
