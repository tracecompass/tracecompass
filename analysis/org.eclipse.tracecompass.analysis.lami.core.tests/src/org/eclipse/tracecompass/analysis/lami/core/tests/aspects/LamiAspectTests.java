/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.lami.core.tests.aspects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiDurationAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiEmptyAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiGenericAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiIRQNameAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiIRQNumberAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiMixedAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiProcessNameAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiProcessPIDAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiProcessTIDAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTimeRangeBeginAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTimeRangeDurationAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTimeRangeEndAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTimestampAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiData;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

/**
 * Test Lami aspects
 *
 * @author Matthew Khouzam
 */
public class LamiAspectTests {

    private static final @NonNull String ASPECT_NAME = "aspect_name";

    private static @NonNull LamiTableEntry createLamiData(int i) throws JSONException {
        List<@NonNull LamiData> data = new ArrayList<>();
        Object nullObject = JSONObject.NULL;
        assertNotNull(nullObject);
        data.add(LamiData.createFromObject(nullObject));
        data.add(LamiData.createFromObject(i % 2 == 1 ? "hot" : "cold"));
        Boolean boolValue = Boolean.valueOf(i % 2 == 1);
        assertNotNull(boolValue);
        data.add(LamiData.createFromObject(boolValue));
        Integer intValue = Integer.valueOf(i);
        assertNotNull(intValue);
        data.add(LamiData.createFromObject(intValue));
        Double doubleValue = Double.valueOf(i);
        assertNotNull(doubleValue);
        data.add(LamiData.createFromObject(doubleValue));
        /* note, this is x10 + 2, this is deliberate. */
        String timerangeString = "{\n" +
                " \"begin\": " + i + ",\n" +
                " \"class\": \"time-range\",\n" +
                " \"end\": " + i + 2 + "\n" +
                "}";
        data.add(LamiData.createFromObject(new JSONObject(timerangeString)));
        return new LamiTableEntry(data);
    }

    /**
     * Test the duration aspect
     *
     * @throws JSONException
     *             won't happen
     */
    @Test
    public void lamiDurationAspectTest() throws JSONException {
        LamiTableEntryAspect aspect = new LamiDurationAspect(ASPECT_NAME, 3);
        LamiTableEntry entry1 = createLamiData(1);
        LamiTableEntry entry2 = createLamiData(2);
        assertEquals(ASPECT_NAME + " (ns)", aspect.getLabel());
        assertEquals(ASPECT_NAME, aspect.getName());
        assertEquals(0, aspect.getComparator().compare(entry1, entry2));
        assertEquals("1", aspect.resolveString(entry1));
        assertNull(aspect.resolveNumber(entry1));
    }

    /**
     * Test the empty aspect
     *
     * @throws JSONException
     *             won't happen
     */
    @Test
    public void lamiEmptyAspectTest() throws JSONException {
        LamiTableEntryAspect aspect = LamiEmptyAspect.INSTANCE;
        LamiTableEntry entry1 = createLamiData(1);
        LamiTableEntry entry2 = createLamiData(2);
        assertEquals("", aspect.getLabel());
        assertEquals("", aspect.getName());
        assertEquals(0, aspect.getComparator().compare(entry1, entry2));
        assertNull(aspect.resolveString(entry1));
        assertNull(aspect.resolveNumber(entry1));
    }

    /**
     * Test the generic aspect
     *
     * @throws JSONException
     *             won't happen
     */
    @Test
    public void lamiGenericAspectTest() throws JSONException {
        LamiTableEntryAspect aspect = new LamiGenericAspect(ASPECT_NAME, "gigaTeacups", 3, true, false);
        LamiTableEntry entry1 = createLamiData(1);
        LamiTableEntry entry2 = createLamiData(2);
        assertEquals(ASPECT_NAME + " (gigaTeacups)", aspect.getLabel());
        assertEquals(ASPECT_NAME, aspect.getName());
        assertEquals(-1, aspect.getComparator().compare(entry1, entry2));
        assertEquals("1", aspect.resolveString(entry1));
        assertEquals(1.0, aspect.resolveNumber(entry1));
    }

