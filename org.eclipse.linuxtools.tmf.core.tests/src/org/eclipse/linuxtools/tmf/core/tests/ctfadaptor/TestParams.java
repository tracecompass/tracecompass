package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import java.io.File;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;


public abstract class TestParams {

    /* Path to test traces */
    private static final String testTracePath1 = "testfiles/kernel"; //$NON-NLS-1$
    private static CtfTmfTrace testTrace1 = null;

    private static final File emptyFile = new File(""); //$NON-NLS-1$
    private static CtfTmfTrace emptyTrace = new CtfTmfTrace();

    public static File getEmptyFile() {
        return emptyFile;
    }

    public static CtfTmfTrace getEmptyTrace() {
        return emptyTrace;
    }

    public static String getPath(){
        return testTracePath1;
    }

    public synchronized static CtfTmfTrace createTrace() throws TmfTraceException {
        if (testTrace1 == null) {
            testTrace1 = new CtfTmfTrace();
            testTrace1.initTrace(null, testTracePath1, CtfTmfEvent.class);
        }
        return testTrace1;
    }
}
