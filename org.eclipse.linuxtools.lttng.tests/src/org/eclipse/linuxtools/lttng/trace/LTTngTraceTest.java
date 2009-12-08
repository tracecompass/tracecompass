package org.eclipse.linuxtools.lttng.trace;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;
import junit.framework.TestCase;

/*
 Functions tested here :
	public LttngEventStream(String path) throws Exception 
    public LttngEventStream(String path, boolean waitForCompletion) throws Exception 
    public synchronized TmfEvent parseNextEvent() 
    public TmfTraceContext seekLocation(Object location) 
    public Object getCurrentLocation() 
    public JniTrace getCurrentJniTrace() 
    public LttngEvent getCurrentEvent() 
    public String toString()
    

 */

public class LTTngTraceTest extends TestCase {
	
    private final static String tracepath1="traceset/trace-618339events-1293lost-1cpu";
    private final static String wrongTracePath="/somewhere/that/does/not/exist";
    
    private final static boolean waitForCompletion=true;
    private final static boolean skipIndexing=true;
    
    private final static long   firstEventTimestamp = 952088954601L;
    private final static long   secondEventTimestamp = 952088959952L;
    
    private final static long   contextValueAfterFirstEvent = firstEventTimestamp;
    private final static String firstEventReference = tracepath1 + "/metadata_0";
    private final static long   timestampToSeekTest1 = 953852206193L;
    private final static long   contextValueAfterSeekTest1 = timestampToSeekTest1;
    private final static String seek1EventReference = tracepath1 + "/kernel_0"; 
    private final static long   timestampToSeekLast = 960386638531L;
    private final static long   contextValueAfterSeekLast = timestampToSeekLast;
    private final static String seekLastEventReference = tracepath1 + "/kernel_0"; 
    
	
	private LTTngTrace prepareStreamToTest() {
		LTTngTrace tmpStream = null;

		// This trace should be valid
		try {
			tmpStream = new LTTngTrace(tracepath1, waitForCompletion, skipIndexing);
		} 
		catch (Exception e) {
			System.out.println("ERROR : Could not open " + tracepath1);
		}

		return tmpStream;
	}
	
	public void testTraceConstructors() {
		@SuppressWarnings("unused")
		LTTngTrace testStream1 = null;
        
		// Default constructor
		// Test constructor with argument on a wrong tracepath, skipping indexing
        try {
        		testStream1 = new LTTngTrace(wrongTracePath, waitForCompletion, skipIndexing);
                fail("Construction with wrong tracepath should fail!");
        }
        catch( Exception e) { 
        }
        
        // Test constructor with argument on a correct tracepath, skipping indexing
        try {
        		testStream1 = new LTTngTrace(tracepath1, waitForCompletion, skipIndexing);
        }
        catch( Exception e) {
                fail("Construction with correct tracepath failed!");
        }
		
		
		// Constructor with parameters to wait for completion
		// ** Note : We only test with "wait for completion" true, there is no point testing with false
        // Test constructor with argument on a wrong tracepath, with a certain wait for completion
        // Of course, we turn off indexing skipping.
        try {
        		testStream1 = new LTTngTrace(wrongTracePath, waitForCompletion, false);
                fail("Construction with wrong tracepath should fail!");
        }
        catch( Exception e) { 
        }
        
        // Test constructor with argument on a correct tracepath, with a certain wait for completion
        try {
        		testStream1 = new LTTngTrace(tracepath1, waitForCompletion, false);
        }
        catch( Exception e) {
                fail("Construction with correct tracepath failed!");
        }
	}
	
