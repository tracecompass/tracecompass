package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfLocation;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class <code>CtfLocationTest</code> contains tests for the class <code>{@link CtfLocation}</code>.
 *
 * @generatedBy CodePro at 03/05/12 2:29 PM
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CtfLocationTest {
    /**
     * Run the CtfLocation(Long) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testCtfLocation_1()
        throws Exception {
        Long location = new Long(1L);

        CtfLocation result = new CtfLocation(location);

        // add additional test code here
        assertNotNull(result);
        assertEquals(new Long(1L), result.getLocation());
    }

    /**
     * Run the CtfLocation(ITmfTimestamp) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testCtfLocation_2()
        throws Exception {
        ITmfTimestamp timestamp = new TmfTimestamp();

        CtfLocation result = new CtfLocation(timestamp);

        // add additional test code here
        assertNotNull(result);
        assertEquals(new Long(0L), result.getLocation());
    }

    /**
     * Run the CtfLocation clone() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testClone_1()
        throws Exception {
        CtfLocation fixture = new CtfLocation(new Long(1L));
        fixture.setLocation(new Long(1L));

        CtfLocation result = fixture.clone();

        // add additional test code here
        assertNotNull(result);
        assertEquals(new Long(1L), result.getLocation());
    }

    /**
     * Run the Long getLocation() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetLocation_1()
        throws Exception {
        CtfLocation fixture = new CtfLocation(new Long(1L));
        fixture.setLocation(new Long(1L));

        Long result = fixture.getLocation();

        // add additional test code here
        assertNotNull(result);
        assertEquals("1", result.toString());
        assertEquals((byte) 1, result.byteValue());
        assertEquals((short) 1, result.shortValue());
        assertEquals(1, result.intValue());
        assertEquals(1L, result.longValue());
        assertEquals(1.0f, result.floatValue(), 1.0f);
        assertEquals(1.0, result.doubleValue(), 1.0);
    }

    /**
     * Run the void setLocation(Long) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testSetLocation_1()
        throws Exception {
        CtfLocation fixture = new CtfLocation(new Long(1L));
        fixture.setLocation(new Long(1L));
        Long location = new Long(1L);

        fixture.setLocation(location);

        // add additional test code here
    }

    /**
     * Perform pre-test initialization.
     *
     * @throws Exception
     *         if the initialization fails for some reason
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Before
    public void setUp()
        throws Exception {
        // add additional set up code here
    }

    /**
     * Perform post-test clean-up.
     *
     * @throws Exception
     *         if the clean-up fails for some reason
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @After
    public void tearDown()
        throws Exception {
        // Add additional tear down code here
    }

    /**
     * Launch the test.
     *
     * @param args the command line arguments
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CtfLocationTest.class);
    }
}