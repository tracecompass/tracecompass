/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Rail - Initial API and implementation
 *   Guilliano Molaire - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Class that contains all the values associated with a type needed by an
 * analysis in order to execute. Each value is peered with a level that
 * determines the importance of that specific value for the requirement.
 *
 * The type gives an indication about the kind of value the requirement
 * contains. The value should depend on the type. For instance, a requirement
 * type could be "event" and all the values that would be added in the
 * requirement object could indicate the possible events handled by the
 * analysis.
 *
 * For these values, a level will be assigned indicating how important the value
 * is based on two possibilities: Mandatory or optional.
 *
 * Moreover, useful information that can not be leveled with a priority but are
 * important for the proper execution of an analysis can be added.
 *
 * @author Guilliano Molaire
 * @author Mathieu Rail
 * @since 3.0
 */
public class TmfAnalysisRequirement {

    private final String fType;
    private final Map<String, ValuePriorityLevel> fValues = new HashMap<>();
    private final Set<String> fInformation = new HashSet<>();

    /**
     * The possible level for each value. They must be listed in ascending order
     * of priority.
     */
    public enum ValuePriorityLevel {
        /** The value could be absent and the analysis would still work */
        OPTIONAL,
        /** The value must be present at runtime (for the analysis) */
        MANDATORY
    }

    /**
     * Constructor
     *
     * @param type
     *            The type of the requirement
     */
    public TmfAnalysisRequirement(String type) {
        fType = type;
    }

    /**
     * Constructor. Instantiate a requirement object with a list of values of
     * the same level
     *
     * @param type
     *            The type of the requirement
     * @param values
     *            All the values associated with that type
     * @param level
     *            A level associated with all the values
     */
    public TmfAnalysisRequirement(String type, Iterable<String> values, ValuePriorityLevel level) {
        fType = type;
        addValues(values, level);
    }

    /**
     * Merges the values of the specified requirement with those of this
     * requirement. All new values will retain their priority value level. If a
     * value was already inside the current requirement, the current priority
     * level will be overridden if the new priority level is higher.
     *
     * @param subRequirement
     *            The requirement to be merged into the current one
     * @param maxSubRequirementValueLevel
     *            The level associated with all the new values or currently
     *            lower priority ones
     * @return True if the merge was successful
     */
    public Boolean merge(TmfAnalysisRequirement subRequirement, ValuePriorityLevel maxSubRequirementValueLevel) {
        /* Two requirements can't be merged if their types are different */
        if (!isSameType(subRequirement)) {
            return false;
        }

        Set<String> values = subRequirement.getValues();
        for (String value : values) {
            /*
             * Sub-requirement value levels are limited to
             * maxSubRequirementValueLevel, so the level associated with the
             * values in the merge is the minimum value between
             * maxSubRequirementValueLevel and its true level.
             */
            int minLevel = Math.min(subRequirement.getValueLevel(value).ordinal(), maxSubRequirementValueLevel.ordinal());
            ValuePriorityLevel subRequirementValueLevel = ValuePriorityLevel.values()[minLevel];

            if (fValues.containsKey(value)) {
                /*
                 * If a value is already in a requirement, we update the level
                 * by the highest value between the current level in the
                 * requirement and the level of the value in the
                 * sub-requirement.
                 */
                ValuePriorityLevel requirementValueLevel = getValueLevel(value);

                int newValueLevel = Math.max(requirementValueLevel.ordinal(), subRequirementValueLevel.ordinal());
                ValuePriorityLevel highestLevel = ValuePriorityLevel.values()[newValueLevel];
                addValue(value, highestLevel);
            }
            else {
                addValue(value, subRequirementValueLevel);
            }
        }

        /* Merge the information */
        fInformation.addAll(subRequirement.getInformation());

        return true;
    }

    /**
     * Adds a list of value inside the requirement with the same level.
     *
     * @param values
     *            A list of value
     * @param level
     *            The level associated with all the values
     */
    public void addValues(Iterable<String> values, ValuePriorityLevel level) {
        for (String value : values) {
            addValue(value, level);
        }
    }

    /**
     * Adds a value with his associated level into the requirement. If the value
     * is already contained in the requirement the method modifies its existing
     * value level.
     *
     * @param value
     *            The value
     * @param level
     *            The level
     */
    public void addValue(String value, ValuePriorityLevel level) {
        synchronized (fValues) {
            fValues.put(value, level);
        }
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
     * Determines if the analysis requirement has the same type of another
     * requirement.
     *
     * @param requirement
     *            Requirement whose type is to be compared to this requirement's
     *            type.
     * @return True if the two requirements have the same type; otherwise false
     */
    public Boolean isSameType(TmfAnalysisRequirement requirement) {
        return fType.equals(requirement.getType());
    }

    /**
     * Gets the requirement type. The type is read only.
     *
     * @return The type of this requirement
     */
    public String getType() {
        return fType;
    }

    /**
     * Gets all the values associated with the requirement.
     *
     * @return Set containing the values
     */
    public Set<String> getValues() {
        synchronized (fValues) {
            return fValues.keySet();
        }
    }

    /**
     * Gets information about the requirement.
     *
     * @return The set of all the information
     */
    public Set<String> getInformation() {
        return fInformation;
    }

    /**
     * Gets the level associated with a particular type
     *
     * @param value
     *            The value
     * @return The level or null if the value does not exist
     */
    public ValuePriorityLevel getValueLevel(String value) {
        synchronized (fValues) {
            return fValues.get(value);
        }
    }
}
