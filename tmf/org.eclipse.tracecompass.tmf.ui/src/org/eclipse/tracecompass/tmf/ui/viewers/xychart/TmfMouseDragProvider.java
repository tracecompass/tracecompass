/**********************************************************************
 * Copyright (c) 2014, 2020 Ericsson
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
package org.eclipse.tracecompass.tmf.ui.viewers.xychart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

/**
 * Class for updating time ranges based on middle mouse button drag.
 * It also notifies the viewer about a change of range.
 *
 * @author Bernd Hufmann
 * @since 6.0
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
    /** cursor for move */
    private final Cursor fMoveCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_HAND);

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
    }

    // ------------------------------------------------------------------------
    // TmfBaseProvider
    // ------------------------------------------------------------------------

    @Override
    public void refresh() {
        // nothing to do
    }

    // ------------------------------------------------------------------------
    // MouseListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseDoubleClick(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseDown(MouseEvent e) {
        if ((getChartViewer().getWindowDuration() != 0) && ((e.button == 2) || (e.button == 1 && (e.stateMask & SWT.CTRL) != 0))) {
            fStartPosition = e.x;
            fIsUpdate = true;
            getChartViewer().getControl().setCursor(fMoveCursor);
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if ((fIsUpdate) && (fStartTime != fEndTime)) {
            ITmfChartTimeProvider viewer = getChartViewer();
            viewer.updateWindow(fStartTime, fEndTime);
        }
        fIsUpdate = false;
        getChartViewer().getControl().setCursor(null);
    }

    // ------------------------------------------------------------------------
    // MouseMoveListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseMove(MouseEvent e) {
        if (fIsUpdate) {
            IAxis xAxis = getXAxis();

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

            xAxis.setRange(new AxisRange(fStartTime - viewer.getTimeOffset(), fEndTime - viewer.getTimeOffset()));
            redraw();
        }
    }
}