package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp.TimestampType;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CtfTmfTimestampTest</code> contains tests for the class <code>{@link CtfTmfTimestamp}</code>.
 *
 * @generatedBy CodePro at 03/05/12 2:29 PM
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CtfTmfTimestampTest {
    /**
     * Run the CtfTmfTimestamp(long) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testCtfTmfTimestamp()
        throws Exception {
        long timestamp = 1L;

        CtfTmfTimestamp result = new CtfTmfTimestamp(timestamp);
        result.setType(TimestampType.NANOS);

        // add additional test code here
        assertNotNull(result);
        assertEquals("1 ns", result.toString()); //$NON-NLS-1$
        assertEquals(0, result.getPrecision());
        assertEquals(-9, result.getScale());
        assertEquals(1L, result.getValue());
    }


    /**
     * Run the boolean equals(Object) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testEquals_1()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.DAY);
        CtfTmfTimestamp obj = new CtfTmfTimestamp(1L);
        obj.setType(CtfTmfTimestamp.TimestampType.DAY);

        boolean result = fixture.equals(obj);

        // add additional test code here
        assertEquals(true, result);
    }

    /**
     * Run the boolean equals(Object) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testEquals_2()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.DAY);
        Object obj = new Object();

        boolean result = fixture.equals(obj);

        // add additional test code here
        assertEquals(false, result);
    }

    /**
     * Run the boolean equals(Object) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testEquals_3()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.DAY);
        Object obj = new Object();

        boolean result = fixture.equals(obj);

        // add additional test code here
        assertEquals(false, result);
    }

    /**
     * Run the boolean equals(Object) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testEquals_4()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.DAY);
        CtfTmfTimestamp obj = new CtfTmfTimestamp(1L);
        obj.setType(CtfTmfTimestamp.TimestampType.DAY);

        boolean result = fixture.equals(obj);

        // add additional test code here
        assertEquals(true, result);
    }

    /**
     * Run the boolean equals(Object) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testEquals_5()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.DAY);
        CtfTmfTimestamp obj = new CtfTmfTimestamp(1L);
        obj.setType(CtfTmfTimestamp.TimestampType.DAY);

        boolean result = fixture.equals(obj);

        // add additional test code here
        assertEquals(true, result);
    }

    /**
     * Run the ITmfTimestamp getDelta(ITmfTimestamp) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetDelta_1()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.DAY);
        ITmfTimestamp ts = new TmfTimestamp();

        ITmfTimestamp result = fixture.getDelta(ts);

        // add additional test code here
        assertNotNull(result);
        assertEquals(0, result.getPrecision());
        assertEquals(-9, result.getScale());
        assertEquals(1L, result.getValue());
    }

    /**
     * Run the ITmfTimestamp getDelta(ITmfTimestamp) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetDelta_2()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.DAY);
        ITmfTimestamp ts = new TmfTimestamp();

        ITmfTimestamp result = fixture.getDelta(ts);

        // add additional test code here
        assertNotNull(result);
        assertEquals(0, result.getPrecision());
        assertEquals(-9, result.getScale());
        assertEquals(1L, result.getValue());
    }

    /**
     * Run the CtfTmfTimestamp.TimestampType getType() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetType_1()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.DAY);

        CtfTmfTimestamp.TimestampType result = fixture.getType();

        // add additional test code here
        assertNotNull(result);
        assertEquals("DAY", result.name()); //$NON-NLS-1$
        assertEquals("DAY", result.toString()); //$NON-NLS-1$
        assertEquals(1, result.ordinal());
    }

    /**
     * Run the int hashCode() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testHashCode_1()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.DAY);

        int result = fixture.hashCode();

        // add additional test code here
        assertEquals(1012115, result);
    }

    /**
     * Run the int hashCode() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testHashCode_2()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(null);

        int result = fixture.hashCode();

        // add additional test code here
        assertEquals(944663, result);
    }

    /**
     * Run the void setType(TimestampType) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testSetType_1()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.DAY);
        CtfTmfTimestamp.TimestampType value = CtfTmfTimestamp.TimestampType.DAY;

        fixture.setType(value);

        // add additional test code here
    }

    /**
     * Run the String toString() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testToString_1()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.NANOS);

        String result = fixture.toString();

        // add additional test code here
        assertEquals("1 ns", result); //$NON-NLS-1$
    }

    /**
     * Run the String toString() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testToString_2()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.SECONDS);

        String result = fixture.toString();

        // add additional test code here
        assertEquals("1.0E-9 s", result); //$NON-NLS-1$
    }

    /**
     * Run the String toString() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testToString_3()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.DAY);

        String result = fixture.toString();

        // add additional test code here
        assertEquals("19:00:00.000000001", result); //$NON-NLS-1$
    }

    /**
     * Run the String toString() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testToString_4()
        throws Exception {
        CtfTmfTimestamp fixture = new CtfTmfTimestamp(1L);
        fixture.setType(CtfTmfTimestamp.TimestampType.FULL_DATE);

        String result = fixture.toString();

        // add additional test code here
        assertEquals("1969-12-31 19:00:00.000000001", result); //$NON-NLS-1$
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
        new org.junit.runner.JUnitCore().run(CtfTmfTimestampTest.class);
    }
}