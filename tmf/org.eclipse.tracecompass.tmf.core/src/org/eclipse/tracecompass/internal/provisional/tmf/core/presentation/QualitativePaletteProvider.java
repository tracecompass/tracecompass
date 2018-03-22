/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.presentation;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * A qualitative color provider, allows the provider to give alternating slices
 * of a color wheel with respect to a fixed brightness and saturation and a
 * defined sampling on the hue. Please bear in mind, a rotating palette is the
 * easiest way to trigger ambiguity in colorblind individuals. However, it is
 * quite good at describing qualitative aspects, as nothing stands out.
 *
 * This palette is useful for categorizing data. An example would be function
 * calls.
 *
 * This palette allows the largest amount of distinct colors to co-exist in a
 * chart
 *
 * @author Matthew Khouzam
 */
public class QualitativePaletteProvider implements IPaletteProvider {

    private final List<RGBAColor> fColors;
    private final int fNbColors;
    private final float fSaturation;
    private final float fBrightness;

    private final float fAttenuationFactor;

    /**
     * Creates a new Builder. This builder will generate an {@link IPaletteProvider}
     * <p>
     * Initial values are:
     * </p>
     *
     * <ul>
     * <li>brightness(0.8f)</li>
     * <li>saturation(0.8f)</li>
     * <li>nbColors(360)</li>
     * <li>attenuationFactor(0.15f)</li>
     * </ul>
     *
     * @author Matthew Khouzam
     */
    public static class Builder {
        private static final float DEFAULT_BRIGHTNESS = 0.8f;
        private static final float DEFAULT_SATURATION = 0.8f;
        private static final int DEFAULT_NB_COLOR = 360;
        private int fBuilderNbColors = DEFAULT_NB_COLOR;
        private float fBuilderSaturation = DEFAULT_SATURATION;
        private float fBuilderBrightness = DEFAULT_BRIGHTNESS;
        private float fBuilderAttenuation = 0.15f;

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
         * Set the attenuation factor for the darker elements.
         *
         * @param attenuation
         *            The attenuation factor
         *
         * @return the the palette provider
         */
        public Builder setAttenuation(float attenuation) {
            fBuilderAttenuation = attenuation;
            return this;
        }

        /**
         * Build the palette provider
         *
         * @return the the palette provider
         */
        public QualitativePaletteProvider build() {
            return new QualitativePaletteProvider(fBuilderNbColors, fBuilderSaturation, fBuilderBrightness, fBuilderAttenuation);
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
     * @param attenuationFactor
     *            the attenuation factor, this is the jump distance of the color
     *            wheel between alternating elements.
     */
    public QualitativePaletteProvider(int nbColors, float saturation, float brightness, float attenuationFactor) {
        fNbColors = nbColors;
        fSaturation = saturation;
        fBrightness = brightness;
        fAttenuationFactor = attenuationFactor;
        ImmutableList.Builder<RGBAColor> builder = new ImmutableList.Builder<>();
        boolean odd = false;
        float hue = 0;
        double step = RGBAColor.TAU / (nbColors + 1);
        for (int i = 0; i < nbColors; i++) {
            hue += step;
            RGBAColor color = (odd) ? RGBAColor.fromHSBA(hue, saturation * (1.0f - attenuationFactor), brightness * (1.0f - attenuationFactor), 1.0f) : RGBAColor.fromHSBA(hue, saturation, brightness, 1.0f);
            builder.add(color);
            odd = !odd;
        }
        fColors = builder.build();
    }

    /**
     * @return the nbColors
     */
    public int getNbColors() {
        return fNbColors;
    }

    /**
     * @return the saturation
     */
    public float getSaturation() {
        return fSaturation;
    }

    /**
     * @return the brightness
     */
    public float getBrightness() {
        return fBrightness;
    }

    /**
     * @return the attenuationFactor
     */
    public float getAttenuationFactor() {
        return fAttenuationFactor;
    }

    @Override
    public List<RGBAColor> get() {
        return fColors;
    }

}
