package org.eclipse.linuxtools.lttng.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;
import org.junit.Test;

/*
 Functions tested here :
    public LttngEventFormat() 
    public String[] getLabels(LttngEvent thisEvent) 
    public LttngEventField[] parse(LttngEvent thisEvent) 
    public LttngEventField[] parse(HashMap<String, Object> parsedEvents) 
    public LttngEventField[] parse(String uselessContent) 

 */

public class LttngEventFormatTest {
	private final static boolean skipIndexing=true;
	private final static boolean waitForCompletion=true;
    private final static String tracepath1="traceset/trace_617984ev_withlost";
    
    private final static long   timestampAfterMetadata 		 = 952090116049L;
    
    private final static String secondEventFirstField 	     = "loglevel";
    private final static String secondEventSecondField 	     = "string";
    //private final static String secondEventThirdField 	     = "ip";
    
    private final static String secondEventThirdFieldParsedValue  = "ip:0xc04f402c";
    
    
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
    
    
	private LttngEventFormat prepareToTest() {
		LttngEventFormat tmpEventFormat = null;

		// This trace should be valid
		try {
			LTTngTrace tmpStream = initializeEventStream();
			tmpEventFormat = (LttngEventFormat)tmpStream.parseEvent( new TmfTraceContext(null, null, 0) ).getContent().getFormat();
		} 
		catch (Exception e) {
			fail("ERROR : Failed to get format!");
		}

		return tmpEventFormat;
	}

	@Test
	public void testConstructors() {
		LttngEventFormat testFormat = null;
		@SuppressWarnings("unused")
		LttngEventFormat testFormat2 = null;
        
		
	    // Default construction with good argument
        try {
        	testFormat = new LttngEventFormat(new String[1]);
        }
        catch( Exception e) { 
        	fail("Construction failed!");
        }
        
        // Copy constructor
        try {
            testFormat = new LttngEventFormat(new String[1]);
            testFormat2 = new LttngEventFormat(testFormat);
        }
        catch( Exception e) { 
            fail("Copy construction failed!");
        }
        
	}
	
	
    @Test
	public void testGetter() {
    	LttngEventFormat testFormat = null;
    	LTTngTrace tmpStream = null;
    	LttngEvent tmpEvent = null;
    	TmfTraceContext tmpContext = new TmfTraceContext(null, null, 0);
    	
    	//*** Position ourself to the second event to have something interesting to test with ***
    	
    	tmpStream = initializeEventStream();
    	// Skip first events and seek to events past metadata
    	tmpContext= tmpStream.seekLocation(new LttngTimestamp(timestampAfterMetadata) );
    	// Skip first one
    	tmpEvent = (LttngEvent)tmpStream.parseEvent(tmpContext);
    	// Second event should have more fields
    	tmpEvent = (LttngEvent)tmpStream.parseEvent(tmpContext);
    	// Get a real format from the event
    	testFormat = (LttngEventFormat)tmpEvent.getContent().getFormat();
    	
    	// Test getLabels()
    	assertEquals("Label not as expected!",secondEventFirstField,testFormat.getLabels()[0].toString());
    	assertEquals("Label not as expected!",secondEventSecondField,testFormat.getLabels()[1].toString());
    	
    	
    	// Test different parse()
    	// parse(event)
    	assertNotSame("parse() returned null!",null, testFormat.parse(tmpEvent));
    	assertEquals("Parsed field not as expected!",secondEventThirdFieldParsedValue,testFormat.parse(tmpEvent)[2].toString());
    	
    	// parse(hashmap)
    	assertNotSame("parse() returned null!",null, testFormat.parse(tmpEvent.convertEventTmfToJni().parseAllFields()));
    	assertEquals("Parsed field not as expected!",secondEventThirdFieldParsedValue,testFormat.parse(tmpEvent.convertEventTmfToJni().parseAllFields())[2].toString());
    	
    	
    	// parse(string)
    	System.out.println(tmpEvent.getContent().getContent().toString());
    	assertNotSame("parse() returned null!",null, testFormat.parse(tmpEvent.getContent().getContent().toString()));
    	assertEquals("Parsed field not as expected!",secondEventThirdFieldParsedValue,testFormat.parse(tmpEvent.getContent().getContent())[2].toString());
    }
    
    @Test
	public void testToString() {
    	LttngEventFormat tmpFormat = prepareToTest();
    	
		// Just make sure toString() does not return null or the java reference
		assertNotSame("toString returned null",null, tmpFormat.toString() );
		assertNotSame("toString is not overridded!", tmpFormat.getClass().getName() + '@' + Integer.toHexString(tmpFormat.hashCode()), tmpFormat.toString() );
    }
	
}
