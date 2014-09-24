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
 *   Mathieu Rail - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement.ValuePriorityLevel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test suite for the {@link TmfAnalysisRequirement} class.
 *
 * @author Guilliano Molaire
 * @author Mathieu Rail
 */
public class AnalysisRequirementTest {

    /* Requirements used in the tests */
    private TmfAnalysisRequirement fRequirement;
    private TmfAnalysisRequirement fSubRequirement;

    /* Types of requirement type strings */
    private static final String TYPE_A = "Test Type A";
    private static final String TYPE_B = "Test Type B";

    /* Requirement value name strings */
    private static final String VALUE_A = "Test Value A";
    private static final String VALUE_B = "Test Value B";
    private static final String VALUE_C = "Test Value C";
    private static final String VALUE_D = "Test Value D";
    private static final String VALUE_E = "Test Value E";
    private static final String VALUE_F = "Test Value F";

    /* Requirement information strings */
    private static final String INFO_A = "This is an information.";
    private static final String INFO_B = "This is another information.";
    private static final String INFO_C = "This is the last information.";

    /**
     * Test suite for the {@link TmfAnalysisRequirement#addInformation} and the
     * {@link TmfAnalysisRequirement#getInformation} methods.
     */
    @Test
    public void testAddAndGetInformation() {
        fRequirement = new TmfAnalysisRequirement(TYPE_A);

        fRequirement.addInformation(INFO_A);
        fRequirement.addInformation(INFO_B);
        fRequirement.addInformation(INFO_B);

        Set<String> information = fRequirement.getInformation();

        assertEquals(2, information.size());

        assertTrue(information.contains(INFO_A));
        assertTrue(information.contains(INFO_B));
    }

    /**
     * Test suite for the {@link TmfAnalysisRequirement#addValues} and the
     * {@link TmfAnalysisRequirement#addValue} methods.
     */
    @Test
    public void testAddValuesToRequirement() {
        fRequirement = new TmfAnalysisRequirement(TYPE_A);

        assertEquals(0, fRequirement.getValues().size());

        List<String> values = new ArrayList<>();
        values.add(VALUE_A);
        values.add(VALUE_B);
        values.add(VALUE_C);
        values.add(VALUE_C);

        /*
         * Add values to the requirement with the same level, Value C should
         * only exist once
         */
        fRequirement.addValues(values, ValuePriorityLevel.MANDATORY);
        assertEquals(3, fRequirement.getValues().size());

        /*
         * The new value should only exist once and its level should be
         * mandatory
         */
        fRequirement.addValue(VALUE_D, ValuePriorityLevel.OPTIONAL);
        fRequirement.addValue(VALUE_D, ValuePriorityLevel.MANDATORY);

        assertEquals(4, fRequirement.getValues().size());
        assertEquals(ValuePriorityLevel.MANDATORY, fRequirement.getValueLevel(VALUE_D));
    }

    /**
     * Test suite for the {@link TmfAnalysisRequirement#getValueLevel} method.
     */
    @Test
    public void testGetValueLevel() {
        fRequirement = new TmfAnalysisRequirement(TYPE_A);
        fRequirement.addValue(VALUE_A, ValuePriorityLevel.OPTIONAL);

        /* Try to get the level of a value */
        assertEquals(ValuePriorityLevel.OPTIONAL, fRequirement.getValueLevel(VALUE_A));

        /* Try to get the level of a value that doesn't exist */
        assertNull(fRequirement.getValueLevel(VALUE_B));
    }

    /**
     * Test suite for the {@link TmfAnalysisRequirement#merge} method with the
     * parameter value MANDATORY.
     */
    @Test
    public void testMergeMandatory() {
        initMergeRequirements(TYPE_A, TYPE_A);

        assertTrue(fRequirement.merge(fSubRequirement, ValuePriorityLevel.MANDATORY));

        assertEquals(fRequirement.getValues().size(), 6);

        assertEquals(ValuePriorityLevel.MANDATORY, fRequirement.getValueLevel(VALUE_A));
        assertEquals(ValuePriorityLevel.MANDATORY, fRequirement.getValueLevel(VALUE_B));

        assertEquals(ValuePriorityLevel.MANDATORY, fRequirement.getValueLevel(VALUE_C));
        assertEquals(ValuePriorityLevel.OPTIONAL, fRequirement.getValueLevel(VALUE_D));

        assertEquals(ValuePriorityLevel.MANDATORY, fRequirement.getValueLevel(VALUE_E));
        assertEquals(ValuePriorityLevel.OPTIONAL, fRequirement.getValueLevel(VALUE_F));

        Set<String> information = fRequirement.getInformation();

        assertEquals(3, information.size());

        assertTrue(information.contains(INFO_A));
        assertTrue(information.contains(INFO_B));
        assertTrue(information.contains(INFO_C));
    }

