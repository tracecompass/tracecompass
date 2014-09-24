/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.signal;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * Signal indicating a trace event has been selected.
 *
 * The specified event has been selected.
 *
 * @author Patrick Tasse
 * @since 3.2
 */
public class TmfEventSelectedSignal extends TmfSignal {

    private final ITmfEvent fEvent;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param event
     *            The event that was selected
     */
    public TmfEventSelectedSignal(Object source, ITmfEvent event) {
        super(source);
        fEvent = event;
    }

    /**
     * @return The event referred to by this signal
     */
    public ITmfEvent getEvent() {
        return fEvent;
    }

    @Override
    public String toString() {
        return "[TmfEventSelectedSignal (" + fEvent.toString() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
