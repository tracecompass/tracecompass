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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceLogLevel;

/**
* <b><u>BaseEventInfo</u></b>
* <p>
* Implementation of the basic trace event interface (IEventInfo) to store event
* related data. 
* </p>
*/
public class BaseEventInfo extends TraceInfo implements IBaseEventInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The trace event type.
     */
    private TraceEventType fEventType = TraceEventType.UNKNOWN;
    /**
     * The trace log level.
     */
    private TraceLogLevel fLogLevel = TraceLogLevel.TRACE_DEBUG;
  
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - name of base event
     */
    public BaseEventInfo(String name) {
        super(name);
    }

    /**
     * Copy constructor
     * @param other - the instance to copy
     */
    public BaseEventInfo(BaseEventInfo other) {
        super(other);
        fEventType = other.fEventType;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IBaseEventInfo#getEventType()
     */
    @Override
    public TraceEventType getEventType() {
        return fEventType;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IBaseEventInfo#setEventType(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceEventType)
     */
    @Override
    public void setEventType(TraceEventType type) {
        fEventType = type;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IBaseEventInfo#setEventType(java.lang.String)
     */
    @Override
    public void setEventType(String typeName) {
        if(TraceEventType.TRACEPOINT.getInName().equals(typeName)) {
            fEventType = TraceEventType.TRACEPOINT;
        } else if(TraceEventType.SYSCALL.getInName().equals(typeName)) {
            fEventType = TraceEventType.SYSCALL;
        } else if (TraceEventType.PROBE.getInName().equals(typeName)) {
            fEventType = TraceEventType.PROBE;  
        } else {
            fEventType = TraceEventType.UNKNOWN;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IBaseEventInfo#getLogLevel()
     */
    @Override
    public TraceLogLevel getLogLevel() {
        return fLogLevel;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IBaseEventInfo#setLogLevel(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceLogLevel)
     */
    @Override
    public void setLogLevel(TraceLogLevel level) {
        fLogLevel = level;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IBaseEventInfo#setLogLevel(java.lang.String)
     */
    @Override
    public void setLogLevel(String levelName) {
        if(TraceLogLevel.TRACE_EMERG.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_EMERG;
        } else if(TraceLogLevel.TRACE_ALERT.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_ALERT;
        } else if(TraceLogLevel.TRACE_CRIT.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_CRIT;
        } else if(TraceLogLevel.TRACE_ERR.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_ERR;
        } else if(TraceLogLevel.TRACE_WARNING.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_WARNING;
        } else if(TraceLogLevel.TRACE_NOTICE.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_NOTICE;
        } else if(TraceLogLevel.TRACE_INFO.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_INFO;
        } else if(TraceLogLevel.TRACE_DEBUG_SYSTEM.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_DEBUG_SYSTEM;
        } else if(TraceLogLevel.TRACE_DEBUG_PROGRAM.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_DEBUG_PROGRAM;
        } else if(TraceLogLevel.TRACE_DEBUG_PROCESS.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_DEBUG_PROCESS;
        } else if(TraceLogLevel.TRACE_DEBUG_MODULE.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_DEBUG_MODULE;
        } else if(TraceLogLevel.TRACE_DEBUG_UNIT.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_DEBUG_UNIT;
        } else if(TraceLogLevel.TRACE_DEBUG_FUNCTION.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_DEBUG_FUNCTION;
        } else if(TraceLogLevel.TRACE_DEBUG_LINE.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_DEBUG_LINE;
        } else if(TraceLogLevel.TRACE_DEBUG.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.TRACE_DEBUG;
        } else if(TraceLogLevel.LEVEL_UNKNOWN.getInName().equals(levelName)) {
            fLogLevel = TraceLogLevel.LEVEL_UNKNOWN;
        } else {
            fLogLevel = TraceLogLevel.TRACE_DEBUG;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceInfo#formatString()
     */
    @SuppressWarnings("nls")
    @Override
    public String formatString() {
        StringBuffer output = new StringBuffer();
        //    ust_tests_hello:tptest_sighandler (loglevel: TRACE_DEBUG_MODULE (10)) (type: tracepoint)");
        output.append("\n      ");
        output.append(getName());
        if (fLogLevel != TraceLogLevel.LEVEL_UNKNOWN) {
            output.append(" (loglevel: ");
            output.append(fLogLevel.getInName());
            output.append(" (");
            output.append(fLogLevel.ordinal());
            output.append("))");
        }
        output.append(" (type: ");
        output.append(fEventType.getInName());
        output.append(")");
        return output.toString();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceInfo#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fEventType == null) ? 0 : (fEventType.ordinal() + 1));
        result = prime * result + ((fLogLevel == null) ? 0 : (fLogLevel.ordinal() + 1));
        return result;
    }    

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceInfo#equals(java.lang.Object)
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
        BaseEventInfo other = (BaseEventInfo) obj;
        if (fEventType != other.fEventType) {
            return false;
        }
        if (fLogLevel != other.fLogLevel) {
            return false;
        }
        return true;
    }    

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceInfo#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[BaseEventInfo(");
            output.append(super.toString());
            output.append(",type=");
            output.append(fEventType);
            output.append(",level=");
            output.append(fLogLevel);
            output.append(")]");
            return output.toString();
    }

 


}