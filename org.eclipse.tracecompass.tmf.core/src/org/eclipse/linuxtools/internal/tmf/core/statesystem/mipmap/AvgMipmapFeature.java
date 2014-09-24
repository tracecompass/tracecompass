/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Christian Kouamé - Initial API and implementation
 *     Patrick Tasse - Updates to mipmap feature
 *******************************************************************************/
package org.eclipse.linuxtools.internal.tmf.core.statesystem.mipmap;

import java.util.List;

import org.eclipse.linuxtools.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue.Type;

/**
 * The average mipmap feature.
 *
 * Each mipmap state value is the weighted average by time duration of all the
 * lower-level state values it covers. Null state values count as zero in the
 * weighted average. The state value is a Double.
 */
public class AvgMipmapFeature extends TmfMipmapFeature {

    /**
     * Constructor
     *
     * @param baseQuark
     *            The quark for the attribute we want to mipmap
     * @param mipmapQuark
     *            The quark of the mipmap feature attribute
     * @param mipmapResolution
     *            The resolution that will be use in the mipmap
     * @param ss
     *            The state system in which to insert the state changes
     */
    public AvgMipmapFeature(final int baseQuark, final int mipmapQuark, final int mipmapResolution, final ITmfStateSystemBuilder ss) {
        super(baseQuark, mipmapQuark, mipmapResolution, ss);
    }

    @Override
    protected ITmfStateValue computeMipmapValue(List<ITmfStateInterval> lowerIntervals, long startTime, long endTime) {
        long range = endTime - startTime;
        if (range <= 0) {
            return TmfStateValue.newValueDouble(0.0);
        }
        double sum = 0.0;
        try {
            for (ITmfStateInterval interval : lowerIntervals) {
                ITmfStateValue value = interval.getStateValue();
                long duration = interval.getEndTime() - interval.getStartTime();
                if (value.getType() == Type.DOUBLE) {
                    sum += value.unboxDouble() * duration;
                } else {
                    sum += (double) value.unboxLong() * duration;
                }
            }
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        }
        double average = sum / range;
        ITmfStateValue avgValue = TmfStateValue.newValueDouble(average);
        return avgValue;
    }
}
