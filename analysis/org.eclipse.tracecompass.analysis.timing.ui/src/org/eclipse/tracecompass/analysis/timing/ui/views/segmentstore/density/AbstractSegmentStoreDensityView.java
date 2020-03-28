/******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.eclipse.tracecompass.tmf.ui.views.SaveImageUtil;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

import com.google.common.annotations.VisibleForTesting;

/**
 * Displays the segment store analysis data in a density chart and a table
 * corresponding to the selected latencies.
 *
 * @author Matthew Khouzam
 * @author Marc-Andre Laperle
 */
public abstract class AbstractSegmentStoreDensityView extends TmfView {

    private static final int[] DEFAULT_WEIGHTS = new int[] { 4, 6 };

    /**
     * Default zoom range
     * @since 4.1
     */
    public static final Pair<Double, Double> DEFAULT_RANGE = new Pair<>(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);

    private @Nullable AbstractSegmentStoreDensityViewer fDensityViewer;
    private @Nullable AbstractSegmentStoreTableViewer fTableViewer;

    /**
     * Constructs a segment store density view
     *
     * @param viewName
     *            the name of the view
     */
    public AbstractSegmentStoreDensityView(String viewName) {
        super(viewName);
    }

    /**
     * Used to keep the table in sync with the density viewer.
     */
    private final class DataChangedListener implements ISegmentStoreDensityViewerDataListener {

        private void updateTableModel(@Nullable Iterable<? extends ISegment> data) {
            final AbstractSegmentStoreTableViewer viewer = fTableViewer;
            if (viewer != null && data != null) {
                viewer.updateModel(data);
            }
        }

        @Override
        public void viewDataChanged(@NonNull Iterable<? extends @NonNull ISegment> newData) {
            updateTableModel(newData);
        }

        @Override
        public void selectedDataChanged(@Nullable Iterable<? extends @NonNull ISegment> newSelectionData) {
            updateTableModel(newSelectionData);
        }

    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);
        final SashForm sashForm = new SashForm(parent, SWT.NONE);

        fTableViewer = createSegmentStoreTableViewer(sashForm);
        fDensityViewer = createSegmentStoreDensityViewer(sashForm);
        fDensityViewer.addDataListener(new DataChangedListener());

        sashForm.setWeights(DEFAULT_WEIGHTS);

        Action zoomOut = new ZoomOutAction(this);
        IToolBarManager toolBar = getViewSite().getActionBars().getToolBarManager();
        toolBar.add(zoomOut);
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            if (fDensityViewer != null) {
                fDensityViewer.traceSelected(signal);
            }
            if (fTableViewer != null) {
                fTableViewer.traceSelected(signal);
            }
        }
    }

    @Override
    protected @NonNull IAction createSaveAction() {
        return SaveImageUtil.createSaveAction(getName(), this::getDensityViewer);
    }

    /**
     * Create a table viewer suitable for displaying the segment store content.
     *
     * @param parent
     *            the parent composite
     * @return the table viewer
     */
    protected abstract AbstractSegmentStoreTableViewer createSegmentStoreTableViewer(Composite parent);

    /**
     * Create a density viewer suitable for displaying the segment store
     * content.
     *
     * @param parent
     *            the parent composite
     * @return the density viewer
     */
    protected abstract AbstractSegmentStoreDensityViewer createSegmentStoreDensityViewer(Composite parent);

    @Override
    public void setFocus() {
        final AbstractSegmentStoreDensityViewer viewer = fDensityViewer;
        if (viewer != null) {
            viewer.getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
        final AbstractSegmentStoreDensityViewer densityViewer = fDensityViewer;
        if (densityViewer != null) {
            densityViewer.dispose();
        }

        final AbstractSegmentStoreTableViewer tableViewer = fTableViewer;
        if (tableViewer != null) {
            tableViewer.dispose();
        }

        super.dispose();
    }

    /**
     * Get the density viewer
     *
     * @return the density viewer
     * @since 1.2
     */
    @VisibleForTesting
    public @Nullable AbstractSegmentStoreDensityViewer getDensityViewer() {
        return fDensityViewer;
    }

    /**
     * Get the table viewer
     *
     * @return the table viewer
     * @since 1.2
     */
    @VisibleForTesting
    public @Nullable AbstractSegmentStoreTableViewer getTableViewer() {
        return fTableViewer;
    }
}