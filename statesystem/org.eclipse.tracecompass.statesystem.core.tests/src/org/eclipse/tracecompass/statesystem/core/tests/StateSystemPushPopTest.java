/*******************************************************************************
 * Copyright (c) 2012, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 ******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.tracecompass.internal.statesystem.core.StateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for stack-attributes in the Generic State System (using
 * pushAttribute() and popAttribute())
 *
 * @author Alexandre Montplaisir
 */
public class StateSystemPushPopTest {

    private ITmfStateSystemBuilder ss;
    private int attribute;

    private File testHtFile;

    private final static String errMsg = "Caught exception: ";

    /* State values that will be used */
    //private final static ITmfStateValue nullValue = TmfStateValue.nullValue();
    private final static String value1 = "A";
    private final static int value2 = 10;
    private final static Object  value3 = null;
    private final static String value4 = "D";
    private final static long value5 = Long.MAX_VALUE;

    /**
     * Initialization. We run the checks for the return values of
     * .popAttribute() in here, since this is only available when we are
     * building the state history.
     *
     * @throws IOException
     *             If we can write the file to the temporary directory.
     * @throws TimeRangeException
     *             Fails the test
     * @throws AttributeNotFoundException
     *             Fails the test
     * @throws StateValueTypeException
     *             Fails the test
     */
    @Before
    public void setUp() throws IOException, TimeRangeException,
            AttributeNotFoundException, StateValueTypeException {
        Object value;
        testHtFile = File.createTempFile("test", ".ht");

        IStateHistoryBackend backend = StateHistoryBackendFactory.createHistoryTreeBackendNewFile(
                "push-pop-test", checkNotNull(testHtFile), 0, 0, 0);
        ss = new StateSystem(backend, true);

        /* Build the thing */
        final int attrib = ss.getQuarkAbsoluteAndAdd("Test", "stack");

        ss.pushAttribute( 2, value1, attrib);
        ss.pushAttribute( 4, value2, attrib);
        ss.pushAttribute( 6, value3, attrib);
        ss.pushAttribute( 8, value4, attrib);
        ss.pushAttribute(10, value5, attrib);

        value = ss.popAttributeObject(11, attrib);
        assertEquals(value5, value);

        value = ss.popAttributeObject(12, attrib);
        assertEquals(value4, value);

        value = ss.popAttributeObject(14, attrib);
        assertEquals(value3, value);

        value = ss.popAttributeObject(16, attrib);
        assertEquals(value2, value);

        value = ss.popAttributeObject(17, attrib);
        assertEquals(value1, value);

        value = ss.popAttributeObject(20, attrib);
        assertEquals(null, value); // Stack should already be empty here.

        ss.pushAttribute(21, value1, attrib);
        //ss.pushAttribute(22, value1, attrib); //FIXME pushing twice the same value bugs out atm
        ss.pushAttribute(22, value2, attrib);

        value = ss.popAttributeObject(24, attrib);
        //assertEquals(value1, value);
        assertEquals(value2, value);

        value = ss.popAttributeObject(26, attrib);
        assertEquals(value1, value);

        value = ss.popAttributeObject(28, attrib);
        assertEquals(null, value); // Stack should already be empty here.

        ss.closeHistory(30);
        attribute = ss.getQuarkAbsolute("Test", "stack");
    }

    /**
     * Clean-up after running a test. Delete the .ht file we created.
     */
    @After
    public void tearDown() {
        testHtFile.delete();
    }

    /**
     * Test that the value of the stack-attribute at the start and end of the
     * history are correct.
     */
    @Test
    public void testBeginEnd() {
        try {
            ITmfStateInterval interval = ss.querySingleState(0, attribute);
            assertEquals(0, interval.getStartTime());
            assertEquals(1, interval.getEndTime());
            assertTrue(interval.getStateValue().isNull());

            interval = ss.querySingleState(29, attribute);
            assertEquals(26, interval.getStartTime());
            assertEquals(30, interval.getEndTime());
            assertTrue(interval.getStateValue().isNull());

        } catch (TimeRangeException | StateSystemDisposedException e) {
            fail(errMsg + e.toString());
        }
    }

    /**
     * Run single queries on the attribute stacks (with .querySingleState()).
     */
    @Test
    public void testSingleQueries() {
        try {
            final int subAttribute1 = ss.getQuarkRelative(attribute, "1");
            final int subAttribute2 = ss.getQuarkRelative(attribute, "2");

            /* Test the stack attributes themselves */
            ITmfStateInterval interval = ss.querySingleState(11, attribute);
            assertEquals(4, interval.getStateValue().unboxInt());

            interval = ss.querySingleState(24, attribute);
            assertEquals(1, interval.getStateValue().unboxInt());

            /* Go retrieve the user values manually */
            interval = ss.querySingleState(10, subAttribute1);
            assertEquals(value1, interval.getValue()); //

            interval = ss.querySingleState(22, subAttribute2);
            assertEquals(value2, interval.getValue());

            interval = ss.querySingleState(25, subAttribute2);
            assertNull(interval.getValue()); // Stack depth is 1 at that point.

        } catch (AttributeNotFoundException | TimeRangeException | StateSystemDisposedException e) {
            fail(errMsg + e.toString());
        }
    }

