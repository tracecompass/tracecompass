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
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceAdapterManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

/**
 * Trace Annotation Provider to provide annotations for a given trace.
 *
 * It gets all instances of {@link IOutputAnnotationProvider} registered
 * {@link TmfTraceAdapterManager} for trace instances and provides
 * annotation categories and annotations globally for a trace. They can
 * be fetched on top of data provider's annotations.
 *
 * @author Bernd Hufmann
 *
 */
public class TraceAnnotationProvider implements IOutputAnnotationProvider {

    private CopyOnWriteArrayList<IOutputAnnotationProvider> fTraceAnnotations = new CopyOnWriteArrayList<>();


    /**
     * Constructor
     *
     * @param trace
     *            the trace to construct the trace annotation provider with.
     */

    public TraceAnnotationProvider(ITmfTrace trace) {
        init(trace);
    }

    private void init(ITmfTrace trace) {
        if (trace instanceof TmfExperiment) {
            @NonNull Collection<@NonNull ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
            for (ITmfTrace child : traces) {
                init(child);
            }
            return;
        }
        List<@NonNull IOutputAnnotationProvider> adapters = TmfTraceAdapterManager.getAdapters(trace, IOutputAnnotationProvider.class);
        fTraceAnnotations.addAll(adapters);
    }

    @Override
    public TmfModelResponse<AnnotationCategoriesModel> fetchAnnotationCategories(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        AnnotationCategoriesModel model = new AnnotationCategoriesModel(Collections.emptyList());
        for (IOutputAnnotationProvider dataProvider : fTraceAnnotations) {
            TmfModelResponse<AnnotationCategoriesModel> response = dataProvider.fetchAnnotationCategories(fetchParameters, monitor);
            model = AnnotationCategoriesModel.of(model, response.getModel());
        }
        if (model.getAnnotationCategories().isEmpty()) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        return new TmfModelResponse<>(model, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public TmfModelResponse<AnnotationModel> fetchAnnotations(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        boolean isComplete = true;
        AnnotationModel model = new AnnotationModel(Collections.emptyMap());
        for (IOutputAnnotationProvider dataProvider : fTraceAnnotations) {
            TmfModelResponse<AnnotationModel> response = dataProvider.fetchAnnotations(fetchParameters, monitor);
            isComplete &= response.getStatus() == ITmfResponse.Status.COMPLETED;
            model = AnnotationModel.of(model, response.getModel());
            if (monitor != null && monitor.isCanceled()) {
                return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
            }
        }
        if (isComplete) {
            return new TmfModelResponse<>(model, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        return new TmfModelResponse<>(model, ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }
}
