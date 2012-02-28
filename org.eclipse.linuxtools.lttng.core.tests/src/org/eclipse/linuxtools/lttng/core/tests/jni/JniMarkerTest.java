
package org.eclipse.linuxtools.lttng.core.tests.jni;


import junit.framework.TestCase;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;

/*
 Functions tested here :
        public JniMarker(JniMarker oldMarker)
        public JniMarker(long newMarkerPtr) throws JniException
        
        public String[] requestMarkerFieldToString()
        
        public String getName()
        public String  getFormatOverview()
        public ArrayList<JniMarkerField> getMarkerFieldArrayList()
        
        public String toString()
        public void printMarkerInformation()
*/

@SuppressWarnings("nls")
public class JniMarkerTest extends TestCase
{
		private final static boolean printLttDebug = false;
		
        private final static String tracepath="traceset/trace-15316events_nolost_newformat";
        private final static String eventName="kernel0";
        
        private final static int    numberOfMarkersFieldInMarker = 3;
        
        private JniMarker prepareMarkerToTest() {
                
                JniEvent tmpEvent = null;
                JniMarker tmpMarker = null;
                
                // This trace should be valid
                // We will read the second event to have something interesting to test on
                try {
                        tmpEvent = JniTraceFactory.getJniTrace(tracepath, null, printLttDebug).requestEventByName(eventName);
                        tmpEvent.readNextEvent();
                        
                        tmpMarker = tmpEvent.requestEventMarker();
                }
                catch( JniException e) { }
                
                return tmpMarker;
        }
        
        public void testEventConstructors() {
                
                JniEvent tmpEvent = null;
                
                // This event should be valid and will be used in test
                try {
                        tmpEvent = JniTraceFactory.getJniTrace(tracepath, null, printLttDebug).requestEventByName(eventName);
                }
                catch( JniException e) {
                    fail("Could not find event");
                }
                
                // Test constructor with pointer on a correct pointer
                try {
                        tmpEvent.getParentTracefile().allocateNewJniMarker( tmpEvent.requestEventMarker().getMarkerPtr() );
                }
                catch( JniException e) {
                        fail("Construction with correct pointer failed!");
                }
                
                /*
                // Test copy constructor
                try {
                        testMarker1 = new JniMarker( tmpEvent.requestEventMarker().getMarkerPtr() );
                        testMarker2 = new JniMarker( testMarker1);
                }
                catch( JniException e) {
                        fail("Copy constructor failed!");
                }
                assertSame("JniMarker name not same after using copy constructor", testMarker1.getName() , testMarker2.getName());
                */
                
        }
        
        public void testGetSet() {
                
                JniMarker testMarker = prepareMarkerToTest();
                
                // Test that all Get/Set return data
                assertNotSame("getName is empty","",testMarker.getName() );
                assertNotSame("getFormat is empty","",testMarker.getFormatOverview() );
                
                assertNotSame("getMarkerFieldArrayList is null",null,testMarker.getMarkerFieldsArrayList() );
                // Also check that the map contain a certains number of data
                assertSame("getMarkerFieldArrayList returned an unexpected number of markers",numberOfMarkersFieldInMarker,testMarker.getMarkerFieldsArrayList().size() );
                
                assertNotSame("getMarkerPtr is 0",0,testMarker.getMarkerPtr() );
        }
        
        public void testPrintAndToString() {
                
                JniMarker testMarker = prepareMarkerToTest();
                
                // Test printMarkerInformation
                try {
                        testMarker.printMarkerInformation();
                }
                catch( Exception e) { 
                        fail("printMarkerInformation failed!");
                }
                
                // Test ToString()
                assertNotSame("toString returned empty data","",testMarker.toString() );
                
        }
}
