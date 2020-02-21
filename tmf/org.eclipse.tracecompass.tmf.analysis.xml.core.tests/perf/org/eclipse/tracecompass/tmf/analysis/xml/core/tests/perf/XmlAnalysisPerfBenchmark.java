/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.perf;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestUtils;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.junit.Test;

/**
 * Base class for XML analysis performance test
 *
 * @author Geneviève Bastien
 */
public abstract class XmlAnalysisPerfBenchmark {

    private static final String TEST_ID = "org.eclipse.tracecompass.xml.analysis#XML analysis#%s (%s)";

    /**
     * Get the trace to run the analysis on. The test class will dispose of the
     * trace at the end of each run of the analysis, so many instances of the
     * trace will be created during test. The callee shouldn't keep any
     * reference to it.
     *
     * @return The initialized trace
     * @throws TmfTraceException
     *             Exception thrown by trace
     */
    protected abstract @NonNull ITmfTrace getTrace() throws TmfTraceException;

    private final String fTestName;
    private final String fXmlFile;
    private final @NonNull String fAnalysisId;
    private final int fLoopCount;

    /**
     * Constructor
     *
     * @param testName
     *            A friently name for this test
     * @param absoluteFilePath
     *            The absolute file path of the XML file to use
     * @param moduleId
     *            The ID of the module to test
     * @param loopCount
     *            The number of iterations
     */
    public XmlAnalysisPerfBenchmark(String testName, String absoluteFilePath, @NonNull String moduleId, int loopCount) {
        fTestName = testName;
        fXmlFile = absoluteFilePath;
        fAnalysisId = moduleId;
        fLoopCount = loopCount;
    }

    /**
     * Run the performance test
     *
     * @throws TmfTraceException
     *             Exceptions thrown by trace
     * @throws TmfAnalysisException
     *             Exception thrown by setting the trace
     */
    @Test
    public void testAnalysisPerformance() throws TmfTraceException, TmfAnalysisException {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(String.format(TEST_ID, fAnalysisId, fTestName));
        perf.tagAsSummary(pm, "XML analysis: " + fAnalysisId + " " + fTestName, Dimension.CPU_TIME);

        for (int i = 0; i < fLoopCount; i++) {

            ITmfTrace trace = null;
            TmfAbstractAnalysisModule module = null;
            try {
                trace = getTrace();
                module = TmfXmlTestUtils.getModuleInFile(fXmlFile, fAnalysisId);
                module.setTrace(trace);
                pm.start();
                TmfTestHelper.executeAnalysis(module);
                pm.stop();

                // Print properties for the first run, for debug purposes
                if (i == 0) {
                    System.out.println(module.getProperties());
                }

                /*
                 * Delete the supplementary files, so that the next iteration
                 * rebuilds the state system.
                 */
                File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
                for (File file : suppDir.listFiles()) {
                    file.delete();
                }
            } finally {
                if (trace != null) {
                    trace.dispose();
                }
                if (module != null) {
                    module.dispose();
                }
            }

        }
        pm.commit();

    }

}
