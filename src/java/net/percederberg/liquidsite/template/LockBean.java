/*
 * LockBean.java
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

package net.percederberg.liquidsite.template;

import java.util.Date;

import net.percederberg.liquidsite.content.Lock;

/**
 * A content lock bean.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class LockBean {

    /**
     * The lock being encapsulated.
     */
    private Lock lock;

    /**
     * Creates a new content lock bean.
     *
     * @param lock           the content lock, or null for empty
     */
    LockBean(Lock lock) {
        this.lock = lock;
    }

    /**
     * Returns the lock status.
     *
     * @return true if the lock is open, or
     *         false if the lock has been acquired
     */
    public boolean getOpen() {
        return lock == null;
    }

    /**
     * Returns the lock acquiring date.
     *
     * @return the lock acquiring date, or
     *         the current date and time for an open lock
     */
    public Date getDate() {
        if (lock == null) {
            return new Date();
        } else {
            return lock.getAcquiredDate();
        }
    }

    /**
     * Returns the lock owner login name.
     *
     * @return the lock owner user name, or
     *         an empty string for an open lock
     */
    public String getUser() {
        if (lock == null) {
            return "";
        } else {
            return lock.getUserName();
        }
    }
}
