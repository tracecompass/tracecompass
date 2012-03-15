
package org.eclipse.linuxtools.lttng.core.tests.jni;


import junit.framework.TestCase;

import org.eclipse.linuxtools.internal.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniMarkerField;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;

/*
 Functions tested here :
        public JniMarkerField(JniMarkerField oldMarkerField)
        public JniMarkerField(long newMarkerPtr) throws JniException
        
        public String getField()
        public String getFormat()
        
        public String toString()
        public void printMarkerFieldInformation()
*/

@SuppressWarnings("nls")
public class JniMarkerFieldTest extends TestCase
{
		private final static boolean printLttDebug = false;
	
        private final static String tracepath="traceset/trace-15316events_nolost_newformat";
        private final static String eventName="kernel0";
        
        private JniMarkerField prepareMarkerFieldToTest() {
                
                JniEvent tmpEvent = null;
                JniMarkerField tmpMarkerField = null;
                
                // This trace should be valid
                // We will read the first 2 event to have something interesting to test on
                try {
                        tmpEvent = JniTraceFactory.getJniTrace(tracepath, null, printLttDebug).requestEventByName(eventName);
                        tmpEvent.readNextEvent();
                        tmpEvent.readNextEvent();
                        
                        // Use the first field
                        tmpMarkerField = tmpEvent.requestEventMarker().getMarkerFieldsArrayList().get(0);
                }
                catch( JniException e) { }
                
                return tmpMarkerField;
        }
        
        public void testEventConstructors() {
                
                JniMarker tmpMarker = null;
                
                // This event should be valid and will be used in test
                try {
                        tmpMarker = JniTraceFactory.getJniTrace(tracepath, null, printLttDebug).requestEventByName(eventName).requestEventMarker();
                }
                catch( JniException e) {
                    fail("Could not get marker");
                }
                
                // Test constructor with pointer on a correct pointer
                try {
                        tmpMarker.allocateNewJniMarkerField( tmpMarker.getMarkerFieldsArrayList().get(0).getMarkerFieldPtr() );
                }
                catch( JniException e) {
                        fail("Construction with correct pointer failed!");
                }
                
                /*
                // Test copy constructor
                try {
                        tmpMarkerField1 = new JniMarkerField( tmpMarker.getMarkerFieldsArrayList().get(0) );
                        tmpMarkerField2 = new JniMarkerField( tmpMarkerField1);
                }
                catch( Exception e) {
                        fail("Copy constructor failed!");
                }
                assertSame("JniMarker name not same after using copy constructor", tmpMarkerField1.getField() , tmpMarkerField2.getField());
                */
                
        }
        
        public void testGetSet() {
                
                JniMarkerField testMarkerField = prepareMarkerFieldToTest();
                
                // Test that all Get/Set return data
                assertNotSame("getName is empty","",testMarkerField.getField() );
                assertNotSame("getFormat is empty","",testMarkerField.getFormat() );
                assertNotSame("getMarkerFieldPtr is 0",0,testMarkerField.getMarkerFieldPtr() );
        }
        
        public void testPrintAndToString() {
                
                JniMarkerField testMarkerField = prepareMarkerFieldToTest();
                
                // Test printMarkerInformation
                try {
                        testMarkerField.printMarkerFieldInformation();
                }
                catch( Exception e) { 
                        fail("printMarkerFieldInformation failed!");
                }
                
                // Test ToString()
                assertNotSame("toString returned empty data","",testMarkerField.toString() );
        }
}
