/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.shared;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.nio.file.Paths;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub2;

/**
 * Generic TMF test traces
 *
 * @author Geneviève Bastien
 */
public enum TmfTestTrace {
    /** A test */
    A_TEST_10K("../../tmf/org.eclipse.tracecompass.tmf.core.tests/testfiles/A-Test-10K"),
    /** A second trace */
    A_TEST_10K2("../../tmf/org.eclipse.tracecompass.tmf.core.tests/testfiles/A-Test-10K-2"),
    /** A third trace */
    E_TEST_10K("../../tmf/org.eclipse.tracecompass.tmf.core.tests/testfiles/E-Test-10K"),
    /** A fourth trace */
    O_TEST_10K("../../tmf/org.eclipse.tracecompass.tmf.core.tests/testfiles/O-Test-10K"),
    /** And oh! a fifth trace */
    R_TEST_10K("../../tmf/org.eclipse.tracecompass.tmf.core.tests/testfiles/R-Test-10K");

    private final @NonNull String fPath;
    private final @NonNull String fFileName;

    private ITmfTrace fTrace = null;

    private TmfTestTrace(@NonNull String file) {
        fPath = file;
        fFileName = checkNotNull(Paths.get(fPath).getFileName().toString());
    }

    /**
     * Get the path of the trace
     *
     * @return The path of this trace
     */
    public @NonNull String getPath() {
        return fPath;
    }

    /**
     * Return the file name (or base name of the full path) of this trace.
     *
     * @return The trace's file name
     */
    public @NonNull String getFileName() {
        return fFileName;
    }

    /**
     * Return a ITmfTrace object of this test trace. It will be already
     * initTrace()'ed. This method will always return a new trace and dispose of
     * the old one.
     *
     * After being used by unit tests, traces must be properly disposed of by
     * calling the {@link TmfTestTrace#dispose()} method.
     *
     * @return A {@link ITmfTrace} reference to this trace
     */
    public @NonNull ITmfTrace getTrace() {
        if (fTrace != null) {
            fTrace.dispose();
        }
        try {
            ITmfTrace trace = new TmfTraceStub(fPath, ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false, null);
            fTrace = trace;
            return trace;
        } catch (TmfTraceException  e) {
            throw new IllegalStateException(e);
        }

    }

    /**
     * Return a ITmfTrace object that is of type {@link TmfTraceStub2}. It
     * will be already initTrace()'ed. But the trace will be deregistered from
     * signal managers and will need to be manually disposed of by the caller.
     *
     * @return a {@link ITmfTrace} reference to this trace
     */
    public ITmfTrace getTraceAsStub2() {
        ITmfTrace trace = null;
        try {
            trace = new TmfTraceStub2(fPath, ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false, null);
            TmfSignalManager.deregister(trace);

        } catch (TmfTraceException  e) {
            throw new IllegalStateException(e);
        }
        return trace;
    }

    /**
     * Dispose of the trace
     */
    public void dispose() {
        if (fTrace != null) {
            fTrace.dispose();
            fTrace = null;
        }
    }
}
