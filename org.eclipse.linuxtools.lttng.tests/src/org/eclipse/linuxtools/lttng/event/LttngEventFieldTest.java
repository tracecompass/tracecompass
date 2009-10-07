package org.eclipse.linuxtools.lttng.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;
import org.junit.Test;

/*
 Functions tested here :
        public LttngEventField(String name, Object newContent) 
        public LttngEventField(LttngEventField oldField) 
        public String getName() 
        public String toString() 

 */

public class LttngEventFieldTest {
	private final static boolean skipIndexing=true;
	private final static boolean waitForCompletion=true;
    private final static String tracepath1="traceset/trace_617984ev_withlost";
    
    private final static String firstEventName 		= "alignment";
    private final static String firstEventValue 	= "0";
    
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
    
    
	private LttngEventField prepareToTest() {
		LttngEventField tmpField = null;

		// This trace should be valid
		try {
			LTTngTrace tmpStream = initializeEventStream();
			tmpField = (LttngEventField)tmpStream.parseEvent( new TmfTraceContext(null, null, 0) ).getContent().getField(0);
		} 
		catch (Exception e) {
			fail("ERROR : Failed to get field!");
		}

		return tmpField;
	}

	@Test
	public void testConstructors() {
		LttngEventField testField 	= null;
		@SuppressWarnings("unused")
		LttngEventField testField2 	= null;
        
	    // Default construction with good argument
        try {
        	testField = new LttngEventField("test", "test");
        }
        catch( Exception e) { 
        	fail("Default construction failed!");
        }
        
        // Copy constructor with correct parameters
        try {
        	testField = new LttngEventField("test", "test");
        	testField2 = new LttngEventField(testField);
        }
        catch( Exception e) { 
        	fail("Copy constructor failed!");
        }
        
	}
	
	
    @Test
	public void testGetter() {
    	
    	// *** To "really" test the field, we will get a real field from LTTngTrace
    	LTTngTrace tmpStream = initializeEventStream();
    	
    	LttngEventField testField 	= (LttngEventField)tmpStream.parseEvent( new TmfTraceContext(null, null, 0) ).getContent().getField(0);
    	assertNotSame("getField is null!",null,testField);
    	
    	assertEquals("getName() returned unexpected result!",firstEventName, testField.getName().toString());
    	assertEquals("getValue() returned unexpected result!",firstEventValue, testField.getValue().toString());
    	
    	
    }
    
    @Test
	public void testToString() {
    	LttngEventField tmpField = prepareToTest();
    	
		// Just make sure toString() does not return null or the java reference
		assertNotSame("toString returned null",null, tmpField.toString() );
		assertNotSame("toString is not overridded!", tmpField.getClass().getName() + '@' + Integer.toHexString(tmpField.hashCode()), tmpField.toString() );
    }
	
}
