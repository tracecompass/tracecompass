package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.ctf.core.trace.StreamInputPacketIndexEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputPacketIndexEntryTest</code> contains tests for the
 * class <code>{@link StreamInputPacketIndexEntry}</code>.
 * 
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StreamInputPacketIndexEntryTest {

    private StreamInputPacketIndexEntry fixture;

    /**
     * Launch the test.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(StreamInputPacketIndexEntryTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new StreamInputPacketIndexEntry(1L);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the StreamInputPacketIndexEntry(long) constructor test.
     */
    @Test
    public void testStreamInputPacketIndexEntry_1() {
        String expectedResult = "PacketIndexEntry [offset=1, " + //$NON-NLS-1$
                "timestampBegin=0, timestampEnd=0, " + //$NON-NLS-1$
                "dataOffset=0, packetSize=0, contentSize=0]"; //$NON-NLS-1$

        assertNotNull(fixture);
        assertEquals(expectedResult, fixture.toString());
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String expectedResult = "PacketIndexEntry [offset=1, " + //$NON-NLS-1$
                "timestampBegin=1, timestampEnd=1, " + //$NON-NLS-1$
                "dataOffset=1, packetSize=1, contentSize=1]"; //$NON-NLS-1$

        fixture.contentSizeBits = 1;
        fixture.dataOffsetBits = 1;
        fixture.timestampEnd = 1L;
        fixture.packetSizeBits = 1;
        fixture.timestampBegin = 1L;

        assertEquals(expectedResult, fixture.toString());
    }
}