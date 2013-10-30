/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.shared;

import java.io.File;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;

/**
 * Here is the list of the available test traces for the CTF parser.
 *
 * Make sure you run the traces/get-traces.xml Ant script to download them
 * first!
 *
 * @author Alexandre Montplaisir
 */
public enum CtfTestTrace {
    /** Example kernel trace */
    KERNEL("../org.eclipse.linuxtools.ctf.core.tests/traces/kernel"),

    /** Another kernel trace */
    TRACE2("../org.eclipse.linuxtools.ctf.core.tests/traces/trace2"),

    /** Kernel trace with event contexts */
    KERNEL_VM("../org.eclipse.linuxtools.ctf.core.tests/traces/kernel_vm"),

    /** Trace synchronization: source trace */
    SYNC_SRC("../org.eclipse.linuxtools.ctf.core.tests/traces/synctraces/scp_src"),

    /** Trace synchronization: destination trace */
    SYNC_DEST("../org.eclipse.linuxtools.ctf.core.tests/traces/synctraces/scp_dest"),

    /** UST trace with lots of lost events */
    HELLO_LOST("../org.eclipse.linuxtools.ctf.core.tests/traces/hello-lost"),

    /** UST trace with lttng-ust-cyg-profile events (aka -finstrument-functions) */
    CYG_PROFILE("../org.eclipse.linuxtools.ctf.core.tests/traces/cyg-profile/glxgears-cyg-profile"),

    /** UST trace with lttng-ust-cyg-profile-fast events (no address in func_exit) */
    CYG_PROFILE_FAST("../org.eclipse.linuxtools.ctf.core.tests/traces/cyg-profile/glxgears-cyg-profile-fast");


    private final String fPath;
    private CTFTrace fTrace = null;
    private CTFTrace fTraceFromFile = null;

    private CtfTestTrace(String path) {
        fPath = path;
    }

    /** @return The path to the test trace */
    public String getPath() {
        return fPath;
    }

    /**
     * Get a CTFTrace instance of a test trace. Make sure
     * {@link #exists()} before calling this!
     *
     * @return The CTFTrace object
     * @throws CTFReaderException
     *             If the trace cannot be found.
     */
    public CTFTrace getTrace() throws CTFReaderException {
        if (fTrace == null) {
            fTrace = new CTFTrace(fPath);
        }
        return fTrace;
    }

    /**
     * Get a CTFTrace instance created from a File. Make sure
     * {@link #exists()} before calling this!
     *
     * @return The CTFTrace object
     * @throws CTFReaderException
     *             If the trace cannot be found.
     */
    public CTFTrace getTraceFromFile() throws CTFReaderException {
        if (fTraceFromFile == null) {
            fTraceFromFile = new CTFTrace(new File(fPath));
        }
        return fTraceFromFile;
    }

    /**
     * Check if this test trace actually exists on disk.
     *
     * @return If the trace exists
     */
    public boolean exists() {
        try {
            getTrace();
        } catch (CTFReaderException e) {
            return false;
        }
        return true;
    }
}
