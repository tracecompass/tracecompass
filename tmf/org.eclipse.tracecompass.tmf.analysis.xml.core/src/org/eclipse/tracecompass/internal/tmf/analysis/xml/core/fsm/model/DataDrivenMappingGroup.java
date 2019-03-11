/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

import com.google.common.collect.ImmutableSet;

/**
 * A data-driven mapping group, that maps values in the analysis to some other
 * value.
 *
 * @author Jean-Christian Kouame
 * @author Geneviève Bastien
 */
public class DataDrivenMappingGroup implements IDataDrivenRuntimeObject {

    /**
     * @author gbastien
     *
     */
    /**
     * A data-driven mapping group entry
     */
    public static class DataDrivenMappingEntry {
        private final DataDrivenValue fKey;
        private final DataDrivenValue fValue;

        /**
         * Constructor
         *
         * @param key
         *            The value acting as the key
         * @param value
         *            The value the key maps to
         */
        public DataDrivenMappingEntry(DataDrivenValue key, DataDrivenValue value) {
            fKey = key;
            fValue = value;
        }

        /**
         * Get the key this entry represents
         *
         * @return The data-driven value for the key
         */
        public DataDrivenValue getKey() {
            return fKey;
        }

        /**
         * Get the value this entry maps to
         *
         * @return The data-driven value for the value this entry maps to
         */
        public DataDrivenValue getValue() {
            return fValue;
        }
    }

    private final Collection<DataDrivenMappingEntry> fEntries;
    private final String fId;

    /**
     * Constructor
     *
     * @param id
     *            The ID of the mapping group
     * @param entries
     *            The entries
     */
    public DataDrivenMappingGroup(String id, Collection<DataDrivenMappingEntry> entries) {
        fEntries = ImmutableSet.copyOf(entries);
        fId = id;
    }

    /**
     * Get the ID of this mapping group
     *
     * @return The ID of the mapping group
     */
    public String getId() {
        return fId;
    }

    /**
     * Map the value of
     *
     * @param event
     *            The event to use in state value. Can be <code>null</code> if this
     *            mapping is not done at execution time
     * @param baseQuark
     *            The base quark at which to start mapping the values
     * @param scenarioInfo
     *            The scenario info. Can be <code>null</code> if this mapping is not
     *            done at execution time
     * @param container
     *            The analysis data container
     * @param resolvedValue
     *            The value to map. It will be compared with the keys of the mapping
     * @return The mapped value or <code>null</code> if the resolved value mapped
     *         with no key
     */
    public @Nullable Object map(@Nullable ITmfEvent event, int baseQuark, @Nullable DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container, @Nullable Object resolvedValue) {
        for (DataDrivenMappingEntry entry : fEntries) {
            Object value = entry.getKey().getValue(event, baseQuark, scenarioInfo, container);
            if (resolvedValue == value ||
                    (resolvedValue != null && resolvedValue.equals(value))) {
                return entry.getValue().getValue(event, baseQuark, scenarioInfo, container);
            }
        }
        return null;
    }

}
