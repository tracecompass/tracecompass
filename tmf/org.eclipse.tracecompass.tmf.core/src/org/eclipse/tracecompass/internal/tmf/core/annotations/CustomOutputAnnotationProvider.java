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
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IOutputAnnotationProvider;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerConfigXmlParser;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSet;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 * Trace Annotation provider for providing custom (frame) annotations. It
 * encapsulates the individual annotation providers per marker set.
 */
public class CustomOutputAnnotationProvider implements IOutputAnnotationProvider {

    private static final String INVALID_MARKER_ID = "Invalid marker ID %s"; //$NON-NLS-1$
    private static final String NO_MARKER_ID = "no markerID"; //$NON-NLS-1$
    private final Map<String, CustomAnnotationProvider> fProviders = Collections.synchronizedMap(new HashMap<>());
    private final ITmfTrace fTrace;

    static {
        MarkerConfigXmlParser.initMarkerSets();
    }

    /**
     * Constructor
     */
    public CustomOutputAnnotationProvider(ITmfTrace trace) {
        fTrace = trace;
    }

    @Override
    public TmfModelResponse<AnnotationCategoriesModel> fetchAnnotationCategories(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        Object markerID = fetchParameters.get(DataProviderParameterUtils.REQUESTED_MARKER_SET_KEY);
        /* Ignore if trace is not the first element of its trace set. */
        if (!isFirstTrace()) {
            return new TmfModelResponse<>(null, Status.COMPLETED, ""); //$NON-NLS-1$
        }
        if (markerID == null) {
            return new TmfModelResponse<>(null, Status.FAILED, NO_MARKER_ID);
        }
        MarkerSet ms = getMarkerSet(markerID);
        if (ms == null) {
            return new TmfModelResponse<>(null, Status.FAILED, formatError(markerID));
        }
        return getAnnotationProvider(null, ms).fetchAnnotationCategories(fetchParameters, monitor);
    }

    @Override
    public TmfModelResponse<AnnotationModel> fetchAnnotations(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        Object markerID = fetchParameters.get(DataProviderParameterUtils.REQUESTED_MARKER_SET_KEY);
        Object hostID = fetchParameters.get(DataProviderParameterUtils.REQUESTED_TRACE_KEY);
        Optional<ITmfTrace> requestedTrace = getTrace(hostID);
        /*
         * Ignore if trace is not the requested trace, or if no requested trace,
         * if the trace is not the first element of its trace set.
         */
        if ((requestedTrace.isPresent() && !requestedTrace.get().equals(fTrace)) ||
                (!requestedTrace.isPresent() && !isFirstTrace())) {
            return new TmfModelResponse<>(null, Status.COMPLETED, ""); //$NON-NLS-1$
        }
        if (markerID == null) {
            return new TmfModelResponse<>(null, Status.FAILED, NO_MARKER_ID);
        }
        MarkerSet ms = getMarkerSet(markerID);
        if (ms == null) {
            return new TmfModelResponse<>(null, Status.FAILED, formatError(markerID));
        }
        return getAnnotationProvider(requestedTrace.isPresent() ? requestedTrace.get() : null, ms).fetchAnnotations(fetchParameters, monitor);
    }

    private CustomAnnotationProvider getAnnotationProvider(@Nullable ITmfTrace trace, MarkerSet ms) {
        return fProviders.computeIfAbsent(ms.getId(), msId -> new CustomAnnotationProvider(trace, ms));
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

    /*
     * Returns true if, for any opened trace, this provider's trace is the first
     * element in the opened trace's trace set.
     */
    private boolean isFirstTrace() {
        return TmfTraceManager.getInstance().getOpenedTraces().stream()
                .anyMatch(t -> TmfTraceManager.getTraceSet(t).stream().findFirst().get().equals(fTrace));
    }

    private static String formatError(Object markerID) {
        return String.format(INVALID_MARKER_ID, markerID);
    }
}
