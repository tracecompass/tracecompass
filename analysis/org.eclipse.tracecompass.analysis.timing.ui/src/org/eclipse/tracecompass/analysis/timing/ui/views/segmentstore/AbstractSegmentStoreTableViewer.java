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
 *   Bernd Hufmann - Move abstract class to TMF
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore;

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
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.Messages;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.SegmentStoreContentProvider;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
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
import org.eclipse.tracecompass.tmf.ui.viewers.table.TmfSimpleTableViewer;

/**
 * Displays the segment store analysis data in a column table
 *
 * @author France Lapointe Nguyen
 * @since 2.0
 */
public abstract class AbstractSegmentStoreTableViewer extends TmfSimpleTableViewer {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Abstract class for the column label provider for the segment store
     * analysis table viewer
     */
    private abstract class SegmentStoreTableColumnLabelProvider extends ColumnLabelProvider {

        @Override
        public String getText(@Nullable Object input) {
            if (!(input instanceof ISegment)) {
                /* Doubles as a null check */
                return ""; //$NON-NLS-1$
            }
            return getTextForSegment((ISegment) input);
        }

        public abstract String getTextForSegment(ISegment input);
    }

    /**
     * Listener to update the model with the segment store analysis results
     * once the analysis is fully completed
     */
    private final class AnalysisProgressListener implements IAnalysisProgressListener {
        @Override
        public void onComplete(AbstractSegmentStoreAnalysisModule activeAnalysis, ISegmentStore<ISegment> data) {
            // Check if the active trace was changed while the analysis was
            // running
            if (activeAnalysis.equals(fAnalysisModule)) {
                updateModel(data);
            }
        }
    }

