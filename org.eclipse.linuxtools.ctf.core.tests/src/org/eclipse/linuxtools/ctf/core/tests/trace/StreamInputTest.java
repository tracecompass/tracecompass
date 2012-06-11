package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.tests.TestParams;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.internal.ctf.core.trace.Stream;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInput;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputTest</code> contains tests for the class
 * <code>{@link StreamInput}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class StreamInputTest {

    private StreamInput fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(StreamInputTest.class);
    }

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        fixture = new StreamInput(new Stream(TestParams.createTrace()),
                (FileChannel) null, createFile());
        fixture.setTimestampEnd(1L);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    private static File createFile() {
        return new File("Tests/traces/trace20m/channel_0"); //$NON-NLS-1$
    }

    /**
     * Run the StreamInput(Stream,FileChannel,File) constructor test.
     */
    @Test
    public void testStreamInput() {
        assertNotNull(fixture);
    }

    /**
     * Run the FileChannel getFileChannel() method test.
     *
     * @throws IOException
     */
    @Test
    public void testGetFileChannel() throws IOException {
        FileChannel result = fixture.getFileChannel();
        assertNull(result);
        if (result != null) {
            result.close();
        }
    }

    /**
     * Run the String getFilename() method test.
     */
    @Test
    public void testGetFilename() {
        String result = fixture.getFilename();
        assertNotNull(result);
    }

    /**
     * Run the StreamInputPacketIndex getIndex() method test.
     */
    @Test
    public void testGetIndex() {
        StreamInputPacketIndex result = fixture.getIndex();
        assertNotNull(result);
    }

    /**
     * Run the String getPath() method test.
     */
    @Test
    public void testGetPath() {
        String result = fixture.getPath();
        assertNotNull(result);
    }

    /**
     * Run the Stream getStream() method test.
     */
    @Test
    public void testGetStream() {
        Stream result = fixture.getStream();
        assertNotNull(result);
    }

    /**
     * Run the long getTimestampEnd() method test.
     */
    @Test
    public void testGetTimestampEnd() {
        long result = fixture.getTimestampEnd();
        assertTrue(0L < result);
    }

    /**
     * Run the Definition lookupDefinition(String) method test.
     */
    @Test
    public void testLookupDefinition() {
        Definition result = fixture.lookupDefinition("id"); //$NON-NLS-1$
        assertNull(result);
    }

    /**
     * Run the void setTimestampEnd(long) method test.
     */
    @Test
    public void testSetTimestampEnd() {
        fixture.setTimestampEnd(1L);
        assertEquals(fixture.getTimestampEnd(), 1L);
    }

    StreamInput s1;
    StreamInput s2;


    @Test
    public void testEquals1() throws CTFReaderException{
        s1 = new StreamInput(new Stream(TestParams.createTrace()),
                (FileChannel) null, createFile());
        assertFalse(s1.equals(null));
    }

    @Test
    public void testEquals2() throws CTFReaderException{
        s1 = new StreamInput(new Stream(TestParams.createTrace()),
                (FileChannel) null, createFile());
        assertFalse(s1.equals(new Long(23L)));

    }
    @Test
    public void testEquals3() throws CTFReaderException{
        s1 = new StreamInput(new Stream(TestParams.createTrace()),
                (FileChannel) null, createFile());
        assertEquals(s1,s1);

    }
    @Test
    public void testEquals4() throws CTFReaderException{
        s1 = new StreamInput(new Stream(TestParams.createTrace()),
                (FileChannel) null, createFile());
        s2 = new StreamInput(new Stream(TestParams.createTrace()),
                (FileChannel) null, createFile());
        assertEquals(s1,s2);
    }
}