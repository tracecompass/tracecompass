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

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.linuxtools.lttng.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IDomainInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.DomainInfo;

/**
 * The class <code>ChannelInfoTest</code> contains tests for the class <code>{@link DomainInfo}</code>.
 *
 */
@SuppressWarnings("nls")
public class DomainInfoTest extends TestCase {
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
     *
     * @throws Exception
     *         if the initialization fails for some reason
     *
     */
    @Override
    public void setUp() {
        // Get test instances from the factory
        ModelImplFactory factory = new ModelImplFactory();
        fChannelInfo1 = factory.getChannel1();
        fChannelInfo2 = factory.getChannel2();
        fDomainInfo1 =  factory.getDomainInfo1();
        fDomainInfo2 =  factory.getDomainInfo2();
    }

    /**
     * Perform post-test clean-up.
     *
     * @throws Exception
     *         if the clean-up fails for some reason
     *
     */
    @Override
    public void tearDown() {
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Run the ChannelInfo() constructor test.
     *
     */
    public void testDomainInfo() {
        DomainInfo result = new DomainInfo("test");
        assertNotNull(result);
        
        assertEquals("test", result.getName());
        assertEquals(0, result.getChannels().length);
    }

    public void testDomainInfoCopy() {
        DomainInfo channelInfo = new DomainInfo((DomainInfo)fDomainInfo1);
        IChannelInfo[] orignalEvents = fDomainInfo1.getChannels();
        IChannelInfo[] resultEvents = channelInfo.getChannels();
        for (int i = 0; i < orignalEvents.length; i++) {
            assertEquals(orignalEvents[i], resultEvents[i]);
        }
    }

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
     *
     */
    public void testGetAndSetters() {
        
        // Note that addChannel() has been executed in setUp()
        // check get method here
        assertEquals(1, fDomainInfo1.getChannels().length);
        assertNotNull(fDomainInfo1.getChannels()[0]);
        assertEquals(fChannelInfo1, fDomainInfo1.getChannels()[0]);
        
        IDomainInfo domain = new DomainInfo("domain");
        List<IChannelInfo> list = new LinkedList<IChannelInfo>();
        list.add(fChannelInfo1);
        list.add(fChannelInfo2);
        domain.setChannels(list);
        
        IChannelInfo[] result = domain.getChannels();
        assertEquals(2, result.length);
        assertEquals(fChannelInfo1, result[0]);
        assertEquals(fChannelInfo2, result[1]);
    }

    public void testToString_1() {
        DomainInfo fixture = new DomainInfo("domain");

        String result = fixture.toString();

        assertEquals("[DomainInfo([TraceInfo(Name=domain)],Channels=None,isKernel=false)]", result);
    }

    /**
     * Run the String toString() method test.
     *
     */
    public void testToString_2() {
        String result = fDomainInfo1.toString();

        assertEquals("[DomainInfo([TraceInfo(Name=test1)],Channels=[ChannelInfo([TraceInfo(Name=channel1)],State=DISABLED,OverwriteMode=true,SubBuffersSize=13,NumberOfSubBuffers=12,SwitchTimer=10,ReadTimer=11,output=splice(),Events=[EventInfo([BaseEventInfo([TraceInfo(Name=event1)],type=TRACEPOINT,level=TRACE_DEBUG)],State=ENABLED)])],isKernel=false)]", result);
    }
    
    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    public void testEqualsReflexivity() {
        assertTrue("equals", fDomainInfo1.equals(fDomainInfo1));
        assertTrue("equals", fDomainInfo2.equals(fDomainInfo2));

        assertTrue("equals", !fDomainInfo1.equals(fDomainInfo2));
        assertTrue("equals", !fDomainInfo2.equals(fDomainInfo1));
    }
    
    public void testEqualsSymmetry() {
        DomainInfo event1 = new DomainInfo((DomainInfo)fDomainInfo1);
        DomainInfo event2 = new DomainInfo((DomainInfo)fDomainInfo2);

        assertTrue("equals", event1.equals(fDomainInfo1));
        assertTrue("equals", fDomainInfo1.equals(event1));

        assertTrue("equals", event2.equals(fDomainInfo2));
        assertTrue("equals", fDomainInfo2.equals(event2));
    }
    
    public void testEqualsTransivity() {
        DomainInfo channel1 = new DomainInfo((DomainInfo)fDomainInfo1);
        DomainInfo channel2 = new DomainInfo((DomainInfo)fDomainInfo1);
        DomainInfo channel3 = new DomainInfo((DomainInfo)fDomainInfo1);

        assertTrue("equals", channel1.equals(channel2));
        assertTrue("equals", channel2.equals(channel3));
        assertTrue("equals", channel1.equals(channel3));
    }
    
    public void testEqualsNull() throws Exception {
        assertTrue("equals", !fDomainInfo1.equals(null));
        assertTrue("equals", !fDomainInfo2.equals(null));
    }
    
    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    public void testHashCode() {
        DomainInfo channel1 = new DomainInfo((DomainInfo)fDomainInfo1);
        DomainInfo channel2 = new DomainInfo((DomainInfo)fDomainInfo2);

        assertTrue("hashCode", fDomainInfo1.hashCode() == channel1.hashCode());
        assertTrue("hashCode", fDomainInfo2.hashCode() == channel2.hashCode());

        assertTrue("hashCode", fDomainInfo1.hashCode() != channel2.hashCode());
        assertTrue("hashCode", fDomainInfo2.hashCode() != channel1.hashCode());
    }
}