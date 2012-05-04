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
package org.eclipse.linuxtools.internal.lttng2.core.control.model.impl;

import org.eclipse.linuxtools.internal.lttng2.core.control.model.IEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceEnablement;

/**
* <b><u>EventInfo</u></b>
* <p>
* Implementation of the trace event interface (IEventInfo) to store event
* related data. 
* </p>
*/
public class EventInfo extends BaseEventInfo implements IEventInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The enable state of the event.
     */
    private TraceEnablement fState = TraceEnablement.DISABLED;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - name of event
     */
    public EventInfo(String name) {
        super(name);
    }
    
    /**
     * Copy constructor
     * @param other - the instance to copy
     */
    public EventInfo(EventInfo other) {
        super(other);
        fState = other.fState;
    }
    
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IEventInfo#getState()
     */
    @Override
    public TraceEnablement getState() {
        return fState;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IEventInfo#setState(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceEnablement)
     */
    @Override
    public void setState(TraceEnablement state) {
        fState = state;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IEventInfo#setState(java.lang.String)
     */
    @Override
    public void setState(String stateName) {
        fState = TraceEnablement.DISABLED;
        if (TraceEnablement.DISABLED.getInName().equals(stateName)) {
            fState = TraceEnablement.DISABLED;
        } else if (TraceEnablement.ENABLED.getInName().equals(stateName)) {
            fState = TraceEnablement.ENABLED;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.BaseEventInfo#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fState == null) ? 0 : (fState.ordinal() + 1));
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.BaseEventInfo#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EventInfo other = (EventInfo) obj;
        if (fState != other.fState) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.BaseEventInfo#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[EventInfo(");
            output.append(super.toString());
            output.append(",State=");
            output.append(fState);
            output.append(")]");
            return output.toString();
    }
}
