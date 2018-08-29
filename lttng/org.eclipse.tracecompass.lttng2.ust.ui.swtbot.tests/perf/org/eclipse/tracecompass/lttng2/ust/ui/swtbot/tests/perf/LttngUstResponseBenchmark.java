/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.ui.swtbot.tests.perf;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.profiling.ui.views.flamechart.FlameChartView;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfBenchmarkTrace;
import org.eclipse.tracecompass.internal.lttng2.ust.core.callstack.LttngUstCallStackAnalysis;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.perf.views.ViewsResponseTest;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.views.TracingPerspectiveFactory;
import org.junit.Test;

/**
 * Test the responsiveness of some UST-specific views. Ideally, when running
 * this test, JUL logging should be enabled using a logger.properties file.
 * LTTng JUL handler is advised since it works better with multi-threaded
 * applications than other log handlers
 *
 * @author Geneviève Bastien
 */
public class LttngUstResponseBenchmark extends ViewsResponseTest {

    private static final @NonNull String FLAMECHART_VIEW_ID = FlameChartView.ID;
    private static final String TRACE_TYPE = "org.eclipse.linuxtools.lttng2.ust.tracetype";

    @Override
    protected void prepareWorkspace() {
        /* Switch to kernel perspective */
        SWTBotUtils.switchToPerspective(TracingPerspectiveFactory.ID);
    }

    @Override
    protected void beforeRunningTest(ITmfTrace trace) {
        List<IAnalysisModule> modules = new ArrayList<>(3);
        modules.add(trace.getAnalysisModule(LttngUstCallStackAnalysis.ID));
        for (IAnalysisModule module : modules) {
            if (module != null) {
                module.schedule();
            }
        }
        for (IAnalysisModule module : modules) {
            if (module != null) {
                assertTrue(module.waitForCompletion());
            }
        }
    }

    /**
     * Test with the cyg-profile trace
     *
     * @throws SecurityException
     *             If a security manager is present and any the wrong class is
     *             loaded or the class loader is not the same as its ancestor's
     *             loader.
     * @throws IllegalArgumentException
     *             the object is not the correct class type
     * @throws IOException
     *             Exceptions with the trace file
     *
     */
    @Test
    public void testWithCygProfile() throws SecurityException, IllegalArgumentException, IOException {
        runTestWithTrace(FileLocator.toFileURL(CtfTestTrace.CYG_PROFILE.getTraceURL()).getPath(), TRACE_TYPE, Collections.singleton(FLAMECHART_VIEW_ID));
    }

    /**
     * Test with the qmlscene benchmark trace
     *
     * @throws SecurityException
     *             If a security manager is present and any the wrong class is
     *             loaded or the class loader is not the same as its ancestor's
     *             loader.
     * @throws IllegalArgumentException
     *             the object is not the correct class type
     */
    @Test
    public void testWithQmlScene() throws SecurityException, IllegalArgumentException {
        runTestWithTrace(CtfBenchmarkTrace.UST_QMLSCENE.getTracePath().toString(), TRACE_TYPE, Collections.singleton(FLAMECHART_VIEW_ID));
    }

}
