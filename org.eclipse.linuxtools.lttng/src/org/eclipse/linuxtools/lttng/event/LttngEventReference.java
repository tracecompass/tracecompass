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
 * <b><u>LttngEventReference</u></b>
 * <p>
 * Lttng specific implementation of the TmfEventReference
 * <p>
 * The Lttng implementation is the same as the basic Tmf Implementation but allow construction with a String
 */
public class LttngEventReference extends TmfEventReference {
    
    private String tracepath = ""; 
    
    /**
     * Constructor with parameters
     * 
     * @param referencePath  A string that will be our reference
     */
    public LttngEventReference(String newTracefilePath, String newTracePath) {
        super(newTracefilePath);
        
        // Save the path of the trace 
        tracepath = newTracePath;
    }
    
    /**
     * Copy Constructor
     * 
     * @param oldReference  Reference to copy
     */
    public LttngEventReference(LttngEventReference oldReference) {
        this( oldReference.getValue().toString(), oldReference.getTracepath() );
    }
    
    
    public String getTracepath() {
        return tracepath;
    }
    
    public void setTracepath(String tracepath) {
        this.tracepath = tracepath;
    }
    
    
    @Override
	public String toString() {
    	return tracepath + " " + this.getValue();
    }
    
}
