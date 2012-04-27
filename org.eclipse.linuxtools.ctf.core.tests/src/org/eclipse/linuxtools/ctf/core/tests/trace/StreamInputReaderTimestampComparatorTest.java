package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.channels.FileChannel;

import org.eclipse.linuxtools.ctf.core.tests.TestParams;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;
import org.eclipse.linuxtools.internal.ctf.core.trace.Stream;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInput;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputReaderTimestampComparator;
import org.junit.*;

/**
 * The class <code>StreamInputReaderTimestampComparatorTest</code> contains
 * tests for the class <code>{@link StreamInputReaderTimestampComparator}</code>
 * 
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StreamInputReaderTimestampComparatorTest {

    private StreamInputReaderTimestampComparator fixture;

    /**
     * Launch the test.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(StreamInputReaderTimestampComparatorTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new StreamInputReaderTimestampComparator();
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the StreamInputReaderTimestampComparator() constructor test.
     */
    @Test
    public void testStreamInputReaderTimestampComparator_1() {
        assertNotNull(fixture);
    }

    /**
     * Run the int compare(StreamInputReader,StreamInputReader) method test.
     * 
     * @throws CTFReaderException 
     */
    @Test
    public void testCompare() throws CTFReaderException {
        StreamInputReader a, b;
        a = new StreamInputReader(new StreamInput(new Stream(
                TestParams.createTrace()), (FileChannel) null,
                TestParams.getEmptyFile()));
        a.setCurrentEvent(null);
        b = new StreamInputReader(new StreamInput(new Stream(
                TestParams.createTrace()), (FileChannel) null,
                TestParams.getEmptyFile()));

        int result = fixture.compare(a, b);
        assertEquals(0, result);
    }
}
