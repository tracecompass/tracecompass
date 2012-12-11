/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

import org.eclipse.core.resources.IFile;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Signal indicating a trace has been opened.
 *
 * Receivers can get ready to receive TmfTraceRangeUpdatedSignal for coalescing
 * and can expect TmfTraceSelectedSignal to follow.
 *
 * @version 1.0
 * @author Patrick Tasse
 * @since 2.0
 */
public class TmfTraceOpenedSignal extends TmfSignal {

    private final ITmfTrace fTrace;
    private final IFile fBookmarksFile;

    /**
     * Constructor for a new signal.
     *
     * @param source
     *            The object sending this signal
     * @param trace
     *            The trace that has been opened
     * @param bookmarksFile
     *            Pointer to the bookmark file
     */
    public TmfTraceOpenedSignal(Object source, ITmfTrace trace, IFile bookmarksFile) {
        super(source);
        fTrace = trace;
        fBookmarksFile = bookmarksFile;
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
     * Get a pointer to the bookmark file of this trace
     *
     * @return The IFile object
     */
    public IFile getBookmarksFile() {
        return fBookmarksFile;
    }

    @Override
    public String toString() {
        return "[TmfTraceOpenedSignal (" + fTrace.getName() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
