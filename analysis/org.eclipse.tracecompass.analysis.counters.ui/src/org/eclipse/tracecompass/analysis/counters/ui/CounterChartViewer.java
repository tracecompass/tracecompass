/*******************************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.analysis.counters.core.CounterDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectedCounterQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;

/**
 * XY line chart which displays the counters data.
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 */
public final class CounterChartViewer extends TmfFilteredXYChartViewer {

    private boolean fIsCumulative = false;

    /**
     * Constructor
     *
     * @param parent
     *            Parent composite
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     */
    public CounterChartViewer(Composite parent, TmfXYChartSettings settings) {
        // Avoid displaying chart title and axis titles (to reduce wasted space)
        super(parent, settings, CounterDataProvider.ID);
    }

    /**
     * Display the counters data cumulatively or not.
     */
    public void toggleCumulative() {
        cancelUpdate();
        fIsCumulative ^= true;
        updateContent();
    }

    @Deprecated
    @Override
    protected TimeQueryFilter createQueryFilter(long start, long end, int nb) {
        return new SelectedCounterQueryFilter(start, end, nb, getSelected(), fIsCumulative);
    }

    @Override
    protected @NonNull Map<String, Object> createQueryParameters(long start, long end, int nb) {
        Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.selectionTimeQueryToMap(new SelectionTimeQueryFilter(start, end, nb, getSelected()));
        parameters.put(CounterDataProvider.CUMULATIVE_PARAMETER_KEY, fIsCumulative);
        return parameters;
    }
}
