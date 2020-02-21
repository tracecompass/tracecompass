/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.tracecompass.internal.tmf.ui.util.SymbolHelper;
import org.eclipse.tracecompass.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
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
        drawStyleLine(gc, lineColor, imageWidth, imageHeight, appearance);

        drawStyledDot(gc, lineColor, imageWidth, imageHeight, appearance);

        gc.dispose();
        lineColor.dispose();
        return image;
    }

    private static void drawStyleLine(GC gc, Color lineColor, int imageWidth, int imageHeight, IYAppearance appearance) {
        Color prev = gc.getForeground();
        LineStyle lineStyle = LineStyle.valueOf(appearance.getStyle());
        if (lineStyle != LineStyle.NONE) {
            gc.setForeground(lineColor);
            gc.setLineWidth(appearance.getWidth());
            gc.setLineStyle(LineStyle.valueOf(appearance.getStyle()).ordinal());
            gc.drawLine(0, imageHeight / 2, imageWidth, imageHeight / 2);
            gc.setForeground(prev);
        }
    }

    private static void drawStyledDot(GC gc, Color lineColor, int imageWidth, int imageHeight, IYAppearance appearance) {
        String symbolStyle = appearance.getSymbolStyle();
        int symbolSize = appearance.getSymbolSize();
        int centerX = imageWidth / 2;
        int centerY = imageHeight / 2;
        Color prevBg = gc.getBackground();
        Color prevFg = gc.getForeground();
        switch(symbolStyle) {
        case IYAppearance.SymbolStyle.CIRCLE:
            SymbolHelper.drawCircle(gc, lineColor, symbolSize, centerX, centerY);
            break;
        case IYAppearance.SymbolStyle.DIAMOND:
            SymbolHelper.drawDiamond(gc, lineColor, symbolSize, centerX, centerY);
            break;
        case IYAppearance.SymbolStyle.SQUARE:
            SymbolHelper.drawSquare(gc, lineColor, symbolSize, centerX, centerY);
            break;
        case IYAppearance.SymbolStyle.CROSS:
            SymbolHelper.drawCross(gc, lineColor, symbolSize, centerX, centerY);
            break;
        case IYAppearance.SymbolStyle.PLUS:
            SymbolHelper.drawPlus(gc, lineColor, symbolSize, centerX, centerY);
            break;

        case IYAppearance.SymbolStyle.INVERTED_TRIANGLE:
            SymbolHelper.drawInvertedTriangle(gc, lineColor, symbolSize, centerX, centerY);
            break;
        case IYAppearance.SymbolStyle.TRIANGLE:
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
