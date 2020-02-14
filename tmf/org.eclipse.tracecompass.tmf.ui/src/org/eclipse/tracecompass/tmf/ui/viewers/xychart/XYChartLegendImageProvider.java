/*******************************************************************************
 * Copyright (c) 2017, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xychart;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.timegraph.BaseXYPresentationProvider;
import org.eclipse.tracecompass.internal.tmf.ui.util.SymbolHelper;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.viewers.ILegendImageProvider2;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfCommonXAxisChartViewer;

/**
 * Provides a legend image from an XY chart viewer. With a name, desired height
 * and width, an image is created.
 *
 * @author Yonni Chen
 * @since 6.0
 */
public class XYChartLegendImageProvider implements ILegendImageProvider2 {

    private static final @NonNull RGBAColor DEFAULT_COLOR = new RGBAColor(255, 255, 255);

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
    public Image getLegendImage(int imageHeight, int imageWidth, @NonNull Long id) {
        /*
         * If series exists in chart, then image legend match that series. Image will
         * make sense if series exists in chart. If it does not exists, an image will
         * still be created.
         */
        OutputElementStyle appearance = fChartViewer.getSeriesStyle(id);
        BaseXYPresentationProvider presProvider = fChartViewer.getPresentationProvider();
        RGBAColor rgb = presProvider.getColorStyleOrDefault(appearance, StyleProperties.COLOR, DEFAULT_COLOR);


        Color lineColor = new Color(Display.getDefault(), rgb.getRed(), rgb.getGreen(), rgb.getBlue());
        Color background = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

        PaletteData palette = new PaletteData(background.getRGB(), lineColor.getRGB());
        ImageData imageData = new ImageData(imageWidth, imageHeight, 8, palette);
        imageData.transparentPixel = 0;
        Image image = new Image(Display.getDefault(), imageData);
        GC gc = new GC(image);

        gc.setBackground(background);
        gc.fillRectangle(0, 0, imageWidth, imageHeight);
        drawStyleLine(gc, lineColor, imageWidth, imageHeight, appearance);

        drawStyledDot(gc, lineColor, imageWidth, imageHeight, appearance);

        gc.dispose();
        lineColor.dispose();
        return image;
    }

    private void drawStyleLine(GC gc, Color lineColor, int imageWidth, int imageHeight, @NonNull OutputElementStyle appearance) {
        Color prev = gc.getForeground();
        BaseXYPresentationProvider presProvider = fChartViewer.getPresentationProvider();
        LineStyle lineStyle = LineStyle.valueOf((String) presProvider.getStyleOrDefault(appearance, StyleProperties.SERIES_STYLE, StyleProperties.SeriesStyle.SOLID));
        if (lineStyle != LineStyle.NONE) {
            gc.setForeground(lineColor);
            gc.setLineWidth(((Number) presProvider.getFloatStyleOrDefault(appearance, StyleProperties.WIDTH, 1.0f)).intValue());
            gc.setLineStyle(lineStyle.ordinal());
            gc.drawLine(0, imageHeight / 2, imageWidth, imageHeight / 2);
            gc.setForeground(prev);
        }
    }

    private void drawStyledDot(GC gc, Color lineColor, int imageWidth, int imageHeight, @NonNull OutputElementStyle appearance) {
        BaseXYPresentationProvider presProvider = fChartViewer.getPresentationProvider();
        String symbolStyle = (String) presProvider.getStyleOrDefault(appearance, StyleProperties.SYMBOL_TYPE, StyleProperties.SymbolType.NONE);
        int symbolSize = ((Number) presProvider.getFloatStyleOrDefault(appearance, StyleProperties.HEIGHT, 1.0f)).intValue();
        int centerX = imageWidth / 2;
        int centerY = imageHeight / 2;
        Color prevBg = gc.getBackground();
        Color prevFg = gc.getForeground();
        switch(symbolStyle) {
        case StyleProperties.SymbolType.CIRCLE:
            SymbolHelper.drawCircle(gc, lineColor, symbolSize, centerX, centerY);
            break;
        case StyleProperties.SymbolType.DIAMOND:
            SymbolHelper.drawDiamond(gc, lineColor, symbolSize, centerX, centerY);
            break;
        case StyleProperties.SymbolType.SQUARE:
            SymbolHelper.drawSquare(gc, lineColor, symbolSize, centerX, centerY);
            break;
        case StyleProperties.SymbolType.CROSS:
            SymbolHelper.drawCross(gc, lineColor, symbolSize, centerX, centerY);
            break;
        case StyleProperties.SymbolType.PLUS:
            SymbolHelper.drawPlus(gc, lineColor, symbolSize, centerX, centerY);
            break;

        case StyleProperties.SymbolType.INVERTED_TRIANGLE:
            SymbolHelper.drawInvertedTriangle(gc, lineColor, symbolSize, centerX, centerY);
            break;
        case StyleProperties.SymbolType.TRIANGLE:
            SymbolHelper.drawTriangle(gc, lineColor, symbolSize, centerX, centerY);
            break;

        default:
            // Default is nothing
            break;
        }
        gc.setForeground(prevFg);
        gc.setBackground(prevBg);
    }

}
