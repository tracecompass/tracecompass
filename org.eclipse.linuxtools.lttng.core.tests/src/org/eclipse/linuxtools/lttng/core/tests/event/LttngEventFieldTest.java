package org.eclipse.linuxtools.lttng.core.tests.event;



import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEventField;
import org.eclipse.linuxtools.internal.lttng.core.trace.LTTngTextTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;
import org.osgi.framework.FrameworkUtil;

/*
 Functions tested here :
        public LttngEventField(String name, Object newContent)
        public LttngEventField(LttngEventField oldField)
        public String getName()
        public String toString()

 */
@SuppressWarnings("nls")
public class LttngEventFieldTest extends TestCase {
    private final static String tracepath1="traceset/trace-15316events_nolost_newformat.txt";
    private final static boolean skipIndexing=true;

    //    private final static String firstEventName 		= "alignment";
    private final static String firstEventValue 	= "0";

    private static LTTngTextTrace testStream = null;
    private LTTngTextTrace initializeEventStream() {
        if (testStream == null)
            try {
                final URL location = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(tracepath1), null);
                final File testfile = new File(FileLocator.toFileURL(location).toURI());
                final LTTngTextTrace tmpStream = new LTTngTextTrace(null, testfile.getPath(), skipIndexing);
                testStream = tmpStream;
            }
        catch (final Exception e) {
            System.out.println("ERROR : Could not open " + tracepath1);
            testStream = null;
        }
        else
            testStream.seekEvent(0);

        return testStream;
    }

    private LttngEventField prepareToTest() {
        LttngEventField tmpField = null;

        // This trace should be valid
        try {
            final LTTngTextTrace tmpStream = initializeEventStream();
            tmpField = (LttngEventField)tmpStream.getNextEvent( new TmfContext(new TmfLocation<Long>(0L), 0) ).getContent().getField(0);
        }
        catch (final Exception e) {
            fail("ERROR : Failed to get field!");
        }

        return tmpField;
    }

    public void testConstructors() {
        LttngEventField testField 	= null;

        // Default construction with good argument
        try {
            testField = new LttngEventField("test", "test");
        }
        catch( final Exception e) {
            fail("Default construction failed!");
        }

        // Copy constructor with correct parameters
        try {
            testField = new LttngEventField("test", "test");
            new LttngEventField(testField);
        }
        catch( final Exception e) {
            fail("Copy constructor failed!");
        }

    }

    public void testGetter() {

        // *** To "really" test the field, we will get a real field from LTTngTrace
        final LTTngTextTrace tmpStream = initializeEventStream();

        LttngEventField testField = null;
        //        try {
        testField = (LttngEventField) tmpStream.getNextEvent( new TmfContext(new TmfLocation<Long>(0L), 0) ).getContent().getField(0);
        //        } catch (TmfNoSuchFieldException e) {
        //            e.printStackTrace();
        //        }
        assertNotSame("getField is null!", null, testField);

        // *** FIXME ***
        // Depending from the Java version because of the "hashcode()" on String.
        // We can't really test that safetly
        //
        //assertTrue("getName() returned unexpected result!",firstEventName.equals(testField.getId().toString()));
        assertNotSame("getName() returned unexpected result!",null, testField.getName());

        assertTrue("getValue() returned unexpected result!",firstEventValue.equals(testField.getValue().toString()));


    }

    public void testToString() {
        final LttngEventField tmpField = prepareToTest();

        // Just make sure toString() does not return null or the java reference
        assertNotSame("toString returned null",null, tmpField.toString() );
        assertNotSame("toString is not overridded!", tmpField.getClass().getName() + '@' + Integer.toHexString(tmpField.hashCode()), tmpField.toString() );
    }

}
