/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jean-Christian Kouamé - Initial API and implementation
 *     Patrick Tasse - Updates to mipmap feature
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.core.statesystem.mipmap;

import java.util.List;

import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

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
        for (ITmfStateInterval interval : lowerIntervals) {
            ITmfStateValue value = interval.getStateValue();
            long duration = interval.getEndTime() - interval.getStartTime();
            if (value.getType() == Type.DOUBLE) {
                sum += value.unboxDouble() * duration;
            } else {
                sum += (double) value.unboxLong() * duration;
            }
        }
        double average = sum / range;
        ITmfStateValue avgValue = TmfStateValue.newValueDouble(average);
        return avgValue;
    }
}
