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

package org.eclipse.linuxtools.lttng.event;

import org.eclipse.linuxtools.tmf.event.*;

/**
 * <b><u>LttngEventType</u></b><p>
 * 
 * Lttng specific implementation of the TmfEventType.<p>
 * 
 * This implementation add some attributes to the basic Tmf object.
 */
public class LttngEventType extends TmfEventType {
    
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
     * Constructor with parameters.<p>
     * 
     * @param thisTracefileName		Tracefile (channel) name in Ltt
     * @param thisMarkerName 		Marker name in LTT
     * @param thisMarkerfieldsName  MarkerFields related to this marker	
     */
    public LttngEventType(String thisTracefileName, Long thisCpuId, String thisMarkerName, int thisMarkerId, String[] thisMarkerfieldsName) {
        super( thisTracefileName + "/" + thisCpuId + "/" + thisMarkerName, thisMarkerfieldsName); //$NON-NLS-1$ //$NON-NLS-2$
        
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
        this(oldType.tracefileName, oldType.cpuId, oldType.markerName, oldType.markerId, oldType.getLabels());
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
		clone.tracefileName = new String(tracefileName);
		clone.cpuId         = new Long(cpuId);
		clone.markerName    = new String(markerName);
		clone.markerId      = markerId;
    	return clone;
    }

}
