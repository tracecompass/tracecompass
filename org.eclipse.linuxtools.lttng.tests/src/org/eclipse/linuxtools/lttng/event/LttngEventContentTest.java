package org.eclipse.linuxtools.lttng.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;
import org.junit.Test;

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

public class LttngEventContentTest {
	private final static boolean skipIndexing=true;
	private final static boolean waitForCompletion=true;
    private final static String tracepath1="traceset/trace_617984ev_withlost";
    
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

	@Test
	public void testConstructors() {
		LttngEventContent testContent 	= null;
		@SuppressWarnings("unused")
		LttngEventContent testContent2 	= null;
		LttngEventFormat	testFormat	= new LttngEventFormat(new String[1]); 
        LttngEventField[] 	testFields  = new LttngEventField[1];
        testFields[0] = new LttngEventField("test", "test");
        
	    // Default construction with good argument
        try {
        	testContent = new LttngEventContent(testFormat);
        }
        catch( Exception e) { 
        	fail("Construction with format failed!");
        }
        
        // Construction with good parameters
        try {
        	testContent = new LttngEventContent(testFormat, "test", testFields);
        }
        catch( Exception e) { 
        	fail("Construction with format, content and fields failed!");
        }
        
        // Copy constructor with correct parameters
        try {
        	testContent = new LttngEventContent(testFormat);
        	testContent2 = new LttngEventContent(testContent);
        }
        catch( Exception e) { 
        	fail("Copy constructor failed!");
        }
        
	}
	
	
    @Test
	public void testGetter() {
    	LttngEventContent testContent = null;
    	LTTngTrace tmpStream = null;
    	LttngEvent tmpEvent = null;
    	TmfTraceContext tmpContext = null;
    	
    	//*** Basic (very bad) interface ***
    	testContent =  prepareToTest();
    	// getFieldS()
    	assertNotSame("getFields() returned null!",null,testContent.getFields().toString());
    	// getField(int)
    	assertEquals("getField(int) returned unexpected result!",firstEventContentFirstField, testContent.getField(0).toString());
    	
    	
    	//*** Upgraded (better) interface ***
    	// Require an event
    	tmpStream = initializeEventStream();
    	tmpContext = new TmfTraceContext(null, null, 0);
    	tmpEvent = (LttngEvent)tmpStream.parseEvent(tmpContext);
    	
		testContent = prepareToTest();
    	// getFieldS()
    	assertNotSame("getFields(event) returned null!",null,testContent.getFields(tmpEvent) );
    	// getField(int)
    	assertEquals("getField(int, event) returned unexpected result!",firstEventContentFirstField, testContent.getField(0, tmpEvent).toString());
    	
    	
    	
    	//*** To test getFiels with a fields number >0, we need to move to an event that have some more
    	tmpStream = initializeEventStream();
    	tmpContext = new TmfTraceContext(null, null, 0);
    	// Skip first events and seek to event pass metadata
    	tmpContext= tmpStream.seekLocation(new LttngTimestamp(timestampAfterMetadata) );
    	// Skip first one 
    	tmpEvent = (LttngEvent)tmpStream.parseEvent(tmpContext);
    	// Second event past metadata should have more fields
    	tmpEvent = (LttngEvent)tmpStream.parseEvent(tmpContext);
    	
    	//*** Basic (very bad) interface ***
    	testContent =  (LttngEventContent)tmpEvent.getContent();
    	// getFieldS()
    	assertNotSame("getFields() returned null!",null,testContent.getFields().toString());
    	// getField(int)
    	assertEquals("getField(int) returned unexpected result!",secondEventContentSecondField, testContent.getField(1).toString());
    	
    	
    	//*** Upgraded (better) interface ***
    	// getFieldS()
    	assertNotSame("getFields(event) returned null!",null,testContent.getFields(tmpEvent) );
    	// getField(int)
    	assertEquals("getField(int, event) returned unexpected result!",secondEventContentSecondField, testContent.getField(1, tmpEvent).toString());
    	
    }
    
    @Test
	public void testToString() {
    	LttngEventContent tmpContent = prepareToTest();
    	
		// Just make sure toString() does not return null or the java reference
		assertNotSame("toString returned null",null, tmpContent.toString() );
		assertNotSame("toString is not overridded!", tmpContent.getClass().getName() + '@' + Integer.toHexString(tmpContent.hashCode()), tmpContent.toString() );
    }
	
}
