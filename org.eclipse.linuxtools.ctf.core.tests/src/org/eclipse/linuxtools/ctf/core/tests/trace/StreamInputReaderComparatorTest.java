package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.channels.FileChannel;
import org.eclipse.linuxtools.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.tests.TestParams;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.Stream;
import org.eclipse.linuxtools.ctf.core.trace.StreamInput;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputReaderComparator;
import org.junit.*;

/**
 * The class <code>StreamInputReaderComparatorTest</code> contains tests for the
 * class <code>{@link StreamInputReaderComparator}</code>.
 * 
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StreamInputReaderComparatorTest {

    private StreamInputReaderComparator fixture;

    /**
     * Launch the test.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(StreamInputReaderComparatorTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new StreamInputReaderComparator();
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the StreamInputReaderComparator() constructor test.
     */
    @Test
    public void testStreamInputReaderComparator() {
        assertNotNull(fixture);
    }

    /**
     * Run the int compare(StreamInputReader,StreamInputReader) method test.
     * 
     * @throws CTFReaderException 
     */
    @Test
    public void testCompare() throws CTFReaderException {
        StreamInputReader sir1, sir2;
        EventDefinition ed1, ed2;

        sir1 = new StreamInputReader(new StreamInput(new Stream(
                TestParams.createTrace()), (FileChannel) null,
                TestParams.getEmptyFile()));
        ed1 = new EventDefinition(new EventDeclaration(),
                new StreamInputReader(new StreamInput(new Stream(
                        TestParams.createTrace()), (FileChannel) null,
                        TestParams.getEmptyFile())));
        ed1.timestamp = 1L;
        sir1.setCurrentEvent(ed1);

        sir2 = new StreamInputReader(new StreamInput(new Stream(
                TestParams.createTrace()), (FileChannel) null,
                TestParams.getEmptyFile()));
        ed2 = new EventDefinition(new EventDeclaration(),
                new StreamInputReader(new StreamInput(new Stream(
                        TestParams.createTrace()), (FileChannel) null,
                        TestParams.getEmptyFile())));

        ed2.timestamp = 1L;
        sir2.setCurrentEvent(ed2);

        int result = fixture.compare(sir1, sir2);
        assertEquals(0, result);
    }
}
