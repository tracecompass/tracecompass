package org.eclipse.linuxtools.lttng.core.tests.trace;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.lttng.core.event.LttngLocation;
import org.eclipse.linuxtools.lttng.core.tests.LTTngCoreTestPlugin;
import org.eclipse.linuxtools.lttng.core.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;

/*
 Functions tested here :
 	public LTTngTrace(String path) throws Exception
    public LTTngTrace(String path, boolean skipIndexing) throws Exception
    
    public TmfTraceContext seekLocation(Object location) {
    public TmfTraceContext seekEvent(TmfTimestamp timestamp) {
    public TmfTraceContext seekEvent(long position) {
    
    public TmfEvent getNextEvent(TmfTraceContext context) {
    public Object getCurrentLocation() {
    
    public LttngEvent parseEvent(TmfTraceContext context) {
    
    public int getCpuNumber() {
 */

@SuppressWarnings("nls")
public class LTTngTraceTest extends TestCase {
	
    private final static String tracepath1="traceset/trace-15316events_nolost_newformat";
    private final static String wrongTracePath="/somewhere/that/does/not/exist";
    
    private final static int 	traceCpuNumber=1;
    
    private final static boolean skipIndexing=true;
    
    private final static long   firstEventTimestamp = 13589759412128L;
    private final static long   secondEventTimestamp = 13589759419903L;
    private final static Long   locationAfterFirstEvent = 13589759412128L;
    
    private final static String tracename = "traceset/trace-15316events_nolost_newformat";
    
    private final static long	indexToSeekFirst = 0;
    private final static Long   locationToSeekFirst = 13589759412128L;
    private final static long   contextValueAfterFirstEvent = 13589759412128L;
    private final static String firstEventReference = tracename + "/metadata_0";
    
    
    private final static long   timestampToSeekTest1 = 13589826657302L;
    private final static Long	indexToSeekTest1 = 7497L;
    private final static long   locationToSeekTest1 = 13589826657302L;
    private final static long   contextValueAfterSeekTest1 = 13589826657302L;
    private final static String seek1EventReference = tracename + "/vm_state_0"; 
    
    private final static long   timestampToSeekLast = 13589906758692L;
    private final static Long	indexToSeekLast = 15315L;
    private final static long   locationToSeekLast = 13589906758692L;
    private final static long   contextValueAfterSeekLast = 13589906758692L;
    private final static String seekLastEventReference = tracename + "/kernel_0"; 

    private static LTTngTrace testStream = null;
    private LTTngTrace prepareStreamToTest() {
		if (testStream == null) {
			try {
				URL location = FileLocator.find(LTTngCoreTestPlugin.getPlugin().getBundle(), new Path(tracepath1), null);
				File testfile = new File(FileLocator.toFileURL(location).toURI());
				LTTngTrace tmpStream = new LTTngTrace(testfile.getName(), testfile.getPath(), false);
				testStream = tmpStream;
			} 
			catch (Exception e) {
				System.out.println("ERROR : Could not open " + tracepath1);
				testStream = null;
			}
		}
		else {
			testStream.seekEvent(0L);
		}
		
		
		return testStream;
	}

    public void testTraceConstructors() {
		@SuppressWarnings("unused")
		LTTngTrace testStream1 = null;
        
		// Default constructor
		// Test constructor with argument on a wrong tracepath, skipping indexing
        try {
        		testStream1 = new LTTngTrace("", wrongTracePath, skipIndexing);
                fail("Construction with wrong tracepath should fail!");
        }
        catch( Exception e) { 
        }
        
        // Test constructor with argument on a correct tracepath, skipping indexing
        try {
            URL location = FileLocator.find(LTTngCoreTestPlugin.getPlugin().getBundle(), new Path(tracepath1), null);
            File testfile = new File(FileLocator.toFileURL(location).toURI());
            testStream1 = new LTTngTrace(testfile.getName(), testfile.getPath(), skipIndexing);
        }
        catch( Exception e) {
                fail("Construction with correct tracepath failed!");
        }
        System.out.println("Test completed");
	}
	
