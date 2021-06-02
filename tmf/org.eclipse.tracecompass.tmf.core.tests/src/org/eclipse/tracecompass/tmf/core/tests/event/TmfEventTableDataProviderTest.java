/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.events.TmfEventTableColumnDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.events.TmfEventTableDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.events.TmfEventTableFilterModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.EventTableQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.VirtualTableQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.EventTableLine;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfVirtualTableDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfVirtualTableModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.TmfVirtualTableModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.VirtualTableCell;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.model.CoreFilterProperty;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the {@link TmfEventTableDataProvider}
 *
 * @author Yonni Chen
 */
@NonNullByDefault
public class TmfEventTableDataProviderTest {

    private static final TmfTestTrace TEST_TRACE = TmfTestTrace.A_TEST_10K;

    private static ITmfTrace fTrace = new TmfTraceStub();
    private static ITmfVirtualTableDataProvider<TmfEventTableColumnDataModel, EventTableLine> fProvider = new TmfEventTableDataProvider(fTrace);

    private static final String TIMESTAMP_COLUMN_NAME = "Timestamp";
    private static final String TIMESTAMP_NS_COLUMN_NAME = "Timestamp ns";
    private static final String EVENT_TYPE_COLUMN_NAME = "Event type";
    private static final String CONTENTS_COLUMN_NAME = "Contents";

    private static Map<String, Long> fColumns = Collections.emptyMap();

    /**
     * Set up resources
     *
     * @throws TmfTraceException
     *             Trace exception should not happen
     */
    @BeforeClass
    public static void beforeClass() throws TmfTraceException {
        fTrace.dispose();
        fTrace = new TmfTraceStub(TEST_TRACE.getFullPath(), ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, true, null);
        fProvider = new TmfEventTableDataProvider(fTrace);
        // Make sure the columns are computed before the test
        fColumns = fetchColumnId();
    }

    private static Map<String, Long> fetchColumnId() {
        TmfTreeModel<TmfEventTableColumnDataModel> columns = fProvider.fetchTree(FetchParametersUtils.timeQueryToMap(new TimeQueryFilter(0, 0, 1)), null).getModel();
        if (columns == null) {
            return Collections.emptyMap();
        }

        List<TmfEventTableColumnDataModel> columnEntries = columns.getEntries();
        // Order should be timestamp, event type and contents
        assertEquals(TIMESTAMP_COLUMN_NAME, columnEntries.get(0).getName());
        assertEquals(EVENT_TYPE_COLUMN_NAME, columnEntries.get(1).getName());
        assertEquals(CONTENTS_COLUMN_NAME, columnEntries.get(2).getName());
        assertEquals(TIMESTAMP_NS_COLUMN_NAME, columnEntries.get(3).getName());

        Map<String, Long> expectedColumns = new LinkedHashMap<>();
        for (TmfEventTableColumnDataModel column : columnEntries) {
            expectedColumns.put(column.getName(), column.getId());
        }
        return expectedColumns;
    }

    private static String lineTimestamp(long millisecond) {
        return TmfTimestamp.fromMillis(millisecond).toString();
    }

    private static String lineNsTimestamp(int millisecond) {
        return String.valueOf(TmfTimestamp.fromMillis(millisecond).toNanos());
    }

    /**
     * Dispose resources
     */
    @AfterClass
    public static void tearDown() {
        fTrace.dispose();
    }

