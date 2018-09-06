/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.requirements;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;

/**
 * This class allows to group requirements together and implement their test for
 * each level
 *
 * @author Geneviève Bastien
 * @since 2.0
 */
public class TmfCompositeAnalysisRequirement extends TmfAbstractAnalysisRequirement {

    private final Collection<TmfAbstractAnalysisRequirement> fSubReqs;

    /**
     * Constructor with sub requirements
     *
     * @param subRequirements
     *            The collection of sub-requirements for this requirement
     * @param level
     *            The level of this requirement
     */
    public TmfCompositeAnalysisRequirement(Collection<TmfAbstractAnalysisRequirement> subRequirements, PriorityLevel level) {
        super(Collections.emptySet(), level);
        fSubReqs = ImmutableList.copyOf(subRequirements);
    }

    @Override
    public boolean test(ITmfTrace trace) {
        Collection<TmfAbstractAnalysisRequirement> subReqs = fSubReqs;
        if (subReqs.isEmpty()) {
            return true;
        }
        // Count the number of requirements testing to true
        long count = subReqs.stream()
                .filter(r -> r.test(trace))
                .count();
        switch (getPriorityLevel()) {
        case ALL_OR_NOTHING:
            return count == subReqs.size() || count == 0;
        case AT_LEAST_ONE:
            return count > 0;
        case MANDATORY:
            return count == subReqs.size();
        case OPTIONAL:
            return true;
        default:
            throw new IllegalStateException("Composite requirement: Unknown value level"); //$NON-NLS-1$
        }
    }

}
