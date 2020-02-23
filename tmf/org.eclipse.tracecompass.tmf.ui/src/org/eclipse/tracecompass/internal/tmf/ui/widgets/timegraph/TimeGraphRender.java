/*******************************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.widgets.timegraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.colors.RGBAUtil;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;

/**
 * Deferred draw util, can be used to store draws in a list so that they can be
 * displayed at a later time.
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
public final class TimeGraphRender {

    private interface IDrawable {
        /**
         * Draw the element
         *
         * @param provider
         *            the presentation provider for post-drawing
         * @param gc
         *            the Graphics context
         */
        public void draw(@Nullable ITimeGraphPresentationProvider provider, GC gc);
    }

    /**
     * Deferred entry, basically a row to draw
     */
    public static class DeferredEntry implements IDrawable {
        private Rectangle fBounds;
        private ITimeGraphEntry fEntry;
        private final List<DeferredItem> fItems = new ArrayList<>();

        /**
         * Constructor
         *
         * @param entry
         *            the entry to draw
         * @param bounds
         *            the bounds of the entry
         */
        public DeferredEntry(ITimeGraphEntry entry, Rectangle bounds) {
            fEntry = entry;
            fBounds = bounds;
        }

        @Override
        public void draw(@Nullable ITimeGraphPresentationProvider provider, GC gc) {
            for (DeferredItem item : getItems()) {
                item.draw(provider, gc);
            }
            if (provider != null) {
                provider.postDrawEntry(fEntry, fBounds, gc);
            }
        }

        /**
         * gets the items list
         *
         * @return the items
         */
        public List<DeferredItem> getItems() {
            return fItems;
        }
    }

    /**
     * The deferredItem's responsibility is to set and reset the graphics
     * context so that derived classes can draw in it.
     */
    public abstract static class DeferredItem implements IDrawable {
        /**
         * Style with no border
         */
        public static final int NO_BORDER = Integer.MIN_VALUE;
        private final Collection<PostDrawEvent> fPDEs = new ArrayList<>();
        private final Rectangle fBounds;
        private RGBAColor fBgColor;
        private final RGBAColor fBorderColor;
        private final int fLineWidth;

        /**
         * Copy constructor
         *
         * @param other
         *            other {@link DeferredItem} to copy
         */
        public DeferredItem(DeferredItem other) {
            Rectangle bounds = other.fBounds;
            fBounds = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
            fBgColor = other.getBackgroundColor();
            fPDEs.addAll(other.getPostDrawEvents());
            fBorderColor = other.fBorderColor;
            fLineWidth = other.fLineWidth;

        }

        /**
         * Constructor
         *
         * @param bounds
         *            bounding box of item
         * @param backgroundColor
         *            the background color
         * @param borderColor
         *            the border color
         * @param lineWidth
         *            border width, NO_BORDER if it should not be drawn
         */
        public DeferredItem(Rectangle bounds, RGBAColor backgroundColor, RGBAColor borderColor, int lineWidth) {
            fBounds = bounds;
            fLineWidth = lineWidth;
            fBgColor = backgroundColor;
            fBorderColor = borderColor;
        }

        /**
         * Add a post draw event
         *
         * @param pde
         *            post draw event
         */
        public void add(PostDrawEvent pde) {
            fPDEs.add(pde);
        }

        @Override
        public final void draw(@Nullable ITimeGraphPresentationProvider provider, GC gc) {
            Rectangle bounds = getBounds();
            // basic sanity
            if (bounds.width <= 0 || bounds.height <= 0) {
                return;
            }
            int prevAlpha = gc.getAlpha();
            Color prevBgColor = gc.getBackground();
            Color prevFgColor = gc.getForeground();
            int prevLineWidth = gc.getLineWidth();
            setContext(gc);
            innerDraw(gc);
            if (fLineWidth != NO_BORDER) {
                setContext(gc);
                gc.setAlpha(fBorderColor.getAlpha());
                drawBorder(gc);
            }
            setContext(gc);
            drawLabel(gc);
            if (provider != null) {
                gc.setLineWidth(prevLineWidth);
                gc.setBackground(prevBgColor);
                gc.setForeground(prevFgColor);
                gc.setAlpha(prevAlpha);
                postDraw(provider, gc);
            }
            // reset context
            gc.setLineWidth(prevLineWidth);
            gc.setBackground(prevBgColor);
            gc.setForeground(prevFgColor);
            gc.setAlpha(prevAlpha);
        }

        /**
         * This function has the context set for it. The most important being
         * ForegroundColor
         *
         * @param gc
         *            the graphics context
         */
        protected void drawBorder(GC gc) {
            gc.drawRectangle(getBounds());
        }

        /**
         * Draw the label if applicable
         *
         * @param gc
         *            the graphics context
         */
        protected void drawLabel(GC gc) {
            // do nothing
        }

        /**
         * Gets the background color
         *
         * @return the background color
         */
        public RGBAColor getBackgroundColor() {
            return fBgColor;
        }

        /**
         * Gets the border color
         *
         * @return the border color
         */
        public @Nullable RGBAColor getBorderColor() {
            return fBorderColor;
        }

        /**
         * Gets the bounds (bounding box)
         *
         * @return the bounds
         */
        public Rectangle getBounds() {
            return fBounds;
        }

        /**
         * Gets the post draw events
         *
         * @return the post draw events
         */
        private Collection<PostDrawEvent> getPostDrawEvents() {
            return fPDEs;
        }

        /**
         * Draw where the line width, background and foreground color and alpha
         * is already set and reset.
         *
         * @param gc
         *            the Graphics context
         */
        protected abstract void innerDraw(GC gc);

        private final void postDraw(ITimeGraphPresentationProvider provider, GC gc) {
            for (PostDrawEvent pde : fPDEs) {
                pde.draw(provider, gc);
            }
        }

        /**
         * Sets the background color, useful for merging states
         *
         * @param color
         *            the background color
         */
        protected void setBackgroundColor(RGBAColor color) {
            fBgColor = color;
        }

        /**
         * Sets the graphical context.
         * <ul>
         * <li>background color</li>
         * <li>alpha</li>
         * <li>foreground color</li>
         * <li>line width</li>
         * </ul>
         *
         * @param gc
         *            the graphics context
         */
        private void setContext(GC gc) {
            RGBAColor backgroundRgba = getBackgroundColor();
            Color bgColor = getColor(backgroundRgba.toInt());
            gc.setAlpha(backgroundRgba.getAlpha());
            gc.setBackground(bgColor);
            RGBAColor foregroundRgba = getBorderColor();
            if (foregroundRgba == null) {
                foregroundRgba = BLACK;
            }
            Color fgColor = getColor(foregroundRgba.toInt());
            gc.setForeground(fgColor);
            if (fLineWidth >= 0) {
                gc.setLineWidth(fLineWidth);
            }
        }
    }

    /**
     * Deferred poly line drawing
     */
    public static class DeferredLine implements IDrawable {
        private Rectangle fBounds;
        private long fMin;
        private List<List<LongPoint>> fSeriesPoints;
        private RGBAColor fColorRGBA;
        private double fScale;

        /**
         * Constructor
         *
         * @param bounds
         *            bounds of the line
         * @param min
         *            minimum value of the line
         * @param seriesPoints
         *            list of series (list of long points)
         * @param colorRGBA
         *            the color of the line
         * @param scale
         *            the scale of the line
         */
        public DeferredLine(Rectangle bounds, long min, List<List<LongPoint>> seriesPoints, RGBAColor colorRGBA, double scale) {
            fBounds = bounds;
            fMin = min;
            fSeriesPoints = seriesPoints;
            fColorRGBA = colorRGBA;
            fScale = scale;
        }

        @Override
        public void draw(@Nullable ITimeGraphPresentationProvider provider, GC gc) {
            RGBAColor rgba = fColorRGBA;
            int colorInt = rgba.toInt();
            Color color = getColor(colorInt);
            for (int i = 0; i < this.fSeriesPoints.size(); i++) {
                Color prev = gc.getForeground();
                int prevAlpha = gc.getAlpha();
                gc.setAlpha(rgba.getAlpha());
                gc.setForeground(color);
                List<LongPoint> series = fSeriesPoints.get(i);
                int[] points = new int[series.size() * 2];
                for (int point = 0; point < series.size(); point++) {
                    LongPoint longPoint = series.get(point);
                    points[point * 2] = longPoint.x;
                    points[point * 2 + 1] = fBounds.height - (int) ((longPoint.y - fMin) * fScale) + fBounds.y;
                }
                gc.drawPolyline(points);
                gc.setForeground(prev);
                gc.setAlpha(prevAlpha);
            }
        }
    }

    /**
     * Deferred Transparent State
     */
    public static class DeferredTransparentState extends DeferredItem {

        /**
         * Constructor
         *
         * @param bounds
         *            the bounds
         * @param bgColor
         *            the background color
         */
        public DeferredTransparentState(Rectangle bounds, RGBAColor bgColor) {
            super(bounds, bgColor, BLACK, DeferredItem.NO_BORDER);
        }

        @Override
        protected void innerDraw(GC gc) {
            Rectangle drawRect = getBounds();
            if (drawRect.width >= 2) {
                gc.fillRectangle(drawRect);
                if (drawRect.width > 2) {
                    // Draw the top and bottom borders
                    RGBAColor foregroundRGB = BLACK;
                    gc.setAlpha(foregroundRGB.getAlpha());
                    gc.setForeground(getColor(foregroundRGB.toInt()));
                    gc.drawLine(drawRect.x, drawRect.y, drawRect.x + drawRect.width - 1, drawRect.y);
                    gc.drawLine(drawRect.x, drawRect.y + drawRect.height - 1, drawRect.x + drawRect.width - 1, drawRect.y + drawRect.height - 1);
                }
            } else {
                gc.setForeground(gc.getBackground());
                gc.drawLine(drawRect.x, drawRect.y, drawRect.x, drawRect.y + drawRect.height - 1);
            }

        }
    }

    /**
     * Deferred segment, can be a collection of points or a single point. Starts
     * as one point then grows on the X axis
     */
    public static class DeferredSegment implements IDrawable {
        private final int fX;
        private final int fY;
        private int fLength;

        /**
         * Constructor
         *
         * @param x
         *            x coordinate
         * @param y
         *            y coordinate
         */
        public DeferredSegment(int x, int y) {
            fX = x;
            fY = y;
            fLength = 1;
        }

        /**
         * Does the segment contain another point?
         *
         * @param x
         *            the x coordinate
         * @param y
         *            the Y coordinate
         * @return true if the segment crosses that point, false otherwise
         */
        public boolean contains(int x, int y) {
            return (y == fY) && x >= fX && x <= fX + fLength;
        }

        @Override
        public void draw(@Nullable ITimeGraphPresentationProvider provider, GC gc) {
            if (fLength == 1) {
                gc.drawPoint(fX, fY);
            } else {
                gc.drawLine(fX, fY, fX + fLength - 1, fY);
            }
        }

        /**
         * Extend the point if it's at the end
         *
         * @param x
         *            the x coordinate to extend to
         */
        public void extend(int x) {
            if (x == fX + fLength) {
                fLength++;
            }
        }
    }

    /**
     * Deferred State class, the bigger states that can have labels and corners.
     * (in the world of graphics, these are LOD1 and tiny are LOD0)
     */
    public static class DeferredState extends DeferredItem {
        private final int fArc;
        private final @Nullable String fLabel;

        /**
         * Constructor
         *
         * @param bounds
         *            the bounds of the item
         * @param bgColor
         *            the background color
         * @param borderColor
         *            the border color
         * @param arc
         *            the radius of the arc
         * @param lineWidth
         *            the border width
         * @param label
         *            the label to display, can be {@code null}
         */
        public DeferredState(Rectangle bounds, RGBAColor bgColor, RGBAColor borderColor, int arc, int lineWidth, @Nullable String label) {
            super(bounds, bgColor, borderColor, lineWidth);
            fArc = arc;
            fLabel = label;
        }

        @Override
        protected void drawBorder(GC gc) {
            Rectangle bounds = getBounds();
            gc.drawRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height, fArc, fArc);
        }

        @Override
        protected void drawLabel(GC gc) {
            Rectangle bounds = getBounds();
            RGBAColor backgroundColor = getBackgroundColor();
            if (fLabel != null && !fLabel.isEmpty() && bounds.width > bounds.height) {
                gc.setForeground(Utils.getDistinctColor(RGBAUtil.fromRGBAColor(backgroundColor).rgb));
                Utils.drawText(gc, fLabel, bounds.x, bounds.y, bounds.width, bounds.height, true, true);
            }
        }

        @Override
        protected void innerDraw(GC gc) {
            Rectangle bounds = getBounds();
            gc.fillRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height, fArc, fArc);
        }
    }

    /**
     * States that are smaller than a pixel
     */
    public static class DeferredTinyState extends DeferredItem {

        /**
         * Blend 2 colors
         *
         * @param alpha
         *            the ratio
         * @param c0
         *            color channel 1, the base
         * @param c1
         *            color channel 2, the top layer
         * @return the blended color
         */
        private static int alphaBlend(int alpha, short c0, short c1) {
            return (int) (c0 * ((255 - alpha) / 255.0) + c1 * ((alpha) / 255.0));
        }

        /**
         * Can the items be merged? (note, right needs to have x >= left's x)
         *
         * @param left
         *            the left element
         * @param right
         *            the right element
         * @return true if the items can be merged
         */
        public static boolean areMergeable(DeferredItem left, DeferredItem right) {
            Rectangle rightBounds = right.getBounds();
            Rectangle largerBounds = new Rectangle(rightBounds.x - 1, rightBounds.y, rightBounds.width + 1, rightBounds.height);
            return Objects.equals(left.getBackgroundColor(), right.getBackgroundColor())
                    && Objects.equals(left.getBorderColor(), right.getBorderColor())
                    && largerBounds.intersects(left.getBounds())
                    && largerBounds.height == right.getBounds().height;
        }

        /**
         * Copy constructor
         *
         * @param other
         *            the other state
         */
        public DeferredTinyState(DeferredTinyState other) {
            super(other);
        }

        /**
         * Constructor
         *
         * @param bounds
         *            the bounds of the state
         * @param backgroundColor
         *            the background color
         * @param borderColor
         *            the border color
         * @param lineWidth
         *            the line width, could be NO_BORDER
         */
        public DeferredTinyState(Rectangle bounds, RGBAColor backgroundColor, RGBAColor borderColor, int lineWidth) {
            super(bounds, backgroundColor, borderColor, lineWidth);
        }

        /**
         * Extend a tiny state if another is overlapping
         *
         * @param other
         *            the other state to overlap
         * @return if the size was grown.
         */
        public boolean extend(DeferredTinyState other) {
            Rectangle bounds = getBounds();
            if (areMergeable(this, other)) {
                bounds.add(other.getBounds());
                return true;
            }
            return false;
        }

        @Override
        protected void innerDraw(GC gc) {
            gc.fillRectangle(getBounds());
        }

        /**
         * Squash two pixels, assume the height is the largest.
         *
         * @param other
         *            the state to squash.
         * @return true if the state is squashed
         */
        public boolean squash(DeferredTinyState other) {
            Rectangle bounds = getBounds();
            Rectangle otherBounds = other.getBounds();
            if (bounds.x != otherBounds.x || bounds.width != otherBounds.width) {
                return false;
            }
            // if the color or the height change here, areMergeable will no
            // longer hold true.
            RGBAColor prevColor = getBackgroundColor();
            RGBAColor newColor = other.getBackgroundColor();
            bounds.y = Math.min(bounds.y, otherBounds.y);
            bounds.height = Math.max(bounds.height, otherBounds.height);
            int alpha = newColor.getAlpha() / 2;
            setBackgroundColor(new RGBAColor(alphaBlend(alpha, prevColor.getRed(), newColor.getRed()),
                    alphaBlend(alpha, prevColor.getGreen(), newColor.getGreen()),
                    alphaBlend(alpha, prevColor.getBlue(), newColor.getBlue()),
                    alphaBlend(alpha, prevColor.getAlpha(), newColor.getAlpha())));
            return true;
        }
    }

    /**
     * Long point, a point with a Y value that's long rather than an int.
     */
    public static class LongPoint {
        final int x;
        final long y;

        /**
         * Constructor
         *
         * @param x
         *            x value
         * @param y
         *            y value
         */
        public LongPoint(int x, long y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof LongPoint) {
                LongPoint longPoint = (LongPoint) obj;
                return longPoint.x == x && longPoint.y == y;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    /**
     * Post draw event
     */
    public static class PostDrawEvent implements IDrawable {
        private Rectangle fBounds;
        private ITimeEvent fEvent;

        /**
         * Constructor
         *
         * @param event
         *            the event
         * @param bounds
         *            the bounds of the event
         */
        public PostDrawEvent(ITimeEvent event, Rectangle bounds) {
            fEvent = event;
            fBounds = bounds;
        }

        @Override
        public void draw(@Nullable ITimeGraphPresentationProvider provider, GC gc) {
            if (provider != null) {
                provider.postDrawEvent(fEvent, fBounds, gc);
            }
        }

    }

    private static final RGBAColor BLACK = new RGBAColor(0, 0, 0, 255);
    private static final ColorRegistry COLOR_REGISTRY = new ColorRegistry();

    /**
     * Get the color for a given color integer
     *
     * @param colorInt
     *            the color integer (0xRRGGBBAA)
     * @return the {@link Color}
     * @since 6.0
     */
    public static Color getColor(int colorInt) {
        String hexRGB = Integer.toHexString(colorInt);
        Color color = COLOR_REGISTRY.get(hexRGB);
        if (color == null) {
            COLOR_REGISTRY.put(hexRGB, RGBAUtil.fromInt(colorInt).rgb);
            color = COLOR_REGISTRY.get(hexRGB);
        }
        return color;
    }
}
