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

import org.eclipse.linuxtools.internal.lttng2.core.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IDomainInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.DomainInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>ChannelInfoTest</code> contains tests for the class
 * <code>{@link DomainInfo}</code>.
 */
public class DomainInfoTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private IDomainInfo fDomainInfo1 = null;
    private IDomainInfo fDomainInfo2 = null;
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
        // Get test instances from the factory
        ModelImplFactory factory = new ModelImplFactory();
        fChannelInfo1 = factory.getChannel1();
        fChannelInfo2 = factory.getChannel2();
        fDomainInfo1 =  factory.getDomainInfo1();
        fDomainInfo2 =  factory.getDomainInfo2();
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
    public void testDomainInfo() {
        DomainInfo result = new DomainInfo("test");
        assertNotNull(result);

        assertEquals("test", result.getName());
        assertEquals(0, result.getChannels().length);
    }

    /**
     * Test the copy constructor.
     */
    @Test
    public void testDomainInfoCopy() {
        DomainInfo channelInfo = new DomainInfo((DomainInfo)fDomainInfo1);
        IChannelInfo[] orignalEvents = fDomainInfo1.getChannels();
        IChannelInfo[] resultEvents = channelInfo.getChannels();
        for (int i = 0; i < orignalEvents.length; i++) {
            assertEquals(orignalEvents[i], resultEvents[i]);
        }
    }

    /**
     * Test the copy constructor.
     */
    @Test
    public void testDomainlCopy2() {
        try {
            DomainInfo domain = null;
            new DomainInfo(domain);
            fail("null copy");
        }
        catch (IllegalArgumentException e) {
            // Success
        }
    }

    /**
     * Run the long getNumberOfSubBuffers() method test.
     */
    @Test
    public void testGetAndSetters() {

        // Note that addChannel() has been executed in setUp()
        // check get method here
        assertEquals(1, fDomainInfo1.getChannels().length);
        assertNotNull(fDomainInfo1.getChannels()[0]);
        assertEquals(fChannelInfo1, fDomainInfo1.getChannels()[0]);

        IDomainInfo domain = new DomainInfo("domain");
        List<IChannelInfo> list = new LinkedList<>();
        list.add(fChannelInfo1);
        list.add(fChannelInfo2);
        domain.setChannels(list);

        IChannelInfo[] result = domain.getChannels();
        assertEquals(2, result.length);
        assertEquals(fChannelInfo1, result[0]);
        assertEquals(fChannelInfo2, result[1]);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_1() {
        DomainInfo fixture = new DomainInfo("domain");

        String result = fixture.toString();

        assertEquals("[DomainInfo([TraceInfo(Name=domain)],Channels=None,isKernel=false)]", result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_2() {
        String result = fDomainInfo1.toString();

        assertEquals("[DomainInfo([TraceInfo(Name=test1)],Channels=[ChannelInfo([TraceInfo(Name=channel1)],State=DISABLED,OverwriteMode=true,SubBuffersSize=13,NumberOfSubBuffers=12,SwitchTimer=10,ReadTimer=11,output=splice(),Events=[EventInfo([BaseEventInfo([TraceInfo(Name=event1)],type=TRACEPOINT,level=TRACE_DEBUG)],State=ENABLED)])],isKernel=false)]", result);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fDomainInfo1.equals(fDomainInfo1));
        assertTrue("equals", fDomainInfo2.equals(fDomainInfo2));

        assertTrue("equals", !fDomainInfo1.equals(fDomainInfo2));
        assertTrue("equals", !fDomainInfo2.equals(fDomainInfo1));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsSymmetry() {
        DomainInfo event1 = new DomainInfo((DomainInfo)fDomainInfo1);
        DomainInfo event2 = new DomainInfo((DomainInfo)fDomainInfo2);

        assertTrue("equals", event1.equals(fDomainInfo1));
        assertTrue("equals", fDomainInfo1.equals(event1));

        assertTrue("equals", event2.equals(fDomainInfo2));
        assertTrue("equals", fDomainInfo2.equals(event2));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsTransivity() {
        DomainInfo channel1 = new DomainInfo((DomainInfo)fDomainInfo1);
        DomainInfo channel2 = new DomainInfo((DomainInfo)fDomainInfo1);
        DomainInfo channel3 = new DomainInfo((DomainInfo)fDomainInfo1);

        assertTrue("equals", channel1.equals(channel2));
        assertTrue("equals", channel2.equals(channel3));
        assertTrue("equals", channel1.equals(channel3));
    }

    /**
     * Run the equals() method test.
     */
    @Test
    public void testEqualsNull() {
        assertTrue("equals", !fDomainInfo1.equals(null));
        assertTrue("equals", !fDomainInfo2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    /**
     * Run the hashCode() method test.
     */
    @Test
    public void testHashCode() {
        DomainInfo channel1 = new DomainInfo((DomainInfo)fDomainInfo1);
        DomainInfo channel2 = new DomainInfo((DomainInfo)fDomainInfo2);

        assertTrue("hashCode", fDomainInfo1.hashCode() == channel1.hashCode());
        assertTrue("hashCode", fDomainInfo2.hashCode() == channel2.hashCode());

        assertTrue("hashCode", fDomainInfo1.hashCode() != channel2.hashCode());
        assertTrue("hashCode", fDomainInfo2.hashCode() != channel1.hashCode());
    }
}