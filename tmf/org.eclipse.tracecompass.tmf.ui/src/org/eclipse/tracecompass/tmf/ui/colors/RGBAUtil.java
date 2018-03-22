/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.colors;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;

/**
 * RGBA Utility to create them from ints
 *
 * @author Matthew Khouzam
 * @since 3.4
 */
public final class RGBAUtil {

    private RGBAUtil() {
        // Do nothing
    }

    /**
     * Convert to an RGBA from an integer
     *
     * @param value
     *            the integer encoded RGBA
     * @return the RGBA
     */
    public static RGBA fromInt(int value) {
        return new RGBA((value >> 24) & 0xff, (value >> 16) & 0xff, (value >>> 8) & 0xff, value & 0xff);
    }

    /**
     * Convert to an integer from a RGBA
     *
     * @param value
     *            the RGBA
     * @return the integer encoded RGBA
     */
    public static int fromRGBA(RGBA value) {
        RGB rgb = value.rgb;
        return (rgb.red << 24) | (rgb.green << 16) | (rgb.blue << 8) | value.alpha;
    }
}
