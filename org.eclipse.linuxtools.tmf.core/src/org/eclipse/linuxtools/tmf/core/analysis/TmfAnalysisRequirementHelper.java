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

package org.eclipse.linuxtools.tmf.core.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement.ValuePriorityLevel;

/**
 * Helper class to simplify analysis requirement management.
 *
 * @author Guilliano Molaire
 * @since 3.0
 */
public final class TmfAnalysisRequirementHelper {

    /**
     * Private constructor. The class should not be instantiated.
     */
    private TmfAnalysisRequirementHelper() {
    }

    /**
     * Gets the requirement values of a given type from an analysis requirement
     * provider. Only values linked to the type will be returned.
     *
     * @param provider
     *            The analysis requirement provider
     * @param type
     *            The type of the requirement values we need
     * @return The list of values for the specified type
     */
    public static Set<String> getRequirementValues(IAnalysisRequirementProvider provider, String type) {
        Set<String> values = new HashSet<>();
        for (TmfAnalysisRequirement requirement : provider.getAnalysisRequirements()) {
            if (requirement.getType().equalsIgnoreCase(type)) {
                values.addAll(requirement.getValues());
            }
        }
        return values;
    }

    /**
     * Gets the requirement values of a given type from an analysis requirement
     * provider, with the specified level. Only values associated with that type
     * and level will be returned.
     *
     * @param provider
     *            The analysis requirement provider
     * @param type
     *            The type of the requirement values we need
     * @param level
     *            The priority level of the values to be returned
     * @return The list of values for the specified type
     */
    public static Set<String> getRequirementValues(IAnalysisRequirementProvider provider, String type, ValuePriorityLevel level) {
        Set<String> values = new HashSet<>();
        for (TmfAnalysisRequirement requirement : provider.getAnalysisRequirements()) {
            if (requirement.getType().equalsIgnoreCase(type)) {
                for (String value : requirement.getValues()) {
                    if (requirement.getValueLevel(value) == level) {
                        values.add(value);
                    }
                }
            }
        }
        return values;
    }

    /**
     * Gets a map in which the keys are the types of different requirements and
     * the values represent a set of requirement values linked to that type.
     *
     * @param providers
     *            The set of analysis requirement provider
     * @return A map with the values keyed by type
     */
    public static Map<String, Set<String>> getRequirementValuesMap(Iterable<IAnalysisRequirementProvider> providers) {
        Map<String, Set<String>> valuesByType = new HashMap<>();
        for (IAnalysisRequirementProvider provider : providers) {
            for (TmfAnalysisRequirement requirement : provider.getAnalysisRequirements()) {
                Set<String> values = valuesByType.get(requirement.getType());
                if (values == null) {
                    values = new HashSet<>();
                }
                /* Since it's a set, there will be no duplicate */
                values.addAll(requirement.getValues());
                valuesByType.put(requirement.getType(), values);
            }
        }
        return valuesByType;
    }

    /**
     * Gets a map in which the keys are the types of different requirements and
     * the values represents a list of requirement values linked to that type.
     * We only take values with the same priority level as the argument.
     *
     * All types of requirement needed by the providers will be returned, but
     * the values may be empty if none has the requested priority level.
     *
     * @param providers
     *            The set of analysis requirement provider
     * @param level
     *            The priority level of the values to be returned
     * @return A map with the values keyed by type
     */
    public static Map<String, Set<String>> getRequirementValuesMap(Iterable<IAnalysisRequirementProvider> providers, ValuePriorityLevel level) {
        Map<String, Set<String>> valuesByType = new HashMap<>();
        for (IAnalysisRequirementProvider provider : providers) {
            for (TmfAnalysisRequirement requirement : provider.getAnalysisRequirements()) {
                Set<String> values = valuesByType.get(requirement.getType());
                if (values == null) {
                    values = new HashSet<>();
                }
                /* Since it's a set, there will be no duplicate */
                for (String value : requirement.getValues()) {
                    if (requirement.getValueLevel(value) == level) {
                        values.add(value);
                    }
                }
                valuesByType.put(requirement.getType(), values);
            }
        }
        return valuesByType;
    }
}
