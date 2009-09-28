
package org.eclipse.linuxtools.lttng.jni;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

/*
 Functions tested here :
        public JniTrace()
        public JniTrace(JniTrace oldTrace)
        public JniTrace(String newpath) throws JafException
        public JniTrace(long newPtr) throws JafException
        
        public void openTrace(String newPath) throws JafException
        public void openTrace() throws JafException
        public void closeTrace( ) throws JafException
        
        public JniEvent readNextEvent()
        public JniEvent findNextEvent()
        public JniEvent seekAndRead(JniTime seekTime)
        public void seekToTime(JniTime seekTime)
        
        public JniTracefile requestTracefileByName(String tracefileName)
        public JniEvent requestEventByName(String tracefileName)
        public ArrayList<Location> requestTraceLocation()
        
        public String getTracepath()
        public int getCpuNumber()
        public long getArchType()
        public long getArchVariant()
        public short getArchSize()
        public short getLttMajorVersion()
        public short getLttMinorVersion()
        public short getFlightRecorder()
        public long getFreqScale()
        public long getStartFreq()
        public long getStartTimestampCurrentCounter()
        public long getStartMonotonic()
        public JniTime getStartTime()
        public JniTime getStartTimeFromTimestampCurrentCounter()
        public HashMap<String, JniTracefile> getTracefilesMap()
        public long getTracePtr()
        
        public void printAllTracefilesInformation()
        public void printTraceInformation()
        
        public String toString() 
 */


public class TraceTest
{
        private final static String tracepath1="traceset/trace_617984ev_withlost";
        private final static String tracepath2="traceset/trace_211064ev_nolost";
        private final static String wrongTracePath="/somewhere/that/does/not/exist";
        
        private final static String correctTracefileName="kernel0";
        private final static String wrongTracefileName="somethingThatDoesNotExists";
        
        private final static int   numberOfTracefilesInTrace = 17;
        
        private final static long   firstEventTimestamp = 952090116049L;
        private final static String secondEventName = "kernel";
        
        private final static long   timestampToSeekTest1 = 953852206193L;
        private final static String eventNameAfterSeekTest1 = "kernel";
        private final static String nextEventNameAfterSeekTest1 = "fs";
        
        private final static long   timestampToSeekTest2 = 953852210706L;
        private final static String eventNameAfterSeekTest2 = "fs";
        private final static String nextEventNameAfterSeekTest2 = "kernel";
        
        private final static long   timestampToSeekLast = 960386638531L;
        private final static String eventNameAfterSeekLast = "kernel";
        
        
        private JniTrace prepareTraceToTest() {
                JniTrace tmpTrace = null;
                
                // This trace should be valid
                try {
                        tmpTrace = new JniTrace(tracepath1);
                }
                catch( JniException e) { }
                
                return tmpTrace;
        }
        
        @Test
        public void testTraceConstructors() {
                JniTrace testTrace1 = null;
                @SuppressWarnings("unused")
                JniTrace testTrace2 = null;
                
                // Test constructor with argument on a wrong tracepath
                try {
                		System.out.println("TEST1");
                		
                        testTrace1 = new JniTrace(wrongTracePath);
                        fail("Construction with wrong tracepath should fail!");
                }
                catch( JniException e) { 
                }
                
                // Test constructor with argument on a correct tracepath
                try {
                		System.out.println("TEST2");
                		
                        testTrace1 = new JniTrace(tracepath1);
                }
                catch( JniException e) {
                        fail("Construction with correct tracepath failed!");
                }
                
                // Test copy constructor that take a pointer with a good pointer
                try {
                		System.out.println("TEST3");
                		
                        testTrace1 = new JniTrace( new C_Pointer(0) );
                        fail("Construction with wrong pointer should fail!");
                }
                catch( JniException e) { 
                }
                
                // Test copy constructor that take a pointer with a good pointer
                try {
                		System.out.println("TEST4");
                		
                        testTrace1 = new JniTrace(tracepath1); // This trace should be valid
                        testTrace2 = new JniTrace( testTrace1.getTracePtr() );
                }
                catch( JniException e) { 
                        fail("Construction with correct pointer failed!");
                }
                
        }
        
        @Test
        public void testTraceOpenClose() {
                
                JniTrace testTrace = prepareTraceToTest(); // This trace should be valid
                
                // test the constructor with arguments passing a wrong tracepath
                try {
                        testTrace.openTrace(wrongTracePath);
                        fail("Open with wrong tracepath should fail!");
                }
                catch( JniException e) { }
                
                // Test open with a correct tracepath
                try {
                        testTrace.openTrace(tracepath1);
                        assertNotSame("getTracepath is empty after open","",testTrace.getTracepath() );
                }
                catch( JniException e) { 
                        fail("Open with a correct tracepath failed!");
                }
                
                // Test to open a trace already opened
                try {
                        testTrace.openTrace(tracepath1);
                        testTrace.openTrace(tracepath2);
                        assertNotSame("getTracepath is empty after open","",testTrace.getTracepath() );
                }
                catch( JniException e) { 
                        fail("Reopen of a trace failed!");
                }
                
                
                // Test to open a trace already opened, but with a wrong tracepath
                try {
                        testTrace.openTrace(tracepath1);
                        testTrace.openTrace(wrongTracePath);
                        fail("Reopen with wrong tracepath should fail!");
                }
                catch( JniException e) {
                }
        }
        
