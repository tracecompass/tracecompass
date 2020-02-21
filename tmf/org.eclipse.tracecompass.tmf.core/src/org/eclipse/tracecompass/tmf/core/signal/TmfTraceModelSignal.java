/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

/**
 * Base class for TMF signals for a given host, broadcast in a context that
 * involves the trace itself or rather the system model represented by the trace
 * (CPU, thread), as opposed to other signals related to the traceCompass
 * signals (actions on trace, times, analyses).
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public abstract class TmfTraceModelSignal extends TmfSignal {

    private final String fHostId;

    /**
     * Standard constructor
     *
     * @param source
     *            Object sending this signal
     * @param reference
     *            Reference index to assign to this signal
     * @param host
     *            the host id
     */
    public TmfTraceModelSignal(Object source, int reference, String host) {
        super(source, reference);
        fHostId = host;
    }

    /**
     * Gets a trace host id
     *
     * @return the host id
     */
    public String getHostId() {
        return fHostId;
    }

}
