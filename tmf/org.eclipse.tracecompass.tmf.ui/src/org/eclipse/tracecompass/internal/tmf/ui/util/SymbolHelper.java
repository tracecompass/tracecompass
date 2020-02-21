/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * Symbol drawing helper. Can draw common shapes to annotate legends or charts
 *
 * @author Matthew Khouzam
 */
public final class SymbolHelper {

    private SymbolHelper() {
        // do nothing
    }

    /**
     * Draw a square
     *
     * @param gc
     *            the graphics context to draw to
     * @param color
     *            the color of the symbol
     * @param symbolSize
     *            the size of the symbol (radius in pixels)
     * @param centerX
     *            the center point x coordinate
     * @param centerY
     *            the center point y coordinate
     */
    public static void drawSquare(GC gc, Color color, int symbolSize, int centerX, int centerY) {
        Color oldColor = gc.getBackground();
        gc.setBackground(color);
        int[] pts = new int[8];
        pts[0] = centerX - symbolSize;
        pts[1] = centerY - symbolSize;
        pts[2] = centerX + symbolSize;
        pts[3] = centerY - symbolSize;
        pts[4] = centerX + symbolSize;
        pts[5] = centerY + symbolSize;
        pts[6] = centerX - symbolSize;
        pts[7] = centerY + symbolSize;
        gc.fillPolygon(pts);
        gc.setBackground(oldColor);
    }

    /**
     * Draw an isoceles triangle where the summit is facing upwards
     *
     * @param gc
     *            the graphics context to draw to
     * @param color
     *            the color of the symbol
     * @param symbolSize
     *            the size of the symbol (radius in pixels)
     * @param centerX
     *            the center point x coordinate
     * @param centerY
     *            the center point y coordinate
     */
    public static void drawTriangle(GC gc, Color color, int symbolSize, int centerX, int centerY) {
        Color oldColor = gc.getBackground();
        gc.setBackground(color);
        int[] pts = new int[6];
        pts[0] = centerX - symbolSize;
        pts[1] = centerY + symbolSize / 3;
        pts[2] = centerX;
        pts[3] = centerY - symbolSize;
        pts[4] = centerX + symbolSize;
        pts[5] = centerY + symbolSize / 3;
        gc.fillPolygon(pts);
        gc.setBackground(oldColor);
    }

    /**
     * Draw an isoceles triangle where the summit is facing downwards
     *
     * @param gc
     *            the graphics context to draw to
     * @param color
     *            the color of the symbol
     * @param symbolSize
     *            the size of the symbol (radius in pixels)
     * @param centerX
     *            the center point x coordinate
     * @param centerY
     *            the center point y coordinate
     */
    public static void drawInvertedTriangle(GC gc, Color color, int symbolSize, int centerX, int centerY) {
        Color oldColor = gc.getBackground();
        gc.setBackground(color);
        int[] pts = new int[6];
        pts[0] = centerX - symbolSize;
        pts[1] = centerY - symbolSize / 3;
        pts[2] = centerX;
        pts[3] = centerY + symbolSize;
        pts[4] = centerX + symbolSize;
        pts[5] = centerY - symbolSize / 3;
        gc.fillPolygon(pts);
        gc.setBackground(oldColor);
    }

    /**
     * Draw a plus
     *
     * @param gc
     *            the graphics context to draw to
     * @param color
     *            the color of the symbol
     * @param symbolSize
     *            the size of the symbol (radius in pixels)
     * @param centerX
     *            the center point x coordinate
     * @param centerY
     *            the center point y coordinate
     */
    public static void drawPlus(GC gc, Color color, int symbolSize, int centerX, int centerY) {
        int prevLs = gc.getLineStyle();
        int prevLw = gc.getLineWidth();
        Color oldColor = gc.getForeground();
        gc.setForeground(color);
        gc.setLineWidth(Math.max(2, symbolSize / 4));
        gc.drawLine(centerX - symbolSize, centerY, centerX + symbolSize, centerY);
        gc.drawLine(centerX, centerY - symbolSize, centerX, centerY + symbolSize);
        gc.setLineStyle(prevLs);
        gc.setLineWidth(prevLw);
        gc.setBackground(oldColor);
    }

    /**
     * Draw a cross, an X symbol
     *
     * @param gc
     *            the graphics context to draw to
     * @param color
     *            the color of the symbol
     * @param symbolSize
     *            the size of the symbol (radius in pixels)
     * @param centerX
     *            the center point x coordinate
     * @param centerY
     *            the center point y coordinate
     */
    public static void drawCross(GC gc, Color color, int symbolSize, int centerX, int centerY) {
        int prevLs = gc.getLineStyle();
        int prevLw = gc.getLineWidth();
        Color oldColor = gc.getForeground();
        gc.setForeground(color);
        gc.setLineWidth(Math.max(2, symbolSize / 4));
        gc.drawLine(centerX - symbolSize, centerY - symbolSize, centerX + symbolSize, centerY + symbolSize);
        gc.drawLine(centerX - symbolSize, centerY + symbolSize, centerX + symbolSize, centerY - symbolSize);
        gc.setLineStyle(prevLs);
        gc.setLineWidth(prevLw);
        gc.setForeground(oldColor);
    }

    /**
     * Draw a diamond, a square rotated by 45 degrees
     *
     * @param gc
     *            the graphics context to draw to
     * @param color
     *            the color of the symbol
     * @param symbolSize
     *            the size of the symbol (radius in pixels)
     * @param centerX
     *            the center point x coordinate
     * @param centerY
     *            the center point y coordinate
     */
    public static void drawDiamond(GC gc, Color color, int symbolSize, int centerX, int centerY) {
        Color oldColor = gc.getBackground();
        gc.setBackground(color);
        int[] pts = new int[8];
        pts[0] = centerX - symbolSize;
        pts[1] = centerY;
        pts[2] = centerX;
        pts[3] = centerY - symbolSize;
        pts[4] = centerX + symbolSize;
        pts[5] = centerY;
        pts[6] = centerX;
        pts[7] = centerY + symbolSize;
        gc.fillPolygon(pts);
        gc.setBackground(oldColor);
    }

    /**
     * Draw a circle
     *
     * @param gc
     *            the graphics context to draw to
     * @param color
     *            the color of the symbol
     * @param symbolSize
     *            the size of the symbol (radius in pixels)
     * @param centerX
     *            the center point x coordinate
     * @param centerY
     *            the center point y coordinate
     */
    public static void drawCircle(GC gc, Color color, int symbolSize, int centerX, int centerY) {
        Color oldColor = gc.getBackground();
        gc.setBackground(color);
        gc.fillOval(centerX - symbolSize, centerY - symbolSize, symbolSize * 2, symbolSize * 2);
        gc.setBackground(oldColor);
    }
}
