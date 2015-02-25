/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemFactory;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.InMemoryBackend;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link StateSystemUtils} class
 *
 * @author Geneviève Bastien
 */
public class StateSystemUtilsTest {

    private static final long START_TIME = 1000L;
    private static final @NonNull String DUMMY_STRING = "test";

    private ITmfStateSystemBuilder fStateSystem;

    /**
     * Build a small test state system in memory
     */
    @Before
    public void setupStateSystem() {
        try {
            IStateHistoryBackend backend = new InMemoryBackend(START_TIME);
            fStateSystem = StateSystemFactory.newStateSystem(DUMMY_STRING, backend);
            int quark = fStateSystem.getQuarkAbsoluteAndAdd(DUMMY_STRING);

            fStateSystem.modifyAttribute(1200L, TmfStateValue.newValueInt(10), quark);
            fStateSystem.modifyAttribute(1500L, TmfStateValue.newValueInt(20), quark);
            fStateSystem.closeHistory(2000L);
        } catch (StateValueTypeException | AttributeNotFoundException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Clean-up
     */
    @After
    public void tearDown() {
        fStateSystem.dispose();
    }

    /**
     * Test the {@link StateSystemUtils#queryUntilNonNullValue} method.
     */
    @Test
    public void testQueryUntilNonNullValue() {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);

        int quark;
        try {
            quark = ss.getQuarkAbsolute(DUMMY_STRING);

            /* Should return null if requested range is not within range */
            assertNull(StateSystemUtils.queryUntilNonNullValue(ss, quark, 0, 999L));
            assertNull(StateSystemUtils.queryUntilNonNullValue(ss, quark, 2001L, 5000L));

            /*
             * Should return null if request within range, but condition is
             * false
             */
            assertNull(StateSystemUtils.queryUntilNonNullValue(ss, quark, 1000L, 1199L));

            /*
             * Should return the right interval if an interval is within range,
             * even if the range starts or ends outside state system range
             */
            ITmfStateInterval interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 1000L, 1300L);
            assertNotNull(interval);
            assertEquals(ITmfStateValue.Type.INTEGER, interval.getStateValue().getType());
            assertEquals(10, interval.getStateValue().unboxInt());

            interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 800L, 2500L);
            assertNotNull(interval);
            assertEquals(ITmfStateValue.Type.INTEGER, interval.getStateValue().getType());
            assertEquals(10, interval.getStateValue().unboxInt());

            interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 1300L, 1800L);
            assertNotNull(interval);
            assertEquals(ITmfStateValue.Type.INTEGER, interval.getStateValue().getType());
            assertEquals(10, interval.getStateValue().unboxInt());

            interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 1500L, 1800L);
            assertNotNull(interval);
            assertEquals(ITmfStateValue.Type.INTEGER, interval.getStateValue().getType());
            assertEquals(20, interval.getStateValue().unboxInt());

            interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 1800L, 2500L);
            assertNotNull(interval);
            assertEquals(ITmfStateValue.Type.INTEGER, interval.getStateValue().getType());
            assertEquals(20, interval.getStateValue().unboxInt());

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }

    }

}
