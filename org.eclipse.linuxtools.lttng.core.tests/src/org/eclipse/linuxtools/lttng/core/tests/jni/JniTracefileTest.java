
package org.eclipse.linuxtools.lttng.core.tests.jni;

import junit.framework.TestCase;

import org.eclipse.linuxtools.internal.lttng.jni.common.JniTime;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;

/*
 Functions tested here :
        public JniTracefile(JniTracefile oldTracefile)
        public JniTracefile(long newPtr) throws JniException
        
        public int readNextEvent()
        public int seekToTime(JniTime seekTime)
        
        public Location requestTracefileLocation()
        
        public boolean getIsCpuOnline()
        public String getTracefilePath()
        public String getTracefileName()
        public long getCpuNumber()
        public long getTid()
        public long getPgid()
        public long getCreation()
        public long getTracePtr()
        public long getMarkerDataPtr()
        public int getCFileDescriptor()
        public long getFileSize()
        public long getBlocksNumber()
        public boolean getIsBytesOrderReversed()
        public boolean getIsFloatWordOrdered()
        public long getAlignement()
        public long getBufferHeaderSize()
        public int getBitsOfCurrentTimestampCounter()
        public int getBitsOfEvent()
        public long getCurrentTimestampCounterMask()
        public long getCurrentTimestampCounterMaskNextBit()
        public long getEventsLost()
        public long getSubBufferCorrupt()
        public JniEvent getCurrentEvent()
        public long getBufferPtr()
        public long getBufferSize()
        public HashMap<Integer, JniMarker> getTracefileMarkersMap()
        public JniTrace getParentTrace()
        public long getTracefilePtr()
        
        public String toString()
        public void printTracefileInformation()
*/

@SuppressWarnings("nls")
public class JniTracefileTest extends TestCase {

    private final static boolean printLttDebug = false;

    private final static String tracepath1 = "traceset/trace-15316events_nolost_newformat";
    private final static String tracefileName1 = "kernel0";

    private final static int numberOfMarkersInTracefile = 45;

    private final static long firstEventTimestamp = 13589760262237L;
    private final static long secondEventTimestamp = 13589762149621L;
    private final static long thirdEventTimestamp = 13589762917527L;

    private final static long timestampToSeekTest1 = 13589807108560L;
    private final static long timestampAfterSeekTest1 = 13589807116344L;

    private final static long timestampToSeekLast = 13589906758692L;

    private JniTracefile prepareTracefileToTest() {

        JniTracefile tmpTracefile = null;

        // This trace should be valid
        try {
            tmpTracefile = JniTraceFactory.getJniTrace(tracepath1, null, printLttDebug)
                            .requestTracefileByName(tracefileName1);

        } catch (JniException e) {
        }

        return tmpTracefile;
    }

    public void testTracefileConstructors() {
        JniTrace testTrace = null;

        // This trace should be valid and will be used in test
        try {
            testTrace = JniTraceFactory.getJniTrace(tracepath1, null, printLttDebug);
        } catch (JniException e) {
            fail("Could not get trace file");
        }

        // Test constructor with pointer on a correct pointer
        try {
            testTrace.allocateNewJniTracefile(testTrace.requestEventByName(tracefileName1)
                            .getTracefilePtr(), testTrace);
        } catch (JniException e) {
            fail("Construction with correct pointer failed!");
        }

        /*
        // Test copy constructor
        try {
                testTracefile1 = new JniTracefile( testTrace.requestEventByName(tracefileName1).getTracefilePtr(), testTrace );
                testTracefile2 = new JniTracefile( testTracefile1);
        }
        catch( JniException e) {
                fail("Copy constructor failed!");
        }
        assertSame("JniTracefile name not same after using copy constructor", testTracefile1.getTracefileName() , testTracefile2.getTracefileName());
        */

    }

