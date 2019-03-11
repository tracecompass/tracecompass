/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.cpuusage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageEntryModel;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.KernelCpuUsageAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.Activator;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.TmfXmlKernelTraceStub;
import org.eclipse.tracecompass.analysis.os.linux.core.tid.TidAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage.CpuUsageDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectedCpuQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link CpuUsageDataProvider} class
 *
 * @author Geneviève Bastien
 */
public class CpuUsageDataProviderTest {

    private static final String CPU_USAGE_FILE = "testfiles/cpu_analysis.xml";

    private IKernelTrace fTrace;

    private CpuUsageDataProvider fDataProvider;

    private static void deleteSuppFiles(@NonNull ITmfTrace trace) {
        /* Remove supplementary files */
        File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
        for (File file : suppDir.listFiles()) {
            file.delete();
        }
    }

    /**
     * Setup the trace for the tests
     */
    @Before
    public void setUp() {
        IKernelTrace trace = new TmfXmlKernelTraceStub();
        IPath filePath = Activator.getAbsoluteFilePath(CPU_USAGE_FILE);
        IStatus status = trace.validate(null, filePath.toOSString());
        if (!status.isOK()) {
            fail(status.getException().getMessage());
        }
        try {
            trace.initTrace(null, filePath.toOSString(), TmfEvent.class);
        } catch (TmfTraceException e) {
            fail(e.getMessage());
        }
        deleteSuppFiles(trace);
        ((TmfTrace) trace).traceOpened(new TmfTraceOpenedSignal(this, trace, null));
        /*
         * FIXME: Make sure this analysis is finished before running the CPU analysis.
         * This block can be removed once analysis dependency and request precedence is
         * implemented
         */
        IAnalysisModule module = null;
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, TidAnalysisModule.class)) {
            module = mod;
        }
        assertNotNull(module);
        module.schedule();
        module.waitForCompletion();
        /* End of the FIXME block */

        module = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelCpuUsageAnalysis.class, KernelCpuUsageAnalysis.ID);
        assertNotNull(module);
        module.schedule();
        module.waitForCompletion();
        fDataProvider = CpuUsageDataProvider.create(trace);
        assertNotNull(fDataProvider);
        fTrace = trace;
    }

    /**
     * Clean up
     */
    @After
    public void tearDown() {
        IKernelTrace trace = fTrace;
        if (trace != null) {
            deleteSuppFiles(trace);
            trace.dispose();
        }
    }

    /**
     * Test the
     * {@link CpuUsageDataProvider#fetchTree(Map, IProgressMonitor)}
     * method.
     * <p>

     */
    @Test
    public void testTree() {
        CpuUsageDataProvider dataProvider = fDataProvider;
        IProgressMonitor monitor = new NullProgressMonitor();

        /* This range should query the total range */
        TimeQueryFilter filter = new SelectedCpuQueryFilter(0L, 30L, 2, Collections.emptyList(), Collections.emptySet());
        @NonNull Map<@NonNull String, @NonNull Object> parameters = new HashMap<>();
        parameters.put(DataProviderParameterUtils.TIME_REQUESTED_KEY, getTimeRequested(filter));
        parameters.put(DataProviderParameterUtils.SELECTED_ITEMS_KEY, Collections.emptyList());
        parameters.put("cpus", Collections.emptySet());
        TmfModelResponse<@NonNull TmfTreeModel<@NonNull CpuUsageEntryModel>> response = dataProvider.fetchTree(parameters, monitor);

        assertTrue(response.getStatus() == Status.COMPLETED);

        TmfTreeModel<@NonNull CpuUsageEntryModel> model = response.getModel();
        assertNotNull(model);
        /* Maps a tid to the total time */
        Map<Integer, Long> expected = new HashMap<>();
        expected.put(1, 5L);
        expected.put(2, 19L);
        expected.put(3, 11L);
        expected.put(4, 13L);
        expected.put(-2, 48L);
        compareModel(expected, model.getEntries());

        /* Verify a range when a process runs at the start */
        filter = new SelectedCpuQueryFilter(22L, 25L, 2, Collections.emptyList(), Collections.emptySet());
        parameters.put(DataProviderParameterUtils.TIME_REQUESTED_KEY, getTimeRequested(filter));
        response = dataProvider.fetchTree(parameters, monitor);
        assertTrue(response.getStatus() == Status.COMPLETED);

        model = response.getModel();
        assertNotNull(model);
        /* Maps a tid to the total time */
        expected.clear();
        expected.put(3, 3L);
        expected.put(4, 3L);
        expected.put(-2, 6L);
        compareModel(expected, model.getEntries());

        /* Verify a range when a process runs at the end */
        filter = new SelectedCpuQueryFilter(1L, 4L, 2, Collections.emptyList(), Collections.emptySet());
        parameters.put(DataProviderParameterUtils.TIME_REQUESTED_KEY, getTimeRequested(filter));
        response = dataProvider.fetchTree(parameters, monitor);
        assertTrue(response.getStatus() == Status.COMPLETED);

        model = response.getModel();
        assertNotNull(model);
        /* Maps a tid to the total time */
        expected.clear();
        expected.put(2, 3L);
        expected.put(3, 1L);
        expected.put(4, 2L);
        expected.put(-2, 6L);
        compareModel(expected, model.getEntries());

        /* Verify a range when a process runs at start and at the end */
        filter = new SelectedCpuQueryFilter(4L, 13L, 2, Collections.emptyList(), Collections.emptySet());
        parameters.put(DataProviderParameterUtils.TIME_REQUESTED_KEY, getTimeRequested(filter));
        response = dataProvider.fetchTree(parameters, monitor);
        assertTrue(response.getStatus() == Status.COMPLETED);

        model = response.getModel();
        assertNotNull(model);
        /* Maps a tid to the total time */
        expected.clear();
        expected.put(2, 9L);
        expected.put(3, 5L);
        expected.put(4, 4L);
        expected.put(-2, 18L);
        compareModel(expected, model.getEntries());

    }

    private static void compareModel(Map<Integer, Long> expected, List<CpuUsageEntryModel> model) {
        assertEquals("Size of model entries", expected.size(), model.size());
        Map<Integer, Long> actual = new HashMap<>();
        model.forEach(entry -> actual.put(entry.getTid(), entry.getTime()));
        assertEquals("model entries", expected, actual);
    }

    private static @NonNull List<Long> getTimeRequested(TimeQueryFilter filter) {
        List<Long> times = new ArrayList<>();
        for (long time : filter.getTimesRequested()) {
            times.add(time);
        }
        return times;
    }

}
