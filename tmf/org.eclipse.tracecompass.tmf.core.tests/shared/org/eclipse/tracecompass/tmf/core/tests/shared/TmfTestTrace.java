/*******************************************************************************
 * Copyright (c) 2013, 2017 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.shared;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
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
    A_TEST_10K("A-Test-10K"),
    /** A second trace */
    A_TEST_10K2("A-Test-10K-2"),
    /** A third trace */
    E_TEST_10K("E-Test-10K"),
    /** A fourth trace */
    O_TEST_10K("O-Test-10K"),
    /** And oh! a fifth trace */
    R_TEST_10K("R-Test-10K"),
    /** Syslog trace */
    SYSLOG_1("syslog1"),
    /** Syslog trace */
    SYSLOG_2("syslog2"),
    /** Syslog trace */
    SYSLOG_3("syslog3"),
    /** Syslog trace */
    SYSLOG_4("syslog4"),
    /** Syslog trace */
    SYSLOG_5("syslog5"),
    /** Syslog trace */
    SYSLOG_6("syslog6");


    private final @NonNull String fPath;
    private final String fDirectory = "testfiles";
    private ITmfTrace fTrace = null;

    private TmfTestTrace(@NonNull String file) {
        fPath = file;
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
     * Get the full path of the trace
     *
     * @return The full path of the trace
     */
    public String getFullPath() {
        URL resource = TmfCoreTestPlugin.getDefault().getBundle().getResource(fDirectory + IPath.SEPARATOR + fPath);
        try {
            return FileLocator.toFileURL(resource).toURI().getPath();
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException(e);
        }
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
            ITmfTrace trace = new TmfTraceStub(getFullPath(), ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false, null);
            fTrace = trace;
            return trace;
        } catch (TmfTraceException e) {
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
        final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(fDirectory + File.separator + fPath), null);
        try {
            File test = new File(FileLocator.toFileURL(location).toURI());
            trace = new TmfTraceStub2(test.toURI().getPath(), ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false, null);
            TmfSignalManager.deregister(trace);

        } catch (URISyntaxException | IOException | TmfTraceException  e) {
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
