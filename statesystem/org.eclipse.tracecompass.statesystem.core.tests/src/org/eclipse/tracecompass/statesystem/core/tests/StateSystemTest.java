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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.tracecompass.internal.statesystem.core.StateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
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

    /**
     * Test getting various lists of attributes
     */
    @Test
    public void testAttributes() {
        String searchPattern = "abc";
        String attribute1 = "Test";
        String attribute2 = attribute1 + searchPattern;
        String attribute3 = attribute1 + "\n" + searchPattern;
        String search = "(.*)" + searchPattern + "(.*)";
        ITmfStateSystemBuilder ss = fSs;
        assertNotNull(ss);

        // Add the 3 attributes to the root
        int quark1 = ss.getQuarkAbsoluteAndAdd(attribute1);
        int quark2 = ss.getQuarkAbsoluteAndAdd(attribute2);
        int quark3 = ss.getQuarkAbsoluteAndAdd(attribute3);

        // Add the 3 attributes as children of one attribute
        int quark11 = ss.getQuarkRelativeAndAdd(quark1, attribute1);
        int quark12 = ss.getQuarkRelativeAndAdd(quark1, attribute2);
        int quark132 = ss.getQuarkRelativeAndAdd(quark1, attribute3, attribute2);
        // This attribute should already be created from previous call
        int quark13 = ss.getQuarkRelativeAndAdd(quark1, attribute3);

        // Query the sub-attributes from the root
        List<Integer> subAttributes = ss.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, false);
        List<Integer> expected = Arrays.asList(quark1, quark2, quark3);
        assertArrayContent(expected, subAttributes);

        // Query the sub-attributes from the root recursively
        subAttributes = ss.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, true);
        expected = Arrays.asList(quark1, quark11, quark12, quark13, quark132, quark2, quark3);
        assertArrayContent(expected, subAttributes);

        // Query the sub-attributes from the root with exact match
        subAttributes = ss.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, false, attribute1);
        expected = Arrays.asList(quark1);
        assertArrayContent(expected, subAttributes);

        // Query the sub-attributes from the root with exact match
        // recursively
        subAttributes = ss.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, true, attribute1);
        expected = Arrays.asList(quark1, quark11);
        assertArrayContent(expected, subAttributes);

        // Query the sub-attributes from the root with regex
        subAttributes = ss.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, false, search);
        expected = Arrays.asList(quark2, quark3);
        assertArrayContent(expected, subAttributes);

        // Query the sub-attributes from the root recursively with regex
        subAttributes = ss.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, true, search);
        expected = Arrays.asList(quark2, quark3, quark12, quark13, quark132);
        assertArrayContent(expected, subAttributes);

        // Query the sub-attributes from another quark
        subAttributes = ss.getSubAttributes(quark1, false);
        expected = Arrays.asList(quark11, quark12, quark13);
        assertArrayContent(expected, subAttributes);

        // Query the sub-attributes from another quark with regex
        subAttributes = ss.getSubAttributes(quark1, false, search);
        expected = Arrays.asList(quark12, quark13);
        assertArrayContent(expected, subAttributes);

        // Test getting the attribute names
        assertEquals(attribute1, ss.getAttributeName(quark1));
        assertEquals(attribute2, ss.getAttributeName(quark2));
        assertEquals(attribute3, ss.getAttributeName(quark3));
    }

    private static void assertArrayContent(List<Integer> expected, List<Integer> actual) {
        // Use a temp list to remove matched object, in case there are
        // duplicates
        List<Integer> list = new ArrayList<>(actual);
        for (Integer expectedInt : expected) {
            assertTrue("Missing value " + expectedInt, list.contains(expectedInt));
            list.remove(expectedInt);
        }
        assertEquals(expected.size(), actual.size());

    }

}
