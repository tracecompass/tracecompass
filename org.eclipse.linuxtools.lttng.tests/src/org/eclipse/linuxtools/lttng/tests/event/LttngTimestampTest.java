package org.eclipse.linuxtools.lttng.tests.event;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.tests.LTTngCoreTestPlugin;
import org.eclipse.linuxtools.lttng.trace.LTTngTextTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;

/*
 Functions tested here :
    public LttngTimestamp(TmfTimestamp newEventTime) 
    public LttngTimestamp(long newEventTime) 
    public String getSeconds() 
    public String getNanoSeconds() 
    public String toString() 
 */

public class LttngTimestampTest extends TestCase {
	private final static String tracepath1="traceset/trace-15316events_nolost_newformat.txt";
    private final static boolean skipIndexing=true;
    
    private final static String firstEventTimeSecond     = "13589";
    private final static String firstEventTimeNano       = "759412127";
    private final static long   firstEventTimeFull       = 13589759412127L;
    
    private static LTTngTextTrace testStream = null;
    private LTTngTextTrace initializeEventStream() {
		if (testStream == null) {
			try {
				URL location = FileLocator.find(LTTngCoreTestPlugin.getPlugin().getBundle(), new Path(tracepath1), null);
				File testfile = new File(FileLocator.toFileURL(location).toURI());
				LTTngTextTrace tmpStream = new LTTngTextTrace(testfile.getPath(), skipIndexing);
				testStream = tmpStream;
			} 
			catch (Exception e) {
				System.out.println("ERROR : Could not open " + tracepath1);
				testStream = null;
			}
		}
		return testStream;
	}

    private LttngTimestamp prepareToTest() {
        LttngTimestamp tmpTime = null;

        // This trace should be valid
        try {
            LTTngTextTrace tmpStream = initializeEventStream();
            tmpTime = (LttngTimestamp)tmpStream.getNextEvent( new TmfTraceContext(null, null, 0) ).getTimestamp();
        } 
        catch (Exception e) {
            fail("ERROR : Failed to get reference!");
        }

        return tmpTime;
    }
    
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
    
    
    public void testGetter() {
        LttngTimestamp tmpTime = prepareToTest();
        
        assertEquals("Time in second is wrong", firstEventTimeSecond, tmpTime.getSeconds() );
        assertEquals("Time in nano second is wrong", firstEventTimeNano, tmpTime.getNanoSeconds() );
        
        assertEquals("Full time is wrong", firstEventTimeFull, tmpTime.getValue() );
    }
    
    
    public void testToString() {
        LttngTimestamp tmpTime = prepareToTest();
        
        // Just make sure toString() does not return null or the java reference
        assertNotSame("toString returned null",null, tmpTime.toString() );
        assertNotSame("toString is not overridded!", tmpTime.getClass().getName() + '@' + Integer.toHexString(tmpTime.hashCode()), tmpTime.toString() );
    }
    
}
