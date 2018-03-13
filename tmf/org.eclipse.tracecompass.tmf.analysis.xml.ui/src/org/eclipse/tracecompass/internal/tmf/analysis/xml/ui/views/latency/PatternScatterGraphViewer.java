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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter.AbstractSegmentStoreScatterChartViewer;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;

/**
 * Displays the latency analysis data in a scatter graph
 *
 * @author Jean-Christian Kouame
 */
public class PatternScatterGraphViewer extends AbstractSegmentStoreScatterChartViewer {

    private @NonNull String fAnalysisId = ""; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param title
     *            The view title
     * @param xLabel
     *            The x axis label
     * @param yLabel
     *            The y axis label
     */
    public PatternScatterGraphViewer(@NonNull Composite parent, @NonNull String title, @NonNull String xLabel, @NonNull String yLabel) {
        super(parent, new TmfXYChartSettings(title, xLabel, yLabel, 1));
    }

    @Override
    protected @NonNull String getAnalysisId() {
        return fAnalysisId;
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
            initializeDataProvider(trace);
        }
    }
}
