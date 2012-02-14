package org.eclipse.linuxtools.lttng.core.tests.event;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.lttng.core.event.LttngEventReference;
import org.eclipse.linuxtools.lttng.core.tests.LTTngCoreTestPlugin;
import org.eclipse.linuxtools.lttng.core.trace.LTTngTextTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;

/*
 Functions tested here :
    public LttngEventReference(String newTraceName)
    public LttngEventReference(String newTracefilePath, String newTraceName)
    public LttngEventReference(LttngEventReference oldReference)
    
    public String getTracepath()
    public String getValue()
    
    public void setTracepath(String tracename)
    public void setValue(String newReference)
    
    public String toString()
 */

@SuppressWarnings("nls")
public class LttngEventReferenceTest extends TestCase {
    private final static String tracepath1="traceset/trace-15316events_nolost_newformat.txt";
    private final static boolean skipIndexing=true;
    
    private final static String firstEventReference        = "metadata_0";
    
    private static LTTngTextTrace testStream = null;
    private LTTngTextTrace initializeEventStream() {
		if (testStream == null) {
			try {
				URL location = FileLocator.find(LTTngCoreTestPlugin.getPlugin().getBundle(), new Path(tracepath1), null);
				File testfile = new File(FileLocator.toFileURL(location).toURI());
				LTTngTextTrace tmpStream = new LTTngTextTrace(testfile.getPath(), skipIndexing);
				testStream = tmpStream;
			} 
			catch (Exception e) {
				System.out.println("ERROR : Could not open " + tracepath1);
				testStream = null;
			}
		}
		else {
			testStream.seekEvent(0);
		}
		
		return testStream;
	}
    
    private LttngEventReference prepareToTest() {
        LttngEventReference tmpEventRef = null;

        // This trace should be valid
        try {
            LTTngTextTrace tmpStream = initializeEventStream();
            tmpEventRef = (LttngEventReference)tmpStream.getNextEvent(new TmfContext(null, 0) ).getReference();
        } 
        catch (Exception e) {
            fail("ERROR : Failed to get reference!");
        }

        return tmpEventRef;
    }
    
    public void testConstructors() {
        LttngEventReference testRef = null;
        @SuppressWarnings("unused")
        LttngEventReference testRef2 = null;
        
        // Default construction with good argument (newTracefilePath)
        try {
            testRef = new LttngEventReference("test");
        }
        catch( Exception e) { 
            fail("Construction failed!");
        }
        
        // Default construction with good arguments (newTracefilePath, newTraceName)
        try {
            testRef = new LttngEventReference("test", "test");
        }
        catch( Exception e) { 
            fail("Construction failed!");
        }
        
        // Copy constructor
        try {
            testRef = new LttngEventReference("test", "test");
            testRef2 = new LttngEventReference(testRef);
        }
        catch( Exception e) { 
            fail("Construction failed!");
        }
    }
    
    
    public void testGetter() {
        LttngEventReference tmpRef = prepareToTest();
        
        assertTrue("Tracepath not what was expected!",((String)tmpRef.getValue()).contains(firstEventReference) );
        assertEquals("Content not what expected!",firstEventReference,tmpRef.getTracepath());
    }
    
    public void testSetter() {
    	// Not much to do here, we will just make sure the setter does not throw
        LttngEventReference tmpRef = prepareToTest();
        
        try {
        	tmpRef.setTracepath("test");
    	}
    	catch( Exception e) { 
        	fail("setTracepath(string) failed!");
        }
    	
    	try {
        	tmpRef.setValue("test");
    	}
    	catch( Exception e) { 
        	fail("setTracepath(string) failed!");
        }
    }
    
    public void testToString() {
        LttngEventReference tmpRef = prepareToTest();
        
        // Just make sure toString() does not return null or the java reference
        assertNotSame("toString returned null",null, tmpRef.toString() );
        assertNotSame("toString is not overridded!", tmpRef.getClass().getName() + '@' + Integer.toHexString(tmpRef.hashCode()), tmpRef.toString() );
    }
    
}
