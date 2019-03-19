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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test {@link DataProviderParameterUtils}
 *
 * @author Simon Delisle
 */
@NonNullByDefault
public class DataProviderParameterTest {

    private static final String CUSTOM_KEY = "MyKey";
    private static Map<String, Object> fParameters = new HashMap<>();
    private List<?> fLongList = Arrays.asList(new Long(1), new Long(2), new Long(3));
    private List<?> fIntList = Arrays.asList(new Integer(1), new Integer(2), new Integer(3));
    private List<?> fMixedList = Arrays.asList(new Integer(1), new Long(2), new Integer(3));
    private String fWrongParameter = "Unsupported";

    /**
     * Setup everything necessary for all tests
     */
    @BeforeClass
    public static void setUp() {
        fParameters.put(DataProviderParameterUtils.TIME_REQUESTED_KEY, Collections.emptyList());
        fParameters.put(DataProviderParameterUtils.SELECTED_ITEMS_KEY, Collections.emptyList());
    }

    /**
     * Test {@link DataProviderParameterUtils#extractTimeRequested(Map)} by
     * passing 4 different values
     */
    @Test
    public void testExtractTimeRequested() {
        fParameters.put(DataProviderParameterUtils.TIME_REQUESTED_KEY, fLongList);
        List<@NonNull Long> timeRequested = DataProviderParameterUtils.extractTimeRequested(fParameters);
        assertNotNull(timeRequested);
        testLongList(timeRequested);

        fParameters.put(DataProviderParameterUtils.TIME_REQUESTED_KEY, fIntList);
        timeRequested = DataProviderParameterUtils.extractTimeRequested(fParameters);
        assertNotNull(timeRequested);
        testLongList(timeRequested);

        fParameters.put(DataProviderParameterUtils.TIME_REQUESTED_KEY, fMixedList);
        timeRequested = DataProviderParameterUtils.extractTimeRequested(fParameters);
        assertNotNull(timeRequested);
        testLongList(timeRequested);

        fParameters.put(DataProviderParameterUtils.TIME_REQUESTED_KEY, fWrongParameter);
        timeRequested = DataProviderParameterUtils.extractTimeRequested(fParameters);
        assertNull(timeRequested);
    }

    /**
     * Test {@link DataProviderParameterUtils#extractSelectedItems(Map)} by
     * passing 4 different values
     */
    @Test
    public void testExtractSelectedItems() {
        fParameters.put(DataProviderParameterUtils.SELECTED_ITEMS_KEY, fLongList);
        List<@NonNull Long> items = DataProviderParameterUtils.extractSelectedItems(fParameters);
        assertNotNull(items);
        testLongList(items);

        fParameters.put(DataProviderParameterUtils.SELECTED_ITEMS_KEY, fIntList);
        items = DataProviderParameterUtils.extractSelectedItems(fParameters);
        assertNotNull(items);
        testLongList(items);

        fParameters.put(DataProviderParameterUtils.SELECTED_ITEMS_KEY, fMixedList);
        items = DataProviderParameterUtils.extractSelectedItems(fParameters);
        assertNotNull(items);
        testLongList(items);

        fParameters.put(DataProviderParameterUtils.SELECTED_ITEMS_KEY, fWrongParameter);
        items = DataProviderParameterUtils.extractSelectedItems(fParameters);
        assertNull(items);
    }

    /**
     * Test {@link DataProviderParameterUtils#extractLongList(Map, String)}
     */
    @Test
    public void testExtractLongList() {
        fParameters.put(CUSTOM_KEY, fLongList);
        List<Long> longList = DataProviderParameterUtils.extractLongList(fParameters, CUSTOM_KEY);
        assertNotNull(longList);
        testLongList(longList);
    }

    /**
     * Test {@link DataProviderParameterUtils#extractBoolean(Map, String)}
     */
    @Test
    public void testExtractBoolean() {
        fParameters.put(CUSTOM_KEY, new Boolean(true));
        Boolean extractedBoolean = DataProviderParameterUtils.extractBoolean(fParameters, CUSTOM_KEY);
        assertNotNull(extractedBoolean);
        assertTrue(extractedBoolean);

        fParameters.put(CUSTOM_KEY, new Boolean(false));
        extractedBoolean = DataProviderParameterUtils.extractBoolean(fParameters, CUSTOM_KEY);
        assertNotNull(extractedBoolean);
        assertFalse(extractedBoolean);
    }

    private static void testLongList(List<?> listToTest) {
        assertEquals(3, listToTest.size());
        assertTrue(listToTest.stream().allMatch(e -> e instanceof Long));
    }

}
