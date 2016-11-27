/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.project.model;

import java.util.Objects;

/**
 * Trace import helper class
 *
 * @author Matthew Khouzam
 * @deprecated Use TmfTraceType.getTraceTypeHelpers and
 *             {@link TraceTypeHelper#validate(String)} or
 *             {@link TraceTypeHelper#validateWithConfidence(String)}
 */
@Deprecated
public class TraceValidationHelper implements Comparable<TraceValidationHelper> {

    private final String fTraceToScan;
    private final String fTraceType;

    /**
     * Trace To validate constructor
     *
     * @param traceToScan
     *            the path of the trace
     * @param traceType
     *            the trace type of the trace to add (canonical name)
     */
    public TraceValidationHelper(String traceToScan, String traceType) {
        this.fTraceToScan = traceToScan;
        this.fTraceType = traceType;
    }

    /**
     * @return the trace filename
     */
    public String getTraceToScan() {
        return fTraceToScan;
    }

    /**
     * @return the trace type canonical name
     */
    public String getTraceType() {
        return fTraceType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fTraceToScan == null) ? 0 : fTraceToScan.hashCode());
        result = prime * result + ((fTraceType == null) ? 0 : fTraceType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TraceValidationHelper)) {
            return false;
        }
        TraceValidationHelper other = (TraceValidationHelper) obj;
        if (!Objects.equals(fTraceToScan, other.fTraceToScan)) {
            return false;
        }
        if(!Objects.equals(fTraceType, other.fTraceType)){
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(TraceValidationHelper o) {
        int retVal = fTraceToScan.compareTo(o.getTraceToScan());
        if (retVal == 0) {
            retVal = fTraceType.compareTo(o.fTraceType);
        }
        return retVal;
    }
}