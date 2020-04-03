/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *   Bernd Hufmann - Move abstract class to TMF
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.ViewFilterDialog;
import org.eclipse.tracecompass.internal.tmf.ui.commands.ExportToTsvAction;
import org.eclipse.tracecompass.internal.tmf.ui.commands.ExportToTsvUtils;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.ITmfFilterableControl;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

import com.google.common.annotations.VisibleForTesting;

/**
 * View for displaying a segment store analysis in a table.
 *
 * @author France Lapointe Nguyen
 * @since 2.0
 */
public abstract class AbstractSegmentStoreTableView extends TmfView implements ITmfFilterableControl {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final String TMF_VIEW_UI_CONTEXT = "org.eclipse.tracecompass.tmf.ui.view.context"; //$NON-NLS-1$

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
    private @Nullable ViewFilterDialog fFilterDialog = null;

    private List<IContextActivation> fActiveContexts = new ArrayList<>();
    private @Nullable IContextService fContextService = null;

    private @Nullable Action fFilterAction;

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
        AbstractSegmentStoreTableViewer segmentStoreViewer = createSegmentStoreViewer(tableViewer);
        fSegmentStoreViewer = segmentStoreViewer;
        getViewSite().getActionBars().getMenuManager().add(fExportAction);
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            if (fSegmentStoreViewer != null) {
                fSegmentStoreViewer.traceSelected(signal);
            }
        }
        // Add focus lost event
        segmentStoreViewer.getControl().addFocusListener(new FocusListener() {
            @Override
            public void focusLost(@Nullable FocusEvent e) {
                deactivateContextService();
            }

            @Override
            public void focusGained(@Nullable FocusEvent e) {
                activateContextService();
            }
        });

        Action timeEventFilterAction = new Action() {

            @Override
            public void run() {
                ViewFilterDialog dialog = fFilterDialog;
                if (dialog != null) {
                    fFilterDialog = null;
                    dialog.close();
                }
                dialog = new ViewFilterDialog(segmentStoreViewer.getControl().getShell(), AbstractSegmentStoreTableView.this, segmentStoreViewer.getControl());
                fFilterDialog = dialog;
                dialog.open();
            }
        };

        fFilterAction = timeEventFilterAction;

        IWorkbenchPartSite site = getSite();
        fContextService = site.getWorkbenchWindow().getService(IContextService.class);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void setFocus() {
        if (fSegmentStoreViewer != null) {
            fSegmentStoreViewer.getTableViewer().getControl().setFocus();
        }
        activateContextService();
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

    private void activateContextService() {
        IContextService contextService = fContextService;
        if (fActiveContexts.isEmpty() && contextService != null) {
            fActiveContexts.add(Objects.requireNonNull(contextService.activateContext(TMF_VIEW_UI_CONTEXT)));
        }
    }

    private void deactivateContextService() {
        IContextService contextService = fContextService;
        if (contextService != null) {
            contextService.deactivateContexts(fActiveContexts);
            fActiveContexts.clear();
        }
    }

    @Override
    public Action getFilterAction() {
        return Objects.requireNonNull(fFilterAction);
    }

    @Override
    public void filterUpdated(@Nullable String regex, Set<String> filterRegexes) {
        AbstractSegmentStoreTableViewer segmentStoreViewer = fSegmentStoreViewer;
        if (segmentStoreViewer != null) {
            segmentStoreViewer.setLocalRegexes(filterRegexes);
        }
    }

}
