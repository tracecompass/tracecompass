/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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

package org.eclipse.linuxtools.tmf.core.tests.statesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.StateSystem;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree.HistoryTreeBackend;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
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

    private ITmfStateInterval interval;
    private int attribute;

    private File testHtFile;

    private final static String errMsg = "Caught exception: ";

    /* State values that will be used */
    //private final static ITmfStateValue nullValue = TmfStateValue.nullValue();
    private final static ITmfStateValue value1 = TmfStateValue.newValueString("A");
    private final static ITmfStateValue value2 = TmfStateValue.newValueInt(10);
    private final static ITmfStateValue value3 = TmfStateValue.nullValue();
    private final static ITmfStateValue value4 = TmfStateValue.newValueString("D");
    private final static ITmfStateValue value5 = TmfStateValue.newValueLong(Long.MAX_VALUE);

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
        ITmfStateValue value;
        testHtFile = File.createTempFile("test", ".ht");

        IStateHistoryBackend backend = new HistoryTreeBackend(testHtFile, 0, 0L);
        ss = new StateSystem("push-pop-test", backend, true);

        /* Build the thing */
        final int attrib = ss.getQuarkAbsoluteAndAdd("Test", "stack");

        ss.pushAttribute( 2, value1, attrib);
        ss.pushAttribute( 4, value2, attrib);
        ss.pushAttribute( 6, value3, attrib);
        ss.pushAttribute( 8, value4, attrib);
        ss.pushAttribute(10, value5, attrib);

        value = ss.popAttribute(11, attrib);
        assertEquals(value5, value);

        value = ss.popAttribute(12, attrib);
        assertEquals(value4, value);

        value = ss.popAttribute(14, attrib);
        assertEquals(value3, value);

        value = ss.popAttribute(16, attrib);
        assertEquals(value2, value);

        value = ss.popAttribute(17, attrib);
        assertEquals(value1, value);

        value = ss.popAttribute(20, attrib);
        assertEquals(null, value); // Stack should already be empty here.

        ss.pushAttribute(21, value1, attrib);
        //ss.pushAttribute(22, value1, attrib); //FIXME pushing twice the same value bugs out atm
        ss.pushAttribute(22, value2, attrib);

        value = ss.popAttribute(24, attrib);
        //assertEquals(value1, value);
        assertEquals(value2, value);

        value = ss.popAttribute(26, attrib);
        assertEquals(value1, value);

        value = ss.popAttribute(28, attrib);
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
            interval = ss.querySingleState(0, attribute);
            assertEquals(0, interval.getStartTime());
            assertEquals(1, interval.getEndTime());
            assertTrue(interval.getStateValue().isNull());

            interval = ss.querySingleState(29, attribute);
            assertEquals(26, interval.getStartTime());
            assertEquals(30, interval.getEndTime());
            assertTrue(interval.getStateValue().isNull());

        } catch (AttributeNotFoundException e) {
            fail(errMsg + e.toString());
        } catch (TimeRangeException e) {
            fail(errMsg + e.toString());
        } catch (StateSystemDisposedException e) {
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
            interval = ss.querySingleState(11, attribute);
            assertEquals(4, interval.getStateValue().unboxInt());

            interval = ss.querySingleState(24, attribute);
            assertEquals(1, interval.getStateValue().unboxInt());

            /* Go retrieve the user values manually */
            interval = ss.querySingleState(10, subAttribute1);
            assertEquals(value1, interval.getStateValue()); //

            interval = ss.querySingleState(22, subAttribute2);
            assertEquals(value2, interval.getStateValue());

            interval = ss.querySingleState(25, subAttribute2);
            assertTrue(interval.getStateValue().isNull()); // Stack depth is 1 at that point.

        } catch (AttributeNotFoundException e) {
            fail(errMsg + e.toString());
        } catch (StateValueTypeException e) {
            fail(errMsg + e.toString());
        } catch (TimeRangeException e) {
            fail(errMsg + e.toString());
        } catch (StateSystemDisposedException e) {
            fail(errMsg + e.toString());
        }
    }

    /**
     * Test the .querySingletStackTop() convenience method.
     */
    @Test
    public void testStackTop() {
        try {
            interval = ss.querySingleStackTop(10, attribute);
            assertEquals(value5, interval.getStateValue());

            interval = ss.querySingleStackTop(9, attribute);
            assertEquals(value4, interval.getStateValue());

            interval = ss.querySingleStackTop(13, attribute);
            assertEquals(value3, interval.getStateValue());

            interval = ss.querySingleStackTop(16, attribute);
            assertEquals(value1, interval.getStateValue());

            interval = ss.querySingleStackTop(25, attribute);
            assertEquals(value1, interval.getStateValue());

        } catch (AttributeNotFoundException e) {
            fail(errMsg + e.toString());
        } catch (StateValueTypeException e) {
            fail(errMsg + e.toString());
        } catch (TimeRangeException e) {
            fail(errMsg + e.toString());
        } catch (StateSystemDisposedException e) {
            fail(errMsg + e.toString());
        }
    }

    /**
     * Test the places where the stack is empty.
     */
    @Test
    public void testEmptyStack() {
        try {
            /* At the start */
            interval = ss.querySingleState(1, attribute);
            assertTrue(interval.getStateValue().isNull());
            interval = ss.querySingleStackTop(1, attribute);
            assertEquals(null, interval);

            /* Between the two "stacks" in the state history */
            interval = ss.querySingleState(19, attribute);
            assertTrue(interval.getStateValue().isNull());
            interval = ss.querySingleStackTop(19, attribute);
            assertEquals(null, interval);

            /* At the end */
            interval = ss.querySingleState(27, attribute);
            assertTrue(interval.getStateValue().isNull());
            interval = ss.querySingleStackTop(27, attribute);
            assertEquals(null, interval);

        } catch (AttributeNotFoundException e) {
            fail(errMsg + e.toString());
        } catch (StateValueTypeException e) {
            fail(errMsg + e.toString());
        } catch (TimeRangeException e) {
            fail(errMsg + e.toString());
        } catch (StateSystemDisposedException e) {
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
            assertEquals(value1, state.get(subAttrib1).getStateValue());
            assertEquals(value2, state.get(subAttrib2).getStateValue());
            assertEquals(value3, state.get(subAttrib3).getStateValue());
            assertEquals(value4, state.get(subAttrib4).getStateValue());

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
            assertEquals(value1, state.get(subAttrib1).getStateValue());
            assertTrue(state.get(subAttrib2).getStateValue().isNull());
            assertTrue(state.get(subAttrib3).getStateValue().isNull());
            assertTrue(state.get(subAttrib4).getStateValue().isNull());

        } catch (AttributeNotFoundException e) {
            fail(errMsg + e.toString());
        } catch (StateValueTypeException e) {
            fail(errMsg + e.toString());
        } catch (TimeRangeException e) {
            fail(errMsg + e.toString());
        } catch (StateSystemDisposedException e) {
            fail(errMsg + e.toString());
        }
    }
}
