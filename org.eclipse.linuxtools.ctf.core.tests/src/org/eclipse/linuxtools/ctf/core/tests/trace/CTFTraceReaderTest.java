package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.tests.TestParams;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CTFTraceReaderTest</code> contains tests for the class
 * <code>{@link CTFTraceReader}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CTFTraceReaderTest {

    CTFTraceReader fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CTFTraceReaderTest.class);
    }

    /**
     * Perform pre-test initialization.
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        fixture = new CTFTraceReader(TestParams.createTrace());
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the CTFTraceReader(CTFTrace) constructor test. Open a known good
     * trace.
     * @throws CTFReaderException
     */
    @Test
    public void testOpen_existing() throws CTFReaderException {
        CTFTrace trace = TestParams.createTrace();

        CTFTraceReader result = new CTFTraceReader(trace);
        assertNotNull(result);
    }

    /**
     * Run the CTFTraceReader(CTFTrace) constructor test. Open a non-existing
     * trace, expect the exception.
     *
     * @throws CTFReaderException
     */
    @Test(expected = org.eclipse.linuxtools.ctf.core.trace.CTFReaderException.class)
    public void testOpen_nonexisting() throws CTFReaderException {
        CTFTrace trace = new CTFTrace("badfile.bad"); //$NON-NLS-1$

        CTFTraceReader result = new CTFTraceReader(trace);
        assertNotNull(result);
    }

    /**
     * Run the CTFTraceReader(CTFTrace) constructor test. Try to pen an invalid
     * path, expect exception.
     *
     * @throws CTFReaderException
     */
    @Test(expected = org.eclipse.linuxtools.ctf.core.trace.CTFReaderException.class)
    public void testOpen_invalid() throws CTFReaderException {
        CTFTrace trace = new CTFTrace(""); //$NON-NLS-1$

        CTFTraceReader result = new CTFTraceReader(trace);
        assertNotNull(result);
    }

    /**
     * Run the boolean advance() method test. Test advancing normally.
     */
    @Test
    public void testAdvance_normal() {
        boolean result = fixture.advance();
        assertTrue(result);
    }

    /**
     * Run the boolean advance() method test. Test advancing when we're at the
     * end, so we expect that there is no more events.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testAdvance_end() throws CTFReaderException {
        fixture.goToLastEvent();
        while (fixture.hasMoreEvents()) {
            fixture.advance();
        }
        boolean result = fixture.advance();
        assertFalse(result);
    }

    /**
     * Run the CTFTraceReader copy constructor test.
     */
    @Test
    public void testCopyFrom() {
        CTFTraceReader result = fixture.copyFrom();
        assertNotNull(result);
    }

    /**
     * Test the hashCode method.
     */
    @Test
    public void testHash() {
        int result = fixture.hashCode();
        assertTrue(0 != result);
    }

    /**
     * Test the equals method. Uses the class-wide 'fixture' and another
     * method-local 'fixture2', which both point to the same trace.
     *
     * Both trace reader are different objects, so they shouldn't "equals" each
     * other.
     * @throws CTFReaderException
     */
    @Test
    public void testEquals() throws CTFReaderException {
        CTFTraceReader fixture2 = new CTFTraceReader(TestParams.createTrace());
        assertFalse(fixture.equals(fixture2));
    }

    /**
     * Run the getCurrentEventDef() method test. Get the first event's
     * definition.
     */
    @Test
    public void testGetCurrentEventDef_first() {
        EventDefinition result = fixture.getCurrentEventDef();
        assertNotNull(result);
    }

    /**
     * Run the getCurrentEventDef() method test. Get the last event's
     * definition.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testGetCurrentEventDef_last() throws CTFReaderException {
        fixture.goToLastEvent();
        EventDefinition result = fixture.getCurrentEventDef();
        assertNotNull(result);
    }

    /**
     * Run the long getEndTime() method test.
     */
    @Test
    public void testGetEndTime() {
        long result = fixture.getEndTime();
        assertTrue(0L < result);
    }

    /**
     * Run the long getStartTime() method test.
     */
    @Test
    public void testGetStartTime() {
        long result = fixture.getStartTime();
        assertTrue(0L < result);
    }

    /**
     * Run the void goToLastEvent() method test.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testGoToLastEvent() throws CTFReaderException {
        fixture.goToLastEvent();
        long ts1 = getTimestamp();
        long ts2 = fixture.getEndTime();
        assertTrue(ts1 == ts2);
    }

    /**
     * Run the boolean hasMoreEvents() method test.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testHasMoreEvents() {
        boolean result = fixture.hasMoreEvents();
        assertTrue(result);
    }

    /**
     * Run the void printStats() method test with no 'width' parameter.
     */
    @Test
    public void testPrintStats_noparam() {
        fixture.advance();
        fixture.printStats();
    }

    /**
     * Run the void printStats(int) method test with width = 0.
     */
    @Test
    public void testPrintStats_width0() {
        fixture.advance();
        fixture.printStats(0);
    }

    /**
     * Run the void printStats(int) method test with width = 1.
     */
    @Test
    public void testPrintStats_width1() {
        fixture.advance();
        fixture.printStats(1);
    }

    /**
     * Run the void printStats(int) method test with width = 2.
     */
    @Test
    public void testPrintStats_width2() {
        fixture.advance();
        fixture.printStats(2);
    }

    /**
     * Run the void printStats(int) method test with width = 10.
     */
    @Test
    public void testPrintStats_width10() {
        fixture.advance();
        fixture.printStats(10);
    }

    /**
     * Run the void printStats(int) method test with width = 100.
     */
    @Test
    public void testPrintStats_100() {
        for (int i = 0; i < 1000; i++) {
            fixture.advance();
        }
        fixture.printStats(100);
    }

    /**
     * Run the boolean seek(long) method test.
     */
    @Test
    public void testSeek() {
        long timestamp = 1L;
        boolean result = fixture.seek(timestamp);
        assertTrue(result);
    }


    /**
     * Run the boolean seek(long) method test.
     */
    @Test
    public void testSeekIndex() {
        long rank = 30000L;
        long first, second = 0, third , fourth;
        /*
         * we need to read the trace before seeking
         */
        first = getTimestamp();
        for( int i = 0 ; i < 60000; i++ )
        {
            if( i == rank) {
                second = getTimestamp();
            }
            fixture.advance();
        }
        boolean result= fixture.seekIndex(0);
        third = getTimestamp();
        boolean result2 = fixture.seekIndex(rank);
        fourth = getTimestamp();
        assertTrue(result);
        assertTrue(result2);
        assertEquals( first , third);
        assertEquals( second , fourth);
    }

    /**
     * @return
     */
    private long getTimestamp() {
        return fixture.getCurrentEventDef().timestamp;
    }
}
