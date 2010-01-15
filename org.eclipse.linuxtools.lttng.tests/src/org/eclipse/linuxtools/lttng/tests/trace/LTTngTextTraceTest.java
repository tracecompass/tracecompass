package org.eclipse.linuxtools.lttng.tests.trace;

import org.eclipse.linuxtools.lttng.trace.LTTngTextTrace;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;
import junit.framework.TestCase;

/*
 Functions tested here :
	public LTTngTextTrace(String path) throws Exception
	
    public TmfTraceContext seekLocation(Object location)
    public TmfTraceContext seekEvent(TmfTimestamp timestamp)
    public TmfTraceContext seekEvent(long position)
    
    public TmfEvent getNextEvent(TmfTraceContext context)
    
    public Object getCurrentLocation()
    
    public LttngEvent parseEvent(TmfTraceContext context)
 */

public class LTTngTextTraceTest extends TestCase {
	
    private final static String tracepath1="traceset/trace-15316events_nolost_newformat.txt";
    private final static String wrongTracePath="/somewhere/that/does/not/exist";
    
    private final static boolean skipIndexing=true;
    
    private final static long   firstEventTimestamp = 13589759412127L;
    private final static long   secondEventTimestamp = 13589759419902L;
    private final static Long   locationAfterFirstEvent = 245L;
    
    private final static String tracename = "traceset/trace_15316events_nolost_newformat";
    
    private final static long	indexToSeekFirst = 0;
    private final static Long   locationToSeekFirst = 0L;
    private final static long   contextValueAfterFirstEvent = 13589759412127L;
    private final static String firstEventReference = tracename + "/metadata_0";
    
    
    private final static long   timestampToSeekTest1 = 13589826657302L;
    private final static Long	indexToSeekTest1 = 7497L;
    private final static long   locationToSeekTest1 = 1682942;
    private final static long   contextValueAfterSeekTest1 = 13589826657302L;
    private final static String seek1EventReference = tracename + "/vm_state_0"; 
    
    private final static long   timestampToSeekLast = 13589906758691L;
    private final static Long	indexToSeekLast = 15315L;
    private final static long   locationToSeekLast = 3410544;
    private final static long   contextValueAfterSeekLast = 13589906758691L;
    private final static String seekLastEventReference = tracename + "/kernel_0"; 
    
	private LTTngTextTrace prepareStreamToTest() {
		LTTngTextTrace tmpStream = null;

		// This trace should be valid
		try {
			tmpStream = new LTTngTextTrace(tracepath1);
		} 
		catch (Exception e) {
			System.out.println("ERROR : Could not open " + tracepath1);
		}

		return tmpStream;
	}
	
	public void testTraceConstructors() {
		@SuppressWarnings("unused")
		LTTngTextTrace testStream1 = null;
        
		// Default constructor
		// Test constructor with argument on a wrong tracepath, skipping indexing
        try {
        		testStream1 = new LTTngTextTrace(wrongTracePath, skipIndexing);
                fail("Construction with wrong tracepath should fail!");
        }
        catch( Exception e) { 
        }
        
        // Test constructor with argument on a correct tracepath, skipping indexing
        try {
        		testStream1 = new LTTngTextTrace(tracepath1, skipIndexing);
        }
        catch( Exception e) {
                fail("Construction with correct tracepath failed!");
        }
	}
	
	public void testGetNextEvent() {
		TmfEvent tmpEvent = null;
		LTTngTextTrace testStream1 = prepareStreamToTest();
		
		TmfTraceContext tmpContext = new TmfTraceContext(null, null, 0);
		// We should be at the beginning of the trace, so we will just read the first event now
		tmpEvent = testStream1.getNextEvent(tmpContext );
		assertNotSame("tmpEvent is null after first getNextEvent()",null,tmpEvent );
		assertEquals("tmpEvent has wrong timestamp after first getNextEvent()",firstEventTimestamp,(long)tmpEvent.getTimestamp().getValue() );
		
		// Read the next event as well
		tmpEvent = testStream1.getNextEvent( tmpContext);
		assertNotSame("tmpEvent is null after second getNextEvent()",null,tmpEvent );
		assertEquals("tmpEvent has wrong timestamp after second getNextEvent()",secondEventTimestamp,(long)tmpEvent.getTimestamp().getValue() );
	}
	
	public void testParseEvent() {
		TmfEvent tmpEvent = null;
		LTTngTextTrace testStream1 = prepareStreamToTest();
		
		TmfTraceContext tmpContext = new TmfTraceContext(null, null, 0);
		// We should be at the beginning of the trace, so we will just parse the first event now
		tmpEvent = testStream1.parseEvent(tmpContext );
		assertNotSame("tmpEvent is null after first parseEvent()",null,tmpEvent );
		assertEquals("tmpEvent has wrong timestamp after first parseEvent()",firstEventTimestamp,(long)tmpEvent.getTimestamp().getValue() );
		
		// Use parseEvent again. Should be the same event
		tmpEvent = testStream1.parseEvent(tmpContext );
		assertNotSame("tmpEvent is null after first parseEvent()",null,tmpEvent );
		assertEquals("tmpEvent has wrong timestamp after first parseEvent()",firstEventTimestamp,(long)tmpEvent.getTimestamp().getValue() );
	}
	
