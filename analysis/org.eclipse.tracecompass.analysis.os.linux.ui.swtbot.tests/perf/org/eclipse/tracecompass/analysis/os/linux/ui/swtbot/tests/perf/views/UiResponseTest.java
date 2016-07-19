/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.swtbot.tests.perf.views;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.KernelCpuUsageAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.InputOutputAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.ControlFlowView;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.cpuusage.CpuUsageView;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.io.diskioactivity.DiskIOActivityView;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.kernelmemoryusage.KernelMemoryUsageView;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.ResourcesView;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.perf.views.ViewsResponseTest;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableMap;

/**
 * Test the responsiveness of Control Flow View and Resources View for different
 * traces and scenarios. Ideally, when running this test, JUL logging should be
 * enabled using a logger.properties file. LTTng JUL handler is advised since it
 * works better with multi-threaded applications than other log handlers
 *
 * @author Geneviève Bastien
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class UiResponseTest extends ViewsResponseTest {

    /**
     * An enumeration of the views available to test
     */
    protected enum OsLinuxViews {
        /**
         * The Control Flow View
         */
        CONTROL_FLOW,
        /**
         * The Resources View
         */
        RESOURCES,
        /**
         * The CPU Usage View
         */
        CPU_USAGE,
        /**
         * The Disk IO Activity View
         */
        DISK_IO_ACTIVITY,
        /**
         * The Kernel Memory Usage View
         */
        KERNEL_MEMORY_USAGE
    }

    private static final @NonNull Map<OsLinuxViews, String> VIEW_IDS = ImmutableMap.of(OsLinuxViews.CONTROL_FLOW, ControlFlowView.ID,
            OsLinuxViews.RESOURCES, ResourcesView.ID,
            OsLinuxViews.CPU_USAGE, CpuUsageView.ID,
            OsLinuxViews.DISK_IO_ACTIVITY, DiskIOActivityView.ID,
            OsLinuxViews.KERNEL_MEMORY_USAGE, KernelMemoryUsageView.ID);

    @Override
    protected void beforeRunningTest(ITmfTrace trace) {
        List<IAnalysisModule> modules = new ArrayList<>(3);
        modules.add(trace.getAnalysisModule(KernelCpuUsageAnalysis.ID));
        modules.add(trace.getAnalysisModule(KernelAnalysisModule.ID));
        modules.add(trace.getAnalysisModule(InputOutputAnalysisModule.ID));
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
     * Run this swtbot with the trace specified at the specified path. The trace
     * will be navigate for each view ID separately, then, after renaming the
     * trace, with all the views opened. After this test, all views will be
     * closed.
     *
     * @param tracePath
     *            The full path of the trace to open
     * @param traceType
     *            The trace type of the trace to open
     * @param views
     *            The os linux specific views to test
     */
    protected void runTestWithTrace(String tracePath, String traceType, EnumSet<OsLinuxViews> views) {
        List<String> viewIDs = new ArrayList<>();
        for (OsLinuxViews view : views) {
            viewIDs.add(VIEW_IDS.get(view));
        }
        runTestWithTrace(tracePath, traceType, viewIDs);
    }

}
