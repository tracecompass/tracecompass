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
 * <b><u>TmfSyncSequenceDiagramEvent</u></b>
 * <p>
 * Sample implementation of the ITmfSyncSequenceDiagramEvent
 * </p>
 */
public class TmfSyncSequenceDiagramEvent implements ITmfSyncSequenceDiagramEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    final protected ITmfTimestamp fStartTime;
    final protected String fSender;
    final protected String fReceiver;
    final protected String fName;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------    
    public TmfSyncSequenceDiagramEvent(TmfEvent startEvent, String sender, String receiver, String name) {

        if (startEvent == null || sender == null || receiver == null || name == null) {
            throw new IllegalArgumentException("TmfSyncSequenceDiagramEvent constructor: " +  //$NON-NLS-1$
                    (startEvent == null ? ", startEvent=null" : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    (sender == null ? ", sender=null" : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    (receiver == null ? ", receiver=null" : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    (name == null ? ", name=null" : "")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        fStartTime = startEvent.getTimestamp().clone();

        fSender = sender;
        fReceiver = receiver;
        
        fName = name;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.uml2sd.ITmfSyncSequenceDiagramEvent#getSender()
     */
    @Override
    public String getSender() {
        return fSender;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.uml2sd.ITmfSyncSequenceDiagramEvent#getReceiver()
     */
    @Override
    public String getReceiver() {
        return fReceiver;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.uml2sd.ITmfSyncSequenceDiagramEvent#getName()
     */
    @Override
    public String getName() {
        return fName;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.uml2sd.ITmfSyncSequenceDiagramEvent#getStartTime()
     */
    @Override
    public ITmfTimestamp getStartTime() {
        return fStartTime;
    }
}
