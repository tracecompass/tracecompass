/**********************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.scatter;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.ITmfChartTimeProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfClosestDataPointTooltipProvider;
import org.swtchart.ISeries;

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
        register();
    }

    // ------------------------------------------------------------------------
    // TmfClosestDataPointTooltipProvider
    // ------------------------------------------------------------------------
    @Override
    protected @Nullable String createToolTipText(Parameter param) {
        ISeries[] series = getChart().getSeriesSet().getSeries();
        int seriesIndex = param.getSeriesIndex();
        int dataIndex = param.getDataIndex();
        if ((series != null) && (seriesIndex < series.length)) {
            ISeries serie = series[seriesIndex];
            double[] xS = serie.getXSeries();
            double[] yS = serie.getYSeries();
            if ((xS != null) && (yS != null) && (dataIndex < xS.length) && (dataIndex < yS.length)) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(checkNotNull(Messages.SegmentStoreScatterGraphViewer_xAxis)).append('=');
                buffer.append(new TmfTimestamp((long) xS[dataIndex] + getChartViewer().getTimeOffset(), ITmfTimestamp.NANOSECOND_SCALE).toString());
                buffer.append('\n');
                buffer.append(Messages.SegmentStoreScatterGraphViewer_yAxis).append('=');
                buffer.append((long) yS[dataIndex]);
                return buffer.toString();
            }
        }
        return null;
    }

}