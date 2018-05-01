/*******************************************************************************
 * Copyright (c) 2018 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.presentation;

/**
 * A rotating color provider, allows the provider to give a slice of a color
 * wheel with respect to a fixed brightness and saturation and a defined
 * sampling on the hue. Please keep in mind, a rotating palette is the easiest
 * way to trigger ambiguity in colorblind individuals. However, it is quite good
 * at describing qualitative aspects, as nothing stands out. It is nearly
 * identical to the parent class but is locked to a single color wheel slice
 * rather than alternating them. (This is the palette provider that can give a
 * rainbow).
 *
 * This palette is useful for categorizing data. An example would be function
 * calls.
 *
 * @author Matthew Khouzam
 * @since 4.0
 */
public class RotatingPaletteProvider extends QualitativePaletteProvider {

    /**
     * Creates a new Builder initialized with the Trace Compass defaults.
     * This builder will generate an {@link IPaletteProvider}
     * <p>
     * Initial values are:
     * </p>
     *
     * <ul>
     * <li>brightness(0.6f)</li>
     * <li>saturation(0.6f)</li>
     * <li>nbColors(360)</li>
     * </ul>
     *
     * @author Matthew Khouzam
     */
    public static class Builder {
        private static final float DEFAULT_BRIGHTNESS = 0.6f;
        private static final float DEFAULT_SATURATION = 0.6f;
        private static final int DEFAULT_NB_COLOR = 360;
        private int fBuilderNbColors = DEFAULT_NB_COLOR;
        private float fBuilderSaturation = DEFAULT_SATURATION;
        private float fBuilderBrightness = DEFAULT_BRIGHTNESS;

        /**
         * Set the brightness of the color wheel
         *
         * @param brightness
         *            the brightness between 0 and 1
         * @return the builder
         */
        public Builder setBrightness(float brightness) {
            fBuilderBrightness = brightness;
            return this;
        }

        /**
         * Set the saturation of the color wheel
         *
         * @param saturation
         *            the saturation between 0 and 1
         * @return the builder
         */
        public Builder setSaturation(float saturation) {
            fBuilderSaturation = saturation;
            return this;
        }

        /**
         * Set the number of colors of the wheel
         *
         * @param nbColors
         *            the number of colors, positive integer
         * @return the builder
         */
        public Builder setNbColors(int nbColors) {
            fBuilderNbColors = nbColors;
            return this;
        }

        /**
         * Build the palette provider
         *
         * @return the the palette provider
         */
        public RotatingPaletteProvider build() {
            return new RotatingPaletteProvider(fBuilderNbColors, fBuilderSaturation, fBuilderBrightness);
        }
    }

    /**
     * Constructor
     *
     * @param nbColors
     *            number of colors to have in the palette
     * @param saturation
     *            saturation of the colors
     * @param brightness
     *            the brightness of the colors
     */
    protected RotatingPaletteProvider(int nbColors, float saturation, float brightness) {
        super(nbColors, saturation, brightness, 0.0f);
    }

}
