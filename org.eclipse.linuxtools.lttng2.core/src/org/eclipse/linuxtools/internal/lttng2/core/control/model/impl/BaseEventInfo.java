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
package org.eclipse.linuxtools.internal.lttng2.core.control.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.core.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IFieldInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceLogLevel;

/**
* <p>
* Implementation of the basic trace event interface (IEventInfo) to store event
* related data.
* </p>
*
* @author Bernd Hufmann
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
    /**
     * The Event fields
     */
    private final List<IFieldInfo> fFields = new ArrayList<IFieldInfo>();
    /**
     * The filter expression.
     */
    private String fFilterExpression;

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
        for (Iterator<IFieldInfo> iterator = other.fFields.iterator(); iterator.hasNext();) {
            IFieldInfo field = iterator.next();
            if (field instanceof FieldInfo) {
                fFields.add(new FieldInfo((FieldInfo)field));
            } else {
                fFields.add(field);
            }
        }
        fFilterExpression = other.fFilterExpression;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public TraceEventType getEventType() {
        return fEventType;
    }

    @Override
    public void setEventType(TraceEventType type) {
        fEventType = type;
    }

    @Override
    public void setEventType(String typeName) {
        if(TraceEventType.TRACEPOINT.getInName().equals(typeName)) {
            fEventType = TraceEventType.TRACEPOINT;
        } else if(TraceEventType.SYSCALL.getInName().equals(typeName)) {
            fEventType = TraceEventType.SYSCALL;
        } else if (TraceEventType.PROBE.getInName().equals(typeName)) {
            fEventType = TraceEventType.PROBE;
        } else if (TraceEventType.FUNCTION.getInName().equals(typeName)) {
            fEventType = TraceEventType.FUNCTION;
        } else {
            fEventType = TraceEventType.UNKNOWN;
        }
    }

    @Override
    public TraceLogLevel getLogLevel() {
        return fLogLevel;
    }

    @Override
    public void setLogLevel(TraceLogLevel level) {
        fLogLevel = level;
    }

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

    @Override
    public IFieldInfo[] getFields() {
        return fFields.toArray(new IFieldInfo[fFields.size()]);
    }

    @Override
    public void addField(IFieldInfo field) {
        fFields.add(field);
    }

    @Override
    public void setFields(List<IFieldInfo> fields) {
        fFields.clear();
        for (Iterator<IFieldInfo> iterator = fields.iterator(); iterator.hasNext();) {
            IFieldInfo fieldInfo = iterator.next();
            fFields.add(fieldInfo);
        }
    }

    @Override
    public String getFilterExpression() {
        return fFilterExpression;
    }

    @Override
    public void setFilterExpression(String filter) {
        fFilterExpression = filter;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fEventType == null) ? 0 : fEventType.hashCode());
        result = prime * result + fFields.hashCode();
        result = prime * result + ((fFilterExpression == null) ? 0 : fFilterExpression.hashCode());
        result = prime * result + ((fLogLevel == null) ? 0 : fLogLevel.hashCode());
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
        BaseEventInfo other = (BaseEventInfo) obj;
        if (fEventType != other.fEventType) {
            return false;
        }
        if (!fFields.equals(other.fFields)) {
            return false;
        }
        if (fFilterExpression == null) {
            if (other.fFilterExpression != null) {
                return false;
            }
        } else if (!fFilterExpression.equals(other.fFilterExpression)) {
            return false;
        }
        if (fLogLevel != other.fLogLevel) {
            return false;
        }
        return true;
    }

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
            if (!fFields.isEmpty()) {
                output.append(",Fields=");
                for (Iterator<IFieldInfo> iterator = fFields.iterator(); iterator.hasNext();) {
                    IFieldInfo field = iterator.next();
                    output.append(field.toString());
                }
            }
            if (fFilterExpression != null) {
                output.append(",Filter=");
                output.append(fFilterExpression);
            }
            output.append(")]");
            return output.toString();
    }
}