    /**
     * Test the irq name aspect
     *
     * @throws JSONException
     *             won't happen
     */
    @Test
    public void lamiIRQNameAspectTest() throws JSONException {
        LamiTableEntryAspect aspect = new LamiIRQNameAspect(ASPECT_NAME, 3);
        LamiTableEntry entry1 = createLamiData(1);
        LamiTableEntry entry2 = createLamiData(2);
        assertEquals(ASPECT_NAME + " (name)", aspect.getLabel());
        assertEquals(ASPECT_NAME + " (name)", aspect.getName());
        assertEquals(-1, aspect.getComparator().compare(entry1, entry2));
        assertEquals("1", aspect.resolveString(entry1));
        assertNull(aspect.resolveNumber(entry1));
    }

    /**
     * Test the irq number aspect
     *
     * @throws JSONException
     *             won't happen
     */
    @Test
    public void lamiIRQNumberAspectTest() throws JSONException {
        LamiTableEntryAspect aspect = new LamiIRQNumberAspect(ASPECT_NAME, 3);
        LamiTableEntry entry1 = createLamiData(1);
        LamiTableEntry entry2 = createLamiData(2);
        assertEquals(ASPECT_NAME + " (#)", aspect.getLabel());
        assertEquals(ASPECT_NAME + " (#)", aspect.getName());
        assertEquals(0, aspect.getComparator().compare(entry1, entry2));
        assertEquals("1", aspect.resolveString(entry1));
        assertNull(aspect.resolveNumber(entry1));
    }

    /**
     * Test the mixed aspect
     *
     * @throws JSONException
     *             won't happen
     */
    @Test
    public void lamiMixedAspectTest() throws JSONException {
        LamiTableEntryAspect aspect = new LamiMixedAspect(ASPECT_NAME, 0);
        LamiTableEntry entry1 = createLamiData(1);
        LamiTableEntry entry2 = createLamiData(2);
        assertEquals(ASPECT_NAME, aspect.getLabel());
        assertEquals(ASPECT_NAME, aspect.getName());
        assertEquals(0, aspect.getComparator().compare(entry1, entry2));
        assertNull(aspect.resolveString(entry1));
        assertNull(aspect.resolveNumber(entry1));
    }

    /**
     * Test the process name aspect
     *
     * @throws JSONException
     *             won't happen
     */
    @Test
    public void lamiProcessNameAspectTest() throws JSONException {
        LamiTableEntryAspect aspect = new LamiProcessNameAspect(ASPECT_NAME, 0);
        LamiTableEntry entry1 = createLamiData(1);
        LamiTableEntry entry2 = createLamiData(2);
        assertEquals(ASPECT_NAME + " (name)", aspect.getLabel());
        assertEquals(ASPECT_NAME + " (name)", aspect.getName());
        assertEquals(0, aspect.getComparator().compare(entry1, entry2));
        assertNull(aspect.resolveString(entry1));
        assertNull(aspect.resolveNumber(entry1));
    }

    /**
     * Test the process pid aspect
     *
     * @throws JSONException
     *             won't happen
     */
    @Test
    public void lamiProcessPIDAspectTest() throws JSONException {
        LamiTableEntryAspect aspect = new LamiProcessPIDAspect(ASPECT_NAME, 0);
        LamiTableEntry entry1 = createLamiData(1);
        LamiTableEntry entry2 = createLamiData(2);
        assertEquals(ASPECT_NAME + " (PID)", aspect.getLabel());
        assertEquals(ASPECT_NAME + " (PID)", aspect.getName());
        assertEquals(0, aspect.getComparator().compare(entry1, entry2));
        assertNull(aspect.resolveString(entry1));
        assertNull(aspect.resolveNumber(entry1));
    }

    /**
     * Test the process tid aspect
     *
     * @throws JSONException
     *             won't happen
     */
    @Test
    public void lamiProcessTIDAspectTest() throws JSONException {
        LamiTableEntryAspect aspect = new LamiProcessTIDAspect(ASPECT_NAME, 0);
        LamiTableEntry entry1 = createLamiData(1);
        LamiTableEntry entry2 = createLamiData(2);
        assertEquals(ASPECT_NAME + " (TID)", aspect.getLabel());
        assertEquals(ASPECT_NAME + " (TID)", aspect.getName());
        assertEquals(0, aspect.getComparator().compare(entry1, entry2));
        assertNull(aspect.resolveString(entry1));
        assertNull(aspect.resolveNumber(entry1));
    }

