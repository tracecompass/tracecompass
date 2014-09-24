/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adjusted for new Event Model
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.Test;

/**
 * Test suite for the TmfEvent class.
 */
@SuppressWarnings("javadoc")
public class TmfEventTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final String fSource = "Source";

    private final String fContext = ITmfEventType.DEFAULT_CONTEXT_ID;
    private final String fTypeId = "TestType";
    private final String fLabel1 = "AString";
    private final String fLabel2 = "AnInteger";
    private final String[] fLabels = new String[] { fLabel1, fLabel2 };
    private final TmfEventType fType = new TmfEventType(fContext, fTypeId, TmfEventField.makeRoot(fLabels));

    private final Object fValue1a = "Some string";
    private final Object fValue1b = Integer.valueOf(10);
    private final ITmfEventField fField1a = new TmfEventField(fLabel1, fValue1a, null);
    private final ITmfEventField fField1b = new TmfEventField(fLabel2, fValue1b, null);
    private final ITmfEventField[] fFields1 = new ITmfEventField[] { fField1a, fField1b };
    private final String fRawContent1 = fField1a.toString() + fField1b.toString();
    private final ITmfEventField fContent1 = new TmfEventField(fRawContent1, null, fFields1);
    private final TmfTimestamp fTimestamp1 = new TmfTimestamp(12345, 2, 5);
    private final String fReference1 = "Some reference";
    private final ITmfEvent fEvent1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);

    private final Object fValue2a = "Another string";
    private final Object fValue2b = Integer.valueOf(-4);
    private final ITmfEventField fField2a = new TmfEventField(fLabel1, fValue2a, null);
    private final ITmfEventField fField2b = new TmfEventField(fLabel2, fValue2b, null);
    private final ITmfEventField[] fFields2 = new ITmfEventField[] { fField2a, fField2b };
    private final String fRawContent2 = fField2a.toString() + fField2b.toString();
    private final ITmfEventField fContent2 = new TmfEventField(fRawContent2, null, fFields2);
    private final TmfTimestamp fTimestamp2 = new TmfTimestamp(12350, 2, 5);
    private final String fReference2 = "Some other reference";
    private final ITmfEvent fEvent2 = new TmfEvent(null, 1, fTimestamp2, fSource, fType, fContent2, fReference2);

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private static TmfTraceStub openTrace() {
        TmfTraceStub trace = null;
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TmfTestTrace.A_TEST_10K.getFullPath()), null);
            final File test = new File(FileLocator.toFileURL(location).toURI());
            trace = new TmfTraceStub(test.toURI().getPath(), 500, false, null);
        } catch (final TmfTraceException e) {
            e.printStackTrace();
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return trace;
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testDefaultConstructor() {
        final ITmfEvent event = new TmfEvent();
        assertNull("getTrace", event.getTrace());
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, event.getRank());
        assertNull("getTimestamp", event.getTimestamp());
        assertNull("getSource", event.getSource());
        assertNull("getType", event.getType());
        assertNull("getContent", event.getContent());
        assertNull("getReference", event.getReference());
    }

    @Test
    public void testFullConstructor() {
        assertNull("getTrace", fEvent1.getTrace());
        assertEquals("getRank", 0, fEvent1.getRank());
        assertEquals("getTimestamp", fTimestamp1, fEvent1.getTimestamp());
        assertEquals("getSource", fSource, fEvent1.getSource());
        assertEquals("getType", fType, fEvent1.getType());
        assertEquals("getContent", fContent1, fEvent1.getContent());
        assertEquals("getReference", fReference1, fEvent1.getReference());

        assertNull("getTrace", fEvent2.getTrace());
        assertEquals("getRank", 1, fEvent2.getRank());
        assertEquals("getTimestamp", fTimestamp2, fEvent2.getTimestamp());
        assertEquals("getSource", fSource, fEvent2.getSource());
        assertEquals("getType", fType, fEvent2.getType());
        assertEquals("getContent", fContent2, fEvent2.getContent());
        assertEquals("getReference", fReference2, fEvent2.getReference());
    }

    @Test
    public void testNoRankConstructor() {
        final ITmfEvent event = new TmfEvent(null, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertNull("getTrace", event.getTrace());
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, event.getRank());
        assertEquals("getTimestamp", fTimestamp1, event.getTimestamp());
        assertEquals("getSource", fSource, event.getSource());
        assertEquals("getType", fType, event.getType());
        assertEquals("getContent", fContent1, event.getContent());
        assertEquals("getReference", fReference1, event.getReference());
    }

    @Test
    public void testConstructorWithTrace() {
        final ITmfTrace trace = openTrace();
        final ITmfEvent event = new TmfEvent(trace, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertNotNull("getTrace", event.getTrace());
        assertEquals("getRank", 0, event.getRank());
        assertEquals("getTimestamp", fTimestamp1, event.getTimestamp());
        assertEquals("getSource", fSource, event.getSource());
        assertEquals("getType", fType, event.getType());
        assertEquals("getContent", fContent1, event.getContent());
        assertEquals("getReference", fReference1, event.getReference());
        trace.dispose();
    }

    @Test
    public void testTmfEventCopy() {
        final ITmfEvent event = new TmfEvent(fEvent1);
        assertNull("getTrace", event.getTrace());
        assertEquals("getRank", 0, event.getRank());
        assertEquals("getTimestamp", fTimestamp1, event.getTimestamp());
        assertEquals("getSource", fSource, event.getSource());
        assertEquals("getType", fType, event.getType());
        assertEquals("getContent", fContent1, event.getContent());
        assertEquals("getReference", fReference1, event.getReference());
    }

    @Test
    public void testEventCopy2() {
        try {
            new TmfEvent(null);
            fail("null copy");
        } catch (final IllegalArgumentException e) {
            // Success
        }
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        ITmfEvent event1 = new TmfEvent();
        ITmfEvent event2 = new TmfEvent();

        assertTrue("hashCode", event1.hashCode() == event2.hashCode());

        final ITmfTrace trace = openTrace();
        event1 = new TmfEvent(trace, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        event2 = new TmfEvent(trace, 1, fTimestamp2, fSource, fType, fContent2, fReference2);
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
        final ITmfTrace trace1 = openTrace();
        final ITmfTrace trace2 = openTrace();

        final ITmfEvent event1 = new TmfEvent(trace1, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        ITmfEvent event2 = new TmfEvent(trace1,  0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(null,  0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        event2 = new TmfEvent(trace2,  0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        trace1.dispose();
        trace2.dispose();
    }

    @Test
    public void testNonEqualRanks() {
        final ITmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        ITmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 1, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    @Test
    public void testNonEqualTimestamps() {
        final ITmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        ITmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp2, fSource, fType, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, null, fSource, fType, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    @Test
    public void testNonEqualSources() {
        final ITmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        ITmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, fSource + "x", fType, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, null, fType, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    @Test
    public void testNonEqualTypes() {
        final ITmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        ITmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType,  fContent1, fReference1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        final String typeId = "OtherTestType";
        final String[] labels = new String[] { fLabel2, fLabel1 };
        final TmfEventType newType = new TmfEventType(fContext, typeId, TmfEventField.makeRoot(labels));

        event2 = new TmfEvent(null, 0, fTimestamp1, fSource, newType, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, fSource, null, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    @Test
    public void testNonEqualContents() {
        final ITmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        ITmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent2, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, null, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    @Test
    public void testNonEqualReferences() {
        final ITmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        ITmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference2);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, null);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        final String expected1 = "TmfEvent [fTimestamp=" + fTimestamp1 + ", fTrace=null, fRank=0, fSource=" + fSource
                + ", fType=" + fType + ", fContent=" + fContent1 + ", fReference=" + fReference1 + "]";
        assertEquals("toString", expected1, fEvent1.toString());

        final String expected2 = "TmfEvent [fTimestamp=" + fTimestamp2 + ", fTrace=null, fRank=1, fSource=" + fSource
                + ", fType=" + fType + ", fContent=" + fContent2 + ", fReference=" + fReference2 + "]";
        assertEquals("toString", expected2, fEvent2.toString());
    }

    /**
     * Test the .toString() with extended classes.
     * It should print the correct class name.
     */
    @Test
    public void testToStringExtended() {
        class ExtendedEvent extends TmfEvent {
            ExtendedEvent(ITmfEvent event) {
                super(event);
            }
        }
        ExtendedEvent event = new ExtendedEvent(fEvent1);
        String expected = "ExtendedEvent [fTimestamp=" + fTimestamp1
                + ", fTrace=null, fRank=0, fSource=" + fSource
                + ", fType=" + fType + ", fContent=" + fContent1
                + ", fReference=" + fReference1 + "]";

        assertEquals(expected, event.toString());
    }

}
