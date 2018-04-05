/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.presentation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.presentation.IPaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Sequential Palette color provider. Uses a gamma curve to provide ideal
 * distances. This typically denotes rising intensities. The first values in the
 * list are lightest and denote "low" values, the last values are the ones
 * denoting higher or intenser values.
 *
 * Users seeing this scheme will automatically assume the values are comparable
 * and can be ordered.
 *
 * Can be used in heatmaps to show slower trends.
 *
 * Please do not use too many steps (over 9) as it will blend in easily on
 * printed out paper and lower end LCD displays.
 *
 * @author Matthew Khouzam
 */
public class SequentialPaletteProvider implements IPaletteProvider {

    private final List<RGBAColor> fColors;

    private SequentialPaletteProvider() {
        // Do nothing
        fColors = Collections.emptyList();
    }

    private SequentialPaletteProvider(Collection<RGBAColor> colors) {
        fColors = ImmutableList.copyOf(colors);
    }

    /**
     * Create a palette
     *
     * @param startingColor
     *            the source color that will be diluted until it faded to white.
     * @param nbColors
     *            the number of colors to use
     * @return the palette provider
     */
    public static IPaletteProvider create(RGBAColor startingColor, int nbColors) {
        float[] components = startingColor.getHSBA();
        float stepBright = (1.0f - components[2]) / nbColors; // brightness steps
        float stepSat = (components[1]) / nbColors; // brightness steps
        float hue = components[0];
        float brightness = 1.0f;
        float saturation = 0.0f;
        ImmutableList.Builder<RGBAColor> builder = new Builder<>();
        for (int i = 0; i < nbColors; i++) {
            builder.add(RGBAColor.fromHSBA(hue, saturation, brightness, components[3]));
            saturation += stepSat;
            brightness -= stepBright;
        }
        return new SequentialPaletteProvider(builder.build());
    }

    @Override
    public List<RGBAColor> get() {
        return fColors;
    }

}
