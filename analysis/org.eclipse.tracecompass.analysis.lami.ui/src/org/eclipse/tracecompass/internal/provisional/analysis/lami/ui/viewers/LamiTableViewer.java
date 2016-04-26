/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson, EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.viewers;


import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.signals.LamiSelectionUpdateSignal;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views.LamiReportView;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.ui.viewers.table.TmfSimpleTableViewer;

/**
 * Table viewer to use in {@link LamiReportView}s.
 *
 * @author Alexandre Montplaisir
 */
public final class LamiTableViewer extends TmfSimpleTableViewer implements ILamiViewer {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final LamiResultTable fResultTable;
    private Set<Integer> fSelections;

    // ------------------------------------------------------------------------
    // Inner class definitions
    // ------------------------------------------------------------------------

    /**
     * Abstract class for the column label provider for the latency analysis
     * table viewer
     */
    private static class LamiTableColumnLabelProvider extends ColumnLabelProvider {

        private final LamiTableEntryAspect fColumnAspect;

        public LamiTableColumnLabelProvider(LamiTableEntryAspect aspect) {
            fColumnAspect = aspect;
        }

        @Override
        public String getText(@Nullable Object input) {
            if (!(input instanceof LamiTableEntry)) {
                /* Doubles as a null check */
                return ""; //$NON-NLS-1$
            }
            LamiTableEntry entry = (LamiTableEntry) input;
            return nullToEmptyString(fColumnAspect.resolveString(entry));
        }
    }

    /**
     * Listener to update in other viewers when a cell of the latency
     * table view is selected
     */
    private class LamiTableSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(@Nullable SelectionEvent e) {

            IStructuredSelection selections = getTableViewer().getStructuredSelection();

            Set<Integer> selectionIndexes = new HashSet<>();
            for (Object selectedEntry : selections.toArray() ) {
                selectionIndexes.add(fResultTable.getEntries().indexOf(selectedEntry));
            }

            fSelections = selectionIndexes;

            /* Signal all Lami viewers & views of the selection */
            LamiSelectionUpdateSignal signal = new LamiSelectionUpdateSignal(LamiTableViewer.this, selectionIndexes, checkNotNull(fResultTable).hashCode());
            TmfSignalManager.dispatchSignal(signal);
        }
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param tableViewer
     *            Table viewer of the view
     * @param resultTable
     *            Data table populating this viewer
     */
    public LamiTableViewer(TableViewer tableViewer, LamiResultTable resultTable) {
        super(tableViewer);
        /*
         * The table viewer should always be the first element in the control.
         */
        tableViewer.getTable().moveAbove(null);

        fResultTable = resultTable;
        fSelections = new HashSet<>();

        /* Default sort order of the content provider is by its first column */
        getTableViewer().setContentProvider(new LamiTableContentProvider());
        getTableViewer().getTable().addSelectionListener(new LamiTableSelectionListener());

        createColumns();
        fillData();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    private void createColumns() {
        final List<LamiTableEntryAspect> aspects = fResultTable.getTableClass().getAspects();

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                for (LamiTableEntryAspect aspect : aspects) {
                    createColumn(aspect.getLabel(), new LamiTableColumnLabelProvider(aspect), aspect.getComparator());
                }
            }
        });
    }

    /**
     * Update the data in the table viewer
     *
     * @param dataInput
     *            New data input
     */
    private void fillData() {
        final TableViewer tableViewer = getTableViewer();
        Display.getDefault().asyncExec(() -> {
            if (tableViewer.getTable().isDisposed()) {
                return;
            }
            // Go to the top of the table
            tableViewer.getTable().setTopIndex(0);
            // Reset selected row
            tableViewer.setSelection(StructuredSelection.EMPTY);

            /* Fill the table data */
            tableViewer.setInput(fResultTable.getEntries());
            LamiTableContentProvider latencyContentProvider = (LamiTableContentProvider) getTableViewer().getContentProvider();
            tableViewer.setItemCount(latencyContentProvider.getNbEntries());

            /* Set the column's alignment and pack them */
            TableColumn[] cols = tableViewer.getTable().getColumns();
            for (int i = 0; i < cols.length; i++) {
                LamiTableEntryAspect colAspect = fResultTable.getTableClass().getAspects().get(i);
                int alignment = (colAspect.isContinuous() ? SWT.RIGHT : SWT.LEFT);
                cols[i].setAlignment(alignment);

            }

            /*
             * On creation check if there is selections if so update the table
             * selections here. Selections needs the ContentProvider for valid
             * index lookup and since the content provider is set in an
             * asynchronous task we cannot use the normal signal handler since
             * we have no guarantee of time of execution of the fill data.
             */
            if (!fSelections.isEmpty()) {
                int[] selectionsIndexes = fSelections.stream().map(index -> fResultTable.getEntries().get(index)).mapToInt(entry -> ((LamiTableContentProvider) getTableViewer().getContentProvider()).getIndexOf(entry)).toArray();
                Display.getDefault().asyncExec(() -> {
                    getTableViewer().getTable().setSelection(selectionsIndexes);
                    getTableViewer().getTable().redraw();
                });
            }
        });
        Display.getDefault().asyncExec(() -> {
            TableColumn[] cols = tableViewer.getTable().getColumns();
            for (int i = 0; i < cols.length; i++) {
                cols[i].pack();
            }
        });
    }

    /**
     * The signal handler for selection update.
     *
     * @param signal
     *          The selection update signal
     */
    @TmfSignalHandler
    public void updateSelection(LamiSelectionUpdateSignal signal) {

        if (fResultTable.hashCode() != signal.getSignalHash() || equals(signal.getSource())) {
            /* The signal is not for us */
            return;
         }
        /* Fetch the position of the selected entry in the actual table since it could be sorted by another column */
        LamiTableContentProvider latencyContentProvider = (LamiTableContentProvider) getTableViewer().getContentProvider();

        Set<Integer> selections = signal.getEntryIndex();

        int[] selectionsIndexes = selections.stream()
                .map(index -> fResultTable.getEntries().get(index))
                .mapToInt(entry -> latencyContentProvider.getIndexOf(entry))
                .toArray();

        fSelections = new HashSet<>(selections);

        Display.getDefault().asyncExec(() -> {
            getTableViewer().getTable().setSelection(selectionsIndexes);
            getTableViewer().getTable().redraw();
        });
    }
}
