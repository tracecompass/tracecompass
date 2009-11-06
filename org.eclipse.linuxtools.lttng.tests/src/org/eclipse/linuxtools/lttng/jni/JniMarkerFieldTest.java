
package org.eclipse.linuxtools.lttng.jni;

import static org.junit.Assert.*;
import org.junit.Test;

/*
 Functions tested here :
        public JniMarkerField(JniMarkerField oldMarkerField)
        public JniMarkerField(long newMarkerPtr) throws JniException
        
        public String getField()
        public String getFormat()
        
        public String toString()
        public void printMarkerFieldInformation()
*/

public class JniMarkerFieldTest
{
        private final static String tracepath="traceset/trace-618339events-1293lost-1cpu";
        private final static String eventName="kernel0";
        
        private JniMarkerField prepareMarkerFieldToTest() {
                
                JniEvent tmpEvent = null;
                JniMarkerField tmpMarkerField = null;
                
                // This trace should be valid
                // We will read the first 2 event to have something interesting to test on
                try {
                        tmpEvent = new JniTrace(tracepath).requestEventByName(eventName);
                        tmpEvent.readNextEvent();
                        tmpEvent.readNextEvent();
                        
                        // Use the first field
                        tmpMarkerField = tmpEvent.requestEventMarker().getMarkerFieldsArrayList().get(0);
                }
                catch( JniException e) { }
                
                return tmpMarkerField;
        }
        
        
        @Test
        public void testEventConstructors() {
                
                JniMarker tmpMarker = null;
                
                JniMarkerField tmpMarkerField1 = null;
                JniMarkerField tmpMarkerField2 = null;
                
                // This event should be valid and will be used in test
                try {
                        tmpMarker = new JniTrace(tracepath).requestEventByName(eventName).requestEventMarker();
                }
                catch( JniException e) { }
                
                // Test constructor with pointer on a wrong pointer
                try {
                        tmpMarkerField1 = new JniMarkerField( new Jni_C_Pointer(0) );
                        fail("Construction with wrong pointer should fail!");
                }
                catch( JniException e) {
                }
                
                // Test constructor with pointer on a correct pointer
                try {
                        tmpMarkerField1 = new JniMarkerField( tmpMarker.getMarkerFieldsArrayList().get(0).getMarkerFieldPtr() );
                }
                catch( JniException e) {
                        fail("Construction with correct pointer failed!");
                }
                
                
                // Test copy constructor
                try {
                        tmpMarkerField1 = new JniMarkerField( tmpMarker.getMarkerFieldsArrayList().get(0) );
                        tmpMarkerField2 = new JniMarkerField( tmpMarkerField1);
                }
                catch( Exception e) {
                        fail("Copy constructor failed!");
                }
                
                assertSame("JniMarker name not same after using copy constructor", tmpMarkerField1.getField() , tmpMarkerField2.getField());
                
        }
        
        @Test
        public void testGetSet() {
                
                JniMarkerField testMarkerField = prepareMarkerFieldToTest();
                
                // Test that all Get/Set return data
                assertNotSame("getName is empty","",testMarkerField.getField() );
                assertNotSame("getFormat is empty","",testMarkerField.getFormat() );
                assertNotSame("getMarkerFieldPtr is 0",0,testMarkerField.getMarkerFieldPtr() );
        }
        
        @Test
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
