package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfLocation;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CtfIteratorTest</code> contains tests for the class <code>{@link CtfIterator}</code>.
 *
 * @generatedBy CodePro at 03/05/12 2:29 PM
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("static-method")
public class CtfIteratorTest {
    /**
     * Run the CtfIterator(CtfTmfTrace) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testCtfIterator_1()
        throws Exception {
        CtfTmfTrace trace = createTrace();
        CtfIterator result = new CtfIterator(trace);
        assertNotNull(result);
    }

    /**
     * Run the CtfIterator(CtfTmfTrace) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testCtfIterator_2()
        throws Exception {
        CtfTmfTrace trace = createTrace();
        trace.init("test"); //$NON-NLS-1$

        CtfIterator result = new CtfIterator(trace);

        assertNotNull(result);
    }

    /**
     * Run the CtfIterator(CtfTmfTrace,long,long) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testCtfIterator_3()
        throws Exception {
        CtfTmfTrace trace = createTrace();
        long timestampValue = 1L;
        long rank = 1L;

        CtfIterator result = new CtfIterator(trace, timestampValue, rank);

        assertNotNull(result);
    }


    /**
     * Run the boolean advance() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testAdvance_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();

        boolean result = fixture.advance();
        assertTrue(result);
    }

    /**
     * Run the boolean advance() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testAdvance_2()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();

        boolean result = fixture.advance();

        assertTrue(result);
    }

    /**
     * Run the CtfIterator clone() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testClone_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();

        CtfIterator result = fixture.clone();
        assertNotNull(result);
    }

    /**
     * Run the int compareTo(CtfIterator) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */

    @Test
    public void testCompareTo_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
        CtfIterator o = new CtfIterator(createTrace());

        int result = fixture.compareTo(o);

        assertEquals(1L, result);
    }

    /**
     * @return
     * @throws TmfTraceException
     */
    private CtfTmfTrace createTrace() throws TmfTraceException {
        return TestParams.createTrace();
    }

    /**
     * Run the int compareTo(CtfIterator) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testCompareTo_2()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
        CtfIterator o = new CtfIterator(createTrace());

        int result = fixture.compareTo(o);

        assertEquals(1, result);
    }

    /**
     * Run the int compareTo(CtfIterator) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testCompareTo_3()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
        CtfIterator o = new CtfIterator(createTrace());

        int result = fixture.compareTo(o);
        assertEquals(1, result);
    }

    /**
     * Run the void dispose() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testDispose_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();

        fixture.dispose();
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
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
        CtfIterator obj = new CtfIterator(createTrace());
        CtfLocation ctfLocation1 = new CtfLocation(Long.valueOf(1));
        ctfLocation1.setLocation(Long.valueOf(1));
        obj.setLocation(ctfLocation1);
        obj.increaseRank();

        boolean result = fixture.equals(obj);

        assertTrue(result);
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
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
        Object obj = new Object();

        boolean result = fixture.equals(obj);

        assertFalse(result);
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
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
        Object obj = new Object();

        boolean result = fixture.equals(obj);

        assertFalse(result);
    }

    /**
     * Run the CtfTmfTrace getCtfTmfTrace() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetCtfTmfTrace_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();

        CtfTmfTrace result = fixture.getCtfTmfTrace();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
        assertNotNull(result);
    }

    /**
     * Run the CtfTmfEvent getCurrentEvent() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetCurrentEvent_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();

        CtfTmfEvent result = fixture.getCurrentEvent();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
        assertNotNull(result);
    }

    /**
     * Run the CtfTmfEvent getCurrentEvent() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetCurrentEvent_2()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();

        CtfTmfEvent result = fixture.getCurrentEvent();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
        assertNotNull(result);
    }

    /**
     * Run the CtfLocation getLocation() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetLocation_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();

        CtfLocation result = fixture.getLocation();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
        assertNotNull(result);
    }

    /**
     * Run the long getRank() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testGetRank_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();

        long result = fixture.getRank();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
        assertEquals(1L, result);
    }

    /**
     * Run the boolean hasValidRank() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testHasValidRank_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();

        boolean result = fixture.hasValidRank();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
        assertTrue(result);
    }

    /**
     * Run the boolean hasValidRank() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testHasValidRank_2()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();

        boolean result = fixture.hasValidRank();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
        assertTrue(result);
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
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();

        int result = fixture.hashCode();
        int result2 = fixture.hashCode();
        assertEquals(result, result2);
    }

    /**
     * Run the void increaseRank() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testIncreaseRank_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();

        fixture.increaseRank();

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
    }

    /**
     * Run the boolean seek(long) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testSeek_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
        long timestamp = 1L;

        boolean result = fixture.seek(timestamp);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
        assertTrue(result);
    }

    /**
     * Run the boolean seek(long) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testSeek_2()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
        long timestamp = 1L;

        boolean result = fixture.seek(timestamp);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
        assertTrue(result);
    }

    /**
     * Run the boolean seekRank(long) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testSeekRank_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
        long rank = 1L;

        boolean result = fixture.seekRank(rank);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
        assertTrue(result);
    }

    /**
     * Run the boolean seekRank(long) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testSeekRank_2()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
        long rank = 1L;

        boolean result = fixture.seekRank(rank);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
        assertTrue(result);
    }

    /**
     * Run the void setLocation(ITmfLocation<?>) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testSetLocation_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
        CtfLocation location = new CtfLocation(Long.valueOf(1));
        location.setLocation(Long.valueOf(1));

        fixture.setLocation(location);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
    }

    /**
     * Run the void setRank(long) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testSetRank_1()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
        long rank = 1L;

        fixture.setRank(rank);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
    }

    /**
     * Run the void setRank(long) method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testSetRank_2()
        throws Exception {
        CtfIterator fixture = new CtfIterator(createTrace());
        CtfLocation ctfLocation = new CtfLocation(Long.valueOf(1));
        ctfLocation.setLocation(Long.valueOf(1));
        fixture.setLocation(ctfLocation);
        fixture.increaseRank();
        long rank = 1L;

        fixture.setRank(rank);

        // add additional test code here
        // An unexpected exception was thrown in user code while executing this test:
        //    java.lang.NullPointerException
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.createStreamInputReaders(CTFTraceReader.java:152)
        //       at org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader.<init>(CTFTraceReader.java:92)
        //       at org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator.<init>(CtfIterator.java:40)
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
        new org.junit.runner.JUnitCore().run(CtfIteratorTest.class);
    }
}