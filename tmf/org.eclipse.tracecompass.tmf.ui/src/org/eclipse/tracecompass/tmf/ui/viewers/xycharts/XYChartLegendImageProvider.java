/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xycharts;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.viewers.ILegendImageProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.swtchart.LineStyle;

/**
 * Provides a legend image from an XY chart viewer. With a name, desired height
 * and width, an image is created.
 *
 * @author Yonni Chen
 * @since 3.2
 */
public class XYChartLegendImageProvider implements ILegendImageProvider {

    private static final int OVAL_LEGEND_SIZE = 3;
    private static final @NonNull String DOT_SHAPE_OVAL = "oval"; //$NON-NLS-1$
    private final TmfCommonXAxisChartViewer fChartViewer;

    /**
     * Constructor
     *
     * @param chartViewer
     *            XY Chart with which we create a legend image
     */
    public XYChartLegendImageProvider(TmfCommonXAxisChartViewer chartViewer) {
        fChartViewer = chartViewer;
    }

    @Override
    public Image getLegendImage(int imageHeight, int imageWidth, @NonNull String name) {
        /*
         * If series exists in chart, then image legend match that series. Image will
         * make sense if series exists in chart. If it does not exists, an image will
         * still be created.
         */
        IYAppearance appearance = fChartViewer.getSeriesAppearance(name);
        RGBAColor rgb = appearance.getColor();
        Color lineColor = new Color(Display.getDefault(), rgb.getRed(), rgb.getGreen(), rgb.getBlue());
        Color background = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

        PaletteData palette = new PaletteData(background.getRGB(), lineColor.getRGB());
        ImageData imageData = new ImageData(imageWidth, imageHeight, 8, palette);
        imageData.transparentPixel = 0;
        Image image = new Image(Display.getDefault(), imageData);
        GC gc = new GC(image);

        gc.setBackground(background);
        gc.fillRectangle(0, 0, imageWidth, imageHeight);
        gc.setForeground(lineColor);
        gc.setLineWidth(appearance.getWidth());
        LineStyle lineStyle = LineStyle.valueOf(appearance.getStyle());
        if (lineStyle != LineStyle.NONE) {
            gc.setLineStyle(LineStyle.valueOf(appearance.getStyle()).ordinal());
            gc.drawLine(0, imageHeight / 2, imageWidth, imageHeight / 2);
        } else {
            // Not a line, draw a dot
            // FIXME: support more shapes and add a getter to the IYAppearance class
            drawStyledDot(gc, lineColor, imageWidth, imageHeight, DOT_SHAPE_OVAL);
        }

        gc.dispose();
        lineColor.dispose();
        return image;
    }

    private static void drawStyledDot(GC gc, Color lineColor, int imageWidth, int imageHeight, String shape) {
        gc.setBackground(lineColor);
        switch(shape) {
        case DOT_SHAPE_OVAL:
        default:
            // Default is an oval
            gc.fillOval(imageWidth / 2 - OVAL_LEGEND_SIZE, imageHeight / 2 - OVAL_LEGEND_SIZE, OVAL_LEGEND_SIZE * 2, OVAL_LEGEND_SIZE * 2);
            break;
        }

    }
}
