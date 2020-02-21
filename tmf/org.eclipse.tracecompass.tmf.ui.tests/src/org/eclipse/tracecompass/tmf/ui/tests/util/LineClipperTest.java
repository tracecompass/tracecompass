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

package org.eclipse.tracecompass.tmf.ui.tests.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.tracecompass.internal.tmf.ui.util.LineClipper;
import org.junit.Test;

/**
 * Boundary value tests for Cohen Sutherland Line Clipping algorithm
 *
 * @author Matthew Khouzam
 *
 */
public class LineClipperTest {

    /**
     * Doesn't intersect
     */
    @Test
    public void glancingTest() {
        Rectangle bounds = new Rectangle(0, 0, 638, 1119);

        int x0 = 1115;
        int y0 = -468;
        int x1 = 1132;
        int y1 = 42;
        Rectangle rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertNull(rect);
    }

    /**
     * Doesn't intersect
     */
    @Test
    public void ousideSquareTest() {
        Rectangle bounds = new Rectangle(0, 0, 100, 100);

        final double SIZE = 100;
        final double ROOT_2 = 1.415;

        int x0 = (int) -Math.ceil(SIZE * ROOT_2);
        int y0 = (int) (SIZE / 2);
        int x1 = (int) (SIZE / 2);
        int y1 = (int) -Math.ceil(SIZE * ROOT_2);
        Rectangle rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertNull(rect);

        x0 = (int) -Math.ceil(SIZE * ROOT_2);
        y0 = (int) (3 * SIZE / 2);
        x1 = (int) (SIZE / 2);
        y1 = (int) Math.ceil(SIZE * ROOT_2);
        rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertNull(rect);

        x0 = (int) Math.ceil(SIZE * ROOT_2);
        y0 = (int) (SIZE / 2);
        x1 = (int) (3 * SIZE / 2);
        y1 = (int) -Math.ceil(SIZE * ROOT_2);
        rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertNull(rect);

        x0 = (int) Math.ceil(SIZE * ROOT_2);
        y0 = (int) (3 * SIZE / 2);
        x1 = (int) (3 * SIZE / 2);
        y1 = (int) Math.ceil(SIZE * ROOT_2);
        rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertNull(rect);
    }

    /**
     * Doesn't intersect
     */
    @Test
    public void tangentTest() {
        Rectangle bounds = new Rectangle(0, 0, 1000, 1000);

        int x0 = -1;
        int y0 = -1;
        int x1 = 1000;
        int y1 = -1;
        Rectangle rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertNull(rect);
    }

    /**
     * touches the line
     */
    @Test
    public void tangentInsideTest() {
        Rectangle bounds = new Rectangle(0, 0, 1000, 1000);

        int x0 = -1;
        int y0 = 0;
        int x1 = 1000;
        int y1 = 0;
        Rectangle rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertEquals(new Rectangle(0, 0, 1000, 0), rect);
    }

    /**
     * touches the corner
     */
    @Test
    public void tangentInsideCornerTest() {
        Rectangle bounds = new Rectangle(0, 0, 1000, 1000);

        int x0 = -1;
        int y0 = -1;
        int x1 = 1000;
        int y1 = 0;
        Rectangle rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertEquals(new Rectangle(1000, 0, 0, 0), rect);
    }

    /**
     * clip top
     */
    @Test
    public void clipTopTest() {
        Rectangle bounds = new Rectangle(0, 0, 1000, 1000);

        int x0 = 100;
        int y0 = 100;
        int x1 = 100;
        int y1 = 1000;
        Rectangle rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertEquals(new Rectangle(100, 100, 0, 900), rect);
    }

    /**
     * clip left
     */
    @Test
    public void clipLeftTest() {
        Rectangle bounds = new Rectangle(0, 0, 1000, 1000);

        int x0 = -100;
        int y0 = 100;
        int x1 = 100;
        int y1 = 100;
        Rectangle rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertEquals(new Rectangle(0, 100, 100, 0), rect);
    }

    /**
     * clip bottom
     */
    @Test
    public void clipBottomTest() {
        Rectangle bounds = new Rectangle(0, 0, 1000, 1000);

        int x0 = 100;
        int y0 = -100;
        int x1 = 100;
        int y1 = 1000;
        Rectangle rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertEquals(new Rectangle(100, 0, 0, 1000), rect);
    }

    /**
     * clip right hand side
     */
    @Test
    public void clipRightTest() {
        Rectangle bounds = new Rectangle(0, 0, 1000, 1000);

        int x0 = 100;
        int y0 = 100;
        int x1 = 1100;
        int y1 = 100;
        Rectangle rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertEquals(new Rectangle(100, 100, 900, 0), rect);
    }

    /**
     * clip an X to a smaller X
     */
    @Test
    public void clipXTest() {
        Rectangle bounds = new Rectangle(0, 0, 1000, 1000);

        int x0 = -100;
        int y0 = -100;
        int x1 = 1100;
        int y1 = 1100;
        Rectangle rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertEquals(new Rectangle(0, 0, 1000, 1000), rect);

        x0 = 1100;
        y0 = -100;
        x1 = -100;
        y1 = 1100;
        rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertEquals(new Rectangle(1000, 0, -1000, 1000), rect);
    }

    /**
     * Everything inside
     */
    @Test
    public void insideTest() {
        Rectangle bounds = new Rectangle(0, 0, 1000, 1000);

        int x0 = 0;
        int y0 = 0;
        int x1 = 1000;
        int y1 = 1000;
        Rectangle rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertEquals(new Rectangle(0, 0, 1000, 1000), rect);
    }

    /**
     * Test a line with slope = 0
     */
    @Test
    public void verticalTest() {
        Rectangle bounds = new Rectangle(0, 0, 1000, 1000);

        int x0 = 100;
        int y0 = -10000;
        int x1 = 100;
        int y1 = 10000;
        Rectangle rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertEquals(new Rectangle(100, 0, 0, 1000), rect);
    }

    /**
     * Test a line with slope == inf
     */
    @Test
    public void horizontalTest() {
        Rectangle bounds = new Rectangle(0, 0, 1000, 1000);

        int x0 = 10000;
        int y0 = 100;
        int x1 = -100;
        int y1 = 100;
        Rectangle rect = LineClipper.clip(bounds, x0, y0, x1, y1);
        assertEquals(new Rectangle(1000, 100, -1000, 0), rect);
    }
}
