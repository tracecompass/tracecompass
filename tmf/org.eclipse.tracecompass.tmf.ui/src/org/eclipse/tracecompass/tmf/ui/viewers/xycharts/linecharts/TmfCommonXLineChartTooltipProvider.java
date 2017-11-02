/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts;

import java.util.Arrays;
import java.util.Formatter;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.ITmfChartTimeProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfBaseProvider;
import org.swtchart.IAxis;
import org.swtchart.ISeries;

/**
 * Displays a tooltip on line charts. For each series, it shows the y value at
 * the selected x value. This tooltip assumes that all series share a common set
 * of X axis values. If the X series is not common, the tooltip text may not be
 * accurate.
 *
 * @author Geneviève Bastien
 */
public class TmfCommonXLineChartTooltipProvider extends TmfBaseProvider implements MouseTrackListener {

    /**
     * Constructor for the tooltip provider
     *
     * @param tmfChartViewer
     *            The parent chart viewer
     */
    public TmfCommonXLineChartTooltipProvider(ITmfChartTimeProvider tmfChartViewer) {
        super(tmfChartViewer);
        register();
    }

    // ------------------------------------------------------------------------
    // TmfBaseProvider
    // ------------------------------------------------------------------------

    @Override
    public void register() {
        getChart().getPlotArea().addMouseTrackListener(this);
    }

    @Override
    public void deregister() {
        if ((getChartViewer().getControl() != null) && !getChartViewer().getControl().isDisposed()) {
            getChart().getPlotArea().removeMouseTrackListener(this);
        }
    }

    @Override
    public void refresh() {
        // nothing to do
    }

    // ------------------------------------------------------------------------
    // MouseTrackListener
    // ------------------------------------------------------------------------

    @Override
    public void mouseEnter(MouseEvent e) {
    }

    @Override
    public void mouseExit(MouseEvent e) {
    }

    @Override
    public void mouseHover(MouseEvent e) {
        if (getChartViewer().getWindowDuration() != 0) {
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);

            double xCoordinate = xAxis.getDataCoordinate(e.x);

            ISeries[] series = getChart().getSeriesSet().getSeries();

            if ((xCoordinate < 0) || (series.length == 0)) {
                return;
            }

            /* Find the index of the value we want */
            double[] xS = series[0].getXSeries();
            if (xS == null) {
                return;
            }
            int index = Arrays.binarySearch(xS, xCoordinate);
            index = index >= 0 ? index : -index - 1;
            int maxLen = 0;
            for (ISeries serie : series) {
                /* Make sure the series values and the value at index exist */
                if (isValid(index, serie)) {
                    maxLen = Math.max(maxLen, serie.getId().length());
                }
            }

            /* set tooltip of closest data point */
            StringBuilder buffer = new StringBuilder(Messages.TmfCommonXLineChartTooltipProvider_time + "\t" //$NON-NLS-1$
                    + TmfTimestamp.fromNanos((long) xCoordinate + getChartViewer().getTimeOffset()).toString() + "\n"); //$NON-NLS-1$

            // FIXME this is overridden by the shell font.
            String formatString = "%-" + (maxLen) + "s%12.2f%n"; //$NON-NLS-1$ //$NON-NLS-2$
            try (Formatter formatter = new Formatter(buffer)) {
                /* For each series, get the value at the index */
                for (ISeries serie : series) {
                    double[] yS = serie.getYSeries();
                    /* Make sure the series values and the value at index exist */
                    if (isValid(index, serie)) {
                        formatter.format(formatString, serie.getId(), yS[index]);
                    }
                }
            }

            getChart().getPlotArea().setToolTipText(buffer.toString());
            getChart().redraw();
        }
    }

    private static boolean isValid(int index, ISeries serie) {
        double[] ySeries = serie.getYSeries();
        return serie.isVisible() && ySeries != null && ySeries.length > index;
    }

}
