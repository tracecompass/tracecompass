/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adjusted for new Event Model
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.event;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * <b><u>TmfEventTest</u></b>
 * <p>
 * Test suite for the TmfEvent class.
 */
@SuppressWarnings("nls")
public class TmfEventTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final String fSource = "Source";

    private final String fContext = TmfEventType.DEFAULT_CONTEXT_ID;
    private final String fTypeId = "TestType";
    private final String fLabel1 = "AString";
    private final String fLabel2 = "AnInteger";
    private final String[] fLabels = new String[] { fLabel1, fLabel2 };
    private final TmfEventType fType = new TmfEventType(fContext, fTypeId, TmfEventField.makeRoot(fLabels));

    private final Object fValue1a = "Some string";
    private final Object fValue1b = Integer.valueOf(10);
    private final ITmfEventField fField1a = new TmfEventField(fLabel1, fValue1a);
    private final ITmfEventField fField1b = new TmfEventField(fLabel2, fValue1b);
    private final ITmfEventField[] fFields1 = new ITmfEventField[] { fField1a, fField1b };
    private final String fRawContent1 = fField1a.toString() + fField1b.toString();
    private final ITmfEventField fContent1 = new TmfEventField(fRawContent1, fFields1);
    private final TmfTimestamp fTimestamp1 = new TmfTimestamp(12345, 2, 5);
    private final String fReference1 = "Some reference";
    private final ITmfEvent fEvent1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);

    private final Object fValue2a = "Another string";
    private final Object fValue2b = Integer.valueOf(-4);
    private final ITmfEventField fField2a = new TmfEventField(fLabel1, fValue2a);
    private final ITmfEventField fField2b = new TmfEventField(fLabel2, fValue2b);
    private final ITmfEventField[] fFields2 = new ITmfEventField[] { fField2a, fField2b };
    private final String fRawContent2 = fField2a.toString() + fField2b.toString();
    private final ITmfEventField fContent2 = new TmfEventField(fRawContent2, fFields2);
    private final TmfTimestamp fTimestamp2 = new TmfTimestamp(12350, 2, 5);
    private final String fReference2 = "Some other reference";
    private final ITmfEvent fEvent2 = new TmfEvent(null, 1, fTimestamp2, fSource, fType, fContent2, fReference2);

    private final String fTracePath = "testfiles" + File.separator + "A-Test-10K";

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
    public TmfEventTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private TmfTraceStub openTrace() {
        final String DIRECTORY = "testfiles";
        final String TEST_STREAM = "A-Test-10K";
        final String path = DIRECTORY + File.separator + TEST_STREAM;

        TmfTraceStub trace = null;
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path), null);
            final File test = new File(FileLocator.toFileURL(location).toURI());
            trace = new TmfTraceStub(test.toURI().getPath(), 500, false);
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

    public void testDefaultConstructor() {
        final ITmfEvent event = new TmfEvent();
        assertNull("getTrace", event.getTrace());
        assertEquals("getRank", TmfContext.UNKNOWN_RANK, event.getRank());
        assertNull("getTimestamp", event.getTimestamp());
        assertNull("getSource", event.getSource());
        assertNull("getType", event.getType());
        assertNull("getContent", event.getContent());
        assertNull("getReference", event.getReference());
    }

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

    public void testNoRankConstructor() {
        final TmfEvent event = new TmfEvent(null, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertNull("getTrace", event.getTrace());
        assertEquals("getRank", TmfContext.UNKNOWN_RANK, event.getRank());
        assertEquals("getTimestamp", fTimestamp1, event.getTimestamp());
        assertEquals("getSource", fSource, event.getSource());
        assertEquals("getType", fType, event.getType());
        assertEquals("getContent", fContent1, event.getContent());
        assertEquals("getReference", fReference1, event.getReference());
    }

    public void testConstructorWithTrace() {
        final ITmfTrace<TmfEvent> trace = openTrace();
        final TmfEvent event = new TmfEvent(trace, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertNotNull("getTrace", event.getTrace());
        assertEquals("getRank", 0, event.getRank());
        assertEquals("getTimestamp", fTimestamp1, event.getTimestamp());
        assertEquals("getSource", fSource, event.getSource());
        assertEquals("getType", fType, event.getType());
        assertEquals("getContent", fContent1, event.getContent());
        assertEquals("getReference", fReference1, event.getReference());
        trace.dispose();
    }

    public void testTmfEventCopy() {
        final TmfEvent event = new TmfEvent(fEvent1);
        assertNull("getTrace", event.getTrace());
        assertEquals("getRank", 0, event.getRank());
        assertEquals("getTimestamp", fTimestamp1, event.getTimestamp());
        assertEquals("getSource", fSource, event.getSource());
        assertEquals("getType", fType, event.getType());
        assertEquals("getContent", fContent1, event.getContent());
        assertEquals("getReference", fReference1, event.getReference());
    }

    public void testEventCopy2() throws Exception {
        try {
            new TmfEvent(null);
            fail("null copy");
        } catch (final IllegalArgumentException e) {
            // Success
        }
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    private static class TestEvent extends TmfEvent {

        public TestEvent(final ITmfEvent event) {
            super(event);
        }

        @Override
        public void setTrace(final ITmfTrace<? extends ITmfEvent> trace) {
            super.setTrace(trace);
        }

        @Override
        public void setRank(final long rank) {
            super.setRank(rank);
        }

        @Override
        public void setTimestamp(final ITmfTimestamp timestamp) {
            super.setTimestamp(timestamp);
        }

        @Override
        public void setSource(final String source) {
            super.setSource(source);
        }

        @Override
        public void setType(final ITmfEventType type) {
            super.setType(type);
        }

        @Override
        public void setContent(final ITmfEventField content) {
            super.setContent(content);
        }

        @Override
        public void setReference(final String reference) {
            super.setReference(reference);
        }

    }

    private ITmfTrace<TmfEvent> setupTrace() {
        ITmfTrace<TmfEvent> trace = null;
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(fTracePath), null);
            final File test = new File(FileLocator.toFileURL(location).toURI());
            trace = new TmfTraceStub(test.toURI().getPath(), 500, false);
        } catch (final TmfTraceException e) {
            e.printStackTrace();
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return trace;
    }

    public void testSetTrace() {
        final ITmfTrace<TmfEvent> trace = setupTrace();
        assertNotNull(trace);

        final TestEvent event = new TestEvent(fEvent1);
        assertNull("setTrace", event.getTrace());

        event.setTrace(trace);
        assertEquals("setTrace", trace, event.getTrace());

        event.setTrace(null);
        assertNull("setTrace", event.getTrace());

        trace.dispose();
    }

    public void testSetRank() {
        final TestEvent event = new TestEvent(fEvent1);
        assertEquals("setRank", 0, event.getRank());

        event.setRank(1);
        assertEquals("setRank", 1, event.getRank());

        event.setRank(-1);
        assertEquals("setRank", -1, event.getRank());

        event.setRank(0);
        assertEquals("setRank", 0, event.getRank());
    }

    public void testSetTimestamp() {
        final TestEvent event = new TestEvent(fEvent1);
        assertEquals("setTimestamp", fTimestamp1, event.getTimestamp());

        event.setTimestamp(fTimestamp2);
        assertEquals("setTimestamp", fTimestamp2, event.getTimestamp());

        event.setTimestamp(null);
        assertNull("setTimestamp", event.getTimestamp());

        event.setTimestamp(fTimestamp1);
        assertEquals("setTimestamp", fTimestamp1, event.getTimestamp());
    }

    public void testSetSource() {
        final TestEvent event = new TestEvent(fEvent1);
        assertEquals("setSource", fSource, event.getSource());

        final String source2 = "another source";
        event.setSource(source2);
        assertEquals("setContent", source2, event.getSource());

        event.setSource(null);
        assertNull("setContent", event.getSource());

        event.setSource(fSource);
        assertEquals("setContent", fSource, event.getSource());
    }

    public void testSetType() {
        final TestEvent event = new TestEvent(fEvent1);
        assertEquals("setType", fType, event.getType());

        final String typeId = "OtherTestType";
        final String[] labels = new String[] { fLabel2, fLabel1 };
        final TmfEventType newType = new TmfEventType(fContext, typeId, TmfEventField.makeRoot(labels));

        event.setType(newType);
        assertEquals("setType", newType, event.getType());

        event.setType(null);
        assertNull("setType", event.getType());

        event.setType(fType);
        assertEquals("setType", fType, event.getType());
    }

    public void testSetContent() {
        final TestEvent event = new TestEvent(fEvent1);
        assertEquals("setContent", fContent1, event.getContent());

        event.setContent(fContent2);
        assertEquals("setContent", fContent2, event.getContent());

        event.setContent(null);
        assertNull("setContent", event.getContent());

        event.setContent(fContent1);
        assertEquals("setContent", fContent1, event.getContent());
    }

    public void testSetReference() {
        final TestEvent event = new TestEvent(fEvent1);
        assertEquals("setReference", fReference1, event.getReference());

        event.setReference(fReference2);
        assertEquals("setReference", fReference2, event.getReference());

        event.setReference(null);
        assertNull("setReference", event.getReference());

        event.setReference(fReference1);
        assertEquals("setReference", fReference1, event.getReference());
    }

    // ------------------------------------------------------------------------
    // clone
    // ------------------------------------------------------------------------

    public static class MyEvent extends TmfEvent {

        @Override
        public boolean equals(final Object other) {
            return super.equals(other);
        }
        @Override
        public MyEvent clone() {
            return (MyEvent) super.clone();
        }
    }

    public void testClone1() throws Exception {
        final ITmfEvent clone = fEvent1.clone();

        assertTrue("clone", fEvent1.clone().equals(fEvent1));
        assertTrue("clone", clone.clone().equals(clone));

        assertEquals("clone", fEvent1, clone);
        assertEquals("clone", clone, fEvent1);
    }

    public void testClone2() throws Exception {
        final TmfEvent event = new MyEvent();
        final TmfEvent clone = event.clone();

        assertTrue("clone", event.clone().equals(event));
        assertTrue("clone", clone.clone().equals(clone));

        assertEquals("clone", event, clone);
        assertEquals("clone", clone, event);
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    public void testHashCode() throws Exception {
        TmfEvent event1 = new TmfEvent();
        TmfEvent event2 = new TmfEvent();

        assertTrue("hashCode", event1.hashCode() == event2.hashCode());

        final ITmfTrace<TmfEvent> trace = openTrace();
        event1 = new TmfEvent(trace, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        event2 = new TmfEvent(trace, 1, fTimestamp2, fSource, fType, fContent2, fReference2);
        final TmfEvent event1b = new TmfEvent(event1);
        final TmfEvent event2b = new TmfEvent(event2);

        assertTrue("hashCode", event1.hashCode() == event1b.hashCode());
        assertTrue("hashCode", event2.hashCode() == event2b.hashCode());

        assertTrue("hashCode", event1.hashCode() != event2.hashCode());
        assertTrue("hashCode", event2.hashCode() != event1.hashCode());

        trace.dispose();
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    public void testEqualsReflexivity() throws Exception {
        assertTrue("equals", fEvent1.equals(fEvent1));
        assertTrue("equals", fEvent2.equals(fEvent2));

        assertFalse("equals", fEvent1.equals(fEvent2));
        assertFalse("equals", fEvent2.equals(fEvent1));
    }

    public void testEqualsSymmetry() throws Exception {
        final TmfEvent event1 = new TmfEvent(fEvent1);
        final TmfEvent event2 = new TmfEvent(fEvent2);

        assertTrue("equals", event1.equals(fEvent1));
        assertTrue("equals", fEvent1.equals(event1));

        assertTrue("equals", event2.equals(fEvent2));
        assertTrue("equals", fEvent2.equals(event2));
    }

    public void testEqualsTransivity() throws Exception {
        final TmfEvent event1 = new TmfEvent(fEvent1);
        final TmfEvent event2 = new TmfEvent(fEvent1);
        final TmfEvent event3 = new TmfEvent(fEvent1);

        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event3));
        assertTrue("equals", event1.equals(event3));
    }

    public void testEqualsNull() throws Exception {
        assertFalse("equals", fEvent1.equals(null));
        assertFalse("equals", fEvent2.equals(null));
    }

    public void testNonEqualClasses() throws Exception {
        assertFalse("equals", fEvent1.equals(fEvent1.getType()));
        assertFalse("equals", fEvent1.equals(null));
    }

    public void testNonEqualTraces() throws Exception {
        final ITmfTrace<TmfEvent> trace1 = openTrace();
        final ITmfTrace<TmfEvent> trace2 = openTrace();

        final TmfEvent event1 = new TmfEvent(trace1, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        TmfEvent event2 = new TmfEvent(trace1,  0, fTimestamp1, fSource, fType, fContent1, fReference1);
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

    public void testNonEqualRanks() throws Exception {
        final TmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        TmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 1, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    public void testNonEqualTimestamps() throws Exception {
        final TmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        TmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp2, fSource, fType, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, null, fSource, fType, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    public void testNonEqualSources() throws Exception {
        final TmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        TmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, fSource + "x", fType, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, null, fType, fContent1, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    public void testNonEqualTypes() throws Exception {
        final TmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        TmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType,  fContent1, fReference1);
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

    public void testNonEqualContents() throws Exception {
        final TmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        TmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        assertTrue("equals", event1.equals(event2));
        assertTrue("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent2, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));

        event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, null, fReference1);
        assertFalse("equals", event1.equals(event2));
        assertFalse("equals", event2.equals(event1));
    }

    public void testNonEqualReferences() throws Exception {
        final TmfEvent event1 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
        TmfEvent event2 = new TmfEvent(null, 0, fTimestamp1, fSource, fType, fContent1, fReference1);
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

    public void testToString() {
        final String expected1 = "TmfEvent [fTimestamp=" + fTimestamp1 + ", fTrace=null, fRank=0, fSource=" + fSource
                + ", fType=" + fType + ", fContent=" + fContent1 + ", fReference=" + fReference1 + "]";
        assertEquals("toString", expected1, fEvent1.toString());

        final String expected2 = "TmfEvent [fTimestamp=" + fTimestamp2 + ", fTrace=null, fRank=1, fSource=" + fSource
                + ", fType=" + fType + ", fContent=" + fContent2 + ", fReference=" + fReference2 + "]";
        assertEquals("toString", expected2, fEvent2.toString());
    }

}
