/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityViewer;
import org.swtchart.IAxis;
import org.swtchart.ICustomPaintListener;
import org.swtchart.IPlotArea;
import org.swtchart.Range;

/**
 * Class for providing zooming based on mouse drag with right mouse button.
 * It also notifies the viewer about a change of range.
 *
 * @author Bernd Hufmann
 * @author Marc-Andre Laperle
 */
@Deprecated
public class MouseDragZoomProvider extends BaseMouseProvider implements MouseListener, MouseMoveListener, ICustomPaintListener {

    /** Cached start coordinate */
    private double fStartCoordinate;
    /** Cached end coordinate */
    private double fEndCoordinate;
    /** Flag indicating that an update is ongoing */
    private boolean fIsUpdate;

    /**
     * Default constructor
     *
     * @param densityViewer
     *            the density viewer reference.
     */
    public MouseDragZoomProvider(AbstractSegmentStoreDensityViewer densityViewer) {
        super(densityViewer);
        register();
    }

    @Override
    public final void register() {
        getChart().getPlotArea().addMouseListener(this);
        getChart().getPlotArea().addMouseMoveListener(this);
        ((IPlotArea) getChart().getPlotArea()).addCustomPaintListener(this);
    }

    @Override
    public final void deregister() {
        if (!getChart().isDisposed()) {
            getChart().getPlotArea().removeMouseListener(this);
            getChart().getPlotArea().removeMouseMoveListener(this);
            ((IPlotArea) getChart().getPlotArea()).removeCustomPaintListener(this);
        }
    }

    @Override
    public void mouseDoubleClick(@Nullable MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseDown(@Nullable MouseEvent e) {
        if (e != null && e.button == 3) {
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);
            fStartCoordinate = xAxis.getDataCoordinate(e.x);
            fEndCoordinate = fStartCoordinate;
            fIsUpdate = true;
        }
    }

    @Override
    public void mouseUp(@Nullable MouseEvent e) {
        if ((fIsUpdate) && (fStartCoordinate != fEndCoordinate)) {
            if (fStartCoordinate > fEndCoordinate) {
                double tmp = fStartCoordinate;
                fStartCoordinate = fEndCoordinate;
                fEndCoordinate = tmp;
            }
            getDensityViewer().zoom(new Range(fStartCoordinate, fEndCoordinate));
        }

        if (fIsUpdate) {
            getChart().redraw();
        }
        fIsUpdate = false;
    }

    @Override
    public void mouseMove(@Nullable MouseEvent e) {
        if (e != null && fIsUpdate) {
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);
            fEndCoordinate = xAxis.getDataCoordinate(e.x);
            getChart().redraw();
        }
    }

    @Override
    public void paintControl(@Nullable PaintEvent e) {
        if (e != null && fIsUpdate && (fStartCoordinate != fEndCoordinate)) {
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);
            int startX = xAxis.getPixelCoordinate(fStartCoordinate);
            int endX = xAxis.getPixelCoordinate(fEndCoordinate);

            e.gc.setBackground(getChart().getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
            if (fStartCoordinate < fEndCoordinate) {
                e.gc.fillRectangle(startX, 0, endX - startX, e.height);
            } else {
                e.gc.fillRectangle(endX, 0, startX - endX, e.height);
            }
            e.gc.drawLine(startX, 0, startX, e.height);
            e.gc.drawLine(endX, 0, endX, e.height);
        }
    }

    @Override
    public boolean drawBehindSeries() {
        return true;
    }
}