    /**
     * Test suite for the {@link TmfAnalysisRequirement#merge} method with the
     * parameter value OPTIONAL.
     */
    @Test
    public void testMergeOptional() {
        initMergeRequirements(TYPE_A, TYPE_A);

        assertTrue(fRequirement.merge(fSubRequirement, ValuePriorityLevel.OPTIONAL));

        assertEquals(6, fRequirement.getValues().size());

        assertEquals(ValuePriorityLevel.MANDATORY, fRequirement.getValueLevel(VALUE_A));
        assertEquals(ValuePriorityLevel.MANDATORY, fRequirement.getValueLevel(VALUE_B));

        assertEquals(ValuePriorityLevel.OPTIONAL, fRequirement.getValueLevel(VALUE_C));
        assertEquals(ValuePriorityLevel.OPTIONAL, fRequirement.getValueLevel(VALUE_D));

        assertEquals(ValuePriorityLevel.OPTIONAL, fRequirement.getValueLevel(VALUE_E));
        assertEquals(ValuePriorityLevel.OPTIONAL, fRequirement.getValueLevel(VALUE_F));

        Set<String> information = fRequirement.getInformation();

        assertEquals(3, information.size());

        assertTrue(information.contains(INFO_A));
        assertTrue(information.contains(INFO_B));
        assertTrue(information.contains(INFO_C));
    }

    /**
     * Test suite for the {@link TmfAnalysisRequirement#merge} method with
     * different requirement types.
     */
    @Test
    public void testMergeDifferentTypes() {
        initMergeRequirements(TYPE_A, TYPE_B);

        assertFalse(fRequirement.merge(fSubRequirement, ValuePriorityLevel.OPTIONAL));
    }

    /**
     * Test suite for the {@link TmfAnalysisRequirement#isSameType} method.
     */
    @Test
    public void testIsSameRequirementType() {
        fRequirement = new TmfAnalysisRequirement(TYPE_A);

        assertTrue(fRequirement.isSameType(new TmfAnalysisRequirement(TYPE_A)));
        assertFalse(fRequirement.isSameType(new TmfAnalysisRequirement(TYPE_B)));
    }

    /**
     * Initialize the requirement and sub-requirement for the merge tests.
     *
     * @param typeA
     *            The type of the first requirement
     * @param typeB
     *            The type of the second requirement
     */
    private void initMergeRequirements(String typeA, String typeB) {
        fRequirement = new TmfAnalysisRequirement(typeA);
        fRequirement.addValue(VALUE_A, ValuePriorityLevel.MANDATORY);
        fRequirement.addValue(VALUE_B, ValuePriorityLevel.MANDATORY);

        fRequirement.addValue(VALUE_C, ValuePriorityLevel.OPTIONAL);
        fRequirement.addValue(VALUE_D, ValuePriorityLevel.OPTIONAL);

        fRequirement.addInformation(INFO_A);
        fRequirement.addInformation(INFO_B);

        /* This sub-requirement will be merged into requirement */
        fSubRequirement = new TmfAnalysisRequirement(typeB);
        fSubRequirement.addValue(VALUE_A, ValuePriorityLevel.MANDATORY);
        fSubRequirement.addValue(VALUE_B, ValuePriorityLevel.OPTIONAL);

        fSubRequirement.addValue(VALUE_C, ValuePriorityLevel.MANDATORY);
        fSubRequirement.addValue(VALUE_D, ValuePriorityLevel.OPTIONAL);

        fSubRequirement.addValue(VALUE_E, ValuePriorityLevel.MANDATORY);
        fSubRequirement.addValue(VALUE_F, ValuePriorityLevel.OPTIONAL);

        fSubRequirement.addInformation(INFO_B);
        fSubRequirement.addInformation(INFO_C);
    }
}
