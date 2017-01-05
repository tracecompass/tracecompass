/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.chart.core.tests.chart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartType;
import org.junit.Test;

/**
 * Test the {@link ChartModel} class
 *
 * @author Geneviève Bastien
 */
public class ChartModelTest {

    /**
     * Test constructor of the class
     */
    @Test
    public void testConstructor() {
        // Normal title and x and y log scales
        String title = "title";
        ChartModel cm = new ChartModel(ChartType.BAR_CHART, title, true, true);
        assertEquals(ChartType.BAR_CHART, cm.getChartType());
        assertEquals(title, cm.getTitle());
        assertTrue(cm.isXLogscale());
        assertTrue(cm.isYLogscale());

        // Title with accented characters, y log scale, but not x log scale
        title = "éèëâ\nbla";
        cm = new ChartModel(ChartType.SCATTER_CHART, title, false, true);
        assertEquals(ChartType.SCATTER_CHART, cm.getChartType());
        assertEquals(title, cm.getTitle());
        assertFalse(cm.isXLogscale());
        assertTrue(cm.isYLogscale());

        // Title with special characters, no log scale
        title = "&?%$/\"()";
        cm = new ChartModel(ChartType.PIE_CHART, title, false, false);
        assertEquals(ChartType.PIE_CHART, cm.getChartType());
        assertEquals(title, cm.getTitle());
        assertFalse(cm.isXLogscale());
        assertFalse(cm.isYLogscale());
    }
}
