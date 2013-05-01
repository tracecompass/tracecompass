/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *   Alexandre Montplaisir - Refactoring, performance tweaks
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statesystem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Displays the State System at a current time.
 *
 * @author Florian Wininger
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfStateSystemExplorer extends TmfView {

    /** The Environment View's ID */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.ssview"; //$NON-NLS-1$

    private static final String emptyString = ""; //$NON-NLS-1$

    /* Order of columns */
    private static final int ATTRIBUTE_NAME_COL = 0;
    private static final int QUARK_COL = 1;
    private static final int VALUE_COL = 2;
    private static final int START_TIME_COL = 3;
    private static final int END_TIME_COL = 4;
    private static final int ATTRIBUTE_FULLPATH_COL = 5;

    private ITmfTrace fTrace;
    private Tree fTree;
    private volatile long fCurrentTimestamp = -1L;

    /**
     * Default constructor
     */
    public TmfStateSystemExplorer() {
        super(ID);
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {
        fTree = new Tree(parent, SWT.NONE);
        TreeColumn nameCol = new TreeColumn(fTree, SWT.NONE, ATTRIBUTE_NAME_COL);
        TreeColumn quarkCol = new TreeColumn(fTree, SWT.NONE, QUARK_COL);
        TreeColumn valueCol = new TreeColumn(fTree, SWT.NONE, VALUE_COL);
        TreeColumn startCol = new TreeColumn(fTree, SWT.NONE, START_TIME_COL);
        TreeColumn endCol = new TreeColumn(fTree, SWT.NONE, END_TIME_COL);
        TreeColumn pathCol = new TreeColumn(fTree, SWT.NONE, ATTRIBUTE_FULLPATH_COL);

        nameCol.setText(Messages.TreeNodeColumnLabel);
        quarkCol.setText(Messages.QuarkColumnLabel);
        valueCol.setText(Messages.ValueColumnLabel);
        startCol.setText(Messages.StartTimeColumLabel);
        endCol.setText(Messages.EndTimeColumLabel);
        pathCol.setText(Messages.AttributePathColumnLabel);

        fTree.setItemCount(0);

        fTree.setHeaderVisible(true);
        nameCol.pack();
        valueCol.pack();

        fTree.addListener(SWT.Expand, new Listener() {
            @Override
            public void handleEvent(Event e) {
                TreeItem item = (TreeItem) e.item;
                item.setExpanded(true);
                updateTable();
            }
        });

        ITmfTrace trace = getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Create the initial tree from a trace.
     */
    private synchronized void createTable() {
        if (fTrace == null) {
            return;
        }

        /* Clear the table, in case a trace was previously using it */
        fTree.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                fTree.setItemCount(0);
            }
        });

        for (final ITmfTrace currentTrace : fTraceManager.getActiveTraceSet()) {
            /*
             * We will first do all the queries for this trace, then update that
             * sub-tree in the UI thread.
             */
            final Map<String, ITmfStateSystem> sss = currentTrace.getStateSystems();
            final Map<String, List<ITmfStateInterval>> fullStates =
                    new LinkedHashMap<String, List<ITmfStateInterval>>();
            for (Map.Entry<String, ITmfStateSystem> entry : sss.entrySet()) {
                String ssName = entry.getKey();
                ITmfStateSystem ss = entry.getValue();
                ss.waitUntilBuilt();
                long startTime = ss.getStartTime();
                try {
                    fullStates.put(ssName, ss.queryFullState(startTime));
                } catch (TimeRangeException e) {
                    /* Should not happen since we're querying at start time */
                    throw new RuntimeException();
                } catch (StateSystemDisposedException e) {
                    /* Probably shutting down, cancel and return */
                    return;
                }
            }

            /* Update the table (in the UI thread) */
            fTree.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    TreeItem traceRoot = new TreeItem(fTree, SWT.NONE);
                    traceRoot.setText(ATTRIBUTE_NAME_COL, currentTrace.getName());

                    for (Map.Entry<String, ITmfStateSystem> entry : sss.entrySet()) {
                        String ssName = entry.getKey();
                        ITmfStateSystem ss = entry.getValue();
                        List<ITmfStateInterval> fullState = fullStates.get(ssName);

                        /* Root item of the current state system */
                        TreeItem item = new TreeItem(traceRoot, SWT.NONE);

                        /* Name of the SS goes in the first column */
                        item.setText(ATTRIBUTE_NAME_COL, ssName);

                        /*
                         * Calling with quark '-1' here to start with the root
                         * attribute, then it will be called recursively.
                         */
                        addChildren(ss, fullState, -1, item);
                    }

                    /* Expand the first-level tree items */
                    for (TreeItem item : fTree.getItems()) {
                        item.setExpanded(true);
                    }
                    packColumns();
                }
            });
        }
    }

    /**
     * Add children node to a newly-created tree. Should only be called by the
     * UI thread.
     */
    private void addChildren(ITmfStateSystem ss,
            List<ITmfStateInterval> fullState, int rootQuark, TreeItem root) {
        try {
            for (int quark : ss.getSubAttributes(rootQuark, false)) {
                TreeItem subItem = new TreeItem(root, SWT.NONE);

                /* Write the info we already know */
                subItem.setText(ATTRIBUTE_NAME_COL, ss.getAttributeName(quark));
                subItem.setText(QUARK_COL, String.valueOf(quark));
                subItem.setText(ATTRIBUTE_FULLPATH_COL, ss.getFullAttributePath(quark));

                /* Populate the other columns */
                ITmfStateInterval interval = fullState.get(quark);
                populateColumns(subItem, interval);

                /* Update this node's children recursively */
                addChildren(ss, fullState, quark, subItem);
            }

        } catch (AttributeNotFoundException e) {
            /* Should not happen, we're iterating on known attributes */
            throw new RuntimeException();
        }
    }

    /**
     * Update the tree, which means keep the tree of attributes in the first
     * column as-is, but update the values to the ones at a new timestamp.
     */
    private synchronized void updateTable() {
        ITmfTrace[] traces = fTraceManager.getActiveTraceSet();
        long ts = fCurrentTimestamp;

        /* For each trace... */
        for (int traceNb = 0; traceNb < traces.length; traceNb++) {
            Map<String, ITmfStateSystem> sss = traces[traceNb].getStateSystems();

            /* For each state system associated with this trace... */
            int ssNb = 0;
            for (Map.Entry<String, ITmfStateSystem> entry : sss.entrySet()) {
                /*
                 * Even though we only use the value, it just feels safer to
                 * iterate the same way as before to keep the order the same.
                 */
                final ITmfStateSystem ss = entry.getValue();
                final int traceNb1 = traceNb;
                final int ssNb1 = ssNb;
                ts = (ts == -1 ? ss.getStartTime() : ts);
                try {
                    final List<ITmfStateInterval> fullState = ss.queryFullState(ts);
                    fTree.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            /* Get the tree item of the relevant state system */
                            TreeItem traceItem = fTree.getItem(traceNb1);
                            TreeItem item = traceItem.getItem(ssNb1);
                            /* Update it, then its children, recursively */
                            item.setText(VALUE_COL, emptyString);
                            updateChildren(ss, fullState, -1, item);
                        }
                    });

                } catch (TimeRangeException e) {
                    /*
                     * This can happen in an experiment, if the user selects a
                     * range valid in the experiment, but this specific does not
                     * exist. Print "out-of-range" into all the values.
                     */
                    fTree.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            TreeItem traceItem = fTree.getItem(traceNb1);
                            TreeItem item = traceItem.getItem(ssNb1);
                            markOutOfRange(item);
                        }
                    });
                } catch (StateSystemDisposedException e) {
                    return;
                }

                ssNb++;
            }
        }
    }

    /**
     * Update the values shown by a child row when doing an update. Should only
     * be called by the UI thread.
     */
    private void updateChildren(ITmfStateSystem ss,
            List<ITmfStateInterval> state, int root_quark, TreeItem root) {
        try {
            for (TreeItem item : root.getItems()) {
                int quark = ss.getQuarkRelative(root_quark, item.getText(0));
                ITmfStateInterval interval = state.get(quark);
                populateColumns(item, interval);

                /* Update children recursively */
                updateChildren(ss, state, quark, item);
            }

        } catch (AttributeNotFoundException e) {
            /* We're iterating on known attributes, should not happen */
            throw new RuntimeException();
        }
    }

    /**
     * Populate an 'item' (a row in the tree) with the information found in the
     * interval. This method should only be called by the UI thread.
     */
    private static void populateColumns(TreeItem item, ITmfStateInterval interval) {
        try {
            ITmfStateValue state = interval.getStateValue();

            // add the value in the 2nd column
            switch (state.getType()) {
            case INTEGER:
                item.setText(VALUE_COL, String.valueOf(state.unboxInt()));
                break;
            case LONG:
                item.setText(VALUE_COL, String.valueOf(state.unboxLong()));
                break;
            case STRING:
                item.setText(VALUE_COL, state.unboxStr());
                break;
            case NULL:
            default:
                item.setText(VALUE_COL, emptyString);
                break;
            }

            TmfTimestamp startTime = new TmfTimestamp(interval.getStartTime(), ITmfTimestamp.NANOSECOND_SCALE);
            item.setText(START_TIME_COL, startTime.toString());

            TmfTimestamp endTime = new TmfTimestamp(interval.getEndTime(), ITmfTimestamp.NANOSECOND_SCALE);
            item.setText(END_TIME_COL, endTime.toString());

        } catch (StateValueTypeException e) {
            /* Should not happen, we're case-switching on the specific types */
            throw new RuntimeException();
        }
    }

    /**
     * Same concept as {@link updateChildren}, but instead of printing actual
     * values coming from the state system, we print "Out of range" into all
     * values. This is to indicate that this specific state system is not
     * currently defined at the selected timestamp.
     *
     * Guess by which thread this should be called? Hint: starts with a U, ends
     * with an I.
     */
    private void markOutOfRange(TreeItem root) {
        root.setText(VALUE_COL, Messages.OutOfRangeMsg);
        root.setText(START_TIME_COL, emptyString);
        root.setText(END_TIME_COL, emptyString);
        for (TreeItem item : root.getItems()) {
            markOutOfRange(item);
        }
    }

    /**
     * Auto-pack all the columns in the display. Should only be called by the UI
     * thread.
     */
    private void packColumns() {
        //FIXME should add a bit of padding
        for (TreeColumn column : fTree.getColumns()) {
            column.pack();
        }
    }

    @Override
    public void setFocus() {
        fTree.setFocus();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the trace selected signal. This will make the view display
     * the information for the newly-selected trace.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        ITmfTrace trace = signal.getTrace();
        if (trace != fTrace) {
            fTrace = trace;
            Thread thread = new Thread("State system visualizer construction") { //$NON-NLS-1$
                @Override
                public void run() {
                    createTable();
                }
            };
            thread.start();
        }
    }

    /**
     * Handler for the trace closed signal. This will clear the view.
     *
     * @param signal
     *            the incoming signal
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {
        // delete the tree at the trace closed
        if (signal.getTrace() == fTrace) {
            fTrace = null;
            fTree.setItemCount(0);
        }
    }

    /**
     * Handles the current time updated signal. This will update the view's
     * values to the newly-selected timestamp.
     *
     * @param signal
     *            the signal to process
     */
    @TmfSignalHandler
    public void currentTimeUpdated(final TmfTimeSynchSignal signal) {
        Thread thread = new Thread("State system visualizer update") { //$NON-NLS-1$
            @Override
            public void run() {
                ITmfTimestamp currentTime = signal.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE);
                fCurrentTimestamp = currentTime.getValue();
                updateTable();
            }
        };
        thread.start();
    }
}
