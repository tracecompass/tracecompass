/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 **********************************************************************/

package org.eclipse.linuxtools.lttng2.control.core.tests.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.IEventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.EventInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>EventInfoTest</code> contains test for the class
 * <code>{@link EventInfo}</code>.
 */
public class EventInfoTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private IEventInfo fEventInfo1 = null;
    private IEventInfo fEventInfo2 = null;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        ModelImplFactory factory = new ModelImplFactory();
        fEventInfo1 = factory.getEventInfo1();
        fEventInfo2 = factory.getEventInfo2();
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    /**
     * Run the EventInfo() constructor test.
     */
    @Test
    public void testBaseEventInfo() {
        EventInfo fixture = new EventInfo("event");
        assertNotNull(fixture);

        TraceEventType result = fixture.getEventType();

        assertEquals("event", fixture.getName());
        assertEquals("unknown", result.getInName());
        assertEquals("UNKNOWN", result.name());
        assertEquals("UNKNOWN", result.toString());
        assertEquals(4, result.ordinal());

        TraceEnablement state = fixture.getState();
        assertEquals("disabled", state.getInName());
        assertEquals("DISABLED", state.name());
        assertEquals("DISABLED", state.toString());
        assertEquals(0, state.ordinal());

        LogLevelType logType = fixture.getLogLevelType();
        assertEquals("", logType.getShortName());
        assertEquals("LOGLEVEL_NONE", logType.name());
        assertEquals("LOGLEVEL_NONE", logType.toString());
        assertEquals(0, state.ordinal());


    }

    /**
     * Test Copy Constructor
     */
    @Test
    public void testEventInfoCopy() {
        EventInfo info = new EventInfo((EventInfo)fEventInfo1);

        assertEquals(fEventInfo1.getName(), info.getName());
        assertEquals(fEventInfo1.getEventType(), info.getEventType());
        assertEquals(fEventInfo1.getState(), info.getState());
        assertEquals(fEventInfo1.getLogLevelType(), info.getLogLevelType());
    }

    /**
     * Test Copy Constructor
     */
    @Test
    public void testEventCopy2() {
        try {
            EventInfo info = null;
            new EventInfo(info);
            fail("null copy");
        }
        catch (IllegalArgumentException e) {
            // Success
        }
    }

    /**
     *  Getter/Setter tests
     */
    @Test
    public void testGetAndSetter() {
        EventInfo fixture = new EventInfo("event");

        fixture.setEventType(TraceEventType.TRACEPOINT);
        TraceEventType result = fixture.getEventType();

        // setEventType(TraceEventType type)
        assertNotNull(result);
        assertEquals("tracepoint", result.getInName());
        assertEquals("TRACEPOINT", result.name());
        assertEquals("TRACEPOINT", result.toString());
        assertEquals(0, result.ordinal());

        fixture.setEventType(TraceEventType.UNKNOWN);
        result = fixture.getEventType();
        assertEquals("unknown", result.getInName());
        assertEquals("UNKNOWN", result.name());
        assertEquals("UNKNOWN", result.toString());
        assertEquals(4, result.ordinal());

        // setEventType(String typeName)
        String typeName = "";
        fixture.setEventType(typeName);
        result = fixture.getEventType();

        assertEquals("unknown", result.getInName());
        assertEquals("UNKNOWN", result.name());
        assertEquals("UNKNOWN", result.toString());
        assertEquals(4, result.ordinal());

        typeName = "unknown";

        fixture.setEventType(typeName);
        result = fixture.getEventType();

        assertEquals("unknown", result.getInName());
        assertEquals("UNKNOWN", result.name());
        assertEquals("UNKNOWN", result.toString());
        assertEquals(4, result.ordinal());

        // setState(String stateName)
        fixture.setState("disabled");
        TraceEnablement state = fixture.getState();
        assertEquals("disabled", state.getInName());
        assertEquals("DISABLED", state.name());
        assertEquals("DISABLED", state.toString());
        assertEquals(0, state.ordinal());

        fixture.setState("bla");
        state = fixture.getState();
        assertEquals("disabled", state.getInName());
        assertEquals("DISABLED", state.name());
        assertEquals("DISABLED", state.toString());
        assertEquals(0, state.ordinal());

        fixture.setState("enabled");
        state = fixture.getState();
        assertEquals("enabled", state.getInName());
        assertEquals("ENABLED", state.name());
        assertEquals("ENABLED", state.toString());
        assertEquals(1, state.ordinal());

        // setState(TraceEnablement state)
        fixture.setState(TraceEnablement.DISABLED);
        state = fixture.getState();
        assertEquals("disabled", state.getInName());
        assertEquals("DISABLED", state.name());
        assertEquals("DISABLED", state.toString());
        assertEquals(0, state.ordinal());

        fixture.setState(TraceEnablement.ENABLED);
        state = fixture.getState();
        assertEquals("enabled", state.getInName());
        assertEquals("ENABLED", state.name());
        assertEquals("ENABLED", state.toString());
        assertEquals(1, state.ordinal());

        // setLogLevelType(String name)
        fixture.setLogLevelType("==");
        assertEquals("LOGLEVEL_ONLY", fixture.getLogLevelType().name());
        assertEquals("==", fixture.getLogLevelType().getShortName());

        fixture.setLogLevelType("<=");
        assertEquals("LOGLEVEL", fixture.getLogLevelType().name());
        assertEquals("<=", fixture.getLogLevelType().getShortName());

        fixture.setLogLevelType("");
        assertEquals("LOGLEVEL_ALL", fixture.getLogLevelType().name());
        assertEquals("", fixture.getLogLevelType().getShortName());

        fixture.setLogLevelType(LogLevelType.LOGLEVEL_ONLY);
        assertEquals("LOGLEVEL_ONLY", fixture.getLogLevelType().name());
        assertEquals("==", fixture.getLogLevelType().getShortName());

        fixture.setLogLevelType(LogLevelType.LOGLEVEL);
        assertEquals("LOGLEVEL", fixture.getLogLevelType().name());
        assertEquals("<=", fixture.getLogLevelType().getShortName());

        fixture.setLogLevelType(LogLevelType.LOGLEVEL_ALL);
        assertEquals("LOGLEVEL_ALL", fixture.getLogLevelType().name());
        assertEquals("", fixture.getLogLevelType().getShortName());

        fixture.setLogLevelType(LogLevelType.LOGLEVEL_NONE);
        assertEquals("LOGLEVEL_NONE", fixture.getLogLevelType().name());
        assertEquals("", fixture.getLogLevelType().getShortName());

        // setLogLevelType(String name)
        // machine interface
        fixture.setLogLevelType("SINGLE");
        assertEquals("LOGLEVEL_ONLY", fixture.getLogLevelType().name());
        assertEquals("SINGLE", fixture.getLogLevelType().getMiName());

        fixture.setLogLevelType("RANGE");
        assertEquals("LOGLEVEL", fixture.getLogLevelType().name());
        assertEquals("RANGE", fixture.getLogLevelType().getMiName());

        fixture.setLogLevelType("ALL");
        assertEquals("LOGLEVEL_ALL", fixture.getLogLevelType().name());
        assertEquals("ALL", fixture.getLogLevelType().getMiName());

        fixture.setLogLevelType("UNKNOWN");
        assertEquals("LOGLEVEL_NONE", fixture.getLogLevelType().name());
        assertEquals("UNKNOWN", fixture.getLogLevelType().getMiName());

    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_1() {
        EventInfo fixture = new EventInfo("event");
        fixture.setName("testName");
        fixture.setEventType(TraceEventType.TRACEPOINT);

        String result = fixture.toString();

        // add additional test code here
        assertEquals("[EventInfo([BaseEventInfo([TraceInfo(Name=testName)],type=TRACEPOINT,level=TRACE_DEBUG)],State=DISABLED,levelType=LOGLEVEL_NONE)]", result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_2() {
        EventInfo fixture = new EventInfo("event");
        fixture.setName("testName");
        fixture.setEventType(TraceEventType.TRACEPOINT);
        fixture.setLogLevelType(LogLevelType.LOGLEVEL_ONLY);

        String result = fixture.toString();

        // add additional test code here
        assertEquals("[EventInfo([BaseEventInfo([TraceInfo(Name=testName)],type=TRACEPOINT,level=TRACE_DEBUG)],State=DISABLED,levelType=LOGLEVEL_ONLY)]", result);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fEventInfo1.equals(fEventInfo1));
        assertTrue("equals", fEventInfo2.equals(fEventInfo2));

        assertTrue("equals", !fEventInfo1.equals(fEventInfo2));
        assertTrue("equals", !fEventInfo2.equals(fEventInfo1));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsSymmetry() {
        EventInfo info1 = new EventInfo((EventInfo)fEventInfo1);
        EventInfo info2 = new EventInfo((EventInfo)fEventInfo2);

        assertTrue("equals", info1.equals(fEventInfo1));
        assertTrue("equals", fEventInfo1.equals(info1));

        assertTrue("equals", info2.equals(fEventInfo2));
        assertTrue("equals", fEventInfo2.equals(info2));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsTransivity() {
        EventInfo info1 = new EventInfo((EventInfo)fEventInfo1);
        EventInfo info2 = new EventInfo((EventInfo)fEventInfo1);
        EventInfo info3 = new EventInfo((EventInfo)fEventInfo1);

        assertTrue("equals", info1.equals(info2));
        assertTrue("equals", info2.equals(info3));
        assertTrue("equals", info1.equals(info3));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsNull() {
        assertTrue("equals", !fEventInfo1.equals(null));
        assertTrue("equals", !fEventInfo2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    /**
     * Run the hashCode() method test.
     */
    @Test
    public void testHashCode() {
        EventInfo info1 = new EventInfo((EventInfo)fEventInfo1);
        EventInfo info2 = new EventInfo((EventInfo)fEventInfo2);

        assertTrue("hashCode", fEventInfo1.hashCode() == info1.hashCode());
        assertTrue("hashCode", fEventInfo2.hashCode() == info2.hashCode());

        assertTrue("hashCode", fEventInfo1.hashCode() != info2.hashCode());
        assertTrue("hashCode", fEventInfo2.hashCode() != info1.hashCode());
    }
}