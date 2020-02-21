/**********************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.uml2sd;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * <p>
 * A basic implementation of ITmfAsyncSequenceDiagramEvent.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TmfAsyncSequenceDiagramEvent extends TmfSyncSequenceDiagramEvent implements ITmfAsyncSequenceDiagramEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The end time of the sequence diagram event (i.e. time when signal was received).
     */
    private final ITmfTimestamp fEndTime;

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

    @Override
    public ITmfTimestamp getEndTime() {
        return fEndTime;
    }
}
