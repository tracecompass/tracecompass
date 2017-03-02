/*******************************************************************************
 * Copyright (c) 2017 Ericsson
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

import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerConfigXmlParser;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSet;
import org.eclipse.tracecompass.tmf.core.trace.AbstractTmfTraceAdapterFactory;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEventSource;

/**
 * Configurable marker event source factory.
 */
public class ConfigurableMarkerEventSourceFactory extends AbstractTmfTraceAdapterFactory {

    @Override
    protected <T> T getTraceAdapter(ITmfTrace trace, Class<T> adapterType) {
        if (IMarkerEventSource.class.equals(adapterType)) {
            ConfigurableMarkerEventSource adapter = new ConfigurableMarkerEventSource(trace);
            configure(adapter);
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

    private static void configure(ConfigurableMarkerEventSource source) {
        String defaultMarkerSetId = MarkerUtils.getDefaultMarkerSetId();
        for (MarkerSet markerSet : MarkerConfigXmlParser.getMarkerSets()) {
            if (markerSet.getId().equals(defaultMarkerSetId)) {
                source.configure(markerSet);
            }
        }
    }
}
