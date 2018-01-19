/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.util;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Cohen Sutherland line clipping algorithm, developed in 1967, public domain,
 * clean room implemented.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Cohen%E2%80%93Sutherland_algorithm">Cohen
 *      Sutherland algorithm (Wikipedia)</a>
 *
 * @author Matthew Khouzam
 *
 */
public final class LineClipper {

    /**
     * Quadrant bitmasks for Cohen Sutherland
     */
    private static final int DOWN = 1 << 3;
    private static final int UP = 1 << 2;
    private static final int RIGHT = 1 << 1;
    private static final int LEFT = 1 << 0;
    private static final int INSIDE = 0;

    private LineClipper() {
        // do nothing
    }

    /**
     * Computer graphics algorithm used for line clipping, workaround for Cairo bug
     *
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=470115
     *
     * https://bugs.freedesktop.org/show_bug.cgi?id=103840
     *
     * @param bounds
     *            the clipping rectangle
     * @param x0
     *            point 0 x
     * @param y0
     *            point 0 y
     * @param x1
     *            point 1 x
     * @param y1
     *            point 1 y
     * @return the clipped rectangle of the arrow
     */
    public static Rectangle clip(Rectangle bounds, int x0, int y0, int x1, int y1) {
        int maxLoops = 3;
        int quadrant0 = computeQuadrant(x0, y0, bounds);
        int quadrant1 = computeQuadrant(x1, y1, bounds);
        if (quadrant0 == 0 && quadrant1 == 0) {
            return new Rectangle(x0, y0, x1 - x0, y1 - y0);
        }
        if (guaranteedOut(quadrant0, quadrant1) || bounds.width == 0 || bounds.height == 0) {
            return null;
        }
        long deltaY = (long) y1 - (long) y0;
        long deltaX = (long) x1 - (long) x0;
        double slope = deltaY / (double) deltaX;
        double antiSlope = deltaX / (double) deltaY;
        int newX0 = x0;
        int newY0 = y0;
        int newX1 = x1;
        int newY1 = y1;
        // log n iterations
        for (int i = 0; i < maxLoops && quadrant0 != 0; i++) {
            if (guaranteedOut(quadrant0, quadrant1)) {
                return null;
            }
            if ((quadrant0 & UP) != 0) {
                newX0 = (int) (newX0 + antiSlope * (bounds.y + bounds.height - newY0));
                newY0 = bounds.height + bounds.y;
            } else if ((quadrant0 & DOWN) != 0) {
                newX0 = (int) (newX0 + antiSlope * (bounds.y - newY0));
                newY0 = bounds.y;
            } else if ((quadrant0 & RIGHT) != 0) {
                newY0 = (int) (newY0 + slope * (bounds.x + bounds.width - newX0));
                newX0 = bounds.x + bounds.width;
            } else {
                newY0 = (int) (newY0 + slope * (bounds.x - newX0));
                newX0 = bounds.x;
            }
            quadrant0 = computeQuadrant(newX0, newY0, bounds);
        }
        // ditto
        for (int i = 0; i < maxLoops && quadrant1 != 0; i++) {
            if (guaranteedOut(quadrant0, quadrant1)) {
                return null;
            }
            if ((quadrant1 & UP) != 0) {
                newX1 = (int) (newX1 + antiSlope * (bounds.y + bounds.height - newY1));
                newY1 = bounds.height + bounds.y;
            } else if ((quadrant1 & DOWN) != 0) {
                newX1 = (int) (newX1 + antiSlope * (bounds.y - newY1));
                newY1 = bounds.y;
            } else if ((quadrant1 & RIGHT) != 0) {
                newY1 = (int) (newY1 + slope * (bounds.x + bounds.width - newX1));
                newX1 = bounds.x + bounds.width;
            } else {
                newY1 = (int) (newY1 + slope * (bounds.x - newX1));
                newX1 = bounds.x;
            }
            quadrant1 = computeQuadrant(newX1, newY1, bounds);
        }
        return (guaranteedOut(quadrant0, quadrant1)) ? null : new Rectangle(newX0, newY0, newX1 - newX0, newY1 - newY0);
    }

    private static boolean guaranteedOut(int quadrant0, int quadrant1) {
        return (quadrant0 & quadrant1) != 0;
    }

    /**
     * Cohen Sutherland compute algorithm
     * <table>
     * <tr>
     * <th>0101</th>
     * <th>0100</th>
     * <th>0110</th>
     * </tr>
     * <tr>
     * <th>0001</th>
     * <th>0000</th>
     * <th>0010</th>
     * </tr>
     * <tr>
     * <th>1001</th>
     * <th>1000</th>
     * <th>1010</th>
     * </tr>
     * </table>
     *
     * @param x
     *            point x
     * @param y
     *            point y
     * @param bounds
     *            bounds rectangle
     * @return a quadrant
     */
    private static int computeQuadrant(int x, int y, Rectangle bounds) {
        int retCode = INSIDE;
        if (x < bounds.x) {
            retCode |= LEFT;
        } else if (x > (bounds.x + bounds.width)) {
            retCode |= RIGHT;
        }
        if (y < bounds.y) {
            retCode |= DOWN;
        } else if (y > (bounds.y + bounds.height)) {
            retCode |= UP;
        }
        return retCode;
    }

}
