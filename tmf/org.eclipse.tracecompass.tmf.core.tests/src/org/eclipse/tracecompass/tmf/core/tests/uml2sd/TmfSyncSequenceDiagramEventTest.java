/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
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
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.uml2sd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.uml2sd.TmfSyncSequenceDiagramEvent;
import org.junit.Test;

/**
 * TmfSyncSequenceDiagramEventTest
 */
public class TmfSyncSequenceDiagramEventTest {

    private final @NonNull String fTypeId  = "Some type";
    private final @NonNull String fLabel0  = "label1";
    private final @NonNull String fLabel1  = "label2";
    private final String[] fLabels  = new String[] { fLabel0, fLabel1 };

    private final ITmfTimestamp fTimestamp1 = TmfTimestamp.create(12345, (byte) 2);
    private final TmfEventType fType       = new TmfEventType(fTypeId, TmfEventField.makeRoot(fLabels));

    private final ITmfEvent fEvent1;
    private final TmfEventField fContent1;

    /**
     * Constructor for the test case
     */
    public TmfSyncSequenceDiagramEventTest() {
        fContent1 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some content", null);
        fEvent1 = new TmfEvent(null, ITmfContext.UNKNOWN_RANK, fTimestamp1, fType, fContent1);
    }

    /**
     * Main test
     */
    @Test
    public void testTmfSyncSequenceDiagramEvent() {
        TmfSyncSequenceDiagramEvent event = null;
        try {
            event = new TmfSyncSequenceDiagramEvent(null, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("testTmfSyncSequenceDiagramEvent", e.getMessage().contains("startEvent=null"));
        }

        try {
            event = new TmfSyncSequenceDiagramEvent(fEvent1, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("testTmfSyncSequenceDiagramEvent", e.getMessage().contains("sender=null"));
        }

        try {
            event = new TmfSyncSequenceDiagramEvent(fEvent1, "sender", null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("testTmfSyncSequenceDiagramEvent", e.getMessage().contains("receiver=null"));
        }

        try {
            event = new TmfSyncSequenceDiagramEvent(fEvent1, "sender", "receiver", null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("testTmfSyncSequenceDiagramEvent", e.getMessage().contains("name=null"));
        }

        try {
            event = new TmfSyncSequenceDiagramEvent(fEvent1, "sender", "receiver", "signal");
            // success
            assertEquals("testTmfSyncSequenceDiagramEvent", 0, event.getStartTime().compareTo(fTimestamp1));
            assertEquals("testTmfSyncSequenceDiagramEvent", "sender", event.getSender());
            assertEquals("testTmfSyncSequenceDiagramEvent", "receiver", event.getReceiver());
            assertEquals("testTmfSyncSequenceDiagramEvent", "signal", event.getName());

        } catch (IllegalArgumentException e) {
            fail();
        }
    }
}
