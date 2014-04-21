/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.cpuusage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.lttng2.kernel.core.analysis.LttngKernelAnalysisModule;
import org.eclipse.linuxtools.lttng2.kernel.core.cpuusage.LttngKernelCpuUsageAnalysis;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.ui.viewers.tree.AbstractTmfTreeViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.linuxtools.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.linuxtools.tmf.ui.viewers.tree.TmfTreeColumnData.ITmfColumnPercentageProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.tree.TmfTreeViewerEntry;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;

/**
 * Tree viewer to display CPU usage information in a specified time range. It
 * shows the process's TID, its name, the time spent on the CPU during that
 * range, in % and absolute value.
 *
 * @author Geneviève Bastien
 */
public class CpuUsageComposite extends AbstractTmfTreeViewer {

    private LttngKernelCpuUsageAnalysis fModule = null;

    private static final String[] COLUMN_NAMES = new String[] {
            Messages.CpuUsageComposite_ColumnTID,
            Messages.CpuUsageComposite_ColumnProcess,
            Messages.CpuUsageComposite_ColumnPercent,
            Messages.CpuUsageComposite_ColumnTime
    };

    /* A map that saves the mapping of a thread ID to its executable name */
    private final Map<String, String> fProcessNameMap = new HashMap<>();

    /** Provides label for the CPU usage tree viewer cells */
    protected static class CpuLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            CpuUsageEntry obj = (CpuUsageEntry) element;
            if (columnIndex == 0) {
                return obj.getTid();
            } else if (columnIndex == 1) {
                return obj.getProcessName();
            } else if (columnIndex == 2) {
                return String.format(Messages.CpuUsageComposite_TextPercent, obj.getPercent());
            } else if (columnIndex == 3) {
                return NLS.bind(Messages.CpuUsageComposite_TextTime, obj.getTime());
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

                        return n1.getTid().compareTo(n2.getTid());

                    }
                });
                columns.add(column);
                column = new TmfTreeColumnData(COLUMN_NAMES[1]);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        CpuUsageEntry n1 = (CpuUsageEntry) e1;
                        CpuUsageEntry n2 = (CpuUsageEntry) e2;

                        return n1.getProcessName().compareTo(n2.getProcessName());

                    }
                });
                columns.add(column);
                column = new TmfTreeColumnData(COLUMN_NAMES[2]);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        CpuUsageEntry n1 = (CpuUsageEntry) e1;
                        CpuUsageEntry n2 = (CpuUsageEntry) e2;

                        return n1.getPercent().compareTo(n2.getPercent());

                    }
                });
                column.setPercentageProvider(new ITmfColumnPercentageProvider() {

                    @Override
                    public double getPercentage(Object data) {
                        CpuUsageEntry parent = (CpuUsageEntry) data;
                        return parent.getPercent() / 100;
                    }
                });
                columns.add(column);
                column = new TmfTreeColumnData(COLUMN_NAMES[3]);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        CpuUsageEntry n1 = (CpuUsageEntry) e1;
                        CpuUsageEntry n2 = (CpuUsageEntry) e2;

                        return n1.getTime().compareTo(n2.getTime());

                    }
                });
                columns.add(column);

                return columns;
            }

        };
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void initializeDataSource() {
        fModule = getTrace().getAnalysisModuleOfClass(LttngKernelCpuUsageAnalysis.class, LttngKernelCpuUsageAnalysis.ID);
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
        if (getTrace() == null || fModule == null) {
            return null;
        }
        ITmfStateSystem ss = fModule.getStateSystem();
        /* Don't wait for the module completion, when it's ready, we'll know */
        if (ss == null) {
            return null;
        }

        /* Initialize the data */
        Map<String, Long> cpuUsageMap = fModule.getCpuUsageInRange(Math.max(start, getStartTime()), Math.min(end, getEndTime()));

        TmfTreeViewerEntry root = new TmfTreeViewerEntry(""); //$NON-NLS-1$
        List<ITmfTreeViewerEntry> entryList = root.getChildren();

        for (Entry<String, Long> entry : cpuUsageMap.entrySet()) {
            /*
             * Process only entries representing the total of all CPUs and that
             * have time on CPU
             */
            if (entry.getValue() == 0) {
                continue;
            }
            if (!entry.getKey().startsWith(LttngKernelCpuUsageAnalysis.TOTAL)) {
                continue;
            }
            String[] strings = entry.getKey().split(LttngKernelCpuUsageAnalysis.SPLIT_STRING, 2);

            if ((strings.length > 1) && !(strings[1].equals(LttngKernelCpuUsageAnalysis.TID_ZERO))) {
                CpuUsageEntry obj = new CpuUsageEntry(strings[1], getProcessName(strings[1]), (double) entry.getValue() / (double) (end - start) * 100, entry.getValue());
                entryList.add(obj);
            }
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
        TmfStateSystemAnalysisModule module = getTrace().getAnalysisModuleOfClass(TmfStateSystemAnalysisModule.class, LttngKernelAnalysisModule.ID);
        if (module == null) {
            return tid;
        }
        /*
         * Do not schedule the analysis here. It should have been executed when
         * the CPU usage analysis was executed. If it's not available, there
         * might be a good reason (disk space?) so don't force it.
         */
        ITmfStateSystem kernelSs = module.getStateSystem();
        if (kernelSs == null) {
            return tid;
        }

        try {
            int cpusNode = kernelSs.getQuarkAbsolute(Attributes.THREADS);

            /* Get the quarks for each cpu */
            List<Integer> cpuNodes = kernelSs.getSubAttributes(cpusNode, false);

            for (Integer tidQuark : cpuNodes) {
                if (kernelSs.getAttributeName(tidQuark).equals(tid)) {
                    int execNameQuark;
                    List<ITmfStateInterval> execNameIntervals;
                    try {
                        execNameQuark = kernelSs.getQuarkRelative(tidQuark, Attributes.EXEC_NAME);
                        execNameIntervals = kernelSs.queryHistoryRange(execNameQuark, getStartTime(), getEndTime());
                    } catch (AttributeNotFoundException e) {
                        /* No information on this thread (yet?), skip it for now */
                        continue;
                    } catch (StateSystemDisposedException e) {
                        /* State system is closing down, no point continuing */
                        break;
                    }

                    for (ITmfStateInterval execNameInterval : execNameIntervals) {
                        if (!execNameInterval.getStateValue().isNull() &&
                                execNameInterval.getStateValue().getType() == ITmfStateValue.Type.STRING) {
                            execName = execNameInterval.getStateValue().unboxStr();
                            fProcessNameMap.put(tid, execName);
                            return execName;
                        }
                    }
                }
            }

        } catch (AttributeNotFoundException e) {
            /* can't find the process name, just return the tid instead */
        }
        return tid;
    }

}
