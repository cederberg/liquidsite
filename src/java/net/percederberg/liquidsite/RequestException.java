/*
 * RequestException.java
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

package net.percederberg.liquidsite;

/**
 * A request exception. This exception is thrown when a request
 * couldn't be processed normally. This can be due to various causes,
 * such as authentification failure, inexistent resource, or internal
 * errors. No new instances are created of this exception, but the
 * existing ones are thrown instead.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class RequestException extends Exception {

    /**
     * The internal error constant. This error is used when a problem
     * was encountered during the request processing, making the
     * normal request processing fail.
     */
    public static final RequestException INTERNAL_ERROR =
        new RequestException(500, "An internal error was encountered");

    /**
     * The unauthorized access request error. This error is used when
     * a request for a resource requires user authentification in
     * order to be retrieved.
     */
    public static final RequestException UNAUTHORIZED =
        new RequestException(401, "User authentification required");

    /**
     * The forbidden access request error. This error is used when
     * the user wasn't authorized to access the requested resource.
     */
    public static final RequestException FORBIDDEN =
        new RequestException(403, "Access to resource forbidden");

    /**
     * The resource not found request error. This error is used when
     * the requested resource couldn't be found.
     */
    public static final RequestException RESOURCE_NOT_FOUND =
        new RequestException(404, "Requested resource does not exist");

    /**
     * The HTTP error code.
     */
    private int errorCode;

    /**
     * Creates a new request exception.
     *
     * @param code           the HTTP error code
     * @param message        the error message
     */
    private RequestException(int code, String message) {
        super(message);
        this.errorCode = code;
    }

    /**
     * Returns the HTTP error code.
     *
     * @return the HTTP error code
     */
    public int getCode() {
        return errorCode;
    }
}
