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

package org.eclipse.tracecompass.statesystem.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemBuilderUtils;
import org.eclipse.tracecompass.statesystem.core.StateSystemFactory;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link StateSystemBuilderUtils} class
 *
 * @author Geneviève Bastien
 */
public class StateSystemBuilderUtilsTest {

    private static final long START_TIME = 1000L;
    private static final long TIME_INCREMENT = 10;
    private static final @NonNull String DUMMY_STRING = "test";

    private ITmfStateSystemBuilder fStateSystem;

    /**
     * Build a small test state system in memory
     */
    @Before
    public void setupStateSystem() {
        try {
            IStateHistoryBackend backend = StateHistoryBackendFactory.createInMemoryBackend(DUMMY_STRING, START_TIME);
            fStateSystem = StateSystemFactory.newStateSystem(backend);

        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test the
     * {@link StateSystemBuilderUtils#incrementAttributeLong(ITmfStateSystemBuilder, long, int, long)}
     * method
     */
    @Test
    public void testIncrementLong() {
        ITmfStateSystemBuilder ss = fStateSystem;
        int quark = ss.getQuarkAbsoluteAndAdd(DUMMY_STRING);

        /* Value should be null at the beginning */
        ITmfStateValue value = ss.queryOngoingState(quark);
        assertEquals(TmfStateValue.nullValue(), value);

        /* Increment by 3 */
        long increment = 3;
        StateSystemBuilderUtils.incrementAttributeLong(ss, START_TIME + TIME_INCREMENT, quark, increment);
        value = ss.queryOngoingState(quark);
        assertEquals(TmfStateValue.newValueLong(increment), value);

        /* Increment by 1000 */
        Long increment2 = 1000L;
        StateSystemBuilderUtils.incrementAttributeLong(ss, START_TIME + TIME_INCREMENT, quark, increment2);
        value = ss.queryOngoingState(quark);
        assertEquals(TmfStateValue.newValueLong(increment + increment2), value);

        /* Increment by a negative value */
        Long increment3 = -500L;
        StateSystemBuilderUtils.incrementAttributeLong(ss, START_TIME + TIME_INCREMENT, quark, increment3);
        value = ss.queryOngoingState(quark);
        assertEquals(TmfStateValue.newValueLong(increment + increment2 + increment3), value);
    }

    /**
     * Test the
     * {@link StateSystemBuilderUtils#incrementAttributeInt(ITmfStateSystemBuilder, long, int, int)}
     * method
     */
    @Test
    public void testIncrementInt() {
        ITmfStateSystemBuilder ss = fStateSystem;
        int quark = ss.getQuarkAbsoluteAndAdd(DUMMY_STRING);

        /* Value should be null at the beginning */
        ITmfStateValue value = ss.queryOngoingState(quark);
        assertEquals(TmfStateValue.nullValue(), value);

        /* Increment by 3 */
        int increment = 3;
        StateSystemBuilderUtils.incrementAttributeInt(ss, START_TIME + TIME_INCREMENT, quark, increment);
        value = ss.queryOngoingState(quark);
        assertEquals(TmfStateValue.newValueInt(increment), value);

        /* Increment by 1000 */
        int increment2 = 1000;
        StateSystemBuilderUtils.incrementAttributeInt(ss, START_TIME + TIME_INCREMENT, quark, increment2);
        value = ss.queryOngoingState(quark);
        assertEquals(TmfStateValue.newValueInt(increment + increment2), value);

        /* Increment by a negative value */
        int increment3 = -500;
        StateSystemBuilderUtils.incrementAttributeInt(ss, START_TIME + TIME_INCREMENT, quark, increment3);
        value = ss.queryOngoingState(quark);
        assertEquals(TmfStateValue.newValueInt(increment + increment2 + increment3), value);
    }

    /**
     * Test the
     * {@link StateSystemBuilderUtils#incrementAttributeDouble(ITmfStateSystemBuilder, long, int, double)}
     * method
     */
    @Test
    public void testIncrementDouble() {
        ITmfStateSystemBuilder ss = fStateSystem;
        int quark = ss.getQuarkAbsoluteAndAdd(DUMMY_STRING);

        /* Value should be null at the beginning */
        ITmfStateValue value = ss.queryOngoingState(quark);
        assertEquals(TmfStateValue.nullValue(), value);

        /* Increment by 3 */
        double increment = 3.0;
        StateSystemBuilderUtils.incrementAttributeDouble(ss, START_TIME + TIME_INCREMENT, quark, increment);
        value = ss.queryOngoingState(quark);
        assertEquals(TmfStateValue.newValueDouble(increment), value);

        /* Increment by 1000 */
        double increment2 = 1000.0;
        StateSystemBuilderUtils.incrementAttributeDouble(ss, START_TIME + TIME_INCREMENT, quark, increment2);
        value = ss.queryOngoingState(quark);
        assertEquals(TmfStateValue.newValueDouble(increment + increment2), value);

        /* Increment by a negative value */
        double increment3 = -500.0;
        StateSystemBuilderUtils.incrementAttributeDouble(ss, START_TIME + TIME_INCREMENT, quark, increment3);
        value = ss.queryOngoingState(quark);
        assertEquals(TmfStateValue.newValueDouble(increment + increment2 + increment3), value);
    }
}
