/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.tests.uml2sd;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventReference;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.uml2sd.TmfAsyncSequenceDiagramEvent;

@SuppressWarnings("nls")
public class TmfAsyncSequenceDiagramEventTest extends TestCase {

    private final String   fTypeId = "Some type";
    private final String   fLabel0 = "label1";
    private final String   fLabel1 = "label2";
    private final String[] fLabels = new String[] { fLabel0, fLabel1 };

    private final TmfTimestamp      fTimestamp1 = new TmfTimestamp(12345, (byte) 2, 5);
    private final TmfTimestamp      fTimestamp2 = new TmfTimestamp(12350, (byte) 2, 5);
    private final String            fSource     = "Source";
    private final TmfEventType      fType       = new TmfEventType(fTypeId, fLabels);
    private final TmfEventReference fReference  = new TmfEventReference("Some reference");

    private final TmfEvent fEvent1;
    private final TmfEvent fEvent2;
    private final TmfEventContent fContent1;
    private final TmfEventContent fContent2;

   
    public TmfAsyncSequenceDiagramEventTest() {
        fEvent1 = new TmfEvent(fTimestamp1, fSource, fType, fReference);
        fContent1 = new TmfEventContent(fEvent1, "Some content");
        fEvent1.setContent(fContent1);

        fEvent2 = new TmfEvent(fTimestamp2, fSource, fType, fReference);
        fContent2 = new TmfEventContent(fEvent2, "Some other content");
        fEvent2.setContent(fContent2);
    }
    
    @Override 
    public void setUp() throws Exception {
    }
    
    @Override
    public void tearDown() throws Exception {
    }
    
    public void testTmfAsyncSequenceDiagramEvent() {
        TmfAsyncSequenceDiagramEvent event = null;
        
        // Check for illegal arguments (i.e. null for the parameters) 
        try {
            event = new TmfAsyncSequenceDiagramEvent(null, null, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("TmfAsyncSequenceDiagramEvent", e.getMessage().contains("startEvent=null"));
        }
        
        try {
            event = new TmfAsyncSequenceDiagramEvent(fEvent1,  fEvent2, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("TmfAsyncSequenceDiagramEvent", e.getMessage().contains("sender=null"));
        }
        
        try {
            event = new TmfAsyncSequenceDiagramEvent(fEvent1, fEvent2, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("TmfAsyncSequenceDiagramEvent", e.getMessage().contains("receiver=null"));
        }

        try {
            event = new TmfAsyncSequenceDiagramEvent(fEvent1, fEvent2, "sender", null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("TmfAsyncSequenceDiagramEvent", e.getMessage().contains("name=null"));
        }
        
        try {
            event = new TmfAsyncSequenceDiagramEvent(fEvent1, null, "sender", "receiver", "signal");
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("TmfAsyncSequenceDiagramEvent", e.getMessage().contains("endEvent=null"));
        }

        try {
            event = new TmfAsyncSequenceDiagramEvent(fEvent1, fEvent2, "sender", "receiver", "signal");
            // success
            assertEquals("testTmfAsyncSequenceDiagramEvent", 0, event.getStartTime().compareTo(fTimestamp1, true));
            assertEquals("testTmfAsyncSequenceDiagramEvent", 0, event.getEndTime().compareTo(fTimestamp2, true));
            assertEquals("testTmfAsyncSequenceDiagramEvent", "sender", event.getSender());
            assertEquals("testTmfAsyncSequenceDiagramEvent", "receiver", event.getReceiver());
            assertEquals("testTmfAsyncSequenceDiagramEvent", "signal", event.getName());
            
        } catch (IllegalArgumentException e) {
            fail();
        }
    }
}
