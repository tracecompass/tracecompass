/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng.ui.tests.control.model.impl;

import junit.framework.TestCase;

import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.IProbeEventInfo;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.BaseEventInfo;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.EventInfo;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.model.impl.ProbeEventInfo;

/**
 * The class <code>BaseEventInfoTest</code> contains test for the class <code>{@link BaseEventInfo}</code>.
 */
@SuppressWarnings("nls")
public class ProbeEventInfoTest extends TestCase {

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
     *
     * @throws Exception if the initialization fails for some reason
     *
     */
    @Override
    public void setUp() throws Exception {
        ModelImplFactory factory = new ModelImplFactory();
        fEventInfo1 = factory.getProbeEventInfo1();
        fEventInfo2 = factory.getProbeEventInfo2();
    }

    /**
     * Perform post-test clean-up.
     *
     * @throws Exception if the clean-up fails for some reason
     *
     */
    @Override
    public void tearDown() throws Exception {
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    /**
     * Run the BaseEventInfo() constructor test.
     *
     */
    public void testBaseEventInfo() {
        ProbeEventInfo fixture = new ProbeEventInfo("event");
        assertNotNull(fixture);
        
        TraceEventType result = fixture.getEventType();
        
        assertEquals("event", fixture.getName());
        assertEquals("unknown", result.getInName());
        assertEquals("UNKNOWN", result.name());
        assertEquals("UNKNOWN", result.toString());
        assertEquals(3, result.ordinal());
        
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
    public void testGetAndSetter() {
        ProbeEventInfo fixture = new ProbeEventInfo("event");
        
        // Make sure that ProbeEventInfo extends EventInfo
        // -> so we don't need to re-test common parts
        assertTrue(fixture instanceof EventInfo);
        
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
    public void testToString_1() {
        assertEquals("[ProbeEventInfo([EventInfo([BaseEventInfo([TraceInfo(Name=probeEvent1)],type=TRACEPOINT,level=TRACE_DEBUG)],State=ENABLED)],fAddress=0xc1231234)]", fEventInfo1.toString());
        assertEquals("[ProbeEventInfo([EventInfo([BaseEventInfo([TraceInfo(Name=probeEvent2)],type=UNKNOWN,level=TRACE_DEBUG)],State=DISABLED)],fOffset=0x100,fSymbol=init_post)]", fEventInfo2.toString());
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------
    public void testEqualsReflexivity() {
        assertTrue("equals", fEventInfo1.equals(fEventInfo1));
        assertTrue("equals", fEventInfo2.equals(fEventInfo2));

        assertTrue("equals", !fEventInfo1.equals(fEventInfo2));
        assertTrue("equals", !fEventInfo2.equals(fEventInfo1));
    }
    
    public void testEqualsSymmetry() {
        ProbeEventInfo info1 = new ProbeEventInfo((ProbeEventInfo)fEventInfo1);
        ProbeEventInfo info2 = new ProbeEventInfo((ProbeEventInfo)fEventInfo2);

        assertTrue("equals", info1.equals(fEventInfo1));
        assertTrue("equals", fEventInfo1.equals(info1));

        assertTrue("equals", info2.equals(fEventInfo2));
        assertTrue("equals", fEventInfo2.equals(info2));
    }
    
    public void testEqualsTransivity() {
        ProbeEventInfo info1 = new ProbeEventInfo((ProbeEventInfo)fEventInfo1);
        ProbeEventInfo info2 = new ProbeEventInfo((ProbeEventInfo)fEventInfo1);
        ProbeEventInfo info3 = new ProbeEventInfo((ProbeEventInfo)fEventInfo1);

        assertTrue("equals", info1.equals(info2));
        assertTrue("equals", info2.equals(info3));
        assertTrue("equals", info1.equals(info3));
    }
    
    public void testEqualsNull() {
        assertTrue("equals", !fEventInfo1.equals(null));
        assertTrue("equals", !fEventInfo2.equals(null));
    }
    
    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    public void testHashCode() {
        ProbeEventInfo info1 = new ProbeEventInfo((ProbeEventInfo)fEventInfo1);
        ProbeEventInfo info2 = new ProbeEventInfo((ProbeEventInfo)fEventInfo2);

        assertTrue("hashCode", fEventInfo1.hashCode() == info1.hashCode());
        assertTrue("hashCode", fEventInfo2.hashCode() == info2.hashCode());

        assertTrue("hashCode", fEventInfo1.hashCode() != info2.hashCode());
        assertTrue("hashCode", fEventInfo2.hashCode() != info1.hashCode());
    }
}