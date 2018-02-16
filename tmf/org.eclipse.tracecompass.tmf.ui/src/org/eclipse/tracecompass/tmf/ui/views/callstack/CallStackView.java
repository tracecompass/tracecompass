/*******************************************************************************
 * Copyright (c) 2013, 2017 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Updated signal handling
 *   Marc-Andre Laperle - Map from binary file
 *   Mikael Ferland - Support multiple symbol providers for a trace
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.callstack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.core.callstack.provider.CallStackDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.callstack.provider.CallStackEntryModel;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.symbols.ISymbolProvider;
import org.eclipse.tracecompass.tmf.core.symbols.SymbolProviderManager;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.editors.ITmfTraceEditor;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProviderPreferencePage;
import org.eclipse.tracecompass.tmf.ui.symbols.SymbolProviderConfigDialog;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphContentProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NamedTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry.Sampling;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

/**
 * Main implementation for the Call Stack view
 *
 * @author Patrick Tasse
 */
public class CallStackView extends AbstractTimeGraphView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** View ID. */
    public static final @NonNull String ID = "org.eclipse.linuxtools.tmf.ui.views.callstack"; //$NON-NLS-1$

    private static final String[] COLUMN_NAMES = new String[] {
            Messages.CallStackView_FunctionColumn,
            Messages.CallStackView_PidTidColumn,
            Messages.CallStackView_DepthColumn,
            Messages.CallStackView_EntryTimeColumn,
            Messages.CallStackView_ExitTimeColumn,
            Messages.CallStackView_DurationColumn
    };

    private static final Comparator<ITimeGraphEntry> NAME_COMPARATOR = Comparator.comparing(ITimeGraphEntry::getName);

    private static final Comparator<ITimeGraphEntry> THREAD_ID_COMPARATOR = (o1, o2) -> {
        if (o1 instanceof TimeGraphEntry && o2 instanceof TimeGraphEntry) {
            TimeGraphEntry t1 = (TimeGraphEntry) o1;
            TimeGraphEntry t2 = (TimeGraphEntry) o2;
            ITimeGraphEntryModel model1 = t1.getModel();
            ITimeGraphEntryModel model2 = t2.getModel();
            if (model1 instanceof CallStackEntryModel && model2 instanceof CallStackEntryModel) {
                CallStackEntryModel m1 = (CallStackEntryModel) model1;
                CallStackEntryModel m2 = (CallStackEntryModel) model2;
                if (m1.getStackLevel() == 0 && m2.getStackLevel() == 0) {
                    return Long.compare(m1.getPid(), m2.getPid());
                }
            }
        }
        return 0;
    };

    private static final Comparator<ITimeGraphEntry> DEPTH_COMPARATOR = (o1, o2) -> {
        if (o1 instanceof TimeGraphEntry && o2 instanceof TimeGraphEntry) {
            TimeGraphEntry t1 = (TimeGraphEntry) o1;
            TimeGraphEntry t2 = (TimeGraphEntry) o2;
            ITimeGraphEntryModel model1 = t1.getModel();
            ITimeGraphEntryModel model2 = t2.getModel();
            if (model1 instanceof CallStackEntryModel && model2 instanceof CallStackEntryModel) {
                return Integer.compare(((CallStackEntryModel) model1).getStackLevel(), ((CallStackEntryModel) model2).getStackLevel());
            }
        }
        return 0;
    };

    @SuppressWarnings("unchecked")
    private static final Comparator<ITimeGraphEntry>[] COMPARATORS = new Comparator[] {
            Comparator.comparing(ITimeGraphEntry::getName),
            THREAD_ID_COMPARATOR,
            DEPTH_COMPARATOR,
            Comparator.comparingLong(ITimeGraphEntry::getStartTime),
            Comparator.comparingLong(ITimeGraphEntry::getEndTime)
    };

    private static final String[] FILTER_COLUMN_NAMES = new String[] {
            Messages.CallStackView_ThreadColumn
    };

    /** Timeout between updates in the build thread in ms */
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    private static final Image PROCESS_IMAGE = Activator.getDefault().getImageFromPath("icons/obj16/process_obj.gif"); //$NON-NLS-1$
    private static final Image THREAD_IMAGE = Activator.getDefault().getImageFromPath("icons/obj16/thread_obj.gif"); //$NON-NLS-1$
    private static final Image STACKFRAME_IMAGE = Activator.getDefault().getImageFromPath("icons/obj16/stckframe_obj.gif"); //$NON-NLS-1$

    private static final String IMPORT_BINARY_ICON_PATH = "icons/obj16/binaries_obj.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    // The next event action
    private Action fNextEventAction;

    // The previous event action
    private Action fPrevEventAction;

    // The next item action
    private Action fNextItemAction;

    // The previous item action
    private Action fPreviousItemAction;

    // The action to import a binary file mapping */
    private Action fConfigureSymbolsAction;

    // When set to true, syncToTime() will select the first call stack entry
    // whose current state start time exactly matches the sync time.
    private boolean fSyncSelection = false;

    private final Map<Long, ITimeGraphState> fFunctions = new HashMap<>();

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private static class TraceEntry extends TimeGraphEntry {
        private final @NonNull CallStackDataProvider fProvider;

        public TraceEntry(CallStackEntryModel model, @NonNull CallStackDataProvider provider) {
            super(model);
            fProvider = provider;
        }

        @Override
        public boolean hasTimeEvents() {
            return false;
        }

        public @NonNull CallStackDataProvider getProvider() {
            return fProvider;
        }
    }

    private class CallStackComparator implements Comparator<ITimeGraphEntry> {
        @Override
        public int compare(ITimeGraphEntry o1, ITimeGraphEntry o2) {
            if (o1 instanceof TimeGraphEntry && o2 instanceof TimeGraphEntry) {
                TimeGraphEntry t1 = (TimeGraphEntry) o1;
                TimeGraphEntry t2 = (TimeGraphEntry) o2;
                ITimeGraphEntryModel model1 = t1.getModel();
                ITimeGraphEntryModel model2 = t2.getModel();
                CallStackEntryModel m1 = (CallStackEntryModel) model1;
                CallStackEntryModel m2 = (CallStackEntryModel) model2;
                if (m1.getStackLevel() == 0 && m2.getStackLevel() == 0) {
                    return NAME_COMPARATOR.compare(t1, t2);
                } else if (m1.getStackLevel() == -1 && m2.getStackLevel() == -1) {
                    return Integer.compare(m1.getPid(), m2.getPid());
                }
            }
            return 0;
        }
    }

    private class CallStackTreeLabelProvider extends TreeLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0 && element instanceof TimeGraphEntry) {
                TimeGraphEntry entry = (TimeGraphEntry) element;
                ITimeGraphEntryModel entryModel = entry.getModel();
                if (entryModel instanceof CallStackEntryModel) {
                    CallStackEntryModel callStackEntryModel = (CallStackEntryModel) entryModel;
                    if (callStackEntryModel.getStackLevel() == CallStackEntryModel.PROCESS) {
                        return PROCESS_IMAGE;
                    } else if (callStackEntryModel.getStackLevel() == CallStackEntryModel.THREAD) {
                        return THREAD_IMAGE;
                    } else if (fFunctions.containsKey(entryModel.getId())) {
                        return STACKFRAME_IMAGE;
                    }
                }
            }
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof TraceEntry && columnIndex == 0) {
                return ((TraceEntry) element).getName();
            } else if (element instanceof TimeGraphEntry) {
                TimeGraphEntry entry = (TimeGraphEntry) element;
                ITimeGraphEntryModel model = entry.getModel();
                ITimeGraphState function = fFunctions.get(model.getId());
                if (columnIndex == 0 && (!(model instanceof CallStackEntryModel) ||
                        (model instanceof CallStackEntryModel && ((CallStackEntryModel) model).getStackLevel() <= 0))) {
                    // trace, process, threads
                    return entry.getName();
                }

                if (function != null) {
                    if (columnIndex == 0) {
                        // functions
                        return function.getLabel();
                    } else if (columnIndex == 2 && model instanceof CallStackEntryModel) {
                        return Integer.toString(((CallStackEntryModel) model).getStackLevel());
                    } else if (columnIndex == 3) {
                        return TmfTimestampFormat.getDefaulTimeFormat().format(function.getStartTime());
                    } else if (columnIndex == 4) {
                        return TmfTimestampFormat.getDefaulTimeFormat().format(function.getStartTime() + function.getDuration());
                    } else if (columnIndex == 5) {
                        return TmfTimestampFormat.getDefaulIntervalFormat().format(function.getDuration());
                    }
                } else if (model instanceof CallStackEntryModel) {
                    CallStackEntryModel callStackEntryModel = (CallStackEntryModel) model;
                    if (columnIndex == 1 && callStackEntryModel.getStackLevel() <= 0 && callStackEntryModel.getPid() >= 0) {
                        return Integer.toString(callStackEntryModel.getPid());
                    } else if (columnIndex == 3 && callStackEntryModel.getStackLevel() <= 0) {
                        return TmfTimestampFormat.getDefaulTimeFormat().format(model.getStartTime());
                    } else if (columnIndex == 4 && callStackEntryModel.getStackLevel() <= 0) {
                        return TmfTimestampFormat.getDefaulTimeFormat().format(model.getEndTime());
                    }

                }
            }
            return ""; //$NON-NLS-1$
        }
    }

    private class CallStackFilterContentProvider extends TimeGraphContentProvider {
        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof TraceEntry) {
                return super.hasChildren(element);
            }
            return false;
        }

        @Override
        public ITimeGraphEntry[] getChildren(Object parentElement) {
            if (parentElement instanceof TraceEntry) {
                return super.getChildren(parentElement);
            }
            return new ITimeGraphEntry[0];
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public CallStackView() {
        super(ID, new CallStackPresentationProvider());
        setTreeColumns(COLUMN_NAMES, COMPARATORS, 0);
        setTreeLabelProvider(new CallStackTreeLabelProvider());
        setEntryComparator(new CallStackComparator());
        setFilterColumns(FILTER_COLUMN_NAMES);
        setFilterContentProvider(new CallStackFilterContentProvider());
        setFilterLabelProvider(new CallStackTreeLabelProvider());
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        getTimeGraphViewer().addTimeListener(event -> synchingToTime(event.getBeginTime()));

        getTimeGraphViewer().getTimeGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent event) {
                ITimeGraphEntry selection = getTimeGraphViewer().getSelection();
                ITimeGraphState function = fFunctions.get(((TimeGraphEntry) selection).getModel().getId());
                if (function != null) {
                    long entryTime = function.getStartTime();
                    long exitTime = entryTime + function.getDuration();
                    TmfTimeRange range = new TmfTimeRange(TmfTimestamp.fromNanos(entryTime), TmfTimestamp.fromNanos(exitTime));
                    broadcast(new TmfWindowRangeUpdatedSignal(CallStackView.this, range, getTrace()));
                    getTimeGraphViewer().setStartFinishTime(entryTime, exitTime);
                    startZoomThread(entryTime, exitTime);
                }
            }
        });

        getTimeGraphViewer().getTimeGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                TimeGraphControl timeGraphControl = getTimeGraphViewer().getTimeGraphControl();
                ISelection selection = timeGraphControl.getSelection();
                if (selection instanceof IStructuredSelection) {
                    for (Object object : ((IStructuredSelection) selection).toList()) {
                        if (object instanceof NamedTimeEvent) {
                            NamedTimeEvent event = (NamedTimeEvent) object;
                            long startTime = event.getTime();
                            long endTime = startTime + event.getDuration();
                            TmfTimeRange range = new TmfTimeRange(TmfTimestamp.fromNanos(startTime), TmfTimestamp.fromNanos(endTime));
                            broadcast(new TmfWindowRangeUpdatedSignal(CallStackView.this, range, getTrace()));
                            getTimeGraphViewer().setStartFinishTime(startTime, endTime);
                            startZoomThread(startTime, endTime);
                            break;
                        }
                    }
                }
            }
        });

        IEditorPart editor = getSite().getPage().getActiveEditor();
        if (editor instanceof ITmfTraceEditor) {
            ITmfTrace trace = ((ITmfTraceEditor) editor).getTrace();
            if (trace != null) {
                traceSelected(new TmfTraceSelectedSignal(this, trace));
            }
        }
    }

    /**
     * Handler for the selection range signal.
     *
     * @param signal
     *            The incoming signal
     * @since 1.0
     */
    @Override
    @TmfSignalHandler
    public void selectionRangeUpdated(final TmfSelectionRangeUpdatedSignal signal) {
        fSyncSelection = true;
        super.selectionRangeUpdated(signal);
    }

    /**
     * @since 2.0
     */
    @Override
    @TmfSignalHandler
    public void windowRangeUpdated(final TmfWindowRangeUpdatedSignal signal) {
        if (signal.getSource() == this) {
            return;
        }
        super.windowRangeUpdated(signal);
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    /**
     * @since 2.1
     * @deprecated no need to link back and forth between the
     *             {@link CallStackPresentationProvider} and {@link CallStackView}
     *             anymore
     */
    @Deprecated
    @Override
    protected CallStackPresentationProvider getPresentationProvider() {
        /* Set to this type by the constructor */
        return (CallStackPresentationProvider) super.getPresentationProvider();
    }

    /**
     * @since 2.0
     */
    @Override
    protected void refresh() {
        super.refresh();
        updateConfigureSymbolsAction();
    }

    @Override
    protected void buildEntryList(final ITmfTrace trace, final ITmfTrace parentTrace, final IProgressMonitor monitor) {
        CallStackDataProvider provider = DataProviderManager.getInstance().getDataProvider(trace,
                CallStackDataProvider.ID, CallStackDataProvider.class);
        if (provider == null) {
            addUnavailableEntry(trace, parentTrace);
            return;
        }

        SubMonitor subMonitor = SubMonitor.convert(monitor);
        provider.resetFunctionNames(subMonitor);
        boolean complete = false;
        TraceEntry traceEntry = null;
        Map<Long, TimeGraphEntry> map = new HashMap<>();
        while (!complete && !subMonitor.isCanceled()) {
            TmfModelResponse<List<CallStackEntryModel>> response = provider.fetchTree(new TimeQueryFilter(0, Long.MAX_VALUE, 2), subMonitor);
            if (response.getStatus() == ITmfResponse.Status.FAILED) {
                Activator.getDefault().logError("Call Stack Data Provider failed: " + response.getStatusMessage()); //$NON-NLS-1$
                return;
            } else if (response.getStatus() == ITmfResponse.Status.CANCELLED) {
                return;
            }
            complete = response.getStatus() == ITmfResponse.Status.COMPLETED;

            List<CallStackEntryModel> model = response.getModel();
            if (model != null) {
                for (CallStackEntryModel entry : model) {
                    if (entry.getStackLevel() != CallStackEntryModel.TRACE) {
                        TimeGraphEntry uiEntry = map.get(entry.getId());
                        if (uiEntry != null) {
                            uiEntry.updateModel(entry);
                        } else {
                            uiEntry = new TimeGraphEntry(entry);
                            map.put(entry.getId(), uiEntry);
                            TimeGraphEntry parent = map.getOrDefault(entry.getParentId(), traceEntry);
                            parent.addChild(uiEntry);
                        }
                    } else {
                        setStartTime(Long.min(getStartTime(), entry.getStartTime()));
                        setEndTime(Long.max(getEndTime(), entry.getEndTime() + 1));

                        if (traceEntry != null) {
                            traceEntry.updateModel(entry);
                        } else {
                            traceEntry = new TraceEntry(entry, provider);
                            traceEntry.sortChildren(NAME_COMPARATOR);
                            addToEntryList(parentTrace, Collections.singletonList(traceEntry));
                        }
                    }
                }
                Objects.requireNonNull(traceEntry);
                long start = traceEntry.getStartTime();
                long end = traceEntry.getEndTime();
                final long resolution = Long.max(1, (end - start) / getDisplayWidth());
                zoomEntries(map.values(), start, end, resolution, subMonitor);
            }
            if (parentTrace.equals(getTrace())) {
                synchingToTime(getTimeGraphViewer().getSelectionBegin());
                refresh();
            }
            subMonitor.worked(1);

            if (!complete) {
                try {
                    Thread.sleep(BUILD_UPDATE_TIMEOUT);
                } catch (InterruptedException e) {
                    Activator.getDefault().logError("Failed to wait for data provider", e); //$NON-NLS-1$
                }
            }
        }
    }

    private void addUnavailableEntry(ITmfTrace trace, ITmfTrace parentTrace) {
        String name = Messages.CallStackView_StackInfoNotAvailable + ' ' + '(' + trace.getName() + ')';
        TimeGraphEntry unavailableEntry = new TimeGraphEntry(name, 0, 0) {
            @Override
            public boolean hasTimeEvents() {
                return false;
            }
        };
        addToEntryList(parentTrace, Collections.singletonList(unavailableEntry));
        if (parentTrace == getTrace()) {
            refresh();
        }
    }

    @Override
    protected void zoomEntries(@NonNull Iterable<@NonNull TimeGraphEntry> entries, long zoomStartTime, long zoomEndTime,
            long resolution, @NonNull IProgressMonitor monitor) {
        if (resolution < 0) {
            // StateSystemUtils.getTimes would throw an illegal argument exception.
            return;
        }

        long start = Long.min(zoomStartTime, zoomEndTime);
        long end = Long.max(zoomStartTime, zoomEndTime);
        List<@NonNull Long> times = StateSystemUtils.getTimes(start, end, resolution);
        Sampling sampling = new Sampling(start, end, resolution);
        Table<CallStackDataProvider, Long, TimeGraphEntry> callStackEntries = filterGroup(entries);
        SubMonitor subMonitor = SubMonitor.convert(monitor, "CallStackView#zoomEntries", callStackEntries.rowKeySet().size()); //$NON-NLS-1$

        for (Map.Entry<CallStackDataProvider, Map<Long, TimeGraphEntry>> entry : callStackEntries.rowMap().entrySet()) {
            CallStackDataProvider dataProvider = entry.getKey();
            Map<Long, TimeGraphEntry> map = entry.getValue();
            SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(times, map.keySet());
            TmfModelResponse<List<ITimeGraphRowModel>> response = dataProvider.fetchRowModel(filter, monitor);

            List<ITimeGraphRowModel> model = response.getModel();
            if (model != null) {
                zoomEntries(map, model, response.getStatus() == ITmfResponse.Status.COMPLETED, sampling, zoomEndTime);
            }
            subMonitor.worked(1);
        }
    }

    private void zoomEntries(Map<Long, TimeGraphEntry> map, List<ITimeGraphRowModel> model, boolean completed, Sampling sampling, long zoomEndTime) {
        boolean isZoomThread = Thread.currentThread() instanceof ZoomThread;
        for (ITimeGraphRowModel rowModel : model) {
            TimeGraphEntry callStackEntry = map.get(rowModel.getEntryID());
            if (callStackEntry != null) {
                List<ITimeEvent> events = createTimeEvents(callStackEntry, rowModel.getStates(), zoomEndTime);
                if (isZoomThread) {
                    applyResults(() -> {
                        callStackEntry.setZoomedEventList(events);
                        if (completed) {
                            callStackEntry.setSampling(sampling);
                        }
                    });
                } else {
                    callStackEntry.setEventList(events);
                }
            }
        }
    }

    private static Table<CallStackDataProvider, Long, TimeGraphEntry> filterGroup(@NonNull Iterable<@NonNull TimeGraphEntry> entries) {
        Iterable<TimeGraphEntry> timeGraphEntries = Iterables.filter(entries, TimeGraphEntry.class);
        Iterable<TimeGraphEntry> callStackEntries = Iterables.filter(timeGraphEntries, tge -> tge.getModel() instanceof CallStackEntryModel);
        Iterable<TimeGraphEntry> functionEntries = Iterables.filter(callStackEntries,
                e -> ((CallStackEntryModel) e.getModel()).getStackLevel() > 0);
        Table<CallStackDataProvider, Long, TimeGraphEntry> table = HashBasedTable.create();
        for (TimeGraphEntry entry : functionEntries) {
            table.put(getProvider(entry), entry.getModel().getId(), entry);
        }
        return table;
    }

    /**
     * Get the {@link CallStackDataProvider} from a {@link TimeGraphEntry}'s
     * parent.
     *
     * @param entry
     *            queried {@link TimeGraphEntry}.
     * @return the {@link CallStackDataProvider}
     * @since 3.3
     */
    public static @NonNull CallStackDataProvider getProvider(TimeGraphEntry entry) {
        ITimeGraphEntry parent = entry;
        while (parent != null) {
            if (parent instanceof TraceEntry) {
                return ((TraceEntry) parent).getProvider();
            }
            parent = parent.getParent();
        }
        throw new IllegalStateException(entry + " should have a TraceEntry parent"); //$NON-NLS-1$
    }

    private static List<ITimeEvent> createTimeEvents(TimeGraphEntry callStackEntry, @NonNull List<@NonNull ITimeGraphState> values, long endTime) {
        List<ITimeEvent> events = new ArrayList<>(values.size());
        long lastEndTime = -1;
        boolean lastIsNull = false;
        boolean isZoomThread = Thread.currentThread() instanceof ZoomThread;
        final int modulo = CallStackPresentationProvider.NUM_COLORS / 2;
        for (ITimeGraphState state : values) {
            long time = state.getStartTime();
            long duration = state.getDuration();
            if (state.getValue() != Integer.MIN_VALUE) {
                int value = ((int) state.getValue()) % modulo + modulo;
                String label = state.getLabel();
                if (label != null) {
                    events.add(new NamedTimeEvent(callStackEntry, time, duration, value, label));
                } else {
                    events.add(new TimeEvent(callStackEntry, time, duration, value));
                }
                lastIsNull = false;
            } else {
                if (isZoomThread && (lastEndTime == -1 || time + duration >= endTime)) {
                    // add null event if it intersects the start time or end time:
                    events.add(new NullTimeEvent(callStackEntry, time, duration));
                } else if (lastEndTime != time && lastIsNull) {
                    // add unknown event if between two null states
                    events.add(new TimeEvent(callStackEntry, lastEndTime, time - lastEndTime));
                }
                lastIsNull = true;
            }
            lastEndTime = time + duration;
        }
        return events;
    }

    /**
     * @since 1.2
     */
    @Override
    protected void synchingToTime(final long time) {
        List<TimeGraphEntry> traceEntries = getEntryList(getTrace());
        if (traceEntries != null) {
            for (TraceEntry traceEntry : Iterables.filter(traceEntries, TraceEntry.class)) {
                Iterable<TimeGraphEntry> unfiltered = Iterables.filter(Utils.flatten(traceEntry), TimeGraphEntry.class);
                Map<Long, TimeGraphEntry> map = Maps.uniqueIndex(unfiltered, e -> e.getModel().getId());
                // use time -1 as a lower bound for the end of Time events to be included.
                SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(time - 1, time, 2, map.keySet());
                TmfModelResponse<@NonNull List<@NonNull ITimeGraphRowModel>> response = traceEntry.getProvider().fetchRowModel(filter, null);
                List<@NonNull ITimeGraphRowModel> model = response.getModel();
                if (model != null) {
                    for (ITimeGraphRowModel row : model) {
                        syncToRow(row, time, map);
                    }
                }
            }
        }
        fSyncSelection = false;
        if (Display.getCurrent() != null) {
            getTimeGraphViewer().refresh();
        }
    }

    private void syncToRow(ITimeGraphRowModel rowModel, long time, Map<Long, TimeGraphEntry> entryMap) {
        long id = rowModel.getEntryID();
        List<@NonNull ITimeGraphState> list = rowModel.getStates();
        if (!list.isEmpty()) {
            ITimeGraphState event = list.get(0);
            if (event.getStartTime() + event.getDuration() <= time && list.size() > 1) {
                /*
                 * get the second time graph state as passing time - 1 as a first argument to
                 * the filter will get the previous state, if time is the beginning of an event
                 */
                event = list.get(1);
            }
            if (event.getLabel() != null) {
                fFunctions.put(id, event);
            } else {
                fFunctions.remove(id);
            }

            if (fSyncSelection && time == event.getStartTime()) {
                TimeGraphEntry entry = entryMap.get(id);
                if (entry != null) {
                    fSyncSelection = false;
                    Display.getDefault().asyncExec(() -> {
                        getTimeGraphViewer().setSelection(entry, true);
                        getTimeGraphViewer().getTimeGraphControl().fireSelectionChanged();
                    });
                }
            }
        } else {
            fFunctions.remove(id);
        }
    }

    private void makeActions() {
        fPreviousItemAction = getTimeGraphViewer().getPreviousItemAction();
        fPreviousItemAction.setText(Messages.TmfTimeGraphViewer_PreviousItemActionNameText);
        fPreviousItemAction.setToolTipText(Messages.TmfTimeGraphViewer_PreviousItemActionToolTipText);
        fNextItemAction = getTimeGraphViewer().getNextItemAction();
        fNextItemAction.setText(Messages.TmfTimeGraphViewer_NextItemActionNameText);
        fNextItemAction.setToolTipText(Messages.TmfTimeGraphViewer_NextItemActionToolTipText);
    }

    /**
     * @since 1.2
     */
    @Override
    protected void fillLocalToolBar(IToolBarManager manager) {
        makeActions();
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getConfigureSymbolsAction());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, new Separator());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getTimeGraphViewer().getShowFilterDialogAction());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, new Separator());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getTimeGraphViewer().getResetScaleAction());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getPreviousEventAction());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getNextEventAction());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, new Separator());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getTimeGraphViewer().getToggleBookmarkAction());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getTimeGraphViewer().getPreviousMarkerAction());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getTimeGraphViewer().getNextMarkerAction());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, new Separator());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, fPreviousItemAction);
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, fNextItemAction);
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getTimeGraphViewer().getZoomInAction());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getTimeGraphViewer().getZoomOutAction());
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
                    TimeGraphViewer viewer = getTimeGraphViewer();
                    ITimeGraphEntry entry = viewer.getSelection();
                    if (entry instanceof TimeGraphEntry) {
                        TimeGraphEntry callStackEntry = (TimeGraphEntry) entry;
                        CallStackDataProvider provider = getProvider(callStackEntry);
                        long selectionBegin = viewer.getSelectionBegin();
                        SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(selectionBegin, Long.MAX_VALUE, 2, Collections.singleton(callStackEntry.getModel().getId()));
                        TmfModelResponse<@NonNull List<@NonNull ITimeGraphRowModel>> response = provider.fetchRowModel(filter, null);
                        List<@NonNull ITimeGraphRowModel> model = response.getModel();
                        if (model == null || model.size() != 1) {
                            return;
                        }
                        List<@NonNull ITimeGraphState> row = model.get(0).getStates();
                        if (row.size() != 1) {
                            return;
                        }
                        ITimeGraphState stackInterval = row.get(0);
                        if (stackInterval.getStartTime() <= selectionBegin && selectionBegin <= stackInterval.getStartTime() + stackInterval.getDuration()) {
                            viewer.setSelectedTimeNotify(stackInterval.getStartTime() + stackInterval.getDuration() + 1, true);
                        } else {
                            viewer.setSelectedTimeNotify(stackInterval.getStartTime(), true);
                        }
                        int stackLevel = (int) stackInterval.getValue();
                        ITimeGraphEntry selectedEntry = callStackEntry.getParent().getChildren().get(Integer.max(0, stackLevel - 1));
                        viewer.setSelection(selectedEntry, true);
                        viewer.getTimeGraphControl().fireSelectionChanged();
                        startZoomThread(viewer.getTime0(), viewer.getTime1());
                    }
                }
            };

            fNextEventAction.setText(Messages.TmfTimeGraphViewer_NextStateChangeActionNameText);
            fNextEventAction.setToolTipText(Messages.TmfTimeGraphViewer_NextStateChangeActionToolTipText);
            fNextEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEXT_STATE_CHANGE));
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
                    TimeGraphViewer viewer = getTimeGraphViewer();
                    ITimeGraphEntry entry = viewer.getSelection();
                    if (entry instanceof TimeGraphEntry) {
                        TimeGraphEntry callStackEntry = (TimeGraphEntry) entry;
                        CallStackDataProvider provider = getProvider(callStackEntry);
                        long selectionBegin = viewer.getSelectionBegin();
                        SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(Lists.newArrayList(Long.MIN_VALUE, selectionBegin), Collections.singleton(callStackEntry.getModel().getId()));
                        TmfModelResponse<@NonNull List<@NonNull ITimeGraphRowModel>> response = provider.fetchRowModel(filter, null);
                        List<@NonNull ITimeGraphRowModel> model = response.getModel();
                        if (model == null || model.size() != 1) {
                            return;
                        }
                        List<@NonNull ITimeGraphState> row = model.get(0).getStates();
                        if (row.size() != 1) {
                            return;
                        }
                        ITimeGraphState stackInterval = row.get(0);
                        viewer.setSelectedTimeNotify(stackInterval.getStartTime(), true);
                        int stackLevel = (int) stackInterval.getValue();
                        ITimeGraphEntry selectedEntry = callStackEntry.getParent().getChildren().get(Integer.max(0, stackLevel - 1));
                        viewer.setSelection(selectedEntry, true);
                        viewer.getTimeGraphControl().fireSelectionChanged();
                        startZoomThread(viewer.getTime0(), viewer.getTime1());
                    }
                }
            };

            fPrevEventAction.setText(Messages.TmfTimeGraphViewer_PreviousStateChangeActionNameText);
            fPrevEventAction.setToolTipText(Messages.TmfTimeGraphViewer_PreviousStateChangeActionToolTipText);
            fPrevEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PREV_STATE_CHANGE));
        }

        return fPrevEventAction;
    }

    // ------------------------------------------------------------------------
    // Methods related to function name mapping
    // ------------------------------------------------------------------------

    private Action getConfigureSymbolsAction() {
        if (fConfigureSymbolsAction != null) {
            return fConfigureSymbolsAction;
        }

        fConfigureSymbolsAction = new Action(Messages.CallStackView_ConfigureSymbolProvidersText) {
            @Override
            public void run() {
                SymbolProviderConfigDialog dialog = new SymbolProviderConfigDialog(getSite().getShell(), getProviderPages());
                if (dialog.open() == IDialogConstants.OK_ID) {
                    List<TimeGraphEntry> traceEntries = getEntryList(getTrace());
                    if (traceEntries != null) {
                        for (TraceEntry traceEntry : Iterables.filter(traceEntries, TraceEntry.class)) {
                            traceEntry.getProvider().resetFunctionNames(new NullProgressMonitor());

                            // reset full and zoomed events here
                            Iterable<TimeGraphEntry> flatten = Utils.flatten(traceEntry);
                            flatten.forEach(e -> e.setSampling(null));

                            // recompute full events
                            long start = traceEntry.getStartTime();
                            long end = traceEntry.getEndTime();
                            final long resolution = Long.max(1, (end - start) / getDisplayWidth());
                            zoomEntries(flatten, start, end, resolution, new NullProgressMonitor());
                        }
                        // zoomed events will be retriggered by refreshing
                        refresh();
                    }
                    synchingToTime(getTimeGraphViewer().getSelectionBegin());
                }
            }
        };

        fConfigureSymbolsAction.setToolTipText(Messages.CallStackView_ConfigureSymbolProvidersTooltip);
        fConfigureSymbolsAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(IMPORT_BINARY_ICON_PATH));

        /*
         * The updateConfigureSymbolsAction() method (called by refresh()) will
         * set the action to true if applicable after the symbol provider has
         * been properly loaded.
         */
        fConfigureSymbolsAction.setEnabled(false);

        return fConfigureSymbolsAction;
    }

    /**
     * @return an array of {@link ISymbolProviderPreferencePage} that will
     *         configure the current traces
     */
    private ISymbolProviderPreferencePage[] getProviderPages() {
        List<ISymbolProviderPreferencePage> pages = new ArrayList<>();
        ITmfTrace trace = getTrace();
        if (trace != null) {
            for (ITmfTrace subTrace : getTracesToBuild(trace)) {
                Collection<@NonNull ISymbolProvider> symbolProviders = SymbolProviderManager.getInstance().getSymbolProviders(subTrace);
                for (org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProvider provider : Iterables.filter(symbolProviders, org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProvider.class)) {
                    ISymbolProviderPreferencePage page = provider.createPreferencePage();
                    if (page != null) {
                        pages.add(page);
                    }
                }
            }
        }
        return pages.toArray(new ISymbolProviderPreferencePage[pages.size()]);
    }

    /**
     * Update the enable status of the configure symbols action
     */
    private void updateConfigureSymbolsAction() {
        ISymbolProviderPreferencePage[] providerPages = getProviderPages();
        getConfigureSymbolsAction().setEnabled(providerPages.length > 0);
    }

}
