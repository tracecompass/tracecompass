/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.eclipse.tracecompass.internal.statesystem.core.StateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Test the {@link StateSystem} specific methods
 *
 * @author Geneviève Bastien
 */
public class StateSystemTest {

    /** Time-out tests after 1 minute. */
    @Rule
    public TestRule globalTimeout = new Timeout(1, TimeUnit.MINUTES);

    private ITmfStateSystemBuilder fSs;

    /**
     * Create the test state system
     */
    @Before
    public void setup() {
        IStateHistoryBackend backend = StateHistoryBackendFactory.createNullBackend("Test");
        fSs = new StateSystem(backend);
    }

    /**
     * Test the {@link StateSystem#waitUntilBuilt(long)} method
     */
    @Test
    public void testWaitUntilBuilt() {
        ITmfStateSystemBuilder ss = fSs;
        assertNotNull(ss);

        long timeout = 500;
        long begin = System.currentTimeMillis();
        assertFalse(ss.waitUntilBuilt(timeout));
        long end = System.currentTimeMillis();

        /*
         * We cannot be sure of the exact time, but just make sure the method returned
         * and the delay is longer than the timeout
         */
        assertTrue(end - begin >= timeout);

        /*
         * The delay is undeterministic, we cannot check anything else than whether it
         * returned or not
         */
        assertFalse(ss.waitUntilBuilt(0));

        ss.closeHistory(timeout);

        /* The history is closed, so now these methods should return true */
        assertTrue(ss.waitUntilBuilt(timeout));
        assertTrue(ss.waitUntilBuilt(0));
    }

    /**
     * Test modifying or updating the state value, then querying it
     */
    @Test
    public void testSetAndQueryOngoing() {
        ITmfStateSystemBuilder ss = fSs;
        assertNotNull(ss);
        long time = 10;

        int quark = ss.getQuarkAbsoluteAndAdd("Test");

        // Modify the attribute, then query the ongoing value
        long val = 10L;
        ss.modifyAttribute(time, val, quark);
        // Query the state value
        ITmfStateValue ongoingState = ss.queryOngoingState(quark);
        assertTrue(ongoingState.getType() == Type.LONG);
        assertEquals(val, ongoingState.unboxLong());
        // Query the value
        Object ongoing = ss.queryOngoing(quark);
        assertTrue(ongoing instanceof Long);
        assertEquals(val, ongoing);

        // Modify with a state value, the query the ongoing value
        val = 12L;
        ss.modifyAttribute(time + 1, val, quark);
        // Query the state value
        ongoingState = ss.queryOngoingState(quark);
        assertTrue(ongoingState.getType() == Type.LONG);
        assertEquals(val, ongoingState.unboxLong());
        // Query the value
        ongoing = ss.queryOngoing(quark);
        assertTrue(ongoing instanceof Long);
        assertEquals(val, ongoing);

        // Update the ongoing value with a state value, then query
        val = 14L;
        ss.updateOngoingState(TmfStateValue.newValue(val), quark);
        // Query the state value
        ongoingState = ss.queryOngoingState(quark);
        assertTrue(ongoingState.getType() == Type.LONG);
        assertEquals(val, ongoingState.unboxLong());
        // Query the value
        ongoing = ss.queryOngoing(quark);
        assertTrue(ongoing instanceof Long);
        assertEquals(val, ongoing);

        // Update the ongoing value, then query
        val = 16L;
        ss.updateOngoingState(val, quark);
        // Query the state value
        ongoingState = ss.queryOngoingState(quark);
        assertTrue(ongoingState.getType() == Type.LONG);
        assertEquals(val, ongoingState.unboxLong());
        // Query the value
        ongoing = ss.queryOngoing(quark);
        assertTrue(ongoing instanceof Long);
        assertEquals(val, ongoing);
    }

}
