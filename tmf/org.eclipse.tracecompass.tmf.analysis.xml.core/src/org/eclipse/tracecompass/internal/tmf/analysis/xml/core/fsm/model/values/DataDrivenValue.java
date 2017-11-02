/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.IDataDrivenRuntimeObject;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenMappingGroup;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * The base class for data-driven values
 *
 * @author Geneviève Bastien
 */
public abstract class DataDrivenValue implements IDataDrivenRuntimeObject {

    /**
     * A message indicating the state system hasn't been initialized yet
     */
    protected static final String ILLEGAL_STATE_EXCEPTION_MESSAGE = "The state system hasn't been initialized yet"; //$NON-NLS-1$

    private final @Nullable String fMappingGroupId;

    private final Type fForcedType;

    /**
     * Constructor
     *
     * @param mappingGroupId
     *            The ID of the mapping group to use to map the retrieved value to
     *            another value
     * @param forcedType
     *            The desired type of the value
     */
    public DataDrivenValue(@Nullable String mappingGroupId, ITmfStateValue.Type forcedType) {
        fMappingGroupId = mappingGroupId;
        fForcedType = forcedType;
    }

    /**
     * Resolve the value with the analysis data.
     *
     * @param baseQuark
     *            The quark for this value
     * @param container
     *            The analysis data container
     * @return The resolved value
     */
    protected abstract @Nullable Object resolveValue(int baseQuark, IAnalysisDataContainer container);

    /**
     * Resolve the value, using an event
     *
     * @param event
     *            The event being handled
     * @param baseQuark
     *            The quark for this value
     * @param scenarioInfo
     *            The active scenario details
     * @param container
     *            The analysis data container
     * @return The value resolved for the event
     */
    protected abstract @Nullable Object resolveValue(ITmfEvent event, int baseQuark, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container);

    /**
     * Get the value this data-driven value resolves to, possibly using an event
     *
     * @param event
     *            The event being handled. If there is no event is available, use
     *            <code>null</code>.
     * @param baseQuark
     *            The quark for this value
     * @param scenarioInfo
     *            The active scenario details. The value should be null if there no
     *            scenario.
     * @param container
     *            The analysis data container
     * @return The value resolved for the event
     */
    public final @Nullable Object getValue(@Nullable ITmfEvent event, int baseQuark, @Nullable DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        // Resolve the value
        Object resolvedValue = null;
        if (event == null && scenarioInfo == null) {
            resolvedValue = resolveValue(baseQuark, container);
        } else if (event == null || scenarioInfo == null) {
            throw new NullPointerException("event and scenarioInfo should not be null. Yet one of them is..."); //$NON-NLS-1$
        } else {
            resolvedValue = resolveValue(event, baseQuark, scenarioInfo, container);
        }

        // Map the resolved value using a mapping group
        if (fMappingGroupId != null) {
            DataDrivenMappingGroup mappingGroup = container.getMappingGroup(fMappingGroupId);
            resolvedValue = mappingGroup.map(event, baseQuark, scenarioInfo, container, resolvedValue);
        }

        // Set the type of the value if a forced type is requested
        if (fForcedType != Type.NULL) {
            resolvedValue = TmfXmlUtils.newTmfStateValueFromObjectWithForcedType(resolvedValue, fForcedType).unboxValue();
        }
        return resolvedValue;
    }

    @Override
    public String toString() {
        return "TmfXmlValue: " + getClass().getSimpleName(); //$NON-NLS-1$
    }

}
