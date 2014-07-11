/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Updated signal handling
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.callstack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
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
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue.Type;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampDelta;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
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
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
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
import org.eclipse.swt.widgets.FileDialog;
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

    private static final String[] COLUMN_NAMES = new String[] {
            Messages.CallStackView_FunctionColumn,
            Messages.CallStackView_DepthColumn,
            Messages.CallStackView_EntryTimeColumn,
            Messages.CallStackView_ExitTimeColumn,
            Messages.CallStackView_DurationColumn
    };

    private static final int[] COLUMN_WIDTHS = new int[] {
            200,
            50,
            120,
            120,
            120
    };

    // Fraction of a function duration to be added as spacing
    private static final double SPACING_RATIO = 0.01;

    private static final Image THREAD_IMAGE = Activator.getDefault().getImageFromPath("icons/obj16/thread_obj.gif"); //$NON-NLS-1$
    private static final Image STACKFRAME_IMAGE = Activator.getDefault().getImageFromPath("icons/obj16/stckframe_obj.gif"); //$NON-NLS-1$

    private static final String IMPORT_MAPPING_ICON_PATH = "icons/etool16/import.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    // The time graph combo
    private TimeGraphCombo fTimeGraphCombo;

    // The selected trace
    private ITmfTrace fTrace;

    // The selected thread map
    private final Map<ITmfTrace, String> fSelectedThreadMap = new HashMap<>();

    // The time graph entry list
    private List<TraceEntry> fEntryList;

    // The trace to entry list hash map
    private final Map<ITmfTrace, ArrayList<TraceEntry>> fEntryListMap = new HashMap<>();

    // The trace to build thread hash map
    private final Map<ITmfTrace, BuildThread> fBuildThreadMap = new HashMap<>();

    /** The map to map function addresses to function names */
    private Map<String, String> fNameMapping;

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

    /** The action to import a function-name mapping file */
    private Action fImportMappingAction;

    // The zoom thread
    private ZoomThread fZoomThread;

    // The redraw state used to prevent unnecessary queuing of display runnables
    private State fRedrawState = State.IDLE;

    // The redraw synchronization object
    private final Object fSyncObj = new Object();

    // The saved time sync. signal used when switching off the pinning of a view
    private TmfTimeSynchSignal fSavedTimeSyncSignal;

    // The saved time range sync. signal used when switching off the pinning of a view
    private TmfRangeSynchSignal fSavedRangeSyncSignal;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private class TraceEntry extends TimeGraphEntry {
        public TraceEntry(String name, long startTime, long endTime) {
            super(name, startTime, endTime);
        }

        @Override
        public boolean hasTimeEvents() {
            return false;
        }
    }

    private class ThreadEntry extends TimeGraphEntry {
        // The call stack quark
        private final int fCallStackQuark;
        // The state system from which this entry comes
        private final ITmfStateSystem fSS;

        public ThreadEntry(ITmfStateSystem ss, String name, int callStackQuark, long startTime, long endTime) {
            super(name, startTime, endTime);
            fCallStackQuark = callStackQuark;

            fSS = ss;
        }

        @Override
        public boolean hasTimeEvents() {
            return false;
        }

        public int getCallStackQuark() {
            return fCallStackQuark;
        }

        @Nullable
        public ITmfStateSystem getStateSystem() {
            return fSS;
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
            if (element instanceof CallStackEntry) {
                CallStackEntry entry = (CallStackEntry) element;
                if (columnIndex == 0) {
                    return entry.getFunctionName();
                } else if (columnIndex == 1 && entry.getFunctionName().length() > 0) {
                    int depth = entry.getStackLevel();
                    return Integer.toString(depth);
                } else if (columnIndex == 2 && entry.getFunctionName().length() > 0) {
                    ITmfTimestamp ts = new TmfTimestamp(entry.getFunctionEntryTime(), ITmfTimestamp.NANOSECOND_SCALE);
                    return ts.toString();
                } else if (columnIndex == 3 && entry.getFunctionName().length() > 0) {
                    ITmfTimestamp ts = new TmfTimestamp(entry.getFunctionExitTime(), ITmfTimestamp.NANOSECOND_SCALE);
                    return ts.toString();
                } else if (columnIndex == 4 && entry.getFunctionName().length() > 0) {
                    ITmfTimestamp ts = new TmfTimestampDelta(entry.getFunctionExitTime() - entry.getFunctionEntryTime(), ITmfTimestamp.NANOSECOND_SCALE);
                    return ts.toString();
                }
            } else if (element instanceof ITimeGraphEntry) {
                if (columnIndex == 0) {
                    return ((ITimeGraphEntry) element).getName();
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
        private final List<TraceEntry> fZoomEntryList;
        private final long fZoomStartTime;
        private final long fZoomEndTime;
        private final IProgressMonitor fMonitor;

        public ZoomThread(List<TraceEntry> entryList, long startTime, long endTime) {
            super("CallStackView zoom"); //$NON-NLS-1$
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
            for (TraceEntry traceEntry : fZoomEntryList) {
                for (ITimeGraphEntry threadEntry : traceEntry.getChildren()) {
                    ITmfStateSystem ss = ((ThreadEntry) threadEntry).getStateSystem();
                    if (ss == null) {
                        continue;
                    }
                    ss.waitUntilBuilt();
                    if (ss.isCancelled()) {
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

        fTimeGraphCombo.getTreeViewer().getTree().getColumn(0).setWidth(COLUMN_WIDTHS[0]);
        fTimeGraphCombo.getTreeViewer().getTree().getColumn(1).setWidth(COLUMN_WIDTHS[1]);
        fTimeGraphCombo.getTreeViewer().getTree().getColumn(2).setWidth(COLUMN_WIDTHS[2]);
        fTimeGraphCombo.getTreeViewer().getTree().getColumn(3).setWidth(COLUMN_WIDTHS[3]);
        fTimeGraphCombo.getTreeViewer().getTree().getColumn(4).setWidth(COLUMN_WIDTHS[4]);

        fTimeGraphCombo.setTimeGraphProvider(new CallStackPresentationProvider(this));
        fTimeGraphCombo.getTimeGraphViewer().setTimeFormat(TimeFormat.CALENDAR);

        fTimeGraphCombo.getTimeGraphViewer().addRangeListener(new ITimeGraphRangeListener() {
            @Override
            public void timeRangeUpdated(TimeGraphRangeUpdateEvent event) {
                long startTime = event.getStartTime();
                long endTime = event.getEndTime();
                TmfTimeRange range = new TmfTimeRange(new TmfNanoTimestamp(startTime), new TmfNanoTimestamp(endTime));
                broadcast(new TmfRangeSynchSignal(CallStackView.this, range));
                startZoomThread(startTime, endTime);
            }
        });

        fTimeGraphCombo.getTimeGraphViewer().addTimeListener(new ITimeGraphTimeListener() {
            @Override
            public void timeSelected(TimeGraphTimeEvent event) {
                long beginTime = event.getBeginTime();
                long endTime = event.getEndTime();
                selectTime(beginTime);
                broadcast(new TmfTimeSynchSignal(CallStackView.this, new TmfNanoTimestamp(beginTime), new TmfNanoTimestamp(endTime)));
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
                        long entryTime = entry.getFunctionEntryTime();
                        long exitTime = entry.getFunctionExitTime();
                        long spacingTime = (long) ((exitTime - entryTime) * SPACING_RATIO);
                        entryTime -= spacingTime;
                        exitTime += spacingTime;
                        TmfTimeRange range = new TmfTimeRange(new TmfNanoTimestamp(entryTime), new TmfNanoTimestamp(exitTime));
                        broadcast(new TmfRangeSynchSignal(CallStackView.this, range));
                        fTimeGraphCombo.getTimeGraphViewer().setStartFinishTime(entryTime, exitTime);
                        startZoomThread(entryTime, exitTime);
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
                        long spacingTime = (long) ((endTime - startTime) * SPACING_RATIO);
                        startTime -= spacingTime;
                        endTime += spacingTime;
                        TmfTimeRange range = new TmfTimeRange(new TmfNanoTimestamp(startTime), new TmfNanoTimestamp(endTime));
                        broadcast(new TmfRangeSynchSignal(CallStackView.this, range));
                        fTimeGraphCombo.getTimeGraphViewer().setStartFinishTime(startTime, endTime);
                        startZoomThread(startTime, endTime);
                    }
                }
            }
        });

        IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
        fTimeGraphCombo.getTimeGraphViewer().getTimeGraphControl().setStatusLineManager(statusLineManager);

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
     * Handler for the trace opened signal.
     * @param signal
     *            The incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        fTrace = signal.getTrace();
        loadTrace();
    }

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
        loadTrace();
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

        fSavedTimeSyncSignal = isPinned() ? new TmfTimeSynchSignal(signal.getSource(), signal.getBeginTime(), signal.getEndTime()) : null;

        if (signal.getSource() == this || fTrace == null || isPinned()) {
            return;
        }
        final long beginTime = signal.getBeginTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        final long endTime = signal.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                if (beginTime == endTime) {
                    fTimeGraphCombo.getTimeGraphViewer().setSelectedTime(beginTime, true);
                } else {
                    fTimeGraphCombo.getTimeGraphViewer().setSelectionRange(beginTime, endTime);
                }
                selectTime(beginTime);
                startZoomThread(fTimeGraphCombo.getTimeGraphViewer().getTime0(), fTimeGraphCombo.getTimeGraphViewer().getTime1());
                if (fEntryList == null) {
                    return;
                }
                TimeGraphViewer viewer = fTimeGraphCombo.getTimeGraphViewer();
                for (TraceEntry traceEntry : fEntryList) {
                    for (ITimeGraphEntry child : traceEntry.getChildren()) {
                        ThreadEntry threadEntry = (ThreadEntry) child;
                        ITmfStateSystem ss = threadEntry.getStateSystem();
                        if (ss == null || beginTime < ss.getStartTime() || beginTime > ss.getCurrentEndTime()) {
                            continue;
                        }
                        try {
                            int quark = threadEntry.getCallStackQuark();
                            ITmfStateInterval stackInterval = ss.querySingleState(beginTime, quark);
                            if (beginTime == stackInterval.getStartTime()) {
                                int stackLevel = stackInterval.getStateValue().unboxInt();
                                ITimeGraphEntry selectedEntry = threadEntry.getChildren().get(Math.max(0, stackLevel - 1));
                                fTimeGraphCombo.setSelection(selectedEntry);
                                viewer.getTimeGraphControl().fireSelectionChanged();
                                break;
                            }
                        } catch (AttributeNotFoundException e) {
                            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                        } catch (TimeRangeException e) {
                            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                        } catch (StateSystemDisposedException e) {
                            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                        } catch (StateValueTypeException e) {
                            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                        }
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
                    new TmfRangeSynchSignal(signal.getSource(), new TmfTimeRange(signal.getCurrentRange().getStartTime(), signal.getCurrentRange().getEndTime()));

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
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                fTimeGraphCombo.getTimeGraphViewer().setStartFinishTime(startTime, endTime);
                startZoomThread(startTime, endTime);
            }
        });
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    private void loadTrace() {
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

    private void buildThreadList(final ITmfTrace trace, IProgressMonitor monitor) {
        fStartTime = Long.MAX_VALUE;
        fEndTime = Long.MIN_VALUE;
        ITmfTrace[] traces = TmfTraceManager.getTraceSet(trace);
        ArrayList<TraceEntry> entryList = new ArrayList<>();
        for (ITmfTrace aTrace : traces) {
            if (monitor.isCanceled()) {
                return;
            }
            AbstractCallStackAnalysis module = getCallStackModule(trace);
            if (module == null) {
                return;
            }
            ITmfStateSystem ss = module.getStateSystem();
            if (ss == null) {
                addUnavailableEntry(aTrace, entryList);
                continue;
            }
            ss.waitUntilBuilt();
            if (ss.isCancelled()) {
                addUnavailableEntry(aTrace, entryList);
                continue;
            }
            long startTime = ss.getStartTime();
            long endTime = ss.getCurrentEndTime() + 1;
            fStartTime = Math.min(fStartTime, startTime);
            fEndTime = Math.max(fEndTime, endTime);
            String[] threadPaths = module.getThreadsPattern();
            List<Integer> threadQuarks = ss.getQuarks(threadPaths);
            TraceEntry traceEntry = new TraceEntry(trace.getName(), startTime, endTime);
            entryList.add(traceEntry);
            for (int i = 0; i < threadQuarks.size(); i++) {
                if (monitor.isCanceled()) {
                    return;
                }
                int threadQuark = threadQuarks.get(i);
                try {
                    String[] callStackPath = module.getCallStackPath();
                    int callStackQuark = ss.getQuarkRelative(threadQuark, callStackPath);
                    String threadName = ss.getAttributeName(threadQuark);
                    ThreadEntry threadEntry = new ThreadEntry(ss, threadName, callStackQuark, startTime, endTime);
                    traceEntry.addChild(threadEntry);
                    int level = 1;
                    for (int stackLevelQuark : ss.getSubAttributes(callStackQuark, false)) {
                        CallStackEntry callStackEntry = new CallStackEntry(threadName, stackLevelQuark, level++, aTrace, ss);
                        threadEntry.addChild(callStackEntry);
                    }
                } catch (AttributeNotFoundException e) {
                    Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                }
            }
        }
        synchronized (fEntryListMap) {
            fEntryListMap.put(trace, new ArrayList<>(entryList));
        }
        if (trace == fTrace) {
            refresh();
        }
        for (TraceEntry traceEntry : entryList) {
            for (ITimeGraphEntry threadEntry : traceEntry.getChildren()) {
                for (ITimeGraphEntry callStackEntry : threadEntry.getChildren()) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    buildStatusEvents(trace, (CallStackEntry) callStackEntry, monitor);
                }
            }
        }
    }

    private void addUnavailableEntry(ITmfTrace trace, List<TraceEntry> list) {
        String name = Messages.CallStackView_StackInfoNotAvailable + ' ' + '(' + trace.getName() + ')';
        TraceEntry unavailableEntry = new TraceEntry(name, 0, 0);
        list.add(unavailableEntry);
    }

    private void buildStatusEvents(ITmfTrace trace, CallStackEntry entry, IProgressMonitor monitor) {
        ITmfStateSystem ss = entry.getStateSystem();
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
        ITmfStateSystem ss = entry.getStateSystem();
        long start = Math.max(startTime, ss.getStartTime());
        long end = Math.min(endTime, ss.getCurrentEndTime() + 1);
        if (end <= start) {
            return null;
        }
        List<ITimeEvent> eventList = null;
        try {
            List<ITmfStateInterval> stackIntervals = ss.queryHistoryRange(entry.getQuark(), start, end - 1, resolution, monitor);
            eventList = new ArrayList<>(stackIntervals.size());
            long lastEndTime = -1;
            boolean lastIsNull = true;
            for (ITmfStateInterval statusInterval : stackIntervals) {
                if (monitor.isCanceled()) {
                    return null;
                }
                long time = statusInterval.getStartTime();
                long duration = statusInterval.getEndTime() - time + 1;
                if (!statusInterval.getStateValue().isNull()) {
                    final int modulo = CallStackPresentationProvider.NUM_COLORS / 2;
                    int value = statusInterval.getStateValue().toString().hashCode() % modulo + modulo;
                    eventList.add(new CallStackEvent(entry, time, duration, value));
                    lastIsNull = false;
                } else {
                    if (lastEndTime == -1) {
                        // add null event if it intersects the start time
                        eventList.add(new NullTimeEvent(entry, time, duration));
                    } else {
                        if (lastEndTime != time && lastIsNull) {
                            // add unknown event if between two null states
                            eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime));
                        }
                        if (time + duration >= endTime) {
                            // add null event if it intersects the end time
                            eventList.add(new NullTimeEvent(entry, time, duration));
                        }
                    }
                    lastIsNull = true;
                }
                lastEndTime = time + duration;
            }
        } catch (AttributeNotFoundException e) {
            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
        } catch (TimeRangeException e) {
            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
        return eventList;
    }

    private void selectTime(long time) {
        if (fEntryList == null) {
            return;
        }
        for (TraceEntry traceEntry : fEntryList) {
            for (ITimeGraphEntry threadEntry : traceEntry.getChildren()) {
                ITmfStateSystem ss = ((ThreadEntry) threadEntry).getStateSystem();
                if (ss == null) {
                    continue;
                }
                ss.waitUntilBuilt();
                if (ss.isCancelled()) {
                    continue;
                }
                long queryTime = Math.max(ss.getStartTime(), Math.min(ss.getCurrentEndTime(), time));
                for (ITimeGraphEntry child : threadEntry.getChildren()) {
                    CallStackEntry callStackEntry = (CallStackEntry) child;
                    try {
                        ITmfStateInterval stackLevelInterval = ss.querySingleState(queryTime, callStackEntry.getQuark());
                        ITmfStateValue nameValue = stackLevelInterval.getStateValue();
                        String name = ""; //$NON-NLS-1$
                        try {
                            if (nameValue.getType() == Type.STRING) {
                                String address = nameValue.unboxStr();
                                name = getFunctionName(address);
                            } else if (nameValue.getType() == Type.INTEGER) {
                                name = "0x" + Integer.toHexString(nameValue.unboxInt()); //$NON-NLS-1$
                            } else if (nameValue.getType() == Type.LONG) {
                                name = "0x" + Long.toHexString(nameValue.unboxLong()); //$NON-NLS-1$
                            }
                        } catch (StateValueTypeException e) {
                        }
                        callStackEntry.setFunctionName(name);
                        if (name.length() > 0) {
                            callStackEntry.setFunctionEntryTime(stackLevelInterval.getStartTime());
                            callStackEntry.setFunctionExitTime(stackLevelInterval.getEndTime() + 1);
                        }
                    } catch (AttributeNotFoundException e) {
                        Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                    } catch (TimeRangeException e) {
                        Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                    } catch (StateSystemDisposedException e) {
                        Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                    }
                }
            }
        }
        fTimeGraphCombo.refresh();
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
                        fEntryList = new ArrayList<>();
                    }
                    entries = fEntryList.toArray(new ITimeGraphEntry[0]);
                }
                fTimeGraphCombo.setInput(entries);
                fTimeGraphCombo.getTimeGraphViewer().setTimeBounds(fStartTime, fEndTime);

                long selectionBeginTime = fTrace == null ? 0 : fTraceManager.getSelectionBeginTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long selectionEndTime = fTrace == null ? 0 : fTraceManager.getSelectionEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long startTime = fTrace == null ? 0 : fTraceManager.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long endTime = fTrace == null ? 0 : fTraceManager.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                startTime = Math.max(startTime, fStartTime);
                endTime = Math.min(endTime, fEndTime);
                fTimeGraphCombo.getTimeGraphViewer().setSelectionRange(selectionBeginTime, selectionEndTime);
                selectTime(selectionBeginTime);
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
                if (IAction.CHECKED.equals(event.getProperty()) && !isPinned()) {
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
        });
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(getImportMappingAction());
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
                            ITmfStateSystem ss = callStackEntry.getStateSystem();
                            long time = Math.max(ss.getStartTime(), Math.min(ss.getCurrentEndTime(), viewer.getSelectionBegin()));
                            ThreadEntry threadEntry = (ThreadEntry) callStackEntry.getParent();
                            int quark = ss.getParentAttributeQuark(callStackEntry.getQuark());
                            ITmfStateInterval stackInterval = ss.querySingleState(time, quark);
                            long newTime = stackInterval.getEndTime() + 1;
                            viewer.setSelectedTimeNotify(newTime, true);
                            stackInterval = ss.querySingleState(Math.min(ss.getCurrentEndTime(), newTime), quark);
                            int stackLevel = stackInterval.getStateValue().unboxInt();
                            ITimeGraphEntry selectedEntry = threadEntry.getChildren().get(Math.max(0, stackLevel - 1));
                            fTimeGraphCombo.setSelection(selectedEntry);
                            viewer.getTimeGraphControl().fireSelectionChanged();
                            startZoomThread(viewer.getTime0(), viewer.getTime1());

                        } catch (AttributeNotFoundException e) {
                            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                        } catch (TimeRangeException e) {
                            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                        } catch (StateSystemDisposedException e) {
                            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                        } catch (StateValueTypeException e) {
                            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
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
                            ITmfStateSystem ss = callStackEntry.getStateSystem();
                            long time = Math.max(ss.getStartTime(), Math.min(ss.getCurrentEndTime(), viewer.getSelectionBegin()));
                            ThreadEntry threadEntry = (ThreadEntry) callStackEntry.getParent();
                            int quark = ss.getParentAttributeQuark(callStackEntry.getQuark());
                            ITmfStateInterval stackInterval = ss.querySingleState(time, quark);
                            if (stackInterval.getStartTime() == time && time > ss.getStartTime()) {
                                stackInterval = ss.querySingleState(time - 1, quark);
                            }
                            viewer.setSelectedTimeNotify(stackInterval.getStartTime(), true);
                            int stackLevel = stackInterval.getStateValue().unboxInt();
                            ITimeGraphEntry selectedEntry = threadEntry.getChildren().get(Math.max(0, stackLevel - 1));
                            fTimeGraphCombo.setSelection(selectedEntry);
                            viewer.getTimeGraphControl().fireSelectionChanged();
                            startZoomThread(viewer.getTime0(), viewer.getTime1());

                        } catch (AttributeNotFoundException e) {
                            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                        } catch (TimeRangeException e) {
                            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                        } catch (StateSystemDisposedException e) {
                            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
                        } catch (StateValueTypeException e) {
                            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
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

    private static @Nullable AbstractCallStackAnalysis getCallStackModule(ITmfTrace trace) {
        /*
         * Since we cannot know the exact analysis ID (in separate plugins), we
         * will search using the analysis type.
         */
        Iterable<AbstractCallStackAnalysis> modules =
                trace.getAnalysisModulesOfClass(AbstractCallStackAnalysis.class);
        Iterator<AbstractCallStackAnalysis> it = modules.iterator();
        if (!it.hasNext()) {
            /* This trace does not provide a call-stack analysis */
            return null;
        }

        /*
         * We only look at the first module we find.
         *
         * TODO Handle the advanced case where one trace provides more than one
         * call-stack analysis.
         */
        AbstractCallStackAnalysis module = it.next();
        /* This analysis is not automatic, we need to schedule it on-demand */
        module.schedule();
        module.waitForInitialization();
        return module;
    }

    // ------------------------------------------------------------------------
    // Methods related to function name mapping
    // ------------------------------------------------------------------------

    /**
     * Toolbar icon to import the function address-to-name mapping file.
     */
    private Action getImportMappingAction() {
        if (fImportMappingAction != null) {
            return fImportMappingAction;
        }
        fImportMappingAction = new Action() {
            @Override
            public void run() {
                FileDialog dialog = new FileDialog(getViewSite().getShell());
                dialog.setText(Messages.CallStackView_ImportMappingDialogTitle);
                String filePath = dialog.open();
                if (filePath == null) {
                    /* No file was selected, don't change anything */
                    return;
                }
                /*
                 * Start the mapping import in a separate thread (we do not want
                 * to UI thread to do this).
                 */
                Job job = new ImportMappingJob(new File(filePath));
                job.schedule();
            }
        };

        fImportMappingAction.setText(Messages.CallStackView_ImportMappingButtonText);
        fImportMappingAction.setToolTipText(Messages.CallStackView_ImportMappingButtonTooltip);
        fImportMappingAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(IMPORT_MAPPING_ICON_PATH));

        return fImportMappingAction;
    }

    private class ImportMappingJob extends Job {
        private final File fMappingFile;

        public ImportMappingJob(File mappingFile) {
            super(Messages.CallStackView_ImportMappingJobName);
            fMappingFile = mappingFile;
        }

        @Override
        public IStatus run(IProgressMonitor monitor) {
            fNameMapping = FunctionNameMapper.mapFromNmTextFile(fMappingFile);

            /* Refresh the time graph and the list of entries */
            buildThreadList(fTrace, new NullProgressMonitor());
            redraw();

            return Status.OK_STATUS;
        }
    }

    String getFunctionName(String address) {
        if (fNameMapping == null) {
            /* No mapping available, just print the addresses */
            return address;
        }
        String ret = fNameMapping.get(address);
        if (ret == null) {
            /* We didn't find this address in the mapping file, just use the address */
            return address;
        }
        return ret;
    }

}
