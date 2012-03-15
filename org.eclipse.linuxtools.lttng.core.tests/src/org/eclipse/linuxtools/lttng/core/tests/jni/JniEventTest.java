
/*
 Functions tested here :
        public JniEvent(JniEvent oldEvent)
        public JniEvent(long newEventPtr, long newTracefilePtr, HashMap<Integer, JniMarker> newMarkersMap, JniTracefile newParentTracefile) throws JafException
        
        public int readNextEvent()
        public int seekToTime(JniTime seekTime)
        public int seekOrFallBack(JniTime seekTime)
        
        public JniMarker requestEventMarker()
        public String requestEventSource()
        public JniTime requestNextEventTime()
        
        public ArrayList<ParsedContent> parse()
        
        public int getEventMarkerId()
        public JniTime getEventTime()
        public long getEventDataSize()
        public HashMap<Integer, JniMarker> getMarkersMap()
        public long getTracefilePtr()
        public long getEventPtr()
        public int getEventState()
        public JniTracefile getParentTracefile()
        
        public String toString()
        public void printEventInformation()
*/


package org.eclipse.linuxtools.lttng.core.tests.jni;


import junit.framework.TestCase;

import org.eclipse.linuxtools.internal.lttng.jni.common.JniTime;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;

@SuppressWarnings("nls")
public class JniEventTest extends TestCase
{
		private final static boolean printLttDebug = false;
	
        private final static String tracepath="traceset/trace-15316events_nolost_newformat";
        private final static String eventName="kernel0";
        
        private final static int    numberOfMarkersInTracefile = 45;
        
        private final static int    numberOfparsedFieldsFirstEvent = 1;
        private final static int    numberOfparsedFieldsSecondEvent = 3;
        
        private final static int    chosenPositionOfFieldsFirstEvent = 1;
        private final static int    chosenPositionOfFieldsSecondEvent = 0;
        private final static int    chosenPositionOfFieldsAfterSeekEvent = 1;
        
        private final static String chosenNameOfFieldsFirstEvent = "string";
        private final static String chosenNameOfFieldsSecondEvent = "ip";
        private final static String chosenNameOfFieldsThirdEvent = "syscall_id";
        
        private final static String contentOfFieldsFirstEvent = "LTT state dump begin";
        private final static String   contentOfFieldsSecondEvent = "0xc142176d";
        private final static long   contentOfFieldsThirdEvent = 3L;
        
        private final static int    numberOfByteInContent = 4;
        
        private final static long   firstEventTimestamp = 13589760262237L;
        private final static long   secondEventTimestamp = 13589762149621L;
        private final static long   thirdEventTimestamp = 13589762917527L;
        
        private final static long   timestampToSeekTest1 = 13589807108560L;
        private final static long   timestampAfterSeekTest1 = 13589807116344L;
        
        private final static long   timestampToSeekLast = 13589906758692L;
        
        private final static long   timestampToSeekTooFarAway = Long.MAX_VALUE;

        private JniEvent prepareEventToTest() {
                
                JniEvent tmpEvent = null;
                
                // This trace should be valid
                try {
                        tmpEvent = JniTraceFactory.getJniTrace(tracepath, null, printLttDebug).requestEventByName(eventName);
                }
                catch( JniException e) { }
                
                return tmpEvent;
        }
        
        
        public void testEventConstructors() {
                JniTracefile testTracefile = null;
                
                // This trace should be valid and will be used in test
                try {
                        testTracefile = JniTraceFactory.getJniTrace(tracepath, null, printLttDebug).requestTracefileByName(eventName);
                }
                catch( JniException e) {
                    fail("Could not get trace file");
                }
                
                
                // Test constructor on a wrong marker HashMap
                try {
                        testTracefile.allocateNewJniEvent( testTracefile.getCurrentEvent().getEventPtr(), null, testTracefile );
                        fail("Construction with wrong marker hashmap should fail!");
                }
                catch( JniException e) { 
                }
                
                // Test constructor on a wrong tracefile reference
                try {
                        testTracefile.allocateNewJniEvent( testTracefile.getCurrentEvent().getEventPtr(), testTracefile.getTracefileMarkersMap(), null );
                        fail("Construction with wrong tracefile reference should fail!");
                }
                catch( JniException e) { 
                }
                
                // Finally, test constructor with correct information
                try {
                        testTracefile.allocateNewJniEvent( testTracefile.getCurrentEvent().getEventPtr(), testTracefile.getTracefileMarkersMap(), testTracefile );
                }
                catch( JniException e) { 
                        fail("Construction with correct information failed!");
                }
                
                /*
                // Test copy constructor
                try {
                        testEvent1 = new JniEvent( testTracefile.getCurrentEvent() );
                        testEvent2 = new JniEvent( testEvent1);
                }
                catch( Exception e) {
                        fail("Copy constructor failed!");
                }
                assertEquals("JniEvent timestamp not same after using copy constructor", testEvent1.getEventTime().getTime() , testEvent2.getEventTime().getTime() );
                */
                
        }
        
