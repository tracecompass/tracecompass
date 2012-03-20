package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;


public abstract class TestParams {
    
    /* Path to test traces */
    private static final String testTracePath1 = "Tests/traces/trace20m"; //$NON-NLS-1$
    private static CtfTmfTrace testTrace1 = null;
    
    private static final File emptyFile = new File(""); //$NON-NLS-1$
    private static CtfTmfTrace emptyTrace = new CtfTmfTrace();
    
    public static File getEmptyFile() {
        return emptyFile;
    }
    
    public static CtfTmfTrace getEmptyTrace() {
        return emptyTrace;
    }
    
    public static CtfTmfTrace createTrace() throws FileNotFoundException {
        if ( testTrace1 == null ) {
            testTrace1 = new CtfTmfTrace();
            testTrace1.initTrace("test-trace", testTracePath1, CtfTmfEvent.class); //$NON-NLS-1$
        }
        return testTrace1;
    }
}
