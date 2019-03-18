/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.dataprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.provisional.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.VirtualTableQueryFilter;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test {@link FetchParametersUtils}
 *
 * @author Simon Delisle
 */
@SuppressWarnings("restriction")
@NonNullByDefault
public class FetchParametersTest {

    private static List<Long> fTimeList = Arrays.asList(1L, 2L, 3L);
    private static List<Long> fItemList = Arrays.asList(0L, 1L, 2L);
    private static final int TABLE_COUNT = 5;
    private static final long TABLE_INDEX = 0;

    private static Map<String, Object> fExpectedTimeQueryMap = new HashMap<>();
    private static Map<String, Object> fExpectedSelectionTimeQueryMap = new HashMap<>();
    private static Map<String, Object> fExpectedVirtualTableQueryMap = new HashMap<>();

    private TimeQueryFilter fExpectedTimeQuery = new TimeQueryFilter(fTimeList);
    private SelectionTimeQueryFilter fExpectedSelectionTimeQuery = new SelectionTimeQueryFilter(fTimeList, fItemList);
    private VirtualTableQueryFilter fExpectedVirtualTableQuery = new VirtualTableQueryFilter(fItemList, TABLE_INDEX, TABLE_COUNT);

    /**
     * Setup everything necessary for all tests
     */
    @BeforeClass
    public static void setUp() {
        fExpectedTimeQueryMap.put(DataProviderParameterUtils.TIME_REQUESTED_KEY, fTimeList);

        fExpectedSelectionTimeQueryMap.put(DataProviderParameterUtils.TIME_REQUESTED_KEY, fTimeList);
        fExpectedSelectionTimeQueryMap.put(DataProviderParameterUtils.SELECTED_ITEMS_KEY, fItemList);

        fExpectedVirtualTableQueryMap.put(DataProviderParameterUtils.COLUMN_ID_KEY, fItemList);
        fExpectedVirtualTableQueryMap.put(DataProviderParameterUtils.TABLE_INDEX, TABLE_INDEX);
        fExpectedVirtualTableQueryMap.put(DataProviderParameterUtils.TABLE_COUNT, TABLE_COUNT);
    }

    /**
     * Test {@link FetchParametersUtils#timeQueryToMap(TimeQueryFilter)} and
     * {@link FetchParametersUtils#createTimeQuery(Map)}
     */
    @Test
    public void testTimeQuery() {
        TimeQueryFilter timeQuery = FetchParametersUtils.createTimeQuery(fExpectedTimeQueryMap);
        assertNotNull(timeQuery);
        assertEquals(fExpectedTimeQuery, timeQuery);

        Map<String, Object> timeQueryMap = FetchParametersUtils.timeQueryToMap(fExpectedTimeQuery);
        assertFalse(timeQueryMap.isEmpty());
        assertEquals(fExpectedTimeQueryMap, timeQueryMap);
    }

    /**
     * Test
     * {@link FetchParametersUtils#selectionTimeQueryToMap(SelectionTimeQueryFilter)}
     * and {@link FetchParametersUtils#createSelectionTimeQuery(Map)}
     */
    @Test
    public void testSelectionTimeQuery() {
        TimeQueryFilter selectionTimeQuery = FetchParametersUtils.createSelectionTimeQuery(fExpectedSelectionTimeQueryMap);
        assertNotNull(selectionTimeQuery);
        assertEquals(fExpectedSelectionTimeQuery, selectionTimeQuery);

        Map<String, Object> selectionTimeQueryMap = FetchParametersUtils.selectionTimeQueryToMap(fExpectedSelectionTimeQuery);
        assertFalse(selectionTimeQueryMap.isEmpty());
        assertEquals(fExpectedSelectionTimeQueryMap, selectionTimeQueryMap);
    }

    /**
     * Test
     * {@link FetchParametersUtils#virtualTableQueryToMap(VirtualTableQueryFilter)}
     * and {@link FetchParametersUtils#createVirtualTableQueryFilter(Map)}
     */
    @Test
    public void testVirtualTableQuery() {
        VirtualTableQueryFilter virtualTableQuery = FetchParametersUtils.createVirtualTableQueryFilter(fExpectedVirtualTableQueryMap);
        assertNotNull(virtualTableQuery);
        assertEquals(fExpectedVirtualTableQuery, virtualTableQuery);

        Map<String, Object> virtualTableQueryMap = FetchParametersUtils.virtualTableQueryToMap(fExpectedVirtualTableQuery);
        assertFalse(virtualTableQueryMap.isEmpty());
        assertEquals(fExpectedVirtualTableQueryMap.get(DataProviderParameterUtils.COLUMN_ID_KEY), virtualTableQueryMap.get(DataProviderParameterUtils.COLUMN_ID_KEY));
        assertEquals(fExpectedVirtualTableQueryMap.get(DataProviderParameterUtils.TABLE_INDEX), virtualTableQueryMap.get(DataProviderParameterUtils.TABLE_INDEX));
        assertEquals(fExpectedVirtualTableQueryMap.get(DataProviderParameterUtils.TABLE_COUNT), virtualTableQueryMap.get(DataProviderParameterUtils.TABLE_COUNT));
    }

}
