/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * A control that shows marker labels on a time axis.
 *
 * @since 2.0
 */
public class TimeGraphMarkerAxis extends TimeGraphBaseControl {

    private static final int HEIGHT;
    static {
        GC gc = new GC(Display.getDefault());
        HEIGHT = gc.getFontMetrics().getHeight() + 1;
        gc.dispose();
    }

    private static final int TOP_MARGIN = 1;
    private static final int MAX_LABEL_LENGTH = 256;
    private static final int TEXT_MARGIN = 2;
    private static final int MAX_GAP = 5;
    private static final int X_LIMIT = Integer.MAX_VALUE / 256;

    private @NonNull ITimeDataProvider fTimeProvider;
    private Multimap<String, IMarkerEvent> fMarkers = LinkedHashMultimap.create();
    private List<String> fCategories = Collections.EMPTY_LIST;

    /**
     * Contructor
     *
     * @param parent
     *            The parent composite object
     * @param colorScheme
     *            The color scheme to use
     * @param timeProvider
     *            The time data provider
     */
    public TimeGraphMarkerAxis(Composite parent, @NonNull TimeGraphColorScheme colorScheme, @NonNull ITimeDataProvider timeProvider) {
        super(parent, colorScheme, SWT.NO_BACKGROUND | SWT.NO_FOCUS | SWT.DOUBLE_BUFFERED);
        fTimeProvider = timeProvider;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                IMarkerEvent marker = getMarkerForEvent(e);
                if (marker != null) {
                    fTimeProvider.setSelectionRangeNotify(marker.getTime(), marker.getTime() + marker.getDuration(), false);
                }
            }
        });
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        int height = 0;
        if (!fMarkers.isEmpty() && fTimeProvider.getTime0() != fTimeProvider.getTime1()) {
            height = TOP_MARGIN + fMarkers.keySet().size() * HEIGHT;
        }
        return super.computeSize(wHint, height, changed);
    }

    /**
     * Set the time provider
     *
     * @param timeProvider
     *            The provider to use
     */
    public void setTimeProvider(@NonNull ITimeDataProvider timeProvider) {
        fTimeProvider = timeProvider;
    }

    /**
     * Set the markers list.
     *
     * @param markers
     *            The markers list
     */
    public void setMarkers(List<IMarkerEvent> markers) {
        Multimap<String, IMarkerEvent> map = LinkedHashMultimap.create();
        for (IMarkerEvent marker : markers) {
            map.put(marker.getCategory(), marker);
        }
        List<String> categories = Lists.newArrayList(map.keySet());
        Collections.sort(categories);
        Display.getDefault().asyncExec(() -> {
            fMarkers = map;
            fCategories = categories;
            getParent().layout();
            redraw();
        });
    }

    @Override
    void paint(Rectangle bounds, PaintEvent e) {
        drawMarkerAxis(bounds, fTimeProvider.getNameSpace(), e.gc);
    }

    /**
     * Draw the marker axis
     *
     * @param bounds
     *            the bounds of the marker axis
     * @param nameSpace
     *            the width of the marker name area
     * @param gc
     *            the GC instance
     */
    protected void drawMarkerAxis(Rectangle bounds, int nameSpace, GC gc) {
        // draw background
        gc.fillRectangle(bounds);

        Rectangle rect = new Rectangle(bounds.x, bounds.y + TOP_MARGIN, bounds.width, HEIGHT);
        for (String category : fCategories) {
            rect.x = bounds.x;
            rect.width = nameSpace;
            drawMarkerCategory(category, rect, gc);
            rect.x = nameSpace;
            rect.width = bounds.width - nameSpace;
            drawMarkerLabels(category, rect, gc);
            rect.y += HEIGHT;
        }
    }

    /**
     * Draw the marker category
     *
     * @param category
     *            the category
     * @param rect
     *            the bounds of the marker name area
     * @param gc
     *            the GC instance
     */
    protected void drawMarkerCategory(String category, Rectangle rect, GC gc) {
        if (rect.isEmpty()) {
            return;
        }
        // draw marker category
        gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
        gc.setClipping(rect);
        int width = gc.textExtent(category).x + TEXT_MARGIN;
        gc.drawText(category, Math.max(rect.x, rect.x + rect.width - width), rect.y, true);
    }

    /**
     * Draw the marker labels for the specified category
     *
     * @param category
     *            the category
     * @param rect
     *            the bounds of the marker time area
     * @param gc
     *            the GC instance
     */
    protected void drawMarkerLabels(String category, Rectangle rect, GC gc) {
        if (rect.isEmpty()) {
            return;
        }
        long time0 = fTimeProvider.getTime0();
        long time1 = fTimeProvider.getTime1();
        if (time0 == time1) {
            return;
        }
        int timeSpace = fTimeProvider.getTimeSpace();
        double pixelsPerNanoSec = (timeSpace <= RIGHT_MARGIN) ? 0 :
            (double) (timeSpace - RIGHT_MARGIN) / (time1 - time0);

        gc.setClipping(rect);
        for (IMarkerEvent markerEvent : fMarkers.get(category)) {
            Color color = getColorScheme().getColor(markerEvent.getColor());
            gc.setForeground(color);
            int x1 = getXForTime(rect, time0, pixelsPerNanoSec, markerEvent.getTime());
            if (x1 > rect.x + rect.width) {
                return;
            }
            if (markerEvent.getEntry() != null) {
                continue;
            }
            int x2 = getXForTime(rect, time0, pixelsPerNanoSec, markerEvent.getTime() + markerEvent.getDuration()) - 1;
            String label = getTrimmedLabel(markerEvent);
            if (label != null) {
                int width = gc.textExtent(label).x + TEXT_MARGIN;
                if (x1 < rect.x && x1 + width < x2) {
                    int gap = Math.min(rect.x - x1, MAX_GAP);
                    x1 = Math.min(rect.x + gap, x2 - width);
                    if (x1 > rect.x) {
                        int y = rect.y + rect.height / 2;
                        gc.drawLine(rect.x, y, x1, y);
                    }
                }
                gc.fillRectangle(x1, rect.y, width, rect.height - 1);
                Utils.drawText(gc, label, x1 + TEXT_MARGIN, rect.y, true);
                gc.drawRectangle(x1, rect.y, width, rect.height - 1);
                if (x2 > x1 + width) {
                    int y = rect.y + rect.height / 2;
                    gc.drawLine(x1 + width, y, x2, y);
                }
            } else {
                int y = rect.y + rect.height / 2;
                gc.drawLine(x1, y, x2, y);
            }
        }
    }

    private static String getTrimmedLabel(IMarkerEvent marker) {
        String label = marker.getLabel();
        if (label == null) {
            return null;
        }
        return label.substring(0, Math.min(label.indexOf(SWT.LF) != -1 ? label.indexOf(SWT.LF) : label.length(), MAX_LABEL_LENGTH));
    }

    private static int getXForTime(Rectangle rect, long time0, double pixelsPerNanoSec, long time) {
        int x = rect.x + (int) (Math.floor((time - time0) * pixelsPerNanoSec));
        return Math.min(Math.max(x, -X_LIMIT), X_LIMIT);
    }

    private IMarkerEvent getMarkerForEvent(MouseEvent event) {
        long time0 = fTimeProvider.getTime0();
        long time1 = fTimeProvider.getTime1();
        if (time0 == time1) {
            return null;
        }
        int timeSpace = fTimeProvider.getTimeSpace();
        double pixelsPerNanoSec = (timeSpace <= RIGHT_MARGIN) ? 0 :
            (double) (timeSpace - RIGHT_MARGIN) / (time1 - time0);

        int categoryIndex = Math.max((event.y - TOP_MARGIN) / HEIGHT, 0);
        String category = fCategories.get(categoryIndex);

        IMarkerEvent marker = null;
        GC gc = new GC(Display.getDefault());
        Rectangle rect = getBounds();
        rect.x += fTimeProvider.getNameSpace();
        rect.width -= fTimeProvider.getNameSpace();

        for (IMarkerEvent markerEvent : fMarkers.get(category)) {
            String label = getTrimmedLabel(markerEvent);
            if (markerEvent.getEntry() == null) {
                int x1 = getXForTime(rect, time0, pixelsPerNanoSec, markerEvent.getTime());
                if (x1 <= event.x) {
                    if (label != null) {
                        int width = gc.textExtent(label).x + TEXT_MARGIN;
                        if (event.x <= x1 + width) {
                            marker = markerEvent;
                            continue;
                        }
                    }
                    int x2 = getXForTime(rect, time0, pixelsPerNanoSec, markerEvent.getTime() + markerEvent.getDuration()) - 1;
                    if (event.x <= x2) {
                        marker = markerEvent;
                    }
                } else {
                    break;
                }
            }
        }
        gc.dispose();
        return marker;
    }
}
