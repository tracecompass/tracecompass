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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * A control that shows marker labels on a time axis.
 *
 * @since 2.0
 */
public class TimeGraphMarkerAxis extends TimeGraphBaseControl {

    private static final Image COLLAPSED = Activator.getDefault().getImageFromPath("icons/ovr16/collapsed_ovr.gif"); //$NON-NLS-1$
    private static final Image EXPANDED = Activator.getDefault().getImageFromPath("icons/ovr16/expanded_ovr.gif"); //$NON-NLS-1$
    private static final Image HIDE = Activator.getDefault().getImageFromPath("icons/etool16/hide.gif"); //$NON-NLS-1$
    private static final int HIDE_BORDER = 4; // transparent border of the hide icon

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
    private final Set<IMarkerAxisListener> fListeners = new LinkedHashSet<>();
    private Multimap<String, IMarkerEvent> fMarkers = LinkedHashMultimap.create();
    private @NonNull List<String> fCategories = Collections.EMPTY_LIST;
    private boolean fCollapsed = false;

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
                Point size = getSize();
                Rectangle bounds = new Rectangle(0, 0, size.x, size.y);
                TimeGraphMarkerAxis.this.mouseDown(e, bounds, fTimeProvider.getNameSpace());
            }
        });
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        int height = 0;
        if (!fMarkers.isEmpty() && fTimeProvider.getTime0() != fTimeProvider.getTime1()) {
            if (fCollapsed) {
                height = COLLAPSED.getBounds().height;
            } else {
                height = TOP_MARGIN + fMarkers.keySet().size() * HEIGHT;
            }
        }
        return super.computeSize(wHint, height, changed);
    }

    /**
     * Add a marker axis listener.
     *
     * @param listener
     *            the listener
     */
    public void addMarkerAxisListener(IMarkerAxisListener listener) {
        fListeners.add(listener);
    }

    /**
     * Remove a marker axis listener.
     *
     * @param listener
     *            the listener
     */
    public void removeMarkerAxisListener(IMarkerAxisListener listener) {
        fListeners.remove(listener);
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
     * Set the list of marker categories.
     *
     * @param categories
     *            The list of marker categories, or null
     */
    public void setMarkerCategories(List<String> categories) {
        if (categories == null) {
            fCategories = Collections.EMPTY_LIST;
        } else {
            fCategories = categories;
        }
    }

    /**
     * Handle a mouseDown event.
     *
     * @param e
     *            the mouse event
     * @param bounds
     *            the bounds of the marker axis in the mouse event's coordinates
     * @param nameSpace
     *            the width of the marker name area
     */
    public void mouseDown(MouseEvent e, Rectangle bounds, int nameSpace) {
        if (bounds.isEmpty()) {
            return;
        }
        if (fCollapsed || (e.x < bounds.x + Math.min(nameSpace, EXPANDED.getBounds().width))) {
            fCollapsed = !fCollapsed;
            getParent().layout();
            redraw();
            return;
        }
        if (e.x < bounds.x + nameSpace) {
            String category = getHiddenCategoryForEvent(e, bounds);
            if (category != null) {
                for (IMarkerAxisListener listener : fListeners) {
                    listener.setMarkerCategoryVisible(category, false);
                }
            }
            return;
        }
        IMarkerEvent marker = getMarkerForEvent(e);
        if (marker != null) {
            fTimeProvider.setSelectionRangeNotify(marker.getTime(), marker.getTime() + marker.getDuration(), false);
        }
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
        Display.getDefault().asyncExec(() -> {
            if (isDisposed()) {
                return;
            }
            fMarkers = map;
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
        if (bounds.isEmpty()) {
            return;
        }
        // draw background
        gc.fillRectangle(bounds);

        if (!fCollapsed) {
            Rectangle rect = new Rectangle(bounds.x, bounds.y + TOP_MARGIN, bounds.width, HEIGHT);
            for (String category : getVisibleCategories()) {
                rect.x = bounds.x;
                rect.width = nameSpace;
                drawMarkerCategory(category, rect, gc);
                rect.x = nameSpace;
                rect.width = bounds.width - nameSpace;
                drawMarkerLabels(category, rect, gc);
                rect.y += HEIGHT;
            }
        }

        Rectangle rect = new Rectangle(bounds.x, bounds.y, nameSpace, bounds.height);
        gc.setClipping(rect);
        drawToolbar(rect, nameSpace, gc);
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
        int x = rect.x + EXPANDED.getBounds().width + HIDE.getBounds().width;
        gc.drawText(category, Math.max(x, rect.x + rect.width - width), rect.y, true);
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
            gc.setBackground(color);
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
                gc.drawRectangle(x1, rect.y, width, rect.height - 1);
                if (x2 > x1 + width) {
                    int y = rect.y + rect.height / 2;
                    gc.drawLine(x1 + width, y, x2, y);
                }
                gc.setForeground(getDistinctForeground(color.getRGB()));
                Utils.drawText(gc, label, x1 + TEXT_MARGIN, rect.y, true);
            } else {
                int y = rect.y + rect.height / 2;
                gc.drawLine(x1, y, x2, y);
            }
        }
    }

    private static Color getDistinctForeground(RGB rgb) {
        /* Calculate the relative luminance of the color, high value is bright */
        final int luminanceThreshold = 128;
        /* Relative luminance (Y) coefficients as defined in ITU.R Rec. 709 */
        final double redCoefficient = 0.2126;
        final double greenCoefficient = 0.7152;
        final double blueCoefficient = 0.0722;
        int luminance = (int) (redCoefficient * rgb.red + greenCoefficient * rgb.green + blueCoefficient * rgb.blue);
        /* Use black over bright colors and white over dark colors */
        return Display.getDefault().getSystemColor(
                luminance > luminanceThreshold ? SWT.COLOR_BLACK : SWT.COLOR_WHITE);
    }

    /**
     * Draw the toolbar
     *
     * @param bounds
     *            the bounds of the marker axis
     * @param nameSpace
     *            the width of the marker name area
     * @param gc
     *            the GC instance
     */
    protected void drawToolbar(Rectangle bounds, int nameSpace, GC gc) {
        if (bounds.isEmpty()) {
            return;
        }
        if (fCollapsed) {
            gc.drawImage(COLLAPSED, bounds.x, bounds.y);
        } else {
            gc.drawImage(EXPANDED, bounds.x, bounds.y);
            int x = bounds.x + EXPANDED.getBounds().width;
            for (int i = 0; i < fMarkers.keySet().size(); i++) {
                int y = bounds.y + TOP_MARGIN + i * HEIGHT;
                gc.drawImage(HIDE, x, y);
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
        String category = getVisibleCategories().get(categoryIndex);

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

    private String getHiddenCategoryForEvent(MouseEvent e, Rectangle bounds) {
        List<String> categories = getVisibleCategories();
        Rectangle rect = HIDE.getBounds();
        rect.x += bounds.x + EXPANDED.getBounds().width + HIDE_BORDER;
        rect.y += bounds.y + TOP_MARGIN + HIDE_BORDER;
        rect.width -= 2 * HIDE_BORDER;
        rect.height -= 2 * HIDE_BORDER;
        for (int i = 0; i < categories.size(); i++) {
            if (rect.contains(e.x, e.y)) {
                return categories.get(i);
            }
            rect.y += HEIGHT;
        }
        return null;
    }

    private List<String> getVisibleCategories() {
        List<String> categories = new ArrayList<>(fCategories);
        categories.retainAll(fMarkers.keySet());
        return categories;
    }
}
