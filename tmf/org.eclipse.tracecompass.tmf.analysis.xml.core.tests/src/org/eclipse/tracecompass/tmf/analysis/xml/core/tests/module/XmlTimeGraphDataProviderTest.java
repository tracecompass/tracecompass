/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlAnalysisModuleSource;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.XmlDataProviderManager;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableList;

/**
 * Test the XML time graph data provider with a simple test
 *
 * @author Geneviève Bastien
 */
public class XmlTimeGraphDataProviderTest {

    private static final @NonNull String TEST_TRACE = "test_traces/testTrace4.xml";

    private static final @NonNull String ANALYSIS_ID = "xml.core.tests.simple.pattern";
    private static final @NonNull String TIME_GRAPH_VIEW_ID = "xml.core.tests.simple.pattern.timegraph";
    private static final @NonNull String TIME_GRAPH_VIEW_ID2 = "xml.core.tests.simple.pattern.timegraph2";
    private static final @NonNull IProgressMonitor MONITOR = new NullProgressMonitor();

    /**
     * Load the XML files for the current test
     */
    @Before
    public void setUp() {
        XmlUtils.addXmlFile(TmfXmlTestFiles.VALID_PATTERN_SIMPLE_FILE.getFile());
        XmlUtils.addXmlFile(TmfXmlTestFiles.DATA_PROVIDER_SIMPLE_FILE.getFile());

        XmlAnalysisModuleSource.notifyModuleChange();
    }

    /**
     * Clean
     */
    public void cleanUp() {
        XmlUtils.deleteFiles(ImmutableList.of(
                TmfXmlTestFiles.VALID_PATTERN_SIMPLE_FILE.getFile().getName(),
                TmfXmlTestFiles.DATA_PROVIDER_SIMPLE_FILE.getFile().getName()));
        XmlAnalysisModuleSource.notifyModuleChange();
    }

    private ITmfTrace getTrace() {
        // Initialize the trace and module
        ITmfTrace trace = XmlUtilsTest.initializeTrace(TEST_TRACE);
        TmfTraceOpenedSignal signal = new TmfTraceOpenedSignal(this, trace, null);
        ((TmfTrace) trace).traceOpened(signal);
        // The data provider manager uses opened traces from the manager
        TmfTraceManager.getInstance().traceOpened(signal);
        return trace;
    }

    private static void runModule(ITmfTrace trace) {
        IAnalysisModule module = trace.getAnalysisModule(ANALYSIS_ID);
        assertNotNull(module);
        module.schedule();
        assertTrue(module.waitForCompletion());
    }

    /**
     * Test getting the XML data provider for one trace, with an analysis that
     * applies to a trace
     *
     * @throws IOException
     *             Exception thrown by analyses
     */
    @Test
    public void testTwoLevels() throws IOException {
        ITmfTrace trace = getTrace();
        assertNotNull(trace);
        try {
            runModule(trace);
            // Get the view element from the file
            Element viewElement = TmfXmlUtils.getElementInFile(TmfXmlTestFiles.DATA_PROVIDER_SIMPLE_FILE.getPath().toOSString(), TmfXmlStrings.TIME_GRAPH_VIEW, TIME_GRAPH_VIEW_ID);
            assertNotNull(viewElement);
            ITimeGraphDataProvider<@NonNull TimeGraphEntryModel> timeGraphProvider = XmlDataProviderManager.getInstance().getTimeGraphProvider(trace, viewElement);

            assertNotNull(timeGraphProvider);

            List<String> expectedStrings = Files.readAllLines(Paths.get("test_traces/simple_dataprovider/expectedTimeGraphTree"));
            Map<Long, String> tree = assertAndGetTree(timeGraphProvider, trace, expectedStrings);

            expectedStrings = Files.readAllLines(Paths.get("test_traces/simple_dataprovider/expectedTimeGraphRows"));
            assertRows(timeGraphProvider, tree, expectedStrings);

        } finally {
            trace.dispose();
            TmfTraceManager.getInstance().traceClosed(new TmfTraceClosedSignal(this, trace));
        }

    }

    private static void assertRows(ITimeGraphDataProvider<@NonNull TimeGraphEntryModel> provider, Map<Long, String> tree, List<String> expectedStrings) {
        TmfModelResponse<List<ITimeGraphRowModel>> rowResponse = provider.fetchRowModel(new SelectionTimeQueryFilter(1, 20, 20, tree.keySet()), null);
        assertNotNull(rowResponse);
        assertEquals(ITmfResponse.Status.COMPLETED, rowResponse.getStatus());
        List<ITimeGraphRowModel> rowModel = rowResponse.getModel();
        assertNotNull(rowModel);
        // ensure row order
        rowModel.sort(Comparator.comparingLong(ITimeGraphRowModel::getEntryID));

        assertEquals(expectedStrings.size(), rowModel.size());
        for (int i = 0; i < expectedStrings.size(); i++) {
            String expectedString = expectedStrings.get(i);
            String[] split = expectedString.split(":");
            ITimeGraphRowModel row = rowModel.get(i);

            assertEquals(split[0], tree.get(row.getEntryID()));

            assertEqualsStates(split[0], split[1], row.getStates());
        }

    }

