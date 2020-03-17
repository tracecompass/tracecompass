/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.timegraph.BaseXYPresentationProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.IOutputStyleProvider;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ICheckboxTreeViewerListener;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;
import org.swtchart.Chart;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * XY chart viewer which queries and displays selected entries.
 *
 * @author Loic Prieur-Drevon
 * @since 3.2
 */
public class TmfFilteredXYChartViewer extends TmfCommonXAxisChartViewer implements ICheckboxTreeViewerListener {

    private static final int DEFAULT_SERIES_WIDTH = 2;

    private @NonNull Collection<@NonNull Long> fSelectedIds = Collections.emptyList();

    private final String fId;
    private boolean fUseDefaultStyleValues = true;

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     * @param id
     *            The ITmfXYDataProvider ID.
     */
    public TmfFilteredXYChartViewer(Composite parent, TmfXYChartSettings settings, String id) {
        super(parent, settings);
        Chart chart = getSwtChart();
        // Avoid displaying chart title and axis titles (to reduce wasted space)
        chart.getLegend().setVisible(false);
        fId = id;
    }

    /**
     * Update the chart depending on the selected entries.
     *
     * @param entries
     *            Counters to display on the chart
     */
    @Override
    public void handleCheckStateChangedEvent(Collection<ITmfTreeViewerEntry> entries) {
        cancelUpdate();

        Iterable<TmfGenericTreeEntry> counterEntries = Iterables.filter(entries, TmfGenericTreeEntry.class);
        Collection<@NonNull Long> selectedIds = Sets.newHashSet(Iterables.transform(counterEntries, e -> e.getModel().getId()));
        if (!selectedIds.containsAll(fSelectedIds)) {
            clearContent();
        }
        fSelectedIds = selectedIds;
        // Update the styles as well
        BaseXYPresentationProvider presProvider = getPresentationProvider2();
        for (ITmfTreeViewerEntry entry : entries) {
            if (entry instanceof TmfGenericTreeEntry) {
                TmfGenericTreeEntry<TmfTreeDataModel> genericEntry = (TmfGenericTreeEntry<TmfTreeDataModel>) entry;
                TmfTreeDataModel model = genericEntry.getModel();
                OutputElementStyle style = model.getStyle();
                if (style != null) {
                    presProvider.setStyle(model.getId(), style);
                }
            }
        }

        updateContent();
    }

    @TmfSignalHandler
    @Override
    public void traceSelected(@Nullable TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);
        clearContent();
        fSelectedIds.clear();
    }

    @TmfSignalHandler
    @Override
    public void traceClosed(@Nullable TmfTraceClosedSignal signal) {
        if (signal != null && signal.getTrace().equals(getTrace())) {
            fSelectedIds.clear();
        }
        super.traceClosed(signal);
    }

    @Override
    protected @NonNull Map<String, Object> createQueryParameters(long start, long end, int nb) {
        Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.selectionTimeQueryToMap(new SelectionTimeQueryFilter(start, end, nb, fSelectedIds));
        Multimap<@NonNull Integer, @NonNull String> regexesMap = getRegexes();
        if (!regexesMap.isEmpty()) {
            parameters.put(DataProviderParameterUtils.REGEX_MAP_FILTERS_KEY, regexesMap.asMap());
        }
        return parameters;
    }

    /**
     * Get the IDs of the selected entries
     *
     * @return a collection of the IDs of the selected entries
     */
    public @NonNull Collection<@NonNull Long> getSelected() {
        return fSelectedIds;
    }

    @Override
    public @NonNull IYAppearance getSeriesAppearance(@NonNull String seriesName) {
        return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.LINE, DEFAULT_SERIES_WIDTH);
    }

    @Override
    public @NonNull OutputElementStyle getSeriesStyle(@NonNull Long seriesId) {
        return fUseDefaultStyleValues ? getPresentationProvider2().getSeriesStyle(seriesId, IYAppearance.Type.LINE, DEFAULT_SERIES_WIDTH) : getPresentationProvider2().getSeriesStyle(seriesId);
    }

    @Override
    protected ITmfXYDataProvider initializeDataProvider(ITmfTrace trace) {
        ITmfTreeXYDataProvider dataProvider = DataProviderManager.getInstance().getDataProvider(trace, fId, ITmfTreeXYDataProvider.class);
        if (dataProvider instanceof IOutputStyleProvider) {
            getPresentationProvider2().addProvider(dataProvider);
            fUseDefaultStyleValues = false;
        }
        return dataProvider;
    }

}
