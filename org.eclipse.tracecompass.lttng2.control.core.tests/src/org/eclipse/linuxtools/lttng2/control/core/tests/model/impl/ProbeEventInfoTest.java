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

import static org.junit.Assert.*;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.IProbeEventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.ProbeEventInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>ProbEventInfoTest</code> contains test for the class
 * <code>{@link ProbeEventInfo}</code>.
 */
public class ProbeEventInfoTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private IProbeEventInfo fEventInfo1 = null;
    private IProbeEventInfo fEventInfo2 = null;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------
    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        ModelImplFactory factory = new ModelImplFactory();
        fEventInfo1 = factory.getProbeEventInfo1();
        fEventInfo2 = factory.getProbeEventInfo2();
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    /**
     * Run the BaseEventInfo() constructor test.
     */
    @Test
    public void testBaseEventInfo() {
        ProbeEventInfo fixture = new ProbeEventInfo("event");
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

        assertNull(fixture.getAddress());
        assertNull(fixture.getOffset());
        assertNull(fixture.getSymbol());
    }

    /**
     * Test Copy Constructor
     */
    @Test
    public void testEventInfoCopy() {
        ProbeEventInfo info = new ProbeEventInfo((ProbeEventInfo)fEventInfo1);

        assertEquals(fEventInfo1.getName(), info.getName());
        assertEquals(fEventInfo1.getEventType(), info.getEventType());
        assertEquals(fEventInfo1.getState(), info.getState());
        assertEquals(fEventInfo1.getAddress(), info.getAddress());
        assertEquals(fEventInfo1.getOffset(), info.getOffset());
        assertEquals(fEventInfo1.getSymbol(), info.getSymbol());
    }

    /**
     * Test Copy Constructor
     */
    @Test
    public void testEventCopy2() {
        try {
            ProbeEventInfo info = null;
            new ProbeEventInfo(info);
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
        ProbeEventInfo fixture = new ProbeEventInfo("event");

        fixture.setAddress("0xc12344321");
        String result = fixture.getAddress();

        assertNotNull(result);
        assertEquals("0xc12344321", result);

        fixture.setOffset("0x1000");
        result = fixture.getOffset();

        assertNotNull(result);
        assertEquals("0x1000", result);

        fixture.setSymbol("cpu_idle");
        result = fixture.getSymbol();

        assertNotNull(result);
        assertEquals("cpu_idle", result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_1() {
        assertEquals("[ProbeEventInfo([EventInfo([BaseEventInfo([TraceInfo(Name=probeEvent1)],type=TRACEPOINT,level=TRACE_DEBUG)],State=ENABLED,levelType=LOGLEVEL_NONE)],fAddress=0xc1231234)]", fEventInfo1.toString());
        assertEquals("[ProbeEventInfo([EventInfo([BaseEventInfo([TraceInfo(Name=probeEvent2)],type=FUNCTION,level=TRACE_DEBUG)],State=DISABLED,levelType=LOGLEVEL_NONE)],fOffset=0x100,fSymbol=init_post)]", fEventInfo2.toString());
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
        ProbeEventInfo info1 = new ProbeEventInfo((ProbeEventInfo)fEventInfo1);
        ProbeEventInfo info2 = new ProbeEventInfo((ProbeEventInfo)fEventInfo2);

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
        ProbeEventInfo info1 = new ProbeEventInfo((ProbeEventInfo)fEventInfo1);
        ProbeEventInfo info2 = new ProbeEventInfo((ProbeEventInfo)fEventInfo1);
        ProbeEventInfo info3 = new ProbeEventInfo((ProbeEventInfo)fEventInfo1);

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
        ProbeEventInfo info1 = new ProbeEventInfo((ProbeEventInfo)fEventInfo1);
        ProbeEventInfo info2 = new ProbeEventInfo((ProbeEventInfo)fEventInfo2);

        assertTrue("hashCode", fEventInfo1.hashCode() == info1.hashCode());
        assertTrue("hashCode", fEventInfo2.hashCode() == info2.hashCode());

        assertTrue("hashCode", fEventInfo1.hashCode() != info2.hashCode());
        assertTrue("hashCode", fEventInfo2.hashCode() != info1.hashCode());
    }
}