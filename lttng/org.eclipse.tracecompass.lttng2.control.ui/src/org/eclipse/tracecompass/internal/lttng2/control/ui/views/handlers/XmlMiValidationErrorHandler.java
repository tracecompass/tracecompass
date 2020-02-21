/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Jonathan Rajotte - Initial implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LTTngControlServiceMI;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * Error handler for xml xsd validation while using machine interface mode
 * in {@link LTTngControlServiceMI}.
 * </p>
 *
 * @author Jonathan Rajotte
 */
public class XmlMiValidationErrorHandler implements ErrorHandler {

    @Override
    public void error(SAXParseException e) throws SAXException {
        Activator.getDefault().logError(Messages.TraceControl_XmlValidationError, e);
        throw new SAXException(Messages.TraceControl_XmlValidationError, e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        Activator.getDefault().logError(Messages.TraceControl_XmlValidationError, e);
        throw new SAXException(Messages.TraceControl_XmlValidationError, e);
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        Activator.getDefault().logWarning(Messages.TraceControl_XmlValidationWarning, e);
    }

}
