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

package org.eclipse.tracecompass.tmf.tests.stubs.analysis;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisRequirement.PriorityLevel;

import com.google.common.collect.ImmutableSet;

/**
 * Factory class to facilitate requirement usage across test case
 */
@SuppressWarnings("javadoc")
@NonNullByDefault
public final class AnalysisRequirementFactory {

    private AnalysisRequirementFactory() {}

    public static final String REQUIREMENT_VALUE_1 = "value1";
    public static final String REQUIREMENT_VALUE_2 = "value2";
    public static final String REQUIREMENT_VALUE_3 = "value3";
    public static final String REQUIREMENT_VALUE_4 = "value4";
    public static final String REQUIREMENT_VALUE_5 = "value5";

    public static final Set<String> REQUIREMENT_VALUES_1 = ImmutableSet.of(
            REQUIREMENT_VALUE_1,
            REQUIREMENT_VALUE_2,
            REQUIREMENT_VALUE_3,
            REQUIREMENT_VALUE_5
            );

    public static final Set<String> REQUIREMENT_VALUES_2 = ImmutableSet.of(
            REQUIREMENT_VALUE_2,
            REQUIREMENT_VALUE_3
            );

    public static final Set<String> REQUIREMENT_VALUES_3 = ImmutableSet.of(
            REQUIREMENT_VALUE_3,
            REQUIREMENT_VALUE_4,
            REQUIREMENT_VALUE_5
            );

    public static final TmfAnalysisRequirement REQUIREMENT_1 =
            new TmfAnalysisRequirement(REQUIREMENT_VALUES_1, PriorityLevel.MANDATORY);
    public static final TmfAnalysisRequirement REQUIREMENT_2 =
            new TmfAnalysisRequirement(REQUIREMENT_VALUES_2, PriorityLevel.MANDATORY);
    public static final TmfAnalysisRequirement REQUIREMENT_3 =
            new TmfAnalysisRequirement(REQUIREMENT_VALUES_3, PriorityLevel.MANDATORY);

}
