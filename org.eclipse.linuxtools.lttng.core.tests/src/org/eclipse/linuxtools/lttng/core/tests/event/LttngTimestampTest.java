package org.eclipse.linuxtools.lttng.core.tests.event;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.lttng.core.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.core.tests.LTTngCoreTestPlugin;
import org.eclipse.linuxtools.lttng.core.trace.LTTngTextTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;

/*
 Functions tested here :
 public LttngTimestamp()
 public LttngTimestamp(long newEventTime)
 public LttngTimestamp(TmfTimestamp oldEventTime)

 public long getValue()
 public String getSeconds()
 public String getNanoSeconds()

 public void setValue(long newValue)

 public String toString()
 */

@SuppressWarnings("nls")
public class LttngTimestampTest extends TestCase {
    private final static String tracepath1 = "traceset/trace-15316events_nolost_newformat.txt";
    private final static boolean skipIndexing = true;

    private final static String firstEventTimeSecond = "13589";
    private final static String firstEventTimeNano   = "759412128";
    private final static long   firstEventTimeFull   = 13589759412128L;

    private static LTTngTextTrace testStream = null;

    private LTTngTextTrace initializeEventStream() {
        if (testStream == null) {
            try {
                URL location = FileLocator.find(LTTngCoreTestPlugin.getPlugin().getBundle(), new Path(tracepath1), null);
                File testfile = new File(FileLocator.toFileURL(location).toURI());
                LTTngTextTrace tmpStream = new LTTngTextTrace(testfile.getName(), testfile.getPath(), skipIndexing);
                testStream = tmpStream;
            } catch (Exception e) {
                System.out.println("ERROR : Could not open " + tracepath1);
                testStream = null;
            }
        }
        return testStream;
    }

    private LttngTimestamp prepareToTest() {
        LttngTimestamp tmpTime = null;

        // This trace should be valid
        try {
            LTTngTextTrace tmpStream = initializeEventStream();
            tmpTime = (LttngTimestamp) tmpStream.getNextEvent(new TmfContext(null, 0)).getTimestamp();
        } catch (Exception e) {
            fail("ERROR : Failed to get reference!");
        }

        return tmpTime;
    }

    public void testConstructors() {
        LttngTimestamp tmpTime = null;

        // Default construction with no argument
        try {
            tmpTime = new LttngTimestamp();
        } catch (Exception e) {
            fail("Construction failed!");
        }

        // Default construction with good argument
        try {
            tmpTime = new LttngTimestamp(1);
        } catch (Exception e) {
            fail("Construction failed!");
        }

        // Copy constructor
        try {
            tmpTime = new LttngTimestamp(1);
            new LttngTimestamp(tmpTime);
        } catch (Exception e) {
            fail("Construction failed!");
        }
    }

    public void testGetter() {
        LttngTimestamp tmpTime = prepareToTest();

        assertEquals("Time in second is wrong", firstEventTimeSecond, tmpTime.getSeconds());
        assertEquals("Time in nano second is wrong", firstEventTimeNano, tmpTime.getNanoSeconds());

        assertEquals("Full time is wrong", firstEventTimeFull, tmpTime.getValue());
    }

    public void testSetter() {
        LttngTimestamp tmpTime = prepareToTest();

        // We will set a time and we will make sure the set is working then
        tmpTime.setValue(1);
        assertEquals("Full time is wrong after set", 1, tmpTime.getValue());
    }

    public void testToString() {
        LttngTimestamp tmpTime = prepareToTest();

        // Just make sure toString() does not return null or the java reference
        assertNotSame("toString returned null", null, tmpTime.toString());
        assertNotSame("toString is not overridded!", tmpTime.getClass().getName() + '@' + Integer.toHexString(tmpTime.hashCode()), tmpTime.toString());
    }

    // Better test...
    public void testToString2() {
        LttngTimestamp ts1 = new LttngTimestamp(2064357056377L);
        String expectedTS1 = "2064.357056377";

        LttngTimestamp ts2 = new LttngTimestamp(1L);
        String expectedTS2 = "0.000000001";

        LttngTimestamp ts3 = new LttngTimestamp(123456789L);
        String expectedTS3 = "0.123456789";

        LttngTimestamp ts4 = new LttngTimestamp(1234567890L);
        String expectedTS4 = "1.234567890";

        assertEquals("toString()", expectedTS1, ts1.toString());
        assertEquals("toString()", expectedTS2, ts2.toString());
        assertEquals("toString()", expectedTS3, ts3.toString());
        assertEquals("toString()", expectedTS4, ts4.toString());
        
        LttngTimestamp ts5 = new LttngTimestamp(2234567890L);
        LttngTimestamp delta = ts4.getDelta(ts5);
        String expectedDelta = "-1.000000000";
        assertEquals("toString()", expectedDelta, delta.toString());
    }
}
