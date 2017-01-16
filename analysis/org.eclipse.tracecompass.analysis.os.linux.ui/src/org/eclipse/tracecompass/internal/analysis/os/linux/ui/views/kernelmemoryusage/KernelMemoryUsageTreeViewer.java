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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelThreadInformationProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage.KernelMemoryAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage.KernelMemoryStateProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractTmfTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;

/**
 * Tree viewer to select which process to display in the kernel memory usage
 * chart.
 *
 * @author Mahdi Zolnouri
 * @author Wassim Nasrallah
 * @author Najib Arbaoui
 */
public class KernelMemoryUsageTreeViewer extends AbstractTmfTreeViewer {

    private KernelMemoryAnalysisModule fModule = null;
    private String fSelectedThread = null;
    private static final String[] COLUMN_NAMES = new String[] {
            Messages.KernelMemoryUsageComposite_ColumnTID,
            Messages.KernelMemoryUsageComposite_ColumnProcess
    };

    /* A map that saves the mapping of a thread ID to its executable name */
    private final Map<String, String> fProcessNameMap = new HashMap<>();

    /** Provides label for the Kernel memory usage tree viewer cells */
    protected static class KernelMemoryLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            KernelMemoryUsageEntry obj = (KernelMemoryUsageEntry) element;
            if (columnIndex == 0) {
                return obj.getTid();
            } else if (columnIndex == 1) {
                return obj.getProcessName();
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

                        return n1.getTid().compareTo(n2.getTid());
                    }
                });
                columns.add(column);
                column = new TmfTreeColumnData(COLUMN_NAMES[1]);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        KernelMemoryUsageEntry n1 = (KernelMemoryUsageEntry) e1;
                        KernelMemoryUsageEntry n2 = (KernelMemoryUsageEntry) e2;

                        return n1.getProcessName().compareTo(n2.getProcessName());
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
            for (ITmfTreeViewerEntry entry : rootEntry.getChildren()) {
                if (entry instanceof KernelMemoryUsageEntry) {
                    if (selectedThread.equals(((KernelMemoryUsageEntry) entry).getTid())) {
                        List<ITmfTreeViewerEntry> list = Collections.singletonList(entry);
                        super.setSelection(list);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void initializeDataSource() {
        /* Should not be called while trace is still null */
        ITmfTrace trace = checkNotNull(getTrace());

        fModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelMemoryAnalysisModule.class, KernelMemoryAnalysisModule.ID);
        if (fModule == null) {
            return;
        }
        fModule.schedule();
        fModule.waitForInitialization();
        fProcessNameMap.clear();
    }

    @Override
    protected ITmfTreeViewerEntry updateElements(long start, long end, boolean isSelection) {
        if (isSelection || (start == end)) {
            return null;
        }
        KernelMemoryAnalysisModule module = fModule;
        if (getTrace() == null || module == null) {
            return null;
        }
        module.waitForInitialization();
        ITmfStateSystem ss = module.getStateSystem();
        if (ss == null) {
            return null;
        }
        ss.waitUntilBuilt();
        TmfTreeViewerEntry root = new TmfTreeViewerEntry(""); //$NON-NLS-1$
        List<ITmfTreeViewerEntry> entryList = root.getChildren();

        try {
            long newStart = Math.max(start, ss.getStartTime());
            long newEnd = Math.min(end, ss.getCurrentEndTime());
            if (ss.getStartTime() > newEnd || ss.getCurrentEndTime() < start) {
                return root;
            }
            List<ITmfStateInterval> memoryStates = ss.queryFullState(newStart);
            List<Integer> threadQuarkList = ss.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, false);

            for (Integer threadQuark : threadQuarkList) {
                ITmfStateInterval threadMemoryInterval = memoryStates.get(threadQuark);
                if (threadMemoryInterval.getEndTime() < end) {
                    String tid = ss.getAttributeName(threadQuark);
                    String procname = getProcessName(tid);
                    KernelMemoryUsageEntry obj = new KernelMemoryUsageEntry(tid, procname);
                    entryList.add(obj);
                }
            }
        } catch (StateSystemDisposedException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
        return root;
    }

    /*
     * Get the process name from its TID by using the LTTng kernel analysis
     * module
     */
    private String getProcessName(String tid) {
        String execName = fProcessNameMap.get(tid);
        if (execName != null) {
            return execName;
        }
        if (tid.equals(KernelMemoryStateProvider.OTHER_TID)) {
            fProcessNameMap.put(tid, tid);
            return tid;
        }
        ITmfTrace trace = checkNotNull(getTrace());
        KernelAnalysisModule kernelModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelAnalysisModule.class, KernelAnalysisModule.ID);
        if (kernelModule == null) {
            return tid;
        }
        execName = KernelThreadInformationProvider.getExecutableName(kernelModule, Integer.parseInt(tid));
        if (execName == null) {
            return tid;
        }
        fProcessNameMap.put(tid, execName);
        return execName;
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
        String thread = data instanceof String ? (String) data : null;
        setSelectedThread(thread);
    }
}