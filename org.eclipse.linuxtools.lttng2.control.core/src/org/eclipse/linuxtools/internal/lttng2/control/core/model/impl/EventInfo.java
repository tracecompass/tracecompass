/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.core.model.impl;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.IEventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceEnablement;

/**
* <p>
* Implementation of the trace event interface (IEventInfo) to store event
* related data.
* </p>
*
* @author Bernd Hufmann
*/
public class EventInfo extends BaseEventInfo implements IEventInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The enable state of the event.
     */
    private TraceEnablement fState = TraceEnablement.DISABLED;
    /**
     * The log level type.
     */
    private LogLevelType fLogLevelType = LogLevelType.LOGLEVEL_NONE;

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
        fLogLevelType = other.fLogLevelType;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public TraceEnablement getState() {
        return fState;
    }

    @Override
    public void setState(TraceEnablement state) {
        fState = state;
    }

    @Override
    public void setState(String stateName) {
        fState = TraceEnablement.valueOfString(stateName);
    }

    @Override
    public LogLevelType getLogLevelType() {
        return fLogLevelType;
    }

    @Override
    public void setLogLevelType(LogLevelType type) {
        fLogLevelType = type;
    }

    @Override
    public void setLogLevelType(String shortName) {
        fLogLevelType = LogLevelType.valueOfString(shortName);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fState == null) ? 0 : (fState.ordinal() + 1));
        result = prime * result + ((fLogLevelType == null) ? 0 : fLogLevelType.hashCode());
        return result;
    }

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
        if (fLogLevelType != other.fLogLevelType) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[EventInfo(");
            output.append(super.toString());
            output.append(",State=");
            output.append(fState);
            output.append(",levelType=");
            output.append(fLogLevelType);
            output.append(")]");
            return output.toString();
    }
}
