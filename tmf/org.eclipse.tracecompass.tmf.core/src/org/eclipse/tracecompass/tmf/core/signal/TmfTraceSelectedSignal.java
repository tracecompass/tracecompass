/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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

import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Signal indicating a trace has been selected.
 *
 * The specified trace is the active trace and has been brought to top
 * or the signal is used as a trigger to bring it to top.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfTraceSelectedSignal extends TmfSignal {

    private final ITmfTrace fTrace;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param trace
     *            The trace that was selected
     */
    public TmfTraceSelectedSignal(Object source, ITmfTrace trace) {
        super(source);
        fTrace = trace;
    }

    /**
     * @return The trace referred to by this signal
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public String toString() {
        return "[TmfTraceSelectedSignal (" + fTrace.getName() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
