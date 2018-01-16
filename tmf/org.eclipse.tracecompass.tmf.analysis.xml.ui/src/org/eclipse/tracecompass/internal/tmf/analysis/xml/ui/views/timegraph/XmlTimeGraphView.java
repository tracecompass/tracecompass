/*******************************************************************************
 * Copyright (c) 2014, 2017 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *   Geneviève Bastien - Review of the initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.timegraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.XmlViewInfo;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlDataProviderManager;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlTimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.views.TmfViewFactory;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider2;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry.Sampling;
import org.w3c.dom.Element;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * This view displays state system data in a time graph view. It uses an XML
 * {@link TmfXmlStrings#TIME_GRAPH_VIEW} element from an XML file. This
 * element defines which entries from the state system will be shown and also
 * gives additional information on the presentation of the view (states, colors,
 * etc)
 *
 * @author Florian Wininger
 * @author Mikael Ferland
 */
public class XmlTimeGraphView extends AbstractTimeGraphView {

    /** View ID. */
    public static final @NonNull String ID = "org.eclipse.linuxtools.tmf.analysis.xml.ui.views.timegraph"; //$NON-NLS-1$

    private static final String[] DEFAULT_COLUMN_NAMES = new String[] {
            Messages.XmlTimeGraphView_ColumnName,
            Messages.XmlTimeGraphView_ColumnId,
            Messages.XmlTimeGraphView_ColumnParentId,
    };

    private static final String[] DEFAULT_FILTER_COLUMN_NAMES = new String[] {
            Messages.XmlTimeGraphView_ColumnName,
            Messages.XmlTimeGraphView_ColumnId
    };

    /** The relative weight of the sash */
    private static final int[] fWeight = { 1, 2 };

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private static final Comparator<XmlTimeGraphEntryModel> XML_ENTRY_COMPARATOR = Comparator
            .comparing(XmlTimeGraphEntryModel::getPath, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(XmlTimeGraphEntryModel::getName).thenComparingLong(XmlTimeGraphEntryModel::getStartTime);

    private static final Comparator<ITimeGraphEntry> ENTRY_COMPARATOR = Comparator.comparing(x -> (XmlTimeGraphEntryModel) ((TimeGraphEntry) x).getModel(), XML_ENTRY_COMPARATOR);
    /** Timeout between updates in the build thread in ms */
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    private final @NonNull XmlViewInfo fViewInfo = new XmlViewInfo(ID);
    private final Map<String, Integer> fStringValueMap = new HashMap<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public XmlTimeGraphView() {
        super(ID, new XmlPresentationProvider());
        setWeight(fWeight);
        setTreeColumns(DEFAULT_COLUMN_NAMES);
        setTreeLabelProvider(new XmlTreeLabelProvider());
        setFilterColumns(DEFAULT_FILTER_COLUMN_NAMES);
        setFilterLabelProvider(new XmlTreeLabelProvider());
        setEntryComparator(ENTRY_COMPARATOR);
        addPartPropertyListener(event -> {
            Object newValue = event.getNewValue();
            if (event.getProperty().equals(TmfXmlStrings.XML_OUTPUT_DATA) && newValue instanceof String) {
                String data = (String) newValue;
                fViewInfo.setViewData(data);
                loadNewXmlView();
            }
        });
    }

    @Override
    public void createPartControl(Composite parent) {
        String name = getViewSite().getSecondaryId();
        if (name != null) {
            name = TmfViewFactory.getBaseSecId(name);
        }
        if (name != null) {
            /* must initialize view info before calling super */
            fViewInfo.setName(name);
        }
        super.createPartControl(parent);
    }

    private void loadNewXmlView() {
        rebuild();
    }

    private void setViewTitle(final String title) {
        Display.getDefault().asyncExec(() -> setPartName(title));
    }

    @Override
    protected String getNextText() {
        return Messages.XmlTimeGraphView_NextText;
    }

    @Override
    protected String getNextTooltip() {
        return Messages.XmlTimeGraphView_NextTooltip;
    }

    @Override
    protected String getPrevText() {
        return Messages.XmlTimeGraphView_PreviousText;
    }

    @Override
    protected String getPrevTooltip() {
        return Messages.XmlTimeGraphView_PreviousInterval;
    }

    /**
     * Getter for the presentation provider
     *
     * @return The time graph presentation provider
     */
    @Override
    protected XmlPresentationProvider getPresentationProvider() {
        return (XmlPresentationProvider) super.getPresentationProvider();
    }

    /**
     * Default label provider, it shows name, id and parent columns
     *
     * TODO: There should be a way to define columns in the XML
     */
    private static class XmlTreeLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof TimeGraphEntry) {
                TimeGraphEntry entry = (TimeGraphEntry) element;

