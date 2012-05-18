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

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.lttng2.kernel.core.trace.Attributes;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.CtfKernelTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
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
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ResourcesView extends TmfView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * View ID.
     */
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.ui.views.resources"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    // The time graph viewer
    TimeGraphViewer fTimeGraphViewer;

    // The selected experiment
    private TmfExperiment<ITmfEvent> fSelectedExperiment;

    // The time graph entry list
    private ArrayList<ITimeGraphEntry> fEntryList;

    // The start time
    private long fStartTime;

    // The end time
    private long fEndTime;

    // The display width
    private int fDisplayWidth;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private class GroupEntry implements ITimeGraphEntry {
        public ITimeGraphEntry fParent;
        public ArrayList<ITimeGraphEntry> fChildren;
        public String fName;

        public GroupEntry(ITimeGraphEntry parent, String name) {
            fParent = parent;
            fChildren = new ArrayList<ITimeGraphEntry>();
            fName = name;
        }

        @Override
        public ITimeGraphEntry getParent() {
            return fParent;
        }

        @Override
        public boolean hasChildren() {
            return fChildren != null && fChildren.size() > 0;
        }

        @Override
        public ITimeGraphEntry[] getChildren() {
            return fChildren.toArray(new ITimeGraphEntry[0]);
        }

        @Override
        public String getName() {
            return fName;
        }

        @Override
        public long getStartTime() {
            return -1;
        }

        @Override
        public long getStopTime() {
            return -1;
        }

        @Override
        public Iterator<ITimeEvent> getTimeEventsIterator() {
            return null;
        }

        @Override
        public <T extends ITimeEvent> Iterator<T> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
            return null;
        }

        public void addChild(ITimeGraphEntry entry) {
            fChildren.add(entry);
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public ResourcesView() {
        super(ID);
        fDisplayWidth = Display.getDefault().getBounds().width;
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.TmfView#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        fTimeGraphViewer = new TimeGraphViewer(parent, SWT.NONE);

        fTimeGraphViewer.setTimeGraphProvider(new TimeGraphProvider() {
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
                if (event.getTime() % 2 == 0) {
                    return StateColor.BLACK;
                } else {
                    return StateColor.GRAY;
                }
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
        fTimeGraphViewer.setFocus();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    @TmfSignalHandler
    public void experimentSelected(final TmfExperimentSelectedSignal<? extends TmfEvent> signal) {
        if (signal.getExperiment().equals(fSelectedExperiment)) {
            return;
        }

        final Thread thread = new Thread() {
            @Override
            public void run() {
                selectExperiment(signal.getExperiment());
            }};
        thread.start();
    }

    @SuppressWarnings("unchecked")
    private void selectExperiment(TmfExperiment<?> experiment) {
        fStartTime = Long.MAX_VALUE;
        fEndTime = Long.MIN_VALUE;
        fSelectedExperiment = (TmfExperiment<ITmfEvent>) experiment;
        fEntryList = new ArrayList<ITimeGraphEntry>();
        for (ITmfTrace<?> trace : experiment.getTraces()) {
            GroupEntry groupEntry = new GroupEntry(null, trace.getPath());
            fEntryList.add(groupEntry);
            refresh();
            if (trace instanceof CtfKernelTrace) {
                CtfKernelTrace ctfKernelTrace = (CtfKernelTrace) trace;
                IStateSystemQuerier ssq = ctfKernelTrace.getStateSystem();
                long start = ssq.getStartTime();
                long end = ssq.getCurrentEndTime();
                fStartTime = Math.min(fStartTime, start);
                fEndTime = Math.max(fEndTime, end);
                List<Integer> cpuQuarks = ssq.getQuarks(Attributes.CPUS, "*"); //$NON-NLS-1$
                for (int cpuQuark : cpuQuarks) {
                    String cpuName = "CPU " + ssq.getAttributeName(cpuQuark);
                    ResourcesEntry entry = new ResourcesEntry(groupEntry, ctfKernelTrace, cpuName);
                    try {
                        int currentThreadQuark = ssq.getQuarkRelative(cpuQuark, Attributes.CURRENT_THREAD);
                        long resolution = (end - start) / fDisplayWidth;
                        List<ITmfStateInterval> currentThreadIntervals = ssq.queryHistoryRange(currentThreadQuark, start, end, resolution);
                        for (ITmfStateInterval currentThreadInterval : currentThreadIntervals) {
                            if (!currentThreadInterval.getStateValue().isNull() && currentThreadInterval.getStateValue().getType() == 0) {
                                int currentThread = currentThreadInterval.getStateValue().unboxInt();
                                long startTime = currentThreadInterval.getStartTime();
                                long endTime = currentThreadInterval.getEndTime();
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
                    groupEntry.addChild(entry);
                    refresh();
                }
            }
        }
    }

    private void refresh() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphViewer.getControl().isDisposed()) {
                    return;
                }
                fTimeGraphViewer.setInput(fEntryList.toArray(new ITimeGraphEntry[0]));
                fTimeGraphViewer.setTimeBounds(fStartTime, fEndTime);
                fTimeGraphViewer.setStartFinishTime(fStartTime, fEndTime);
            }
        });
    }

}
