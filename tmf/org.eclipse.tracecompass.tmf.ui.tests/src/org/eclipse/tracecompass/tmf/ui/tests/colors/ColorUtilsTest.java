/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.colors;

import static org.junit.Assert.assertEquals;

import org.eclipse.tracecompass.tmf.ui.colors.ColorUtils;
import org.junit.Test;

/**
 * Test the {@link ColorUtils} class
 *
 * @author Geneviève Bastien
 */
public class ColorUtilsTest {

    /**
     * Test the {@link ColorUtils#toHexColor(int, int, int)} method
     */
    @Test
    public void testToHexStringRGB() {

        int r = 0x12;
        int g = 0x34;
        int b = 0x56;
        assertEquals("#123456", ColorUtils.toHexColor(r, g, b));

        // Add 256 to each value, it should still be the same string
        assertEquals("#123456", ColorUtils.toHexColor(r + 256, g + 256, b + 256));

        // Use negative values
        assertEquals("#123456", ColorUtils.toHexColor(-r, -g, -b));

        // 0 and ff
        assertEquals("#000000", ColorUtils.toHexColor(0, 0, 0));
        assertEquals("#ffffff", ColorUtils.toHexColor(255, 255, 255));

    }

}
