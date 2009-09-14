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
 * <b><u>LttngEventType</u></b>
 * <p>
 * Lttng specific implementation of the TmfEventType
 * <p>
 * This implementation add some attributes to the basic Tmf object.
 */
public class LttngEventType extends TmfEventType {
    private String  channelName = "";
    private long    cpuId = 0;
    private String  markerName = "";
    
    /**
     * Constructor with parameters
     * 
     * @param thisChannelName   Channel name. It is the Tracefile name in LTT.
     * @param thisCpuId         CPU id number from LTT
     * @param thisMarkerName    JniMarker name. It is the marker_info name in LTT.
     * @param thisFormat        The format relative to the event
     * @see org.eclipse.linuxtools.lttng.event.LttngEventFormat
     */
    public LttngEventType(String thisChannelName, long thisCpuId, String thisMarkerName, LttngEventFormat thisFormat) {
        super( thisChannelName + "/" + thisCpuId + "/" + thisMarkerName, thisFormat);
        
        channelName = thisChannelName;
        cpuId       = thisCpuId;
        markerName  = thisMarkerName;
    }

    
    public String getChannelName() {
        return channelName;
    }
    
    
    public long getCpuId() {
        return cpuId;
    }
    
    
    public String getMarkerName() {
        return markerName;
    }
    
    
    /**
     * toString() method.
     * 
     * @return String  TypeId of the object
     */
    public String toString() {
        return getTypeId();
    }
}
