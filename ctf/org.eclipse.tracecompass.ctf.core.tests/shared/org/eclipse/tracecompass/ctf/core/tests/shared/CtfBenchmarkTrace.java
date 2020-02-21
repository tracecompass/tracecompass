/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.shared;

import java.nio.file.Path;
import java.nio.file.Paths;

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
    ALL_OS_ANALYSES("os-events"),

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
    UST_QMLSCENE("qmlscene"),

    /**
     * A UST trace of the vlc program instrumented with -finstrument-functions.
     * This trace can be used to benchmark UST cyg-profile analyses
     *
     * <pre>
     * Trace Size: 350 MB
     * Tracer: lttng-ust 2.10
     * Event count: 12 504 554
     * Trace length: ~100 s
     * </pre>
     */
    UST_VLC("vlc/vlc_ust");

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
    public Path getTracePath() {
        return Paths.get(CtfCoreTestPlugin.getAbsoluteFilePath(TRACE_PATH + '/' + fTraceName).toOSString());
    }

}
