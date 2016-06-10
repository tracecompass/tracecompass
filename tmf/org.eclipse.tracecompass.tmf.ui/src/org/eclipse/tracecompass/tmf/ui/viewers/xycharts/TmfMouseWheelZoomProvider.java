/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xycharts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.swtchart.IAxis;
import org.swtchart.Range;

/**
 * Class for providing zooming based on mouse wheel. It centers the zoom on
 * mouse position. It also notifies the viewer about a change of range.
 *
 * @author Bernd Hufmann
 */
public class TmfMouseWheelZoomProvider extends TmfBaseProvider implements MouseWheelListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final double ZOOM_FACTOR = 0.8;
    private static final long MIN_WINDOW_SIZE = 1;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Standard constructor.
     *
     * @param tmfChartViewer
     *            The parent histogram object
     */
    public TmfMouseWheelZoomProvider(ITmfChartTimeProvider tmfChartViewer) {
        super(tmfChartViewer);
        register();
    }

    // ------------------------------------------------------------------------
    // TmfBaseProvider
    // ------------------------------------------------------------------------
    @Override
    public void register() {
        getChart().getPlotArea().addMouseWheelListener(this);
    }

    @Override
    public void deregister() {
        if ((getChartViewer().getControl() != null) && !getChartViewer().getControl().isDisposed()) {
            getChart().getPlotArea().removeMouseWheelListener(this);
        }
    }

    @Override
    public void refresh() {
        // nothing to do
    }

    // ------------------------------------------------------------------------
    // MouseWheelListener
    // ------------------------------------------------------------------------
    @Override
    public synchronized void mouseScrolled(MouseEvent event) {
        ITmfChartTimeProvider viewer = getChartViewer();

        final int count = event.count;
        if (count != 0 && (event.stateMask & SWT.CTRL) != 0) {
            final int x = event.x;
            zoom(viewer, count, x);
        }
    }

    private void zoom(ITmfChartTimeProvider viewer, final int count, final int x) {
        // Compute the new time range
        long newDuration = viewer.getWindowDuration();
        if (newDuration == 0 || count == 0) {
            return;
        }
        double ratio = 1.0;
        if (count > 0) {
            ratio = ZOOM_FACTOR;
            newDuration = Math.round(ZOOM_FACTOR * newDuration);
        } else {
            ratio = 1.0 / ZOOM_FACTOR;
            newDuration = (long) Math.ceil(newDuration * ratio);
        }
        newDuration = Math.max(MIN_WINDOW_SIZE, newDuration);

        // Center the zoom on mouse position, distribute new duration and adjust for boundaries.
        IAxis xAxis = getChart().getAxisSet().getXAxis(0);
        long timeAtXPos = limitXDataCoordinate(xAxis.getDataCoordinate(x)) + viewer.getTimeOffset();
        // Note: ratio = newDuration/oldDuration
        long newWindowStartTime = timeAtXPos - Math.round(ratio * (timeAtXPos - viewer.getWindowStartTime()));
        long newWindowEndTime = validateWindowEndTime(newWindowStartTime, newWindowStartTime + newDuration);
        newWindowStartTime = validateWindowStartTime(newWindowStartTime);
        viewer.updateWindow(newWindowStartTime, newWindowEndTime);
        xAxis.setRange(new Range(newWindowStartTime - viewer.getTimeOffset(),
            newWindowEndTime - viewer.getTimeOffset()));
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    private long validateWindowStartTime(long start) {
        ITmfChartTimeProvider viewer = getChartViewer();
        long realStart = start;

        long startTime = viewer.getStartTime();
        long endTime = viewer.getEndTime();

        if (realStart < startTime) {
            realStart = startTime;
        }
        if (realStart > endTime) {
            realStart = endTime;
        }
        return realStart;
    }

    private long validateWindowEndTime(long start, long end) {
        ITmfChartTimeProvider viewer = getChartViewer();
        long realEnd = end;
        long endTime = viewer.getEndTime();

        if (realEnd > endTime) {
            realEnd = endTime;
        }
        if (realEnd < start + MIN_WINDOW_SIZE) {
            realEnd = start + MIN_WINDOW_SIZE;
        }
        return realEnd;
    }
}