/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bruno Roy - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.core.model.impl;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.ILoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;

/**
* Implementation of the logger interface (ILoggerInfo) to store logger
* related data.
*
* @author Bruno Roy
*/
public class LoggerInfo extends BaseLoggerInfo implements ILoggerInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The enable state of the logger.
     */
    private TraceEnablement fState = TraceEnablement.DISABLED;
    /**
     * The log level type.
     */
    private LogLevelType fLogLevelType = LogLevelType.LOGLEVEL_ALL;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param name
     *            name of logger
     */
    public LoggerInfo(String name) {
        super(name);
    }

    /**
     * Copy constructor
     *
     * @param other
     *            the instance to copy
     */
    public LoggerInfo(LoggerInfo other) {
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
        LoggerInfo other = (LoggerInfo) obj;
        if (fState != other.fState) {
            return false;
        }
        return (fLogLevelType == other.fLogLevelType) ;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[LoggerInfo(");
            output.append(super.toString());
            output.append(",State=");
            output.append(fState);
            output.append(",levelType=");
            output.append(fLogLevelType);
            output.append(")]");
            return output.toString();
    }

}
