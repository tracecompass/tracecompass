package org.eclipse.linuxtools.lttng.core.tests.event;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.lttng.core.event.LttngEventContent;
import org.eclipse.linuxtools.lttng.core.event.LttngEventField;
import org.eclipse.linuxtools.lttng.core.event.LttngEventType;
import org.eclipse.linuxtools.lttng.core.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.core.tests.LTTngCoreTestPlugin;
import org.eclipse.linuxtools.lttng.core.trace.LTTngTextTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

/*
 Functions tested here :
 	
	public LttngEventContent()
    public LttngEventContent(LttngEvent thisParent)
    public LttngEventContent(LttngEvent thisParent, HashMap<String, LttngEventField> thisContent)
    public LttngEventContent(LttngEventContent oldContent)
    
    public void emptyContent()
    
    public LttngEventField[] getFields()
    public LttngEventField getField(int position)
    public LttngEventField getField(String name)
    public LttngEvent getEvent()
    public LttngEventType getType()
    public Object[] getContent()
    public HashMap<String, LttngEventField> getRawContent()
    
    public void setType(LttngEventType newType)
    public void setEvent(LttngEvent newParent)
    
    public String toString()
 */

@SuppressWarnings("nls")
public class LttngEventContentTest extends TestCase {
    private final static String tracepath1="traceset/trace-15316events_nolost_newformat.txt";
//    private final static boolean skipIndexing=true;
    
    private final static String firstEventContentFirstField 	= "alignment:0";
    private final static String firstEventContentFirstFieldName = "alignment";
    private final static String firstEventContentType 			= "metadata/0/core_marker_id";
    
    private final static String secondEventContentSecondField 	= "string:LTT state dump begin";
    private final static String secondEventContentSecondFieldName = "string";
    private final static String secondEventContentType 			= "kernel/0/vprintk";
    
    private final static long   timestampAfterMetadata 		 = 13589760262237L;
    
    private static LTTngTextTrace testStream = null;
    