	public void testSeekEventTimestamp() {
		TmfEvent tmpEvent = null;
		TmfTraceContext tmpContext = new TmfTraceContext(null, null, 0);
		LTTngTextTrace testStream1 = prepareStreamToTest();
		
		// We should be at the beginning of the trace, we will seek at a certain timestamp
		tmpContext = testStream1.seekEvent(new TmfTimestamp(timestampToSeekTest1));
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekEvent()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekEvent()",contextValueAfterSeekTest1,(long)((TmfTimestamp)tmpContext.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after first seekEvent()",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after first seekEvent()", ((String)tmpEvent.getReference().getReference()).contains(seek1EventReference) );
		
		// Seek to the last timestamp
		tmpContext = testStream1.seekEvent(new TmfTimestamp(timestampToSeekLast));
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after seekEvent() to last",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after seekEvent() to last",contextValueAfterSeekLast,(long)((TmfTimestamp)tmpContext.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after seekEvent() to last ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekEvent() to last",((String)tmpEvent.getReference().getReference()).contains(seekLastEventReference) );
		
		// Seek to the first timestamp (startTime)
		tmpContext = testStream1.seekEvent(new TmfTimestamp(firstEventTimestamp));
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpEvent is null after seekEvent() to start ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekEvent() to start",((String)tmpEvent.getReference().getReference()).contains(firstEventReference) );
		assertNotSame("tmpContext is null after seekEvent() to first",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after seekEvent() to first",contextValueAfterFirstEvent,(long)((TmfTimestamp)tmpContext.getTimestamp()).getValue() );
	}
	
	public void testSeekEventIndex() {
		TmfEvent tmpEvent = null;
		TmfTraceContext tmpContext = new TmfTraceContext(null, null, 0);
		LTTngTextTrace testStream1 = prepareStreamToTest();
		
		// We should be at the beginning of the trace, we will seek at a certain timestamp
		tmpContext = testStream1.seekEvent(indexToSeekTest1);
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekEvent()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekEvent()",contextValueAfterSeekTest1,(long)((TmfTimestamp)tmpContext.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after first seekEvent()",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after first seekEvent()", ((String)tmpEvent.getReference().getReference()).contains(seek1EventReference) );
		
		// Seek to the last timestamp
		tmpContext = testStream1.seekEvent(indexToSeekLast);
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekEvent()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekEvent()",contextValueAfterSeekLast,(long)((TmfTimestamp)tmpContext.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after seekEvent() to last ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekEvent() to last",((String)tmpEvent.getReference().getReference()).contains(seekLastEventReference) );
		
		// Seek to the first timestamp (startTime)
		tmpContext = testStream1.seekEvent(indexToSeekFirst);
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekEvent()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekEvent()",contextValueAfterFirstEvent,(long)((TmfTimestamp)tmpContext.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after seekEvent() to start ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekEvent() to start",((String)tmpEvent.getReference().getReference()).contains(firstEventReference) );
	}
	
	public void testSeekLocation() {
		TmfEvent tmpEvent = null;
		TmfTraceContext tmpContext = new TmfTraceContext(null, null, 0);
		LTTngTextTrace testStream1 = prepareStreamToTest();
		
		// We should be at the beginning of the trace, we will seek at a certain timestamp
		tmpContext = testStream1.seekLocation(locationToSeekTest1);
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekLocation()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekLocation()",contextValueAfterSeekTest1,(long)((TmfTimestamp)tmpContext.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after first seekLocation()",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after first seekLocation()", ((String)tmpEvent.getReference().getReference()).contains(seek1EventReference) );
		
		// Seek to the last timestamp
		tmpContext = testStream1.seekLocation(locationToSeekLast);
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekLocation()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekLocation()",contextValueAfterSeekLast,(long)((TmfTimestamp)tmpContext.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after seekLocation() to last ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekLocation() to last",((String)tmpEvent.getReference().getReference()).contains(seekLastEventReference) );
		
		// Seek to the first timestamp (startTime)
		tmpContext = testStream1.seekLocation(locationToSeekFirst);
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekLocation()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekLocation()",contextValueAfterFirstEvent,(long)((TmfTimestamp)tmpContext.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after seekLocation() to start ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekLocation() to start",((String)tmpEvent.getReference().getReference()).contains(firstEventReference) );
	}
	
	public void testGetter() {
    	TmfEvent tmpEvent = null;
    	LTTngTextTrace testStream1 = prepareStreamToTest();
		
		// Move to the first event to have something to play with
		tmpEvent = testStream1.parseEvent( new TmfTraceContext(null, null, 0));
		
		// Test current event
		assertNotSame("tmpEvent is null after first event",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after first event",((String)tmpEvent.getReference().getReference()).contains(firstEventReference) );
		assertNotSame("tmpContext is null after first seekEvent()",null,testStream1.getCurrentLocation() );
		assertEquals("tmpContext has wrong timestamp after first seekEvent()",locationAfterFirstEvent,(Long)testStream1.getCurrentLocation() );
		
    }
    
	public void testToString() {
		LTTngTextTrace testStream1 = prepareStreamToTest();
		
		// Move to the first event to have something to play with
		testStream1.parseEvent( new TmfTraceContext(null, null, 0) );
		
		// Just make sure toString() does not return null or the java reference
		assertNotSame("toString returned null",null, testStream1.toString() );
		assertNotSame("toString is not overridded!", testStream1.getClass().getName() + '@' + Integer.toHexString(testStream1.hashCode()), testStream1.toString() );
    }
	
}
