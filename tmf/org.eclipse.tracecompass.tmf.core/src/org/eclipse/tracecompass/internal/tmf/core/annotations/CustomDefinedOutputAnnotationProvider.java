/*******************************************************************************
 * Copyright (c) 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.annotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationCategoriesModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.CustomAnnotationProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IOutputAnnotationProvider;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerConfigXmlParser;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSet;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 * Trace Annotation provider for providing custom (frame) annotations. This one
 * is a singleton for all of trace compass, it encapsulates the individual
 * annotation providers per trace.
 */
public class CustomDefinedOutputAnnotationProvider implements IOutputAnnotationProvider {

    private static final String INVALID_MARKER_ID = "Invalid marker ID %s"; //$NON-NLS-1$
    private static final String NO_MARKER_ID = "no markerID"; //$NON-NLS-1$
    private final Map<String, Map<String, CustomAnnotationProvider>> fTraceMarkers = Collections.synchronizedMap(new HashMap<>());

    /**
     * Constructor
     */
    public CustomDefinedOutputAnnotationProvider() {
        MarkerConfigXmlParser.initMarkerSets();
    }

    @Override
    public TmfModelResponse<AnnotationCategoriesModel> fetchAnnotationCategories(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        Object markerID = fetchParameters.get(DataProviderParameterUtils.REQUESTED_MARKER_SET_KEY);
        Object hostID = fetchParameters.get(DataProviderParameterUtils.REQUESTED_TRACE_KEY);
        Optional<ITmfTrace> activeTrace = getTrace(hostID);
        if (markerID == null) {
            return new TmfModelResponse<>(null, Status.FAILED, NO_MARKER_ID);
        }
        MarkerSet ms = getMarkerSet(markerID);
        if (ms != null) {
            return getAnnotationProvider(String.valueOf(hostID), activeTrace.get(), ms).fetchAnnotationCategories(fetchParameters, monitor);
        }
        return new TmfModelResponse<>(null, Status.FAILED, formatError(markerID));
    }

    @Override
    public TmfModelResponse<AnnotationModel> fetchAnnotations(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        Object markerID = fetchParameters.get(DataProviderParameterUtils.REQUESTED_MARKER_SET_KEY);
        Object hostID = fetchParameters.get(DataProviderParameterUtils.REQUESTED_TRACE_KEY);
        Optional<ITmfTrace> activeTrace = getTrace(hostID);
        if (markerID == null) {
            return new TmfModelResponse<>(null, Status.FAILED, NO_MARKER_ID);
        }
        MarkerSet ms = getMarkerSet(markerID);
        if (ms != null) {
            return getAnnotationProvider(String.valueOf(hostID), activeTrace.isPresent() ? activeTrace.get() : null, ms).fetchAnnotations(fetchParameters, monitor);
        }
        return new TmfModelResponse<>(null, Status.FAILED, formatError(markerID));
    }

    private CustomAnnotationProvider getAnnotationProvider(String hostID, @Nullable ITmfTrace activeTrace, MarkerSet ms) {
        Map<String, CustomAnnotationProvider> capMap = fTraceMarkers.computeIfAbsent(hostID, e -> Collections.synchronizedMap(new HashMap<>()));
        return capMap.computeIfAbsent(ms.getId(), a -> new CustomAnnotationProvider(activeTrace, ms));
    }

    private static @Nullable MarkerSet getMarkerSet(Object markerID) {
        Optional<@Nullable MarkerSet> markerOptional = MarkerConfigXmlParser.getMarkerSets().stream()
                .filter(markerSet -> Objects.equals(markerID, markerSet.getId()))
                .findAny();
        if (markerOptional.isPresent()) {
            return markerOptional.get();
        }
        return null;
    }

    private static Optional<ITmfTrace> getTrace(Object hostID) {
        return TmfTraceManager.getInstance().getOpenedTraces().stream().flatMap(trace -> TmfTraceManager.getTraceSetWithExperiment(trace).stream()).filter(t -> Objects.equals(t.getHostId(), hostID)).findAny();
    }

    private static String formatError(Object markerID) {
        return String.format(INVALID_MARKER_ID, markerID);
    }
}