	public void testParseNextEvent() {
		TmfEvent tmpEvent = null;
		LTTngTrace testStream1 = prepareStreamToTest();
		
		TmfTraceContext tmpContext = new TmfTraceContext(null, null, 0);
		// We should be at the beginning of the trace, so we will just read the first event now
		tmpEvent = testStream1.parseEvent( tmpContext );
		assertNotSame("tmpEvent is null after first parseEvent()",null,tmpEvent );
		assertEquals("tmpEvent has wrong timestamp after first parseEvent()",firstEventTimestamp,(long)tmpEvent.getTimestamp().getValue() );
		
		// Read the next event as well
		tmpEvent = testStream1.parseEvent( tmpContext);
		assertNotSame("tmpEvent is null after second parseEvent()",null,tmpEvent );
		assertEquals("tmpEvent has wrong timestamp after second parseEvent()",secondEventTimestamp,(long)tmpEvent.getTimestamp().getValue() );
	}
	
	public void testSeekLocation() {
		TmfEvent tmpEvent = null;
		TmfTraceContext tmpContext = new TmfTraceContext(null, null, 0);
		LTTngTrace testStream1 = prepareStreamToTest();
		
		// We should be at the beginning of the trace, we will seek at a certain timestamp
		tmpContext = testStream1.seekLocation(new TmfTimestamp(timestampToSeekTest1));
		assertNotSame("tmpContext is null after first seekLocation()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekLocation()",contextValueAfterSeekTest1,(long)((TmfTimestamp)tmpContext.getLocation()).getValue() );
		tmpEvent = testStream1.parseEvent(tmpContext);
		assertNotSame("tmpEvent is null after first seekLocation()",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after first seekLocation()", ((String)tmpEvent.getReference().getReference()).contains(seek1EventReference) );
		
		// Seek to the last timestamp
		tmpContext = testStream1.seekLocation(new TmfTimestamp(timestampToSeekLast));
		assertNotSame("tmpContext is null after seekLocation() to last",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after seekLocation() to last",contextValueAfterSeekLast,(long)((TmfTimestamp)tmpContext.getLocation()).getValue() );
		tmpEvent = testStream1.parseEvent(tmpContext);
		assertNotSame("tmpEvent is null after seekLocation() to last ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekLocation() to last",((String)tmpEvent.getReference().getReference()).contains(seekLastEventReference) );
		
		// Seek to the first timestamp (startTime)
		tmpContext = testStream1.seekLocation(new TmfTimestamp(firstEventTimestamp));
		assertNotSame("tmpContext is null after seekLocation() to start",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after seekLocation() to start",contextValueAfterFirstEvent,(long)((TmfTimestamp)tmpContext.getLocation()).getValue() );
		tmpEvent = testStream1.parseEvent(tmpContext);
		assertNotSame("tmpEvent is null after seekLocation() to start ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekLocation() to start",((String)tmpEvent.getReference().getReference()).contains(firstEventReference) );
	}
	
	
	public void testGetter() {
    	TmfEvent tmpEvent = null;
		LTTngTrace testStream1 = prepareStreamToTest();
		
		// Move to the first event to have something to play with
		tmpEvent = testStream1.parseEvent( new TmfTraceContext(null, null, 0));
		
		// Test trace
		assertTrue("JniTrace is wrong after first event",testStream1.getCurrentJniTrace().getTracepath().contains(tracepath1) );
		
		// Test location
		assertEquals("Location is wrong after first event",contextValueAfterFirstEvent,(long)((TmfTimestamp)testStream1.getCurrentLocation()).getValue() );
		
		// Test current event
		assertNotSame("tmpEvent is null after first event",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after first event",((String)tmpEvent.getReference().getReference()).contains(firstEventReference) );
    }
    
	public void testToString() {
		LTTngTrace testStream1 = prepareStreamToTest();
		
		// Move to the first event to have something to play with
		testStream1.parseEvent( new TmfTraceContext(null, null, 0) );
		
		// Just make sure toString() does not return null or the java reference
		assertNotSame("toString returned null",null, testStream1.toString() );
		assertNotSame("toString is not overridded!", testStream1.getClass().getName() + '@' + Integer.toHexString(testStream1.hashCode()), testStream1.toString() );
    }
	
}
