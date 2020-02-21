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

import org.eclipse.core.resources.IFile;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Signal indicating a trace has been opened.
 *
 * Receivers can get ready to receive TmfTraceRangeUpdatedSignal for coalescing
 * and can expect TmfTraceSelectedSignal to follow.
 *
 * @author Patrick Tasse
 */
public class TmfTraceOpenedSignal extends TmfSignal {

    private final ITmfTrace fTrace;
    private final IFile fEditorFile;

    /**
     * Constructor for a new signal.
     *
     * @param source
     *            The object sending this signal
     * @param trace
     *            The trace that has been opened
     * @param editorFile
     *            Pointer to the editor file
     */
    public TmfTraceOpenedSignal(Object source, ITmfTrace trace, IFile editorFile) {
        super(source);
        fTrace = trace;
        fEditorFile = editorFile;
    }

    /**
     * Get the trace object concerning this signal
     *
     * @return The trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Get a pointer to the editor file for this trace
     *
     * @return The IFile object
     */
    public IFile getEditorFile() {
        return fEditorFile;
    }

    @Override
    public String toString() {
        return "[TmfTraceOpenedSignal (" + fTrace.getName() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
