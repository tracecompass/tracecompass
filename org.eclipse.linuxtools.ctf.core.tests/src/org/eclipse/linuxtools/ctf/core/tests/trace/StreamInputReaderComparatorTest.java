package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputReaderComparator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
}
