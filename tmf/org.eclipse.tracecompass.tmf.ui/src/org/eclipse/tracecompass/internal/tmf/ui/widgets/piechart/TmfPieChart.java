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

package org.eclipse.tracecompass.internal.tmf.ui.widgets.piechart;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.IPlotArea;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.piecharts.Messages;
import org.eclipse.tracecompass.tmf.core.presentation.IPaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.QualitativePaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;

/**
 * Pie Chart.
 *
 * Note, improvements should be contributed into org.eclipse.swtchart's pie
 * chart
 *
 * @author Matthew Khouzam
 */
public class TmfPieChart extends Chart {

    private final class PiePaintListener implements PaintListener {

        private void drawPieChart(PaintEvent e, Rectangle bounds) {

            int nbSeries = fSlices.size();
            /*
             * first element is always zero, so we store the end angles also all
             * slices are contiguous.
             */
            fEndAngles = new int[nbSeries - 1];
            double selectAngle1 = Double.NaN;
            double selectAngle2 = Double.NaN;
            double sumTotal = getTotal();

            GC gc = e.gc;
            Color prevFg = gc.getForeground();
            Color prevBg = gc.getBackground();
            gc.setForeground(BLACK);
            gc.setLineWidth(1);
            gc.setAntialias(SWT.ON);

            int pieX = bounds.x + (bounds.width - fWidth) / 2;
            int pieY = bounds.y + (bounds.height - fWidth) / 2;
            fCenter = new Point(pieX + fWidth / 2, pieY + fWidth / 2);
            if (sumTotal == 0) {
                gc.drawOval(pieX, pieY, fWidth, fWidth);
            } else {
                double factor = 100 / sumTotal;
                int arcAngle = 0;
                /*
                 * 90 degrees is 12 o'clock
                 */
                final int startAngle = 90;
                /*
                 * -270 degrees since it is 360 degrees from 12 o'clock
                 */
                final int remainingAngle = -270;
                int currentAngle = startAngle;
                for (int i = 0; i < nbSeries; i++) {
                    PieSlice pieSlice = fSlices.get(i);
                    // Stored angles increase in clockwise direction from 0
                    // degrees at 12:00
                    if (i > 0) {
                        fEndAngles[i - 1] = startAngle - currentAngle;
                    }
                    IBarSeries<?> series = (IBarSeries<?>) getSeriesSet().getSeries(pieSlice.getID());
                    Color bgColor = series.getBarColor();

                    gc.setBackground(bgColor);

                    if (i == (nbSeries - 1)) {
                        arcAngle = currentAngle - remainingAngle;
                    } else {
                        double angle = pieSlice.getValue() * factor * 3.6;
                        arcAngle = (int) Math.round(angle);
                    }
                    if (Objects.equals(pieSlice.getID(), fSelectedId)) {
                        gc.setLineWidth(3);
                        gc.setForeground(BLACK);
                        selectAngle1 = currentAngle;
                        selectAngle2 = currentAngle - arcAngle;
                    } else {
                        gc.setForeground(WHITE);
                        /*
                         * Wider line + alpha looks like what anti-aliasing
                         * should do
                         */
                        gc.setLineWidth(2);
                        int prevAlpha = gc.getAlpha();
                        gc.setAlpha(80);
                        drawRadius(gc, fCenter, fWidth * 0.5, currentAngle);
                        drawRadius(gc, fCenter, fWidth * 0.5, currentAngle - arcAngle);
                        gc.setAlpha(prevAlpha);
                    }
                    gc.fillArc(pieX, pieY, fWidth, fWidth, currentAngle, -arcAngle);
                    currentAngle -= arcAngle;

                    gc.setLineWidth(1);
                }
                gc.setForeground(BLACK);
            }
            if (!Double.isNaN(selectAngle1) && !Double.isNaN(selectAngle2)) {
                gc.setLineWidth(2);
                drawRadius(gc, fCenter, fWidth * 0.5, selectAngle1);
                drawRadius(gc, fCenter, fWidth * 0.5, selectAngle2);
                gc.drawArc(pieX, pieY, fWidth, fWidth, (int) selectAngle1, (int) (selectAngle2 - selectAngle1));
                gc.setLineWidth(1);
            }
            gc.setForeground(prevFg);
            gc.setBackground(prevBg);
        }

        private void drawRadius(GC gc, Point center, double radius, double angle) {
            double radians = Math.toRadians(-angle);
            gc.drawLine(center.x, center.y, (int) Math.round(center.x + Math.cos(radians) * radius), (int) Math.round(center.y + Math.sin(radians) * radius));
        }

