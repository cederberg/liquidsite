/*
 * FormattingContext.java
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

package org.liquidsite.core.text;

/**
 * An text formatting context. This interface is used to provide the
 * context for the text formatting, such as methods to resolve
 * relative links and similar.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public interface FormattingContext {

    /**
     * Returns a relative link to an object. If the specified path
     * starts with '/' it is assumed to be relative to the site root
     * directory and will be modified to a page relative link. If the
     * link starts with a protocol (for example "http:") it is
     * considered absolute and will not be modified. Otherwise it is
     * assumed to be relative to the document and may be modified to
     * point to the document directory.
     *
     * @param link           the link URL
     *
     * @return the resolved URL link
     */
    String linkTo(String link);
}
