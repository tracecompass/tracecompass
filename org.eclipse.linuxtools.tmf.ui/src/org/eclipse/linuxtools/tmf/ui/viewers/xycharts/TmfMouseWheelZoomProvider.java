/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.xycharts;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.swtchart.IAxis;

/**
 * Class for providing zooming based on mouse wheel. It centers the zoom on
 * mouse position. It also notifies the viewer about a change of range.
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
public class TmfMouseWheelZoomProvider extends TmfBaseProvider implements MouseWheelListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private final static double ZOOM_FACTOR = 0.8;
    private final static long MIN_WINDOW_SIZE = 1;

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

        long oldDuration = viewer.getWindowDuration();

        if (oldDuration == 0) {
            return;
        }

        // Compute the new time range
        long newDuration = oldDuration;
        double ratio = 1.0;
        if (event.count > 0) {
            ratio = ZOOM_FACTOR;
            newDuration = Math.round(ZOOM_FACTOR * oldDuration);
        } else {
            ratio = 1.0 / ZOOM_FACTOR;
            newDuration = (long) Math.ceil(oldDuration * ratio);
        }
        newDuration = Math.max(MIN_WINDOW_SIZE, newDuration);

        // Center the zoom on mouse position, distribute new duration and adjust for boundaries.
        IAxis xAxis = getChart().getAxisSet().getXAxis(0);
        long timeAtXPos = limitXDataCoordinate(xAxis.getDataCoordinate(event.x)) + viewer.getTimeOffset();
        // Note: ratio = newDuration/oldDuration
        long newWindowStartTime = timeAtXPos - Math.round(ratio * (timeAtXPos - viewer.getWindowStartTime()));
        long newWindowEndTime = validateWindowEndTime(newWindowStartTime, newWindowStartTime + newDuration);
        newWindowStartTime = validateWindowStartTime(newWindowStartTime);
        viewer.updateWindow(newWindowStartTime, newWindowEndTime);
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