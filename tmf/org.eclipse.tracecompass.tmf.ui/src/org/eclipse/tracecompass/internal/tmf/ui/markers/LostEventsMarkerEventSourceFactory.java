/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.markers;

import org.eclipse.tracecompass.tmf.core.trace.AbstractTmfTraceAdapterFactory;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEventSource;

/**
 * Marker event source factory for lost events.
 */
public class LostEventsMarkerEventSourceFactory extends AbstractTmfTraceAdapterFactory {

    @Override
    protected <T> T getTraceAdapter(ITmfTrace trace, Class<T> adapterType) {
        if (IMarkerEventSource.class.equals(adapterType)) {
            IMarkerEventSource adapter = new LostEventsMarkerEventSource(trace);
            return adapterType.cast(adapter);
        }
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] {
                IMarkerEventSource.class
        };
    }
}
