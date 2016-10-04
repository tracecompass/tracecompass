/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal and others.
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
 *   Geneviève Bastien - Moved state system explorer to use the abstract tree viewer
 *   Patrick Tasse - Refactoring
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.statesystem;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractTmfTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;

/**
 * Displays the content of the state systems at the current time
 *
 * @author Florian Wininger
 * @author Alexandre Montplaisir
 * @author Geneviève Bastien
 */
public class TmfStateSystemViewer extends AbstractTmfTreeViewer {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final int DEFAULT_AUTOEXPAND = 2;
    private boolean fFilterStatus = false;
    private long fSelection = 0;

    /* Order of columns */
    private static final int ATTRIBUTE_NAME_COL = 0;
    private static final int QUARK_COL = 1;
    private static final int VALUE_COL = 2;
    private static final int TYPE_COL = 3;
    private static final int START_TIME_COL = 4;
    private static final int END_TIME_COL = 5;
    private static final int ATTRIBUTE_FULLPATH_COL = 6;

    /**
     * Base class to provide the labels for the tree viewer. Views extending
     * this class typically need to override the getColumnText method if they
     * have more than one column to display
     */
    protected static class StateSystemTreeLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof StateEntry) {
                StateEntry entry = (StateEntry) element;
                switch (columnIndex) {
                case ATTRIBUTE_NAME_COL:
                    return entry.getName();
                case QUARK_COL:
                    return String.valueOf(entry.getQuark());
                case VALUE_COL:
                    return entry.getValue();
                case TYPE_COL:
                    return entry.getType();
                case START_TIME_COL:
                    return entry.getStartTime();
                case END_TIME_COL:
                    return entry.getEndTime();
                case ATTRIBUTE_FULLPATH_COL:
                    return entry.getFullPath();
                default:
                    return EMPTY_STRING;
                }
            }
            return super.getColumnText(element, columnIndex);
        }

        @Override
        public Color getBackground(Object element, int columnIndex) {
            if (element instanceof StateEntry) {
                if (((StateEntry) element).isModified()) {
                    return Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
                }
            }
            return super.getBackground(element, columnIndex);
        }
    }

    /**
     * Constructor
     *
     * @param parent
     *            The parent containing this viewer
     */
    public TmfStateSystemViewer(Composite parent) {
        super(parent, false);
        this.setLabelProvider(new StateSystemTreeLabelProvider());
        getTreeViewer().setAutoExpandLevel(DEFAULT_AUTOEXPAND);
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return new ITmfTreeColumnDataProvider() {

            @Override
            public List<TmfTreeColumnData> getColumnData() {
                List<TmfTreeColumnData> columns = new ArrayList<>();
                TmfTreeColumnData column = new TmfTreeColumnData(Messages.TreeNodeColumnLabel);
                columns.add(column);
                column.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        TmfTreeViewerEntry n1 = (TmfTreeViewerEntry) e1;
                        TmfTreeViewerEntry n2 = (TmfTreeViewerEntry) e2;

                        return n1.getName().compareTo(n2.getName());
                    }
                });
                columns.add(new TmfTreeColumnData(Messages.QuarkColumnLabel));
                columns.add(new TmfTreeColumnData(Messages.ValueColumnLabel));
                columns.add(new TmfTreeColumnData(Messages.TypeColumnLabel));
                columns.add(new TmfTreeColumnData(Messages.StartTimeColumLabel));
                columns.add(new TmfTreeColumnData(Messages.EndTimeColumLabel));
                columns.add(new TmfTreeColumnData(Messages.AttributePathColumnLabel));
                return columns;
            }

        };
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected ITmfTreeViewerEntry updateElements(long start, long end, boolean selection) {

        if (selection) {
            fSelection = start;
        } else {
            TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
            fSelection = ctx.getSelectionRange().getStartTime().toNanos();
        }

        if (getTrace() == null) {
            return null;
        }

        ITmfTreeViewerEntry root = getInput();

        if (root == null) {
            root = createRoot();
        } else if (fFilterStatus) {
            clearStateSystemEntries(root);
        }

        /*
         * Update the values of the elements of the state systems at the
         * selection start time
         */
        boolean changed = updateStateSystemEntries(root, fSelection);

        return selection || changed ? root : null;
    }

    private ITmfTreeViewerEntry createRoot() {
        // 'Fake' root node
        TmfTreeViewerEntry rootEntry = new TmfTreeViewerEntry("root"); //$NON-NLS-1$

        for (final ITmfTrace trace : TmfTraceManager.getTraceSetWithExperiment(getTrace())) {
            rootEntry.addChild(createTraceEntry(trace));
        }
        return rootEntry;
    }

    private static TmfTreeViewerEntry createTraceEntry(ITmfTrace trace) {
        TmfTreeViewerEntry traceEntry = new TmfTreeViewerEntry(trace.getName());
        Iterable<IAnalysisModule> modules = trace.getAnalysisModules();
        for (IAnalysisModule module : modules) {
            if (module instanceof ITmfAnalysisModuleWithStateSystems) {
                ITmfAnalysisModuleWithStateSystems moduleWithStateSystem = (ITmfAnalysisModuleWithStateSystems) module;
                // Add the module as an entry to the trace
                TmfTreeViewerEntry moduleEntry = new ModuleEntry(moduleWithStateSystem);
                traceEntry.addChild(moduleEntry);
                // Add the state system as children of the module, they may not
                // be initialized yet, the list will be empty in that case
                for (ITmfStateSystem ss : moduleWithStateSystem.getStateSystems()) {
                    moduleEntry.addChild(new StateSystemEntry(ss));
                }
            }
        }
        return traceEntry;
    }

    private static void clearStateSystemEntries(ITmfTreeViewerEntry root) {
        for (ITmfTreeViewerEntry traceEntry : root.getChildren()) {
            for (ITmfTreeViewerEntry ssEntry : traceEntry.getChildren()) {
                ssEntry.getChildren().clear();
            }
        }
    }

    private boolean updateStateSystemEntries(ITmfTreeViewerEntry root, long timestamp) {
        boolean changed = false;
        for (ITmfTreeViewerEntry traceEntry : root.getChildren()) {
            for (ITmfTreeViewerEntry moduleEntry : traceEntry.getChildren()) {
                // If there are no children, see if new state systems are
                // available now
                if (moduleEntry.getChildren().isEmpty()) {
                    for (ITmfStateSystem ss : ((ModuleEntry) moduleEntry).getModule().getStateSystems()) {
                        ((ModuleEntry) moduleEntry).addChild(new StateSystemEntry(ss));
                    }
                }
                for (ITmfTreeViewerEntry ssEntry : moduleEntry.getChildren()) {
                    StateSystemEntry stateSystemEntry = (StateSystemEntry) ssEntry;
                    ITmfStateSystem ss = stateSystemEntry.getSS();
                    try {
                        List<ITmfStateInterval> fullState = ss.queryFullState(timestamp);
                        changed |= updateStateEntries(ss, fullState, stateSystemEntry, -1, timestamp);
                    } catch (TimeRangeException e) {
                        markOutOfRange(stateSystemEntry);
                        changed = true;
                    } catch (StateSystemDisposedException e) {
                        /* Ignored */
                    }
                }
            }
        }
        return changed;
    }

    private boolean updateStateEntries(ITmfStateSystem ss, List<ITmfStateInterval> fullState, TmfTreeViewerEntry parent, int parentQuark, long timestamp) {
        boolean changed = false;
        for (int quark : ss.getSubAttributes(parentQuark, false)) {
            if (quark >= fullState.size()) {
                // attribute was created after the full state query
                continue;
            }
            ITmfStateInterval interval = fullState.get(quark);
            StateEntry stateEntry = findStateEntry(parent, quark);
            if (stateEntry == null) {
                boolean modified = fFilterStatus ?
                        interval.getStartTime() == timestamp :
                            !interval.getStateValue().isNull();
                stateEntry = new StateEntry(ss.getAttributeName(quark), quark, ss.getFullAttributePath(quark),
                        interval.getStateValue(),
                        TmfTimestamp.fromNanos(interval.getStartTime()),
                        TmfTimestamp.fromNanos(interval.getEndTime()),
                        modified);

                // update children first to know if parent is really needed
                updateStateEntries(ss, fullState, stateEntry, quark, timestamp);

                /*
                 * Add this entry to parent if filtering is off, or
                 * if the entry has children to display, or
                 * if there is a state change at the current timestamp
                 */
                if (!fFilterStatus || stateEntry.hasChildren() || interval.getStartTime() == timestamp) {
                    parent.addChild(stateEntry);
                    changed = true;
                }
            } else {
                stateEntry.update(interval.getStateValue(),
                        TmfTimestamp.fromNanos(interval.getStartTime()),
                        TmfTimestamp.fromNanos(interval.getEndTime()));

                // update children recursively
                updateStateEntries(ss, fullState, stateEntry, quark, timestamp);
            }

        }
        return changed;
    }

    private static StateEntry findStateEntry(TmfTreeViewerEntry parent, int quark) {
        for (ITmfTreeViewerEntry child : parent.getChildren()) {
            StateEntry stateEntry = (StateEntry) child;
            if (stateEntry.getQuark() == quark) {
                return stateEntry;
            }
        }
        return null;
    }
    /**
     * Set the entries as out of range
     */
    private static void markOutOfRange(ITmfTreeViewerEntry parent) {
        for (ITmfTreeViewerEntry entry : parent.getChildren()) {
            if (entry instanceof StateEntry) {
                ((StateEntry) entry).setOutOfRange();

                /* Update this node's children recursively */
                markOutOfRange(entry);
            }
        }
    }

    /**
     * Set the filter status of the viewer. By default, all entries of all state
     * system are present, and the values that changed since last refresh are
     * shown in yellow. When the filter status is true, only the entries with
     * values modified at current time are displayed.
     */
    public void changeFilterStatus() {
        fFilterStatus = !fFilterStatus;
        if (fFilterStatus) {
            getTreeViewer().setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        } else {
            getTreeViewer().setAutoExpandLevel(DEFAULT_AUTOEXPAND);
            clearContent();
        }
        updateContent(getSelectionBeginTime(), getSelectionEndTime(), true);
    }

    /**
     * Update the display to use the updated timestamp format
     *
     * @param signal
     *            the incoming signal
     */
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        updateContent(getSelectionBeginTime(), getSelectionEndTime(), true);
    }

    private static class StateSystemEntry extends TmfTreeViewerEntry {
        private final @NonNull ITmfStateSystem fSS;

        public StateSystemEntry(@NonNull ITmfStateSystem ss) {
            super(ss.getSSID());
            fSS = ss;
        }

        public @NonNull ITmfStateSystem getSS() {
            return fSS;
        }
    }

    private static class ModuleEntry extends TmfTreeViewerEntry {
        private final @NonNull ITmfAnalysisModuleWithStateSystems fModule;

        public ModuleEntry(@NonNull ITmfAnalysisModuleWithStateSystems moduleWithStateSystem) {
            super(moduleWithStateSystem.getName());
            fModule = moduleWithStateSystem;
        }

        public @NonNull ITmfAnalysisModuleWithStateSystems getModule() {
            return fModule;
        }
    }

    private class StateEntry extends TmfTreeViewerEntry {

        private final int fQuark;
        private final String fFullPath;
        private @NonNull ITmfTimestamp fStart;
        private @NonNull ITmfTimestamp fEnd;
        private ITmfStateValue fValue;
        private boolean fModified;
        private boolean fOutOfRange = false;

        public StateEntry(String name, int quark, String fullPath, ITmfStateValue value, @NonNull ITmfTimestamp start, @NonNull ITmfTimestamp end, boolean modified) {
            super(name);
            fQuark = quark;
            fFullPath = fullPath;
            fStart = start;
            fEnd = end;
            fValue = value;
            fModified = modified;
        }

        public int getQuark() {
            return fQuark;
        }

        public String getFullPath() {
            return fFullPath;
        }

        public String getStartTime() {
            if (fOutOfRange) {
                return EMPTY_STRING;
            }
            return fStart.toString();
        }

        public String getEndTime() {
            if (fOutOfRange) {
                return EMPTY_STRING;
            }
            return fEnd.toString();
        }

        public String getValue() {
            if (fOutOfRange) {
                return Messages.OutOfRangeMsg;
            }
            switch (fValue.getType()) {
            case INTEGER:
            case LONG:
            case DOUBLE:
            case STRING:
            case CUSTOM:
                return fValue.toString();
            case NULL:
            default:
                return EMPTY_STRING;
            }
        }

        public String getType() {
            if (fOutOfRange) {
                return EMPTY_STRING;
            }
            switch (fValue.getType()) {
            case INTEGER:
                return Messages.TypeInteger;
            case LONG:
                return Messages.TypeLong;
            case DOUBLE:
                return Messages.TypeDouble;
            case STRING:
                return Messages.TypeString;
            case CUSTOM:
                return Messages.TypeCustom;
            case NULL:
            default:
                return EMPTY_STRING;
            }
        }

        public boolean isModified() {
            return fModified;
        }

        public void update(ITmfStateValue value, @NonNull ITmfTimestamp start, @NonNull ITmfTimestamp end) {
            fModified = false;
            fOutOfRange = false;
            if (!start.equals(fStart)) {
                fModified = true;
                fStart = start;
                fEnd = end;
                fValue = value;
            }
        }

        public void setOutOfRange() {
            fModified = false;
            fOutOfRange = true;
        }
    }
}
