/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.signal;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;

/**
 * Signal indicating the trace parser has finished reading a chunk, so the
 * parsing has been updated.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfTraceParserUpdatedSignal extends TmfSignal {

    private final IResource fTraceResource;

    /**
     * Constructor for the signal.
     *
     * @param source The object sending this signal
     * @param traceResource The trace resource concerning this signal
     */
    public TmfTraceParserUpdatedSignal(Object source, IResource traceResource) {
        super(source);
        fTraceResource = traceResource;
    }

    /**
     * Get the trace resource object of this signal.
     *
     * @return The trace resource.
     */
    public IResource getTraceResource() {
        return fTraceResource;
    }

    @Override
    public String toString() {
        return "[TmfTraceParserUpdatedSignal (" + fTraceResource.getName() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
