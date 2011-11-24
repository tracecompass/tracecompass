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
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.impl;

/**
 * <b><u>TmfSyncMessages</u></b>
 * <p>
 * Extends AsyncMessage class to provide additional information about the trace event.
 * </p>
 */
import org.eclipse.linuxtools.tmf.core.uml2sd.ITmfSyncSequenceDiagramEvent;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessage;

public class TmfSyncMessage extends SyncMessage implements ITmfSyncSequenceDiagramEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    protected ITmfSyncSequenceDiagramEvent fSdEvent;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public TmfSyncMessage(ITmfSyncSequenceDiagramEvent sdEvent, int eventOccurrence) {
        this.fSdEvent = sdEvent;
        setEventOccurrence(eventOccurrence);
        setName(sdEvent.getName());
        setTime(sdEvent.getStartTime());
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.ust.examples.event.ISequenceDiagramEvent#getSender()
     */
    @Override
    public String getSender() {
        return fSdEvent.getSender();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.ust.examples.event.ISequenceDiagramEvent#getReceiver()
     */
    @Override
    public String getReceiver() {
        return fSdEvent.getReceiver();
    }
}