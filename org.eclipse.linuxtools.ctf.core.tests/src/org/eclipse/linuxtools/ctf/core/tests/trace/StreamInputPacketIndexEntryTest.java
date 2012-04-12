package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndexEntry;
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
        String expectedResult = "StreamInputPacketIndexEntry [offsetBytes=1, " + //$NON-NLS-1$
                "timestampBegin=0, timestampEnd=0, " + //$NON-NLS-1$
                "indexBegin=9223372036854775807, indexEnd=9223372036854775807]"; //$NON-NLS-1$

        assertNotNull(fixture);
        assertEquals(expectedResult, fixture.toString());
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String expectedResult = "StreamInputPacketIndexEntry [offsetBytes=1,"+ //$NON-NLS-1$
                " timestampBegin=1, timestampEnd=1, indexBegin=9223372036854775807,"+ //$NON-NLS-1$
                " indexEnd=9223372036854775807]"; //$NON-NLS-1$

        fixture.setContentSizeBits(1);
        fixture.setDataOffsetBits(1);
        fixture.setTimestampEnd(1L);
        fixture.setPacketSizeBits(1);
        fixture.setTimestampBegin(1L);

        assertEquals(expectedResult, fixture.toString());
    }
}