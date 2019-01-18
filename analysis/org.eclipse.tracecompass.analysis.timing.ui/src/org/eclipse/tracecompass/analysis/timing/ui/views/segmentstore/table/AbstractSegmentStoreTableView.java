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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.tracecompass.internal.tmf.ui.commands.ExportToTsvAction;
import org.eclipse.tracecompass.internal.tmf.ui.commands.ExportToTsvUtils;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

import com.google.common.annotations.VisibleForTesting;

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
        super.createPartControl(parent);
        SashForm sf = new SashForm(parent, SWT.NONE);
        TableViewer tableViewer = new TableViewer(sf, SWT.FULL_SELECTION | SWT.VIRTUAL);
        fSegmentStoreViewer = createSegmentStoreViewer(tableViewer);
        getViewSite().getActionBars().getMenuManager().add(fExportAction);
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            if (fSegmentStoreViewer != null) {
                fSegmentStoreViewer.traceSelected(signal);
            }
        }
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
        AbstractSegmentStoreTableViewer segmentStoreViewer = getSegmentStoreViewer();
        if (segmentStoreViewer == null) {
            return;
        }
        Table table = segmentStoreViewer.getTableViewer().getTable();
        ExportToTsvUtils.exportTableToTsv(table, stream);
    }
}
