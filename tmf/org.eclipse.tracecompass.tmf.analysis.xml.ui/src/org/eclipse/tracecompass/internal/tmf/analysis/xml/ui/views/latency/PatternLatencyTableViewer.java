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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Displays the latency analysis data in a column table
 *
 * @author Jean-Christian Kouame
 */
public class PatternLatencyTableViewer extends AbstractSegmentStoreTableViewer {

    private String fAnalysisId;

    /**
     * Constructor
     *
     * @param tableViewer
     *            The table viewer
     */
    public PatternLatencyTableViewer(@NonNull TableViewer tableViewer) {
        super(tableViewer);
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
                setData(getSegmentStoreProvider(trace));
            }
        }
    }

}
