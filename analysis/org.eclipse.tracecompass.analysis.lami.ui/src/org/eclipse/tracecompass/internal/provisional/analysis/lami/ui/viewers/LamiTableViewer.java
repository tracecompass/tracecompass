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

import java.io.OutputStream;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views.LamiReportView;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views.LamiReportViewTabPage;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.signal.ChartSelectionUpdateSignal;
import org.eclipse.tracecompass.internal.tmf.ui.commands.ExportToTsvUtils;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.ui.viewers.table.TmfSimpleTableViewer;

/**
 * Table viewer to use in {@link LamiReportView}s.
 *
 * @author Alexandre Montplaisir
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public final class LamiTableViewer extends TmfSimpleTableViewer implements ILamiViewer {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final LamiReportViewTabPage fPage;
    private Set<Object> fSelection;

    // ------------------------------------------------------------------------
    // Inner class definitions
    // ------------------------------------------------------------------------

    /**
     * Abstract class for the column label provider for the latency analysis
     * table viewer.
     */
    private static class LamiTableColumnLabelProvider extends ColumnLabelProvider {
        private final LamiTableEntryAspect fColumnAspect;

        public LamiTableColumnLabelProvider(LamiTableEntryAspect aspect) {
            fColumnAspect = aspect;
        }

        @Override
        public String getText(@Nullable Object input) {
            /* Doubles as a null check */
            if (!(input instanceof LamiTableEntry)) {
                return ""; //$NON-NLS-1$
            }

            return nullToEmptyString(fColumnAspect.resolveString((LamiTableEntry) input));
        }
    }

    /**
     * Listener to update in other viewers when a cell of the latency table view
     * is selected.
     */
    private class LamiTableSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(@Nullable SelectionEvent event) {
            IStructuredSelection selections = getTableViewer().getStructuredSelection();

            /* Find all selected entries */
            Set<Object> selectionSet = new HashSet<>();
            for (Object selectedEntry : selections.toArray()) {
                selectionSet.add(checkNotNull(selectedEntry));
            }
            fSelection = selectionSet;


            /* Signal all Lami viewers & views of the selection */
            ChartSelectionUpdateSignal customSignal = new ChartSelectionUpdateSignal(LamiTableViewer.this, fPage.getResultTable(), selectionSet);
            TmfSignalManager.dispatchSignal(customSignal);
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param tableViewer
     *            Table viewer of the view
     * @param page
     *            The {@link LamiReportViewTabPage} parent page
     */
    public LamiTableViewer(TableViewer tableViewer, LamiReportViewTabPage page) {
        super(tableViewer);

        /* The viewer should always be the first element in the control. */
        tableViewer.getTable().moveAbove(null);

        fPage = page;
        fSelection = new HashSet<>();

        /* Default sort order of the content provider is by its first column */
        getTableViewer().setContentProvider(new LamiTableContentProvider());
        getTableViewer().getTable().addSelectionListener(new LamiTableSelectionListener());

        createColumns();
        fillData();
    }

    /**
     * Factory method to create a new Table viewer.
     *
     * @param parent
     *            The parent composite
     * @param page
     *            The {@link LamiReportViewTabPage} parent page
     * @return The new viewer
     */
    public static LamiTableViewer createLamiTable(Composite parent, LamiReportViewTabPage page) {
        TableViewer tableViewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
        return new LamiTableViewer(tableViewer, page);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    private void createColumns() {
        final List<LamiTableEntryAspect> aspects = fPage.getResultTable().getTableClass().getAspects();

        Display.getDefault().asyncExec(() -> {
            for (LamiTableEntryAspect aspect : aspects) {
                createColumn(aspect.getLabel(), new LamiTableColumnLabelProvider(aspect), aspect.getComparator());
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
            tableViewer.setInput(fPage.getResultTable().getEntries());
            LamiTableContentProvider latencyContentProvider = (LamiTableContentProvider) getTableViewer().getContentProvider();
            tableViewer.setItemCount(latencyContentProvider.getNbEntries());

            /* Set the column's alignment and pack them */
            TableColumn[] cols = tableViewer.getTable().getColumns();
            for (int i = 0; i < cols.length; i++) {
                LamiTableEntryAspect colAspect = fPage.getResultTable().getTableClass().getAspects().get(i);
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
            if (!fSelection.isEmpty()) {
                LamiTableContentProvider provider = (LamiTableContentProvider) getTableViewer().getContentProvider();

                /* Find the indexes in the UI table of the selected objects */
                int[] selectionsIndexes = fSelection.stream()
                        .map(obj -> (LamiTableEntry) obj)
                        .mapToInt(entry -> provider.getIndexOf(checkNotNull(entry)))
                        .toArray();

                /* Update the selection in the UI table */
                Display.getDefault().asyncExec(() -> {
                    getTableViewer().getTable().setSelection(selectionsIndexes);
                    getTableViewer().getTable().redraw();
                });
            }
        });
        Display.getDefault().asyncExec(() -> {
            if (tableViewer.getTable().isDisposed()) {
                return;
            }

            TableColumn[] cols = tableViewer.getTable().getColumns();
            for (int i = 0; i < cols.length; i++) {
                cols[i].pack();
            }
        });
    }

    /**
     * Export table to tsv file.
     *
     * @param stream
     *              the output stream
     */
    public void exportToTsv(@Nullable OutputStream stream) {
        TableViewer tableViewer = getTableViewer();
        if (tableViewer == null) {
            return;
        }
        Table table = tableViewer.getTable();
        ExportToTsvUtils.exportTableToTsv(table, stream);
    }

    // ------------------------------------------------------------------------
    // Signals
    // ------------------------------------------------------------------------

    /**
     * The signal handler for selection update.
     *
     * @param signal
     *            The selection update signal
     */
    @TmfSignalHandler
    public void updateCustomSelection(ChartSelectionUpdateSignal signal) {
        /* Make sure we are not sending a signal to ourself */
        if (signal.getSource() == this) {
            return;
        }

        /* Make sure the signal comes from the data provider's scope */
        if (fPage.getResultTable().hashCode() != signal.getDataProvider().hashCode()) {
            return;
        }

        /* Get the set of selected objects */
        fSelection = signal.getSelectedObject();

        /* Get the selected index in the UI table */
        LamiTableContentProvider tableContentProvider = (LamiTableContentProvider) getTableViewer().getContentProvider();
        int[] tableSelection = fSelection.stream()
                .map(obj -> (LamiTableEntry) obj)
                .mapToInt(entry -> tableContentProvider.getIndexOf(checkNotNull(entry)))
                .toArray();

        /* Update the selection in the UI table */
        Display.getDefault().asyncExec(() -> {
            getTableViewer().getTable().setSelection(tableSelection);
            getTableViewer().getTable().redraw();
        });
    }

}