	public void testGetNextEvent() {
		TmfEvent tmpEvent = null;
		LTTngTrace testStream1 = prepareStreamToTest();
		
		TmfContext tmpContext = new TmfContext(null, 0);
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
		LTTngTrace testStream1 = prepareStreamToTest();
		
		TmfContext tmpContext = new TmfContext(null, 0);
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
		TmfContext tmpContext = new TmfContext(null, 0);
		LTTngTrace testStream1 = prepareStreamToTest();
		
		// We should be at the beginning of the trace, we will seek at a certain timestamp
		tmpContext = testStream1.seekEvent(new TmfTimestamp(timestampToSeekTest1, (byte) -9, 0));
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekEvent()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekEvent()",contextValueAfterSeekTest1,(long)((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after first seekEvent()",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after first seekEvent()", seek1EventReference.contains((String)tmpEvent.getReference()));
		
		// Seek to the last timestamp
		tmpContext = testStream1.seekEvent(new TmfTimestamp(timestampToSeekLast, (byte) -9, 0));
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after seekEvent() to last",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after seekEvent() to last",contextValueAfterSeekLast,(long)((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after seekEvent() to last ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekEvent() to last", seekLastEventReference.contains((String)tmpEvent.getReference()));
		
		// Seek to the first timestamp (startTime)
		tmpContext = testStream1.seekEvent(new TmfTimestamp(firstEventTimestamp, (byte) -9, 0));
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpEvent is null after seekEvent() to start ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekEvent() to start", firstEventReference.contains((String)tmpEvent.getReference()));
		assertNotSame("tmpContext is null after seekEvent() to first",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after seekEvent() to first",contextValueAfterFirstEvent,(long)((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
	}
	
	public void testSeekEventIndex() {
		TmfEvent tmpEvent = null;
		TmfContext tmpContext = new TmfContext(null, 0);
		LTTngTrace testStream1 = prepareStreamToTest();
		
		// We should be at the beginning of the trace, we will seek at a certain timestamp
		tmpContext = testStream1.seekEvent(indexToSeekTest1);
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekEvent()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekEvent()",contextValueAfterSeekTest1,(long)((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after first seekEvent()",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after first seekEvent()", seek1EventReference.contains((String)tmpEvent.getReference()));
		
		// Seek to the last timestamp
		tmpContext = testStream1.seekEvent(indexToSeekLast);
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekEvent()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekEvent()",contextValueAfterSeekLast,(long)((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after seekEvent() to last ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekEvent() to last", seekLastEventReference.contains((String)tmpEvent.getReference()));
		
		// Seek to the first timestamp (startTime)
		tmpContext = testStream1.seekEvent(indexToSeekFirst);
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekEvent()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekEvent()",contextValueAfterFirstEvent,(long)((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after seekEvent() to start ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekEvent() to start", firstEventReference.contains((String)tmpEvent.getReference()));
	}
	
	public void testSeekLocation() {
		TmfEvent tmpEvent = null;
		TmfContext tmpContext = new TmfContext(null, 0);
		LTTngTrace testStream1 = prepareStreamToTest();
		
		// We should be at the beginning of the trace, we will seek at a certain timestamp
		tmpContext = testStream1.seekLocation(new LttngLocation(locationToSeekTest1));
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekLocation()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekLocation()",contextValueAfterSeekTest1,(long)((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after first seekLocation()",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after first seekLocation()", seek1EventReference.contains((String)tmpEvent.getReference()));
		
		// Seek to the last timestamp
		tmpContext = testStream1.seekLocation(new LttngLocation(locationToSeekLast));
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekLocation()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekLocation()",contextValueAfterSeekLast,(long)((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after seekLocation() to last ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekLocation() to last", seekLastEventReference.contains((String)tmpEvent.getReference()));
		
		// Seek to the first timestamp (startTime)
		tmpContext = testStream1.seekLocation(new LttngLocation(locationToSeekFirst));
		tmpEvent = testStream1.getNextEvent(tmpContext);
		assertNotSame("tmpContext is null after first seekLocation()",null,tmpContext );
		assertEquals("tmpContext has wrong timestamp after first seekLocation()",contextValueAfterFirstEvent,(long)((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
		assertNotSame("tmpEvent is null after seekLocation() to start ",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after seekLocation() to start", firstEventReference.contains((String)tmpEvent.getReference()));
	}
	
	public void testGetter() {
    	TmfEvent tmpEvent = null;
    	LTTngTrace testStream1 = prepareStreamToTest();
		
		// Move to the first event to have something to play with
		tmpEvent = testStream1.parseEvent( new TmfContext(null, 0));
		
		// Test current event
		assertNotSame("tmpEvent is null after first event",null,tmpEvent );
		assertTrue("tmpEvent has wrong reference after first event", firstEventReference.contains((String)tmpEvent.getReference()));
		assertNotSame("tmpContext is null after first seekEvent()",null,testStream1.getCurrentLocation() );
		assertTrue("tmpContext has wrong timestamp after first seekEvent()",locationAfterFirstEvent.equals( ((LttngLocation)testStream1.getCurrentLocation()).getOperationTimeValue()) );
		
		// Test CPU number of the trace
		assertSame("getCpuNumber() return wrong number of cpu",traceCpuNumber ,testStream1.getCpuNumber() );
    }
    
	public void testToString() {
		LTTngTrace testStream1 = prepareStreamToTest();
		
		// Move to the first event to have something to play with
		testStream1.parseEvent( new TmfContext(null, 0) );
		
		// Just make sure toString() does not return null or the java reference
		assertNotSame("toString returned null",null, testStream1.toString() );
		assertNotSame("toString is not overridded!", testStream1.getClass().getName() + '@' + Integer.toHexString(testStream1.hashCode()), testStream1.toString() );
    }
	
}
