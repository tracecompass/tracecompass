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

import org.eclipse.tracecompass.internal.lttng2.control.core.model.ILoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceJulLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLog4jLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.LoggerInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>LoggerInfoTest</code> contains test for the class
 * <code>{@link LoggerInfo}</code>.
 */
public class LoggerInfoTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private ILoggerInfo fLoggerInfo1 = null;
    private ILoggerInfo fLoggerInfo2 = null;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        ModelImplFactory factory = new ModelImplFactory();
        fLoggerInfo1 = factory.getLoggerInfo1();
        fLoggerInfo2 = factory.getLoggerInfo2();
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    /**
     * Run the LoggerInfo() constructor test.
     */
    @Test
    public void testLoggerInfo() {
        LoggerInfo fixture = new LoggerInfo("logger");
        assertNotNull(fixture);

        // Name
        String name = fixture.getName();
        assertEquals("logger", name);

        // Domain
        TraceDomainType result = fixture.getDomain();
        assertEquals("UNKNOWN", result.name());
        assertEquals("UNKNOWN", result.toString());
        assertEquals(5, result.ordinal());

        // State
        TraceEnablement state = fixture.getState();
        assertEquals("disabled", state.getInName());
        assertEquals("DISABLED", state.name());
        assertEquals("DISABLED", state.toString());
        assertEquals(0, state.ordinal());

        // Log level type
        LogLevelType logType = fixture.getLogLevelType();
        assertEquals("", logType.getShortName());
        assertEquals("LOGLEVEL_ALL", logType.name());
        assertEquals("LOGLEVEL_ALL", logType.toString());
        assertEquals(0, state.ordinal());

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
    public void testLoggerInfoCopy() {
        LoggerInfo info = new LoggerInfo((LoggerInfo) fLoggerInfo1);

        assertEquals(fLoggerInfo1.getName(), info.getName());
        assertEquals(fLoggerInfo1.getState(), info.getState());
        assertEquals(fLoggerInfo1.getLogLevelType(), info.getLogLevelType());
        assertEquals(fLoggerInfo1.getLogLevel(), info.getLogLevel());
        assertEquals(fLoggerInfo1.getDomain(), info.getDomain());
    }

    /**
     * Test Copy Constructor
     */
    @Test
    public void testEventCopy2() {
        try {
            LoggerInfo info = null;
            new LoggerInfo(info);
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
        LoggerInfo fixture = new LoggerInfo("logger");

        // setState(String stateName)
        fixture.setState("disabled");
        TraceEnablement state = fixture.getState();
        assertEquals("disabled", state.getInName());
        assertEquals("DISABLED", state.name());
        assertEquals("DISABLED", state.toString());
        assertEquals(0, state.ordinal());

        fixture.setState("true");
        state = fixture.getState();
        assertEquals("true", state.getInMiName());
        assertEquals("ENABLED", state.name());
        assertEquals("ENABLED", state.toString());
        assertEquals(1, state.ordinal());

        fixture.setState("false");
        state = fixture.getState();
        assertEquals("false", state.getInMiName());
        assertEquals("DISABLED", state.name());
        assertEquals("DISABLED", state.toString());
        assertEquals(0, state.ordinal());

        fixture.setState("enabled");
        state = fixture.getState();
        assertEquals("enabled", state.getInName());
        assertEquals("ENABLED", state.name());
        assertEquals("ENABLED", state.toString());
        assertEquals(1, state.ordinal());

        fixture.setState("bla");
        state = fixture.getState();
        assertEquals("disabled", state.getInName());
        assertEquals("DISABLED", state.name());
        assertEquals("DISABLED", state.toString());
        assertEquals(0, state.ordinal());

        // setState(TraceEnablement state)
        fixture.setState(TraceEnablement.ENABLED);
        state = fixture.getState();
        assertEquals("enabled", state.getInName());
        assertEquals("ENABLED", state.name());
        assertEquals("ENABLED", state.toString());
        assertEquals(1, state.ordinal());

        fixture.setState(TraceEnablement.DISABLED);
        state = fixture.getState();
        assertEquals("disabled", state.getInName());
        assertEquals("DISABLED", state.name());
        assertEquals("DISABLED", state.toString());
        assertEquals(0, state.ordinal());

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
    public void testToString() {
        LoggerInfo fixture = new LoggerInfo("logger");
        fixture.setName("testName");

        String result = fixture.toString();
        assertEquals("[LoggerInfo([BaseLoggerInfo([TraceInfo(Name=testName)],domain=UNKNOWN,level=LEVEL_UNKNOWN)],State=DISABLED,levelType=LOGLEVEL_ALL)]", result);

        fixture.setDomain(TraceDomainType.JUL);
        result = fixture.toString();
        assertEquals("[LoggerInfo([BaseLoggerInfo([TraceInfo(Name=testName)],domain=JUL,level=LEVEL_UNKNOWN)],State=DISABLED,levelType=LOGLEVEL_ALL)]", result);

        fixture.setLogLevel(TraceJulLogLevel.JUL_FINE);
        result = fixture.toString();
        assertEquals("[LoggerInfo([BaseLoggerInfo([TraceInfo(Name=testName)],domain=JUL,level=JUL_FINE)],State=DISABLED,levelType=LOGLEVEL_ALL)]", result);

        fixture.setState(TraceEnablement.ENABLED);
        result = fixture.toString();
        assertEquals("[LoggerInfo([BaseLoggerInfo([TraceInfo(Name=testName)],domain=JUL,level=JUL_FINE)],State=ENABLED,levelType=LOGLEVEL_ALL)]", result);

        fixture.setDomain(TraceDomainType.LOG4J);
        result = fixture.toString();
        assertEquals("[LoggerInfo([BaseLoggerInfo([TraceInfo(Name=testName)],domain=LOG4J,level=JUL_FINE)],State=ENABLED,levelType=LOGLEVEL_ALL)]", result);

        fixture.setLogLevel(TraceLog4jLogLevel.LOG4J_FATAL);
        result = fixture.toString();
        assertEquals("[LoggerInfo([BaseLoggerInfo([TraceInfo(Name=testName)],domain=LOG4J,level=LOG4J_FATAL)],State=ENABLED,levelType=LOGLEVEL_ALL)]", result);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fLoggerInfo1.equals(fLoggerInfo1));
        assertTrue("equals", fLoggerInfo2.equals(fLoggerInfo2));

        assertTrue("equals", !fLoggerInfo1.equals(fLoggerInfo2));
        assertTrue("equals", !fLoggerInfo2.equals(fLoggerInfo1));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsSymmetry() {
        LoggerInfo info1 = new LoggerInfo((LoggerInfo)fLoggerInfo1);
        LoggerInfo info2 = new LoggerInfo((LoggerInfo)fLoggerInfo2);

        assertTrue("equals", info1.equals(fLoggerInfo1));
        assertTrue("equals", fLoggerInfo1.equals(info1));

        assertTrue("equals", info2.equals(fLoggerInfo2));
        assertTrue("equals", fLoggerInfo2.equals(info2));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsTransivity() {
        LoggerInfo info1 = new LoggerInfo((LoggerInfo)fLoggerInfo1);
        LoggerInfo info2 = new LoggerInfo((LoggerInfo)fLoggerInfo1);
        LoggerInfo info3 = new LoggerInfo((LoggerInfo)fLoggerInfo1);

        assertTrue("equals", info1.equals(info2));
        assertTrue("equals", info2.equals(info3));
        assertTrue("equals", info1.equals(info3));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsNull() {
        assertTrue("equals", !fLoggerInfo1.equals(null));
        assertTrue("equals", !fLoggerInfo2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    /**
     * Run the hashCode() method test.
     */
    @Test
    public void testHashCode() {
        LoggerInfo info1 = new LoggerInfo((LoggerInfo)fLoggerInfo1);
        LoggerInfo info2 = new LoggerInfo((LoggerInfo)fLoggerInfo2);

        assertTrue("hashCode", fLoggerInfo1.hashCode() == info1.hashCode());
        assertTrue("hashCode", fLoggerInfo2.hashCode() == info2.hashCode());

        assertTrue("hashCode", fLoggerInfo1.hashCode() != info2.hashCode());
        assertTrue("hashCode", fLoggerInfo2.hashCode() != info1.hashCode());
    }
}
