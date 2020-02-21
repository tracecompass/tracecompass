/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adjusted for new Event Model
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Test;

/**
 * Test suite for the TmfEvent class.
 */
@SuppressWarnings("javadoc")
public class TmfEventTest {

    /** A trace to associate events with */
    private static final TmfTestTrace STUB_TRACE = TmfTestTrace.A_TEST_10K;

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final @NonNull ITmfTrace fTrace = STUB_TRACE.getTrace();

    private final @NonNull String fTypeId = "TestType";
    private final @NonNull String fLabel1 = "AString";
    private final @NonNull String fLabel2 = "AnInteger";
    private final String[] fLabels = new String[] { fLabel1, fLabel2 };
    private final TmfEventType fType = new TmfEventType(fTypeId, TmfEventField.makeRoot(fLabels));

    private final Object fValue1a = "Some string";
    private final Object fValue1b = Integer.valueOf(10);
    private final ITmfEventField fField1a = new TmfEventField(fLabel1, fValue1a, null);
    private final ITmfEventField fField1b = new TmfEventField(fLabel2, fValue1b, null);
    private final ITmfEventField[] fFields1 = new ITmfEventField[] { fField1a, fField1b };
    private final @NonNull String fRawContent1 = fField1a.toString() + fField1b.toString();
    private final ITmfEventField fContent1 = new TmfEventField(fRawContent1, null, fFields1);
    private final ITmfTimestamp fTimestamp1 = TmfTimestamp.create(12345, 2);
    private final @NonNull ITmfEvent fEvent1 = new TmfEvent(fTrace, 0, fTimestamp1, fType, fContent1);

    private final Object fValue2a = "Another string";
    private final Object fValue2b = Integer.valueOf(-4);
    private final ITmfEventField fField2a = new TmfEventField(fLabel1, fValue2a, null);
    private final ITmfEventField fField2b = new TmfEventField(fLabel2, fValue2b, null);
    private final ITmfEventField[] fFields2 = new ITmfEventField[] { fField2a, fField2b };
    private final @NonNull String fRawContent2 = fField2a.toString() + fField2b.toString();
    private final ITmfEventField fContent2 = new TmfEventField(fRawContent2, null, fFields2);
    private final ITmfTimestamp fTimestamp2 = TmfTimestamp.create(12350, 2);
    private final @NonNull ITmfEvent fEvent2 = new TmfEvent(fTrace, 1, fTimestamp2, fType, fContent2);

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    @After
    public void disposeTrace() {
        fTrace.dispose();
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testDefaultConstructor() {
        final ITmfEvent event = new TmfEvent(fTrace, ITmfContext.UNKNOWN_RANK, null, null, null);
        assertNotNull("getTrace", event.getTrace());
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, event.getRank());
        assertEquals("getTimestamp", TmfTimestamp.ZERO, event.getTimestamp());
        assertNull("getType", event.getType());
        assertNull("getContent", event.getContent());
    }

    @Test
    public void testFullConstructor() {
        assertNotNull("getTrace", fEvent1.getTrace());
        assertEquals("getRank", 0, fEvent1.getRank());
        assertEquals("getTimestamp", fTimestamp1, fEvent1.getTimestamp());
        assertEquals("getType", fType, fEvent1.getType());
        assertEquals("getContent", fContent1, fEvent1.getContent());

        assertNotNull("getTrace", fEvent2.getTrace());
        assertEquals("getRank", 1, fEvent2.getRank());
        assertEquals("getTimestamp", fTimestamp2, fEvent2.getTimestamp());
        assertEquals("getType", fType, fEvent2.getType());
        assertEquals("getContent", fContent2, fEvent2.getContent());
    }

