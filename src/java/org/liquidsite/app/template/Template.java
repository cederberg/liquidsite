/*
 * Template.java
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

package org.liquidsite.app.template;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.TemplateExceptionHandler;

import org.liquidsite.core.web.Request;
import org.liquidsite.core.content.ContentManager;
import org.liquidsite.util.log.Log;

/**
 * A template class. This class wraps a FreeMarker template and adds
 * the liquidsite data model upon processing.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public class Template {

    /**
     * The class logger.
     */
    private static final Log LOG = new Log(Template.class);

    /**
     * The FreeMarker template.
     */
    private freemarker.template.Template template;

    /**
     * The template output MIME type.
     */
    private String mimeType = null;

    /**
     * The template output data.
     */
    private String output = null;

    /**
     * Creates a new template
     *
     * @param template       the FreeMarker template to use
     */
    Template(freemarker.template.Template template) {
        TemplateExceptionHandler  handler;

        handler = TemplateExceptionHandler.RETHROW_HANDLER;
        template.setTemplateExceptionHandler(handler);
        this.template = template;
    }

    /**
     * Processes the template with a request and a content manager.
     * All the attributes in the request will be exposed in the
     * template data model. The processing result will be sent as
     * data to the request.
     *
     * @param request        the request object
     * @param manager        the content manager to use
     *
     * @throws TemplateException if the template processing failed
     */
    public void processNormal(Request request, ContentManager manager)
        throws TemplateException {

        process(request, manager);
        if (!request.hasResponse()) {
            request.sendData(mimeType, output);
        }
    }

    /**
     * Processes the template with a request and a content manager.
     * All the attributes in the request will be exposed in the
     * template data model. The processing result will be sent as
     * an error to the request.
     *
     * @param request        the request object
     * @param manager        the content manager to use
     * @param code           the error code
     *
     * @throws TemplateException if the template processing failed
     */
    public void processError(Request request, ContentManager manager, int code)
        throws TemplateException {

        process(request, manager);
        request.sendError(code, mimeType, output);
    }

    /**
     * Processes the template with a request and a content manager.
     * All the attributes in the request will be exposed in the
     * template data model. The processing result will be stored in
     * the corresponding instance variables.
     *
     * @param request        the request object
     * @param manager        the content manager to use
     *
     * @throws TemplateException if the template processing failed
     */
    private void process(Request request, ContentManager manager)
        throws TemplateException {

        Map          data = request.getAllAttributes();
        Environment  env;
        BeanContext  context;
        StringWriter buffer = new StringWriter();

        context = new BeanContext(request, manager);
        data.put("liquidsite", new LiquidSiteBean(context));
        try {
            env = template.createProcessingEnvironment(data, buffer);
            if (request.getUser() != null) {
                env.setTimeZone(request.getUser().getTimeZone());
            }
            env.process();
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new TemplateException(e);
        } catch (freemarker.template.TemplateException e) {
            LOG.error(e.getMessage());
            throw new TemplateException(e);
        } catch (RuntimeException e) {
            LOG.error(e.getMessage());
            throw new TemplateException(e);
        }
        mimeType = context.getMimeType();
        output = buffer.toString();
    }
}
