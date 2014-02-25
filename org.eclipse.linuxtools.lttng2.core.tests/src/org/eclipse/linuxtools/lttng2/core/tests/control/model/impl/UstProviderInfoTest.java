/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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

package org.eclipse.linuxtools.lttng2.core.tests.control.model.impl;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.core.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IUstProviderInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.BaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.UstProviderInfo;
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

        IBaseEventInfo[] orignalEvents = fUstProviderInfo1.getEvents();
        IBaseEventInfo[] resultEvents = providerInf.getEvents();
        for (int i = 0; i < orignalEvents.length; i++) {
            assertEquals(orignalEvents[i], resultEvents[i]);
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

        // add an event
        IBaseEventInfo event = new BaseEventInfo("event");
        fixture.addEvent(event);

        // Verify the stored events
        IBaseEventInfo[] result = fixture.getEvents();

        assertNotNull(result);
        assertEquals(1, result.length);
        assertNotNull(result[0]);
        assertTrue(event.equals(result[0]));

        assertEquals(2468, fixture.getPid());
    }

    /**
     * Run the void setEvents(List<IBaseEventInfo>) method test.
     */
    @Test
    public void testSetEvents_1() {
        UstProviderInfo fixture = new UstProviderInfo("test");
        fixture.setPid(2468);
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
     * Run the String toString() method test.
     */
    @Test
    public void testToString_1() {
        UstProviderInfo fixture = new UstProviderInfo("test");
        fixture.setPid(2468);
        String result = fixture.toString();

        assertEquals("[EventInfo([TraceInfo(Name=test)],PID=2468,Events=None)]", result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_2() {
        String result = fUstProviderInfo2.toString();
        assertEquals("[EventInfo([TraceInfo(Name=myUST2)],PID=2345,Events=[BaseEventInfo([TraceInfo(Name=event1)]," +
                "type=UNKNOWN,level=TRACE_DEBUG,Fields=[FieldInfo([TraceInfo(Name=intfield)],type=int[FieldInfo" +
                "([TraceInfo(Name=stringfield)],type=string,Filter=intField==10)][BaseEventInfo([TraceInfo(Name=event2)]," +
                "type=TRACEPOINT,level=TRACE_DEBUG)])]", result);
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