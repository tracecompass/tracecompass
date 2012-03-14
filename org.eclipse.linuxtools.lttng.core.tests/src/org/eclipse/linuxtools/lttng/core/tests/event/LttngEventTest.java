package org.eclipse.linuxtools.lttng.core.tests.event;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEventContent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEventType;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngTimestamp;
import org.eclipse.linuxtools.internal.lttng.core.trace.LTTngTextTrace;
import org.eclipse.linuxtools.internal.lttng.core.trace.LTTngTrace;
import org.eclipse.linuxtools.lttng.core.tests.LTTngCoreTestPlugin;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

/*
 Functions tested here :
    public LttngEvent(LttngTimestamp timestamp, TmfEventSource source, LttngEventType type, LttngEventContent content, LttngEventReference reference, JniEvent lttEvent)
    public LttngEvent(LttngEvent oldEvent)
    
    public String getChannelName()
    public long getCpuId()
    public String getMarkerName()
    public LttngEventType getType()
    public LttngEventContent getContent()
    
    public void updateJniEventReference(JniEvent newJniEventReference)
    public void setContent(LttngEventContent newContent)
    public void setType(LttngEventType newType)
    
    public JniEvent convertEventTmfToJni()
    
	public String toString()
 */

@SuppressWarnings("nls")
public class LttngEventTest extends TestCase {
    private final static String tracepath1="traceset/trace-15316events_nolost_newformat.txt";
    private final static boolean skipIndexing=true;
    
    private final static long   eventTimestamp 	= 13589759412128L;
    private final static String eventSource 	= "Kernel Core";
    private final static String eventType 		= "metadata/0/core_marker_id";
    private final static String eventChannel 	= "metadata";
    private final static long 	eventCpu 		= 0;
    private final static String eventMarker 	= "core_marker_id";
//    private final static String eventContent 	= "alignment:0 size_t:4 int:4 name:vm_map pointer:4 event_id:0 long:4 channel:vm_state ";
    private final static String eventReference 	= eventChannel + "_" + eventCpu;
    
    
    private static LTTngTextTrace testStream = null;
    private LTTngTextTrace initializeEventStream() {
		if (testStream == null) {
			try {
				URL location = FileLocator.find(LTTngCoreTestPlugin.getPlugin().getBundle(), new Path(tracepath1), null);
				File testfile = new File(FileLocator.toFileURL(location).toURI());
				LTTngTextTrace tmpStream = new LTTngTextTrace(testfile.getName(), testfile.getPath(), skipIndexing);
				testStream = tmpStream;
			} 
			catch (Exception e) {
				System.out.println("ERROR : Could not open " + tracepath1);
				testStream = null;
			}
		}
		else {
			testStream.seekEvent(0);
		}
		
		return testStream;
	}

    private LttngEvent prepareToTest() {
		LttngEvent tmpEvent = null;
		
		try {
			LTTngTextTrace tmpStream = initializeEventStream();
			tmpEvent = (LttngEvent)tmpStream.getNextEvent(new TmfContext(new TmfLocation<Long>(0L), 0) );
		}
		catch (Exception e) {
			System.out.println("ERROR : Could not open " + tracepath1);
		}

		return tmpEvent;
	}

