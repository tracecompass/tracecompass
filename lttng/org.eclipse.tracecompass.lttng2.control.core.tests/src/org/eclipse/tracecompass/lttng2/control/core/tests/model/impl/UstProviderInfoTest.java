/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.control.core.tests.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ILoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IUstProviderInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.BaseEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.LoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.UstProviderInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>ChannelInfoTest</code> contains tests for the class
 * <code>{@link UstProviderInfo}</code>.
 */
public class UstProviderInfoTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private IUstProviderInfo fUstProviderInfo1 = null;
    private IUstProviderInfo fUstProviderInfo2 = null;

    private IBaseEventInfo fEventInfo1 = null;
    private IBaseEventInfo fEventInfo2 = null;

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
        fUstProviderInfo1 = factory.getUstProviderInfo1();
        fUstProviderInfo2 = factory.getUstProviderInfo2();
        fEventInfo1 = factory.getBaseEventInfo1();
        fEventInfo2 = factory.getBaseEventInfo2();
        fLoggerInfo1 = factory.getLoggerInfo1();
        fLoggerInfo2 = factory.getLoggerInfo2();
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Run the UstProviderInfo() constructor test.
     */
    @Test
    public void testUstProviderInfo() {
        IUstProviderInfo result = new UstProviderInfo("test");
        assertNotNull(result);

        assertEquals("test", result.getName());
        assertEquals(0, result.getPid());
        assertEquals(0, result.getEvents().length);
        assertEquals(0, result.getLoggers().size());
    }

    /**
     * Test the copy constructor.
     */
    @Test
    public void testUstProviderInfoCopy() {
        IUstProviderInfo providerInf = new UstProviderInfo((UstProviderInfo)fUstProviderInfo1);

        assertEquals(fUstProviderInfo1.getName(), providerInf.getName());
        assertEquals(fUstProviderInfo1.getPid(), providerInf.getPid());
        assertEquals(fUstProviderInfo1.getEvents().length, providerInf.getEvents().length);
        assertEquals(fUstProviderInfo1.getLoggers().size(), providerInf.getLoggers().size());

        IBaseEventInfo[] orignalEvents = fUstProviderInfo1.getEvents();
        IBaseEventInfo[] resultEvents = providerInf.getEvents();
        for (int i = 0; i < orignalEvents.length; i++) {
            assertEquals(orignalEvents[i], resultEvents[i]);
        }

        List<ILoggerInfo> originalLoggers = fUstProviderInfo1.getLoggers();
        List<ILoggerInfo> resultLoggers = providerInf.getLoggers();
        for (int i = 0; i < originalLoggers.size(); i++) {
            assertEquals(originalLoggers.get(i), resultLoggers.get(i));
        }
    }

    /**
     * Test the copy constructor.
     */
    @Test
    public void testUstProviderCopy2() {
        try {
            UstProviderInfo providerInfo = null;
            new UstProviderInfo(providerInfo);
            fail("null copy");
        }
        catch (IllegalArgumentException e) {
            // Success
        }
    }

    /**
     * Run the IEventInfo[] getEvents() method test.
     */
    @Test
    public void testGetAndSetters() {
        IUstProviderInfo fixture = new UstProviderInfo("test");

        fixture.setPid(2468);
        assertEquals(2468, fixture.getPid());

        // add an event
        IBaseEventInfo event = new BaseEventInfo("event");
        fixture.addEvent(event);

        // verify the stored events
        IBaseEventInfo[] events = fixture.getEvents();
        assertNotNull(events);
        assertEquals(1, events.length);
        assertNotNull(events[0]);
        assertTrue(event.equals(events[0]));

        // add a logger
        ILoggerInfo logger = new LoggerInfo("logger");
        fixture.addLogger(logger);

        // verify the stored loggers
        List<ILoggerInfo> loggers = fixture.getLoggers();
        assertNotNull(loggers);
        assertEquals(1, loggers.size());
        assertNotNull(loggers.get(0));
        assertTrue(logger.equals(loggers.get(0)));
    }

    /**
     * Run the void setEvents(List<IBaseEventInfo>) method test.
     */
    @Test
    public void testSetEvents_1() {
        UstProviderInfo fixture = new UstProviderInfo("test");
        List<IBaseEventInfo> events = new LinkedList<>();
        events.add(fEventInfo1);
        events.add(fEventInfo2);
        fixture.setEvents(events);

        IBaseEventInfo[] infos = fixture.getEvents();

        assertEquals(events.size(), infos.length);

        for (int i = 0; i < infos.length; i++) {
            assertEquals(events.get(i), infos[i]);
        }
    }

    /**
     * Run the void setLoggers(List<ILoggerInfo>) method test.
     */
    @Test
    public void testSetLoggers_1() {
        UstProviderInfo fixture = new UstProviderInfo("test");
        List<ILoggerInfo> originalLoggers = new LinkedList<>();
        originalLoggers.add(fLoggerInfo1);
        originalLoggers.add(fLoggerInfo2);
        fixture.setLoggers(originalLoggers);

        List<ILoggerInfo> resultLoggers = fixture.getLoggers();

        assertEquals(originalLoggers.size(), resultLoggers.size());

        for (int i = 0; i < resultLoggers.size(); i++) {
            assertEquals(originalLoggers.get(i), resultLoggers.get(i));
        }
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_1() {
        UstProviderInfo fixture = new UstProviderInfo("test");
        fixture.setPid(2468);
        String result = fixture.toString();
        assertEquals("[UstProviderInfo([TraceInfo(Name=test)],PID=2468,Events=None,Loggers=None)]", result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_2() {
        String result = fUstProviderInfo2.toString();

        assertEquals("[UstProviderInfo([TraceInfo(Name=myUST2)],PID=2345,Events=[BaseEventInfo([TraceInfo(Name=event1)],"
                + "type=UNKNOWN,level=TRACE_ERR,Fields=[FieldInfo([TraceInfo(Name=intfield)],type=int[FieldInfo([TraceInfo(Name=stringfield)],"
                + "type=string,Filter=intField==10)][BaseEventInfo([TraceInfo(Name=event2)],type=TRACEPOINT,level=TRACE_DEBUG)],"
                + "Loggers=[LoggerInfo([BaseLoggerInfo([TraceInfo(Name=logger1)],domain=JUL,level=LOG4J_INFO)],"
                + "State=ENABLED,levelType=LOGLEVEL_ALL)][LoggerInfo([BaseLoggerInfo([TraceInfo(Name=logger2)],domain=LOG4J,level=LEVEL_UNKNOWN)],"
                + "State=DISABLED,levelType=LOGLEVEL_ALL)])]", result);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fUstProviderInfo1.equals(fUstProviderInfo1));
        assertTrue("equals", fUstProviderInfo2.equals(fUstProviderInfo2));

        assertTrue("equals", !fUstProviderInfo1.equals(fUstProviderInfo2));
        assertTrue("equals", !fUstProviderInfo2.equals(fUstProviderInfo1));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsSymmetry() {
        UstProviderInfo event1 = new UstProviderInfo((UstProviderInfo)fUstProviderInfo1);
        UstProviderInfo event2 = new UstProviderInfo((UstProviderInfo)fUstProviderInfo2);

        assertTrue("equals", event1.equals(fUstProviderInfo1));
        assertTrue("equals", fUstProviderInfo1.equals(event1));

        assertTrue("equals", event2.equals(fUstProviderInfo2));
        assertTrue("equals", fUstProviderInfo2.equals(event2));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsTransivity() {
        UstProviderInfo UstProvider1 = new UstProviderInfo((UstProviderInfo)fUstProviderInfo1);
        UstProviderInfo UstProvider2 = new UstProviderInfo((UstProviderInfo)fUstProviderInfo1);
        UstProviderInfo UstProvider3 = new UstProviderInfo((UstProviderInfo)fUstProviderInfo1);

        assertTrue("equals", UstProvider1.equals(UstProvider2));
        assertTrue("equals", UstProvider2.equals(UstProvider3));
        assertTrue("equals", UstProvider1.equals(UstProvider3));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsNull() {
        assertTrue("equals", !fUstProviderInfo1.equals(null));
        assertTrue("equals", !fUstProviderInfo2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    /**
     * Run the hashCode() method test.
     */
    @Test
    public void testHashCode() {
        UstProviderInfo UstProvider1 = new UstProviderInfo((UstProviderInfo)fUstProviderInfo1);
        UstProviderInfo UstProvider2 = new UstProviderInfo((UstProviderInfo)fUstProviderInfo2);

        assertTrue("hashCode", fUstProviderInfo1.hashCode() == UstProvider1.hashCode());
        assertTrue("hashCode", fUstProviderInfo2.hashCode() == UstProvider2.hashCode());

        assertTrue("hashCode", fUstProviderInfo1.hashCode() != UstProvider2.hashCode());
        assertTrue("hashCode", fUstProviderInfo2.hashCode() != UstProvider1.hashCode());
    }
}