    private static void assertEqualsStates(String name, String string, @NonNull List<@NonNull ITimeGraphState> states) {
        String[] stringStates = string.split(",");
        for (int i = 0; i < stringStates.length / 4; i++) {
            ITimeGraphState state = states.get(i);
            assertNotNull("State " + i + " for " + name, state);
            assertEquals("Start time of state " + i + " for " + name, Long.parseLong(stringStates[i * 4]), state.getStartTime());
            assertEquals("Duration of state " + i + " for " + name, Long.parseLong(stringStates[i * 4 + 1]), state.getDuration());
            assertEquals("Value of state " + i + " for " + name, Long.parseLong(stringStates[i * 4 + 2]), state.getValue());
            assertEquals("Label of state " + i + " for " + name, stringStates[i * 4 + 3], String.valueOf(state.getLabel()));
        }
        assertEquals("Expected number of states", stringStates.length / 4, states.size());
    }

    private static Map<Long, String> assertAndGetTree(ITimeGraphDataProvider<@NonNull TimeGraphEntryModel> timeGraphProvider, ITmfTrace trace, List<String> expectedStrings) {
        TmfModelResponse<@NonNull List<@NonNull TimeGraphEntryModel>> treeResponse = timeGraphProvider.fetchTree(new TimeQueryFilter(0, Long.MAX_VALUE, 2), MONITOR);
        assertNotNull(treeResponse);
        assertEquals(ITmfResponse.Status.COMPLETED, treeResponse.getStatus());
        List<TimeGraphEntryModel> treeModel = treeResponse.getModel();
        assertNotNull(treeModel);

        Collections.sort(treeModel, Comparator.comparingLong(TimeGraphEntryModel::getId));
        Map<Long, String> map = new HashMap<>();
        for (int i = 0; i < expectedStrings.size(); i++) {
            String expectedString = expectedStrings.get(i);
            assertTrue("actual entry present at " + i + ": " + expectedString, treeModel.size() > i);
            String[] split = expectedString.split(",");
            TimeGraphEntryModel xmlTgEntry = treeModel.get(i);

            assertEquals("Checking entry name at " + i, split[0], xmlTgEntry.getName());
            assertEquals("Checking entry start time at " + i, Long.parseLong(split[1]), xmlTgEntry.getStartTime());
            assertEquals("Checking entry end time at " + i, Long.parseLong(split[2]), xmlTgEntry.getEndTime());
            // Check the parent
            long parentId = xmlTgEntry.getParentId();
            if (parentId < 0) {
                assertEquals("Checking empty parent at " + i, split[3], "null");
            } else {
                String parentName = map.get(parentId);
                assertEquals("Checking parent at " + i, split[3], parentName);
            }
            map.put(xmlTgEntry.getId(), xmlTgEntry.getName());
        }
        assertEquals("Extra actual entries", expectedStrings.size(), treeModel.size());

        return map;
    }

    /**
     * Test getting the XML data provider for one trace, with an analysis that
     * applies to a trace
     *
     * @throws IOException
     *             Exception thrown by analyses
     */
    @Test
    public void testNoParentDisplay() throws IOException {
        ITmfTrace trace = getTrace();
        assertNotNull(trace);
        try {
            runModule(trace);
            // Get the view element from the file
            Element viewElement = TmfXmlUtils.getElementInFile(TmfXmlTestFiles.DATA_PROVIDER_SIMPLE_FILE.getPath().toOSString(), TmfXmlStrings.TIME_GRAPH_VIEW, TIME_GRAPH_VIEW_ID2);
            assertNotNull(viewElement);
            ITimeGraphDataProvider<@NonNull TimeGraphEntryModel> timeGraphProvider = XmlDataProviderManager.getInstance().getTimeGraphProvider(trace, viewElement);
            assertNotNull(timeGraphProvider);

            List<String> expectedStrings = Files.readAllLines(Paths.get("test_traces/simple_dataprovider/expectedTimeGraphTree"));
            Map<Long, String> tree = assertAndGetTree(timeGraphProvider, trace, expectedStrings);

            expectedStrings = Files.readAllLines(Paths.get("test_traces/simple_dataprovider/expectedTimeGraphRows"));
            // The CPU entry does not have entries
            expectedStrings.remove(0);
            assertRows(timeGraphProvider, tree, expectedStrings);

        } finally {
            trace.dispose();
            TmfTraceManager.getInstance().traceClosed(new TmfTraceClosedSignal(this, trace));
        }

    }

}
