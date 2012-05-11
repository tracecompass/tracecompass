package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CTFEventTest</code> contains tests for the class
 * <code>{@link CTFEvent}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CtfTmfEventTest {

    private CtfTmfEvent fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CtfTmfEventTest.class);
    }

    /**
     * Perform pre-test initialization.
     *
     * @throws FileNotFoundException
     */
    @Before
    public void setUp() throws TmfTraceException {
        CtfTmfTrace trace = TestParams.createTrace();
        CtfIterator tr = new CtfIterator(trace);
        tr.advance();
        fixture = tr.getCurrentEvent();
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the CTFEvent(EventDefinition,StreamInputReader) constructor test.
     */
    @Test
    public void testCTFEvent_read() {
        assertNotNull(fixture);
    }

    /**
     * Run the int getCPU() method test.
     */
    @Test
    public void testGetCPU() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();
        int result = nullEvent.getCPU();

        assertEquals(-1, result);
    }

    /**
     * Run the String getChannelName() method test.
     */
    @Test
    public void testGetChannelName() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();
        String result = nullEvent.getChannelName();

        assertEquals("No stream", result); //$NON-NLS-1$
    }

    /**
     * Run the String getEventName() method test.
     */
    @Test
    public void testGetEventName() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();
        String result = nullEvent.getEventName();

        assertEquals("Empty CTF event", result); //$NON-NLS-1$
    }

    /**
     * Run the ArrayList<String> getFieldNames() method test.
     */
    @Test
    public void testGetFieldNames() {
        String[] result = fixture.getContent().getFieldNames();
        assertNotNull(result);
    }

    /**
     * Run the Object getFieldValue(String) method test.
     */
    @Test
    public void testGetFieldValue() {
        String fieldName = "ret"; //$NON-NLS-1$
        Object result = fixture.getContent().getField(fieldName).getValue();

        assertNotNull(result);
    }

    /**
     * Run the HashMap<String, CTFEventField> getFields() method test.
     */
    @Test
    public void testGetFields() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();
        ITmfEventField[] fields = nullEvent.getContent().getFields();
        ITmfEventField[] fields2 = new ITmfEventField[0];
        assertArrayEquals(fields2, fields);
    }

    /**
     * Run the long getID() method test.
     */
    @Test
    public void testGetID() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();
        long result = nullEvent.getID();

        assertEquals(-1L, result);
    }

    @Test
    public void testClone() {
        CtfTmfEvent other = CtfTmfEvent.getNullEvent().clone();
        assertNotNull(other);
    }

    /**
     * Run the CTFEvent getNullEvent() method test.
     */
    @Test
    public void testGetNullEvent() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();

        assertNotNull(nullEvent);
        assertEquals(-1, nullEvent.getCPU());
        assertEquals("Empty CTF event", nullEvent.getEventName()); //$NON-NLS-1$
        assertEquals("No stream", nullEvent.getChannelName()); //$NON-NLS-1$
        assertArrayEquals(new ITmfEventField[0], nullEvent.getContent().getFields());
        assertEquals(-1L, nullEvent.getID());
        assertEquals(-1L, nullEvent.getTimestampValue());
    }

    /**
     * Run the long getTimestamp() method test.
     *
     */
    @Test
    public void testGetTimestamp() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();
        long result = nullEvent.getTimestampValue();

        assertEquals(-1L, result);
    }

    @Test
    public void testRankTraceRefSourceType() {
        long rank = fixture.getRank();
        CtfTmfTrace trace = fixture.getTrace();
        String channelName = fixture.getChannelName();
        String reference = fixture.getReference();
        String source = fixture.getSource();
        ITmfEventType type = fixture.getType();
        assertEquals(rank, 0);
        assertEquals(trace.getName(), "kernel"); //$NON-NLS-1$
        assertEquals(channelName, "channel0_1"); //$NON-NLS-1$
        assertEquals(reference,"channel0_1"); //$NON-NLS-1$
        assertEquals(source, "1"); //$NON-NLS-1$
        assertEquals(type.toString(), "exit_syscall"); //$NON-NLS-1$
    }

    @Test
    public void TestToString() {
        String s = fixture.getContent().toString();
        assertEquals("ret=4132", s); //$NON-NLS-1$
    }
}