	public void testConstructors() {
        LttngEvent 			testEvent 		= null;
        LTTngTrace			testTrace 		= null;
        LttngTimestamp		testTime		= null;
        String      		testSource 		= null;
        LttngEventType   	testType   		= null;
        LttngEventContent	testContent		= null;
        String              testReference 	= null;
        JniEvent			testJniEvent 	= null;
		String[]			testMarkerFields = null;
		
        // This need to work if we want to perform tests
        try {
    			// In order to test LttngEvent, we need all these constructors/functions to work.
            	// Make sure to run their unit tests first!
        		testMarkerFields = new String[] { "test" };
                testEvent 	= null;
                testTime	= new LttngTimestamp(0L);
                testSource 	= "test";
                testType   	= new LttngEventType("test", 0L, "test", 0, testMarkerFields);
                testContent	= new LttngEventContent(testEvent);
                testReference = "test";
        }
        catch( Exception e) {
                fail("Cannot allocate an EventStream, junit failed!");
        }
		
        // Test constructor with correct information
        try {
        		testEvent = new LttngEvent(testTrace, testTime, testSource, testType, testContent, testReference, testJniEvent);
        }
        catch( Exception e) { 
                fail("Construction with correct information failed!");
        }
        
        // Test about copy constructor
        // Passing a null to copy constructor should fail
        try {
        	 new LttngEvent(null);
            fail("Copy constructor with null old event should fail!");
		}
		catch( Exception e) { 
		}
        
		// Copy constructor used properly
        testEvent = prepareToTest();
        try {
        	new LttngEvent(testEvent);
		}
		catch( Exception e) { 
			fail("Correct utilisation of copy constructor failed!");
		}
		
	}
	
	public void testGetter() {
    	LttngEvent testEvent = prepareToTest();
    	
    	// These will test TMF functions but since we are expecting it to work...
    	assertEquals("Timestamp not what expected!",eventTimestamp,testEvent.getTimestamp().getValue());
    	assertEquals("Source not what expected!",eventSource,testEvent.getSource());
    	assertEquals("Reference not what expected!", eventReference, testEvent.getReference());
    	
    	// These should be overridden functions
    	assertEquals("Type not what expected!",eventType,testEvent.getType().getName());
    	assertEquals("Channel not what expected!",eventChannel,testEvent.getChannelName());
    	assertEquals("CpuId not what expected!",eventCpu,testEvent.getCpuId());
    	assertEquals("Marker not what expected!",eventMarker,testEvent.getMarkerName());
    	
    	// All events should have a parent
    	assertNotNull("Trace parent for this event is null!", testEvent.getTrace() );
    	
    	// *** FIXME ***
    	// Depending from the Java version because of the "hashcode()" on String. 
    	// We can't really test that safetly
    	//
    	//assertEquals("Content not what expected!",eventContent,testEvent.getContent().toString());
    	assertNotSame("Content is null!", null,testEvent.getContent());
    }
    
	public void testSetter() {
    	LttngEvent testEvent = prepareToTest();
    	
        LttngEventType   	testType   		= null;
        LttngEventContent	testContent		= null;
        JniEvent			testJniEvent 	= null;
		
        String[] testMarkerFields = new String[] { "test" };
        testType   	= new LttngEventType("test", 0L, "test", 0, testMarkerFields);
        testContent	= new LttngEventContent(testEvent);
        
    	try {
    		// *** FIXME ***
    		// This won't do anything good on a text trace
        	testEvent.updateJniEventReference(testJniEvent);
        	
        	testEvent.setContent(testContent);
        	testEvent.setType(testType);
		}
		catch( Exception e) { 
			fail("Setters raised an exception!");
		}
    	
		assertSame("SetType failed : type not what expected!",testType,testEvent.getType());
    	assertSame("SetContent failed : content not what expected!",testContent,testEvent.getContent());
		
	}
	
	
	public void testConversion() {
    	@SuppressWarnings("unused")
		JniEvent tmpJniEvent = null;
    	LttngEvent testEvent = null;
    	
        testEvent = prepareToTest();
        
        try {
        	tmpJniEvent = testEvent.convertEventTmfToJni();
		}
		catch( Exception e) { 
			fail("Conversion raised an exception!");
		}
		
		// *** FIXME ***
		// This test can't work with a text trace, commented for now 
		//assertNotSame("Conversion returned a null event!",null, tmpJniEvent );
    }
    
	public void testToString() {
    	LttngEvent tmpEvent = prepareToTest();
    	
		// Just make sure toString() does not return null or the java reference
		assertNotSame("toString returned null",null, tmpEvent.toString() );
		assertNotSame("toString is not overridded!", tmpEvent.getClass().getName() + '@' + Integer.toHexString(tmpEvent.hashCode()), tmpEvent.toString() );
    }
	
}
