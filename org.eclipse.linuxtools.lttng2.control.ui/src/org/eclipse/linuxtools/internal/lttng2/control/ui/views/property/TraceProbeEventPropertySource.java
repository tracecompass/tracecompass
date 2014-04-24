/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceEventComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceProbeEventComponent;
import org.eclipse.linuxtools.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

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

    /**
     * Constructor
     *
     * @param component
     *            A trace event component
     */
    public TraceProbeEventPropertySource(TraceEventComponent component) {
        super(component);
        if (component.getClass() != TraceProbeEventComponent.class) {
            throw new IllegalArgumentException("Invalid type passed. Only class of type TraceProbeEventComponent allowed:\n" + component.getClass()); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] superProperties = super.getPropertyDescriptors();

        List<IPropertyDescriptor> superList = Arrays.asList(superProperties);
        ArrayList<IPropertyDescriptor> list = new ArrayList<>();
        list.addAll(superList);

        if (fEvent instanceof TraceProbeEventComponent) {
            TraceProbeEventComponent event = (TraceProbeEventComponent) fEvent;
            if (event.getAddress() != null) {
                list.add(new ReadOnlyTextPropertyDescriptor(TRACE_EVENT_PROBE_ADDRESS_PROPERTY_ID, TRACE_EVENT_PROBE_ADDRESS_PROPERTY_NAME));
            }

            if (event.getOffset() != null) {
                list.add(new ReadOnlyTextPropertyDescriptor(TRACE_EVENT_PROBE_OFFSET_PROPERTY_ID, TRACE_EVENT_PROBE_OFFSET_PROPERTY_NAME));
            }

            if (event.getSymbol() != null) {
                list.add(new ReadOnlyTextPropertyDescriptor(TRACE_EVENT_PROBE_SYMBOL_PROPERTY_ID, TRACE_EVENT_PROBE_SYMBOL_PROPERTY_NAME));
            }
        }
        return list.toArray(new IPropertyDescriptor[list.size()]);
    }

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