        @Override
        public void paintControl(PaintEvent e) {
            GC gc = e.gc;
            IPlotArea canvas = getPlotArea();
            Chart chart = canvas.getChart();
            Rectangle bounds = canvas.getControl().getBounds();

            fCenter = new Point(bounds.x + bounds.width / 2, bounds.height / 2 + bounds.y);
            fEndAngles = new int[fSlices.size()];
            if (fSlices.isEmpty()) {
                bounds = gc.getClipping();
                Font oldFont = gc.getFont();
                Font font = new Font(Display.getDefault(), FONT, 15, SWT.BOLD);
                gc.setForeground(chart.getForeground());
                gc.setFont(font);
                gc.setFont(font);
                String text = org.eclipse.tracecompass.internal.tmf.ui.Messages.TmfChartViewer_NoData;
                Point textSize = e.gc.textExtent(text);
                gc.drawText(text, (bounds.width - textSize.x) / 2, (bounds.height - textSize.y) / 2);
                gc.setFont(oldFont);
                font.dispose();
                return;
            }

            int width = bounds.width;
            int x = bounds.x;

            if (chart.getLegend().isVisible()) {
                Rectangle legendBounds = ((Control) chart.getLegend()).getBounds();
                chart.getLegend().setBackground(chart.getBackground());
                chart.getLegend().setForeground(chart.getForeground());
                Font oldFont = gc.getFont();
                Font font = new Font(Display.getDefault(), FONT, 10, SWT.BOLD);
                gc.setForeground(chart.getForeground());
                gc.setFont(font);
                String text = chart.getAxisSet().getXAxis(0).getTitle().getText();
                Point textSize = e.gc.textExtent(text);
                gc.drawText(text, legendBounds.x + (legendBounds.width - textSize.x) / 2, legendBounds.y - textSize.y);
                gc.setFont(oldFont);
                font.dispose();
            }

            fWidth = Math.min(width - X_GAP, bounds.height);
            drawPieChart(e, new Rectangle(x, bounds.y, width, bounds.height));

        }
    }

    private static final Color BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    private static final Color WHITE = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

    private static final String FONT = "Arial"; //$NON-NLS-1$
    // Current colors should be odd in order to alternate in the palette
    private static final int NUM_COLORS = 23;

    private static final IPaletteProvider PALETTE = new QualitativePaletteProvider.Builder().setNbColors(NUM_COLORS).build();
    private static final ColorRegistry REGISTRY = new ColorRegistry();

    private static final int X_GAP = 10;
    private Point fCenter = new Point(0, 0);
    private int fCurrentColor = 0;
    private String fSelectedId;
    private int[] fEndAngles;
    private List<PieSlice> fSlices = new ArrayList<>();
    private int fWidth;

    /**
     * Constructor.
     *
     * @param parent
     *            the parent composite on which chart is placed
     * @param style
     *            the style of widget to construct
     */
    public TmfPieChart(Composite parent, int style) {
        super(parent, style);
        fSlices.clear();
        // Hide all original axes and plot area
        for (IAxis axis : getAxisSet().getAxes()) {
            axis.getTitle().setVisible(false);
            axis.getTick().setVisible(false);
        }
        IPlotArea plotArea = getPlotArea();
        Chart chart = plotArea.getChart();
        ((Composite) plotArea).setVisible(false);

        chart.addPaintListener(new PiePaintListener());
        REGISTRY.put(Messages.TmfStatisticsView_PieChartOthersSliceName, new RGB(128, 128, 128));
    }

    /**
     * Add a slice to the model
     *
     * @param label
     *            Label
     * @param value
     *            Value (numerical)
     * @param id
     *            the unique ID
     */
    public void addPieSlice(String label, double value, String id) {
        PieSlice pieSlice = new PieSlice(label, value, id);
        IBarSeries<?> bs = (IBarSeries<?>) getSeriesSet().createSeries(SeriesType.BAR, id);
        Color sliceColor = REGISTRY.get(id);
        if (sliceColor == null) {
            fCurrentColor += 3;
            RGBAColor rgba = PALETTE.get().get(fCurrentColor % NUM_COLORS);
            REGISTRY.put(id, new RGB(rgba.getRed(), rgba.getGreen(), rgba.getBlue()));
            sliceColor = REGISTRY.get(id);
        }
        bs.setBarColor(sliceColor);
        fSlices.add(pieSlice);
    }

    /**
     * Clear the model
     */
    public void clear() {
        for (PieSlice slice : fSlices) {
            getSeriesSet().deleteSeries(slice.getID());
        }
        fSlices.clear();
    }

    /**
     * Get the pie slice, make a radial transform from cartesian XY
     *
     * @param x
     *            X coordinate in pixels
     * @param y
     *            Y coordinate in pixels
     * @return the equivalent pie slice
     */
    public PieSlice getSliceFromPosition(int x, int y) {
        // Only continue if the point is inside the pie circle
        double rad = Math.sqrt(Math.pow(fCenter.x - x, 2) + Math.pow(fCenter.y - y, 2));
        if (2 * rad > fWidth) {
            return null;
        }
        // Angle is relative to 12:00 position, increases clockwise
        double angle = Math.acos((fCenter.y - y) / rad) / Math.PI * 180.0;
        if (x - fCenter.x < 0) {
            angle = 360 - angle;
        }
        if (fEndAngles.length == 0 || angle < fEndAngles[0]) {
            return fSlices.get(0);
        }
        for (int s = 0; s < fEndAngles.length - 1; s++) {
            if (fEndAngles[s] <= angle && angle < fEndAngles[s + 1]) {
                return fSlices.get(s + 1);
            }
        }
        return fSlices.get(fSlices.size() - 1);
    }

    /**
     * Get the total amount in the model
     *
     * @return the total
     */
    public double getTotal() {
        return fSlices.stream().collect(Collectors.summingDouble(PieSlice::getValue));
    }

    /**
     * Select an ID
     *
     * @param id
     *            the ID to select
     */
    public void select(String id) {
        fSelectedId = id;
    }

    /**
     * Set the color of a slice
     *
     * @param sliceId
     *            the slice ID
     * @param color
     *            the color
     */
    public void addColor(String sliceId, @NonNull RGB color) {
        REGISTRY.put(sliceId, color);
    }
}