    private LTTngTextTrace initializeEventStream() {
    	if (testStream == null) {
			try {
				URL location = FileLocator.find(LTTngCoreTestPlugin.getPlugin().getBundle(), new Path(tracepath1), null);
				File testfile = new File(FileLocator.toFileURL(location).toURI());
				LTTngTextTrace tmpStream = new LTTngTextTrace(testfile.getName(), testfile.getPath());
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
    
    
	private LttngEventContent prepareToTest() {
		LttngEventContent tmpEventContent = null;

		// This trace should be valid
		try {
			testStream = null;
		    LTTngTextTrace tmpStream = initializeEventStream();
			tmpEventContent = (LttngEventContent)tmpStream.getNextEvent( new TmfContext(new TmfLocation<Long>(0L), 0) ).getContent();
		}
		catch (Exception e) {
			fail("ERROR : Failed to get content!");
		}

		return tmpEventContent;
	}

	public void testConstructors() {
		LttngEvent 		  testEvent = null;
		LttngEventContent testContent 	= null;
        LttngEventField[] testFields  = new LttngEventField[1];
        testFields[0] = new LttngEventField("test");
        
	    // Default construction with good argument
        try {
        	testContent = new LttngEventContent();
        }
        catch( Exception e) { 
        	fail("Construction with format failed!");
        }
        
        // Construction with good parameters (parent event)
        try {
        	testContent = new LttngEventContent(testEvent);
        }
        catch( Exception e) { 
        	fail("Construction with format, content and fields failed!");
        }
        
        // Construction with good parameters (parent event and pre-parsed content)
        try {
        	HashMap<String, LttngEventField> parsedContent = new HashMap<String, LttngEventField>();
        	testContent = new LttngEventContent(testEvent, parsedContent);
        }
        catch( Exception e) { 
        	fail("Construction with format, content and fields failed!");
        }
        
        
        // Copy constructor with correct parameters
        try {
        	testContent = new LttngEventContent(testEvent);
        	new LttngEventContent(testContent);
        }
        catch( Exception e) { 
        	fail("Copy constructor failed!");
        }
        
	}
	
	
	public void testGetter() {
		LttngEventContent testContent = null;
    	LTTngTextTrace tmpStream = null;
    	LttngEvent tmpEvent = null;
    	TmfContext tmpContext = null;
    	
    	// Require an event
    	tmpStream = initializeEventStream();
    	tmpContext = new TmfContext(new TmfLocation<Long>(0L), 0);
    	tmpEvent = (LttngEvent)tmpStream.getNextEvent(tmpContext);
		testContent = prepareToTest();
    	// getFieldS()
    	assertNotSame("getFields() returned null!", null, testContent.getFields() );
    	
    	// *** FIXME ***
    	// Depending from the Java version because of the "hashcode()" on String. 
    	// We can't really test that safetly
    	//
    	// getField(int)
    	//assertEquals("getField(int) returned unexpected result!",firstEventContentFirstField, testContent.getField(0).toString());
    	assertNotSame("getField(int) returned unexpected result!", null, testContent.getField(0).toString());
    	
    	
    	// getField(name)
    	assertEquals("getField(name) returned unexpected result!",firstEventContentFirstField, testContent.getField(firstEventContentFirstFieldName).toString());
    	// getRawContent
    	assertNotSame("getRawContent() returned null!",null, testContent.getMapContent());
    	// Test that get event return the correct event
    	assertTrue("getEvent() returned unexpected result!", tmpEvent.getTimestamp().getValue() == testContent.getEvent().getTimestamp().getValue());
    	// getType()
    	assertEquals("getType() returned unexpected result!",firstEventContentType, testContent.getEvent().getType().toString());
    	
    	//*** To test getFields with a fields number >0, we need to move to an event that have some more
    	tmpStream = initializeEventStream();
    	tmpContext = new TmfContext(new TmfLocation<Long>(0L), 0);
    	// Skip first events and seek to event pass metadata
    	tmpContext= tmpStream.seekEvent(new LttngTimestamp(timestampAfterMetadata) );
    	// Skip first one 
    	tmpEvent = (LttngEvent)tmpStream.getNextEvent(tmpContext);
    	
    	// Second event past metadata should have more fields
    	tmpEvent = (LttngEvent)tmpStream.getNextEvent(tmpContext);
    	// Get the content
    	testContent = tmpEvent.getContent();
    	
    	// Test that get event return the correct event
    	assertTrue("getEvent() returned unexpected result!",tmpEvent.getTimestamp().getValue() == testContent.getEvent().getTimestamp().getValue());
    	// getType()
    	assertEquals("getType() returned unexpected result!",secondEventContentType, testContent.getEvent().getType().toString());
    	
    	
    	// getFieldS()
    	assertNotSame("getFields() returned null!", null, testContent.getFields() );
    	// getField(int)
    	assertEquals("getField(int) returned unexpected result!",secondEventContentSecondField, testContent.getField(1).toString());
    	// getField(name)
    	assertEquals("getField(name) returned unexpected result!",secondEventContentSecondField, testContent.getField(secondEventContentSecondFieldName).toString());
    	// getRawContent
    	assertNotSame("getRawContent() returned null!", null, testContent.getMapContent());
    	
    }
	
	public void testSetter() {
		// Not much to test here, we will just make sure the set does not fail for any reason.
    	// It's pointless to test with a getter...
    	LTTngTextTrace tmpStream = null;
    	LttngEvent tmpEvent = null;
    	TmfContext tmpContext = null;
    	
    	// Require an event
    	tmpStream = initializeEventStream();
    	tmpContext = new TmfContext(new TmfLocation<Long>(0L), 0);
    	tmpEvent = (LttngEvent)tmpStream.getNextEvent(tmpContext);
		
    	LttngEventContent tmpContent = prepareToTest();
    	try {
    		tmpContent.setEvent(tmpEvent);
    	}
    	catch( Exception e) { 
        	fail("setEvent(event) failed!");
        }
    	
    	
    	LttngEventType testType = new LttngEventType();
    	try {
    		tmpContent.getEvent().setType(testType);
    	}
    	catch( Exception e) { 
        	fail("setType(type) failed!");
        }
	}
	
	public void testEmptyContent() {
		LttngEventContent testContent = null;
    	LTTngTextTrace tmpStream = null;
    	LttngEvent tmpEvent = null;
    	TmfContext tmpContext = null;
    	
    	// Require an event
    	tmpStream = initializeEventStream();
    	tmpContext = new TmfContext(new TmfLocation<Long>(0L), 0);
    	tmpEvent = (LttngEvent)tmpStream.getNextEvent(tmpContext);
    	// Get the content
    	testContent = tmpEvent.getContent();
    	// Get all the fields to make sure there is something in the HashMap
    	testContent.getFields();
    	// Just making sure there is something in the HashMap
    	assertNotSame("HashMap is empty but should not!", 0, testContent.getMapContent().size() );
    	
    	// This is the actual test
    	testContent.emptyContent();
    	assertSame("HashMap is not empty but should be!", 0, testContent.getMapContent().size() );
	}
    
	public void testToString() {
    	LttngEventContent tmpContent = prepareToTest();
    	
		// Just make sure toString() does not return null or the java reference
		assertNotSame("toString returned null",null, tmpContent.toString() );
		assertNotSame("toString is not overridded!", tmpContent.getClass().getName() + '@' + Integer.toHexString(tmpContent.hashCode()), tmpContent.toString() );
    }
	
}
