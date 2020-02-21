/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement.PriorityLevel;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.AnalysisRequirementFactory;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test suite for the {@link TmfAbstractAnalysisRequirement} class.
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
     * Test the {@link TmfAbstractAnalysisRequirement#addInformation(String)} and the
     * {@link TmfAbstractAnalysisRequirement#getInformation()} methods.
     */
    @Test
    public void testAddAndGetInformation() {
        TmfAbstractAnalysisRequirement requirement = new AnalysisRequirementFactory.TmfRequirementStub(Collections.EMPTY_SET, PriorityLevel.OPTIONAL);
        requirement.addInformation(INFO_A);
        requirement.addInformation(INFO_B);
        requirement.addInformation(INFO_B);

        Set<String> information = requirement.getInformation();

        assertEquals(2, information.size());

        assertTrue(information.contains(INFO_A));
        assertTrue(information.contains(INFO_B));
    }

    /**
     * Test the {@link TmfAbstractAnalysisRequirement#getPriorityLevel()} method.
     */
    @Test
    public void testGetValueLevel() {
        /* Optional requirement */
        TmfAbstractAnalysisRequirement requirement = new AnalysisRequirementFactory.TmfRequirementStub(Collections.EMPTY_SET, PriorityLevel.OPTIONAL);
        assertEquals(PriorityLevel.OPTIONAL, requirement.getPriorityLevel());

        /* All or nothing */
        requirement = new AnalysisRequirementFactory.TmfRequirementStub(Collections.EMPTY_SET, PriorityLevel.ALL_OR_NOTHING);
        assertEquals(PriorityLevel.ALL_OR_NOTHING, requirement.getPriorityLevel());

        /* At least one */
        requirement = new AnalysisRequirementFactory.TmfRequirementStub(Collections.EMPTY_SET, PriorityLevel.AT_LEAST_ONE);
        assertEquals(PriorityLevel.AT_LEAST_ONE, requirement.getPriorityLevel());

        /* Mandatory */
        requirement = new AnalysisRequirementFactory.TmfRequirementStub(Collections.EMPTY_SET, PriorityLevel.MANDATORY);
        assertEquals(PriorityLevel.MANDATORY, requirement.getPriorityLevel());
    }

    /**
     * Test the {@link TmfAbstractAnalysisRequirement#getValues()} method
     */
    @Test
    public void testGetValues() {
        ImmutableSet<@NonNull String> values = ImmutableSet.of(VALUE_A, VALUE_B);

        TmfAbstractAnalysisRequirement requirement = new AnalysisRequirementFactory.TmfRequirementStub(values, PriorityLevel.OPTIONAL);
        assertEquals(values, requirement.getValues());
    }

}