    /**
     * Test columns returned by the provider.
     */
    @Test
    public void testDataProviderFetchColumn() {
        Long timestampColumnId = fColumns.get(TIMESTAMP_COLUMN_NAME);
        Long eventTypeColumnId = fColumns.get(EVENT_TYPE_COLUMN_NAME);
        Long contentsColumnId = fColumns.get(CONTENTS_COLUMN_NAME);
        Long timestampNsColumnId = fColumns.get(TIMESTAMP_NS_COLUMN_NAME);
        assertNotNull(timestampColumnId);
        assertNotNull(eventTypeColumnId);
        assertNotNull(contentsColumnId);
        assertNotNull(timestampNsColumnId);
        List<TmfEventTableColumnDataModel> expectedColumnEntries = Arrays.asList(
                new TmfEventTableColumnDataModel(timestampColumnId, -1, Collections.singletonList(TIMESTAMP_COLUMN_NAME), "", false),
                new TmfEventTableColumnDataModel(eventTypeColumnId, -1, Collections.singletonList(EVENT_TYPE_COLUMN_NAME), "The type of this event. This normally determines the field layout.", false),
                new TmfEventTableColumnDataModel(contentsColumnId, -1, Collections.singletonList(CONTENTS_COLUMN_NAME), "The fields (or payload) of this event", false),
                new TmfEventTableColumnDataModel(timestampNsColumnId, -1, Collections.singletonList(TIMESTAMP_NS_COLUMN_NAME), "Timestamp in nanoseconds, normalized and useful for calculations", true));

        TmfModelResponse<TmfTreeModel<TmfEventTableColumnDataModel>> response = fProvider.fetchTree(FetchParametersUtils.timeQueryToMap(new TimeQueryFilter(0, 0, 1)), null);
        TmfTreeModel<TmfEventTableColumnDataModel> currentColumnModel = response.getModel();
        assertNotNull(currentColumnModel);
        List<TmfEventTableColumnDataModel> currentColumnEntries = currentColumnModel.getEntries();
        assertEquals(expectedColumnEntries, currentColumnEntries);
    }

