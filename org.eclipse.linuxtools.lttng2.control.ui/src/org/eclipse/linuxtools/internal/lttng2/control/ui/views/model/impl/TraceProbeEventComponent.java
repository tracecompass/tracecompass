/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.IEventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.ProbeEventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.property.TraceProbeEventPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;


/**
 * <p>
 * Implementation of the trace channel component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceProbeEventComponent extends TraceEventComponent {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */
    public TraceProbeEventComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        fEventInfo = new ProbeEventInfo(name);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Sets the event information.
     * @param eventInfo - the event information to set.
     */
    @Override
    public void setEventInfo(IEventInfo eventInfo) {
        if (eventInfo instanceof ProbeEventInfo) {
            fEventInfo = eventInfo;
            return;
        }
        throw new IllegalArgumentException("Invalid type passed. Only class of type ProbeEventInfo allowed:\n" + eventInfo.getClass()); //$NON-NLS-1$
    }

    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            return new TraceProbeEventPropertySource(this);
        }
        return null;
    }
    /**
     * @return the address of the probe. (null if Symbol is used)
     */
    public String getAddress() {
        return getEventInfo().getAddress();
    }
    /**
     * Sets the address of the probe.
     * @param address - a address
     */
    public void setAddress(String address) {
        getEventInfo().setAddress(address);
    }
    /**
     * @return the offset applied to the symbol.
     */
    public String getOffset() {
        return getEventInfo().getOffset();
    }
    /**
     * Sets the offset applied to the symbol. (valid if symbol is used)
     * @param offset - a offset
     */
    public void setOffset(String offset) {
        getEventInfo().setOffset(offset);
    }
    /**
     * @return the symbol name. (null if address is used)
     */
    public String getSymbol() {
        return getEventInfo().getSymbol();
    }
    /**
     * Sets the symbol name.
     * @param symbol - a symbol name (null if address is used)
     */
    public void setSymbol(String symbol) {
        getEventInfo().setSymbol(symbol);
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    private ProbeEventInfo getEventInfo() {
        return (ProbeEventInfo) fEventInfo;
    }

}
