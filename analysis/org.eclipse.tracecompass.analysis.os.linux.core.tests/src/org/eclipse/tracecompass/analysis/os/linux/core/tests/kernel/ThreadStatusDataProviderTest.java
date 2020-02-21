/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.Activator;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.TmfXmlKernelTraceStub;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadEntryModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadStatusDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.junit.Test;

/**
 * {@link ThreadStatusDataProvider} test
 *
 * @author Loic Prieur-Drevon
 */
public class ThreadStatusDataProviderTest {

    private static final String KERNEL_ANALYSIS = "testfiles/kernel_analysis/lttng_kernel_analysis.xml";

    /**
     * Test the {@link ThreadStatusDataProvider} for the XML kernel test trace
     *
     * @throws TmfTraceException
     *             If we couldn't open the trace
     * @throws IOException
     *             if an I/O error occurs reading from the expected value file or a
     *             malformed or unmappable byte sequence is read
     */
    @Test
    public void testThreadStatusDataProvider() throws TmfTraceException, IOException {
        TmfXmlKernelTraceStub trace = new TmfXmlKernelTraceStub();
        try {
            IPath filePath = Activator.getAbsoluteFilePath(KERNEL_ANALYSIS);
            trace.initTrace(null, filePath.toOSString(), TmfEvent.class);
            trace.traceOpened(new TmfTraceOpenedSignal(this, trace, null));

            KernelAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelAnalysisModule.class, KernelAnalysisModule.ID);
            assertNotNull(module);
            assertTrue(module.schedule().isOK());
            assertTrue(module.waitForCompletion());

            ThreadStatusDataProvider provider = new ThreadStatusDataProvider(trace, module);

            Map<Long, String> idsToNames = assertAndGetTree(provider);

            assertRows(provider, idsToNames);

            assertArrows(provider, idsToNames);
        } finally {
            trace.dispose();
        }
    }

    private static Map<Long, String> assertAndGetTree(ThreadStatusDataProvider provider) throws IOException {
        TmfModelResponse<TmfTreeModel<@NonNull TimeGraphEntryModel>> treeResponse = provider.fetchTree(FetchParametersUtils.timeQueryToMap(new TimeQueryFilter(0, Long.MAX_VALUE, 2)), null);
        assertNotNull(treeResponse);
        assertEquals(ITmfResponse.Status.COMPLETED, treeResponse.getStatus());
        TmfTreeModel<@NonNull TimeGraphEntryModel> treeModel = treeResponse.getModel();
        assertNotNull(treeModel);
        List<@NonNull TimeGraphEntryModel> treeEntries = treeModel.getEntries();

        List<String> expectedStrings = Files.readAllLines(Paths.get("testfiles/kernel_analysis/expectedThreadStatusTree"));
        assertEquals(expectedStrings.size(), treeEntries.size());
        for (int i = 0; i < expectedStrings.size(); i++) {
            String expectedString = expectedStrings.get(i);
            String[] split = expectedString.split(",");
            TimeGraphEntryModel threadEntry = treeEntries.get(i);

            // Assert common fields
            assertEquals(split[0], threadEntry.getName());
            assertEquals(Long.parseLong(split[1]), threadEntry.getStartTime());
            assertEquals(Long.parseLong(split[2]), threadEntry.getEndTime());
            if (threadEntry instanceof ThreadEntryModel) {
                // Verify the thread entry fields
                ThreadEntryModel threadEntryModel = (ThreadEntryModel) threadEntry;
                assertEquals(Integer.parseInt(split[3]), threadEntryModel.getThreadId());
                assertEquals(Integer.parseInt(split[4]), threadEntryModel.getProcessId());
                assertEquals(Integer.parseInt(split[5]), threadEntryModel.getParentThreadId());
            } else {
                // Make sure there is no extra expected fields
                assertEquals(3, split.length);
            }
        }
        Map<Long, String> map = new HashMap<>();
        for (TimeGraphEntryModel threadModel : treeEntries) {
            map.put(threadModel.getId(), threadModel.getName());
        }
        return map;
    }

    private static void assertRows(ThreadStatusDataProvider provider, Map<Long, String> idsToNames) throws IOException {
        TmfModelResponse<TimeGraphModel> rowResponse = provider.fetchRowModel(FetchParametersUtils.selectionTimeQueryToMap(new SelectionTimeQueryFilter(1, 80, 80, idsToNames.keySet())), null);
        assertNotNull(rowResponse);
        assertEquals(ITmfResponse.Status.COMPLETED, rowResponse.getStatus());
        TimeGraphModel rowModel = rowResponse.getModel();
        assertNotNull(rowModel);
        List<@NonNull ITimeGraphRowModel> rows = rowModel.getRows();
        // ensure row order
        rows.sort(Comparator.comparingLong(ITimeGraphRowModel::getEntryID));

        List<String> expectedStrings = Files.readAllLines(Paths.get("testfiles/kernel_analysis/expectedThreadStatusRows"));
        assertEquals(expectedStrings.size(), rows.size());
        for (int i = 0; i < expectedStrings.size(); i++) {
            String expectedString = expectedStrings.get(i);
            String[] split = expectedString.split(":");
            ITimeGraphRowModel row = rows.get(i);

            assertEquals(split[0], idsToNames.get(row.getEntryID()));

            assertEqualsStates(split[1], row.getStates(), split[0]);
        }
    }

    private static void assertEqualsStates(String string, @NonNull List<@NonNull ITimeGraphState> states, String element) {
        String[] stringStates = string.split(",");
        for (int i = 0; i < stringStates.length / 4; i++) {
            ITimeGraphState state = states.get(i);
            assertEquals(element + ": start time at position " + i, Long.parseLong(stringStates[i * 4]), state.getStartTime());
            assertEquals(element + ": duration at position " + i, Long.parseLong(stringStates[i * 4 + 1]), state.getDuration());
            OutputElementStyle style = state.getStyle();
            if (style == null) {
                // Expected a value of Long
                try {
                    assertEquals(element + ": value at position " + i, Long.parseLong(stringStates[i * 4 + 2]), state.getValue());
                } catch (NumberFormatException e) {
                    fail(element + ": value at position " + i + ": did not expect a null style");
                }
            } else {
                assertEquals(element + ": value at position " + i, stringStates[i * 4 + 2], style.getParentKey());
            }
            assertEquals(element + ": label at position " + i, stringStates[i * 4 + 3], String.valueOf(state.getLabel()));
        }
    }

    private static void assertArrows(ThreadStatusDataProvider provider, Map<Long, String> idsToNames) throws IOException {
        TmfModelResponse<List<ITimeGraphArrow>> arrowResponse = provider.fetchArrows(FetchParametersUtils.timeQueryToMap(new TimeQueryFilter(1, 80, 80)), null);
        assertNotNull(arrowResponse);
        assertEquals(ITmfResponse.Status.COMPLETED, arrowResponse.getStatus());
        List<ITimeGraphArrow> arrows = arrowResponse.getModel();
        assertNotNull(arrows);

        List<String> expectedStrings = Files.readAllLines(Paths.get("testfiles/kernel_analysis/expectedThreadStatusArrows"));
        assertEquals(expectedStrings.size(), arrows.size());
        for (int i = 0; i < expectedStrings.size(); i++) {
            String expectedString = expectedStrings.get(i);
            String[] split = expectedString.split(",");
            ITimeGraphArrow arrow = arrows.get(i);

            assertEquals(split[0], idsToNames.get(arrow.getSourceId()));
            assertEquals(split[1], idsToNames.get(arrow.getDestinationId()));
            assertEquals(Long.parseLong(split[2]), arrow.getStartTime());
            assertEquals(Long.parseLong(split[3]), arrow.getDuration());
        }
    }

}
