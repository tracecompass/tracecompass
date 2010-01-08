package org.eclipse.linuxtools.lttng.event;



import junit.framework.TestCase;

import org.eclipse.linuxtools.lttng.trace.LTTngTextTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;

/*
 Functions tested here :
        public LttngEventField(String name, Object newContent) 
        public LttngEventField(LttngEventField oldField) 
        public String getName() 
        public String toString() 

 */

public class LttngEventFieldTest extends TestCase {
    private final static String tracepath1="traceset/trace-15316events_nolost_newformat.txt";
    private final static boolean skipIndexing=true;
    
    private final static String firstEventName 		= "alignment";
    private final static String firstEventValue 	= "0";
    
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
    
    
	private LttngEventField prepareToTest() {
		LttngEventField tmpField = null;

		// This trace should be valid
		try {
			LTTngTextTrace tmpStream = initializeEventStream();
			tmpField = (LttngEventField)tmpStream.getNextEvent( new TmfTraceContext(0, new LttngTimestamp(0L), 0) ).getContent().getField(0);
		} 
		catch (Exception e) {
			fail("ERROR : Failed to get field!");
		}

		return tmpField;
	}

	public void testConstructors() {
		LttngEventContent testContent = null;
		LttngEventField testField 	= null;
		@SuppressWarnings("unused")
		LttngEventField testField2 	= null;
        
	    // Default construction with good argument
        try {
        	testField = new LttngEventField(testContent, "test", "test");
        }
        catch( Exception e) { 
        	fail("Default construction failed!");
        }
        
        // Copy constructor with correct parameters
        try {
        	testField = new LttngEventField(testContent, "test", "test");
        	testField2 = new LttngEventField(testField);
        }
        catch( Exception e) { 
        	fail("Copy constructor failed!");
        }
        
	}
	
	public void testGetter() {
    	
    	// *** To "really" test the field, we will get a real field from LTTngTrace
    	LTTngTextTrace tmpStream = initializeEventStream();
    	
    	LttngEventField testField 	= (LttngEventField)tmpStream.getNextEvent( new TmfTraceContext(0, new LttngTimestamp(0L), 0) ).getContent().getField(0);
    	assertNotSame("getField is null!",null,testField);
    	
    	assertTrue("getName() returned unexpected result!",firstEventName.equals(testField.getId().toString()));
    	assertTrue("getValue() returned unexpected result!",firstEventValue.equals(testField.getValue().toString()));
    	
    	
    }
    
	public void testToString() {
    	LttngEventField tmpField = prepareToTest();
    	
		// Just make sure toString() does not return null or the java reference
		assertNotSame("toString returned null",null, tmpField.toString() );
		assertNotSame("toString is not overridded!", tmpField.getClass().getName() + '@' + Integer.toHexString(tmpField.hashCode()), tmpField.toString() );
    }
	
}
