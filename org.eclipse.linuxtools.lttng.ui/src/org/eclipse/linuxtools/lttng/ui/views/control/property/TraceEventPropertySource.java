/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.control.property;

import org.eclipse.linuxtools.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceEventComponent;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * <b><u>TraceEventPropertySource</u></b>
 * <p>
 * Property source implementation for the trace event component.
 * </p>
 */
public class TraceEventPropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The trace event 'name' property ID.
     */
    public static final String TRACE_EVENT_NAME_PROPERTY_ID = "trace.event.name"; //$NON-NLS-1$
    /**
     * The trace event 'type' property ID.
     */
    public static final String TRACE_EVENT_TYPE_PROPERTY_ID = "trace.event.type"; //$NON-NLS-1$
    /**
     * The trace event 'log level' property ID.
     */
    public static final String TRACE_EVENT_LOGLEVEL_PROPERTY_ID = "trace.event.loglevel"; //$NON-NLS-1$
    /**
     * The trace event 'state' property ID.
     */
    public static final String TRACE_EVENT_STATE_PROPERTY_ID = "trace.event.state"; //$NON-NLS-1$
    /**
     *  The trace event 'name' property name. 
     */
    public static final String TRACE_EVENT_NAME_PROPERTY_NAME = Messages.TraceControl_EventNamePropertyName;
    /**
     * The trace event 'type' property name.
     */
    public static final String TRACE_EVENT_TYPE_PROPERTY_NAME = Messages.TraceControl_EventTypePropertyName;
    /**
     * The trace event 'log level' property name.
     */
    public static final String TRACE_EVENT_LOGLEVEL_PROPERTY_NAME = Messages.TraceControl_LogLevelPropertyName;
    /**
     * The trace event 'state' property name.
     */
    public static final String TRACE_EVENT_STATE_PROPERTY_NAME = Messages.TraceControl_StatePropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The event component which this property source is for. 
     */
    private final TraceEventComponent fEvent;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param component - the base event component
     */
    public TraceEventPropertySource(TraceEventComponent component) {
        fEvent = component;
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.property.BasePropertySource#getPropertyDescriptors()
     */
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
                new TextPropertyDescriptor(TRACE_EVENT_NAME_PROPERTY_ID, TRACE_EVENT_NAME_PROPERTY_NAME),
                new TextPropertyDescriptor(TRACE_EVENT_TYPE_PROPERTY_ID, TRACE_EVENT_TYPE_PROPERTY_NAME),
                new TextPropertyDescriptor(TRACE_EVENT_LOGLEVEL_PROPERTY_ID, TRACE_EVENT_LOGLEVEL_PROPERTY_NAME),
                new TextPropertyDescriptor(TRACE_EVENT_STATE_PROPERTY_ID, TRACE_EVENT_STATE_PROPERTY_NAME)};
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.property.BasePropertySource#getPropertyValue(java.lang.Object)
     */
    @Override
    public Object getPropertyValue(Object id) {
        if(TRACE_EVENT_NAME_PROPERTY_ID.equals(id)) {
            return fEvent.getName();
        }
        if (TRACE_EVENT_TYPE_PROPERTY_ID.equals(id)) {
            return fEvent.getEventType().name();
        }
        if (TRACE_EVENT_LOGLEVEL_PROPERTY_ID.equals(id)) {
            return fEvent.getLogLevel().name();
        }
        if (TRACE_EVENT_STATE_PROPERTY_ID.equals(id)) {
            return fEvent.getState().name();
        }
        return null;
    }

}
