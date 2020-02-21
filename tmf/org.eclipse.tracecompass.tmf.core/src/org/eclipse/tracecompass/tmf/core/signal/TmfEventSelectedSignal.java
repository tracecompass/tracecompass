/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Signal indicating a trace event has been selected.
 *
 * The specified event has been selected.
 *
 * @author Patrick Tasse
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
