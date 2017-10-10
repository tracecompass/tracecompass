/*******************************************************************************
 * Copyright (c) 2014, 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.cpuusage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageDataProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageEntryModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectedCpuQueryFilter;
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
 * Tree viewer to display CPU usage information in a specified time range. It
 * shows the process's TID, its name, the time spent on the CPU during that
 * range, in % and absolute value.
 *
 * @author Geneviève Bastien
 */
public class CpuUsageComposite extends AbstractTmfTreeViewer {

    private static final String[] COLUMN_NAMES = new String[] {
            Messages.CpuUsageComposite_ColumnTID,
            Messages.CpuUsageComposite_ColumnProcess,
            Messages.CpuUsageComposite_ColumnPercent,
            Messages.CpuUsageComposite_ColumnTime
    };

    /** Provides label for the CPU usage tree viewer cells */
    protected static class CpuLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            CpuUsageEntry obj = (CpuUsageEntry) element;
            if (columnIndex == 0) {
                return obj.getName();
            } else if (columnIndex == 1) {
                return obj.getModel().getProcessName();
            } else if (columnIndex == 2) {
                return String.format(Messages.CpuUsageComposite_TextPercent, obj.getPercent());
            } else if (columnIndex == 3) {
                return NLS.bind(Messages.CpuUsageComposite_TextTime, obj.getModel().getTime());
            }

            return element.toString();
        }
    }

    private String fSelectedThread = null;
    private final @NonNull Set<@NonNull Integer> fCpus = new TreeSet<>();
    private CpuUsageDataProvider fProvider;

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite that holds this viewer
     */
    public CpuUsageComposite(Composite parent) {
        super(parent, false);
        setLabelProvider(new CpuLabelProvider());
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
                        CpuUsageEntry n1 = (CpuUsageEntry) e1;
                        CpuUsageEntry n2 = (CpuUsageEntry) e2;

                        return n1.getName().compareTo(n2.getName());

                    }
                });
                columns.add(column);
                column = new TmfTreeColumnData(COLUMN_NAMES[1]);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        CpuUsageEntry n1 = (CpuUsageEntry) e1;
                        CpuUsageEntry n2 = (CpuUsageEntry) e2;

                        return n1.getModel().getProcessName().compareTo(n2.getModel().getProcessName());

                    }
                });
                columns.add(column);
                column = new TmfTreeColumnData(COLUMN_NAMES[2]);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        CpuUsageEntry n1 = (CpuUsageEntry) e1;
                        CpuUsageEntry n2 = (CpuUsageEntry) e2;

                        return Double.compare(n1.getPercent(), n2.getPercent());

                    }
                });
                column.setPercentageProvider(data -> ((CpuUsageEntry) data).getPercent() / 100);
                columns.add(column);
                column = new TmfTreeColumnData(COLUMN_NAMES[3]);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        CpuUsageEntry n1 = (CpuUsageEntry) e1;
                        CpuUsageEntry n2 = (CpuUsageEntry) e2;

                        return Long.compare(n1.getModel().getTime(), n2.getModel().getTime());

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
        String selectedThread = fSelectedThread;
        if (selectedThread != null) {
            /* Find the selected thread among the inputs */
            for (CpuUsageEntry entry : Iterables.filter(rootEntry.getChildren(), CpuUsageEntry.class)) {
                if (selectedThread.equals(entry.getName())) {
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
        ITmfTrace trace = Objects.requireNonNull(getTrace());
        fProvider = DataProviderManager.getInstance().getDataProvider(trace, CpuUsageDataProvider.ID, CpuUsageDataProvider.class);
    }

    @Override
    protected ITmfTreeViewerEntry updateElements(long start, long end, boolean isSelection) {
        if (isSelection || (start == end)) {
            return null;
        }
        ITmfTrace trace = getTrace();
        if (trace == null || fProvider == null) {
            return null;
        }

        TimeQueryFilter filter = new SelectedCpuQueryFilter(Long.max(start, getStartTime()), Long.min(end, getEndTime()), 2, fSelectedThread, fCpus);
        TmfModelResponse<List<@NonNull CpuUsageEntryModel>> response = fProvider.fetchTree(filter, null);
        List<@NonNull CpuUsageEntryModel> model = response.getModel();
        if (model == null) {
            return null;
        }

        double time = end - start;
        TmfTreeViewerEntry root = new TmfTreeViewerEntry(""); //$NON-NLS-1$
        List<ITmfTreeViewerEntry> entryList = root.getChildren();
        for (CpuUsageEntryModel entryModel : model) {
            entryList.add(new CpuUsageEntry(entryModel, entryModel.getTime() / time));
        }

        return root;
    }

    /**
     * Set the currently selected thread ID
     *
     * @param tid
     *            The selected thread ID
     */
    public void setSelectedThread(String tid) {
        fSelectedThread = tid;
    }

    /**
     * Add a core
     *
     * @param core
     *            the core to add
     * @since 2.0
     */
    public void addCpu(int core) {
        fCpus.add(core);
        updateContent(getWindowStartTime(), getWindowEndTime(), false);
    }

    /**
     * Remove a core
     *
     * @param core
     *            the core to remove
     * @since 2.0
     */
    public void removeCpu(int core) {
        fCpus.remove(core);
        updateContent(getWindowStartTime(), getWindowEndTime(), false);
    }

    /**
     * Clears the cores
     *
     * @since 2.0
     *
     */
    public void clearCpu() {
        fCpus.clear();
        updateContent(getWindowStartTime(), getWindowEndTime(), false);
    }

    @Override
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        initSelection();
        initCPU();
        super.traceSelected(signal);
    }

    @Override
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        initSelection();
        initCPU();
        super.traceOpened(signal);
    }

    private void initSelection() {
        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        String thread = (String) ctx.getData(CpuUsageView.CPU_USAGE_SELECTED_THREAD);
        setSelectedThread(thread);
    }

    private void initCPU() {
        clearCpu();
        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        Object data = ctx.getData(CpuUsageView.CPU_USAGE_FOLLOW_CPU);
        if (data instanceof Set<?>) {
            Set<?> set = (Set<?>) data;
            for (Object coreObject : set) {
                if (coreObject instanceof Integer) {
                    Integer core = (Integer) coreObject;
                    if (core >= 0) {
                        addCpu(core);
                    }
                }
            }
        }
    }
}