        @Test
        public void testGetSet() {
                
                JniTrace testTrace = prepareTraceToTest();
                
                // Test that all Get/Set return data
                assertNotSame("getTracepath is empty","",testTrace.getTracepath() );
                assertNotSame("getCpuNumber is 0",0,testTrace.getCpuNumber() );
                assertNotSame("getArchType is 0",0,testTrace.getArchType() );
                assertNotSame("getArchVariant is 0",0,testTrace.getArchVariant() );
                assertNotSame("getArchSize is 0",0,testTrace.getArchSize() );
                assertNotSame("getLttMajorVersion is 0",0,testTrace.getLttMajorVersion() );
                assertNotSame("getLttMinorVersion is 0",0,testTrace.getLttMinorVersion() );
                assertNotSame("getFlightRecorder is 0",0,testTrace.getFlightRecorder() );
                assertNotSame("getFreqScale is 0",0,testTrace.getFreqScale() );
                assertNotSame("getStartFreq is 0",0,testTrace.getStartFreq() );
                assertNotSame("getStartTimestampCurrentCounter is 0",0,testTrace.getStartTimestampCurrentCounter());
                assertNotSame("getStartMonotonic is 0",0,testTrace.getStartMonotonic() );
                assertNotSame("getStartTime is null",null,testTrace.getStartTime() );
                assertNotSame("getStartTimeFromTimestampCurrentCounter is null",null,testTrace.getStartTimeFromTimestampCurrentCounter() );
                assertNotSame("getTracefilesMap is null",null,testTrace.getTracefilesMap() );
                // Also check that the map contain some tracefiles
                assertSame("getTracefilesMap returned an unexpected number of tracefiles",numberOfTracefilesInTrace,testTrace.getTracefilesMap().size() );
                assertNotSame("getTracePtr is 0",0,testTrace.getTracePtr() );
                
                
        }
        
        @Test
        public void testPrintAndToString() {
                
                JniTrace testTrace = prepareTraceToTest();
                
                // Test printTraceInformation
                try {
                        testTrace.printTraceInformation();
                }
                catch( Exception e) { 
                        fail("printTraceInformation failed!");
                }
                
                // Test ToString()
                assertNotSame("toString returned empty data","",testTrace.toString() );
        }
        
        @Test
        public void testRequestFunctions() {
        
                JniTrace testTrace = prepareTraceToTest();
                
                // Test requestTracefileByName()
                assertNotSame("requestTracefileByName returned null",null,testTrace.requestTracefileByName(correctTracefileName) );
                assertSame("requestTracefileByName returned content on non existent name",null,testTrace.requestTracefileByName(wrongTracefileName) );
                
                // Test requestEventByName()
                assertNotSame("requestEventByName returned null",null,testTrace.requestEventByName(correctTracefileName) );
                assertSame("requestEventByName returned content on non existent name",null,testTrace.requestEventByName(wrongTracefileName) );
        }
        
