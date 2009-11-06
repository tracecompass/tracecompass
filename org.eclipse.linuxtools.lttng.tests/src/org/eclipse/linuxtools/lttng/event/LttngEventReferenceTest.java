package org.eclipse.linuxtools.lttng.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;
import org.junit.Test;

/*
 Functions tested here :
    public LttngEventReference(String newTracefilePath, String newTracePath) 
    public LttngEventReference(LttngEventReference oldReference) 
    public String getTracepath() 
    public void setTracepath(String tracepath) 
    public String toString() 
 */

public class LttngEventReferenceTest {
	private final static boolean skipIndexing=true;
	private final static boolean waitForCompletion=true;
    private final static String tracepath1="traceset/trace-618339events-1293lost-1cpu";
    
    private final static String firstEventReference        = "trace-618339events-1293lost-1cpu";
    
    
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
    
    
    private LttngEventReference prepareToTest() {
        LttngEventReference tmpEventRef = null;

        // This trace should be valid
        try {
            LTTngTrace tmpStream = initializeEventStream();
            tmpEventRef = (LttngEventReference)tmpStream.parseEvent(new TmfTraceContext(null, null, 0) ).getReference();
        } 
        catch (Exception e) {
            fail("ERROR : Failed to get reference!");
        }

        return tmpEventRef;
    }

    @Test
    public void testConstructors() {
        LttngEventReference testRef = null;
        @SuppressWarnings("unused")
        LttngEventReference testRef2 = null;
        
        // Default construction with good argument
        try {
            testRef = new LttngEventReference("test", "test");
        }
        catch( Exception e) { 
            fail("Construction failed!");
        }
        
        // Copy constructor
        try {
            testRef = new LttngEventReference("test", "test");
            testRef2 = new LttngEventReference(testRef);
        }
        catch( Exception e) { 
            fail("Construction failed!");
        }
    }
    
    
    @Test
    public void testGetter() {
        LttngEventReference tmpRef = prepareToTest();
        
        assertTrue("Tracepath not what was expected!",((String)tmpRef.getValue()).contains(firstEventReference) );
        assertEquals("Content not what expected!",firstEventReference,tmpRef.getTracepath());
    }
    
    @Test
    public void testToString() {
        LttngEventReference tmpRef = prepareToTest();
        
        // Just make sure toString() does not return null or the java reference
        assertNotSame("toString returned null",null, tmpRef.toString() );
        assertNotSame("toString is not overridded!", tmpRef.getClass().getName() + '@' + Integer.toHexString(tmpRef.hashCode()), tmpRef.toString() );
    }
    
}
