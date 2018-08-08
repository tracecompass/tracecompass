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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.table.Messages;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.table.SegmentStoreContentProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser.FilterCu;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser.IFilterStrings;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TmfFilterAppliedSignal;
import org.eclipse.tracecompass.internal.segmentstore.core.arraylist.ArrayListStore;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IFilterProperty;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.table.TmfSimpleTableViewer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Displays the segment store provider data in a column table
 *
 * @author France Lapointe Nguyen
 * @since 2.0
 */
public abstract class AbstractSegmentStoreTableViewer extends TmfSimpleTableViewer {

    private static final Format FORMATTER = new DecimalFormat("###,###.##"); //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Abstract class for the column label provider for the segment store
     * provider table viewer
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
     * Listener to update the model with the segment store provider results once
     * its store is fully completed
     */
    private final class SegmentStoreProviderProgressListener implements IAnalysisProgressListener {
        @Override
        public void onComplete(ISegmentStoreProvider activeProvider, ISegmentStore<ISegment> data) {
            // Check if the active trace was changed while the provider was
            // building its segment store
            if (activeProvider.equals(fSegmentProvider)) {
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
            ITmfTimestamp start = TmfTimestamp.fromNanos(selectedSegment.getStart());
            ITmfTimestamp end = TmfTimestamp.fromNanos(selectedSegment.getEnd());
            TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(AbstractSegmentStoreTableViewer.this, start, end, fTrace));
        }
    }

    /**
     * Current segment store provider
     */
    private @Nullable ISegmentStoreProvider fSegmentProvider = null;

    /**
     * provider progress listener
     */
    private final @Nullable SegmentStoreProviderProgressListener fListener;

    /**
     * The selected trace
     */
    private @Nullable ITmfTrace fTrace;

    /**
     * Flag to create columns once
     */
    boolean fColumnsCreated = false;

