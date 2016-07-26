/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bruno Roy- Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.lttng2.control.core.tests.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseLoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceJulLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.BaseLoggerInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>BaseLoggerInfoTest</code> contains test for the class
 * <code>{@link BaseLoggerInfo}</code>.
 */
public class BaseLoggerInfoTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private IBaseLoggerInfo fBaseLoggerInfo1 = null;
    private IBaseLoggerInfo fBaseLoggerInfo2 = null;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------
    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        ModelImplFactory factory = new ModelImplFactory();
        fBaseLoggerInfo1 = factory.getBaseLoggerInfo1();
        fBaseLoggerInfo2 = factory.getBaseLoggerInfo2();
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    /**
     * Run the BaseLoggerInfo() constructor test.
     */
    @Test
    public void testBaseLoggerInfo() {
        BaseLoggerInfo fixture = new BaseLoggerInfo("logger");
        assertNotNull(fixture);

        // Name
        String name = fixture.getName();
        assertEquals("logger", name);

        // Domain
        TraceDomainType domain = fixture.getDomain();
        assertEquals("Unknown domain type", domain.getInName());
        assertEquals("UNKNOWN", domain.name());
        assertEquals("UNKNOWN", domain.toString());
        assertEquals(5, domain.ordinal());

        // Log level
        TraceJulLogLevel logLevel = (TraceJulLogLevel) fixture.getLogLevel();
        assertEquals("LEVEL_UNKNOWN", logLevel.getInName());
        assertEquals("LEVEL_UNKNOWN", logLevel.name());
        assertEquals("LEVEL_UNKNOWN", logLevel.toString());
        assertEquals(9, logLevel.ordinal());
    }

    /**
     * Test Copy Constructor
     */
    @Test
    public void testBaseLoggerInfoCopy() {
        BaseLoggerInfo fixture = new BaseLoggerInfo((BaseLoggerInfo) fBaseLoggerInfo1);

        assertEquals(fBaseLoggerInfo1.getName(), fixture.getName());
        assertEquals(fBaseLoggerInfo1.getDomain(), fBaseLoggerInfo1.getDomain());
        assertEquals(fBaseLoggerInfo1.getLogLevel(), fixture.getLogLevel());
    }

    /**
     * Test Copy Constructor
     */
    @Test
    public void testBaseLoggerInfoCopy2() {
        try {
            BaseLoggerInfo info = null;
            new BaseLoggerInfo(info);
            fail("null copy");
        }
        catch (IllegalArgumentException e) {
            // Success
        }
    }

    /**
     * Run the void setLogLevel(TraceJulLogLevel) method test.
     * Run the TraceJulLogLevel getLogLevel() method test
     */
    @Test
    public void testSetLogLevel1() {
        BaseLoggerInfo fixture = new BaseLoggerInfo("logger");

        // Case 1 : JUL_ALL
        fixture.setLogLevel(TraceJulLogLevel.JUL_ALL);
        TraceJulLogLevel result = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("All", result.getInName());
        assertEquals("JUL_ALL", result.name());
        assertEquals("JUL_ALL", result.toString());
        assertEquals(8, result.ordinal());

        // Case 2 : JUL_WARNING
        fixture.setLogLevel(TraceJulLogLevel.JUL_WARNING);
        result = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("Warning", result.getInName());
        assertEquals("JUL_WARNING", result.name());
        assertEquals("JUL_WARNING", result.toString());
        assertEquals(2, result.ordinal());
    }

    /**
     * Run the void setLogLevel(String) method test.
     * Run the TraceJulLogLevel getLogLevel() method test
     */
    @Test
    public void testSetLogLevel2() {
        BaseLoggerInfo fixture = new BaseLoggerInfo("logger");

        fixture.setLogLevel("Off");

        TraceJulLogLevel result = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("Off", result.getInName());
        assertEquals("JUL_OFF", result.name());
        assertEquals(0, result.ordinal());

        //------------------------
        fixture.setLogLevel("Severe");

        result = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("Severe", result.getInName());
        assertEquals("JUL_SEVERE", result.name());
        assertEquals(1, result.ordinal());

        //------------------------
        fixture.setLogLevel("Warning");

        result = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("Warning", result.getInName());
        assertEquals("JUL_WARNING", result.name());
        assertEquals(2, result.ordinal());

        //------------------------
        fixture.setLogLevel("Info");

        result = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("Info", result.getInName());
        assertEquals("JUL_INFO", result.name());
        assertEquals(3, result.ordinal());

        //------------------------
        fixture.setLogLevel("Config");

        result = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("Config", result.getInName());
        assertEquals("JUL_CONFIG", result.name());
        assertEquals(4, result.ordinal());

        //------------------------
        fixture.setLogLevel("Fine");

        result = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("Fine", result.getInName());
        assertEquals("JUL_FINE", result.name());
        assertEquals(5, result.ordinal());

        //------------------------
        fixture.setLogLevel("Finer");

        result = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("Finer", result.getInName());
        assertEquals("JUL_FINER", result.name());
        assertEquals(6, result.ordinal());

        //------------------------
        fixture.setLogLevel("Finest");

        result = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("Finest", result.getInName());
        assertEquals("JUL_FINEST", result.name());
        assertEquals(7, result.ordinal());

        //------------------------
        fixture.setLogLevel("All");

        result = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("All", result.getInName());
        assertEquals("JUL_ALL", result.name());
        assertEquals(8, result.ordinal());

        //------------------------
        fixture.setLogLevel("LEVEL_UNKNOWN");

        result = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(result);
        assertEquals("LEVEL_UNKNOWN", result.getInName());
        assertEquals("LEVEL_UNKNOWN", result.name());
        assertEquals(9, result.ordinal());
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_1() {
        BaseLoggerInfo fixture = new BaseLoggerInfo("logger");
        fixture.setName("testName");
        fixture.setLogLevel(TraceJulLogLevel.JUL_ALL);

        fixture.setDomain(TraceDomainType.JUL);
        String result = fixture.toString();
        assertEquals("[BaseLoggerInfo([TraceInfo(Name=testName)],domain=JUL,level=JUL_ALL)]", result);

        fixture.setDomain(TraceDomainType.LOG4J);
        result = fixture.toString();
        assertEquals("[BaseLoggerInfo([TraceInfo(Name=testName)],domain=LOG4J,level=JUL_ALL)]", result);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    /**
     * Test the .equals() method.
     */
    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fBaseLoggerInfo1.equals(fBaseLoggerInfo1));
        assertTrue("equals", fBaseLoggerInfo2.equals(fBaseLoggerInfo2));

        assertTrue("equals", !fBaseLoggerInfo1.equals(fBaseLoggerInfo2));
        assertTrue("equals", !fBaseLoggerInfo2.equals(fBaseLoggerInfo1));
    }

    /**
     * Test the .equals() method.
     */
    @Test
    public void testEqualsSymmetry() {
        BaseLoggerInfo info1 = new BaseLoggerInfo((BaseLoggerInfo) fBaseLoggerInfo1);
        BaseLoggerInfo info2 = new BaseLoggerInfo((BaseLoggerInfo) fBaseLoggerInfo2);

        assertTrue("equals", info1.equals(fBaseLoggerInfo1));
        assertTrue("equals", fBaseLoggerInfo1.equals(info1));

        assertTrue("equals", info2.equals(fBaseLoggerInfo2));
        assertTrue("equals", fBaseLoggerInfo2.equals(info2));
    }

    /**
     * Test the .equals() method.
     */
    @Test
    public void testEqualsTransivity() {
        BaseLoggerInfo info1 = new BaseLoggerInfo((BaseLoggerInfo) fBaseLoggerInfo1);
        BaseLoggerInfo info2 = new BaseLoggerInfo((BaseLoggerInfo) fBaseLoggerInfo1);
        BaseLoggerInfo info3 = new BaseLoggerInfo((BaseLoggerInfo) fBaseLoggerInfo1);

        assertTrue("equals", info1.equals(info2));
        assertTrue("equals", info2.equals(info3));
        assertTrue("equals", info1.equals(info3));
    }

    /**
     * Test the .equals() method.
     */
    @Test
    public void testEqualsNull() {
        assertTrue("equals", !fBaseLoggerInfo1.equals(null));
        assertTrue("equals", !fBaseLoggerInfo2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    /**
     * Test the hashCode() method.
     */
    @Test
    public void testHashCode() {
        BaseLoggerInfo info1 = new BaseLoggerInfo((BaseLoggerInfo) fBaseLoggerInfo1);
        BaseLoggerInfo info2 = new BaseLoggerInfo((BaseLoggerInfo) fBaseLoggerInfo2);

        assertTrue("hashCode", fBaseLoggerInfo1.hashCode() == info1.hashCode());
        assertTrue("hashCode", fBaseLoggerInfo2.hashCode() == info2.hashCode());

        assertTrue("hashCode", fBaseLoggerInfo1.hashCode() != info2.hashCode());
        assertTrue("hashCode", fBaseLoggerInfo2.hashCode() != info1.hashCode());
    }
}
