package org.eclipse.linuxtools.lttng.event;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;
import junit.framework.TestCase;

/*
 Functions tested here :
    public LttngEvent(LttngTimestamp timestamp, LttngEventSource source, LttngEventType type, LttngEventContent content, LttngEventReference reference, JniEvent lttEvent)
    public LttngEvent(LttngEvent oldEvent)
    public String getChannelName()
    public long getCpuId()
    public String getMarkerName()
    public JniEvent convertEventTmfToJni()
    public String toString()

 */

public class LttngEventTest extends TestCase {
	private final static boolean skipIndexing=true;
	private final static boolean waitForCompletion=true;
    private final static String tracepath1="traceset/trace-618339events-1293lost-1cpu";
    
    private final static long   eventTimestamp 	= 952088954601L;
    private final static String eventSource 	= "Kernel Core";
    private final static String eventType 		= "metadata/0/core_marker_id";
    private final static String eventChannel 	= "metadata";
    private final static long 	eventCpu 		= 0 ;
    private final static String eventMarker 	= "core_marker_id";
    private final static String eventContent 	= "alignment:0 int:4 size_t:4 name:vm_map event_id:0 pointer:4 long:4 channel:vm_state ";
    private final static String eventReference 	= tracepath1;
    
	private LttngEvent prepareToTest() {
		LttngEvent tmpEvent = null;

		// This trace should be valid
		try {
			LTTngTrace tmpStream = new LTTngTrace(tracepath1, waitForCompletion, skipIndexing);
			tmpEvent = (LttngEvent)tmpStream.parseEvent(new TmfTraceContext(null, null, 0) );
		} 
		catch (Exception e) {
			System.out.println("ERROR : Could not open " + tracepath1);
		}

		return tmpEvent;
	}

	public void testConstructors() {
		
		LTTngTrace testStream1 = null;
        LttngEvent 			testEvent 		= null;
        @SuppressWarnings("unused")
		LttngEvent 			testAnotherEvent = null;
        LttngTimestamp		testTime		= null;
        TmfEventSource 		testSource 		= null;
        LttngEventType   	testType   		= null;
        LttngEventContent	testContent		= null;
        LttngEventReference testReference 	= null;
        JniEvent			testJniEvent 	= null;
		String[]			testMarkerFields = null;
        
        // This need to work if we want to perform tests
        try {
    			// In order to test LttngEvent, we need all these constructors/functions to work.
            	// Make sure to run their unit tests first!
        		testMarkerFields = new String[1];
        		testStream1 = new LTTngTrace(tracepath1, waitForCompletion, skipIndexing);
                testEvent 	= null;
                testTime	= new LttngTimestamp(0L);
                testSource 	= new TmfEventSource("test");
                testType   	= new LttngEventType("test", 0L, "test", testMarkerFields);
                testContent	= new LttngEventContent(testEvent);
                testReference = new LttngEventReference("test", "test");
                testJniEvent = testStream1.getCurrentJniTrace().findNextEvent();
        }
        catch( Exception e) {
                fail("Cannot allocate an EventStream, junit failed!");
        }
		
        
        
        // Test with null timestamp 
        try {
        		testEvent = new LttngEvent( null, testSource, testType, testContent, testReference, testJniEvent );
                fail("Construction with null timestamp should fail!");
        }
        catch( Exception e) { 
        }
        
        // Test with null source 
        try {
        		testEvent = new LttngEvent( testTime, null, testType, testContent, testReference, testJniEvent );
                fail("Construction with null source should fail!");
        }
        catch( Exception e) { 
        }
        
        // Test with null type 
        try {
        		testEvent = new LttngEvent( testTime, testSource, null, testContent, testReference, testJniEvent );
                fail("Construction with null type should fail!");
        }
        catch( Exception e) { 
        }
        
        // Test with null content 
        try {
        		testEvent = new LttngEvent( testTime, testSource, testType, null, testReference, testJniEvent );
                fail("Construction with null content should fail!");
        }
        catch( Exception e) { 
        }
        
        // Test with null reference 
        try {
        		testEvent = new LttngEvent( testTime, testSource, testType, testContent, null, testJniEvent );
                fail("Construction with null reference should fail!");
        }
        catch( Exception e) { 
        }
        
        // Test with null jni Event 
        try {
        		testEvent = new LttngEvent( testTime, testSource, testType, testContent, testReference, null);
                fail("Construction with null jniEvent should fail!");
        }
        catch( Exception e) { 
        }
        
        // Finally, test constructor with correct information
        try {
        		testEvent = new LttngEvent( testTime, testSource, testType, testContent, testReference, testJniEvent);
        }
        catch( Exception e) { 
                fail("Construction with correct information failed!");
        }
        
        // Test about copy constructor
        
        // Pass a null to copy constructor
        try {
        	testAnotherEvent = new  LttngEvent(null);
            fail("Copy constructor with null old event should fail!");
		}
		catch( Exception e) { 
		}
        
		// Copy constructor used properly
        testEvent = prepareToTest();
        try {
        	testAnotherEvent = new  LttngEvent(testEvent);
		}
		catch( Exception e) { 
			fail("Correct utilisation of copy constructor failed!");
		}
		
	}
	
	public void testGetter() {
    	LttngEvent testEvent = prepareToTest();
    	
    	// Some of these will test TMF functions but since we are expecting it to work...
    	assertEquals("Timestamp not what expected!",eventTimestamp,testEvent.getTimestamp().getValue());
    	assertEquals("Source not what expected!",eventSource,testEvent.getSource().getSourceId());
    	assertEquals("Type not what expected!",eventType,testEvent.getType().getTypeId());
    	assertEquals("Channel not what expected!",eventChannel,testEvent.getChannelName());
    	assertEquals("CpuId not what expected!",eventCpu,testEvent.getCpuId());
    	assertEquals("Marker not what expected!",eventMarker,testEvent.getMarkerName());
    	assertEquals("Content not what expected!",eventContent,testEvent.getContent().getContent());
    	assertTrue("Reference not what expected!",((String)testEvent.getReference().getReference()).contains(eventReference) );
    }
    
	public void testConversion() {
    	JniEvent tmpJniEvent = null;
    	LttngEvent testEvent = null;
    	
    	
    	// Copy constructor used properly
        testEvent = prepareToTest();
        try {
        	tmpJniEvent = testEvent.convertEventTmfToJni();
		}
		catch( Exception e) { 
			fail("Conversion raised an exception!");
		}
		
		assertNotSame("Conversion returned a null event!",null, tmpJniEvent );
    	
    }
    
	public void testToString() {
    	LttngEvent tmpEvent = prepareToTest();
    	
		// Just make sure toString() does not return null or the java reference
		assertNotSame("toString returned null",null, tmpEvent.toString() );
		assertNotSame("toString is not overridded!", tmpEvent.getClass().getName() + '@' + Integer.toHexString(tmpEvent.hashCode()), tmpEvent.toString() );
    }
	
}
