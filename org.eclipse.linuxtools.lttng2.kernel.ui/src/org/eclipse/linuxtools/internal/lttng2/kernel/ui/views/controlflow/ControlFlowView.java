/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.controlflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.Attributes;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.CtfKernelTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemQuerier;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphRangeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphSelectionListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphCombo;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphRangeUpdateEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ControlFlowView extends TmfView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * View ID.
     */
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.ui.views.controlflow"; //$NON-NLS-1$

    private final String PROCESS_COLUMN    = Messages.ControlFlowView_processColumn;
    private final String TID_COLUMN        = Messages.ControlFlowView_tidColumn;
    private final String TGID_COLUMN       = Messages.ControlFlowView_tgidColumn;
    private final String PPID_COLUMN       = Messages.ControlFlowView_ppidColumn;
    private final String CPU_COLUMN        = Messages.ControlFlowView_cpuColumn;
    private final String BIRTH_SEC_COLUMN  = Messages.ControlFlowView_birthSecColumn;
    private final String BIRTH_NSEC_COLUMN = Messages.ControlFlowView_birthNsecColumn;
    private final String TRACE_COLUMN      = Messages.ControlFlowView_traceColumn;

    private final String[] COLUMN_NAMES = new String[] {
            PROCESS_COLUMN,
            TID_COLUMN,
            TGID_COLUMN,
            PPID_COLUMN,
            CPU_COLUMN,
            BIRTH_SEC_COLUMN,
            BIRTH_NSEC_COLUMN,
            TRACE_COLUMN
    };

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    // The timegraph combo
    private TimeGraphCombo fTimeGraphCombo;

    // The selected experiment
    private TmfExperiment<ITmfEvent> fSelectedExperiment;

    // The timegraph entry list
    private ArrayList<ITimeGraphEntry> fEntryList;

    // The start time
    private long fStartTime;

    // The end time
    private long fEndTime;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private class TreeContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return (ITimeGraphEntry[]) inputElement;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            ITimeGraphEntry entry = (ITimeGraphEntry) parentElement;
            return entry.getChildren();
        }

        @Override
        public Object getParent(Object element) {
            ITimeGraphEntry entry = (ITimeGraphEntry) element;
            return entry.getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
            ITimeGraphEntry entry = (ITimeGraphEntry) element;
            return entry.hasChildren();
        }
        
    }

    private class TreeLabelProvider implements ITableLabelProvider {

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            ControlFlowEntry entry = (ControlFlowEntry) element;
            if (columnIndex == 0) {
                return entry.getName();
            } else if (columnIndex == 1) {
                return Integer.toString(entry.getThreadId());
            } else if (columnIndex == 3) {
                if (entry.getPPID() != -1) {
                    return Integer.toString(entry.getPPID());
                }
            }
            return ""; //$NON-NLS-1$
        }
        
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public ControlFlowView() {
        super(ID);
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.TmfView#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        fTimeGraphCombo = new TimeGraphCombo(parent, SWT.NONE);

        fTimeGraphCombo.setTreeContentProvider(new TreeContentProvider());

        fTimeGraphCombo.setTreeLabelProvider(new TreeLabelProvider());

        fTimeGraphCombo.setTimeGraphProvider(new TimeGraphProvider() {
            @Override
            public String getTraceClassName(ITimeGraphEntry trace) {
                return "trace class"; //$NON-NLS-1$
            }
            
            @Override
            public String getStateName(StateColor color) {
                return "state name"; //$NON-NLS-1$
            }
            
            @Override
            public String getEventName(ITimeEvent event, boolean upper, boolean extInfo) {
                return "event name"; //$NON-NLS-1$
            }
            
            @Override
            public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
                return new HashMap<String, String>();
            }
            
            @Override
            public StateColor getEventColor(ITimeEvent event) {
                return StateColor.BLACK;
            }
        });

        fTimeGraphCombo.setTreeColumns(COLUMN_NAMES);

        fTimeGraphCombo.getTimeGraphViewer().addRangeListener(new ITimeGraphRangeListener() {
            @Override
            public void timeRangeUpdated(TimeGraphRangeUpdateEvent event) {
                long startTime = event.getStartTime();
                long endTime = event.getEndTime();
                System.out.println("timeRangeUpdated: startTime="+startTime+" endTime="+endTime);
            }
        });

        fTimeGraphCombo.getTimeGraphViewer().addTimeListener(new ITimeGraphTimeListener() {
            @Override
            public void timeSelected(TimeGraphTimeEvent event) {
                long time = event.getTime();
                System.out.println("timeSelected: time="+time);
            }
        });

        fTimeGraphCombo.addSelectionListener(new ITimeGraphSelectionListener() {
            @Override
            public void selectionChanged(TimeGraphSelectionEvent event) {
                ITimeGraphEntry selection = event.getSelection();
                System.out.println("selectionChanged: source="+event.getSource()+" selection="+ (selection == null ? selection : selection.getName()));
            }
        });

        final Thread thread = new Thread() {
            @Override
            public void run() {
                if (TmfExperiment.getCurrentExperiment() != null) {
                    selectExperiment(TmfExperiment.getCurrentExperiment());
                }
            }
        };
        thread.start();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        fTimeGraphCombo.setFocus();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    @TmfSignalHandler
    public void experimentSelected(final TmfExperimentSelectedSignal<? extends ITmfEvent> signal) {
        if (signal.getExperiment().equals(fSelectedExperiment)) {
            return;
        }

        final Thread thread = new Thread() {
            @Override
            public void run() {
                selectExperiment(signal.getExperiment());
            }};
        thread.run();
    }

    @SuppressWarnings("unchecked")
    private void selectExperiment(TmfExperiment<?> experiment) {
        fStartTime = Long.MAX_VALUE;
        fEndTime = Long.MIN_VALUE;
        fSelectedExperiment = (TmfExperiment<ITmfEvent>) experiment;
        HashMap<String, ITimeGraphEntry> traces = new HashMap<String, ITimeGraphEntry>();
        fEntryList = new ArrayList<ITimeGraphEntry>();
        for (ITmfTrace<?> trace : experiment.getTraces()) {
            if (trace instanceof CtfKernelTrace) {
                CtfKernelTrace ctfKernelTrace = (CtfKernelTrace) trace;
                IStateSystemQuerier ssq = ctfKernelTrace.getStateSystem();
                ControlFlowEntry swapperEntry = null;
                long start = ssq.getStartTime();
                long end = ssq.getCurrentEndTime();
                fStartTime = Math.min(fStartTime, start);
                fEndTime = Math.max(fEndTime, end);
                List<Integer> threadQuarks = ssq.getQuarks(Attributes.THREADS, "*"); //$NON-NLS-1$
                for (int threadQuark : threadQuarks) {
                    String threadName = ssq.getAttributeName(threadQuark);
                    int threadId = -1;
                    try {
                        threadId = Integer.parseInt(threadName);
                    } catch (NumberFormatException e1) {
                        continue;
                    }
                    int execNameQuark = -1;
                    try {
                        try {
                            execNameQuark = ssq.getQuarkRelative(threadQuark, Attributes.EXEC_NAME);
                        } catch (AttributeNotFoundException e) {
                            continue;
                        }
                        int ppidQuark = ssq.getQuarkRelative(threadQuark, Attributes.PPID);
                        List<ITmfStateInterval> execNameIntervals = ssq.queryHistoryRange(execNameQuark, start, end);
                        for (ITmfStateInterval execNameInterval : execNameIntervals) {
                            if (!execNameInterval.getStateValue().isNull() && execNameInterval.getStateValue().getType() == 1) {
                                String execName = execNameInterval.getStateValue().unboxStr();
                                long startTime = execNameInterval.getStartTime();
                                long endTime = execNameInterval.getEndTime();
                                int ppid = -1;
                                if (ppidQuark != -1) {
                                    ITmfStateInterval ppidInterval = ssq.querySingleState(startTime, ppidQuark);
                                    ppid = ppidInterval.getStateValue().unboxInt();
                                }
                                ControlFlowEntry entry;
                                if (threadId == 0) {
                                    if (swapperEntry == null) {
                                        swapperEntry = new ControlFlowEntry(ctfKernelTrace, "swapper", threadId, ppid, startTime, endTime);
                                        fEntryList.add(swapperEntry);
                                    }
                                    entry = swapperEntry;
                                } else {
                                    entry = new ControlFlowEntry(ctfKernelTrace, execName, threadId, ppid, startTime, endTime);
                                    fEntryList.add(entry);
                                }
                                entry.addTraceEvent(new TimeEvent(entry, startTime, endTime - startTime));
                            }
                        }
                    } catch (AttributeNotFoundException e) {
                        e.printStackTrace();
                    } catch (TimeRangeException e) {
                        e.printStackTrace();
                    } catch (StateValueTypeException e) {
                        e.printStackTrace();
                    }
                }
            }
            refresh();
        }
    }

    private void refresh() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                ITimeGraphEntry[] entries = fEntryList.toArray(new ITimeGraphEntry[0]);
                Arrays.sort(entries);
                fTimeGraphCombo.setInput(entries);
                fTimeGraphCombo.getTimeGraphViewer().setTimeBounds(fStartTime, fEndTime);
                fTimeGraphCombo.getTimeGraphViewer().setStartFinishTime(fStartTime, fEndTime);
            }
        });
    }

}
