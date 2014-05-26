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

package org.eclipse.linuxtools.lttng2.control.core.tests.model.impl;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IEventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.BufferType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.ChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.EventInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>ChannelInfoTest</code> contains tests for the class
 * <code>{@link ChannelInfo}</code>.
 */
public class ChannelInfoTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private IChannelInfo fChannelInfo1 = null;
    private IChannelInfo fChannelInfo2 = null;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        ModelImplFactory factory = new ModelImplFactory();
        fChannelInfo1 = factory.getChannel1();
        fChannelInfo2 = factory.getChannel2();
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Run the ChannelInfo() constructor test.
     */
    @Test
    public void testChannelInfo() {
        ChannelInfo result = new ChannelInfo("test");
        assertNotNull(result);

        assertEquals("test", result.getName());
        assertEquals(0, result.getNumberOfSubBuffers());
        assertEquals("", result.getOutputType());
        assertEquals(false, result.isOverwriteMode());
        assertEquals(0, result.getReadTimer());
        assertEquals("disabled", result.getState().getInName());
        assertEquals(0, result.getSubBufferSize());
        assertEquals(0, result.getSwitchTimer());
        assertEquals(0, result.getMaxSizeTraceFiles());
        assertEquals(0, result.getMaxNumberTraceFiles());
        assertEquals(BufferType.BUFFER_TYPE_UNKNOWN, result.getBufferType());
    }

    /**
     * Test copy constructor.
     */
    @Test
    public void testChannelInfoCopy() {
        ChannelInfo channelInfo = new ChannelInfo((ChannelInfo)fChannelInfo1);

        assertEquals(fChannelInfo1.getName(), channelInfo.getName());
        assertEquals(fChannelInfo1.getNumberOfSubBuffers(), channelInfo.getNumberOfSubBuffers());
        assertEquals(fChannelInfo1.getOutputType(), channelInfo.getOutputType());
        assertEquals(fChannelInfo1.isOverwriteMode(), channelInfo.isOverwriteMode());
        assertEquals(fChannelInfo1.getReadTimer(), channelInfo.getReadTimer());
        assertEquals(fChannelInfo1.getState(), channelInfo.getState());
        assertEquals(fChannelInfo1.getSwitchTimer(), channelInfo.getSwitchTimer());
        assertEquals(fChannelInfo1.getEvents().length, channelInfo.getEvents().length);
        assertEquals(fChannelInfo1.getMaxSizeTraceFiles(), channelInfo.getMaxSizeTraceFiles());
        assertEquals(fChannelInfo1.getMaxNumberTraceFiles(), channelInfo.getMaxNumberTraceFiles());
        assertEquals(fChannelInfo1.getBufferType(), channelInfo.getBufferType());

        IEventInfo[] orignalEvents = fChannelInfo1.getEvents();
        IEventInfo[] resultEvents = channelInfo.getEvents();
        for (int i = 0; i < orignalEvents.length; i++) {
            assertEquals(orignalEvents[i], resultEvents[i]);
        }
    }

    /**
     * Test copy constructor with a null value.
     */
    @Test
    public void testChannelCopy2() {
        try {
            ChannelInfo channel = null;
            new ChannelInfo(channel);
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
    public void testAddAndGetEvents_1() {
        ChannelInfo fixture = new ChannelInfo("test");
        fixture.setSwitchTimer(1L);
        fixture.setOverwriteMode(true);
        fixture.setReadTimer(1L);
        fixture.setState(TraceEnablement.DISABLED);
        fixture.setNumberOfSubBuffers(1);
        fixture.setOutputType("");
        fixture.setSubBufferSize(1L);

        // add an event
        IEventInfo event = new EventInfo("event");
        fixture.addEvent(event);

        // Verify the stored events
        IEventInfo[] result = fixture.getEvents();

        assertNotNull(result);
        assertEquals(1, result.length);
        assertNotNull(result[0]);
        assertTrue(event.equals(result[0]));
    }

    /**
     * Run the long getNumberOfSubBuffers() method test.
     */
    @Test
    public void testGetAndSetters() {
        ChannelInfo fixture = new ChannelInfo("test");
        fixture.setSwitchTimer(2L);
        fixture.setOverwriteMode(true);
        fixture.setReadTimer(3L);
        fixture.setState(TraceEnablement.DISABLED);
        fixture.setNumberOfSubBuffers(4);
        fixture.setOutputType("splice()");
        fixture.setSubBufferSize(1L);
        fixture.setMaxSizeTraceFiles(1024);
        fixture.setMaxNumberTraceFiles(20);
        fixture.setBufferType(BufferType.BUFFER_PER_UID);
        fixture.addEvent(new EventInfo("event"));

        long switchTimer = fixture.getSwitchTimer();
        assertEquals(2L, switchTimer);

        boolean mode = fixture.isOverwriteMode();
        assertTrue(mode);

        long readTimer = fixture.getReadTimer();
        assertEquals(3L, readTimer);

        TraceEnablement state = fixture.getState();
        assertEquals("disabled", state.getInName());

        long numSubBuffers = fixture.getNumberOfSubBuffers();
        assertEquals(4, numSubBuffers);

        String outputType = fixture.getOutputType();
        assertEquals("splice()", outputType);

        long subBufferSize = fixture.getSubBufferSize();
        assertEquals(1L, subBufferSize);

        int maxSizeTraceFiles = fixture.getMaxSizeTraceFiles();
        assertEquals(1024, maxSizeTraceFiles);

        int maxNumberTraceFiles = fixture.getMaxNumberTraceFiles();
        assertEquals(20, maxNumberTraceFiles);

        BufferType bufferType = fixture.getBufferType();
        assertTrue(bufferType == BufferType.BUFFER_PER_UID);

        fixture.setSwitchTimer(5L);
        fixture.setOverwriteMode(false);
        fixture.setReadTimer(6L);
        fixture.setState(TraceEnablement.ENABLED);
        fixture.setNumberOfSubBuffers(7);
        fixture.setOutputType("mmap()");
        fixture.setSubBufferSize(8L);
        fixture.setMaxSizeTraceFiles(4096);
        fixture.setMaxNumberTraceFiles(10);
        fixture.setBufferType(BufferType.BUFFER_PER_PID);

        switchTimer = fixture.getSwitchTimer();
        assertEquals(5L, switchTimer);

        mode = fixture.isOverwriteMode();
        assertFalse(mode);

        readTimer = fixture.getReadTimer();
        assertEquals(6L, readTimer);

        state = fixture.getState();
        assertEquals("enabled", state.getInName());

        numSubBuffers = fixture.getNumberOfSubBuffers();
        assertEquals(7, numSubBuffers);

        outputType = fixture.getOutputType();
        assertEquals("mmap()", outputType);

        subBufferSize = fixture.getSubBufferSize();
        assertEquals(8L, subBufferSize);

        maxSizeTraceFiles = fixture.getMaxSizeTraceFiles();
        assertEquals(4096, maxSizeTraceFiles);

        maxNumberTraceFiles = fixture.getMaxNumberTraceFiles();
        assertEquals(10, maxNumberTraceFiles);

        bufferType = fixture.getBufferType();
        assertTrue(bufferType == BufferType.BUFFER_PER_PID);
    }

    /**
     * Run the void setEvents(List<IEventInfo>) method test.
     */
    @Test
    public void testSetEvents_1() {
        ChannelInfo fixture = new ChannelInfo("test");
        fixture.setSwitchTimer(1L);
        fixture.setOverwriteMode(true);
        fixture.setReadTimer(1L);
        fixture.setState(TraceEnablement.DISABLED);
        fixture.setNumberOfSubBuffers(1);
        fixture.setOutputType("");
        fixture.setSubBufferSize(1L);
        List<IEventInfo> events = new LinkedList<>();

        for (int i = 0; i < 2; i++) {
            IEventInfo info = new EventInfo("event" + i);
            info.setEventType("tracepoint");
            info.setState((i % 2 == 0 ? "enabled" : "disabled"));
            events.add(info);
        }

        fixture.setEvents(events);

        IEventInfo[] infos = fixture.getEvents();

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
        ChannelInfo fixture = new ChannelInfo("channel");
        fixture.setSwitchTimer(1L);
        fixture.setOverwriteMode(true);
        fixture.setReadTimer(1L);
        fixture.setState(TraceEnablement.DISABLED);
        fixture.setNumberOfSubBuffers(1);
        fixture.setOutputType("splice()");
        fixture.setSubBufferSize(1L);

        String result = fixture.toString();

        // add additional test code here
        assertEquals("[ChannelInfo([TraceInfo(Name=channel)],State=DISABLED,OverwriteMode=true,SubBuffersSize=1,NumberOfSubBuffers=1,SwitchTimer=1,ReadTimer=1,output=splice(),Events=None)]", result);
    }

    /**
     * Run another String toString() method test.
     */
    @Test
    public void testToString_2() {
        String result = fChannelInfo1.toString();

        // add additional test code here
        assertEquals("[ChannelInfo([TraceInfo(Name=channel1)],State=DISABLED,OverwriteMode=true,SubBuffersSize=13,NumberOfSubBuffers=12,SwitchTimer=10,ReadTimer=11,output=splice(),Events=[EventInfo([BaseEventInfo([TraceInfo(Name=event1)],type=TRACEPOINT,level=TRACE_DEBUG)],State=ENABLED,levelType=LOGLEVEL_ONLY)])]", result);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fChannelInfo1.equals(fChannelInfo1));
        assertTrue("equals", fChannelInfo2.equals(fChannelInfo2));

        assertTrue("equals", !fChannelInfo1.equals(fChannelInfo2));
        assertTrue("equals", !fChannelInfo2.equals(fChannelInfo1));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsSymmetry() {
        ChannelInfo event1 = new ChannelInfo((ChannelInfo)fChannelInfo1);
        ChannelInfo event2 = new ChannelInfo((ChannelInfo)fChannelInfo2);

        assertTrue("equals", event1.equals(fChannelInfo1));
        assertTrue("equals", fChannelInfo1.equals(event1));

        assertTrue("equals", event2.equals(fChannelInfo2));
        assertTrue("equals", fChannelInfo2.equals(event2));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsTransivity() {
        ChannelInfo channel1 = new ChannelInfo((ChannelInfo)fChannelInfo1);
        ChannelInfo channel2 = new ChannelInfo((ChannelInfo)fChannelInfo1);
        ChannelInfo channel3 = new ChannelInfo((ChannelInfo)fChannelInfo1);

        assertTrue("equals", channel1.equals(channel2));
        assertTrue("equals", channel2.equals(channel3));
        assertTrue("equals", channel1.equals(channel3));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsNull() {
        assertTrue("equals", !fChannelInfo1.equals(null));
        assertTrue("equals", !fChannelInfo2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    /**
     * Run the hashCode() method test.
     */
    @Test
    public void testHashCode() {
        ChannelInfo channel1 = new ChannelInfo((ChannelInfo)fChannelInfo1);
        ChannelInfo channel2 = new ChannelInfo((ChannelInfo)fChannelInfo2);

        assertTrue("hashCode", fChannelInfo1.hashCode() == channel1.hashCode());
        assertTrue("hashCode", fChannelInfo2.hashCode() == channel2.hashCode());

        assertTrue("hashCode", fChannelInfo1.hashCode() != channel2.hashCode());
        assertTrue("hashCode", fChannelInfo2.hashCode() != channel1.hashCode());
    }
}