    private Collection<String> fGlobalFilter = Collections.emptySet();

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
        this(tableViewer, true);
    }

    /**
     * Constructor
     *
     * @param tableViewer
     *            Table viewer of the view
     * @param withListener
     *            Whether to add a listener to this table viewer. For instance,
     *            for table viewers who are part of another view who update the
     *            table's data, this value can be <code>false</code> so only the
     *            other listeners will update the data
     * @since 2.0
     */
    public AbstractSegmentStoreTableViewer(TableViewer tableViewer, boolean withListener) {
        super(tableViewer);
        // Sort order of the content provider is by start time by default
        getTableViewer().setContentProvider(new SegmentStoreContentProvider());
        createColumns();
        getTableViewer().getTable().addSelectionListener(new TableSelectionListener());
        addPackListener();
        fListener = withListener ? new SegmentStoreProviderProgressListener() : null;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Sets the segment provider, use only in test, only run in display thread
     *
     * @param segmentProvider
     *            the segment provider
     * @since 1.2
     */
    @VisibleForTesting
    public void setSegmentProvider(ISegmentStoreProvider segmentProvider) {
        fSegmentProvider = segmentProvider;
        // Sort order of the content provider is by start time by default
        getTableViewer().setContentProvider(new SegmentStoreContentProvider());

        Table table = getTableViewer().getTable();
        table.setRedraw(false);
        while (table.getColumnCount() > 0) {
            table.getColumn(0).dispose();
        }
        createColumns();
        createProviderColumns();
        getTableViewer().getTable().addSelectionListener(new TableSelectionListener());
        addPackListener();
        table.setRedraw(true);
    }

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
                return NonNullUtils.nullToEmptyString(FORMATTER.format(input.getLength()));
            }
        }, SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
    }

    /**
     * Create columns specific to the provider
     */
    protected void createProviderColumns() {
        if (!fColumnsCreated) {
            ISegmentStoreProvider provider = getSegmentProvider();
            if (provider != null) {
                for (final ISegmentAspect aspect : provider.getSegmentAspects()) {
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
                    addPackListener();
                    tableViewer.setInput(dataInput);
                    SegmentStoreContentProvider contentProvider = (SegmentStoreContentProvider) getTableViewer().getContentProvider();
                    tableViewer.setItemCount((int) Math.min(Integer.MAX_VALUE, contentProvider.getSegmentCount()));
                }
            }
        });
    }

    /**
     * Set the data into the viewer. It will update the model. If the provider
     * is an analysis, the analysis will be scheduled.
     *
     * @param provider
     *            segment store provider
     */
    public void setData(@Nullable ISegmentStoreProvider provider) {
        // Set the current segment store provider
        fSegmentProvider = provider;
        if (provider == null) {
            updateModel(null);
            return;
        }

        createProviderColumns();

        ISegmentStore<ISegment> segStore = provider.getSegmentStore();
        // If results are not null, then the segment of the provider is ready
        // and model can be updated

        //FIXME Filtering should be done at the data provider level
        Map<@NonNull Integer, @NonNull Predicate<@NonNull Map<@NonNull String, @NonNull String>>> predicates = computeRegexPredicate();
        Predicate<ISegment> predicate = (segment) -> {
            if (!predicates.isEmpty()) {

                // Get the filter external input data
                Map<@NonNull String, @NonNull String> input = getFilterInput(segment, provider);

                // Test each predicates and set the status of the property associated to the
                // predicate
                boolean activateProperty = false;
                for (Map.Entry<Integer, Predicate<Map<String, String>>> mapEntry : predicates.entrySet()) {
                    Integer property = Objects.requireNonNull(mapEntry.getKey());
                    Predicate<Map<String, String>> value = Objects.requireNonNull(mapEntry.getValue());
                    if (property == IFilterProperty.DIMMED | property == IFilterProperty.EXCLUDE) {
                        boolean status = value.test(input);
                        activateProperty |= status;
                    }
                }
                return activateProperty;
            }
            return true;
        };

        if (segStore != null) {
            if (predicates.isEmpty()) {
                updateModel(segStore);
                return;
            }
            Collection<ISegment> filtered = Collections2.filter(segStore, seg -> predicate.test(seg));
            ISegmentStore<ISegment> filteredStore = new ArrayListStore<>(filtered.toArray(new ISegment[filtered.size()]));
            updateModel(filteredStore);
            return;
        }
        // If results are null, then add completion listener and if the provider
        // is an analysis, run the analysis
        updateModel(null);
        SegmentStoreProviderProgressListener listener = fListener;
        if (listener != null) {
            provider.addListener(listener);
        }
        if (provider instanceof IAnalysisModule) {
            ((IAnalysisModule) provider).schedule();
        }
    }

    private static Map<String, String> getFilterInput(ISegment segment, ISegmentStoreProvider provider) {
        Map<String, String> map = new HashMap<>();
        for(ISegmentAspect aspect : provider.getSegmentAspects()) {
            Object resolve = aspect.resolve(segment);
            if (resolve != null) {
                map.put(aspect.getName(), String.valueOf(resolve));
            }
        }
        return map;
    }

    /**
     * Compute the predicate for every property regexes
     *
     * @param queryFilter
     *            The query filter holding the regexes
     * @return A map of time event filters predicate by property
     * @since 3.1
     */
    protected Map<Integer, Predicate<@NonNull Map<@NonNull String, @NonNull String>>> computeRegexPredicate() {
        Multimap<@NonNull Integer, @NonNull String> regexes = getRegexes();
        Map<@NonNull Integer, @NonNull Predicate<@NonNull Map<@NonNull String, @NonNull String>>> predicates = new HashMap<>();
        for (Map.Entry<Integer, Collection<String>> entry : regexes.asMap().entrySet()) {
            String regex = IFilterStrings.mergeFilters(entry.getValue());
            FilterCu cu = FilterCu.compile(regex);
            Predicate<@NonNull Map<@NonNull String, @NonNull String>> predicate = cu != null ? cu.generate() : null;
            if (predicate != null) {
                predicates.put(entry.getKey(), predicate);
            }
        }
        return predicates;
    }

    /**
     * Returns the segment store provider
     *
     * @param trace
     *            The trace to consider
     * @return the segment store provider
     */
    protected @Nullable abstract ISegmentStoreProvider getSegmentStoreProvider(ITmfTrace trace);

    @Override
    protected void appendToTablePopupMenu(IMenuManager manager, IStructuredSelection sel) {
        final ISegment segment = (ISegment) sel.getFirstElement();
        if (segment != null) {
            IAction gotoStartTime = new Action(Messages.SegmentStoreTableViewer_goToStartEvent) {
                @Override
                public void run() {
                    broadcast(new TmfSelectionRangeUpdatedSignal(AbstractSegmentStoreTableViewer.this, TmfTimestamp.fromNanos(segment.getStart()), TmfTimestamp.fromNanos(segment.getStart()), fTrace));
                }
            };

            IAction gotoEndTime = new Action(Messages.SegmentStoreTableViewer_goToEndEvent) {
                @Override
                public void run() {
                    broadcast(new TmfSelectionRangeUpdatedSignal(AbstractSegmentStoreTableViewer.this, TmfTimestamp.fromNanos(segment.getEnd()), TmfTimestamp.fromNanos(segment.getEnd()), fTrace));
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
     * Get current segment store provider
     *
     * @return current segment store provider
     */
    public @Nullable ISegmentStoreProvider getSegmentProvider() {
        return fSegmentProvider;
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
        fTrace = trace;
        if (trace != null) {
            setData(getSegmentStoreProvider(trace));
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
        fTrace = trace;
        if (trace != null) {
            setData(getSegmentStoreProvider(trace));
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
                getTableViewer().setItemCount(0);
                refresh();
            }

            ISegmentStoreProvider provider = getSegmentProvider();
            if ((provider != null)) {
                SegmentStoreProviderProgressListener listener = fListener;
                if (listener != null) {
                    provider.removeListener(listener);
                }
            }
            fTrace = null;
        }
    }

    /**
     * Set or remove the global regex filter value
     *
     * @param signal
     *                   the signal carrying the regex value
     * @since 3.1
     */
    @TmfSignalHandler
    public void regexFilterApplied(TmfFilterAppliedSignal signal) {
        Collection<String> regex = signal.getFilter().getRegexes();
        setGlobalRegexFilter(regex);
        setData(getSegmentProvider());
    }

    private void setGlobalRegexFilter(Collection<String> regex) {
        fGlobalFilter = regex;
    }

    /**
     * This method build the multimap of regexes by property that will be used to
     * filter the timegraph states
     *
     * Override this method to add other regexes with their properties. The data
     * provider should handle everything after.
     *
     * @return The multimap of regexes by property
     * @since 3.1
     */
    protected Multimap<@NonNull Integer, @NonNull String> getRegexes() {
        Multimap<@NonNull Integer, @NonNull String> regexes = HashMultimap.create();

        if (!fGlobalFilter.isEmpty()) {
            regexes.putAll(IFilterProperty.DIMMED, checkNotNull(fGlobalFilter));
        }

        return regexes;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /*
     * Add the listener for SetData on the table
     */
    private void addPackListener() {
        getControl().addListener(SWT.SetData, new Listener() {
            @Override
            public void handleEvent(@Nullable Event event) {
                // Remove the listener before the pack
                getControl().removeListener(SWT.SetData, this);

                // Pack the column the first time data is set
                TableViewer tableViewer = getTableViewer();
                if (tableViewer != null) {
                    for (TableColumn col : tableViewer.getTable().getColumns()) {
                        col.pack();
                    }
                }
            }
        });
    }
}
