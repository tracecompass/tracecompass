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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlAnalysisModuleSource;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlDataProviderManager;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ISeriesModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
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
 * Test the XML XY data provider
 *
 * @author Geneviève Bastien
 */
public class XmlXyDataProviderTest {

    private static final @NonNull String TEST_TRACE = "test_traces/testTrace4.xml";

    private static final @NonNull String ANALYSIS_ID = "xml.core.tests.simple.pattern";
    private static final @NonNull String XY_VIEW_ID = "xml.core.tests.simple.pattern.xy";
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
        IAnalysisModule module = trace.getAnalysisModule(ANALYSIS_ID);
        assertNotNull(module);
        module.schedule();
        assertTrue(module.waitForCompletion());
        return trace;
    }

    /**
     * Test getting the XML data provider for one trace, with an analysis that
     * applies to a trace
     *
     * @throws IOException
     *             Exception thrown by analyses
     */
    @Test
    public void testXYDataProvider() throws IOException {
        ITmfTrace trace = getTrace();
        assertNotNull(trace);
        try {
            // Get the view element from the file
            Element viewElement = TmfXmlUtils.getElementInFile(TmfXmlTestFiles.DATA_PROVIDER_SIMPLE_FILE.getPath().toOSString(), TmfXmlStrings.XY_VIEW, XY_VIEW_ID);
            assertNotNull(viewElement);
            ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> xyProvider = XmlDataProviderManager.getInstance().getXyProvider(trace, viewElement);
            assertNotNull(xyProvider);

            List<String> expectedStrings = Files.readAllLines(Paths.get("test_traces/simple_dataprovider/expectedXYTree"));
            Map<Long, String> tree = assertAndGetTree(xyProvider, trace, expectedStrings);

            expectedStrings = Files.readAllLines(Paths.get("test_traces/simple_dataprovider/expectedXYData"));
            assertRows(xyProvider, tree, expectedStrings);

        } finally {
            trace.dispose();
            TmfTraceManager.getInstance().traceClosed(new TmfTraceClosedSignal(this, trace));
        }

    }

    private static void assertRows(ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> xyProvider, Map<Long, String> tree, List<String> expectedStrings) {
        TmfModelResponse<@NonNull ITmfXyModel> rowResponse = xyProvider.fetchXY(new SelectionTimeQueryFilter(1, 20, 20, tree.keySet()), null);
        assertNotNull(rowResponse);
        assertEquals(ITmfResponse.Status.COMPLETED, rowResponse.getStatus());
        ITmfXyModel rowModel = rowResponse.getModel();
        assertNotNull(rowModel);
        Map<@NonNull String, @NonNull ISeriesModel> data = rowModel.getData();

        for (int i = 0; i < expectedStrings.size(); i++) {
            String expectedString = expectedStrings.get(i);
            String[] split = expectedString.split(":");
            String rowName = split[0];
            ISeriesModel row = data.get(rowName);
            assertNotNull(row);

            String[] expectedData = split[1].split(",");
            double[] actualData = row.getData();
            for (int j = 0; j < expectedData.length; j++) {
                assertTrue("Presence of data at position " + j + " for row " + rowName, actualData.length > j);
                double expectedValue = Double.parseDouble(expectedData[j]);
                assertEquals("Data at position " + j + " for row " + rowName, expectedValue, actualData[j], 0.001);
            }

        }
        assertEquals("Same number of data", expectedStrings.size(), data.size());

    }

    private static Map<Long, String> assertAndGetTree(ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> xyProvider, ITmfTrace trace, List<String> expectedStrings) {
        TmfModelResponse<@NonNull List<ITmfTreeDataModel>> treeResponse = xyProvider.fetchTree(new TimeQueryFilter(0, Long.MAX_VALUE, 2), MONITOR);
        assertNotNull(treeResponse);
        assertEquals(ITmfResponse.Status.COMPLETED, treeResponse.getStatus());
        List<ITmfTreeDataModel> treeModel = treeResponse.getModel();
        assertNotNull(treeModel);

        Map<Long, String> map = new HashMap<>();
        for (int i = 0; i < expectedStrings.size(); i++) {
            String expectedString = expectedStrings.get(i);
            assertTrue("actual entry absent at " + i + ": " + expectedString, treeModel.size() > i);
            String[] split = expectedString.split(",");
            ITmfTreeDataModel xmlXyEntry = treeModel.get(i);

            assertEquals("Checking entry name at " + i, split[0], xmlXyEntry.getName());
            // Check the parent
            long parentId = xmlXyEntry.getParentId();
            if (parentId < 0) {
                assertEquals("Checking empty parent at " + i, split[1], "null");
            } else {
                String parentName = map.get(parentId);
                assertEquals("Checking parent at " + i, split[1], parentName);
            }
            map.put(xmlXyEntry.getId(), xmlXyEntry.getName());
        }
        assertEquals("Extra actual entries", expectedStrings.size(), treeModel.size());

        return map;
    }

}
