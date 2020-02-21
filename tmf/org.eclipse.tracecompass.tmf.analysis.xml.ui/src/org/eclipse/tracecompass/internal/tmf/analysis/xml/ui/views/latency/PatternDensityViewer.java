/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityViewer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Displays the latency analysis data in a density chart
 *
 * @author Jean-Christian Kouame
 */
public class PatternDensityViewer extends AbstractSegmentStoreDensityViewer {

    private String fAnalysisId;

    /**
     * Contructor
     *
     * @param parent
     *            The parent composite
     */
    public PatternDensityViewer(@NonNull Composite parent) {
        super(parent);
    }

    @Override
    protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(@NonNull ITmfTrace trace) {
        return fAnalysisId != null ? TmfTraceUtils.getAnalysisModuleOfClass(trace, XmlPatternAnalysis.class, fAnalysisId) : null;
    }

    /**
     * Set the analysis ID and update the view
     *
     * @param analysisId
     *            The analysis ID
     */
    public void updateViewer(String analysisId) {
        if (analysisId != null) {
            fAnalysisId = analysisId;
            final ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
            if (trace != null) {
                loadTrace(trace);
            }
        }
    }
}
