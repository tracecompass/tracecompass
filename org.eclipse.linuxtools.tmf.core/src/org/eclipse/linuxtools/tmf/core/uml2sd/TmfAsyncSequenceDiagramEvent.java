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
package org.eclipse.linuxtools.tmf.core.uml2sd;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * <p>
 * A basic implementation of ITmfAsyncSequenceDiagramEvent.
 * </p>
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public class TmfAsyncSequenceDiagramEvent extends TmfSyncSequenceDiagramEvent implements ITmfAsyncSequenceDiagramEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The end time of the sequence diagram event (i.e. time when signal was received).
     */
    final protected ITmfTimestamp fEndTime;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param startEvent The start event (on sender side).
     * @param endEvent The end event (receiver side).
     * @param sender The name of sender of signal.
     * @param receiver The Name of receiver of signal.
     * @param name - The signal name
     */
    public TmfAsyncSequenceDiagramEvent(ITmfEvent startEvent, ITmfEvent endEvent, String sender, String receiver, String name) {
        super(startEvent, sender, receiver, name);

        if (endEvent == null) {
            throw new IllegalArgumentException("TmfAsyncSequenceDiagramEvent constructor: endEvent=null"); //$NON-NLS-1$
        }
        fEndTime = endEvent.getTimestamp();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public ITmfTimestamp getEndTime() {
        return fEndTime;
    }
}
