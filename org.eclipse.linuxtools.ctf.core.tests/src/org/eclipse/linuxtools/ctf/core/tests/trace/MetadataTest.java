package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.tests.TestParams;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.Metadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>MetadataTest</code> contains tests for the class
 * <code>{@link Metadata}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class MetadataTest {

    private Metadata fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(MetadataTest.class);
    }

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        assumeTrue(TestParams.tracesExist());
        fixture = new Metadata(TestParams.createTrace());
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the Metadata(CTFTrace) constructor test.
     */
    @Test
    public void testMetadata() {
        assertNotNull(fixture);
    }

    /**
     * Run the ByteOrder getDetectedByteOrder() method test.
     */
    @Test
    public void testGetDetectedByteOrder() {
        ByteOrder result = fixture.getDetectedByteOrder();
        assertNull(result);
    }

    /**
     * Test toString
     */
    @Test
    public void testToSting() {
        String result = fixture.toString();
        assertNotNull(result);
    }

    /**
     * Run the void parse() method test.
     *
     * @throws CTFReaderException
     */
    @Test(expected = org.eclipse.linuxtools.ctf.core.trace.CTFReaderException.class)
    public void testParse() throws CTFReaderException {
        fixture.parse();
    }
}
