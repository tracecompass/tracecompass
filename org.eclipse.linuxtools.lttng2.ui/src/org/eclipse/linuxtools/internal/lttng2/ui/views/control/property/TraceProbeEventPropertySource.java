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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceEventComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceProbeEventComponent;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * <p>
 * Property source implementation for the trace probe event component.
 * </p>
 * 
 * @author Bernd Hufmann
 */
public class TraceProbeEventPropertySource extends TraceEventPropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The trace event 'probe address' property ID.
     */
    public static final String TRACE_EVENT_PROBE_ADDRESS_PROPERTY_ID = "trace.event.probe.address"; //$NON-NLS-1$
    /**
     * The trace event 'probe offset' property ID.
     */
    public static final String TRACE_EVENT_PROBE_OFFSET_PROPERTY_ID = "trace.event.probe.offset"; //$NON-NLS-1$
    /**
     * The trace event 'probe symbol' property ID.
     */
    public static final String TRACE_EVENT_PROBE_SYMBOL_PROPERTY_ID = "trace.event.probe.symbol"; //$NON-NLS-1$
    /**
     * The trace event 'probe address' property name.
     */
    public static final String TRACE_EVENT_PROBE_ADDRESS_PROPERTY_NAME = Messages.TraceControl_ProbeAddressPropertyName;
    /**
     * The trace event 'probe offset' property ID.
     */
    public static final String TRACE_EVENT_PROBE_OFFSET_PROPERTY_NAME = Messages.TraceControl_ProbeOffsetPropertyName;
    /**
     * The trace event 'probe symbol' property ID.
     */
    public static final String TRACE_EVENT_PROBE_SYMBOL_PROPERTY_NAME = Messages.TraceControl_ProbeSymbolPropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public TraceProbeEventPropertySource(TraceEventComponent component) {
        super(component);
        if (component.getClass() != TraceProbeEventComponent.class) {
            throw new IllegalArgumentException("Invalid type passed. Only class of type TraceProbeEventComponent allowed:\n" + component.getClass()); //$NON-NLS-1$
        }
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.BasePropertySource#getPropertyDescriptors()
     */
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] superProperties = super.getPropertyDescriptors();

        List<IPropertyDescriptor> superList = Arrays.asList(superProperties);
        ArrayList<IPropertyDescriptor> list = new ArrayList<IPropertyDescriptor>();
        list.addAll(superList);

        if (fEvent instanceof TraceProbeEventComponent) {
            TraceProbeEventComponent event = (TraceProbeEventComponent) fEvent;
            if (event.getAddress() != null) {
                list.add(new TextPropertyDescriptor(TRACE_EVENT_PROBE_ADDRESS_PROPERTY_ID, TRACE_EVENT_PROBE_ADDRESS_PROPERTY_NAME));                
            }

            if (event.getOffset() != null) {
                list.add(new TextPropertyDescriptor(TRACE_EVENT_PROBE_OFFSET_PROPERTY_ID, TRACE_EVENT_PROBE_OFFSET_PROPERTY_NAME));
            }

            if (event.getSymbol() != null) {
                list.add(new TextPropertyDescriptor(TRACE_EVENT_PROBE_SYMBOL_PROPERTY_ID, TRACE_EVENT_PROBE_SYMBOL_PROPERTY_NAME));
            }
        }
        return (IPropertyDescriptor [])list.toArray(new IPropertyDescriptor[list.size()]);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.BasePropertySource#getPropertyValue(java.lang.Object)
     */
    @Override
    public Object getPropertyValue(Object id) {
        if(TRACE_EVENT_PROBE_ADDRESS_PROPERTY_ID.equals(id)) {
            return ((TraceProbeEventComponent)fEvent).getAddress();
        }
        if (TRACE_EVENT_PROBE_OFFSET_PROPERTY_ID.equals(id)) {
            return ((TraceProbeEventComponent)fEvent).getOffset();
        }
        if (TRACE_EVENT_PROBE_SYMBOL_PROPERTY_ID.equals(id)) {
            return ((TraceProbeEventComponent)fEvent).getSymbol();
        }
        return super.getPropertyValue(id);
    }
}