    /**
     * Given a start index and count, we check model returned by the data provider.
     * This test don't provide desired columns, so it queries data for all of them
     */
    @Test
    public void testDataProvider() {
        VirtualTableQueryFilter queryFilter = new EventTableQueryFilter(Collections.emptyList(), 0, 5, null);

        List<EventTableLine> expectedData = Arrays.asList(
                new EventTableLine(Arrays.asList(new VirtualTableCell(lineTimestamp(1)), new VirtualTableCell("Type-0"), new VirtualTableCell(""), new VirtualTableCell(lineNsTimestamp(1))), 0, TmfTimestamp.fromMillis(1), 0, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell(lineTimestamp(2)), new VirtualTableCell("Type-1"), new VirtualTableCell(""), new VirtualTableCell(lineNsTimestamp(2))), 1, TmfTimestamp.fromMillis(2), 1, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell(lineTimestamp(3)), new VirtualTableCell("Type-2"), new VirtualTableCell(""), new VirtualTableCell(lineNsTimestamp(3))), 2, TmfTimestamp.fromMillis(3), 2, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell(lineTimestamp(4)), new VirtualTableCell("Type-3"), new VirtualTableCell(""), new VirtualTableCell(lineNsTimestamp(4))), 3, TmfTimestamp.fromMillis(4), 3, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell(lineTimestamp(5)), new VirtualTableCell("Type-4"), new VirtualTableCell(""), new VirtualTableCell(lineNsTimestamp(5))), 4, TmfTimestamp.fromMillis(5), 4, 0));

        TmfModelResponse<ITmfVirtualTableModel<EventTableLine>> response = fProvider.fetchLines(FetchParametersUtils.virtualTableQueryToMap(queryFilter), null);
        ITmfVirtualTableModel<EventTableLine> currentModel = response.getModel();

        ITmfVirtualTableModel<EventTableLine> expectedModel = new TmfVirtualTableModel<>(new ArrayList<>(fColumns.values()), expectedData, 0, fTrace.getNbEvents());
        assertEquals(expectedModel, currentModel);
    }

    /**
     * Given a start index that is out of bound and count, we check data returned
     * by the data provider.
     */
    @Test
    public void testDataProviderWithOutOfBoundIndex() {
        VirtualTableQueryFilter queryFilter = new EventTableQueryFilter(Collections.emptyList(), 2000000, 5, null);
        TmfModelResponse<ITmfVirtualTableModel<EventTableLine>> response = fProvider.fetchLines(FetchParametersUtils.virtualTableQueryToMap(queryFilter), null);
        ITmfVirtualTableModel<EventTableLine> currentModel = response.getModel();

        assertNotNull(currentModel);
        assertEquals(new ArrayList<>(fColumns.values()), currentModel.getColumnIds());
        assertTrue(currentModel.getLines().isEmpty());
    }

    /**
     * Given a start index, count and a list of desired columns, we check model
     * returned by the data provider.
     */
    @Test
    public void testDataProviderWithDesiredColumns() {
        Long eventTypeColumnId = fColumns.get(EVENT_TYPE_COLUMN_NAME);
        assertNotNull(eventTypeColumnId);
        VirtualTableQueryFilter queryFilter = new EventTableQueryFilter(Collections.singletonList(eventTypeColumnId), 5, 5, null);

        List<Long> expectedColumnsId = Arrays.asList(eventTypeColumnId);
        List<EventTableLine> expectedData = Arrays.asList(
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-5")), 5, TmfTimestamp.fromMillis(6), 5, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-6")), 6, TmfTimestamp.fromMillis(7), 6, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-0")), 7, TmfTimestamp.fromMillis(8), 7, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-1")), 8, TmfTimestamp.fromMillis(9), 8, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-2")), 9, TmfTimestamp.fromMillis(10), 9, 0));

        TmfModelResponse<ITmfVirtualTableModel<EventTableLine>> response = fProvider.fetchLines(FetchParametersUtils.virtualTableQueryToMap(queryFilter), null);
        ITmfVirtualTableModel<EventTableLine> currentModel = response.getModel();

        ITmfVirtualTableModel<EventTableLine> expectedModel = new TmfVirtualTableModel<>(expectedColumnsId, expectedData, 5, fTrace.getNbEvents());
        assertEquals(expectedModel, currentModel);
    }

    /**
     * Given a start index, count and a list of desired columns that contains a
     * non-existent column, we check model returned by the data provider.
     */
    @Test
    public void testDataProviderWithOneNonExistentColumns() {
        Long eventTypeColumnId = fColumns.get(EVENT_TYPE_COLUMN_NAME);
        Long timestampColumnId = fColumns.get(TIMESTAMP_COLUMN_NAME);
        assertNotNull(timestampColumnId);
        assertNotNull(eventTypeColumnId);
        VirtualTableQueryFilter queryFilter = new EventTableQueryFilter(Arrays.asList(eventTypeColumnId, 10L, timestampColumnId), 150, 5, null);

        List<Long> expectedColumnsId = Arrays.asList(eventTypeColumnId, timestampColumnId);
        List<EventTableLine> expectedData = Arrays.asList(
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-3"), new VirtualTableCell(lineTimestamp(151))), 150, TmfTimestamp.fromMillis(151), 150, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-4"), new VirtualTableCell(lineTimestamp(152))), 151, TmfTimestamp.fromMillis(152), 151, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-5"), new VirtualTableCell(lineTimestamp(153))), 152, TmfTimestamp.fromMillis(153), 152, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-6"), new VirtualTableCell(lineTimestamp(154))), 153, TmfTimestamp.fromMillis(154), 153, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-0"), new VirtualTableCell(lineTimestamp(155))), 154, TmfTimestamp.fromMillis(155), 154, 0));

        TmfModelResponse<ITmfVirtualTableModel<EventTableLine>> response = fProvider.fetchLines(FetchParametersUtils.virtualTableQueryToMap(queryFilter), null);
        ITmfVirtualTableModel<EventTableLine> currentModel = response.getModel();

        ITmfVirtualTableModel<EventTableLine> expectedModel = new TmfVirtualTableModel<>(expectedColumnsId, expectedData, 150, fTrace.getNbEvents());
        assertEquals(expectedModel, currentModel);
    }

    /**
     * Given a start index, count and a list of desired columns that contains only
     * non-existent columns, we check data returned by the data provider.
     */
    @Test
    public void testDataProviderWithNonExistentColumns() {
        VirtualTableQueryFilter queryFilter = new EventTableQueryFilter(Arrays.asList(10L, 11L), 0, 10, null);
        TmfModelResponse<ITmfVirtualTableModel<EventTableLine>> response = fProvider.fetchLines(FetchParametersUtils.virtualTableQueryToMap(queryFilter), null);
        ITmfVirtualTableModel<EventTableLine> currentModel = response.getModel();

        assertNotNull(currentModel);
        assertTrue(currentModel.getColumnIds().isEmpty());
        assertTrue(currentModel.getLines().isEmpty());
    }

    /**
     * Given a start index, count and a list of desired columns, we check model
     * returned by the data provider. We also apply a filter on a column
     */
    @Test
    public void testDataProviderWithSimpleFilter() {
        Long eventTypeColumnId = fColumns.get(EVENT_TYPE_COLUMN_NAME);
        Long timestampColumnId = fColumns.get(TIMESTAMP_COLUMN_NAME);
        assertNotNull(timestampColumnId);
        assertNotNull(eventTypeColumnId);

        Map<Long, String> tableFilter = new HashMap<>();
        tableFilter.put(eventTypeColumnId, "1");
        TmfEventTableFilterModel filterModel = new TmfEventTableFilterModel(tableFilter, null, false);
        VirtualTableQueryFilter queryFilter = new EventTableQueryFilter(Arrays.asList(eventTypeColumnId, timestampColumnId), 0, 5, filterModel);
        Map<String, Object> parameters = FetchParametersUtils.virtualTableQueryToMap(queryFilter);
        parameters.put(TmfEventTableDataProvider.TABLE_FILTERS_KEY, filterModel);

        List<Long> expectedColumnsId = Arrays.asList(eventTypeColumnId, timestampColumnId);
        TmfTimestampFormat.getDefaulTimeFormat().format(TmfTimestamp.fromMillis(2).toNanos());
        List<EventTableLine> expectedData = Arrays.asList(
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-1"), new VirtualTableCell(lineTimestamp(2))), 0, TmfTimestamp.fromMillis(2), 1, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-1"), new VirtualTableCell(lineTimestamp(9))), 1, TmfTimestamp.fromMillis(9), 8, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-1"), new VirtualTableCell(lineTimestamp(16))), 2, TmfTimestamp.fromMillis(16), 15, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-1"), new VirtualTableCell(lineTimestamp(23))), 3, TmfTimestamp.fromMillis(23), 22, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-1"), new VirtualTableCell(lineTimestamp(30))), 4, TmfTimestamp.fromMillis(30), 29, 0));

        TmfModelResponse<ITmfVirtualTableModel<EventTableLine>> response = fProvider.fetchLines(parameters, null);
        ITmfVirtualTableModel<EventTableLine> currentModel = response.getModel();

        TmfVirtualTableModel<@NonNull EventTableLine> expectedModel = new TmfVirtualTableModel<>(expectedColumnsId, expectedData, 0, 1429);
        assertEquals(expectedModel, currentModel);
    }

    /**
     * Given a start index, count and a list of desired columns, we check model
     * returned by the data provider. We also apply two filters on two columns
     */
    @Test
    public void testDataProviderWithMultipleFilter() {
        Long eventTypeColumnId = fColumns.get(EVENT_TYPE_COLUMN_NAME);
        Long timestampColumnId = fColumns.get(TIMESTAMP_COLUMN_NAME);
        assertNotNull(timestampColumnId);
        assertNotNull(eventTypeColumnId);

        Map<Long, String> tableFilter = new HashMap<>();
        tableFilter.put(eventTypeColumnId, "0");
        tableFilter.put(timestampColumnId, "8");
        TmfEventTableFilterModel filterModel = new TmfEventTableFilterModel(tableFilter, null, false);
        VirtualTableQueryFilter queryFilter = new EventTableQueryFilter(Arrays.asList(eventTypeColumnId, timestampColumnId), 0, 5, filterModel);
        Map<String, Object> parameters = FetchParametersUtils.virtualTableQueryToMap(queryFilter);
        parameters.put(TmfEventTableDataProvider.TABLE_FILTERS_KEY, filterModel);

        List<Long> expectedColumnsId = Arrays.asList(eventTypeColumnId, timestampColumnId);
        List<EventTableLine> expectedData = Arrays.asList(
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-0"), new VirtualTableCell(lineTimestamp(8))), 0, TmfTimestamp.fromMillis(8), 7, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-0"), new VirtualTableCell(lineTimestamp(78))), 1, TmfTimestamp.fromMillis(78), 77, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-0"), new VirtualTableCell(lineTimestamp(85))), 2, TmfTimestamp.fromMillis(85), 84, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-0"), new VirtualTableCell(lineTimestamp(148))), 3, TmfTimestamp.fromMillis(148), 147, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-0"), new VirtualTableCell(lineTimestamp(183))), 4, TmfTimestamp.fromMillis(183), 182, 0));

        TmfModelResponse<ITmfVirtualTableModel<EventTableLine>> response = fProvider.fetchLines(parameters, null);
        ITmfVirtualTableModel<EventTableLine> currentModel = response.getModel();

        ITmfVirtualTableModel<EventTableLine> expectedModel = new TmfVirtualTableModel<>(expectedColumnsId, expectedData, 0, 492);
        assertEquals(expectedModel, currentModel);
    }

    /**
     * Given a start index, count and a list of desired columns, we check model
     * returned by the data provider. We also apply a filter on a column
     */
    @Test
    public void testDataProviderWithSimpleSearch() {
        Long eventTypeColumnId = fColumns.get(EVENT_TYPE_COLUMN_NAME);
        Long timestampColumnId = fColumns.get(TIMESTAMP_COLUMN_NAME);

        assertNotNull(timestampColumnId);
        assertNotNull(eventTypeColumnId);

        // Query for the index for the first matching event
        VirtualTableQueryFilter queryFilter = new EventTableQueryFilter(Arrays.asList(eventTypeColumnId, timestampColumnId), 0, 1, null);
        Map<String, Object> parameters = FetchParametersUtils.virtualTableQueryToMap(queryFilter);

        List<String> searchExpressions = new ArrayList<>();
        searchExpressions.add("\""+EVENT_TYPE_COLUMN_NAME+"\"" + " matches " + "Type-2");

        parameters.put(TmfEventTableDataProvider.TABLE_SEARCH_KEY, searchExpressions);
        parameters.put(TmfEventTableDataProvider.TABLE_SEARCH_INDEX_KEY, true);

        List<Long> expectedColumnsId = Arrays.asList(eventTypeColumnId, timestampColumnId);
        TmfTimestampFormat.getDefaulTimeFormat().format(TmfTimestamp.fromMillis(2).toNanos());
        List<EventTableLine> expectedData = Arrays.asList(
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-2"), new VirtualTableCell(lineTimestamp(3))), 2, TmfTimestamp.fromMillis(3), 2, 0));
        expectedData.get(0).setActiveProperties(CoreFilterProperty.HIGHLIGHT);

        TmfModelResponse<ITmfVirtualTableModel<EventTableLine>> response = fProvider.fetchLines(parameters, null);
        ITmfVirtualTableModel<EventTableLine> currentModel = response.getModel();

        TmfVirtualTableModel<@NonNull EventTableLine> expectedModel = new TmfVirtualTableModel<>(expectedColumnsId, expectedData, 0, 10000);
        assertEquals(expectedModel, currentModel);

        // Query for events with search filter active. Matching lines will be tagged for highlighting
        int nbEventsRequested = 5;
        queryFilter = new EventTableQueryFilter(Arrays.asList(eventTypeColumnId, timestampColumnId), 0, nbEventsRequested, null);
        parameters = FetchParametersUtils.virtualTableQueryToMap(queryFilter);
        parameters.put(TmfEventTableDataProvider.TABLE_SEARCH_KEY, searchExpressions);

        response = fProvider.fetchLines(parameters, null);
        currentModel = response.getModel();
        assertNotNull(currentModel);
        assertEquals(nbEventsRequested, currentModel.getLines().size());
        expectedData = Arrays.asList(
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-0"), new VirtualTableCell(lineTimestamp(1))), 0, TmfTimestamp.fromMillis(1), 0, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-1"), new VirtualTableCell(lineTimestamp(2))), 1, TmfTimestamp.fromMillis(2), 1, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-2"), new VirtualTableCell(lineTimestamp(3))), 2, TmfTimestamp.fromMillis(3), 2, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-3"), new VirtualTableCell(lineTimestamp(4))), 3, TmfTimestamp.fromMillis(4), 3, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell("Type-4"), new VirtualTableCell(lineTimestamp(5))), 4, TmfTimestamp.fromMillis(5), 4, 0));
        expectedData.get(2).setActiveProperties(CoreFilterProperty.HIGHLIGHT);
        expectedModel = new TmfVirtualTableModel<>(expectedColumnsId, expectedData, 0, 10000);
        assertEquals(expectedModel, currentModel);
    }

    /**
     * Given a start index, count and a list of desired columns, we check model
     * returned by the data provider. We also apply a filter on a column
     */
    @Test
    public void testDataProviderWithComplexSearch() {
        // Query for the index for the first matching event
        VirtualTableQueryFilter queryFilter = new EventTableQueryFilter(new ArrayList<>(fColumns.values()), 0, 1, null);
        Map<String, Object> parameters = FetchParametersUtils.virtualTableQueryToMap(queryFilter);

        List<String> searchExpressions = new ArrayList<>();
        searchExpressions.add("\""+EVENT_TYPE_COLUMN_NAME+"\"" + " == " + "Type-3");
        searchExpressions.add("\""+TIMESTAMP_COLUMN_NAME+"\"" + " contains " + "4");

        parameters.put(TmfEventTableDataProvider.TABLE_SEARCH_KEY, searchExpressions);
        parameters.put(TmfEventTableDataProvider.TABLE_SEARCH_INDEX_KEY, true);

        List<Long> expectedColumnsId = new ArrayList<>(fColumns.values());
        TmfTimestampFormat.getDefaulTimeFormat().format(TmfTimestamp.fromMillis(2).toNanos());
        List<EventTableLine> expectedData = Arrays.asList(
                new EventTableLine(Arrays.asList(new VirtualTableCell(lineTimestamp(4)), new VirtualTableCell("Type-3"), new VirtualTableCell(""), new VirtualTableCell(lineNsTimestamp(4))), 3, TmfTimestamp.fromMillis(4), 3, 0));
        expectedData.get(0).setActiveProperties(CoreFilterProperty.HIGHLIGHT);

        TmfModelResponse<ITmfVirtualTableModel<EventTableLine>> response = fProvider.fetchLines(parameters, null);
        ITmfVirtualTableModel<EventTableLine> currentModel = response.getModel();

        TmfVirtualTableModel<@NonNull EventTableLine> expectedModel = new TmfVirtualTableModel<>(expectedColumnsId, expectedData, 0, 10000);
        assertEquals(expectedModel, currentModel);

        // Query for events with search filter active. Matching lines will be tagged for highlighting
        int nbEventsRequested = 5;
        queryFilter = new EventTableQueryFilter(new ArrayList<>(fColumns.values()), 0, nbEventsRequested, null);
        parameters = FetchParametersUtils.virtualTableQueryToMap(queryFilter);
        parameters.put(TmfEventTableDataProvider.TABLE_SEARCH_KEY, searchExpressions);

        response = fProvider.fetchLines(parameters, null);
        currentModel = response.getModel();
        assertNotNull(currentModel);
        assertEquals(nbEventsRequested, currentModel.getLines().size());
        expectedData = Arrays.asList(
                new EventTableLine(Arrays.asList(new VirtualTableCell(lineTimestamp(1)), new VirtualTableCell("Type-0"), new VirtualTableCell(""), new VirtualTableCell(lineNsTimestamp(1))), 0, TmfTimestamp.fromMillis(1), 0, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell(lineTimestamp(2)), new VirtualTableCell("Type-1"), new VirtualTableCell(""), new VirtualTableCell(lineNsTimestamp(2))), 1, TmfTimestamp.fromMillis(2), 1, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell(lineTimestamp(3)), new VirtualTableCell("Type-2"), new VirtualTableCell(""), new VirtualTableCell(lineNsTimestamp(3))), 2, TmfTimestamp.fromMillis(3), 2, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell(lineTimestamp(4)), new VirtualTableCell("Type-3"), new VirtualTableCell(""), new VirtualTableCell(lineNsTimestamp(4))), 3, TmfTimestamp.fromMillis(4), 3, 0),
                new EventTableLine(Arrays.asList(new VirtualTableCell(lineTimestamp(5)), new VirtualTableCell("Type-4"), new VirtualTableCell(""), new VirtualTableCell(lineNsTimestamp(5))), 4, TmfTimestamp.fromMillis(5), 4, 0));
        expectedData.get(3).setActiveProperties(CoreFilterProperty.HIGHLIGHT);
        expectedModel = new TmfVirtualTableModel<>(expectedColumnsId, expectedData, 0, 10000);
        assertEquals(expectedModel, currentModel);
    }

    /**
     * Sets a negative index to EventTableQueryFilter. Expected an
     * IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testQueryFilterIndexParameter() {
        new EventTableQueryFilter(Collections.emptyList(), -1, 5, null);
    }
}
