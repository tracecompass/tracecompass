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
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLog4jLogLevel;
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
        TraceJulLogLevel resultJul = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(resultJul);
        assertEquals("All", resultJul.getInName());
        assertEquals("JUL_ALL", resultJul.name());
        assertEquals("JUL_ALL", resultJul.toString());
        assertEquals(8, resultJul.ordinal());

        // Case 2 : JUL_WARNING
        fixture.setLogLevel(TraceJulLogLevel.JUL_WARNING);
        resultJul = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(resultJul);
        assertEquals("Warning", resultJul.getInName());
        assertEquals("JUL_WARNING", resultJul.name());
        assertEquals("JUL_WARNING", resultJul.toString());
        assertEquals(2, resultJul.ordinal());

        // Case 3 : LOG4J_ALL
        fixture.setDomain(TraceDomainType.LOG4J);
        fixture.setLogLevel(TraceLog4jLogLevel.LOG4J_ALL);
        TraceLog4jLogLevel resultLog4j = (TraceLog4jLogLevel) fixture.getLogLevel();
        assertNotNull(resultLog4j);
        assertEquals("All", resultLog4j.getInName());
        assertEquals("LOG4J_ALL", resultLog4j.name());
        assertEquals("LOG4J_ALL", resultLog4j.toString());
        assertEquals(7, resultLog4j.ordinal());

        // Case 4 : LOG4J_FATAL
        fixture.setLogLevel(TraceLog4jLogLevel.LOG4J_FATAL);
        resultLog4j = (TraceLog4jLogLevel) fixture.getLogLevel();
        assertNotNull(resultLog4j);
        assertEquals("Fatal", resultLog4j.getInName());
        assertEquals("LOG4J_FATAL", resultLog4j.name());
        assertEquals("LOG4J_FATAL", resultLog4j.toString());
        assertEquals(1, resultLog4j.ordinal());
    }

    /**
     * Run the void setLogLevel(String) method test.
     * Run the TraceJulLogLevel getLogLevel() method test
     */
    @Test
    public void testSetLogLevel2() {
        BaseLoggerInfo fixture = new BaseLoggerInfo("logger");

        // Case 1: JUL log level
        fixture.setDomain(TraceDomainType.JUL);
        fixture.setLogLevel("Off");

        TraceJulLogLevel resultJul = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(resultJul);
        assertEquals("Off", resultJul.getInName());
        assertEquals("JUL_OFF", resultJul.name());
        assertEquals(0, resultJul.ordinal());

        //------------------------
        fixture.setLogLevel("Severe");

        resultJul = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(resultJul);
        assertEquals("Severe", resultJul.getInName());
        assertEquals("JUL_SEVERE", resultJul.name());
        assertEquals(1, resultJul.ordinal());

        //------------------------
        fixture.setLogLevel("Warning");

        resultJul = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(resultJul);
        assertEquals("Warning", resultJul.getInName());
        assertEquals("JUL_WARNING", resultJul.name());
        assertEquals(2, resultJul.ordinal());

        //------------------------
        fixture.setLogLevel("Info");

        resultJul = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(resultJul);
        assertEquals("Info", resultJul.getInName());
        assertEquals("JUL_INFO", resultJul.name());
        assertEquals(3, resultJul.ordinal());

        //------------------------
        fixture.setLogLevel("Config");

        resultJul = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(resultJul);
        assertEquals("Config", resultJul.getInName());
        assertEquals("JUL_CONFIG", resultJul.name());
        assertEquals(4, resultJul.ordinal());

        //------------------------
        fixture.setLogLevel("Fine");

        resultJul = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(resultJul);
        assertEquals("Fine", resultJul.getInName());
        assertEquals("JUL_FINE", resultJul.name());
        assertEquals(5, resultJul.ordinal());

        //------------------------
        fixture.setLogLevel("Finer");

        resultJul = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(resultJul);
        assertEquals("Finer", resultJul.getInName());
        assertEquals("JUL_FINER", resultJul.name());
        assertEquals(6, resultJul.ordinal());

        //------------------------
        fixture.setLogLevel("Finest");

        resultJul = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(resultJul);
        assertEquals("Finest", resultJul.getInName());
        assertEquals("JUL_FINEST", resultJul.name());
        assertEquals(7, resultJul.ordinal());

        //------------------------
        fixture.setLogLevel("All");

        resultJul = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(resultJul);
        assertEquals("All", resultJul.getInName());
        assertEquals("JUL_ALL", resultJul.name());
        assertEquals(8, resultJul.ordinal());

        //------------------------
        fixture.setLogLevel("LEVEL_UNKNOWN");

        resultJul = (TraceJulLogLevel) fixture.getLogLevel();
        assertNotNull(resultJul);
        assertEquals("LEVEL_UNKNOWN", resultJul.getInName());
        assertEquals("LEVEL_UNKNOWN", resultJul.name());
        assertEquals(9, resultJul.ordinal());

        // Case 2: LOG4J log level
        fixture.setDomain(TraceDomainType.LOG4J);

        fixture.setLogLevel("Off");

        TraceLog4jLogLevel resultLog4j = (TraceLog4jLogLevel) fixture.getLogLevel();
        assertNotNull(resultLog4j);
        assertEquals("Off", resultLog4j.getInName());
        assertEquals("LOG4J_OFF", resultLog4j.name());
        assertEquals(0, resultLog4j.ordinal());

        //------------------------
        fixture.setLogLevel("Fatal");

        resultLog4j = (TraceLog4jLogLevel) fixture.getLogLevel();
        assertNotNull(resultLog4j);
        assertEquals("Fatal", resultLog4j.getInName());
        assertEquals("LOG4J_FATAL", resultLog4j.name());
        assertEquals(1, resultLog4j.ordinal());

        //------------------------
        fixture.setLogLevel("Error");

        resultLog4j = (TraceLog4jLogLevel) fixture.getLogLevel();
        assertNotNull(resultLog4j);
        assertEquals("Error", resultLog4j.getInName());
        assertEquals("LOG4J_ERROR", resultLog4j.name());
        assertEquals(2, resultLog4j.ordinal());

        //------------------------
        fixture.setLogLevel("Warn");

        resultLog4j = (TraceLog4jLogLevel) fixture.getLogLevel();
        assertNotNull(resultLog4j);
        assertEquals("Warn", resultLog4j.getInName());
        assertEquals("LOG4J_WARN", resultLog4j.name());
        assertEquals(3, resultLog4j.ordinal());

        //------------------------
        fixture.setLogLevel("Info");

        resultLog4j = (TraceLog4jLogLevel) fixture.getLogLevel();
        assertNotNull(resultLog4j);
        assertEquals("Info", resultLog4j.getInName());
        assertEquals("LOG4J_INFO", resultLog4j.name());
        assertEquals(4, resultLog4j.ordinal());

        //------------------------
        fixture.setLogLevel("Debug");

        resultLog4j = (TraceLog4jLogLevel) fixture.getLogLevel();
        assertNotNull(resultLog4j);
        assertEquals("Debug", resultLog4j.getInName());
        assertEquals("LOG4J_DEBUG", resultLog4j.name());
        assertEquals(5, resultLog4j.ordinal());

        //------------------------
        fixture.setLogLevel("Trace");

        resultLog4j = (TraceLog4jLogLevel) fixture.getLogLevel();
        assertNotNull(resultLog4j);
        assertEquals("Trace", resultLog4j.getInName());
        assertEquals("LOG4J_TRACE", resultLog4j.name());
        assertEquals(6, resultLog4j.ordinal());

        //------------------------
        fixture.setLogLevel("All");

        resultLog4j = (TraceLog4jLogLevel) fixture.getLogLevel();
        assertNotNull(resultLog4j);
        assertEquals("All", resultLog4j.getInName());
        assertEquals("LOG4J_ALL", resultLog4j.name());
        assertEquals(7, resultLog4j.ordinal());

        //------------------------
        fixture.setLogLevel("LEVEL_UNKNOWN");

        resultLog4j = (TraceLog4jLogLevel) fixture.getLogLevel();
        assertNotNull(resultLog4j);
        assertEquals("LEVEL_UNKNOWN", resultLog4j.getInName());
        assertEquals("LEVEL_UNKNOWN", resultLog4j.name());
        assertEquals(8, resultLog4j.ordinal());
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_1() {
        BaseLoggerInfo fixture = new BaseLoggerInfo("logger");
        fixture.setName("testName");

        fixture.setDomain(TraceDomainType.JUL);
        fixture.setLogLevel(TraceJulLogLevel.JUL_ALL);
        String result = fixture.toString();
        assertEquals("[BaseLoggerInfo([TraceInfo(Name=testName)],domain=JUL,level=JUL_ALL)]", result);

        fixture.setDomain(TraceDomainType.LOG4J);
        fixture.setLogLevel(TraceLog4jLogLevel.LOG4J_ERROR);
        result = fixture.toString();
        assertEquals("[BaseLoggerInfo([TraceInfo(Name=testName)],domain=LOG4J,level=LOG4J_ERROR)]", result);
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
