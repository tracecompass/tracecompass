/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.profiling.ui.callgraph.statistics;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.profiling.ui.symbols.TmfSymbolProviderUpdatedSignal;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics.AbstractSegmentsStatisticsView;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics.AbstractSegmentsStatisticsViewer;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;

import com.google.common.annotations.VisibleForTesting;

/**
 * View to display Function Duration statistics.
 *
 * @author Matthew Khouzam
 *
 */
public class CallGraphStatisticsView extends AbstractSegmentsStatisticsView {

    /** The view ID */
    public static final String ID = CallGraphStatisticsView.class.getPackage().getName() + ".callgraphStatistics"; //$NON-NLS-1$
    private @Nullable CallGraphStatisticsViewer fViewer;

    /**
     * Constructor
     */
    public CallGraphStatisticsView() {
        super();
        TmfSignalManager.register(this);
    }

    @Override
    protected AbstractSegmentsStatisticsViewer createSegmentStoreStatisticsViewer(Composite parent) {
        fViewer = new CallGraphStatisticsViewer(parent);
        return fViewer;
    }

    /**
     * Symbol map provider updated
     *
     * @param signal
     *            the signal
     */
    @TmfSignalHandler
    public void symbolMapUpdated(TmfSymbolProviderUpdatedSignal signal) {
        if (signal.getSource() != this) {
            CallGraphStatisticsViewer viewer = fViewer;
            if (viewer != null) {
                viewer.refresh();
            }
        }
    }

    /**
     * Get the current tree viewer
     *
     * @return the current tree viewer
     */
    @VisibleForTesting
    public @Nullable CallGraphStatisticsViewer getTreeViewer() {
        return fViewer;
    }

}