/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.LatencyAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.LatencyAnalysisListener;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.table.TmfSimpleTableViewer;

/**
 * Displays the latency analysis data in a column table
 *
 * @author France Lapointe Nguyen
 */
public class LatencyTableViewer extends TmfSimpleTableViewer {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------


    /**
     * Abstract class for the column label provider for the latency analysis
     * table viewer
     */
    private abstract class LatencyTableColumnLabelProvider extends ColumnLabelProvider {

        @Override
        public String getText(@Nullable Object input) {
            if (!(input instanceof ISegment)) {
                /* Doubles as a null check */
                return ""; //$NON-NLS-1$
            }
            return getTextFoITimeRange((ISegment) input);
        }

        public abstract String getTextFoITimeRange(ISegment input);
    }

    /**
     * Listener to update the model with the latency analysis results once the
     * latency analysis is fully completed
     */
    private final class LatencyListener implements LatencyAnalysisListener {
        @Override
        public void onComplete(LatencyAnalysis activeAnalysis, ISegmentStore<ISegment> data) {
            // Check if the active trace was changed while the analysis was
            // running
            if (activeAnalysis.equals(fAnalysisModule)) {
                updateModel(data);
            }
        }
    }

    /**
     * Listener to select a range in other viewers when a cell of the latency
     * table view is selected
     */
    private class LatencyTableSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(@Nullable SelectionEvent e) {
            ISegment selectedSegment = ((ISegment) NonNullUtils.checkNotNull(e).item.getData());
            ITmfTimestamp start = new TmfNanoTimestamp(selectedSegment.getStart());
            ITmfTimestamp end = new TmfNanoTimestamp(selectedSegment.getEnd());
            TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(LatencyTableViewer.this, start, end));
        }
    }

    /**
     * Current latency analysis module
     */
    private @Nullable LatencyAnalysis fAnalysisModule = null;

    /**
     * Latency analysis completion listener
     */
    private LatencyListener fListener;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param tableViewer
     *            Table viewer of the view
     */
    public LatencyTableViewer(TableViewer tableViewer) {
        super(tableViewer);
        // Sort order of the content provider is by start time by default
        getTableViewer().setContentProvider(new LatencyContentProvider());
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            fAnalysisModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, LatencyAnalysis.class, LatencyAnalysis.ID);
        }
        createColumns();
        getTableViewer().getTable().addSelectionListener(new LatencyTableSelectionListener());
        fListener = new LatencyListener();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Create columns for start time, end time and duration
     */
    private void createColumns() {
        createColumn(Messages.LatencyTableViewer_startTime, new LatencyTableColumnLabelProvider() {
            @Override
            public String getTextFoITimeRange(ISegment input) {
                return NonNullUtils.nullToEmptyString(TmfTimestampFormat.getDefaulTimeFormat().format(input.getStart()));
            }
        }, SegmentComparators.INTERVAL_START_COMPARATOR);

        createColumn(Messages.LatencyTableViewer_endTime, new LatencyTableColumnLabelProvider() {
            @Override
            public String getTextFoITimeRange(ISegment input) {
                return NonNullUtils.nullToEmptyString(TmfTimestampFormat.getDefaulTimeFormat().format(input.getEnd()));
            }
        }, SegmentComparators.INTERVAL_END_COMPARATOR);

        createColumn(Messages.LatencyTableViewer_duration, new LatencyTableColumnLabelProvider() {
            @Override
            public String getTextFoITimeRange(ISegment input) {
                return NonNullUtils.nullToEmptyString(Long.toString(input.getLength()));
            }
        }, SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
    }

    /**
     * Update the data in the table viewer
     *
     * @param dataInput
     *            New data input
     */
    public void updateModel(final @Nullable ISegmentStore<ISegment> dataInput) {
        final TableViewer tableViewer = getTableViewer();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!tableViewer.getTable().isDisposed()) {
                    // Go to the top of the table
                    tableViewer.getTable().setTopIndex(0);
                    // Reset selected row
                    tableViewer.setSelection(StructuredSelection.EMPTY);
                    if (dataInput == null) {
                        tableViewer.setInput(null);
                        tableViewer.setItemCount(0);
                        return;
                    }
                    tableViewer.setInput(dataInput);
                    LatencyContentProvider latencyContentProvider = (LatencyContentProvider) getTableViewer().getContentProvider();
                    tableViewer.setItemCount(latencyContentProvider.getSegmentCount());
                }
            }
        });
    }

    /**
     * Set the data into the viewer. Will update model is analysis is completed
     * or run analysis if not completed
     *
     * @param analysis
     *            Latency analysis module
     */
    public void setData(@Nullable LatencyAnalysis analysis) {
        // Set the current latency analysis module
        fAnalysisModule = analysis;
        if (analysis == null) {
            updateModel(null);
            return;
        }
        ISegmentStore<ISegment> results = analysis.getResults();
        // If results are not null, then analysis is completed and model can be
        // updated
        if (results != null) {
            updateModel(results);
            return;
        }
        // If results are null, then add completion listener and run analysis
        updateModel(null);
        analysis.addListener(fListener);
        analysis.schedule();
    }

    @Override
    protected void appendToTablePopupMenu(IMenuManager manager, IStructuredSelection sel) {
        final ISegment segment = (ISegment) sel.getFirstElement();

        IAction gotoStartTime = new Action(Messages.LatencyView_goToStartEvent) {
            @Override
            public void run() {
                broadcast(new TmfSelectionRangeUpdatedSignal(LatencyTableViewer.this, new TmfNanoTimestamp(segment.getStart())));
            }
        };

        IAction gotoEndTime = new Action(Messages.LatencyView_goToEndEvent) {
            @Override
            public void run() {
                broadcast(new TmfSelectionRangeUpdatedSignal(LatencyTableViewer.this, new TmfNanoTimestamp(segment.getEnd())));
            }
        };

       manager.add(gotoStartTime);
       manager.add(gotoEndTime);
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * Get current latency analysis module
     *
     * @return current latency analysis module
     */
    public @Nullable LatencyAnalysis getAnalysisModule() {
        return fAnalysisModule;
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Trace selected handler
     *
     * @param signal
     *            Different opened trace (on which latency analysis as already
     *            been performed) has been selected
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        ITmfTrace trace = signal.getTrace();
        if (trace != null) {
            setData(TmfTraceUtils.getAnalysisModuleOfClass(trace, LatencyAnalysis.class, LatencyAnalysis.ID));
        }
    }

    /**
     * Trace opened handler
     *
     * @param signal
     *            New trace (on which latency analysis has not been performed)
     *            is opened
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        ITmfTrace trace = signal.getTrace();
        if (trace != null) {
            setData(TmfTraceUtils.getAnalysisModuleOfClass(trace, LatencyAnalysis.class, LatencyAnalysis.ID));
        }
    }

    /**
     * Trace closed handler
     *
     * @param signal
     *            Last opened trace was closed
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {
        // Check if there is no more opened trace
        if (TmfTraceManager.getInstance().getActiveTrace() == null) {
            if (!getTableViewer().getTable().isDisposed()) {
                getTableViewer().setInput(null);
                refresh();
            }
        }
    }
}
