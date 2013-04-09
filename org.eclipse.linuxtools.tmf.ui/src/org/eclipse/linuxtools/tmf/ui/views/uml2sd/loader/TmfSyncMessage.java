/**********************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.loader;

import org.eclipse.linuxtools.tmf.core.uml2sd.ITmfSyncSequenceDiagramEvent;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessage;


/**
 * <p>
 * Extends SyncMessage class to provide additional information about the trace event.
 * </p>
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public class TmfSyncMessage extends SyncMessage implements ITmfSyncSequenceDiagramEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * A synchronous sequence diagram event implementation
     */
    protected ITmfSyncSequenceDiagramEvent fSdEvent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param sdEvent The synchronous sequence diagram event implementation
     * @param eventOccurrence The event index
     */
    public TmfSyncMessage(ITmfSyncSequenceDiagramEvent sdEvent, int eventOccurrence) {
        this.fSdEvent = sdEvent;
        setEventOccurrence(eventOccurrence);
        setName(sdEvent.getName());
        setTime(sdEvent.getStartTime());
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public String getSender() {
        return fSdEvent.getSender();
    }

    @Override
    public String getReceiver() {
        return fSdEvent.getReceiver();
    }
}
