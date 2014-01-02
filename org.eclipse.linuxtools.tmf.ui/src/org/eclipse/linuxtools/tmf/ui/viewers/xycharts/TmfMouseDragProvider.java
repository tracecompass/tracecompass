/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.viewers.xycharts;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.swtchart.IAxis;
import org.swtchart.Range;

/**
 * Class for updating time ranges based on middle mouse button drag.
 * It also notifies the viewer about a change of range.
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
public class TmfMouseDragProvider extends TmfBaseProvider implements MouseListener, MouseMoveListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** Cached start time */
    private long fStartTime;
    /** Cached end time */
    private long fEndTime;
    /** Flag indicating that an update is ongoing */
    private boolean fIsUpdate;
    /** Cached position when mouse button was pressed */
    private int fStartPosition;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     *
     * @param tmfChartViewer
     *          the chart viewer reference.
     */
    public TmfMouseDragProvider(ITmfChartTimeProvider tmfChartViewer) {
        super(tmfChartViewer);
        register();
    }

    // ------------------------------------------------------------------------
    // TmfBaseProvider
    // ------------------------------------------------------------------------
    @Override
    public void register() {
        getChart().getPlotArea().addMouseListener(this);
        getChart().getPlotArea().addMouseMoveListener(this);
    }

    @Override
    public void deregister() {
        if ((getChartViewer().getControl() != null) && !getChartViewer().getControl().isDisposed()) {
            getChart().getPlotArea().removeMouseListener(this);
            getChart().getPlotArea().removeMouseMoveListener(this);
        }
    }

    @Override
    public void refresh() {
        // nothing to do
    }

    // ------------------------------------------------------------------------
    // MouseListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseDoubleClick(MouseEvent e) {
    }

    @Override
    public void mouseDown(MouseEvent e) {
        if ((getChartViewer().getWindowDuration() != 0) && (e.button == 2)) {
            fStartPosition = e.x;
            fIsUpdate = true;
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if ((fIsUpdate) && (fStartTime != fEndTime)) {
            ITmfChartTimeProvider viewer = getChartViewer();
            viewer.updateWindow(fStartTime, fEndTime);
        }
        fIsUpdate = false;
    }

    // ------------------------------------------------------------------------
    // MouseMoveListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseMove(MouseEvent e) {
        if (fIsUpdate) {
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);

            ITmfChartTimeProvider viewer = getChartViewer();

            fStartTime = viewer.getWindowStartTime();
            fEndTime = viewer.getWindowEndTime();

            long startTime = viewer.getStartTime();
            long endTime = viewer.getEndTime();

            long delta = 0;
            if (fStartPosition > e.x) {
                delta = (long) (xAxis.getDataCoordinate(fStartPosition) - xAxis.getDataCoordinate(e.x));
                long max = endTime - fEndTime;
                delta = Math.max(0, Math.min(delta, max));
                fStartTime = fStartTime + delta;
                fEndTime = fEndTime + delta;
            } else if (fStartPosition < e.x) {
                delta = (long) (xAxis.getDataCoordinate(e.x) - xAxis.getDataCoordinate(fStartPosition));
                long max = fStartTime - startTime;
                delta = Math.max(0, Math.min(delta, max));
                fStartTime = fStartTime - delta;
                fEndTime = fEndTime - delta;
            }

            xAxis.setRange(new Range(fStartTime - viewer.getTimeOffset(), fEndTime - viewer.getTimeOffset()));
        }
    }
}