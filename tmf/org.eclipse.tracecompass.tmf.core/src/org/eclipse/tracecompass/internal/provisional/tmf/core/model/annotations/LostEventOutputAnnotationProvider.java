/**********************************************************************
 * Copyright (c) 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IAnnotation.AnnotationType;
import org.eclipse.tracecompass.internal.tmf.core.model.annontations.Messages;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStateStatistics.Attributes;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsEventTypesModule;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.TreeMultimap;

/**
 * Trace Annotation provider for providing lost events annotations.
 */
public class LostEventOutputAnnotationProvider implements IOutputAnnotationProvider {

    private static final String LOST_EVENTS = checkNotNull(Messages.TmfCoreModelAnnotations_LostEventsCategory);
    private static final TmfModelResponse<AnnotationModel> NO_DATA = new TmfModelResponse<>(new AnnotationModel(Collections.emptyMap()), Status.COMPLETED, ""); //$NON-NLS-1$

    private final ITmfTrace fTrace;
    private List<Long> fLastRequest = Collections.emptyList();
    private @Nullable AnnotationModel fLastAnnotationModel;

    /**
     * Constructor
     *
     * @param trace
     *            the trace to provide lost events annotations.
     */
    public LostEventOutputAnnotationProvider(ITmfTrace trace) {
        fTrace = trace;
    }

    private static final String COLOR = "#FF0032"; //$NON-NLS-1$

    @Override
    public TmfModelResponse<AnnotationCategoriesModel> fetchAnnotationCategories(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        AnnotationCategoriesModel model = new AnnotationCategoriesModel(Collections.emptyList());
        ITmfStateSystem ss = getStateSystem();
        if (ss != null && getLostEventsQuark(ss) != -1) {
            model = new AnnotationCategoriesModel(Arrays.asList(LOST_EVENTS));
        }
        return new TmfModelResponse<>(model, Status.COMPLETED, ""); //$NON-NLS-1$
    }

    @SuppressWarnings("null")
    @Override
    public TmfModelResponse<AnnotationModel> fetchAnnotations(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        IProgressMonitor progressMonitor = monitor;
        if (progressMonitor == null) {
            progressMonitor = new NullProgressMonitor();
        }
        ITmfStateSystem ss = getStateSystem();
        if (ss == null) {
            return NO_DATA;
        }
        int lostEventsQuark = getLostEventsQuark(ss);
        if (lostEventsQuark == -1) {
            return NO_DATA;
        }

        List<Long> timeRequested = DataProviderParameterUtils.extractTimeRequested(fetchParameters);
        List<@NonNull Long> entries = DataProviderParameterUtils.extractSelectedItems(fetchParameters);
        if (timeRequested == null || timeRequested.size() < 2|| entries == null) {
            return NO_DATA;
        }

        if (timeRequested.equals(fLastRequest)) {
            return new TmfModelResponse<>(fLastAnnotationModel, Status.COMPLETED, ""); //$NON-NLS-1$
        }

        fLastRequest = new ArrayList<>(timeRequested);
        TreeMultimap<String, Annotation> markers = TreeMultimap.create(
                Comparator.naturalOrder(),
                Comparator.comparing(Annotation::getStartTime));
        try {
            long start = Math.max(timeRequested.get(0), ss.getStartTime());
            long end = Math.min(timeRequested.get(timeRequested.size() - 1), ss.getCurrentEndTime());
            if (start <= end) {
                /* Update start to ensure that the previous marker is included. */
                start = Math.max(start - 1, ss.getStartTime());
                /* Update end to ensure that the next marker is included. */
                long nextStartTime = ss.querySingleState(end, lostEventsQuark).getEndTime() + 1;
                end = Math.min(nextStartTime, ss.getCurrentEndTime());

                timeRequested.set(0, start);
                timeRequested.set(timeRequested.size() - 1, end);

                for (ITmfStateInterval interval : ss.query2D(ImmutableList.of(lostEventsQuark), timeRequested)) {
                    if (progressMonitor.isCanceled()) {
                        fLastRequest = Collections.emptyList();
                        fLastAnnotationModel = new AnnotationModel(Collections.emptyMap());
                        return new TmfModelResponse<>(fLastAnnotationModel, Status.CANCELLED, ""); //$NON-NLS-1$
                    }

                    if (interval.getStateValue().isNull()) {
                        continue;
                    }
                    long lostEventsStartTime = interval.getStartTime();
                    /*
                     * The end time of the lost events range is the value of the
                     * attribute, not the end time of the interval.
                     */
                    long lostEventsEndTime = interval.getStateValue().unboxLong();
                    long duration = lostEventsEndTime - lostEventsStartTime;
                    Map<String, Object> style = new HashMap<>();
                    style.put(StyleProperties.COLOR, COLOR);
                    markers.put(LOST_EVENTS, new Annotation(lostEventsStartTime, duration, -1, AnnotationType.CHART, null, new OutputElementStyle(LOST_EVENTS, style)));
                }
            }
        } catch (StateSystemDisposedException e) {
            /* ignored */
        }
        fLastAnnotationModel = new AnnotationModel(markers.asMap());
        return new TmfModelResponse<>(fLastAnnotationModel, Status.COMPLETED, ""); //$NON-NLS-1$
    }

    private @Nullable ITmfStateSystem getStateSystem() {
        TmfStatisticsModule module = TmfTraceUtils.getAnalysisModuleOfClass(fTrace, TmfStatisticsModule.class, TmfStatisticsModule.ID);
        if (module == null) {
            return null;
        }
        return module.getStateSystem(TmfStatisticsEventTypesModule.ID);
    }

    private static int getLostEventsQuark(ITmfStateSystem ss) {
        try {
            return ss.getQuarkAbsolute(Attributes.LOST_EVENTS);
        } catch (AttributeNotFoundException e) {
            return -1;
        }
    }

}
