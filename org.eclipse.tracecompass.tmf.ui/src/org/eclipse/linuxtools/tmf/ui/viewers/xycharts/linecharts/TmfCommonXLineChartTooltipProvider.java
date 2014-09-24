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

package org.eclipse.linuxtools.tmf.ui.viewers.xycharts.linecharts;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.viewers.xycharts.ITmfChartTimeProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.xycharts.TmfBaseProvider;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.swtchart.IAxis;
import org.swtchart.ISeries;

/**
 * Displays a tooltip on line charts. For each series, it shows the y value at
 * the selected x value. This tooltip assumes that all series share a common set
 * of X axis values. If the X series is not common, the tooltip text may not be
 * accurate.
 *
 * @author Geneviève Bastien
 * @since 3.0
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
            int index = 0;
            for (int i = 0; i < xS.length; i++) {
                if (xS[i] > xCoordinate) {
                    break;
                }
                index = i;
            }

            /* set tooltip of closest data point */
            StringBuffer buffer = new StringBuffer();
            buffer.append("time="); //$NON-NLS-1$
            buffer.append(new TmfTimestamp((long) xCoordinate + getChartViewer().getTimeOffset(), ITmfTimestamp.NANOSECOND_SCALE).toString());
            buffer.append('\n');

            /* For each series, get the value at the index */
            for (ISeries serie : series) {
                double[] yS = serie.getYSeries();
                /* Make sure the series values and the value at index exist */
                if (yS == null || yS.length <= index) {
                    continue;
                }
                buffer.append(serie.getId());
                buffer.append('=');
                buffer.append(yS[index]);
                buffer.append('\n');
            }

            getChart().getPlotArea().setToolTipText(buffer.toString());
            getChart().redraw();
        }
    }

}
