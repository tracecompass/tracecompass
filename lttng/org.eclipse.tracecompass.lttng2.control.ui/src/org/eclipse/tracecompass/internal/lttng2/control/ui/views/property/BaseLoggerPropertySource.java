/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Roy - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.property;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.BaseLoggerComponent;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;


/**
 * Property source implementation for the base logger component.
 *
 * @author Bruno Roy
 */
public class BaseLoggerPropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The base logger 'name' property ID.
     */
    public static final String BASE_LOGGER_NAME_PROPERTY_ID = "base.logger.name"; //$NON-NLS-1$
    /**
     * The base logger 'domain' property ID.
     */
    public static final String BASE_LOGGER_DOMAIN_PROPERTY_ID = "base.logger.domain"; //$NON-NLS-1$
    /**
     *  The base logger 'name' property name.
     */
    public static final String BASE_LOGGER_NAME_PROPERTY_NAME = Messages.TraceControl_LoggerNamePropertyName;
    /**
     * The base logger 'domain' property name.
     */
    public static final String BASE_LOGGER_DOMAIN_PROPERTY_NAME = Messages.TraceControl_LoggerDomainPropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The base logger component which this property source is for.
     */
    private final BaseLoggerComponent fBaseLogger;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param component
     *            the base logger component
     */
    public BaseLoggerPropertySource(BaseLoggerComponent component) {
        fBaseLogger = component;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        List<IPropertyDescriptor> list = new ArrayList<> ();
        list.add(new ReadOnlyTextPropertyDescriptor(BASE_LOGGER_NAME_PROPERTY_ID, BASE_LOGGER_NAME_PROPERTY_NAME));
        list.add(new ReadOnlyTextPropertyDescriptor(BASE_LOGGER_DOMAIN_PROPERTY_ID, BASE_LOGGER_DOMAIN_PROPERTY_NAME));
        return list.toArray(new IPropertyDescriptor[list.size()]);
    }

    @Override
    public Object getPropertyValue(Object id) {
        if(BASE_LOGGER_NAME_PROPERTY_ID.equals(id)) {
            return fBaseLogger.getName();
        } else if (BASE_LOGGER_DOMAIN_PROPERTY_ID.equals(id)) {
            return fBaseLogger.getDomain().name();
        }
        return null;
    }
}
