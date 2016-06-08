/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Roy- Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.property;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceJulLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceLoggerComponent;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * Property source implementation for the trace logger component.
 *
 * @author Bruno Roy
 */
public class TraceLoggerPropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The trace logger 'name' property ID.
     */
    public static final String TRACE_LOGGER_NAME_PROPERTY_ID = "trace.logger.name"; //$NON-NLS-1$
    /**
     * The trace logger 'log level' property ID.
     */
    public static final String TRACE_LOGGER_LOGLEVEL_PROPERTY_ID = "trace.logger.loglevel"; //$NON-NLS-1$
    /**
     * The trace logger 'state' property ID.
     */
    public static final String TRACE_LOGGER_STATE_PROPERTY_ID = "trace.logger.state"; //$NON-NLS-1$

    /**
     * The trace logger 'name' property name.
     */
    public static final String TRACE_LOGGER_NAME_PROPERTY_NAME = Messages.TraceControl_LoggerNamePropertyName;
    /**
     * The trace logger 'log level' property name.
     */
    public static final String TRACE_LOGGER_LOGLEVEL_PROPERTY_NAME = Messages.TraceControl_LogLevelPropertyName;
    /**
     * The trace logger 'state' property name.
     */
    public static final String TRACE_LOGGER_STATE_PROPERTY_NAME = Messages.TraceControl_StatePropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The logger component which this property source is for.
     */
    protected final TraceLoggerComponent fLogger;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param component
     *            the base logger component
     */
    public TraceLoggerPropertySource(TraceLoggerComponent component) {
        fLogger = component;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        List<IPropertyDescriptor> list = new ArrayList<>();
        list.add(new ReadOnlyTextPropertyDescriptor(TRACE_LOGGER_NAME_PROPERTY_ID, TRACE_LOGGER_NAME_PROPERTY_NAME));
        list.add(new ReadOnlyTextPropertyDescriptor(TRACE_LOGGER_STATE_PROPERTY_ID, TRACE_LOGGER_STATE_PROPERTY_NAME));
        if (!fLogger.getLogLevel().equals(TraceJulLogLevel.LEVEL_UNKNOWN)) {
            list.add(new ReadOnlyTextPropertyDescriptor(TRACE_LOGGER_LOGLEVEL_PROPERTY_ID, TRACE_LOGGER_LOGLEVEL_PROPERTY_NAME));
        }
        return list.toArray(new IPropertyDescriptor[list.size()]);
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (TRACE_LOGGER_NAME_PROPERTY_ID.equals(id)) {
            return fLogger.getName();
        }
        if (TRACE_LOGGER_LOGLEVEL_PROPERTY_ID.equals(id)) {
            StringBuffer buffer = new StringBuffer();
            if (fLogger.getLogLevelType() != LogLevelType.LOGLEVEL_NONE) {
                buffer.append(fLogger.getLogLevelType().getShortName()).append(' ');
            }
            buffer.append(fLogger.getLogLevel().name());
            return buffer.toString();
        }
        if (TRACE_LOGGER_STATE_PROPERTY_ID.equals(id)) {
            return fLogger.getState().name();
        }
        return null;
    }

}