        public void testPrintAndToString() {
                
                JniEvent testEvent = prepareEventToTest();
                
                // Test printEventInformation
                try {
                        testEvent.printEventInformation();
                }
                catch( Exception e) { 
                        fail("printEventInformation failed!");
                }
                
                // Test ToString()
                assertNotSame("toString returned empty data","",testEvent.toString() );
                
        }
        
        public void testEventDisplacement() {
                
                int readValue = -1;
                int seekValue = -1; 
                JniEvent testEvent = prepareEventToTest();
                
                // Test #1 readNextEvent()
                readValue = testEvent.readNextEvent();
                assertSame("readNextEvent() returned error (test #1)",0,readValue);
                assertEquals("readNextEvent() event timestamp is incoherent (test #1)",secondEventTimestamp,testEvent.getEventTime().getTime() );
                
                // Test #2 readNextEvent()
                readValue = testEvent.readNextEvent();
                assertSame("readNextEvent() returned error (test #1)",0,readValue);
                assertEquals("readNextEvent() event timestamp is incoherent (test #1)",thirdEventTimestamp,testEvent.getEventTime().getTime() );
                
                
                // Test  #1 of seekToTime()
                seekValue = testEvent.seekToTime(new JniTime(timestampToSeekTest1) );
                assertSame("seekToTime() returned error (test #1)",0,seekValue);
                // Read SHOULD NOT be performed after a seek!
                assertEquals("readNextEvent() event timestamp is incoherent (test #1)",timestampToSeekTest1,testEvent.getEventTime().getTime() );
                
                readValue = testEvent.readNextEvent();
                assertEquals("readNextEvent() event timestamp is incoherent (test #1)",timestampAfterSeekTest1,testEvent.getEventTime().getTime() );
                
                
                // Test  #2 of seekToTime()
                seekValue = testEvent.seekToTime(new JniTime(timestampToSeekLast) );
                assertSame("seekToTime() returned error (test #2)",0,seekValue);
                // Read SHOULD NOT be performed after a seek!
                assertEquals("readNextEvent() event timestamp is incoherent (test #2)",timestampToSeekLast,testEvent.getEventTime().getTime() );
                
                // Read AFTER the last event should bring an error
                readValue = testEvent.readNextEvent();
                assertNotSame("readNextEvent() AFTER last event should return error (test #2)",0,readValue);
                
                
                // Test to see if we can seek back
                seekValue = testEvent.seekToTime(new JniTime(firstEventTimestamp) );
                assertSame("seekToTime() returned error (test seek back)",0,seekValue);
                // Read SHOULD NOT be performed after a seek!
                assertEquals("readNextEvent() event timestamp is incoherent (test seek back)",firstEventTimestamp,testEvent.getEventTime().getTime() );
                
                
                // Test  #1 of seekOrFallBack() (seek within range)
                seekValue = testEvent.seekOrFallBack(new JniTime(timestampToSeekTest1) );
                assertSame("seekToTime() returned error (test #1)",0,seekValue);
                // Read SHOULD NOT be performed after a seek!
                assertEquals("readNextEvent() event timestamp is incoherent (test #1)",timestampToSeekTest1,testEvent.getEventTime().getTime() );
                
                // Test  #2 of seekOrFallBack() (seek out of range, should fall back)
                seekValue = testEvent.seekOrFallBack(new JniTime(timestampToSeekTooFarAway) );
                assertNotSame("seekOrFallBack() should return an error (test #2)",0,seekValue);
                // The read should return the "last" value as we seek back
                assertEquals("readNextEvent() event timestamp is incoherent (test #2)",timestampToSeekTest1,testEvent.getEventTime().getTime() );
        }
        
        public void testGetSet() {
                
                JniEvent testEvent = prepareEventToTest();
                
                // Test that all Get/Set return data
                assertNotSame("getEventMarkerId is 0",0,testEvent.getEventMarkerId() );
                
                // JniTime should never be null
                assertNotNull("getEventTime returned null", testEvent.getEventTime() );
                
                assertNotSame("getEventDataSize is 0",0,testEvent.getEventDataSize() );
                
                // Test that the marker map is not null
                assertNotSame("getMarkersMap is null",null,testEvent.getMarkersMap() );
                // Also check that the map contain some data
                assertSame("getMarkersMap returned an unexpected number of markers",numberOfMarkersInTracefile,testEvent.getMarkersMap().size() );
                
                assertNotSame("getTracefilePtr is 0",0,testEvent.getTracefilePtr() );
                assertNotSame("getEventPtr is 0",0,testEvent.getEventPtr() );
                // State 0 (EOK) means the event is in a sane state
                assertSame("getEventState is not EOK",0,testEvent.getEventState() );
                
                // ParentTracefile should never be null
                assertNotNull("getParentTracefile returned null", testEvent.getParentTracefile() );
        }
        
