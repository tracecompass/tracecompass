/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.SegmentStoreStatistics;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics.AbstractSegmentStoreStatisticsViewer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternLatencyStatisticsAnalysis;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;

/**
 * A tree viewer implementation for displaying pattern latency statistics
 *
 * @author Jean-Christian Kouame
 */
public class PatternStatisticsViewer extends AbstractSegmentStoreStatisticsViewer {

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
    protected ITmfTreeViewerEntry updateElements(long start, long end, boolean isSelection) {
        if (isSelection || (start == end)) {
            return null;
        }

        TmfAbstractAnalysisModule analysisModule = getStatisticsAnalysisModule();

        if (getTrace() == null || !(analysisModule instanceof XmlPatternLatencyStatisticsAnalysis)) {
            return null;
        }

        XmlPatternLatencyStatisticsAnalysis module = (XmlPatternLatencyStatisticsAnalysis) analysisModule;

        module.waitForCompletion();

        TmfTreeViewerEntry root = new TmfTreeViewerEntry(""); //$NON-NLS-1$
        final SegmentStoreStatistics entry = module.getTotalStats();
        if (entry != null) {

            List<ITmfTreeViewerEntry> entryList = root.getChildren();

            TmfTreeViewerEntry child = new SegmentStoreStatisticsEntry(checkNotNull("Total"), entry); //$NON-NLS-1$
            entryList.add(child);
            HiddenTreeViewerEntry segments = new HiddenTreeViewerEntry(PATTERN_SEGMENTS_LEVEL);
            child.addChild(segments);

            final Map<@NonNull String, @NonNull SegmentStoreStatistics> perTypeStats = module.getPerSegmentTypeStats();
            if (perTypeStats != null) {
                for (Entry<@NonNull String, @NonNull SegmentStoreStatistics> statsEntry : perTypeStats.entrySet()) {
                    segments.addChild(new SegmentStoreStatisticsEntry(statsEntry.getKey(), statsEntry.getValue()));
                }
            }
        }
        return root;
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
            initializeDataSource();
        }
    }

}
