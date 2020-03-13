/**********************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.cpuusage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.core.presentation.XYPresentationProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Let the CPU usage tree set which entries should be shown as lines instead of
 * areas
 *
 * FIXME: This class is only temporary, until styles are provided by the data
 * provider itself. Remove then.
 *
 * @author Geneviève Bastien
 */
public class CPUUsagePresentationProvider extends XYPresentationProvider {

    private static final int DEFAULT_SERIES_WIDTH = 1;
    private Set<Long> fTotalSeries = new TreeSet<>();

    private static Map<ITmfTrace, CPUUsagePresentationProvider> INSTANCES = new HashMap<>();

    /**
     * Get the presentation provider for a specific trace
     *
     * @param trace
     *            The trace to get the provider for
     * @return The presentation provider
     */
    public static CPUUsagePresentationProvider getForTrace(ITmfTrace trace) {
        return INSTANCES.computeIfAbsent(trace, t -> new CPUUsagePresentationProvider());
    }

    /**
     * Set a series ID as a total series, that should have a line style
     *
     * @param id
     *            The ID of the series that is a total series
     */
    public void setTotalSeries(long id) {
        fTotalSeries.add(id);
    }

    @Override
    public @NonNull IYAppearance getAppearance(@NonNull Long seriesId) {
        if (fTotalSeries.contains(seriesId)) {
            return getAppearance(seriesId, IYAppearance.Type.LINE, DEFAULT_SERIES_WIDTH);
        }
        return getAppearance(seriesId, IYAppearance.Type.AREA, DEFAULT_SERIES_WIDTH);
    }

}
