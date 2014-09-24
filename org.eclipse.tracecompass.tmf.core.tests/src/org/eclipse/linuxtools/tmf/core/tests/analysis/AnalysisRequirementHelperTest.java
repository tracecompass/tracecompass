/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Guilliano Molaire - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisRequirementProvider;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement.ValuePriorityLevel;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirementHelper;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.AnalysisModuleTestHelper;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.AnalysisModuleTestHelper.moduleStubEnum;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.AnalysisRequirementFactory;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Multimap;

/**
 * Test suite for the {@link TmfAnalysisRequirementHelper} class
 */
public class AnalysisRequirementHelperTest {

    private AnalysisModuleTestHelper fTestModuleHelper;
    private AnalysisModuleTestHelper fTestModuleHelper2;

    /**
     * Set up analysis modules
     */
    @Before
    public void setUpTest() {
        fTestModuleHelper = new AnalysisModuleTestHelper(moduleStubEnum.TEST);
        fTestModuleHelper2 = new AnalysisModuleTestHelper(moduleStubEnum.TEST2);
    }

    /**
     * Test suite for the
     * {@link TmfAnalysisRequirementHelper#getRequirementValues(IAnalysisRequirementProvider, String)}
     * method
     */
    @Test
    public void testGetRequirementValues() {
        /* Test with a supported type */
        String type = AnalysisRequirementFactory.REQUIREMENT_TYPE_1;
        Set<String> values = TmfAnalysisRequirementHelper.getRequirementValues(fTestModuleHelper, type);
        assertEquals(AnalysisRequirementFactory.REQUIREMENT_VALUES_1.size(), values.size());

        /* Test with a type not supported */
        type = AnalysisRequirementFactory.REQUIREMENT_TYPE_2;
        values = TmfAnalysisRequirementHelper.getRequirementValues(fTestModuleHelper, type);
        assertTrue(values.isEmpty());
    }

    /**
     * Test suite for the
     * {@link TmfAnalysisRequirementHelper#getRequirementValues(IAnalysisRequirementProvider, String, ValuePriorityLevel)}
     * method
     */
    @Test
    public void testGetRequirementValuesWithLevel() {
        /* Test with a supported type */
        String type = AnalysisRequirementFactory.REQUIREMENT_TYPE_2;
        Set<String> values = TmfAnalysisRequirementHelper.getRequirementValues(fTestModuleHelper2, type, ValuePriorityLevel.MANDATORY);
        assertEquals(3, values.size());

        /* Test with another value level */
        values = TmfAnalysisRequirementHelper.getRequirementValues(fTestModuleHelper2, type, ValuePriorityLevel.OPTIONAL);
        assertEquals(2, values.size());

        /* Test with a type not supported */
        type = AnalysisRequirementFactory.REQUIREMENT_TYPE_1;
        values = TmfAnalysisRequirementHelper.getRequirementValues(fTestModuleHelper2, type, ValuePriorityLevel.MANDATORY);
        assertTrue(values.isEmpty());
    }

    /**
     * Test suite for the
     * {@link TmfAnalysisRequirementHelper#getRequirementValuesMap(Iterable)}
     * method
     */
    @Test
    public void testGetRequirementValuesMap() {
        Set<IAnalysisRequirementProvider> providers = new HashSet<>();
        providers.add(fTestModuleHelper2);
        providers.add(fTestModuleHelper);

        Multimap<String, String> valuesByType = TmfAnalysisRequirementHelper.getRequirementValuesMap(providers);
        assertFalse(valuesByType.isEmpty());

        /* There should be 3 types */
        assertEquals(3, valuesByType.keySet().size());

        Collection<String> values = valuesByType.get(AnalysisRequirementFactory.REQUIREMENT_TYPE_1);
        assertEquals(4, values.size());
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_1));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_2));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_3));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_5));

        values = valuesByType.get(AnalysisRequirementFactory.REQUIREMENT_TYPE_2);
        assertEquals(5, values.size());
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_1));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_2));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_3));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_4));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_5));

        values = valuesByType.get(AnalysisRequirementFactory.REQUIREMENT_TYPE_3);
        assertEquals(3, values.size());
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_3));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_4));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_5));
    }

    /**
     * Test suite for the
     * {@link TmfAnalysisRequirementHelper#getRequirementValuesMap(Iterable, ValuePriorityLevel)}
     * method
     */
    @Test
    public void testGetRequirementValuesMapWithLevel() {
        Set<IAnalysisRequirementProvider> providers = new HashSet<>();
        providers.add(fTestModuleHelper2);
        providers.add(fTestModuleHelper);

        /* There should be 3 optional requirements types */
        Multimap<String, String> valuesByType = TmfAnalysisRequirementHelper.getRequirementValuesMap(providers, ValuePriorityLevel.OPTIONAL);
        assertTrue(!valuesByType.isEmpty());
        assertEquals(1, valuesByType.keySet().size());

        Collection<String> values = valuesByType.get(AnalysisRequirementFactory.REQUIREMENT_TYPE_1);
        assertTrue(values.isEmpty());

        values = valuesByType.get(AnalysisRequirementFactory.REQUIREMENT_TYPE_2);
        assertEquals(2, values.size());
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_2));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_4));

        values = valuesByType.get(AnalysisRequirementFactory.REQUIREMENT_TYPE_3);
        assertTrue(values.isEmpty());

        /* And 3 types with mandatory requirements */
        valuesByType = TmfAnalysisRequirementHelper.getRequirementValuesMap(providers, ValuePriorityLevel.MANDATORY);
        assertTrue(!valuesByType.isEmpty());
        assertEquals(3, valuesByType.keySet().size());

        values = valuesByType.get(AnalysisRequirementFactory.REQUIREMENT_TYPE_1);
        assertEquals(4, values.size());
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_1));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_2));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_3));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_5));

        values = valuesByType.get(AnalysisRequirementFactory.REQUIREMENT_TYPE_2);
        assertEquals(3, values.size());
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_1));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_3));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_5));

        values = valuesByType.get(AnalysisRequirementFactory.REQUIREMENT_TYPE_3);
        assertEquals(3, values.size());
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_3));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_4));
        assertTrue(values.contains(AnalysisRequirementFactory.REQUIREMENT_VALUE_5));
    }

}