    /**
     * Listener to select a range in other viewers when a cell of the segment
     * store table view is selected
     */
    private class TableSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(@Nullable SelectionEvent e) {
            ISegment selectedSegment = ((ISegment) NonNullUtils.checkNotNull(e).item.getData());
            ITmfTimestamp start = new TmfNanoTimestamp(selectedSegment.getStart());
            ITmfTimestamp end = new TmfNanoTimestamp(selectedSegment.getEnd());
            TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(AbstractSegmentStoreTableViewer.this, start, end));
        }
    }

    /**
     * Current segment store analysis module
     */
    private @Nullable AbstractSegmentStoreAnalysisModule fAnalysisModule = null;

    /**
     * Analysis progress listener
     */
    private AnalysisProgressListener fListener;

    /**
     * Flag to create columns once
     */
    boolean fColumnsCreated = false;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param tableViewer
     *            Table viewer of the view
     */
    public AbstractSegmentStoreTableViewer(TableViewer tableViewer) {
        super(tableViewer);
        // Sort order of the content provider is by start time by default
        getTableViewer().setContentProvider(new SegmentStoreContentProvider());
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            fAnalysisModule = getSegmentStoreAnalysisModule(trace);
        }
        createColumns();
        getTableViewer().getTable().addSelectionListener(new TableSelectionListener());
        fListener = new AnalysisProgressListener();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Create default columns for start time, end time and duration
     */
    private void createColumns() {
        createColumn(Messages.SegmentStoreTableViewer_startTime, new SegmentStoreTableColumnLabelProvider() {
            @Override
            public String getTextForSegment(ISegment input) {
                return NonNullUtils.nullToEmptyString(TmfTimestampFormat.getDefaulTimeFormat().format(input.getStart()));
            }
        }, SegmentComparators.INTERVAL_START_COMPARATOR);

        createColumn(Messages.SegmentStoreTableViewer_endTime, new SegmentStoreTableColumnLabelProvider() {
            @Override
            public String getTextForSegment(ISegment input) {
                return NonNullUtils.nullToEmptyString(TmfTimestampFormat.getDefaulTimeFormat().format(input.getEnd()));
            }
        }, SegmentComparators.INTERVAL_END_COMPARATOR);

        createColumn(Messages.SegmentStoreTableViewer_duration, new SegmentStoreTableColumnLabelProvider() {
            @Override
            public String getTextForSegment(ISegment input) {
                return NonNullUtils.nullToEmptyString(Long.toString(input.getLength()));
            }
        }, SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
    }

    /**
     * Create columns specific to the analysis
     */
    protected void createAnalysisColumns() {
        if (!fColumnsCreated) {
            AbstractSegmentStoreAnalysisModule analysis = getAnalysisModule();
            if (analysis != null) {
                for (final ISegmentAspect aspect : analysis.getSegmentAspects()) {
                    createColumn(aspect.getName(), new SegmentStoreTableColumnLabelProvider() {
                        @Override
                        public String getTextForSegment(ISegment input) {
                            return NonNullUtils.nullToEmptyString(aspect.resolve(input));
                        }
                    },
                    aspect.getComparator());
                }
            }
            fColumnsCreated = true;
        }
    }

    /**
     * Update the data in the table viewer
     *
     * @param dataInput
     *            New data input
     */
    public void updateModel(final @Nullable Object dataInput) {
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
                    SegmentStoreContentProvider contentProvider = (SegmentStoreContentProvider) getTableViewer().getContentProvider();
                    tableViewer.setItemCount(contentProvider.getSegmentCount());
                }
            }
        });
    }

    /**
     * Set the data into the viewer. Will update model is analysis is completed
     * or run analysis if not completed
     *
     * @param analysis
     *            segment store analysis module
     */
    public void setData(@Nullable AbstractSegmentStoreAnalysisModule analysis) {
        // Set the current segment store analysis module
        fAnalysisModule = analysis;
        if (analysis == null) {
            updateModel(null);
            return;
        }

        createAnalysisColumns();

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

    /**
     * Returns the segment store analysis module
     * @param trace
     *            The trace to consider
     * @return the segment store analysis module
     */
    protected @Nullable abstract AbstractSegmentStoreAnalysisModule getSegmentStoreAnalysisModule(ITmfTrace trace);

    @Override
    protected void appendToTablePopupMenu(IMenuManager manager, IStructuredSelection sel) {
        final ISegment segment = (ISegment) sel.getFirstElement();
        if (segment != null) {
            IAction gotoStartTime = new Action(Messages.SegmentStoreTableViewer_goToStartEvent) {
                @Override
                public void run() {
                    broadcast(new TmfSelectionRangeUpdatedSignal(AbstractSegmentStoreTableViewer.this, new TmfNanoTimestamp(segment.getStart())));
                }
            };

            IAction gotoEndTime = new Action(Messages.SegmentStoreTableViewer_goToEndEvent) {
                @Override
                public void run() {
                    broadcast(new TmfSelectionRangeUpdatedSignal(AbstractSegmentStoreTableViewer.this, new TmfNanoTimestamp(segment.getEnd())));
                }
            };

            manager.add(gotoStartTime);
            manager.add(gotoEndTime);
        }
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * Get current segment store analysis module
     *
     * @return current segment store analysis module
     */
    public @Nullable AbstractSegmentStoreAnalysisModule getAnalysisModule() {
        return fAnalysisModule;
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Trace selected handler
     *
     * @param signal
     *            Different opened trace (on which segment store analysis as
     *            already been performed) has been selected
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        ITmfTrace trace = signal.getTrace();
        if (trace != null) {
            setData(getSegmentStoreAnalysisModule(trace));
        }
    }

    /**
     * Trace opened handler
     *
     * @param signal
     *            New trace (on which segment store analysis has not been
     *            performed) is opened
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        ITmfTrace trace = signal.getTrace();
        if (trace != null) {
            setData(getSegmentStoreAnalysisModule(trace));
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

            AbstractSegmentStoreAnalysisModule analysis = getAnalysisModule();
            if ((analysis != null)) {
                analysis.removeListener(fListener);
            }
        }
    }
}
