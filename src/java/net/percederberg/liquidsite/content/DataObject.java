/*
 * DataObject.java
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

package net.percederberg.liquidsite.content;

/**
 * The base data object class.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public abstract class DataObject {

    /**
     * The modified data flag. By default this is set to true, as a
     * non-persistent object must be considered modified.
     */
    private boolean modified = true;

    /**
     * The persistent data flag. By default this is set to false, but
     * will be set to true by the Peer class methods. 
     */
    private boolean persistent = false;

    /**
     * Checks if this object has been modified since read from the
     * database.
     * 
     * @return true if the object has been modified, or
     *         false otherwise
     */
    public boolean isModified() {
        return modified;
    }
    
    /**
     * Sets the modified flag.
     * 
     * @param modified       the new modified flag
     */
    void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * Checks if this object is persistent. I.e. if it corresponds to
     * an object in the database.
     * 
     * @return true if the object is persistent, or
     *         false otherwise
     */
    public boolean isPersistent() {
        return persistent;
    }

    /**
     * Sets the persistent flag.
     * 
     * @param persistent     the new persistent flag
     */
    void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * Validates this data object. This method is called before 
     * writing the data to the database. 
     * 
     * @throws ContentException if the data object contained errors
     */
    public abstract void validate() throws ContentException;

    /**
     * Saves this data object to the database.
     * 
     * @throws ContentException if the database couldn't be accessed 
     *             properly
     */
    public abstract void save() throws ContentException;
}
