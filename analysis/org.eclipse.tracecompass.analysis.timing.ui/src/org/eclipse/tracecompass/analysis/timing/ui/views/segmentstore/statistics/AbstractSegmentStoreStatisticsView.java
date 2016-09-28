/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.ExportToTsvAction;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;

/**
 * Abstract view to to be extended to display segment store statistics.
 *
 * @author Bernd Hufmann
 *
 */
public abstract class AbstractSegmentStoreStatisticsView extends TmfView {

    private final Action fExportAction = new ExportToTsvAction() {
        @Override
        protected void exportToTsv(@Nullable OutputStream stream) {
            AbstractSegmentStoreStatisticsView.this.exportToTsv(stream);
        }

        @Override
        protected @Nullable Shell getShell() {
            return getViewSite().getShell();
        }

    };

    private @Nullable AbstractSegmentStoreStatisticsViewer fStatsViewer = null;

    /**
     * Constructor
     */
    public AbstractSegmentStoreStatisticsView() {
        super("StatisticsView"); //$NON-NLS-1$
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);
        AbstractSegmentStoreStatisticsViewer statsViewer = createSegmentStoreStatisticsViewer(NonNullUtils.checkNotNull(parent));
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            statsViewer.loadTrace(trace);
        }
        fStatsViewer = statsViewer;
        getViewSite().getActionBars().getMenuManager().add(fExportAction);
    }

    @Override
    public void setFocus() {
        AbstractSegmentStoreStatisticsViewer statsViewer = fStatsViewer;
        if (statsViewer != null) {
            statsViewer.getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        AbstractSegmentStoreStatisticsViewer statsViewer = fStatsViewer;
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
    protected abstract AbstractSegmentStoreStatisticsViewer createSegmentStoreStatisticsViewer(Composite parent);

    /**
     * Export a given items's TSV
     *
     * @param stream
     *            an output stream to write the TSV to
     * @since 1.2
     */
    @VisibleForTesting
    protected void exportToTsv(@Nullable OutputStream stream) {
        try (PrintWriter pw = new PrintWriter(stream)) {
            AbstractSegmentStoreStatisticsViewer statsViewer = fStatsViewer;
            if (statsViewer == null) {
                return;
            }
            Tree tree = statsViewer.getTreeViewer().getTree();
            int size = tree.getItemCount();
            List<String> columns = new ArrayList<>();
            for (int i = 0; i < tree.getColumnCount(); i++) {
                String valueOf = String.valueOf(tree.getColumn(i).getText());
                if (valueOf.isEmpty() && i == tree.getColumnCount() - 1) {
                    // Linux "feature", an invisible column is added at the end
                    // with gtk2
                    break;
                }
                columns.add(valueOf);
            }
            String join = Joiner.on('\t').skipNulls().join(columns);
            pw.println(join);
            for (int i = 0; i < size; i++) {
                TreeItem item = tree.getItem(i);
                printItem(pw, columns, item);
            }
        }
    }

    private void printItem(PrintWriter pw, List<String> columns, @Nullable TreeItem item) {
        if (item == null) {
            return;
        }
        List<String> data = new ArrayList<>();
        for (int col = 0; col < columns.size(); col++) {
            data.add(String.valueOf(item.getText(col)));
        }
        pw.println(Joiner.on('\t').join(data));
        for (TreeItem child : item.getItems()) {
            printItem(pw, columns, child);
        }
    }

}