    public void testGetSet() {

        JniTracefile testTracefile = prepareTracefileToTest();

        // Test that all Get/Set return data
        //boolean getIsCpuOnline will always be sane...
        assertNotSame("getIsCpuOnline() failed", null, testTracefile.getIsCpuOnline());
        assertNotSame("getTracefilePath is empty", "", testTracefile.getTracefilePath());
        assertNotSame("getTracefileName is empty", "", testTracefile.getTracefileName());
        assertNotSame("getCpuNumber is 0", 0, testTracefile.getCpuNumber());
        assertNotSame("getTid is 0", 0, testTracefile.getTid());
        assertNotSame("getPgid is 0", 0, testTracefile.getPgid());
        assertNotSame("getCreation is 0", 0, testTracefile.getCreation());
        assertNotSame("getTracePtr is 0", 0, testTracefile.getTracePtr());
        assertNotSame("getMarkerDataPtr is 0", 0, testTracefile.getMarkerDataPtr());
        assertNotSame("getCFileDescriptor is 0", 0, testTracefile.getCFileDescriptor());
        assertNotSame("getFileSize is 0", 0, testTracefile.getFileSize());
        assertNotSame("getBlocksNumber is 0", 0, testTracefile.getBlocksNumber());
        //boolean getIsBytesOrderReversed will always be sane...
        assertNotSame("getIsBytesOrderReversed() failed", null,
                        testTracefile.getIsBytesOrderReversed());
        //boolean getIsFloatWordOrdered will always be sane...
        assertNotSame("getIsFloatWordOrdered() failed", null, testTracefile.getIsFloatWordOrdered());
        assertNotSame("getAlignement is 0", 0, testTracefile.getAlignement());
        assertNotSame("getBufferHeaderSize is 0", 0, testTracefile.getBufferHeaderSize());
        assertNotSame("getBitsOfCurrentTimestampCounter is 0", 0,
                        testTracefile.getBitsOfCurrentTimestampCounter());
        assertNotSame("getBitsOfEvent is 0", 0, testTracefile.getBitsOfEvent());
        assertNotSame("getCurrentTimestampCounterMask is 0", 0,
                        testTracefile.getCurrentTimestampCounterMask());
        assertNotSame("getCurrentTimestampCounterMaskNextBit is 0", 0,
                        testTracefile.getCurrentTimestampCounterMaskNextBit());
        assertNotSame("getEventsLost is 0", 0, testTracefile.getEventsLost());
        assertNotSame("getSubBufferCorrupt is 0", 0, testTracefile.getSubBufferCorrupt());
        // There should be at least 1 event, so it shouldn't be null
        assertNotNull("getCurrentEvent returned null", testTracefile.getCurrentEvent());

        assertNotSame("getBufferPtr is 0", 0, testTracefile.getBufferPtr());
        assertNotSame("getBufferSize is 0", 0, testTracefile.getBufferSize());

        assertNotSame("getTracefileMarkersMap is null", null,
                        testTracefile.getTracefileMarkersMap());
        // Also check that the map contain a certains number of data
        assertSame("getTracefileMarkersMap returned an unexpected number of markers",
                        numberOfMarkersInTracefile, testTracefile.getTracefileMarkersMap().size());

        assertNotSame("getParentTrace is null", null, testTracefile.getParentTrace());

        assertNotSame("getTracefilePtr is 0", 0, testTracefile.getTracefilePtr());

    }

    public void testPrintAndToString() {

        JniTracefile testTracefile = prepareTracefileToTest();

        // Test printTraceInformation
        try {
            testTracefile.printTracefileInformation();
        } catch (Exception e) {
            fail("printTraceInformation failed!");
        }

        // Test ToString()
        assertNotSame("toString returned empty data", "", testTracefile.toString());

    }

    public void testEventDisplacement() {

        int readValue = -1;
        int seekValue = -1;
        JniTracefile testTracefile = prepareTracefileToTest();

        // Test #1 readNextEvent()
        readValue = testTracefile.readNextEvent();
        assertSame("readNextEvent() returned error (test #1)", 0, readValue);
        assertEquals("readNextEvent() event timestamp is incoherent (test #1)",
                        secondEventTimestamp, testTracefile.getCurrentEvent().getEventTime()
                                        .getTime());

        // Test #2 readNextEvent()
        readValue = testTracefile.readNextEvent();
        assertSame("readNextEvent() returned error (test #1)", 0, readValue);
        assertEquals("readNextEvent() event timestamp is incoherent (test #1)",
                        thirdEventTimestamp, testTracefile.getCurrentEvent().getEventTime()
                                        .getTime());

        // Test  #1 of seekToTime()
        seekValue = testTracefile.seekToTime(new JniTime(timestampToSeekTest1));
        assertSame("seekToTime() returned error (test #1)", 0, seekValue);
        // Read SHOULD NOT be performed after a seek!
        assertEquals("readNextEvent() event timestamp is incoherent (test #1)",
                        timestampToSeekTest1, testTracefile.getCurrentEvent().getEventTime()
                                        .getTime());

        readValue = testTracefile.readNextEvent();
        assertEquals("readNextEvent() event timestamp is incoherent (test #1)",
                        timestampAfterSeekTest1, testTracefile.getCurrentEvent().getEventTime()
                                        .getTime());

        // Test  #2 of seekToTime()
        seekValue = testTracefile.seekToTime(new JniTime(timestampToSeekLast));
        assertSame("seekToTime() returned error (test #2)", 0, seekValue);
        // Read SHOULD NOT be performed after a seek!
        assertEquals("readNextEvent() event timestamp is incoherent (test #2)",
                        timestampToSeekLast, testTracefile.getCurrentEvent().getEventTime()
                                        .getTime());

        // Read AFTER the last event should bring an error
        readValue = testTracefile.readNextEvent();
        assertNotSame("readNextEvent() AFTER last event should return error (test #2)", 0,
                        readValue);

        // Test to see if we can seek back
        seekValue = testTracefile.seekToTime(new JniTime(firstEventTimestamp));
        assertSame("seekToTime() returned error (test seek back)", 0, seekValue);
        // Read SHOULD NOT be performed after a seek!
        assertEquals("readNextEvent() event timestamp is incoherent (test seek back)",
                        firstEventTimestamp, testTracefile.getCurrentEvent().getEventTime()
                                        .getTime());

    }
}
