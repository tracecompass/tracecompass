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
 * <b><u>LttngEventSource</u></b><p>
 * 
 * Lttng specific implementation of the TmfEventSource
 */
public class LttngEventSource extends TmfEventSource {
    
    /**
     * Default Constructor.<p>
     * 
     */
    public LttngEventSource() {
        super();
    }
    
    /**
     * Copy Constructor.<p>
     * 
     * @param newSource  Source of the event as string.
     */
    public LttngEventSource(String newSource) {
        super(newSource);
    }
    
    
    /**
     * Copy Constructor.<p>
     * 
     * @param oldSource  LttngEventSource to copy from.
     */
    public LttngEventSource(LttngEventSource oldSource) {
        this( (String)oldSource.getSourceId() );
    }
    
    
    public String getSourceId() {
        return (String)fSourceId;
    }
    
    public void setSourceId(String newSource) {
        fSourceId = newSource;
    }
    
    public String toString() {
        return fSourceId.toString();
    }
}
