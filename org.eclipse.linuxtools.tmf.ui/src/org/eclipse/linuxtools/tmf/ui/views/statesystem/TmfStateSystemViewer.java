/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statesystem;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.viewers.tree.AbstractTmfTreeViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.linuxtools.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.linuxtools.tmf.ui.viewers.tree.TmfTreeViewerEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Displays the content of the state systems at the current time
 *
 * @author Florian Wininger
 * @author Alexandre Montplaisir
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfStateSystemViewer extends AbstractTmfTreeViewer {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private boolean fFilterStatus = false;
    private static final int DEFAULT_AUTOEXPAND = 2;

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
            if (element instanceof StateSystemEntry) {
                StateSystemEntry entry = (StateSystemEntry) element;
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
            if (element instanceof StateSystemEntry) {
                if (((StateSystemEntry) element).isModified()) {
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
    protected List<ITmfTreeViewerEntry> updateElements(long start, long end, boolean selection) {
        if (getTrace() == null) {
            return null;
        }

        List<ITmfTreeViewerEntry> entries = (List<ITmfTreeViewerEntry>) getInput();

        if ((!selection) && (entries != null)) {
            return null;
        }

        /*
         * Build the entries if it is the first time or to show only modified
         * values
         */
        if (entries == null || fFilterStatus) {
            entries = buildEntriesList(start);
        } else {
            /*
             * Update the values of the elements of the state systems at time
             * 'start'
             */
            entries = updateEntriesList(entries, start);
        }

        return entries;
    }

    private List<ITmfTreeViewerEntry> buildEntriesList(long timestamp) {
        List<ITmfTreeViewerEntry> rootEntries = new ArrayList<>();
        for (final ITmfTrace currentTrace : TmfTraceManager.getTraceSet(getTrace())) {
            if (currentTrace == null) {
                continue;
            }
            buildEntriesForTrace(currentTrace, timestamp, rootEntries);
        }
        return rootEntries;
    }

    /*
     * Update the values of the entries. It will also create trace and state
     * system entries if they do not exist yet.
     */
    private List<ITmfTreeViewerEntry> updateEntriesList(List<ITmfTreeViewerEntry> entries, long timestamp) {
        for (final ITmfTrace trace : TmfTraceManager.getTraceSet(getTrace())) {
            if (trace == null) {
                continue;
            }
            ITmfTreeViewerEntry traceEntry = null;
            for (ITmfTreeViewerEntry entry : entries) {
                if (entry.getName().equals(trace.getName())) {
                    traceEntry = entry;
                }
            }
            if (traceEntry == null) {
                traceEntry = buildEntriesForTrace(trace, timestamp, entries);
            }

            /* Find the state system entries for this trace */
            Iterable<ITmfAnalysisModuleWithStateSystems> modules = trace.getAnalysisModulesOfClass(ITmfAnalysisModuleWithStateSystems.class);
            for (ITmfAnalysisModuleWithStateSystems module : modules) {
                module.schedule();
                for (ITmfStateSystem ss : module.getStateSystems()) {
                    if (ss == null) {
                        continue;
                    }
                    ITmfTreeViewerEntry ssEntry = null;
                    for (ITmfTreeViewerEntry entry : traceEntry.getChildren()) {
                        if (entry.getName().equals(ss.getSSID())) {
                            ssEntry = entry;
                        }
                    }

                    if (ssEntry == null) {
                        /* The state system entry has not been built yet */
                        buildEntriesForStateSystem(ss, timestamp, (TmfTreeViewerEntry) traceEntry);
                    } else if (ssEntry.hasChildren()) {
                        /*
                         * Typical case at this point, update the data from the
                         * state system
                         */
                        updateEntriesForStateSystem(ss, timestamp, (TmfTreeViewerEntry) ssEntry);
                    } else {
                        /*
                         * The state system existed but entries were not filled,
                         * that would occur if for instance the values were out
                         * of range at the first query.
                         */
                        fillEntriesForStateSystem(ss, timestamp, (TmfTreeViewerEntry) ssEntry);
                    }
                }
            }
        }
        return entries;
    }

    @NonNull
    private ITmfTreeViewerEntry buildEntriesForTrace(@NonNull ITmfTrace trace, long timestamp, @NonNull List<ITmfTreeViewerEntry> rootEntries) {
        TmfTreeViewerEntry traceEntry = new TmfTreeViewerEntry(trace.getName());
        rootEntries.add(traceEntry);

        Iterable<ITmfAnalysisModuleWithStateSystems> modules = trace.getAnalysisModulesOfClass(ITmfAnalysisModuleWithStateSystems.class);
        for (ITmfAnalysisModuleWithStateSystems module : modules) {
            /* Just schedule the module, the data will be filled when available */
            module.schedule();
            for (ITmfStateSystem ss : module.getStateSystems()) {
                if (ss == null) {
                    continue;
                }
                buildEntriesForStateSystem(ss, timestamp, traceEntry);
            }
        }
        return traceEntry;
    }

    private void buildEntriesForStateSystem(ITmfStateSystem ss, long timestamp, TmfTreeViewerEntry traceEntry) {
        TmfTreeViewerEntry ssEntry = new TmfTreeViewerEntry(ss.getSSID());
        traceEntry.addChild(ssEntry);
        fillEntriesForStateSystem(ss, timestamp, ssEntry);
    }

    private void fillEntriesForStateSystem(ITmfStateSystem ss, long timestamp, TmfTreeViewerEntry ssEntry) {
        try {
            addChildren(ss, ss.queryFullState(timestamp), -1, ssEntry, timestamp);
        } catch (StateSystemDisposedException | TimeRangeException e) {
            /* Nothing to do */
        }
    }

    /**
     * Add children node to an entry. It will create all necessary entries.
     */
    private void addChildren(ITmfStateSystem ss, List<ITmfStateInterval> fullState, int rootQuark, TmfTreeViewerEntry root, long timestamp) {
        try {
            for (int quark : ss.getSubAttributes(rootQuark, false)) {

                ITmfStateInterval interval = fullState.get(quark);

                StateSystemEntry entry = new StateSystemEntry(ss.getAttributeName(quark), quark, ss.getFullAttributePath(quark),
                        interval.getStateValue(),
                        new TmfTimestamp(interval.getStartTime(), ITmfTimestamp.NANOSECOND_SCALE),
                        new TmfTimestamp(interval.getEndTime(), ITmfTimestamp.NANOSECOND_SCALE));

                /* Add this node's children recursively */
                addChildren(ss, fullState, quark, entry, timestamp);

                /**
                 * <pre>
                 * Do not add this entry to root if
                 * 1- the filter status is ON
                 * AND
                 * 2- the entry has no children
                 * AND
                 * 3- the start time is not the current timestamp
                 * </pre>
                 */
                if (!(fFilterStatus && !entry.hasChildren() && (interval.getStartTime() != timestamp))) {
                    root.addChild(entry);
                }
            }

        } catch (AttributeNotFoundException e) {
            /* Should not happen, we're iterating on known attributes */
            throw new RuntimeException();
        }
    }

    private void updateEntriesForStateSystem(ITmfStateSystem ss, long timestamp, TmfTreeViewerEntry ssEntry) {
        try {
            updateChildren(ss, ss.queryFullState(timestamp), ssEntry);
        } catch (StateSystemDisposedException e) {
        } catch (TimeRangeException e) {
            /* Mark all entries out of range */
            markOutOfRange(ssEntry);
        }
    }

    /**
     * Update the values of existing entries.
     */
    private void updateChildren(ITmfStateSystem ss, List<ITmfStateInterval> fullState, ITmfTreeViewerEntry root) {
        for (ITmfTreeViewerEntry entry : root.getChildren()) {
            if (entry instanceof StateSystemEntry) {
                /*
                 * FIXME: if new sub attributes were added since the element was
                 * built, then then will not be added
                 */
                StateSystemEntry ssEntry = (StateSystemEntry) entry;
                ITmfStateInterval interval = fullState.get(ssEntry.getQuark());
                if (interval != null) {
                    ssEntry.update(interval.getStateValue(), new TmfTimestamp(interval.getStartTime(), ITmfTimestamp.NANOSECOND_SCALE),
                            new TmfTimestamp(interval.getEndTime(), ITmfTimestamp.NANOSECOND_SCALE));
                }

                /* Update this node's children recursively */
                updateChildren(ss, fullState, ssEntry);
            }
        }
    }

    /**
     * Set the entries as out of range
     */
    private void markOutOfRange(ITmfTreeViewerEntry root) {
        for (ITmfTreeViewerEntry entry : root.getChildren()) {
            if (entry instanceof StateSystemEntry) {
                ((StateSystemEntry) entry).setOutOfRange();

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

    private class StateSystemEntry extends TmfTreeViewerEntry {

        private final int fQuark;
        private final String fFullPath;
        private @NonNull TmfTimestamp fStart;
        private @NonNull TmfTimestamp fEnd;
        private ITmfStateValue fValue;
        private boolean fModified = false;
        private boolean fOutOfRange = false;

        public StateSystemEntry(String name, int quark, String fullPath, ITmfStateValue value, @NonNull TmfTimestamp start, @NonNull TmfTimestamp end) {
            super(name);
            fQuark = quark;
            fFullPath = fullPath;
            fStart = start;
            fEnd = end;
            fValue = value;
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
            case NULL:
            default:
                return EMPTY_STRING;
            }
        }

        public boolean isModified() {
            return fModified;
        }

        public void update(ITmfStateValue value, @NonNull TmfTimestamp start, @NonNull TmfTimestamp end) {
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
