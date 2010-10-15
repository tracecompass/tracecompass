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
 * <b><u>LttngEventReference</u></b><p>
 * 
 * Lttng specific implementation of the TmfEventReference
 */
public class LttngEventReference extends TmfEventReference {
    
    private String tracename = ""; 
    
    /**
     * Constructor with parameters.<p>
     * 
     * @param newTraceName      Trace name 
     */
    public LttngEventReference(String newTraceName) {
        super("");
        tracename = newTraceName;
    }
    
    /**
     * Constructor with parameters with optional tracefile path.<p>
     * 
     * @param newTracefilePath  Complete tracefile path
     * @param newTraceName      Trace name 
     */
    public LttngEventReference(String newTracefilePath, String newTraceName) {
        super(newTracefilePath);
        
        // Save the name of the trace 
        tracename = newTraceName;
    }
    
    /**
     * Copy Constructor.<p>
     * 
     * @param oldReference  LttngEventReference to copy from.
     */
    public LttngEventReference(LttngEventReference oldReference) {
        this( oldReference.getValue().toString(), oldReference.getTracepath() );
    }
    
    
    public String getTracepath() {
        return tracename;
    }
    
    public void setTracepath(String tracename) {
        this.tracename = tracename;
    }
    
    public String getValue() {
        return (String)fReference;
    }
    
    public void setValue(String newReference) {
        fReference = newReference;
    }
    
    /**
     * toString() method.<p>
     * 
     * We return only tracename, as it will be used directly in the eventsView.
     *  Returning only tracename gives a better output.
     * 
     * @return tracename as String
     */
    @Override
	public String toString() {
    	return tracename;
    }
    
	@Override
	public LttngEventReference clone() {
		LttngEventReference clone = (LttngEventReference) super.clone();
		clone.fReference = new String((String) fReference);
		clone.tracename  = new String((String) tracename);
		return clone;
	}
}
