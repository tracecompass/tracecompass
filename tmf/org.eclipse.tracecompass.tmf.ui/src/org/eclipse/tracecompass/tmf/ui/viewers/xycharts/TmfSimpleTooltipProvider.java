/**********************************************************************
 * Copyright (c) 2013, 2019 Ericsson
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
package org.eclipse.tracecompass.tmf.ui.viewers.xycharts;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfAbstractToolTipHandler;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;

/**
 * Tool tip provider for TMF chart viewer. It displays the x and y value of the
 * current mouse position.
 *
 * @author Bernd Hufmann
 */
public class TmfSimpleTooltipProvider extends TmfBaseProvider implements MouseTrackListener {

    private final class XYToolTipHandler extends TmfAbstractToolTipHandler {
        @Override
        public void fill(Control control, MouseEvent event, Point pt) {
            Chart chart = getChart();
            IAxisSet axisSet = chart.getAxisSet();
            IAxis xAxis = axisSet.getXAxis(0);
            IAxis yAxis = axisSet.getYAxis(0);

            double xCoordinate = xAxis.getDataCoordinate(pt.x);
            double yCoordinate = yAxis.getDataCoordinate(pt.y);

            ITmfChartTimeProvider viewer = getChartViewer();

            ITmfTimestamp time = TmfTimestamp.fromNanos((long) xCoordinate + viewer.getTimeOffset());
            /* set tooltip of current data point */
            addItem(null, ToolTipString.fromString("x"), ToolTipString.fromTimestamp(time.toString(), time.toNanos())); //$NON-NLS-1$
            addItem(null, "y", Double.toString(yCoordinate)); //$NON-NLS-1$
        }
    }

    private TmfAbstractToolTipHandler fTooltipHandler = new XYToolTipHandler();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor for a tool tip provider.
     *
     * @param tmfChartViewer
     *            The parent chart viewer
     */
    public TmfSimpleTooltipProvider(ITmfChartTimeProvider tmfChartViewer) {
        super(tmfChartViewer);
        register();
    }

    // ------------------------------------------------------------------------
    // TmfBaseProvider
    // ------------------------------------------------------------------------
    @Override
    public void register() {
        fTooltipHandler.activateHoverHelp(getChart().getPlotArea());
    }

    @Override
    public void deregister() {
        if ((getChartViewer().getControl() != null) && !getChartViewer().getControl().isDisposed()) {
            fTooltipHandler.deactivateHoverHelp(getChart().getPlotArea());
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
        // Do nothing
    }

    @Override
    public void mouseExit(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseHover(MouseEvent e) {
        // Do nothing
    }
}