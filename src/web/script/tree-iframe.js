/*
 * tree-iframe.js
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
    window.parent.treeAddItem(parent, id, type, name, desc, status);
}

/**
 * Adds an item container to the tree. This function must be called
 * before adding any child item to a parent item. It it used to
 * verify that the contents of the parent has been loaded.
 *
 * @param id                  the container item id
 */
function treeAddContainer(id) {
    window.parent.treeAddContainer(id);
}

/**
 * Shows the contents of a container in the tree.
 *
 * @param type                the item type
 * @param id                  the item id
 */
function treeOpen(type, id) {
    window.parent.treeOpen(type, id);
}
