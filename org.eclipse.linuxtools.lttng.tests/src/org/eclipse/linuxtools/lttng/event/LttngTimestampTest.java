package org.eclipse.linuxtools.lttng.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;
import org.junit.Test;

/*
 Functions tested here :
    public LttngTimestamp(TmfTimestamp newEventTime) 
    public LttngTimestamp(long newEventTime) 
    public String getSeconds() 
    public String getNanoSeconds() 
    public String toString() 
 */

public class LttngTimestampTest {
	private final static boolean skipIndexing=true;
	private final static boolean waitForCompletion=true;
    private final static String tracepath1="traceset/trace_617984ev_withlost";
    
    private final static String firstEventTimeSecond     = "952";
    private final static String firstEventTimeNano       = "088954601";
    private final static long   firstEventTimeFull       = 952088954601L;
    
    private LTTngTrace initializeEventStream() {
        LTTngTrace tmpStream = null;
        try {
            tmpStream = new LTTngTrace(tracepath1, waitForCompletion, skipIndexing);
        } 
        catch (Exception e) {
            fail("ERROR : Could not open " + tracepath1 + ". Test failed!" );
        }
        
        return tmpStream;
    }
    
    
    private LttngTimestamp prepareToTest() {
        LttngTimestamp tmpTime = null;

        // This trace should be valid
        try {
            LTTngTrace tmpStream = initializeEventStream();
            tmpTime = (LttngTimestamp)tmpStream.parseEvent( new TmfTraceContext(null, null, 0) ).getTimestamp();
        } 
        catch (Exception e) {
            fail("ERROR : Failed to get reference!");
        }

        return tmpTime;
    }

    @Test
    public void testConstructors() {
        LttngTimestamp tmpTime = null;
        @SuppressWarnings("unused")
        LttngTimestamp tmpTime2 = null;
        
        // Default construction with good argument
        try {
            tmpTime = new LttngTimestamp(1);
        }
        catch( Exception e) { 
            fail("Construction failed!");
        }
        
        // Copy constructor
        try {
            tmpTime = new LttngTimestamp(1);
            tmpTime2 = new LttngTimestamp(tmpTime);
        }
        catch( Exception e) { 
            fail("Construction failed!");
        }
    }
    
    
    @Test
    public void testGetter() {
        LttngTimestamp tmpTime = prepareToTest();
        
        assertEquals("Time in second is wrong", firstEventTimeSecond, tmpTime.getSeconds() );
        assertEquals("Time in nano second is wrong", firstEventTimeNano, tmpTime.getNanoSeconds() );
        
        assertEquals("Full time is wrong", firstEventTimeFull, tmpTime.getValue() );
    }
    
    
    @Test
    public void testToString() {
        LttngTimestamp tmpTime = prepareToTest();
        
        // Just make sure toString() does not return null or the java reference
        assertNotSame("toString returned null",null, tmpTime.toString() );
        assertNotSame("toString is not overridded!", tmpTime.getClass().getName() + '@' + Integer.toHexString(tmpTime.hashCode()), tmpTime.toString() );
    }
    
}
