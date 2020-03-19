/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;

/**
 * Provide utility methods for building the state system
 *
 * @since 2.0
 */
public final class StateSystemBuilderUtils {

    private StateSystemBuilderUtils() {
    }

    /**
     * Increments attribute by a certain long value. Reads the current value of
     * a given attribute as a long, and increment it by a certain increment.
     *
     * @param ssb
     *            The state system builder
     * @param t
     *            The time at which to do the increment
     * @param attributeQuark
     *            The quark of the attribute to increment
     * @param increment
     *            The value to increment. This value can be negative.
     * @throws StateValueTypeException
     *             If the attribute already exists but is not of type Long
     */
    public static void incrementAttributeLong(ITmfStateSystemBuilder ssb, long t, int attributeQuark, long increment)
            throws StateValueTypeException {
        @Nullable Object stateValue = ssb.queryOngoing(attributeQuark);

        /* if the attribute was previously null, start counting at 0 */
        long prevValue = 0;
        if (stateValue != null && stateValue instanceof Long) {
            prevValue = (long) stateValue;
        }
        ssb.modifyAttribute(t, prevValue + increment, attributeQuark);
    }

    /**
     * Increments attribute by a certain integer value. Reads the current value
     * of a given attribute as an int, and increment it by a certain increment.
     *
     * @param ssb
     *            The state system builder
     * @param t
     *            The time at which to do the increment
     * @param attributeQuark
     *            The quark of the attribute to increment
     * @param increment
     *            The value to increment. This value can be negative.
     * @throws StateValueTypeException
     *             If the attribute already exists but is not of type Integer
     */
    public static void incrementAttributeInt(ITmfStateSystemBuilder ssb, long t, int attributeQuark, int increment)
            throws StateValueTypeException {
        @Nullable Object stateValue = ssb.queryOngoing(attributeQuark);

        /* if the attribute was previously null, start counting at 0 */
        int prevValue = 0;
        if (stateValue != null && stateValue instanceof Integer) {
            prevValue = (int) stateValue;
        }
        ssb.modifyAttribute(t, prevValue + increment, attributeQuark);
    }

}
