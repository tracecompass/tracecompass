/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.shared;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.tests.CtfCoreTestPlugin;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;

/**
 * List of test traces larger than the ones provided by {@link CtfTestTrace}, to
 * be used for benchmarks that require CTF traces.
 *
 * @author Geneviève Bastien
 */
public enum CtfBenchmarkTrace {

    /**
     * Kernel trace that contains all the events necessary to run all the
     * analyses available as of Trace Compass 2.3
     *
     * Trivia: This trace traced the startup of Trace Compass from the Eclipse
     * IDE
     *
     * <pre>
     * Trace Size: 149 MB
     * Tracer: lttng-modules 2.9
     * Kernel version: 4.9.6-1
     * Event count: 5 065 710
     * Trace length: ~31 s
     * </pre>
     */
    ALL_OS_ANALYSES("/os-events"),

    /**
     * A UST trace of the qmlscene program instrumented with -finstrument-functions.
     * This trace can be used to benchmark UST cyg-profile analyses
     *
     * <pre>
     * Trace Size: 20 MB
     * Tracer: lttng-ust 2.8
     * Event count: 465 662
     * Trace length: ~10 s
     * </pre>
     */
    UST_QMLSCENE("/qmlscene");

    private static final @NonNull String TRACE_PATH = "traces";
    private final String fTraceName;

    private CtfBenchmarkTrace(String traceName) {
        fTraceName = traceName;
    }

    /**
     * Get the URL for this trace
     *
     * @return The path for this trace
     */
    public IPath getTracePath() {
        IPath url = CtfCoreTestPlugin.getAbsolutePath(new Path(TRACE_PATH + fTraceName));
        if (url == null) {
            /* Project configuration problem? */
            throw new IllegalStateException("Test trace not found");
        }
        return url;
    }

}
