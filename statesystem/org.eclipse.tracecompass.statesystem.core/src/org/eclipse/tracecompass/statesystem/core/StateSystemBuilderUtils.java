/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core;

import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

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
     * @throws AttributeNotFoundException
     *             If the quark is invalid
     */
    public static void incrementAttributeLong(ITmfStateSystemBuilder ssb, long t, int attributeQuark, long increment)
            throws StateValueTypeException, AttributeNotFoundException {
        ITmfStateValue stateValue = ssb.queryOngoingState(attributeQuark);

        /* if the attribute was previously null, start counting at 0 */
        long prevValue = 0;
        if (!stateValue.isNull()) {
            prevValue = stateValue.unboxLong();
        }
        ssb.modifyAttribute(t, TmfStateValue.newValueLong(prevValue + increment), attributeQuark);
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
     * @throws AttributeNotFoundException
     *             If the quark is invalid
     */
    public static void incrementAttributeInt(ITmfStateSystemBuilder ssb, long t, int attributeQuark, int increment)
            throws StateValueTypeException, AttributeNotFoundException {
        ITmfStateValue stateValue = ssb.queryOngoingState(attributeQuark);

        /* if the attribute was previously null, start counting at 0 */
        int prevValue = 0;
        if (!stateValue.isNull()) {
            prevValue = stateValue.unboxInt();
        }
        ssb.modifyAttribute(t, TmfStateValue.newValueInt(prevValue + increment), attributeQuark);
    }

}
