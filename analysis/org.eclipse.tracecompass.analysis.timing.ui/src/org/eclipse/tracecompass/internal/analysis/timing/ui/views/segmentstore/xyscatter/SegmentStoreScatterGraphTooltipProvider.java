/**********************************************************************
 * Copyright (c) 2015, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.xyscatter;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.ITmfChartTimeProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.IXYSeries;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfClosestDataPointTooltipProvider;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.Resolution;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat;

/**
 * Tooltip provider for durations scatter charts. It displays the y value of
 * position x as well as it highlights the closest data point.
 *
 * @author Bernd Hufmann
 */
public class SegmentStoreScatterGraphTooltipProvider extends TmfClosestDataPointTooltipProvider{

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor for the segment store scatter chart tooltip provider.
     *
     * @param tmfChartViewer
     *                  - the parent chart viewer
     */
    public SegmentStoreScatterGraphTooltipProvider(ITmfChartTimeProvider tmfChartViewer) {
        super(tmfChartViewer);
    }

    // ------------------------------------------------------------------------
    // TmfClosestDataPointTooltipProvider
    // ------------------------------------------------------------------------
    @Override
    protected @Nullable Map<String, Map<String, Object>> createToolTipMap(Parameter param) {
        List<IXYSeries> series = getSeries();
        int seriesIndex = param.getSeriesIndex();
        int dataIndex = param.getDataIndex();
        if ((series != null) && (seriesIndex < series.size())) {
            Map<String, Object> segMap = new HashMap<>();
            IXYSeries serie = series.get(seriesIndex);
            double[] xS = serie.getXSeries();
            double[] yS = serie.getYSeries();
            if ((xS != null) && (yS != null) && (dataIndex < xS.length) && (dataIndex < yS.length)) {
                ITmfTimestamp segTs = TmfTimestamp.fromNanos((long) xS[dataIndex] + getChartViewer().getTimeOffset());
                long segDuration = (long) yS[dataIndex];
                segMap.put(checkNotNull(Messages.SegmentStoreScatterGraphViewer_xAxis), segTs);
                segMap.put(checkNotNull(Messages.SegmentStoreScatterGraphViewer_yAxis), String.valueOf(FormatTimeUtils.formatDelta(segDuration, TimeFormat.RELATIVE, Resolution.NANOSEC)));
                return Collections.singletonMap(checkNotNull(Messages.SegmentStoreScatterGraphTooltipProvider_category), segMap);
            }
        }
        return null;
    }

}