    /**
     * Test the time range begin aspect
     *
     * @throws JSONException
     *             won't happen
     */
    @Test
    public void lamiTimeRangeBeginAspectTest() throws JSONException {
        LamiTableEntryAspect aspect = new LamiTimeRangeBeginAspect(ASPECT_NAME, 5);
        LamiTableEntry entry1 = createLamiData(1);
        LamiTableEntry entry2 = createLamiData(2);
        assertEquals(ASPECT_NAME + " (begin)", aspect.getLabel());
        assertEquals(ASPECT_NAME + " (begin)", aspect.getName());
        assertEquals(-1, aspect.getComparator().compare(entry1, entry2));
        String timeRangeString = aspect.resolveString(entry1);
        assertNotNull(timeRangeString);
        timeRangeString = timeRangeString.substring(timeRangeString.indexOf(".") + 1, timeRangeString.length());
        assertEquals("000 000 001", timeRangeString);
        assertEquals(1L, aspect.resolveNumber(entry1));
    }

    /**
     * Test the time range duration aspect
     *
     * @throws JSONException
     *             won't happen
     */
    @Test
    public void lamiTimeRangeDurationAspectTest() throws JSONException {
        LamiTableEntryAspect aspect = new LamiTimeRangeDurationAspect(ASPECT_NAME, 5);
        LamiTableEntry entry1 = createLamiData(1);
        LamiTableEntry entry2 = createLamiData(2);
        assertEquals(ASPECT_NAME + " (duration) (ns)", aspect.getLabel());
        assertEquals(ASPECT_NAME + " (duration)", aspect.getName());
        assertEquals(-1, aspect.getComparator().compare(entry1, entry2));
        String timeRangeString = aspect.resolveString(entry1);
        assertNotNull(timeRangeString);
        assertEquals("11", timeRangeString);
        assertEquals(11L, aspect.resolveNumber(entry1));
    }

    /**
     * Test the time range end aspect
     *
     * @throws JSONException
     *             won't happen
     */
    @Test
    public void lamiTimeRangeEndAspectTest() throws JSONException {
        LamiTableEntryAspect aspect = new LamiTimeRangeEndAspect(ASPECT_NAME, 5);
        LamiTableEntry entry1 = createLamiData(1);
        LamiTableEntry entry2 = createLamiData(2);
        assertEquals(ASPECT_NAME + " (end)", aspect.getLabel());
        assertEquals(ASPECT_NAME + " (end)", aspect.getName());
        assertEquals(-1, aspect.getComparator().compare(entry1, entry2));
        String timeRangeString = aspect.resolveString(entry1);
        assertNotNull(timeRangeString);
        timeRangeString = timeRangeString.substring(timeRangeString.indexOf(".") + 1, timeRangeString.length());
        assertEquals("000 000 012", timeRangeString);
        assertEquals(12L, aspect.resolveNumber(entry1));
    }

    /**
     * Test the timestamp aspect
     *
     * @throws JSONException
     *             won't happen
     */
    @Test
    public void lamiTimestampAspectTest() throws JSONException {
        LamiTableEntryAspect aspect = new LamiTimestampAspect(ASPECT_NAME, 0);
        LamiTableEntry entry1 = createLamiData(1);
        LamiTableEntry entry2 = createLamiData(2);
        assertEquals(ASPECT_NAME, aspect.getLabel());
        assertEquals(ASPECT_NAME, aspect.getName());
        assertEquals(0, aspect.getComparator().compare(entry1, entry2));
        assertNull(aspect.resolveString(entry1));
        assertNull(aspect.resolveNumber(entry1));
    }

    /**
     * Test the equivalence of aspects, for aggregating in views.
     */
    @Test
    public void testAspectEquality() {
        LamiTableEntryAspect aspect1 = new LamiTimestampAspect(ASPECT_NAME, 0);
        LamiTableEntryAspect aspect2 = new LamiTimeRangeEndAspect(ASPECT_NAME, 5);
        LamiTableEntryAspect aspect3 = new LamiProcessTIDAspect(ASPECT_NAME, 0);

        assertTrue(aspect1.arePropertiesEqual(aspect1));
        assertTrue(aspect1.arePropertiesEqual(aspect2));
        assertFalse(aspect1.arePropertiesEqual(aspect3));
        assertTrue(aspect2.arePropertiesEqual(aspect1));
        assertTrue(aspect2.arePropertiesEqual(aspect2));
        assertFalse(aspect2.arePropertiesEqual(aspect3));
        assertFalse(aspect3.arePropertiesEqual(aspect1));
        assertFalse(aspect3.arePropertiesEqual(aspect2));
        assertTrue(aspect3.arePropertiesEqual(aspect3));
    }
}
