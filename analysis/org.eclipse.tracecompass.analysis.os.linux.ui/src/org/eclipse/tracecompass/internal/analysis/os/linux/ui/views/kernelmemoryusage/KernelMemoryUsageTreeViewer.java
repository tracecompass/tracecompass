/**********************************************************************
 * Copyright (c) 2016, 2017 Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.kernelmemoryusage;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage.KernelMemoryUsageDataProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage.KernelMemoryUsageTreeModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractTmfTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;

import com.google.common.collect.Iterables;

/**
 * Tree viewer to select which process to display in the kernel memory usage
 * chart.
 *
 * @author Mahdi Zolnouri
 * @author Wassim Nasrallah
 * @author Najib Arbaoui
 */
public class KernelMemoryUsageTreeViewer extends AbstractTmfTreeViewer {

    private Long fSelectedEntry = null;
    private static final String[] COLUMN_NAMES = new String[] {
            Messages.KernelMemoryUsageComposite_ColumnTID,
            Messages.KernelMemoryUsageComposite_ColumnProcess
    };

    /** Provides label for the Kernel memory usage tree viewer cells */
    protected static class KernelMemoryLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            KernelMemoryUsageEntry obj = (KernelMemoryUsageEntry) element;
            if (columnIndex == 0) {
                return obj.getModel().getTid();
            } else if (columnIndex == 1) {
                return obj.getName();
            }
            return element.toString();
        }
    }

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite that holds this viewer
     */
    public KernelMemoryUsageTreeViewer(Composite parent) {
        super(parent, false);
        setLabelProvider(new KernelMemoryLabelProvider());
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return new ITmfTreeColumnDataProvider() {

            @Override
            public List<TmfTreeColumnData> getColumnData() {
                /* All columns are sortable */
                List<TmfTreeColumnData> columns = new ArrayList<>();
                TmfTreeColumnData column = new TmfTreeColumnData(COLUMN_NAMES[0]);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        KernelMemoryUsageEntry n1 = (KernelMemoryUsageEntry) e1;
                        KernelMemoryUsageEntry n2 = (KernelMemoryUsageEntry) e2;

                        return n1.getModel().getTid().compareTo(n2.getModel().getTid());
                    }
                });
                columns.add(column);
                column = new TmfTreeColumnData(COLUMN_NAMES[1]);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        KernelMemoryUsageEntry n1 = (KernelMemoryUsageEntry) e1;
                        KernelMemoryUsageEntry n2 = (KernelMemoryUsageEntry) e2;

                        return n1.getName().compareTo(n2.getName());
                    }
                });
                columns.add(column);
                return columns;
            }
        };
    }

    @Override
    protected ITmfTrace getTrace() {
        return super.getTrace();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void contentChanged(ITmfTreeViewerEntry rootEntry) {
        Long selectedEntry = fSelectedEntry;
        if (selectedEntry != null) {
            /* Find the selected thread among the inputs */
            for (KernelMemoryUsageEntry entry : Iterables.filter(rootEntry.getChildren(), KernelMemoryUsageEntry.class)) {
                if (selectedEntry == entry.getModel().getId()) {
                    List<ITmfTreeViewerEntry> list = Collections.singletonList(entry);
                    super.setSelection(list);
                    return;
                }
            }
        }
    }

    @Override
    public void initializeDataSource() {
        /* Should not be called while trace is still null */
        ITmfTrace trace = checkNotNull(getTrace());
        DataProviderManager.getInstance().getDataProvider(trace,
                KernelMemoryUsageDataProvider.ID, KernelMemoryUsageDataProvider.class);
    }

    @Override
    protected ITmfTreeViewerEntry updateElements(long start, long end, boolean isSelection) {
        if (isSelection || (start == end)) {
            return null;
        }

        KernelMemoryUsageDataProvider provider = DataProviderManager.getInstance().getDataProvider(getTrace(),
                KernelMemoryUsageDataProvider.ID, KernelMemoryUsageDataProvider.class);
        if (provider == null) {
            return null;
        }
        TmfTreeViewerEntry root = new TmfTreeViewerEntry(""); //$NON-NLS-1$
        List<ITmfTreeViewerEntry> entryList = root.getChildren();

        TmfModelResponse<@NonNull List<@NonNull KernelMemoryUsageTreeModel>> response = provider.fetchTree(new TimeQueryFilter(start, end, 2), null);
        List<@NonNull KernelMemoryUsageTreeModel> model = response.getModel();
        if (model != null) {
            for (KernelMemoryUsageTreeModel k : model) {
                entryList.add(new KernelMemoryUsageEntry(k));
            }
        }
        return root;
    }

    /**
     * Set the currently selected entry's ID
     *
     * @param id
     *            The selected entry's ID
     */
    public void setSelectedEntry(Long id) {
        fSelectedEntry = id;
    }

    @Override
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        initSelection();
        super.traceSelected(signal);
    }

    @Override
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        initSelection();
        super.traceOpened(signal);
    }

    private void initSelection() {
        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        final @Nullable Object data = ctx.getData(KernelMemoryUsageView.KERNEL_MEMORY);
        Long id = data instanceof Long ? (Long) data : null;
        setSelectedEntry(id);
    }
}