package org.eclipse.linuxtools.ctf.core.tests;

import java.io.File;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;

/**
 * Here are the definitions common to all the CTF parser tests.
 * 
 * @author alexmont
 *
 */
public abstract class TestParams {
    
    /* Path to test traces */
    private static final String testTracePath1 = "Tests/traces/trace20m"; //$NON-NLS-1$
    private static CTFTrace testTrace1 = null;
    private static CTFTrace testTraceFromFile1 = null;
    
    private static final File emptyFile = new File(""); //$NON-NLS-1$
    private static CTFTrace emptyTrace = null;
    
    public static File getEmptyFile() {
        return emptyFile;
    }
    
    public static CTFTrace getEmptyTrace() {
        if (emptyTrace == null) {
            try {
                emptyTrace = new CTFTrace(""); //$NON-NLS-1$
            } catch (CTFReaderException e) {
                /* We know this trace should exist */
                throw new RuntimeException(e);
            } 
        }
        return emptyTrace;
    }
    
    public static CTFTrace createTrace() throws CTFReaderException {
        if (testTrace1 == null) {
            testTrace1 = new CTFTrace(testTracePath1);
        }
        return testTrace1;
    }

    public static CTFTrace createTraceFromFile() {
        if (testTraceFromFile1 == null) {
            try {
                testTraceFromFile1 = new CTFTrace(new File(testTracePath1));
            } catch (CTFReaderException e) {
                /* We know this trace should exist */
                throw new RuntimeException(e);
            }
        }
        return testTraceFromFile1;
    }
}
