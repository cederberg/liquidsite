/*
 * FormHandler.java
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

package net.percederberg.liquidsite.form;

import net.percederberg.liquidsite.Request;

/**
 * A form request handler. This class attempts to provide some
 * workflow support, by making initial request parameter analysis and
 * calling the appropriate event methods. A subclass should implement
 * the various event methods.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public abstract class FormHandler {

    /**
     * Processes the form request. This method is called for each
     * incoming request to the form workflow, including the first one
     * requesting presentation of the (first) form. This method
     * handles errors by calling the error event method. To handle
     * errors externally, use the processWithoutErrorHandling method
     * instead.
     *
     * @param request        the request object
     *
     * @see #processWithoutErrorHandling
     */
    public final void process(Request request) {
        try {
            processWithoutErrorHandling(request);
        } catch (FormHandlingException e) {
            workflowError(request, e);
        }
    }

    /**
     * Processes the form request. This method is called for each
     * incoming request to the form workflow, including the first one
     * requesting presentation of the (first) form. Note that this
     * method will not trigger the error event on error, but will
     * return an exception instead.
     *
     * @param request        the request object
     *
     * @throws FormHandlingException if an error was encountered
     *             while processing the form
     *
     * @see #process
     */
    public final void processWithoutErrorHandling(Request request)
        throws FormHandlingException {

        String  stepParam;
        String  prevParam;
        int     step;

        // Retrieve workflow parameters
        stepParam = request.getParameter("liquidsite.step", "0");
        prevParam = request.getParameter("liquidsite.prev", "");
        try {
            step = Integer.parseInt(stepParam);
        } catch (NumberFormatException e) {
            step = 0;
        }

        // Validate and handle form data
        if (step <= 0) {
            workflowEntered(request);
            step = 1;
        } else if (prevParam.equals("true")) {
            step--;
        } else {
            try {
                validate(request, step);
                step = handle(request, step);
            } catch (FormValidationException e) {
                request.setAttribute("error", e.getMessage());
            }
        }

        // Display next form
        if (step <= 0) {
            workflowExited(request);
            display(request, 0);
        } else {
            display(request, step);
        }
    }

    /**
     * Displays a form for the specified workflow step. The special
     * workflow step zero (0) is used for indicating display of the
     * originating page outside the form workflow. Normally that
     * would cause a redirect.
     *
     * @param request        the request object
     * @param step           the workflow step, or zero (0)
     *
     * @throws FormHandlingException if an error was encountered
     *             while processing the form
     */
    protected abstract void display(Request request, int step)
        throws FormHandlingException;

    /**
     * Validates a form for the specified workflow step. If the form
     * validation fails in this step, the form page for the workflow
     * step will be displayed again with an 'error' attribute
     * containing the message in the validation exception.
     *
     * @param request        the request object
     * @param step           the workflow step
     *
     * @throws FormValidationException if the form request data
     *             validation failed
     * @throws FormHandlingException if an error was encountered
     *             while processing the form
     */
    protected abstract void validate(Request request, int step)
        throws FormValidationException, FormHandlingException;

    /**
     * Handles a validated form for the specified workflow step. This
     * method returns the next workflow step, i.e. the step used when
     * calling the display method. If the special zero (0) workflow
     * step is returned, the workflow is assumed to have terminated.
     * Note that this method also allows additional validation to
     * occur. By returning the incoming workflow step number and
     * setting the appropriate request attributes the same results as
     * in the normal validate method can be achieved. For recoverable
     * errors, this is the recommended course of action.
     *
     * @param request        the request object
     * @param step           the workflow step
     *
     * @return the next workflow step, or
     *         zero (0) if the workflow has finished
     *
     * @throws FormHandlingException if an error was encountered
     *             while processing the form
     */
    protected abstract int handle(Request request, int step)
        throws FormHandlingException;

    /**
     * This method is called when the user is entering the workflow.
     * This event can be used for handling object locking or
     * messaging related to the workflow. By default this method does
     * nothing.
     *
     * @param request        the request object
     *
     * @throws FormHandlingException if an error was encountered
     *             while processing the form
     */
    protected void workflowEntered(Request request)
        throws FormHandlingException {
    }

    /**
     * This method is called when the user is exiting the workflow.
     * This even is triggered either by finishing the complete
     * workflow, or by backing out to workflow step zero (0). This
     * event can be used for handling object locking or messaging
     * related to the workflow. By default this method does
     * nothing.<p>
     *
     * <strong>NOTE:</strong> This method will not be called when the
     * user exits the workflow in a non-standard way. Closing the
     * browser window, using back buttons or causing form handling
     * exceptions are all examples of when this method will NOT be
     * called.
     *
     * @param request        the request object
     *
     * @throws FormHandlingException if an error was encountered
     *             while processing the form
     */
    protected void workflowExited(Request request)
        throws FormHandlingException {
    }

    /**
     * This method is called when an error was encountered during the
     * processing. This event can be used for logging errors and
     * displaying a simple error page. By default this method does
     * nothing.
     *
     * @param request        the request object
     * @param e              the form handling exception
     */
    protected void workflowError(Request request, FormHandlingException e) {
    }
}
