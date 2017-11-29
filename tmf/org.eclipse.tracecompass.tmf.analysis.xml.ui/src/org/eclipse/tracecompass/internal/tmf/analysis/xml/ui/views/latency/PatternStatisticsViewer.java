/*******************************************************************************
 * Copyright (c) 2016, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics.AbstractSegmentsStatisticsViewer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternLatencyStatisticsAnalysis;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A tree viewer implementation for displaying pattern latency statistics
 *
 * @author Jean-Christian Kouame
 */
public class PatternStatisticsViewer extends AbstractSegmentsStatisticsViewer {

    private String fAnalysisId;

    private static final @NonNull String PATTERN_SEGMENTS_LEVEL = "Pattern Segments"; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     */
    public PatternStatisticsViewer(@NonNull Composite parent) {
        super(parent);
    }

    @Override
    protected @Nullable TmfAbstractAnalysisModule createStatisticsAnalysiModule() {
        return new XmlPatternLatencyStatisticsAnalysis(fAnalysisId);
    }

    @Override
    protected @NonNull String getTypeLabel() {
        return PATTERN_SEGMENTS_LEVEL;
    }

    /**
     * Set the analysis ID and update the view
     *
     * @param analysisId
     *            The analysis ID
     */
    public void updateViewer(String analysisId) {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return;
        }
        if (analysisId != null) {
            fAnalysisId = analysisId;
            initializeDataSource(trace);
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!trace.equals(getTrace())) {
                        return;
                    }
                    clearContent();
                    updateContent(getWindowStartTime(), getWindowEndTime(), false);
                }
            });
        }
    }

}