    @Test
    public void testNoRankConstructor() {
        final ITmfEvent event = new TmfEvent(fTrace, ITmfContext.UNKNOWN_RANK, fTimestamp1, fType, fContent1);
        assertNotNull("getTrace", event.getTrace());
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, event.getRank());
        assertEquals("getTimestamp", fTimestamp1, event.getTimestamp());
        assertEquals("getType", fType, event.getType());
        assertEquals("getContent", fContent1, event.getContent());
    }

    @Test
    public void testConstructorWithTrace() {
        final ITmfTrace trace = fTrace;
        final ITmfEvent event = new TmfEvent(trace, 0, fTimestamp1, fType, fContent1);
        assertNotNull("getTrace", event.getTrace());
        assertEquals("getRank", 0, event.getRank());
        assertEquals("getTimestamp", fTimestamp1, event.getTimestamp());
        assertEquals("getType", fType, event.getType());
        assertEquals("getContent", fContent1, event.getContent());
        trace.dispose();
    }

    @Test
    public void testTmfEventCopy() {
        final ITmfEvent event = new TmfEvent(fEvent1);
        assertNotNull("getTrace", event.getTrace());
        assertEquals("getRank", 0, event.getRank());
        assertEquals("getTimestamp", fTimestamp1, event.getTimestamp());
        assertEquals("getType", fType, event.getType());
        assertEquals("getContent", fContent1, event.getContent());
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        ITmfEvent event1 = new TmfEvent(fTrace, ITmfContext.UNKNOWN_RANK, null, null, null);
        ITmfEvent event2 = new TmfEvent(fTrace, ITmfContext.UNKNOWN_RANK, null, null, null);

        assertTrue("hashCode", event1.hashCode() == event2.hashCode());

        final ITmfTrace trace = fTrace;
        event1 = new TmfEvent(trace, 0, fTimestamp1, fType, fContent1);
        event2 = new TmfEvent(trace, 1, fTimestamp2, fType, fContent2);
        final ITmfEvent event1b = new TmfEvent(event1);
        final ITmfEvent event2b = new TmfEvent(event2);

        assertTrue("hashCode", event1.hashCode() == event1b.hashCode());
        assertTrue("hashCode", event2.hashCode() == event2b.hashCode());

        assertTrue("hashCode", event1.hashCode() != event2.hashCode());
        assertTrue("hashCode", event2.hashCode() != event1.hashCode());

        trace.dispose();
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fEvent1.equals(fEvent1));
        assertTrue("equals", fEvent2.equals(fEvent2));

        assertFalse("equals", fEvent1.equals(fEvent2));
        assertFalse("equals", fEvent2.equals(fEvent1));
    }

    @Test
    public void testEqualsSymmetry() {
        final ITmfEvent event1 = new TmfEvent(fEvent1);
        final ITmfEvent event2 = new TmfEvent(fEvent2);

        assertTrue("equals", event1.equals(fEvent1));
        assertTrue("equals", fEvent1.equals(event1));

        assertTrue("equals", event2.equals(fEvent2));
        assertTrue("equals", fEvent2.equals(event2));
    }

    @Test
    public void testEqualsTransivity() {
        final ITmfEvent event1 = new TmfEvent(fEvent1);
        final ITmfEvent event2 = new TmfEvent(fEvent1);
        final ITmfEvent event3 = new TmfEvent(fEvent1);

        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event3));
        assertTrue("equals", event1.equals(event3));
    }

    @Test
    public void testEqualsNull() {
        assertFalse("equals", fEvent1.equals(null));
        assertFalse("equals", fEvent2.equals(null));
    }

    @Test
    public void testNonEqualClasses() {
        assertFalse("equals", fEvent1.equals(fEvent1.getType()));
        assertFalse("equals", fEvent1.equals(null));
    }

    @Test
    public void testNonEqualTraces() {
        final ITmfTrace trace1 = fTrace;
        final ITmfTrace trace2 = STUB_TRACE.getTrace();

        final ITmfEvent event1 = new TmfEvent(trace1, 0, fTimestamp1, fType, fContent1);
        ITmfEvent event2 = new TmfEvent(trace1,  0, fTimestamp1, fType, fContent1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(trace2,  0, fTimestamp1, fType, fContent1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        trace2.dispose();
    }

    @Test
    public void testNonEqualRanks() {
        final ITmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fType, fContent1);
        ITmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fType, fContent1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 1, fTimestamp1, fType, fContent1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    @Test
    public void testNonEqualTimestamps() {
        final ITmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fType, fContent1);
        ITmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fType, fContent1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp2, fType, fContent1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, null, fType, fContent1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    @Test
    public void testNonEqualTypes() {
        final ITmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fType, fContent1);
        ITmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fType,  fContent1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        final String typeId = "OtherTestType";
        final String[] labels = new String[] { fLabel2, fLabel1 };
        final TmfEventType newType = new TmfEventType(typeId, TmfEventField.makeRoot(labels));

        event2 = new TmfEvent(null, 0, fTimestamp1, newType, fContent1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, null, fContent1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    @Test
    public void testNonEqualContents() {
        final ITmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fType, fContent1);
        ITmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fType, fContent1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, fType, fContent2);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, fType, null);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        final String expected1 = "TmfEvent [fTimestamp=" + fTimestamp1 + ", fTrace=" + fTrace +
                ", fRank=0, fType=" + fType + ", fContent=" + fContent1 +  "]";
        assertEquals("toString", expected1, fEvent1.toString());

        final String expected2 = "TmfEvent [fTimestamp=" + fTimestamp2 + ", fTrace=" + fTrace +
                ", fRank=1, fType=" + fType + ", fContent=" + fContent2 + "]";
        assertEquals("toString", expected2, fEvent2.toString());
    }

    /**
     * Test the .toString() with extended classes.
     * It should print the correct class name.
     */
    @Test
    public void testToStringExtended() {
        class ExtendedEvent extends TmfEvent {
            ExtendedEvent(@NonNull ITmfEvent event) {
                super(event);
            }
        }
        ExtendedEvent event = new ExtendedEvent(fEvent1);
        String expected = "ExtendedEvent [fTimestamp=" + fTimestamp1
                + ", fTrace=" + fTrace + ", fRank=0"
                + ", fType=" + fType + ", fContent=" + fContent1 + "]";

        assertEquals(expected, event.toString());
    }

}
