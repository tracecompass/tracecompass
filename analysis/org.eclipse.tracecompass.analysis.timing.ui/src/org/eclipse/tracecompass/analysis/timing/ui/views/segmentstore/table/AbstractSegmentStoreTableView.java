/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *   Bernd Hufmann - Move abstract class to TMF
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.ExportToTsvAction;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;

/**
 * View for displaying a segment store analysis in a table.
 *
 * @author France Lapointe Nguyen
 * @since 2.0
 */
public abstract class AbstractSegmentStoreTableView extends TmfView {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Action fExportAction = new ExportToTsvAction() {
        @Override
        protected void exportToTsv(@Nullable OutputStream stream) {
            AbstractSegmentStoreTableView.this.exportToTsv(stream);

        }

        @Override
        protected @Nullable Shell getShell() {
            return getViewSite().getShell();
        }
    };

    private @Nullable AbstractSegmentStoreTableViewer fSegmentStoreViewer;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public AbstractSegmentStoreTableView() {
        super(""); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(@Nullable Composite parent) {
        SashForm sf = new SashForm(parent, SWT.NONE);
        TableViewer tableViewer = new TableViewer(sf, SWT.FULL_SELECTION | SWT.VIRTUAL);
        fSegmentStoreViewer = createSegmentStoreViewer(tableViewer);
        getViewSite().getActionBars().getMenuManager().add(fExportAction);
        setInitialData();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void setFocus() {
        if (fSegmentStoreViewer != null) {
            fSegmentStoreViewer.getTableViewer().getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fSegmentStoreViewer != null) {
            fSegmentStoreViewer.dispose();
        }
    }

    /**
     * Returns the latency analysis table viewer instance
     *
     * @param tableViewer
     *            the table viewer to use
     * @return the latency analysis table viewer instance
     */
    protected abstract AbstractSegmentStoreTableViewer createSegmentStoreViewer(TableViewer tableViewer);

    /**
     * Get the table viewer
     *
     * @return the table viewer, useful for testing
     */
    @Nullable
    public AbstractSegmentStoreTableViewer getSegmentStoreViewer() {
        return fSegmentStoreViewer;
    }

    /**
     * Set initial data into the viewer
     */
    private void setInitialData() {
        if (fSegmentStoreViewer != null) {
            fSegmentStoreViewer.setData(fSegmentStoreViewer.getSegmentProvider());
        }
    }

    /**
     * Export a given items's TSV
     *
     * @param stream
     *            an output stream to write the TSV to
     * @since 1.2
     */
    @VisibleForTesting
    protected void exportToTsv(@Nullable OutputStream stream) {
        try (PrintWriter pw = new PrintWriter(stream)) {
            AbstractSegmentStoreTableViewer segmentStoreViewer = getSegmentStoreViewer();
            if (segmentStoreViewer == null) {
                return;
            }
            Table table = segmentStoreViewer.getTableViewer().getTable();
            int size = table.getItemCount();
            List<String> columns = new ArrayList<>();
            for (int i = 0; i < table.getColumnCount(); i++) {
                TableColumn column = table.getColumn(i);
                if (column == null) {
                    return;
                }
                String columnName = String.valueOf(column.getText());
                if (columnName.isEmpty() && i == table.getColumnCount() - 1) {
                    // Linux GTK2 undocumented feature
                    break;
                }
                columns.add(columnName);
            }
            pw.println(Joiner.on('\t').join(columns));
            for (int i = 0; i < size; i++) {
                TableItem item = table.getItem(i);
                if (item == null) {
                    continue;
                }
                List<String> data = new ArrayList<>();
                for (int col = 0; col < columns.size(); col++) {
                    data.add(String.valueOf(item.getText(col)));
                }
                pw.println(Joiner.on('\t').join(data));
            }
        }
    }
}
