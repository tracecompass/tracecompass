package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.channels.FileChannel;
import java.util.Set;

import org.eclipse.linuxtools.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.tests.TestParams;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.Stream;
import org.eclipse.linuxtools.ctf.core.trace.StreamInput;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputReaderTest</code> contains tests for the class
 * <code>{@link StreamInputReader}</code>.
 * 
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StreamInputReaderTest {

    private StreamInputReader fixture;

    /**
     * Launch the test.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(StreamInputReaderTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = createStreamInputReader();
        fixture.setName(1);
        fixture.setCurrentEvent(new EventDefinition(new EventDeclaration(),
                createStreamInputReader()));
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    private static StreamInputReader createStreamInputReader() {
        CTFTrace trace = TestParams.createTrace();
        Stream s = trace.getStream((long) 0);
        Set<StreamInput> streamInput = s.getStreamInputs();
        StreamInputReader retVal = null;
        for (StreamInput si : streamInput) {
            /*
             * For the tests, we'll use the stream input corresponding to the
             * CPU 0
             */
            if (si.getFilename().endsWith("0_0")) { //$NON-NLS-1$
                retVal = new StreamInputReader(si);
                break;
            }
        }
        return retVal;
    }

    /**
     * Run the StreamInputReader(StreamInput) constructor test, with a valid
     * trace.
     */
    @Test
    public void testStreamInputReader_valid() {
        assertNotNull(fixture);
    }

    /**
     * Run the StreamInputReader(StreamInput) constructor test, with an invalid
     * trace.
     * 
     * @throws CTFReaderException
     */
    @Test(expected = CTFReaderException.class)
    public void testStreamInputReader_invalid() throws CTFReaderException {
        StreamInput streamInput = new StreamInput(
                new Stream(new CTFTrace("")), (FileChannel) null, TestParams.getEmptyFile()); //$NON-NLS-1$

        StreamInputReader result = new StreamInputReader(streamInput);
        assertNotNull(result);
    }

    /**
     * Run the int getCPU() method test.
     */
    @Test
    public void testGetCPU() {
        int result = fixture.getCPU();
        assertEquals(0, result);
    }

    /**
     * Run the EventDefinition getCurrentEvent() method test.
     */
    @Test
    public void testGetCurrentEvent() {
        EventDefinition result = fixture.getCurrentEvent();
        assertNotNull(result);
    }

    /**
     * Run the StructDefinition getCurrentPacketContext() method test.
     */
    @Test
    public void testGetCurrentPacketContext() {
        StructDefinition result = fixture.getCurrentPacketContext();
        assertNotNull(result);
    }

    /**
     * Run the int getName() method test.
     */
    @Test
    public void testGetName() {
        int result = fixture.getName();
        assertEquals(1, result);
    }

    /**
     * Run the StreamInput getStreamInput() method test.
     */
    @Test
    public void testGetStreamInput() {
        StreamInput result = fixture.getStreamInput();
        assertNotNull(result);
    }

    /**
     * Run the void goToLastEvent() method test.
     * 
     * @throws CTFReaderException
     */
    @Test
    public void testGoToLastEvent() throws CTFReaderException {
        fixture.goToLastEvent();
    }

    /**
     * Run the boolean readNextEvent() method test.
     */
    @Test
    public void testReadNextEvent() {
        boolean result = fixture.readNextEvent();
        assertTrue(result);
    }

    /**
     * Run the void seek(long) method test. Seek by direct timestamp
     */
    @Test
    public void testSeek_timestamp() {
        long timestamp = 1L;
        fixture.seek(timestamp);
    }

    /**
     * Run the seek test. Seek by passing an EventDefinition to which we've
     * given the timestamp we want.
     */
    @Test
    public void testSeek_eventDefinition() {
        EventDefinition eventDefinition = new EventDefinition(
                new EventDeclaration(), createStreamInputReader());
        eventDefinition.timestamp = 1L;
        fixture.setCurrentEvent(eventDefinition);
    }
}