        public void testRequestFunctions() {
                
                JniEvent testEvent = prepareEventToTest();
                
                // Test requestEventMarker(). Should return an unique marker
                assertNotNull("requestEventMarker returned null",testEvent.requestEventMarker() );
                
                // Test requestEventSource()
                assertNotSame("requestEventSource is empty","",testEvent.requestEventSource() );
                
                // Test requestEventContent()
                assertNotNull("requestEventContent returned null",testEvent.requestEventContent() );
                
                // Also check that the byte array contain some data
                assertSame("requestEventContent returned an unexpected number of markers",numberOfByteInContent,testEvent.requestEventContent().length );
                
        }
        
        public void testParseAllFieldsFunctions() {
                
                JniEvent testEvent = prepareEventToTest();
                
                // Test parse()
                assertNotNull("parseAllFields returned null",testEvent.parseAllFields() );
                // Parse shouldn't be empty
                assertSame("parseAllFields returned an unexpected number of parsed fields",numberOfparsedFieldsFirstEvent,testEvent.parseAllFields().size() );
                
                // MORE PARSING TESTS 
                // We will perform several more unit tests about parsing as it is very important
                // All those below use the same call as in the displacement test
                // Test #1 readNextEvent()
                testEvent.readNextEvent();
                assertNotNull("parseAllFields returned null",testEvent.parseAllFields() );
                assertSame("parseAllFields returned an unexpected number of parsed fields",numberOfparsedFieldsSecondEvent,testEvent.parseAllFields().size() );
                // Test #2 readNextEvent()
                testEvent.readNextEvent();
                assertNotNull("parseAllFields returned null",testEvent.parseAllFields() );
                
                // Test  #1 of seekToTime()
                testEvent.seekToTime(new JniTime(timestampToSeekTest1) );
                // Read need to be perform after a seek!
                testEvent.readNextEvent();
                assertNotNull("parseAllFields returned null",testEvent.parseAllFields() );
                testEvent.readNextEvent();
                assertNotNull("parseAllFields returned null",testEvent.parseAllFields() );
                
                // Test  #2 of seekToTime()
                testEvent.seekToTime(new JniTime(timestampToSeekLast) );
                // Read need to be perform after a seek!
                testEvent.readNextEvent();
                assertNotNull("parseAllFields returned null",testEvent.parseAllFields() );
                
                // Test to see if we can seek back
                testEvent.seekToTime(new JniTime(firstEventTimestamp) );
                // Read need to be perform after a seek!
                testEvent.readNextEvent();
                assertNotNull("parseAllFields returned null",testEvent.parseAllFields() );
        }
        
        public void testParseFieldByIdFunctions() {
                JniEvent testEvent = prepareEventToTest();
                
                // Test parse()
                assertNotNull("parseFieldById returned null",testEvent.parseFieldById(0) );
                
                testEvent.readNextEvent();
                assertNotNull("parseFieldById returned null",testEvent.parseFieldById(chosenPositionOfFieldsFirstEvent) );
                assertEquals("Content return by parseFieldById is invalid",contentOfFieldsFirstEvent, testEvent.parseFieldById(chosenPositionOfFieldsFirstEvent) );
                assertEquals("Content return by parseFieldByName is invalid",contentOfFieldsFirstEvent, testEvent.parseFieldByName(chosenNameOfFieldsFirstEvent) );
                
                // MORE PARSING TESTS 
                // We will perform several more unit tests about parsing as it is very important
                // All those below use the same call as in the displacement test
                // Test #1 readNextEvent()
                testEvent.readNextEvent();
                assertNotNull("parseFieldById returned null",testEvent.parseFieldById(chosenPositionOfFieldsSecondEvent) );
                assertEquals("Content return by parseFieldById is invalid", contentOfFieldsSecondEvent, testEvent.parseFieldById(chosenPositionOfFieldsSecondEvent).toString() );
                assertEquals("Content return by parseFieldByName is invalid",contentOfFieldsSecondEvent, testEvent.parseFieldByName(chosenNameOfFieldsSecondEvent).toString() );
                
                // Test  #1 of seekToTime()
                testEvent.seekToTime(new JniTime(timestampToSeekTest1) );
                // Read need to be perform after a seek!
                testEvent.readNextEvent();
                assertNotNull("parseFieldById returned null",testEvent.parseFieldById(chosenPositionOfFieldsAfterSeekEvent) );
                assertEquals("Content return by parseFieldById is invalid",contentOfFieldsThirdEvent, testEvent.parseFieldById(chosenPositionOfFieldsAfterSeekEvent) );
                assertEquals("Content return by parseFieldByName is invalid",contentOfFieldsThirdEvent, testEvent.parseFieldByName(chosenNameOfFieldsThirdEvent) );
                
                // Test to see if we can seek back
                testEvent.seekToTime(new JniTime(firstEventTimestamp) );
                // Read need to be perform after a seek!
                testEvent.readNextEvent();
                assertNotNull("parseFieldById returned null",testEvent.parseFieldById(chosenPositionOfFieldsFirstEvent) );
                assertEquals("Content return by parseFieldById is invalid",contentOfFieldsFirstEvent, testEvent.parseFieldById(chosenPositionOfFieldsFirstEvent) );
                assertEquals("Content return by parseFieldByName is invalid",contentOfFieldsFirstEvent, testEvent.parseFieldByName(chosenNameOfFieldsFirstEvent) );
        }
}
