/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Changed to updated histogram data model
 *   Patrick Tasse - Update for mouse wheel zoom
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * A histogram widget that displays the event distribution of a whole trace.
 * <p>
 * It also features a selected range window that can be dragged and zoomed.
 *
 * @version 1.1
 * @author Francois Chouinard
 */
public class FullTraceHistogram extends Histogram {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final HistogramZoom fZoom;

    private long fRangeStartTime = 0L;
    private long fRangeDuration;

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * Full Constructor
     *
     * @param view A reference to the parent histogram view
     * @param parent A reference to the parent composite
     */
    public FullTraceHistogram(HistogramView view, Composite parent) {
        super(view, parent);
        fZoom = new HistogramZoom(this, getStartTime(), getTimeLimit());
        addMouseWheelListener(fZoom);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void clear() {
        fRangeStartTime = 0L;
        fRangeDuration = 0L;
        if (fZoom != null) {
            fZoom.setFullRange(0L, 0L);
            fZoom.setNewRange(0L, 0L);
        }
        super.clear();
    }

    /**
     * Sets the time range of the full histogram.
     *
     * @param startTime A start time
     * @param endTime A end time
     */
    public void setFullRange(long startTime, long endTime) {
        fZoom.setFullRange(startTime, endTime);
    }

    /**
     * Sets the selected time range.
     *
     * @param startTime The histogram start time
     * @param duration The histogram duration
     */
    public void setTimeRange(long startTime, long duration) {
        fRangeStartTime = startTime;
        fRangeDuration = duration;
        fZoom.setNewRange(fRangeStartTime, fRangeDuration);
        fDataModel.complete();
    }

    // ------------------------------------------------------------------------
    // MouseListener
    // ------------------------------------------------------------------------

    private int fStartPosition;
    private boolean fMouseMoved;

    @Override
    public void mouseDown(MouseEvent event) {
        if (fDragState == DRAG_NONE && fDataModel.getNbEvents() != 0) {
            if (event.button == 2 || (event.button == 1 && (event.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL)) {
                fDragState = DRAG_RANGE;
                fDragButton = event.button;
                fStartPosition = event.x;
                fMouseMoved = false;
                return;
            } else if (event.button == 3) {
                fDragState = DRAG_ZOOM;
                fDragButton = event.button;
                long time = Math.min(getTimestamp(event.x), getEndTime());
                if ((event.stateMask & SWT.MODIFIER_MASK) == SWT.SHIFT) {
                    if (time < fRangeStartTime + fRangeDuration / 2) {
                        fRangeStartTime = fRangeStartTime + fRangeDuration;
                    }
                } else {
                    fRangeStartTime = time;
                }
                fRangeDuration = time - fRangeStartTime;
                fCanvas.redraw();
                return;
            }
        }
        super.mouseDown(event);
    }

    @Override
    public void mouseUp(MouseEvent event) {
        if (fDragState == DRAG_RANGE && event.button == fDragButton) {
            fDragState = DRAG_NONE;
            fDragButton = 0;
            if (!fMouseMoved) {
                // if single click without move, center on the click
                long startTime = getTimestamp(event.x) - fRangeDuration / 2;
                fRangeStartTime = Math.max(getStartTime(), Math.min(getEndTime() - fRangeDuration, startTime));
            }
            ((HistogramView) fParentView).updateTimeRange(fRangeStartTime, fRangeStartTime + fRangeDuration);
            return;
        } else if (fDragState == DRAG_ZOOM && event.button == fDragButton) {
            fDragState = DRAG_NONE;
            fDragButton = 0;
            if (fRangeDuration < 0) {
                fRangeStartTime = fRangeStartTime + fRangeDuration;
                fRangeDuration = -fRangeDuration;
            }
            if (fRangeDuration > 0) {
                ((HistogramView) fParentView).updateTimeRange(fRangeStartTime, fRangeStartTime + fRangeDuration);
            } else {
                fRangeStartTime = fZoom.getStartTime();
                fRangeDuration = fZoom.getDuration();
                fCanvas.redraw();
            }
            return;
        }
        super.mouseUp(event);
    }

    // ------------------------------------------------------------------------
    // MouseMoveListener
    // ------------------------------------------------------------------------

    @Override
    public void mouseMove(MouseEvent event) {
        if (fDragState == DRAG_RANGE) {
            int nbBuckets = event.x - fStartPosition;
            long delta = nbBuckets * fScaledData.fBucketDuration;
            long newStart = fZoom.getStartTime() + delta;
            if (newStart < getStartTime()) {
                newStart = getStartTime();
            }
            long newEnd = newStart + fZoom.getDuration();
            if (newEnd > getEndTime()) {
                newEnd = getEndTime();
                newStart = newEnd - fZoom.getDuration();
            }
            fRangeStartTime = newStart;
            fCanvas.redraw();
            fMouseMoved = true;
            return;
        } else if (fDragState == DRAG_ZOOM) {
            long endTime = Math.max(getStartTime(), Math.min(getEndTime(), getTimestamp(event.x)));
            fRangeDuration = endTime - fRangeStartTime;
            fCanvas.redraw();
            return;
        }
        super.mouseMove(event);
    }

    // ------------------------------------------------------------------------
    // PaintListener
    // ------------------------------------------------------------------------

    @Override
    public void paintControl(PaintEvent event) {
        super.paintControl(event);

        Image image = (Image) fCanvas.getData(IMAGE_KEY);
        assert image != null;

        Image rangeRectangleImage = new Image(image.getDevice(), image, SWT.IMAGE_COPY);
        GC rangeWindowGC = new GC(rangeRectangleImage);

        if ((fScaledData != null) && (fRangeDuration != 0 || fDragState == DRAG_ZOOM)) {
            drawTimeRangeWindow(rangeWindowGC, fRangeStartTime, fRangeDuration);
        }

        // Draws the buffer image onto the canvas.
        event.gc.drawImage(rangeRectangleImage, 0, 0);

        rangeWindowGC.dispose();
        rangeRectangleImage.dispose();
    }

    /**
     * Get the histogram zoom
     * @return the histogram zoom
     * @since 2.0
     */
    public HistogramZoom getZoom() {
        return fZoom;
    }
}
