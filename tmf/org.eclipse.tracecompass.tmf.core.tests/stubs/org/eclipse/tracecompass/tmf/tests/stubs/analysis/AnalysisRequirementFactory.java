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
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.analysis;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement.PriorityLevel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableSet;

/**
 * Factory class to facilitate requirement usage across test case
 */
@SuppressWarnings("javadoc")
@NonNullByDefault
public final class AnalysisRequirementFactory {

    private AnalysisRequirementFactory() {
    }

    public static final String REQUIREMENT_VALUE_1 = "value1";
    public static final String REQUIREMENT_VALUE_2 = "value2";
    public static final String REQUIREMENT_VALUE_3 = "value3";
    public static final String REQUIREMENT_VALUE_4 = "value4";
    public static final String REQUIREMENT_VALUE_5 = "value5";

    public static final Set<String> REQUIREMENT_VALUES_1 = ImmutableSet.of(
            REQUIREMENT_VALUE_1,
            REQUIREMENT_VALUE_2,
            REQUIREMENT_VALUE_3,
            REQUIREMENT_VALUE_5);

    public static final Set<String> REQUIREMENT_VALUES_2 = ImmutableSet.of(
            REQUIREMENT_VALUE_2,
            REQUIREMENT_VALUE_3);

    public static final Set<String> REQUIREMENT_VALUES_3 = ImmutableSet.of(
            REQUIREMENT_VALUE_3,
            REQUIREMENT_VALUE_4,
            REQUIREMENT_VALUE_5);

    public static class TmfRequirementStub extends TmfAbstractAnalysisRequirement {

        public TmfRequirementStub(Collection<@NonNull String> values, PriorityLevel level) {
            super(values, level);
        }

        @Override
        @NonNullByDefault({})
        public boolean test(ITmfTrace arg0) {
            return true;
        }
    }

    public static final TmfAbstractAnalysisRequirement REQUIREMENT_1 = new TmfRequirementStub(REQUIREMENT_VALUES_1, PriorityLevel.MANDATORY);
    public static final TmfAbstractAnalysisRequirement REQUIREMENT_2 = new TmfRequirementStub(REQUIREMENT_VALUES_2, PriorityLevel.MANDATORY);
    public static final TmfAbstractAnalysisRequirement REQUIREMENT_3 = new TmfRequirementStub(REQUIREMENT_VALUES_3, PriorityLevel.MANDATORY);

}
