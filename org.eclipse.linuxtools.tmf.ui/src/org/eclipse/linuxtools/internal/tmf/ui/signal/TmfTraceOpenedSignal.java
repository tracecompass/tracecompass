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

package org.eclipse.linuxtools.internal.tmf.ui.signal;

import org.eclipse.core.resources.IFile;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.viewers.events.ITmfEventsFilterProvider;

/**
 * Signal that a trace was opened
 * 
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfTraceOpenedSignal extends TmfSignal {

    private final ITmfTrace fTrace;
    private final IFile fBookmarksFile;
    private final ITmfEventsFilterProvider fEventsFilterProvider;

    public TmfTraceOpenedSignal(Object source, ITmfTrace trace, IFile bookmarksFile, ITmfEventsFilterProvider eventsFilterProvider) {
        super(source);
        fTrace = trace;
        fBookmarksFile = bookmarksFile;
        fEventsFilterProvider = eventsFilterProvider;
    }

    public ITmfTrace getTrace() {
        return fTrace;
    }

    public IFile getBookmarksFile() {
        return fBookmarksFile;
    }

    public ITmfEventsFilterProvider getEventsFilterProvider() {
        return fEventsFilterProvider;
    }

    @Override
    public String toString() {
        return "[TmfTraceOpenedSignal (" + fTrace.getName() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
