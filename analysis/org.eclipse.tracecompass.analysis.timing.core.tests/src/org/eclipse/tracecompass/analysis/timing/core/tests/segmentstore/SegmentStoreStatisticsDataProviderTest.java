/**********************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.analysis.timing.core.tests.segmentstore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreStatisticsModel;
import org.eclipse.tracecompass.analysis.timing.core.tests.segmentstore.statistics.StubSegmentStatisticsAnalysis;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TableColumnDescriptor;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.model.ITableColumnDescriptor;
import org.eclipse.tracecompass.tmf.core.model.filters.FilterTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStubNs;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class to verify {@link SegmentStoreStatisticsDataProvider}
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("restriction")
public class SegmentStoreStatisticsDataProviderTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private static final @NonNull List<@NonNull String> EXPECTED_HEADER_LIST = Arrays.asList("Label", "Minimum", "Maximum", "Average", "Std Dev", "Count", "Total");
    private static final @NonNull List<@NonNull String> EXPECTED_TOOLTIP_LIST = Arrays.asList("", "", "", "", "", "", "");

    private static final List<@NonNull List<@NonNull String>> LIST_OF_EXPECTED_LABELS_FULL = Arrays.asList(
            Arrays.asList("", "0", "65.534 µs", "32.767 µs", "18.918 µs", "65535", "2.147 s"),
            Arrays.asList("Total", "0", "65.534 µs", "32.767 µs", "18.918 µs", "65535", "2.147 s"),
            Arrays.asList("even", "0", "65.534 µs", "32.767 µs", "18.919 µs", "32768", "1.074 s"),
            Arrays.asList("odd", "1 ns", "65.533 µs", "32.767 µs", "18.918 µs", "32767", "1.074 s"));

    private static final @NonNull List<@NonNull List<@NonNull String>> LIST_OF_EXPECTED_LABELS_SELECTION = Arrays.asList(
            Arrays.asList("Selection", "512 ns", "4.096 µs", "2.304 µs", "1.035 µs", "3585", "8.26 ms"),
            Arrays.asList("even", "512 ns", "4.096 µs", "2.304 µs", "1.035 µs", "1793", "4.131 ms"),
            Arrays.asList("odd", "513 ns", "4.095 µs", "2.304 µs", "1.035 µs", "1792", "4.129 ms"));

    private static final @NonNull List<@NonNull StatisticsHolder> EXPECTED_STATS_FULL = Arrays.asList(
            new StatisticsHolder("", 0, -1, 0, 65534, 32767.0, 18918.46, 65535, 2147385345.0, 0, 0, 65534, 131068),
            new StatisticsHolder("Total", 1, 0, 0, 65534, 32767.0, 18918.46, 65535, 2147385345.0, 0, 0, 65534, 131068),
            new StatisticsHolder("even", 2, 1, 0, 65534, 32767.0, 18918.90, 32768, 1073709056.0, 0, 0, 65534, 131068),
            new StatisticsHolder("odd", 3, 1, 1, 65533, 32767.0, 18918.32, 32767, 1073676289.0, 1, 2, 65533, 131066));

    private static final @NonNull List<@NonNull StatisticsHolder> EXPECTED_STATS_SELECTION = Arrays.asList(
            new StatisticsHolder("Selection", 4, 0, 512, 4096, 2304.0, 1035.04, 3585, 8259840.0, 512, 1024, 4096, 8192),
            new StatisticsHolder("even", 5, 4, 512, 4096, 2304.0, 1035.48, 1793, 4131072.0, 512, 1024, 4096, 8192),
            new StatisticsHolder("odd", 6, 4, 513, 4095, 2304.0, 1034.9, 1792, 4128768.0, 513, 1026, 4095, 8190));

    private static List<ITableColumnDescriptor> fExpectedDescriptors;

    private static SegmentStoreStatisticsDataProvider fTestDataProvider;

    private static TmfXmlTraceStub fTrace;

    // ------------------------------------------------------------------------
    // Test setup and cleanup
    // ------------------------------------------------------------------------
    /**
     * Test class setup
     *
     * @throws TmfAnalysisException
     *             thrown when analysis failed
     */
    @BeforeClass
    public static void init() throws TmfAnalysisException {
        fExpectedDescriptors = new ArrayList<>();
        for (int i = 0; i < EXPECTED_HEADER_LIST.size(); i++) {
            fExpectedDescriptors.add(new TableColumnDescriptor.Builder()
                    .setText(EXPECTED_HEADER_LIST.get(i))
                    .setTooltip(EXPECTED_TOOLTIP_LIST.get(i))
                    .build());
        }

        fTrace = new TmfXmlTraceStubNs();
        @NonNull
        StubSegmentStatisticsAnalysis fixture = getValidSegmentStats(fTrace);
        assertNotNull(fTrace);
        fTestDataProvider = new SegmentStoreStatisticsDataProvider(fTrace, fixture, "org.eclipse.tracecompass.analysis.timing.core.tests.segmentstore");
    }

    /**
     * Test class clean-up
     */
    @AfterClass
    public static void cleanup() {
        if (fTestDataProvider != null) {
            fTestDataProvider.dispose();
        }
        if (fTrace != null) {
            fTrace.dispose();
        }
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    /**
     * Test to verify
     * {@link SegmentStoreStatisticsDataProvider#fetchTree(Map, org.eclipse.core.runtime.IProgressMonitor)}
     * for the full trace
     */
    @Test
    public void testFetchTreeFullRange() {
        Map<@NonNull String, @NonNull Object> fetchParameters = new HashMap<>();
        TmfModelResponse<@NonNull TmfTreeModel<@NonNull SegmentStoreStatisticsModel>> response = fTestDataProvider.fetchTree(fetchParameters, new NullProgressMonitor());
        assertNotNull(response);

        TmfTreeModel<@NonNull SegmentStoreStatisticsModel> treeModel = response.getModel();
        assertNotNull(treeModel);

        assertEquals("Header list size", EXPECTED_HEADER_LIST.size(), treeModel.getHeaders().size());
        assertEquals("Header list", EXPECTED_HEADER_LIST, treeModel.getHeaders());

        List<@NonNull ITableColumnDescriptor> columnDescriptors = treeModel.getColumnDescriptors();
        assertEquals("Header descriptor list size", EXPECTED_HEADER_LIST.size(), columnDescriptors.size());

        assertEquals("Column descriptor list", fExpectedDescriptors, columnDescriptors);

        assertNull("Scope", treeModel.getScope());

        List<@NonNull SegmentStoreStatisticsModel> entries = treeModel.getEntries();
        assertNotNull("Entries", entries);

        verifyEntries(LIST_OF_EXPECTED_LABELS_FULL,
                EXPECTED_STATS_FULL,
                entries,
                0,
                EXPECTED_STATS_FULL.size());
    }

    /**
     * Test to verify
     * {@link SegmentStoreStatisticsDataProvider#fetchTree(Map, org.eclipse.core.runtime.IProgressMonitor)}
     * for a specific time range
     */
    @Test
    public void testFetchTreeSpecificRange() {
        long start = 1024;
        long end = 4096;
        FilterTimeQueryFilter filter = new FilterTimeQueryFilter(start, end, 2, true);
        Map<@NonNull String, @NonNull Object> fetchParameters = FetchParametersUtils.filteredTimeQueryToMap(filter);
        TmfModelResponse<@NonNull TmfTreeModel<@NonNull SegmentStoreStatisticsModel>> response = fTestDataProvider.fetchTree(fetchParameters, new NullProgressMonitor());
        assertNotNull(response);

        TmfTreeModel<@NonNull SegmentStoreStatisticsModel> treeModel = response.getModel();
        assertNotNull(treeModel);

        assertEquals("Header list size", EXPECTED_HEADER_LIST.size(), treeModel.getHeaders().size());
        assertEquals("Header list", EXPECTED_HEADER_LIST, treeModel.getHeaders());

        List<@NonNull ITableColumnDescriptor> columnDescriptors = treeModel.getColumnDescriptors();
        assertEquals("Header descriptor list size", EXPECTED_HEADER_LIST.size(), columnDescriptors.size());

        assertEquals("Column descriptor list", fExpectedDescriptors, columnDescriptors);

        assertNull("Scope", treeModel.getScope());

        List<@NonNull SegmentStoreStatisticsModel> entries = treeModel.getEntries();
        assertNotNull("Entries", entries);

        verifyEntries(LIST_OF_EXPECTED_LABELS_FULL,
                EXPECTED_STATS_FULL,
                entries,
                0,
                EXPECTED_STATS_FULL.size() + EXPECTED_STATS_SELECTION.size());
        verifyEntries(LIST_OF_EXPECTED_LABELS_SELECTION,
                EXPECTED_STATS_SELECTION,
                entries,
                LIST_OF_EXPECTED_LABELS_FULL.size(),
                EXPECTED_STATS_FULL.size() + EXPECTED_STATS_SELECTION.size());
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------
    private static @NonNull StubSegmentStatisticsAnalysis getValidSegmentStats(@NonNull ITmfTrace trace) throws TmfAnalysisException {
        StubSegmentStatisticsAnalysis fixture = new StubSegmentStatisticsAnalysis();
        fixture.setTrace(trace);
        fixture.getDependentAnalyses();
        fixture.schedule();
        fixture.waitForCompletion();
        return fixture;
    }

    private static void verifyEntries(List<@NonNull List<@NonNull String>> expectedLabels,
            @NonNull List<@NonNull StatisticsHolder> expectedEntries, List<@NonNull SegmentStoreStatisticsModel> entries, int startIndex, int nbEntries) {
        assertEquals("Number of entries", nbEntries, entries.size());
        for (int i = 0; i < expectedLabels.size(); i++) {
            int index = startIndex + i;
            SegmentStoreStatisticsModel entry = entries.get(index);
            assertEquals("Entry (index " + index + ")", expectedLabels.get(i), entry.getLabels());
            assertEquals("name (index " + index + ")", expectedEntries.get(i).fName, entry.getName());
            assertEquals("id (index " + index + ")", expectedEntries.get(i).fId, entry.getId());
            assertEquals("parentId (index " + index + ")", expectedEntries.get(i).fParentId, entry.getParentId());

            assertEquals("min (index " + index + ")", expectedEntries.get(i).fMin, entry.getMin());
            assertEquals("max (index " + index + ")", expectedEntries.get(i).fMax, entry.getMax());
            assertEquals("Average (index " + index + ")", expectedEntries.get(i).fAverage, entry.getMean(), 0.02);
            assertEquals("StdDev (index " + index + ")", expectedEntries.get(i).fStdDev, entry.getStdDev(), 0.02);
            assertEquals("Count (index " + index + ")", expectedEntries.get(i).fNbElements, entry.getNbElements());
            assertEquals("Total (index " + index + ")", expectedEntries.get(i).fTotal, entry.getTotal(), 0.02);

            assertEquals("Min start (index " + index + ")", expectedEntries.get(i).fMinStart, entry.getMinStart());
            assertEquals("Min end (index " + index + ")", expectedEntries.get(i).fMinEnd, entry.getMinEnd());
            assertEquals("Max start (index " + index + ")", expectedEntries.get(i).fMaxStart, entry.getMaxStart());
            assertEquals("Max end (index " + index + ")", expectedEntries.get(i).fMaxEnd, entry.getMaxEnd());
        }
    }

    private static class StatisticsHolder {
        String fName;
        long fId;
        long fParentId;
        long fMin;
        long fMax;
        long fNbElements;
        double fAverage;
        double fStdDev;
        double fTotal;
        long fMinStart;
        long fMinEnd;
        long fMaxStart;
        long fMaxEnd;

        public StatisticsHolder(String name, long id, long parentId, long min, long max, double average,
                double stdDev, long nbElements, double total, long minStart, long minEnd, long maxStart, long maxEnd) {
            fName = name;
            fId = id;
            fParentId = parentId;
            fMin = min;
            fMax = max;
            fNbElements = nbElements;
            fAverage = average;
            fStdDev = stdDev;
            fTotal = total;
            fMinStart = minStart;
            fMinEnd = minEnd;
            fMaxStart = maxStart;
            fMaxEnd = maxEnd;
        }
    }
}
