/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.requirements;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableSet;

/**
 * This class represents a requirement that a trace must verify for an analysis
 * to be able to run on it. This class is the base class for more specific
 * requirement classes that will implement what to do with the values specified.
 *
 * A requirement has a priority level which will determine how the values will
 * be tested on the trace and what the result should be depending on the
 * presence or absence of those values.
 *
 * Moreover, useful information that can not be leveled with a priority but are
 * important for the proper execution of an analysis can be added.
 *
 * @author Guilliano Molaire
 * @author Mathieu Rail
 * @author Geneviève Bastien
 * @since 2.0
 */
public abstract class TmfAbstractAnalysisRequirement implements Predicate<ITmfTrace> {

    private final Set<@NonNull String> fValues = new HashSet<>();
    private final Set<@NonNull String> fInformation = new HashSet<>();
    private final PriorityLevel fLevel;

    /**
     * The possible level for a requirement.
     */
    public enum PriorityLevel {
        /** The value could be absent and the analysis would still work */
        OPTIONAL,
        /**
         * The values can be absent, but if one is present, then all should be
         * present
         */
        ALL_OR_NOTHING,
        /** At least one of the values must be present at runtime */
        AT_LEAST_ONE,
        /** The value must be present at runtime (for the analysis) */
        MANDATORY
    }

    /**
     * Constructor. Instantiate a requirement object with a list of values of
     * the same level
     *
     * @param values
     *            All the values associated with that type
     * @param level
     *            A level associated with all the values
     */
    public TmfAbstractAnalysisRequirement(Collection<String> values, PriorityLevel level) {
        fLevel = level;
        fValues.addAll(values);
    }

    /**
     * Adds an information about the requirement.
     *
     * @param information
     *            The information to be added
     */
    public void addInformation(String information) {
        fInformation.add(information);
    }

    /**
     * Gets all the values associated with the requirement.
     *
     * @return Set containing the values
     */
    public Set<String> getValues() {
        synchronized (fValues) {
            return ImmutableSet.copyOf(fValues);
        }
    }

    /**
     * Gets information about the requirement.
     *
     * @return The set of all the information
     */
    public Set<@NonNull String> getInformation() {
        return fInformation;
    }

    /**
     * Gets the level of this requirement
     *
     * @return The level of the requirement
     */
    public PriorityLevel getPriorityLevel() {
        return fLevel;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + fValues; //$NON-NLS-1$
    }
}
