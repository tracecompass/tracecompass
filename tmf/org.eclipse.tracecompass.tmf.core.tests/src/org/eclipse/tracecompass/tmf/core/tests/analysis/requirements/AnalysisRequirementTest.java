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

package org.eclipse.tracecompass.tmf.core.tests.analysis.requirements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisRequirement.PriorityLevel;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test suite for the {@link TmfAnalysisRequirement} class.
 *
 * @author Guilliano Molaire
 * @author Mathieu Rail
 */
public class AnalysisRequirementTest {

    /* Requirement value name strings */
    private static final @NonNull String VALUE_A = "Test Value A";
    private static final @NonNull String VALUE_B = "Test Value B";

    /* Requirement information strings */
    private static final @NonNull String INFO_A = "This is an information.";
    private static final @NonNull String INFO_B = "This is another information.";

    /**
     * Test the {@link TmfAnalysisRequirement#addInformation} and the
     * {@link TmfAnalysisRequirement#getInformation} methods.
     */
    @Test
    public void testAddAndGetInformation() {
        TmfAnalysisRequirement requirement = new TmfAnalysisRequirement(Collections.EMPTY_SET, PriorityLevel.OPTIONAL);

        requirement.addInformation(INFO_A);
        requirement.addInformation(INFO_B);
        requirement.addInformation(INFO_B);

        Set<String> information = requirement.getInformation();

        assertEquals(2, information.size());

        assertTrue(information.contains(INFO_A));
        assertTrue(information.contains(INFO_B));
    }

    /**
     * Test the {@link TmfAnalysisRequirement#getValueLevel} method.
     */
    @Test
    public void testGetValueLevel() {
        /* Optional requirement */
        TmfAnalysisRequirement requirement = new TmfAnalysisRequirement(Collections.EMPTY_SET, PriorityLevel.OPTIONAL);
        assertEquals(PriorityLevel.OPTIONAL, requirement.getPriorityLevel());

        /* All or nothing */
        requirement = new TmfAnalysisRequirement(Collections.EMPTY_SET, PriorityLevel.ALL_OR_NOTHING);
        assertEquals(PriorityLevel.ALL_OR_NOTHING, requirement.getPriorityLevel());

        /* At least one */
        requirement = new TmfAnalysisRequirement(Collections.EMPTY_SET, PriorityLevel.AT_LEAST_ONE);
        assertEquals(PriorityLevel.AT_LEAST_ONE, requirement.getPriorityLevel());

        /* Mandatory */
        requirement = new TmfAnalysisRequirement(Collections.EMPTY_SET, PriorityLevel.MANDATORY);
        assertEquals(PriorityLevel.MANDATORY, requirement.getPriorityLevel());
    }

    /**
     * Test the {@link TmfAnalysisRequirement#getValues()} method
     */
    @Test
    public void testGetValues() {
        ImmutableSet<@NonNull String> values = ImmutableSet.of(VALUE_A, VALUE_B);
        TmfAnalysisRequirement requirement = new TmfAnalysisRequirement(values, PriorityLevel.OPTIONAL);
        assertEquals(values, requirement.getValues());
    }

}
