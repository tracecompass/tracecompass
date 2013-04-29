/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.callstack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.callstack.CallStackStateProvider;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampDelta;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.editors.ITmfTraceEditor;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphRangeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphCombo;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphRangeUpdateEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.TimeGraphSelection;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;

/**
 * Main implementation for the Call Stack view
 *
 * @author Patrick Tasse
 * @since 2.0
 */
public class CallStackView extends TmfView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** View ID. */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.callstack"; //$NON-NLS-1$

    /**
     * Redraw state enum
     */
    private enum State { IDLE, BUSY, PENDING }

    private final String[] COLUMN_NAMES = new String[] {
            Messages.CallStackView_FunctionColumn,
            Messages.CallStackView_DepthColumn,
            Messages.CallStackView_EntryTimeColumn,
            Messages.CallStackView_ExitTimeColumn,
            Messages.CallStackView_DurationColumn
    };

    private static final Image THREAD_IMAGE = Activator.getDefault().getImageFromPath("icons/obj16/thread_obj.gif"); //$NON-NLS-1$
    private static final Image STACKFRAME_IMAGE = Activator.getDefault().getImageFromPath("icons/obj16/stckframe_obj.gif"); //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    // The time graph combo
    TimeGraphCombo fTimeGraphCombo;

    // The selected trace
    private ITmfTrace fTrace;

    // The selected thread map
    final private HashMap<ITmfTrace, String> fSelectedThreadMap = new HashMap<ITmfTrace, String>();

    // The time graph entry list
    private ArrayList<ThreadEntry> fEntryList;

    // The trace to entry list hash map
    final private HashMap<ITmfTrace, ArrayList<ThreadEntry>> fEntryListMap = new HashMap<ITmfTrace, ArrayList<ThreadEntry>>();

    // The trace to build thread hash map
    final private HashMap<ITmfTrace, BuildThread> fBuildThreadMap = new HashMap<ITmfTrace, BuildThread>();

    // The start time
    private long fStartTime;

    // The end time
    private long fEndTime;

    // The display width
    private int fDisplayWidth;

    // The next event action
    private Action fNextEventAction;

    // The previous event action
    private Action fPrevEventAction;

    // The next item action
    private Action fNextItemAction;

    // The previous item action
    private Action fPreviousItemAction;

    // The zoom thread
    private ZoomThread fZoomThread;

    // The redraw state used to prevent unnecessary queuing of display runnables
    private State fRedrawState = State.IDLE;

    // The redraw synchronization object
    final private Object fSyncObj = new Object();

    // The saved time sync. signal used when switching off the pinning of a view
    private TmfTimeSynchSignal fSavedTimeSyncSignal;

    // The saved time range sync. signal used when switching off the pinning of a view
    private TmfRangeSynchSignal fSavedRangeSyncSignal;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private class ThreadEntry implements ITimeGraphEntry {
        // The Trace
        private final ITmfTrace fThreadTrace;
        // The start time
        private final long fTraceStartTime;
        // The end time
        private final long fTraceEndTime;
        // The children of the entry
        private ArrayList<CallStackEntry> fChildren;
        // The name of entry
        private final String fName;
        // The thread attribute quark
        private final int fThreadQuark;

        public ThreadEntry(ITmfTrace trace, String name, int threadQuark, long startTime, long endTime) {
            fThreadTrace = trace;
            fChildren = new ArrayList<CallStackEntry>();
            fName = name;
            fTraceStartTime = startTime;
            fTraceEndTime = endTime;
            fThreadQuark = threadQuark;
        }

        @Override
        public ITimeGraphEntry getParent() {
            return null;
        }

        @Override
        public boolean hasChildren() {
            if (fChildren == null) {
                ITmfStateSystem ss = fThreadTrace.getStateSystems().get(CallStackStateProvider.ID);
                try {
                    int eventStackQuark = ss.getQuarkRelative(fThreadQuark, CallStackStateProvider.CALL_STACK);
                    ITmfStateInterval eventStackInterval = ss.querySingleState(ss.getStartTime(), eventStackQuark);
                    return ! eventStackInterval.getStateValue().isNull() || eventStackInterval.getEndTime() != ss.getCurrentEndTime();
                } catch (AttributeNotFoundException e) {
                    e.printStackTrace();
                } catch (TimeRangeException e) {
                    e.printStackTrace();
                } catch (StateSystemDisposedException e) {
                    /* Ignored */
                }
            }
            return fChildren != null && fChildren.size() > 0;
        }

        @Override
        public List<CallStackEntry> getChildren() {
            return fChildren;
        }

        @Override
        public String getName() {
            return fName;
        }

        @Override
        public long getStartTime() {
            return fTraceStartTime;
        }

        @Override
        public long getEndTime() {
            return fTraceEndTime;
        }

        @Override
        public boolean hasTimeEvents() {
            return false;
        }

        @Override
        public Iterator<ITimeEvent> getTimeEventsIterator() {
            return null;
        }

        @Override
        public <T extends ITimeEvent> Iterator<T> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
            return null;
        }

        public int getThreadQuark() {
            return fThreadQuark;
        }

        public ITmfTrace getTrace() {
            return fThreadTrace;
        }

        public void addChild(CallStackEntry entry) {
            entry.setParent(this);
            fChildren.add(entry);
        }
    }

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
            return entry.getChildren().toArray();
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
            if (columnIndex == 0) {
                if (element instanceof ThreadEntry) {
                    return THREAD_IMAGE;
                } else if (element instanceof CallStackEntry) {
                    CallStackEntry entry = (CallStackEntry) element;
                    if (entry.getFunctionName().length() > 0) {
                        return STACKFRAME_IMAGE;
                    }
                }
            }
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ThreadEntry) {
                if (columnIndex == 0) {
                    return ((ThreadEntry) element).getName();
                }
            } else if (element instanceof CallStackEntry) {
                CallStackEntry entry = (CallStackEntry) element;
                if (columnIndex == 0) {
                    return entry.getFunctionName();
                } else if (columnIndex == 1) {
                    if (entry.getFunctionName().length() > 0) {
                        int depth = entry.getStackLevel();
                        return Integer.toString(depth);
                    }
                } else if (columnIndex == 2) {
                    if (entry.getFunctionName().length() > 0) {
                        ITmfTimestamp ts = new TmfTimestamp(entry.getStartTime(), ITmfTimestamp.NANOSECOND_SCALE);
                        return ts.toString();
                    }
                } else if (columnIndex == 3) {
                    if (entry.getFunctionName().length() > 0) {
                        ITmfTimestamp ts = new TmfTimestamp(entry.getEndTime(), ITmfTimestamp.NANOSECOND_SCALE);
                        return ts.toString();
                    }
                } else if (columnIndex == 4) {
                    if (entry.getFunctionName().length() > 0) {
                        ITmfTimestamp ts = new TmfTimestampDelta(entry.getEndTime() - entry.getStartTime(), ITmfTimestamp.NANOSECOND_SCALE);
                        return ts.toString();
                    }
                }
            }
            return ""; //$NON-NLS-1$
        }

    }

    private class BuildThread extends Thread {
        private final ITmfTrace fBuildTrace;
        private final IProgressMonitor fMonitor;

        public BuildThread(ITmfTrace trace) {
            super("CallStackView build"); //$NON-NLS-1$
            fBuildTrace = trace;
            fMonitor = new NullProgressMonitor();
        }

        @Override
        public void run() {
            buildThreadList(fBuildTrace, fMonitor);
            synchronized (fBuildThreadMap) {
                fBuildThreadMap.remove(this);
            }
        }

        public void cancel() {
            fMonitor.setCanceled(true);
        }
    }

    private class ZoomThread extends Thread {
        private final ArrayList<ThreadEntry> fZoomEntryList;
        private final long fZoomStartTime;
        private final long fZoomEndTime;
        private final IProgressMonitor fMonitor;

        public ZoomThread(ArrayList<ThreadEntry> entryList, long startTime, long endTime) {
            super("ResourcesView zoom"); //$NON-NLS-1$
            fZoomEntryList = entryList;
            fZoomStartTime = startTime;
            fZoomEndTime = endTime;
            fMonitor = new NullProgressMonitor();
        }

        @Override
        public void run() {
            if (fZoomEntryList == null) {
                return;
            }
            long resolution = Math.max(1, (fZoomEndTime - fZoomStartTime) / fDisplayWidth);
            for (ThreadEntry threadEntry : fZoomEntryList) {
                ITmfStateSystem ss = threadEntry.fThreadTrace.getStateSystems().get(CallStackStateProvider.ID);
                if (ss == null || !ss.waitUntilBuilt()) {
                    continue;
                }
                for (ITimeGraphEntry child : threadEntry.getChildren()) {
                    if (fMonitor.isCanceled()) {
                        break;
                    }
                    CallStackEntry entry = (CallStackEntry) child;
                    if (fZoomStartTime <= fStartTime && fZoomEndTime >= fEndTime) {
                        entry.setZoomedEventList(null);
                    } else {
                        List<ITimeEvent> zoomedEventList = getEventList(entry, fZoomStartTime, fZoomEndTime, resolution, fMonitor);
                        if (zoomedEventList != null) {
                            entry.setZoomedEventList(zoomedEventList);
                        }
                    }
                    redraw();
                }
            }
        }

        public void cancel() {
            fMonitor.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public CallStackView() {
        super(ID);
        fDisplayWidth = Display.getDefault().getBounds().width;
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {
        fTimeGraphCombo = new TimeGraphCombo(parent, SWT.NONE);

        fTimeGraphCombo.setTreeContentProvider(new TreeContentProvider());

        fTimeGraphCombo.setTreeLabelProvider(new TreeLabelProvider());

        fTimeGraphCombo.setTreeColumns(COLUMN_NAMES);

        fTimeGraphCombo.getTreeViewer().getTree().getColumn(0).setWidth(200);
        fTimeGraphCombo.getTreeViewer().getTree().getColumn(1).setWidth(50);
        fTimeGraphCombo.getTreeViewer().getTree().getColumn(2).setWidth(120);
        fTimeGraphCombo.getTreeViewer().getTree().getColumn(3).setWidth(120);
        fTimeGraphCombo.getTreeViewer().getTree().getColumn(4).setWidth(120);

        fTimeGraphCombo.setTimeGraphProvider(new CallStackPresentationProvider());
        fTimeGraphCombo.getTimeGraphViewer().setTimeFormat(TimeFormat.CALENDAR);

        fTimeGraphCombo.getTimeGraphViewer().addRangeListener(new ITimeGraphRangeListener() {
            @Override
            public void timeRangeUpdated(TimeGraphRangeUpdateEvent event) {
                long startTime = event.getStartTime();
                long endTime = event.getEndTime();
                TmfTimeRange range = new TmfTimeRange(new CtfTmfTimestamp(startTime), new CtfTmfTimestamp(endTime));
                TmfTimestamp time = new CtfTmfTimestamp(fTimeGraphCombo.getTimeGraphViewer().getSelectedTime());
                broadcast(new TmfRangeSynchSignal(CallStackView.this, range, time));
                startZoomThread(startTime, endTime);
            }
        });

        fTimeGraphCombo.getTimeGraphViewer().addTimeListener(new ITimeGraphTimeListener() {
            @Override
            public void timeSelected(TimeGraphTimeEvent event) {
                long time = event.getTime();
                selectTime(time);
                broadcast(new TmfTimeSynchSignal(CallStackView.this, new CtfTmfTimestamp(time)));
            }
        });

        fTimeGraphCombo.getTimeGraphViewer().getControl().addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                fDisplayWidth = fTimeGraphCombo.getTimeGraphViewer().getControl().getSize().x;
                if (fEntryList != null) {
                    startZoomThread(fTimeGraphCombo.getTimeGraphViewer().getTime0(), fTimeGraphCombo.getTimeGraphViewer().getTime1());
                }
            }
        });

        fTimeGraphCombo.getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
                if (selection instanceof CallStackEntry) {
                    CallStackEntry entry = (CallStackEntry) selection;
                    if (entry.getFunctionName().length() > 0) {
                        long startTime = entry.getStartTime();
                        long endTime = entry.getEndTime();
                        long spacingTime = (long) ((endTime - startTime) * 0.01);
                        startTime -= spacingTime;
                        endTime += spacingTime;
                        TmfTimeRange range = new TmfTimeRange(new CtfTmfTimestamp(startTime), new CtfTmfTimestamp(endTime));
                        TmfTimestamp time = new CtfTmfTimestamp(fTimeGraphCombo.getTimeGraphViewer().getSelectedTime());
                        broadcast(new TmfRangeSynchSignal(CallStackView.this, range, time));
                        fTimeGraphCombo.getTimeGraphViewer().setStartFinishTime(startTime, endTime);
                        startZoomThread(startTime, endTime);
                    }
                }
            }
        });

        fTimeGraphCombo.getTimeGraphViewer().getTimeGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                TimeGraphControl timeGraphControl = fTimeGraphCombo.getTimeGraphViewer().getTimeGraphControl();
                ISelection selection = timeGraphControl.getSelection();
                if (selection instanceof TimeGraphSelection) {
                    Object o = ((TimeGraphSelection) selection).getFirstElement();
                    if (o instanceof CallStackEvent) {
                        CallStackEvent event = (CallStackEvent) o;
                        long startTime = event.getTime();
                        long endTime = startTime + event.getDuration();
                        long spacingTime = (long) ((endTime - startTime) * 0.01);
                        startTime -= spacingTime;
                        endTime += spacingTime;
                        TmfTimeRange range = new TmfTimeRange(new CtfTmfTimestamp(startTime), new CtfTmfTimestamp(endTime));
                        TmfTimestamp time = new CtfTmfTimestamp(fTimeGraphCombo.getTimeGraphViewer().getSelectedTime());
                        broadcast(new TmfRangeSynchSignal(CallStackView.this, range, time));
                        fTimeGraphCombo.getTimeGraphViewer().setStartFinishTime(startTime, endTime);
                        startZoomThread(startTime, endTime);
                    }
                }
            }
        });

        // View Action Handling
        makeActions();
        contributeToActionBars();

        IEditorPart editor = getSite().getPage().getActiveEditor();
        if (editor instanceof ITmfTraceEditor) {
            ITmfTrace trace = ((ITmfTraceEditor) editor).getTrace();
            if (trace != null) {
                traceSelected(new TmfTraceSelectedSignal(this, trace));
            }
        }
    }

    @Override
    public void setFocus() {
        fTimeGraphCombo.setFocus();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the trace selected signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        if (signal.getTrace() == fTrace) {
            return;
        }
        fTrace = signal.getTrace();

        synchronized (fEntryListMap) {
            fEntryList = fEntryListMap.get(fTrace);
            if (fEntryList == null) {
                synchronized (fBuildThreadMap) {
                    BuildThread buildThread = new BuildThread(fTrace);
                    fBuildThreadMap.put(fTrace, buildThread);
                    buildThread.start();
                }
            } else {
                fStartTime = fTrace.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                fEndTime = fTrace.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                refresh();
            }
        }
    }

    /**
     * Trace is closed: clear the data structures and the view
     *
     * @param signal the signal received
     */
    @TmfSignalHandler
    public void traceClosed(final TmfTraceClosedSignal signal) {
        synchronized (fBuildThreadMap) {
            BuildThread buildThread = fBuildThreadMap.remove(signal.getTrace());
            if (buildThread != null) {
                buildThread.cancel();
            }
        }
        synchronized (fEntryListMap) {
            fEntryListMap.remove(signal.getTrace());
        }
        fSelectedThreadMap.remove(signal.getTrace());
        if (signal.getTrace() == fTrace) {
            fTrace = null;
            fStartTime = 0;
            fEndTime = 0;
            refresh();
        }
    }

    /**
     * Handler for the TimeSynch signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void synchToTime(final TmfTimeSynchSignal signal) {

        fSavedTimeSyncSignal = isPinned() ? new TmfTimeSynchSignal(signal.getSource(), signal.getCurrentTime()) : null;

        if (signal.getSource() == this || fTrace == null || isPinned()) {
            return;
        }
        final long time = signal.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                selectTime(time);
                startZoomThread(fTimeGraphCombo.getTimeGraphViewer().getTime0(), fTimeGraphCombo.getTimeGraphViewer().getTime1());
                if (fEntryList == null) {
                    return;
                }
                TimeGraphViewer viewer = fTimeGraphCombo.getTimeGraphViewer();
                for (ThreadEntry threadEntry : fEntryList) {
                    ITmfStateSystem ss = threadEntry.getTrace().getStateSystems().get(CallStackStateProvider.ID);
                    if (ss == null || time < ss.getStartTime() || time > ss.getCurrentEndTime()) {
                        continue;
                    }
                    try {
                        int quark = ss.getQuarkRelative(threadEntry.getThreadQuark(), CallStackStateProvider.CALL_STACK);
                        ITmfStateInterval stackInterval = ss.querySingleState(time, quark);
                        if (time == stackInterval.getStartTime()) {
                            int stackLevel = stackInterval.getStateValue().unboxInt();
                            CallStackEntry selectedEntry = threadEntry.getChildren().get(Math.max(0, stackLevel - 1));
                            fTimeGraphCombo.setSelection(selectedEntry);
                            viewer.getTimeGraphControl().fireSelectionChanged();
                            break;
                        }
                    } catch (AttributeNotFoundException e) {
                        e.printStackTrace();
                    } catch (TimeRangeException e) {
                        e.printStackTrace();
                    } catch (StateSystemDisposedException e) {
                        e.printStackTrace();
                    } catch (StateValueTypeException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Handler for the RangeSynch signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void synchToRange(final TmfRangeSynchSignal signal) {

        if (isPinned()) {
            fSavedRangeSyncSignal =
                    new TmfRangeSynchSignal(signal.getSource(), new TmfTimeRange(signal.getCurrentRange().getStartTime(), signal.getCurrentRange().getEndTime()), signal.getCurrentTime());

            fSavedTimeSyncSignal = null;
        }

        if (signal.getSource() == this || fTrace == null || isPinned()) {
            return;
        }
        if (signal.getCurrentRange().getIntersection(fTrace.getTimeRange()) == null) {
            return;
        }
        final long startTime = signal.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        final long endTime = signal.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        final long time = signal.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                fTimeGraphCombo.getTimeGraphViewer().setStartFinishTime(startTime, endTime);
                fTimeGraphCombo.getTimeGraphViewer().setSelectedTime(time, false);
                startZoomThread(startTime, endTime);
            }
        });
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    private void buildThreadList(final ITmfTrace trace, IProgressMonitor monitor) {
        fStartTime = Long.MAX_VALUE;
        fEndTime = Long.MIN_VALUE;
        ITmfTrace[] traces;
        if (trace instanceof TmfExperiment) {
            TmfExperiment experiment = (TmfExperiment) trace;
            traces = experiment.getTraces();
        } else {
            traces = new ITmfTrace[] { trace };
        }
        ArrayList<ThreadEntry> entryList = new ArrayList<ThreadEntry>();
        for (ITmfTrace aTrace : traces) {
            if (monitor.isCanceled()) {
                return;
            }
            ITmfStateSystem ss = aTrace.getStateSystems().get(CallStackStateProvider.ID);
            if (ss == null || !ss.waitUntilBuilt()) {
                String threadName = Messages.CallStackView_StackInfoNotAvailable + ' ' + '(' + aTrace.getName() + ')';
                ThreadEntry threadEntry = new ThreadEntry(aTrace, threadName, -1, 0, 0);
                entryList.add(threadEntry);
                continue;
            }
            long startTime = ss.getStartTime();
            long endTime = ss.getCurrentEndTime() + 1;
            fStartTime = Math.min(fStartTime, startTime);
            fEndTime = Math.max(fEndTime, endTime);
            List<Integer> threadQuarks = ss.getQuarks(CallStackStateProvider.THREADS, "*"); //$NON-NLS-1$
            for (int i = 0; i < threadQuarks.size(); i++) {
                if (monitor.isCanceled()) {
                    return;
                }
                int threadQuark = threadQuarks.get(i);
                String thread = ss.getAttributeName(threadQuark);
                String threadEntryName = thread + ' ' + '(' + aTrace.getName() + ')';
                ThreadEntry threadEntry = new ThreadEntry(aTrace, threadEntryName, threadQuark, startTime, endTime);
                entryList.add(threadEntry);
                int eventStackQuark;
                try {
                    eventStackQuark = ss.getQuarkRelative(threadQuark, CallStackStateProvider.CALL_STACK);
                    int level = 1;
                    for (int stackLevelQuark : ss.getSubAttributes(eventStackQuark, false)) {
                        CallStackEntry callStackEntry = new CallStackEntry(stackLevelQuark, level++, aTrace);
                        threadEntry.addChild(callStackEntry);
                    }
                } catch (AttributeNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        synchronized (fEntryListMap) {
            fEntryListMap.put(trace, (ArrayList<ThreadEntry>) entryList.clone());
        }
        if (trace == fTrace) {
            refresh();
        }
        for (ThreadEntry threadEntry : entryList) {
            for (CallStackEntry callStackEntry : threadEntry.getChildren()) {
                if (monitor.isCanceled()) {
                    return;
                }
                buildStatusEvents(trace, callStackEntry, monitor);
            }
        }
    }

    private void buildStatusEvents(ITmfTrace trace, CallStackEntry entry, IProgressMonitor monitor) {
        ITmfStateSystem ss = entry.getTrace().getStateSystems().get(CallStackStateProvider.ID);
        long start = ss.getStartTime();
        long end = ss.getCurrentEndTime() + 1;
        long resolution = Math.max(1, (end - start) / fDisplayWidth);
        List<ITimeEvent> eventList = getEventList(entry, start, end, resolution, monitor);
        if (monitor.isCanceled()) {
            return;
        }
        entry.setEventList(eventList);
        if (trace == fTrace) {
            redraw();
        }
    }

    private static List<ITimeEvent> getEventList(CallStackEntry entry,
            long startTime, long endTime, long resolution,
            IProgressMonitor monitor) {
        ITmfStateSystem ss = entry.getTrace().getStateSystems().get(CallStackStateProvider.ID);
        long start = Math.max(startTime, ss.getStartTime());
        long end = Math.min(endTime, ss.getCurrentEndTime() + 1);
        if (end <= start) {
            return null;
        }
        List<ITimeEvent> eventList = null;
        try {
            List<ITmfStateInterval> stackIntervals = ss.queryHistoryRange(entry.getQuark(), start, end - 1, resolution, monitor);
            eventList = new ArrayList<ITimeEvent>(stackIntervals.size());
            long lastEndTime = -1;
            boolean lastIsNull = true;
            for (ITmfStateInterval statusInterval : stackIntervals) {
                if (monitor.isCanceled()) {
                    return null;
                }
                long time = statusInterval.getStartTime();
                long duration = statusInterval.getEndTime() - time + 1;
                if (!statusInterval.getStateValue().isNull()) {
                    final int MODULO = CallStackPresentationProvider.NUM_COLORS / 2;
                    int value = statusInterval.getStateValue().toString().hashCode() % MODULO + MODULO;
                    eventList.add(new CallStackEvent(entry, time, duration, value));
                    lastIsNull = false;
                } else {
                    if (lastEndTime != time && lastEndTime != -1 && lastIsNull) {
                        eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime));
                    }
                    eventList.add(new NullTimeEvent(entry, time, duration));
                    lastIsNull = true;
                }
                lastEndTime = time + duration;
            }
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (TimeRangeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
        return eventList;
    }

    private void selectTime(long time) {
        if (fEntryList == null) {
            return;
        }
        fTimeGraphCombo.getTimeGraphViewer().setSelectedTime(time, true);
        for (ThreadEntry threadEntry : fEntryList) {
            ITmfStateSystem ss = threadEntry.fThreadTrace.getStateSystems().get(CallStackStateProvider.ID);
            if (ss == null || !ss.waitUntilBuilt()) {
                continue;
            }
            long queryTime = Math.max(ss.getStartTime(), Math.min(ss.getCurrentEndTime(), time));
            for (CallStackEntry callStackEntry : threadEntry.getChildren()) {
                try {
                    ITmfStateInterval stackLevelInterval = ss.querySingleState(queryTime, callStackEntry.getQuark());
                    ITmfStateValue nameValue = stackLevelInterval.getStateValue();
                    String name = ""; //$NON-NLS-1$
                    try {
                        if (nameValue.getType() == Type.STRING) {
                            name = nameValue.unboxStr();
                        } else if (nameValue.getType() == Type.INTEGER) {
                            name = "0x" + Integer.toHexString(nameValue.unboxInt()); //$NON-NLS-1$
                        } else if (nameValue.getType() == Type.LONG) {
                            name = "0x" + Long.toHexString(nameValue.unboxLong()); //$NON-NLS-1$
                        }
                    } catch (StateValueTypeException e) {
                    }
                    callStackEntry.setFunctionName(name);
                    if (name.length() > 0) {
                        callStackEntry.setStartTime(stackLevelInterval.getStartTime());
                        callStackEntry.setEndTime(stackLevelInterval.getEndTime() + 1);
                    }
                } catch (AttributeNotFoundException e) {
                    e.printStackTrace();
                } catch (TimeRangeException e) {
                    e.printStackTrace();
                } catch (StateSystemDisposedException e) {
                    e.printStackTrace();
                }
            }
        }
        fTimeGraphCombo.refresh();
        fTimeGraphCombo.getTimeGraphViewer().setSelectedTime(time, true);
    }

    private void refresh() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                ITimeGraphEntry[] entries = null;
                synchronized (fEntryListMap) {
                    fEntryList = fEntryListMap.get(fTrace);
                    if (fEntryList == null) {
                        fEntryList = new ArrayList<ThreadEntry>();
                    }
                    entries = fEntryList.toArray(new ITimeGraphEntry[0]);
                }
                fTimeGraphCombo.setInput(entries);
                fTimeGraphCombo.getTimeGraphViewer().setTimeBounds(fStartTime, fEndTime);

                long timestamp = fTrace == null ? 0 : fTraceManager.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long startTime = fTrace == null ? 0 : fTraceManager.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long endTime = fTrace == null ? 0 : fTraceManager.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                startTime = Math.max(startTime, fStartTime);
                endTime = Math.min(endTime, fEndTime);
                selectTime(timestamp);
                fTimeGraphCombo.getTimeGraphViewer().setStartFinishTime(startTime, endTime);
                startZoomThread(startTime, endTime);
            }
        });
    }

    private void redraw() {
        synchronized (fSyncObj) {
            if (fRedrawState == State.IDLE) {
                fRedrawState = State.BUSY;
            } else {
                fRedrawState = State.PENDING;
                return;
            }
        }
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                fTimeGraphCombo.redraw();
                fTimeGraphCombo.update();
                synchronized (fSyncObj) {
                    if (fRedrawState == State.PENDING) {
                        fRedrawState = State.IDLE;
                        redraw();
                    } else {
                        fRedrawState = State.IDLE;
                    }
                }
            }
        });
    }

    private void startZoomThread(long startTime, long endTime) {
        if (fZoomThread != null) {
            fZoomThread.cancel();
        }
        fZoomThread = new ZoomThread(fEntryList, startTime, endTime);
        fZoomThread.start();
    }

    private void makeActions() {
        fPreviousItemAction = fTimeGraphCombo.getTimeGraphViewer().getPreviousItemAction();
        fPreviousItemAction.setText(Messages.TmfTimeGraphViewer_PreviousItemActionNameText);
        fPreviousItemAction.setToolTipText(Messages.TmfTimeGraphViewer_PreviousItemActionToolTipText);
        fNextItemAction = fTimeGraphCombo.getTimeGraphViewer().getNextItemAction();
        fNextItemAction.setText(Messages.TmfTimeGraphViewer_NextItemActionNameText);
        fNextItemAction.setToolTipText(Messages.TmfTimeGraphViewer_NextItemActionToolTipText);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalToolBar(bars.getToolBarManager());

        // Create pin action
        contributePinActionToToolBar();
        fPinAction.addPropertyChangeListener(new IPropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (IAction.CHECKED.equals(event.getProperty())) {
                    if (!isPinned()) {
                        if (fSavedRangeSyncSignal != null) {
                            synchToRange(fSavedRangeSyncSignal);
                            fSavedRangeSyncSignal = null;
                        }

                        if (fSavedTimeSyncSignal != null) {
                            synchToTime(fSavedTimeSyncSignal);
                            fSavedTimeSyncSignal = null;
                        }
                    }
                }
            }
        });
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getResetScaleAction());
        manager.add(getPreviousEventAction());
        manager.add(getNextEventAction());
        manager.add(fPreviousItemAction);
        manager.add(fNextItemAction);
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getZoomInAction());
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getZoomOutAction());
        manager.add(new Separator());
    }

    /**
     * Get the the next event action.
     *
     * @return The action object
     */
    private Action getNextEventAction() {
        if (fNextEventAction == null) {
            fNextEventAction = new Action() {
                @Override
                public void run() {
                    TimeGraphViewer viewer = fTimeGraphCombo.getTimeGraphViewer();
                    ITimeGraphEntry entry = viewer.getSelection();
                    if (entry instanceof CallStackEntry) {
                        try {
                            CallStackEntry callStackEntry = (CallStackEntry) entry;
                            ITmfTrace trace = callStackEntry.getTrace();
                            ITmfStateSystem ss = trace.getStateSystems().get(CallStackStateProvider.ID);
                            long time = Math.max(ss.getStartTime(), Math.min(ss.getCurrentEndTime(), viewer.getSelectedTime()));
                            ThreadEntry threadEntry = (ThreadEntry) callStackEntry.getParent();
                            int quark = ss.getQuarkRelative(threadEntry.getThreadQuark(), CallStackStateProvider.CALL_STACK);
                            ITmfStateInterval stackInterval = ss.querySingleState(time, quark);
                            long newTime = stackInterval.getEndTime() + 1;
                            viewer.setSelectedTimeNotify(newTime, true);
                            stackInterval = ss.querySingleState(Math.min(ss.getCurrentEndTime(), newTime), quark);
                            int stackLevel = stackInterval.getStateValue().unboxInt();
                            CallStackEntry selectedEntry = threadEntry.getChildren().get(Math.max(0, stackLevel - 1));
                            fTimeGraphCombo.setSelection(selectedEntry);
                            viewer.getTimeGraphControl().fireSelectionChanged();
                            startZoomThread(viewer.getTime0(), viewer.getTime1());
                        } catch (AttributeNotFoundException e) {
                            e.printStackTrace();
                        } catch (TimeRangeException e) {
                            e.printStackTrace();
                        } catch (StateSystemDisposedException e) {
                            e.printStackTrace();
                        } catch (StateValueTypeException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            fNextEventAction.setText(Messages.TmfTimeGraphViewer_NextEventActionNameText);
            fNextEventAction.setToolTipText(Messages.TmfTimeGraphViewer_NextEventActionToolTipText);
            fNextEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEXT_EVENT));
        }

        return fNextEventAction;
    }

    /**
     * Get the previous event action.
     *
     * @return The Action object
     */
    private Action getPreviousEventAction() {
        if (fPrevEventAction == null) {
            fPrevEventAction = new Action() {
                @Override
                public void run() {
                    TimeGraphViewer viewer = fTimeGraphCombo.getTimeGraphViewer();
                    ITimeGraphEntry entry = viewer.getSelection();
                    if (entry instanceof CallStackEntry) {
                        try {
                            CallStackEntry callStackEntry = (CallStackEntry) entry;
                            ITmfTrace trace = callStackEntry.getTrace();
                            ITmfStateSystem ss = trace.getStateSystems().get(CallStackStateProvider.ID);
                            long time = Math.max(ss.getStartTime(), Math.min(ss.getCurrentEndTime(), viewer.getSelectedTime()));
                            ThreadEntry threadEntry = (ThreadEntry) callStackEntry.getParent();
                            int quark = ss.getQuarkRelative(threadEntry.getThreadQuark(), CallStackStateProvider.CALL_STACK);
                            ITmfStateInterval stackInterval = ss.querySingleState(time, quark);
                            if (stackInterval.getStartTime() == time && time > ss.getStartTime()) {
                                stackInterval = ss.querySingleState(time - 1, quark);
                            }
                            viewer.setSelectedTimeNotify(stackInterval.getStartTime(), true);
                            int stackLevel = stackInterval.getStateValue().unboxInt();
                            CallStackEntry selectedEntry = threadEntry.getChildren().get(Math.max(0, stackLevel - 1));
                            fTimeGraphCombo.setSelection(selectedEntry);
                            viewer.getTimeGraphControl().fireSelectionChanged();
                            startZoomThread(viewer.getTime0(), viewer.getTime1());
                        } catch (AttributeNotFoundException e) {
                            e.printStackTrace();
                        } catch (TimeRangeException e) {
                            e.printStackTrace();
                        } catch (StateSystemDisposedException e) {
                            e.printStackTrace();
                        } catch (StateValueTypeException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            fPrevEventAction.setText(Messages.TmfTimeGraphViewer_PreviousEventActionNameText);
            fPrevEventAction.setToolTipText(Messages.TmfTimeGraphViewer_PreviousEventActionToolTipText);
            fPrevEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PREV_EVENT));
        }

        return fPrevEventAction;
    }

}
