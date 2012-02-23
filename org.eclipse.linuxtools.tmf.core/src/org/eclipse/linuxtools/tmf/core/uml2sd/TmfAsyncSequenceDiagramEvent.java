/**********************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.tmf.core.uml2sd;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;

/**
 * <b><u>TmfAsyncSequenceDiagramEvent</u></b>
 * <p>
 * Sample implementation of the ITmfAsyncSequenceDiagramEvent
 * </p>
 */
public class TmfAsyncSequenceDiagramEvent extends TmfSyncSequenceDiagramEvent implements ITmfAsyncSequenceDiagramEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    final protected ITmfTimestamp fEndTime;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------    
    public TmfAsyncSequenceDiagramEvent(TmfEvent startEvent, TmfEvent endEvent, String sender, String receiver, String name) {
        super(startEvent, sender, receiver, name);
        
        if (endEvent == null) {
            throw new IllegalArgumentException("TmfAsyncSequenceDiagramEvent constructor: endEvent=null"); //$NON-NLS-1$
        }
        fEndTime = endEvent.getTimestamp().clone(); 
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------    

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.uml2sd.ITmfAsyncSequenceDiagramEvent#getEndTime()
     */
    @Override
    public ITmfTimestamp getEndTime() {
        return fEndTime;
    }
}
