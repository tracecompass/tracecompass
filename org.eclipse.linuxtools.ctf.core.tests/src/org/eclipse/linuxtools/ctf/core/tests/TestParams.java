package org.eclipse.linuxtools.ctf.core.tests;

import java.io.File;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;

/**
 * Here are the definitions common to all the CTF parser tests.
 *
 * @author alexmont
 */
@SuppressWarnings("nls")
public abstract class TestParams {

    /*
     * Path to test traces. Make sure you run the traces/get-traces.sh script
     * first!
     */
    private static final String testTracePath1 = "traces/kernel";
    private static CTFTrace testTrace1 = null;
    private static CTFTrace testTraceFromFile1 = null;
    private static final File testTraceFile1 = new File(testTracePath1 + "/channel0_0");

    private static final File emptyFile = new File("");
    private static CTFTrace emptyTrace = null;

    /**
     * Return an empty file (new File("");)
     *
     * @return An empty file
     */
    public static File getEmptyFile() {
        return emptyFile;
    }

    /**
     * Return a file in test trace #1 (channel0_0).
     *
     * Make sure {@link #tracesExist()} before calling this!
     *
     * @return A file in a test trace
     */
    public static File getTraceFile(){
        return testTraceFile1;
    }

    /**
     * Return a trace out of an empty file (new CTFTrace("");)
     *
     * @return An empty trace
     */
    public static CTFTrace getEmptyTrace() {
        if (emptyTrace == null) {
            try {
                emptyTrace = new CTFTrace("");
            } catch (CTFReaderException e) {
                /* Should always work... */
                throw new RuntimeException(e);
            }
        }
        return emptyTrace;
    }

    /**
     * Get a CTFTrace reference to test trace #1.
     *
     * Make sure {@link #tracesExist()} before calling this!
     *
     * @return Reference to test trace #1
     * @throws CTFReaderException
     *             If the trace cannot be found
     */
    public static CTFTrace createTrace() throws CTFReaderException {
        if (testTrace1 == null) {
            testTrace1 = new CTFTrace(testTracePath1);
        }
        return testTrace1;
    }

    /**
     * Same as {@link #createTrace()}, except the CTFTrace is create from the
     * File object and not the path.
     *
     * Make sure {@link #tracesExist()} before calling this!
     *
     * @return Reference to test trace #1
     */
    public static CTFTrace createTraceFromFile() {
        if (testTraceFromFile1 == null) {
            try {
                testTraceFromFile1 = new CTFTrace(new File(testTracePath1));
            } catch (CTFReaderException e) {
                /* This trace should exist */
                throw new RuntimeException(e);
            }
        }
        return testTraceFromFile1;
    }

    /**
     * Check if the test traces are present in the tree. If not, you can get
     * them by running traces/get-traces.sh or traces/get-traces.xml
     *
     * @return True if *all* the test files could be found, false otherwise.
     */
    public static boolean tracesExist() {
        if (testTrace1 != null) {
            return true;
        }
        try {
            createTrace();
        } catch (CTFReaderException e) {
            return false;
        }
        return true;
    }
}
