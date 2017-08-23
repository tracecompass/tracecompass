/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui.swtbot.tests;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.analysis.counters.core.CounterAnalysis;
import org.eclipse.tracecompass.analysis.counters.ui.CounterView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.perf.views.ViewsResponseTest;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.views.TracingPerspectiveFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the reactivity of the {@link CounterView}.
 *
 * @author Loic Prieur-Drevon
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CountersViewBenchmark extends ViewsResponseTest {

    private static final String TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";

    @Override
    protected void beforeRunningTest(ITmfTrace trace) {
        IAnalysisModule analysis = trace.getAnalysisModule(CounterAnalysis.ID);
        assertNotNull("CounterAnalysis cannot run on trace " + trace.getName(), analysis);
        analysis.schedule();
        analysis.waitForCompletion();
    }

    @Override
    protected void prepareWorkspace() {
        /* Switch to Tracing perspective */
        SWTBotUtils.switchToPerspective(TracingPerspectiveFactory.ID);
    }

    /**
     * Test with the Kernel VM trace
     *
     * @throws IOException
     *             if an error occurs during the path to URL conversion
     */
    @Test
    public void testKernelVM() throws IOException {
        runTestWithTrace(FileLocator.toFileURL(CtfTestTrace.KERNEL_VM.getTraceURL()).getPath(),
                TRACE_TYPE, Collections.singleton(CounterView.ID));
    }

    @Override
    public void prepareView(SWTBotView view) {
        SWTBotTree tree = view.bot().tree();
        for (SWTBotTreeItem item : tree.getAllItems()) {
            item.check();
        }
    }

}