    /**
     * Test the .querySingletStackTop() convenience method.
     */
    @Test
    public void testStackTop() {
        final ITmfStateSystemBuilder ss2 = ss;
        assertNotNull(ss2);

        try {
            ITmfStateInterval interval = StateSystemUtils.querySingleStackTop(ss2, 10, attribute);
            assertNotNull(interval);
            assertEquals(value5, interval.getValue());

            interval = StateSystemUtils.querySingleStackTop(ss2, 9, attribute);
            assertNotNull(interval);
            assertEquals(value4, interval.getValue());

            interval = StateSystemUtils.querySingleStackTop(ss2, 13, attribute);
            assertNotNull(interval);
            assertEquals(value3, interval.getValue());

            interval = StateSystemUtils.querySingleStackTop(ss2, 16, attribute);
            assertNotNull(interval);
            assertEquals(value1, interval.getValue());

            interval = StateSystemUtils.querySingleStackTop(ss2, 25, attribute);
            assertNotNull(interval);
            assertEquals(value1, interval.getValue());

        } catch (AttributeNotFoundException | TimeRangeException | StateSystemDisposedException e) {
            fail(errMsg + e.toString());
        }
    }

    /**
     * Test the places where the stack is empty.
     */
    @Test
    public void testEmptyStack() {
        final ITmfStateSystemBuilder ss2 = ss;
        assertNotNull(ss2);

        try {
            /* At the start */
            ITmfStateInterval interval = ss.querySingleState(1, attribute);
            assertTrue(interval.getStateValue().isNull());
            interval = StateSystemUtils.querySingleStackTop(ss2, 1, attribute);
            assertEquals(null, interval);

            /* Between the two "stacks" in the state history */
            interval = ss.querySingleState(19, attribute);
            assertTrue(interval.getStateValue().isNull());
            interval = StateSystemUtils.querySingleStackTop(ss2, 19, attribute);
            assertEquals(null, interval);

            /* At the end */
            interval = ss.querySingleState(27, attribute);
            assertTrue(interval.getStateValue().isNull());
            interval = StateSystemUtils.querySingleStackTop(ss2, 27, attribute);
            assertEquals(null, interval);

        } catch (AttributeNotFoundException | TimeRangeException | StateSystemDisposedException e) {
            fail(errMsg + e.toString());
        }
    }

    /**
     * Test full-queries (.queryFullState()) on the attribute stacks.
     */
    @Test
    public void testFullQueries() {
        List<ITmfStateInterval> state;
        try {
            final int subAttrib1 = ss.getQuarkRelative(attribute, "1");
            final int subAttrib2 = ss.getQuarkRelative(attribute, "2");
            final int subAttrib3 = ss.getQuarkRelative(attribute, "3");
            final int subAttrib4 = ss.getQuarkRelative(attribute, "4");

            /* Stack depth = 5 */
            state = ss.queryFullState(10);
            assertEquals(5, state.get(attribute).getStateValue().unboxInt());
            assertEquals(value1, state.get(subAttrib1).getValue());
            assertEquals(value2, state.get(subAttrib2).getValue());
            assertEquals(value3, state.get(subAttrib3).getValue());
            assertEquals(value4, state.get(subAttrib4).getValue());

            /* Stack is empty */
            state = ss.queryFullState(18);
            assertTrue(state.get(attribute).getStateValue().isNull());
            assertTrue(state.get(subAttrib1).getStateValue().isNull());
            assertTrue(state.get(subAttrib2).getStateValue().isNull());
            assertTrue(state.get(subAttrib3).getStateValue().isNull());
            assertTrue(state.get(subAttrib4).getStateValue().isNull());

            /* Stack depth = 1 */
            state = ss.queryFullState(21);
            assertEquals(1, state.get(attribute).getStateValue().unboxInt());
            assertEquals(value1, state.get(subAttrib1).getValue());
            assertNull(state.get(subAttrib2).getValue());
            assertNull(state.get(subAttrib3).getValue());
            assertNull(state.get(subAttrib4).getValue());

        } catch (AttributeNotFoundException | TimeRangeException | StateSystemDisposedException e) {
            fail(errMsg + e.toString());
        }
    }
}
