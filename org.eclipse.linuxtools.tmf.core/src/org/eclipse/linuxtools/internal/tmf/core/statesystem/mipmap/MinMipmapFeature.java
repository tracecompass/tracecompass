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
import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue.Type;

/**
 * The minimum mipmap feature.
 *
 * Each mipmap state value is the minimum numerical value of all the non-null
 * lower-level state values it covers. The state value is of the same type as
 * the base attribute.
 */
public class MinMipmapFeature extends TmfMipmapFeature {

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
    public MinMipmapFeature(final int baseQuark, final int mipmapQuark, final int mipmapResolution, final ITmfStateSystemBuilder ss) {
        super(baseQuark, mipmapQuark, mipmapResolution, ss);
    }

    @Override
    protected ITmfStateValue computeMipmapValue(List<ITmfStateInterval> lowerIntervals, long startTime, long endTime) {
        ITmfStateValue minValue = null;
        try {
            for (ITmfStateInterval interval : lowerIntervals) {
                ITmfStateValue value = interval.getStateValue();
                if (value.getType() == Type.DOUBLE) {
                    if (minValue == null || value.unboxDouble() < minValue.unboxDouble()) {
                        minValue = value;
                    }
                } else {
                    if (minValue == null || value.unboxLong() < minValue.unboxLong()) {
                        minValue = value;
                    }
                }
            }
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        }
        return minValue;
    }
}
