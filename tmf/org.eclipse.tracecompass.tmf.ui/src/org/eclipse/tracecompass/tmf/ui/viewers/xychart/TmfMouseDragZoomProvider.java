/**********************************************************************
 * Copyright (c) 2013, 2020 Ericsson
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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swtchart.ICustomPaintListener;

/**
 * Class for providing zooming based on mouse drag with right mouse button.
 * It also notifies the viewer about a change of range.
 *
 * @author Bernd Hufmann
 * @since 6.0
 */
public class TmfMouseDragZoomProvider extends TmfBaseProvider implements MouseListener, MouseMoveListener, ICustomPaintListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** Cached start time */
    private long fStartTime;
    /** Cached end time */
    private long fEndTime;
    /** Flag indicating that an update is ongoing */
    private boolean fIsUpdate;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     *
     * @param tmfChartViewer
     *          the chart viewer reference.
     */
    public TmfMouseDragZoomProvider(ITmfChartTimeProvider tmfChartViewer) {
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
        if ((getChartViewer().getWindowDuration() != 0) && (e.button == 3)) {
            IAxis xAxis = getXAxis();
            fStartTime = limitXDataCoordinate(xAxis.getDataCoordinate(e.x));
            fEndTime = fStartTime;
            fIsUpdate = true;
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if ((fIsUpdate) && (fStartTime != fEndTime)) {
            if (fStartTime > fEndTime) {
                long tmp = fStartTime;
                fStartTime = fEndTime;
                fEndTime = tmp;
            }
            ITmfChartTimeProvider viewer = getChartViewer();
            viewer.updateWindow(fStartTime + viewer.getTimeOffset(), fEndTime + viewer.getTimeOffset());
        }

        if (fIsUpdate) {
            getChartViewer().getControl().redraw();
        }
        fIsUpdate = false;
    }

    // ------------------------------------------------------------------------
    // MouseMoveListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseMove(MouseEvent e) {
        if (fIsUpdate) {
            IAxis xAxis = getXAxis();
            fEndTime = limitXDataCoordinate(xAxis.getDataCoordinate(e.x));

            ITmfChartTimeProvider viewer = getChartViewer();
            if (viewer instanceof TmfXYChartViewer) {
                TmfXYChartViewer xyChartViewer = (TmfXYChartViewer) viewer;
                xyChartViewer.updateStatusLine(fStartTime, fEndTime, limitXDataCoordinate(xAxis.getDataCoordinate(e.x)));
            }

            getChartViewer().getControl().redraw();
        }
    }

    // ------------------------------------------------------------------------
    // ICustomPaintListener
    // ------------------------------------------------------------------------
    @Override
    public void paintControl(PaintEvent e) {
        if (fIsUpdate && (fStartTime != fEndTime)) {
            IAxis xAxis = getXAxis();
            int startX = xAxis.getPixelCoordinate(fStartTime);
            int endX = xAxis.getPixelCoordinate(fEndTime);
            int prevAlpha = e.gc.getAlpha();
            e.gc.setAlpha(64);
            e.gc.setBackground(TmfXYChartViewer.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
            if (fStartTime < fEndTime) {
                e.gc.fillRectangle(startX, 0, endX - startX, e.height);
            } else {
                e.gc.fillRectangle(endX, 0, startX - endX, e.height);
            }
            e.gc.setAlpha(prevAlpha);
            e.gc.drawLine(startX, 0, startX, e.height);
            e.gc.drawLine(endX, 0, endX, e.height);
        }
    }

    @Override
    public boolean drawBehindSeries() {
        return false;
    }
}