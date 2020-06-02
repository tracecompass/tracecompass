/**********************************************************************
 * Copyright (c) 2015, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density2;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.ICustomPaintListener;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density2.AbstractSegmentStoreDensityView;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density2.AbstractSegmentStoreDensityViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.AxisRange;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.IAxis;

/**
 * Class for providing selection with the left mouse button. It also notifies
 * the viewer about a change of selection.
 *
 * @author Bernd Hufmann
 * @author Marc-Andre Laperle
 */
public class MouseSelectionProvider extends BaseMouseProvider implements MouseListener, MouseMoveListener, ICustomPaintListener {

    /** Cached start coordinate */
    private double fStartCoordinate;
    /** Cached end coordinate */
    private double fEndCoordinate;
    /** Flag indicating that an update is ongoing */
    private boolean fIsUpdate;
    /** Flag indicating that the begin marker is dragged */
    private boolean fDragBeginMarker;

    /**
     * Constructor for a mouse selection provider.
     *
     * @param densityViewer
     *            The parent density viewer
     */
    public MouseSelectionProvider(AbstractSegmentStoreDensityViewer densityViewer) {
        super(densityViewer);
    }

    @Override
    public void mouseDoubleClick(@Nullable MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseDown(@Nullable MouseEvent e) {
        if (e != null && (e.button == 1)) {
            fDragBeginMarker = false;
            if (((e.stateMask & SWT.SHIFT) != SWT.SHIFT) || (fEndCoordinate == fStartCoordinate)) {
                IAxis xAxis = getXAxis();
                fStartCoordinate = xAxis.getDataCoordinate(e.x);
                fEndCoordinate = fStartCoordinate;
            } else {
                double selectionBegin = fStartCoordinate;
                double selectionEnd = fEndCoordinate;
                IAxis xAxis = getXAxis();
                double time = xAxis.getDataCoordinate(e.x);
                if (Math.abs(time - selectionBegin) < Math.abs(time - selectionEnd)) {
                    fDragBeginMarker = true;
                    fStartCoordinate = time;
                    fEndCoordinate = selectionEnd;
                } else {
                    fStartCoordinate = selectionBegin;
                    fEndCoordinate = time;
                }
            }
            fIsUpdate = true;
        }
    }

    @Override
    public void mouseUp(@Nullable MouseEvent e) {
        if ((fIsUpdate)) {
            if (fStartCoordinate > fEndCoordinate) {
                double tmp = fStartCoordinate;
                fStartCoordinate = fEndCoordinate;
                fEndCoordinate = tmp;
            }
            if (!isEmptySelection()) {
                getDensityViewer().select(new AxisRange(AbstractSegmentStoreDensityView.DEFAULT_RANGE.getFirst(), AbstractSegmentStoreDensityView.DEFAULT_RANGE.getSecond()));
            } else {
                getDensityViewer().select(new AxisRange(fStartCoordinate, fEndCoordinate));
            }
            fIsUpdate = false;
            redraw();
        }
    }

    @Override
    public void mouseMove(@Nullable MouseEvent e) {
        if (e != null && fIsUpdate) {
            IAxis xAxis = getXAxis();
            if (fDragBeginMarker) {
                fStartCoordinate = xAxis.getDataCoordinate(e.x);
            } else {
                fEndCoordinate = xAxis.getDataCoordinate(e.x);
            }
            redraw();
        }
    }

    private boolean isEmptySelection() {
        IAxis xAxis = getXAxis();
        int begin = xAxis.getPixelCoordinate(fStartCoordinate);
        int end = xAxis.getPixelCoordinate(fEndCoordinate);

        return Math.abs(end - begin) > 2;
    }

    @Override
    public void paintControl(@Nullable PaintEvent e) {
        if (e == null || !isEmptySelection()) {
            return;
        }

        Display display = getControl().getDisplay();

        IAxis xAxis = getXAxis();
        e.gc.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
        e.gc.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
        e.gc.setLineStyle(SWT.LINE_SOLID);
        int begin = xAxis.getPixelCoordinate(fStartCoordinate);
        e.gc.drawLine(begin, 0, begin, e.height);

        int end = xAxis.getPixelCoordinate(fEndCoordinate);
        e.gc.drawLine(end, 0, end, e.height);

        e.gc.setAlpha(64);
        if (Math.abs(fEndCoordinate - fStartCoordinate) > 1) {
            e.gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
            int beginX = xAxis.getPixelCoordinate(fStartCoordinate);
            int endX = xAxis.getPixelCoordinate(fEndCoordinate);
            if (fEndCoordinate > fStartCoordinate) {
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
