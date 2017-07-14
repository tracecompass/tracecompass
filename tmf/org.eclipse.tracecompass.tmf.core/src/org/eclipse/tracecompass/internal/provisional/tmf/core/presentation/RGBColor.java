/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.presentation;

/**
 * This class represents a color with its red, green and blue component. The
 * red, green and blue values must be between 0 and 255.
 *
 * @author Yonni Chen
 */
public class RGBColor {

    private final short fRed;
    private final short fGreen;
    private final short fBlue;

    /**
     * Constructor
     *
     * @param red
     *            The red component of the color
     * @param green
     *            The green component of the color
     * @param blue
     *            The blue component of the color
     */
    public RGBColor(int red, int green, int blue) {
        if ((red > 255) || (red < 0)) {
            throw new IllegalArgumentException("Red component must be between 0 and 255"); //$NON-NLS-1$
        }

        if ((green > 255) || (green < 0)) {
            throw new IllegalArgumentException("Green component must be between 0 and 255"); //$NON-NLS-1$
        }

        if ((blue > 255) || (blue < 0)) {
            throw new IllegalArgumentException("Blue component must be between 0 and 255"); //$NON-NLS-1$
        }

        fRed = (short) red;
        fGreen = (short) green;
        fBlue = (short) blue;
    }

    /**
     * Constructor. This constructor extract RGB components from an integer.
     *
     * <li>0 maps to 0x000000 or rgb(0, 0, 0)</li>
     * <li>16777215 maps to 0xFFFFFF or rgb(255, 255, 255)</li>
     * <br/>
     * If you give an integer greater than 16777215, there will be an overflow. For
     * example, if you give 16777216, it will map to the same color as 0.
     *
     * @param color
     *            The color as an integer
     */
    public RGBColor(int color) {
        fRed = (short) ((color >> 16) & 0xFF);
        fGreen = (short) ((color >> 8) & 0xFF);
        fBlue = (short) (color & 0xFF);
    }

    /**
     * Gets the red component of the color
     *
     * @return The red component of the color
     */
    public int getRed() {
        return fRed;
    }

    /**
     * Gets the green component of the color
     *
     * @return The green component of the color
     */
    public int getGreen() {
        return fGreen;
    }

    /**
     * Gets the blue component of the color
     *
     * @return The blue component of the color
     */
    public int getBlue() {
        return fBlue;
    }
}
