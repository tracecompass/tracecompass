package org.eclipse.linuxtools.lttng.core.tests.event;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.lttng.core.event.LttngEventType;
import org.eclipse.linuxtools.lttng.core.tests.LTTngCoreTestPlugin;
import org.eclipse.linuxtools.lttng.core.trace.LTTngTextTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;

/*
 Functions tested here :
    public LttngEventType()
    public LttngEventType(String thisTracefileName, Long thisCpuId, String thisMarkerName, String[] thisMarkerfieldsName)
    public LttngEventType(LttngEventType oldType)
    
    public String getTracefileName()
    public Long getCpuId()
    public String getMarkerName()
    
    public String toString()
 */

@SuppressWarnings("nls")
public class LttngEventTypeTest extends TestCase {
	private final static String tracepath1="traceset/trace-15316events_nolost_newformat.txt";
    private final static boolean skipIndexing=true;
    
    private final static String firstEventChannel       = "metadata";
    private final static long firstEventCpu             = 0;
    private final static String firstEventMarker        = "core_marker_id";
    
    private static LTTngTextTrace testStream = null;
    private LTTngTextTrace initializeEventStream() {
		if (testStream == null) {
			try {
				URL location = FileLocator.find(LTTngCoreTestPlugin.getPlugin().getBundle(), new Path(tracepath1), null);
				File testfile = new File(FileLocator.toFileURL(location).toURI());
				LTTngTextTrace tmpStream = new LTTngTextTrace(testfile.getName(), testfile.getPath(), skipIndexing);
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
    
    private LttngEventType prepareToTest() {
        LttngEventType tmpEventType = null;

        // This trace should be valid
        try {
            LTTngTextTrace tmpStream = initializeEventStream();
            tmpEventType = (LttngEventType)tmpStream.getNextEvent( new TmfContext(null, 0) ).getType();
        } 
        catch (Exception e) {
            fail("ERROR : Failed to get reference!");
        }

        return tmpEventType;
    }
    
    public void testConstructors() {
        LttngEventType tmpEventType = null;
        
        // Default construction, no argument
        try {
            tmpEventType = new LttngEventType();
        }
        catch( Exception e) { 
            fail("Construction failed!");
        }
        
        // Default construction with good arguments
        try {
            tmpEventType = new LttngEventType("test", 0L, "test",  0, new String[] { "test" });
        }
        catch (Exception e) { 
            fail("Construction failed!");
        }
        
        // Copy constructor
        try {
            tmpEventType = new LttngEventType("test", 0L, "test", 0, new String[] { "test" });
            new LttngEventType(tmpEventType);
        }
        catch (Exception e) { 
            fail("Construction failed!");
        }
    }
    
    
    public void testGetter() {
        LttngEventType tmpEventType = prepareToTest();
        
        assertTrue("Channel name not what was expected!",firstEventChannel.equals((String)tmpEventType.getTracefileName()) );
        assertTrue("Cpu Id not what was expected!",firstEventCpu == tmpEventType.getCpuId() );
        assertTrue("Marker Name not what was expected!",firstEventMarker.equals((String)tmpEventType.getMarkerName()) );
        // Just test the non-nullity of labels
        assertNotSame("getLabels returned null",null, tmpEventType.getFieldNames() );
    }
    
    public void testToString() {
        LttngEventType tmpEventType = prepareToTest();
        
        // Just make sure toString() does not return null or the java reference
        assertNotSame("toString returned null",null, tmpEventType.toString() );
        assertNotSame("toString is not overridded!", tmpEventType.getClass().getName() + '@' + Integer.toHexString(tmpEventType.hashCode()), tmpEventType.toString() );
    }
    
}
