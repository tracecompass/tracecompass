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

import org.eclipse.linuxtools.lttng.ui.views.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceEventType;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceLogLevel;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.BaseEventInfo;

/**
 * The class <code>BaseEventInfoTest</code> contains test for the class <code>{@link BaseEventInfo}</code>.
 */
@SuppressWarnings("nls")
public class BaseEventInfoTest extends TestCase {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------
    private IBaseEventInfo fEventInfo1 = null;
    private IBaseEventInfo fEventInfo2 = null;
   
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
        super.setUp();
        ModelImplFactory factory = new ModelImplFactory(); 
        fEventInfo1 = factory.getBaseEventInfo1();
        fEventInfo2 = factory.getBaseEventInfo2();
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
        BaseEventInfo fixture = new BaseEventInfo("event");
        assertNotNull(fixture);
        
        TraceEventType result = fixture.getEventType();
        
        assertEquals("event", fixture.getName());
        assertEquals("unknown", result.getInName());
        assertEquals("UNKNOWN", result.name());
        assertEquals("UNKNOWN", result.toString());
        assertEquals(1, result.ordinal());
        
        TraceLogLevel level = fixture.getLogLevel();
        assertEquals("TRACE_DEBUG", level.getInName());
        assertEquals("TRACE_DEBUG", level.name());
        assertEquals("TRACE_DEBUG", level.toString());
        assertEquals(14, level.ordinal());
    }

    /**
     * Test Copy Constructor
     */
    public void testEventInfoCopy() {
        BaseEventInfo info = new BaseEventInfo((BaseEventInfo)fEventInfo1);
        
        assertEquals(fEventInfo1.getName(), info.getName());
        assertEquals(fEventInfo1.getEventType(), info.getEventType());
        assertEquals(fEventInfo1.getLogLevel(), info.getLogLevel());
    }

    /**
     * Test Copy Constructor
     */
    public void testEventCopy2() {
        try {
            BaseEventInfo info = null;
            new BaseEventInfo(info);
            fail("null copy");
        }
        catch (IllegalArgumentException e) {
            // Success
        }
    }
    
    /**
     * Run the TraceEventType getEventType() method test.
     *
     * @throws Exception
     *
     */
    public void testGetEventType_1() {
        BaseEventInfo fixture = new BaseEventInfo("event");
        fixture.setEventType("unknown");

        TraceEventType result = fixture.getEventType();

        assertNotNull(result);
        assertEquals("unknown", result.getInName());
        assertEquals("UNKNOWN", result.name());
        assertEquals("UNKNOWN", result.toString());
        assertEquals(1, result.ordinal());
        
        fixture.setEventType("");
        result = fixture.getEventType();
        assertEquals("unknown", result.getInName());
        assertEquals("UNKNOWN", result.name());
        assertEquals("UNKNOWN", result.toString());
        assertEquals(1, result.ordinal());
        
        fixture.setEventType("tracepoint");
        result = fixture.getEventType();
        assertNotNull(result);
        assertEquals("tracepoint", result.getInName());
        assertEquals("TRACEPOINT", result.name());
        assertEquals("TRACEPOINT", result.toString());
        assertEquals(0, result.ordinal());
    }

    /**
     * Run the void setEventType(TraceEventType) method test.
     *
     */
    public void testSetEventType_2() {
        BaseEventInfo fixture = new BaseEventInfo("event");
        fixture.setEventType(TraceEventType.TRACEPOINT);
        
        TraceEventType result = fixture.getEventType();
        
        assertNotNull(result);
        assertEquals("tracepoint", result.getInName());
        assertEquals("TRACEPOINT", result.name());
        assertEquals("TRACEPOINT", result.toString());
        assertEquals(0, result.ordinal());
        
        fixture.setEventType(TraceEventType.UNKNOWN);
        result = fixture.getEventType();
        
        assertNotNull(result);
        assertEquals("unknown", result.getInName());
        assertEquals("UNKNOWN", result.name());
        assertEquals("UNKNOWN", result.toString());
        assertEquals(1, result.ordinal());
    }
    
    /**
     * Run the void setLogLevel(TraceLogLevel) method test.
     * Run the TraceLogLevel getLogLevel() method test
     * 
     */
    public void testSetLogLevel1() {
        BaseEventInfo fixture = new BaseEventInfo("event");
        fixture.setEventType(TraceEventType.TRACEPOINT);
        fixture.setLogLevel(TraceLogLevel.TRACE_CRIT);
        
        // 2 set/get-operations are enough to test the method
        TraceLogLevel result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_CRIT", result.getInName());
        assertEquals("TRACE_CRIT", result.name());
        assertEquals("TRACE_CRIT", result.toString());
        assertEquals(2, result.ordinal());

        fixture.setLogLevel(TraceLogLevel.TRACE_DEBUG_FUNCTION);
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_DEBUG_FUNCTION", result.getInName());
        assertEquals("TRACE_DEBUG_FUNCTION", result.name());
        assertEquals("TRACE_DEBUG_FUNCTION", result.toString());
        assertEquals(12, result.ordinal());
    }
    
    /**
     * Run the void setLogLevel(String) method test.
     * Run the TraceLogLevel getLogLevel() method test
     * 
     */
    public void testSetLogLevel2() {
        BaseEventInfo fixture = new BaseEventInfo("event");
        fixture.setEventType(TraceEventType.TRACEPOINT);
        fixture.setLogLevel("TRACE_EMERG");
        
        TraceLogLevel result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_EMERG", result.getInName());
        assertEquals("TRACE_EMERG", result.name());
        assertEquals(0, result.ordinal());

        //------------------------
        fixture.setLogLevel("TRACE_ALERT");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_ALERT", result.getInName());
        assertEquals("TRACE_ALERT", result.name());
        assertEquals(1, result.ordinal());
        
        //------------------------
        fixture.setLogLevel("TRACE_CRIT");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_CRIT", result.getInName());
        assertEquals("TRACE_CRIT", result.name());
        assertEquals(2, result.ordinal());

        //------------------------
        fixture.setLogLevel("TRACE_ERR");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_ERR", result.getInName());
        assertEquals("TRACE_ERR", result.name());
        assertEquals(3, result.ordinal());

        //------------------------
        fixture.setLogLevel("TRACE_WARNING");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_WARNING", result.getInName());
        assertEquals("TRACE_WARNING", result.name());
        assertEquals(4, result.ordinal());

        //------------------------
        fixture.setLogLevel("TRACE_NOTICE");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_NOTICE", result.getInName());
        assertEquals("TRACE_NOTICE", result.name());
        assertEquals(5, result.ordinal());

        //------------------------
        fixture.setLogLevel("TRACE_INFO");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_INFO", result.getInName());
        assertEquals("TRACE_INFO", result.name());
        assertEquals(6, result.ordinal());

        //------------------------
        fixture.setLogLevel("TRACE_DEBUG_SYSTEM");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_DEBUG_SYSTEM", result.getInName());
        assertEquals("TRACE_DEBUG_SYSTEM", result.name());
        assertEquals(7, result.ordinal());

        //------------------------
        fixture.setLogLevel("TRACE_DEBUG_PROGRAM");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_DEBUG_PROGRAM", result.getInName());
        assertEquals("TRACE_DEBUG_PROGRAM", result.name());
        assertEquals(8, result.ordinal());

        //------------------------
        fixture.setLogLevel("TRACE_DEBUG_PROCESS");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_DEBUG_PROCESS", result.getInName());
        assertEquals("TRACE_DEBUG_PROCESS", result.name());
        assertEquals(9, result.ordinal());

        //------------------------
        fixture.setLogLevel("TRACE_DEBUG_MODULE");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_DEBUG_MODULE", result.getInName());
        assertEquals("TRACE_DEBUG_MODULE", result.name());
        assertEquals(10, result.ordinal());

        //------------------------
        fixture.setLogLevel("TRACE_DEBUG_UNIT");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_DEBUG_UNIT", result.getInName());
        assertEquals("TRACE_DEBUG_UNIT", result.name());
        assertEquals(11, result.ordinal());

        //------------------------
        fixture.setLogLevel("TRACE_DEBUG_FUNCTION");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_DEBUG_FUNCTION", result.getInName());
        assertEquals("TRACE_DEBUG_FUNCTION", result.name());
        assertEquals(12, result.ordinal());

        //------------------------
        fixture.setLogLevel("TRACE_DEBUG_LINE");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_DEBUG_LINE", result.getInName());
        assertEquals("TRACE_DEBUG_LINE", result.name());
        assertEquals(13, result.ordinal());

        //------------------------
        fixture.setLogLevel("TRACE_DEBUG");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_DEBUG", result.getInName());
        assertEquals("TRACE_DEBUG", result.name());
        assertEquals(14, result.ordinal());

        //-------------------------
        fixture.setLogLevel("LEVEL_UNKNOWN");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("LEVEL_UNKNOWN", result.getInName());
        assertEquals("LEVEL_UNKNOWN", result.name());
        assertEquals(15, result.ordinal());
        
        fixture.setLogLevel("garbage");
        
        result = fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("TRACE_DEBUG", result.getInName());
        assertEquals("TRACE_DEBUG", result.name());
        assertEquals(14, result.ordinal());
    }
    

    /**
     * Run the String toString() method test.
     *
     */
    public void testToString_1() {
        BaseEventInfo fixture = new BaseEventInfo("event");
        fixture.setName("testName");
        fixture.setEventType(TraceEventType.TRACEPOINT);
        fixture.setLogLevel(TraceLogLevel.TRACE_ERR);

        String result = fixture.toString();

        // add additional test code here
        assertEquals("[BaseEventInfo([TraceInfo(Name=testName)],type=TRACEPOINT,level=TRACE_ERR)]", result);
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
        BaseEventInfo info1 = new BaseEventInfo((BaseEventInfo)fEventInfo1);
        BaseEventInfo info2 = new BaseEventInfo((BaseEventInfo)fEventInfo2);

        assertTrue("equals", info1.equals(fEventInfo1));
        assertTrue("equals", fEventInfo1.equals(info1));

        assertTrue("equals", info2.equals(fEventInfo2));
        assertTrue("equals", fEventInfo2.equals(info2));
    }
    
    public void testEqualsTransivity() {
        BaseEventInfo info1 = new BaseEventInfo((BaseEventInfo)fEventInfo1);
        BaseEventInfo info2 = new BaseEventInfo((BaseEventInfo)fEventInfo1);
        BaseEventInfo info3 = new BaseEventInfo((BaseEventInfo)fEventInfo1);

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
        BaseEventInfo info1 = new BaseEventInfo((BaseEventInfo)fEventInfo1);
        BaseEventInfo info2 = new BaseEventInfo((BaseEventInfo)fEventInfo2);

        assertTrue("hashCode", fEventInfo1.hashCode() == info1.hashCode());
        assertTrue("hashCode", fEventInfo2.hashCode() == info2.hashCode());

        assertTrue("hashCode", fEventInfo1.hashCode() != info2.hashCode());
        assertTrue("hashCode", fEventInfo2.hashCode() != info1.hashCode());
    }
}