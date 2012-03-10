/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.core.event;

import org.eclipse.linuxtools.tmf.core.event.*;

/**
 * <b><u>LttngEventType</u></b><p>
 * 
 * Lttng specific implementation of the TmfEventType.<p>
 * 
 * This implementation add some attributes to the basic Tmf object.
 */
public class LttngEventType extends TmfEventType {
    
    private static final String DEFAULT_CONTEXT = "Kernel Trace"; //$NON-NLS-1$
    private static final String DEFAULT_TYPE_ID = "Kernel Trace"; //$NON-NLS-1$
    // These should match the column names in LTTng Events Table
    public static final String TIMESTAMP_LABEL = "Timestamp"; //$NON-NLS-1$
    public static final String TRACE_LABEL = "Trace"; //$NON-NLS-1$
    public static final String MARKER_LABEL = "Marker"; //$NON-NLS-1$
    public static final String CONTENT_LABEL = "Content"; //$NON-NLS-1$
    private static final String[] DEFAULT_LABELS = {
        TIMESTAMP_LABEL, TRACE_LABEL, MARKER_LABEL, CONTENT_LABEL
    };
    public static final LttngEventType DEFAULT_EVENT_TYPE = new LttngEventType(DEFAULT_TYPE_ID, DEFAULT_LABELS);
    
    private String tracefileName    = null;
    private Long   cpuId            = null;
    private String markerName       = null;
    private int markerId            = -1;
    
    /**
     * Default Constructor.<p>
     * 
     */
    public LttngEventType() {
        super();
    }
    
    /**
     * Default Constructor.<p>
     * 
     */
    public LttngEventType(String typeId, String[] labels) {
        super(DEFAULT_CONTEXT, typeId, TmfEventField.makeRoot(labels));
    }
    
    /**
     * Constructor with parameters.<p>
     * 
     * @param thisTracefileName		Tracefile (channel) name in Ltt
     * @param thisMarkerName 		Marker name in LTT
     * @param thisMarkerfieldsName  MarkerFields related to this marker	
     */
    public LttngEventType(String thisTracefileName, Long thisCpuId, String thisMarkerName, int thisMarkerId, String[] thisMarkerfieldsName) {
        super(DEFAULT_CONTEXT, thisTracefileName + "/" + thisCpuId + "/" + thisMarkerName,  TmfEventField.makeRoot(thisMarkerfieldsName)); //$NON-NLS-1$ //$NON-NLS-2$
        
        tracefileName   = thisTracefileName;
        cpuId           = thisCpuId;
        markerName      = thisMarkerName;
        markerId        = thisMarkerId;
    }

    /**
     * Copy constructor.<p>
     * 
     * @param oldType   Type we want to copy from
     */
    public LttngEventType(LttngEventType oldType) {
        this(oldType.tracefileName, oldType.cpuId, oldType.markerName, oldType.markerId, oldType.getFieldNames());
    }
    
    
    public String getTracefileName() {
        return tracefileName;
    }
    
    public Long getCpuId() {
        return cpuId;
    }
    
    public String getMarkerName() {
        return markerName;
    }
    
    public int getMarkerId() {
        return markerId;
    }
    
    /**
     * toString() method.
     * 
     * @return TypeId (channel/marker) of the object
     */
    @Override
    @SuppressWarnings("nls")
	public String toString() {
        // *** TODO ***
        // This is used as-it in the events view, so we won't change its format.
        //  ...but maybe we should?
        return tracefileName + "/" + cpuId.toString() + "/" + markerName;
    }

    @Override
	public LttngEventType clone() {
    	LttngEventType clone = (LttngEventType) super.clone();
		clone.tracefileName = tracefileName;
		clone.cpuId         = Long.valueOf(cpuId);
		clone.markerName    = markerName;
		clone.markerId      = markerId;
    	return clone;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((cpuId == null) ? 0 : cpuId.hashCode());
        result = prime * result + markerId;
        result = prime * result + ((markerName == null) ? 0 : markerName.hashCode());
        result = prime * result + ((tracefileName == null) ? 0 : tracefileName.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof LttngEventType)) {
            return false;
        }
        LttngEventType other = (LttngEventType) obj;
        if (cpuId == null) {
            if (other.cpuId != null) {
                return false;
            }
        } else if (!cpuId.equals(other.cpuId)) {
            return false;
        }
        if (markerId != other.markerId) {
            return false;
        }
        if (markerName == null) {
            if (other.markerName != null) {
                return false;
            }
        } else if (!markerName.equals(other.markerName)) {
            return false;
        }
        if (tracefileName == null) {
            if (other.tracefileName != null) {
                return false;
            }
        } else if (!tracefileName.equals(other.tracefileName)) {
            return false;
        }
        return true;
    }

}
