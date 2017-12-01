/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *   Alexandre Montplaisir - Refactoring, performance tweaks
 *   Bernd Hufmann - Updated signal handling
 *   Marc-Andre Laperle - Add time zone preference
 *   Geneviève Bastien - Use a tree viewer instead of a tree
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.statesystem;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfStartAnalysisSignal;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry.Sampling;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.ui.IWorkbenchActionConstants;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import com.google.common.collect.TreeMultimap;

/**
 * Displays the State System at a current time.
 *
 * @author Florian Wininger
 * @author Alexandre Montplaisir
 * @author Loic Prieur-Drevon - make extend {@link AbstractTimeGraphView}
 */
public class TmfStateSystemExplorer extends AbstractTimeGraphView {

    private static final String HT_EXTENSION = ".ht"; //$NON-NLS-1$

    /** The Environment View's ID */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.ssvisualizer"; //$NON-NLS-1$

    private static final Image FILTER_IMAGE =
            Activator.getDefault().getImageFromPath( File.separator + "icons" +  File.separator + "elcl16" +  File.separator + "filter_items.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private static final String[] COLUMN_NAMES = new String[] {
            Messages.TreeNodeColumnLabel,
            Messages.QuarkColumnLabel,
            Messages.ValueColumnLabel
    };

    private static final Comparator<ITimeGraphEntry> NAME_COMPARATOR = (a, b) -> {
        if (a instanceof AttributeEntry && b instanceof AttributeEntry) {
            return a.getName().compareTo(b.getName());

        }
        return 0;
    };

    private static final Comparator<ITimeGraphEntry> QUARK_COMPARATOR = (a, b) -> {
        if (a instanceof AttributeEntry && b instanceof AttributeEntry) {
            return Integer.compare(((AttributeEntry) a).getQuark(), ((AttributeEntry) b).getQuark());

        }
        return 0;
    };

    // Puts the experiment entries at the top of the list
    private static final Comparator<ITimeGraphEntry> TRACE_ENTRY_COMPARATOR = (a, b) -> {
        if (a instanceof TraceEntry && b instanceof TraceEntry) {
            TraceEntry ta = (TraceEntry) a;
            TraceEntry tb = (TraceEntry) b;
            if (ta.fEntryTrace instanceof TmfExperiment) {
                return (tb.fEntryTrace instanceof TmfExperiment) ? a.getName().compareTo(b.getName()) : 1;
            }
            return (tb.fEntryTrace instanceof TmfExperiment) ? -1 : a.getName().compareTo(b.getName());

        }
        return a.getName().compareTo(b.getName());
    };

    private static final Comparator<ITimeGraphEntry>[] COLUMN_COMPARATORS;
    static {
        ImmutableList.Builder<Comparator<ITimeGraphEntry>> builder = ImmutableList.builder();
        builder.add(NAME_COMPARATOR)
                .add(QUARK_COMPARATOR);
        List<Comparator<ITimeGraphEntry>> l = builder.build();
        COLUMN_COMPARATORS = l.toArray(new Comparator[l.size()]);
    }

    private static final int QUARK_COLUMN_INDEX = 1;

    private static final int ITERATION_WAIT = 500;
    /**
     * Setting the auto expand level to 2 shows all entries down to the state
     * systems.
     */
    private static final int DEFAULT_AUTOEXPAND = 2;

    private class FilterAction extends Action {
        public FilterAction(String text, int style) {
            super(text, style);
        }

        @Override
        public void run() {
            boolean showAll = isChecked();
            setAutoExpandLevel(showAll ? AbstractTreeViewer.ALL_LEVELS : DEFAULT_AUTOEXPAND);
            List<@NonNull TimeGraphEntry> traceEntries = getEntryList(getTrace());
            if (traceEntries == null) {
                return;
            }
            TimeGraphControl control = getTimeGraphViewer().getTimeGraphControl();
            if (showAll) {
                Iterables.concat(Iterables.transform(traceEntries, Utils::flatten))
                        .forEach(e -> control.setExpandedState(e, true));
            } else {
                traceEntries.forEach(trace -> expandUpToStateSystems(control, trace));
            }
            redraw();
        }

        private void expandUpToStateSystems(TimeGraphControl control, TimeGraphEntry traceEntry) {
            control.setExpandedState(traceEntry, true);
            for (TimeGraphEntry moduleEntry : traceEntry.getChildren()) {
                control.setExpandedState(moduleEntry, true);
                moduleEntry.getChildren().forEach(ss -> control.setExpandedState(ss, false));
            }
        }
    }

