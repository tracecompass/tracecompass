/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.uml2sd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.uml2sd.TmfAsyncSequenceDiagramEvent;

import org.junit.Test;

/**
 * TmfAsyncSequenceDiagramEventTest
 */
public class TmfAsyncSequenceDiagramEventTest {

    private final String fContext = ITmfEventType.DEFAULT_CONTEXT_ID;
    private final String fTypeId  = "Some type";
    private final String fLabel0  = "label1";
    private final String fLabel1  = "label2";
    private final String[] fLabels  = new String[] { fLabel0, fLabel1 };

    private final TmfTimestamp fTimestamp1 = new TmfTimestamp(12345, (byte) 2, 5);
    private final TmfTimestamp fTimestamp2 = new TmfTimestamp(12350, (byte) 2, 5);
    private final String       fSource     = "Source";
    private final TmfEventType fType       = new TmfEventType(fContext, fTypeId, TmfEventField.makeRoot(fLabels));
    private final String       fReference  = "Some reference";

    private final ITmfEvent fEvent1;
    private final ITmfEvent fEvent2;
    private final TmfEventField fContent1;
    private final TmfEventField fContent2;

    /**
     * Constructor for the test case
     */
    public TmfAsyncSequenceDiagramEventTest() {
        fContent1 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some content", null);
        fEvent1 = new TmfEvent(null, fTimestamp1, fSource, fType, fContent1, fReference);

        fContent2 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some other content", null);
        fEvent2 = new TmfEvent(null, fTimestamp2, fSource, fType, fContent2, fReference);
    }

    /**
     * Main test
     */
    @Test
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
