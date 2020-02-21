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

package org.eclipse.tracecompass.statesystem.core.tests.shared.utils;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

/**
 * State interval class to help with unit testing. This class should be used in
 * unit test to store the expected values of an interval that can then be
 * compared with an actual interval from the state system. It is not meant to be
 * inserted in the state system, so the attribute field does not need to be set.
 *
 * @author Geneviève Bastien
 */
public class StateIntervalStub implements ITmfStateInterval {

    /*
     * For this kind of interval, the attribute does not matter, so just set a
     * default value here
     */
    private static final int TEST_ATTRIBUTE = 1;

    private final long fStart;
    private final long fEnd;
    private final @Nullable Object fObject;

    /**
     * Constructor. New code using this class should consider using
     * {@link #StateIntervalStub(int, int, Object)} instead
     *
     * @param start
     *            Start time of the interval
     * @param end
     *            End time of the interval
     * @param value
     *            Value of the interval
     */
    public StateIntervalStub(final int start, final int end, final ITmfStateValue value) {
        this (start, end, value.unboxValue());
    }

    /**
     * Constructor
     *
     * @param start
     *            Start time of the interval
     * @param end
     *            End time of the interval
     * @param value
     *            Value of the interval
     */
    public StateIntervalStub(final int start, final int end, final @Nullable Object value) {
        fStart = start;
        fEnd = end;
        fObject = value;
    }

    @Override
    public long getStartTime() {
        return fStart;
    }

    @Override
    public long getEndTime() {
        return fEnd;
    }

    @Override
    public int getAttribute() {
        return TEST_ATTRIBUTE;
    }

    @Override
    public ITmfStateValue getStateValue() {
        return TmfStateValue.newValue(fObject);
    }

    @Override
    public @Nullable Object getValue() {
        return fObject;
    }

    @Override
    public boolean intersects(long timestamp) {
        return (fStart >= timestamp && fEnd <= timestamp);
    }

}