    private static class StateSystemTreeLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof TimeGraphEntry) {
                TimeGraphEntry entry = (TimeGraphEntry) element;
                if (columnIndex == 0) {
                    return entry.getName();
                } else if (columnIndex == 1 && entry instanceof AttributeEntry) {
                    return Integer.toString(((AttributeEntry) entry).getQuark());
                } else if (columnIndex == 2 && entry instanceof AttributeEntry) {
                    List<ITmfStateInterval> fullState = StateSystemEntry.getFullStates(entry);
                    if (fullState != null) {
                        ITmfStateInterval interval = fullState.get(((AttributeEntry) entry).getQuark());
                        return String.valueOf(interval.getValue());
                    }
                }
            }
            return ""; //$NON-NLS-1$
        }
    }

    static class TraceEntry extends TimeGraphEntry {

        private final ITmfTrace fEntryTrace;

        public TraceEntry(ITmfTrace trace) {
            super(trace.getName(), trace.getStartTime().toNanos(), trace.getStartTime().toNanos());
            fEntryTrace = trace;
        }

        @Override
        public boolean hasTimeEvents() {
            return false;
        }
    }

    static class ModuleEntry extends TimeGraphEntry {
        private final ITmfAnalysisModuleWithStateSystems fModule;

        public ModuleEntry(ITmfAnalysisModuleWithStateSystems module, long startTime) {
            super(module.getName(), startTime, startTime);
            fModule = module;
            addEvent(new TimeEvent(this, startTime, 0));
        }

        @Override
        public void updateEndTime(long endTime) {
            super.updateEndTime(endTime);
            addEvent(new TimeEvent(this, getStartTime(), endTime - getStartTime()));
        }

        public ITmfAnalysisModuleWithStateSystems getModule() {
            return fModule;
        }
    }

    static class StateSystemEntry extends TimeGraphEntry {
        private final ITmfStateSystem fSs;
        private List<ITmfStateInterval> fFullStates = null;

        public StateSystemEntry(ITmfStateSystem ss) {
            super(ss.getSSID(), ss.getStartTime(), ss.getCurrentEndTime());
            fSs = ss;
            addEvent(new TimeEvent(this, ss.getStartTime(), ss.getCurrentEndTime() - ss.getStartTime()));
        }

        @Override
        public void updateEndTime(long endTime) {
            super.updateEndTime(endTime);
            addEvent(new TimeEvent(this, getStartTime(), endTime - getStartTime()));
        }

        public ITmfStateSystem getStateSystem() {
            return fSs;
        }

        public void setFullStates(List<ITmfStateInterval> fullStates) {
            fFullStates = fullStates;
        }

        static List<ITmfStateInterval> getFullStates(TimeGraphEntry entry) {
            TimeGraphEntry parent = entry;
            while (parent != null) {
                if (parent instanceof StateSystemEntry) {
                    return ((StateSystemEntry) parent).fFullStates;
                }
                parent = parent.getParent();
            }
            return null;
        }
    }

    static class AttributeEntry extends TimeGraphEntry {
        private final int fQuark;

        public AttributeEntry(String name, long start, long end, int quark) {
            super(name, start, end);
            fQuark = quark;
        }

        public int getQuark() {
            return fQuark;
        }
    }

    /**
     * Set of {@link ITmfAnalysisModuleWithStateSystems} that were received by
     * {@link TmfStateSystemExplorer#handleAnalysisStarted(TmfStartAnalysisSignal)}.
     * These are non automatic analysis that the build entry must join on.
     */
    private final Set<ITmfAnalysisModuleWithStateSystems> fStartedAnalysis = ConcurrentHashMap.newKeySet();

    /**
     * Default constructor
     */
    public TmfStateSystemExplorer() {
        super(ID, new StateSystemPresentationProvider());
        setTreeColumns(COLUMN_NAMES, COLUMN_COMPARATORS, QUARK_COLUMN_INDEX);
        setTreeLabelProvider(new StateSystemTreeLabelProvider());
        setEntryComparator(TRACE_ENTRY_COMPARATOR);
        setAutoExpandLevel(DEFAULT_AUTOEXPAND);
    }

    @Override
    protected void fillLocalToolBar(IToolBarManager manager) {
        Action collapseExpand = new FilterAction(Messages.FilterButton, IAction.AS_CHECK_BOX);

        collapseExpand.setImageDescriptor(ImageDescriptor.createFromImage(FILTER_IMAGE));
        collapseExpand.setToolTipText(Messages.FilterButton);

        manager.add(collapseExpand);

        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getTimeGraphViewer().getResetScaleAction());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getTimeGraphViewer().getPreviousEventAction());
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getTimeGraphViewer().getNextEventAction());

    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        getTimeGraphViewer().addTimeListener(event -> synchingToTime(event.getBeginTime()));

        getTimeGraphViewer().getTimeGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent event) {
                ITimeGraphEntry selection = getTimeGraphViewer().getSelection();
                if (selection instanceof ModuleEntry && selection.getChildren().isEmpty()) {
                    /**
                     * Schedule the analysis if it has not run yet.
                     */
                    ITmfAnalysisModuleWithStateSystems module = ((ModuleEntry) selection).fModule;
                    module.schedule();
                }
                TimeGraphControl control = getTimeGraphViewer().getTimeGraphControl();
                boolean expandedState = control.getExpandedState(selection);
                control.setExpandedState(selection, !expandedState);
            }
        });
    }

    @Override
    protected void buildEntryList(@NonNull ITmfTrace trace, @NonNull ITmfTrace parentTrace, @NonNull IProgressMonitor monitor) {
        long start = trace.getStartTime().toNanos();
        long end = start;
        TraceEntry traceEntry = new TraceEntry(trace);
        addToEntryList(parentTrace, Collections.singletonList(traceEntry));

        Iterable<@NonNull ITmfAnalysisModuleWithStateSystems> modules = Iterables.
                filter(trace.getAnalysisModules(), ITmfAnalysisModuleWithStateSystems.class);

        for (ITmfAnalysisModuleWithStateSystems m : modules) {
            if (monitor.isCanceled()) {
                return;
            }
            waitForInitialization(trace, m);
        }

        boolean complete = false;
        while (!complete && !monitor.isCanceled()) {
            complete = true;
            for (ITmfAnalysisModuleWithStateSystems module : modules) {
                if (monitor.isCanceled()) {
                    return;
                }
                // Add the module as an entry to the trace
                ModuleEntry moduleEntry = getOrCreateModuleEntry(traceEntry, module);
                /*
                 * Add the state system as children of the module, they may not be initialized
                 * yet, the list will be empty in that case.
                 */
                for (ITmfStateSystem ss : module.getStateSystems()) {
                    complete &= ss.waitUntilBuilt(0);
                    end = Long.max(end, ss.getCurrentEndTime());
                    getOrCreateStateSystemEntry(moduleEntry, ss);
                }
            }

            traceEntry.updateEndTime(end);

            long resolution = Long.max(1, (end - start) / getDisplayWidth());
            zoomEntries(Utils.flatten(traceEntry), start, end, resolution, monitor);

            if (monitor.isCanceled()) {
                return;
            }

            if (parentTrace == getTrace()) {
                synchronized (this) {
                    setStartTime(Long.min(getStartTime(), start));
                    setEndTime(Long.max(getEndTime(), end));
                }
                refresh();
            }

            if (monitor.isCanceled()) {
                return;
            }

            if (!complete) {
                try {
                    Thread.sleep(ITERATION_WAIT);
                } catch (InterruptedException e) {
                    Activator.getDefault().logError("Failed to wait", e); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Wait for automatic, started and persisted analysis to be initialized.
     *
     * @param trace
     *            trace for which entries are being built.
     * @param module
     *            potential {@link ITmfAnalysisModuleWithStateSystems} to wait for
     */
    private void waitForInitialization(@NonNull ITmfTrace trace, ITmfAnalysisModuleWithStateSystems module) {
        if (module.isAutomatic() || fStartedAnalysis.remove(module)) {
            module.waitForInitialization();
            return;
        }
        /*
         * See if an analysis was already run by searching for its state history tree.
         * Scheduling the analysis will run it, and upon running, it will just open the
         * previous state system. We wait for the state system to be initialized. FIXME
         * if another naming convention is used, this might fail.
         */
        String dir = TmfTraceManager.getSupplementaryFileDir(trace);
        boolean exists = Paths.get(dir, module.getId() + HT_EXTENSION).toFile().exists();
        if (exists) {
            module.schedule();
            module.waitForInitialization();
        }
    }

    private static ModuleEntry getOrCreateModuleEntry(TraceEntry traceEntry, ITmfAnalysisModuleWithStateSystems module) {
        ModuleEntry moduleEntry = null;
        for (ModuleEntry entry : Iterables.filter(traceEntry.getChildren(), ModuleEntry.class)) {
            if (entry.getName().equals(module.getName())) {
                moduleEntry = entry;
                break;
            }
        }
        if (moduleEntry == null) {
            moduleEntry = new ModuleEntry(module, traceEntry.getStartTime());
            traceEntry.addChild(moduleEntry);
        }
        return moduleEntry;
    }

    private static StateSystemEntry getOrCreateStateSystemEntry(ModuleEntry moduleEntry, ITmfStateSystem ss) {
        StateSystemEntry ssEntry = null;
        long currentEndTime = ss.getCurrentEndTime();
        for (StateSystemEntry entry : Iterables.filter(moduleEntry.getChildren(), StateSystemEntry.class)) {
            if (entry.getStateSystem() == ss) {
                ssEntry = entry;
                ssEntry.updateEndTime(currentEndTime);
                break;
            }
        }
        if (ssEntry == null) {
            ssEntry = new StateSystemEntry(ss);
            moduleEntry.addChild(ssEntry);
        }
        moduleEntry.updateEndTime(currentEndTime);
        for (Integer q : ssEntry.fSs.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, false)) {
            getOrCreateAttributes(ssEntry, ssEntry.fSs, q, currentEndTime);
        }
        return ssEntry;
    }

    private static TimeGraphEntry getOrCreateAttributes(TimeGraphEntry parent, ITmfStateSystem ss, int quark, long end) {
        long start = ss.getStartTime();
        String name = ss.getAttributeName(quark);
        TimeGraphEntry entry = null;
        for (TimeGraphEntry e : parent.getChildren()) {
            if (name.equals(e.getName())) {
                entry = e;
                entry.updateEndTime(end);
                break;
            }
        }
        if (entry == null) {
            entry = new AttributeEntry(name, start, end, quark);
            parent.addChild(entry);
        }
        for (Integer child : ss.getSubAttributes(quark, false)) {
            getOrCreateAttributes(entry, ss, child, end);
        }
        return entry;
    }

    @Override
    protected void zoomEntries(@NonNull Iterable<@NonNull TimeGraphEntry> entries,
            long zoomStartTime, long zoomEndTime, long resolution, @NonNull IProgressMonitor monitor) {

        boolean isZoomThread = Thread.currentThread() instanceof ZoomThread;
        Table<ITmfStateSystem, Integer, TimeGraphEntry> table = filterGroupEntries(entries);
        TreeMultimap<Integer, ITmfStateInterval> intervals = TreeMultimap.create(Comparator.naturalOrder(),
                Comparator.comparingLong(ITmfStateInterval::getStartTime));

        for (Entry<ITmfStateSystem, Map<Integer, TimeGraphEntry>> ssEntries : table.rowMap().entrySet()) {
            ITmfStateSystem ss = ssEntries.getKey();
            /* Get the time stamps for the 2D query */
            long start = Long.max(zoomStartTime, ss.getStartTime());
            long end = Long.min(zoomEndTime, ss.getCurrentEndTime());
            if (start > end) {
                continue;
            }
            Sampling clampedSampling = new Sampling(start, end, resolution);
            List<Long> times = StateSystemUtils.getTimes(start, end, resolution);
            Map<Integer, TimeGraphEntry> quarksToEntries = ssEntries.getValue();
            /* Do the actual query */
            try {
                for (ITmfStateInterval interval : ss.query2D(quarksToEntries.keySet(), times)) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    intervals.put(interval.getAttribute(), interval);
                }
                for (Entry<Integer, TimeGraphEntry> entry : quarksToEntries.entrySet()) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    TimeGraphEntry tge = entry.getValue();
                    SortedSet<ITmfStateInterval> states = intervals.removeAll(entry.getKey());
                    List<ITimeEvent> events = createTimeEvents(tge, states);
                    if (isZoomThread) {
                        applyResults(() -> {
                            tge.setZoomedEventList(events);
                            tge.setSampling(clampedSampling);
                        });
                    } else {
                        tge.setEventList(events);
                    }
                }
            } catch (TimeRangeException e) {
                Activator.getDefault().logError("State System Explorer: incorrect query times for zoomEvent", e); //$NON-NLS-1$
            } catch (StateSystemDisposedException e) {
                // If the state system was disposed, the trace was closed, nothing to do here.
            } finally {
                intervals.clear();
            }
        }
    }

    private static Table<ITmfStateSystem, Integer, TimeGraphEntry> filterGroupEntries(Iterable<TimeGraphEntry> visible) {
        Table<ITmfStateSystem, Integer, TimeGraphEntry> quarksToEntries = HashBasedTable.create();
        for (AttributeEntry entry : Iterables.filter(visible, AttributeEntry.class)) {
            ITmfStateSystem ss = getStateSystem(entry);
            if (ss != null) {
                quarksToEntries.put(ss, entry.getQuark(), entry);
            }
        }
        return quarksToEntries;
    }

    private static List<ITimeEvent> createTimeEvents(TimeGraphEntry tge, SortedSet<ITmfStateInterval> intervals) {
        List<ITimeEvent> events = new ArrayList<>(intervals.size());
        ITimeEvent prev = null;
        for (ITmfStateInterval interval : intervals) {
            ITimeEvent event = new StateSystemEvent(tge, interval);
            if (prev != null) {
                long prevEnd = prev.getTime() + prev.getDuration();
                if (prevEnd < event.getTime()) {
                    // fill in the gap.
                    events.add(new TimeEvent(tge, prevEnd, event.getTime() - prevEnd));
                }
            }
            prev = event;
            events.add(event);
        }
        return events;
    }

    @Override
    protected void synchingToTime(long time) {
        List<@NonNull TimeGraphEntry> traceEntries = getEntryList(getTrace());
        if (traceEntries == null) {
            return;
        }
        Iterable<TimeGraphEntry> moduleEntries = Iterables.concat(Iterables.transform(traceEntries, TimeGraphEntry::getChildren));
        Iterable<TimeGraphEntry> stateSystemEntries = Iterables.concat(Iterables.transform(moduleEntries, TimeGraphEntry::getChildren));
        for (StateSystemEntry stateSystemEntry : Iterables.filter(stateSystemEntries, StateSystemEntry.class)) {
            /**
             * Cache the full state for this state system at time, if it exists, for reuse
             * by the tree label provider.
             */
            ITmfStateSystem ss = stateSystemEntry.getStateSystem();
            if (ss.getStartTime() <= time && time <= ss.getCurrentEndTime()) {
                try {
                    List<@NonNull ITmfStateInterval> full = ss.queryFullState(time);
                    stateSystemEntry.setFullStates(full);
                } catch (StateSystemDisposedException e) {
                    stateSystemEntry.setFullStates(null);
                }
            } else {
                stateSystemEntry.setFullStates(null);
            }
        }
        refresh();
    }

    /**
     * Get the {@link ITmfStateSystem} and path for an entry.
     *
     * @param entry
     *            any {@link TimeGraphEntry} from the tree
     * @return a {@link Pair} encapsulating both, else null if the entry was for a
     *         trace / module / state system.
     */
    static ITmfStateSystem getStateSystem(TimeGraphEntry entry) {
        TimeGraphEntry parent = entry;
        while (parent != null) {
            parent = parent.getParent();
            if (parent instanceof StateSystemEntry) {
                return ((StateSystemEntry) parent).getStateSystem();
            }
        }
        return null;
    }

    /**
     * Rebuild the view's entry tree to ensure that entries from a newly started
     * trace are added.
     *
     * @param signal
     *            analysis started signal.
     * @since 3.3
     */
    @TmfSignalHandler
    public void handleAnalysisStarted(TmfStartAnalysisSignal signal) {
        IAnalysisModule module = signal.getAnalysisModule();
        if (module instanceof ITmfAnalysisModuleWithStateSystems && !module.isAutomatic()) {
            /* use set to wait for initialization in build entry list to avoid deadlocks. */
            fStartedAnalysis.add((ITmfAnalysisModuleWithStateSystems) module);
            rebuild();
        }
    }

    @Override
    protected @NonNull Iterable<ITmfTrace> getTracesToBuild(@Nullable ITmfTrace trace) {
        return TmfTraceManager.getTraceSetWithExperiment(trace);
    }

}
