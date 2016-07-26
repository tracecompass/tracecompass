/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bruno Roy - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.core.model.impl;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseLoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ITraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceJulLogLevel;

/**
 * Implementation of the basic trace logger interface (IBaseLoggerInfo) to store
 * logger related data.
 *
 * @author Bruno Roy
 */
public class BaseLoggerInfo extends TraceInfo implements IBaseLoggerInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The trace log level.
     */
    private ITraceLogLevel fLogLevel = TraceJulLogLevel.LEVEL_UNKNOWN;
    /**
     * The logger domain.
     */
    private TraceDomainType fDomain = TraceDomainType.UNKNOWN;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param name
     *            name of base event
     */
    public BaseLoggerInfo(String name) {
        super(name);
    }

    /**
     * Copy constructor
     *
     * @param other
     *            the instance to copy
     */
    public BaseLoggerInfo(BaseLoggerInfo other) {
        super(other);
        fDomain = other.fDomain;
        fLogLevel = other.fLogLevel;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public ITraceLogLevel getLogLevel() {
        return fLogLevel;
    }

    @Override
    public void setLogLevel(ITraceLogLevel level) {
        fLogLevel = level;
    }

    @Override
    public void setLogLevel(String levelName) {
        fLogLevel = TraceJulLogLevel.valueOfString(levelName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fDomain == null) ? 0 : fDomain.hashCode());
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
        BaseLoggerInfo other = (BaseLoggerInfo) obj;
        if (fDomain != other.fDomain) {
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
        output.append("[BaseLoggerInfo(");
        output.append(super.toString());
        output.append(",domain=");
        output.append(fDomain);
        output.append(",level=");
        output.append(fLogLevel);
        output.append(")]");
        return output.toString();
    }

    @Override
    public TraceDomainType getDomain() {
        return fDomain;
    }

    @Override
    public void setDomain(TraceDomainType domain) {
        fDomain = domain;
    }
}