                if (DEFAULT_COLUMN_NAMES[columnIndex].equals(Messages.XmlTimeGraphView_ColumnName)) {
                    return entry.getName();
                }

                ITimeGraphEntryModel model = entry.getModel();
                if (model instanceof XmlTimeGraphEntryModel) {
                    XmlTimeGraphEntryModel xmlModel = (XmlTimeGraphEntryModel) model;
                    if (DEFAULT_COLUMN_NAMES[columnIndex].equals(Messages.XmlTimeGraphView_ColumnId)) {
                        return xmlModel.getXmlId();
                    } else if (DEFAULT_COLUMN_NAMES[columnIndex].equals(Messages.XmlTimeGraphView_ColumnParentId)) {
                        return xmlModel.getXmlParentId();
                    }
                }
            }
            return EMPTY_STRING;
        }
    }

    private static class TraceEntry extends TimeGraphEntry {
        private final @NonNull ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel> fProvider;

        public TraceEntry(XmlTimeGraphEntryModel model, @NonNull ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel> provider) {
            super(model);
            fProvider = provider;
        }

        @Override
        public boolean hasTimeEvents() {
            return false;
        }

        public @NonNull ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel> getProvider() {
            return fProvider;
        }
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected void buildEntryList(ITmfTrace trace, ITmfTrace parentTrace, IProgressMonitor monitor) {
        /*
         * Get the view element from the XML file. If the element can't be found,
         * return.
         */
        Element viewElement = fViewInfo.getViewElement(TmfXmlStrings.TIME_GRAPH_VIEW);
        if (viewElement == null) {
            return;
        }
        ITimeGraphPresentationProvider2 pres = this.getPresentationProvider();
        if (pres instanceof XmlPresentationProvider) {
            /*
             * TODO: Each entry of a line could have their own states/color. That will
             * require an update to the presentation provider
             */
            ((XmlPresentationProvider) pres).loadNewStates(viewElement);
        }

        String title = fViewInfo.getViewTitle(viewElement);
        if (title == null) {
            title = Messages.XmlTimeGraphView_DefaultTitle;
        }
        setViewTitle(title);

        // Empty the additional state values
        fStringValueMap.clear();

        SubMonitor subMonitor = SubMonitor.convert(monitor);
        boolean complete = false;
        ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel> provider = XmlDataProviderManager.getInstance().getTimeGraphProvider(trace, viewElement);
        Map<Long, TimeGraphEntry> map = new HashMap<>();
        while (!complete && !subMonitor.isCanceled()) {
            TmfModelResponse<List<XmlTimeGraphEntryModel>> response = provider.fetchTree(new TimeQueryFilter(0, Long.MAX_VALUE, 2), subMonitor);
            if (response.getStatus() == ITmfResponse.Status.FAILED) {
                Activator.logError("Call Stack Data Provider failed: " + response.getStatusMessage()); //$NON-NLS-1$
                return;
            } else if (response.getStatus() == ITmfResponse.Status.CANCELLED) {
                return;
            }
            complete = response.getStatus() == ITmfResponse.Status.COMPLETED;

            List<XmlTimeGraphEntryModel> model = response.getModel();
            if (model != null) {
                /*
                 * Ensure that all the entries exist
                 */
                for (XmlTimeGraphEntryModel entry : model) {
                    map.computeIfAbsent(entry.getId(), id -> {
                        if (entry.getParentId() != -1) {
                            return new TimeGraphEntry(entry);
                        }
                        TraceEntry traceEntry = new TraceEntry(entry, provider);
                        addToEntryList(parentTrace, Collections.singletonList(traceEntry));
                        setStartTime(Long.min(getStartTime(), entry.getStartTime()));
                        setEndTime(Long.max(getEndTime(), entry.getEndTime()));
                        return traceEntry;
                    });
                }
                /*
                 * set the correct child / parent relation
                 */
                for (XmlTimeGraphEntryModel entry : model) {
                    TimeGraphEntry child = map.get(entry.getId());
                    TimeGraphEntry parent = map.get(entry.getParentId());
                    if (child != null && parent != null) {
                        child.updateModel(entry);
                        parent.addChild(child);
                    }
                }
                long start = getStartTime();
                long end = getEndTime();
                final long resolution = Long.max(1, (end - start) / getDisplayWidth());
                zoomEntries(map.values(), start, end, resolution, subMonitor);
            }
            if (parentTrace.equals(getTrace())) {
                refresh();
            }
            subMonitor.worked(1);

            if (!complete) {
                try {
                    Thread.sleep(BUILD_UPDATE_TIMEOUT);
                } catch (InterruptedException e) {
                    Activator.logError("Failed to wait for data provider", e); //$NON-NLS-1$
                }
            }
        }
    }

    @Override
    protected void zoomEntries(@NonNull Iterable<@NonNull TimeGraphEntry> entries, long zoomStartTime, long zoomEndTime,
            long resolution, @NonNull IProgressMonitor monitor) {
        if (resolution < 0) {
            return;
        }
        long zoomStart = Long.min(zoomStartTime, zoomEndTime);
        long zoomEnd = Long.max(zoomStartTime, zoomEndTime);
        List<@NonNull Long> times = StateSystemUtils.getTimes(zoomStart, zoomEnd, resolution);
        Sampling sampling = new Sampling(zoomStart, zoomEnd, resolution);
        Table<ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel>, Long, TimeGraphEntry> groupedEntries = filterGroup(entries, zoomStartTime, zoomEndTime);
        // One unit of work per data provider
        IProgressMonitor subMonitor = SubMonitor.convert(monitor, "XmlTimeGraphView#zoomEntries", groupedEntries.rowKeySet().size()); //$NON-NLS-1$
        for (Entry<ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel>, Map<Long, TimeGraphEntry>> entry : groupedEntries.rowMap().entrySet()) {
            Map<Long, TimeGraphEntry> map = entry.getValue();
            SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(times, map.keySet());
            TmfModelResponse<List<ITimeGraphRowModel>> fetchRowModel = entry.getKey().fetchRowModel(filter, monitor);

            List<ITimeGraphRowModel> model = fetchRowModel.getModel();
            if (model != null) {
                for (ITimeGraphRowModel rowModel : model) {
                    if (subMonitor.isCanceled()) {
                        return;
                    }
                    TimeGraphEntry tgEntry = map.get(rowModel.getEntryID());
                    if (tgEntry != null) {
                        List<ITimeEvent> events = createTimeEvents(tgEntry, rowModel.getStates());
                        if (Thread.currentThread() instanceof ZoomThread) {
                            applyResults(() -> {
                                tgEntry.setZoomedEventList(events);
                                if (fetchRowModel.getStatus() == ITmfResponse.Status.COMPLETED) {
                                    tgEntry.setSampling(sampling);
                                }
                            });
                        } else {
                            tgEntry.setEventList(events);
                        }
                    }
                }
            }
            subMonitor.worked(1);
        }
    }

    /**
     * Filter the entries to return only TimeGraphEntry which intersect the time
     * range and group them by data provider.
     *
     * @param visible
     *            the input list of visible entries
     * @param zoomStartTime
     *            the leftmost time bound of the view
     * @param zoomEndTime
     *            the rightmost time bound of the view
     * @return A Table of the visible entries keyed by their data provider and id.
     */
    private static Table<ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel>, Long, TimeGraphEntry> filterGroup(Iterable<TimeGraphEntry> visible,
            long zoomStartTime, long zoomEndTime) {
        Table<ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel>, Long, TimeGraphEntry> table = HashBasedTable.create();
        for (TimeGraphEntry entry : visible) {
            if (zoomStartTime <= entry.getEndTime() && zoomEndTime >= entry.getStartTime()) {
                table.put(getProvider(entry), entry.getModel().getId(), entry);
            }
        }
        return table;
    }

    /**
     * Get the {@link ITimeGraphDataProvider} from a {@link TimeGraphEntry}'s
     * parent.
     *
     * @param entry
     *            queried Control Flow Entry.
     * @return the {@link ITimeGraphDataProvider}
     */
    public static @NonNull ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel> getProvider(TimeGraphEntry entry) {
        ITimeGraphEntry parent = entry;
        while (parent != null) {
            if (parent instanceof TraceEntry) {
                return ((TraceEntry) parent).getProvider();
            }
            parent = parent.getParent();
        }
        throw new IllegalStateException(entry + " should have a TraceEntry parent"); //$NON-NLS-1$
    }

    /**
     * Create {@link ITimeEvent}s for an entry from the list of
     * {@link ITimeGraphState}s, filling in the gaps.
     *
     * @param entry
     *            the {@link TimeGraphEntry} on which we are working
     * @param values
     *            the list of {@link ITimeGraphState}s from the
     *            {@link ThreadStatusDataProvider}.
     * @return a contiguous List of {@link ITimeEvent}s
     */
    private List<ITimeEvent> createTimeEvents(TimeGraphEntry entry, List<ITimeGraphState> values) {
        List<ITimeEvent> events = new ArrayList<>(values.size());
        ITimeEvent prev = null;
        for (ITimeGraphState state : values) {
            ITimeEvent event = createTimeEvent(entry, state);
            if (prev != null) {
                long prevEnd = prev.getTime() + prev.getDuration();
                if (prevEnd < event.getTime()) {
                    // fill in the gap.
                    events.add(new TimeEvent(entry, prevEnd, event.getTime() - prevEnd));
                }
            }
            prev = event;
            events.add(event);
        }
        return events;
    }

    private ITimeEvent createTimeEvent(TimeGraphEntry entry, ITimeGraphState state) {
        String label = state.getLabel();
        if (state.getValue() == Integer.MIN_VALUE && label != null) {
            // String interval
            int status = getStringIndex(label);

            return new TimeEvent(entry, state.getStartTime(), state.getDuration(), status);
        }
        int status = (int) state.getValue();
        XmlPresentationProvider pres = getPresentationProvider();
        if (label != null && !pres.hasIndex(status) && !label.equals(String.valueOf(-1))) {
            status = getStringIndex(label);
        }
        return new TimeEvent(entry, state.getStartTime(), state.getDuration(), status);
    }

    private int getStringIndex(String state) {
        XmlPresentationProvider pres = getPresentationProvider();
        Integer statusInt = fStringValueMap.get(state);
        if (statusInt != null) {
            return statusInt;
        }

        // Add this new state to the presentation provider
        int status = pres.addState(state);
        fStringValueMap.put(state, status);
        return status;
    }

    @Override
    protected @NonNull Iterable<ITmfTrace> getTracesToBuild(@Nullable ITmfTrace trace) {
        /*
         * Return the current trace only. Experiments will return their
         * children's analyses
         */
        return (trace != null) ? Collections.singleton(trace) : Collections.emptyList();
    }

}
