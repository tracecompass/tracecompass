package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
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
    public void setUp() throws FileNotFoundException {
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
        String fieldName = "id"; //$NON-NLS-1$
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

        assertArrayEquals(null, fields);
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
        assertArrayEquals(null, nullEvent.getContent().getFields());
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
}
