/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.swtchart.IAxis;
import org.swtchart.ICustomPaintListener;
import org.swtchart.IPlotArea;

/**
 * Class for providing selection of ranges with the left mouse button.
 * It also notifies the viewer about a change of selection.
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
public class TmfMouseSelectionProvider extends TmfBaseProvider implements MouseListener, MouseMoveListener, ICustomPaintListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** Cached start time */
    private long fBeginTime;
    /** Cached end time */
    private long fEndTime;
    /** Flag indicating that an update is ongoing */
    private boolean fIsInternalUpdate;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     *
     * @param tmfChartViewer
     *          The chart viewer reference.
     */
    public TmfMouseSelectionProvider(ITmfChartTimeProvider tmfChartViewer) {
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
        ((IPlotArea) getChart().getPlotArea()).addCustomPaintListener(this);
    }

    @Override
    public void deregister() {
        if ((getChartViewer().getControl() != null) && !getChartViewer().getControl().isDisposed()) {
            getChart().getPlotArea().removeMouseListener(this);
            getChart().getPlotArea().removeMouseMoveListener(this);
            ((IPlotArea) getChart().getPlotArea()).removeCustomPaintListener(this);
        }
    }

    // ------------------------------------------------------------------------
    // MouseListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseDoubleClick(MouseEvent e) {
    }

    @Override
    public void mouseDown(MouseEvent e) {
        if ((getChartViewer().getWindowDuration() != 0) && (e.button == 1)) {
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);
            fBeginTime = limitXDataCoordinate(xAxis.getDataCoordinate(e.x));
            fEndTime = fBeginTime;
            fIsInternalUpdate = true;
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if (fIsInternalUpdate) {
            if (fBeginTime > fEndTime) {
                // Swap time
                long tmp = fBeginTime;
                fBeginTime = fEndTime;
                fEndTime = tmp;
            }
            ITmfChartTimeProvider viewer = getChartViewer();
            viewer.updateSelectionRange(fBeginTime + viewer.getTimeOffset(), fEndTime + viewer.getTimeOffset());
            fIsInternalUpdate = false;
            getChart().redraw();
        }
    }

    // ------------------------------------------------------------------------
    // MouseMoveListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseMove(MouseEvent e) {
        if (fIsInternalUpdate) {
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);
            fEndTime = limitXDataCoordinate(xAxis.getDataCoordinate(e.x));
            getChart().redraw();
        }
    }

    // ------------------------------------------------------------------------
    // ICustomPaintListener
    // ------------------------------------------------------------------------
    @Override
    public void paintControl(PaintEvent e) {
        ITmfChartTimeProvider viewer = getChartViewer();

        if (!fIsInternalUpdate) {
            fBeginTime = viewer.getSelectionBeginTime() - viewer.getTimeOffset();
            fEndTime = viewer.getSelectionEndTime() - viewer.getTimeOffset();
        }
        long windowStartTime = viewer.getWindowStartTime() - viewer.getTimeOffset();
        long windowEndTime = viewer.getWindowEndTime() - viewer.getTimeOffset();

        IAxis xAxis = getChart().getAxisSet().getXAxis(0);
        e.gc.setBackground(TmfXYChartViewer.getDisplay().getSystemColor(SWT.COLOR_BLUE));
        e.gc.setForeground(TmfXYChartViewer.getDisplay().getSystemColor(SWT.COLOR_BLUE));
        e.gc.setLineStyle(SWT.LINE_SOLID);
        if ((fBeginTime >= windowStartTime) && (fBeginTime <= windowEndTime)) {
            int beginX = xAxis.getPixelCoordinate(fBeginTime);
            e.gc.drawLine(beginX, 0, beginX, e.height);
        }

        if ((fEndTime >= windowStartTime) && (fEndTime <= windowEndTime) && (fBeginTime != fEndTime)) {
            int endX = xAxis.getPixelCoordinate(fEndTime);
            e.gc.drawLine(endX, 0, endX, e.height);
        }
        e.gc.setAlpha(150);
        if (Math.abs(fEndTime - fBeginTime) > 1) {
            e.gc.setBackground(TmfXYChartViewer.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
            int beginX = xAxis.getPixelCoordinate(fBeginTime);
            int endX = xAxis.getPixelCoordinate(fEndTime);
            if (fEndTime > fBeginTime) {
                e.gc.fillRectangle(beginX + 1, 0, endX - beginX - 1, e.height);
            } else {
                e.gc.fillRectangle(endX + 1, 0, beginX - endX - 1, e.height);
            }
        }
    }

    @Override
    public boolean drawBehindSeries() {
        return false;
    }
}
