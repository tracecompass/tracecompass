/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui;

import java.util.Collections;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.counters.core.CounterAnalysis;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractTmfTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;

/**
 * Display the state system as a filtered checkbox tree:
 *
 * <pre>
 * {trace name}
 *   +- Grouped
 *   |   +- {group id}
 *   |   |   +- {group element}
 *   |   |       +- ...
 *   |   +- {group id}
 *   |       +- ...
 *   +- Ungrouped
 *       +- {counter}
 *       +- ...
 * </pre>
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 */
public class CounterTreeViewer extends AbstractTmfTreeViewer {

    private TmfTreeViewerEntry fRootEntry;

    /**
     * Constructor
     *
     * @param parent
     *            Parent composite
     */
    public CounterTreeViewer(Composite parent) {
        super(parent, true);

        setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return (element instanceof TmfTreeViewerEntry)
                        ? ((TmfTreeViewerEntry) element).getName()
                        : super.getText(element);
            }
        });
    }

    @Override
    public void initializeDataSource() {
        fRootEntry = null;
        CounterAnalysis module = null;
        ITmfTrace trace = getTrace();
        if (trace != null) {

            module = TmfTraceUtils.getAnalysisModuleOfClass(trace, CounterAnalysis.class, CounterAnalysis.ID);
            if (module != null) {
                fRootEntry = new TmfTreeViewerEntry(trace.getName());
                TmfTreeViewerEntry rootBranch = new TmfTreeViewerEntry(getTrace().getName());
                fRootEntry.getChildren().add(rootBranch);
                Display.getDefault().asyncExec(() -> getTreeViewer().setInput(fRootEntry));

                module.schedule();
                module.waitForCompletion();

                ITmfStateSystem stateSystem = module.getStateSystem();
                if (stateSystem != null) {

                    /*
                     * Add grouped and ungrouped counters branches along with their entries (if
                     * applicable).
                     */
                    addTreeViewerBranch(stateSystem, rootBranch, CounterAnalysis.GROUPED_COUNTER_ASPECTS_ATTRIB);
                    addTreeViewerBranch(stateSystem, rootBranch, CounterAnalysis.UNGROUPED_COUNTER_ASPECTS_ATTRIB);
                }
            }
        }
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> {
            TmfTreeColumnData column = new TmfTreeColumnData("Counters"); //$NON-NLS-1$
            column.setComparator(new ViewerComparator());
            return Collections.singletonList(column);
        };
    }

    @Override
    protected ITmfTreeViewerEntry updateElements(long start, long end, boolean isSelection) {
        /*
         * The tree displaying the trace's counters does not change when manipulating
         * the corresponding chart.
         */
        return fRootEntry;
    }

    private void addTreeViewerBranch(ITmfStateSystem stateSystem, TmfTreeViewerEntry rootBranch, String branchName) {
        int quark = stateSystem.optQuarkAbsolute(branchName);
        if (quark != ITmfStateSystem.INVALID_ATTRIBUTE && !stateSystem.getSubAttributes(quark, false).isEmpty()) {
            TmfTreeViewerEntry branch = new TmfTreeViewerEntry(branchName);
            rootBranch.addChild(branch);
            addTreeViewerEntries(stateSystem, branch, quark);
        }
    }

    /**
     * Recursively add all child entries of a parent branch from the state system.
     */
    private void addTreeViewerEntries(ITmfStateSystem stateSystem, TmfTreeViewerEntry parentBranch, int quark) {
        for (int childQuark : stateSystem.getSubAttributes(quark, false)) {
            TmfTreeViewerEntry childBranch = stateSystem.getSubAttributes(childQuark, false).isEmpty()
                    ? new CounterTreeViewerEntry(stateSystem.getAttributeName(childQuark), childQuark)
                    : new TmfTreeViewerEntry(stateSystem.getAttributeName(childQuark));
            parentBranch.addChild(childBranch);
            addTreeViewerEntries(stateSystem, childBranch, childQuark);
        }
    }

    @Override
    public ITmfTrace getTrace() {
        return super.getTrace();
    }
}
