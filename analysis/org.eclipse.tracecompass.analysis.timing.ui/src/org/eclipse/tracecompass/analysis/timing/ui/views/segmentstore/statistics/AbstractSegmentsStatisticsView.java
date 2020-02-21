/*******************************************************************************
 * Copyright (c) 2015, 2017 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics;

import java.io.OutputStream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.ui.commands.ExportToTsvAction;
import org.eclipse.tracecompass.internal.tmf.ui.commands.ExportToTsvUtils;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractTmfTreeViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

import com.google.common.annotations.VisibleForTesting;

/**
 * Abstract view to to be extended to display segment store statistics.
 *
 * @author Bernd Hufmann
 * @author Geneviève Bastien
 * @since 1.3
 */
public abstract class AbstractSegmentsStatisticsView extends TmfView {

    private final Action fExportAction = new ExportToTsvAction() {
        @Override
        protected void exportToTsv(@Nullable OutputStream stream) {
            AbstractSegmentsStatisticsView.this.exportToTsv(stream);
        }

        @Override
        protected @Nullable Shell getShell() {
            return getViewSite().getShell();
        }

    };

    private @Nullable AbstractTmfTreeViewer fStatsViewer = null;

    /**
     * Constructor
     */
    public AbstractSegmentsStatisticsView() {
        super("StatisticsView"); //$NON-NLS-1$
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);
        AbstractTmfTreeViewer statsViewer = createSegmentStoreStatisticsViewer(NonNullUtils.checkNotNull(parent));
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            statsViewer.loadTrace(trace);
        }
        fStatsViewer = statsViewer;
        getViewSite().getActionBars().getMenuManager().add(fExportAction);
    }

    @Override
    public void setFocus() {
        AbstractTmfTreeViewer statsViewer = fStatsViewer;
        if (statsViewer != null) {
            statsViewer.getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        AbstractTmfTreeViewer statsViewer = fStatsViewer;
        if (statsViewer != null) {
            statsViewer.dispose();
        }
    }

    /**
     * Creates a segment store statistics viewer instance.
     *
     * @param parent
     *            the parent composite to create the viewer in.
     * @return the latency statistics viewer implementation
     */
    protected abstract AbstractTmfTreeViewer createSegmentStoreStatisticsViewer(Composite parent);

    /**
     * Export a given items's TSV
     *
     * @param stream
     *            an output stream to write the TSV to
     * @since 1.2
     */
    @VisibleForTesting
    protected void exportToTsv(@Nullable OutputStream stream) {
        AbstractTmfTreeViewer statsViewer = fStatsViewer;
        if (statsViewer == null) {
            return;
        }
        Tree tree = statsViewer.getTreeViewer().getTree();
        ExportToTsvUtils.exportTreeToTsv(tree, stream);
    }
}
