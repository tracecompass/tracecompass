package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.channels.FileChannel;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.tests.TestParams;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.Stream;
import org.eclipse.linuxtools.ctf.core.trace.StreamInput;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputPacketIndexEntry;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputPacketReader;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputPacketReaderTest</code> contains tests for the
 * class <code>{@link StreamInputPacketReader}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StreamInputPacketReaderTest {

    private StreamInputPacketReader fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(StreamInputPacketReaderTest.class);
    }

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        // FIXME The test is broken here. "FileChannel" can't be null because we
        // need it further in. Heck this whole thing shouldn't be public in the
        // first place, perhaps fixing that is the best way to go.
        fixture = new StreamInputPacketReader(new StreamInputReader(
                new StreamInput(new Stream(TestParams.createTrace()),
                        (FileChannel) null, TestParams.getEmptyFile())));
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the StreamInputPacketReader(StreamInputReader) constructor test.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testStreamInputPacketReader() throws CTFReaderException {
        StreamInputReader streamInputReader;
        StreamInputPacketReader result;

        streamInputReader = new StreamInputReader(new StreamInput(new Stream(
                TestParams.createTrace()), (FileChannel) null,
                TestParams.getEmptyFile()));

        result = new StreamInputPacketReader(streamInputReader);

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
     * Run the StreamInputPacketIndexEntry getCurrentPacket() method test.
     */
    @Test
    public void testGetCurrentPacket() {
        StreamInputPacketIndexEntry sipie = new StreamInputPacketIndexEntry(1L);
        fixture.setCurrentPacket(sipie);
        StreamInputPacketIndexEntry result = fixture.getCurrentPacket();
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
     * Run the StructDefinition getStreamPacketContextDef() method test.
     */
    @Test
    public void testGetStreamPacketContextDef() {
        fixture.setCurrentPacket(new StreamInputPacketIndexEntry(1L));
        StructDefinition result = fixture.getStreamPacketContextDef();
        assertNotNull(result);
    }

    /**
     * Run the boolean hasMoreEvents() method test.
     */
    @Test
    public void testHasMoreEvents() {
        fixture.setCurrentPacket(new StreamInputPacketIndexEntry(1L));
        boolean result = fixture.hasMoreEvents();
        assertTrue(result);
    }

    /**
     * Run the Definition lookupDefinition(String) method test.
     */
    @Test
    public void testLookupDefinition() {
        fixture.setCurrentPacket(new StreamInputPacketIndexEntry(1L));
        String lookupPath = ""; //$NON-NLS-1$
        Definition result = fixture.lookupDefinition(lookupPath);
        assertNotNull(result);
    }

    /**
     * Run the EventDefinition readNextEvent() method test.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testReadNextEvent() throws CTFReaderException {
        fixture.setCurrentPacket(new StreamInputPacketIndexEntry(1L));
        EventDefinition result = fixture.readNextEvent();
        assertNotNull(result);
    }

    /**
     * Run the void setCurrentPacket(StreamInputPacketIndexEntry) method test.
     */
    @Test
    public void testSetCurrentPacket() {
        fixture.setCurrentPacket(new StreamInputPacketIndexEntry(1L));
        StreamInputPacketIndexEntry currentPacket = new StreamInputPacketIndexEntry(
                1L);
        currentPacket.setPacketSizeBits(1);
        fixture.setCurrentPacket(currentPacket);
    }

    /**
     * Run the void setCurrentPacket(StreamInputPacketIndexEntry) method test.
     */
    @Test
    public void testSetCurrentPacket_2() throws Exception {
        fixture.setCurrentPacket(new StreamInputPacketIndexEntry(1L));
        StreamInputPacketIndexEntry currentPacket = null;
        fixture.setCurrentPacket(currentPacket);

    }

    /**
     * Run the void setCurrentPacket(StreamInputPacketIndexEntry) method test.
     */
    @Test
    public void testSetCurrentPacket_3() {
        fixture.setCurrentPacket(new StreamInputPacketIndexEntry(1L));
        StreamInputPacketIndexEntry currentPacket = new StreamInputPacketIndexEntry(
                1L);
        currentPacket.setTimestampBegin(1L);
        currentPacket.setPacketSizeBits(1);

        fixture.setCurrentPacket(currentPacket);
    }
}
