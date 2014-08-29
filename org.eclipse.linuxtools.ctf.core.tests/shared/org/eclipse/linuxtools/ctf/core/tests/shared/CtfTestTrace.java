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

import org.eclipse.linuxtools.ctf.core.tests.synthetictraces.LttngKernelTraceGenerator;
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
    /**
     * Example kernel trace
     *
     * <pre>
     * Trace Size: 13 MB
     * Tracer: lttng-modules 2.0.0
     * Event count: 695 319
     * Kernel version: 3.0.0-16-generic-pae
     * Trace length: 10s
     * </pre>
     */
    KERNEL("../org.eclipse.linuxtools.ctf.core.tests/traces/kernel"),

    /**
     * Another kernel trace
     *
     * <pre>
     * Trace Size: 14 MB
     * Tracer: lttng-modules 2.0.0
     * Event count: 595 641
     * Kernel version: 3.2.0-18-generic
     * Trace length: 11s
     * </pre>
     */
    TRACE2("../org.eclipse.linuxtools.ctf.core.tests/traces/trace2"),

    /**
     * Kernel trace with event contexts: pid, ppid, tid, procname,
     * perf_page_fault, perf_major_faults, perf_minor_faults
     *
     * <pre>
     * Trace Size: 56 MB
     * Tracer: lttng-modules 2.1.0
     * Event count: 714 484
     * Kernel version: 3.8.1
     * Trace length: 29s
     * </pre>
     */
    KERNEL_VM("../org.eclipse.linuxtools.ctf.core.tests/traces/kernel_vm"),

    /**
     * Kernel trace with all events enabled. Contains 'inet_sock_local_*' events
     * provided by Francis's Giraldeau lttng-modules addons branch to trace TCP
     * events. Can be used along with {@link CtfTestTrace#SYNC_DEST} for trace
     * synchronization.
     *
     * <pre>
     * Trace Size: 2.4 MB
     * Tracer: lttng-modules 2.1.0
     * Event count: 110 771
     * Kernel version: 3.6.11-1-ARCH
     * Trace length: 22s
     * </pre>
     */
    SYNC_SRC("../org.eclipse.linuxtools.ctf.core.tests/traces/synctraces/scp_src"),

    /**
     * Kernel trace with all events enabled. Contains 'inet_sock_local_*' events
     * provided by Francis's Giraldeau lttng-modules addons branch to trace TCP
     * events. Can be used along with {@link CtfTestTrace#SYNC_SRC} for trace
     * synchronization.
     *
     * <pre>
     * Trace Size: 1.9 MB
     * Tracer: lttng-modules 2.1.0
     * Event count: 85 729
     * Kernel version: 3.6.11-1-ARCH
     * Trace length: 17s
     * </pre>
     */
    SYNC_DEST("../org.eclipse.linuxtools.ctf.core.tests/traces/synctraces/scp_dest"),

    /**
     * LTTng Kernel trace. Contains 'inet_sock_local_*' events provided by
     * Francis's Giraldeau lttng-modules addons branch to trace TCP events. Can
     * be used along with {@link CtfTestTrace#DJANGO_DB} and
     * {@link CtfTestTrace#DJANGO_HTTPD} for trace synchronization.
     *
     * <pre>
     * Trace Size: 33 MB
     * Tracer: lttng-modules 2.4.0
     * Event count: 754 787
     * Kernel version: 3.13.0-24-generic
     * Trace length: 15s
     * </pre>
     */
    DJANGO_CLIENT("../org.eclipse.linuxtools.ctf.core.tests/traces/django-benchmark/django-client"),

    /**
     * LTTng Kernel trace. Contains 'inet_sock_local_*' events provided by
     * Francis's Giraldeau lttng-modules addons branch to trace TCP events. Can
     * be used along with {@link CtfTestTrace#DJANGO_CLIENT} and
     * {@link CtfTestTrace#DJANGO_HTTPD} for trace synchronization.
     *
     * <pre>
     * Trace Size: 28 MB
     * Tracer: lttng-modules 2.4.0
     * Event count: 692 098
     * Kernel version: 3.13.0-24-generic
     * Trace length: 14s
     * </pre>
     */
    DJANGO_DB("../org.eclipse.linuxtools.ctf.core.tests/traces/django-benchmark/django-db"),

    /**
     * LTTng Kernel trace. Contains 'inet_sock_local_*' events provided by
     * Francis's Giraldeau lttng-modules addons branch to trace TCP events. Can
     * be used along with {@link CtfTestTrace#DJANGO_DB} and
     * {@link CtfTestTrace#DJANGO_CLIENT} for trace synchronization.
     *
     * <pre>
     * Trace Size: 31 MB
     * Tracer: lttng-modules 2.4.0
     * Event count: 779 096
     * Kernel version:3.13.0-24-generic
     * Trace length: 13s
     * </pre>
     */
    DJANGO_HTTPD("../org.eclipse.linuxtools.ctf.core.tests/traces/django-benchmark/django-httpd"),

    /**
     * UST trace with lots of lost events
     *
     * <pre>
     * Trace Size: 3.4 MB
     * Tracer: lttng-ust 2.3
     * Event count: 1 000 000, with 967 700 lost events
     * Trace length: 279ms
     * </pre>
     */
    HELLO_LOST("../org.eclipse.linuxtools.ctf.core.tests/traces/hello-lost"),

    /**
     * UST trace with lttng-ust-cyg-profile events (aka -finstrument-functions)
     *
     * <pre>
     * Trace Size: 236 KB
     * Tracer: lttng-ust 2.3
     * Event count: 4 977
     * Trace length: 10s
     * </pre>
     */
    CYG_PROFILE("../org.eclipse.linuxtools.ctf.core.tests/traces/cyg-profile/glxgears-cyg-profile"),

    /**
     * UST trace with lttng-ust-cyg-profile-fast events (no address in
     * func_exit)
     *
     * <pre>
     * Trace Size: 184 KB
     * Tracer: lttng-ust 2.3
     * Event count: 5 161
     * Trace length: 11s
     * </pre>
     */
    CYG_PROFILE_FAST("../org.eclipse.linuxtools.ctf.core.tests/traces/cyg-profile/glxgears-cyg-profile-fast"),

    /** Autogenerated Syntetic trace */
    SYNTHETIC_TRACE(LttngKernelTraceGenerator.getPath()),

    /** Trace with non-standard field sizes */
    FUNKY_TRACE("../org.eclipse.linuxtools.ctf.core.tests/traces/funky_trace"),

    /** Set of many traces, do not call getTrace */
    TRACE_EXPERIMENT("../org.eclipse.linuxtools.ctf.core.tests/traces/exp");

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
