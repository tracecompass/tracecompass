package org.eclipse.linuxtools.lttng.event;

import junit.framework.TestCase;

import org.eclipse.linuxtools.lttng.trace.LTTngTextTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;

/*
 Functions tested here :
	public LttngEventContent(LttngEventFormat thisFormat)
    public LttngEventContent(LttngEventFormat thisFormat, String thisParsedContent, LttngEventField[] thisFields)
    public LttngEventContent(LttngEventContent oldContent)
    public TmfEventField[] getFields()
    public LttngEventField getField(int id)
    public TmfEventField[] getFields(LttngEvent thisEvent)
    public LttngEventField getField(int id, LttngEvent thisEvent)
    public String toString()
 */

public class LttngEventContentTest extends TestCase {
    private final static String tracepath1="traceset/trace-15316events_nolost_newformat.txt";
    private final static boolean skipIndexing=true;
    
    private final static String firstEventContentFirstField 	= "alignment:0";
    private final static String secondEventContentSecondField 	= "string:LTT state dump begin";
    
    private final static long   timestampAfterMetadata 		 = 13589760262237L;
    
    private LTTngTextTrace initializeEventStream() {
        LTTngTextTrace tmpStream = null;
		try {
			tmpStream = new LTTngTextTrace(tracepath1, skipIndexing);
		} 
		catch (Exception e) {
			fail("ERROR : Could not open " + tracepath1 + ". Test failed!" );
		}
		
		return tmpStream;
    }
    
    
	private LttngEventContent prepareToTest() {
		LttngEventContent tmpEventContent = null;

		// This trace should be valid
		try {
		    LTTngTextTrace tmpStream = initializeEventStream();
			tmpEventContent = (LttngEventContent)tmpStream.getNextEvent( new TmfTraceContext(0L, new LttngTimestamp(0L), 0) ).getContent();
		}
		catch (Exception e) {
			fail("ERROR : Failed to get content!");
		}

		return tmpEventContent;
	}

	public void testConstructors() {
		LttngEvent 		  testEvent = null;
		LttngEventContent testContent 	= null;
		LttngEventContent testContent2 	= null;
        LttngEventField[] 	testFields  = new LttngEventField[1];
        testFields[0] = new LttngEventField(testContent2, "test");
        
	    // Default construction with good argument
        try {
        	testContent = new LttngEventContent();
        }
        catch( Exception e) { 
        	fail("Construction with format failed!");
        }
        
        // Construction with good parameters
        try {
        	testContent = new LttngEventContent(testEvent);
        }
        catch( Exception e) { 
        	fail("Construction with format, content and fields failed!");
        }
        
        // Copy constructor with correct parameters
        try {
        	testContent = new LttngEventContent(testEvent);
        	testContent2 = new LttngEventContent(testContent);
        }
        catch( Exception e) { 
        	fail("Copy constructor failed!");
        }
        
	}
	
	
	public void testGetter() {
    	LttngEventContent testContent = null;
    	LTTngTextTrace tmpStream = null;
    	LttngEvent tmpEvent = null;
    	TmfTraceContext tmpContext = null;
    	
    	// Require an event
    	tmpStream = initializeEventStream();
    	tmpContext = new TmfTraceContext(0L, new LttngTimestamp(0L), 0);
    	tmpEvent = (LttngEvent)tmpStream.getNextEvent(tmpContext);
    	
		testContent = prepareToTest();
    	// getFieldS()
    	assertNotSame("getFields() returned null!",null,testContent.getFields() );
    	// getField(int)
    	assertEquals("getField(int) returned unexpected result!",firstEventContentFirstField, testContent.getField(0).toString());
    	
    	
    	
    	//*** To test getFiels with a fields number >0, we need to move to an event that have some more
    	tmpStream = initializeEventStream();
    	tmpContext = new TmfTraceContext(0L, new LttngTimestamp(0L), 0);
    	// Skip first events and seek to event pass metadata
    	tmpContext= tmpStream.seekEvent(new LttngTimestamp(timestampAfterMetadata) );
    	// Skip first one 
    	tmpEvent = (LttngEvent)tmpStream.getNextEvent(tmpContext);
    	
    	// Second event past metadata should have more fields
    	tmpEvent = (LttngEvent)tmpStream.getNextEvent(tmpContext);
    	// Get the content
    	testContent = tmpEvent.getContent();
    	
    	// getFieldS()
    	assertNotSame("getFields() returned null!",null,testContent.getFields() );
    	// getField(int)
    	assertEquals("getField(int) returned unexpected result!",secondEventContentSecondField, testContent.getField(1).toString());
    	
    }
    
	public void testToString() {
    	LttngEventContent tmpContent = prepareToTest();
    	
		// Just make sure toString() does not return null or the java reference
		assertNotSame("toString returned null",null, tmpContent.toString() );
		assertNotSame("toString is not overridded!", tmpContent.getClass().getName() + '@' + Integer.toHexString(tmpContent.hashCode()), tmpContent.toString() );
    }
	
}
