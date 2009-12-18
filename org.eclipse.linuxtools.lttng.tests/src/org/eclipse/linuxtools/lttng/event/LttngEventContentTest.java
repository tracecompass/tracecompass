package org.eclipse.linuxtools.lttng.event;

import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;
import junit.framework.TestCase;

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
	private final static boolean skipIndexing=true;
	private final static boolean waitForCompletion=true;
    private final static String tracepath1="traceset/trace-618339events-1293lost-1cpu";
    
    private final static String firstEventContentFirstField 	= "alignment:0";
    private final static String secondEventContentSecondField 	= "string:LTT state dump begin";
    
    private final static long   timestampAfterMetadata 		 = 952090116049L;
    
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
    
    
	private LttngEventContent prepareToTest() {
		LttngEventContent tmpEventContent = null;

		// This trace should be valid
		try {
		    LTTngTrace tmpStream = initializeEventStream();
			tmpEventContent = (LttngEventContent)tmpStream.parseEvent( new TmfTraceContext(null, null, 0) ).getContent();
		} 
		catch (Exception e) {
			fail("ERROR : Failed to get content!");
		}

		return tmpEventContent;
	}

	public void testConstructors() {
		LttngEvent 		  testEvent    = null;
		LttngEventContent testContent  = null;
		LttngEventContent testContent2 = null;
        LttngEventField[] testFields   = new LttngEventField[1];
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
    	LTTngTrace tmpStream = null;
    	@SuppressWarnings("unused")
		LttngEvent tmpEvent = null;
    	TmfTraceContext tmpContext = null;
    	
    	// Require an event
    	tmpStream = initializeEventStream();
    	tmpContext = new TmfTraceContext(null, null, 0);
    	tmpEvent = (LttngEvent)tmpStream.parseEvent(tmpContext);
    	
		testContent = prepareToTest();
    	// getFieldS()
    	assertNotSame("getFields() returned null!",null,testContent.getFields() );
    	// getField(int)
    	assertEquals("getField(int) returned unexpected result!",firstEventContentFirstField, testContent.getField(0).toString());
    	
    	
    	
    	//*** To test getFiels with a fields number >0, we need to move to an event that have some more
    	tmpStream = initializeEventStream();
    	tmpContext = new TmfTraceContext(null, null, 0);
    	// Skip first events and seek to event pass metadata
    	tmpContext= tmpStream.seekLocation(new LttngTimestamp(timestampAfterMetadata) );
    	// Skip first one 
    	tmpEvent = (LttngEvent)tmpStream.parseEvent(tmpContext);
    	// Second event past metadata should have more fields
    	tmpEvent = (LttngEvent)tmpStream.parseEvent(tmpContext);
    	
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
