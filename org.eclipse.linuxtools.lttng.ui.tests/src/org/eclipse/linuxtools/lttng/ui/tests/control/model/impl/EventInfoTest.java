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

import org.eclipse.linuxtools.lttng.ui.views.control.model.IEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceEnablement;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceEventType;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.BaseEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.EventInfo;

/**
 * The class <code>BaseEventInfoTest</code> contains test for the class <code>{@link BaseEventInfo}</code>.
 */
@SuppressWarnings("nls")
public class EventInfoTest extends TestCase {

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
     *
     * @throws Exception if the initialization fails for some reason
     *
     */
    @Override
    public void setUp() throws Exception {
        ModelImplFactory factory = new ModelImplFactory();
        fEventInfo1 = factory.getEventInfo1();
        fEventInfo2 = factory.getEventInfo2();
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
        EventInfo fixture = new EventInfo("event");
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

    }

    /**
     * Test Copy Constructor
     */
    public void testEventInfoCopy() {
        EventInfo info = new EventInfo((EventInfo)fEventInfo1);
        
        assertEquals(fEventInfo1.getName(), info.getName());
        assertEquals(fEventInfo1.getEventType(), info.getEventType());
        assertEquals(fEventInfo1.getState(), info.getState());
    }

    /**
     * Test Copy Constructor
     */
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
        assertEquals(3, result.ordinal());
        
        // setEventType(String typeName)
        String typeName = "";
        fixture.setEventType(typeName);
        result = fixture.getEventType();
        
        assertEquals("unknown", result.getInName());
        assertEquals("UNKNOWN", result.name());
        assertEquals("UNKNOWN", result.toString());
        assertEquals(3, result.ordinal());

        typeName = "unknown";

        fixture.setEventType(typeName);
        result = fixture.getEventType();
        
        assertEquals("unknown", result.getInName());
        assertEquals("UNKNOWN", result.name());
        assertEquals("UNKNOWN", result.toString());
        assertEquals(3, result.ordinal());

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
    }

    /**
     * Run the String toString() method test.
     */
    public void testToString_1() {
        EventInfo fixture = new EventInfo("event");
        fixture.setName("testName");
        fixture.setEventType(TraceEventType.TRACEPOINT);

        String result = fixture.toString();

        // add additional test code here
        assertEquals("[EventInfo([BaseEventInfo([TraceInfo(Name=testName)],type=TRACEPOINT,level=TRACE_DEBUG)],State=DISABLED)]", result);
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
        EventInfo info1 = new EventInfo((EventInfo)fEventInfo1);
        EventInfo info2 = new EventInfo((EventInfo)fEventInfo2);

        assertTrue("equals", info1.equals(fEventInfo1));
        assertTrue("equals", fEventInfo1.equals(info1));

        assertTrue("equals", info2.equals(fEventInfo2));
        assertTrue("equals", fEventInfo2.equals(info2));
    }
    
    public void testEqualsTransivity() {
        EventInfo info1 = new EventInfo((EventInfo)fEventInfo1);
        EventInfo info2 = new EventInfo((EventInfo)fEventInfo1);
        EventInfo info3 = new EventInfo((EventInfo)fEventInfo1);

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
        EventInfo info1 = new EventInfo((EventInfo)fEventInfo1);
        EventInfo info2 = new EventInfo((EventInfo)fEventInfo2);

        assertTrue("hashCode", fEventInfo1.hashCode() == info1.hashCode());
        assertTrue("hashCode", fEventInfo2.hashCode() == info2.hashCode());

        assertTrue("hashCode", fEventInfo1.hashCode() != info2.hashCode());
        assertTrue("hashCode", fEventInfo2.hashCode() != info1.hashCode());
    }
}