        @Test
        public void testEventDisplacement() {
        
                JniEvent testEvent = null; 
                JniTrace testTrace = prepareTraceToTest();
                
                // Test readNextEvent()
                testEvent = testTrace.readNextEvent();
                assertNotSame("readNextEvent() returned null",null,testEvent);
                assertEquals("readNextEvent() timestamp is incoherent",firstEventTimestamp,testEvent.getEventTime().getTime() );
                
                // Test findNextEvent()
                testEvent = testTrace.findNextEvent();
                assertNotSame("findNextEvent() returned null",null,testEvent);
                assertEquals("findNextEvent() name is incoherent",secondEventName,testEvent.getParentTracefile().getTracefileName() );
                
                // Test readNextEvent()
                testEvent = testTrace.readNextEvent();
                assertNotSame("readNextEvent() returned null",null,testEvent);
                assertEquals("readNextEvent() timestamp is incoherent",secondEventName,testEvent.getParentTracefile().getTracefileName() );
                
                // Tests below are for seekAndRead()
                // After, we will perform the same operation for seekTime
                //
                // Test  #1 of seekAndRead()
                testEvent = testTrace.seekAndRead(new JniTime(timestampToSeekTest1) );
                assertNotSame("seekAndRead(time) returned null (test #1)",null,testEvent);
                assertEquals("seekAndRead(time) timestamp is incoherent (test #1)",timestampToSeekTest1,testEvent.getEventTime().getTime());
                assertEquals("event name after seekAndRead(time) is incoherent (test #1)",eventNameAfterSeekTest1,testEvent.getParentTracefile().getTracefileName());
                // Test that the next event after seek in the one we expect
                testEvent = testTrace.readNextEvent();
                assertEquals("readNextEvent() name after seekAndRead(time) is incoherent (test #1)",nextEventNameAfterSeekTest1,testEvent.getParentTracefile().getTracefileName());
                
                // Test  #2 of seekAndRead()
                testEvent = testTrace.seekAndRead(new JniTime(timestampToSeekTest2) );
                assertNotSame("seekAndRead(time) returned null (test #2)",null,testEvent);
                assertEquals("seekAndRead(time) timestamp is incoherent (test #2)",timestampToSeekTest2,testEvent.getEventTime().getTime());
                assertEquals("event name after seekAndRead(time) is incoherent (test #2)",eventNameAfterSeekTest2,testEvent.getParentTracefile().getTracefileName());
                // Test that the next event after seek in the one we expect
                testEvent = testTrace.readNextEvent();
                assertEquals("readNextEvent() name after seekAndRead(time) is incoherent (test #2)",nextEventNameAfterSeekTest2,testEvent.getParentTracefile().getTracefileName());
                
                
                // Seek to the LAST event of the trace
                testEvent = testTrace.seekAndRead(new JniTime(timestampToSeekLast) );
                assertNotSame("seekAndRead(time) returned null ",null,testEvent);
                assertEquals("seekAndRead(time) timestamp is incoherent ",timestampToSeekLast,testEvent.getEventTime().getTime());
                assertEquals("event name after seekTime(time) is incoherent ",eventNameAfterSeekLast,testEvent.getParentTracefile().getTracefileName());
                // Test that the next event is NULL (end of the trace)
                testEvent = testTrace.readNextEvent();
                assertSame("seekAndRead(time) returned null ",null,testEvent);
                
                
                // Make sure we can seek back
                testEvent = testTrace.seekAndRead(new JniTime(firstEventTimestamp) );
                assertNotSame("seekAndRead(time) to seek back returned null",null,testEvent);
                assertEquals("seekAndRead(time) timestamp after seek back is incoherent",firstEventTimestamp,testEvent.getEventTime().getTime());
                
                
                
                // Tests below are for seekToTime()
                // These are the same test as seekAndRead() for a readNextEvent() should be performed after seek
                //
                // Test  #1 of seekToTime()
                testTrace.seekToTime(new JniTime(timestampToSeekTest1) );
                testEvent = testTrace.readNextEvent();
                assertNotSame("seekToTime(time) returned null (test #1)",null,testEvent);
                assertEquals("seekToTime(time) timestamp is incoherent (test #1)",timestampToSeekTest1,testEvent.getEventTime().getTime());
                assertEquals("event name after seekTime(time) is incoherent (test #1)",eventNameAfterSeekTest1,testEvent.getParentTracefile().getTracefileName());
                // Test that the next event after seek in the one we expect
                testEvent = testTrace.readNextEvent();
                assertEquals("readNextEvent() name after seekToTime(time) is incoherent (test #1)",nextEventNameAfterSeekTest1,testEvent.getParentTracefile().getTracefileName());
                
                // Test  #2 of seekToTime()
                testTrace.seekToTime(new JniTime(timestampToSeekTest2) );
                testEvent = testTrace.readNextEvent();
                assertNotSame("seekToTime(time) returned null (test #2)",null,testEvent);
                assertEquals("seekToTime(time) timestamp is incoherent (test #2)",timestampToSeekTest2,testEvent.getEventTime().getTime());
                assertEquals("event name after seekTime(time) is incoherent (test #2)",eventNameAfterSeekTest2,testEvent.getParentTracefile().getTracefileName());
                // Test that the next event after seek in the one we expect
                testEvent = testTrace.readNextEvent();
                assertEquals("readNextEvent() name after seekToTime(time) is incoherent (test #2)",nextEventNameAfterSeekTest2,testEvent.getParentTracefile().getTracefileName());
                
                
                // Seek to the LAST event of the trace
                testTrace.seekToTime(new JniTime(timestampToSeekLast) );
                testEvent = testTrace.readNextEvent();
                assertNotSame("seekToTime(time) returned null ",null,testEvent);
                assertEquals("seekToTime(time) timestamp is incoherent ",timestampToSeekLast,testEvent.getEventTime().getTime());
                assertEquals("event name after seekTime(time) is incoherent ",eventNameAfterSeekLast,testEvent.getParentTracefile().getTracefileName());
                // Test that the next event is NULL (end of the trace)
                testEvent = testTrace.readNextEvent();
                assertSame("seekToTime(time) returned null ",null,testEvent);
                
                
                // Make sure we can seek back
                testTrace.seekToTime(new JniTime(firstEventTimestamp) );
                testEvent = testTrace.readNextEvent();
                assertNotSame("seekToTime(time) to seek back returned null",null,testEvent);
                assertEquals("seekToTime(time) timestamp after seek back is incoherent",firstEventTimestamp,testEvent.getEventTime().getTime());
        }
}
