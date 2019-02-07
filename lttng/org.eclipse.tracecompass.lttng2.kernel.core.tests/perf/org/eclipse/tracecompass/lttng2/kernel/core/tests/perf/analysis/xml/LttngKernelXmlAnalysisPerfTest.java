/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis.xml;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfBenchmarkTrace;
import org.eclipse.tracecompass.lttng2.kernel.core.tests.Activator;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.perf.XmlAnalysisPerfTest;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Performance test of some XML analyses for kernel traces
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class LttngKernelXmlAnalysisPerfTest extends XmlAnalysisPerfTest {

    /**
     * @return The arrays of parameters
     * @throws IOException
     *             Exception thrown by reading files
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() throws IOException {
        return Arrays.asList(new Object[][] {
                { "syscall with many threads", 25, "syscallSegments.xml", "xml.syscall.cpu.time", FileUtils.toFile(FileLocator.toFileURL(CtfTestTrace.MANY_THREADS.getTraceURL())).getAbsolutePath() },
                { "syscall with os_events", 10, "syscallSegments.xml", "xml.syscall.cpu.time", CtfBenchmarkTrace.ALL_OS_ANALYSES.getTracePath().toString() },
        });
    }

    private final String fFileTracePath;

    /**
     * Constructor
     *
     * @param name
     *            Name of the test
     * @param loopCount
     *            The number of iterations to do
     * @param xmlFileName
     *            File name
     * @param xmlAnalysisName
     *            The name of the XML analysis
     * @param testTrace
     *            The CTF test trace to use
     */
    public LttngKernelXmlAnalysisPerfTest(String name, int loopCount, String xmlFileName, @NonNull String xmlAnalysisName, String testTrace) {
        super(name, Activator.getAbsoluteFilePath("testfiles/xml/" + xmlFileName).toOSString(), xmlAnalysisName, loopCount);
        fFileTracePath = testTrace;
    }

    @Override
    protected ITmfTrace getTrace() throws TmfTraceException {
        LttngKernelTrace trace = new LttngKernelTrace();
        try {
            trace.initTrace(null, fFileTracePath, CtfTmfEvent.class);
            return trace;
        } catch (TmfTraceException e) {
            trace.dispose();
            throw e;
        }
    }

}
