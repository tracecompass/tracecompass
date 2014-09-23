/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jonathan Rajotte - Initial implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.ui.views.handlers;

import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.service.LTTngControlServiceMI;
import org.eclipse.linuxtools.internal.lttng2.control.ui.Activator;
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
